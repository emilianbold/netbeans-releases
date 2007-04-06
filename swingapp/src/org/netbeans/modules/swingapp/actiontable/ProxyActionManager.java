/*
 * ProxyActionManager.java
 *
 * Created on February 5, 2007, 1:03 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.swingapp.actiontable;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import org.netbeans.modules.swingapp.*;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.AbstractNode;

/**
 *
 * @author joshy
 */
public class ProxyActionManager extends JPanel implements ExplorerManager.Provider {
    public ProxyActionManager(List<ProxyAction> acts) {
        mgr.setRootContext(new AbstractNode(new ProxyActionChildren(acts)));
    }
    private final ExplorerManager mgr = new ExplorerManager();
    public ExplorerManager getExplorerManager() {
        return mgr;
    }
}
