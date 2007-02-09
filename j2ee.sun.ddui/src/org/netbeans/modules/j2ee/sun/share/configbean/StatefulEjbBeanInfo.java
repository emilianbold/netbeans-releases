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
package org.netbeans.modules.j2ee.sun.share.configbean;
import java.beans.*;
import org.openide.util.Exceptions;

public class StatefulEjbBeanInfo extends SimpleBeanInfo { // BaseEjbBeanInfo implements Constants {

    /**
     * Gets the bean's <code>BeanDescriptor</code>s.
     *
     * @return BeanDescriptor describing the editable
     * properties of this bean.  May return null if the
     * information should be obtained by automatic analysis.
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor beanDescriptor = new BeanDescriptor  ( StatefulEjb.class , org.netbeans.modules.j2ee.sun.share.configbean.customizers.ejbmodule.StatefulEjbCustomizer.class );//GEN-HEADEREND:BeanDescriptor
        return beanDescriptor;
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
        int PROPERTY_beanCache = 0;
        int PROPERTY_DDBean = 1;
        int PROPERTY_iorSecurityConfig = 2;
        int PROPERTY_jndiName = 3;
        int PROPERTY_parent = 4;
        int PROPERTY_passByReference = 5;
        int PROPERTY_principalName = 6;
        int PROPERTY_webserviceEndpoint = 7;
        int PROPERTY_xpaths = 8;
        PropertyDescriptor[] properties = new PropertyDescriptor[9];
        
        try {
            properties[PROPERTY_beanCache] = new PropertyDescriptor ( "beanCache", StatefulEjb.class, "getBeanCache", "setBeanCache" );
            properties[PROPERTY_beanCache].setPropertyEditorClass ( org.netbeans.modules.j2ee.sun.share.configbean.editors.DummyPropertyEditor.class );
            properties[PROPERTY_DDBean] = new PropertyDescriptor ( "DDBean", StatefulEjb.class, "getDDBean", null );
            properties[PROPERTY_DDBean].setHidden ( true );
            properties[PROPERTY_iorSecurityConfig] = new PropertyDescriptor ( "iorSecurityConfig", StatefulEjb.class, "getIorSecurityConfig", "setIorSecurityConfig" );
            properties[PROPERTY_iorSecurityConfig].setPropertyEditorClass ( org.netbeans.modules.j2ee.sun.share.configbean.editors.DummyPropertyEditor.class );
            properties[PROPERTY_jndiName] = new PropertyDescriptor ( "jndiName", StatefulEjb.class, "getJndiName", "setJndiName" );
            properties[PROPERTY_parent] = new PropertyDescriptor ( "parent", StatefulEjb.class, "getParent", null );
            properties[PROPERTY_parent].setHidden ( true );
            properties[PROPERTY_passByReference] = new PropertyDescriptor ( "passByReference", StatefulEjb.class, "getPassByReference", "setPassByReference" );
            properties[PROPERTY_principalName] = new PropertyDescriptor ( "principalName", StatefulEjb.class, "getPrincipalName", "setPrincipalName" );
            properties[PROPERTY_principalName].setHidden ( true );
            properties[PROPERTY_webserviceEndpoint] = new IndexedPropertyDescriptor ( "webserviceEndpoint", StatefulEjb.class, "getWebserviceEndpoint", "setWebserviceEndpoint", "getWebserviceEndpoint", "setWebserviceEndpoint" );
            properties[PROPERTY_webserviceEndpoint].setPropertyEditorClass ( org.netbeans.modules.j2ee.sun.share.configbean.editors.DummyPropertyEditor.class );
            properties[PROPERTY_xpaths] = new PropertyDescriptor ( "xpaths", StatefulEjb.class, "getXpaths", null );
            properties[PROPERTY_xpaths].setHidden ( true );
        } catch( IntrospectionException e) {
            Exceptions.printStackTrace(e);
        }
        return properties;    
    }
    
    /**
     * Gets the bean's <code>EventSetDescriptor</code>s.
     *
     * @return  An array of EventSetDescriptors describing the kinds of
     * events fired by this bean.  May return null if the information
     * should be obtained by automatic analysis.
     */
    public EventSetDescriptor[] getEventSetDescriptors() {
        int EVENT_propertyChangeListener = 0;
        int EVENT_vetoableChangeListener = 1;

        EventSetDescriptor[] eventSets = new EventSetDescriptor[2];

        try {
            eventSets[EVENT_propertyChangeListener] = new EventSetDescriptor ( org.netbeans.modules.j2ee.sun.share.configbean.StatefulEjb.class, "propertyChangeListener", java.beans.PropertyChangeListener.class, new String[] {"propertyChange"}, "addPropertyChangeListener", "removePropertyChangeListener" );
            eventSets[EVENT_propertyChangeListener].setHidden ( true );
            eventSets[EVENT_vetoableChangeListener] = new EventSetDescriptor ( org.netbeans.modules.j2ee.sun.share.configbean.StatefulEjb.class, "vetoableChangeListener", java.beans.VetoableChangeListener.class, new String[] {"vetoableChange"}, "addVetoableChangeListener", "removeVetoableChangeListener" );
            eventSets[EVENT_vetoableChangeListener].setHidden ( true );
        }
        catch( IntrospectionException e) {
            Exceptions.printStackTrace(e);
        }
        return eventSets;       
    }
    
    /**
     * Gets the bean's <code>MethodDescriptor</code>s.
     *
     * @return  An array of MethodDescriptors describing the methods
     * implemented by this bean.  May return null if the information
     * should be obtained by automatic analysis.
     */
    public MethodDescriptor[] getMethodDescriptors() {
        return new MethodDescriptor[0];
    }
    
    /** Return an appropriate icon (currently, only 16x16 color is available)
     */
    public java.awt.Image getIcon(int iconKind) {
            return loadImage("resources/SessionBean.png");	// NOI18N
    }
}

