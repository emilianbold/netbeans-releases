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

import org.openide.nodes.Children;
import java.io.IOException;
import java.util.*;
import java.text.MessageFormat;
import com.netbeans.ddl.*;
import com.netbeans.ddl.impl.*;
import com.netbeans.enterprise.modules.db.*;
import com.netbeans.enterprise.modules.db.explorer.*;
import com.netbeans.enterprise.modules.db.explorer.infos.*;
import org.openide.util.datatransfer.PasteType;
import java.awt.datatransfer.Transferable;
import org.openide.nodes.NodeTransfer;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.nodes.*;

public class ColumnNode extends LeafNode
{		
	protected PropertySupport createPropertySupport(String name, Class type, String displayName, String shortDescription, DatabaseNodeInfo rep, boolean writable, boolean expert)
	{
		PropertySupport ps;
		if (name.equals("datatype")) {
			ps = new DatabaseTypePropertySupport(name, type, displayName, shortDescription, rep, writable, expert);
		} else ps = super.createPropertySupport(name, type, displayName, shortDescription, rep, writable, expert);
		return ps;
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
		} catch (CommandNotSupportedException ex) {
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
/*
 * <<Log>>
 *  10   Gandalf   1.9         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  9    Gandalf   1.8         9/30/99  Slavek Psenicka 
 *  8    Gandalf   1.7         9/13/99  Slavek Psenicka 
 *  7    Gandalf   1.6         9/13/99  Slavek Psenicka 
 *  6    Gandalf   1.5         9/8/99   Slavek Psenicka 
 *  5    Gandalf   1.4         8/19/99  Slavek Psenicka English
 *  4    Gandalf   1.3         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  3    Gandalf   1.2         5/21/99  Slavek Psenicka new version
 *  2    Gandalf   1.1         5/14/99  Slavek Psenicka new version
 *  1    Gandalf   1.0         4/23/99  Slavek Psenicka 
 * $
 */
