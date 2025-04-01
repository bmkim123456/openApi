package com.data;

import org.apache.commons.lang3.ObjectUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.nio.charset.StandardCharsets;

public class KftcRequestTest {

    @Test
    void kftcRequestTest() throws Exception {

        String crn = "2178137652";
        String serviceKey = "QXHPt0cp%2FBAWoWQcvcwS7KIA%2BU5v6fy1AehdhW%2Fey8X%2B8v%2Bs7a%2FuxH%2FVASDc8AN%2FU%2BKkUxOyNe3KSS4Sxyof4Q%3D%3D";
        String url = "https://apis.data.go.kr/1130000/MllBsDtl_2Service/getMllBsInfoDetail_2";

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        String requestUrl = url
                + "?serviceKey=" + serviceKey
                + "&pageNo=1"
                + "&numOfRows=1"
                + "&brno=" + crn;

        URI apiUrl = new URI(requestUrl);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.GET, entity, String.class);

        if (ObjectUtils.isEmpty(response.getBody())) {
            System.out.println("Response is empty");
        }

        String result = response.getBody();
        String enr = extractCrno(result);
        String addr = new String(extractAddr(result)
                .getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);

        System.out.println(enr);
        System.out.println(addr);
    }

    private String extractCrno(String response) {
        String startTag = "<crno>";
        String endTag = "</crno>";

        int startIndex = response.indexOf(startTag);
        int endIndex = response.indexOf(endTag);

        return response.substring(startIndex + startTag.length(), endIndex);
    }

    private String extractAddr(String response) {
        String startTag = "<lctnAddr>";
        String endTag = "</lctnAddr>";

        int startIndex = response.indexOf(startTag);
        int endIndex = response.indexOf(endTag);

        return response.substring(startIndex + startTag.length(), endIndex);
    }
}
