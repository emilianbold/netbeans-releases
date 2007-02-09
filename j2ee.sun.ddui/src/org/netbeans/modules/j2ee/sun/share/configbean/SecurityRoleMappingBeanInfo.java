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

public class SecurityRoleMappingBeanInfo extends SimpleBeanInfo {
	
	/** Return an appropriate icon (currently, only 16x16 color is available)
	 */
	public java.awt.Image getIcon(int iconKind) {
		return loadImage("resources/SecurityRoleMappingIcon16.gif");	// NOI18N
	}
	
        /**
         * Gets the bean's <code>BeanDescriptor</code>s.
         *
         * @return BeanDescriptor describing the editable
         * properties of this bean.  May return null if the
         * information should be obtained by automatic analysis.
         */
        public BeanDescriptor getBeanDescriptor() {
            BeanDescriptor beanDescriptor = new BeanDescriptor  ( SecurityRoleMapping.class , org.netbeans.modules.j2ee.sun.share.configbean.customizers.SecurityRoleMappingCustomizer.class );//GEN-HEADEREND:BeanDescriptor
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
            int PROPERTY_groupName = 0;
            int PROPERTY_identity = 1;
            int PROPERTY_principalName = 2;
            int PROPERTY_roleName = 3;
            PropertyDescriptor[] properties = new PropertyDescriptor[4];

            try {
                properties[PROPERTY_groupName] = new IndexedPropertyDescriptor ( "groupName", SecurityRoleMapping.class, null, null, "getGroupName", null );
                properties[PROPERTY_groupName].setBound ( true );
                properties[PROPERTY_groupName].setConstrained ( true );
                properties[PROPERTY_identity] = new PropertyDescriptor ( "identity", SecurityRoleMapping.class, "getIdentity", null );
                properties[PROPERTY_identity].setBound ( true );
                properties[PROPERTY_identity].setConstrained ( true );
                properties[PROPERTY_principalName] = new IndexedPropertyDescriptor ( "principalName", SecurityRoleMapping.class, null, null, "getPrincipalName", null );
                properties[PROPERTY_principalName].setBound ( true );
                properties[PROPERTY_principalName].setConstrained ( true );
                properties[PROPERTY_roleName] = new PropertyDescriptor ( "roleName", SecurityRoleMapping.class, "getRoleName", null );
                properties[PROPERTY_roleName].setBound ( true );
                properties[PROPERTY_roleName].setConstrained ( true );
            }
            catch( IntrospectionException e) {
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
                eventSets[EVENT_propertyChangeListener] = new EventSetDescriptor ( org.netbeans.modules.j2ee.sun.share.configbean.SecurityRoleMapping.class, "propertyChangeListener", java.beans.PropertyChangeListener.class, new String[] {"propertyChange"}, "addPropertyChangeListener", "removePropertyChangeListener" );
                eventSets[EVENT_vetoableChangeListener] = new EventSetDescriptor ( org.netbeans.modules.j2ee.sun.share.configbean.SecurityRoleMapping.class, "vetoableChangeListener", java.beans.VetoableChangeListener.class, new String[] {"vetoableChange"}, "addVetoableChangeListener", "removeVetoableChangeListener" );
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
	
}

