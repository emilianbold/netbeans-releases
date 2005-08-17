/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.db.sql.execute;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is used to create a TableModel for a ResultSet.
 *
 * @author Andrei Badea
 */
public class ResultSetTableModelSupport {
    
    /**
     * Holds the ColumnTypeDef for all the types in java.sql.Types
     */
    static final Map/*<Integer,ColumnTypeDef>*/ TYPE_TO_DEF = new HashMap();
    
    static {
        // editable types
        
        ColumnTypeDef booleanTypeDef = new GenericWritableColumnDef(Boolean.class);
        
        TYPE_TO_DEF.put(new Integer(Types.BOOLEAN), booleanTypeDef);
        TYPE_TO_DEF.put(new Integer(Types.BIT), booleanTypeDef);
        
        ColumnTypeDef integerTypeDef = new GenericWritableColumnDef(Integer.class);
        
        TYPE_TO_DEF.put(new Integer(Types.TINYINT), integerTypeDef);
        TYPE_TO_DEF.put(new Integer(Types.SMALLINT), integerTypeDef);
        TYPE_TO_DEF.put(new Integer(Types.INTEGER), integerTypeDef);
        
        ColumnTypeDef charTypeDef = new GenericWritableColumnDef(String.class);
        
        TYPE_TO_DEF.put(new Integer(Types.CHAR), charTypeDef);
        TYPE_TO_DEF.put(new Integer(Types.VARCHAR), charTypeDef);
        
        ColumnTypeDef longTypeDef = new GenericWritableColumnDef(Long.class);
        
        TYPE_TO_DEF.put(new Integer(Types.BIGINT), longTypeDef);
        
        ColumnTypeDef floatTypeDef = new GenericWritableColumnDef(Double.class);
        
        TYPE_TO_DEF.put(new Integer(Types.FLOAT), floatTypeDef);
        TYPE_TO_DEF.put(new Integer(Types.DOUBLE), floatTypeDef);
        
        ColumnTypeDef decimalTypeDef = new GenericWritableColumnDef(BigDecimal.class);
        
        TYPE_TO_DEF.put(new Integer(Types.REAL), decimalTypeDef);
        TYPE_TO_DEF.put(new Integer(Types.NUMERIC), decimalTypeDef);
        TYPE_TO_DEF.put(new Integer(Types.DECIMAL), decimalTypeDef);
        
        ColumnTypeDef dateTypeDef = new GenericWritableColumnDef(Date.class);
        
        TYPE_TO_DEF.put(new Integer(Types.DATE), dateTypeDef);
        TYPE_TO_DEF.put(new Integer(Types.TIME), dateTypeDef);
        TYPE_TO_DEF.put(new Integer(Types.TIMESTAMP), dateTypeDef);        
        
        // binary types -- we can't edit them, and 
        // we display them like "0xdeadbeef..."
        
        ColumnTypeDef binaryTypeDef = new ColumnTypeDef() {
            public boolean isWritable() {
                return false;
            }
            public Class getColumnClass() {
                return Object.class;
            }
            public Object getColumnValue(ResultSet rs, int column) throws SQLException, IOException {
                return BinaryColumnValue.forBinaryColumn(rs, column);
            }
        };
        
        TYPE_TO_DEF.put(new Integer(Types.BINARY), binaryTypeDef);
        TYPE_TO_DEF.put(new Integer(Types.VARBINARY), binaryTypeDef);
        TYPE_TO_DEF.put(new Integer(Types.LONGVARBINARY), binaryTypeDef);
        
        // blob type -- we can't edit it, and 
        // we display it like "0xdeadbeef..."
        
        ColumnTypeDef blobTypeDef = new ColumnTypeDef() {
            public boolean isWritable() {
                return false;
            }
            public Class getColumnClass() {
                return Object.class;
            }
            public Object getColumnValue(ResultSet rs, int column) throws SQLException, IOException {
                return BinaryColumnValue.forBlobColumn(rs, column);
            }
        };
        
        TYPE_TO_DEF.put(new Integer(Types.BLOB), blobTypeDef);
        
        // long varchar type -- we don't retrieve the full contents (it is too
        // long), and we display only the first n characters
        
        ColumnTypeDef longVarCharTypeDef = new ColumnTypeDef() {
            public boolean isWritable() {
                return false;
            }
            public Class getColumnClass() {
                return Object.class;
            }
            public Object getColumnValue(ResultSet rs, int column) throws SQLException, IOException {
                return LongVarCharColumnValue.forCharColumn(rs, column);
            }
        };
        
        TYPE_TO_DEF.put(new Integer(Types.LONGVARCHAR), longVarCharTypeDef);
        
        // clob type -- we don't retrieve the full contents (it is are too
        // long), and we display only the first n characters
        
        ColumnTypeDef clobTypeDef = new ColumnTypeDef() {
            public boolean isWritable() {
                return false;
            }
            public Class getColumnClass() {
                return Object.class;
            }
            public Object getColumnValue(ResultSet rs, int column) throws SQLException, IOException {
                return LongVarCharColumnValue.forClobColumn(rs, column);
            }
        };
        
        TYPE_TO_DEF.put(new Integer(Types.CLOB), clobTypeDef);
        
        // other types -- we can hardly edit them
        
        ColumnTypeDef otherTypeDef = new ColumnTypeDef() {
            public boolean isWritable() {
                return false;
            }
            public Class getColumnClass() {
                return Object.class;
            }
            public Object getColumnValue(ResultSet rs, int column) throws SQLException {
                return rs.getObject(column);
            }
        };
        
        TYPE_TO_DEF.put(new Integer(Types.NULL), otherTypeDef);
        TYPE_TO_DEF.put(new Integer(Types.OTHER), otherTypeDef);
        TYPE_TO_DEF.put(new Integer(Types.JAVA_OBJECT), otherTypeDef);
        TYPE_TO_DEF.put(new Integer(Types.DISTINCT), otherTypeDef);
        TYPE_TO_DEF.put(new Integer(Types.STRUCT), otherTypeDef);
        TYPE_TO_DEF.put(new Integer(Types.ARRAY), otherTypeDef);
        TYPE_TO_DEF.put(new Integer(Types.REF), otherTypeDef);
        TYPE_TO_DEF.put(new Integer(Types.DATALINK), otherTypeDef);
    }
    
    /**
     * Describes how we handle columns.
     */
    private interface ColumnTypeDef {
        
        /**
         * Do we know how to edit this column?
         */
        public boolean isWritable();
        
        /**
         * The class used in the table model.
         */
        public Class getColumnClass();
        
        /**
         * The value displayed in the table.
         */
        public Object getColumnValue(ResultSet rs, int column) throws SQLException, IOException;
    }
    
    /**
     * Helper ColumnTypeDef for writable types.
     */
    private static final class GenericWritableColumnDef implements ColumnTypeDef {
        
        private Class columnClass;
        
        public GenericWritableColumnDef(Class columnClass) {
            this.columnClass = columnClass;
        }
        
        public boolean isWritable() {
            return true;
        }
        
        public Class getColumnClass() {
            return columnClass;
        }
        
        public Object getColumnValue(ResultSet rs, int column) throws SQLException, IOException {
            return rs.getObject(column);
        }
    }
    
    /**
     * Represents the value of a long varchar or clob column. Instances of this
     * class are placed in the table model for the result set.
     */
    private static final class LongVarCharColumnValue {

        private static final int COUNT = 100;

        private String data;
        
        public static LongVarCharColumnValue forCharColumn(ResultSet rs, int column) throws SQLException, IOException {
            Reader reader = rs.getCharacterStream(column);
            try {
                return new LongVarCharColumnValue(reader);
            } finally {
                reader.close();
            }
        }
        
        public static LongVarCharColumnValue forClobColumn(ResultSet rs, int column) throws SQLException, IOException {
            Clob clob = rs.getClob(column);
            Reader reader = clob.getCharacterStream();
            try {
                return new LongVarCharColumnValue(reader);
            } finally {
                reader.close();
            }
        }

        private LongVarCharColumnValue(Reader reader) throws SQLException, IOException {
            char[] charData = new char[COUNT];
            int read = reader.read(charData, 0, charData.length);

            data = new String(charData, 0, read);

            // display an ellipsis if there are more characters in the stream
            if (read >= COUNT && reader.read() != -1) {
                data += "..."; // NOI18N
            }
        }

        public String toString() {
            return data;
        }
    }

    /**
     * Represents the value of a binary or blob column. Instances of this
     * class are placed in the table model for the result set.
     */
    private static final class BinaryColumnValue {

        private static final int COUNT = 100;

        private String data;
        
        public static BinaryColumnValue forBinaryColumn(ResultSet rs, int column) throws SQLException, IOException {
            InputStream input = rs.getBinaryStream(column);
            try {
                return new BinaryColumnValue(input);
            } finally {
                input.close();
            }
        }
        
        public static BinaryColumnValue forBlobColumn(ResultSet rs, int column) throws SQLException, IOException {
            Blob blob = rs.getBlob(column);
            InputStream input = blob.getBinaryStream();
            try {
                return new BinaryColumnValue(input);
            } finally {
                input.close();
            }
        }

        private BinaryColumnValue(InputStream input) throws SQLException, IOException {
            byte[] byteData = new byte[COUNT];
            int read = input.read(byteData, 0, byteData.length);
            StringBuffer buffer = new StringBuffer(2 + 2 * read);

            buffer.append("0x"); // NOI18N
            for (int i = 0; i < read; i++) {
                int b = byteData[i];
                if (b < 0) {
                    b += 256;
                }
                if (b < 16) {
                    buffer.append('0');
                }
                buffer.append(Integer.toHexString(b).toUpperCase());
            }

            // display an ellipsis if there are more characters in the stream
            if (read >= COUNT && input.read() != -1) {
                buffer.append("..."); // NOI18N
            }

            data = buffer.toString();
        }

        public String toString() {
            return data;
        }
    }
    
    private static ColumnTypeDef getColumnTypeDef(int type) {
        return (ColumnTypeDef)TYPE_TO_DEF.get(new Integer(type));
    }
    
    public static List/*<ColumnDef>*/ createColumnDefs(ResultSetMetaData rsmd) throws SQLException {
        int count = rsmd.getColumnCount();
        List columns = new ArrayList(count);

        for (int i = 1; i <= count; i++) {
            int type = rsmd.getColumnType(i);
            ColumnTypeDef ctd = getColumnTypeDef(type);
            
            // TODO: does writable depend on the result set type (updateable?)
            
            ColumnDef column = new ColumnDef(
                    rsmd.getColumnName(i), 
                    rsmd.isWritable(i) && ctd.isWritable(),
                    ctd.getColumnClass());
            
            columns.add(column);
        }
        return columns;
    }
    
    public static List/*<List>*/ retrieveRows(ResultSet rs, ResultSetMetaData rsmd, FetchLimitHandler handler) throws SQLException, IOException {
        List rows = new ArrayList();
        int columnCount = rsmd.getColumnCount();
        int fetchLimit = handler.getFetchLimit();

        while (rs.next()) {
            int fetchCount = rows.size();
            if (fetchLimit > 0 && fetchCount >= fetchLimit) {
                fetchLimit = handler.fetchLimitReached(fetchCount);
                if (fetchLimit != 0 && fetchLimit <= fetchCount) {
                    break;
                }
            }

            List row = new ArrayList();
            for (int i = 1; i <= columnCount; i++) {
                if (rs.getObject(i) == null) {
                    row.add(NullValue.getDefault());
                } else {
                    int type = rsmd.getColumnType(i);
                    ColumnTypeDef ctd = getColumnTypeDef(type);
                    Object columnValue = ctd.getColumnValue(rs, i);
                    row.add(columnValue);
                }
            }
            rows.add(row);
        }
        return rows;
    }
}
