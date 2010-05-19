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
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaPort;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaServiceEngineServiceUnit;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel;
import org.netbeans.modules.compapp.casaeditor.model.jbi.impl.JBIAttributes;
import org.netbeans.modules.compapp.casaeditor.nodes.actions.AddConnectionAction;
import org.netbeans.modules.compapp.casaeditor.nodes.actions.GoToSourceAction;
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
public abstract class EndpointNode extends CasaNode {

    public EndpointNode(CasaEndpointRef component, CasaNodeFactory factory) {
        super(component, Children.LEAF, factory);
    }

    @Override
    protected void setupPropertySheet(Sheet sheet) {
        final CasaEndpointRef endpointRef = (CasaEndpointRef) getData();
        if (endpointRef == null) {
            return;
        }

        Sheet.Set mainPropertySet =
                getPropertySet(sheet, PropertyUtils.PropertiesGroups.MAIN_SET);

        PropertyUtils.installEndpointInterfaceQNameProperty(
                mainPropertySet,
                this,
                endpointRef,
                JBIAttributes.INTERFACE_NAME.getName(),
                "interfaceQName", // NOI18N
                NbBundle.getMessage(getClass(), "PROP_InterfaceName"), // NOI18N
                NbBundle.getMessage(getClass(), "PROP_InterfaceName")); // NOI18N

        PropertyUtils.installEndpointServiceQNameProperty(
                mainPropertySet,
                this,
                endpointRef,
                JBIAttributes.SERVICE_NAME.getName(),
                "serviceQName", // NOI18N
                NbBundle.getMessage(getClass(), "PROP_ServiceName"), // NOI18N
                NbBundle.getMessage(getClass(), "PROP_ServiceName")); // NOI18N

        PropertyUtils.installEndpointNameProperty(
                mainPropertySet,
                this,
                endpointRef,
                JBIAttributes.ENDPOINT_NAME.getName(),
                "endpointName", // NOI18N
                NbBundle.getMessage(getClass(), "PROP_EndpointName"), // NOI18N 
                NbBundle.getMessage(getClass(), "PROP_EndpointName")); // NOI18N

        /*
        String displayName = endpointRef.getDisplayName();
        if (displayName != null && displayName.length() > 0) {
            PropertyUtils.installEndpointDisplayNameProperty(
                    mainPropertySet,
                    this,
                    endpointRef,
                    CasaAttribute.DISPLAY_NAME.getName(),
                    "displayName", // NOI18N
                    NbBundle.getMessage(getClass(), "PROP_DisplayName"), // NOI18N 
                    NbBundle.getMessage(getClass(), "PROP_DisplayName")); // NOI18N
        }

        String processName = endpointRef.getProcessName();
        if (processName != null && processName.length() > 0) {
            PropertyUtils.installEndpointProcessNameProperty(
                    mainPropertySet,
                    this,
                    endpointRef,
                    CasaAttribute.PROCESS_NAME.getName(),
                    "processName", // NOI18N
                    NbBundle.getMessage(getClass(), "PROP_ProcessName"), // NOI18N 
                    NbBundle.getMessage(getClass(), "PROP_ProcessName")); // NOI18N
        }

        String filePath = endpointRef.getFilePath();
        if (filePath != null && filePath.length() > 0) {
            PropertyUtils.installEndpointFilePathProperty(
                    mainPropertySet,
                    this,
                    endpointRef,
                    CasaAttribute.FILE_PATH.getName(),
                    "filePath", // NOI18N
                    NbBundle.getMessage(getClass(), "PROP_FilePath"), // NOI18N 
                    NbBundle.getMessage(getClass(), "PROP_FilePath")); // NOI18N
        }
        */

        // Add JBI extensions
        CasaWrapperModel model = (CasaWrapperModel) endpointRef.getModel();
        String compName = model.getComponentName(endpointRef);
        if (compName != null) {
            ExtensionPropertyHelper.setupExtensionPropertySheet(this,
                    endpointRef, sheet, "endpoint", getConfigExtensionSubType(), compName); // NOI18N
        }
    }

    /**
     * Gets the config extension subtype for "endpoint" extension type,
     * that is, "consume" or "provide".
     *
     * @return config extension subtype
     */
    protected abstract String getConfigExtensionSubType();

    @Override
    public String getName() {
        CasaEndpointRef endpoint = (CasaEndpointRef) getData();
        if (endpoint != null) {
            try {
                return endpoint.getDisplayName();
            } catch (Throwable t) {
                // getName MUST recover gracefully.
                return getBadName();
            }
        }
        return super.getName();
    }

    @Override
    public boolean isEditable(String propertyType) {
        if (propertyType.equals(ALWAYS_WRITABLE_PROPERTY)) {
            return true;
        }

        CasaEndpointRef endpoint = (CasaEndpointRef) getData();
        if (endpoint != null) {
            return getModel().isEditable(endpoint, propertyType);
        }
        return false;
    }

    @Override
    public boolean isDeletable() {
        CasaEndpointRef endpoint = (CasaEndpointRef) getData();
        if (endpoint != null) {
            return getModel().isDeletable(endpoint);
        }
        return false;
    }

    @Override
    protected void addCustomActions(List<Action> actions) {
        AddConnectionAction addConnectionAction =
                SystemAction.get(AddConnectionAction.class);
        updateAddConnectionActionState(addConnectionAction); // #131853
        actions.add(addConnectionAction);

        CasaEndpointRef endpointRef = (CasaEndpointRef) getData();
        if (endpointRef != null) {
            String filePath = endpointRef.getFilePath();
            if (filePath != null && filePath.length() > 0) {
                actions.add(SystemAction.get(GoToSourceAction.class));
            }
        }
    }

    protected void updateAddConnectionActionState(AddConnectionAction action) {
        // do nothing
        // subclass might want to overwrite this to disable the action (see ConsumesNode)
    }
}
