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

package org.netbeans.beaninfo;

import javax.swing.ActionMap;
import javax.swing.text.DefaultEditorKit;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.util.Lookup;

/** Common panel to be used in core beaninfo and editors.
 */
public class ExplorerPanel extends org.openide.windows.TopComponent 
implements ExplorerManager.Provider {
    private ExplorerManager manager = new ExplorerManager();

    public ExplorerPanel() {
        manager = new ExplorerManager();
        ActionMap map = getActionMap();
        map.put(DefaultEditorKit.copyAction, ExplorerUtils.actionCopy(manager));
        map.put(DefaultEditorKit.cutAction, ExplorerUtils.actionCut(manager));
        map.put(DefaultEditorKit.pasteAction, ExplorerUtils.actionPaste(manager));
        map.put("delete", ExplorerUtils.actionDelete(manager, true));

        associateLookup(ExplorerUtils.createLookup (manager, map));
    }

    public ExplorerManager getExplorerManager() {
        return manager;
    }
    public void addNotify() {
        super.addNotify();
        ExplorerUtils.activateActions(manager, true);
    }
    public void removeNotify() {
        ExplorerUtils.activateActions(manager, false);
        super.removeNotify();
    }
}

