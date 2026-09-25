-- Apply before starting the updated application when Hibernate schema updates are disabled.
CREATE TABLE IF NOT EXISTS outbox_events (
    id uuid PRIMARY KEY,
    exchange_name varchar(255) NOT NULL,
    routing_key varchar(255) NOT NULL,
    payload text NOT NULL,
    event_type varchar(255) NOT NULL,
    created_at timestamptz NOT NULL,
    next_attempt_at timestamptz NOT NULL,
    published_at timestamptz,
    attempts integer NOT NULL DEFAULT 0,
    last_error varchar(1000)
);
CREATE INDEX IF NOT EXISTS idx_outbox_pending
    ON outbox_events (published_at, next_attempt_at, created_at);
