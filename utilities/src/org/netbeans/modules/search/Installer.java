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
    private SearchHook hook;


    // NOTE: It would be nice in installed method will be needed to set initial order of
    // search types. Then it will be possible to manage the order by user
    // in Project Settings... . Now there is always fixed order made by
    // SearchPanel.SearchPanelComparator.
    // !!! But all the above will probably be needless when UI of search will change.

    /** Restores module. Overrides superclass method. 
     * Hooks <code>SearchPerformer</code> on <code>FindAction</code>. */
    public void restored () {
        hook = new SearchHook(SearchPerformer.getDefault());
        hook.hook();
        // NOTE listen on new project?
    }

    /** Unistalls module. Overrides superclass method.
     * Unhooks <code>SearchPerformer</code> from <code>FindAction</code>. */
    public void uninstalled () {
        hook.unhook();
    }
}
