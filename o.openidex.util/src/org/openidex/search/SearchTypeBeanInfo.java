/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.openidex.search;


import java.awt.Image;
import java.beans.*;

import org.openide.TopManager;
import org.openide.util.Utilities;

/** Bean info for <code>SearchType</code> class.
 *
 * @author  Petr Kuzel
 * */
public class SearchTypeBeanInfo extends SimpleBeanInfo {

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
        try {
	    PropertyDescriptor[] properties = new PropertyDescriptor[2];
            properties[0] = new PropertyDescriptor ( "name", SearchType.class, "getName", "setName" ); // NOI18N
            properties[0].setHidden ( true );
            properties[1] = new PropertyDescriptor ( "helpCtx", SearchType.class, "getHelpCtx", null ); // NOI18N
            properties[1].setHidden ( true );
    	    return properties;
        } catch( IntrospectionException e) {
	    TopManager.getDefault().getErrorManager().notify(e);
	    return null;
        }
    }

    /**
     * Gets the beans <code>EventSetDescriptor</code>s.
     * 
     * @return  An array of EventSetDescriptors describing the kinds of 
     * events fired by this bean.  May return null if the information
     * should be obtained by automatic analysis.
     */
    public EventSetDescriptor[] getEventSetDescriptors() {
        try {
	    return new EventSetDescriptor[] {
        	new EventSetDescriptor ( SearchType.class, "propertyChangeListener", PropertyChangeListener.class, new String[0], "addPropertyChangeListener", "removePropertyChangeListener" ) // NOI18N
	    };
        } catch( IntrospectionException e) {
	    TopManager.getDefault().getErrorManager().notify(e);
	    return null;
        }
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

    public Image getIcon(int iconKind) {
        if (iconKind == ICON_COLOR_16x16)
            return loadImage( "org/openidex/search/res/find.gif" ); // NOI18N

        return null;
    }

    public BeanDescriptor getBeanDescriptor(){
        BeanDescriptor descr = new BeanDescriptor(SearchType.class);
        descr.setDisplayName(org.openide.util.NbBundle.getBundle(SearchTypeBeanInfo.class).getString("CTL_SearchTypes"));
        return descr;
    }

    public BeanInfo[] getAdditionalBeanInfo () {
        try {
            return new BeanInfo[] { Introspector.getBeanInfo (org.openide.ServiceType.class) };
        } catch (IntrospectionException ie) {
	    TopManager.getDefault().getErrorManager().notify(ie);
            return null;
        }
    }


}
