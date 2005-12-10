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

package org.netbeans.modules.collab.ui;

import javax.swing.ActionMap;
import javax.swing.text.DefaultEditorKit;
import org.openide.explorer.*;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

/**
 *
 * @author nenik
 */
class ExplorerPanel extends TopComponent implements ExplorerManager.Provider, Lookup.Provider {
    private ExplorerManager manager;

    public ExplorerPanel() {
        this.manager = new ExplorerManager();
        ActionMap map = this.getActionMap ();
        map.put(DefaultEditorKit.copyAction, ExplorerUtils.actionCopy(manager));
        map.put(DefaultEditorKit.cutAction, ExplorerUtils.actionCut(manager));
        map.put(DefaultEditorKit.pasteAction, ExplorerUtils.actionPaste(manager));
        map.put("delete", ExplorerUtils.actionDelete(manager, true)); // or false

        // following line tells the top component which lookup should be associated with it
        associateLookup (ExplorerUtils.createLookup (manager, map));
    }
    
    public ExplorerManager getExplorerManager() {
        return manager;
    }
    // It is good idea to switch all listeners on and off when the
    // component is shown or hidden. In the case of TopComponent use:
    protected void componentActivated() {
        ExplorerUtils.activateActions(manager, true);
    }
    protected void componentDeactivated() {
        ExplorerUtils.activateActions(manager, false);
    }
}
