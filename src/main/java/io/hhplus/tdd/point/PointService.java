package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class PointService {

    @Autowired
    UserPointTable userPointTable;

    @Autowired
    PointHistoryTable pointHistoryTable;

    private final ConcurrentHashMap<Long, Lock> userLocks = new ConcurrentHashMap<>();

    //같은 유저에 대해서만 동시성 제어
    private Lock getUserLock(Long userId){
        return userLocks.computeIfAbsent(userId, id -> new ReentrantLock());
    }

    /**
     * 포인트를 충전한다
     * @param userId
     * @param amount
     * @return
     */
    public UserPoint chargePoint(Long userId, Long amount){
            Lock lock = getUserLock(userId); //같은 유저임을 확인 하고 lock을 획득
            lock.lock();

            try{
                //유저 포인트를 조회한다
                UserPoint userPoint = this.getUserPoint(userId);

                long currentPoints = userPoint.point();
                long newPoints;

                if(amount > 100000){
                    throw new IllegalArgumentException("최대 10만 포인트 까지 적립 가능합니다.");
                }
                newPoints = currentPoints + amount;

                userPoint = userPointTable.insertOrUpdate(userId, newPoints);
                saveUserHistory(userId, amount, TransactionType.CHARGE);
                return userPoint;
            } finally {
                lock.unlock();
            }
    }

    public void saveUserHistory(Long userId, Long amount, TransactionType type){
        pointHistoryTable.insert(userId, amount, type, System.currentTimeMillis());
    }

    public UserPoint usePoint(Long userId, Long amount){
            Lock lock = getUserLock(userId);
            lock.lock();

            try{
                //유저 포인트를 조회한다
                UserPoint userPoint = this.getUserPoint(userId);

                long currentPoints = userPoint.point();
                long newPoints;

                if(amount > currentPoints){
                    throw new IllegalArgumentException("사용 하려는 포인트가 현재 가지고 있는 포인트보다 큽니다. 현재 잔액 : "
                            + currentPoints);
                }
                newPoints = currentPoints - amount;

                userPoint = userPointTable.insertOrUpdate(userId, newPoints);
                saveUserHistory(userId, amount, TransactionType.USE);
                return userPoint;
            } finally {
                lock.unlock();
            }
    }

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
        List<PointHistory> result = pointHistoryTable.selectAllByUserId(userId);
        if(result.isEmpty()){
            throw new RuntimeException("포인트 내역 없음");
        }
        return result;
    }

}
