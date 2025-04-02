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
        List<DataCompileDto> compileDtoList = dataCompileService.companyDataList(ftcDataList);

        dataSaveService.saveApiData(compileDtoList);

        return "ok";
    }
}
