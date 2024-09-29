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

        pointService.chargePoints(userId, amount);
        UserPoint userPoint = pointService.getUserPoint(userId);

        Assertions.assertEquals(userPoint.id(), 1L);
        Assertions.assertEquals(userPoint.point(), 3000L);
    }

    @Test
    void getUserPointHistories() {
        long userId = 1L;

        pointService.chargePoints(userId, 3000L);
        pointService.chargePoints(userId, 2000L);
        pointService.chargePoints(userId, 1000L);
        pointService.chargePoints(userId, 500L);

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
//        long amount = 100001L;

        CompletableFuture<UserPoint> userPoint = pointService.chargePoints(userId, amount);

        Assertions.assertEquals(userPoint.get().id(), 1L);
        //테스트 실패해보기
//        Assertions.assertEquals(userPoint.point(), 3000L);
        //테스트 성공값
        Assertions.assertEquals(userPoint.get().point(), 2000L);
    }

    @Test
    void useUserPoint(){
        long userId = 1L;
        long amount = 2000L;
//        long amount = 100001L;
        pointService.chargePoints(userId, 4000L);

        UserPoint userPoint = pointService.useUserPoint(userId, amount);

        Assertions.assertEquals(userPoint.id(), 1L);
        //테스트 실패해보기
//        Assertions.assertEquals(userPoint.point(), 3000L);
        //테스트 성공값
        Assertions.assertEquals(userPoint.point(), 2000L);

    }

    /**
     * 1번 통합 테스트 : 출금과 입금 동시에
     */
    @Test
    void pointIntegrationTestOne(){
        long userId = 1L;
        CompletableFuture.allOf(
                CompletableFuture.runAsync(() -> pointService.chargePoints(userId, 300L)),
                CompletableFuture.runAsync(() -> pointService.chargePoints(userId, 500L)),
                CompletableFuture.runAsync(() -> pointService.chargePoints(userId, 40000L)),
                CompletableFuture.runAsync(() -> pointService.usePoint(userId, 2000L)),
                CompletableFuture.runAsync(() -> pointService.usePoint(userId, 3000L))
        ).join();

        UserPoint userPoint = pointService.getUserPoint(userId);
        Assertions.assertEquals(userPoint.point(), 300 + 500 + 40000 - 2000 - 3000);
        List<PointHistory> historyList = pointService.getUserPointHistories(userId);
        System.out.println("historyList = " + historyList);
    }

    /**
     * 2번 통합 테스트 : 내 돈 보다 많은 차감 요청이 들어올때
     */
    @Test
    void pointIntegrationTestTwo(){
        CompletableFuture<UserPoint> userPoint = pointService.chargePoints(1L, 5000L);
        pointService.useUserPoint(1L, 6000L);
        //exception발생
    }

    /**
     * 3번 통합 테스트 : 동시에 입금 요청
     */
    @Test
    void pointIntegrationTestThree(){
        long userId = 1L;
        CompletableFuture.allOf(
                CompletableFuture.runAsync(() -> pointService.chargePoints(userId, 300L)),
                CompletableFuture.runAsync(() -> pointService.chargePoints(userId, 500L)),
                CompletableFuture.runAsync(() -> pointService.chargePoints(userId, 40000L)),
                CompletableFuture.runAsync(() -> pointService.chargePoints(userId, 2000L)),
                CompletableFuture.runAsync(() -> pointService.chargePoints(userId, 3000L))
        ).join();

        UserPoint userPoint = pointService.getUserPoint(userId);
        Assertions.assertEquals(userPoint.point(), 300 + 500 + 40000 + 2000 + 3000);
        List<PointHistory> historyList = pointService.getUserPointHistories(userId);
        System.out.println("historyList = " + historyList);
    }


    /**
     * 4번 통합 테스트 : 동시에 출금 요청
     */
    @Test
    void pointIntegrationTestFour(){
        long userId = 1L;
        pointService.chargePoints(userId, 100000L);
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
    }
}
