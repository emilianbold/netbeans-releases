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

package org.netbeans.lib.ddl.impl;

import java.util.*;
import org.netbeans.lib.ddl.*;
import org.netbeans.lib.ddl.impl.*;

/**
* Rename table command. Encapsulates name and new name of table.
*
* @author Slavek Psenicka
*/

public class RenameColumn extends ColumnCommand
{
    /** New name */
    private String newname;

    static final long serialVersionUID =7150074600789999024L;
    /** Remove simple column
    * @param name Column name
    */
    public TableColumn renameColumn(String name, String nname)
    throws ClassNotFoundException, IllegalAccessException, InstantiationException
    {
        newname = nname;
        return specifyColumn(TableColumn.COLUMN, name, 
            Specification.RENAME_COLUMN, false, false);
    }

    /** Returns properties of command:
    * object.newname	New name of table
    */
    public Map getCommandProperties()
    throws DDLException
    {
        Map args = super.getCommandProperties();
        args.put("column.newname", newname); // NOI18N
        return args;
    }
}

/*
* <<Log>>
*  3    Gandalf   1.2         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  2    Gandalf   1.1         8/17/99  Ian Formanek    Generated serial version 
*       UID
*  1    Gandalf   1.0         4/23/99  Slavek Psenicka 
* $
*/
