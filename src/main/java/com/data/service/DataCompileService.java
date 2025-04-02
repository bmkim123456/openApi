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
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataCompileService {

    private final ApiSource apiSource;

    public List<DataCompileDto> companyDataList(List<FtcResultDto> ftcDataList) {
        List<DataCompileDto> resultList = new ArrayList<>();
        log.info("enr 및 주소코드 병합");
        for (FtcResultDto ftcData : ftcDataList) {
            String[] enrValue = new String[1];
            String[] addrCodeValue = new String[1];

            String kftcResponseBody = apiSource.openDataApiResponse(ftcData.getCrn());
            if (ObjectUtils.isEmpty(kftcResponseBody)) {
                continue;
            }
            if (ftcData.getAddress().contains("admCdN/A")) {
                if (extractAddr(kftcResponseBody).contains("N/A")) {
                    log.info("기업명 : {} 행정동코드 확인 실패, <admCd> 값 N/A", ftcData.getCompanyName());
                    continue;
                }
                String address = extractAddr(kftcResponseBody);
                ftcData.setAddress(convertAddrss(address));
            }

            try {
                Thread enrJob = new Thread(() -> {
                    if (extractCrno(kftcResponseBody).contains("N/A")) {
                        log.info("기업명 : {} 법인번호 확인 실패, <crno> 값 N/A", ftcData.getCompanyName());
                        enrValue[0] = "Fail";
                        return;
                    }
                    enrValue[0] = extractCrno(kftcResponseBody);
                });

                Thread addrJob = new Thread(() -> {
                    String addressResponseBody = apiSource.addressApiResponse(ftcData.getAddress());
                    if (ObjectUtils.isEmpty(addressResponseBody)) {
                        addrCodeValue[0] = "Fail";
                        return;
                    }
                    if (extractAddrCode(addressResponseBody).contains("N/A")) {
                        log.error("기업명 {} 의 주소 확인 실패, <lctnAddr> 값 N/A", ftcData.getCompanyName());
                        addrCodeValue[0] = "Fail";
                        return;
                    }
                    addrCodeValue[0] = extractAddr(addressResponseBody);
                });

                enrJob.start();
                addrJob.start();

                enrJob.join();
                addrJob.join();

                String enr = enrValue[0];
                String addrCode = addrCodeValue[0];

                if (enr.contains("FAIL") || addrCode.contains("FAIL")) {
                    continue;
                }

                DataCompileDto dataCompileDto = DataCompileDto
                        .builder()
                        .mailOrderNumber(ftcData.getMailOrderNumber())
                        .companyName(ftcData.getCompanyName())
                        .crn(ftcData.getCrn())
                        .enr(enr)
                        .districtCode(addrCode)
                        .build();
                resultList.add(dataCompileDto);
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
        }
        return resultList;
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
        if (ObjectUtils.isEmpty(address) || address.contains("null")) {
            return "admCdN/A";
        }

        String[] splitAddress = address.split(" ");
        return splitAddress[0] + " " + splitAddress[1] + " " + splitAddress[2] + " " + splitAddress[3];
    }

}
