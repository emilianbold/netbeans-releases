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

public class DropIndex extends AbstractCommand {
    /** Table name */
    private String tablename;
    
    static final long serialVersionUID =-3890617111076632936L;
    
    public String getTableName()
    {
        return tablename;
    }

    public void setTableName(String tname)
    {
        tablename = tname;
    }


    public Map getCommandProperties() throws DDLException {
        Map args = super.getCommandProperties();
        args.put("table.name", quote(tablename)); // NOI18N
        
        return args;
    }
}
