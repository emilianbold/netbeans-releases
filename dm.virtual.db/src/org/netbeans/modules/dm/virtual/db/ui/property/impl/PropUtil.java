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

package org.netbeans.modules.dm.virtual.db.ui.property.impl;

import java.beans.PropertyEditor;
import java.util.Map;

import org.netbeans.modules.dm.virtual.db.ui.property.INode;
import org.netbeans.modules.dm.virtual.db.ui.property.IProperty;
import org.netbeans.modules.dm.virtual.db.ui.property.IPropertyCustomizer;
import org.openide.nodes.Node;


/**
 * @author Ritesh Adval
 */
public class PropUtil {

    public static void setInitialPropertyValues(Map map, Map customizerMap, PropertyNode pNode) {
        Node.PropertySet[] pSets = pNode.getPropertySets();
        for (int i = 0; i < pSets.length; i++) {
            Node.PropertySet pSet = pSets[i];
            setInitialPropertyValues(map, customizerMap, pSet);
        }
    }

    public static void setInitialPropertyValues(Object bean, Map customizerMap, PropertyNode pNode) {
        Node.PropertySet[] pSets = pNode.getPropertySets();
        for (int i = 0; i < pSets.length; i++) {
            Node.PropertySet pSet = pSets[i];
            setInitialPropertyValues(bean, customizerMap, pSet);
        }
    }

    public static void setModifiedPropertyValues(Object bean, PropertyNode pNode) {
        Node.PropertySet[] pSets = pNode.getPropertySets();
        for (int i = 0; i < pSets.length; i++) {
            Node.PropertySet pSet = pSets[i];
            setModifiedPropertyValues(bean, pSet);
        }
    }

    @SuppressWarnings("unchecked")
    private static void setInitialPropertyValues(Map map, Map customizerMap, Node.PropertySet pSet) {
        Node.Property[] properties = pSet.getProperties();
        for (int i = 0; i < properties.length; i++) {
            Node.Property property = properties[i];
            Object value = map.get(property.getName());
            if (value != null) {
                try {
                    IProperty p = (IProperty) property;
                    setPropertyCustomizer(customizerMap, p);
                    property.setValue(value);
                } catch (Exception ex) {
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void setInitialPropertyValues(Object bean, Map customizerMap, Node.PropertySet pSet) {
        Node.Property[] properties = pSet.getProperties();
        for (int i = 0; i < properties.length; i++) {
            Node.Property property = properties[i];
            try {
                IProperty p = (IProperty) property;
                setPropertyCustomizer(customizerMap, p);
                property.setValue(TemplateFactory.invokeGetter(bean, property.getName(), null, null));

                // check if property has a custom editor if so get it from the bean
                if (p.isCustomEditor()) {
                    p.setPropertyEditor((PropertyEditor) TemplateFactory.invokeGetter(bean, "customEditor", new Class[] { Node.Property.class},
                        new Object[] { property}));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private static void setModifiedPropertyValues(Object bean, Node.PropertySet pSet) {
        Node.Property[] properties = pSet.getProperties();
        for (int i = 0; i < properties.length; i++) {
            Node.Property property = properties[i];
            IProperty p = (IProperty) property;
            if (p.isReadOnly()) {
                continue;
            }

            try {
                // if value is differnt then only set it
                Object oldVal = TemplateFactory.invokeGetter(bean, property.getName(), null, null);
                if (oldVal != null && oldVal.equals(property.getValue())) {
                    continue;
                }

                TemplateFactory.invokeSetter(bean, property.getName(), property.getValue());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    
    private static void setPropertyCustomizer(Map customizerMap, IProperty property) {
        if (customizerMap == null) {
            return;
        }

        IPropertyCustomizer pCustomizer = (IPropertyCustomizer) customizerMap.get(property.getName());
        if (pCustomizer != null) {
            String gName = pCustomizer.getPropertyGroupName();
            INode pgNode = property.getParent();
            if (gName != null && pgNode != null && gName.equals(pgNode.getName())) {
                property.setPropertyCustomizer(pCustomizer);
            }
        }
    }

    private PropUtil() {
    }
}

