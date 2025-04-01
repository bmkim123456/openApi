package com.data;

import com.data.dto.FtcResultDto;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

public class FtcCsvFileCheckTest {

    @Test
    void ftcResultDataTest() {
        String[] splitData = getMailOrderSaleData().split("\n");
        String[] header = splitData[0].split(",");

        List<Integer> indexes = indexList(header);
        int mailOrderNumberIdx = indexes.get(0);
        int companyNameIdx = indexes.get(1);
        int crnIdx = indexes.get(2);
        int corpTypeIdx = indexes.get(3);
        int addressIdx = indexes.get(4);
        int roadAddressIdx = indexes.get(5);

        List<FtcResultDto> ftcResultDtoList = new ArrayList<>();

        for (int i = 1; i < splitData.length; i++) {
            String[] value = splitData[i].split(",");
            if (value.length < 4 || ObjectUtils.isEmpty(value[corpTypeIdx])) {
                continue;
            }
            if (!value[corpTypeIdx].equals("법인")) {
                continue;
            }

            String address = value[addressIdx];
            String[] splitAddress = address.split(" ");
            if (value[addressIdx].length() < 3 || splitAddress.length < 4) {
                address = value[roadAddressIdx];
            }
            String finalAddress = extractAddress(address);
            System.out.println(finalAddress);

            FtcResultDto result = FtcResultDto
                    .builder()
                    .mailOrderNumber(value[mailOrderNumberIdx])
                    .companyName(value[companyNameIdx])
                    .crn(value[crnIdx])
                    .address(address)
                    .build();
            ftcResultDtoList.add(result);
        }

        for (FtcResultDto company : ftcResultDtoList) {
            String[] splitAddress = company.getAddress().split(" ");
            if (splitAddress.length < 4) {
                System.out.println(company.getCompanyName());
                System.out.println(company.getAddress());
            }
        }
    }

    private String getMailOrderSaleData() {
        try {
            String city = "서울특별시";
            String district = "노원구";
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

            if (new String(responseBody).contains("<title>오류")) {
                System.out.println("전달할 파일 없음");
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

    private String extractAddress(String address) {
        if (address.contains("null")) {
            return "주소매핑 실패";
        }

        String[] splitAddress = address.split(" ");
        return splitAddress[0] + " " + splitAddress[1] + " " + splitAddress[2] + " " + splitAddress[3];
    }
}
