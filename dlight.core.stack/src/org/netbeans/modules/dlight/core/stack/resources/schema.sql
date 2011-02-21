CREATE TABLE Func (
    func_id INT NOT NULL,
    func_name VARCHAR(16384) NOT NULL,
    func_source_file_id INT NOT NULL DEFAULT -1,
    line_number INT NOT NULL DEFAULT -1,    
--  FOREIGN KEY (func_source_file_id) REFERENCES (SourceFiles.id)    
--    time_incl BIGINT NOT NULL DEFAULT 0,
--    time_excl BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (func_id)
);

CREATE TABLE FuncMetricAggr (
    func_id INT NOT NULL,
    bucket_id BIGINT NOT NULL,
    time_incl BIGINT NOT NULL DEFAULT 0,
    time_excl BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (func_id, bucket_id)
--  FOREIGN KEY (func_id) REFERENCES (Func.func_id)
);

CREATE TABLE Node (
    node_id INT NOT NULL,
    caller_id INT NOT NULL,
    func_id INT NOT NULL,
    offset BIGINT NOT NULL,
    time_incl BIGINT NOT NULL DEFAULT 0,
    time_excl BIGINT NOT NULL DEFAULT 0,
    line_number INT NOT NULL DEFAULT -1,
    PRIMARY KEY (node_id)
--    FOREIGN KEY (caller_id) REFERENCES (Node.node_id),
--    FOREIGN KEY (func_id) REFERENCES (Func.func_id)
);

CREATE TABLE SourceFiles (
    id INT {AUTO_INCREMENT} ,
    source_file VARCHAR (16384)    ,
    PRIMARY KEY(id)
);
