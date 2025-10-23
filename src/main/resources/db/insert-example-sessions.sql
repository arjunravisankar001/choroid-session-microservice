-- Insert 1: Typical session
INSERT INTO sessions (
    id,
    creator_id,
    title,
    start,
    duration,
    tags,
    meeting_link,
    resources_link
)
VALUES (
    '3e4e8c0b-9a52-4f2b-9c14-5d91b1c42f8f',
    'alice',
    'Spring Boot Crash Course',
    '2025-10-20 15:00:00',
    90,
    '["spring-boot", "java", "web-development"]'::jsonb,
    'https://meet.google.com/xyz-abcd-efg',
    'https://drive.google.com/file/spring-boot-resources'
);


-- Insert 2: Another valid session
INSERT INTO sessions (
    id,
    creator_id,
    title,
    start,
    duration,
    tags,
    meeting_link,
    resources_link
)
VALUES (
    '1a4d6c10-7549-493c-9c4a-df8c9d3b5e77',
    'bob',
    'Introduction to Probability',
    '2025-10-21 18:30:00',
    60,
    '["probability", "math", "statistics"]'::jsonb,
    'https://zoom.us/j/1234567890',
    'https://myserver.com/resources/probability.pdf'
);


-- Insert 3: For testing edge tags
INSERT INTO sessions (
    id,
    creator_id,
    title,
    start,
    duration,
    tags,
    meeting_link,
    resources_link
)
VALUES (
    'd4c88d7b-d716-49cb-8d10-57c9be15ef35',
    'charlie',
    'Advanced Operating Systems',
    '2025-11-01 10:00:00',
    120,
    '["operating-systems", "kernel", "process-management"]'::jsonb,
    'https://teams.microsoft.com/l/meetup-join/19%3ameeting-abcdef',
    'https://repo.example.com/os-course/resources.zip'
);
