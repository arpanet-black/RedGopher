SHUTDOWN

SELECT t0.id, t0.content, t0.creation_date, t0.display_text, t0.domain_name, 
        t0.gopher_path, t0.parent_path, t0.persist_over_restart, t0.port, 
        t1.id, t1.gopher_resource_type, t1.resource_description, 
        t1.server_resource_type, t0.resource_path 
    FROM "PUBLIC"."GOPHER_ITEM" t0 LEFT OUTER JOIN "PUBLIC"."RESOURCE_DESCRIPTOR" t1 ON 
        t0.resource_descriptor = t1.id 
    ORDER BY t0.creation_date ASC 
    
SELECT * FROM "PUBLIC"."SERVER_FILE_TYPE"
SELECT * FROM "PUBLIC"."GOPHER_ITEM"
SELECT * FROM "PUBLIC"."RESOURCE_DESCRIPTOR"