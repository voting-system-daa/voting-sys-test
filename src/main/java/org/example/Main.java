package org.example;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.io.IOException;

public class Main {

    private static final int USER_ID_START_POINT = 100_001;

    private static final int USER_ID_END_POINT = 110_000;

    private static final String API_POST_AUTH = "http://35.195.9.1:8080/authenticate";

    private static final String API_POST_NEW_USER = "http://35.195.9.1:8080/users";

    private static final String API_POST_SAVE_VOTE = "http://34.38.120.146:8080/election/save";

    private static final String JSON_AUTH_INPUT_STRING = "{\"idnp\": \"XXXXXXXXXXXXX\", \"password\": \"passYYY\"}";

    private static final String JSON_VOTE_INPUT_STRING = "{\"idnp\": \"XXXXXXXXXXXXX\", \"electionId\": YYY}";

    private static final String JSON_USER_SAVE_INPUT_STRING =
            "{\"idnp\": \"9000000XXXXXX\", " +
                    "\"password\": \"user\", " +
                    "\"email\": \"userX@gmail.com\"," +
                    "\"name\": \"User\", " +
                    "\"surname\": \"Test\", " +
                    "\"isEnabled\": true, " +
                    "\"roles\": [\"User\"]" +
                    "}";

    public static void main(String[] args) throws Exception {
//        generateUsers();

        saveVote();
    }

    private static void generateUsers() throws Exception {
        String adminData = JSON_AUTH_INPUT_STRING
                .replace("XXXXXXXXXXXXX", "2000000000000")
                .replace("passYYY", "admin");
        createNewUsers(retrieveJwtToken(adminData));
    }

    private static void saveVote() throws Exception {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            for (int i = USER_ID_START_POINT; i < USER_ID_END_POINT; i++) {
                String userData = JSON_AUTH_INPUT_STRING
                        .replace("XXXXXXXXXXXXX", "9000000" + i)
                        .replace("passYYY", "user");
                String userToken = retrieveJwtToken(userData);

                String bodyRequest = JSON_VOTE_INPUT_STRING
                        .replace("XXXXXXXXXXXXX", "9000000" + i)
                        .replace("YYY", "1");

                HttpPost post = new HttpPost(API_POST_SAVE_VOTE);
                post.setHeader("Content-Type", "application/json");
                post.setHeader("Authorization", "Bearer " + userToken);

                StringEntity entity = new StringEntity(bodyRequest);
                post.setEntity(entity);

                try (CloseableHttpResponse response = httpClient.execute(post)) {
                    System.out.println("Save user vote with idnp=[" + i + "] with code=[" + response.getCode() + "]");
                }
            }
        }
    }

    private static void createNewUsers(String adminToken) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            for (int i = USER_ID_START_POINT; i < USER_ID_END_POINT; i++) {
                String bodyRequest = JSON_USER_SAVE_INPUT_STRING
                        .replace("XXXXXX", String.valueOf(i))
                        .replace("userX", "user" + i);

                HttpPost post = new HttpPost(API_POST_NEW_USER);
                post.setHeader("Content-Type", "application/json");
                post.setHeader("Authorization", "Bearer " + adminToken);

                StringEntity entity = new StringEntity(bodyRequest);
                post.setEntity(entity);

                try (CloseableHttpResponse response = httpClient.execute(post)) {
                    System.out.println("Inserted user with idnp=[" + i + "] with code=[" + response.getCode() + "]");
                }
            }
        }
    }

    private static String retrieveJwtToken(String userData) throws Exception {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(API_POST_AUTH);
            post.setHeader("Content-Type", "application/json");

            StringEntity entity = new StringEntity(userData);
            post.setEntity(entity);

            try (CloseableHttpResponse response = httpClient.execute(post)) {
                return EntityUtils.toString(response.getEntity());
            }
        }
    }
}