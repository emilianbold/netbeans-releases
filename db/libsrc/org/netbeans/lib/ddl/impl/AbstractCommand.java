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

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.windows.IOProvider;
import org.openide.windows.OutputWriter;

import org.netbeans.lib.ddl.DatabaseSpecification;
import org.netbeans.lib.ddl.DDLCommand;
import org.netbeans.lib.ddl.DDLException;
import org.netbeans.lib.ddl.util.CommandFormatter;
import org.openide.windows.InputOutput;

/**
* Basic implementation of DDLCommand. This class can be used for really simple
* commands with format and without arguments. Heavilly subclassed.
*/
public class AbstractCommand implements Serializable, DDLCommand {
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

    static final long serialVersionUID =-560515030304320086L;

    private String quoteStr;
    
    /** Indicates if the object is being newly created or is an existing object */
    private boolean newObject = false;

    /** Returns specification (DatabaseSpecification) for this command */
    public DatabaseSpecification getSpecification() {
        return spec;
    }

    /**
    * Sets specification (DatabaseSpecification) for this command. This method is usually called
    * in relevant createXXX method.
    * @param specification New specification object.
    */
    public void setSpecification(DatabaseSpecification specification) {
        spec = specification;
    }

    /**
    * Sets format for this command. This method is usually called in relevant createXXX
    * method.
    * @param fmt New format.
    */
    public void setFormat(String fmt) {
        format = fmt;
    }

    /** Returns name of modified object */
    public String getObjectName() {
        return name;
    }

    /** Sets name to be used in command
    * @param nam New name.
    */
    public void setObjectName(String nam) {
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

    public boolean isNewObject() {
        return newObject;
    }

    public void setNewObject(boolean newObject) {
        this.newObject = newObject;
    }
    
    

    /** Returns general property */
    public Object getProperty(String pname) {
        return addprops.get(pname);
    }

    /** Sets general property */
    public void setProperty(String pname, Object pval) {
        if (addprops == null)
            addprops = new HashMap();
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
            args.put("object.name", 
                newObject ? getObjectName() : quote(getObjectName())); // NOI18N
        else
            throw new DDLException(NbBundle.getBundle("org.netbeans.lib.ddl.resources.Bundle").getString("EXC_Unknown")); // NOI18N
        args.put("object.owner", quote(getObjectOwner())); // NOI18N

        return args;
    }

    /**
    * Executes command.
    * First it calls getCommand() to obtain command text. Then tries to open JDBC
    * connection and execute it. If connection is already open, uses it and leave
    * open; otherwise creates new one and closes after use. Throws DDLException if
    * something wrong occurs.
    */
    public void execute() throws DDLException {
        String fcmd;
        Connection fcon = null;
        boolean opened = false;
        executionWithException = false;

        try {
            fcmd = getCommand();
        } catch (Exception e) {
            Logger.getLogger("global").log(Level.INFO, null, e);
            executionWithException = true;
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getBundle("org.netbeans.lib.ddl.resources.Bundle").getString("EXC_UnableToFormat")+"\n" + format + "\n" + e.getMessage(), NotifyDescriptor.ERROR_MESSAGE)); // NOI18N
            return;
        }

        //In case of debug mode print command
        if (spec.getSpecificationFactory().isDebugMode()) {

            try {
                InputOutput io = IOProvider.getDefault().getIO(NbBundle.getBundle("org.netbeans.lib.ddl.resources.Bundle").getString("LBL_Output_Window"), false);
                io.select();
                OutputWriter ow = io.getOut(); //NOI18N
                if (ow != null) {
                    ow.println(fcmd);
                    ow.println(" "); //NOI18N
                } else
                    throw new Exception();

            } catch (Exception e) {
                Logger.getLogger("global").log(Level.INFO, e.getMessage() + "\n" + fcmd); //NOI18N
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
            executionWithException = true;
            if (opened && fcon != null)
                spec.closeJDBCConnection();

            throw new DDLException(NbBundle.getBundle("org.netbeans.lib.ddl.resources.Bundle").getString("EXC_UnableToExecute")+"\n" + fcmd + "\n" + e.getMessage()); // NOI18N
        }

        if (opened)
            spec.closeJDBCConnection();
    }

    /**
    * Returns full string representation of command. This string needs no
    * formatting and could be used directly as argument of executeUpdate()
    * command. Throws DDLException if format is not specified or CommandFormatter
    * can't format it (it uses MapFormat to process entire lines and can solve []
    * enclosed expressions as optional.
    */
    public String getCommand() throws DDLException {
        if (format == null)
            throw new DDLException(NbBundle.getBundle("org.netbeans.lib.ddl.resources.Bundle").getString("EXC_NoFormatSpec")); // NOI18N
        try {
            Map props = getCommandProperties();
            return CommandFormatter.format(format, props);
        } catch (Exception e) {
            throw new DDLException(e.getMessage());
        }
    }

    /** information about appearance some exception in the last execute a bunch of commands */
    public boolean wasException() {
        return executionWithException;
    }

    private String getQuoteString() {
        try {
            quoteStr = getSpecification().getJDBCConnection().getMetaData().getIdentifierQuoteString();
            
            //Firebird patch (commands don't work with quoted names)
            if (getSpecification().getJDBCConnection().getMetaData().getDatabaseProductName().indexOf("Firebird") != -1) //NOI18N
                quoteStr = "";
        } catch (SQLException exc) {
            //PENDING
        }
        if (quoteStr == null)
            quoteStr = ""; //NOI18N
        else
            quoteStr.trim();

        return quoteStr;
    }

    public String quote(String name) {
        if (name == null || name.equals(""))
            return name;

        if (quoteStr == null)
            quoteStr = getQuoteString();

        return quoteStr + name + quoteStr;
    }
    
    /** Reads object from stream */
    public void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        format = (String)in.readObject();
        owner = (String)in.readObject();
        name = (String)in.readObject();
        addprops = (Map)in.readObject();
    }

    /** Writes object to stream */
    public void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        //System.out.println("Writing command "+name);
        out.writeObject(format);
        out.writeObject(owner);
        out.writeObject(name);
        out.writeObject(addprops);
    }
}
