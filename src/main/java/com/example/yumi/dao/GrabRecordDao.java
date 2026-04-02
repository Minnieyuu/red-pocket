package com.example.yumi.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.yumi.entity.GrabRecord;

@Repository
public interface GrabRecordDao extends JpaRepository<GrabRecord, Long> {

}
