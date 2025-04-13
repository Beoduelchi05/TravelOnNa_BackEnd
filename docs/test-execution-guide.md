# 여행ON나 백엔드 테스트 실행 가이드

## 테스트 실행 방법

테스트를 실행하는 방법에는 여러 가지가 있습니다. 상황에 맞게 적절한 방법을 선택하여 테스트를 실행해 주세요.

### 1. 전체 테스트 실행

모든 테스트를 한 번에 실행하려면 다음 명령어를 사용합니다.

```bash
./gradlew test
```

테스트 결과는 `build/reports/tests/test/index.html` 파일에서 확인할 수 있습니다.

### 2. 특정 테스트 실행

특정 테스트 클래스만 실행하려면 다음 명령어를 사용합니다.

```bash
./gradlew test --tests "com.travelonna.demo.domain.log.service.LogCommentServiceTest"
```

특정 테스트 메서드만 실행하려면 다음 명령어를 사용합니다.

```bash
./gradlew test --tests "com.travelonna.demo.domain.log.service.LogCommentServiceTest.createCommentTest"
```

### 3. 특정 패키지의 테스트만 실행

특정 도메인이나 패키지의 테스트만 실행하려면 다음과 같이 와일드카드(`*`)를 사용합니다.

```bash
./gradlew test --tests "com.travelonna.demo.domain.user.*"
```

### 4. IDE에서 실행

IntelliJ IDEA나 Eclipse 같은 IDE에서는 다음과 같이 테스트를 실행할 수 있습니다.

1. 테스트 클래스나 메서드 옆의 실행 아이콘(▶)을 클릭
2. 또는 테스트 클래스나 메서드에서 마우스 우클릭 → Run 선택

## 테스트 실패 대응 방법

테스트가 실패했을 때는 다음 단계로 문제를 해결해보세요.

### 1. 테스트 로그 확인

테스트 실패 시 로그를 확인하여 문제 원인을 파악합니다.

```bash
./gradlew test --debug
```

### 2. 테스트 환경 설정 확인

`application-test.yml` 파일의 설정이 올바른지 확인합니다.

```yaml
spring:
  datasource:
    driver-class-name: org.h2.Driver
    url: jdbc:h2:mem:testdb;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1
    username: sa
    password: 
```

### 3. 테스트 데이터 확인

`data.sql` 파일에 테스트에 필요한 데이터가 올바르게 설정되어 있는지 확인합니다.

### 4. 테스트 의존성 문제 해결

테스트 간 의존성 문제가 발생할 경우, 각 테스트가 독립적으로 실행될 수 있도록 설정합니다.

```java
@BeforeEach
void setUp() {
    // 테스트 전 데이터 초기화
    repository.deleteAll();
}
```

### 5. 순차적 테스트 실행

문제가 발생한 테스트만 독립적으로 실행해 봅니다.

```bash
./gradlew test --tests "문제가_발생한_테스트_클래스"
```

## 테스트 결과 확인

테스트 실행 후 결과를 확인하는 방법입니다.

### 테스트 보고서 확인

테스트 실행 결과는 HTML 보고서로 생성됩니다. 웹 브라우저로 다음 경로의 파일을 열어 확인하세요.

```
build/reports/tests/test/index.html
```

HTML 보고서에서는 다음 정보를 확인할 수 있습니다:

1. 전체 테스트 성공/실패 통계
2. 테스트 클래스별 성공/실패 정보
3. 실패한 테스트의 상세 오류 메시지
4. 테스트 실행 시간 및 성능 정보

### JaCoCo 코드 커버리지 보고서

코드 커버리지를 확인하려면 JaCoCo 플러그인을 사용합니다. 다음 명령어로 코드 커버리지 보고서를 생성할 수 있습니다.

```bash
./gradlew test jacocoTestReport
```

보고서는 다음 경로에서 확인할 수 있습니다.

```
build/reports/jacoco/test/html/index.html
``` 