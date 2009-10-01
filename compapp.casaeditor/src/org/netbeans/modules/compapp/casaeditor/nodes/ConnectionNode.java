/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

/*
 * ConnectionNode.java
 *
 * Created on November 2, 2006, 8:59 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.netbeans.modules.compapp.casaeditor.nodes;

import java.awt.Image;
import java.beans.PropertyEditor;
import java.util.List;
import javax.swing.Action;
import javax.xml.namespace.QName;
import org.netbeans.modules.compapp.casaeditor.Constants;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaConnection;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaEndpoint;
import org.netbeans.modules.compapp.casaeditor.nodes.actions.ClearConfigExtensionsAction;
import org.netbeans.modules.compapp.casaeditor.properties.NamespaceEditor;
import org.netbeans.modules.compapp.casaeditor.properties.PropertyUtils;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Josh Sandusky
 */
public class ConnectionNode extends CasaNode {

    private static final Image ICON = ImageUtilities.loadImage(
            "org/netbeans/modules/compapp/casaeditor/nodes/resources/ConnectionNode.png");  // NOI18N

    public ConnectionNode(CasaConnection component, CasaNodeFactory factory) {
        super(component, Children.LEAF, factory);
    }

    @Override
    protected void setupPropertySheet(Sheet sheet) {
        final CasaConnection casaConnection = (CasaConnection) getData();
        if (casaConnection == null) {
            return;
        }
        final CasaEndpoint casaConsumes = casaConnection.getConsumer().get();
        final CasaEndpoint casaProvides = casaConnection.getProvider().get();

        Sheet.Set consumerProperties =
                getPropertySet(sheet, PropertyUtils.PropertiesGroups.CONSUMER_SET);
        Node.Property<QName> consumerServiceNameSupport = new PropertySupport.ReadOnly<QName>(
                "serviceName", // NOI18N
                QName.class,
                NbBundle.getMessage(getClass(), "PROP_ServiceName"), // NOI18N
                Constants.EMPTY_STRING) {

            public QName getValue() {
                return casaConsumes.getServiceQName();
            }
      
            @Override
            public PropertyEditor getPropertyEditor() {
                return new NamespaceEditor(
                        getModel(),
                        getValue(),
                        getDisplayName(),
                        false);
            }
        };
        Node.Property<String> consumerEndpointNameSupport = new PropertySupport.ReadOnly<String>(
                "endpointName", // NOI18N
                String.class,
                NbBundle.getMessage(getClass(), "PROP_EndpointName"), // NOI18N
                Constants.EMPTY_STRING) {

            public String getValue() {
                return casaConsumes.getEndpointName();
            }
        };
        consumerProperties.put(consumerServiceNameSupport);
        consumerProperties.put(consumerEndpointNameSupport);

        Sheet.Set providerProperties =
                getPropertySet(sheet, PropertyUtils.PropertiesGroups.PROVIDER_SET);
        Node.Property<QName> providerServiceNameSupport = new PropertySupport.ReadOnly<QName>(
                "serviceName", // NOI18N
                QName.class,
                NbBundle.getMessage(getClass(), "PROP_ServiceName"), // NOI18N
                Constants.EMPTY_STRING) {

            public QName getValue() {
                return casaProvides.getServiceQName();
            }
            
            @Override
            public PropertyEditor getPropertyEditor() {
                return new NamespaceEditor(
                        getModel(),
                        getValue(),
                        getDisplayName(),
                        false);
            }
        };
        Node.Property<String> providerEndpointNameSupport = new PropertySupport.ReadOnly<String>(
                "endpointName", // NOI18N
                String.class,
                NbBundle.getMessage(getClass(), "PROP_EndpointName"), // NOI18N
                Constants.EMPTY_STRING) {

            public String getValue() {
                return casaProvides.getEndpointName();
            }
        };
        providerProperties.put(providerServiceNameSupport);
        providerProperties.put(providerEndpointNameSupport);


        // Add JBI extensions on connection
        ExtensionPropertyHelper.setupExtensionPropertySheet(this,
                casaConnection, sheet, "connection", null, "all"); // NOI18N
        
    }

    //The navigator title is unable to decode HTML text and showing the encoded chars (&#60;-&#62;) as is...
    @Override
    public String getName() {
        CasaConnection casaConnection = (CasaConnection) getData();
        if (casaConnection != null) {
            try {
                return casaConnection.getConsumer().get().getDisplayName() +
                        "<->" + casaConnection.getProvider().get().getDisplayName();   // NOI18N
            } catch (Throwable t) {
                // getName MUST recover gracefully.
                return getBadName();
            }
        }
        return super.getName();
    }

    private String getEncodingName() {
        CasaConnection casaConnection = (CasaConnection) getData();
        if (casaConnection != null) {
            return casaConnection.getConsumer().get().getDisplayName() +
                    "&#60;-&#62;" + casaConnection.getProvider().get().getDisplayName();   // NOI18N
        }
        return super.getName();
    }

    @Override
    public String getHtmlDisplayName() {
        try {
            String htmlDisplayName = getEncodingName();
            CasaConnection casaConnection = (CasaConnection) getData();
            String decoration = null;
            if (casaConnection != null) {
                String attr = casaConnection.getState();
                if ((attr != null) && (attr.equalsIgnoreCase("deleted"))) {         // NOI18N
                    decoration = "<font color='#999999'><DEL>" + htmlDisplayName + "</DEL></font>"; // NOI18N
                }
            }
            if (decoration == null) {
                return htmlDisplayName;
            }
            return decoration;
        } catch (Throwable t) {
            // getHtmlDisplayName MUST recover gracefully.
            return getBadName();
        }
    }

    @Override
    public Image getIcon(int type) {
        return ICON;
    }

    @Override
    public Image getOpenedIcon(int type) {
        return ICON;
    }

    @Override
    public boolean isDeletable() {
        CasaConnection connection = (CasaConnection) getData();
        if (connection != null) {
            return getModel().isDeletable(connection);
        }
        return false;
    }

    @Override
    public boolean isEditable(String propertyType) {
        if (propertyType.equals(ALWAYS_WRITABLE_PROPERTY)) {
            return true;
        }

        return false;
    }

    @Override
    protected void addCustomActions(List<Action> actions) {
        CasaConnection casaConnection = (CasaConnection) getData();

        if (casaConnection != null && isConnectionConfiguredWithQoS(casaConnection)) {
            actions.add(new ClearConfigExtensionsAction(
                    NbBundle.getMessage(ConnectionNode.class,
                    "CLEAR_QOS_CONFIG"), this));  // NOI18N
        }
    }

    private boolean isConnectionConfiguredWithQoS(CasaConnection casaConnection) {
        return casaConnection.getChildren().size() != 0;
    }
}

