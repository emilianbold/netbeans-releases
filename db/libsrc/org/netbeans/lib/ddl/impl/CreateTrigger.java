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

package com.netbeans.ddl.impl;

import java.util.*;
import com.netbeans.ddl.*;
import com.netbeans.ddl.impl.*;

/** 
* Interface of database action command. Instances should remember connection 
* information of DBSpec and use it in execute() method. This is a base interface
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
			case BEFORE: return "BEFORE";
			case AFTER: return "AFTER";
		}

		return null;
	}
		
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
			Map bindmap = (Map)props.get("Binding");
			String tname = (String)bindmap.get("EVENT");
			if (tname != null) {
				Map typemap = (Map)gprops.get(tname);
				if (typemap == null) throw new InstantiationException("unable to locate binded object "+tname);
				Class typeclass = Class.forName((String)typemap.get("Class"));
				String format = (String)typemap.get("Format");
				TriggerEvent evt = (TriggerEvent)typeclass.newInstance();
				Map temap = (Map)props.get("TriggerEventMap");
				evt.setName(TriggerEvent.getName(when));
				evt.setColumn(columnname);
				evt.setFormat(format);
				return (TriggerEvent)evt;
			} else throw new InstantiationException("unable to locate type EVENT in table: "+bindmap);
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
		String evs = "", argdelim = (String)props.get("TriggerEventListDelimiter");
		Map cmdprops = super.getCommandProperties();

		Enumeration col_e = events.elements();
		while (col_e.hasMoreElements()) {
			TriggerEvent evt = (TriggerEvent)col_e.nextElement();
			boolean inscomma = col_e.hasMoreElements();
			evs = evs + evt.getCommand(this)+(inscomma ? argdelim : "");
		}
		
		cmdprops.put("trigger.events", evs);
		cmdprops.put("trigger.condition", cond);
		cmdprops.put("trigger.timing", getTimingName(timing));	
		cmdprops.put("table.name", table);	
		cmdprops.put("trigger.body", body);
		if (eachrow) cmdprops.put("each.row", "");
		return cmdprops;	
	}
}

/*
* <<Log>>
*  1    Gandalf   1.0         4/6/99   Slavek Psenicka 
* $
*/
