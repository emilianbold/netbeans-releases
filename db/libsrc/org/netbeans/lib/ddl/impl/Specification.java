/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.lib.ddl.impl;

import java.beans.*;
import java.sql.*;
import java.util.*;
import java.text.MessageFormat;
import org.openide.util.NbBundle;

import org.netbeans.lib.ddl.*;
import org.netbeans.lib.ddl.adaptors.*;

import org.openide.*;

/**
* @author Slavek Psenicka
*/
public class Specification implements DatabaseSpecification {

    /** Used DBConnection */
    private HashMap desc;

    /** Used JDBC Connection */
    private Connection jdbccon;

    private static ResourceBundle bundle = NbBundle.getBundle("org.netbeans.lib.ddl.resources.Bundle"); // NOI18N

    /** Owned factory */
    SpecificationFactory factory;

    /** Metadata adaptor */
    String adaptorClass;
    DatabaseMetaData dmdAdaptor;

    public static final String CREATE_TABLE = "CreateTableCommand";
    public static final String RENAME_TABLE = "RenameTableCommand";
    public static final String DROP_TABLE = "DropTableCommand";
    public static final String COMMENT_TABLE = "CommentTableCommand";
    public static final String ADD_COLUMN = "AddColumnCommand";
    public static final String MODIFY_COLUMN = "ModifyColumnCommand";
    public static final String RENAME_COLUMN = "RenameColumnCommand";
    public static final String REMOVE_COLUMN = "RemoveColumnCommand";
    public static final String CREATE_INDEX = "CreateIndexCommand";
    public static final String DROP_INDEX = "DropIndexCommand";
    public static final String ADD_CONSTRAINT = "AddConstraintCommand";
    public static final String DROP_CONSTRAINT = "DropConstraintCommand";
    public static final String CREATE_VIEW = "CreateViewCommand";
    public static final String RENAME_VIEW = "RenameViewCommand";
    public static final String COMMENT_VIEW = "CommentViewCommand";
    public static final String DROP_VIEW = "DropViewCommand";
    public static final String CREATE_PROCEDURE = "CreateProcedureCommand";
    public static final String DROP_PROCEDURE = "DropProcedureCommand";
    public static final String CREATE_FUNCTION = "CreateFunctionCommand";
    public static final String DROP_FUNCTION = "DropFunctionCommand";
    public static final String CREATE_TRIGGER = "CreateTriggerCommand";
    public static final String DROP_TRIGGER = "DropTriggerCommand";

    /** Constructor */
    public Specification(HashMap description)
    {
        desc = description;
    }

    /** Constructor */
    public Specification(HashMap description, Connection c)
    {
        desc = description;
        jdbccon = c;
    }

    /** Returns all database properties */
    public Map getProperties()
    {
        return (Map)desc;
    }

    /** Returns command description */
    public Map getCommandProperties(String command)
    {
        return (Map)desc.get(command);
    }

    /** Returns used connection */
    public DBConnection getConnection()
    {
        return (DBConnection)desc.get("connection"); // NOI18N
    }

    public DatabaseSpecificationFactory getSpecificationFactory()
    {
        return factory;
    }

    public void setSpecificationFactory(DatabaseSpecificationFactory fac)
    {
        factory = (SpecificationFactory)fac;
    }

    public String getMetaDataAdaptorClassName()
    {
        if (adaptorClass == null || adaptorClass.length() == 0) {
            adaptorClass = "org.netbeans.lib.ddl.adaptors.DefaultAdaptor"; // NOI18N
        }

        return adaptorClass;
    }

    public void setMetaDataAdaptorClassName(String name)
    {
        if (name.startsWith("Database.Adaptors.")) // NOI18N
            adaptorClass = name;
        else
            adaptorClass = "Database.Adaptors."+name; // NOI18N
        //		System.out.println("Metadata adaptor class set = "+adaptorClass);
        dmdAdaptor = null;
    }

    /** Returns database metadata */
    public DatabaseMetaData getMetaData() throws SQLException
    {
        try {

            if (dmdAdaptor == null) {
                if (jdbccon != null) {
                    String adc = getMetaDataAdaptorClassName();
                    if (adc != null) {
                        ClassLoader loader;
                        try {
                            loader = TopManager.getDefault().currentClassLoader();
                        } catch (Exception ex) {
                            loader = null;
                        }

                        //						System.out.println("Metadata adaptor class name = "+adc);
                        dmdAdaptor = (DatabaseMetaData)Beans.instantiate(loader, adc);
                        if (dmdAdaptor instanceof DatabaseMetaDataAdaptor) {
                            ((DatabaseMetaDataAdaptor)dmdAdaptor).setConnection(jdbccon);
                        } else throw new ClassNotFoundException(bundle.getString("EXC_AdaptorInterface"));
                    } else throw new ClassNotFoundException(bundle.getString("EXC_AdaptorUnspecClass"));
                }
            }

            return dmdAdaptor;

        } catch (Exception ex) {
            ex.printStackTrace();
            throw new SQLException(ex.getMessage());
        }
    }

    /** Opens JDBC Connection.
    * This method usually calls command when it need to process something. 
    * But you can call it explicitly and leave connection open until last
    * command gets executed. Don't forget to close it.
    */
    public Connection openJDBCConnection()
    throws DDLException
    {
        if (jdbccon != null) throw new DDLException(bundle.getString("EXC_ConnOpen"));
        DBConnection dbcon = getConnection();
        if (dbcon == null) throw new DDLException(bundle.getString("EXC_ConnNot"));
        try {
            jdbccon = dbcon.createJDBCConnection();
        } catch (Exception e) {
            throw new DDLException(bundle.getString("EXC_ConnNot"));
        }

        return jdbccon;
    }

    /** Returns JDBC connection.
    * Commands must test if the connection is not open yet; if you simply call
    * openJDBCConnection without test (and the connection will be open by user),
    * a DDLException throws. This is a self-checking mechanism; you must always
    * close used connection.
    */ 
    public Connection getJDBCConnection()
    {
        return jdbccon;
    }

    public void closeJDBCConnection()
    throws DDLException
    {
        if (jdbccon == null) throw new DDLException(bundle.getString("EXC_ConnNot"));
        try {
            jdbccon.close();
            jdbccon = null;
        } catch (SQLException e) {
            throw new DDLException(bundle.getString("EXC_ConnUnableClose"));
        }
    }

    /** Creates command identified by commandName. Command names will include
    * create/rename/drop table/view/index/column and comment table/column. It 
    * returns null if command specified by commandName was not found. Used 
    * system allows developers to extend db-specification files and simply 
    * address new commands (everybody can implement createXXXCommand()).
    */
    public DDLCommand createCommand(String commandName)
    throws CommandNotSupportedException
    {
        return createCommand(commandName, null);
    }

    /** Creates command identified by commandName on table tableName.
    * Returns null if command specified by commandName was not found. It does not
    * check tableName existency; it simply waits for relevant execute() command
    * which fires SQLException.
    */	
    public DDLCommand createCommand(String commandName, String tableName)
    throws CommandNotSupportedException
    {
        String classname;
        Class cmdclass;
        AbstractCommand cmd;
        HashMap cprops = (HashMap)desc.get(commandName);
        if (cprops != null) classname = (String)cprops.get("Class"); // NOI18N
        //else throw new CommandNotSupportedException(commandName, "command "+commandName+" is not supported by system");
        else throw new CommandNotSupportedException(commandName,
            MessageFormat.format(bundle.getString("EXC_CommandNotSupported"), new String[] {commandName})); // NOI18N
        try {
            cmdclass = Class.forName(classname);
            cmd = (AbstractCommand)cmdclass.newInstance();
        } catch (Exception e) {
            throw new CommandNotSupportedException(commandName,
                MessageFormat.format(bundle.getString("EXC_UnableFindOrInitCommand"), new String[] {classname, commandName, e.getMessage()})); // NOI18N
        }

        cmd.setObjectName(tableName);
        cmd.setSpecification(this);
        cmd.setFormat((String)cprops.get("Format")); // NOI18N
        return cmd;
    }

    /** Create table command
    * @param tableName Name of the table
    */
    public CreateTable createCommandCreateTable(String tableName)
    throws CommandNotSupportedException
    {
        return (CreateTable)createCommand(CREATE_TABLE, tableName);
    }

    /** Comment table command
    * @param tableName Name of the table
    * @param comment New comment
    */
    public CommentTable createCommandCommentTable(String tableName, String comment)
    throws CommandNotSupportedException
    {
        CommentTable cmd = (CommentTable)createCommand(COMMENT_TABLE, tableName);
        cmd.setComment(comment);
        return cmd;
    }

    /** Drop table command
    * @param tableName Name of the table
    */
    public AbstractCommand createCommandDropTable(String tableName)
    throws CommandNotSupportedException
    {
        return (AbstractCommand)createCommand(DROP_TABLE, tableName);
    }

    /** Drop table command
    * @param tableName Name of the table
    */
    public RenameTable createCommandRenameTable(String tableName, String newName)
    throws CommandNotSupportedException
    {
        RenameTable cmd = (RenameTable)createCommand(RENAME_TABLE, tableName);
        cmd.setNewName(newName);
        return cmd;
    }

    /** Add column */
    public AddColumn createCommandAddColumn(String tableName)
    throws CommandNotSupportedException
    {
        return (AddColumn)createCommand(ADD_COLUMN, tableName);
    }

    /** Modify column */
    public ModifyColumn createCommandModifyColumn(String tableName)
    throws CommandNotSupportedException
    {
        ModifyColumn cmd = (ModifyColumn)createCommand(MODIFY_COLUMN, tableName);
        return cmd;
    }

    /** Rename column */
    public RenameColumn createCommandRenameColumn(String tableName)
    throws CommandNotSupportedException
    {
        RenameColumn cmd = (RenameColumn)createCommand(RENAME_COLUMN, tableName);
        return cmd;
    }

    /** Remove column
    * @param tableName Name of the table
    */
    public RemoveColumn createCommandRemoveColumn(String tableName)
    throws CommandNotSupportedException
    {
        RemoveColumn rcol = (RemoveColumn)createCommand(REMOVE_COLUMN, tableName);
        return rcol;
    }

    /** Create index
    * @param indexName Name of index
    * @param tableName Name of the table
    */
    public CreateIndex createCommandCreateIndex(String tableName)
    throws CommandNotSupportedException
    {
        CreateIndex cicmd = (CreateIndex)createCommand(CREATE_INDEX, tableName);
        return cicmd;
    }

    /** Drop index
    * @param indexName Name of index
    */
    public AbstractCommand createCommandDropIndex(String indexName)
    throws CommandNotSupportedException
    {
        return (AbstractCommand)createCommand(DROP_INDEX, indexName);
    }

    /** Create view
    * @param viewname Name of index
    */
    public CreateView createCommandCreateView(String viewname)
    throws CommandNotSupportedException
    {
        return (CreateView)createCommand(CREATE_VIEW, viewname);
    }

    /** Drop table command
    * @param tableName Name of the table
    */
    public RenameView createCommandRenameView(String tableName, String newName)
    throws CommandNotSupportedException
    {
        RenameView cmd = (RenameView)createCommand(RENAME_VIEW, tableName);
        cmd.setNewName(newName);
        return cmd;
    }

    /** Comment view command
    * @param tableName Name of the view
    * @param comment New comment
    */
    public CommentView createCommandCommentView(String viewName, String comment)
    throws CommandNotSupportedException
    {
        CommentView cmd = (CommentView)createCommand(COMMENT_VIEW, viewName);
        cmd.setComment(comment);
        return cmd;
    }

    /** Drop view
    * @param viewname Name of index
    */
    public AbstractCommand createCommandDropView(String viewname)
    throws CommandNotSupportedException
    {
        return (AbstractCommand)createCommand(DROP_VIEW, viewname);
    }

    /** Create procedure
    * @param viewname Name of procedure
    */
    public CreateProcedure createCommandCreateProcedure(String name)
    throws CommandNotSupportedException
    {
        return (CreateProcedure)createCommand(CREATE_PROCEDURE, name);
    }

    /** Drop procedure
    * @param viewname Name of procedure
    */
    public AbstractCommand createCommandDropProcedure(String name)
    throws CommandNotSupportedException
    {
        return (AbstractCommand)createCommand(DROP_PROCEDURE, name);
    }

    /** Create function
    * @param viewname Name of function
    */
    public CreateFunction createCommandCreateFunction(String name)
    throws CommandNotSupportedException
    {
        return (CreateFunction)createCommand(CREATE_FUNCTION, name);
    }

    /** Drop function
    * @param viewname Name of function
    */
    public AbstractCommand createCommandDropFunction(String name)
    throws CommandNotSupportedException
    {
        return (AbstractCommand)createCommand(DROP_FUNCTION, name);
    }

    /** Create trigger
    * @param viewname Name of trigger
    */
    public CreateTrigger createCommandCreateTrigger(String name, String tablename, int timing)
    throws CommandNotSupportedException
    {
        CreateTrigger ctrig = (CreateTrigger)createCommand(CREATE_TRIGGER, name);
        ctrig.setTableName(tablename);
        ctrig.setTiming(timing);
        return ctrig;
    }

    /** Drop trigger
    * @param viewname Name of trigger
    */
    public AbstractCommand createCommandDropTrigger(String name)
    throws CommandNotSupportedException
    {
        return (AbstractCommand)createCommand(DROP_TRIGGER, name);
    }

    /** Returns type map */
    public Map getTypeMap()
    {
        return (Map)desc.get("TypeMap"); // NOI18N
    }

    /** Returns DBType where maps specified java type */
    public String getType(int type)
    {
        String typestr = "";
        Map typemap = getTypeMap();

        switch(type) {
        case java.sql.Types.ARRAY: typestr = "ARRAY"; break; // NOI18N
        case java.sql.Types.BIGINT: typestr = "BIGINT"; break; // NOI18N
        case java.sql.Types.BINARY: typestr = "BINARY"; break; // NOI18N
        case java.sql.Types.BIT: typestr = "BIT"; break; // NOI18N
        case java.sql.Types.BLOB: typestr = "BLOB"; break; // NOI18N
        case java.sql.Types.CHAR: typestr = "CHAR"; break; // NOI18N
        case java.sql.Types.CLOB: typestr = "CLOB"; break; // NOI18N
        case java.sql.Types.DATE: typestr = "DATE"; break; // NOI18N
        case java.sql.Types.DECIMAL: typestr = "DECIMAL"; break; // NOI18N
        case java.sql.Types.DISTINCT: typestr = "DISTINCT"; break; // NOI18N
        case java.sql.Types.DOUBLE: typestr = "DOUBLE"; break; // NOI18N
        case java.sql.Types.FLOAT: typestr = "FLOAT"; break; // NOI18N
        case java.sql.Types.INTEGER: typestr = "INTEGER"; break; // NOI18N
        case java.sql.Types.JAVA_OBJECT: typestr = "JAVA_OBJECT"; break; // NOI18N
        case java.sql.Types.LONGVARBINARY: typestr = "LONGVARBINARY"; break; // NOI18N
        case java.sql.Types.LONGVARCHAR: typestr = "LONGVARCHAR"; break; // NOI18N
        case java.sql.Types.NUMERIC: typestr = "NUMERIC"; break; // NOI18N
        case java.sql.Types.REAL: typestr = "REAL"; break; // NOI18N
        case java.sql.Types.REF: typestr = "REF"; break; // NOI18N
        case java.sql.Types.SMALLINT: typestr = "SMALLINT"; break; // NOI18N
        case java.sql.Types.TIME: typestr = "TIME"; break; // NOI18N
        case java.sql.Types.TIMESTAMP: typestr = "TIMESTAMP"; break; // NOI18N
        case java.sql.Types.TINYINT: typestr = "TINYINT"; break; // NOI18N
        case java.sql.Types.VARBINARY: typestr = "VARBINARY"; break; // NOI18N
        case java.sql.Types.VARCHAR: typestr = "VARCHAR"; break; // NOI18N
        }

        return (String)typemap.get("java.sql.Types."+typestr); // NOI18N
    }

    /** Returns DBType where maps specified java type */
    public static int getType(String type)
    {
        if (type.equals("java.sql.Types.ARRAY")) return java.sql.Types.ARRAY; // NOI18N
        if (type.equals("java.sql.Types.BIGINT")) return java.sql.Types.BIGINT; // NOI18N
        if (type.equals("java.sql.Types.BINARY")) return java.sql.Types.BINARY; // NOI18N
        if (type.equals("java.sql.Types.BIT")) return java.sql.Types.BIT; // NOI18N
        if (type.equals("java.sql.Types.BLOB")) return java.sql.Types.BLOB; // NOI18N
        if (type.equals("java.sql.Types.CHAR")) return java.sql.Types.CHAR; // NOI18N
        if (type.equals("java.sql.Types.DATE")) return java.sql.Types.DATE; // NOI18N
        if (type.equals("java.sql.Types.DECIMAL")) return java.sql.Types.DECIMAL; // NOI18N
        if (type.equals("java.sql.Types.DISTINCT")) return java.sql.Types.DISTINCT; // NOI18N
        if (type.equals("java.sql.Types.DOUBLE")) return java.sql.Types.DOUBLE; // NOI18N
        if (type.equals("java.sql.Types.FLOAT")) return java.sql.Types.FLOAT; // NOI18N
        if (type.equals("java.sql.Types.INTEGER")) return java.sql.Types.INTEGER; // NOI18N
        if (type.equals("java.sql.Types.JAVA_OBJECT")) return java.sql.Types.JAVA_OBJECT; // NOI18N
        if (type.equals("java.sql.Types.LONGVARBINARY")) return java.sql.Types.LONGVARBINARY; // NOI18N
        if (type.equals("java.sql.Types.LONGVARCHAR")) return java.sql.Types.LONGVARCHAR; // NOI18N
        if (type.equals("java.sql.Types.NUMERIC")) return java.sql.Types.NUMERIC; // NOI18N
        if (type.equals("java.sql.Types.REAL")) return java.sql.Types.REAL; // NOI18N
        if (type.equals("java.sql.Types.REF")) return java.sql.Types.REF; // NOI18N
        if (type.equals("java.sql.Types.SMALLINT")) return java.sql.Types.SMALLINT; // NOI18N
        if (type.equals("java.sql.Types.TIME")) return java.sql.Types.TIME; // NOI18N
        if (type.equals("java.sql.Types.TIMESTAMP")) return java.sql.Types.TIMESTAMP; // NOI18N
        if (type.equals("java.sql.Types.TINYINT")) return java.sql.Types.TINYINT; // NOI18N
        if (type.equals("java.sql.Types.VARBINARY")) return java.sql.Types.VARBINARY; // NOI18N
        if (type.equals("java.sql.Types.VARCHAR")) return java.sql.Types.VARCHAR; // NOI18N

        return -1;
    }
}

/*
* <<Log>>
*  10   Gandalf   1.9         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  9    Gandalf   1.8         10/12/99 Radko Najman    debug messages removed
*  8    Gandalf   1.7         9/27/99  Slavek Psenicka setMetaDataAdaptorClassName
*        changed
*  7    Gandalf   1.6         9/13/99  Slavek Psenicka 
*  6    Gandalf   1.5         9/10/99  Slavek Psenicka 
*  5    Gandalf   1.4         5/14/99  Slavek Psenicka new version
*  4    Gandalf   1.3         4/23/99  Slavek Psenicka Chyba v createSpec pri 
*       ConnectAs
*  3    Gandalf   1.2         4/23/99  Slavek Psenicka Opravy v souvislosti se 
*       spravnym throwovanim :) CommandNotImplementedException
*  2    Gandalf   1.1         4/23/99  Slavek Psenicka new version
*  1    Gandalf   1.0         4/6/99   Slavek Psenicka 
* $
*/
