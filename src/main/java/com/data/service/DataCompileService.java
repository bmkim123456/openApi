package com.data.service;

import com.data.dto.DataCompileDto;
import com.data.dto.FtcResultDto;
import com.data.util.ApiSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataCompileService {

    private final ApiSource apiSource;

    public List<DataCompileDto> companyDataList(List<FtcResultDto> csvDataList) {

        List<DataCompileDto> resultList = new ArrayList<>();
        for (FtcResultDto csvData : csvDataList) {
            String kftcResponseBody = apiSource.kftcResponse(csvData.getCrn());
            if (!kftcResponseBody.contains("<crno>")) {
                log.info("기업명 : {} 법인번호 확인 실패", csvData.getCompanyName());
                continue;
            }
            String enr = extractCrno(kftcResponseBody);

            String addressResponseBody = apiSource.addressApiResponse(csvData.getAddress());
            if (csvData.getAddress().contains("주소매핑 실패")) {
                addressResponseBody = extractAddr(kftcResponseBody);
            }
            String addressCode = extractCode(addressResponseBody);

            DataCompileDto dataCompileDto = DataCompileDto
                    .builder()
                    .mailOrderNumber(csvData.getMailOrderNumber())
                    .companyName(csvData.getCompanyName())
                    .crn(csvData.getCrn())
                    .enr(enr)
                    .districtCode(addressCode)
                    .build();
            resultList.add(dataCompileDto);
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

    private String extractCode(String response) {
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

}
