UPDATE entry
SET title              = :title,
    content            = :content,
    tags               = STRING_TO_ARRAY(:tags, ','),
    created_by         = :createdBy,
    created_date       = :createdDate,
    last_modified_by   = :lastModifiedBy,
    last_modified_date = :lastModifiedDate
WHERE entry_id = :entryId