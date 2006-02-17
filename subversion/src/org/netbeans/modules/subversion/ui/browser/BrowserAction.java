/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.subversion.ui.browser;

import java.beans.PropertyVetoException;
import javax.swing.AbstractAction;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;

/**
 *
 * @author Tomas Stupka
 */
public abstract class BrowserAction extends AbstractAction {
    
    private Browser browser;
        
    protected Browser getBrowser() {
        return browser;
    }

    public void setBrowser(Browser browser) {
        this.browser = browser;
    }
    
    protected ExplorerManager getExplorerManager() {
        return getBrowser().getExplorerManager();
    }
    
    protected Node[] getSelectedNodes() {
        return getBrowser().getSelectedNodes();
    }
    
    protected void setSelectedNodes(Node[] selection ) throws PropertyVetoException {
        getBrowser().setSelectedNodes(selection);
    }
    
}
