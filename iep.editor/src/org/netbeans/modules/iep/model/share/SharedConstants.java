/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.iep.model.share;

import java.text.SimpleDateFormat;

// Must match library.xml
public interface SharedConstants {
    // Operator types (must match vm/ems/library.xml)
    public static final String OP_STREAM_INPUT = "StreamInput";
    
    public static final String OP_TABLE_INPUT = "TableInput";
    
    public static final String OP_STREAM_OUTPUT = "StreamOutput";
    
    public static final String OP_BATCHED_STREAM_OUTPUT = "BatchedStreamOutput";
    
    public static final String OP_RELATION_OUTPUT = "RelationOutput";
    
    public static final String OP_TABLE_OUTPUT = "TableOutput";
    
    public static final String OP_STREAM_PROJECTION_AND_FILTER = "StreamProjectionAndFilter";
    
    public static final String OP_TUPLE_SERIAL_CORRELATION = "TupleSerialCorrelation";

    public static final String OP_TUPLE_BASED_AGGREGATOR = "TupleBasedAggregator";

    public static final String OP_TIME_BASED_AGGREGATOR = "TimeBasedAggregator";

    public static final String OP_RELATION_AGGREGATOR = "RelationAggregator";
    
    public static final String OP_TUPLE_BASED_WINDOW = "TupleBasedWindow";
    
    public static final String OP_PARTITIONED_WINDOW = "PartitionedWindow";
    
    public static final String OP_ATTRIBUTE_BASED_WINDOW = "AttributeBasedWindow";
    
    public static final String OP_TIME_BASED_WINDOW = "TimeBasedWindow";
    
    public static final String OP_RELATION_MAP = "RelationMap";
    
    public static final String OP_DISTINCT = "Distinct";
    
    public static final String OP_UNION_ALL = "UnionAll";
    
    public static final String OP_UNION = "Union";
    
    public static final String OP_INTERSECT = "Intersect";
    
    public static final String OP_MINUS = "Minus";
    
    public static final String OP_INSERT_STREAM = "InsertStream";
    
    public static final String OP_DELETE_STREAM = "DeleteStream";
    
    public static final String OP_RELATION_STREAM = "RelationStream";
    
    public static final String OP_NOTIFICATION_STREAM = "NotificationStream";

    public static final String OP_TABLE = "Table";
    
    public static final String OP_GAP_WINDOW = "GapWindow";
    
    public static final String OP_CONTIGUOUS_ORDER = "ContiguousOrder";
    
    public static final String OP_INVOKE_SERVICE = "InvokeService";
    
    public static final String OP_INVOKE_STREAM = "InvokeStream";
    
    public static final String OP_EXTERNAL_TABLE_POLLING_STREAM = "ExternalTablePollingStream";
    
    public static final String OP_REPLAY_STREAM = "ReplayStream";
    
    public static final String OP_SAVE_STREAM = "SaveStream";

    public static final String OP_MERGE = "Merge";
    
    // Sub TcgCompoent
    public static String COMP_METADATA = "Metadata";
    
    public static String COMP_SCHEMAS = "Schemas";
    
    public static String COMP_OPERATORS = "Operators";

    public static String COMP_LINKS = "Links";

    public static String COMP_COMPONENT_NAMES = "ComponentNames";
    
    public static String COMP_VIEW = "View";
    
    // TcgProperty
    public static String PROP_ATTRIBUTE = "attribute";

    public static String PROP_ATTRIBUTE_LIST = "attributeList"; 
    
    public static String PROP_IS_SCHEMA_OWNER = "isSchemaOwner"; 

    public static String PROP_DOC = "doc"; 
    
    public static String PROP_FROM = "from";
    
    public static String PROP_ID = "id";
    
    public static String PROP_INPUT_TYPE = "inputType";
    
    public static String PROP_IS_RELATION_INPUT_STATIC = "isRelationInputStatic";
    
    public static String PROP_INPUT_MAX_COUNT = "inputMaxCount";
    
    public static String PROP_INPUT_ID_LIST = "inputIdList";
    
    public static String PROP_INPUT_SCHEMA_ID_LIST = "inputSchemaIdList";
    
    public static String PROP_NAME = "name";
    
    public static String PROP_DESCRIPTION = "description";
     
    public static String PROP_OUTPUT_TYPE = "outputType";
    
    public static String PROP_OUTPUT_SCHEMA_ID = "outputSchemaId";
    
    public static String PROP_ORTHO_FLOW = "orthoflow";
    
    public static String PROP_TOPO_SCORE = "topoScore";
    
    public static String PROP_FROM_COLUMN_LIST = "fromColumnList";
    
    public static String PROP_WHERE_CLAUSE = "whereClause";
    
    public static String PROP_TYPE = "type";
    
    public static String PROP_START = "start";

    public static String PROP_INCREMENT = "increment";

    public static String PROP_SIZE = "size";
    
    public static String PROP_SCALE = "scale";
    
    public static String PROP_COMMENT = "comment";
    
    public static String PROP_STATIC_INPUT_MAX_COUNT = "staticInputMaxCount";

    public static String PROP_STATIC_INPUT_ID_LIST = "staticInputIdList";
    
    public static String PROP_SUBTRACT_FROM = "subtractFrom";
    
    public static String PROP_TO = "to";
    
    //SAVE STREAM
    public static String PROP_IS_PRESERVE_TABLE = "isPreserveTable";
    
    public static String PROP_DATABASE_JNDI_NAME = "databaseJndiName";
    
    public static String PROP_TABLE_NAME = "tableName";
    
    public static String PROP_TO_COLUMN_LIST = "toColumnList";
    
    public static String PROP_VALUE = "value";
    
    public static String PROP_X = "x";
    
    public static String PROP_Y = "y";
    
    public static String PROP_Z = "z";
    
    public static String PROP_INCLUDE_TIMESTAMP = "includeTimestamp";
    
    public static String PROP_SERVICE_NAMESPACE_URI = "serviceNamespaceURI";
            
    public static String PROP_SERVICE_LOCAL_PART = "serviceLocalPart";

    public static String PROP_OPERATION_NAME = "operationName";
    
    public static String PROP_TEMPLATE = "template";
        
    public static String PROP_UNIT = "unit";
    
    public static String PROP_INCREMENT_UNIT = "incrementUnit";

    //-----Invoke Stream properties --
    public static String PROP_EXTERNAL_IEP_PROCESS_QUALIFIED_NAME = "externalIepProcessQualifiedName";
    
    public static String PROP_EXTERNAL_OPERATOR_NAME = "externalOperatorName";
    
    // ----- External Table Polling Stream operator
    public static String PROP_POLLING_INTERVAL = "pollingInterval";
    
    public static String PROP_POLLING_INTERVAL_TIME_UNIT = "pollingIntervalTimeUnit";
    
    public static String PROP_POLLING_RECORD_SIZE = "pollingRecordSize";
    
    public static String PROP_EXTERNAL_TABLE_NAME = "externalTableName";
    
    //following also used by Replay Stream
    public static String PROP_IS_PRESERVE_LAST_FETCHED_RECORD = "isPreserveLastFetchedRecord";
    
    public static String PROP_LAST_FETCHED_RECORD_TABLE = "lastFetchedRecordTable";
    
    public static String PROP_RECORD_IDENTIFIER_COLUMNS_SCHEMA = "recordIdentifierColumnsSchema";
    
    public static String PROP_IS_DELETE_RECORDS = "isDeleteRecords";
    
    //===================================================
    public static String PROP_IS_GLOBAL = "isGlobal"; 
    
    public static String PROP_DO_NOT_CREATE_TABLE = "doNotCreateTable";
    
    public static String PROP_GLOBAL_ID = "globalId";
    
    public static String PROP_FROM_CLAUSE = "fromClause";
    
    //====================================================
    public static String PROP_GROUP_BY_COLUMN_LIST = "groupByColumnList";
    
    public static String PROP_PROPERTY_EDITOR = "propertyEditor";
    
    public static String PROP_CATEGORY_ORDER = "categoryOrder";
    
    public static String PROP_PALETTE = "palette";

    public static String PROP_WS_TYPE = "wsType";
    
    public static String PROP_ENTRY = "entry";
    
    public static String PROP_EXIT = "exit";
     
    public static String PROP_BATCH_MODE = "batchMode";
    
    public static String PROP_BATCH_SIZE = "batchSize";

    public static String PROP_MAXIMUM_DELAY_SIZE = "maximumDelaySize";

    public static String PROP_MAXIMUM_DELAY_UNIT = "maximumDelayUnit";
    
    public static String PROP_HELP_ID = "helpID";    
    
    //==========Invoke Service============================
    public static String PROP_RESPONSE_ATTRIBUTE_LIST = "responseAttributeList";
    
    public static String PROP_RETAIN_ATTRIBUTE_LIST = "retainAttributeList";

    // Possible values for PROP_INPUT_TYPE and PROP_OUTPUT_TYPE
    public static String IO_TYPE_NONE = "i18n.IEP.IOType.none";

    public static String IO_TYPE_STREAM = "i18n.IEP.IOType.stream";

    public static String IO_TYPE_RELATION = "i18n.IEP.IOType.relation";
    
    public static String IO_TYPE_TABLE = "i18n.IEP.IOType.table";
    
    // Possible values for PROP_UNIT, PROP_INCREMENT_UNIT, PROP_SIZE
    public static String TIME_UNIT_SECOND = "second";
    
    public static String TIME_UNIT_MINUTE = "minute";
    
    public static String TIME_UNIT_HOUR = "hour";
    
    public static String TIME_UNIT_DAY = "day";
    
    public static String TIME_UNIT_WEEK = "week";
    
    // Possible values for WS_TYPE
    public static String WS_TYPE_NONE = "i18n.IEP.WSType.none";
    
    public static String WS_TYPE_IN_ONLY = "i18n.IEP.WSType.inOnly";
    
    public static String WS_TYPE_OUT_ONLY = "i18n.IEP.WSType.outOnly";
    
    public static String WS_TYPE_REQUEST_REPLY = "i18n.IEP.WSType.requestReply";
    
    // SQL types
    public static String SQL_TYPE_BIT = "BIT";
    public static String SQL_TYPE_TINYINT = "TINYINT";
    public static String SQL_TYPE_SMALLINT = "SMALLINT";
    public static String SQL_TYPE_INTEGER = "INTEGER";
    public static String SQL_TYPE_BIGINT = "BIGINT";
    public static String SQL_TYPE_REAL = "REAL";
    public static String SQL_TYPE_FLOAT = "FLOAT";
    public static String SQL_TYPE_DOUBLE = "DOUBLE";
    public static String SQL_TYPE_DECIMAL = "DECIMAL";
    public static String SQL_TYPE_NUMERIC = "NUMERIC";
    public static String SQL_TYPE_CHAR = "CHAR";
    public static String SQL_TYPE_VARCHAR = "VARCHAR";
    public static String SQL_TYPE_LONGVARCHAR = "LONGVARCHAR";
    public static String SQL_TYPE_DATE = "DATE";
    public static String SQL_TYPE_TIME = "TIME";
    public static String SQL_TYPE_TIMESTAMP = "TIMESTAMP";
    public static String SQL_TYPE_BINARY = "BINARY";
    public static String SQL_TYPE_VARBINARY = "VARBINARY";
    public static String SQL_TYPE_LONGVARBINARY = "LONGVARBINARY";
    public static String SQL_TYPE_BLOB = "BLOB";
    public static String SQL_TYPE_CLOB = "CLOB";

    public static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");
    
    public static String[] SQL_TYPE_NAMES = new String[] {
    //      SQL_TYPE_BIT,
    //      SQL_TYPE_TINYINT,
    //      SQL_TYPE_SMALLINT,
          SQL_TYPE_INTEGER,
          SQL_TYPE_BIGINT,
    //     SQL_TYPE_REAL,
    //      SQL_TYPE_FLOAT,
          SQL_TYPE_DOUBLE,
    //      SQL_TYPE_DECIMAL,
    //      SQL_TYPE_NUMERIC,
    //      SQL_TYPE_CHAR,
          SQL_TYPE_VARCHAR,
    //      SQL_TYPE_LONGVARCHAR,
          SQL_TYPE_DATE,
    //      SQL_TYPE_TIME, //#(http://www.netbeans.org/issues/show_bug.cgi?id=149576) remove the TIME datatype from the attribute type definition.
          SQL_TYPE_TIMESTAMP,
    //      SQL_TYPE_BINARY,
    //      SQL_TYPE_VARBINARY,
    //      SQL_TYPE_LONGVARBINARY,
    //      SQL_TYPE_BLOB,
    //      SQL_TYPE_CLOB, //#(http://www.netbeans.org/issues/show_bug.cgi?id=148892) remove the CLOB datatype from the attribute type definition.
    //      "ARRAY",
    //      "REF",
    //      "STRUCT",
  };
  
  //reserved column names  
  public static final String COL_SEQID = "ems_seqid";
  public static final String COL_TIMESTAMP = "ems_timestamp"; 
  
  
  public static String[] RESERVED_COLUMN_NAMES = new String[] {
      COL_SEQID,
      COL_TIMESTAMP
  };
  
  //default jndi name
  public static final String DEFAULT_JNDINAME = "jdbc/iepseDerbyNonXA"; 
}
    
