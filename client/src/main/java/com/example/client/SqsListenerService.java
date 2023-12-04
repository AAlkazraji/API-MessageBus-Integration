package com.example.client;


import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SqsListenerService {

    @Autowired
    private RestTemplateService restTemplateService;

    @Autowired
    private AmazonSQS amazonSqs;

    long startTime=0;
    long endTime =0;
    long duration =0;


    @Scheduled(fixedDelay = 1000)
    public void listenForMessages() {
        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest().withQueueUrl("https://sqs.ap-southeast-2.amazonaws.com/397311419908/SENG400");
        List<Message> messages = amazonSqs.receiveMessage(receiveMessageRequest).getMessages();
        for (Message message : messages) {
            processMessage(message);
        }
    }


    private void processMessage(Message message) {
        String messageBody = message.getBody();
        int question = extractQuestionFromMessage(messageBody);
        if (question > 1000000) {
            System.out.println("Ignoring question with integer value larger than 1000000");
            return;
        }
        List<Integer> answer = calculatePrimeNumbers(question);
//        long timeTaken = measureTimeTaken(() -> calculatePrimeNumbers(question));
        Map<String, Object> answerMap = new HashMap<>();
        answerMap.put("answer", answer);
        answerMap.put("time_taken", duration);
        restTemplateService.postAnswer(answerMap);

        String jsonStr;
        try {
            jsonStr = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(answerMap);
        } catch (JsonProcessingException e) {
            System.err.println("Failed to convert answerMap to JSON string: " + e.getMessage());
            return;
        }
        System.out.println(jsonStr);
    }

    private int extractQuestionFromMessage(String messageBody) {
        try {
            JSONObject jsonMessage = new JSONObject(messageBody);
            int question = jsonMessage.getInt("question");
            return question;
        } catch (JSONException e) {
            throw new RuntimeException("Failed to extract question from message", e);
        }
    }

    private List<Integer> calculatePrimeNumbers(int n) {
        if (n > 1000000) {
            return Collections.emptyList(); // ignore large values
        }
        List<Integer> primes = new ArrayList<>();
        boolean[] isComposite = new boolean[n+1];
        startTime = System.nanoTime(); // Start time
        for (int i = 2; i <= n; i++) {
            if (!isComposite[i]) {
                primes.add(i);
                for (int j = i; j <= n; j += i) {
                    isComposite[j] = true;
                }
            }
        }
        endTime = System.nanoTime();
        duration = (endTime - startTime) / 1000000;
        return primes;
    }

}
