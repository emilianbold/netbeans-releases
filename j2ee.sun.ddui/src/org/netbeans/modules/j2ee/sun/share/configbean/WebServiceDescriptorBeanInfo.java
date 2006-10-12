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

/**
 * @author Peter Williams
 */
public class WebServiceDescriptorBeanInfo extends SimpleBeanInfo {
    
    // Bean descriptor//GEN-FIRST:BeanDescriptor
    /*lazy BeanDescriptor*/
    private static BeanDescriptor getBdescriptor(){
        BeanDescriptor beanDescriptor = new BeanDescriptor  ( org.netbeans.modules.j2ee.sun.share.configbean.WebServiceDescriptor.class , org.netbeans.modules.j2ee.sun.share.configbean.customizers.webservice.WebServiceDescriptorCustomizer.class );
        beanDescriptor.setDisplayName ( "WebServiceDescriptorDisplayName" );
        beanDescriptor.setShortDescription ( "WebServiceDescriptorShortDescription" );//GEN-HEADEREND:BeanDescriptor
        
        // Here you can add code for customizing the BeanDescriptor.
        
        return beanDescriptor;         }//GEN-LAST:BeanDescriptor
    
    
    // Property identifiers//GEN-FIRST:Properties
    private static final int PROPERTY_displayName = 0;
    private static final int PROPERTY_helpId = 1;
    private static final int PROPERTY_identity = 2;
    private static final int PROPERTY_webServiceDescriptionName = 3;
    private static final int PROPERTY_webServiceEndpoint = 4;
    private static final int PROPERTY_webServiceEndpoints = 5;
    private static final int PROPERTY_wsdlPublishLocation = 6;

    // Property array 
    /*lazy PropertyDescriptor*/
    private static PropertyDescriptor[] getPdescriptor(){
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
        catch( IntrospectionException e) {}//GEN-HEADEREND:Properties
        
        // Here you can add code for customizing the properties array.
        
        return properties;         }//GEN-LAST:Properties
    
    // EventSet identifiers//GEN-FIRST:Events

    // EventSet array
    /*lazy EventSetDescriptor*/
    private static EventSetDescriptor[] getEdescriptor(){
        EventSetDescriptor[] eventSets = new EventSetDescriptor[0];//GEN-HEADEREND:Events
        
        // Here you can add code for customizing the event sets array.
        
        return eventSets;         }//GEN-LAST:Events
    
    // Method identifiers//GEN-FIRST:Methods
    private static final int METHOD_addWebServiceEndpoint0 = 0;
    private static final int METHOD_removeWebServiceEndpoint1 = 1;
    private static final int METHOD_setDirty2 = 2;

    // Method array 
    /*lazy MethodDescriptor*/
    private static MethodDescriptor[] getMdescriptor(){
        MethodDescriptor[] methods = new MethodDescriptor[3];
    
        try {
            methods[METHOD_addWebServiceEndpoint0] = new MethodDescriptor ( org.netbeans.modules.j2ee.sun.share.configbean.WebServiceDescriptor.class.getMethod("addWebServiceEndpoint", new Class[] {org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceEndpoint.class}));
            methods[METHOD_addWebServiceEndpoint0].setDisplayName ( "" );
            methods[METHOD_removeWebServiceEndpoint1] = new MethodDescriptor ( org.netbeans.modules.j2ee.sun.share.configbean.WebServiceDescriptor.class.getMethod("removeWebServiceEndpoint", new Class[] {org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceEndpoint.class}));
            methods[METHOD_removeWebServiceEndpoint1].setDisplayName ( "" );
            methods[METHOD_setDirty2] = new MethodDescriptor ( org.netbeans.modules.j2ee.sun.share.configbean.WebServiceDescriptor.class.getMethod("setDirty", new Class[] {}));
            methods[METHOD_setDirty2].setDisplayName ( "" );
        }
        catch( Exception e) {}//GEN-HEADEREND:Methods
        
        // Here you can add code for customizing the methods array.
        
        return methods;         }//GEN-LAST:Methods
    
    
    private static final int defaultPropertyIndex = -1;//GEN-BEGIN:Idx
    private static final int defaultEventIndex = -1;//GEN-END:Idx
    
    
//GEN-FIRST:Superclass
    
    // Here you can add code for customizing the Superclass BeanInfo.
    
//GEN-LAST:Superclass
    
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
        return getBdescriptor();
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
        return getPdescriptor();
    }
    
    /**
     * Gets the bean's <code>EventSetDescriptor</code>s.
     *
     * @return  An array of EventSetDescriptors describing the kinds of
     * events fired by this bean.  May return null if the information
     * should be obtained by automatic analysis.
     */
    public EventSetDescriptor[] getEventSetDescriptors() {
        return getEdescriptor();
    }
    
    /**
     * Gets the bean's <code>MethodDescriptor</code>s.
     *
     * @return  An array of MethodDescriptors describing the methods
     * implemented by this bean.  May return null if the information
     * should be obtained by automatic analysis.
     */
    public MethodDescriptor[] getMethodDescriptors() {
        return getMdescriptor();
    }
    
    /**
     * A bean may have a "default" property that is the property that will
     * mostly commonly be initially chosen for update by human's who are
     * customizing the bean.
     * @return  Index of default property in the PropertyDescriptor array
     * 		returned by getPropertyDescriptors.
     * <P>	Returns -1 if there is no default property.
     */
    public int getDefaultPropertyIndex() {
        return defaultPropertyIndex;
    }
    
    /**
     * A bean may have a "default" event that is the event that will
     * mostly commonly be used by human's when using the bean.
     * @return Index of default event in the EventSetDescriptor array
     *		returned by getEventSetDescriptors.
     * <P>	Returns -1 if there is no default event.
     */
    public int getDefaultEventIndex() {
        return defaultEventIndex;
    }
}

