
INSERT INTO rarf (session_id, user_id, feedback_filled, rating,
    understandable_score, confidence_score, expectations_score,
    engagement_score, organization_score, relevance_score,
    presenter_score, pace_score, most_valuable, suggestions)
VALUES
(1, 'user1', true, 4, 8, 7, 9, 9, 8, 9, 9, 8, 'Clear explanations', 'Add more examples'),

(1, 'user2', true, 5, 10, 9, 10, 10, 10, 10, 10, 9, 'Great presenter', 'None'),

(1, 'user3', true, 3, 6, 6, 5, 7, 6, 7, 7, 6, 'Good intro', 'Too fast in the middle'),

(2, 'user1', true, 4, 8, 7, 8, 8, 7, 8, 8, 7, 'Loved the content', 'Could include a summary'),

(2, 'user4', true, 2, 3, 4, 2, 3, 4, 3, 3, 4, 'Slides were helpful', 'Presenter was hard to follow'),

(3, 'user1', true, 5, 10, 10, 10, 10, 10, 10, 10, 10, 'Perfect session', 'None'),

(3, 'user2', true, 3, 5, 6, 5, 6, 6, 5, 5, 5, 'Some parts were good', 'Improve engagement'),

(3, 'user3', true, 4, 7, 7, 7, 8, 7, 7, 8, 7, 'Interactive session', 'Add more hands-on tasks'),

(3, 'user4', true, 1, 2, 2, 2, 2, 2, 2, 2, 2, 'Needs improvement', 'Simplify explanations'),

(4, 'user1', true, 5, 10, 10, 10, 10, 10, 10, 10, 10, 'Top-notch!', 'None'),

(4, 'user5', true, 4, 9, 9, 8, 9, 9, 9, 9, 8, 'Well delivered', 'Include reference links'),

(5, 'user1', true, 3, 6, 5, 6, 6, 5, 6, 5, 6, 'Basic coverage', 'More real-world examples'),

(5, 'user2', true, 4, 8, 8, 8, 8, 8, 8, 9, 8, 'Great coverage', 'Include quiz at end'),

(5, 'user3', true, 5, 10, 9, 9, 9, 9, 9, 9, 9, 'Very helpful', 'Nothing to add'),

(5, 'user4', true, 2, 4, 3, 4, 4, 3, 4, 4, 4, 'Not engaging', 'More energy from presenter'),

(2, 'user3', true, 3, 5, 6, 5, 6, 6, 5, 6, 5, 'Decent', 'Could be more visual'),

(2, 'user5', true, 4, 9, 9, 8, 9, 8, 9, 9, 8, 'Clear structure', 'None'),

(1, 'user4', true, 3, 6, 5, 6, 6, 5, 6, 6, 5, 'OK session', 'Improve examples'),

(4, 'user2', true, 4, 8, 8, 8, 9, 8, 8, 9, 8, 'Nice pacing', 'Keep that up'),

(4, 'user3', true, 2, 4, 4, 4, 4, 4, 4, 4, 4, 'Not bad', 'Could have been shorter');
