package com.data;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class AddressApiTest {

    @Test
    void getAddressCodeTest() {

        String url = "https://business.juso.go.kr/addrlink/addrLinkApi.do?";
        String keyword = "서울시 노원구 공릉동598-17 HJ빌딩";
        String confmKey = "devU01TX0FVVEgyMDI1MDQwMTA5NTgyNzExNTU5NTc=";

        String request = url + "keyword=" + keyword + "&confmKey=" + confmKey;

        HttpHeaders headers = new HttpHeaders();
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity(request, headers, String.class);

        if (response.getBody() == null) {
            System.out.println("결과 없음");
        }

        if (!response.getBody().contains("<admCd>")) {
            System.out.println("법정동 코드 없음");
            return;
        }
        String addressCode = extractCode(response.getBody());
        System.out.println(addressCode);
    }

    private String extractCode(String response) {

        String startTag = "<admCd>";
        String endTag = "</admCd>";

        int startIndex = response.indexOf(startTag);
        int endIndex = response.indexOf(endTag);

        return response
                .substring(startIndex + startTag.length(), endIndex)
                .replaceAll("[^0-9]", "");
    }
}
