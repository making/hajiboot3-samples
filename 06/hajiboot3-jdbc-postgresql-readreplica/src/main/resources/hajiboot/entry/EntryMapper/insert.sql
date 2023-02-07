INSERT INTO entry(entry_id, title, content, tags, created_by, created_date,
                  last_modified_by, last_modified_date)
VALUES (:entryId, :title, :content, STRING_TO_ARRAY(:tags, ','), :createdBy, :createdDate,
        :lastModifiedBy, :lastModifiedDate)