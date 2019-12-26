package ch.mojanam.mysensorsthing;


import ch.mojanam.mysensorsthing.config.AwsIotPlatform;
import ch.mojanam.mysensorsthing.config.Config;
import ch.mojanam.mysensorsthing.config.MySensorsGateway;
import ch.mojanam.mysensorsthing.mysensors.MySensorsClient;
import ch.mojanam.mysensorsthing.mysensors.serialprotocol.Command;
import ch.mojanam.mysensorsthing.mysensors.serialprotocol.Event;
import ch.mojanam.mysensorsthing.mysensors.serialprotocol.Type;
import com.amazonaws.services.iot.client.AWSIotException;
import com.amazonaws.services.iot.client.AWSIotMqttClient;
import com.google.common.base.CaseFormat;
import com.google.gson.Gson;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.stream.Collectors;

public class Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) throws Exception {
        Config config = readConfig();

        AwsIotPlatform awsIotPlatform = config.getAwsIotPlatform();
        AWSIotMqttClient client = new AWSIotMqttClient(awsIotPlatform.getClientEndpoint(), "client", getSocketFactory(awsIotPlatform.getRootCaCertFilename(), awsIotPlatform.getClientCertFilename(), awsIotPlatform.getClientPrivateKeyFilename(), ""));
        client.connect();

        Map<Integer, String> nodeIdToThingName = config.getThingsConfig().entrySet().stream()
                .collect(Collectors.toMap(e -> e.getValue().getMySensorsNodeId(), Map.Entry::getKey));

        MySensorsClient.MySensorsEventListener mySensorsEventListener = new MySensorsClient.MySensorsEventListener() {
            @Override
            public void received(Event e) {
                if ((e.getCommand() == Command.INTERNAL && e.getType() == Type.I_BATTERY_LEVEL) || e.getCommand() == Command.SET) {
                    String key = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, e.getType().name().substring(2));
                    updateShadow(e.getNodeId(), key, e.getPayload());
                }
            }

            private void updateShadow(Integer nodeId, String key, String value) {
                String thingName = nodeIdToThingName.get(nodeId);
                if (thingName != null) {
                    updateShadow(thingName, key, value);
                }
            }

            private void updateShadow(String thingName, String key, String value) {
                LOGGER.info("Updating shadow of " + thingName + " with key=" + key + " and value=" + value);
                try {
                    String topic = "$aws/things/" + thingName + "/shadow/update";
                    String typeSpecificValue;
                    try {
                        Double.valueOf(value);
                        typeSpecificValue = value;
                    } catch (NumberFormatException nfe) {
                        typeSpecificValue = "\"" + value + "\"";
                    }
                    client.publish(topic, "{\"state\":{\"reported\":{\"" + key + "\": " + typeSpecificValue + "}}}");
                } catch (AWSIotException e) {
                    LOGGER.error("Cannot send shadow update", e);
                }
            }
        };

        MySensorsGateway mySensorsGateway = config.getMySensorsGateway();
        MySensorsClient.MySensorsClientBuilder builder = new MySensorsClient.MySensorsClientBuilder()
                .withHostName(mySensorsGateway.getHostName())
                .withPort(mySensorsGateway.getPort())
                .withSocketInitialRetryDelay(mySensorsGateway.getSocketInitialRetryDelay())
                .withSocketMaximumRetryDelay(mySensorsGateway.getSocketMaximumRetryDelay())
                .withSocketBackoffFactor(mySensorsGateway.getSocketBackoffFactor())
                .withListener(mySensorsEventListener);

        MySensorsClient mySensorsClient = new MySensorsClient(builder);

    }

    private static Config readConfig() throws FileNotFoundException {
        Gson gson = new Gson();
        String configFile = System.getProperty("config.file");
        if (configFile == null) {
            LOGGER.error("No configuration file specified, please add -Dconfig.file=/foo/bar.json");
            throw new FileNotFoundException("No configuration file specified");
        }
        return gson.fromJson(new FileReader(configFile), Config.class);
    }

    // Taken from https://stackoverflow.com/questions/58437531/how-to-fix-javax-net-ssl-sslhandshakeexception-tls-server-certificate-issued-a
    private static SSLSocketFactory getSocketFactory(final String caCrtFile,
                                                     final String crtFile, final String keyFile, final String password)
            throws Exception {
        Security.addProvider(new BouncyCastleProvider());

        // load CA certificate
        X509Certificate caCert = null;

        FileInputStream fis = new FileInputStream(caCrtFile);
        BufferedInputStream bis = new BufferedInputStream(fis);
        CertificateFactory cf = CertificateFactory.getInstance("X.509");

        while (bis.available() > 0) {
            caCert = (X509Certificate) cf.generateCertificate(bis);
        }

        // load client certificate
        bis = new BufferedInputStream(new FileInputStream(crtFile));
        X509Certificate cert = null;
        while (bis.available() > 0) {
            cert = (X509Certificate) cf.generateCertificate(bis);
        }

        // load client private key
        PEMParser pemParser = new PEMParser(new FileReader(keyFile));
        Object object = pemParser.readObject();
        PEMDecryptorProvider decProv = new JcePEMDecryptorProviderBuilder()
                .build(password.toCharArray());
        JcaPEMKeyConverter converter = new JcaPEMKeyConverter()
                .setProvider("BC");
        KeyPair key;
        if (object instanceof PEMEncryptedKeyPair) {
            LOGGER.info("Encrypted key - we will use provided password");
            key = converter.getKeyPair(((PEMEncryptedKeyPair) object)
                    .decryptKeyPair(decProv));
        } else {
            LOGGER.info("Unencrypted key - no password needed");
            key = converter.getKeyPair((PEMKeyPair) object);
        }
        pemParser.close();

        // CA certificate is used to authenticate server
        KeyStore caKs = KeyStore.getInstance(KeyStore.getDefaultType());
        caKs.load(null, null);
        caKs.setCertificateEntry("ca-certificate", caCert);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
        tmf.init(caKs);

        // client key and certificates are sent to server so it can authenticate
        // us
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(null, null);
        ks.setCertificateEntry("certificate", cert);
        ks.setKeyEntry("private-key", key.getPrivate(), password.toCharArray(),
                new java.security.cert.Certificate[]{cert});
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory
                .getDefaultAlgorithm());
        kmf.init(ks, password.toCharArray());

        // finally, create SSL socket factory
        SSLContext context = SSLContext.getInstance("TLSv1.2");
        context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        return context.getSocketFactory();
    }

}
