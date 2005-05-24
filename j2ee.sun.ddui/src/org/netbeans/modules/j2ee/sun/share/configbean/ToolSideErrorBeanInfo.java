/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.sun.share.configbean;

import java.beans.*;

public class ToolSideErrorBeanInfo extends SimpleBeanInfo {
	
    // Bean descriptor//GEN-FIRST:BeanDescriptor
    /*lazy BeanDescriptor*/
    private static BeanDescriptor getBdescriptor(){
        BeanDescriptor beanDescriptor = new BeanDescriptor  ( ToolSideError.class , null );//GEN-HEADEREND:BeanDescriptor
		
		// Here you can add code for customizing the BeanDescriptor.
		
        return beanDescriptor;         }//GEN-LAST:BeanDescriptor
	
	
    // Property identifiers//GEN-FIRST:Properties
    private static final int PROPERTY_ddbeanText = 0;
    private static final int PROPERTY_ddbeanXpath = 1;
    private static final int PROPERTY_identity = 2;
    private static final int PROPERTY_message = 3;

    // Property array 
    /*lazy PropertyDescriptor*/
    private static PropertyDescriptor[] getPdescriptor(){
        PropertyDescriptor[] properties = new PropertyDescriptor[4];
    
        try {
            properties[PROPERTY_ddbeanText] = new PropertyDescriptor ( "ddbeanText", ToolSideError.class, "getDdbeanText", null );
            properties[PROPERTY_ddbeanXpath] = new PropertyDescriptor ( "ddbeanXpath", ToolSideError.class, "getDdbeanXpath", null );
            properties[PROPERTY_identity] = new PropertyDescriptor ( "identity", ToolSideError.class, "getIdentity", "setIdentity" );
            properties[PROPERTY_message] = new PropertyDescriptor ( "message", ToolSideError.class, "getMessage", null );
        }
        catch( IntrospectionException e) {}//GEN-HEADEREND:Properties
		
		// Here you can add code for customizing the properties array.
		
        return properties;         }//GEN-LAST:Properties
	
    // EventSet identifiers//GEN-FIRST:Events
    private static final int EVENT_propertyChangeListener = 0;
    private static final int EVENT_vetoableChangeListener = 1;

    // EventSet array
    /*lazy EventSetDescriptor*/
    private static EventSetDescriptor[] getEdescriptor(){
        EventSetDescriptor[] eventSets = new EventSetDescriptor[2];
    
            try {
            eventSets[EVENT_propertyChangeListener] = new EventSetDescriptor ( org.netbeans.modules.j2ee.sun.share.configbean.ToolSideError.class, "propertyChangeListener", java.beans.PropertyChangeListener.class, new String[] {"propertyChange"}, "addPropertyChangeListener", "removePropertyChangeListener" );
            eventSets[EVENT_propertyChangeListener].setHidden ( true );
            eventSets[EVENT_vetoableChangeListener] = new EventSetDescriptor ( org.netbeans.modules.j2ee.sun.share.configbean.ToolSideError.class, "vetoableChangeListener", java.beans.VetoableChangeListener.class, new String[] {"vetoableChange"}, "addVetoableChangeListener", "removeVetoableChangeListener" );
            eventSets[EVENT_vetoableChangeListener].setHidden ( true );
        }
        catch( IntrospectionException e) {}//GEN-HEADEREND:Events
		
		// Here you can add code for customizing the event sets array.
		
        return eventSets;         }//GEN-LAST:Events
	
    // Method identifiers//GEN-FIRST:Methods
    private static final int METHOD_getDConfigBean0 = 0;
    private static final int METHOD_notifyDDChange1 = 1;
    private static final int METHOD_removeDConfigBean2 = 2;

    // Method array 
    /*lazy MethodDescriptor*/
    private static MethodDescriptor[] getMdescriptor(){
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
        catch( Exception e) {}//GEN-HEADEREND:Methods
		
		// Here you can add code for customizing the methods array.
		
        return methods;         }//GEN-LAST:Methods
	
	
    private static final int defaultPropertyIndex = -1;//GEN-BEGIN:Idx
    private static final int defaultEventIndex = -1;//GEN-END:Idx
	
	
//GEN-FIRST:Superclass
	
	// Here you can add code for customizing the Superclass BeanInfo.
	
//GEN-LAST:Superclass
	
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

