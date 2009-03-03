CREATE TABLE Func (
    func_id INT NOT NULL,
    func_name VARCHAR(255) NOT NULL,
    func_full_name VARCHAR(255) NOT NULL,
    time_incl BIGINT NOT NULL DEFAULT 0,
    time_excl BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (func_id)
);

CREATE TABLE Node (
    node_id INT NOT NULL,
    caller_id INT NOT NULL,
    func_id INT NOT NULL,
    offset BIGINT NOT NULL,
    time_incl BIGINT NOT NULL DEFAULT 0,
    time_excl BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (node_id)
--    FOREIGN KEY (caller_id) REFERENCES (Node.node_id),
--    FOREIGN KEY (func_id) REFERENCES (Func.func_id)
);
