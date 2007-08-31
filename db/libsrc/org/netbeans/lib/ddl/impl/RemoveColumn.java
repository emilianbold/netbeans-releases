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
* Remove column command.
*
* @author Slavek Psenicka
*/

public class RemoveColumn extends ColumnCommand
{
    static final long serialVersionUID =2845249943586553892L;
    /** Remove simple column
    * @param name Column name
    */
    public TableColumn removeColumn(String name)
    throws ClassNotFoundException, IllegalAccessException, InstantiationException
    {
        return specifyColumn(TableColumn.COLUMN, name, 
            Specification.REMOVE_COLUMN, false, false);
    }

    /** Remove unique column
    * @param name Column name
    */
    public TableColumn removeUniqueColumn(String name)
    throws ClassNotFoundException, IllegalAccessException, InstantiationException
    {
        TableColumn col = specifyColumn(TableColumn.UNIQUE, name, 
            Specification.REMOVE_COLUMN, false, false);
        return col;
    }

    /** Remove primary key
    * @param name Column name
    */
    public TableColumn removePrimaryKeyColumn(String name)
    throws ClassNotFoundException, IllegalAccessException, InstantiationException
    {
        TableColumn col = specifyColumn(TableColumn.PRIMARY_KEY, name, 
            Specification.REMOVE_COLUMN, false, false);
        return col;
    }

    /** Remove foreign key
    * @param name Column name
    */
    public TableColumn removeForeignKeyColumn(String name)
    throws ClassNotFoundException, IllegalAccessException, InstantiationException
    {
        TableColumn col = specifyColumn(TableColumn.FOREIGN_KEY, name, 
            Specification.REMOVE_COLUMN, false, false);
        return col;
    }

    /** Remove check
    * @param name Check name
    */
    public TableColumn removeCheckColumn(String name)
    throws ClassNotFoundException, IllegalAccessException, InstantiationException
    {
        TableColumn col = specifyColumn(TableColumn.CHECK, name, 
            Specification.REMOVE_COLUMN, false, false);
        return col;
    }
}

/*
* <<Log>>
*  4    Gandalf   1.3         11/27/99 Patrik Knakal   
*  3    Gandalf   1.2         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  2    Gandalf   1.1         4/23/99  Slavek Psenicka new version
*  1    Gandalf   1.0         4/6/99   Slavek Psenicka 
* $
*/
