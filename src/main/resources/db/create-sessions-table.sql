-- Create the 'sessions' table
CREATE TABLE sessions (
    id UUID PRIMARY KEY,
    creator_id VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    start TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    duration INTEGER NOT NULL CHECK (duration > 0),
    tags JSONB NOT NULL,
    meeting_link TEXT NOT NULL,
    resources_link TEXT NOT NULL
);

CREATE INDEX idx_sessions_tags ON sessions USING GIN (tags);