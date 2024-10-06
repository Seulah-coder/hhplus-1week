package io.hhplus.tdd.point;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@SpringBootTest
public class PointServiceIntergrationTest {

    @Autowired
    PointService pointService;

    @Test
    void getUserPoint() {
        long userId = 1L;
        long amount = 3000L;

        pointService.chargePoint(userId, amount);
        UserPoint userPoint = pointService.getUserPoint(userId);

        Assertions.assertEquals(userPoint.id(), 1L);
        Assertions.assertEquals(userPoint.point(), 3000L);
    }

    @Test
    void getUserPointHistories() {
        long userId = 1L;

        pointService.chargePoint(userId, 3000L);
        pointService.chargePoint(userId, 2000L);
        pointService.chargePoint(userId, 1000L);
        pointService.chargePoint(userId, 500L);

        List<PointHistory> historyList = pointService.getUserPointHistories(userId);

        Assertions.assertEquals(historyList.get(0).amount(), 3000L);
        Assertions.assertEquals(historyList.get(1).amount(), 2000L);
        Assertions.assertEquals(historyList.get(2).amount(), 1000L);
        Assertions.assertEquals(historyList.get(3).amount(), 500L);
    }

    @Test
    void chargeUserPoint() throws ExecutionException, InterruptedException {
        long userId = 1L;
        long amount = 2000L;

        UserPoint userPoint = pointService.chargePoint(userId, amount);

        Assertions.assertEquals(userPoint.id(), 1L);
        //테스트 성공값
        Assertions.assertEquals(userPoint.point(), 2000L);
    }

    @Test
    void useUserPoint(){
        long userId = 1L;
        long amount = 2000L;
        pointService.chargePoint(userId, 4000L);

        UserPoint userPoint = pointService.usePoint(userId, amount);

        Assertions.assertEquals(userPoint.id(), 1L);
        Assertions.assertEquals(userPoint.point(), 2000L);

    }

    /**
     * 1번 통합 테스트 : 출금과 입금 동시에
     */
    @Test
    void pointIntegrationTestOne(){
        long userId = 1L;
        CompletableFuture.allOf(
                CompletableFuture.runAsync(() -> pointService.chargePoint(userId, 300L)),
                CompletableFuture.runAsync(() -> pointService.chargePoint(userId, 500L)),
                CompletableFuture.runAsync(() -> pointService.chargePoint(userId, 40000L)),
                CompletableFuture.runAsync(() -> pointService.usePoint(userId, 2000L)),
                CompletableFuture.runAsync(() -> pointService.usePoint(userId, 3000L))
        ).join();


        UserPoint userPoint = pointService.getUserPoint(userId);
        Assertions.assertEquals(userPoint.point(), 300 + 500 + 40000 - 2000 - 3000);
        List<PointHistory> historyList = pointService.getUserPointHistories(userId);
        System.out.println("historyList = " + historyList);
        System.out.println(userPoint.point());
    }

    /**
     * 2번 통합 테스트 : 내 돈 보다 많은 차감 요청이 들어올때
     */
    @Test
    void pointIntegrationTestTwo(){
        pointService.chargePoint(1L, 5000L);
        pointService.usePoint(1L, 6000L);
        //exception발생!!
    }

    /**
     * 3번 통합 테스트 : 동시에 입금 요청
     */
    @Test
    void pointIntegrationTestThree(){
        long userId = 1L;
        CompletableFuture.allOf(
                CompletableFuture.runAsync(() -> pointService.chargePoint(userId, 300L)),
                CompletableFuture.runAsync(() -> pointService.chargePoint(userId, 500L)),
                CompletableFuture.runAsync(() -> pointService.chargePoint(userId, 40000L)),
                CompletableFuture.runAsync(() -> pointService.chargePoint(userId, 2000L)),
                CompletableFuture.runAsync(() -> pointService.chargePoint(userId, 3000L))
        ).join();

        UserPoint userPoint = pointService.getUserPoint(userId);
        Assertions.assertEquals(userPoint.point(), 300 + 500 + 40000 + 2000 + 3000);
        List<PointHistory> historyList = pointService.getUserPointHistories(userId);
        System.out.println("historyList = " + historyList);
        System.out.println("userPoint = " + userPoint.point());
    }


    /**
     * 4번 통합 테스트 : 동시에 출금 요청
     */
    @Test
    void pointIntegrationTestFour(){
        long userId = 1L;
        pointService.chargePoint(userId, 100000L);
        CompletableFuture.allOf(
                CompletableFuture.runAsync(() -> pointService.usePoint(userId, 300L)),
                CompletableFuture.runAsync(() -> pointService.usePoint(userId, 500L)),
                CompletableFuture.runAsync(() -> pointService.usePoint(userId, 40000L)),
                CompletableFuture.runAsync(() -> pointService.usePoint(userId, 2000L)),
                CompletableFuture.runAsync(() -> pointService.usePoint(userId, 3000L))
        ).join();

        UserPoint userPoint = pointService.getUserPoint(userId);
        Assertions.assertEquals(userPoint.point(), 100000 - 300 - 500 - 40000 - 2000 - 3000);
        List<PointHistory> historyList = pointService.getUserPointHistories(userId);
        System.out.println("historyList = " + historyList);
        System.out.println(userPoint.point());
    }
}
