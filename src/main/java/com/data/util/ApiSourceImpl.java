package com.data.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@Slf4j
@Component
public class ApiSourceImpl implements ApiSource {

    @Value("${ftc.url}")
    private String ftcUrl;

    @Value("${openApi.url}")
    private String openApiUrl;

    @Value("${openApi.key}")
    private String openApiKey;

    @Value("${addressApi.url}")
    private String addressApiUrl;

    @Value("${addressApi.key}")
    private String addressApiKey;

    @Override
    public String ftcResponse(String city, String district) {
        try {
            String requestInfo = "통신판매사업자_" + city + "_" + district + ".csv";
            String requestUrl = ftcUrl + requestInfo;

            HttpHeaders headers = new HttpHeaders();
            headers.add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36");

            HttpEntity<String> entity = new HttpEntity<>(headers);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<byte[]> response = restTemplate
                    .exchange(requestUrl, HttpMethod.GET, entity, byte[].class);

            byte[] responseBody = response.getBody();
            if (ObjectUtils.isEmpty(responseBody)) {
                throw new RuntimeException("데이터가 없습니다");
            }

            if (new String(responseBody).contains("<title>오류")) {
                log.error("전달할 파일이 없습니다. 시/도, 군/구 입력이 정확한지 확인 해주세요");
                throw new RuntimeException("지역 이름이 정확하지 않습니다.");
            }

            return new String(responseBody, "EUC-KR");
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public String openDataApiResponse(String crn) {
        String finalCrn = crn.replace("-", "");

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        String requestUrl = openApiUrl
                + "?serviceKey=" + openApiKey
                + "&pageNo=1"
                + "&numOfRows=1"
                + "&brno=" + finalCrn;

        try {
            URI apiUrl = new URI(requestUrl);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.GET, entity, String.class);

            if (ObjectUtils.isEmpty(response.getBody())) {
                log.info("enr response is empty");
            }

            if (response.getBody().contains("<crno>")) {
                return response.getBody();
            }

            log.error("사업자 번호 {} 법인번호 찾기 실패, <crno> 항목 없음", crn);
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public String addressApiResponse(String address) {
        String request = addressApiUrl + "keyword=" + address + "&confmKey=" + addressApiKey;

        try {
            HttpHeaders headers = new HttpHeaders();
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.postForEntity(request, headers, String.class);

            if (ObjectUtils.isEmpty(response.getBody())) {
                log.error("주소 결과 없음");
                return null;
            }

            if (response.getBody().contains("<admCd>")) {
                return response.getBody();
            }

            log.error("주소 {} 에 대한 행정구역 코드 찾기 실패, <admCd> 항목 없음", address);
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
