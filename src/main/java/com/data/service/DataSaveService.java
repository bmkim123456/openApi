package com.data.service;

import com.data.dto.DataCompileDto;
import com.data.entity.ApiDataEntity;
import com.data.repository.ApiDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.data.entity.ApiDataEntity.convertDtoToEntity;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataSaveService {

    private final ApiDataRepository apiDataRepository;

    public String savaApiDataTmp(Map<String, DataCompileDto> dataCompileDtoMap) {
        List<ApiDataEntity> resultList = new ArrayList<>();
        log.info("데이터 저장 시작");
        try {
            dataCompileDtoMap.forEach((key, value) -> {
                if (apiDataRepository.findByEnr(value.getEnr()).isPresent()) {
                    log.info("이미 저장된 기업 입니다. enr : {}", value.getEnr());
                    return;
                }
                if (ObjectUtils.isEmpty(value.getEnr()) || ObjectUtils.isEmpty(value.getDistrictCode())) {
                    return;
                }
                resultList.add(convertDtoToEntity(value));
            });
            apiDataRepository.saveAll(resultList);
            return "데이터 저장 완료. 저장된 기업 수 : " + resultList.size();
        } catch (Exception e) {
            throw new RuntimeException("데이터 저장 중 오류 발생. 사유 : " + e.getMessage());
        }
    }

}
