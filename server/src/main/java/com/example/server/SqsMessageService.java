package com.example.server;


import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class SqsMessageService {

    private final AmazonSQS sqsClient;
    private final String queueUrl;
    private int maxQuestionValue;
    private long publishInterval;
    private Thread publishThread;

    @Autowired
    public SqsMessageService(AmazonSQS sqsClient,
                             @Value("${app.queue-url}") String queueUrl,
                             @Value("${app.max-question-value:1000000}") int maxQuestionValue,
                             @Value("${app.publish-interval:1000}") long publishInterval) {
        this.sqsClient = sqsClient;
        this.queueUrl = queueUrl;
        this.maxQuestionValue = maxQuestionValue;
        this.publishInterval = publishInterval;

        // Start the infinite loop thread
        startPublishThread();
    }

    @Scheduled(fixedDelayString = "${app.publish-interval:1000}")
    public void publishMessage() throws InterruptedException {
        int questionValue = new Random().nextInt(maxQuestionValue) + 1;
        String message = String.format("{%n    \"question\": \"%d\"%n}", questionValue);
        SendMessageRequest sendMessageRequest = new SendMessageRequest(queueUrl, message);
        sqsClient.sendMessage(sendMessageRequest);
        System.out.println(message);
    }

    private void startPublishThread() {
        publishThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    publishMessage();
                    Thread.sleep(publishInterval);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Set interrupt flag
                    System.out.println("Thread interrupted while waiting for response.");
                }
            }
        });
        publishThread.start();
    }


    public void stopPublishThread() {
        publishThread.interrupt();
    }

    public void setConfiguration(int maxQuestionValue, long publishInterval) {
        this.maxQuestionValue = maxQuestionValue;
        this.publishInterval = publishInterval;
        stopPublishThread();
        startPublishThread();
    }
}
