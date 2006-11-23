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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.sun.ide.sunresources.beans;

import java.beans.*;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.netbeans.modules.j2ee.sun.ide.editors.BooleanEditor;

public class JMSBeanBeanInfo extends SimpleBeanInfo {

    
    static private String getLabel(String key){
        return NbBundle.getMessage(JMSBean.class,key);
    }
     
    // Property identifiers//GEN-FIRST:Properties
    private static final int PROPERTY_description = 0;
    private static final int PROPERTY_isEnabled = 1;
    private static final int PROPERTY_jndiName = 2;
    private static final int PROPERTY_name = 3;
    private static final int PROPERTY_resType = 4;

    private static final int EVENT_propertyChangeListener = 0;

    /**
     * Gets the bean's <code>BeanDescriptor</code>s.
     *
     * @return BeanDescriptor describing the editable
     * properties of this bean.  May return null if the
     * information should be obtained by automatic analysis.
     */
    public BeanDescriptor getBeanDescriptor() {
        return new BeanDescriptor  ( JMSBean.class , null );
    }
    
    /**
     * Gets the bean's <code>PropertyDescriptor</code>s.
     *
     * @return An array of PropertyDescriptors describing the editable
     * properties supported by this bean.  May return null if the
     * information should be obtained by automatic analysis.
     * <p>
     * If a property is indexed, then its entry in the result array will
     * belong to the IndexedPropertyDescriptor subclass of PropertyDescriptor.
     * A client of getPropertyDescriptors can use "instanceof" to check
     * if a given PropertyDescriptor is an IndexedPropertyDescriptor.
     */
    public PropertyDescriptor[] getPropertyDescriptors() {
        PropertyDescriptor[] properties = new PropertyDescriptor[5];
    
        try {
            properties[PROPERTY_description] = new PropertyDescriptor ( "description", JMSBean.class, "getDescription", "setDescription" );
            properties[PROPERTY_description].setDisplayName ( getLabel("LBL_Description") );
            properties[PROPERTY_description].setShortDescription ( getLabel("DSC_Description") );
            properties[PROPERTY_isEnabled] = new PropertyDescriptor ( "isEnabled", JMSBean.class, "getIsEnabled", "setIsEnabled" );
            properties[PROPERTY_isEnabled].setDisplayName ( getLabel("LBL_Enabled") );
            properties[PROPERTY_isEnabled].setShortDescription ( getLabel("DSC_Enabled") );
            properties[PROPERTY_isEnabled].setPropertyEditorClass ( BooleanEditor.class );
            properties[PROPERTY_jndiName] = new PropertyDescriptor ( "jndiName", JMSBean.class, "getJndiName", "setJndiName" );
            properties[PROPERTY_jndiName].setDisplayName ( getLabel("LBL_JndiName") );
            properties[PROPERTY_jndiName].setShortDescription ( getLabel("DSC_JMSJndiName") );
            properties[PROPERTY_name] = new PropertyDescriptor ( "name", JMSBean.class, "getName", "setName" );
            properties[PROPERTY_name].setHidden ( true );
            properties[PROPERTY_resType] = new PropertyDescriptor ( "resType", JMSBean.class, "getResType", "setResType" );
            properties[PROPERTY_resType].setDisplayName ( getLabel("LBL_JMSResType") );
            properties[PROPERTY_resType].setShortDescription ( getLabel("DSC_JMSResType") );
        }
        catch( IntrospectionException e) {
            Exceptions.printStackTrace(e);
        }
        
        return properties;
    }
    
    public EventSetDescriptor[] getEventSetDescriptors() {
        EventSetDescriptor[] eventSets = new EventSetDescriptor[1];
    
            try {
            eventSets[EVENT_propertyChangeListener] = new EventSetDescriptor ( JMSBean.class, "propertyChangeListener", java.beans.PropertyChangeListener.class, new String[] {"propertyChange"}, "addPropertyChangeListener", "removePropertyChangeListener" );
        }
        catch( IntrospectionException e) {
            Exceptions.printStackTrace(e);
        }
        
        // Here you can add code for customizing the event sets array.
        
        return eventSets;
    }
    
    public MethodDescriptor[] getMethodDescriptors() {
        return new MethodDescriptor[0];
    }
    
}

