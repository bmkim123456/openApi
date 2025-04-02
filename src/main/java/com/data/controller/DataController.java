package com.data.controller;

import com.data.dto.DataCompileDto;
import com.data.dto.FtcResultDto;
import com.data.service.DataCompileService;
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

    @PostMapping("/request")
    public String request(@RequestParam String city, @RequestParam String district) {

        List<FtcResultDto> ftcDataList = ftcDataService.ftcDataList(city, district);
        List<DataCompileDto> compileDtoList = dataCompileService.companyDataList(ftcDataList);

        for (DataCompileDto compileDto : compileDtoList) {
            System.out.println(compileDto.getMailOrderNumber());
            System.out.println(compileDto.getCompanyName());
            System.out.println(compileDto.getCrn());
            System.out.println(compileDto.getEnr());
            System.out.println(compileDto.getDistrictCode());
        }

        return "ok";
    }
}
