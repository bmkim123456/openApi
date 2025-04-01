package com.data.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
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

    private final static String FTC_URL = "https://www.ftc.go.kr/www/downloadBizComm.do?atchFileUrl=dataopen&atchFileNm=";
    private final static String KFTC_URL = "https://apis.data.go.kr/1130000/MllBsDtl_2Service/getMllBsInfoDetail_2";
    private final static String ADDRESS_URL = "https://business.juso.go.kr/addrlink/addrLinkApi.do?";

    @Override
    public String ftcResponse(String city, String district) {
        try {
            String requestInfo = "통신판매사업자_" + city + "_" + district + ".csv";
            String requestUrl = FTC_URL + requestInfo;

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
    public String kftcResponse(String crn) {
        String finalCrn = crn.replace("-", "");
        String serviceKey = "QXHPt0cp%2FBAWoWQcvcwS7KIA%2BU5v6fy1AehdhW%2Fey8X%2B8v%2Bs7a%2FuxH%2FVASDc8AN%2FU%2BKkUxOyNe3KSS4Sxyof4Q%3D%3D";

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        String requestUrl = KFTC_URL
                + "?serviceKey=" + serviceKey
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

            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public String addressApiResponse(String address) {
        String confmKey = "devU01TX0FVVEgyMDI1MDQwMTA5NTgyNzExNTU5NTc=";
        String request = ADDRESS_URL + "keyword=" + address + "&confmKey=" + confmKey;

        HttpHeaders headers = new HttpHeaders();
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity(request, headers, String.class);

        if (ObjectUtils.isEmpty(response.getBody())) {
            log.error("주소 결과 없음");
        }

        if (!response.getBody().contains("<admCd>")) {
            System.out.println("법정동 코드 없음");
            return "주소매핑 실패";
        }

        return response.getBody();
    }
}
