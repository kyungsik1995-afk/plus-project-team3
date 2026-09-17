import React, { useRef, useState } from 'react';
import { createRoot } from 'react-dom/client';
import { request } from './api.js';
import Diagnostics, { ScenarioPanel } from './Diagnostics.jsx';
import Checkout from './Checkout.jsx';
import './style.css';

const money = value => value == null ? '확인 불가' : `${value.toLocaleString('ko-KR')}원`;
const initialFilters = { category: '', minPrice: '', maxPrice: '', size: '20' };
const categoryIcon = { FOOD: '●', FASHION: '◆', ELECTRONICS: '◉' };
const productImageByName = {
  '사과': '/products/apple.svg', '바나나': '/products/banana.svg', '우유': '/products/milk.svg', '계란': '/products/egg.svg',
  '빵': '/products/bread.svg', '치즈': '/products/cheese.svg', '소고기': '/products/beef.png', '쌀': '/products/rice.png',
  '라면': '/products/ramen.png', '김치': '/products/kimchi.png', '반팔티': '/products/short-sleeve-tshirt.svg',
  '긴팔티': '/products/long-sleeve-tshirt.svg', '청바지': '/products/jeans.svg', '반바지': '/products/shorts.svg',
  '후드티': '/products/hoodie.svg', '자켓': '/products/jacket.svg', '운동화': '/products/sneakers.svg', '코트': '/products/coat.svg',
  '모자': '/products/cap.svg', '가방': '/products/bag.svg', '마우스': '/products/mouse.svg', '키보드': '/products/keyboard.svg',
  '이어폰': '/products/earphones.svg', '헤드셋': '/products/headset.svg', '모니터': '/products/monitor.svg',
  '태블릿': '/products/tablet.svg', '노트북': '/products/laptop.svg', '데스크탑': '/products/desktop.svg',
  '스마트워치': '/products/smartwatch.svg', '스마트폰': '/products/smartphone.svg',
};

function ProductVisual({ product, compact = false }) {
  const category = product?.category || 'PRODUCT';
  const productName = product?.name || product?.productName || '';
  const imageUrl = productImageByName[productName];
  return <div className={`product-visual product-visual--${category.toLowerCase()} ${compact ? 'product-visual--compact' : ''}`} aria-hidden="true">
    {imageUrl ? <img className="product-visual__image" src={imageUrl} alt=""/> : <span className="product-visual__shape">{categoryIcon[category] || '●'}</span>}
    {!compact && <span className="product-visual__caption">PLUS SELECT · {product?.productId ?? ''}</span>}
  </div>;
}

function Hero() {
  return <section className="hero">
    <div className="hero-copy"><p className="eyebrow">PLUS SELECT</p><h1>좋은 일상이<br/>특별한 쇼핑으로</h1><p>필요한 상품을 간편하게,<br/>지금 바로 경험해보세요.</p></div>
    <div className="hero-art" aria-hidden="true"><span className="hero-leaf">✦</span><span className="hero-orbit hero-orbit--one">●</span><span className="hero-orbit hero-orbit--two">◆</span><div className="hero-bag"><b>PLUS</b><span>SELECT</span></div></div>
  </section>;
}

function App() {
  const [token, setToken] = useState('');
  const [authMode, setAuthMode] = useState('welcome');
  const [loginEmail, setLoginEmail] = useState('');
  const [member, setMember] = useState(null);
  const [busy, setBusy] = useState(false);
  const [scenarioOpen, setScenarioOpen] = useState(false);
  const lock = useRef(false);
  const [notice, setNotice] = useState('로그인하거나 새 계정을 만들어 쇼핑을 시작하세요.');
  const [error, setError] = useState('');
  const [filters, setFilters] = useState(initialFilters);
  const [applied, setApplied] = useState(initialFilters);
  const [productApiVersion, setProductApiVersion] = useState('v2');
  const [products, setProducts] = useState(null);
  const [detail, setDetail] = useState(null);
  const [cart, setCart] = useState(null);

  function clearSession() { setToken(''); setMember(null); setProducts(null); setDetail(null); setCart(null); setAuthMode('welcome'); }
  async function run(action) {
    if (lock.current) return;
    lock.current = true; setBusy(true); setError(''); setNotice('');
    try { await action(); }
    catch (e) {
      if (e.status === 401 && token && e.authToken === token) clearSession();
      setError(e.message);
    } finally { lock.current = false; setBusy(false); }
  }
  async function loadProducts(page, values = applied, version = productApiVersion) {
    const query = new URLSearchParams({ page: String(page) });
    Object.entries(values).forEach(([key, value]) => { if (value !== '') query.set(key, value); });
    setProducts(null);
    const endpoint = version === 'v1' ? '/api/products' : '/api/v2/products';
    setProducts(await request(`${endpoint}?${query}`, { token }));
    setApplied({ ...values });
    setProductApiVersion(version);
    setNotice(`✓ ${version === 'v1' ? '기본 조회 v1' : '캐시 조회 v2'} 상품 목록을 불러왔습니다.`);
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
    <header className={`topbar ${token ? '' : 'topbar--auth'}`}>
      <a className="brand" href={token ? '#products' : '#auth'} aria-label="PLUS SELECT 홈"><span className="brand-mark">P</span><span>PLUS SELECT</span></a>
      {token && <nav aria-label="주요 메뉴"><a className="active" href="#products">상품</a><a href="#cart">장바구니</a><a href="#orders">주문</a><a href="#payment">결제</a></nav>}
      <div className="account-area">{token && <><span className="login-state is-login">{member?.name ? `${member.name}님` : loginEmail || '로그인됨'}</span><button className="button-secondary button-small" disabled={busy} onClick={() => { clearSession(); setError(''); setNotice('이 브라우저의 로그인 정보를 지웠습니다.'); }}>로그아웃</button></>}<button className="scenario-toggle" aria-expanded={scenarioOpen} onClick={() => setScenarioOpen(value => !value)}>검증 시나리오</button></div>
    </header>

    <div className={`page-shell ${scenarioOpen ? 'scenario-is-open' : ''}`}>
      <main className={`storefront ${token ? 'storefront--shop' : 'storefront--auth'}`}>
        {!token ? <div id="auth" className={`auth-landing auth-landing--${authMode}`}><Hero/><section className="login-card">
          {authMode === 'welcome' ? <div className="auth-welcome"><p className="section-kicker">WELCOME TO PLUS SELECT</p><h2>오늘도 좋은 선택을<br/>시작해보세요</h2><p className="muted">실제 상품과 장바구니를 이용하려면 로그인하거나 새 계정을 만들어주세요.</p><div className="auth-welcome__actions"><button onClick={() => { setAuthMode('login'); setError(''); }}>로그인</button><button className="button-secondary" onClick={() => { setAuthMode('signup'); setError(''); }}>회원가입</button></div><div className="auth-benefits"><span><b>✓</b> 실제 상품</span><span><b>✓</b> 안전한 인증</span><span><b>✓</b> 간편한 쇼핑</span></div></div> : authMode === 'signupComplete' ? <div className="auth-complete"><span className="auth-complete__check">✓</span><p className="section-kicker">WELCOME</p><h2>회원가입이 완료되었습니다!</h2><p className="muted">PLUS SELECT의 새로운 회원이 되신 것을 환영합니다.</p><div className="auth-complete__note">이제 가입한 이메일로 로그인하여<br/>다양한 실제 상품을 만나보세요.</div><button onClick={() => { setAuthMode('login'); setError(''); }}>로그인하러 가기</button></div> : <><div className="auth-card__intro"><button className="auth-back button-text" onClick={() => { setAuthMode('welcome'); setError(''); }}>← 처음으로</button><p className="section-kicker">PLUS SELECT</p><h2>{authMode === 'login' ? '로그인' : '회원가입'}</h2><p className="muted">{authMode === 'login' ? '계정에 로그인하고 쇼핑을 시작하세요.' : 'PLUS SELECT와 함께 더 나은 쇼핑을 시작하세요.'}</p><div className="auth-switch" role="tablist" aria-label="인증 화면 선택"><button type="button" role="tab" aria-selected={authMode === 'login'} className={authMode === 'login' ? 'is-active' : ''} onClick={() => { setAuthMode('login'); setError(''); }}>로그인</button><button type="button" role="tab" aria-selected={authMode === 'signup'} className={authMode === 'signup' ? 'is-active' : ''} onClick={() => { setAuthMode('signup'); setError(''); }}>회원가입</button></div></div>{authMode === 'login' ? <form onSubmit={e => {
            e.preventDefault(); const form = e.currentTarget; const data = new FormData(form);
            run(async () => {
              const result = await request('/api/members/login', { method: 'POST', body: { email: data.get('email'), password: data.get('password') }, check: { id: 'login', status: 200, validate: value => !!value?.accessToken } });
              if (!result?.accessToken) throw new Error('로그인 응답에 accessToken이 없습니다.');
              const currentMember = await request('/api/members/me', { token: result.accessToken });
              setMember(currentMember); setToken(result.accessToken); setNotice('✓ 로그인되었습니다. 상품 조회를 눌러 실제 상품을 확인하세요.');
            });
          }}><fieldset disabled={busy} className="login-fields"><label>이메일<input name="email" type="email" autoComplete="username" required placeholder="email@example.com" value={loginEmail} onChange={event => setLoginEmail(event.target.value)}/></label><label>비밀번호<input name="password" type="password" autoComplete="current-password" required placeholder="비밀번호"/></label><button>로그인</button></fieldset></form> : <form onSubmit={e => {
            e.preventDefault(); const form = e.currentTarget; const data = new FormData(form); const email = data.get('email');
            run(async () => {
              await request('/api/members/signup', { method: 'POST', body: { email, password: data.get('password'), name: data.get('name'), phoneNumber: data.get('phoneNumber') } });
              setLoginEmail(email); setAuthMode('signupComplete'); setNotice('✓ 회원가입이 완료되었습니다.');
            });
          }}><fieldset disabled={busy} className="signup-fields"><label>이메일<input name="email" type="email" autoComplete="email" required placeholder="email@example.com"/></label><label>비밀번호<input name="password" type="password" autoComplete="new-password" minLength="8" required placeholder="8자 이상"/></label><label>이름<input name="name" type="text" maxLength="50" required placeholder="이름"/></label><label>전화번호<input name="phoneNumber" type="tel" required placeholder="010-1234-5678"/></label><button>가입하기</button></fieldset></form>}
          </>}
        </section></div> : null}

        <div className="result-stack"><div aria-live="polite" role="status">{busy && <p className="notice result-message">요청 처리 중…</p>}{notice && <p className="notice result-message">{notice}</p>}</div>{error && <p role="alert" className="error result-message"><b>⚠ 요청 실패</b><span>{error}</span></p>}</div>

        {token && <>
        <section id="products" className="catalog-section">
          <div className="catalog-layout">
            <aside className="catalog-sidebar"><form onSubmit={e => { e.preventDefault(); const version = e.nativeEvent.submitter?.value || productApiVersion; run(async () => { if (filters.minPrice !== '' && filters.maxPrice !== '' && Number(filters.minPrice) > Number(filters.maxPrice)) throw new Error('최소 가격은 최대 가격보다 클 수 없습니다.'); await loadProducts(0, filters, version); }); }}><fieldset disabled={!token || busy}>
              <div className="category-filter"><span>카테고리</span><div className="category-options">{[
                ['', '전체'],
                ['FOOD', '식품 (FOOD)'],
                ['FASHION', '패션 (FASHION)'],
                ['ELECTRONICS', '전자제품 (ELECTRONICS)'],
              ].map(([value, label]) => <button key={value || 'all'} type="button" className={filters.category === value ? 'is-active' : ''} onClick={() => setFilters({ ...filters, category: value })}>{label}</button>)}</div></div>
              <div className="price-filter"><span>가격 범위</span><div><input aria-label="최소 가격" type="number" min="0" max={Number.MAX_SAFE_INTEGER} step="1" value={filters.minPrice} onChange={e => setFilters({ ...filters, minPrice: e.target.value })} placeholder="최소 가격"/><span>~</span><input aria-label="최대 가격" type="number" min="0" max={Number.MAX_SAFE_INTEGER} step="1" value={filters.maxPrice} onChange={e => setFilters({ ...filters, maxPrice: e.target.value })} placeholder="최대 가격"/></div></div>
              <label className="size-field"><span>페이지 크기</span><input aria-label="페이지 크기" type="number" min="1" max="100" required value={filters.size} onChange={e => setFilters({ ...filters, size: e.target.value })}/></label>
              <div className="version-actions"><button className={productApiVersion === 'v1' ? '' : 'button-secondary'} name="version" value="v1">기본 조회 v1</button><button className={productApiVersion === 'v2' ? '' : 'button-secondary'} name="version" value="v2">캐시 조회 v2</button></div>
            </fieldset></form><p className="sidebar-note">같은 조건으로 v1/v2를 호출하고 실제 응답 시간은 하단 Diagnostics에서 확인합니다.</p></aside>
            <div className="catalog-results"><div className="section-heading"><div><p className="section-kicker">PRODUCTS</p><h2>상품 목록</h2></div>{products && <p className="muted">총 {products.totalElements}개의 실제 상품</p>}</div><div className="cache-note"><b>{productApiVersion === 'v1' ? 'v1 · QueryDSL 기본 조회' : 'v2 · Caffeine Local Cache 적용 조회'}</b><span>Frontend에서는 Cache HIT/MISS를 확정하지 않습니다.</span></div>
              {!products && <div className="empty-state"><span>⌁</span><h3>상품 조회를 눌러주세요</h3><p>Backend의 실제 상품 데이터만 표시합니다.</p></div>}
              {products && <><div className="product-grid">{products.content.map(product => <article className={`product-card ${detail?.productId === product.productId ? 'is-selected' : ''}`} key={product.productId}><ProductVisual product={product}/><div className="product-card__body"><span className={`category-tag category-tag--${product.category.toLowerCase()}`}>{product.category}</span><h3>{product.name}</h3><strong>{money(product.price)}</strong><p>재고 {product.stockQuantity ?? '확인 불가'}개</p><button className="button-soft" disabled={busy} onClick={() => run(async () => { setDetail(null); setDetail(await request(`/api/products/${product.productId}`, { token })); setNotice('✓ 상품 상세를 불러왔습니다.'); })}>상품 보기</button></div></article>)}</div>{products.content.length === 0 && <div className="empty-state"><h3>조건에 맞는 상품이 없습니다.</h3></div>}<div className="pager"><button className="button-secondary" disabled={busy || products.first} onClick={() => run(() => loadProducts(products.number - 1))}>이전</button><span>{products.number + 1} / {Math.max(1, products.totalPages)}</span><button className="button-secondary" disabled={busy || products.last} onClick={() => run(() => loadProducts(products.number + 1))}>다음</button></div></>}
            </div>
          </div>
        </section>

        <div className="commerce-flow">
          <section className="detail-card"><div className="section-heading"><div><p className="section-kicker">SELECTED ITEM</p><h2>상품 상세</h2></div></div>{!detail ? <div className="empty-state empty-state--small"><h3>상품을 선택해주세요</h3><p>상품 카드의 ‘상품 보기’를 누르면 상세 정보를 확인할 수 있습니다.</p></div> : <div className="detail-layout"><ProductVisual product={detail}/><div className="detail-info"><span className={`category-tag category-tag--${detail.category.toLowerCase()}`}>{detail.category}</span><p className="product-id">PRODUCT #{detail.productId}</p><h3>{detail.name}</h3><strong>{money(detail.price)}</strong><p className="description">{detail.description || '상품 설명이 없습니다.'}</p><p className="stock-copy">현재 재고 <b>{detail.stockQuantity ?? '확인 불가'}개</b></p><form key={detail.productId} onSubmit={e => { e.preventDefault(); const form = e.currentTarget; run(() => mutateCart('/api/carts/items', 'POST', { productId: detail.productId, quantity: quantity(form) })); }}><fieldset disabled={busy || !token || !(detail.stockQuantity > 0)} className="add-cart"><label>수량<input name="quantity" type="number" min="1" max={detail.stockQuantity} step="1" defaultValue="1" required/></label><button>장바구니 담기</button></fieldset></form></div></div>}</section>

          <section id="cart" className="cart-card"><div className="section-heading"><div><p className="section-kicker">MY CART</p><h2>장바구니 {cart ? `(${cart.items.length})` : ''}</h2></div><div className="header-actions"><button className="button-secondary button-small" disabled={!token || busy} onClick={() => run(loadCart)}>새로고침</button><button className="button-text danger-text" disabled={!token || busy || !cart?.items.length} onClick={() => { if (window.confirm('장바구니의 모든 상품을 삭제할까요?')) run(() => mutateCart('/api/carts', 'DELETE')); }}>전체 비우기</button></div></div>
            {!cart && <div className="empty-state empty-state--small"><h3>장바구니를 조회해주세요</h3><p>서버의 현재 수량과 금액을 그대로 표시합니다.</p></div>}{cart && <>{cart.items.length === 0 ? <div className="empty-state empty-state--small"><h3>장바구니가 비어 있습니다.</h3></div> : <div className="cart-items">{cart.items.map(item => <article className="cart-item" key={`${item.cartItemId}-${item.quantity}`}><ProductVisual product={item} compact/><div className="cart-item__info"><h3>{item.productName}</h3><p>단가 {money(item.productPrice)}</p><strong>{money(item.itemTotalPrice)}</strong></div><form onSubmit={e => { e.preventDefault(); const form = e.currentTarget; run(() => mutateCart(`/api/carts/items/${item.cartItemId}`, 'PATCH', { quantity: quantity(form) })); }}><fieldset disabled={busy} className="cart-controls"><label>수량<input aria-label={`${item.productName} 최종 수량`} name="quantity" type="number" min="1" max="2147483647" step="1" defaultValue={item.quantity} required/></label><button className="button-secondary button-small">변경</button><button type="button" className="button-text danger-text" onClick={() => run(() => mutateCart(`/api/carts/items/${item.cartItemId}`, 'DELETE'))}>삭제</button></fieldset></form></article>)}</div>}<div className="cart-total"><span>총 상품 금액</span><strong>{money(cart.totalPrice)}</strong><a className="button-link" href="#orders">주문하기</a></div></>}
          </section>
        </div>

        <Checkout key={token || 'anonymous'} token={token} busy={busy} run={run} cart={cart} refreshCart={loadCart} onNotice={setNotice}/></>}
        <Diagnostics/><footer>현재 Backend API 테스트 · 모든 기록과 JWT는 메모리에만 보관하며 새로고침 시 초기화됩니다.</footer>
      </main>
      <ScenarioPanel open={scenarioOpen} onClose={() => setScenarioOpen(false)}/>
    </div>
  </div>;
}

createRoot(document.getElementById('root')).render(<App/>);
