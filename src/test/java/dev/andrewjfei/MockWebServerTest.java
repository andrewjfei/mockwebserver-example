package dev.andrewjfei;

import java.io.IOException;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MockWebServerTest {
    private MockWebServer mockWebServer;
    private HttpUrl baseUrl;

    private final Logger LOGGER = LoggerFactory.getLogger(MockWebServerTest.class);
    private final String BASE_PATH = "/api/v1";
    private final String RESPONSE_PAYLOAD = "Hello, World!";

    @BeforeEach
    public void beforeTest() throws IOException {
        mockWebServer = new MockWebServer();

        // Start Mock Web Server
        mockWebServer.start();

        // Setting the REST API base URL
        baseUrl = mockWebServer.url(BASE_PATH);

        LOGGER.info(baseUrl.toString());

        Dispatcher dispatcher = new Dispatcher() {
            @NotNull
            @Override
            public MockResponse dispatch(@NotNull RecordedRequest request) {
                MockResponse mockResponse;

                // Mock Responses
                switch (request.getPath()) {
                    case "/api/v1/hello-world":
                        mockResponse = new MockResponse()
                                .setResponseCode(200)
                                .setBody(RESPONSE_PAYLOAD);
                        break;
                    default:
                        mockResponse = new MockResponse()
                                .setResponseCode(404);
                }

                return mockResponse;
            }
        };

        mockWebServer.setDispatcher(dispatcher);
    }

    @AfterEach
    public void afterTest() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    public void testValidWebServerEndpoint_returns200Response() throws IOException, InterruptedException {
        String uriPath = "/hello-world";

        // Given
        HttpUriRequest request = new HttpGet(baseUrl + uriPath);

        // When
        HttpResponse httpResponse = HttpClientBuilder
                .create()
                .build()
                .execute(request);

        // Then
        String responsePayload = EntityUtils.toString(httpResponse.getEntity());

        assertEquals(HttpStatus.SC_OK, httpResponse.getStatusLine().getStatusCode());
        assertEquals(RESPONSE_PAYLOAD, responsePayload);

        RecordedRequest recordedRequest = mockWebServer.takeRequest();

        assertEquals("GET", recordedRequest.getMethod());
        assertEquals(BASE_PATH + uriPath, recordedRequest.getPath());
    }

    @Test
    public void testInvalidWebServerEndpoint_returns404Response() throws IOException {
        String uriPath = "/invalid";

        // Given
        HttpUriRequest request = new HttpGet(baseUrl + uriPath);

        // When
        HttpResponse httpResponse = HttpClientBuilder
                .create()
                .build()
                .execute(request);

        // Then
        String responsePayload = EntityUtils.toString(httpResponse.getEntity());

        assertEquals(HttpStatus.SC_NOT_FOUND, httpResponse.getStatusLine().getStatusCode());
        assertTrue(responsePayload.isEmpty());
    }
}
