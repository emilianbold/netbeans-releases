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

import java.text.ParseException;
import java.text.MessageFormat;

import org.openide.util.NbBundle;
import java.util.*;
import org.netbeans.lib.ddl.*;

/**
* Instances of this command operates with column list (e.g. create column).
* To process command with one column use ColumnCommand.
*
* @author Slavek Psenicka
*/

public class ColumnListCommand extends AbstractCommand
{
    /** Used columns */
    private Vector columns;

    private static ResourceBundle bundle = NbBundle.getBundle("org.netbeans.lib.ddl.resources.Bundle"); // NOI18N

    static final long serialVersionUID =3646663278680222131L;
    /** Constructor */
    public ColumnListCommand()
    {
        columns = new Vector();
    }

    /** Returns list of columns */
    public Vector getColumns()
    {
        return columns;
    }

    /** Creates specification of command
    * @param type Type of column
    * @param name Name of column
    * @param cmd Command
    */	
    public TableColumn specifyColumn(String type, String name, String cmd)
    throws ClassNotFoundException, IllegalAccessException, InstantiationException
    {
        TableColumn column;
        Map gprops = (Map)getSpecification().getProperties();
        Map props = (Map)getSpecification().getCommandProperties(cmd);
        Map bindmap = (Map)props.get("Binding"); // NOI18N
        String tname = (String)bindmap.get(type);
        if (tname != null) {
            Map typemap = (Map)gprops.get(tname);
            if (typemap != null) {
                Class typeclass = Class.forName((String)typemap.get("Class")); // NOI18N
                String format = (String)typemap.get("Format"); // NOI18N
                column = (TableColumn)typeclass.newInstance();
                column.setObjectName(name);
                column.setObjectType(type);
                column.setColumnName(name);
                column.setFormat(format);
                columns.add(column);
            } else throw new InstantiationException(
                    MessageFormat.format(
                        bundle.getString("EXC_UnableLocateType"), // NOI18N
                        new String[] {tname, props.keySet().toString() } ));
        } else throw new InstantiationException(
                    MessageFormat.format(
                        bundle.getString("EXC_UnableToBind"), // NOI18N
                        new String[] {type, bindmap.toString() } ));
        return column;
    }

    /**
    * Returns properties and it's values supported by this object.
    * columns	Specification of columns served by this object
    */
    public Map getCommandProperties()
    throws DDLException
    {
        Map props = (Map)getSpecification().getProperties();
        String cols = (String)props.get("ColumnListHeader"); // NOI18N
        String coldelim = (String)props.get("ColumnListDelimiter"); // NOI18N
        Map args = super.getCommandProperties();

        // Construct string

        Enumeration col_e = columns.elements();
        while (col_e.hasMoreElements()) {
            AbstractTableColumn col = (AbstractTableColumn)col_e.nextElement();
            boolean inscomma = col_e.hasMoreElements();
            cols = cols + col.getCommand(this)+(inscomma ? coldelim : "");
        }

        args.put("columns", cols); // NOI18N
        return args;
    }

    /** Reads object from stream */
    public void readObject(java.io.ObjectInputStream in)
    throws java.io.IOException, ClassNotFoundException
    {
        super.readObject(in);
        columns = (Vector)in.readObject();
    }

    /** Writes object to stream */
    public void writeObject(java.io.ObjectOutputStream out)
    throws java.io.IOException
    {
        super.writeObject(out);
        out.writeObject(columns);
    }
}

/*
* <<Log>>
*  5    Gandalf   1.4         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  4    Gandalf   1.3         8/17/99  Ian Formanek    Generated serial version 
*       UID
*  3    Gandalf   1.2         5/14/99  Slavek Psenicka new version
*  2    Gandalf   1.1         4/23/99  Slavek Psenicka new version
*  1    Gandalf   1.0         4/6/99   Slavek Psenicka 
* $
*/
