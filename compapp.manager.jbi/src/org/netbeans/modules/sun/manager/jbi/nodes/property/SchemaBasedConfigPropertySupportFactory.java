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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.sun.manager.jbi.nodes.property;

import java.beans.PropertyEditor;
import java.util.List;
import javax.management.Attribute;
import javax.management.MBeanAttributeInfo;
import javax.xml.namespace.QName;
import org.netbeans.modules.sun.manager.jbi.editors.ComboBoxPropertyEditor;
import org.netbeans.modules.sun.manager.jbi.editors.PasswordEditor;
import org.netbeans.modules.sun.manager.jbi.management.model.constraint.JBIComponentConfigurationConstraint;
import org.netbeans.modules.sun.manager.jbi.management.model.JBIComponentConfigurationDescriptor;
import org.netbeans.modules.sun.manager.jbi.management.model.JBIComponentConfigurationMBeanAttributeInfo;
import org.netbeans.modules.sun.manager.jbi.management.model.constraint.EnumerationConstraint;
import org.netbeans.modules.sun.manager.jbi.management.model.constraint.MaxExclusiveConstraint;
import org.netbeans.modules.sun.manager.jbi.management.model.constraint.MaxInclusiveConstraint;
import org.netbeans.modules.sun.manager.jbi.management.model.constraint.MinExclusiveConstraint;
import org.netbeans.modules.sun.manager.jbi.management.model.constraint.MinInclusiveConstraint;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.PropertySupport;
import org.openide.util.NbBundle;

/**
 *
 * @author jqian
 */
public class SchemaBasedConfigPropertySupportFactory {

    public static PropertySupport getPropertySupport(
            final PropertySheetOwner propertySheetOwner,
            final Attribute attr,
            final JBIComponentConfigurationMBeanAttributeInfo info,
            String componentName) {

        JBIComponentConfigurationDescriptor descriptor = info.getConfigurationDescriptor();
        QName typeQName = descriptor.getTypeQName();

        List<JBIComponentConfigurationConstraint> constraints =
                descriptor.getConstraints();

        if (descriptor instanceof JBIComponentConfigurationDescriptor.ApplicationConfiguration) {
            return new ApplicationConfigurationsPropertySupport(
                    propertySheetOwner, attr, info, componentName);
        } else if (descriptor instanceof JBIComponentConfigurationDescriptor.ApplicationVariable) {
            return new ApplicationVariablesPropertySupport(
                    propertySheetOwner, attr, info);
        } else if (JBIComponentConfigurationDescriptor.XSD_STRING.equals(typeQName)) {
            if (constraints.size() == 0) {
                return getSimpleStringPropertySupport(propertySheetOwner, attr, info);
            } else if (constraints.size() == 1 &&
                    constraints.get(0) instanceof EnumerationConstraint) {
                List<String> options =
                        ((EnumerationConstraint) constraints.get(0)).getOptions();
                return getEnumeratedStringPropertySupport(
                        propertySheetOwner, attr, info, options);
            }
        } else if (JBIComponentConfigurationDescriptor.XSD_INT.equals(typeQName) ||
                JBIComponentConfigurationDescriptor.XSD_BYTE.equals(typeQName) ||
                JBIComponentConfigurationDescriptor.XSD_SHORT.equals(typeQName) ||
                JBIComponentConfigurationDescriptor.XSD_POSITIVE_INTEGER.equals(typeQName) ||
                JBIComponentConfigurationDescriptor.XSD_NEGATIVE_INTEGER.equals(typeQName) ||
                JBIComponentConfigurationDescriptor.XSD_NON_POSITIVE_INTEGER.equals(typeQName) ||
                JBIComponentConfigurationDescriptor.XSD_NON_NEGATIVE_INTEGER.equals(typeQName)) {

            int minInc, maxInc;

            if (JBIComponentConfigurationDescriptor.XSD_BYTE.equals(typeQName)) {
                minInc = Byte.MIN_VALUE;
                maxInc = Byte.MAX_VALUE;
            } else if (JBIComponentConfigurationDescriptor.XSD_SHORT.equals(typeQName)) {
                minInc = Short.MIN_VALUE;
                maxInc = Short.MAX_VALUE;
            } else if (JBIComponentConfigurationDescriptor.XSD_INT.equals(typeQName)) {
                minInc = Integer.MIN_VALUE;
                maxInc = Integer.MAX_VALUE;
            } else if (JBIComponentConfigurationDescriptor.XSD_POSITIVE_INTEGER.equals(typeQName)) {
                minInc = 1;
                maxInc = Integer.MAX_VALUE;
            } else if (JBIComponentConfigurationDescriptor.XSD_NEGATIVE_INTEGER.equals(typeQName)) {
                minInc = Integer.MIN_VALUE;
                maxInc = -1;
            } else if (JBIComponentConfigurationDescriptor.XSD_NON_POSITIVE_INTEGER.equals(typeQName)) {
                minInc = Integer.MIN_VALUE;
                maxInc = 0;
            } else { //if (JBIComponentConfigurationDescriptor.XSD_NON_NEGATIVE_INTEGER.equals(typeQName)) {
                minInc = 0;
                maxInc = Integer.MAX_VALUE;
            }

            for (JBIComponentConfigurationConstraint constraint : constraints) {
                if (constraint instanceof MinInclusiveConstraint) {
                    minInc = Math.max(minInc,
                            (int) ((MinInclusiveConstraint) constraint).getValue());
                } else if (constraint instanceof MaxInclusiveConstraint) {
                    maxInc = Math.min(maxInc,
                            (int) ((MaxInclusiveConstraint) constraint).getValue());
                } else if (constraint instanceof MinExclusiveConstraint) {
                    minInc = Math.max(minInc,
                            (int) ((MinInclusiveConstraint) constraint).getValue() + 1);
                } else if (constraint instanceof MaxExclusiveConstraint) {
                    maxInc = Math.min(maxInc,
                            (int) ((MaxInclusiveConstraint) constraint).getValue() - 1);
                } else {
                    throw new RuntimeException("Constraint not supported yet: " +
                            constraint.getClass().getName());
                }
            }
            return getIntegerPropertySupport(propertySheetOwner, attr, info, minInc, maxInc);
        } else if (JBIComponentConfigurationDescriptor.XSD_BOOLEAN.equals(typeQName)) {
            return getBooleanPropertySupport(propertySheetOwner, attr, info);
        }

        System.out.println("NewSchemaBasedConfigPropertySupportFactory: Unsupported type: " + typeQName);
        return null;
    }

    public static PropertySupport getEnumeratedStringPropertySupport(
            final PropertySheetOwner propertySheetOwner,
            final Attribute attr,
            final MBeanAttributeInfo info,
            final List<String> validValues) {

        return new SchemaBasedConfigPropertySupport<String>(
                propertySheetOwner, String.class, attr, info) {

            @Override
            public PropertyEditor getPropertyEditor() {
                return new ComboBoxPropertyEditor(validValues);
            }
        };
    }

    public static PropertySupport getIntegerPropertySupport(
            final PropertySheetOwner propertySheetOwner,
            final Attribute attr,
            final MBeanAttributeInfo info,
            final int minInclusiveValue,
            final int maxInclusiveValue) {

        return new SchemaBasedConfigPropertySupport<Integer>(
                propertySheetOwner, Integer.class, attr, info) {

            @Override
            public Integer getValue() {
                // friendly reminder for now
                if (attr.getValue() instanceof String) {
                    String msg = "The component's configuration schema indicates this attribute is of type 'int'." + // NOI18N
                            " However, the MBean attribute is of type 'string'. Please fix the component."; // NOI18N
                    throw new ClassCastException(msg);
                }
                return super.getValue();
            }

            @Override
            protected boolean validate(Integer val) {
                int value = Integer.parseInt(val.toString());
                if (value < minInclusiveValue || value > maxInclusiveValue) {
                    String errMsg = NbBundle.getMessage(getClass(),
                            "MSG_INVALID_INTEGER", value, // NOI18N
                            minInclusiveValue, maxInclusiveValue);
                    NotifyDescriptor d = new NotifyDescriptor.Message(
                            errMsg, NotifyDescriptor.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notify(d);
                    return false;
                }

                return true;
            }
        };
    }

    private static PropertySupport getBooleanPropertySupport(
            PropertySheetOwner propertySheetOwner,
            Attribute attr,
            MBeanAttributeInfo info) {

        return new SchemaBasedConfigPropertySupport<Boolean>(
                propertySheetOwner, Boolean.class, attr, info);
    }

    private static PropertySupport getSimpleStringPropertySupport(
            final PropertySheetOwner propertySheetOwner,
            final Attribute attr,
            final MBeanAttributeInfo info) {

        return new SchemaBasedConfigPropertySupport<String>(
                propertySheetOwner, String.class, attr, info) {

            @Override
            public PropertyEditor getPropertyEditor() {
                if (info instanceof JBIComponentConfigurationMBeanAttributeInfo &&
                        ((JBIComponentConfigurationMBeanAttributeInfo) info).isEncrypted()) {
                    return new PasswordEditor();
                } else {
                    return super.getPropertyEditor();
                }
            }
        };
    }
}
