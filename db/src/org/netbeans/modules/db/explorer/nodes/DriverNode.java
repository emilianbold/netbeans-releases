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

package com.netbeans.enterprise.modules.db.explorer.nodes;

import java.beans.*;
import com.netbeans.ide.nodes.Children;
import com.netbeans.enterprise.modules.db.*;
import com.netbeans.enterprise.modules.db.explorer.*;
import com.netbeans.enterprise.modules.db.explorer.infos.*;

public class DriverNode extends LeafNode implements PropertyChangeListener
{
	public void setInfo(DatabaseNodeInfo info)
	{
		super.setInfo(info);
		DatabaseDriver drv = (DatabaseDriver)info.get(DatabaseNodeInfo.DBDRIVER);
		if (drv != null) {
			info.put(DatabaseNodeInfo.NAME, drv.getName());
			info.put(DatabaseNodeInfo.URL, drv.getURL());		
			info.addDriverListener(this);
		} else System.out.println("driver for "+info.getCode()+" not specified");
	}

	public void propertyChange(PropertyChangeEvent evt)
	{
		DatabaseNodeInfo info = getInfo();
		String pname = evt.getPropertyName();
		Object newval = evt.getNewValue();
		System.out.println("Property "+pname+" changed to "+newval);
		DatabaseDriver drv = (DatabaseDriver)info.get(DatabaseNodeInfo.DBDRIVER);			
		if (pname.equals(DatabaseNodeInfo.NAME)) drv.setName((String)newval);
		if (pname.equals(DatabaseNodeInfo.URL)) drv.setURL((String)newval);
	}
}
