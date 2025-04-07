package com.data.service;

import com.data.dto.DataCompileDto;
import com.data.dto.FtcResultDto;
import com.data.util.ExtractData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataCompileService {

    private final ExtractData extractData;

    public Map<String, DataCompileDto> dataCompileDtoMap(List<FtcResultDto> ftcDataList) {
        log.info("데이터 취합 시작");
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
                    String address = extractData.extractAddress(ftcData.getCrn()).orElse(null);
                    if (ObjectUtils.isEmpty(address)) {
                        return Collections.emptyMap();
                    }
                    ftcData.setAddress(convertAddrss(address));
                }
                enrMap.put(key, enrValue);
                return enrMap;
            });

            Future<Map<String, String>> addrCodeFutureMap = extractJob.submit(() -> {
                Map<String, String> addrMap = new HashMap<>();
                if (ftcData.getAddress().contains("admCdN/A")) {
                    enrFutureMap.get();
                }
                String districtCode = extractData.extractDistrictCode(ftcData.getAddress()).orElse(null);
                if (ObjectUtils.isEmpty(districtCode)) {
                    return Collections.emptyMap();
                }
                return Collections.emptyMap();
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
        } catch (ExecutionException | InterruptedException e) {
            log.error("병합 작업 실행 중 예외 발생, 사유 : {}", e.getMessage());
            throw new RuntimeException("enr, addrCode 변환 작업 도중 실패", e.getCause());
        } finally {
            extractJob.shutdown();
        }
        log.info("데이터 취합 완료");
        return dataCompileMap;
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
