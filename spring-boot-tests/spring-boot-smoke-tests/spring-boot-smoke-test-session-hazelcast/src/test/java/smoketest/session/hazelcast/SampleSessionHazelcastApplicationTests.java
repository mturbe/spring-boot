/*
 * Copyright 2012-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package smoketest.session.hazelcast;

import java.net.URI;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link SampleSessionHazelcastApplication},
 *
 * @author Susmitha Kandula
 * @author Madhura Bhave
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SampleSessionHazelcastApplicationTests {

	@Autowired
	private TestRestTemplate restTemplate;

	@LocalServerPort
	private int port;

	@Test
	@SuppressWarnings("unchecked")
	public void sessionsEndpointShouldReturnUserSession() {
		URI uri = URI.create("http://localhost:" + this.port + "/");
		ResponseEntity<String> firstResponse = performRequest(this.restTemplate, uri, null);
		String cookie = firstResponse.getHeaders().getFirst("Set-Cookie");
		performRequest(this.restTemplate, uri, cookie).getBody();
		ResponseEntity<Map<String, Object>> entity = (ResponseEntity<Map<String, Object>>) (ResponseEntity) this.restTemplate
				.withBasicAuth("user", "password").getForEntity("/actuator/sessions?username=user", Map.class);
		assertThat(entity).isNotNull();
		assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
		List<Map<String, Object>> sessions = (List<Map<String, Object>>) entity.getBody().get("sessions");
		assertThat(sessions.size()).isEqualTo(1);
	}

	private ResponseEntity<String> performRequest(TestRestTemplate restTemplate, URI uri, String cookie) {
		HttpHeaders headers = getHeaders(cookie);
		RequestEntity<Object> request = new RequestEntity<>(headers, HttpMethod.GET, uri);
		return restTemplate.exchange(request, String.class);
	}

	private HttpHeaders getHeaders(String cookie) {
		HttpHeaders headers = new HttpHeaders();
		if (cookie != null) {
			headers.set("Cookie", cookie);
		}
		else {
			headers.set("Authorization", getBasicAuth());
		}
		return headers;
	}

	private String getBasicAuth() {
		return "Basic " + Base64.getEncoder().encodeToString("user:password".getBytes());
	}

}
