/*
 * ProxyActionChildren.java
 *
 * Created on February 5, 2007, 1:02 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.swingapp.actiontable;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.swingapp.*;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author joshy
 */
public class ProxyActionChildren extends Children.Keys {
    private List<ProxyAction> acts = new ArrayList<ProxyAction>();
    ProxyActionChildren() {
        
    }
    ProxyActionChildren(List<ProxyAction> acts) {
        this.acts = acts;
    }
    protected void addNotify() {
        ProxyAction[] acts = new ProxyAction[this.acts.size()];
        for(int i=0; i<acts.length; i++) {
            acts[i] = this.acts.get(i);
        }
        setKeys(acts);
    }
    protected Node[] createNodes(Object key) {
        ProxyAction act = (ProxyAction) key;
        AbstractNode result = new ProxyActionNode(Lookups.singleton(key));
        result.setDisplayName(act.getId());
        return new Node[] {result};
    }
    
    
}
