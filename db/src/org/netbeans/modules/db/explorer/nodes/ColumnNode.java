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

import com.netbeans.ide.nodes.Children;
import com.netbeans.ddl.*;
import com.netbeans.ddl.impl.*;
import com.netbeans.enterprise.modules.db.explorer.infos.DatabaseNodeInfo;
import com.netbeans.enterprise.modules.db.DatabaseException;

public class ColumnNode extends DatabaseNode
{		
	public ColumnNode()
	{
		super(Children.LEAF);
	}

	public void setName(String newname)
	{
		try {
			DatabaseNodeInfo info = getInfo();
			String table = (String)info.get(DatabaseNode.TABLE);
			Specification spec = (Specification)info.getSpecification();
			RenameColumn cmd = spec.createCommandRenameColumn(table);
			cmd.renameColumn(info.getName(), newname);
			cmd.execute();
			super.setName(newname);
		} catch (Exception e) {
			System.out.println("Unable to change the name: "+e);
		}
	}
}