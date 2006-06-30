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

package org.netbeans.modules.schema2beans;

import java.beans.*;
import java.util.*;
import java.lang.reflect.*;

public class DDBeanInfo extends SimpleBeanInfo {

    private static final String BEANINFO = "BeanInfo";	// NOI18N

    private static PropertyDescriptor[] properties = null; //GEN-FIRST:Properties

    // Here you can add code for customizing the properties array.

    //GEN-LAST:Properties

    private static EventSetDescriptor[] eventSets = null; //GEN-FIRST:Events

    // Here you can add code for customizing the event sets array.
    
    //GEN-LAST:Events
    
    private static java.awt.Image iconColor16 = null; //GEN-BEGIN:IconsDef
    private static java.awt.Image iconColor32 = null;
    private static java.awt.Image iconMono16 = null;
    private static java.awt.Image iconMono32 = null; //GEN-END:IconsDef
    private static String iconNameC16 = null; //GEN-BEGIN:Icons
    private static String iconNameC32 = null;
    private static String iconNameM16 = null;
    private static String iconNameM32 = null; //GEN-END:Icons
    
    private static int defaultPropertyIndex = -1; //GEN-BEGIN:Idx
    private static int defaultEventIndex = -1; //GEN-END:Idx
    private static boolean propertiesInited = false;
    
    private void initProperties() {
	ArrayList 	al = new ArrayList();
	String    	classname = null;
	BeanInfo	bi;

	try {
	    classname = this.getClass().getName();
	    if (!classname.endsWith(BEANINFO)) {
		return;
	    }
	    classname = classname.substring(0,(classname.length() - 
					       BEANINFO.length()));
	    bi = Introspector.getBeanInfo(Class.forName(classname));
	} catch (ClassNotFoundException e) {
	    System.err.println("Class name = " + classname);	// NOI18N
	    return;
	} catch (IntrospectionException e) {
	    Thread.dumpStack();
	    return;
	}

	PropertyDescriptor[] pd = bi.getPropertyDescriptors();
	Method m = null;
	for (int i=0;i<pd.length; i++) {
	    Class c = null;
	    if (pd[i] instanceof IndexedPropertyDescriptor) {
		IndexedPropertyDescriptor ipd = 
		    (IndexedPropertyDescriptor)pd[i];
		c = ipd.getIndexedPropertyType();
	    } else {
		c = pd[i].getPropertyType();
	    }
	    // Check for the following:
	    // 1: Does the metohd have a return type that implements 
	    //	the DDNode interface?
	    // 2: Is it a class in java.lang? but not getClass 
	    //	which is inherited from Object
	    // 3: Is it a primitive java type? This would have no "." 
	    //	chars in it.
	    if (c != null) {
		if (BaseBean.class.isAssignableFrom(c) ||
		    (c.getName().startsWith("java.lang.") 
		     && !c.getName().equals("java.lang.Class")) // NOI18N
		    || (c.getName().indexOf(".") < 0)) {	// NOI18N

		    al.add(pd[i]);
		}
	    }
	}
	properties = (PropertyDescriptor[])al.toArray(new PropertyDescriptor[al.size()]);
	
    }
    
    
    /**
     * Gets the beans <code>PropertyDescriptor</code>s.
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
	if (!propertiesInited) {
	    // Avoid recursion here
	    propertiesInited = true;
	    initProperties();
	}
	return properties;
    }
    
    /**
     * Gets the beans <code>EventSetDescriptor</code>s.
     *
     * @return  An array of EventSetDescriptors describing the kinds of
     * events fired by this bean.  May return null if the information
     * should be obtained by automatic analysis.
     */
    public EventSetDescriptor[] getEventSetDescriptors() {
	return eventSets;
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
	return defaultPropertyIndex;
    }
    
    /**
     * This method returns an image object that can be used to
     * represent the bean in toolboxes, toolbars, etc.   Icon images
     * will typically be GIFs, but may in future include other formats.
     * <p>
     * Beans aren't required to provide icons and may return null from
     * this method.
     * <p>
     * There are four possible flavors of icons (16x16 color,
     * 32x32 color, 16x16 mono, 32x32 mono).  If a bean choses to only
     * support a single icon we recommend supporting 16x16 color.
     * <p>
     * We recommend that icons have a "transparent" background
     * so they can be rendered onto an existing background.
     *
     * @param  iconKind  The kind of icon requested.  This should be
     *    one of the constant values ICON_COLOR_16x16, ICON_COLOR_32x32,
     *    ICON_MONO_16x16, or ICON_MONO_32x32.
     * @return  An image object representing the requested icon.  May
     *    return null if no suitable icon is available.
     */
    public java.awt.Image getIcon(int iconKind) {
	switch ( iconKind ) {
	    case ICON_COLOR_16x16:
		if ( iconNameC16 == null )
		    return null;
		else {
		    if( iconColor16 == null )
			iconColor16 = loadImage( iconNameC16 );
		    return iconColor16;
		}
	    case ICON_COLOR_32x32:
		if ( iconNameC32 == null )
		    return null;
		else {
		    if( iconColor32 == null )
			iconColor32 = loadImage( iconNameC32 );
		    return iconColor32;
		}
	    case ICON_MONO_16x16:
		if ( iconNameM16 == null )
		    return null;
		else {
		    if( iconMono16 == null )
			iconMono16 = loadImage( iconNameM16 );
		    return iconMono16;
		}
	    case ICON_MONO_32x32:
		if ( iconNameM32 == null )
		    return null;
		else {
		    if( iconNameM32 == null )
			iconMono32 = loadImage( iconNameM32 );
		    return iconMono32;
		}
	}
	return null;
    }
    
}
