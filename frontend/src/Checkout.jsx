import React, { useEffect, useState } from 'react';
import { request } from './api.js';

const money = value => value == null ? '확인 불가' : `${value.toLocaleString('ko-KR')}원`;

export default function Checkout({ token, busy, run, cart, refreshCart, onNotice }) {
  const [selectedCartItems, setSelectedCartItems] = useState([]);
  const [orders, setOrders] = useState([]);
  const [order, setOrder] = useState(null);
  const [payment, setPayment] = useState(null);

  useEffect(() => {
    const currentIds = new Set(cart?.items.map(item => item.cartItemId) ?? []);
    setSelectedCartItems(previous => previous.filter(id => currentIds.has(id)));
  }, [cart]);

  async function loadOrders(selectOrderId) {
    const data = await request('/api/orders', { token });
    setOrders(data);
    if (selectOrderId) await loadOrder(selectOrderId);
    else onNotice('✓ 주문 목록을 불러왔습니다.');
    return data;
  }

  async function loadOrder(orderId) {
    const data = await request(`/api/orders/${orderId}`, { token });
    setOrder(data);
    setPayment(null);
    onNotice('✓ 주문 상세를 불러왔습니다.');
    return data;
  }

  async function createOrder() {
    if (!selectedCartItems.length) throw new Error('주문할 장바구니 상품을 선택하세요.');
    const created = await request('/api/orders', { token, method: 'POST', body: { cartItemIds: selectedCartItems } });
    try {
      await loadOrders(created.orderId);
      await refreshCart(true);
      onNotice(`✓ 주문이 생성되었습니다. 주문번호 ${created.orderNumber}`);
    } catch (error) {
      error.message = `주문은 생성되었지만 후속 조회에 실패했습니다. ${error.message}`;
      throw error;
    }
  }

  async function cancelOrder() {
    const cancelled = await request(`/api/orders/${order.orderId}/cancel`, { token, method: 'PATCH' });
    setOrder(cancelled);
    await loadOrders(cancelled.orderId);
    onNotice(`✓ 주문 ${cancelled.orderNumber}이 취소되었습니다.`);
  }

  async function confirmPayment() {
    const confirmed = await request('/api/payments/confirm', { token, method: 'POST', body: { orderId: order.orderId, paymentPrice: order.totalAmount } });
    try {
      const paymentData = await request(`/api/payments/${confirmed.paymentId}`, { token });
      setPayment(paymentData);
      await loadOrders(order.orderId);
      await refreshCart(true);
      onNotice(`✓ 결제가 승인되었습니다. 결제번호 ${confirmed.paymentId}`);
    } catch (error) {
      error.message = `결제는 승인되었지만 후속 조회에 실패했습니다. ${error.message}`;
      throw error;
    }
  }

  async function reloadPayment() {
    if (!payment?.paymentId) throw new Error('승인 성공 응답으로 확보한 paymentId가 없습니다.');
    setPayment(await request(`/api/payments/${payment.paymentId}`, { token }));
    onNotice('✓ 결제 정보를 불러왔습니다.');
  }

  const allSelected = !!cart?.items.length && cart.items.every(item => selectedCartItems.includes(item.cartItemId));

  return <div className="checkout-flow">
    <section id="orders" className="orders-section">
      <div className="section-heading"><div><p className="section-kicker">ORDER</p><h2>주문</h2></div><button className="button-secondary button-small" disabled={!token || busy} onClick={() => run(() => loadOrders())}>주문 내역 조회</button></div>
      {!token ? <div className="empty-state empty-state--small"><h3>로그인이 필요합니다.</h3></div> : <div className="order-layout">
        <div className="order-create"><h3>장바구니에서 주문할 상품</h3>{!cart?.items.length ? <p className="muted">장바구니에 상품을 담고 새로고침하세요.</p> : <><label className="select-all"><input type="checkbox" checked={allSelected} onChange={event => setSelectedCartItems(event.target.checked ? cart.items.map(item => item.cartItemId) : [])}/> 전체 선택</label><div className="order-cart-list">{cart.items.map(item => <label key={item.cartItemId}><input type="checkbox" checked={selectedCartItems.includes(item.cartItemId)} onChange={event => setSelectedCartItems(event.target.checked ? [...selectedCartItems, item.cartItemId] : selectedCartItems.filter(id => id !== item.cartItemId))}/><span><b>{item.productName}</b><small>{item.quantity}개 · {money(item.itemTotalPrice)}</small></span></label>)}</div></>}
          <button disabled={busy || !selectedCartItems.length} onClick={() => { if (window.confirm('선택한 장바구니 상품으로 주문을 생성할까요?')) run(createOrder); }}>선택 상품 주문하기</button>
        </div>
        <div className="order-history"><h3>내 주문</h3>{orders.length === 0 ? <p className="muted">주문 내역 조회를 눌러 실제 주문을 확인하세요.</p> : <div className="order-list">{orders.map(item => <button className={`order-row ${order?.orderId === item.orderId ? 'is-selected' : ''}`} key={item.orderId} onClick={() => run(() => loadOrder(item.orderId))}><span><b>{item.orderNumber}</b><small>{item.createdAt?.replace('T', ' ')}</small></span><span><b>{money(item.totalAmount)}</b><small>{item.status} · {item.paymentStatus}</small></span></button>)}</div>}</div>
      </div>}

      {order && <article className="order-detail"><div className="order-detail__header"><div><span className="status-chip">{order.status}</span><span className="status-chip status-chip--payment">{order.paymentStatus}</span></div><strong>{money(order.totalAmount)}</strong></div><h3>{order.orderNumber}</h3><div className="order-products">{order.orderItems.map((item, index) => <div key={`${item.productName}-${index}`}><span>{item.productName} × {item.quantity}</span><b>{money(item.orderPrice * item.quantity)}</b></div>)}</div><button className="button-secondary" disabled={busy || order.status === 'CANCELED'} onClick={() => { if (window.confirm('이 주문을 취소할까요? 실제 재고와 결제 상태가 변경됩니다.')) run(cancelOrder); }}>주문 취소</button></article>}
    </section>

    <section id="payment" className="payment-section"><div className="section-heading"><div><p className="section-kicker">PAYMENT</p><h2>결제</h2></div>{payment && <button className="button-secondary button-small" disabled={busy} onClick={() => run(reloadPayment)}>결제 정보 새로고침</button>}</div>
      {!order ? <div className="empty-state empty-state--small"><h3>결제할 주문을 선택해주세요</h3><p>내 주문에서 결제 대기 주문을 선택하세요.</p></div> : <div className="payment-card"><div><p className="muted">결제 대상</p><h3>{order.orderNumber}</h3><p>{order.orderItems.map(item => item.productName).join(', ')}</p></div><div className="payment-amount"><span>결제 금액</span><strong>{money(order.totalAmount)}</strong></div><button disabled={busy || order.status !== 'PAYMENT_PENDING' || order.paymentStatus !== 'IN_PROGRESS'} onClick={() => { if (window.confirm(`${money(order.totalAmount)} 결제를 승인할까요?`)) run(confirmPayment); }}>결제 승인</button></div>}
      {payment && <div className="payment-result"><span className="status-chip status-chip--payment">{payment.status}</span><div><b>결제 #{payment.paymentId}</b><p>주문 #{payment.orderId} · {money(payment.totalPrice)} · {payment.paidAt?.replace('T', ' ')}</p></div></div>}
    </section>
  </div>;
}
