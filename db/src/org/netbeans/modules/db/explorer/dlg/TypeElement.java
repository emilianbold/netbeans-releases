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

package com.netbeans.enterprise.modules.db.explorer.dlg;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import com.netbeans.ddl.*;
import org.openide.DialogDescriptor;
import org.openide.TopManager;
import org.openide.util.NbBundle;
import com.netbeans.enterprise.modules.db.explorer.*;

/** 
* xxx
*
* @author Slavek Psenicka
*/

class TypeElement
{
	private String tstr, tname;
	
	public TypeElement(String typestr, String name)
	{
		tstr = typestr;
		tname = name;
	}
	
	public String getType()
	{
		return tstr;
	}
	
	public String getName()
	{
		return tname;
	}
	
	public String toString()
	{
		return tname;
	}
}



/*
 * <<Log>>
 *  4    Gandalf   1.3         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         5/21/99  Slavek Psenicka new version
 *  1    Gandalf   1.0         5/14/99  Slavek Psenicka 
 * $
 */
