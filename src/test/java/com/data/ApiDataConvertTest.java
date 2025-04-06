package com.data;

import com.data.dto.DataCompileDto;
import com.data.dto.FtcResultDto;
import com.data.service.FtcDataService;
import com.data.util.ApiSource;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@SpringBootTest
@ActiveProfiles("local")
public class ApiDataConvertTest {

    @Autowired
    FtcDataService ftcDataService;

    @Autowired
    ApiSource apiSource;

    @Test
    void compileDataTest() {
        System.out.println("작업 시작, 시간 : " + LocalDateTime.now());
        List<FtcResultDto> ftcDataList = ftcDataService.ftcDataList("서울특별시", "노원구");

        ExecutorService extractJob = Executors.newFixedThreadPool(8);

        Map<String, DataCompileDto> dataCompileMap = new HashMap<>();
        List<Future<Map<String, String>>> enrList = new ArrayList<>();
        List<Future<Map<String, String>>> addrCodeList = new ArrayList<>();

        for (FtcResultDto ftcData : ftcDataList) {
            String key = ftcData.getMailOrderNumber();

            Future<Map<String, String>> enrFutureMap = extractJob.submit(() -> {
                Map<String, String> enrMap = new HashMap<>();
                Optional<String> kftcResponseBody = apiSource.openDataApiResponse(ftcData.getCrn());
                String kftcValue = kftcResponseBody.orElse(null);

                if (ObjectUtils.isEmpty(kftcValue)) {
                    enrMap.put(key, null);
                    return enrMap;
                }
                // csv 파일에서 주소가 없는 경우 공공데이터 api를 통해 주소 값을 얻을 수도 있으므로 필요한 경우 공공데이터 주소값도 확인
                if (ftcData.getAddress().contains("admCdN/A")) {
                    if (kftcValue.contains("N/A")) {
                        System.out.println("기업명 : {} 행정동코드 확인 실패, <admCd> 값 N/A" + ftcData.getCompanyName());
                        enrMap.put(key, null);
                        return enrMap;
                    }
                    String address = extractAddr(kftcValue);
                    ftcData.setAddress(convertAddrss(address));
                }

                if (kftcValue.contains("N/A")) {
                    System.out.println("기업명 : {} 법인번호 확인 실패, <crno> 값 N/A" + ftcData.getCompanyName());
                    enrMap.put(key, null);
                    return enrMap;
                }
                enrMap.put(key, extractCrno(kftcValue));
                return enrMap;
            });

            Future<Map<String, String>> addrCodeFutureMap = extractJob.submit(() -> {
                Map<String, String> addrMap = new HashMap<>();
                if (ftcData.getAddress().contains("admCdN/A")) {
                    enrFutureMap.get();
                }

                String addressResponseBody = apiSource.addressApiResponse(ftcData.getAddress());
                if (ObjectUtils.isEmpty(addressResponseBody)) {
                    addrMap.put(key, null);
                    return addrMap;
                }
                if (extractAddrCode(addressResponseBody).contains("N/A")) {
                    addrMap.put(key, null);
                    return addrMap;
                }
                addrMap.put(key, extractAddrCode(addressResponseBody));
                return addrMap;
            });

            enrList.add(enrFutureMap);
            addrCodeList.add(addrCodeFutureMap);

            DataCompileDto dto = DataCompileDto
                    .builder()
                    .mailOrderNumber(ftcData.getMailOrderNumber())
                    .companyName(ftcData.getCompanyName())
                    .crn(ftcData.getCrn())
                    .build();

            dataCompileMap.put(key, dto);
        }

        try {
            for (Future<Map<String, String>> enrMap : enrList) {
                Map<String, String> result = enrMap.get();

                result.forEach((key, value) -> dataCompileMap.get(key).setEnr(convertEnr(value)));
            }

            for (Future<Map<String, String>> future : addrCodeList) {
                Map<String, String> result = future.get();
                result.forEach((key, value) -> dataCompileMap.get(key).setDistrictCode(value));
            }

        } catch (Exception e) {
            System.out.println("에러 : " + e.getMessage());
        }

        dataCompileMap.forEach((key, value) -> {
            if (value.getEnr() == null || value.getDistrictCode() == null) {
                return;
            }

            System.out.println(key);
            System.out.println(value.getMailOrderNumber());
            System.out.println(value.getCompanyName());
            System.out.println(value.getEnr());
            System.out.println(value.getDistrictCode());
        });
        System.out.println("작업 종료, 시간 : " + LocalDateTime.now());
    }

    private String extractCrno(String response) {
        String startTag = "<crno>";
        String endTag = "</crno>";

        int startIndex = response.indexOf(startTag);
        int endIndex = response.indexOf(endTag);

        return response.substring(startIndex + startTag.length(), endIndex);
    }

    private String extractAddrCode(String response) {
        String startTag = "<admCd>";
        String endTag = "</admCd>";

        int startIndex = response.indexOf(startTag);
        int endIndex = response.indexOf(endTag);

        return response
                .substring(startIndex + startTag.length(), endIndex)
                .replaceAll("[^0-9]", "");
    }

    private String extractAddr(String response) {
        String startTag = "<lctnAddr>";
        String endTag = "</lctnAddr>";

        int startIndex = response.indexOf(startTag);
        int endIndex = response.indexOf(endTag);

        return new String(response.substring(startIndex + startTag.length(), endIndex)
                .getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
    }

    private String convertAddrss(String address) {
        String[] splitAddress = address.split(" ");
        return splitAddress[0] + " " + splitAddress[1] + " " + splitAddress[2] + " " + splitAddress[3];
    }

    private String convertEnr(String enr) {
        if (ObjectUtils.isEmpty(enr) || enr.length() < 13) {
            return null;
        }

        int enrStartIndex = 0;
        int enrMiddleIndex = 6;
        int enrEndIndex = 13;

        return enr.substring(enrStartIndex, enrMiddleIndex) + "-" + enr.substring(enrMiddleIndex, enrEndIndex);
    }

}
