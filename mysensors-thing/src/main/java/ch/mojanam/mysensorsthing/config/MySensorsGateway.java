package ch.mojanam.mysensorsthing.config;

public class MySensorsGateway {
    private String hostName = "localhost";
    private int port = 5003;
    private Integer socketInitialRetryDelay = 1000;
    private Integer socketMaximumRetryDelay = 60000;
    private Double socketBackoffFactor = 1.5;

    public String getHostName() {
        return hostName;
    }

    public int getPort() {
        return port;
    }

    public Integer getSocketInitialRetryDelay() {
        return socketInitialRetryDelay;
    }

    public Integer getSocketMaximumRetryDelay() {
        return socketMaximumRetryDelay;
    }

    public Double getSocketBackoffFactor() {
        return socketBackoffFactor;
    }
}
