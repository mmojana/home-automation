package ch.mojanam.mysensorsthing.mysensors;

import ch.mojanam.mysensorsthing.mysensors.serialprotocol.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MySensorsClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(MySensorsClient.class);

    public MySensorsClient(MySensorsClientBuilder builder) {
        new Thread(() -> {
            int delay = builder.socketInitialRetryDelay;
            while (true) {
                try {
                    LOGGER.info("Connecting to " + builder.hostName + ":" + builder.port);
                    Socket socket = new Socket(builder.hostName, builder.port);
                    LOGGER.info("Connected");
                    delay = builder.socketInitialRetryDelay;
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    in.lines().map(Event::new).forEach(event -> {
                        LOGGER.info("Dispatching event " + event + " to " + builder.listeners.size() + " listeners");
                        builder.listeners.forEach(listener -> {
                            listener.received(event);
                        });
                    });
                } catch (IOException e) {
                    LOGGER.error("Error while reading messages from the gateway", e);
                    try {
                        Thread.sleep(delay);
                        delay = Math.min(builder.socketMaximumRetryDelay, (int) (builder.socketBackoffFactor * delay));
                    } catch (InterruptedException e1) {
                        LOGGER.error("Received interruption", e1);
                        break;
                    }
                }
            }
        }).run();
    }

    public static class MySensorsClientBuilder {
        private String hostName = "localhost";
        private int port = 5003;
        private int socketInitialRetryDelay = 1000;
        private int socketMaximumRetryDelay = 60000;
        private double socketBackoffFactor = 1.5;
        private List<MySensorsEventListener> listeners = new ArrayList<>();

        public MySensorsClientBuilder() {

        }

        public MySensorsClientBuilder withHostName(String hostName) {
            this.hostName = hostName;
            return this;
        }

        public MySensorsClientBuilder withPort(int port) {
            this.port = port;
            return this;
        }

        public MySensorsClientBuilder withSocketInitialRetryDelay(int socketInitialRetryDelay) {
            this.socketInitialRetryDelay = socketInitialRetryDelay;
            return this;
        }

        public MySensorsClientBuilder withSocketMaximumRetryDelay(int socketMaximumRetryDelay) {
            this.socketMaximumRetryDelay = socketMaximumRetryDelay;
            return this;
        }

        public MySensorsClientBuilder withSocketBackoffFactor(double socketBackoffFactor) {
            this.socketBackoffFactor = socketBackoffFactor;
            return this;
        }

        public MySensorsClientBuilder withListener(MySensorsEventListener listener) {
            listeners.add(listener);
            return this;
        }


    }

    public interface MySensorsEventListener {
        void received(Event event);
    }

}
