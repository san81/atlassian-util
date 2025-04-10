package org.example;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class JiraTicketCreator {
    private final String jiraUrl;
    private final String username;
    private final String apiToken;
    private final RestTemplate restTemplate;

    public JiraTicketCreator(String jiraUrl, String username, String apiToken) {
        this.jiraUrl = jiraUrl;
        this.username = username;
        this.apiToken = apiToken;
        this.restTemplate = new RestTemplate();
    }

    public static void main(String[] args) {

        String jiraUrl = "https://<<your-domain>>.atlassian.net";
        String username = "youruser@email.com";
        String apiToken = "apiKey";

        JiraTicketCreator creator = new JiraTicketCreator(jiraUrl, username, apiToken);

        try {
            for (int i = 0; i < 300000; i++) {
                String ticketKey = creator.createTicket("CEN1",              // Project key
                        "Ticket Summary - " + UUID.randomUUID(),           // Summary
                        RandomStringUtils.randomAlphanumeric(2000), // Description
                        "Task"                  // Issue type
                );
                System.out.println("Created ticket: " + ticketKey);
                if (i % 300 == 0) {
                    Thread.sleep(6000);
                }
            }

        } catch (Exception e) {
            System.out.println("Error creating ticket: " + e.getMessage());
        }
    }

    public String createTicket(String projectKey, String summary, String description, String issueType) {
        String apiEndpoint = jiraUrl + "/rest/api/2/issue/";

        // Create HTTP Headers with Basic Auth
        HttpHeaders headers = new HttpHeaders();
        String auth = username + ":" + apiToken;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        headers.set("Authorization", "Basic " + encodedAuth);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create request body
        Map<String, Object> request = new HashMap<>();
        Map<String, Object> fields = new HashMap<>();
        Map<String, Object> project = new HashMap<>();
        Map<String, Object> issuetype = new HashMap<>();

        project.put("key", projectKey);
        issuetype.put("name", issueType);

        fields.put("project", project);
        fields.put("summary", summary);
        fields.put("description", description);
        fields.put("issuetype", issuetype);


        request.put("fields", fields);

        // Create HTTP entity with headers and body
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        // Make the REST call
        Map<String, Object> response = restTemplate.postForObject(apiEndpoint, entity, Map.class);

        return response.get("key").toString();
    }
}

