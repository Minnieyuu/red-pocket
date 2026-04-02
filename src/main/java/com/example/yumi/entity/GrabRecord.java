package com.example.yumi.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "GRAB_RECORD", uniqueConstraints = {
		// 防止重複領取
		@UniqueConstraint(columnNames = { "user_id", "activity_ref_id" }) })
public class GrabRecord {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id", nullable = false, length = 50)
	private String userId;

	@Column(name = "grab_time", nullable = false)
	private LocalDateTime grabTime;

	@ManyToOne(fetch = FetchType.LAZY) // 提高效能
	@JoinColumn(name = "activity_ref_id", nullable = false)
	private Activity activity;

}
