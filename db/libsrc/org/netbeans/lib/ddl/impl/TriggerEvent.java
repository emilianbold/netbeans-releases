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

import java.util.HashMap;
import java.util.Map;

import org.openide.util.NbBundle;

import org.netbeans.lib.ddl.DDLException;
import org.netbeans.lib.ddl.util.CommandFormatter;

/**
* Describes trigger. Encapsulates name, timing (when it fires; when user INSERTs of
* some data, after UPDATE or DELETE). In trigger descriptor this values should be
* combined together.
*/
public class TriggerEvent {
    public static final int INSERT = 1;
    public static final int UPDATE = 2;
    public static final int DELETE = 3;

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
    public Map getColumnProperties(AbstractCommand cmd) throws DDLException {
        HashMap args = new HashMap();
        args.put("event.name", cmd.quote(name)); // NOI18N
        args.put("event.column", cmd.quote(col)); // NOI18N
        
        return args;
    }

    /** Returns string representation of event
    * @param cmd Command context
    */
    public String getCommand(AbstractCommand cmd)
    throws DDLException
    {
        Map cprops;
        if (format == null) throw new DDLException(NbBundle.getBundle("org.netbeans.lib.ddl.resources.Bundle").getString("EXC_NoFormatSpec")); //NOI18N
        try {
            cprops = getColumnProperties(cmd);
            return CommandFormatter.format(format, cprops);
        } catch (Exception e) {
            throw new DDLException(e.getMessage());
        }
    }
}
