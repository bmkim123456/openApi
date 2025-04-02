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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataCompileService {

    private final ApiSource apiSource;

    public List<DataCompileDto> companyDataList(List<FtcResultDto> ftcDataList) {
        List<DataCompileDto> resultList = new ArrayList<>();
        log.info("enr 및 주소코드 병합");
        for (FtcResultDto ftcData : ftcDataList) {

            CountDownLatch enrLatch = new CountDownLatch(1);
            CountDownLatch latch = new CountDownLatch(2);

            AtomicReference<String> enrValue = new AtomicReference<>();
            AtomicReference<String> addrCodeValue = new AtomicReference<>();

            Thread enrJob = new Thread(() -> {
                try {
                    String kftcResponseBody = apiSource.openDataApiResponse(ftcData.getCrn());
                    if (ObjectUtils.isEmpty(kftcResponseBody)) {
                        return;
                    }
                    // csv 파일에서 주소가 없는 경우 공공데이터 api를 통해 주소 값을 얻을 수도 있으므로 필요한 경우 공공데이터 주소값도 확인
                    if (ftcData.getAddress().contains("admCdN/A")) {
                        if (extractAddr(kftcResponseBody).contains("N/A")) {
                            log.info("기업명 : {} 행정동코드 확인 실패, <admCd> 값 N/A", ftcData.getCompanyName());
                            return;
                        }
                        String address = extractAddr(kftcResponseBody);
                        ftcData.setAddress(convertAddrss(address));
                    }

                    if (extractCrno(kftcResponseBody).contains("N/A")) {
                        log.info("기업명 : {} 법인번호 확인 실패, <crno> 값 N/A", ftcData.getCompanyName());
                        return;
                    }
                    enrValue.set(extractCrno(kftcResponseBody));
                } catch (Exception e) {
                    log.error("enr 작업 중 오류발생");
                    throw new RuntimeException(e.getMessage());
                } finally {
                    enrLatch.countDown();
                    latch.countDown();
                }
            });

            Thread addrJob = new Thread(() -> {
                try {
                    if (ftcData.getAddress().contains("admCdN/A")) {
                        enrLatch.await();
                    }
                    String addressResponseBody = apiSource.addressApiResponse(ftcData.getAddress());
                    if (ObjectUtils.isEmpty(addressResponseBody)) {
                        return;
                    }
                    if (extractAddrCode(addressResponseBody).contains("N/A")) {
                        return;
                    }
                    addrCodeValue.set(extractAddrCode(addressResponseBody));
                } catch (Exception e) {
                    log.error("addrCode 작업 중 오류발생");
                    throw new RuntimeException(e.getMessage());
                } finally {
                    latch.countDown();
                }
            });

            enrJob.start();
            addrJob.start();

            try {
                latch.await();

                String enr = enrValue.toString();
                String addrCode = addrCodeValue.toString();

                if (ObjectUtils.isEmpty(enr) || ObjectUtils.isEmpty(addrCode)) {
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
        log.info("enr 및 주소코드 병합 완료");
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
        String[] splitAddress = address.split(" ");
        return splitAddress[0] + " " + splitAddress[1] + " " + splitAddress[2] + " " + splitAddress[3];
    }

}
