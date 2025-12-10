CREATE TABLE IF NOT EXISTS po (
    poi_id BIGSERIAL PRIMARY KEY,
    po_number VARCHAR(255) UNIQUE,
    rfq_number VARCHAR(255),
    quotation_id BIGINT NULL,
    received_at TIMESTAMP NULL,
    delivered BOOLEAN DEFAULT FALSE,
    delivered_at TIMESTAMP NULL,
    income NUMERIC(19,2) NULL,
    po_total_amount NUMERIC(19,2) NULL,
    paid BOOLEAN DEFAULT FALSE,
    paid_at TIMESTAMP NULL,
    file_path TEXT
);

-- No FK constraint to avoid delete issues on quotation removal. If needed, add FK with ON DELETE SET NULL if DB supports it.

CREATE TABLE IF NOT EXISTS po_expense (
    id BIGSERIAL PRIMARY KEY,
    po_id BIGINT NOT NULL,
    expense_name VARCHAR(255),
    amount NUMERIC(19,2) DEFAULT 0,
    CONSTRAINT fk_po_expense_po FOREIGN KEY (po_id) REFERENCES po(poi_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_po_po_number ON po(po_number);
