INSERT INTO users (fullname,
                   username,
                   email,
                   password,
                   role,
                   active,
                   created_at,
                   updated_at)
VALUES ('Admin User',
        'admin',
        'admin@example.com',
        '$2a$12$pakMgzzJ6ARl2OWZhQW5pOOJpaDNk3CX1elHKDqg6lMbfQlRDjpPa', --@Password0
        'ADMIN',
        TRUE,
        NOW(),
        NOW());
