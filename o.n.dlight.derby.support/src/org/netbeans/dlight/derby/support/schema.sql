

CREATE TABLE Func (
    func_id INT NOT NULL,
    func_name VARCHAR(255) NOT NULL,
    time_incl BIGINT NOT NULL DEFAULT 0,
    time_excl BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (func_id)
);

CREATE TABLE Node (
    node_id INT NOT NULL,
    caller_id INT NOT NULL,
    func_id INT NOT NULL,
    time_incl BIGINT NOT NULL DEFAULT 0,
    time_excl BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (node_id)
--    FOREIGN KEY (caller_id) REFERENCES (Node.node_id),
--    FOREIGN KEY (func_id) REFERENCES (Func.func_id)
);

CREATE TABLE CallStack (
    time_stamp BIGINT NOT NULL,
    cpu_id INT NOT NULL,
    thread_id INT NOT NULL,
    leaf_id INT NOT NULL,
    PRIMARY KEY (time_stamp, cpu_id, thread_id)
--    FOREIGN KEY (leaf_id) REFERENCES (Node.node_id)
);

CREATE INDEX CallStackTimestampIndex ON CallStack(time_stamp);
