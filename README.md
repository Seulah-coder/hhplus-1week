# 항해 1주차 정리

### Java에서 동시성 문제 해결하기

#### 1. 동시성 문제 발생 이유
여러 스레드에서 특정 자원에 동시에 접근할 때 발생되는 문제. 동시에 여러 요청을 받을 때 자원의 상태가 예상치 못하게 변경될 수 있음을 방지하기 위함 -> 데이터 일관성을 해칠 수 있음

#### 2. 찾은 해결방법 
   (1) synchronized 키워드 사용
   (2) ReentrantLock 사용
   
#### 3. 선택한 방법 : ReentrantLock
   이유: synchronized 키워드의 경우 동기화 순서를 보장하지 않으며 적용된 모든 오브젝트에 lock이 발생. synchronized의 인터럽트, 무한 대기, 공정성 부족 문제 발생 또한 하나의 프로세스에만 보장이 됨.
   
   또한 Scale Out을 통해 서버가 2대 이상으로 늘어나는 경우에는 여러 서버에서 데이터의 접근이 가능하게 되므로 여전히 동시성 문제가 발생.
   ReentrantLock의 경우 synchronized 보다 확장된 기능을 가지고 있으며 메소드를 호출함으로 써 어떤 스레드가 먼저 락을 획득하게 될 지 순서 지정이 가능함.

#### 4. 어려웠던 점
   - 테스트 코드를 작성해 본 적은 있지만 기능 단위별로 구현해 본 적은 없어서 테스트를 어떻게 잘 작성할 수 있는지에 대한 고민 - 최대한 간결하게 요구조건이 전부 반영 되게 작성하려고 노력함.
   
   - 자바에서 동시성 제어에 대한 해결 방법도 다양하여 많은 블로그 글 들을 참고하였고 공정한 락, 비공정한 락에 대해 조금 더 공부가 필요함. -> 2주차에 등장..!
   
   - 또한 동시성 테스트를 어떻게 진행해야 하는지에 대한 고민이 있었고 멘토링을 통해 CompletableFuture.allOf().join() 키워드를 알게 되어 검색 후 적용할 수 있었다.
   
   - 멘토링을 통해 알게된 점 : 동시성은 보통 인스턴스 레벨에서 제어하지는 않고 분산환경에서는 레디스나 DB Lock등을 이용하여 동시성을 제어. 동시성을 일어나지 않게 하려면 MessageQueue, Kafka등 큐를 이용해서 한번에 하나씩만 처리하도록 하는 방법을 이용. -> 3주차 부터 시작

#### 5. 피드백 
   - 실제 service 테스트에서 @Mock을 사용하지 않고 의존성 주입 받아서 테스트를 하였고 이로인해 단위 테스트라고 생각해서 작성했던 것이 통합테스트 였다는걸 알게 되었다. 이번 리팩토링을 진행하며 수정 할 부분이 생겼다. -> @Mock을 통해 개선 완료
   
   - ReentrantLock 이 작업단위로 걸려 있기 때문에, 서로 다른 유저끼리의 요청 또한 블라킹 되는 상황으로, ConcurrentHashMap 등을 이용해 같은 유저에 대한 요청끼리만 블라킹 되도록 개선 필요. -> @PointService에서 개선 완료
   
