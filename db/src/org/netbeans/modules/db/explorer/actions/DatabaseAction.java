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

package com.netbeans.enterprise.modules.db.explorer.actions;

import java.io.*;
import java.beans.*;
import java.util.*;
import java.sql.*;
import com.netbeans.ide.*;
import com.netbeans.ide.util.*;
import com.netbeans.ide.util.actions.*;
import com.netbeans.ide.nodes.*;
import com.netbeans.enterprise.modules.db.explorer.*;
import com.netbeans.enterprise.modules.db.explorer.infos.*;
import com.netbeans.enterprise.modules.db.explorer.nodes.*;

public class DatabaseAction extends CookieAction
{
	protected String aname;
	protected String nodename;
	
	public String getName()
	{
		return aname;
	}
	
	public void setName(String name)
	{
		aname = name;	
	}
	
	public String getNode()
	{
		return nodename;
	}
	
	public void setNode(String name)
	{
		nodename = name;	
	}

	protected DatabaseNodeInfo findInfo(DatabaseNodeInfo nodei)
	{
		if (nodename == null) return nodei;
		Node node = nodei.getNode();
		while(true) {
			DatabaseNodeInfo ninfo = (DatabaseNodeInfo)node.getCookie(DatabaseNodeInfo.class);
			if (ninfo != null) {
				DatabaseNode dnode = (DatabaseNode)node;
				String code = ninfo.getCode();
				if (code.equals(nodename)) return ninfo;
				else node = node.getParentNode();
			} else break;
		}	
		
		return null;
	}
	
	public HelpCtx getHelpCtx() {
		return HelpCtx.DEFAULT_HELP;
	}

	protected Class[] cookieClasses()
	{
		return new Class[] {
			this.getClass()
		};	
	}

	protected int mode()
	{
		return MODE_ONE;
	}

	protected boolean enable(Node[] activatedNodes)
	{		
		return true;
	}

	public void performAction (Node[] activatedNodes) 
	{
	}
}

/*
 * <<Log>>
 */
