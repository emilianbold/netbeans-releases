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
import org.openidex.search.SearchEngine;


/**
 * Installation class for former search module (org.netbeans.modules.search), now
 * part of utilities module. It's called from utilities instalation module class.
 * During restored() hooks SearchPresenter on FindAction and sets default search types order. 
 * During uninstalled() frees such hook.
 *
 * @author  Petr Kuzel
 */
public class Installer extends ModuleInstall {

    // PENDING what to do with the freaky number?
    /** Serial version UID. */
    private final static long serialVersionUID = 1L;

    /** Holds hooking code. */
    private SearchHook hook;

    
    /** Called when installed module first time. Overrides superclass method. */
    public void installed() {
        restored();
    }

    /** Called when module restored. Start listening at SELECTED_NODES. Overrides superclass method. 
     * @see SearchHook */
    public void restored () {
        // PENDING reodering here didn't work -> too early? Moved to static init of CriteriaModel.
        // Set default criteria tab order.
        //Registry.reorderBy(new Class[] {FullTextType.class, ObjectNameType.class, ObjectTypeType.class, ModificationDateType.class});

        if (SearchEngine.getDefault() == null)
            SearchEngine.setDefault(new SearchEngineImpl());

        hook = new SearchHook(new SearchPerformer());
        hook.hook();
    }

    /** Unhooks listening at SELECTED_NODES and remove itself from menu.
     * @see SearchHook */
    public void uninstalled () {
        hook.unhook();
    }

}
