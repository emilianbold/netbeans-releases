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
import java.io.Serializable;
import java.text.ParseException;
import org.openide.*;
import org.netbeans.lib.ddl.*;
import org.netbeans.lib.ddl.util.*;
import org.openide.windows.OutputWriter;

/**
* Basic implementation of DDLCommand. This class can be used for really simple
* commands with format and without arguments. Heavilly subclassed.
*
* @author Slavek Psenicka
*/
public class AbstractCommand
            implements Serializable, DDLCommand
{
    /** Command owner */
    private DatabaseSpecification spec;

    /** Command format */
    private String format;

    /** Object owner and name */
    private String owner, name;

    /** Additional properties */
    private Map addprops;

    static final long serialVersionUID =-560515030304320086L;
    /** Returns specification (DatabaseSpecification) for this command */
    public DatabaseSpecification getSpecification()
    {
        return spec;
    }

    /**
    * Sets specification (DatabaseSpecification) for this command. This method is usually called 
    * in relevant createXXX method.
    * @param specification New specification object.
    */
    public void setSpecification(DatabaseSpecification specification)
    {
        spec = specification;
    }

    /**
    * Sets format for this command. This method is usually called in relevant createXXX
    * method.
    * @param fmt New format.
    */
    public void setFormat(String fmt)
    {
        format = fmt;
    }

    /** Returns name of modified object */
    public String getObjectName()
    {
        return name;
    }

    /** Sets name to be used in command
    * @param nam New name.
    */
    public void setObjectName(String nam)
    {
        name = nam;
    }

    /** Returns name of modified object */
    public String getObjectOwner()
    {
        return owner;
    }

    /** Sets name to be used in command
    * @param objectowner New owner.
    */
    public void setObjectOwner(String objectowner)
    {
        owner = objectowner;
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

    /**
    * Returns properties and it's values supported by this object.
    * object.name	Name of the object; use setObjectName() 
    * object.owner	Name of the object; use setObjectOwner() 
    * Throws DDLException if object name is not specified.
    */
    public Map getCommandProperties()
    throws DDLException
    {
        HashMap args = new HashMap();
        if (addprops != null) args.putAll(addprops);
        String oname = getObjectName();
        if (oname != null) args.put("object.name", getObjectName());
        else throw new DDLException("unknown object name");
        args.put("object.owner", getObjectOwner());
        return args;
    }

    /**
    * Executes command.
    * First it calls getCommand() to obtain command text. Then tries to open JDBC
    * connection and execute it. If connection is already open, uses it and leave
    * open; otherwise creates new one and closes after use. Throws DDLException if
    * something wrong occurs.
    */
    public void execute()
    throws DDLException
    {
        String fcmd;
        Connection fcon = null;
        boolean opened = false;

        try {
            fcmd = getCommand();
        } catch (Exception e) {
            //			e.printStackTrace();
            throw new DDLException("Unable to format a command:\n" + format + "\n" + e.getMessage());
        }

        //		System.out.println(fcmd);
        // In case of debug mode, you simply print command and don't execute
        if (spec.getSpecificationFactory().isDebugMode()) {

            try {
                OutputWriter ow = TopManager.getDefault().getStdOut();
                if (ow != null) ow.println(fcmd);
                else throw new Exception();

            } catch (Exception e) {
                //				e.printStackTrace();
                System.out.println(e);
                System.out.println(fcmd);
            }
        }

        try {
            fcon = spec.getJDBCConnection();
            if (fcon == null) {
                fcon = spec.openJDBCConnection();
                opened = true;
            }

            Statement stat = fcon.createStatement();
            stat.execute(fcmd);
            stat.close();
        } catch (Exception e) {
            //			e.printStackTrace();
            if (opened && fcon != null)
                spec.closeJDBCConnection();
            throw new DDLException("Unable to execute a command:\n" + fcmd + "\n" + e.getMessage());
        }
        if (opened) spec.closeJDBCConnection();
    }

    /**
    * Returns full string representation of command. This string needs no 
    * formatting and could be used directly as argument of executeUpdate() 
    * command. Throws DDLException if format is not specified or CommandFormatter
    * can't format it (it uses MapFormat to process entire lines and can solve []
    * enclosed expressions as optional.
    */
    public String getCommand()
    throws DDLException
    {
        if (format == null)
            throw new DDLException("No format specified");
        try {
            Map props = getCommandProperties();
            return CommandFormatter.format(format, props);
        } catch (Exception e) {
            //			e.printStackTrace();
            throw new DDLException(e.getMessage());
        }
    }

    /** Reads object from stream */
    public void readObject(java.io.ObjectInputStream in)
    throws java.io.IOException, ClassNotFoundException
    {
        format = (String)in.readObject();
        owner = (String)in.readObject();
        name = (String)in.readObject();
        addprops = (Map)in.readObject();
    }

    /** Writes object to stream */
    public void writeObject(java.io.ObjectOutputStream out)
    throws java.io.IOException
    {
        //System.out.println("Writing command "+name);
        out.writeObject(format);
        out.writeObject(owner);
        out.writeObject(name);
        out.writeObject(addprops);
    }
}

/*
* <<Log>>
*  14   Gandalf   1.13        2/16/00  Radko Najman    
*  13   Gandalf   1.12        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  12   Gandalf   1.11        10/12/99 Radko Najman    debug messages removed
*  11   Gandalf   1.10        9/15/99  Slavek Psenicka 
*  10   Gandalf   1.9         9/13/99  Slavek Psenicka 
*  9    Gandalf   1.8         9/10/99  Slavek Psenicka 
*  8    Gandalf   1.7         8/17/99  Ian Formanek    Generated serial version 
*       UID
*  7    Gandalf   1.6         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  6    Gandalf   1.5         5/21/99  Slavek Psenicka System.out changed to 
*       Output window for debug prints.
*  5    Gandalf   1.4         5/14/99  Slavek Psenicka new version
*  4    Gandalf   1.3         4/23/99  Slavek Psenicka Chyba v createSpec pri 
*       ConnectAs
*  3    Gandalf   1.2         4/23/99  Slavek Psenicka Opravy v souvislosti se 
*       spravnym throwovanim :) CommandNotImplementedException
*  2    Gandalf   1.1         4/23/99  Slavek Psenicka new version
*  1    Gandalf   1.0         4/6/99   Slavek Psenicka 
* $
*/
