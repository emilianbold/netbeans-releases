/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.search;


import org.openide.modules.ModuleInstall;


/**
 * Module installation class for search 'sub module'.
 *
 * @author  Petr Kuzel
 */
public class Installer extends ModuleInstall {

    /** Serial version UID. */
    private final static long serialVersionUID = 1;

    /** Holds hooking code. */
    private static SearchHook hook;


    /** Restores module. Overrides superclass method. 
     * Hooks <code>SearchPerformer</code> on <code>FindAction</code>. */
    public void restored () {
        hook = new SearchHook(SearchPerformer.getDefault());
        hook.hook();
    }

    /** Unistalls module. Overrides superclass method.
     * Unhooks <code>SearchPerformer</code> from <code>FindAction</code>. */
    public void uninstalled () {
        hook.unhook();
    }
}
