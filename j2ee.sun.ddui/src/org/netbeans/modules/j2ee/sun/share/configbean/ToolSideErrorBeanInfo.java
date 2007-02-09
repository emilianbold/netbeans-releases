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

public class ToolSideErrorBeanInfo extends SimpleBeanInfo {
	
	/**
	 * Gets the bean's <code>BeanDescriptor</code>s.
	 *
	 * @return BeanDescriptor describing the editable
	 * properties of this bean.  May return null if the
	 * information should be obtained by automatic analysis.
	 */
	public BeanDescriptor getBeanDescriptor() {
            BeanDescriptor beanDescriptor = new BeanDescriptor  ( ToolSideError.class , null );//GEN-HEADEREND:BeanDescriptor
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
            int PROPERTY_ddbeanText = 0;
            int PROPERTY_ddbeanXpath = 1;
            int PROPERTY_identity = 2;
            int PROPERTY_message = 3;
            
            PropertyDescriptor[] properties = new PropertyDescriptor[4];
            
            try {
                properties[PROPERTY_ddbeanText] = new PropertyDescriptor ( "ddbeanText", ToolSideError.class, "getDdbeanText", null );
                properties[PROPERTY_ddbeanXpath] = new PropertyDescriptor ( "ddbeanXpath", ToolSideError.class, "getDdbeanXpath", null );
                properties[PROPERTY_identity] = new PropertyDescriptor ( "identity", ToolSideError.class, "getIdentity", "setIdentity" );
                properties[PROPERTY_message] = new PropertyDescriptor ( "message", ToolSideError.class, "getMessage", null );
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
                eventSets[EVENT_propertyChangeListener] = new EventSetDescriptor ( org.netbeans.modules.j2ee.sun.share.configbean.ToolSideError.class, "propertyChangeListener", java.beans.PropertyChangeListener.class, new String[] {"propertyChange"}, "addPropertyChangeListener", "removePropertyChangeListener" );
                eventSets[EVENT_propertyChangeListener].setHidden ( true );
                eventSets[EVENT_vetoableChangeListener] = new EventSetDescriptor ( org.netbeans.modules.j2ee.sun.share.configbean.ToolSideError.class, "vetoableChangeListener", java.beans.VetoableChangeListener.class, new String[] {"vetoableChange"}, "addVetoableChangeListener", "removeVetoableChangeListener" );
                eventSets[EVENT_vetoableChangeListener].setHidden ( true );
            } catch( IntrospectionException e) {
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
        int METHOD_getDConfigBean0 = 0;
        int METHOD_notifyDDChange1 = 1;
        int METHOD_removeDConfigBean2 = 2;
        MethodDescriptor[] methods = new MethodDescriptor[3];
    
        try {
            methods[METHOD_getDConfigBean0] = new MethodDescriptor ( org.netbeans.modules.j2ee.sun.share.configbean.ToolSideError.class.getMethod("getDConfigBean", new Class[] {javax.enterprise.deploy.model.DDBeanRoot.class}));
            methods[METHOD_getDConfigBean0].setHidden ( true );
            methods[METHOD_getDConfigBean0].setDisplayName ( "" );
            methods[METHOD_notifyDDChange1] = new MethodDescriptor ( org.netbeans.modules.j2ee.sun.share.configbean.ToolSideError.class.getMethod("notifyDDChange", new Class[] {javax.enterprise.deploy.model.XpathEvent.class}));
            methods[METHOD_notifyDDChange1].setHidden ( true );
            methods[METHOD_notifyDDChange1].setDisplayName ( "" );
            methods[METHOD_removeDConfigBean2] = new MethodDescriptor ( org.netbeans.modules.j2ee.sun.share.configbean.ToolSideError.class.getMethod("removeDConfigBean", new Class[] {javax.enterprise.deploy.spi.DConfigBean.class}));
            methods[METHOD_removeDConfigBean2].setHidden ( true );
            methods[METHOD_removeDConfigBean2].setDisplayName ( "" );
        }
        catch( Exception e) {
            Exceptions.printStackTrace(e);
        }
        return methods;
    }
}

