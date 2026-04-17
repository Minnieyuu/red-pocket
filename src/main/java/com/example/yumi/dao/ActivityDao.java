package com.example.yumi.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.yumi.Enum.Active;
import com.example.yumi.entity.Activity;

import jakarta.transaction.Transactional;

@Repository
public interface ActivityDao extends JpaRepository<Activity, Long> {

	List<Activity> findByStatus(Active active);

	Optional<Activity> findByStatusAndActivityId(Active active, String activityId);

	// 保持原子姓
	@Modifying
	@Transactional
	@Query(value = "update activity set total_stock=total_stock-1 where "
			+ " activity_id = :activityId and status='Active'  ", nativeQuery = true)
	void decreaseStock(String activityId);

	@Modifying
	@Transactional
	@Query(value = "update Activity set total_stock = total_stock - 1 WHERE activity_id = :activityId and status='Active' AND total_stock > 0", nativeQuery = true)
	int updateStock(String activityId);

	@Modifying
	@Transactional
	@Query(value = "INSERT INTO GRAB_RECORD (user_id, grab_time, activity_ref_id)"
			+ "SELECT :userId ,  GETDATE(),a.id "
			+ "FROM activity  a WHERE a.activity_id = :activityId; ", nativeQuery = true)
	int insertGrabRecord(String activityId, String userId);

}
