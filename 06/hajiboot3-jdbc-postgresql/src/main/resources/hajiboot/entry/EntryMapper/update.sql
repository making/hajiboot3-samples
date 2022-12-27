UPDATE entry
SET title              = ?,
    content            = ?,
    tags               = STRING_TO_ARRAY(?, ','),
    created_by         = ?,
    created_date       = ?,
    last_modified_by   = ?,
    last_modified_date = ?
WHERE entry_id = ?