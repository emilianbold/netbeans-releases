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

import java.util.*;
import org.netbeans.lib.ddl.*;
import org.netbeans.lib.ddl.impl.*;

/**
* Interface of database action command. Instances should remember connection 
* information of DatabaseSpecification and use it in execute() method. This is a base interface
* used heavily for sub-interfacing (it is not subclassing :)
*/

public class CreateIndex extends ColumnListCommand {
    /** Index name */
    private String tablename;
    
    /** Index type */
    private String unique;
    
    static final long serialVersionUID =1899024699690380782L;
    public String getIndexName()
    {
        return tablename;
    }

    public void setIndexName(String tname)
    {
        tablename = tname;
    }

    public String getIndexType()
    {
        return unique;
    }

    public void setIndexType(String idx_type)
    {
        unique = idx_type;
    }

    public TableColumn specifyColumn(String name)
    throws ClassNotFoundException, IllegalAccessException, InstantiationException
    {
        return specifyColumn(TableColumn.COLUMN, name, Specification.CREATE_INDEX);
    }

    public Map getCommandProperties() throws DDLException {
        Map args = super.getCommandProperties();
        args.put("index.name", quote(tablename)); // NOI18N
        args.put("index.unique", unique); // NOI18N
        
        return args;
    }
}
