package com.data;

import com.data.dto.DataCompileDto;
import com.data.dto.FtcResultDto;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class DataCompileTest {

    @Test
    void blockTest() throws InterruptedException {
        System.out.println("::: 시작, 시간 = " + LocalDateTime.now());
        List<FtcResultDto> ftcDataList = ftcDataList();
        Map<String, DataCompileDto> dataMap = new HashMap<>();
        Map<String, String> enrList = new HashMap<>();
        Map<String, String> addrCodeList = new HashMap<>();

        for (FtcResultDto ftcData : ftcDataList) {
            String key = ftcData.getMailOrderNumber();

            DataCompileDto dto = DataCompileDto
                    .builder()
                    .mailOrderNumber(ftcData.getMailOrderNumber())
                    .companyName(ftcData.getCompanyName())
                    .crn(ftcData.getCrn())
                    .build();

            dataMap.put(key, dto);

            String enr = extractEnr(ftcData.getCompanyName());
            enrList.put(key, enr);
            Thread.sleep(500);

            String addrCoed = extractAddr(ftcData.getAddress());
            addrCodeList.put(key, addrCoed);
            Thread.sleep(300);
        }

        try {
            for (String enrMap : enrList.keySet()) {
                enrList.forEach((key, value) -> dataMap.get(key).setEnr(value));
            }

            for (String addrCode : addrCodeList.keySet()) {
                addrCodeList.forEach((key, value) -> dataMap.get(key).setDistrictCode(value));
            }
        } catch (Exception e) {
            System.out.println("에러");
        }

        System.out.println(dataMap.get("0533-앤톡5").getCompanyName());
        System.out.println(dataMap.get("0533-앤톡5").getEnr());
        System.out.println(dataMap.get("0533-앤톡5").getDistrictCode());
        System.out.println("::: 종료, 시간 = " + LocalDateTime.now());
    }

    @Test
    void nonBlockTest() {
        System.out.println("::: 시작, 시간 = " + LocalDateTime.now());
        List<FtcResultDto> ftcDataList = ftcDataList();
        Map<String, DataCompileDto> dataMap = new HashMap<>();

        ExecutorService extractJob = Executors.newFixedThreadPool(8);

        List<Future<Map<String, String>>> enrList = new ArrayList<>();
        List<Future<Map<String, String>>> addrCodeList = new ArrayList<>();

        for (FtcResultDto ftcData : ftcDataList) {
            String key = ftcData.getMailOrderNumber();

            DataCompileDto dto = DataCompileDto
                    .builder()
                    .mailOrderNumber(ftcData.getMailOrderNumber())
                    .companyName(ftcData.getCompanyName())
                    .crn(ftcData.getCrn())
                    .build();

            dataMap.put(key, dto);

            Future<Map<String, String>> enrMap = extractJob.submit(() -> {
                Map<String, String> result = new HashMap<>();
                String enr = extractEnr(ftcData.getCompanyName());
                result.put(key, enr);
                Thread.sleep(500);
                return result;
            });


            Future<Map<String, String>> addrMap = extractJob.submit(() -> {
                if (ftcData.getCompanyName().equals("앤톡4")) {
                    enrMap.get();
                }
                Map<String, String> result = new HashMap<>();
                String addrCoed = extractAddr(ftcData.getAddress());
                result.put(key, addrCoed);
                Thread.sleep(300);
                return result;
            });

            enrList.add(enrMap);
            addrCodeList.add(addrMap);
        }

        try {

            Future<String> setEnrJob = extractJob.submit(() -> {
                for (Future<Map<String, String>> enrMap : enrList) {
                    Map<String, String> result = enrMap.get();
                    result.forEach((key, value) -> dataMap.get(key).setEnr(value));
                }
                return "set enr success";
            });

            Future<String> setAddrCodeJob =  extractJob.submit(() -> {
                for (Future<Map<String, String>> future : addrCodeList) {
                    Map<String, String> result = future.get();
                    result.forEach((key, value) -> dataMap.get(key).setDistrictCode(value));
                }
                return "set addr success";
            });

            setEnrJob.get();
            setAddrCodeJob.get();

        } catch (Exception e) {
            System.out.println("에러");
        }

        System.out.println(dataMap.get("0533-앤톡5").getCompanyName());
        System.out.println(dataMap.get("0533-앤톡5").getEnr());
        System.out.println(dataMap.get("0533-앤톡5").getDistrictCode());
        System.out.println("::: 종료, 시간 = " + LocalDateTime.now());
    }

    @Test
    void threadTest() {
        List<DataCompileDto> resultList = new ArrayList<>();
        List<FtcResultDto> ftcDataList = ftcDataList();
        for (FtcResultDto ftcData : ftcDataList) {
            String[] enrValue = new String[1];
            String[] addressValue = new String[1];
            Thread enrJob = new Thread(() -> {
                System.out.println("enr 작업 시작, 시간 : " + LocalDateTime.now());
                if (extractEnr(ftcData.getCompanyName()).contains("N/A")) {
                    enrValue[0] = "Fail";
                    return;
                }
                enrValue[0] = extractEnr(ftcData.getCompanyName());
                System.out.println("쓰레드 : " + Thread.currentThread().getId());
            });

            Thread addressJob = new Thread(() -> {
                System.out.println("address 작업 시작, 시간 : " + LocalDateTime.now());
                if (extractAddr(ftcData.getAddress()).contains("N/A")) {
                    addressValue[0] = "Fail";
                    return;
                }
                addressValue[0] = extractAddr(ftcData.getAddress());
                System.out.println("쓰레드 : " + Thread.currentThread().getId());
            });

            enrJob.start();
            addressJob.start();

            try {
                enrJob.join();
                addressJob.join();

                String enr = enrValue[0];
                String addrCode = addressValue[0];

                if (enr.equals("Fail") || addrCode.equals("Fail")) {
                    System.out.println("변환 실패, 기업명 : " + ftcData.getCompanyName());
                    return;
                }

                DataCompileDto dto = DataCompileDto
                        .builder()
                        .mailOrderNumber(ftcData.getMailOrderNumber())
                        .companyName(ftcData.getCompanyName())
                        .crn(ftcData.getCrn())
                        .enr(enr)
                        .districtCode(addrCode)
                        .build();
                resultList.add(dto);
            } catch (Exception e) {
                System.out.println("작업 실패");
                return;
            }
        }

        /*for (DataCompileDto dataCompileDto : resultList) {
            System.out.println(dataCompileDto.getMailOrderNumber());
            System.out.println(dataCompileDto.getCompanyName());
            System.out.println(dataCompileDto.getCrn());
            System.out.println(dataCompileDto.getEnr());
            System.out.println(dataCompileDto.getDistrictCode());
        }*/
        System.out.println("기업 수 : " + resultList.size());

    }

    private List<FtcResultDto> ftcDataList() {
        List<FtcResultDto> resultList = new ArrayList<>();

        String mailOrderNumber = "0533-앤톡";
        String companyName = "앤톡";
        String crn = "111-22-3334";
        String address = "서울시 성동구 뚝섬로 1";

        for (int i = 0; i < 20; i++) {
            FtcResultDto dto = FtcResultDto
                    .builder()
                    .mailOrderNumber(mailOrderNumber + i)
                    .companyName(companyName + i)
                    .crn(crn + i)
                    .address(address + i)
                    .build();
            resultList.add(dto);
        }
        return resultList;
    }

    private String extractEnr(String companyName) {
        String result = "N/A";
        result = switch (companyName) {
            case "앤톡0" -> "1101112233440";
            case "앤톡1" -> "1101112233441";
            case "앤톡2" -> "1101112233442";
            case "앤톡3" -> "1101112233443";
            case "앤톡4" -> "1101112233444";
            case "앤톡5" -> "1101112233445";
            case "앤톡6" -> "1101112233446";
            case "앤톡7" -> "1101112233447";
            case "앤톡8" -> "1101112233448";
            case "앤톡9" -> "1101112233449";
            default -> result;
        };
        return result;
    }

    private String extractAddr(String address) {
        String result = "N/A";
        result = switch (address) {
            case "서울시 성동구 뚝섬로 10" -> "앤톡0 1111111111";
            case "서울시 성동구 뚝섬로 11" -> "앤톡1 2222222222";
            case "서울시 성동구 뚝섬로 12" -> "앤톡2 3333333333";
            case "서울시 성동구 뚝섬로 13" -> "앤톡3 4444444444";
            case "서울시 성동구 뚝섬로 14" -> "앤톡4 5555555555";
            case "서울시 성동구 뚝섬로 15" -> "앤톡5 6666666666";
            case "서울시 성동구 뚝섬로 16" -> "앤톡6 7777777777";
            case "서울시 성동구 뚝섬로 17" -> "앤톡7 8888888888";
            case "서울시 성동구 뚝섬로 18" -> "앤톡8 9999999999";
            case "서울시 성동구 뚝섬로 19" -> "앤톡9 0000000000";
            default -> result;
        };
        return result;
    }

}
