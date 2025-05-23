package com.data.controller;

import com.data.dto.DataCompileDto;
import com.data.dto.FtcResultDto;
import com.data.service.DataCompileService;
import com.data.service.DataSaveService;
import com.data.service.FtcDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DataController {

    private final FtcDataService ftcDataService;
    private final DataCompileService dataCompileService;
    private final DataSaveService dataSaveService;

    @PostMapping("/request")
    public String request(@RequestParam String city, @RequestParam String district) {

        List<FtcResultDto> ftcDataList = ftcDataService.ftcDataList(city, district);
        Map<String, DataCompileDto> dataCompileMap = dataCompileService.dataCompileDtoMap(ftcDataList);

        try {
            return dataSaveService.savaApiDataTmp(dataCompileMap);
        } catch (Exception e) {
            throw new RuntimeException("데이터 저장 중 오류 발생, 사유 : " + e.getMessage());
        }
    }
}
