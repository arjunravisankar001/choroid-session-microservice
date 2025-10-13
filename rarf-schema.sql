CREATE TABLE rarf (
    session_id BIGINT NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    feedback_filled BOOLEAN NOT NULL DEFAULT false,
    rating INTEGER CHECK (rating BETWEEN 0 AND 5),

    understandable_score INTEGER CHECK (understandable_score BETWEEN 0 AND 10),
    confidence_score INTEGER CHECK (confidence_score BETWEEN 0 AND 10),
    expectations_score INTEGER CHECK (expectations_score BETWEEN 0 AND 10),
    engagement_score INTEGER CHECK (engagement_score BETWEEN 0 AND 10),
    organization_score INTEGER CHECK (organization_score BETWEEN 0 AND 10),
    relevance_score INTEGER CHECK (relevance_score BETWEEN 0 AND 10),
    presenter_score INTEGER CHECK (presenter_score BETWEEN 0 AND 10),
    pace_score INTEGER CHECK (pace_score BETWEEN 0 AND 10),

    most_valuable TEXT,
    suggestions TEXT,

    PRIMARY KEY (session_id, user_id)
);