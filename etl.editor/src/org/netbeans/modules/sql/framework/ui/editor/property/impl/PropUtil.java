/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.sql.framework.ui.editor.property.impl;

import java.beans.PropertyEditor;
import java.util.Map;

import org.netbeans.modules.sql.framework.ui.editor.property.INode;
import org.netbeans.modules.sql.framework.ui.editor.property.IProperty;
import org.netbeans.modules.sql.framework.ui.editor.property.IPropertyCustomizer;
import org.openide.nodes.Node;


/**
 * @author Ritesh Adval
 * @version $Revision$
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

    /** Creates a new instance of PropUtil */
    private PropUtil() {
    }
}

