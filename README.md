### [29CM] 백엔드 포지션 과제

## 개발 환경
- JDK 17
- Spring Boot 3.3.1
- Spring Data JPA
- Gradle 8.5
- H2
- JUnit

## 프로젝트 구조
~~~
src/
  +- cart/              # 유저 도메인
    +- application/       # 비지니스 로직 관련
    +- domain/            # 도메인 모음
    +- infra/             # 외부 통신 (구현체 리포지토리 등)
    +- payload/           # DTO 모음
  +- common/            # 공통 로직
    +- exception/       
    +- lock/            
  +- config/            # 설정 관련
  +- order/             # 주문 도메인
  +- payment/           # 결제 도메인
  +- product/           # 상품 도메인
  +- util/              # 유틸 클래스 모음
  +- ui/              # 콘솔 입/출력 관련 클래스
    +- input/
    +- output/
  - Application.java
~~~
각 도메인별로 패키지를 구성하였고 **도메인간 경계를 명확히 하기 위헤 도메인 의존 관계는 애그리 거트 내에서는 직접적으로 의존**하도록, **다른 애그리거트의 경우에는 ID 값으로 간접 의존**하도록 구성했습니다.

## 클래스 설명
* **OrderingMachineRunner**: OrderingMachine을 실행하는 클래스입니다.
* **OrderingMachine**: 주문 프로세스가 정의된 클래스. Input과 Output을 관리하며 OrderProcessHandler에게 실제 내부 동작을 위임합니다.
* **OrderProcessHandler**: 각 도메인의 서비스를 주입받아 주문 프로세스의 데이터를 핸들링합니다.

## 구현 관련
* **도메인 서비스 간 통신은 DTO를 통해 이루어지며** 외부에서 직접 도메인을 조작할 수 없도록 하여 느슨한 결합을 유지했습니다.
* 주문 시 상품 **재고 감소는 상품 번호별로 분산 락을 적용**하여, 상품 도메인에 대한 다른 작업이 블로킹되지 않도록 구현했습니다.
  * **재고 감소 작업은 독립적인 트랜잭션으로 실행**되며, 주문 로직에서 예외가 발생할 경우 재고 롤백 이벤트를 발생시켜 독립적으로 처리합니다.
* 장바구니 도메인을 활용하여 View 클래스에서 상품 추가나 주문 시 장바구니를 기반으로 동작하도록 구현했습니다.
* 도메인 조작은 해당 도메인의 서비스에서만 수행하고, View의 동작은 OrderProcessHandler에서 제어하여 OrderingMachine에서는 입출력만 담당하도록 했습니다.
* Command 객체를 통해 사용자 입력을 처리하여 **외부에서의 입력값 조작을 방지**했습니다.
* CommandLineRunner를 사용하여 애플리케이션 실행 시 자동으로 OrderingMachine이 동작하도록 설정했습니다.

## 동시성 테스트
* ExecutorService 로 스레드를 생성한 후 동시에 ProductService.decreaseStock 메서드를 실행하게 하여 동시성 테스트를 진행했습니다.

## 기타
* **MemoryLockManager**: LockManager 인터페이스를 구현한 메모리 기반 락을 생성하여 멀티 스레드 테스트를 통해 동시성 관련 테스트 진행 완료하였습니다.