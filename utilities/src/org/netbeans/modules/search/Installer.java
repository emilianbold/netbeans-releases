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

import org.netbeans.modules.search.types.*;

/**
* During restored() hooks SearchPresenter on FindAction. 
* During uninstalled() frees such hook.
*
* @author  Petr Kuzel
*/
public class Installer extends ModuleInstall {

    private final static long serialVersionUID = 1;

    /** Holds hooking code. */
    private SearchHook hook;

    public void installed() {
        restored();
    }

    /** Start listening at SELECTED_NODES.
    */
    public void restored () {
        // define default criteria tab order
        Registry.reorderBy(new Class[] {ObjectNameType.class, FullTextType.class, ObjectTypeType.class, ModificationDateType.class} );

        if (SearchEngine.getDefault() == null)
            SearchEngine.setDefault(new SearchEngineImpl());

        hook = new SearchHook(new SearchPerformer());
        hook.hook();

        // TODO listen on new project
    }

    /** Unhook listening at SELECTED_NODES and remove itself from menu.
    */
    public void uninstalled () {
        hook.unhook();
    }

}
