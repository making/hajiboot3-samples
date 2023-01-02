WITH page AS (SELECT entry_id,
                     title,
                     '' AS content,
                     tags,
                     created_by,
                     created_date,
                     last_modified_by,
                     last_modified_date
              FROM entry
              WHERE last_modified_date > COALESCE(?, '-infinity'::timestamptz)
              ORDER BY last_modified_date ASC
              LIMIT %d)
SELECT *
FROM page
ORDER BY last_modified_date DESC