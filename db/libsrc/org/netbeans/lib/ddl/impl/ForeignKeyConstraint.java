/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.lib.ddl.impl;

import java.util.*;
import java.sql.*;
import java.text.ParseException;
import org.netbeans.lib.ddl.*;

/**
* Implementation of ForeignKey constraint. 
*/
public class ForeignKeyConstraint extends AbstractTableColumn implements ForeignKeyConstraintDescriptor {
    /** Refernced table */
    String tname;

    /** Referenced column */
    String cname;

    static final long serialVersionUID =9183651896170854492L;
    /** Returns name of Referenced table */
    public String getReferencedTableName()
    {
        return tname;
    }

    /** Sets name of Referenced table */
    public void setReferencedTableName(String name)
    {
        tname = name;
    }

    /** Returns name of Referenced column */
    public String getReferencedColumnName()
    {
        return cname;
    }

    /** Sets name of Referenced column */
    public void setReferencedColumnName(String name)
    {
        cname = name;
    }

    /**
    * Returns properties and it's values supported by this object.
    * object.name	Name of the object; use setObjectName() 
    * object.owner	Name of the object; use setObjectOwner() 
    * fkobject.name	Specification of foreign table 
    * fkcolumn.name	Specification of foreign column 
    */
    public Map getColumnProperties(AbstractCommand cmd) throws DDLException {
        Map args = super.getColumnProperties(cmd);
        args.put("fkobject.name", cmd.quote(tname)); // NOI18N
        args.put("fkcolumn.name", cmd.quote(cname)); // NOI18N
        
        return args;
    }
}
