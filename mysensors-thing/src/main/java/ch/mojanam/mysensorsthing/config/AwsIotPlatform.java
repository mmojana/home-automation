package ch.mojanam.mysensorsthing.config;

public class AwsIotPlatform {

    private String clientEndpoint;
    private String rootCaCertFilename;
    private String clientCertFilename;
    private String clientPrivateKeyFilename;

    public String getClientEndpoint() {
        return clientEndpoint;
    }

    public String getRootCaCertFilename() {
        return rootCaCertFilename;
    }

    public String getClientCertFilename() {
        return clientCertFilename;
    }

    public String getClientPrivateKeyFilename() {
        return clientPrivateKeyFilename;
    }
}
