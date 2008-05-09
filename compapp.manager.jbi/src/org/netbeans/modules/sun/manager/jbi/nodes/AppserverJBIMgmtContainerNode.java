/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.sun.manager.jbi.nodes;

import com.sun.esb.management.api.notification.EventNotification;
import com.sun.esb.management.api.notification.EventNotificationListener;
import com.sun.esb.management.api.notification.NotificationService;
import com.sun.esb.management.common.ManagementRemoteException;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.Notification;
import javax.management.openmbean.CompositeDataSupport;
import javax.swing.Action;
import org.netbeans.modules.sun.manager.jbi.actions.RefreshAction;
import org.netbeans.modules.sun.manager.jbi.management.AppserverJBIMgmtController;
import org.netbeans.modules.sun.manager.jbi.util.Utils;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.actions.SystemAction;

/**
 * Abstract super class for all container nodes in JBI Manager.
 *
 * @author jqian
 */
public abstract class AppserverJBIMgmtContainerNode extends AppserverJBIMgmtNode
        implements Refreshable {

    private static Logger logger = Logger.getLogger("org.netbeans.modules.sun.manager.jbi.nodes.AppserverJBIMgmtContainerNode"); // NOI18N   
    
    /**
     * Creates a new instance of AppserverJBIMgmtContainerNode.
     */
    public AppserverJBIMgmtContainerNode(
            final AppserverJBIMgmtController controller,
            final NodeType nodeType) {
        super(controller, getChildren(controller, nodeType), nodeType);

        setDisplayName(nodeType.getDisplayName());

        String shortDescription = nodeType.getShortDescription();
        // Use HTML version for tooltip.
        setShortDescription(Utils.getTooltip(shortDescription));
        // Use non-HTML version in the property sheet's description area.
        setValue("nodeDescription", shortDescription); // NOI18N 

        try {            
            NotificationService notificationService = controller.getNotificationService();
            notificationService.addNotificationEventListener(new EventNotificationListener() {

                public void processNotification(EventNotification eventNotification) {
                    Notification notification = eventNotification.getNotification();
                    CompositeDataSupport userData = (CompositeDataSupport)notification.getUserData();
                    String notificationSourceType = (String) userData.get("SourceType"); // NOI18N
                    if (needRefresh(notificationSourceType)) {
                        refresh();
                    }
                }
            });
        } catch (ManagementRemoteException ex) {
            //Exceptions.printStackTrace(ex);
            logger.warning("Cannot get notification service: " + ex.getMessage()); // NOI18N
        }
    }
    
    /**
     * Whether this container node needs to be refreshed (that is, children
     * regenerated) when a notification of the given source type is received
     * from the runtime.
     * 
     * @param notificationSourceType  one of the following choices:
     *              "ServiceEngine" | "BindingComponent" | "SharedLibrary" | 
     *              "ServiceAssembly" | "ServiceUnit"
     * 
     * @return <code>true</code> if the children of this container node need
     *         an update; <code>false</code> otherwise.
     */
    protected abstract boolean needRefresh(String notificationSourceType);

    /**
     * Return the actions associated with the menu drop down seen when
     * a user right-clicks on an Applications node in the plugin.
     *
     * @param boolean true/false
     * @return An array of Action objects.
     */
    @Override
    public Action[] getActions(boolean flag) {
        return new SystemAction[]{
                    SystemAction.get(RefreshAction.class)
                };
    }

    /**
     *
     */
    static Children getChildren(final AppserverJBIMgmtController controller,
            final NodeType type) {
        return new JBIContainerChildren(controller, type);
    }

    /**
     *
     *
     */
    public void refresh() {
        setChildren(new JBIContainerChildren(
                getAppserverJBIMgmtController(), getNodeType()));
        JBIContainerChildren ch = (JBIContainerChildren) getChildren();
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
            if (controller == null) {
                getLogger().log(Level.FINE, "Controller for child factory " + "is null");   // NOI18N

                getLogger().log(Level.FINE, "Type: " + type);   // NOI18N

            }
            this.type = type;
            this.cfactory = new JBIContainerChildFactory(controller);
        }

        @Override
        protected void addNotify() {
            try {
                setKeys(this.cfactory.getChildrenObject(getNode(), this.type));
            } catch (ManagementRemoteException e) {
                getLogger().log(Level.FINE, e.getMessage(), e);
            }
        }

        @Override
        protected void removeNotify() {
            setKeys(Collections.EMPTY_SET);
        }

        public void updateKeys() {
            refresh();
        }

        protected Node[] createNodes(Node obj) {
            try {
                return new Node[]{obj                        };
            } catch (RuntimeException rex) {
                getLogger().log(Level.FINE, rex.getMessage(), rex);
                return new Node[]{};
            } catch (Exception e) {
                getLogger().log(Level.FINE, e.getMessage(), e);
                return new Node[]{};
            }
        }
    }
}
