CREATE SEQUENCE IF NOT EXISTS entry_id_seq;

CREATE TABLE IF NOT EXISTS entry
(
    entry_id           INTEGER                  NOT NULL DEFAULT nextval('entry_id_seq'),
    title              VARCHAR(128)             NOT NULL,
    content            TEXT                     NOT NULL,
    tags               VARCHAR(64) ARRAY        NOT NULL DEFAULT ARRAY []::VARCHAR(64)[],
    created_by         VARCHAR(128)             NOT NULL DEFAULT 'system',
    created_date       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    last_modified_by   VARCHAR(128)             NOT NULL DEFAULT 'system',
    last_modified_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    PRIMARY KEY (entry_id)
);

CREATE INDEX IF NOT EXISTS entry_last_modified_date ON entry (last_modified_date);