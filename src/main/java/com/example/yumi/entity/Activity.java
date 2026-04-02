package com.example.yumi.entity;

import java.time.LocalDateTime;

import com.example.yumi.Enum.Active;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Activity")
public class Activity {

	@Id
	@Column(name = "ID")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** 活動唯一識別碼 */
	@Column(name = "activity_id", nullable = false, unique = true, length = 50)
	private String activityId;

	/** 紅包總數量 */
	@Column(name = "total_stock", nullable = false)
	private Integer totalStock;

	/** 活動狀態：ACTIVE / FINISHED */
	@Enumerated(EnumType.STRING)
	@Column(name = "status", length = 20)
	private Active status;

	/** 活動建立時間 */
	@Column(name = "created_time")
	private LocalDateTime createdTime;

}
