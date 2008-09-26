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
package org.netbeans.modules.j2ee.websphere6.ui.nodes;


import java.awt.Component;
import java.awt.Image;
import java.beans.PropertyEditor;

import javax.enterprise.deploy.spi.DeploymentManager;
import javax.swing.Action;

import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.websphere6.ui.nodes.actions.ShowAdminConsoleAction;
import org.netbeans.modules.j2ee.websphere6.ui.nodes.actions.ShowServerLogAction;

import org.netbeans.modules.j2ee.websphere6.WSDeploymentFactory;
import org.netbeans.modules.j2ee.websphere6.WSDeploymentManager;
import org.netbeans.modules.j2ee.websphere6.j2ee.DeploymentManagerProperties;
import org.netbeans.modules.j2ee.websphere6.j2ee.WSJ2eePlatformFactory;
import org.netbeans.modules.j2ee.websphere6.ui.nodes.editors.WSPasswordEditor;

import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;

/**
 * Node that will appear in the Server Registry and represent a concrete server
 * instance.
 *
 * @author Kirill Sorokin
 */
public class WSManagerNode extends AbstractNode implements Node.Cookie {

    /**
     * The associated deployment manager, i.e. the plugin's wrapper for
     * the server implementation of the DEploymentManager interface
     */
    private WSDeploymentManager deploymentManager;

    // properties names
    private static final String DISPLAY_NAME = "displayName";          // NOI18N
    private static final String URL = "url";                           // NOI18N
    private static final String USERNAME = "username";                 // NOI18N
    private static final String PASSWORD = "password";                 // NOI18N
    private static final String SERVER_ROOT = "serverRoot";            // NOI18N
    private static final String DOMAIN_ROOT = "domainRoot";            // NOI18N
    private static final String DEBUGGER_PORT = "debuggerPort";        // NOI18N

    /**
     * Path to the node's icon that should reside in the class path
     */
    private static final String ICON = "org/netbeans/modules/j2ee/" +  // NOI18N
            "websphere6/resources/16x16.gif";                          // NOI18N

    /**
     * Creates a new instance of the WSManagerNode.
     *
     * @param children the node's children
     * @param lookup a lookup object that contains the objects required for
     *      node's customization, such as the deployment manager
     */
    public WSManagerNode(Children children, Lookup lookup) {
        super(children);

        // get the deployment manager from the lookup and save it

        this.deploymentManager = (WSDeploymentManager) lookup.lookup(
                DeploymentManager.class);

        // add the node itself to its cookie list
        getCookieSet().add(this);
    }

    /**
     * Returns the node's tooltip
     *
     * @return the node's tooltip
     */
    public String getDisplayName() {
        return deploymentManager.getInstanceProperties().getProperty(
                InstanceProperties.URL_ATTR);
    }

    /**
     * Returns the node's icon when the node is in closed state
     *
     * @return the node's icon
     */
    public Image getIcon(int type) {
        return ImageUtilities.loadImage(ICON);
    }

    /**
     * Returns the node's icon when the node is in open state
     *
     * @return the node's icon
     */
    public Image getOpenedIcon(int type) {
        return ImageUtilities.loadImage(ICON);
    }

    /**
     * Returns the node's associated help article pointer
     *
     * @return the node's help article
     */
    public HelpCtx getHelpCtx() {
        return new HelpCtx("j2eeplugins_property_sheet_server_" +
                "node_websphere"); //NOI18N
    }

    public Action[] getActions(boolean context) {
        Action[] newActions = new Action[3] ;

        newActions[0] = null;
        newActions[1] = SystemAction.get(ShowAdminConsoleAction.class);
        newActions[2] = SystemAction.get(ShowServerLogAction.class);

        return newActions;
    }

    /**
     * Creates and returns the node's properties sheet
     *
     * @return the node's properties sheet
     */
    protected Sheet createSheet() {
        // create a new sheet
        Sheet sheet = super.createSheet();

        // get the sheet's properties' set object
        Sheet.Set properties = sheet.get(Sheet.PROPERTIES);
        if (properties == null) {
            properties = Sheet.createPropertiesSet();
            sheet.put(properties);
        }

        // declare the new property object and start adding the properties
        Node.Property property;

        // DISPLAY NAME
        property = new PropertySupport.ReadWrite(
                DISPLAY_NAME,
                String.class,
                NbBundle.getMessage(WSManagerNode.class,
                "PROP_displayName"),                       // NOI18N
                NbBundle.getMessage(WSManagerNode.class,
                "HINT_displayName")                        // NOI18N
                ) {
            public Object getValue() {
                return deploymentManager.getInstanceProperties().
                        getProperty(
                        InstanceProperties.DISPLAY_NAME_ATTR);
            }

            public void setValue(Object value) {
                deploymentManager.getInstanceProperties().
                        setProperty(
                        InstanceProperties.DISPLAY_NAME_ATTR,
                        (String) value);
            }
        };
        properties.put(property);

        // URL
        property = new PropertySupport.ReadOnly(
                URL,
                String.class,
                NbBundle.getMessage(WSManagerNode.class,
                "PROP_url"),                               // NOI18N
                NbBundle.getMessage(WSManagerNode.class,
                "HINT_url")                                // NOI18N
                ) {
            public Object getValue() {
                return deploymentManager.getURI();
            }
        };
        properties.put(property);

        // USER NAME
        property = new PropertySupport.ReadWrite(
                USERNAME,
                String.class,
                NbBundle.getMessage(WSManagerNode.class,
                "PROP_username"),                          // NOI18N
                NbBundle.getMessage(WSManagerNode.class,
                "HINT_username")                           // NOI18N
                ) {
            public Object getValue() {
                return deploymentManager.getInstanceProperties().
                        getProperty(InstanceProperties.
                        USERNAME_ATTR);
            }

            public void setValue(Object value) {
                deploymentManager.getInstanceProperties().
                        setProperty(InstanceProperties.
                        USERNAME_ATTR, (String) value);
            }
        };
        properties.put(property);

        // PASSWORD
        property = new PropertySupport.ReadWrite(
                PASSWORD,
                String.class,
                NbBundle.getMessage(WSManagerNode.class,
                "PROP_password"),                          // NOI18N
                NbBundle.getMessage(WSManagerNode.class,
                "HINT_password")                           // NOI18N
                ) {
            public Object getValue() {
                String password = deploymentManager.
                        getInstanceProperties().getProperty(
                        InstanceProperties.PASSWORD_ATTR);
//                           return password.replaceAll(".", "\\*");     // NOI18N
                return password;
            }

            public void setValue(Object value) {
                deploymentManager.getInstanceProperties().
                        setProperty(InstanceProperties.PASSWORD_ATTR,
                        (String) value);
            }

            public PropertyEditor getPropertyEditor() {
                return new WSPasswordEditor();
            }
        };

        properties.put(property);

        // SERVER ROOT
        property = new PropertySupport.ReadOnly(
                SERVER_ROOT,
                String.class,
                NbBundle.getMessage(WSManagerNode.class,
                "PROP_serverRoot"),                        // NOI18N
                NbBundle.getMessage(WSManagerNode.class,
                "HINT_serverRoot")                         // NOI18N
                ) {
            public Object getValue() {
                return deploymentManager.getInstanceProperties().
                        getProperty(WSDeploymentFactory.
                        SERVER_ROOT_ATTR);
            }
        };
        properties.put(property);

        // DOMAIN ROOT
        property = new PropertySupport.ReadOnly(
                DOMAIN_ROOT,
                String.class,
                NbBundle.getMessage(WSManagerNode.class,
                "PROP_domainRoot"),                        // NOI18N
                NbBundle.getMessage(WSManagerNode.class,
                "HINT_domainRoot")                         // NOI18N
                ) {
            public Object getValue() {
                return deploymentManager.getInstanceProperties().
                        getProperty(
                        WSDeploymentFactory.DOMAIN_ROOT_ATTR);
            }
        };
        properties.put(property);

        // DEBUGGER PORT
        property = new PropertySupport.ReadWrite(
                DEBUGGER_PORT,
                Integer.class,
                NbBundle.getMessage(WSManagerNode.class,
                "PROP_debuggerPort"),                      // NOI18N
                NbBundle.getMessage(WSManagerNode.class,
                "HINT_debuggerPort")                       // NOI18N
                ) {
            public Object getValue() {
                String debuggerPort = deploymentManager.
                        getInstanceProperties().getProperty(
                        WSDeploymentFactory.DEBUGGER_PORT_ATTR);
                return new Integer(debuggerPort);
            }

            public void setValue(Object value) {
                deploymentManager.getInstanceProperties().
                        setProperty(WSDeploymentFactory.
                        DEBUGGER_PORT_ATTR, value.toString());
            }
        };
        properties.put(property);

        return sheet;
    }

    public boolean hasCustomizer() {
        return true;
    }

    public Component getCustomizer() {
        return new org.netbeans.modules.j2ee.websphere6.ui.Customizer(
                new WSJ2eePlatformFactory()
                .getJ2eePlatformImpl(deploymentManager),
                new DeploymentManagerProperties(deploymentManager));
    }

    public WSDeploymentManager getDeploymentManager() {
        return deploymentManager;
    }

    public String getAdminConsoleURL() {
        return "http://" + deploymentManager.getHost() + ":" + // NOI18N
                deploymentManager.getInstanceProperties().
                getProperty(WSDeploymentFactory.ADMIN_PORT_ATTR) +
                "/ibm/console"; // NOI18N
    }

    public String getLogFilePath() {
        return deploymentManager.getLogFilePath();
    }
}