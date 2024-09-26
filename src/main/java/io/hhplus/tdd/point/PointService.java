package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class PointService {
    @Autowired
    UserPointTable userPointTable;

    @Autowired
    PointHistoryTable pointHistoryTable;

    private final ReentrantLock lock = new ReentrantLock();

    /**
     * 유저 포인트를 조회한다
     * @param userId
     * @return
     */
    public UserPoint getUserPoint(long userId){

        UserPoint userPoint = userPointTable.selectById(userId);
        if(userPoint == null){
            userPoint = new UserPoint(0, 0, 0);
        }
        return userPoint;
    }

    /**
     *  유저 포인트 히스토리를 조회한다
     * @param userId
     * @return
     */
    public List<PointHistory> getUserPointHistories(long userId){
        return pointHistoryTable.selectAllByUserId(userId);
    }

    /*
    포인트를 충전한다
     */
    public UserPoint chargeUserPoint(long userId, long amount){

        lock.lock();
        try {
            //유저 포인트를 조회한다
            UserPoint userPoint = this.getUserPoint(userId);

            long currentPoints = userPoint.point();
            long newPoints;


            if(amount > 100000){
                throw new IllegalArgumentException("최대 10만 포인트 까지 적립 가능합니다.");
            }
            newPoints = currentPoints + amount;

            long timeStampMillis = System.currentTimeMillis();
            userPoint = userPointTable.insertOrUpdate(userId, newPoints);
            pointHistoryTable.insert(userId, amount, TransactionType.CHARGE, timeStampMillis);
            return userPoint;
        } finally {
            lock.unlock();
        }
    }

    public UserPoint useUserPoint(long userId, long amount){
        lock.lock();
        try {
            //유저 포인트를 조회한다
            UserPoint userPoint = this.getUserPoint(userId);

            long currentPoints = userPoint.point();
            long newPoints;
            if(amount > currentPoints){
                throw new IllegalArgumentException("사용 하려는 포인트가 현재 가지고 있는 포인트보다 큽니다. 현재 잔액 : "
                        + currentPoints);
            }
            newPoints = currentPoints - amount;

            long timeStampMillis = System.currentTimeMillis();
            userPoint = userPointTable.insertOrUpdate(userId, newPoints);
            pointHistoryTable.insert(userId, amount, TransactionType.USE, timeStampMillis);
            return userPoint;
        } finally {
            lock.unlock();
        }
    }
}
