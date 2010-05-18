/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.j2ee.sun.bridge.apis;

import java.util.logging.Level;

import javax.swing.Action;
import org.netbeans.modules.j2ee.sun.util.ContainerChildFactory;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.SystemAction;
import org.openide.util.NbBundle;

/**
 *
 *
 *
 */
public class AppserverMgmtContainerNode extends AppserverMgmtNode  implements RefreshCookie {

        public static final class WaitNode extends AbstractNode {
        public WaitNode() {
            super(Children.LEAF);
            setIconBase("org/netbeans/modules/java/navigation/resources/wait"); // NOI18N
            setDisplayName(NbBundle.getMessage(AppserverMgmtContainerNode.class,"WAITNODE")); //NOI18N
            setName(getClass().getName());
        }
    } // End of WaitNode class
        
    /**
     *
     */
    public AppserverMgmtContainerNode(final AppserverMgmtController controller, final String type) {
        super(controller, getChildren(controller, type), type);
    }
    /**
     *
     *
     */
    public AppserverMgmtContainerNode(final AppserverMgmtController controller, final Children children, final String nodeType) {
        super(controller, children, nodeType);

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
    static Children getChildren(final AppserverMgmtController controller,  final String type){
        return new ContainerChildren(controller, type);
    }

    
    /**
     *
     *
     */
    public void refresh(){
        setChildren(new ContainerChildren(getAppserverMgmtController(), getNodeType()));
        ContainerChildren ch = (ContainerChildren)getChildren();
        ch.updateKeys();
    }

    
    /**
     *
     *
     */
    public static class ContainerChildren extends Children.Keys {
        String type;
        ContainerChildFactory cfactory;
        public ContainerChildren(AppserverMgmtController controller, String type) {
            if(controller == null) {
                getLogger().log(Level.FINE, "Controller for child factory " +
                    "is null");
                getLogger().log(Level.FINE, "Type: " + type);
            }
            this.type = type;
            this.cfactory = new ContainerChildFactory(controller);
        }
        protected void addNotify() {
            
            Node n[]= new Node[1];
            n[0]=new WaitNode();
            setKeys(n);
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    try {
                        setKeys(cfactory.getChildrenObject(type));
                    } catch (RuntimeException e) {
                        getLogger().log(Level.FINE, e.getMessage(), e);
                    }
                }
            });
            
        }
        protected void removeNotify() {
            setKeys(java.util.Collections.EMPTY_SET);
        }
        public void updateKeys() {
            refresh();
        }
        protected org.openide.nodes.Node[] createNodes(Object obj) {
            try {
                return new Node[] { (Node)obj };
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
