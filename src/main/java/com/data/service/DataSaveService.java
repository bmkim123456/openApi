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

import static com.data.entity.ApiDataEntity.convertDtoToEntity;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataSaveService {

    private final ApiDataRepository apiDataRepository;

    public String saveApiData(List<DataCompileDto> dataList) {
        List<ApiDataEntity> resultList = new ArrayList<>();
        log.info("데이터 저장 시작");
        try {
            for (DataCompileDto data : dataList) {
                if (ObjectUtils.isEmpty(apiDataRepository.findByEnr(data.getEnr()))) {
                    resultList.add(convertDtoToEntity(data));
                }
                log.info("중복기업, enr : {}", data.getEnr());
            }
            apiDataRepository.saveAll(resultList);
            return "데이터 저장 완료, 저장된 기업 수 : " + resultList.size();
        } catch (Exception e) {
            throw new RuntimeException("저장 중 오류 발생 :" + e.getMessage());
        }
    }
}
