package com.data.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
public class ExtractDataImpl implements ExtractData {

    private final ApiSource apiSource;

    public Optional<String> extractEnr(String crn) {
        String enrResponse = apiSource.openDataApiResponse(crn).orElse(null);
        if (ObjectUtils.isEmpty(enrResponse)) {
            log.error("[법인번호 확인 실패] 사업자번호 {} : crno 항목 없음", crn);
            return Optional.empty();
        }

        String crno = extractCrno(enrResponse);
        int enrLength = 13;
        if (ObjectUtils.isEmpty(crno) || crno.contains("N/A") || crno.length() < enrLength) {
            log.error("[법인번호 확인 실패] 사업자번호 {} : 유효하지 않은 crno 값 {}", crn, crno);
            return Optional.empty();
        }

        return Optional.of(crno);
    }

    public Optional<String> extractAddress(String crn) {
        String enrResponse = apiSource.openDataApiResponse(crn).orElse(null);
        if (ObjectUtils.isEmpty(enrResponse)) {
            log.error("[법인번호 확인 실패] 사업자번호 {} : crno 항목 없음", crn);
            return Optional.empty();
        }

        if (enrResponse.contains("<lctnAddr>")) {
            String address = extractAddr(enrResponse);
            if (address.contains("N/A")) {
                return Optional.empty();
            }
            return Optional.of(address);
        }

        log.error("[주소 확인 실패] 사업자번호 {} : lctnAddr 항목 없음", crn);
        return Optional.empty();
    }

    public Optional<String> extractDistrictCode(String address) {
        String addrValue = apiSource.addressApiResponse(address).orElse(null);
        if (ObjectUtils.isEmpty(addrValue)) {
            log.error("[행정구역 코드 확인 실패] 주소 {} : adcm 항목 없음", address);
            return Optional.empty();
        }

        String addrCode = extractAddrCode(addrValue);
        if (addrCode.contains("N/A")) {
            log.error("[행정구역 코드 확인 실패] 주소 {} : adcm 항목 N/A", address);
            return Optional.empty();
        }

        return Optional.of(addrCode);
    }

    private String extractCrno(String response) {
        String startTag = "<crno>";
        String endTag = "</crno>";

        int startIndex = response.indexOf(startTag);
        int endIndex = response.indexOf(endTag);

        return response.substring(startIndex + startTag.length(), endIndex);
    }

    private String extractAddr(String response) {
        String startTag = "<lctnAddr>";
        String endTag = "</lctnAddr>";

        int startIndex = response.indexOf(startTag);
        int endIndex = response.indexOf(endTag);

        return new String(response.substring(startIndex + startTag.length(), endIndex)
                .getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
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

}
