/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.utilities;

import java.util.ArrayList;
import java.util.List;
import org.openide.util.actions.SystemAction;

/**
 * Control centre - provides access to various parts of the module.
 * Some of the features are as follows:
 * <ul>
 *     <li>registry of all open windows and non-modal dialogs
 *         (so that they can be closed when the module is about
 *         to be uninstalled)</li>
 *     <li>registry of actions being activated
 *         (so that more instances of the same modal dialog cannot be opened
 *         - see
 *         <a href="http://www.netbeans.org/issues/show_bug.cgi?id=21343">bug
 *         #21343</a>)</li>
 * </ul>
 *
 * @author  Marian Petras
 */
public final class Manager {
    
    /** list of activated actions */
    private static List activatedActions = new ArrayList(2);
    
    /**
     * Marks that the specified action is activated and informs whether
     * the same action is already active or not
     *
     * @return  <code>true</code> if the action may be activated
     *          (i.e. was not active at the moment this method was called),
     *          <code>false</code> if it was already active
     */
    public static boolean actionActivated(SystemAction a) {
        synchronized (activatedActions) {
            if (activatedActions.contains(a)) {
                return false;
            } else {
                activatedActions.add(a);
                return true;
            }
        }
    }
    
    /**
     */
    public static void actionFinished(SystemAction a) {
        synchronized (activatedActions) {
            activatedActions.remove(a);
        }
    }
    
    /** Creates a new instance of Manager */
    private Manager() {
    }
    
}
