/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.iep.editor.share;

import java.sql.Types;
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
    
       
    // Sub TcgCompoent
    public static String METADATA_KEY = "Metadata";
    
    public static String SCHEMAS_KEY = "Schemas";
    
    public static String OPERATORS_KEY = "Operators";

    public static String LINKS_KEY = "Links";

    public static String COMPONENT_NAMES_KEY = "ComponentNames";
    
    public static String VIEW_KEY = "View";
    
    // TcgProperty
    public static String ATTRIBUTE_KEY = "attribute";

    public static String ATTRIBUTE_LIST_KEY = "attributeList"; 
    
    public static String IS_SCHEMA_OWNER_KEY = "isSchemaOwner"; 

    public static String DOC_KEY = "doc"; 
    
    public static String FROM_KEY = "from";
    
    public static String ID_KEY = "id";
    
    public static String INPUT_TYPE_KEY = "inputType";
    
    public static String INPUT_MAX_COUNT_KEY = "inputMaxCount";
    
    public static String INPUT_ID_LIST_KEY = "inputIdList";
    
    public static String INPUT_SCHEMA_ID_LIST_KEY = "inputSchemaIdList";
    
    public static String NAME_KEY = "name";
    
    public static String DESCRIPTION_KEY = "description";
     
    public static String OUTPUT_TYPE_KEY = "outputType";
    
    public static String OUTPUT_SCHEMA_ID_KEY = "outputSchemaId";
    
    public static String ORTHO_FLOW_KEY = "orthoflow";
    
    public static String TOPO_SCORE_KEY = "topoScore";
    
    public static String FROM_COLUMN_LIST_KEY = "fromColumnList";
    
    public static String WHERE_CLAUSE_KEY = "whereClause";
    
    public static String TYPE_KEY = "type";
    
    public static String START_KEY = "start";

    public static String INCREMENT_KEY = "increment";

    public static String SIZE_KEY = "size";
    
    public static String SCALE_KEY = "scale";
    
    public static String STATIC_INPUT_MAX_COUNT_KEY = "staticInputMaxCount";

    public static String STATIC_INPUT_ID_LIST_KEY = "staticInputIdList";
    
    public static String SUBTRACT_FROM_KEY = "subtractFrom";
    
    public static String TO_KEY = "to";
    
    public static String TABLE_NAME_KEY = "tableName";
    
    public static String TO_COLUMN_LIST_KEY = "toColumnList";
    
    public static String VALUE_KEY = "value";
    
    public static String X_KEY = "x";
    
    public static String Y_KEY = "y";
    
    public static String Z_KEY = "z";
    
    public static String INCLUDE_TIMESTAMP_KEY = "includeTimestamp";
    
    public static String SERVICE_NAMESPACE_URI = "serviceNamespaceURI";
            
    public static String SERVICE_LOCAL_PART = "serviceLocalPart";

    public static String OPERATION_NAME = "operationName";
    
    public static String TEMPLATE = "template";
        
    public static String UNIT_KEY = "unit";
    
    public static String INCREMENT_UNIT_KEY = "incrementUnit";

    //===================================================
    public static String IS_GLOBAL_KEY = "isGlobal"; 
    
    public static String GLOBAL_ID_KEY = "globalId";
    
    public static String FROM_CLAUSE_KEY = "fromClause";
    
    //====================================================
    public static String GROUP_BY_COLUMN_LIST_KEY = "groupByColumnList";
    
    public static String PROPERTY_EDITOR_KEY = "propertyEditor";
    
    public static String CATEGORY_ORDER_KEY = "categoryOrder";
    
    public static String PALETTE_KEY = "palette";

    public static String WS_INPUT_KEY = "wsInput";
     
    public static String WS_OUTPUT_KEY = "wsOutput";
    
    public static String BATCH_MODE_KEY = "batchMode";
    
    public static String BATCH_SIZE_KEY = "batchSize";

    public static String MAXIMUM_DELAY_SIZE_KEY = "maximumDelaySize";

    public static String MAXIMUM_DELAY_UNIT_KEY = "maximumDelayUnit";
    
    // TcgProperty value
    public static String IO_TYPE_NONE = "i18n.IEP.IOType.none";

    public static String IO_TYPE_STREAM = "i18n.IEP.IOType.stream";

    public static String IO_TYPE_RELATION = "i18n.IEP.IOType.relation";
    
    public static String IO_TYPE_TABLE = "i18n.IEP.IOType.table";
    
    public static String TIME_UNIT_SECOND = "second";
    
    public static String TIME_UNIT_MINUTE = "minute";
    
    public static String TIME_UNIT_HOUR = "hour";
    
    public static String TIME_UNIT_DAY = "day";
    
    public static String TIME_UNIT_WEEK = "week";
    

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
}
    
