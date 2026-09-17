# 실제 Backend 테스트 Frontend

현재 저장소의 Spring Controller, DTO, Service, Security 계약에 맞춘 React + Vite + JavaScript 테스트 화면입니다. 모든 기능은 실제 `/api` 요청을 사용하며 Mock 데이터나 새 Backend API는 없습니다.

## 실행

Node.js 22.12 이상을 권장합니다.

```powershell
cd frontend
pnpm install --frozen-lockfile
pnpm dev
```

브라우저에서 `http://127.0.0.1:5173/`에 접속합니다. Vite가 `/api`를 기본 `http://localhost:8080`으로 전달하므로 Spring CORS 코드를 수정하지 않습니다. 다른 Backend 주소는 `.env.example`을 `frontend/.env.local`로 복사하고 `BACKEND_URL`만 변경한 뒤 Vite를 재시작합니다.

```powershell
pnpm build
pnpm preview
```

## 사용 중인 실제 API

회원가입과 로그인 이외의 API는 `Authorization: Bearer <accessToken>`을 사용합니다.

| Domain | Method | URL | Request | 정상 Status |
|---|---|---|---|---:|
| Member | POST | `/api/members/signup` | `{email,password,name,phoneNumber}` | 200 |
| Member | POST | `/api/members/login` | `{email,password}` | 200 |
| Member | GET | `/api/members/me` | 없음 | 200 |
| Product | GET | `/api/products` | category, minPrice, maxPrice, page, size | 200 |
| Product | GET | `/api/v2/products` | category, minPrice, maxPrice, page, size | 200 |
| Product | GET | `/api/products/{productId}` | productId | 200 |
| Product | PATCH | `/api/products/{productId}` | name, category, price, description 중 하나 이상 | 200 |
| Cart | POST | `/api/carts/items` | `{productId,quantity}` | 200 |
| Cart | GET | `/api/carts` | 없음 | 200 |
| Cart | PATCH | `/api/carts/items/{cartItemId}` | `{quantity}` | 200 |
| Cart | DELETE | `/api/carts/items/{cartItemId}` | 없음 | 200 |
| Cart | DELETE | `/api/carts` | 없음 | 204 |
| Order | POST | `/api/orders` | `{cartItemIds}`; 빈 배열은 Cart 전체 | 201 |
| Order | GET | `/api/orders` | 없음 | 200 |
| Order | GET | `/api/orders/{orderId}` | orderId | 200 |
| Order | PATCH | `/api/orders/{orderId}/cancel` | 없음 | 200 |
| Payment | POST | `/api/payments/confirm` | `{orderId,paymentPrice}` | 200 |
| Payment | GET | `/api/payments/{paymentId}` | paymentId | 200 |

Refund Controller는 존재하지만 현재 Frontend 검증 범위에서 제외했습니다. 명시적 Payment FAIL, 실제 PG 실패, 부분 환불 정상 흐름, Refund FAILED를 정상 동작처럼 만들지 않습니다.

공통 성공 응답은 `{success:true,data:...}`, 공통 실패 응답은 `{success:false,error:{code,message}}`입니다. Security 또는 proxy 응답이 이 구조와 다르면 실제 HTTP Status와 원문을 그대로 표시합니다.

## 화면 기능

- 기본 화면: 로그인, Product v2 필터·페이지·상세, Cart 추가·조회·수량 변경·삭제·전체 비우기.
- 주문 화면: 실제 CartItem 선택 주문, 주문 목록·상세 조회, 주문 취소.
- 결제 화면: 선택한 주문의 서버 저장 금액으로 결제 승인, 승인 응답의 실제 paymentId로 결제 조회.
- 검증 안내: 우측 Drawer에서 현재 화면으로 확인할 수 있는 시나리오와 Backend 별도 검증 범위를 안내.
- 진단: 최근 HTTP 요청 30건의 Request, Response, Status, ErrorCode, message, 소요 시간을 메모리에 유지.

회원 비밀번호, JWT, Authorization 값은 HTTP 진단에 저장하기 전에 마스킹합니다. JWT와 기록은 브라우저 메모리에만 있으며 새로고침하면 사라집니다. `memberId`나 `cartId`를 요청에 추가하지 않습니다.

주문 생성·취소와 결제 승인은 실행 전 확인 대화상자를 표시합니다. 네트워크 실패나 timeout은 자동 재시도하지 않습니다. 쓰기 처리 여부는 후속 GET으로 확인합니다.

## 판정 범위

Frontend는 공개 HTTP 계약과 재조회 가능한 상태만 판정합니다. 주문 생성 시 재고 기대값과 실제값, 결제 후 CartItem 기대값과 실제값이 다르면 그대로 실패로 표시합니다. Backend 결함을 보정하지 않습니다.

다음 항목은 화면에 `Frontend만으로 검증 불가`로 표시합니다: 명시적 Payment FAIL, 실제 PG 실패, 부분 환불 정상 흐름, Refund FAILED, Cache HIT/MISS 확정, SQL Query 수, Index 효과, Lock 획득 순서, 트랜잭션 내부 상태, Deadlock 안전성. 이 항목은 Backend 통합 테스트와 DB 관찰이 필요합니다.

## 파일 역할

- `src/main.jsx`: 로그인, Product/Cart 화면과 공통 세션 상태.
- `src/Checkout.jsx`: 실제 CartItem 기반 Order/Payment 커머스 흐름.
- `src/Diagnostics.jsx`: 최근 30건 HTTP 기록과 기대값/실제값 판정.
- `src/api.js`: 실제 fetch, JWT, 응답 파싱, timeout, 마스킹, 진단 이벤트.
- `vite.config.js`: 개발 및 preview `/api` proxy.

기존 Spring 파일, 설정, 테스트, Gradle 파일은 수정하지 않습니다.
