SELECT entry_id,
       title,
       content,
       tags,
       created_by,
       created_date,
       last_modified_by,
       last_modified_date
FROM entry
WHERE entry_id = ?