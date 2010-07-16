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
package org.netbeans.modules.xslt.tmap.nodes.properties;

import java.lang.reflect.Method;
import org.netbeans.modules.soa.ui.nodes.InstanceRef;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;
import org.netbeans.modules.xslt.tmap.model.api.TMapComponent;
import org.netbeans.modules.xslt.tmap.nodes.DecoratedTMapComponent;
import org.netbeans.modules.xslt.tmap.nodes.TMapComponentNode;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class PropertyUtils extends org.netbeans.modules.soa.ui.properties.PropertyUtils {
    
    private static final PropertyUtils INSTANCE = new PropertyUtils();
    
    public static PropertyUtils getInstance() {
        return INSTANCE;
    }
    
    private PropertyUtils() {
    }
    
    public PropertyUtils.Reflection getReflection(InstanceRef instanceRef, Class valueType, Method getter, Method setter, Method remover) {
        return new PropertyUtils.Reflection(instanceRef, valueType, getter, setter, remover);
    }

    public PropertyUtils.Reflection getReflection(InstanceRef instanceRef, Class valueType, String getterName, String setterName, String removerName) throws NoSuchMethodException {
        return new PropertyUtils.Reflection(instanceRef, valueType, getterName, setterName, removerName);
    }

    /**
     * Support for properties which take the instance from an
     * InstanceRef.getReference() and use Java Reflection for looking the methods.
     */
    public static class Reflection extends org.netbeans.modules.soa.ui.properties.PropertyUtils.Reflection {
        
        /** Create a support with method objects specified.
         * The methods must be public.
         * @param instanceRef the InstanceRef to work on
         * @param valueType type of the property
         * @param getter getter method, can be <code>null</code>
         * @param setter setter method, can be <code>null</code>
         * @param remover remover method, can be <code>null</code>
         * @throws IllegalArgumentException if the methods are not public
         */
        public Reflection(InstanceRef instanceRef, Class valueType,
                Method getter, Method setter, Method remover) {
            super(instanceRef, valueType, getter, setter, remover);
        }
        
        /** Create a support with methods specified by name.
         * The instance class will be examined for the named methods.
         * But if the instance class is not public, the nearest public superclass
         * will be used instead, so that the getters and setters remain accessible.
         * @param parentNode the BpelNode to work on
         * @param valueType type of the property
         * @param getter name of getter method, can be <code>null</code>
         * @param setter name of setter method, can be <code>null</code>
         * @param remover remover method, can be <code>null</code>
         * @exception NoSuchMethodException if the getter or setter methods cannot be found
         */
        public Reflection(InstanceRef instanceRef, Class valueType,
                String getterName, String setterName, String removerName)
                throws NoSuchMethodException {
            super(instanceRef, valueType, getterName, setterName, removerName);
        }
        
        /** Create a support based on the property name.
         * The getter and setter methods are constructed by capitalizing the first
         * letter in the name of propety and prefixing it with <code>get</code> and
         * <code>set</code>, respectively.
         *
         * @param instanceRef the InstanceRef to work on
         * @param valueType type of the property
         * @param property name of property
         * @exception NoSuchMethodException if the getter or setter methods cannot be found
         */
        public Reflection(InstanceRef instanceRef, Class valueType, String property)
                throws NoSuchMethodException {
            super(instanceRef, valueType, property);
        }

        @Override
        protected Class findAccessibleClass(Object instance) {
            if (instance instanceof DecoratedTMapComponent) {
                Object component = ((DecoratedTMapComponent)instance).getReference();
                if (component instanceof TMapComponent) {
                    return ((TMapComponent)component).getComponentType();
                }
            } else if (instance instanceof TMapComponent) {
                return ((TMapComponent)instance).getComponentType();
            } 
            return findAccessibleClass(instance.getClass());
        }
        
        @Override
        public DocumentComponent getDocumentComponent() {
            Object ref = myInstanceRef.getReference();
            
            if (ref != null) {
                if (ref instanceof DocumentComponent) {
                    return (DocumentComponent) ref;
                }
                
                if (ref instanceof TMapComponentNode) {
                    TMapComponentNode node = (TMapComponentNode) ref;
                    return node.getComponentRef();
                }
            }
            
            if (myInstanceRef.getAlternativeReference() != null) {
                if (myInstanceRef.getAlternativeReference()
                        instanceof DocumentComponent) {
                    return (DocumentComponent) myInstanceRef.getAlternativeReference();
                }
                
                if (myInstanceRef.getAlternativeReference() instanceof TMapComponentNode) {
                    TMapComponentNode node = (TMapComponentNode) myInstanceRef
                            .getAlternativeReference();
                    
                    if ((node.getReference() != null)
                            && (node.getReference() instanceof DocumentComponent)) {
                        return (DocumentComponent) node.getReference();
                    }
                }
            }
            
            return null;
        }
    }
}
