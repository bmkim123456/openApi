package com.data.service;

import com.data.dto.FtcResultDto;
import com.data.util.ApiSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class FtcDataService {

    private final ApiSource apiSource;

    public List<FtcResultDto> ftcDataList(String city, String district) {
        log.info("csv 파일 DTO 변환");

        String[] splitData = getMailOrderSaleData(city, district).split("\n");
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
            if (!value[indexes.get(3)].equals("법인")) {
                continue;
            }

            String address = value[addressIdx];
            if (value[addressIdx].length() < 3) {
                address = value[roadAddressIdx];
            }

            FtcResultDto result = FtcResultDto
                    .builder()
                    .mailOrderNumber(value[mailOrderNumberIdx])
                    .companyName(value[companyNameIdx])
                    .crn(value[crnIdx])
                    .address(address)
                    .build();
            ftcResultDtoList.add(result);
        }

        log.info("DTO 변환 완료, 지역 법인 기업 수 : {}", ftcResultDtoList.size());
        return ftcResultDtoList;
    }

    private String getMailOrderSaleData(String city, String district) {
        try {
            log.info("요청 데이터 : 시/도 : {}, 군/구 : {}", city, district);

            String url = "통신판매사업자_" + city + "_" + district + ".csv";
            String download = apiSource.ftcRequestUrl(url);

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
                log.error("전달할 파일이 없습니다. 시/도, 군/구 입력이 정확한지 확인 해주세요");
                throw new RuntimeException("지역 이름이 정확하지 않습니다.");
            }

            log.info("csv 자료 확인");
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
