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

package examples.colorpicker;

import java.beans.*;

/** BeanInfo for ColorPreview bean.
 */
public class ColorPreviewBeanInfo extends SimpleBeanInfo {

    // Property identifiers //GEN-FIRST:Properties
    private static final int PROPERTY_blue = 0;
    private static final int PROPERTY_green = 1;
    private static final int PROPERTY_red = 2;

    // Property array
    private static PropertyDescriptor[] properties = new PropertyDescriptor[3];

    static {
        try {

            properties[PROPERTY_blue] = new PropertyDescriptor( "blue", ColorPreview.class, "getBlue", "setBlue" );
            properties[PROPERTY_green] = new PropertyDescriptor( "green", ColorPreview.class, "getGreen", "setGreen" );
            properties[PROPERTY_red] = new PropertyDescriptor( "red", ColorPreview.class, "getRed", "setRed" );
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

            eventSets[EVENT_propertyChangeListener] = new EventSetDescriptor( ColorPreview.class, "propertyChangeListener", java.beans.PropertyChangeListener.class, new String[] { "propertyChange" }, "addPropertyChangeListener", "removePropertyChangeListener" );
        }
        catch( IntrospectionException e) {}//GEN-HEADEREND:Events

        // Here you can add code for customizing the event sets array.

    }//GEN-LAST:Events


    private static String ICON_COLOR_16x16 = null; //GEN-BEGIN:Icons
    private static String ICON_COLOR_32x32 = null;
    private static String ICON_MONO_16x16 = null;
    private static String ICON_MONO_32x32 = null; //GEN-END:Icons


    /** This methods returns an array of property descriptors.
     * @return Array of PropertyDescriptor instances associated with this BeanInfo.
     */
    public PropertyDescriptor[] getPropertyDescriptors() {
        return properties;
    }

    /** This methods returns an array of event set descriptors.
     * @return Array of EventSetDescriptor instances associated with this BeanInfo.
     */
    public EventSetDescriptor[] getEventSetDescriptors() {
        return eventSets;
    }


    java.awt.Image icon = loadImage("/tutorial/colorpicker/ColorPreview.gif");

    /** This method returns this bean info icon, depending on given argument.
     * @param iconKind Type of icon.
     * @return Icon associated with this BeanInfo.
     */
    public java.awt.Image getIcon(int iconKind) {

        switch (iconKind) {
            case java.beans.BeanInfo.ICON_COLOR_16x16: return icon;
        }
        return null;

    }
}
