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
/*
 * WebServiceDescriptorBeanInfo.java
 *
 * Created on October 5, 2005, 4:28 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean;

import java.beans.*;
import org.openide.util.Exceptions;

/**
 * @author Peter Williams
 */
public class WebServiceDescriptorBeanInfo extends SimpleBeanInfo {
    
	/** Return an appropriate icon (currently, only 16x16 color is available)
	 */
	public java.awt.Image getIcon(int iconKind) {
		return loadImage("resources/WebServiceDescriptorIcon16.png");	// NOI18N
	}
    
    /**
     * Gets the bean's <code>BeanDescriptor</code>s.
     *
     * @return BeanDescriptor describing the editable
     * properties of this bean.  May return null if the
     * information should be obtained by automatic analysis.
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor beanDescriptor = new BeanDescriptor  ( org.netbeans.modules.j2ee.sun.share.configbean.WebServiceDescriptor.class , org.netbeans.modules.j2ee.sun.share.configbean.customizers.webservice.WebServiceDescriptorCustomizer.class );
        beanDescriptor.setDisplayName ( "WebServiceDescriptorDisplayName" );
        beanDescriptor.setShortDescription ( "WebServiceDescriptorShortDescription" );//GEN-HEADEREND:BeanDescriptor
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
        int PROPERTY_displayName = 0;
        int PROPERTY_helpId = 1;
        int PROPERTY_identity = 2;
        int PROPERTY_webServiceDescriptionName = 3;
        int PROPERTY_webServiceEndpoint = 4;
        int PROPERTY_webServiceEndpoints = 5;
        int PROPERTY_wsdlPublishLocation = 6;
        PropertyDescriptor[] properties = new PropertyDescriptor[7];
    
        try {
            properties[PROPERTY_displayName] = new PropertyDescriptor ( "displayName", org.netbeans.modules.j2ee.sun.share.configbean.WebServiceDescriptor.class, "getDisplayName", null );
            properties[PROPERTY_helpId] = new PropertyDescriptor ( "helpId", org.netbeans.modules.j2ee.sun.share.configbean.WebServiceDescriptor.class, "getHelpId", null );
            properties[PROPERTY_identity] = new PropertyDescriptor ( "identity", org.netbeans.modules.j2ee.sun.share.configbean.WebServiceDescriptor.class, "getIdentity", "setIdentity" );
            properties[PROPERTY_webServiceDescriptionName] = new PropertyDescriptor ( "webServiceDescriptionName", org.netbeans.modules.j2ee.sun.share.configbean.WebServiceDescriptor.class, "getWebServiceDescriptionName", null );
            properties[PROPERTY_webServiceEndpoint] = new IndexedPropertyDescriptor ( "webServiceEndpoint", org.netbeans.modules.j2ee.sun.share.configbean.WebServiceDescriptor.class, null, null, "getWebServiceEndpoint", null );
            properties[PROPERTY_webServiceEndpoints] = new PropertyDescriptor ( "webServiceEndpoints", org.netbeans.modules.j2ee.sun.share.configbean.WebServiceDescriptor.class, "getWebServiceEndpoints", "setWebServiceEndpoints" );
            properties[PROPERTY_wsdlPublishLocation] = new PropertyDescriptor ( "wsdlPublishLocation", org.netbeans.modules.j2ee.sun.share.configbean.WebServiceDescriptor.class, "getWsdlPublishLocation", "setWsdlPublishLocation" );
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
        return new EventSetDescriptor[0];
    }
    
    /**
     * Gets the bean's <code>MethodDescriptor</code>s.
     *
     * @return  An array of MethodDescriptors describing the methods
     * implemented by this bean.  May return null if the information
     * should be obtained by automatic analysis.
     */
    public MethodDescriptor[] getMethodDescriptors() {
        int METHOD_addWebServiceEndpoint0 = 0;
        int METHOD_removeWebServiceEndpoint1 = 1;
        int METHOD_setDirty2 = 2;
        MethodDescriptor[] methods = new MethodDescriptor[3];
    
        try {
            methods[METHOD_addWebServiceEndpoint0] = new MethodDescriptor ( org.netbeans.modules.j2ee.sun.share.configbean.WebServiceDescriptor.class.getMethod("addWebServiceEndpoint", new Class[] {org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceEndpoint.class}));
            methods[METHOD_addWebServiceEndpoint0].setDisplayName ( "" );
            methods[METHOD_removeWebServiceEndpoint1] = new MethodDescriptor ( org.netbeans.modules.j2ee.sun.share.configbean.WebServiceDescriptor.class.getMethod("removeWebServiceEndpoint", new Class[] {org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceEndpoint.class}));
            methods[METHOD_removeWebServiceEndpoint1].setDisplayName ( "" );
            methods[METHOD_setDirty2] = new MethodDescriptor ( org.netbeans.modules.j2ee.sun.share.configbean.WebServiceDescriptor.class.getMethod("setDirty", new Class[] {}));
            methods[METHOD_setDirty2].setDisplayName ( "" );
        }
        catch( Exception e) {
            Exceptions.printStackTrace(e);
        }
        return methods;
    }
}

