/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.tax.beans.beaninfo;

import java.beans.*;

import org.netbeans.tax.TreeEntityDecl;
import org.netbeans.modules.xml.tax.beans.customizer.TreeEntityDeclCustomizer;
import org.netbeans.modules.xml.tax.beans.editor.NullStringEditor;

/**
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public class TreeEntityDeclBeanInfo extends SimpleBeanInfo {

    // Bean descriptor //GEN-FIRST:BeanDescriptor
    private static BeanDescriptor beanDescriptor = new BeanDescriptor  ( TreeEntityDecl.class , TreeEntityDeclCustomizer.class );

    static {//GEN-HEADEREND:BeanDescriptor

	// Here you can add code for customizing the BeanDescriptor.

    }//GEN-LAST:BeanDescriptor

    // Property identifiers //GEN-FIRST:Properties
    private static final int PROPERTY_name = 0;
    private static final int PROPERTY_parameter = 1;
    private static final int PROPERTY_notationName = 2;
    private static final int PROPERTY_systemId = 3;
    private static final int PROPERTY_readOnly = 4;
    private static final int PROPERTY_internalText = 5;
    private static final int PROPERTY_publicId = 6;
//      private static final int PROPERTY_type = 7;

    // Property array 
    private static PropertyDescriptor[] properties = new PropertyDescriptor[7];//8];

    static {
        try {
            properties[PROPERTY_name] = new PropertyDescriptor ( "name", TreeEntityDecl.class, "getName", null ); // NOI18N
            properties[PROPERTY_name].setDisplayName ( Util.getString ( "PROP_TreeEntityDeclBeanInfo_name" ) );
            properties[PROPERTY_name].setShortDescription ( Util.getString ( "HINT_TreeEntityDeclBeanInfo_name" ) );
            properties[PROPERTY_parameter] = new PropertyDescriptor ( "parameter", TreeEntityDecl.class, "isParameter", null ); // NOI18N
            properties[PROPERTY_parameter].setDisplayName ( Util.getString ( "PROP_TreeEntityDeclBeanInfo_parameter" ) );
            properties[PROPERTY_parameter].setShortDescription ( Util.getString ( "HINT_TreeEntityDeclBeanInfo_parameter" ) );
            properties[PROPERTY_notationName] = new PropertyDescriptor ( "notationName", TreeEntityDecl.class, "getNotationName", null ); // NOI18N
            properties[PROPERTY_notationName].setDisplayName ( Util.getString ( "PROP_TreeEntityDeclBeanInfo_notationName" ) );
            properties[PROPERTY_notationName].setShortDescription ( Util.getString ( "HINT_TreeEntityDeclBeanInfo_notationName" ) );
            properties[PROPERTY_notationName].setPropertyEditorClass ( NullStringEditor.class );
            properties[PROPERTY_systemId] = new PropertyDescriptor ( "systemId", TreeEntityDecl.class, "getSystemId", null ); // NOI18N
            properties[PROPERTY_systemId].setDisplayName ( Util.getString ( "PROP_TreeEntityDeclBeanInfo_systemId" ) );
            properties[PROPERTY_systemId].setShortDescription ( Util.getString ( "HINT_TreeEntityDeclBeanInfo_systemId" ) );
            properties[PROPERTY_systemId].setPropertyEditorClass ( NullStringEditor.class );
            properties[PROPERTY_readOnly] = new PropertyDescriptor ( "readOnly", TreeEntityDecl.class, "isReadOnly", null ); // NOI18N
            properties[PROPERTY_readOnly].setDisplayName ( Util.getString ( "PROP_TreeEntityDeclBeanInfo_readOnly" ) );
            properties[PROPERTY_readOnly].setShortDescription ( Util.getString ( "HINT_TreeEntityDeclBeanInfo_readOnly" ) );
            properties[PROPERTY_readOnly].setExpert ( true );
            properties[PROPERTY_internalText] = new PropertyDescriptor ( "internalText", TreeEntityDecl.class, "getInternalText", null ); // NOI18N
            properties[PROPERTY_internalText].setDisplayName ( Util.getString ( "PROP_TreeEntityDeclBeanInfo_internalText" ) );
            properties[PROPERTY_internalText].setShortDescription ( Util.getString ( "HINT_TreeEntityDeclBeanInfo_internalText" ) );
            properties[PROPERTY_internalText].setPropertyEditorClass ( NullStringEditor.class );
            properties[PROPERTY_publicId] = new PropertyDescriptor ( "publicId", TreeEntityDecl.class, "getPublicId", null ); // NOI18N
            properties[PROPERTY_publicId].setDisplayName ( Util.getString ( "PROP_TreeEntityDeclBeanInfo_publicId" ) );
            properties[PROPERTY_publicId].setShortDescription ( Util.getString ( "HINT_TreeEntityDeclBeanInfo_publicId" ) );
            properties[PROPERTY_publicId].setPropertyEditorClass ( NullStringEditor.class );

//              properties[PROPERTY_type] = new PropertyDescriptor ( "type", TreeEntityDecl.class, "getType", null ); // NOI18N
//              properties[PROPERTY_type].setDisplayName ( Util.getString ( "PROP_TreeEntityDeclBeanInfo_type" ) );
//              properties[PROPERTY_type].setShortDescription ( Util.getString ( "HINT_TreeEntityDeclBeanInfo_type" ) );
        }
        catch( IntrospectionException e) {}//GEN-HEADEREND:Properties

	// Here you can add code for customizing the properties array.

    }//GEN-LAST:Properties

    // EventSet identifiers//GEN-FIRST:Events
    private static final int EVENT_propertyChangeListener = 0;

    // EventSet array
    private static EventSetDescriptor[] eventSets = new EventSetDescriptor[1];

    static {
        try {
            eventSets[EVENT_propertyChangeListener] = new EventSetDescriptor ( org.netbeans.tax.TreeEntityDecl.class, "propertyChangeListener", java.beans.PropertyChangeListener.class, new String[] {"propertyChange"}, "addPropertyChangeListener", "removePropertyChangeListener" ); // NOI18N
        }
        catch( IntrospectionException e) {}//GEN-HEADEREND:Events

        // Here you can add code for customizing the event sets array.

    }//GEN-LAST:Events

    // Method identifiers //GEN-FIRST:Methods

    // Method array 
    private static MethodDescriptor[] methods = new MethodDescriptor[0];
    //GEN-HEADEREND:Methods

    // Here you can add code for customizing the methods array.
    
    //GEN-LAST:Methods

    private static final int defaultPropertyIndex = -1;//GEN-BEGIN:Idx
    private static final int defaultEventIndex = -1;//GEN-END:Idx


    /**
     * Gets the bean's <code>BeanDescriptor</code>s.
     * 
     * @return BeanDescriptor describing the editable
     * properties of this bean.  May return null if the
     * information should be obtained by automatic analysis.
     */
    public BeanDescriptor getBeanDescriptor() {
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
        return methods;
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
