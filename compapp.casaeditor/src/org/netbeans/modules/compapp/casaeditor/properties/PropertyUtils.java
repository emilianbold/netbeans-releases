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
package org.netbeans.modules.compapp.casaeditor.properties;

import org.netbeans.modules.compapp.casaeditor.properties.extension.ExtensionPropertyFactory;
import java.util.Map;
import org.netbeans.modules.compapp.casaeditor.Constants;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponent;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaEndpointRef;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaExtensibilityElement;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaServiceUnit;
import org.netbeans.modules.compapp.casaeditor.nodes.CasaNode;
import org.netbeans.modules.compapp.projects.jbi.api.JbiExtensionAttribute;
import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;

/**
 * @author nk160297
 */
public abstract class PropertyUtils {

    public static enum PropertiesGroups {

        MAIN_SET,
        IDENTIFICATION_SET,
        TARGET_SET,
        CONSUMER_SET,
        PROVIDER_SET,
        EXPERT_SET,
        COLOR_SET,
        FONT_SET,
        STYLE_SET,
        GENERIC_SET;
        private String myDisplayName;

        public String getDisplayName() {
            if (myDisplayName == null) {
                myDisplayName = NbBundle.getMessage(PropertyType.class, this.toString());
            }
            return myDisplayName;
        }
    }

    public static Node.Property createErrorProperty(String displayName) {
        return new PropertySupport.ReadOnly<String>(
                "error", // NOI18N
                String.class,
                displayName,
                Constants.EMPTY_STRING) {

            public String getValue() {
                return NbBundle.getMessage(PropertyUtils.class, "PROP_ERROR_VALUE");    // NOI18N

            }
        };
    }
    
    public static Node.Property createErrorMetaDataProperty(String displayName) {
        return new PropertySupport.ReadOnly<String>(
                "error", // NOI18N
                String.class,
                displayName,
                Constants.EMPTY_STRING) {

            public String getValue() {
                return NbBundle.getMessage(PropertyUtils.class, "PROP_ERROR_METADATA_VALUE");    // NOI18N

            }
        };
    }

    public static void installEndpointInterfaceQNameProperty(
            Sheet.Set propertySet,
            CasaNode node,
            CasaEndpointRef component,
            String propertyType,
            String attributeName,
            String displayName,
            String displayDescription) {
        try {
            Node.Property property = new PropertyEndpointInterfaceQName(
                    node,
                    component,
                    propertyType,
                    attributeName,
                    displayName,
                    displayDescription);
            propertySet.put(property);
        } catch (Exception e) {
            propertySet.put(createErrorProperty(displayName));
            ErrorManager.getDefault().notify(e);
        }
    }

    public static void installEndpointServiceQNameProperty(
            Sheet.Set propertySet,
            CasaNode node,
            CasaEndpointRef component,
            String propertyType,
            String attributeName,
            String displayName,
            String displayDescription) {
        try {
            Node.Property property = new PropertyEndpointServiceQName(
                    node,
                    component,
                    propertyType,
                    attributeName,
                    displayName,
                    displayDescription);
            propertySet.put(property);
        } catch (Exception e) {
            propertySet.put(createErrorProperty(displayName));
            ErrorManager.getDefault().notify(e);
        }
    }

    public static void installEndpointNameProperty(
            Sheet.Set propertySet,
            CasaNode node,
            CasaComponent component,
            String propertyType,
            String attributeName,
            String displayName,
            String displayDescription) {
        try {
            Node.Property property = new PropertyEndpointName(
                    node,
                    component,
                    propertyType,
                    attributeName,
                    displayName,
                    displayDescription);
            propertySet.put(property);
        } catch (Exception e) {
            propertySet.put(createErrorProperty(displayName));
            ErrorManager.getDefault().notify(e);
        }
    }

    public static void installServiceUnitNameProperty(
            Sheet.Set propertySet,
            CasaNode node,
            CasaServiceUnit component,
            String propertyType,
            String attributeName,
            String displayName,
            String displayDescription) {
        try {
            Node.Property property = new PropertyServiceUnitName(
                    node,
                    component,
                    propertyType,
                    attributeName,
                    displayName,
                    displayDescription);
            propertySet.put(property);
        } catch (Exception e) {
            propertySet.put(createErrorProperty(displayName));
            ErrorManager.getDefault().notify(e);
        }
    }

    /**
     * Installs a CASA configuration extension property.
     * 
     * @param propertySet   target property sheet set
     * @param node          node corresponding to the extension point component
     * @param extensionPointComponent a CASA extension point component
     * @param firstEE       the first (top-level) CASA extensibility element 
     *                      directly under the CASA extension point component
     * @param lastEE        the owner CASA extensibility element of the new
     *                      attribute being installed
     * @param propertyType
     * @param type          class type of the attribute
     * @param attributeName name of the attribute
     * @param displayname   display name of the attribute
     * @param description   description of the attribute
     */
    public static void installExtensionProperty(
            Sheet.Set propertySet,
            CasaNode node,
            CasaComponent extensionPointComponent,
            CasaExtensibilityElement firstEE,
            CasaExtensibilityElement lastEE,
            String propertyType,
            String attrType,
            String attributeName,
            String displayName,
            String description,
            String provider) {

        if (attrType == null) {
            System.err.println("Unsupported property type for " + attributeName);
            attrType = "String"; //JbiExtensionAttribute.Type.STRING;

        }

        try {
            Node.Property property = ExtensionPropertyFactory.getProperty(
                    node,
                    extensionPointComponent,
                    firstEE,
                    lastEE,
                    propertyType,
                    attrType,
                    attributeName,
                    displayName,
                    description,
                    provider);

            assert property != null;
            propertySet.put(property);

        } catch (Exception e) {
            ErrorManager.getDefault().notify(e);
            propertySet.put(createErrorMetaDataProperty(displayName));
        }
    }

    /**
     * Installs a CASA configuration extension property of enumerated strings.
     * 
     * @param propertySet   target property sheet set
     * @param node          node corresponding to the extension point component
     * @param extensionPointComponent a CASA extension point component
     * @param firstEE       the first (top-level) CASA extensibility element 
     *                      directly under the CASA extension point component
     * @param lastEE        the owner CASA extensibility element of the new
     *                      attribute being installed
     * @param propertyType
     * @param type          class type of the attribute
     * @param attributeName name of the attribute
     * @param displayname   display name of the attribute
     * @param description   description of the attribute
     * @param choiceMap     a map mapping choice element names to pre-built 
     *                      extensibility elements
     * @param choiceDisplayNameBiDiMap  a fake bi-directonal map mapping choice
     *                      element name to display name and display name
     *                      to element name
     * @param defaultChoice default choice
     */
    public static void installChoiceExtensionProperty(
            Sheet.Set propertySet,
            CasaNode node,
            CasaComponent extensionPointComponent,
            CasaExtensibilityElement firstEE,
            CasaExtensibilityElement lastEE,
            String propertyType,
            Class valueType,
            String attributeName,
            String displayName,
            String description,
            Map<String, CasaExtensibilityElement> choiceMap,
            Map<String, String> choiceDisplayNameMap,
            String defaultChoice) {

        assert valueType == String.class;

        try {
            Node.Property property = ExtensionPropertyFactory.getProperty(
                    node,
                    extensionPointComponent,
                    firstEE,
                    lastEE,
                    propertyType,
                    attributeName,
                    displayName,
                    description,
                    choiceMap,
                    choiceDisplayNameMap,
                    defaultChoice);

            propertySet.put(property);
        } catch (Exception e) {
            propertySet.put(createErrorProperty(displayName));
            ErrorManager.getDefault().notify(e);
        }
    }
}
