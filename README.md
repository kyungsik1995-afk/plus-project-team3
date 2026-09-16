# plus-project-team3
# 🛒 커머스 결제 시스템

Spring Boot 기반으로 구현한 커머스 결제 백엔드 팀 프로젝트입니다.

회원, 상품, 장바구니를 기반으로 주문과 결제, 주문 취소까지의 핵심 커머스 기능을 구현하고, CH5에서는 QueryDSL, Index, Cache, 동시성 제어를 주제로 추가적인 성능 및 안정성 개선을 진행했습니다.

---

## 📌 프로젝트 소개

### 프로젝트 목적

커머스 서비스의 핵심 기능을 직접 구현하며 다음과 같은 백엔드 개발 경험을 쌓는 것을 목표로 했습니다.

- REST API 설계 및 구현
- 도메인별 계층 구조 설계
- Spring Security + JWT 기반 인증
- JPA 기반 데이터 처리
- 트랜잭션을 통한 데이터 정합성 관리
- 주문 및 결제 상태 관리
- 재고 차감 및 복구
- 주문 상품 정보 스냅샷 관리
- QueryDSL을 활용한 동적 검색
- 대량 데이터 환경에서의 Index 성능 분석
- Cache를 활용한 조회 성능 개선
- 동시성 상황에서의 데이터 정합성 문제 해결
- GitHub Pull Request 기반 협업

### 주요 서비스 흐름

회원
↓
상품 조회
↓
장바구니
↓
주문 생성
↓
결제 승인
↓
주문 완료
↓
주문 취소

---

# 🛠️ 기술 스택

| 구분 | 기술 |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 4.1.1 |
| ORM | Spring Data JPA / Hibernate |
| Database | MySQL 8 |
| Query | QueryDSL |
| Security | Spring Security |
| Authentication | JWT |
| Build | Gradle |
| Cache | Spring Cache |
| Test / API | Postman |
| IDE | IntelliJ IDEA |
| Version Control | Git / GitHub |

---

# 🏗️ 프로젝트 구조

```text
com.example.plus
├── domain
│   ├── member
│   │   ├── controller
│   │   ├── service
│   │   ├── repository
│   │   ├── entity
│   │   └── dto
│   │
│   ├── product
│   │   ├── controller
│   │   ├── service
│   │   ├── repository
│   │   ├── entity
│   │   └── dto
│   │
│   ├── cart
│   │   ├── controller
│   │   ├── service
│   │   ├── repository
│   │   ├── entity
│   │   └── dto
│   │
│   ├── order
│   │   ├── controller
│   │   ├── service
│   │   ├── repository
│   │   ├── entity
│   │   ├── dto
│   │   └── facade
│   │
│   └── payment
│       ├── controller
│       ├── service
│       ├── repository
│       ├── entity
│       ├── dto
│       └── facade
│
└── global
    ├── common
    │   ├── entity
    │   └── response
    ├── config
    │   └── jpa
    ├── security
    │   └── jwt
    └── exception
        ├── business
        ├── handler
        ├── ErrorResponse
        └── ErrorCode

도메인별로 Controller → Service → Repository 계층을 구성했으며, 주문·결제와 같이 여러 도메인의 협력이 필요한 기능은 Facade를 통해 흐름을 조합했습니다.

🔐 인증 및 인가

Spring Security와 JWT를 활용하여 인증된 사용자만 보호된 API에 접근할 수 있도록 구성했습니다.

인증 흐름
회원가입
   ↓
로그인
   ↓
JWT 발급
   ↓
Authorization Header
   ↓
Spring Security
   ↓
인증된 사용자 확인

주문, 장바구니, 결제 등 회원별 데이터에 접근하는 API에서는 현재 로그인한 회원의 소유권을 확인하여 다른 회원의 데이터에 접근하지 못하도록 처리했습니다.

🛍️ 주요 기능
1. 회원
회원가입
로그인
JWT 기반 인증
2. 상품

상품 목록 조회 시 다음 조건을 조합하여 검색할 수 있습니다.

카테고리
최소 가격
최대 가격
페이지 번호
페이지 크기

예시:

GET /api/products?category=ELECTRONICS&minPrice=30000&maxPrice=70000&page=0&size=20

상품 목록 조회에는 QueryDSL을 사용하여 요청 조건에 따라 동적으로 검색 조건을 구성했습니다.

3. 장바구니

장바구니에서 다음 기능을 제공합니다.

장바구니 조회
상품 추가
상품 수량 변경
상품 삭제
장바구니 전체 삭제

장바구니는 회원별로 관리하며 다른 회원의 장바구니 항목에 접근할 수 없도록 소유권을 확인합니다.

📦 주문
주문 생성

주문 생성은 재고 차감, 주문 생성, 주문 상품 생성, 결제 정보 생성 등 여러 데이터 변경이 함께 발생하기 때문에 하나의 트랜잭션으로 처리합니다.

장바구니 조회
    ↓
주문 대상 확인
    ↓
재고 확인
    ↓
재고 차감
    ↓
Order 생성
    ↓
OrderItem 생성
    ↓
Payment 생성
주문 대상

요청에 장바구니 상품 ID가 포함되어 있으면 해당 상품만 주문하고, 장바구니 상품 ID가 비어 있으면 현재 장바구니의 전체 상품을 주문합니다.

재고 정합성

주문 생성 과정에서 하나의 상품이라도 재고가 부족하면 예외가 발생하고 트랜잭션 전체가 롤백되도록 구성했습니다.

상품 A 재고 차감
상품 B 재고 차감
상품 C 재고 부족
       ↓
    예외 발생
       ↓
   전체 롤백
       ↓
A/B 재고도 원래 상태로 복구

이를 통해 일부 상품의 재고만 차감된 상태로 주문이 남는 문제를 방지했습니다.

주문 상품 스냅샷

주문 당시의 상품명과 가격을 OrderItem에 저장합니다.

Product
 ├── 현재 상품명
 └── 현재 가격

        ↓ 주문

OrderItem
 ├── 주문 당시 상품명
 └── 주문 당시 가격

따라서 주문 이후 상품의 이름이나 가격이 변경되더라도 기존 주문의 주문 당시 정보는 유지됩니다.

장바구니 처리

주문 생성 시 장바구니 상품을 바로 삭제하지 않습니다.

주문 생성
   ↓
장바구니 유지
   ↓
결제 승인
   ↓
주문한 상품만 장바구니에서 삭제

결제 승인에 성공하면 주문에 포함된 상품의 장바구니 항목만 삭제합니다.

따라서 주문하지 않은 상품은 기존 장바구니에 그대로 남아 있습니다.

💳 결제

현재 프로젝트에서는 외부 PG 연동 대신 모의 결제 방식으로 결제 흐름을 구현했습니다.

결제 흐름
주문 생성
   ↓
Payment = PENDING
Order = PAYMENT_PENDING
   ↓
결제 승인
   ↓
Payment = PAID
Order = COMPLETED
   ↓
주문 상품 장바구니 삭제

결제 승인 요청 시 주문에 저장된 금액과 요청된 결제 금액을 비교하여 결제 금액이 임의로 변경되지 않았는지 검증합니다.

예:

주문 금액 = 210,000원
요청 결제 금액 = 209,000원
             ↓
         금액 불일치
             ↓
          결제 거부

이를 통해 클라이언트에서 결제 금액을 임의로 변경하여 요청하는 상황을 방지했습니다.

결제 성공 후 처리

결제가 승인되면 다음 작업을 하나의 흐름으로 처리합니다.

Payment → PAID
Order   → COMPLETED
장바구니 → 주문 상품 삭제

현재 프로젝트에서는 모의 결제 성공 흐름을 중심으로 구현했으며, 별도의 외부 PG 연동이나 실제 결제 실패 시나리오는 구현 범위에 포함하지 않았습니다.

❌ 주문 취소

주문 상태가 PAYMENT_PENDING 또는 COMPLETED인 경우 주문을 취소할 수 있습니다.

PAYMENT_PENDING ──→ CANCELED
COMPLETED ─────────→ CANCELED

주문 취소 시 다음 작업을 처리합니다.

주문 소유권 확인
      ↓
취소 가능 상태 확인
      ↓
Order = CANCELED
      ↓
주문 수량만큼 재고 복구
      ↓
결제 완료 주문이면 결제 취소

이미 취소된 주문을 다시 취소하는 경우 예외를 발생시켜 재고가 중복으로 복구되지 않도록 처리했습니다.

⚡ CH5 성능 및 안정성 개선

기본 기능 구현 이후 데이터 규모와 조회 요청 증가 상황을 고려하여 다음 주제로 추가 개선을 진행했습니다.

주제	내용
QueryDSL	상품 검색 조건의 동적 쿼리 구현
Index	대량 데이터 환경에서 실행 계획 및 인덱스 비교
Cache	상품 조회 결과 캐싱
Concurrency	동시 요청 상황에서 데이터 정합성 문제 해결

1. QueryDSL을 활용한 동적 검색

상품 목록 조회에서 요청에 따라 검색 조건이 달라지는 문제를 QueryDSL을 활용하여 처리했습니다.

category가 있으면
    ↓
category 조건 추가

minPrice가 있으면
    ↓
최소 가격 조건 추가

maxPrice가 있으면
    ↓
최대 가격 조건 추가

조건이 없는 경우 해당 조건을 제외하여 하나의 조회 로직으로 다양한 검색 요청을 처리할 수 있도록 구성했습니다.

예:

GET /api/products

GET /api/products?category=ELECTRONICS

GET /api/products?minPrice=30000

GET /api/products?category=ELECTRONICS&minPrice=30000&maxPrice=70000

2. Index를 활용한 쿼리 성능 개선
실험 목적

상품 데이터가 적은 환경에서는 인덱스 적용 전후의 차이가 크지 않을 수 있기 때문에 대량의 데이터를 생성하여 실행 계획과 실제 실행 시간을 비교했습니다.

최종적으로 products 테이블에 150,006건의 데이터를 구성하여 실험했습니다.

테스트 데이터
전체 상품: 150,006건

ELECTRONICS: 50,007건
FASHION:      50,001건
FOOD:         49,998건

가격 범위: 10,000 ~ 200,000

데이터는 카테고리와 가격을 분산시켜 가격 조건의 선택도에 따른 실행 계획 차이를 확인할 수 있도록 구성했습니다.

대상 쿼리

카테고리와 가격 범위로 상품을 검색하고, 최신 상품부터 20개를 조회하는 상품 목록 쿼리를 대상으로 실험했습니다.

SELECT
    id,
    name,
    category,
    price,
    stock_quantity
FROM products
WHERE category = 'ELECTRONICS'
  AND price BETWEEN 50000 AND 60000
ORDER BY created_at DESC, id DESC
LIMIT 20;

가격 조건의 범위를 좁은 경우와 넓은 경우로 나누어 비교했습니다.

좁은 가격 범위
50,000 ~ 60,000
넓은 가격 범위
10,000 ~ 200,000
실행 계획 확인

MySQL의 EXPLAIN과 EXPLAIN ANALYZE를 활용하여 다음 항목을 확인했습니다.

type
key
rows
Extra
실제 실행 시간
실제 처리된 행 수

EXPLAIN의 rows는 옵티마이저가 예상한 행 수이며, EXPLAIN ANALYZE의 actual rows와 actual time은 실제 실행 결과입니다.

인덱스 적용 전

인덱스가 없는 상태에서는 좁은 가격 범위에서도 전체 테이블을 스캔했습니다.

type  = ALL
key   = NULL
Extra = Using where; Using filesort

EXPLAIN ANALYZE 결과:

Table scan
actual rows  = 150,006
actual time  ≈ 54.7 ms

전체 상품 데이터를 조회한 후 조건에 맞는 데이터를 필터링하고, ORDER BY를 위해 추가 정렬이 발생했습니다.

후보 인덱스 1
(category, price)
CREATE INDEX idx_products_category_price
ON products (category, price);

카테고리와 가격 조건을 함께 사용하는 쿼리에서 가격 범위 검색을 효율적으로 처리할 수 있는 인덱스입니다.

좁은 가격 범위 결과
type  = range
key   = idx_products_category_price
Extra = Using index condition; Using filesort

실제 실행 결과:

인덱스 없음
actual rows ≈ 150,006
actual time ≈ 54.7 ms

(category, price)
actual rows ≈ 2,882
actual time ≈ 18.4 ms

가격 범위가 좁아지면서 인덱스를 통해 읽어야 하는 데이터가 크게 감소했습니다.

다만 created_at을 이용한 정렬 조건은 해당 인덱스에 포함되어 있지 않기 때문에 Using filesort가 발생했습니다.

넓은 가격 범위

가격 범위를

10,000 ~ 200,000

으로 넓혔을 때는 선택도가 낮아졌습니다.

이 경우 MySQL 옵티마이저는 인덱스 범위 검색 대신 전체 테이블 스캔을 선택했습니다.

Table scan
actual rows = 150,006

즉, 인덱스가 존재한다고 해서 항상 인덱스를 사용하는 것은 아니며, 검색 조건의 선택도에 따라 옵티마이저가 실행 계획을 선택한다는 것을 확인했습니다.

후보 인덱스 2
(category, created_at)
CREATE INDEX idx_products_category_created_at
ON products (category, created_at);

이 인덱스는 카테고리 조건으로 범위를 좁힌 후 created_at을 기준으로 최신 데이터를 탐색하는 데 유리합니다.

좁은 가격 범위 결과
type  = ref
key   = idx_products_category_created_at
Extra = Using where; Backward index scan

실제 실행 결과:

actual rows ≈ 279
actual time ≈ 1.39 ms

최신 데이터부터 역방향으로 인덱스를 탐색하면서 가격 조건을 확인하고 LIMIT 20을 만족하면 탐색을 종료할 수 있었습니다.

넓은 가격 범위 결과

넓은 가격 범위에서는 최신 상품부터 조회했을 때 가격 조건을 만족하는 상품이 빠르게 발견되었습니다.

actual rows ≈ 20
actual time ≈ 0.4 ms

LIMIT 20을 빠르게 만족하면서 추가적인 데이터 탐색을 줄일 수 있었습니다.

인덱스 비교 결과
조건	인덱스	실행 계획	주요 특징
좁은 가격 범위	없음	ALL	전체 테이블 스캔
좁은 가격 범위	(category, price)	range	가격 범위 검색에 유리
좁은 가격 범위	(category, created_at)	ref	최신 데이터 탐색 + LIMIT에 유리
넓은 가격 범위	(category, price)	Table scan	낮은 선택도로 인해 테이블 스캔 선택
넓은 가격 범위	(category, created_at)	ref	최신 데이터부터 탐색 후 LIMIT으로 조기 종료
최종 인덱스

이번에 테스트한 상품 목록 조회 패턴에서는 다음 인덱스를 최종적으로 사용했습니다.

CREATE INDEX idx_products_category_created_at
ON products (category, created_at);

이번 실험에서는 단순히 가격 범위 검색만 빠르게 처리하는 것보다

category 조건
    +
최신순 정렬
    +
LIMIT 20

을 함께 효율적으로 처리하는 것이 실제 조회 패턴에 더 적합하다는 결과를 확인했습니다.

특히 Backward index scan을 통해 최신 상품부터 탐색하면서 LIMIT 20을 만족하는 시점에 조회를 종료할 수 있었습니다.

위 결과는 150,006건의 테스트 데이터와 해당 조회 쿼리를 기준으로 측정한 결과입니다.
데이터 분포, 서버 환경, 캐시 상태 및 실제 서비스의 조회 패턴에 따라 실행 계획과 성능은 달라질 수 있습니다.

3. Cache를 활용한 조회 성능 개선

상품 목록처럼 동일한 조건의 조회가 반복될 수 있는 API에 캐시를 적용했습니다.

상품 조회 요청
      ↓
   Cache 확인
   ├── HIT
   │    ↓
   │  캐시 데이터 반환
   │
   └── MISS
        ↓
      DB 조회
        ↓
      캐시 저장
        ↓
      응답

반복적인 상품 조회 요청에서 데이터베이스 접근을 줄여 조회 성능을 개선할 수 있도록 구성했습니다.

4. 동시성 제어

재고와 같이 여러 요청에서 동시에 변경될 수 있는 데이터는 동시 요청에 따른 데이터 정합성 문제가 발생할 수 있습니다.

예를 들어 재고가 1개 남은 상품에 동시에 여러 주문 요청이 들어오는 경우:

재고 = 1

요청 A → 재고 확인
요청 B → 재고 확인

동시에 재고 변경
       ↓
재고 정합성 문제 발생 가능

CH5에서는 이러한 동시 요청 상황에서 발생할 수 있는 재고 데이터 정합성 문제를 확인하고 동시성 제어를 적용했습니다.

🧪 테스트 및 검증

주요 기능은 API 요청을 통해 정상 및 예외 상황을 검증했습니다.

주문
정상적인 주문 생성
재고 부족 시 전체 롤백
주문 당시 상품 가격 스냅샷 유지
주문 생성 후 장바구니 유지
결제
정상 결제 승인
결제 금액 변조 방지
결제 성공 후 주문 상태 변경
결제 성공 후 주문 상품 장바구니 삭제
중복 결제 방지
주문 취소
결제 전 주문 취소
결제 완료 주문 취소
취소 시 재고 복구
결제 완료 주문의 결제 취소
중복 취소 방지
접근 제어
인증되지 않은 사용자의 보호 API 접근 차단
다른 회원의 주문 접근 차단
다른 회원의 장바구니 접근 차단
다른 회원의 결제 정보 접근 제한

🔄 주문 상태
PAYMENT_PENDING
       │
       ├── 결제 성공 ──→ COMPLETED
       │
       └── 주문 취소 ──→ CANCELED

COMPLETED
       │
       └── 주문 취소 ──→ CANCELED
결제 상태
PENDING
   │
   ├── 결제 승인 ──→ PAID
   │
   └── 결제 취소 ──→ CANCELLED

🌿 Git 협업 방식

GitHub를 이용하여 기능별 브랜치와 Pull Request 기반으로 협업했습니다.

main
  │
  └── develop
        │
        └── feature/*

작업 흐름
develop 최신화
      ↓
기능 브랜치 생성
      ↓
기능 구현
      ↓
Commit
      ↓
Pull Request
      ↓
리뷰 및 승인
      ↓
develop Merge

develop 브랜치에는 Pull Request를 통해서만 Merge할 수 있도록 설정하고, 최소 1명의 승인을 받은 후 Merge하는 방식으로 협업했습니다.

🚀 실행 방법
1. Repository Clone

git clone https://github.com/kyungsik1995-afk/plus-project-team3.git
cd plus-project-team3

2. 데이터베이스 생성

MySQL에서 데이터베이스를 생성합니다.

CREATE DATABASE plus_project_team3;

3. 환경 변수 설정

애플리케이션 실행 환경에 다음 환경 변수를 설정합니다.

MYSQL_PASSWORD=MySQL 비밀번호
JWT_SECRET=JWT 서명용 Secret Key

4. 애플리케이션 실행
./gradlew bootRun

기본 실행 주소:

http://localhost:8080

👥 팀원
이름	담당
임경식	주문 / Index
강성현	상품, 장바구니 / Cache
이상민  결제 / 동시성 제어
이건희  회원,인증 / QueryDSL

📚 프로젝트를 통해 경험한 것

커머스 도메인
상품 → 장바구니 → 주문 → 결제로 이어지는 커머스 핵심 흐름 구현
주문 및 결제 상태 관리
재고 차감 및 주문 취소 시 재고 복구
주문 당시 상품 정보 스냅샷 관리

데이터 정합성
@Transactional을 활용한 주문 생성 과정의 원자성 보장
재고 부족 시 전체 롤백
결제 금액 검증
중복 결제 및 중복 주문 취소 방지
회원별 데이터 소유권 검증

쿼리 및 성능
QueryDSL을 활용한 동적 검색 조건 구현
EXPLAIN을 활용한 실행 계획 분석
EXPLAIN ANALYZE를 활용한 실제 실행 시간 및 처리 행 분석
복합 인덱스 (category, price)와 (category, created_at) 비교
선택도에 따른 MySQL 옵티마이저의 실행 계획 변화 확인
Cache를 활용한 반복 조회 성능 개선

협업
Git 기반 브랜치 운영
GitHub Pull Request를 통한 코드 리뷰
브랜치 보호 규칙 적용
충돌 해결 및 develop 최신화 경험
