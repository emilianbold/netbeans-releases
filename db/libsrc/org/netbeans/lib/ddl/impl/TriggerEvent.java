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
import java.sql.*;
import org.openide.util.NbBundle;
import java.text.ParseException;
import org.netbeans.lib.ddl.*;
import org.netbeans.lib.ddl.util.*;

/**
* Describes trigger. Encapsulates name, timing (when it fires; when user INSERTs of 
* some data, after UPDATE or DELETE). In trigger descriptor this values should be 
* combined together. 
*
* @author Slavek Psenicka
*/
public class TriggerEvent
{
    public static final int INSERT = 1;
    public static final int UPDATE = 2;
    public static final int DELETE = 3;

    private static ResourceBundle bundle = NbBundle.getBundle("org.netbeans.lib.ddl.resources.Bundle"); // NOI18N
    
    /** Converts code into string representation */
    public static String getName(int code)
    {
        switch (code) {
        case INSERT: return "INSERT"; // NOI18N
        case UPDATE: return "UPDATE"; // NOI18N
        case DELETE: return "DELETE"; // NOI18N
        }

        return null;
    }

    /** Event */
    private String name;

    /** Column */
    private String col;

    /** Format */
    private String format;

    /** Returns name */
    public String getName()
    {
        return name;
    }

    /** Sets name */
    public void setName(String aname)
    {
        name = aname;
    }

    /** Returns name of column */
    public String getFormat()
    {
        return format;
    }

    /** Sets name of column */
    public void setFormat(String fmt)
    {
        format = fmt;
    }

    /** Returns name of column */
    public String getColumn()
    {
        return col;
    }

    /** Sets name of column */
    public void setColumn(String column)
    {
        col = column;
    }

    /**
    * Returns properties and it's values supported by this object.
    * event.name	Name of event 
    * event.column	Name of column 
    * Throws DDLException if object name is not specified.
    */
    public Map getColumnProperties(AbstractCommand cmd)
    throws DDLException
    {
        HashMap args = new HashMap();
        args.put("event.name", name); // NOI18N
        args.put("event.column", col); // NOI18N
        return args;
    }

    /** Returns string representation of event
    * @param cmd Command context
    */
    public String getCommand(AbstractCommand cmd)
    throws DDLException
    {
        Map cprops;
        if (format == null) throw new DDLException(bundle.getString("EXC_NoFormatSpec"));
        try {
            cprops = getColumnProperties(cmd);
            return CommandFormatter.format(format, cprops);
        } catch (Exception e) {
            throw new DDLException(e.getMessage());
        }
    }
}

/*
* <<Log>>
*  3    Gandalf   1.2         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  2    Gandalf   1.1         4/23/99  Slavek Psenicka new version
*  1    Gandalf   1.0         4/6/99   Slavek Psenicka 
* $
*/
