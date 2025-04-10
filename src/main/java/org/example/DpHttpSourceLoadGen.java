package org.example;

import org.springframework.web.client.RestTemplate;

public class DpHttpSourceLoadGen {

    public static void main(String[] args) {
        RestTemplate restTemplate = new RestTemplate();
        int counter = 0;
        while(true) {
            try {
                restTemplate.postForEntity(
                        "http://localhost:2021/log-pipeline/logs",
                        "[{\"key\": "+counter+"}]",
                        String.class
                );
            }catch (Exception e){
                System.out.println(e.getMessage());
            }

            counter++;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
