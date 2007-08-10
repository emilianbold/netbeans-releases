/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.sun.manager.jbi.nodes;

import java.util.Collections;
import java.util.logging.Level;
import javax.swing.Action;
import org.netbeans.modules.sun.manager.jbi.actions.RefreshAction;
import org.netbeans.modules.sun.manager.jbi.management.AppserverJBIMgmtController;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.actions.SystemAction;


/**
 * Abstract super class for all container node in JBI Manager.
 *
 * @author jqian
 */
public abstract class AppserverJBIMgmtContainerNode extends AppserverJBIMgmtNode 
        implements Refreshable {
    
    /**
     *
     */
    public AppserverJBIMgmtContainerNode(
            
            final AppserverJBIMgmtController controller, final NodeType type) {
        super(controller, getChildren(controller, type), type);
    }
    
    
    /**
     * Return the actions associated with the menu drop down seen when
     * a user right-clicks on an Applications node in the plugin.
     *
     * @param boolean true/false
     * @return An array of Action objects.
     */
    public Action[] getActions(boolean flag) {
        return new SystemAction[] {
            SystemAction.get(RefreshAction.class)
        };
    }

    /**
     *
     */
    static Children getChildren(final AppserverJBIMgmtController controller, 
            final NodeType type){
        return new JBIContainerChildren(controller, type);
    }

    
    /**
     *
     *
     */
    public void refresh(){
        setChildren(new JBIContainerChildren(getAppserverJBIMgmtController(), getNodeType()));
        JBIContainerChildren ch = (JBIContainerChildren)getChildren();
        ch.updateKeys();
    }


    /**
     *
     *
     */
    public static class JBIContainerChildren extends Children.Keys<Node> {
        NodeType type;
        JBIContainerChildFactory cfactory;
        public JBIContainerChildren(AppserverJBIMgmtController controller, NodeType type) {
            if(controller == null) {
                getLogger().log(Level.FINE, "Controller for child factory " + "is null");   // NOI18N
                getLogger().log(Level.FINE, "Type: " + type);   // NOI18N
            }
            this.type = type;
            this.cfactory = new JBIContainerChildFactory(controller);
        }
        protected void addNotify() {
            try {
                setKeys(this.cfactory.getChildrenObject(getNode(), this.type));
            } catch (RuntimeException e) {
                getLogger().log(Level.FINE, e.getMessage(), e);
            }   
        }
        protected void removeNotify() {
            setKeys(Collections.EMPTY_SET);
        }
        public void updateKeys() {
            refresh();
        }
        protected Node[] createNodes(Node obj) {
            try {
                return new Node[] { obj };
            } catch(RuntimeException rex) {
                getLogger().log(Level.FINE, rex.getMessage(), rex);
                return new Node[] {};
            } catch(Exception e) {
                getLogger().log(Level.FINE, e.getMessage(), e);
                return new Node[] {};
            }
        }
    }
}
