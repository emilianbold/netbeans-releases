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

package org.netbeans.lib.ddl;

import java.sql.*;
import java.util.Set;
import org.netbeans.lib.ddl.*;

/**
* The factory interface used for creating instances of DatabaseSpecification class. 
* DatabaseSpecificationFactory collects information about available database 
* description files. Then it's able to specify if system can control 
* the database (specified by product name or live connection). It also
* provides a list of supported databases.
*
* @author Slavek Psenicka
*/
public interface DatabaseSpecificationFactory {

    /** Returns array of database products supported by system. It returns
    * string array only, not the DatabaseSpecification array.
    */
    public Set supportedDatabases();

    /** Returns true if database (specified by databaseProductName) is
    * supported by system. Does not throw exception if it doesn't.
    * @param databaseProductName Database product name as given from DatabaseMetaData
    * @return True if database product is supported.
    */	
    public boolean isDatabaseSupported(String databaseProductName);

    /** Creates instance of DatabaseSpecification class; a database-specification
    * class. This object knows about used database and can be used as
    * factory for db-manipulating commands. It connects to the database 
    * and reads database metadata. Throws DBException if database
    * (obtained from database metadata) is not supported.
    * @param connection Database connection used to obtain database product name
    * directly from the database.
    * @return Specification object.
    */
    public DatabaseSpecification createSpecification(DBConnection connection, Connection c)
    throws DatabaseProductNotFoundException, DDLException;

    /** Creates instance of DatabaseSpecification class; a database-specification
    * class. This object knows about used database and can be used as
    * factory for db-manipulating commands. It connects to database and
    * reads metadata as createSpecification(DBConnection connection), but always
    * uses specified databaseProductName. This is not recommended technique.
    * @param connection Database connection (is NOT used to obtain database product name)
    * @return Specification object.
    */
    public DatabaseSpecification createSpecification(DBConnection connection, String databaseProductName, Connection c) throws DatabaseProductNotFoundException;

    public DatabaseSpecification createSpecification(Connection c)
    throws DatabaseProductNotFoundException, SQLException;

    /** Returns debug-mode flag
    */
    public boolean isDebugMode();

    /** Sets debug-mode flag
    */
    public void setDebugMode(boolean mode);
}

/*
* <<Log>>
*/
