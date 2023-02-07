INSERT INTO entry(entry_id, title, content, tags, created_by, created_date,
                  last_modified_by, last_modified_date)
VALUES (10001, 'Test Title 1', 'Test Content 1', STRING_TO_ARRAY('aa,bb,cc', ','), 'test',
        now(), 'test', now());
INSERT INTO entry(entry_id, title, content, tags, created_by, created_date,
                  last_modified_by, last_modified_date)
VALUES (10002, 'Test Title 2', 'Test Content 2', STRING_TO_ARRAY('aa,bb,cc', ','), 'test',
        now(), 'test', now());
INSERT INTO entry(entry_id, title, content, tags, created_by, created_date,
                  last_modified_by, last_modified_date)
VALUES (10003, 'Test Title 3', 'Test Content 3', STRING_TO_ARRAY('aa,bb,cc', ','), 'test',
        now(), 'test', now());