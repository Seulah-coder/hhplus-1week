package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.mockito.Mockito.when;

@SpringBootTest
class PointServiceTest {

    @InjectMocks
    private PointService pointService;


    @Mock
    private PointHistoryTable pointHistoryTable;


    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
    }


    @Test
    @DisplayName("사용자 포인트 조회")
    void getUserPoint() {
        //given
        long userId = 1L;
        long amount = 3000L;

        UserPoint userPoint = new UserPoint(userId, amount, System.currentTimeMillis());

        //when
        pointService.getUserPoint(userId);
        when(pointService.getUserPoint(userId)).thenReturn(userPoint);

        //then
        Assertions.assertEquals(userPoint.id(), 1L);
        Assertions.assertEquals(userPoint.point(), 3000L);
    }

    @Test
    @DisplayName("사용자 포인트 내역 조회")
    void getUserPointHistories() {

        //given
        long userId = 1L;
        List<PointHistory> expectedPointHistories = List.of(
                new PointHistory(1L, userId, 1000L, TransactionType.CHARGE, System.currentTimeMillis()),
                new PointHistory(2L, userId, 500L, TransactionType.USE, System.currentTimeMillis())
        );

        when(pointHistoryTable.selectAllByUserId(userId)).thenReturn(expectedPointHistories);

        //when
        List<PointHistory> historyList = pointService.getUserPointHistories(userId);

        //then
        Assertions.assertEquals(expectedPointHistories, historyList);
    }

    @Test
    @DisplayName("사용자 포인트 충전")
    void chargeUserPoint() {
        //given
        long userId = 1L;
        long amount = 2000L;
        UserPoint userPoint = new UserPoint(userId, amount, System.currentTimeMillis());

        UserPoint expectedUserPoint = new UserPoint(userId, amount + 3000L, System.currentTimeMillis());
        when(pointService.getUserPoint(userId)).thenReturn(userPoint);
        when(pointService.chargePoint(userId, amount + 3000L)).thenReturn(expectedUserPoint);


        //when
        UserPoint newUserPoint = pointService.chargePoint(userId, amount);


        //then
        Assertions.assertEquals(newUserPoint.id(), 1L);
        Assertions.assertEquals(expectedUserPoint, newUserPoint);
    }

    @Test
    @DisplayName("사용자 포인트 사용")
    void useUserPoint(){

        //given
        long userId = 1L;
        long amount = 2000L;
        UserPoint userPoint = new UserPoint(userId, amount, System.currentTimeMillis());
        UserPoint expectedUserPoint = new UserPoint(userId, amount - 500L, System.currentTimeMillis());

        when(pointService.getUserPoint(userId)).thenReturn(userPoint);
        when(pointService.usePoint(userId, amount + 500L)).thenReturn(expectedUserPoint);


        //when
        UserPoint result = pointService.usePoint(userId, amount);

        //then
        Assertions.assertEquals(result.id(), 1L);
        Assertions.assertEquals(result.point(), amount - 500L);

    }

}