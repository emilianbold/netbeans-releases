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

package com.netbeans.enterprise.modules.db;

import java.io.*;
import com.netbeans.ide.modules.*;
import com.netbeans.ide.TopManager;

/**
* DB module.
* @author Slavek Psenicka
*/
public class DatabaseModule extends Object implements ModuleInstall 
{
	public void installed() {
	}

	public void restored() {
	}

	public void uninstalled() {
	}

	public boolean closing() {
		return true;
	}
}

/*
* <<Log>>
*  2    Gandalf   1.1         5/14/99  Slavek Psenicka new version
*  1    Gandalf   1.0         4/23/99  Slavek Psenicka 
* $
*/
