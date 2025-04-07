package com.data;

import com.data.dto.DataCompileDto;
import com.data.dto.FtcResultDto;
import com.data.service.FtcDataService;
import com.data.util.ExtractData;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@SpringBootTest
@ActiveProfiles("local")
public class ApiDataConvertTest {

    @Autowired
    FtcDataService ftcDataService;

    @Autowired
    ExtractData extractData;

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
                String enrValue = extractData.extractEnr(ftcData.getCrn()).orElse(null);
                if (ObjectUtils.isEmpty(enrValue)) {
                    return Collections.emptyMap();
                }

                // csv 파일에서 주소가 없는 경우 공공데이터 api를 통해 주소 값을 얻을 수도 있으므로 필요한 경우 공공데이터 주소값도 확인
                if (ftcData.getAddress().contains("admCdN/A")) {
                    String addressValue = extractData.extractAddress(ftcData.getCrn()).orElse(null);
                    if (ObjectUtils.isEmpty(addressValue)) {
                        return Collections.emptyMap();
                    }
                    ftcData.setAddress(convertAddrss(addressValue));
                }

                enrMap.put(key, enrValue);
                return enrMap;
            });

            Future<Map<String, String>> addrCodeFutureMap = extractJob.submit(() -> {
                Map<String, String> addrMap = new HashMap<>();
                if (ftcData.getAddress().contains("admCdN/A")) {
                    enrFutureMap.get();
                }

                String address = extractData.extractDistrictCode(ftcData.getAddress()).orElse(null);
                if (ObjectUtils.isEmpty(address)) {
                    addrMap.put(key, null);
                    return addrMap;
                }
                addrMap.put(key, address);
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

        List<DataCompileDto> resultList = new ArrayList<>();

        dataCompileMap.forEach((key, value) -> {
            if (value.getEnr() == null || value.getDistrictCode() == null) {
                return;
            }

            DataCompileDto dto = DataCompileDto
                    .builder()
                    .mailOrderNumber(value.getMailOrderNumber())
                    .companyName(value.getCompanyName())
                    .crn(value.getCrn())
                    .enr(value.getEnr())
                    .districtCode(value.getDistrictCode())
                    .build();
            resultList.add(dto);
        });

        System.out.println("취합 된 데이터 수 : " + resultList.size());
        System.out.println("작업 종료, 시간 : " + LocalDateTime.now());
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
