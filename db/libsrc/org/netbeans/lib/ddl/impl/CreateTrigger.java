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
import java.text.MessageFormat;

import org.openide.util.NbBundle;
import org.netbeans.lib.ddl.*;
import org.netbeans.lib.ddl.impl.*;

/**
* Interface of database action command. Instances should remember connection 
* information of DatabaseSpecification and use it in execute() method. This is a base interface
* used heavily for sub-interfacing (it is not subclassing :)
*
* @author Slavek Psenicka
*/

public class CreateTrigger extends AbstractCommand implements CreateTriggerCommand
{
    public static final int BEFORE = 1;
    public static final int AFTER = 2;

    /** Arguments */
    private Vector events;

    /** for each row */
    boolean eachrow;

    /** Condition */
    private String cond;

    /** Table */
    private String table;

    /** Timing */
    int timing;

    /** Body of the procedure */
    private String body;

    public static String getTimingName(int code)
    {
        switch (code) {
        case BEFORE: return "BEFORE"; // NOI18N
        case AFTER: return "AFTER"; // NOI18N
        }

        return null;
    }

    private static ResourceBundle bundle = NbBundle.getBundle("org.netbeans.lib.ddl.resources.Bundle"); // NOI18N
    
    static final long serialVersionUID =-2217362040968396712L;
    public CreateTrigger()
    {
        events = new Vector();
    }

    public String getTableName()
    {
        return table;
    }

    public void setTableName(String tab)
    {
        table = tab;
    }

    public boolean getForEachRow()
    {
        return eachrow;
    }

    public void setForEachRow(boolean flag)
    {
        eachrow = flag;
    }


    /** Returns text of procedure */
    public String getText()
    {
        return body;
    }

    /** Sets name of table */
    public void setText(String text)
    {
        body = text;
    }

    public String getCondition()
    {
        return cond;
    }

    public void setCondition(String con)
    {
        cond = con;
    }

    public int getTiming()
    {
        return timing;
    }

    public void setTiming(int time)
    {
        timing = time;
    }

    /** Returns arguments */
    public Vector getEvents()
    {
        return events;
    }

    public TriggerEvent getEvent(int index)
    {
        return (TriggerEvent)events.get(index);
    }

    /** Sets argument array */
    public void setEvents(Vector argarr)
    {
        events = argarr;
    }

    public void setEvent(int index, TriggerEvent arg)
    {
        events.set(index, arg);
    }

    public TriggerEvent createTriggerEvent(int when, String columnname)
    throws DDLException
    {
        try {
            Map gprops = (Map)getSpecification().getProperties();
            Map props = (Map)getSpecification().getCommandProperties(Specification.CREATE_TRIGGER);
            Map bindmap = (Map)props.get("Binding"); // NOI18N
            String tname = (String)bindmap.get("EVENT"); // NOI18N
            if (tname != null) {
                Map typemap = (Map)gprops.get(tname);
                if (typemap == null) throw new InstantiationException(
                    MessageFormat.format(
                        bundle.getString("EXC_UnableLocateObject"), // NOI18N
                        new String[] {tname}));
                Class typeclass = Class.forName((String)typemap.get("Class")); // NOI18N
                String format = (String)typemap.get("Format"); // NOI18N
                TriggerEvent evt = (TriggerEvent)typeclass.newInstance();
                Map temap = (Map)props.get("TriggerEventMap"); // NOI18N
                evt.setName(TriggerEvent.getName(when));
                evt.setColumn(columnname);
                evt.setFormat(format);
                return (TriggerEvent)evt;
            } else throw new InstantiationException(
                    MessageFormat.format(
                        bundle.getString("EXC_UnableLocateType"), // NOI18N
                        new String[] {"EVENT", bindmap.toString() })); // NOI18N
        } catch (Exception e) {
            throw new DDLException(e.getMessage());
        }
    }

    public void addTriggerEvent(int when)
    throws DDLException
    {
        addTriggerEvent(when, null);
    }

    public void addTriggerEvent(int when, String columnname)
    throws DDLException
    {
        TriggerEvent te = createTriggerEvent(when, columnname);
        if (te != null) events.add(te);
    }

    public Map getCommandProperties()
    throws DDLException
    {
        Map props = (Map)getSpecification().getProperties();
        String evs = "", argdelim = (String)props.get("TriggerEventListDelimiter"); // NOI18N
        Map cmdprops = super.getCommandProperties();

        Enumeration col_e = events.elements();
        while (col_e.hasMoreElements()) {
            TriggerEvent evt = (TriggerEvent)col_e.nextElement();
            boolean inscomma = col_e.hasMoreElements();
            evs = evs + evt.getCommand(this)+(inscomma ? argdelim : "");
        }

        cmdprops.put("trigger.events", evs); // NOI18N
        cmdprops.put("trigger.condition", cond); // NOI18N
        cmdprops.put("trigger.timing", getTimingName(timing)); // NOI18N
        cmdprops.put("table.name", table); // NOI18N
        cmdprops.put("trigger.body", body); // NOI18N
        if (eachrow) cmdprops.put("each.row", ""); // NOI18N
        return cmdprops;
    }
}

/*
* <<Log>>
*  5    Gandalf   1.4         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  4    Gandalf   1.3         9/10/99  Slavek Psenicka 
*  3    Gandalf   1.2         8/17/99  Ian Formanek    Generated serial version 
*       UID
*  2    Gandalf   1.1         4/23/99  Slavek Psenicka new version
*  1    Gandalf   1.0         4/6/99   Slavek Psenicka 
* $
*/
