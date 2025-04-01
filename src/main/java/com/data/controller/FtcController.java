package com.data.controller;

import com.data.dto.FtcResultDto;
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
public class FtcController {

    private final FtcDataService ftcDataService;

    @PostMapping("/request")
    public String request(@RequestParam String city, @RequestParam String district) {

        List<FtcResultDto> ftcDataList = ftcDataService.ftcDataList(city, district);

        for (FtcResultDto ftcData : ftcDataList) {
            System.out.println(ftcData.getMailOrderNumber());
            System.out.println(ftcData.getCrn());
        }

        return "ok";
    }
}
