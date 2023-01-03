SELECT entry_id,
       title,
       '' AS content,
       tags,
       created_by,
       created_date,
       last_modified_by,
       last_modified_date
FROM entry
WHERE tags @> ARRAY[:tag]
ORDER BY last_modified_date DESC