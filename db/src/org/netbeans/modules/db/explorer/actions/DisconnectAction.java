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

public class DisconnectAction extends DEAction
{
	protected boolean enable(Node[] activatedNodes)
	{
		if (activatedNodes.length < 1) return false;
		Node node = (Node)activatedNodes[0].getCookie(this.getClass());
		if (DEConnectionNode.class.isInstance(node)) {
			DENodeInfo ninfo = ((DEConnectionNode)node).getInfo();
			Connection connection = (Connection)ninfo.get("connection");
			return (connection != null);
		}
		
		return false;
	}
}