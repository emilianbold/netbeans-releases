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

package org.netbeans.lib.ddl;

import java.sql.*;
import org.netbeans.lib.ddl.*;

/**
* Interface of table constraint. This interface should implement primary key 
* constraint and unique constraint.
* @author Slavek Psenicka
*/
public interface TableConstraintDescriptor
{
    /** Returns name of constraint */
    public String getObjectName();
    /** Sets name of constraint */
    public void setObjectName(String cname);

    /** Returns name of column */
    public String getColumnName();
    /** Sets name of column */
    public void setColumnName(String columnName);
}

/*
* <<Log>>
*  4    Gandalf   1.3         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  3    Gandalf   1.2         5/14/99  Slavek Psenicka new version
*  2    Gandalf   1.1         4/23/99  Slavek Psenicka new version
*  1    Gandalf   1.0         4/6/99   Slavek Psenicka 
* $
*/
