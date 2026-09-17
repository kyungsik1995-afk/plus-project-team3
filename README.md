# 🛒 Plus Project Team 3

Spring Boot 기반의 **커머스 결제 시스템** 팀 프로젝트입니다.

회원, 상품, 장바구니, 주문, 결제 기능을 구현하고,
대용량 데이터와 실제 서비스에서 발생할 수 있는 **조회 성능, 캐시, 동시성 문제**를 고려하여 QueryDSL, Index, Cache, 동시성 제어를 적용했습니다.

---

## 👥 팀원 및 역할

| 이름      | 담당 도메인   | CH5 프로젝트 |
| ------- | -------- | --------- |
| **임경식** | 주문       | Index     |
| **강성현** | 상품, 장바구니 | Cache     |
| **이건희** | 회원, 인증   | QueryDSL  |
| **이상민** | 결제       | 동시성 제어    |

각 팀원은 담당 도메인의 기능을 구현하고, CH5에서는 담당한 심화 과제를 진행하여 기능 구현과 성능 및 안정성 개선을 함께 경험했습니다.

---

# 📌 프로젝트 개요

| 항목       | 내용                          |
| -------- | --------------------------- |
| 프로젝트     | Plus Project Team 3         |
| 개발 인원    | 4명                          |
| Backend  | Java 17 / Spring Boot 4.1.1 |
| Database | MySQL 8                     |
| Build    | Gradle                      |
| 주요 목표    | 커머스 결제 시스템 구현 및 성능·동시성 개선   |

### 프로젝트 목표

기본적인 커머스 기능 구현을 넘어 다음과 같은 상황을 고려했습니다.

* 다양한 조건의 상품 검색
* 대량 상품 데이터에서의 조회 성능
* 반복적인 상품 목록 조회에 대한 DB 부하
* 동시에 발생하는 재고 차감 및 복구
* 결제 금액 변조 방지
* 주문과 결제 상태의 일관성
* 환불에 따른 재고 복구
* 테스트 환경과 개발 환경의 데이터 분리

---

# 🛠️ 기술 스택

### Backend

* Java 17
* Spring Boot 4.1.1
* Spring MVC
* Spring Data JPA
* Spring Security
* JWT
* QueryDSL 5.1.0

### Database / Cache

* MySQL 8
* Caffeine Cache

### Build / Test

* Gradle
* JUnit
* Spring Boot Test

---

# 📂 프로젝트 구조

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
    │   ├── cache
    │   └── jpa
    ├── security
    │   └── jwt
    └── exception
        ├── business
        └── handler
```

도메인별로 Controller, Service, Repository, Entity, DTO를 분리했습니다.

주문과 결제처럼 여러 도메인의 흐름을 조합해야 하는 기능에는 Facade를 사용했습니다.

---

# 👤 5. 회원 / 인증

**담당: 이건희**

회원 도메인과 인증 기능을 담당하여 사용자의 회원가입 및 로그인부터 JWT 기반 인증까지 구현했습니다.

### 주요 기능

* 회원가입
* 로그인
* 비밀번호 암호화
* JWT 발급
* JWT 인증
* 인증 사용자 식별
* 권한 검증

### 인증 흐름

```text
로그인
  ↓
회원 정보 확인
  ↓
JWT 발급
  ↓
Client
  ↓
Authorization Header
  ↓
JwtAuthenticationFilter
  ↓
JWT 검증
  ↓
인증 사용자 설정
  ↓
Controller 접근
```

인증이 필요한 API에서는 JWT를 검증하여 현재 요청을 보낸 사용자를 식별하도록 구성했습니다.

또한 다른 사용자의 주문이나 장바구니 등에 접근하지 못하도록 사용자 소유권을 검증했습니다.

---

# 📦 6. 상품 / 장바구니

**담당: 강성현**

상품과 장바구니 도메인을 담당하여 상품 관리부터 주문을 위한 장바구니 기능까지 구현했습니다.

## 상품

### 주요 기능

* 상품 목록 조회
* 카테고리 검색
* 가격 범위 검색
* 페이징
* 상품 상세 조회
* 상품 수정
* 재고 관리

상품 목록에서는 카테고리와 가격 범위를 이용한 검색 조건을 제공하고, 페이징을 통해 필요한 데이터만 조회하도록 구성했습니다.

## 장바구니

### 주요 기능

* 장바구니 상품 추가
* 수량 수정
* 장바구니 상품 삭제
* 장바구니 조회
* 주문 상품의 장바구니 삭제

결제 완료 시 주문한 상품만 장바구니에서 삭제하고, 주문하지 않은 상품은 그대로 유지하도록 구성했습니다.

### 상품 → 장바구니 → 주문 흐름

```text
상품 조회
   ↓
장바구니 상품 추가
   ↓
장바구니 조회
   ↓
주문할 상품 선택
   ↓
주문 생성
```

---

# 🧾 7. 주문

**담당: 임경식**

주문 도메인을 담당하여 장바구니 상품을 실제 주문으로 전환하고 주문 상태 및 주문 상품 정보를 관리하도록 구현했습니다.

### 주요 기능

* 주문 생성
* 주문 목록 조회
* 주문 상세 조회
* 주문 취소
* 주문 당시 상품명 및 가격 저장
* 주문 상태 관리
* 주문 생성 시 재고 차감
* 주문 취소 시 재고 복구

### 주문 생성 흐름

```text
회원 확인
   ↓
장바구니 조회
   ↓
주문 상품 선택
   ↓
상품 및 재고 확인
   ↓
재고 차감
   ↓
OrderItem 생성
   ↓
주문 금액 계산
   ↓
Order 생성
   ↓
Payment 생성
```

주문 생성 직후에는 결제가 완료되지 않은 상태이므로 다음 상태로 관리합니다.

```text
Order   = PAYMENT_PENDING
Payment = PENDING
```

### 주문 가격 스냅샷

주문 생성 당시의 상품명과 가격을 `OrderItem`에 저장합니다.

```text
현재 상품 가격
      ↓
주문 생성
      ↓
OrderItem에 주문 당시 가격 저장
```

이후 상품 가격이 변경되더라도 기존 주문의 가격에는 영향을 주지 않도록 했습니다.

### 주문 취소

```text
PAYMENT_PENDING ──→ CANCELED
COMPLETED ─────────→ CANCELED
```

주문 취소 시 주문 수량만큼 재고를 복구하고, 결제가 완료된 주문이라면 결제 취소 흐름과 연계하도록 구성했습니다.

---

# 💳 8. 결제

**담당: 이상민**

결제 도메인을 담당하여 주문 생성 이후의 결제 승인부터 결제 취소 및 환불까지 구현했습니다.

### 주요 기능

* 결제 생성
* 결제 승인
* 결제 금액 검증
* 결제 상태 관리
* 결제 취소
* 전체 환불
* 부분 환불

### 결제 승인 흐름

```text
Payment 조회
      ↓
결제 금액 검증
      ↓
Payment 승인
      ↓
Order 완료
      ↓
주문 상품 장바구니 삭제
```

결제 승인 시 주문 금액과 결제 금액을 검증하여 클라이언트에서 전달한 금액을 그대로 신뢰하지 않도록 했습니다.

### 결제 상태

```text
PENDING
   ↓
PAID
   ↓
PART_CANCELLED
   ↓
CANCELLED
```

실제 상태 전이는 결제 및 환불 상황에 따라 처리합니다.

### 전체 환불

전체 환불 시 환불 금액과 기존 환불 내역을 확인하고, 주문 상품의 환불 수량만큼 재고를 복구합니다.

### 부분 환불

부분 환불에서는 다음 사항을 검증합니다.

* 환불 대상 상품 존재 여부
* 환불 수량
* 이미 환불된 수량
* 잔여 수량 초과 여부
* 환불 금액
* 누적 환불 금액 초과 여부

부분 환불이 남아 있는 경우 결제 상태를 `PART_CANCELLED`로 관리하고, 전체 환불이 완료되면 결제를 취소 상태로 변경합니다.

---

# ⚡ 9. QueryDSL 동적 쿼리

**담당: 이건희**

상품 목록 조회에서 여러 검색 조건을 동적으로 처리하기 위해 QueryDSL을 적용했습니다.

### 적용 조건

* 카테고리
* 최소 가격
* 최대 가격

각 조건이 전달되지 않은 경우 해당 조건을 제외하도록 동적으로 구성했습니다.

```java
private BooleanExpression categoryEq(ProductCategory category) {
    return category == null ? null : product.category.eq(category);
}

private BooleanExpression priceGoe(Long minPrice) {
    return minPrice == null ? null : product.price.goe(minPrice);
}

private BooleanExpression priceLoe(Long maxPrice) {
    return maxPrice == null ? null : product.price.loe(maxPrice);
}
```

### Repository 구조

```text
ProductRepository
        │
        └── ProductRepositoryCustom
                    │
                    ▼
          ProductRepositoryImpl
                    │
                    ▼
             JPAQueryFactory
                    │
                    ▼
                 QueryDSL
```

### 페이징 및 정렬

```java
.orderBy(
    product.createdAt.desc(),
    product.id.desc()
)
.offset(pageable.getOffset())
.limit(pageable.getPageSize())
```

QueryDSL을 통해 검색 조건, 정렬, 페이징을 하나의 동적 쿼리로 구성했습니다.

---

# 📈 10. Index 성능 개선

**담당: 임경식**

대량의 상품 데이터에서 상품 목록 조회 성능을 확인하고, `EXPLAIN`과 `EXPLAIN ANALYZE`를 활용하여 Index 설계에 따른 실행 계획과 성능을 비교했습니다.

## 테스트 데이터

상품 테이블에 **150,006건**의 데이터를 생성했습니다.

```text
ELECTRONICS : 50,007
FASHION     : 50,001
FOOD        : 49,998
```

## 테스트 쿼리

```sql
SELECT id, name, category, price, stock_quantity
FROM products
WHERE category = 'ELECTRONICS'
  AND price BETWEEN 50000 AND 60000
ORDER BY created_at DESC, id DESC
LIMIT 20;
```

## 비교한 Index

### `(category, price)`

```sql
CREATE INDEX idx_products_category_price
ON products (category, price);
```

### `(category, created_at)`

```sql
CREATE INDEX idx_products_category_created_at
ON products (category, created_at);
```

## 실행 계획 비교

Index가 없는 상태에서는:

```text
type  = ALL
key   = NULL
Extra = Using where; Using filesort
```

형태의 실행 계획이 확인되었습니다.

### 측정 결과

| 조건       | Index                    |       결과 |
| -------- | ------------------------ | -------: |
| 좁은 가격 범위 | 없음                       | 약 54.7ms |
| 좁은 가격 범위 | `(category, price)`      | 약 18.4ms |
| 좁은 가격 범위 | `(category, created_at)` | 약 1ms 내외 |
| 넓은 가격 범위 | `(category, created_at)` | 약 1ms 내외 |

측정값은 로컬 환경에서 동일한 데이터셋과 쿼리 조건을 기준으로 한 결과이며, 실제 환경에서는 데이터 분포와 DB 상태 등에 따라 달라질 수 있습니다.

### Index 설계 과정에서 확인한 내용

* Leftmost Prefix Rule
* 컬럼의 선택도
* 데이터 분포
* WHERE 조건
* ORDER BY
* LIMIT
* Index 유지 비용
* 범위 검색과 정렬의 관계

특히 Index가 존재한다고 해서 항상 사용되는 것은 아니며, MySQL 옵티마이저가 예상 비용을 기준으로 실행 계획을 선택한다는 점을 확인했습니다.

이번 테스트에서는 **카테고리 조건으로 범위를 좁힌 후 최신 상품순으로 `LIMIT 20`건을 가져오는 조회 특성**을 고려하여 다음 Index를 적용했습니다.

```text
(category, created_at)
```

> 해당 결과는 이번 테스트 데이터와 쿼리 패턴을 기준으로 한 것이며, 모든 조회 환경에서 동일한 Index가 최적이라는 의미는 아닙니다.

---

# 💾 11. Cache 성능 개선

**담당: 강성현**

반복적인 상품 목록 조회에서 발생하는 DB 접근을 줄이기 위해 **Caffeine Cache**를 적용했습니다.

## Cache 설정

```text
Cache Name   : productListCache
Maximum Size : 500
Expiration   : 5 minutes
Statistics   : recordStats()
Null Value   : disabled
```

### 적용 API

일반 상품 목록 조회:

```text
GET /api/products
```

캐시 적용 상품 목록 조회:

```text
GET /api/v2/products
```

### 캐시 구조

```text
GET /api/v2/products
        ↓
Caffeine Cache
   ├── Hit
   │    ↓
   │  캐시 데이터 사용
   │
   └── Miss
        ↓
      DB 조회
        ↓
      Cache 저장
        ↓
상품 ID 목록 추출
        ↓
DB에서 최신 재고 조회
        ↓
최종 응답
```

### 캐시 대상

캐시에는 상품의 변경 빈도와 조회 특성을 고려하여 다음 정보를 저장합니다.

```text
productId
name
category
price
```

재고는 캐시하지 않고 DB에서 최신 값을 조회하도록 구성했습니다.

이를 통해 상품 정보는 캐시를 통해 반복 조회를 줄이고, 재고는 최신 상태를 조회할 수 있도록 분리했습니다.

### Cache 무효화

상품 정보가 수정되면 상품 목록 캐시를 전체 무효화합니다.

```java
@CacheEvict(
    cacheNames = PRODUCT_LIST_CACHE,
    allEntries = true
)
```

이를 통해 상품 수정 후 이전 상품 정보가 캐시에 남아 있는 문제를 방지했습니다.

---

# 🔒 12. 동시성 제어

**담당: 이상민**

여러 사용자가 동시에 같은 상품을 주문하거나 환불하는 상황에서 재고 정합성을 유지하기 위해 **비관적 락(Pessimistic Lock)**을 적용했습니다.

## Pessimistic Write Lock

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("select p from Product p where p.id = :productId")
Optional<Product> findByIdWithLock(
        @Param("productId") Long productId
);
```

### 주문 시

```text
주문 요청
   ↓
상품 조회
   ↓
PESSIMISTIC_WRITE 락 획득
   ↓
재고 확인
   ↓
재고 차감
   ↓
OrderItem 생성
```

### 환불 시

```text
환불 요청
   ↓
상품 조회
   ↓
PESSIMISTIC_WRITE 락 획득
   ↓
환불 수량 확인
   ↓
재고 복구
```

주문과 환불에서 상품 재고를 변경하기 전에 동일한 비관적 락 전략을 사용하도록 구성했습니다.

---

# 🔄 주문 / 결제 통합 흐름

각 담당 도메인은 독립적으로 구현하는 동시에 실제 서비스에서는 하나의 흐름으로 연결되도록 구성했습니다.

```text
회원 / 인증
    ↓
상품 조회
    ↓
장바구니
    ↓
주문 생성
    ↓
재고 차감
    ↓
결제 생성
    ↓
결제 승인
    ↓
주문 완료
    ↓
주문 상품 장바구니 삭제
```

취소 및 환불의 경우:

```text
주문 / 결제
    ↓
취소 또는 환불
    ↓
환불 처리
    ↓
재고 복구
    ↓
주문 / 결제 상태 변경
```

---

# 🧪 테스트 및 검증

## 자동화 테스트

다음 영역에 대한 테스트를 구성했습니다.

```text
PlusProjectTeam3ApplicationTests
CartServiceTest
ProductTest
ProductRepositoryTest
ProductCacheServiceTest
ProductServiceTest
```

### 주요 테스트 영역

* Product Entity
* Product Service
* Product Repository
* QueryDSL 조회
* Product Cache
* Cart Service

---

# 🔎 API 통합 검증

주요 비즈니스 흐름은 API를 통해 별도로 통합 검증했습니다.

### 주문

* 정상 주문 생성
* 선택 상품 주문
* 재고 부족 처리
* 재고 차감 롤백
* 주문 당시 가격 저장
* 주문 목록 조회
* 주문 상세 조회
* 다른 사용자의 주문 접근 차단

### 결제

* 결제 승인
* 결제 금액 변조 검증
* 주문 완료 상태 변경
* 중복 결제 방지
* 결제 성공 후 주문 상품 장바구니 삭제

### 취소 / 환불

* 주문 취소
* 재고 복구
* 결제 취소
* 전체 환불
* 부분 환불
* 환불 수량 검증
* 환불 금액 검증
* 중복 취소 방지

---

# 🌿 Git 협업

Feature Branch 기반으로 작업하고 Pull Request를 통해 `develop` 브랜치에 병합하는 방식으로 협업했습니다.

```text
main
  │
  └── develop
        │
        ├── feature/member
        ├── feature/product
        ├── feature/cart
        ├── feature/order
        ├── feature/payment
        ├── feature/querydsl
        ├── feature/cache
        ├── feature/index
        └── feature/concurrency
```

`main` 브랜치에는 Pull Request를 통해 병합할 수 있도록 브랜치 보호 규칙을 적용하고, 코드 변경 사항을 PR 단위로 검토했습니다.

---

# 📊 CH5 프로젝트 정리

| 담당자     | 담당 도메인    | 심화 과제        | 핵심 내용                                      |
| ------- | --------- | ------------ | ------------------------------------------ |
| **임경식** | 주문        | **Index**    | 대량 데이터, EXPLAIN, EXPLAIN ANALYZE, Index 비교 |
| **강성현** | 상품 / 장바구니 | **Cache**    | Caffeine Cache, 캐시 대상 분리, Cache Evict      |
| **이건희** | 회원 / 인증   | **QueryDSL** | 동적 검색 조건, 페이징, 정렬                          |
| **이상민** | 결제        | **동시성 제어**   | Pessimistic Lock, 재고 정합성                   |

---

# 💡 프로젝트를 통해 배운 점

이번 프로젝트에서는 각자 담당 도메인을 구현하는 것뿐만 아니라, 서로 다른 도메인이 연결되는 과정에서 발생하는 문제까지 함께 경험했습니다.

### 도메인 설계

회원 → 상품 → 장바구니 → 주문 → 결제로 이어지는 전체 서비스 흐름을 구현하며 도메인 간 책임과 연결 관계를 이해했습니다.

### 성능

QueryDSL과 Index를 적용하면서 단순히 기능을 구현하는 것을 넘어 **실제 쿼리가 어떻게 실행되는지 확인하고 성능을 분석하는 과정**을 경험했습니다.

### Cache

반복적인 DB 조회를 줄이기 위해 캐시를 적용하고, 모든 데이터를 캐시하는 것이 아니라 **최신성이 필요한 데이터와 캐시 가능한 데이터를 구분**했습니다.

### 동시성

동시에 같은 상품의 재고를 변경하는 상황을 고려하면서 데이터 정합성을 유지하기 위한 DB 수준의 동시성 제어를 경험했습니다.

### 협업

각자의 담당 영역을 나누어 개발하면서도 도메인 간 연결이 필요한 부분은 서로 협의하고 Pull Request를 통해 코드를 통합했습니다.

---

# 🚀 실행 방법

## 1. Clone

```bash
git clone https://github.com/kyungsik1995-afk/plus-project-team3.git
cd plus-project-team3
```

## 2. Database 생성

```sql
CREATE DATABASE plus_project_team3;
CREATE DATABASE plus_project_team3_test;
```

## 3. 환경 변수 설정

```text
MYSQL_PASSWORD=MySQL 비밀번호
JWT_SECRET=JWT Secret Key
```

## 4. 프로젝트 실행

```bash
./gradlew bootRun
```

또는 IntelliJ에서 Spring Boot Application을 실행합니다.

---

# 📌 프로젝트 핵심 요약

```text
                Plus Project Team 3

                     회원 / 인증
                         │
                         ▼
                   상품 / 장바구니
                         │
                         ▼
                       주문
                         │
                         ▼
                       결제
                         │
                         ▼
                     주문 완료

        ┌──────────────┼──────────────┐
        ▼              ▼              ▼
     QueryDSL         Index          Cache
        │              │              │
     동적 조회       조회 성능       DB 부하 감소
                                      

                  동시성 제어
                       │
                       ▼
                  재고 정합성
```

**4명의 도메인 담당자가 각자의 영역을 구현하고, QueryDSL / Index / Cache / 동시성 제어를 통해 기능 구현을 넘어 성능과 데이터 정합성까지 고려한 커머스 결제 시스템을 구축했습니다.**
