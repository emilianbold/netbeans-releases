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


/** Bean info for <code>SearchType</code> class.
 *
 * @author  Petr Kuzel
 * */
public class SearchTypeBeanInfo extends SimpleBeanInfo {

    // Property identifiers 
    private static final int PROPERTY_name = 0;
    private static final int PROPERTY_helpCtx = 1;

    // Property array
    private static PropertyDescriptor[] properties = new PropertyDescriptor[2];

    static {
        try {

            properties[PROPERTY_name] = new PropertyDescriptor ( "name", SearchType.class, "getName", "setName" ); // NOI18N
            properties[PROPERTY_name].setHidden ( true );
            properties[PROPERTY_helpCtx] = new PropertyDescriptor ( "helpCtx", SearchType.class, "getHelpCtx", null ); // NOI18N
            properties[PROPERTY_helpCtx].setHidden ( true );
        } catch( IntrospectionException e) {
            e.printStackTrace();
        }

        // Here you can add code for customizing the properties array.

    }

    // EventSet identifiers
    private static final int EVENT_propertyChangeListener = 0;

    // EventSet array
    private static EventSetDescriptor[] eventSets = new EventSetDescriptor[1];

    static {
        try {
            eventSets[EVENT_propertyChangeListener] = new EventSetDescriptor ( SearchType.class, "propertyChangeListener", PropertyChangeListener.class, new String[0], "addPropertyChangeListener", "removePropertyChangeListener" ); // NOI18N
        }
        catch( IntrospectionException e) {
            e.printStackTrace();
        }

        // Here you can add code for customizing the event sets array.

    }

    private static Image iconColor16 = null; 
    private static Image iconColor32 = null;
    private static Image iconMono16 = null;
    private static Image iconMono32 = null; 
    private static String iconNameC16 = "/org/openidex/search/res/find.gif";
    private static String iconNameC32 = null;
    private static String iconNameM16 = null;
    private static String iconNameM32 = null;


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
        switch ( iconKind ) {
        case ICON_COLOR_16x16:
            return loadImage( iconNameC16 );

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

    public BeanDescriptor getBeanDescriptor(){
        BeanDescriptor descr = new BeanDescriptor(SearchType.class);
        descr.setDisplayName(org.openide.util.NbBundle.getBundle(SearchTypeBeanInfo.class).getString("CTL_SearchTypes"));
        return descr;
    }

    public BeanInfo[] getAdditionalBeanInfo () {
        try {
            return new BeanInfo[] { Introspector.getBeanInfo (org.openide.ServiceType.class) };
        } catch (IntrospectionException ie) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
                ie.printStackTrace ();
            return null;
        }
    }


}
