package com.data.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class ApiSourceImpl implements ApiSource {

    private final static String FTC_URL = "https://www.ftc.go.kr/www/downloadBizComm.do?atchFileUrl=dataopen&atchFileNm=";

    @Override
    public ResponseEntity<byte[]> ftcRequestUrl(String requestInfo) {

        String requestUrl = FTC_URL + requestInfo;

        HttpHeaders headers = new HttpHeaders();
        headers.add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36");

        HttpEntity<String> entity = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.exchange(
                requestUrl,
                HttpMethod.GET,
                entity,
                byte[].class
        );
    }

    @Override
    public String kftcRequestUrl(String requestInfo) {
        return "ok";
    }
}
