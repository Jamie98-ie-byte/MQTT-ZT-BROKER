package io.axiomatics.mqttpep.broker;

import io.moquette.broker.Server;
import io.moquette.broker.config.MemoryConfig;
import io.moquette.broker.security.IAuthorizatorPolicy;
import io.moquette.interception.AbstractInterceptHandler;
import io.moquette.interception.InterceptHandler;
import io.moquette.interception.messages.InterceptPublishMessage;
import io.netty.buffer.ByteBuf;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;


public class MoquetteServer {

    public static void main(String[] args) throws Exception {
        // Initialize Moquette MQTT Broker
        Server mqttBroker = new Server();
        Properties configProps = new Properties();
        configProps.setProperty("port", "1884");
        configProps.setProperty("host", "0.0.0.0");
        configProps.setProperty("allow_anonymous", "true");
        configProps.setProperty("persistent_qeue_type", "segmented");
        String dataDir = "build/moquette-data-" + System.currentTimeMillis();
        new File(dataDir).mkdirs();
        configProps.setProperty("data_path", dataDir);
        MemoryConfig config = new MemoryConfig(configProps);

        // Inject your custom Authorization Handler
        IAuthorizatorPolicy authorizationPolicy = new MoquettePepXacmlAuthorization(config);

        // Define intercept handlers
        List<InterceptHandler> userHandlers = Arrays.asList(new AbstractInterceptHandler() {
            @Override
            public String getID() {
                return "CustomHandler";
            }

            @Override
            public void onPublish(InterceptPublishMessage message) {
                try {
                    ByteBuf payloadBuf = message.getPayload();
                    byte[] payload;

                    // Handle both heap and direct buffers
                    if (payloadBuf.hasArray()) {
                        payload = payloadBuf.array();
                    } else {
                        payload = new byte[payloadBuf.readableBytes()];
                        payloadBuf.getBytes(payloadBuf.readerIndex(), payload);
                    }

                    // Print the intercepted message
                    System.out.println("Intercepted message: " + message.getTopicName() + " -> " + new String(payload));
                } catch (Exception e) {
                    System.err.println("Error processing published message: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            @Override
            public void onSessionLoopError(Throwable cause) {
                System.err.println("Session loop error: " + cause.getMessage());
                cause.printStackTrace();
            }
        });


        Runnable serverTask = () -> {
            try {
                mqttBroker.startServer(config,  userHandlers, null, null, authorizationPolicy);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };  // Inject authorization policy here

        new Thread(serverTask).run();;
        System.out.println("Server started");
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                System.out.println("Stopping broker");
                 mqttBroker.stopServer();

            }
        });

        System.out.println("Moquette MQTT Broker started with custom authorization policy");

        // Keep the server running
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            mqttBroker.stopServer();
            System.out.println("Moquette MQTT Broker stopped");
        }));
    }

}