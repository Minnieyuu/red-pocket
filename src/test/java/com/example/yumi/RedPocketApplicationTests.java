package com.example.yumi;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RedPocketApplicationTests {

	@Test
	void contextLoads() {
	}

//	@Autowired
//	private RabbitTemplate rabbitTemplate;

	/* MQ 壓測 */
//	@Test
//	void stressTestGrab() throws InterruptedException {
//		int userCount = 500; // 模擬 500 人同時搶
//		CountDownLatch latch = new CountDownLatch(1);
//		ExecutorService executor = Executors.newFixedThreadPool(userCount);
//
//		for (int i = 0; i < userCount; i++) {
//			final long userId = i;
//			executor.submit(() -> {
//				try {
//					latch.await(); // 等待 先不推到MQ
//					GrabSuccessEvent event = new GrabSuccessEvent("activity-20260326", String.valueOf(userId));
//					rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.ROUTING_KEY, event);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			});
//		}
//
//		System.out.println(" 500筆同時匯入MQ，開始!");
//		latch.countDown(); // 開始匯入
//
//		executor.shutdown();
//		executor.awaitTermination(1, TimeUnit.MINUTES);//等待一分鐘 確保資料都進MQ
//
//		Thread.sleep(50000);// 等待30秒 確保所有程式執行完畢
//		System.out.println("🏁 壓測訊息發送完畢，觀察 Consumer 消化情況...");
//	}

}
