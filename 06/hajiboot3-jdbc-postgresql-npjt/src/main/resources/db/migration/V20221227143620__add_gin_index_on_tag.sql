CREATE INDEX IF NOT EXISTS entry_tags_gin ON entry USING GIN (tags);