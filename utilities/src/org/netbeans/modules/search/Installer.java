/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.search;


import org.openide.modules.ModuleInstall;


/**
 * Module installation class for search 'sub module'.
 *
 * @author  Petr Kuzel
 * @author  Marian Petras
 */
public class Installer extends ModuleInstall {

    /** Serial version UID. */
    private final static long serialVersionUID = 1;

    /**
     */
    public void restored () {
        FindActionManager.getInstance().init();
        FindDialogMemory.getDefault().initialize();
    }

    /**
     */
    public void uninstalled () {
        FindActionManager.getInstance().cleanup();
        FindDialogMemory.getDefault().uninitialize();
        Manager.getInstance().doCleanup();
    }
}
