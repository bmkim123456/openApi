package com.data.service;

import com.data.dto.FtcResultDto;
import com.data.util.ApiSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class FtcDataService {

    private final ApiSource apiSource;

    public List<FtcResultDto> ftcDataList(String city, String district) {
        log.info("요청 데이터 : 시/도 : {}, 군/구 : {}", city, district);
        String[] splitData = apiSource.ftcResponse(city, district).split("\n");
        String[] header = splitData[0].split(",");

        log.info("csv 인덱스 생성");
        List<Integer> indexes = indexList(header);
        int mailOrderNumberIdx = indexes.get(0);
        int companyNameIdx = indexes.get(1);
        int crnIdx = indexes.get(2);
        int corpTypeIdx = indexes.get(3);
        int addressIdx = indexes.get(4);
        int roadAddressIdx = indexes.get(5);

        List<FtcResultDto> ftcResultDtoList = new ArrayList<>();

        log.info("추출 데이터 DTO 변환");
        for (int i = 1; i < splitData.length; i++) {
            String[] value = splitData[i].split(",");
            if (value.length < 4 || ObjectUtils.isEmpty(value[corpTypeIdx])) {
                continue;
            }
            if (!value[corpTypeIdx].equals("법인")) {
                continue;
            }

            if (value.length < addressIdx || ObjectUtils.isEmpty(value[addressIdx])) {
                FtcResultDto result = FtcResultDto
                        .builder()
                        .mailOrderNumber(value[mailOrderNumberIdx])
                        .companyName(value[companyNameIdx])
                        .crn(value[crnIdx])
                        .address("admCdN/A")
                        .build();
                ftcResultDtoList.add(result);
                continue;
            }

            String address = value[addressIdx];
            String[] splitAddress = address.split(" ");
            if (value[addressIdx].length() < 3 || splitAddress.length < 4) {
                address = value[roadAddressIdx];
            }
            String finalAddress = convertAddrss(address);

            FtcResultDto result = FtcResultDto
                    .builder()
                    .mailOrderNumber(value[mailOrderNumberIdx])
                    .companyName(value[companyNameIdx])
                    .crn(value[crnIdx])
                    .address(finalAddress)
                    .build();
            ftcResultDtoList.add(result);
        }

        log.info("DTO 변환 완료, 지역 법인 기업 수 : {}", ftcResultDtoList.size());
        return ftcResultDtoList;
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

    private String convertAddrss(String address) {
        if (ObjectUtils.isEmpty(address) || address.contains("null")) {
            return "admCdN/A";
        }

        String[] splitAddress = address.split(" ");
        return splitAddress[0] + " " + splitAddress[1] + " " + splitAddress[2] + " " + splitAddress[3];
    }
}
