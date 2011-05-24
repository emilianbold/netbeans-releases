CREATE TABLE StackNode (
    id INT,
    caller_id INT NOT NULL,
    func_id INT NOT NULL,
    offset BIGINT NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE FuncNames (
    id INT NOT NULL,
    fname VARCHAR(16384) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE NodeMetrics (
    node_id INT NOT NULL,
    context_id BIGINT NOT NULL DEFAULT -1,
    bucket BIGINT NOT NULL,
    time_excl BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (node_id, context_id, bucket)
);

CREATE TABLE FuncMetrics (
    func_id INT NOT NULL,
    context_id BIGINT NOT NULL DEFAULT -1,
    bucket BIGINT NOT NULL,
    time_incl BIGINT NOT NULL DEFAULT 0,
    time_excl BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (func_id, context_id, bucket)
);

CREATE TABLE SourceFiles (
     id INT,
     path VARCHAR (16384),
     PRIMARY KEY(id)
);

CREATE TABLE SourceInfo (
    node_id INT NOT NULL,
    context_id BIGINT NOT NULL DEFAULT -1,
    file_id INT NOT NULL DEFAULT -1,    
    fline INT NOT NULL DEFAULT -1,
    fcolumn INT NOT NULL DEFAULT -1,
    file_offset INT NOT NULL DEFAULT -1,
    PRIMARY KEY (node_id, context_id)
);

CREATE TABLE Modules (
     id INT,
     path VARCHAR (1024),
     PRIMARY KEY(id)
);

CREATE TABLE ModuleInfo (
    node_id INT NOT NULL,
    context_id BIGINT NOT NULL DEFAULT -1,
    module_id INT NOT NULL DEFAULT -1,    
    module_offset INT NOT NULL DEFAULT -1,
    PRIMARY KEY (node_id, context_id)
);

