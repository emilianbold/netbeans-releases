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
package org.netbeans.modules.sun.manager.jbi.management.model;

import org.netbeans.modules.sun.manager.jbi.management.model.constraint.JBIComponentConfigurationConstraint;
import org.netbeans.modules.sun.manager.jbi.management.model.constraint.CompositeConstraint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;

/**
 * JBI Component configuration property descriptor. 
 * 
 * This can represents a regular property, a property group (compound property),
 * application variable or application configuration.
 * 
 * @author jqian
 */
public class JBIComponentConfigurationDescriptor {

    public static final String XSD_NS = "http://www.w3.org/2001/XMLSchema"; // NOI18N
    // currently supported xsd types
    public static final QName XSD_BYTE = new QName(XSD_NS, "byte"); // NOI18N
    public static final QName XSD_SHORT = new QName(XSD_NS, "short"); // NOI18N
    public static final QName XSD_INT = new QName(XSD_NS, "int"); // NOI18N
//    public static final QName XSD_LONG = new QName(XSD_NS, "long"); // NOI18N
//    public static final QName XSD_FLOAT = new QName(XSD_NS, "float"); // NOI18N
//    public static final QName XSD_DOUBLE = new QName(XSD_NS, "double"); // NOI18N
//    public static final QName XSD_DECIMAL = new QName(XSD_NS, "decimal"); // NOI18N
//    public static final QName XSD_INTEGER = new QName(XSD_NS, "integer"); // NOI18N
    public static final QName XSD_POSITIVE_INTEGER = new QName(XSD_NS, "positiveInteger"); // NOI18N
    public static final QName XSD_NEGATIVE_INTEGER = new QName(XSD_NS, "negativeInteger"); // NOI18N
    public static final QName XSD_NON_POSITIVE_INTEGER = new QName(XSD_NS, "nonPositiveInteger"); // NOI18N
    public static final QName XSD_NON_NEGATIVE_INTEGER = new QName(XSD_NS, "nonNegativeInteger"); // NOI18N
    public static final QName XSD_STRING = new QName(XSD_NS, "string"); // NOI18N
    public static final QName XSD_BOOLEAN = new QName(XSD_NS, "boolean"); // NOI18N
    public static final QName[] SUPPORTED_TYPES = new QName[]{
        XSD_BYTE, XSD_SHORT, XSD_INT, XSD_POSITIVE_INTEGER, XSD_NEGATIVE_INTEGER,
        XSD_NON_POSITIVE_INTEGER, XSD_NON_NEGATIVE_INTEGER, XSD_STRING, XSD_BOOLEAN
    };
    private static final String SHOWDISPLAY_INSTALLATION = "install"; // NOI18N
    private static final String SHOWDISPLAY_RUNTIME = "runtime"; // NOI18N
    private static final String SHOWDISPLAY_ALL = "all"; // NOI18N
    private static final String PROPERTY = "Property"; // NOI18N
    private static final String PROPERTY_GROUP = "PropertyGroup"; // NOI18N
//    private static final String APPLICATION_VARIABLE = "ApplicationVariable"; // NOI18N
//    private static final String APPLICATION_CONFIGURATION = "ApplicationConfiguration"; // NOI18N
    private String name;
    private String displayName;
    private String description;
    private boolean encrypted = false;
    private boolean applicationRestartRequired;
    private boolean componentRestartRequired;
    private boolean serverRestartRequired;
    private boolean required = false;
    private String showDisplay = SHOWDISPLAY_RUNTIME;
    private String onChangeMessage;
    private QName typeQName;
    private String defaultValue;
    private String propertyType;
    private JBIComponentConfigurationConstraint constraint;
    private Map<String, JBIComponentConfigurationDescriptor> children;

    public JBIComponentConfigurationDescriptor() {
    }

    public String getName() {
        return name;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public QName getTypeQName() {
        return typeQName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description; //Utils.getTooltip(description);
    }

    /**
     * Gets all configuration constraints. Composite constraints are de-composed.
     * @return
     */
    public List<JBIComponentConfigurationConstraint> getConstraints() {
        List<JBIComponentConfigurationConstraint> ret =
                new ArrayList<JBIComponentConfigurationConstraint>();

        addConstraint(ret, constraint);

        return ret;
    }

    private void addConstraint(List<JBIComponentConfigurationConstraint> list,
            JBIComponentConfigurationConstraint constraint) {
        if (constraint instanceof CompositeConstraint) {
            for (JBIComponentConfigurationConstraint childConstraint : ((CompositeConstraint) constraint).getConstraints()) {
                addConstraint(list, childConstraint);
            }
        } else {
            list.add(constraint);
        }
    }

    public boolean showDisplayAtInstallation() {
        return SHOWDISPLAY_INSTALLATION.equals(showDisplay) ||
                SHOWDISPLAY_ALL.equals(showDisplay);
    }
    
    /**
     * Checks whether this descriptor (or any child descriptor, when recursive
     * is true) is an installation-time configuration.
     * 
     * @param recursive whether to search for child descriptors recursively     * 
     * @return
     */
    public boolean showDisplayAtInstallation(boolean recursive) {
        boolean ret = showDisplayAtInstallation();
        
        if (!ret && recursive) {
            for (JBIComponentConfigurationDescriptor child : getChildren()) {
                if (child.showDisplayAtInstallation(recursive)) {
                    return true;
                }
            }
        } 
        
        return ret;
    }

    /**
     * Checks whether this descriptor is an installation-time configuration. 
     * @return
     */
    public boolean showDisplayAtRuntime() {
        return SHOWDISPLAY_RUNTIME.equals(showDisplay) ||
                SHOWDISPLAY_ALL.equals(showDisplay);
    }

    public String getOnChangeMessage() {
        return onChangeMessage;
    }

    public boolean isEncrypted() {
        return encrypted;
    }

    public boolean isApplicationRestartRequired() {
        return applicationRestartRequired;
    }

    public boolean isComponentRestartRequired() {
        return componentRestartRequired;
    }

    public boolean isServerRestartRequired() {
        return serverRestartRequired;
    }

    public boolean isRequired() {
        return required;
    }

    // REMOVE ME
    public boolean isApplicationVariable() {
        return this instanceof ApplicationVariable;
    }

    // REMOVE ME
    public boolean isApplicationConfiguration() {
        return this instanceof ApplicationConfiguration;
    }

    public boolean isProperty() {
        return PROPERTY.equals(propertyType);
    }

    public boolean isPropertyGroup() {
        return PROPERTY_GROUP.equals(propertyType);
    }

    public void addChild(JBIComponentConfigurationDescriptor descriptor) {
        if (children == null) {
            children = new LinkedHashMap<String, JBIComponentConfigurationDescriptor>();
        }
        children.put(descriptor.getName(), descriptor);
    }

    public Set<String> getChildNames() {
        return children == null ? new HashSet<String>() : children.keySet();
    }

    public JBIComponentConfigurationDescriptor getChild(String name) {
        return children == null ? null : children.get(name);
    }

    public Collection<JBIComponentConfigurationDescriptor> getChildren() {
        return children == null || children.values() == null ?
            new ArrayList<JBIComponentConfigurationDescriptor>() : children.values();
    }

    public void setApplicationRestartRequired(boolean applicationRestartRequired) {
        this.applicationRestartRequired = applicationRestartRequired;
    }

    public void setComponentRestartRequired(boolean componentRestartRequired) {
        this.componentRestartRequired = componentRestartRequired;
    }

    public void setConstraint(JBIComponentConfigurationConstraint constraint) {
        this.constraint = constraint;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setEncrypted(boolean encrypted) {
        this.encrypted = encrypted;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOnChangeMessage(String onChangeMessage) {
        this.onChangeMessage = onChangeMessage;
    }

    /**
     * @param propertyType   property type: PROPERTY, APPLICATION_VARIABLE or 
     *                       APPLICATION_CONFIGURATION.
     */
    public void setPropertyType(String propertyType) {
        this.propertyType = propertyType;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public void setServerRestartRequired(boolean serverRestartRequired) {
        this.serverRestartRequired = serverRestartRequired;
    }

    public void setShowDisplay(String showDisplay) {
        this.showDisplay = showDisplay;
    }

    public void setTypeQName(QName typeQName) {
        this.typeQName = typeQName;
    }

    public String validate(Object value) {
        return constraint.validate(value);
    }

    public static class ApplicationVariable extends JBIComponentConfigurationDescriptor {

        ApplicationVariable() {
            setName("ApplicationVariable");
            setDisplayName("Application Variables");
            setDescription("Application Variables");

            JBIComponentConfigurationDescriptor name =
                    new JBIComponentConfigurationDescriptor();
            name.setName("name");
            name.setDisplayName("Name");
            name.setDescription("Application Variable Name");
            name.setTypeQName(XSD_STRING);

            JBIComponentConfigurationDescriptor type =
                    new JBIComponentConfigurationDescriptor();
            type.setName("type");
            type.setDisplayName("Type");
            type.setDescription("Application Variable Type");
            type.setTypeQName(XSD_STRING);

            JBIComponentConfigurationDescriptor value =
                    new JBIComponentConfigurationDescriptor();
            value.setName("value");
            value.setDisplayName("Value");
            value.setDescription("Application Variable Value");
            value.setTypeQName(XSD_STRING);

            addChild(name);
            addChild(type);
            addChild(value);
        }
    }

    public static class ApplicationConfiguration extends JBIComponentConfigurationDescriptor {

        ApplicationConfiguration() {
            setName("ApplicationConfiguration");
            setDisplayName("Application Configuration");
            setDescription("Application Configuration");
        }
    }
}
