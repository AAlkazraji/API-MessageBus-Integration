package com.example.dash;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;


@RestController
public class MessageController {

    private final int MAX_MESSAGES = 50;
    private List<Integer> answerList = new ArrayList<>();
    private long timeTaken;
    private List<Map<String, Object>> messageBuffer = new ArrayList<>(Collections.nCopies(MAX_MESSAGES, null));
    private int bufferHead = 0;

    @PostMapping("/messages")
    public void receiveMessage(@RequestBody Map<String, Object> message) {
        answerList = (List<Integer>) message.get("answer");
        timeTaken = (int) message.get("time_taken");
        messageBuffer.set(bufferHead, message);
        bufferHead = (bufferHead + 1) % MAX_MESSAGES;
    }

    @GetMapping("/")
    public String displayMessage() {
        StringBuilder html = new StringBuilder();
        html.append("<html>");
        html.append("<head>");
        html.append("<meta http-equiv=\"refresh\" content=\"1\">");
        html.append("</head>");
        html.append("<body>");
        html.append("<h1>Messages Dashboard</h1>");
        html.append("<hr>");
        html.append("<br>");

        for (int i = 0; i < MAX_MESSAGES; i++) {
            Map<String, Object> message = messageBuffer.get((bufferHead - i - 1 + MAX_MESSAGES) % MAX_MESSAGES);
            if (message != null) {
                html.append("<p><strong>Answer List:</strong></p>");
                html.append("<p>" + message.get("answer").toString() + "</p>");
                html.append("<p><strong>Time Taken:</strong></p>");
                html.append("<p>" + message.get("time_taken").toString() + "            : in milliseconds</p>");
                html.append("<hr>");
                html.append("<br>");
            }
        }
        html.append("</body>");
        html.append("</html>");
        return html.toString();
    }
}
