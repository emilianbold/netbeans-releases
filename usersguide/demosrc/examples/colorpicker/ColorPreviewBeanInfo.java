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

package examples.colorpicker;

import java.beans.*;

public class ColorPreviewBeanInfo extends SimpleBeanInfo {

    // Property identifiers //GEN-FIRST:Properties
    private static final int PROPERTY_blue = 0;
    private static final int PROPERTY_green = 1;
    private static final int PROPERTY_red = 2;

    // Property array
    private static PropertyDescriptor[] properties = new PropertyDescriptor[3];

    static {
        try {

            properties[PROPERTY_blue] = new PropertyDescriptor ( "blue", ColorPreview.class, "getBlue", "setBlue" );
            properties[PROPERTY_green] = new PropertyDescriptor ( "green", ColorPreview.class, "getGreen", "setGreen" );
            properties[PROPERTY_red] = new PropertyDescriptor ( "red", ColorPreview.class, "getRed", "setRed" );
        }
        catch( IntrospectionException e) {}//GEN-HEADEREND:Properties

        // Here you can add code for customizing the properties array.

    }//GEN-LAST:Properties

    // EventSet identifiers //GEN-FIRST:Events

    private static final int EVENT_propertyChangeListener = 0;
    // EventSet array

    private static EventSetDescriptor[] eventSets = new EventSetDescriptor[1];

    static {
        try {

            eventSets[EVENT_propertyChangeListener] = new EventSetDescriptor ( ColorPreview.class, "propertyChangeListener", java.beans.PropertyChangeListener.class, new String[0], "addPropertyChangeListener", "removePropertyChangeListener" );
        }
        catch( IntrospectionException e) {}//GEN-HEADEREND:Events

        // Here you can add code for customizing the event sets array.

    }//GEN-LAST:Events


    private static String ICON_COLOR_16x16 = null; //GEN-BEGIN:Icons
    private static String ICON_COLOR_32x32 = null;
    private static String ICON_MONO_16x16 = null;
    private static String ICON_MONO_32x32 = null; //GEN-END:Icons


    public PropertyDescriptor[] getPropertyDescriptors() {
        return properties;
    }

    public EventSetDescriptor[] getEventSetDescriptors() {
        return eventSets;
    }


    java.awt.Image icon = loadImage ("/tutorial/colorpicker/ColorPreview.gif");

    public java.awt.Image getIcon (int iconKind) {

        switch (iconKind) {
        case java.beans.BeanInfo.ICON_COLOR_16x16: return icon;
        }
        return null;

    }
}
