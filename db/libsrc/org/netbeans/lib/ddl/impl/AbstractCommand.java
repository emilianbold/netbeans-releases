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
import org.openide.util.NbBundle;
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

    /** Execution command with some exception */
    boolean executionWithException;

    /** Command format */
    private String format;

    /** Object owner and name */
    private String owner, name;

    /** Additional properties */
    private Map addprops;

    private static ResourceBundle bundle = NbBundle.getBundle("org.netbeans.lib.ddl.resources.Bundle"); // NOI18N
    
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
    public String getObjectOwner() {
        if (owner != null)
            if (owner.trim().equals(""))
                setObjectOwner(null);
        
        return owner;
    }

    /** Sets name to be used in command
    * @param objectowner New owner.
    */
    public void setObjectOwner(String objectowner) {
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
    public Map getCommandProperties() throws DDLException {
        HashMap args = new HashMap();
        if (addprops != null)
            args.putAll(addprops);
        String oname = getObjectName();
        if (oname != null)
            args.put("object.name", getObjectName()); // NOI18N
        else
            throw new DDLException(bundle.getString("EXC_Unknown")); // NOI18N
        args.put("object.owner", getObjectOwner()); // NOI18N
        
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
        executionWithException = false;

        try {
            fcmd = getCommand();
        } catch (Exception e) {
            //			e.printStackTrace();
            //throw new DDLException(bundle.getString("EXC_UnableToFormat")+"\n" + format + "\n" + e.getMessage()); // NOI18N
            executionWithException = true;
            TopManager.getDefault().notify(
                new NotifyDescriptor.Message(bundle.getString("EXC_UnableToFormat")+"\n" + format + "\n" + e.getMessage(),
                NotifyDescriptor.ERROR_MESSAGE));
            return;
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
            executionWithException = true;
            if (opened && fcon != null)
                spec.closeJDBCConnection();
            //throw new DDLException(bundle.getString("EXC_UnableToExecute")+"\n" + fcmd + "\n" + e.getMessage()); // NOI18N
            TopManager.getDefault().notify(new NotifyDescriptor.Message(bundle.getString("EXC_UnableToExecute")+"\n" + fcmd + "\n" + e.getMessage(), NotifyDescriptor.ERROR_MESSAGE));
            return;
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
            throw new DDLException(bundle.getString("EXC_NoFormatSpec")); // NOI18N
        try {
            Map props = getCommandProperties();
            return CommandFormatter.format(format, props);
        } catch (Exception e) {
            //			e.printStackTrace();
            throw new DDLException(e.getMessage());
        }
    }

    /** information about appearance some exception in the last execute a bunch of commands */
    public boolean wasException() {
        return executionWithException;
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
