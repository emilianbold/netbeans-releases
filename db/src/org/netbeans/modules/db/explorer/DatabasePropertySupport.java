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
import com.netbeans.enterprise.modules.db.explorer.infos.DatabaseNodeInfo;
import org.openide.nodes.PropertySupport;

public class DatabasePropertySupport extends PropertySupport
{
	protected Object repository;
	
	public DatabasePropertySupport(String name, Class type, String displayName, String shortDescription, Object rep, boolean writable) 
	{
		super(name, type, displayName, shortDescription, true, writable);
		repository = rep;
	}
	
    public Object getValue() 
    throws IllegalAccessException, IllegalArgumentException 
    {
		String code = getName();
		Object rval = ((DatabaseNodeInfo)repository).getProperty(code);
		return rval;
    }	

    public void setValue(Object val) 
    throws IllegalAccessException, IllegalArgumentException 
    {
		String code = getName();
		((DatabaseNodeInfo)repository).setProperty(code, val);
    }	
}
/*
 * <<Log>>
 *  4    Gandalf   1.3         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         5/21/99  Slavek Psenicka new version
 *  1    Gandalf   1.0         4/23/99  Slavek Psenicka 
 * $
 */
