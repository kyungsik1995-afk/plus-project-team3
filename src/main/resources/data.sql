INSERT INTO products (name, category, price, stock_quantity, description, created_at, updated_at)
SELECT '사과', 'FOOD', 100, 1, '사과 테스트 상품', '2026-08-01 09:00:00', '2026-08-01 09:00:00'
WHERE NOT EXISTS (
    SELECT 1
    FROM products
    WHERE name = '사과'
);

INSERT INTO products (name, category, price, stock_quantity, description, created_at, updated_at)
SELECT '바나나', 'FOOD', 200, 2, '바나나 테스트 상품', '2026-08-02 09:00:00', '2026-08-02 09:00:00'
WHERE NOT EXISTS (
    SELECT 1
    FROM products
    WHERE name = '바나나'
);

INSERT INTO products (name, category, price, stock_quantity, description, created_at, updated_at)
SELECT '우유', 'FOOD', 300, 3, '우유 테스트 상품', '2026-08-03 09:00:00', '2026-08-03 09:00:00'
WHERE NOT EXISTS (
    SELECT 1
    FROM products
    WHERE name = '우유'
);

INSERT INTO products (name, category, price, stock_quantity, description, created_at, updated_at)
SELECT '계란', 'FOOD', 400, 4, '계란 테스트 상품', '2026-08-04 09:00:00', '2026-08-04 09:00:00'
WHERE NOT EXISTS (
    SELECT 1
    FROM products
    WHERE name = '계란'
);

INSERT INTO products (name, category, price, stock_quantity, description, created_at, updated_at)
SELECT '빵', 'FOOD', 500, 5, '빵 테스트 상품', '2026-08-05 09:00:00', '2026-08-05 09:00:00'
WHERE NOT EXISTS (
    SELECT 1
    FROM products
    WHERE name = '빵'
);

INSERT INTO products (name, category, price, stock_quantity, description, created_at, updated_at)
SELECT '치즈', 'FOOD', 600, 6, '치즈 테스트 상품', '2026-08-06 09:00:00', '2026-08-06 09:00:00'
WHERE NOT EXISTS (
    SELECT 1
    FROM products
    WHERE name = '치즈'
);

INSERT INTO products (name, category, price, stock_quantity, description, created_at, updated_at)
SELECT '소고기', 'FOOD', 700, 7, '소고기 테스트 상품', '2026-08-07 09:00:00', '2026-08-07 09:00:00'
WHERE NOT EXISTS (
    SELECT 1
    FROM products
    WHERE name = '소고기'
);

INSERT INTO products (name, category, price, stock_quantity, description, created_at, updated_at)
SELECT '쌀', 'FOOD', 800, 8, '쌀 테스트 상품', '2026-08-08 09:00:00', '2026-08-08 09:00:00'
WHERE NOT EXISTS (
    SELECT 1
    FROM products
    WHERE name = '쌀'
);

INSERT INTO products (name, category, price, stock_quantity, description, created_at, updated_at)
SELECT '라면', 'FOOD', 900, 9, '라면 테스트 상품', '2026-08-09 09:00:00', '2026-08-09 09:00:00'
WHERE NOT EXISTS (
    SELECT 1
    FROM products
    WHERE name = '라면'
);

INSERT INTO products (name, category, price, stock_quantity, description, created_at, updated_at)
SELECT '김치', 'FOOD', 1000, 10, '김치 테스트 상품', '2026-08-10 09:00:00', '2026-08-10 09:00:00'
WHERE NOT EXISTS (
    SELECT 1
    FROM products
    WHERE name = '김치'
);

INSERT INTO products (name, category, price, stock_quantity, description, created_at, updated_at)
SELECT '반팔티', 'FASHION', 10000, 10, '반팔티 테스트 상품', '2026-08-11 09:00:00', '2026-08-11 09:00:00'
WHERE NOT EXISTS (
    SELECT 1
    FROM products
    WHERE name = '반팔티'
);

INSERT INTO products (name, category, price, stock_quantity, description, created_at, updated_at)
SELECT '긴팔티', 'FASHION', 20000, 20, '긴팔티 테스트 상품', '2026-08-12 09:00:00', '2026-08-12 09:00:00'
WHERE NOT EXISTS (
    SELECT 1
    FROM products
    WHERE name = '긴팔티'
);

INSERT INTO products (name, category, price, stock_quantity, description, created_at, updated_at)
SELECT '청바지', 'FASHION', 30000, 30, '청바지 테스트 상품', '2026-08-13 09:00:00', '2026-08-13 09:00:00'
WHERE NOT EXISTS (
    SELECT 1
    FROM products
    WHERE name = '청바지'
);

INSERT INTO products (name, category, price, stock_quantity, description, created_at, updated_at)
SELECT '반바지', 'FASHION', 40000, 40, '반바지 테스트 상품', '2026-08-14 09:00:00', '2026-08-14 09:00:00'
WHERE NOT EXISTS (
    SELECT 1
    FROM products
    WHERE name = '반바지'
);

INSERT INTO products (name, category, price, stock_quantity, description, created_at, updated_at)
SELECT '후드티', 'FASHION', 50000, 50, '후드티 테스트 상품', '2026-08-15 09:00:00', '2026-08-15 09:00:00'
WHERE NOT EXISTS (
    SELECT 1
    FROM products
    WHERE name = '후드티'
);

INSERT INTO products (name, category, price, stock_quantity, description, created_at, updated_at)
SELECT '자켓', 'FASHION', 60000, 60, '자켓 테스트 상품', '2026-08-16 09:00:00', '2026-08-16 09:00:00'
WHERE NOT EXISTS (
    SELECT 1
    FROM products
    WHERE name = '자켓'
);

INSERT INTO products (name, category, price, stock_quantity, description, created_at, updated_at)
SELECT '운동화', 'FASHION', 70000, 70, '운동화 테스트 상품', '2026-08-17 09:00:00', '2026-08-17 09:00:00'
WHERE NOT EXISTS (
    SELECT 1
    FROM products
    WHERE name = '운동화'
);

INSERT INTO products (name, category, price, stock_quantity, description, created_at, updated_at)
SELECT '코트', 'FASHION', 80000, 80, '코트 테스트 상품', '2026-08-18 09:00:00', '2026-08-18 09:00:00'
WHERE NOT EXISTS (
    SELECT 1
    FROM products
    WHERE name = '코트'
);

INSERT INTO products (name, category, price, stock_quantity, description, created_at, updated_at)
SELECT '모자', 'FASHION', 90000, 90, '모자 테스트 상품', '2026-08-19 09:00:00', '2026-08-19 09:00:00'
WHERE NOT EXISTS (
    SELECT 1
    FROM products
    WHERE name = '모자'
);

INSERT INTO products (name, category, price, stock_quantity, description, created_at, updated_at)
SELECT '가방', 'FASHION', 100000, 100, '가방 테스트 상품', '2026-08-20 09:00:00', '2026-08-20 09:00:00'
WHERE NOT EXISTS (
    SELECT 1
    FROM products
    WHERE name = '가방'
);

INSERT INTO products (name, category, price, stock_quantity, description, created_at, updated_at)
SELECT '마우스', 'ELECTRONICS', 100000, 100, '마우스 테스트 상품', '2026-08-21 09:00:00', '2026-08-21 09:00:00'
WHERE NOT EXISTS (
    SELECT 1
    FROM products
    WHERE name = '마우스'
);

INSERT INTO products (name, category, price, stock_quantity, description, created_at, updated_at)
SELECT '키보드', 'ELECTRONICS', 200000, 200, '키보드 테스트 상품', '2026-08-22 09:00:00', '2026-08-22 09:00:00'
WHERE NOT EXISTS (
    SELECT 1
    FROM products
    WHERE name = '키보드'
);

INSERT INTO products (name, category, price, stock_quantity, description, created_at, updated_at)
SELECT '이어폰', 'ELECTRONICS', 300000, 300, '이어폰 테스트 상품', '2026-08-23 09:00:00', '2026-08-23 09:00:00'
WHERE NOT EXISTS (
    SELECT 1
    FROM products
    WHERE name = '이어폰'
);

INSERT INTO products (name, category, price, stock_quantity, description, created_at, updated_at)
SELECT '헤드셋', 'ELECTRONICS', 400000, 400, '헤드셋 테스트 상품', '2026-08-24 09:00:00', '2026-08-24 09:00:00'
WHERE NOT EXISTS (
    SELECT 1
    FROM products
    WHERE name = '헤드셋'
);

INSERT INTO products (name, category, price, stock_quantity, description, created_at, updated_at)
SELECT '모니터', 'ELECTRONICS', 500000, 500, '모니터 테스트 상품', '2026-08-25 09:00:00', '2026-08-25 09:00:00'
WHERE NOT EXISTS (
    SELECT 1
    FROM products
    WHERE name = '모니터'
);

INSERT INTO products (name, category, price, stock_quantity, description, created_at, updated_at)
SELECT '태블릿', 'ELECTRONICS', 600000, 600, '태블릿 테스트 상품', '2026-08-26 09:00:00', '2026-08-26 09:00:00'
WHERE NOT EXISTS (
    SELECT 1
    FROM products
    WHERE name = '태블릿'
);

INSERT INTO products (name, category, price, stock_quantity, description, created_at, updated_at)
SELECT '노트북', 'ELECTRONICS', 700000, 700, '노트북 테스트 상품', '2026-08-27 09:00:00', '2026-08-27 09:00:00'
WHERE NOT EXISTS (
    SELECT 1
    FROM products
    WHERE name = '노트북'
);

INSERT INTO products (name, category, price, stock_quantity, description, created_at, updated_at)
SELECT '데스크탑', 'ELECTRONICS', 800000, 800, '데스크탑 테스트 상품', '2026-08-28 09:00:00', '2026-08-28 09:00:00'
WHERE NOT EXISTS (
    SELECT 1
    FROM products
    WHERE name = '데스크탑'
);

INSERT INTO products (name, category, price, stock_quantity, description, created_at, updated_at)
SELECT '스마트워치', 'ELECTRONICS', 900000, 900, '스마트워치 테스트 상품', '2026-08-29 09:00:00', '2026-08-29 09:00:00'
WHERE NOT EXISTS (
    SELECT 1
    FROM products
    WHERE name = '스마트워치'
);

INSERT INTO products (name, category, price, stock_quantity, description, created_at, updated_at)
SELECT '스마트폰', 'ELECTRONICS', 1000000, 1000, '스마트폰 테스트 상품', '2026-08-30 09:00:00', '2026-08-30 09:00:00'
WHERE NOT EXISTS (
    SELECT 1
    FROM products
    WHERE name = '스마트폰'
);
