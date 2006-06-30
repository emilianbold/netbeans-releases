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

import org.netbeans.lib.ddl.Argument;
import org.netbeans.lib.ddl.DatabaseSpecification;
import org.netbeans.lib.ddl.DDLException;
import org.netbeans.lib.ddl.util.CommandFormatter;

/**
* Argument of procedure. Encapsulates name, type (in/out) and datatype.
*/
public class ProcedureArgument implements Argument {
    /** Argument name */
    private String name;

    /** Argument type */
    private int type;

    /** Argument datatype */
    private int dtype;

    /** Format */
    private String format;

    /** Additional properties */
    private Map addprops;

    public static String getArgumentTypeName(int type)
    {
        String typename = null;
        switch (type) {
        case java.sql.DatabaseMetaData.procedureColumnIn: typename = "IN"; break; // NOI18N
        case java.sql.DatabaseMetaData.procedureColumnOut: typename = "OUT"; break; // NOI18N
        case java.sql.DatabaseMetaData.procedureColumnInOut: typename = "INOUT"; break; // NOI18N
        }

        return typename;
    }

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

    /** Returns general property */
    public Object getProperty(String pname)
    {
        return addprops.get(pname);
    }

    /** Sets general property */
    public void setProperty(String pname, Object pval)
    {
        if (addprops == null) addprops = new HashMap();
        addprops.put(pname, pval);
    }

    /** Describes type of argument: in, out, in/out or return value
    * of procedure. Particular values you can find in DatabaseMetadata;
    */
    public int getType()
    {
        return type;
    }

    /** Translates numeric representation of type into IN/OUT/INOUT strings.
    */
    public String getTypeName()
    {
        return getArgumentTypeName(type);
    }

    /** Sets type of argument */
    public void setType(int atype)
    {
        type = atype;
    }

    /** Returns datatype of argument */
    public int getDataType()
    {
        return dtype;
    }

    /** Sets datatype of argument */
    public void setDataType(int atype)
    {
        dtype = atype;
    }

    /**
    * Returns properties and it's values supported by this object.
    * argument.name		Name of argument
    * argument.type		Type of argument 
    * argument.datatype	Datatype of argument
    * Throws DDLException if object name is not specified.
    */
    public Map getColumnProperties(AbstractCommand cmd) throws DDLException {
        HashMap args = new HashMap();
        DatabaseSpecification spec = cmd.getSpecification();
        Map typemap = (Map)spec.getProperties().get("ProcedureArgumentMap"); // NOI18N
        String typename = (String)typemap.get(getArgumentTypeName(type));
        args.put("argument.name", cmd.quote(name)); // NOI18N
        args.put("argument.type", typename); // NOI18N
        args.put("argument.datatype", spec.getType(dtype)); // NOI18N
        return args;
    }

    /**
    * Returns full string representation of argument.
    */
    public String getCommand(CreateProcedure cmd)
    throws DDLException
    {
        Map cprops;
        if (format == null) throw new DDLException(NbBundle.getBundle("org.netbeans.lib.ddl.resources.Bundle").getString("EXC_NoFormatSpec")); // NOI18N
        try {
            cprops = getColumnProperties(cmd);
            return CommandFormatter.format(format, cprops);
        } catch (Exception e) {
            throw new DDLException(e.getMessage());
        }
    }
}
