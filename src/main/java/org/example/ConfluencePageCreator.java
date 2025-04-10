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

public class ConfluencePageCreator {
    private final String confluenceUrl;
    private final String username;
    private final String apiToken;
    private final RestTemplate restTemplate;

    public ConfluencePageCreator(String confluenceUrl, String username, String apiToken) {
        this.confluenceUrl = confluenceUrl;
        this.username = username;
        this.apiToken = apiToken;
        this.restTemplate = new RestTemplate();
    }

    public static void main(String[] args) {
        String confluenceUrl = "https://<<your-domain>>.atlassian.net";
        String username = "youruser@email.com";
        String apiToken = "apiKey";

        ConfluencePageCreator creator = new ConfluencePageCreator(confluenceUrl, username, apiToken);
        try {
            for (int i = 0; i < 300000; i++) {
                String ticketKey = creator.createPage(
                        "KB",              // Project key
                        "Page Title - " + i,           // Summary
                        generateSimpleHtml(), // Description
                        "Task"                  // Issue type
                );
                System.out.println("Created page: " + ticketKey);
                if (i % 300 == 0) {
                    Thread.sleep(6000);
                }
            }

        } catch (Exception e) {
            System.err.println("Error creating page: " + e.getMessage());
        }
    }

    public static String generateSimpleHtml() {
        StringBuilder html = new StringBuilder()
                .append("<!DOCTYPE html>\n")
                .append("<html>\n")
                .append("<head><title>Random Page " + UUID.randomUUID() + "</title></head>\n")
                .append("<body>\n")
                .append("<h1>Random Content</h1>\n")
                .append("<p>" + RandomStringUtils.randomAlphanumeric(2000) + "</p>\n")
                .append("</body>\n")
                .append("</html>");
        return html.toString();
    }

    public String createPage(String spaceKey, String summary, String description, String pageType) {

        /**
         * curl -u admin:admin -X POST -H 'Content-Type: application/json' -d '{"type":"page","title":"new page",
         * "space":{"key":"TST"},"body":{"storage":{"value":"<p>This is <br/> a new page</p>","representation":
         * "storage"}}}' http://localhost:8080/confluence/rest/api/content/ | python -mjson.tool
         */

        String apiEndpoint = confluenceUrl + "/wiki/rest/api/content/";

        // Create HTTP Headers with Basic Auth
        HttpHeaders headers = new HttpHeaders();
        String auth = username + ":" + apiToken;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        headers.set("Authorization", "Basic " + encodedAuth);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create request body
        Map<String, Object> request = new HashMap<>();
        Map<String, Object> fields = new HashMap<>();
        Map<String, Object> space = new HashMap<>();
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("storage", new HashMap<String, Object>() {{
            put("value", description);
            put("representation", "storage");
        }});

        space.put("key", spaceKey);

        fields.put("type", "page");
        fields.put("title", summary);
        fields.put("space", space);
        fields.put("body", bodyMap);


        request.put("fields", fields);

        // Create HTTP entity with headers and body
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(fields, headers);

        // Make the REST call
        Map<String, Object> response = restTemplate.postForObject(
                apiEndpoint,
                entity,
                Map.class
        );

        return response.get("id").toString();
    }
}

