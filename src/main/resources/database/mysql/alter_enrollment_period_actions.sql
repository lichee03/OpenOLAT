-- Enrollment Period Auto-Actions Tracking Table
-- Stores executed auto-actions to prevent duplicate execution

CREATE TABLE IF NOT EXISTS o_en_period_action (
    id BIGINT NOT NULL AUTO_INCREMENT,
    creationdate DATETIME NOT NULL,
    course_id BIGINT NOT NULL,
    course_node_id VARCHAR(64) NOT NULL,
    action_type VARCHAR(32) NOT NULL,
    trigger_type VARCHAR(32) NOT NULL,
    executed_date DATETIME NOT NULL,
    success BOOLEAN NOT NULL DEFAULT FALSE,
    message VARCHAR(2048),
    affected_users INT,
    PRIMARY KEY (id)
);

CREATE INDEX idx_en_period_action_course ON o_en_period_action(course_id, course_node_id);
CREATE INDEX idx_en_period_action_executed ON o_en_period_action(executed_date);
CREATE INDEX idx_en_period_action_type ON o_en_period_action(action_type, trigger_type);
