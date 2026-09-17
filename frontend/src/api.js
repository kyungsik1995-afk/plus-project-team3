// Remove secrets before an event or diagnostics state receives the data.
export function redact(value, secrets = []) {
  if (Array.isArray(value)) return value.map(item => redact(item, secrets));
  if (value && typeof value === 'object') return Object.fromEntries(Object.entries(value).map(([key, item]) => [key,
    /password|token|authorization|secret/i.test(key) ? '[마스킹]' : redact(item, secrets)]));
  if (typeof value !== 'string') return value;
  let text = value.replace(/Bearer\s+[^\s"<>]+/gi, 'Bearer [마스킹]')
    .replace(/eyJ[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+/g, '[JWT 마스킹]');
  for (const secret of secrets.filter(Boolean)) text = text.split(secret).join('[마스킹]');
  return text;
}

export function recordCheck(id, passed, expected, actual) {
  window.dispatchEvent(new CustomEvent('scenario-result', { detail: {
    id, passed, expected, actual: redact(actual), time: new Date().toISOString(),
  } }));
}

export async function inspectRequest(path, { token, method = 'GET', body, check } = {}) {
  const start = performance.now();
  const time = new Date().toISOString();
  const secrets = [token, body?.password];
  let result;
  try {
    const response = await fetch(path, {
      method,
      headers: { ...(token ? { Authorization: `Bearer ${token}` } : {}), ...(body !== undefined ? { 'Content-Type': 'application/json' } : {}) },
      body: body === undefined ? undefined : JSON.stringify(body),
      signal: AbortSignal.timeout(15000),
    });
    const raw = await response.text();
    let payload = null;
    try { payload = raw ? JSON.parse(raw) : null; } catch { /* Preserve actual non-JSON response. */ }
    result = { status: response.status, payload, data: payload?.data ?? null,
      ok: response.ok && (response.status === 204 || payload?.success === true),
      responseBody: payload ?? (raw || null), errorCode: payload?.error?.code ?? null, errorMessage: payload?.error?.message ?? null };
  } catch (error) {
    result = { status: null, ok: false, data: null, responseBody: null, errorCode: null, errorMessage: null,
      transportError: error.name === 'TimeoutError' ? '응답 시간 초과. 쓰기 처리 여부는 재조회로 확인하세요.' : '연결 또는 응답 수신 실패. 백엔드/Vite 상태를 확인하세요.' };
  }
  result.duration = Math.round(performance.now() - start);
  const detail = redact({ id: crypto.randomUUID(), time, method, path, status: result.status,
    duration: result.duration, requestBody: body ?? null, responseBody: result.responseBody,
    errorCode: result.errorCode, errorMessage: result.errorMessage, transportError: result.transportError ?? null }, secrets);
  window.dispatchEvent(new CustomEvent('api-result', { detail }));
  if (check) {
    const passed = result.status === check.status &&
      (check.code ? result.errorCode === check.code && result.payload?.success === false : result.ok) &&
      (!check.validate || check.validate(result.data));
    recordCheck(check.id, passed, `${method} ${path} → ${check.status}${check.code ? ` / ${check.code}` : ''}`, detail);
  }
  return result;
}

export async function request(path, options) {
  const result = await inspectRequest(path, options);
  if (!result.ok) {
    const error = new Error(redact(result.transportError || `HTTP ${result.status}${result.errorCode ? ` · ${result.errorCode}` : ''}: ${result.errorMessage || '요청 실패. 실제 HTTP 응답을 확인하세요.'}`, [options?.token, options?.body?.password]));
    error.status = result.status;
    error.authToken = options?.token;
    throw error;
  }
  return result.data;
}
