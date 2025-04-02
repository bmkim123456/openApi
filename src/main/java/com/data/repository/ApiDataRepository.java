package com.data.repository;

import com.data.entity.ApiDataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ApiDataRepository extends JpaRepository<ApiDataEntity, Long> {

    @Query("SELECT t FROM ApiDataEntity t WHERE t.enr = :enr")
    ApiDataEntity findByEnr(String enr);
}
