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

package com.netbeans.enterprise.modules.db.explorer;

import java.util.*;
import java.beans.PropertyEditor;
import com.netbeans.ddl.impl.*;
import com.netbeans.enterprise.modules.db.explorer.infos.DatabaseNodeInfo;
import org.openide.nodes.PropertySupport;

public class DatabaseTypePropertySupport extends DatabasePropertySupport
{
	private int[] types;
	private String[] names;
	
	public DatabaseTypePropertySupport(String name, Class type, String displayName, String shortDescription, DatabaseNodeInfo rep, boolean writable, boolean expert)	
	{
		super(name, type, displayName, shortDescription, rep, writable);
		repository = rep;
    	int i = 0;
    	
    	Specification spec = (Specification)((DatabaseNodeInfo)repository).getSpecification();
    	if (spec != null && writable) {
			Map tmap = ((Specification)((DatabaseNodeInfo)repository).getSpecification()).getTypeMap();
			if (tmap == null) tmap = new HashMap(1);
			Iterator enu = tmap.keySet().iterator();
			types = new int[tmap.size()];
			names = new String[tmap.size()];
			while(enu.hasNext()) {
				String key = (String)enu.next();
				int xtype = Specification.getType(key);
				String code = (String)tmap.get(key);
				types[i] = xtype;
				names[i++] = code;
			}
		} else {
			types = new int[] {0};
			names = new String[] {name};
		}
		
		if (expert) setExpert(true);
	}
	
    public PropertyEditor getPropertyEditor () 
    {
		PropertyEditor pe = new DatabaseTypePropertyEditor(types, names);
		return pe;
    }
}