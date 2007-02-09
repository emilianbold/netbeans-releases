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

public class CmpEntityEjbBeanInfo extends SimpleBeanInfo {
	
	/**
	 * Gets the bean's <code>BeanDescriptor</code>s.
	 *
	 * @return BeanDescriptor describing the editable
	 * properties of this bean.  May return null if the
	 * information should be obtained by automatic analysis.
	 */
	public BeanDescriptor getBeanDescriptor() {
            BeanDescriptor beanDescriptor = new BeanDescriptor  ( CmpEntityEjb.class , org.netbeans.modules.j2ee.sun.share.configbean.customizers.ejbmodule.CmpEntityEjbCustomizer.class );//GEN-HEADEREND:BeanDescriptor
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
            int PROPERTY_commitOption = 0;
            int PROPERTY_consistency = 1;
            int PROPERTY_isReadOnlyBean = 2;
            int PROPERTY_jndiName = 3;
            int PROPERTY_passByReference = 4;
            int PROPERTY_principalName = 5;
            int PROPERTY_refreshPeriodInSeconds = 6;
            int PROPERTY_schema = 7;
            int PROPERTY_tableName = 9;
            
            PropertyDescriptor[] properties = new PropertyDescriptor[9];

            try {
                properties[PROPERTY_commitOption] = new PropertyDescriptor ( "commitOption", CmpEntityEjb.class, "getCommitOption", "setCommitOption" );
                properties[PROPERTY_commitOption].setHidden ( true );
                properties[PROPERTY_consistency] = new PropertyDescriptor ( "consistency", CmpEntityEjb.class, "getConsistency", "setConsistency" );
                properties[PROPERTY_consistency].setHidden ( true );
                properties[PROPERTY_isReadOnlyBean] = new PropertyDescriptor ( "isReadOnlyBean", CmpEntityEjb.class, "getIsReadOnlyBean", "setIsReadOnlyBean" );
                properties[PROPERTY_isReadOnlyBean].setHidden ( true );
                properties[PROPERTY_jndiName] = new PropertyDescriptor ( "jndiName", CmpEntityEjb.class, "getJndiName", "setJndiName" );
                properties[PROPERTY_passByReference] = new PropertyDescriptor ( "passByReference", CmpEntityEjb.class, "getPassByReference", "setPassByReference" );
                properties[PROPERTY_principalName] = new PropertyDescriptor ( "principalName", CmpEntityEjb.class, "getPrincipalName", "setPrincipalName" );
                properties[PROPERTY_principalName].setHidden ( true );
                properties[PROPERTY_refreshPeriodInSeconds] = new PropertyDescriptor ( "refreshPeriodInSeconds", CmpEntityEjb.class, "getRefreshPeriodInSeconds", "setRefreshPeriodInSeconds" );
                properties[PROPERTY_refreshPeriodInSeconds].setHidden ( true );
                properties[PROPERTY_schema] = new PropertyDescriptor ( "schema", CmpEntityEjb.class, "getSchema", "setSchema" );
                properties[PROPERTY_schema].setHidden ( true );
                properties[PROPERTY_tableName] = new PropertyDescriptor ( "tableName", CmpEntityEjb.class, "getTableName", "setTableName" );
                properties[PROPERTY_tableName].setHidden ( true );
            } catch( IntrospectionException e) {}//GEN-HEADEREND:Properties
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
            EventSetDescriptor[] eventSets = new EventSetDescriptor[0];//GEN-HEADEREND:Events
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
            return loadImage("resources/EntityBean.png");	// NOI18N
    }
}

