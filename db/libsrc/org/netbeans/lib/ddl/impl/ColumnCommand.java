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

import java.text.MessageFormat;
import java.util.Map;

import org.openide.util.NbBundle;

import org.netbeans.lib.ddl.DDLException;

/**
* Instances of this command operates with one column.
*
* @author Slavek Psenicka
*/

public class ColumnCommand extends AbstractCommand
{
    /** Column */
    private TableColumn column;

    static final long serialVersionUID =-4554975764392047624L;
    /** Creates specification of command
    * @param type Type of column
    * @param name Name of column
    * @param cmd Command
    * @param newObject set to true if this column is for a new object (table)
    *   and set to false if this column is for an existing object (table)
    * @param newColumn set to true if this is a new column, false if this
    *   is an existing column.
    */	
    public TableColumn specifyColumn(String type, String name, String cmd,
        boolean newObject, boolean newColumn)
    throws ClassNotFoundException, IllegalAccessException, InstantiationException
    {
        Map gprops = (Map)getSpecification().getProperties();
        Map props = (Map)getSpecification().getCommandProperties(cmd);
        Map bindmap = (Map)props.get("Binding"); // NOI18N
        String tname = (String)bindmap.get(type);
        if (tname != null) {
            Map typemap = (Map)gprops.get(tname);
            if (typemap == null) throw new InstantiationException(
                                                MessageFormat.format(NbBundle.getBundle("org.netbeans.lib.ddl.resources.Bundle").getString("EXC_UnableLocateObject"), // NOI18N
                                                    new String[] {tname}));
            Class typeclass = Class.forName((String)typemap.get("Class")); // NOI18N
            String format = (String)typemap.get("Format"); // NOI18N
            column = (TableColumn)typeclass.newInstance();
            column.setObjectName(name);
            column.setObjectType(type);
            column.setColumnName(name);
            column.setFormat(format);
            column.setNewObject(newObject);
            column.setNewColumn(newColumn);
        } else throw new InstantiationException(MessageFormat.format(NbBundle.getBundle("org.netbeans.lib.ddl.resources.Bundle").getString("EXC_UnableLocateType"), // NOI18N
                                                    new String[] {type, bindmap.toString() }));

        return column;
    }

    public TableColumn getColumn()
    {
        return column;
    }

    public void setColumn(TableColumn col)
    {
        column = col;
    }

    /**
    * Returns properties and it's values supported by this object.
    * column	Specification of the column 
    */
    public Map getCommandProperties()
    throws DDLException
    {
        Map args = super.getCommandProperties();
        args.put("column", column.getCommand(this)); // NOI18N
        return args;
    }
}
