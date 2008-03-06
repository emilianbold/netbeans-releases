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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.db.sql.execute;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is used to create a TableModel for a ResultSet.
 *
 * @author Andrei Badea
 */
public class ResultSetTableModelSupport {
    
    private static Logger LOGGER = Logger.getLogger(
            ResultSetTableModelSupport.class.getName());
    
    /**
     * Holds the ColumnTypeDef for all the types in java.sql.Types.
     * Not private because of unit tests.
     */
    static final Map<Integer,ColumnTypeDef> TYPE_TO_DEF = new HashMap<Integer,ColumnTypeDef>();
    
    /**
     * The default implementation of ColumnTypeDef used for SQL types for which
     * there is no value in {@link #TYPE_TO_DEF}.
     */
    private static ColumnTypeDef DEFAULT_COLUMN_DEF;
    
    static {
        // editable types
        
        ColumnTypeDef booleanTypeDef = new GenericWritableColumnDef(Boolean.class);
        
        TYPE_TO_DEF.put(Integer.valueOf(Types.BOOLEAN), booleanTypeDef);
        TYPE_TO_DEF.put(Integer.valueOf(Types.BIT), booleanTypeDef);

        ColumnTypeDef integerTypeDef = new GenericWritableColumnDef(Integer.class);

        TYPE_TO_DEF.put(Integer.valueOf(Types.TINYINT), integerTypeDef);
        TYPE_TO_DEF.put(Integer.valueOf(Types.SMALLINT), integerTypeDef);
        TYPE_TO_DEF.put(Integer.valueOf(Types.INTEGER), integerTypeDef);

        ColumnTypeDef charTypeDef = new GenericWritableColumnDef(String.class);
        
        TYPE_TO_DEF.put(Integer.valueOf(Types.CHAR), charTypeDef);
        TYPE_TO_DEF.put(Integer.valueOf(Types.VARCHAR), charTypeDef);
        
        // Issue 15248 - JDBC introduced NCHAR(-15), and NVARCHAR (-9),
        TYPE_TO_DEF.put(Integer.valueOf(-15), charTypeDef);
        TYPE_TO_DEF.put(Integer.valueOf(-9), charTypeDef);
        
        ColumnTypeDef longTypeDef = new GenericWritableColumnDef(Long.class);
        
        TYPE_TO_DEF.put(Integer.valueOf(Types.BIGINT), longTypeDef);
        
        ColumnTypeDef floatTypeDef = new GenericWritableColumnDef(Double.class);
        
        TYPE_TO_DEF.put(Integer.valueOf(Types.FLOAT), floatTypeDef);
        TYPE_TO_DEF.put(Integer.valueOf(Types.DOUBLE), floatTypeDef);
        
        ColumnTypeDef decimalTypeDef = new GenericWritableColumnDef(BigDecimal.class);
        
        TYPE_TO_DEF.put(Integer.valueOf(Types.REAL), decimalTypeDef);
        TYPE_TO_DEF.put(Integer.valueOf(Types.NUMERIC), decimalTypeDef);
        TYPE_TO_DEF.put(Integer.valueOf(Types.DECIMAL), decimalTypeDef);
        
        ColumnTypeDef dateTypeDef = new GenericWritableColumnDef(Date.class);
        
        TYPE_TO_DEF.put(Integer.valueOf(Types.DATE), dateTypeDef);
                
        // TIME type must displayed as time -- issue 72607
        
        ColumnTypeDef timeTypeDef = new ColumnTypeDef() {
            public boolean isWritable() {
                return true;
            }
            public Class getColumnClass() {
                return Time.class;
            }
            public Object getColumnValue(ResultSet rs, int column) throws SQLException, IOException {
                return rs.getTime(column);
            }
        };
        
        TYPE_TO_DEF.put(Integer.valueOf(Types.TIME), timeTypeDef);
        
        // TIMESTAMP type -- ensure that it is displayed as date and time
        // issue 64165, issue 70521
        
        TYPE_TO_DEF.put(Integer.valueOf(Types.TIMESTAMP), new ColumnTypeDef() {
            public boolean isWritable() {
                return true;
            }
            public Class getColumnClass() {
                return Timestamp.class;
            }
            public Object getColumnValue(ResultSet rs, int column) throws SQLException, IOException {
                return rs.getTimestamp(column);
            }
        });
        
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
        
        TYPE_TO_DEF.put(Integer.valueOf(Types.BINARY), binaryTypeDef);
        TYPE_TO_DEF.put(Integer.valueOf(Types.VARBINARY), binaryTypeDef);
        TYPE_TO_DEF.put(Integer.valueOf(Types.LONGVARBINARY), binaryTypeDef);
        
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
        
        TYPE_TO_DEF.put(Integer.valueOf(Types.BLOB), blobTypeDef);
        
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

        // Issue 125248 - JDBC4 added LONGNVARCHAR(-16) and SQLXML(2009)
        // We can't use the mnemonic because it would fail to compile
        // on JDK 6.  We can fix this once we only build on JDK6+
        TYPE_TO_DEF.put(Integer.valueOf(Types.LONGVARCHAR), longVarCharTypeDef);
        TYPE_TO_DEF.put(Integer.valueOf(-16), longVarCharTypeDef);
        TYPE_TO_DEF.put(Integer.valueOf(2009), longVarCharTypeDef);
        
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
        
        TYPE_TO_DEF.put(Integer.valueOf(Types.CLOB), clobTypeDef);
        
        // Issue 125248 - JDBC 4 added NCLOB.  We have to use the hardcoded
        // value because otherwise this class would not build under JDK 5
        TYPE_TO_DEF.put(Integer.valueOf(2011), clobTypeDef);
        
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
        
        TYPE_TO_DEF.put(Integer.valueOf(Types.NULL), otherTypeDef);
        TYPE_TO_DEF.put(Integer.valueOf(Types.OTHER), otherTypeDef);
        TYPE_TO_DEF.put(Integer.valueOf(Types.JAVA_OBJECT), otherTypeDef);
        TYPE_TO_DEF.put(Integer.valueOf(Types.DISTINCT), otherTypeDef);
        TYPE_TO_DEF.put(Integer.valueOf(Types.STRUCT), otherTypeDef);
        TYPE_TO_DEF.put(Integer.valueOf(Types.ARRAY), otherTypeDef);
        TYPE_TO_DEF.put(Integer.valueOf(Types.REF), otherTypeDef);
        TYPE_TO_DEF.put(Integer.valueOf(Types.DATALINK), otherTypeDef);
        
        // Issue 125248 - JDBC 4 introduced Types.ROWID.  Can't refer to it
        // directly because it will cause build failure on JDK 5.  So using
        // the hardcoded value of -8 until we start building on JDK 6 only.
        TYPE_TO_DEF.put(Integer.valueOf(-8), integerTypeDef);
        

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
     * Default ColumnTypeDef implementation: not writable and using
     * ResultSet.getObject() to read column values.
     */
    private static final class DefaultColumnDef implements ColumnTypeDef {
        
        public boolean isWritable() {
            return false;
        }
        
        public Class getColumnClass() {
            return Object.class;
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
            if (reader == null) {
                return null;
            }
            try {
                return new LongVarCharColumnValue(reader);
            } finally {
                reader.close();
            }
        }
        
        public static LongVarCharColumnValue forClobColumn(ResultSet rs, int column) throws SQLException, IOException {
            Clob clob = rs.getClob(column);
            if (clob == null) {
                return null;
            }
            Reader reader = clob.getCharacterStream();
            if (reader == null) {
                return null;
            }
            try {
                return new LongVarCharColumnValue(reader);
            } finally {
                reader.close();
            }
        }

        private LongVarCharColumnValue(Reader reader) throws SQLException, IOException {
            char[] charData = new char[COUNT];
            int read = reader.read(charData, 0, charData.length);

            if (read >= 0) {
                data = new String(charData, 0, read); 

                // display an ellipsis if there are more characters in the stream
                if (reader.read() != -1) {
                    data += "..."; // NOI18N
                }
            } else {
                data = ""; // NOI18N
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
            if (input == null) {
                return null;
            }
            try {
                return new BinaryColumnValue(input);
            } finally {
                input.close();
            }
        }
        
        public static BinaryColumnValue forBlobColumn(ResultSet rs, int column) throws SQLException, IOException {
            Blob blob = rs.getBlob(column);
            if (blob == null) {
                return null;
            }
            InputStream input = blob.getBinaryStream();
            if (input == null) {
                return null;
            }
            try {
                return new BinaryColumnValue(input);
            } finally {
                input.close();
            }
        }

        private BinaryColumnValue(InputStream input) throws SQLException, IOException {
            byte[] byteData = new byte[COUNT];
            int read = input.read(byteData, 0, byteData.length);
            
            if (read > 0) {
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
                if (input.read() != -1) {
                    buffer.append("..."); // NOI18N
                }

                data = buffer.toString();
            } else {
                data = ""; // NOI18N
            }
        }

        public String toString() {
            return data;
        }
    }
    
    /**
     * Not private because of unit tests.
     */
    static ColumnTypeDef getColumnTypeDef(DatabaseMetaData dbmd, int type) {
        // Issue 49994: Oracle DATE type needs to be retrieved as full
        // date and time
        if (type == Types.DATE && isOracle(dbmd)) {
            type = Types.TIMESTAMP;
        }
        
        ColumnTypeDef result = (ColumnTypeDef)TYPE_TO_DEF.get(Integer.valueOf(type));
        if (result != null) {
            return result;
        }
        
        synchronized (ResultSetTableModelSupport.class) {
            if (DEFAULT_COLUMN_DEF == null) {
                DEFAULT_COLUMN_DEF = new DefaultColumnDef();
            }
            return DEFAULT_COLUMN_DEF;
        }
    }
    
    /**
     * Returns a List of ColumnDef objects or null if the calling thread was
     * interrupted.
     */
    public static List<ColumnDef> createColumnDefs(DatabaseMetaData dbmd,
            ResultSetMetaData rsmd) throws SQLException {
        int count = rsmd.getColumnCount();
        List<ColumnDef> columns = new ArrayList<ColumnDef>(count);

        for (int i = 1; i <= count; i++) {
            if (Thread.currentThread().isInterrupted()) {
                return null;
            }
            
            int type = rsmd.getColumnType(i);
            ColumnTypeDef ctd = getColumnTypeDef(dbmd, type);
            
            // TODO: does writable depend on the result set type (updateable?)

            // issue 75700: the demo version of the Teradata DB throws SQLException on RSMD.isWritable()
            boolean writable = false;
            try {
                writable = rsmd.isWritable(i) && ctd.isWritable();
            } catch (SQLException e) {
                // ignore
            }

            ColumnDef column = new ColumnDef(
                    rsmd.getColumnName(i),
                    rsmd.getColumnLabel(i),
                    writable,
                    ctd.getColumnClass());
            
            columns.add(column);
        }
        return columns;
    }
    
    private static boolean isOracle(DatabaseMetaData dbmd) {
        try {
            return "Oracle".equals(dbmd.getDatabaseProductName());
        } catch ( SQLException sqle ) {
            LOGGER.log(Level.WARNING, "Unable to obtain database product name", sqle);
            return false;
        }
    }
    
    public static List<List<Object>> retrieveRows(DatabaseMetaData dbmd, ResultSet rs, ResultSetMetaData rsmd, FetchLimitHandler handler) throws SQLException, IOException {
        List<List<Object>> rows = new ArrayList<List<Object>>();
        int columnCount = rsmd.getColumnCount();
        int fetchLimit = handler.getFetchLimit();

        while (rs.next()) {
            if (Thread.currentThread().isInterrupted()) {
                return null;
            }
            
            int fetchCount = rows.size();
            if (fetchLimit > 0 && fetchCount >= fetchLimit) {
                fetchLimit = handler.fetchLimitReached(fetchCount);
                if (fetchLimit != 0 && fetchLimit <= fetchCount) {
                    break;
                }
            }

            List<Object> row = new ArrayList<Object>();
            for (int i = 1; i <= columnCount; i++) {
                if (Thread.currentThread().isInterrupted()) {
                    return null;
                }
                
                int type = rsmd.getColumnType(i);
                ColumnTypeDef ctd = getColumnTypeDef(dbmd, type);
                Object value = ctd.getColumnValue(rs, i);
                row.add(value != null ? value : NullValue.getDefault());
            }
            rows.add(row);
        }
        return rows;
    }
}
