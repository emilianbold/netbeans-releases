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

import org.openide.*;
import org.openide.modules.ModuleInstall;
import org.openide.loaders.*;

import org.openidex.search.SearchEngine;
import org.openidex.util.*;

import org.netbeans.modules.search.res.*;
import org.netbeans.modules.search.types.*;

/**
* During restored() hooks SearchPresenter on FindAction. 
* During uninstalled() frees such hook.
* Add RepositorySearchAction to Tools menu.
*
* @author  Petr Kuzel
* @version 1.0
*/
public class Installer extends ModuleInstall {

    private final static long serialVersionUID = 1;

    // place itself in
    private final String MENU = "Edit"; // NOI18N
    // place itself behind
    private final String MENUITEM = "Goto"; // NOI18N

    /** Holds hooking code. */
    private SearchHook hook;

    /** Install in tools menu. Place after "UnmountFSAction" separated by separator.
    */
    public void installed() {

        try {

            Utilities2.createAction (
                RepositorySearchAction.class,
                DataFolder.create (
                    TopManager.getDefault ().getPlaces ().folders ().menus (),
                    MENU
                ),
                MENUITEM, true, true, false, false
            );


            // add to action pool

            Utilities2.createAction (
                RepositorySearchAction.class,
                DataFolder.create (
                    TopManager.getDefault ().getPlaces ().folders ().actions (),
                    MENU
                )
            );

        } catch (Exception ex) {
            if (System.getProperty ("netbeans.debug.exceptions") != null)
                ex.printStackTrace ();
        }

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
        try {
            hook.unhook();

            Utilities2.removeAction (
                RepositorySearchAction.class,
                DataFolder.create (
                    TopManager.getDefault ().getPlaces ().folders ().menus (),
                    MENU
                )
            );

            // remove from actions pool

            Utilities2.removeAction (
                RepositorySearchAction.class,
                DataFolder.create (
                    TopManager.getDefault ().getPlaces ().folders ().actions (),
                    MENU
                )
            );

        } catch (Exception ex) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
                ex.printStackTrace ();
        }
    }

}
