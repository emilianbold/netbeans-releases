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

package org.netbeans.beaninfo;

import java.awt.Image;
import java.beans.*;

import org.netbeans.core.ui.SwingBrowser;
import org.openide.util.NbBundle;

/**
 * Swing HTML Browser support.
 *
 * Factory for built-in Swing HTML browser.
 *
 * @author Radim Kubacki
 */
public class SwingBrowserBeanInfo extends SimpleBeanInfo {


    // Bean descriptor 
    private static BeanDescriptor beanDescriptor = null /*lazy*/; 

    private static BeanDescriptor getBdescriptor(){
        if(beanDescriptor == null){
            beanDescriptor = new BeanDescriptor  (SwingBrowser.class);
            beanDescriptor.setDisplayName (NbBundle.getMessage (SwingBrowser.class, "CTL_SwingBrowser"));
            beanDescriptor.setShortDescription (NbBundle.getMessage (SwingBrowser.class, "HINT_SwingBrowser"));
        }
        return beanDescriptor;
    }

    // Property array 
    private static PropertyDescriptor[] properties = null /*lazy*/; 

    private static PropertyDescriptor[] getPdescriptor(){
        if(properties == null){
            properties = new PropertyDescriptor[1];
    
            try {
                properties[0] = new PropertyDescriptor ("name", SwingBrowser.class, "getName", null); // NOI18N
            }
            catch( IntrospectionException e) {}
        }
        return properties;
    }

    // EventSet array
    private static EventSetDescriptor[] eventSets = null /*lazy*/; 

    private static EventSetDescriptor[] getEdescriptor(){
        if(eventSets == null){
            eventSets = new EventSetDescriptor[1];
    
            try {
                eventSets[0] = new EventSetDescriptor (
                    SwingBrowser.class, 
                    "propertyChangeListener", 
                    PropertyChangeListener.class, 
                    new String[] {"propertyChange"}, 
                    "addPropertyChangeListener", 
                    "removePropertyChangeListener"
                );   // NOI18N
            }
            catch( IntrospectionException e) {}
        }
        return eventSets;
    }


    /**
     * Gets the bean's <code>BeanDescriptor</code>s.
     * 
     * @return BeanDescriptor describing the editable
     * properties of this bean.  May return null if the
     * information should be obtained by automatic analysis.
     */
    public BeanDescriptor getBeanDescriptor() {
	//return beanDescriptor;
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
        //return properties;
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
        //return eventSets;
	return getEdescriptor();
    }

    /**
    * Returns the internal browser icon. 
    */
    public Image getIcon (int type) {
        return loadImage("/org/openide/resources/html/htmlView.gif"); // NOI18N
    }
}

