import React, { useEffect, useState } from 'react';
import { redact } from './api.js';

export const scenarios = {
  login: '정상 로그인', loginFailure: '로그인 실패', signup: '정상 회원가입', duplicate: '중복 이메일', signupValidation: '회원가입 DTO Validation', me: '내 정보',
  compare: 'v1/v2 동일 조건 응답 비교', patch: 'Product 정상 수정', emptyPatch: '빈 PATCH', badPrice: '잘못된 가격', missingProduct: '존재하지 않는 상품', patchRead: '수정 후 상세 재조회',
  cartSum: '동일 상품 두 번 추가 → 합산', cartFinal: 'Cart PATCH 최종 수량', cartStock: '재고 초과 오류', cartUnchanged: '실패 후 기존 수량', cartDelete: '개별 삭제', cartRedelete: '동일 항목 재삭제', cartClear: '전체 비우기', cartEmpty: '빈 Cart', stockUnchanged: 'Cart 조작 후 재고 유지',
  orderCreate: '주문 생성 HTTP', orderStock: '주문 수량만큼 재고 차감', orderCart: '주문 생성 후 Cart 유지', orderRead: '주문 조회', cancel: '최초 주문 취소', cancelStock: '취소 수량만큼 복구', originalStock: '취소 후 주문 전 재고', duplicateCancel: '중복 취소',
  mismatch: '결제 금액 불일치', approve: '정상 결제 승인', paymentRead: '실제 paymentId 결제 조회', duplicateApprove: '중복 승인', cleanup: '선택 상품 삭제·다른 상품 유지', extraQuantity: '주문 후 추가한 같은 상품 보존', snapshot: '주문 이름·가격 스냅샷',
  authCartPatch: 'B → A CartItem PATCH', authCartDelete: 'B → A CartItem DELETE', authOrderRead: 'B → A 주문 상세', authOrderCancel: 'B → A 주문 취소', authConfirm: 'B → A 결제 승인', authPayment: 'B → A 결제 조회',
};
export function Json({ value }) { return <pre>{JSON.stringify(redact(value), null, 2)}</pre>; }

function ScenarioGroup({ number, title, children, open = false }) {
  return <details className="scenario-group" open={open}><summary><span>{number}</span><b>{title}</b><span aria-hidden="true">⌄</span></summary><div className="scenario-group__body">{children}</div></details>;
}

export function ScenarioPanel({ open, onClose }) {
  return <aside className={`scenario-panel ${open ? 'is-open' : ''}`} aria-hidden={!open}>
    <div className="scenario-panel__header"><div><p className="section-kicker">TEST GUIDE</p><h2>검증 시나리오</h2></div><button className="panel-close" aria-label="검증 시나리오 닫기" onClick={onClose}>×</button></div>
    <p className="scenario-intro">아래 안내를 따라 실제 쇼핑 화면을 조작하세요. 결과는 하단의 최근 HTTP 요청에서 확인할 수 있습니다.</p>

    <ScenarioGroup number="1" title="기본 구매 흐름" open><ol><li>회원가입 또는 기존 계정으로 로그인</li><li>실제 상품 조회 및 선택</li><li>장바구니에 상품 추가</li><li>수량과 합계 금액 확인</li><li>Cart 상품으로 주문 생성</li><li>주문 상태와 상품 스냅샷 확인</li><li>실제 주문 금액으로 결제 승인</li><li>주문·결제 상태와 Cart 후처리 확인</li></ol></ScenarioGroup>

    <ScenarioGroup number="2" title="장바구니 검증"><ul><li><b>같은 상품을 두 번 담기</b><br/>기대: CartItem 행 대신 quantity가 합산됩니다.</li><li><b>재고보다 많은 수량 요청</b><br/>기대: HTTP 400 / PRODUCT_002, 기존 수량 유지</li><li><b>CartItem 하나 삭제</b><br/>기대: 선택한 항목만 삭제</li><li><b>전체 비우기</b><br/>기대: HTTP 204, 재조회 시 items=[] / totalPrice=0</li></ul></ScenarioGroup>

    <ScenarioGroup number="3" title="주문 검증"><ul><li>주문 전 재고와 Cart 수량을 확인하고 주문을 생성합니다.</li><li>정상 기대 재고는 <b>최초 재고 − 주문 수량</b>입니다.</li><li>주문 목록·상세에서 주문 시점 상품명과 가격을 확인합니다.</li><li>취소 후 실제 재고 복구량을 확인합니다.</li><li>중복 취소의 정확한 예외 재현은 Backend 통합 테스트가 필요합니다.</li></ul><p className="scenario-caution">현재 재고 차감 관련 보류 이슈가 있습니다. 화면의 실제값을 정상값으로 보정하지 않습니다.</p></ScenarioGroup>

    <ScenarioGroup number="4" title="결제 검증"><ul><li>화면은 선택한 주문의 실제 서버 금액으로만 승인을 요청합니다.</li><li>정상 금액 승인 후 Payment와 Order 상태를 확인합니다.</li><li>결제 후 주문 상품과 미주문 상품의 Cart 상태를 확인합니다.</li><li>금액 불일치·중복 승인의 정확한 예외 재현은 Backend 통합 테스트가 필요합니다.</li></ul><p className="scenario-caution">명시적 Payment FAIL HTTP 진입 API는 없어 Frontend에서 실행할 수 없습니다.</p></ScenarioGroup>

    <ScenarioGroup number="5" title="상품 조회와 Cache"><ul><li>메인 상품 목록에서 같은 필터로 v2 조회를 반복합니다.</li><li>하단 HTTP 기록에서 각 요청의 소요 시간을 관찰합니다.</li><li>상품 상세의 재고 수량은 실제 응답값으로 확인합니다.</li><li>stockQuantity는 Cache 대상이 아니며 매 요청 DB에서 조회됩니다.</li></ul><p className="scenario-caution">응답 시간만으로 Cache HIT/MISS를 확정하지 않습니다.</p></ScenarioGroup>

    <ScenarioGroup number="6" title="예외와 권한"><ul><li>잘못된 비밀번호: HTTP 401 / MEMBER_003</li><li>화면 조작 중 발생한 Validation 오류와 BusinessException의 실제 응답을 HTTP 기록에서 확인합니다.</li><li>타인 소유 CartItem·주문·결제 접근은 두 계정과 식별자가 필요하므로 Backend 통합 테스트가 필요합니다.</li></ul></ScenarioGroup>

    <ScenarioGroup number="7" title="별도 Backend 검증 필요"><p>이 항목들은 Frontend 화면만으로 확정할 수 없습니다. JUnit, MySQL, SQL Log, EXPLAIN 등 Backend 검증이 별도로 필요합니다.</p><ul><li>Cache HIT / MISS 확정</li><li>실제 SQL Query 수</li><li>Index 효과와 실행 계획</li><li>Transaction 내부 상태</li><li>Lock 획득 순서</li><li>Deadlock 여부와 정확한 동시성 안전성</li></ul></ScenarioGroup>

    <details className="known-issues"><summary>현재 확인 중인 이슈 <span>Known Issues</span></summary><ol><li><b>주문 생성 재고 차감 재검증 필요</b><small>이전 코드에서 재현 · 최신 develop은 중복 차감 호출 제거</small></li><li><b>주문 취소 재고 복구 재검증 필요</b><small>최신 develop 병합 후 초기화된 DB에서 확인 예정</small></li><li><b>주문 후 추가한 동일 상품 수량 삭제</b><small>Product ID 기준 CartItem 삭제 구조 유지 · 실제 재검증 필요</small></li><li><b>명시적 Payment FAIL HTTP 진입 경로 없음</b><small>현재 Frontend 테스트 불가</small></li></ol></details>
  </aside>;
}

export default function Diagnostics() {
  const [requests, setRequests] = useState([]);
  useEffect(() => { const log = e => setRequests(previous => [...previous.slice(-29), e.detail]); window.addEventListener('api-result', log); return () => window.removeEventListener('api-result', log); }, []);
  return <details className="http-history"><summary className="http-history__toggle"><span><small>DEVELOPER TOOLS</small><b>개발자 검증 정보 보기</b></span><span className="http-history__count">최근 요청 {requests.length}건</span><span aria-hidden="true">⌄</span></summary><div className="http-history__body"><div className="section-heading"><div><p className="section-kicker">API ACTIVITY</p><h2>최근 HTTP 요청</h2></div><button className="button-text" onClick={() => setRequests([])}>기록 지우기</button></div><p className="muted">최근 30건 · 실제 Status, Request/Response, ErrorCode, 소요 시간을 기록하며 password와 JWT는 마스킹합니다.</p>
    {requests.length === 0 ? <div className="empty-state empty-state--small"><h3>아직 기록된 요청이 없습니다.</h3><p>화면에서 API를 호출하면 여기에 실제 결과가 표시됩니다.</p></div> : <div className="request-list">{[...requests].reverse().map(entry => <details key={entry.id}><summary><span className={`method method--${entry.method.toLowerCase()}`}>{entry.method}</span><span className="request-path">{entry.path}</span><span className={`status status--${entry.status && entry.status < 400 ? 'success' : 'error'}`}>{entry.status ?? '응답 없음'}</span><span className="request-code">{entry.errorCode ?? '—'}</span><span>{entry.duration}ms</span></summary><p>{entry.time} · ErrorCode: {entry.errorCode ?? '없음'} · Error Message: {entry.errorMessage ?? '없음'}</p>{entry.transportError && <p className="error">Frontend 연결 진단: {entry.transportError}</p>}<div className="payload-grid"><div><h3>Request Body</h3><Json value={entry.requestBody}/></div><div><h3>Response Body</h3><Json value={entry.responseBody}/></div></div></details>)}</div>}
  </div></details>;
}
