/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.makeproject.ui;

import java.lang.reflect.Method;
import org.openide.explorer.ExplorerManager;
import org.openide.util.Lookup;

/*
 * org.netbeans.modules.project.ui.ProjectTab is not public!. Use reflection....
 * See IZ 7551
 * ProjectTabBridge reflection bridge
 */
public class ProjectTabBridge {
    private static ProjectTabBridge instance = null;
    private static final String className = "org.netbeans.modules.project.ui.ProjectTab"; // NOI18N

    private Class refClass = null;

    public ProjectTabBridge() throws ClassNotFoundException {
	ClassLoader c = (ClassLoader)Lookup.getDefault ().lookup (ClassLoader.class);
	// Find the class
	if (c == null) {
	    refClass = Class.forName(className);
	}
	else {
	    refClass = Class.forName(className, true, c);
	}
    }

    public static ProjectTabBridge getInstance() {
	if (instance == null) {
	    try {
		instance = new ProjectTabBridge();
	    }
	    catch (java.lang.ClassNotFoundException e) {
		// FIXUP...
	    }
	}
	return instance;
    }
    

    public Object findDefault(String tcID) {
	String methodName = "findDefault"; // NOI18N
	Method method = null;
	Object ret = null;

	if (refClass == null)
	    return null;

	try {
	    method = refClass.getMethod(methodName, new Class[] {String.class});
	    ret = method.invoke(null, new Object[] {tcID});
	} catch(Exception e) {
	    System.err.println("ProjectTabBridge " + methodName + e); // NOI18N
	}

	return ret;
    }

    public ExplorerManager getExplorerManager() {
	Object projectTab = findDefault("projectTabLogical_tc"); // NOI18N

	String methodName = "getExplorerManager"; // NOI18N
	Method method = null;
	Object ret = null;

	if (refClass == null)
	    return null;

	try {
	    method = refClass.getMethod(methodName, new Class[0]);
	    ret = method.invoke(projectTab, new Object[0]);
	} catch(Exception e) {
	    System.err.println("ProjectTabBridge " + methodName + e); // NOI18N
	}

	return (ExplorerManager)ret;
    }
}
