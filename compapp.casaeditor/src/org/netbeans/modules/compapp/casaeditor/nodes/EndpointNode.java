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

package org.netbeans.modules.compapp.casaeditor.nodes;

import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaEndpointRef;
import org.netbeans.modules.compapp.casaeditor.model.jbi.impl.JBIAttributes;
import org.netbeans.modules.compapp.casaeditor.nodes.actions.AddConnectionAction;
import org.netbeans.modules.compapp.casaeditor.properties.PropertyUtils;
import org.openide.nodes.Children;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 * Base class for ConsumesNode and ProvidesNode.
 * 
 * @author jqian
 */
public class EndpointNode extends CasaNode {

    public EndpointNode(CasaEndpointRef component, CasaNodeFactory factory) {
        super(component, Children.LEAF, factory);
    }

    protected void setupPropertySheet(Sheet sheet) {
        final CasaEndpointRef endpoint = (CasaEndpointRef) getData();
        if (endpoint == null) {
            return;
        }

        Sheet.Set mainPropertySet = getPropertySet(sheet, PropertyUtils.PropertiesGroups.MAIN_SET);

        PropertyUtils.installEndpointInterfaceQNameProperty(mainPropertySet, this, endpoint, JBIAttributes.INTERFACE_NAME.getName(), "interfaceQName", NbBundle.getMessage(getClass(), "PROP_InterfaceName"), NbBundle.getMessage(getClass(), "PROP_InterfaceName")); // NOI18N
        PropertyUtils.installEndpointServiceQNameProperty(mainPropertySet, this, endpoint, JBIAttributes.SERVICE_NAME.getName(), "serviceQName", NbBundle.getMessage(getClass(), "PROP_ServiceName"), NbBundle.getMessage(getClass(), "PROP_ServiceName")); // NOI18N
        PropertyUtils.installEndpointNameProperty(mainPropertySet, this, endpoint, JBIAttributes.ENDPOINT_NAME.getName(), "endpointName", NbBundle.getMessage(getClass(), "PROP_EndpointName"), NbBundle.getMessage(getClass(), "PROP_EndpointName")); // NOI18N
    }

    public String getName() {
        CasaEndpointRef endpoint = (CasaEndpointRef) getData();
        if (endpoint != null) {
            try {
                return endpoint.getEndpointName();
            } catch (Throwable t) {
                // getName MUST recover gracefully.
                return getBadName();
            }
        }
        return super.getName();
    }

    public boolean isEditable(String propertyType) {
        CasaEndpointRef endpoint = (CasaEndpointRef) getData();
        if (endpoint != null) {
            return getModel().isEditable(endpoint, propertyType);
        }
        return false;
    }

    public boolean isDeletable() {
        CasaEndpointRef endpoint = (CasaEndpointRef) getData();
        if (endpoint != null) {
            return getModel().isDeletable(endpoint);
        }
        return false;
    }

    @Override
    protected void addCustomActions(List<Action> actions) {
        actions.add(SystemAction.get(AddConnectionAction.class));
    }
}
