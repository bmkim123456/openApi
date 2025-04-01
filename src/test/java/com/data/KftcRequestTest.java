package com.data;

import org.junit.jupiter.api.Test;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.nio.charset.StandardCharsets;

public class KftcRequestTest {

    @Test
    void kftcRequestTest() throws Exception {

        String crn = "2048187419";
        String prmmiMnno = "2011-서울노원-0713";
        String serviceKey = "QXHPt0cp%2FBAWoWQcvcwS7KIA%2BU5v6fy1AehdhW%2Fey8X%2B8v%2Bs7a%2FuxH%2FVASDc8AN%2FU%2BKkUxOyNe3KSS4Sxyof4Q%3D%3D";
        String url = "https://apis.data.go.kr/1130000/MllBsDtl_2Service/getMllBsInfoDetail_2";

        HttpHeaders headers = new HttpHeaders();


        HttpEntity<String> entity = new HttpEntity<>(headers);
        /*UriComponentsBuilder request = UriComponentsBuilder
                .fromUriString(url)
                .queryParam("ServiceKey", uri)
                .queryParam("pageNo", 1)
                .queryParam("numOfRows", 1)
                .queryParam("crn", crn)
                .queryParam("prmmiMnno", prmmiMnno)
                .encode();*/

        String requestUrl = url + "?serviceKey=" + serviceKey
                + "&pageNo=1"
                + "&numOfRows=1";

        URI apiUrl = new URI(requestUrl);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.GET, entity, String.class);

        String result = new String(response.getBody().getBytes(StandardCharsets.UTF_8));
        System.out.println(result);


        /*RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> responseEntity = restTemplate
                .exchange(requestUrl, HttpMethod.GET, entity, String.class);*/


    }
}
