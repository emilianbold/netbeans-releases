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
import org.netbeans.lib.ddl.*;

/**
* Interface of column-based operations. This interface should implement all commands
* opearting with one column, eg. add and drop column etc. This is not interface for
* multi-column operations (eg. create table).
*
* @author Slavek Psenicka
*/
public interface ColumnOperationCommand extends DDLCommand, OwnedObjectCommand {

    /** Returns column specification array */
    public TableColumnDescriptor getColumn();

    /** Sets column specification array
    * @param col New column.
    */
    public void setColumn(TableColumnDescriptor col);
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

