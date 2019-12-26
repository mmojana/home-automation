package ch.mojanam.mysensorsthing.config;

import java.util.Map;

public class Config {

    private AwsIotPlatform awsIotPlatform;
    private MySensorsGateway mySensorsGateway;
    private Map<String, ThingConfig> thingsConfig;

    public AwsIotPlatform getAwsIotPlatform() {
        return awsIotPlatform;
    }

    public MySensorsGateway getMySensorsGateway() {
        return mySensorsGateway;
    }

    public Map<String, ThingConfig> getThingsConfig() {
        return thingsConfig;
    }
}
