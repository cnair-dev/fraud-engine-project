-- ==========================================================
-- Fraud Rule Engine - Enriched Demo Seed Script
-- ==========================================================

-- 1. Optional reference tables
CREATE TABLE IF NOT EXISTS merchants (
    id UUID PRIMARY KEY,
    name TEXT NOT NULL,
    reputation_score INT,
    blacklist_flag BOOLEAN,
    whitelist_flag BOOLEAN,
    mcc TEXT
);

CREATE TABLE IF NOT EXISTS devices (
    id UUID PRIMARY KEY,
    customer_id UUID,
    fingerprint TEXT,
    trust_score INT
);

-- 2. Customers (insert only if empty)
DO
$$
BEGIN
    IF (SELECT COUNT(*) FROM customers) = 0 THEN
        INSERT INTO customers (id, account_type, risk_segment, created_at) VALUES
        ('11111111-2222-3333-4444-555555555555','STANDARD','NORMAL',NOW() - INTERVAL '100 days'),
        ('22222222-3333-4444-5555-666666666666','PREMIUM','LOW',NOW() - INTERVAL '90 days'),
        ('33333333-4444-5555-6666-777777777777','STANDARD','HIGH',NOW() - INTERVAL '80 days'),
        ('44444444-5555-6666-7777-888888888888','BUSINESS','MEDIUM',NOW() - INTERVAL '200 days'),
        ('55555555-6666-7777-8888-999999999999','STANDARD','NORMAL',NOW() - INTERVAL '300 days'),
        ('66666666-7777-8888-9999-000000000000','CORPORATE','LOW',NOW() - INTERVAL '400 days'),
        ('77777777-8888-9999-aaaa-bbbbbbbbbbbb','STANDARD','MEDIUM',NOW() - INTERVAL '60 days'),
        ('88888888-9999-aaaa-bbbb-cccccccccccc','PREMIUM','LOW',NOW() - INTERVAL '40 days'),
        ('99999999-aaaa-bbbb-cccc-dddddddddddd','STANDARD','HIGH',NOW() - INTERVAL '30 days'),
        ('aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee','BUSINESS','MEDIUM',NOW() - INTERVAL '20 days');
        RAISE NOTICE 'Inserted demo customers.';
    ELSE
        RAISE NOTICE 'Customers already exist, skipping.';
    END IF;
END
$$;

-- 3. Merchants (insert only if empty)
DO
$$
BEGIN
    IF (SELECT COUNT(*) FROM merchants) = 0 THEN
        INSERT INTO merchants (id,name,reputation_score,blacklist_flag,whitelist_flag,mcc) VALUES
        ('11111111-aaaa-bbbb-cccc-111111111111','Amazon ZA',90,FALSE,TRUE,'5311'),
        ('22222222-aaaa-bbbb-cccc-222222222222','Zara SA',85,FALSE,FALSE,'5651'),
        ('33333333-aaaa-bbbb-cccc-333333333333','RiskyBet Sports',25,TRUE,FALSE,'7995'),
        ('44444444-aaaa-bbbb-cccc-444444444444','Uber Eats',80,FALSE,FALSE,'5812'),
        ('55555555-aaaa-bbbb-cccc-555555555555','Luxury Watch SA',50,FALSE,FALSE,'5944'),
        ('66666666-aaaa-bbbb-cccc-666666666666','Game Electronics',70,FALSE,FALSE,'5732'),
        ('77777777-aaaa-bbbb-cccc-777777777777','TravelNow Flights',60,FALSE,FALSE,'4511'),
        ('88888888-aaaa-bbbb-cccc-888888888888','CryptoExchange.io',20,TRUE,FALSE,'6051'),
        ('99999999-aaaa-bbbb-cccc-999999999999','Woolworths SA',95,FALSE,TRUE,'5411'),
        ('aaaaaaaa-bbbb-cccc-dddd-aaaaaaaaaaaa','eBay Imports',55,FALSE,FALSE,'5311'),
        ('bbbbbbbb-cccc-dddd-eeee-bbbbbbbbbbbb','SuperCar Hire',40,FALSE,FALSE,'5521'),
        ('cccccccc-dddd-eeee-ffff-cccccccccccc','BetNation',15,TRUE,FALSE,'7995'),
        ('dddddddd-eeee-ffff-gggg-dddddddddddd','Takealot',92,FALSE,TRUE,'5732');
        RAISE NOTICE 'Inserted demo merchants.';
    ELSE
        RAISE NOTICE 'Merchants already exist, skipping.';
    END IF;
END
$$;

-- 4. Devices (insert only if empty)
DO
$$
BEGIN
    IF (SELECT COUNT(*) FROM devices) = 0 THEN
        INSERT INTO devices (id,customer_id,fingerprint,trust_score) VALUES
        ('11111111-dead-beef-cafe-000000000001','11111111-2222-3333-4444-555555555555','fp-111-iphone14',95),
        ('11111111-dead-beef-cafe-000000000002','11111111-2222-3333-4444-555555555555','fp-112-macbook',90),
        ('11111111-dead-beef-cafe-000000000003','22222222-3333-4444-5555-666666666666','fp-201-ipad',88),
        ('11111111-dead-beef-cafe-000000000004','33333333-4444-5555-6666-777777777777','fp-301-android',40),
        ('11111111-dead-beef-cafe-000000000005','44444444-5555-6666-7777-888888888888','fp-401-windows',75),
        ('11111111-dead-beef-cafe-000000000006','55555555-6666-7777-8888-999999999999','fp-501-samsung',85),
        ('22222222-dead-beef-cafe-000000000007','66666666-7777-8888-9999-000000000000','fp-601-lenovo',92),
        ('33333333-dead-beef-cafe-000000000008','77777777-8888-9999-aaaa-bbbbbbbbbbbb','fp-701-iphone',89),
        ('44444444-dead-beef-cafe-000000000009','88888888-9999-aaaa-bbbb-cccccccccccc','fp-801-huawei',70),
        ('55555555-dead-beef-cafe-000000000010','99999999-aaaa-bbbb-cccc-dddddddddddd','fp-901-hp-laptop',78),
        ('66666666-dead-beef-cafe-000000000011','aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee','fp-1001-pixel',88);
        RAISE NOTICE 'Inserted demo devices.';
    ELSE
        RAISE NOTICE 'Devices already exist, skipping.';
    END IF;
END
$$;

-- 5. Transactions (generate ~250 transactions)
DO
$$
DECLARE
    cust UUID;
    dev  UUID;
    merch UUID;
    amt NUMERIC;
    ts  TIMESTAMPTZ;
    ch  TEXT;
    i   INT;
    merchants UUID[] := ARRAY[
        '11111111-aaaa-bbbb-cccc-111111111111'::uuid,
        '22222222-aaaa-bbbb-cccc-222222222222'::uuid,
        '33333333-aaaa-bbbb-cccc-333333333333'::uuid,
        '44444444-aaaa-bbbb-cccc-444444444444'::uuid,
        '55555555-aaaa-bbbb-cccc-555555555555'::uuid,
        '66666666-aaaa-bbbb-cccc-666666666666'::uuid,
        '77777777-aaaa-bbbb-cccc-777777777777'::uuid,
        '88888888-aaaa-bbbb-cccc-888888888888'::uuid,
        '99999999-aaaa-bbbb-cccc-999999999999'::uuid,
        'aaaaaaaa-bbbb-cccc-dddd-aaaaaaaaaaaa'::uuid,
        'bbbbbbbb-cccc-dddd-eeee-bbbbbbbbbbbb'::uuid,
        'cccccccc-dddd-eeee-ffff-cccccccccccc'::uuid,
        'dddddddd-eeee-ffff-gggg-dddddddddddd'::uuid
    ];
    devices  UUID[] := ARRAY[
        '11111111-dead-beef-cafe-000000000001'::uuid,
        '11111111-dead-beef-cafe-000000000002'::uuid,
        '11111111-dead-beef-cafe-000000000003'::uuid,
        '11111111-dead-beef-cafe-000000000004'::uuid,
        '11111111-dead-beef-cafe-000000000005'::uuid,
        '11111111-dead-beef-cafe-000000000006'::uuid,
        '22222222-dead-beef-cafe-000000000007'::uuid,
        '33333333-dead-beef-cafe-000000000008'::uuid,
        '44444444-dead-beef-cafe-000000000009'::uuid,
        '55555555-dead-beef-cafe-000000000010'::uuid,
        '66666666-dead-beef-cafe-000000000011'::uuid
    ];
    customers UUID[] := ARRAY[
        '11111111-2222-3333-4444-555555555555'::uuid,
        '22222222-3333-4444-5555-666666666666'::uuid,
        '33333333-4444-5555-6666-777777777777'::uuid,
        '44444444-5555-6666-7777-888888888888'::uuid,
        '55555555-6666-7777-8888-999999999999'::uuid,
        '66666666-7777-8888-9999-000000000000'::uuid,
        '77777777-8888-9999-aaaa-bbbbbbbbbbbb'::uuid,
        '88888888-9999-aaaa-bbbb-cccccccccccc'::uuid,
        '99999999-aaaa-bbbb-cccc-dddddddddddd'::uuid,
        'aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee'::uuid
    ];
    channels TEXT[] := ARRAY['POS','WEB','APP','ATM','ECOM'];
BEGIN
    IF (SELECT COUNT(*) FROM transactions) = 0 THEN
        FOR i IN 1..250 LOOP
            cust  := customers[(random()*9)::int + 1];
            merch := merchants[(random()*12)::int + 1];
            dev   := devices[(random()*10)::int + 1];
            amt   := round((random()*35000 + 50)::numeric, 2);
            ch    := channels[(random()*4)::int + 1];
            ts    := (NOW() AT TIME ZONE 'UTC')
                     - ((random()*60)::int || ' days')::interval
                     - ((random()*23)::int || ' hours')::interval;

            INSERT INTO transactions (
                id,
                customer_id,
                amount,
                currency,
                timestamp,
                merchant_id,
                mcc,
                channel,
                device_id,
                description,
                created_at
            )
            VALUES (
                gen_random_uuid(),
                cust,
                amt,
                'ZAR',
                ts,
                merch,
                COALESCE((SELECT mcc FROM merchants WHERE id = merch), '5812'),
                ch,
                dev,
                'Auto-generated transaction ' || ch,
                ts
            );
        END LOOP;

        RAISE NOTICE 'Inserted demo transactions.';
    ELSE
        RAISE NOTICE 'Transactions already exist, skipping.';
    END IF;
END
$$;

-- 6. Flagged transactions subset (insert only if empty)
DO
$$
BEGIN
    IF (SELECT COUNT(*) FROM flagged_transactions) = 0 THEN
        INSERT INTO flagged_transactions (id, txn_id, customer_id, score, decision, reason_codes, details, created_at)
        SELECT gen_random_uuid(), id, customer_id,
               65 + (random()*35)::int,
               CASE WHEN random()>0.75 THEN 'DECLINE' ELSE 'REVIEW' END,
               '["AMOUNT_SPIKE","MCC_RISK","GEO_VELOCITY"]',
               '{"AMOUNT_SPIKE":{"ratio":1.35},"MCC_RISK":{"mcc":"7995"},"GEO_VELOCITY":{"km":1500}}'::jsonb,
               NOW() - (random()*5 || ' days')::interval
        FROM transactions
        ORDER BY random()
        LIMIT 40;
        RAISE NOTICE 'Inserted demo flagged transactions.';
    ELSE
        RAISE NOTICE 'Flagged transactions already exist, skipping.';
    END IF;
END
$$;

-- 7. Summary
DO
$$
BEGIN
    RAISE NOTICE 'Demo seed complete: customers=%, merchants=%, devices=%, transactions=%, flags=%',
        (SELECT COUNT(*) FROM customers),
        (SELECT COUNT(*) FROM merchants),
        (SELECT COUNT(*) FROM devices),
        (SELECT COUNT(*) FROM transactions),
        (SELECT COUNT(*) FROM flagged_transactions);
END
$$;
