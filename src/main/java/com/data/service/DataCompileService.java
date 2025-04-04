package com.data.service;

import com.data.dto.DataCompileDto;
import com.data.dto.FtcResultDto;
import com.data.util.ApiSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataCompileService {

    private final ApiSource apiSource;

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
                String kftcResponseBody = apiSource.openDataApiResponse(ftcData.getCrn());
                if (ObjectUtils.isEmpty(kftcResponseBody)) {
                    log.error("[법인번호 확인 실패] 기업명 : {} , 응답값 없음", ftcData.getCompanyName());
                    enrMap.put(key, null);
                    return enrMap;
                }
                // csv 파일에서 주소가 없는 경우 공공데이터 api를 통해 주소 값을 얻을 수도 있으므로 필요한 경우 공공데이터 주소값도 확인
                if (ftcData.getAddress().contains("admCdN/A")) {
                    if (extractAddr(kftcResponseBody).contains("N/A")) {
                        log.error("[주소 확인 실패] 기업명 : {} , <admCd> 값 N/A", ftcData.getCompanyName());
                        enrMap.put(key, null);
                        return enrMap;
                    }
                    String address = extractAddr(kftcResponseBody);
                    ftcData.setAddress(convertAddrss(address));
                }

                if (extractCrno(kftcResponseBody).contains("N/A")) {
                    log.error("[법인번호 확인 실패] 기업명 : {} , <crno> 값 N/A", ftcData.getCompanyName());
                    enrMap.put(key, null);
                    return enrMap;
                }
                enrMap.put(key, extractCrno(kftcResponseBody));
                return enrMap;
            });

            Future<Map<String, String>> addrCodeFutureMap = extractJob.submit(() -> {
                Map<String, String> addrMap = new HashMap<>();
                if (ftcData.getAddress().contains("admCdN/A")) {
                    enrFutureMap.get();
                }

                String addressResponseBody = apiSource.addressApiResponse(ftcData.getAddress());
                if (ObjectUtils.isEmpty(addressResponseBody) || extractAddrCode(addressResponseBody).contains("N/A")) {
                    log.error("[주소 확인 실패] 기업명 : {} , 응답값 없음", ftcData.getCompanyName());
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
            throw new RuntimeException("enr, addrCode 변환 작업 도중 실패, 사유 : " + e.getMessage());
        }
        log.info("데이터 취합 완료");
        return dataCompileMap;
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
