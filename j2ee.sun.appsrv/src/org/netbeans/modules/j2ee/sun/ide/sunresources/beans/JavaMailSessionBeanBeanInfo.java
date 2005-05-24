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
package org.netbeans.modules.j2ee.sun.ide.sunresources.beans;

import java.beans.*;
import org.openide.util.NbBundle;
import org.netbeans.modules.j2ee.sun.ide.editors.BooleanEditor;

public class JavaMailSessionBeanBeanInfo extends SimpleBeanInfo {
    
    // Bean descriptor//GEN-FIRST:BeanDescriptor
    /*lazy BeanDescriptor*/
    private static BeanDescriptor getBdescriptor(){
        BeanDescriptor beanDescriptor = new BeanDescriptor  ( JavaMailSessionBean.class , null );//GEN-HEADEREND:BeanDescriptor
        
        // Here you can add code for customizing the BeanDescriptor.
        
        return beanDescriptor;         }//GEN-LAST:BeanDescriptor
     
    static private String getLabel(String key){
        return NbBundle.getMessage(JavaMailSessionBean.class,key);
    }

    
    // Property identifiers//GEN-FIRST:Properties
    private static final int PROPERTY_description = 0;
    private static final int PROPERTY_fromAddr = 1;
    private static final int PROPERTY_hostName = 2;
    private static final int PROPERTY_isDebug = 3;
    private static final int PROPERTY_isEnabled = 4;
    private static final int PROPERTY_jndiName = 5;
    private static final int PROPERTY_name = 6;
    private static final int PROPERTY_storeProt = 7;
    private static final int PROPERTY_storeProtClass = 8;
    private static final int PROPERTY_transProt = 9;
    private static final int PROPERTY_transProtClass = 10;
    private static final int PROPERTY_userName = 11;

    // Property array 
    /*lazy PropertyDescriptor*/
    private static PropertyDescriptor[] getPdescriptor(){
        PropertyDescriptor[] properties = new PropertyDescriptor[12];
    
        try {
            properties[PROPERTY_description] = new PropertyDescriptor ( "description", JavaMailSessionBean.class, "getDescription", "setDescription" );
            properties[PROPERTY_description].setDisplayName ( getLabel("LBL_Description") );
            properties[PROPERTY_description].setShortDescription ( getLabel("DSC_Description") );
            properties[PROPERTY_fromAddr] = new PropertyDescriptor ( "fromAddr", JavaMailSessionBean.class, "getFromAddr", "setFromAddr" );
            properties[PROPERTY_fromAddr].setDisplayName ( getLabel("LBL_from") );
            properties[PROPERTY_fromAddr].setShortDescription ( getLabel("DSC_from") );
            properties[PROPERTY_hostName] = new PropertyDescriptor ( "hostName", JavaMailSessionBean.class, "getHostName", "setHostName" );
            properties[PROPERTY_hostName].setDisplayName ( getLabel("LBL_host") );
            properties[PROPERTY_hostName].setShortDescription ( getLabel("DSC_host") );
            properties[PROPERTY_isDebug] = new PropertyDescriptor ( "isDebug", JavaMailSessionBean.class, "getIsDebug", "setIsDebug" );
            properties[PROPERTY_isDebug].setDisplayName ( getLabel("LBL_debug") );
            properties[PROPERTY_isDebug].setShortDescription ( getLabel("DSC_debug") );
            properties[PROPERTY_isDebug].setPropertyEditorClass ( BooleanEditor.class );
            properties[PROPERTY_isEnabled] = new PropertyDescriptor ( "isEnabled", JavaMailSessionBean.class, "getIsEnabled", "setIsEnabled" );
            properties[PROPERTY_isEnabled].setDisplayName ( getLabel("LBL_Enabled") );
            properties[PROPERTY_isEnabled].setShortDescription ( getLabel("DSC_Enabled") );
            properties[PROPERTY_isEnabled].setPropertyEditorClass ( BooleanEditor.class );
            properties[PROPERTY_jndiName] = new PropertyDescriptor ( "jndiName", JavaMailSessionBean.class, "getJndiName", "setJndiName" );
            properties[PROPERTY_jndiName].setDisplayName ( getLabel("LBL_JndiName") );
            properties[PROPERTY_jndiName].setShortDescription ( getLabel("DSC_MailJndiName") );
            properties[PROPERTY_name] = new PropertyDescriptor ( "name", JavaMailSessionBean.class, "getName", "setName" );
            properties[PROPERTY_name].setHidden ( true );
            properties[PROPERTY_storeProt] = new PropertyDescriptor ( "storeProt", JavaMailSessionBean.class, "getStoreProt", "setStoreProt" );
            properties[PROPERTY_storeProt].setDisplayName ( getLabel("LBL_StoreProtocol") );
            properties[PROPERTY_storeProt].setShortDescription ( getLabel("DSC_StoreProtocol") );
            properties[PROPERTY_storeProtClass] = new PropertyDescriptor ( "storeProtClass", JavaMailSessionBean.class, "getStoreProtClass", "setStoreProtClass" );
            properties[PROPERTY_storeProtClass].setDisplayName ( getLabel("LBL_StoreProtocolClass") );
            properties[PROPERTY_storeProtClass].setShortDescription ( getLabel("DSC_StoreProtocolClass") );
            properties[PROPERTY_transProt] = new PropertyDescriptor ( "transProt", JavaMailSessionBean.class, "getTransProt", "setTransProt" );
            properties[PROPERTY_transProt].setDisplayName ( getLabel("LBL_TransportProtocol") );
            properties[PROPERTY_transProt].setShortDescription ( getLabel("DSC_TransportProtocol") );
            properties[PROPERTY_transProtClass] = new PropertyDescriptor ( "transProtClass", JavaMailSessionBean.class, "getTransProtClass", "setTransProtClass" );
            properties[PROPERTY_transProtClass].setDisplayName ( getLabel("LBL_TransportProtocol") );
            properties[PROPERTY_transProtClass].setShortDescription ( getLabel("DSC_TransportProtocol") );
            properties[PROPERTY_userName] = new PropertyDescriptor ( "userName", JavaMailSessionBean.class, "getUserName", "setUserName" );
            properties[PROPERTY_userName].setDisplayName ( getLabel("LBL_user") );
            properties[PROPERTY_userName].setShortDescription ( getLabel("DSC_user") );
        }
        catch( IntrospectionException e) {}//GEN-HEADEREND:Properties
        
        // Here you can add code for customizing the properties array.
        
        return properties;         }//GEN-LAST:Properties
    
    // EventSet identifiers//GEN-FIRST:Events
    private static final int EVENT_propertyChangeListener = 0;

    // EventSet array
    /*lazy EventSetDescriptor*/
    private static EventSetDescriptor[] getEdescriptor(){
        EventSetDescriptor[] eventSets = new EventSetDescriptor[1];
    
            try {
            eventSets[EVENT_propertyChangeListener] = new EventSetDescriptor ( org.netbeans.modules.j2ee.sun.ide.sunresources.beans.JavaMailSessionBean.class, "propertyChangeListener", java.beans.PropertyChangeListener.class, new String[] {"propertyChange"}, "addPropertyChangeListener", "removePropertyChangeListener" );
        }
        catch( IntrospectionException e) {}//GEN-HEADEREND:Events
        
        // Here you can add code for customizing the event sets array.
        
        return eventSets;         }//GEN-LAST:Events
    
    // Method identifiers//GEN-FIRST:Methods

    // Method array 
    /*lazy MethodDescriptor*/
    private static MethodDescriptor[] getMdescriptor(){
        MethodDescriptor[] methods = new MethodDescriptor[0];//GEN-HEADEREND:Methods
        
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

