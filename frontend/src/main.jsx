import React, { useRef, useState } from 'react';
import { createRoot } from 'react-dom/client';
import { request } from './api.js';
import Diagnostics, { ScenarioPanel } from './Diagnostics.jsx';
import Checkout from './Checkout.jsx';
import './style.css';

const money = value => value == null ? '확인 불가' : `${value.toLocaleString('ko-KR')}원`;
const initialFilters = { category: '', minPrice: '', maxPrice: '', size: '20' };
const categoryIcon = { FOOD: '●', FASHION: '◆', ELECTRONICS: '◉' };

function ProductVisual({ product, compact = false }) {
  const category = product?.category || 'PRODUCT';
  return <div className={`product-visual product-visual--${category.toLowerCase()} ${compact ? 'product-visual--compact' : ''}`} aria-hidden="true">
    <span className="product-visual__shape">{categoryIcon[category] || '●'}</span>
    {!compact && <span className="product-visual__caption">PLUS SELECT · {product?.productId ?? ''}</span>}
  </div>;
}

function App() {
  const [token, setToken] = useState('');
  const [busy, setBusy] = useState(false);
  const [scenarioOpen, setScenarioOpen] = useState(() => window.innerWidth >= 1180);
  const lock = useRef(false);
  const [notice, setNotice] = useState('기존 회원 계정으로 로그인하세요. 상품 조회에도 인증이 필요합니다.');
  const [error, setError] = useState('');
  const [filters, setFilters] = useState(initialFilters);
  const [applied, setApplied] = useState(initialFilters);
  const [products, setProducts] = useState(null);
  const [detail, setDetail] = useState(null);
  const [cart, setCart] = useState(null);

  function clearSession() { setToken(''); setProducts(null); setDetail(null); setCart(null); }
  async function run(action) {
    if (lock.current) return;
    lock.current = true; setBusy(true); setError(''); setNotice('');
    try { await action(); }
    catch (e) {
      if (e.status === 401 && token && e.authToken === token) clearSession();
      setError(e.message);
    } finally { lock.current = false; setBusy(false); }
  }
  async function loadProducts(page, values = applied) {
    const query = new URLSearchParams({ page: String(page) });
    Object.entries(values).forEach(([key, value]) => { if (value !== '') query.set(key, value); });
    setProducts(null);
    setProducts(await request(`/api/v2/products?${query}`, { token }));
    setApplied({ ...values });
    setNotice('✓ 상품 목록을 불러왔습니다.');
  }
  async function loadCart(silent = false) {
    setCart(null);
    const result = await request('/api/carts', { token });
    setCart(result);
    if (!silent) setNotice('✓ 장바구니의 현재 상태를 불러왔습니다.');
    return result;
  }
  async function mutateCart(path, method, body) {
    await request(path, { token, method, body });
    setNotice('✓ 장바구니 변경이 완료되었습니다.');
    try { await loadCart(true); }
    catch (e) { e.message = `변경은 완료되었지만 장바구니 재조회에 실패했습니다. ${e.message}`; throw e; }
  }
  function quantity(form) {
    const value = Number(new FormData(form).get('quantity'));
    if (!Number.isInteger(value) || value < 1 || value > 2147483647) throw new Error('수량은 1~2147483647의 정수여야 합니다.');
    return value;
  }

  return <div className="app">
    <header className="topbar">
      <a className="brand" href="#products" aria-label="PLUS COMMERCE 홈"><span className="brand-mark">P</span><span>PLUS COMMERCE</span></a>
      <nav aria-label="주요 메뉴"><a className="active" href="#products">상품</a><a href="#cart">장바구니</a><a href="#orders">주문</a><a href="#payment">결제</a></nav>
      <div className="account-area"><span className={`login-state ${token ? 'is-login' : ''}`}>{token ? '로그인됨' : '로그인 필요'}</span>{token && <button className="button-secondary button-small" disabled={busy} onClick={() => { clearSession(); setError(''); setNotice('이 브라우저의 로그인 정보를 지웠습니다.'); }}>로그아웃</button>}<button className="scenario-toggle" aria-expanded={scenarioOpen} onClick={() => setScenarioOpen(value => !value)}>검증 시나리오</button></div>
    </header>

    <div className={`page-shell ${scenarioOpen ? 'scenario-is-open' : ''}`}>
      <main className="storefront">
        <section className="hero"><div><p className="eyebrow">PLUS COMMERCE</p><h1>좋은 일상이<br/>특별한 쇼핑으로</h1><p>필요한 상품을 간편하게, 지금 바로 경험해보세요.</p></div><div className="hero-art" aria-hidden="true"><span className="hero-leaf">✦</span><div className="hero-bag"><b>PLUS</b><span>COMMERCE</span></div></div></section>

        <section className={`login-card ${token ? 'login-card--complete' : ''}`}>
          {!token ? <><div><p className="section-kicker">MEMBER</p><h2>로그인</h2><p className="muted">상품과 장바구니는 실제 JWT 인증 후 조회합니다.</p></div><form onSubmit={e => {
            e.preventDefault(); const form = e.currentTarget; const data = new FormData(form);
            run(async () => {
              const result = await request('/api/members/login', { method: 'POST', body: { email: data.get('email'), password: data.get('password') }, check: { id: 'login', status: 200, validate: value => !!value?.accessToken } });
              if (!result?.accessToken) throw new Error('로그인 응답에 accessToken이 없습니다.');
              setToken(result.accessToken); form.reset(); setNotice('✓ 로그인되었습니다. 상품 조회를 눌러 실제 상품을 확인하세요.');
            });
          }}><fieldset disabled={busy} className="login-fields"><label>이메일<input name="email" type="email" autoComplete="username" required placeholder="email@example.com"/></label><label>비밀번호<input name="password" type="password" autoComplete="current-password" required placeholder="비밀번호"/></label><button>로그인</button></fieldset></form></> : <><div><p className="section-kicker">MEMBER</p><h2>쇼핑을 시작해보세요</h2><p className="muted">JWT는 브라우저 메모리에만 보관됩니다.</p></div><a className="button-link" href="#products">상품 둘러보기</a></>}
        </section>

        <div className="result-stack"><div aria-live="polite" role="status">{busy && <p className="notice result-message">요청 처리 중…</p>}{notice && <p className="notice result-message">{notice}</p>}</div>{error && <p role="alert" className="error result-message"><b>⚠ 요청 실패</b><span>{error}</span></p>}</div>

        <section id="products" className="catalog-section">
          <div className="section-heading"><div><p className="section-kicker">PRODUCTS</p><h2>상품 둘러보기</h2></div>{products && <p className="muted">총 {products.totalElements}개의 실제 상품</p>}</div>
          <form className="filter-bar" onSubmit={e => { e.preventDefault(); run(async () => { if (filters.minPrice !== '' && filters.maxPrice !== '' && Number(filters.minPrice) > Number(filters.maxPrice)) throw new Error('최소 가격은 최대 가격보다 클 수 없습니다.'); await loadProducts(0, filters); }); }}><fieldset disabled={!token || busy}>
            <label><span>카테고리</span><select value={filters.category} onChange={e => setFilters({ ...filters, category: e.target.value })}><option value="">전체 카테고리</option>{['FOOD', 'FASHION', 'ELECTRONICS'].map(c => <option key={c}>{c}</option>)}</select></label>
            <label><span>최소 가격</span><input aria-label="최소 가격" type="number" min="0" max={Number.MAX_SAFE_INTEGER} step="1" value={filters.minPrice} onChange={e => setFilters({ ...filters, minPrice: e.target.value })} placeholder="0"/></label><span className="range-mark">~</span>
            <label><span>최대 가격</span><input aria-label="최대 가격" type="number" min="0" max={Number.MAX_SAFE_INTEGER} step="1" value={filters.maxPrice} onChange={e => setFilters({ ...filters, maxPrice: e.target.value })} placeholder="제한 없음"/></label>
            <label className="size-field"><span>페이지 크기</span><input aria-label="페이지 크기" type="number" min="1" max="100" required value={filters.size} onChange={e => setFilters({ ...filters, size: e.target.value })}/></label><button>상품 조회</button>
          </fieldset></form>
          <div className="cache-note"><b>v2 · Caffeine Local Cache 적용 조회</b><span>stockQuantity는 캐시하지 않고 매 요청 DB에서 조회합니다. Frontend에서는 HIT/MISS를 확정하지 않습니다.</span></div>
          {!products && <div className="empty-state"><span>⌁</span><h3>{token ? '상품 조회를 눌러주세요' : '로그인 후 상품을 조회할 수 있어요'}</h3><p>Backend의 실제 상품 데이터만 표시합니다.</p></div>}
          {products && <><div className="product-grid">{products.content.map(product => <article className={`product-card ${detail?.productId === product.productId ? 'is-selected' : ''}`} key={product.productId}><ProductVisual product={product}/><div className="product-card__body"><span className={`category-tag category-tag--${product.category.toLowerCase()}`}>{product.category}</span><h3>{product.name}</h3><strong>{money(product.price)}</strong><p>재고 {product.stockQuantity ?? '확인 불가'}개</p><button className="button-soft" disabled={busy} onClick={() => run(async () => { setDetail(null); setDetail(await request(`/api/products/${product.productId}`, { token })); setNotice('✓ 상품 상세를 불러왔습니다.'); })}>상품 보기</button></div></article>)}</div>{products.content.length === 0 && <div className="empty-state"><h3>조건에 맞는 상품이 없습니다.</h3></div>}<div className="pager"><button className="button-secondary" disabled={busy || products.first} onClick={() => run(() => loadProducts(products.number - 1))}>이전</button><span>{products.number + 1} / {Math.max(1, products.totalPages)}</span><button className="button-secondary" disabled={busy || products.last} onClick={() => run(() => loadProducts(products.number + 1))}>다음</button></div></>}
        </section>

        <div className="commerce-flow">
          <section className="detail-card"><div className="section-heading"><div><p className="section-kicker">SELECTED ITEM</p><h2>상품 상세</h2></div></div>{!detail ? <div className="empty-state empty-state--small"><h3>상품을 선택해주세요</h3><p>상품 카드의 ‘상품 보기’를 누르면 상세 정보를 확인할 수 있습니다.</p></div> : <div className="detail-layout"><ProductVisual product={detail}/><div className="detail-info"><span className={`category-tag category-tag--${detail.category.toLowerCase()}`}>{detail.category}</span><p className="product-id">PRODUCT #{detail.productId}</p><h3>{detail.name}</h3><strong>{money(detail.price)}</strong><p className="description">{detail.description || '상품 설명이 없습니다.'}</p><p className="stock-copy">현재 재고 <b>{detail.stockQuantity ?? '확인 불가'}개</b></p><form key={detail.productId} onSubmit={e => { e.preventDefault(); const form = e.currentTarget; run(() => mutateCart('/api/carts/items', 'POST', { productId: detail.productId, quantity: quantity(form) })); }}><fieldset disabled={busy || !token || !(detail.stockQuantity > 0)} className="add-cart"><label>수량<input name="quantity" type="number" min="1" max={detail.stockQuantity} step="1" defaultValue="1" required/></label><button>장바구니 담기</button></fieldset></form></div></div>}</section>

          <section id="cart" className="cart-card"><div className="section-heading"><div><p className="section-kicker">MY CART</p><h2>장바구니 {cart ? `(${cart.items.length})` : ''}</h2></div><div className="header-actions"><button className="button-secondary button-small" disabled={!token || busy} onClick={() => run(loadCart)}>새로고침</button><button className="button-text danger-text" disabled={!token || busy || !cart?.items.length} onClick={() => { if (window.confirm('장바구니의 모든 상품을 삭제할까요?')) run(() => mutateCart('/api/carts', 'DELETE')); }}>전체 비우기</button></div></div>
            {!cart && <div className="empty-state empty-state--small"><h3>장바구니를 조회해주세요</h3><p>서버의 현재 수량과 금액을 그대로 표시합니다.</p></div>}{cart && <>{cart.items.length === 0 ? <div className="empty-state empty-state--small"><h3>장바구니가 비어 있습니다.</h3></div> : <div className="cart-items">{cart.items.map(item => <article className="cart-item" key={`${item.cartItemId}-${item.quantity}`}><ProductVisual product={item} compact/><div className="cart-item__info"><h3>{item.productName}</h3><p>단가 {money(item.productPrice)}</p><strong>{money(item.itemTotalPrice)}</strong></div><form onSubmit={e => { e.preventDefault(); const form = e.currentTarget; run(() => mutateCart(`/api/carts/items/${item.cartItemId}`, 'PATCH', { quantity: quantity(form) })); }}><fieldset disabled={busy} className="cart-controls"><label>수량<input aria-label={`${item.productName} 최종 수량`} name="quantity" type="number" min="1" max="2147483647" step="1" defaultValue={item.quantity} required/></label><button className="button-secondary button-small">변경</button><button type="button" className="button-text danger-text" onClick={() => run(() => mutateCart(`/api/carts/items/${item.cartItemId}`, 'DELETE'))}>삭제</button></fieldset></form></article>)}</div>}<div className="cart-total"><span>총 상품 금액</span><strong>{money(cart.totalPrice)}</strong><a className="button-link" href="#orders">주문하기</a></div></>}
          </section>
        </div>

        <Checkout key={token || 'anonymous'} token={token} busy={busy} run={run} cart={cart} refreshCart={loadCart} onNotice={setNotice}/><Diagnostics/><footer>현재 Backend API 테스트 · 모든 기록과 JWT는 메모리에만 보관하며 새로고침 시 초기화됩니다.</footer>
      </main>
      <ScenarioPanel open={scenarioOpen} onClose={() => setScenarioOpen(false)}/>
    </div>
  </div>;
}

createRoot(document.getElementById('root')).render(<App/>);
