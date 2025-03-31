package com.data;

import org.apache.commons.lang3.ObjectUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

public class FileDownloadTest {

    @Test
    void checkData() {
        String[] splitData = getMailOrderSaleData().split("\n");
        String[] header = splitData[0].split(",");

        List<Integer> indexes = indexList(header);
        List<String> companyData = new ArrayList<>();

        for (int i = 1; i < splitData.length; i++) {
            String[] value = splitData[i].split(",");
            if (value[indexes.get(3)].equals("개인")) {
                continue;
            }

            String crn = value[indexes.get(2)].replace("-", "");
            companyData.add(value[indexes.get(0)]);
            companyData.add(value[indexes.get(1)]);
            companyData.add(crn);
            companyData.add(value[indexes.get(3)]);
            companyData.add(value[indexes.get(4)]);
            companyData.add(value[indexes.get(5)]);
        }

        for (String company : companyData) {
            System.out.println(company);
        }
    }

    private String getMailOrderSaleData() {
        try {
            String city = "서울특별시";
            String district = "도봉구";
            String url = "통신판매사업자_" + city + "_" + district + ".csv";

            String download = "https://www.ftc.go.kr/www/downloadBizComm.do?atchFileUrl=dataopen&atchFileNm=" + url;

            HttpHeaders headers = new HttpHeaders();
            headers.add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    download,
                    HttpMethod.GET,
                    entity,
                    byte[].class
            );

            byte[] responseBody = response.getBody();
            if (ObjectUtils.isEmpty(responseBody)) {
                throw new RuntimeException("데이터가 없습니다");
            }

            return new String(responseBody, "EUC-KR");
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private List<Integer> indexList(String[] header) {
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < header.length; i++) {
            String headerValue = header[i];
            if (headerValue.equals("통신판매번호")) {
                result.add(i);
            }
            if (headerValue.equals("상호")) {
                result.add(i);
            }
            if (headerValue.equals("사업자등록번호")) {
                result.add(i);
            }
            if (headerValue.equals("법인여부")) {
                result.add(i);
            }
            if (headerValue.equals("사업장소재지")) {
                result.add(i);
            }
            if (headerValue.equals("사업장소재지(도로명)")) {
                result.add(i);
            }
        }
        return result;
    }
}
