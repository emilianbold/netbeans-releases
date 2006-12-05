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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.xtest.pes.dbfeeder;

/*
 * Main.java
 *
 * Created on February 11, 2002, 2:25 PM
 */

import java.sql.Statement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.beans.IntrospectionException;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.io.PrintStream;

import org.netbeans.xtest.pes.PESLogger;
import java.util.logging.Level;
/**
 *
 * @author  <a href="mailto:adam.sotona@sun.com>Adam Sotona</a>
 */
public class DbUtils {

    Connection connection;
    PrintStream out = System.out;
    
    /** Creates a new instance of Main */
    public DbUtils(Connection connection) {
        this.connection = connection;
    }

    /** Creates a new instance of Main */
    public DbUtils(String URL, String userName, String password) throws SQLException {
        connection = DriverManager.getConnection(URL, userName, password);
    }
    
    public void setOut(PrintStream out) {
        this.out = out;
    }
    
    public Object getLast(String table, String returnColumn) throws SQLException {
        return queryFirst(table, returnColumn, new String[0], new Object[0], returnColumn, false);
    }

    public Object getFirst(String table, String returnColumn) throws SQLException {
        return queryFirst(table, returnColumn, new String[0], new Object[0], returnColumn, true);
    }

    public Object getFirst(String table, String returnColumn, String sortColumn, boolean ascending) throws SQLException {
        return queryFirst(table, returnColumn, new String[0], new Object[0], sortColumn, ascending);
    }

    public Object queryLast(String table, String returnColumn, String conditionColumn, Object conditionValue) throws SQLException {
        return queryFirst(table, returnColumn, new String[]{conditionColumn}, new Object[]{conditionValue}, returnColumn, false);
    }

    public Object queryLast(String table, String returnColumn, String[] conditionColumns, Object[] conditionValues) throws SQLException {
        return queryFirst(table, returnColumn, conditionColumns, conditionValues, returnColumn, false);
    }

    public Object queryFirst(String table, String returnColumn, String conditionColumn, Object conditionValue) throws SQLException {
        return queryFirst(table, returnColumn, new String[]{conditionColumn}, new Object[]{conditionValue}, returnColumn, true);
    }

    public Object queryFirst(String table, String returnColumn, String[] conditionColumns, Object[] conditionValues) throws SQLException {
        return queryFirst(table, returnColumn, conditionColumns, conditionValues, returnColumn, true);
    }
    
    public Object queryFirst(String table, String returnColumn, String conditionColumn, Object conditionValue, String sortColumn, boolean ascending) throws SQLException {
        return queryFirst(table, returnColumn, new String[]{conditionColumn}, new Object[]{conditionValue}, sortColumn, ascending);
    }

    public Object queryFirst(String table, String returnColumn, String[] conditionColumns, Object[] conditionValues, String sortColumn, boolean ascending) throws SQLException {
        PreparedStatement stmt = prepareQueryStatement(table, new String[]{returnColumn}, conditionColumns, conditionValues, sortColumn, ascending);
        ResultSet resultSet = stmt.executeQuery();
        resultSet.beforeFirst();
        if (!resultSet.first()) {
            resultSet.close();
            stmt.close();
            return null;
        }
        Object o = resultSet.getObject(1);
        resultSet.close();
        stmt.close();
        return o;
    }

    public Object[] getFirst(String table, String[] returnColumns) throws SQLException {
        return queryFirst(table, returnColumns, null, null, null, true);
    }

    public Object[] getFirst(String table, String[] returnColumns, String sortColumn, boolean ascending) throws SQLException {
        return queryFirst(table, returnColumns, null, null, sortColumn, ascending);
    }
    
    public Object[] queryFirst(String table, String[] returnColumns, String[] conditionColumns, Object[] conditionValues) throws SQLException {
        return queryFirst(table, returnColumns, conditionColumns, conditionValues, null, true);
    }
    
    public Object[] queryFirst(String table, String[] returnColumns, String[] conditionColumns, Object[] conditionValues, String sortColumn, boolean ascending) throws SQLException {
        PreparedStatement stmt = prepareQueryStatement(table, returnColumns, conditionColumns, conditionValues, sortColumn, ascending);
        ResultSet resultSet = stmt.executeQuery();
        if (!resultSet.first()) {
            resultSet.close();
            stmt.close();
            return null;
        }
        Object[] result = new Object[returnColumns.length];
        for (int i=0;i<returnColumns.length;i++) {
            result[i] = resultSet.getObject(i+1);
        }
        resultSet.close();
        stmt.close();
        return result;
    }

    public Object[] getAll(String table, String returnColumn) throws SQLException {
        return query(table, returnColumn, new String[0], new Object[0], null, true);
    }

    public Object[] getAll(String table, String returnColumn, String sortColumn, boolean ascending) throws SQLException {
        return query(table, returnColumn, new String[0], new Object[0], sortColumn, ascending);
    }

    public Object[] query(String table, String returnColumn, String conditionColumn, Object conditionValue) throws SQLException {
        return query(table, returnColumn, new String[]{conditionColumn}, new Object[]{conditionValue}, null, true);
    }

    public Object[] query(String table, String returnColumn, String[] conditionColumns, Object[] conditionValues) throws SQLException {
        return query(table, returnColumn, conditionColumns, conditionValues, null, true);
    }
    
    public Object[] query(String table, String returnColumn, String conditionColumn, Object conditionValue, String sortColumn, boolean ascending) throws SQLException {
        return query(table, returnColumn, new String[]{conditionColumn}, new Object[]{conditionValue}, sortColumn, ascending);
    }
    
    public Object[] query(String table, String returnColumn, String[] conditionColumns, Object[] conditionValues, String sortColumn, boolean ascending) throws SQLException {
        PreparedStatement stmt = prepareQueryStatement(table, new String[]{returnColumn}, conditionColumns, conditionValues, sortColumn, ascending);
        ResultSet resultSet = stmt.executeQuery();
        ArrayList list = new ArrayList();
        resultSet.beforeFirst();
        while (resultSet.next()) {
            list.add(resultSet.getObject(1));
        }
        resultSet.close();
        stmt.close();
        return (Object[])list.toArray(new Object[list.size()]);
    }

    public Object[][] getAll(String table, String[] returnColumns) throws SQLException {
        return query(table, returnColumns, new String[0], new Object[0] , null, true);
    }
    
    public Object[][] getAll(String table, String[] returnColumns, String sortColumn, boolean ascending) throws SQLException {
        return query(table, returnColumns, new String[0], new Object[0], sortColumn, ascending);
    }
    
    public Object[][] query(String table, String[] returnColumns, String conditionColumn, Object conditionValue) throws SQLException {
        return query(table, returnColumns, new String[]{conditionColumn}, new Object[]{conditionValue}, null, true);
    }
      
    public Object[][] query(String table, String[] returnColumns, String[] conditionColumns, Object[] conditionValues) throws SQLException {
        return query(table, returnColumns, conditionColumns, conditionValues, null, true);
    }
    
    public Object[][] query(String table, String[] returnColumns, String conditionColumn, Object conditionValue, String sortColumn, boolean ascending) throws SQLException {
        return query(table, returnColumns, new String[]{conditionColumn}, new Object[]{conditionValue}, sortColumn, ascending);
    }
    
    public Object[][] query(String table, String[] returnColumns, String[] conditionColumns, Object[] conditionValues, String sortColumn, boolean ascending) throws SQLException {
        PreparedStatement stmt = prepareQueryStatement(table, returnColumns, conditionColumns, conditionValues, sortColumn, ascending);
        ResultSet resultSet = stmt.executeQuery();
        ArrayList list = new ArrayList();
        resultSet.beforeFirst();
        while (resultSet.next()) {
            Object[] result = new Object[returnColumns.length];
            for (int i=0;i<returnColumns.length;i++) {
                result[i] = resultSet.getObject(i+1);
            }
            list.add(result);
        }
        resultSet.close();
        stmt.close();
        return (Object[][])list.toArray(new Object[list.size()][returnColumns.length]);
    }
    
    public PreparedStatement prepareQueryStatement(String table, String[] returnColumns, String[] conditionColumns, Object[] conditionValues, String sortColumn, boolean ascending) throws SQLException {
        StringBuffer query = new StringBuffer("select ");
        if (returnColumns==null || returnColumns.length<1) {
            query.append('*');
        } else {
            query.append(returnColumns[0]);
            for (int i=1;i<returnColumns.length;i++) {
                query.append(',');
                query.append(returnColumns[i]);
            }
        }
        query.append(" from ");
        query.append(table);
        if (conditionColumns!=null && conditionColumns.length>0) {
            query.append(" where ");
            query.append(conditionColumns[0]);
            if (conditionValues[0]==null) {
                query.append(" is null ");
            } else {
                query.append("=? ");
            }
            for (int i=1;i<conditionColumns.length;i++) {
                query.append("and ");
                query.append(conditionColumns[i]);
                if (conditionValues[i]==null) {
                    query.append(" is null ");
                } else {
                    query.append("=? ");
                }
            }
        }
        if (sortColumn!=null) {
            query.append(" order by ");
            query.append(sortColumn);
            if (ascending) {
                query.append(" asc ");
            } else {
                query.append(" desc ");
            }
        }
        PreparedStatement statement = connection.prepareStatement(query.toString(), ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        if (conditionColumns!=null && conditionColumns.length>0) {
            int index=1;
            for (int i=0;i<conditionColumns.length;i++) {
                if (conditionValues[i]!=null) {
                    statement.setObject(index++, conditionValues[i]);
                }
            }
        }
        return statement;
    }
    
    public int insert(String table, String[] columns, Object values[]) throws SQLException {
        StringBuffer sb = new StringBuffer("insert into ");
        sb.append(table);
        sb.append(" (");
        for (int i=0;i<columns.length;i++) {
            if (values[i]!=null) {
                sb.append(columns[i]);
                sb.append(',');
            }
        }
        sb.deleteCharAt(sb.length()-1);
        sb.append(") values (");
        for (int i=0;i<columns.length;i++) {
            if (values[i]!=null) {
                sb.append("?,");
            }
        }
        sb.deleteCharAt(sb.length()-1);
        sb.append(')');
        PreparedStatement statement = connection.prepareStatement(sb.toString());
        int j=1;
        for (int i=0;i<columns.length;i++) {
            Object value = values[i];
            if (value != null) {
                // this is ugly Oracle hack, since Oracle is not capable
                // of storing String > 4000 chars to varchar2 type
                // this should be done definitely in a different way (ResultsMetaData)
                // but this hack should be sufficient for time being
                if (value instanceof java.lang.String) {
                    String sValue = (String)value;
                    if (sValue.length() > 4000) {                        
                        value = sValue.substring(0,4000);
                    }
                }
                // Oracle hack end
                statement.setObject(j++, value);
            }
        }
        int ret = statement.executeUpdate();
        statement.close();
        return ret;
    }
    
    public Object insertAutoIncrement(String table, String autoIncrementColumn, String[] columns, Object values[]) throws SQLException {
        // statement.setObject(i, "") inserts null into Oracle database. Then we
        // have problem when searching for empty string. We need to prevent such
        // inconsistency and that's why we set null value for empty strings.
        for(int i=0;i<values.length;i++) {
            if(values[i] instanceof String && values[i].toString().length()==0) {
                values[i] = null;
            }
        }
        insert(table, columns, values);
        return queryLast(table, autoIncrementColumn, columns, values);
    }

    /** Inserts values into given table if record doesn't exist. Otherwise
     * it just returns value of autoIncrementColumn for existing record.
     * @param table name of table
     * @param autoIncrementColumn name of auto-increment column
     * @param columns an array of column names
     * @param values an array of column values
     * @return value of autoIncrementColumn for existing or just inserted record.
     */
    public Object insertAutoIncrementIfNotExist(String table, String autoIncrementColumn, String[] columns, Object values[]) throws SQLException {
        // statement.setObject(i, "") inserts null into Oracle database. Then we
        // have problem when searching for empty string. We need to prevent such
        // inconsistency and that's why we set null value for empty strings.
        for(int i=0;i<values.length;i++) {
            if(values[i] instanceof String && values[i].toString().length()==0) {
                values[i] = null;
            }
        }
        // try to find values in table
        Object id = queryFirst(table, autoIncrementColumn, columns, values);
        if(id == null) {
            // not found => insert a new record into table
            StringBuffer valuesToInsert = new StringBuffer();
            for (int i = 0; i < values.length; i++) {
                Object object = values[i];
                valuesToInsert.append(values[i]);
                valuesToInsert.append(" ");
            }
            PESLogger.logger.finest("Inserting "+valuesToInsert+"into "+table);
            id = insertAutoIncrement(table, autoIncrementColumn, columns, values);
        }
        return id;
    }
    
    public Object[] query(Object bean, String returnColumn) throws IntrospectionException, SQLException {
        return query(bean, returnColumn, null, true, new String[0]);
    }

    public Object[] query(Object bean, String returnColumn, String exceptColumn) throws IntrospectionException, SQLException {
        return query(bean, returnColumn, null, true, new String[]{exceptColumn});
    }
    
    public Object[] query(Object bean, String returnColumn, String[] exceptColumns) throws IntrospectionException, SQLException {
        return query(bean, returnColumn, null, true, exceptColumns);
    }

    public Object[] query(Object bean, String returnColumn, String sortColumn, boolean ascending) throws IntrospectionException, SQLException {
        return query(bean, returnColumn, sortColumn, ascending, new String[0]);
    }

    public Object[] query(Object bean, String returnColumn, String sortColumn, boolean ascending, String exceptColumn) throws IntrospectionException, SQLException {
        return query(bean, returnColumn, sortColumn, ascending, new String[]{exceptColumn});
    }

    public Object[] query(Object bean, String returnColumn, String sortColumn, boolean ascending, String[] exceptColumns) throws IntrospectionException, SQLException {
        BeanInfo info = Introspector.getBeanInfo(bean.getClass(),Object.class);
        String table = info.getBeanDescriptor().getName();
        PropertyDescriptor[] desc=info.getPropertyDescriptors();
        List exceptions;
        if (exceptColumns!=null) {
            exceptions = Arrays.asList(exceptColumns);
        } else {
            exceptions = new ArrayList();
        }
        ArrayList columns = new ArrayList();
        ArrayList values = new ArrayList();
        Statement stat = connection.createStatement(); 
        ResultSet set = stat.executeQuery("select * from "+table+" where 1=2");
        for(int i=0;i<desc.length;i++) {
            Object value;
            try {
                if (!exceptions.contains(desc[i].getName())) {
                    set.findColumn(desc[i].getName());
                    value = desc[i].getReadMethod().invoke(bean, null);
                    columns.add(desc[i].getName());
                    values.add(value);
                }
            } catch (Exception e) {
                out.println(e);
            }
        }
        set.close();
        stat.close();
        return query(table, returnColumn, (String[])columns.toArray(new String[columns.size()]), (Object[])values.toArray(new Object[values.size()]), sortColumn, ascending);
    }
    
    public Object queryFirst(Object bean, String returnColumn) throws IntrospectionException, SQLException {
        return queryFirst(bean, returnColumn, returnColumn, true, new String[0]);
    }
    
    public Object queryFirst(Object bean, String returnColumn, String exceptColumn) throws IntrospectionException, SQLException {
        return queryFirst(bean, returnColumn, returnColumn, true, new String[]{exceptColumn});
    }
    
    public Object queryFirst(Object bean, String returnColumn, String[] exceptColumns) throws IntrospectionException, SQLException {
        return queryFirst(bean, returnColumn, returnColumn, true, exceptColumns);
    }
    
    public Object queryLast(Object bean, String returnColumn) throws IntrospectionException, SQLException {
        return queryFirst(bean, returnColumn, returnColumn, false, new String[0]);
    }
    
    public Object queryLast(Object bean, String returnColumn, String exceptColumn) throws IntrospectionException, SQLException {
        return queryFirst(bean, returnColumn, returnColumn, false, new String[]{exceptColumn});
    }
    
    public Object queryLast(Object bean, String returnColumn, String[] exceptColumns) throws IntrospectionException, SQLException {
        return queryFirst(bean, returnColumn, returnColumn, false, exceptColumns);
    }
    
    public Object queryFirst(Object bean, String returnColumn, String sortColumn, boolean ascending) throws IntrospectionException, SQLException {
        return queryFirst(bean, returnColumn, sortColumn, ascending, new String[0]);
    }
    
    public Object queryFirst(Object bean, String returnColumn, String sortColumn, boolean ascending, String exceptColumn) throws IntrospectionException, SQLException {
        return queryFirst(bean, returnColumn, sortColumn, ascending, new String[]{exceptColumn});
    }
    
    public Object queryFirst(Object bean, String returnColumn, String sortColumn, boolean ascending, String[] exceptColumns) throws IntrospectionException, SQLException {
        BeanInfo info = Introspector.getBeanInfo(bean.getClass(),Object.class);
        String table = info.getBeanDescriptor().getName();
        PropertyDescriptor[] desc=info.getPropertyDescriptors();
        List exceptions;
        if (exceptColumns!=null) {
            exceptions = Arrays.asList(exceptColumns);
        } else {
            exceptions = new ArrayList();
        }
        ArrayList columns = new ArrayList();
        ArrayList values = new ArrayList();
        Statement stat = connection.createStatement(); 
        ResultSet set = stat.executeQuery("select * from "+table+" where 1=2");
        for(int i=0;i<desc.length;i++) {
            Object value;
            try {
                if (!exceptions.contains(desc[i].getName())) {
                    set.findColumn(desc[i].getName());
                    value = desc[i].getReadMethod().invoke(bean, null);
                    columns.add(desc[i].getName());
                    values.add(value);
                }
             } catch (SQLException sqle) {
                // column is not found - no problem 
                //System.out.println("Cannot find column for getter "+desc[i].getName()+", sqle"+sqle);
            } catch (InvocationTargetException ite) {
                // this should not happen
                System.out.println("ite for "+desc[i].getName()+", ite="+ite);
            } catch (IllegalAccessException iae) {
                // this should not happen as well
                System.out.println("iae for "+desc[i].getName()+", iae="+iae);
            }
        }
        set.close();
        stat.close();
        return queryFirst(table, returnColumn, (String[])columns.toArray(new String[columns.size()]), (Object[])values.toArray(new Object[values.size()]), sortColumn, ascending);
    }
    
    public Object[] getAllBeans(Class beanClass) throws IntrospectionException, SQLException {
        return queryBeans(beanClass, new String[0], new Object[0]);
    }
    
    public Object[] queryBeans(Class beanClass, String conditionColumn, Object conditionValue) throws IntrospectionException, SQLException {
        return queryBeans(beanClass, new String[]{conditionColumn}, new Object[]{conditionValue});
    }
    
    Object adjustValue(Class dest, Object source) throws Exception {
        if (dest.isPrimitive()) {
            if (dest.equals(Boolean.TYPE)) {
                return Boolean.valueOf(source.toString());
            } else if (dest.equals(Character.TYPE)) {
                return new Character(source.toString().charAt(0));
            } else if (dest.equals(Byte.TYPE)) {
                return new Byte(source.toString());
            } else if (dest.equals(Short.TYPE)) {
                return new Short(source.toString());
            } else if (dest.equals(Integer.TYPE)) {
                return new Integer(source.toString());
            } else if (dest.equals(Long.TYPE)) {
                return new Long(source.toString());
            } else if (dest.equals(Float.TYPE)) {
                return new Float(source.toString());
            } else if (dest.equals(Double.TYPE)) {
                return new Double(source.toString());
            }
        }
        return source;
    }
    
    public Object[] queryBeans(Class beanClass, String[] conditionColumns, Object[] conditionValues) throws IntrospectionException, SQLException {
        BeanInfo info = Introspector.getBeanInfo(beanClass,Object.class);
        String table = info.getBeanDescriptor().getName();
        ResultSet result = prepareQueryStatement(table, null, conditionColumns, conditionValues, null, true).executeQuery();
        result.beforeFirst();
        ArrayList properties = new ArrayList(Arrays.asList(info.getPropertyDescriptors()));
        ArrayList values = new ArrayList();
        try {
            while (result.next()) {
                Object bean = beanClass.getConstructor(null).newInstance(null);
                for (int i=0;i<properties.size();i++) {
                    PropertyDescriptor property = (PropertyDescriptor)properties.get(i);
                    try {
                        Object value = result.getObject(property.getName());
                        value = adjustValue(property.getPropertyType(), value);
                        property.getWriteMethod().invoke(bean, new Object[]{value});
                    } catch (Exception e) {
                        System.out.println("XXXE1");
                        out.println(e);
                        properties.remove(i--);
                    }                        
                }
                values.add(bean);
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        result.close();
        return values.toArray((Object[])Array.newInstance(beanClass,values.size()));
    }
    
    public int insertBean(Object bean) throws IntrospectionException, SQLException {
        BeanInfo info = Introspector.getBeanInfo(bean.getClass(),Object.class);
        String table = info.getBeanDescriptor().getName();
        PropertyDescriptor[] desc=info.getPropertyDescriptors();
        ArrayList columns = new ArrayList();
        ArrayList values = new ArrayList();
        Statement stat = connection.createStatement(); 
        ResultSet set = stat.executeQuery("select * from "+table+" where 1=2");
        for(int i=0;i<desc.length;i++) {
            Object value;
            try {
                //System.out.println("insertBean: desc[i]="+desc[i].getName());
                set.findColumn(desc[i].getName());
                value = desc[i].getReadMethod().invoke(bean, null);
                columns.add(desc[i].getName());
                values.add(value);
            } catch (SQLException sqle) {
                // column is not found - no problem 
                //System.out.println("Cannot find column for getter "+desc[i].getName()+", sqle"+sqle);
            } catch (InvocationTargetException ite) {
                // this should not happen
                System.out.println("ite for "+desc[i].getName()+", ite="+ite);
            } catch (IllegalAccessException iae) {
                // this should not happen as well
                System.out.println("iae for "+desc[i].getName()+", iae="+iae);
            }
        }
        set.close();
        stat.close();
        return insert(table, (String[])columns.toArray(new String[columns.size()]), (Object[])values.toArray(new Object[values.size()]));
    }
        
    public Object insertBeanAutoIncrement(Object bean, String autoIncrementColumn) throws IntrospectionException, SQLException {
        insertBean(bean);
        return queryLast(bean, autoIncrementColumn, autoIncrementColumn);
    }
    
    /* deletes from tableName all rows where ... */
    public void deleteFromTable(String tableName, String where) throws SQLException {
        String deleteCommand = "DELETE FROM "+tableName;
        if (where != null) {
            deleteCommand +=" WHERE "+where;
        }
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(deleteCommand);
        stmt.close();
    }
    
    // executes updatr
    
    
    // 
    public boolean anyResultsFromQuery(String query) {
       Statement stmt = null;
       ResultSet rs = null;
       boolean result = false;
       try {
           stmt = connection.createStatement();
           rs = stmt.executeQuery(query);
           result = rs.next();
       } catch (SQLException sqle) {
           PESLogger.logger.log(Level.SEVERE,"Caught SQLException when performing query: "+query,sqle);
       }
       closeResultSet(rs);
       closeStatement(stmt);
       return result;
   }
    
    
   // helper methods for closing results sets statememts and conncections
   public static boolean closeResultSet(ResultSet rs) {
       if (rs != null) {
           try {
               rs.close();
               return true;
           } catch (SQLException sqle) {
               PESLogger.logger.log(Level.SEVERE,"Caught SQLException when closing result set",sqle);
           }
       }
       return false;
   }

   public static boolean closeStatement(Statement stmt) {
       if (stmt != null) {
           try {
               stmt.close();
               return true;
           } catch (SQLException sqle) {
               PESLogger.logger.log(Level.SEVERE,"Caught SQLException when closing statement",sqle);
           }
       }
       return false;
   }
   
   public static boolean closeConnection(Connection connection) {
       if (connection != null) {
           try {
               connection.close();
               return true;
           } catch (SQLException sqle) {
               PESLogger.logger.log(Level.SEVERE,"Caught SQLException when closing connection",sqle);
           }
       }
       return false;
   }
    
}
