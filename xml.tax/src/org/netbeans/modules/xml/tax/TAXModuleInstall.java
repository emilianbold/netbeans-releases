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
package org.netbeans.modules.xml.tax;

import java.beans.*;
import java.util.Arrays;
import java.util.List;
import java.util.LinkedList;

import org.openide.modules.ModuleInstall;

/**
 * Module installation class for tax module.
 *
 * @author Libor Kramolis
 */
public class TAXModuleInstall extends ModuleInstall {
    
    private static final long serialVersionUID = -668302799172493302L;

    private static final String BEANINFO_PATH = "org.netbeans.modules.xml.tax.beans.beaninfo"; // NOI18N
    private static final String EDITOR_PATH   = "org.netbeans.modules.xml.tax.beans.editor";


    /**
     */
    public void restored () {
        installBeans();
    }

    /**
     */
    public void uninstalled () {
        uninstallBeans();
    }


    //
    // beans
    //

    /**
     */
    private void installBeans () {
	List searchPath;

	searchPath = new LinkedList (Arrays.asList (Introspector.getBeanInfoSearchPath()));
	searchPath.add (BEANINFO_PATH);
        Introspector.setBeanInfoSearchPath ((String[])searchPath.toArray (new String[0]));

	searchPath = new LinkedList (Arrays.asList (PropertyEditorManager.getEditorSearchPath()));
	searchPath.add (EDITOR_PATH);
        PropertyEditorManager.setEditorSearchPath  ((String[])searchPath.toArray (new String[0]));
    }

    /**
     */
    private void uninstallBeans () {
	List searchPath;

	searchPath = new LinkedList (Arrays.asList (Introspector.getBeanInfoSearchPath()));
	searchPath.remove (BEANINFO_PATH);
        Introspector.setBeanInfoSearchPath ((String[])searchPath.toArray (new String[0]));

	searchPath = new LinkedList (Arrays.asList (PropertyEditorManager.getEditorSearchPath()));
	searchPath.remove (EDITOR_PATH);
        PropertyEditorManager.setEditorSearchPath  ((String[])searchPath.toArray (new String[0]));
    }

}
