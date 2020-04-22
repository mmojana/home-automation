package ch.mojanam.shadowtoinflux;

import ch.mojanam.shadowtoinflux.clients.InfluxClient;
import ch.mojanam.shadowtoinflux.entities.ShadowReportedStateEvent;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.google.gson.Gson;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Base64;
import java.util.Date;
import java.util.stream.Collectors;

public class ConverterFunction implements RequestStreamHandler {

    private static final Gson GSON = new Gson();
    private static final String METRIC_SEPARATOR = " ";
    private static final String INFLUX_BASE_URL = "INFLUX_BASE_URL";
    private static final String INFLUX_ORGANIZATION = "INFLUX_ORGANIZATION";
    private static final String INFLUX_BUCKET = "INFLUX_BUCKET";
    private static final String INFLUX_TOKEN_SECRET_REGION = "INFLUX_TOKEN_SECRET_REGION";
    private static final String INFLUX_TOKEN_SECRET_NAME = "INFLUX_TOKEN_SECRET_NAME";
    private static final String PRECISION_MILLISECONDS = "ms";
    private static final String AUTH_TYPE_TOKEN = "Token ";

    public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
        ShadowReportedStateEvent shadowReportedStateEvent = GSON.fromJson(new InputStreamReader(input), ShadowReportedStateEvent.class);

        String metric = buildMetric(shadowReportedStateEvent);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(System.getenv(INFLUX_BASE_URL))
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        InfluxClient influxClient = retrofit.create(InfluxClient.class);

        String token = getSecret(System.getenv(INFLUX_TOKEN_SECRET_REGION), System.getenv(INFLUX_TOKEN_SECRET_NAME));
        influxClient.write(System.getenv(INFLUX_ORGANIZATION),
                System.getenv(INFLUX_BUCKET),
                PRECISION_MILLISECONDS,
                AUTH_TYPE_TOKEN + token,
                metric)
                .execute();

    }

    private String buildMetric(ShadowReportedStateEvent shadowReportedStateEvent) {
        StringBuilder metricBuilder = new StringBuilder();
        metricBuilder.append(shadowReportedStateEvent.getThingName());
        metricBuilder.append(METRIC_SEPARATOR);
        metricBuilder.append(shadowReportedStateEvent.getReportedState().entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining(",")));
        metricBuilder.append(METRIC_SEPARATOR);
        metricBuilder.append(new Date().getTime());
        return metricBuilder.toString();
    }

    private String getSecret(String region, String secretName) {
        AWSSecretsManager client = AWSSecretsManagerClientBuilder.standard()
                .withRegion(region)
                .build();

        GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest()
                .withSecretId(secretName);

        GetSecretValueResult getSecretValueResult = client.getSecretValue(getSecretValueRequest);

        if (getSecretValueResult.getSecretString() != null) {
            return getSecretValueResult.getSecretString();
        } else {
            return new String(Base64.getDecoder().decode(getSecretValueResult.getSecretBinary()).array());
        }
    }
}
