package com.data;

import com.data.dto.DataCompileDto;
import com.data.dto.FtcResultDto;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DataCompileTest {

    @Test
    void blockTest() {
        List<DataCompileDto> resultList = new ArrayList<>();
        List<FtcResultDto> ftcDataList = ftcDataList();
        for (FtcResultDto ftcData : ftcDataList) {
            String[] enrValue = new String[1];
            String[] addressValue = new String[1];
            System.out.println("enr 작업 시작, 시간 : " + LocalDateTime.now());
            if (extractEnr(ftcData.getCompanyName()).contains("N/A")) {
                enrValue[0] = "Fail";
                return;
            }
            enrValue[0] = extractEnr(ftcData.getCompanyName());
            System.out.println("쓰레드 : " + Thread.currentThread().getId());

            System.out.println("address 작업 시작, 시간 : " + LocalDateTime.now());
            if (extractAddr(ftcData.getAddress()).contains("N/A")) {
                addressValue[0] = "Fail";
                return;
            }
            addressValue[0] = extractAddr(ftcData.getAddress());
            System.out.println("쓰레드 : " + Thread.currentThread().getId());

            try {
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
        System.out.println("기업 수 : " + resultList.size());

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

        for (int i = 0; i < 10; i++) {
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
            case "서울시 성동구 뚝섬로 10" -> "1111111111";
            case "서울시 성동구 뚝섬로 11" -> "2222222222";
            case "서울시 성동구 뚝섬로 12" -> "3333333333";
            case "서울시 성동구 뚝섬로 13" -> "4444444444";
            case "서울시 성동구 뚝섬로 14" -> "5555555555";
            case "서울시 성동구 뚝섬로 15" -> "6666666666";
            case "서울시 성동구 뚝섬로 16" -> "7777777777";
            case "서울시 성동구 뚝섬로 17" -> "8888888888";
            case "서울시 성동구 뚝섬로 18" -> "9999999999";
            case "서울시 성동구 뚝섬로 19" -> "0000000000";
            default -> result;
        };
        return result;
    }

}
