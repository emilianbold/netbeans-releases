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

/* $Id$ */

package org.netbeans.modules.form;

import org.openide.nodes.Node;

import java.awt.*;
import java.beans.*;
import java.lang.reflect.Method;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

/**
 * BeanSupport is a utility class with various static methods supporting
 * operations with JavaBeans.
 *
 * @author Ian Formanek
 */
public class BeanSupport
{
    public static final Object NO_VALUE = new Object();
    
    // -----------------------------------------------------------------------------
    // Private variables

    private static HashMap errorEmptyMap = new HashMap(3);
    private static HashMap valuesCache = new HashMap(30);
    private static HashMap instancesCache = new HashMap(30);

    // -----------------------------------------------------------------------------
    // Public methods

    /**
     * Utility method to create an instance of given class. Returns null on
     * error.
     * @param beanClass the class to create inctance of
     * @return new instance of specified class or null if an error occured
     * during instantiation
     */
    public static Object createBeanInstance(Class beanClass) {
        try {
            return CreationFactory.createDefaultInstance(beanClass);
        }
        catch (Exception ex) {
            if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                ex.printStackTrace();
            System.err.println("[WARNING] BeanSupport cannot create default instance of: "+beanClass.getName());
            return null;
        }
    }

    /**
     * Utility method to obtain a BeanInfo of given JavaBean class. Returns
     * null on error.
     * @param beanClass the class to obtain BeanInfo for
     * @return BeanInfo instance or null if an error occured or the BeanInfo
     * cannot be found throughout the BeanInfoSearchPath
     */
    public static BeanInfo createBeanInfo(Class beanClass) {
        try {
            return org.openide.util.Utilities.getBeanInfo(beanClass);
        } catch (IntrospectionException e) {
            return null;
        }
    }

    /**
     * Utility method to obtain an instance of specified beanClass. The
     * instance is reused, and thus should only be used to obtain info about
     * settings of default instances of the specified class.
     * @param beanClass the class to create inctance of
     * @return instance of specified class or null if an error occured during
     * instantiation
     */
    public static Object getDefaultInstance(Class beanClass) {
        Object defInstance = instancesCache.get(beanClass);
        if (defInstance == null) {
            defInstance = createBeanInstance(beanClass);
            instancesCache.put(beanClass, defInstance);
        }
        return defInstance;
    }

    /**
     * Utility method to obtain a default property values of specified JavaBean
     * class.  The default values are property values immediately after the
     * instance is created.  Because some AWT components initialize their
     * properties only after the peer is created, these are treated specially
     * and default values for those properties are provided
     * explicitely(e.g. though the value of Font property of java.awt.Button is
     * null after an instance of Button is created, this method will return the
     * Font(Dialog, 12, PLAIN) as the default value).
     *
     * @param beanClass The Class of the JavaBean for which the default values
     * are to be obtained
     * @return Map containing pairs <PropertyName(String), value(Object)>
     * @see #getDefaultPropertyValue
     */
    
    public static Map getDefaultPropertyValues(Class beanClass) {
        Map defValues =(Map) valuesCache.get(beanClass);
        if (defValues == null) {
            Object beanInstance = getDefaultInstance(beanClass);
            if (beanInstance == null)
                return errorEmptyMap;
            defValues = getPropertyValues(beanInstance);
            valuesCache.put(beanClass, defValues);
        }
        return defValues;
    }

    /**
     * Utility method to obtain a default value of specified JavaBean class and
     * property name.  The default values are property values immediately after
     * the instance is created.  Because some AWT components initialize their
     * properties only after the peer is created, these are treated specially
     * and default values for those properties are provided
     * explicitely(e.g. though the value of Font property of java.awt.Button is
     * null after an instance of Button is created, this method will return the
     * Font(Dialog, 12, PLAIN) as the default value).
     *
     * @param beanClass The Class of the JavaBean for which the default value
     * is to be obtained
     * @param beanClass The name of the propertyn for which the default value
     * is to be obtained
     * @return The default property value for specified property on specified
     * JavaBean class
     * @see #getDefaultPropertyValues
     */
    public static Object getDefaultPropertyValue(Class beanClass, String propertyName) {
        Map values = getDefaultPropertyValues(beanClass);
        Object val = values.get(propertyName);
        if (val == null && !values.containsKey(propertyName))
            val = NO_VALUE;
        return val;
    }

    /**
     * Utility method to obtain a current property values of given JavaBean instance.
     * Only the properties specified in bean info(if it exists) are provided.
     *
     * @return Map containing pairs <PropertyName(String), value(Object)>
     */
    public static Map getPropertyValues(Object beanInstance) {
        if (beanInstance == null) {
            return errorEmptyMap;
        }

        BeanInfo info = createBeanInfo(beanInstance.getClass());
        PropertyDescriptor[] properties = info.getPropertyDescriptors();
        HashMap defaultValues = new HashMap(properties.length * 2);

        for (int i = 0; i < properties.length; i++) {
            defaultValues.put(properties[i].getName(), NO_VALUE);
            
            Method readMethod = properties[i].getReadMethod();
            if (readMethod != null) {
                try {
                    Object value = readMethod.invoke(beanInstance, new Object [0]);
//                    if (value == null)
//                        value = getSpecialDefaultAWTValue(beanInstance, properties[i].getName());
                    defaultValues.put(properties[i].getName(), value);
                } catch (Exception e) {
                    // problem with reading property ==>> no default value
//                    if (FormEditor.getFormSettings().getOutputLevel() != FormLoaderSettings.OUTPUT_MINIMUM) {
                        //            notifyPropertyException(beanInstance.getClass(), properties [i].getName(), "component", e, true); // NOI18N
//                    }
                }
            } 
//            else { // the property does not have plain read method
//                if (properties[i] instanceof IndexedPropertyDescriptor) {
//                    //          [PENDING]
//                    //          Method indexedReadMethod =((IndexedPropertyDescriptor)properties[i]).getIndexedReadMethod();
//                }
//            }
        }

        return defaultValues;
    }

    /** Utility method that obtains icon for a bean class.
     * (This method is currently used only for obtaining default icons for AWT
     *  components. Other icons should be provided by BeanInfo.)
     */
    public static Image getBeanIcon(Class beanClass, int iconType) {
        return getIconForDefault(beanClass);
/*        Image ret = getIconForDefault(beanClass);
        if (ret != null) {
            return ret;
        }
        // [FUTURE: the icon should be obtained from the InstanceCookie somehow, and customizable by the user]
        BeanInfo bi = createBeanInfo(beanClass);
        if (bi != null) {
            return bi.getIcon(iconType);
        }
        return null; */
    }

    /** A utility method that returns a class of event adapter for
     * specified listener. It works only on known listeners from java.awt.event.
     * Null is returned for unknown listeners.
     * @return class of an adapter for specified listener or null if
     *               unknown/does not exist
     */
    public static Class getAdapterForListener(Class listener) {
        if (java.awt.event.ComponentListener.class.equals(listener))
            return java.awt.event.ComponentAdapter.class;
        else if (java.awt.event.ContainerListener.class.equals(listener))
            return java.awt.event.ContainerAdapter.class;
        else if (java.awt.event.FocusListener.class.equals(listener))
            return java.awt.event.FocusAdapter.class;
        else if (java.awt.event.KeyListener.class.equals(listener))
            return java.awt.event.KeyAdapter.class;
        else if (java.awt.event.MouseListener.class.equals(listener))
            return java.awt.event.MouseAdapter.class;
        else if (java.awt.event.MouseMotionListener.class.equals(listener))
            return java.awt.event.MouseMotionAdapter.class;
        else if (java.awt.event.WindowListener.class.equals(listener))
            return java.awt.event.WindowAdapter.class;
        else return null; // not found
    }

/*    public static Node.Property [] createEventsProperties(Object beanInstance) {
        BeanInfo beanInfo = createBeanInfo(beanInstance.getClass());
        EventSetDescriptor[] events = beanInfo.getEventSetDescriptors();
        ArrayList eventsProps = new ArrayList();
        for (int i = 0; i < events.length; i++) {
        }

        Node.Property[] np = new Node.Property [eventsProps.size()];
        eventsProps.toArray(np);

        return np;
    }*/

    // -----------------------------------------------------------------------------
    // Private methods

    private static Object getSpecialDefaultAWTValue(Object beanObject, String propertyName) {
        if ((beanObject instanceof Frame) ||
            (beanObject instanceof Dialog)) {
            if ("background".equals(propertyName)) // NOI18N
                return SystemColor.window;
            else if ("foreground".equals(propertyName)) // NOI18N
                return SystemColor.windowText;
            else if ("font".equals(propertyName)) // NOI18N
                return new Font("Dialog", Font.PLAIN, 12); // NOI18N
        }

        if ((beanObject instanceof Label) ||
            (beanObject instanceof Button) ||
            (beanObject instanceof TextField) ||
            (beanObject instanceof TextArea) ||
            (beanObject instanceof Checkbox) ||
            (beanObject instanceof Choice) ||
            (beanObject instanceof List) ||
            (beanObject instanceof Scrollbar) ||
            (beanObject instanceof Panel) ||
            (beanObject instanceof ScrollPane)) {
            if ("background".equals(propertyName)) // NOI18N
                return SystemColor.control;
            else if ("foreground".equals(propertyName)) // NOI18N
                return SystemColor.controlText;
            else if ("font".equals(propertyName)) // NOI18N
                return new Font("Dialog", Font.PLAIN, 12); // NOI18N
        }

        return null;
    }

    static Reference imageCache;

    private static synchronized Image getIconForDefault(Class klass) {
        Map icons;
        if ((imageCache == null) || ((icons = (Map) imageCache.get()) == null)) {
            icons = createImageCache();
            imageCache = new SoftReference(icons);
        }
        
        String name = klass.getName();
        Object img = icons.get(name);
        
        if (img == null) {
            return null;
        }
        
        if (img instanceof Image) {
            return (Image) img;
        } else {
            Image image = java.awt.Toolkit.getDefaultToolkit().createImage(
                                     FormEditor.class.getResource((String)img));
            icons.put(name, image);
            return image;
        }
    }

    private static Map createImageCache() {
        Map ret = new HashMap();
        
        String[] compos = FormEditorModule.getDefaultAWTComponents();
        String[] icons = FormEditorModule.getDefaultAWTIcons();
        include(ret, compos, icons);
        
//        compos = FormEditorModule.getDefaultSwingComponents();
//        icons = FormEditorModule.getDefaultSwingIcons();
//        include(ret, compos, icons);

//        compos = FormEditorModule.getDefaultSwing2Components();
//        icons = FormEditorModule.getDefaultSwing2Icons();
//        include(ret, compos, icons);
        
//        compos = FormEditorModule.getDefaultLayoutsComponents();
//        icons = FormEditorModule.getDefaultLayoutsIcons();
//        include(ret, compos, icons);

//        compos = FormEditorModule.getDefaultBorders();
//        icons = FormEditorModule.getDefaultBordersIcons();
//        include(ret, compos, icons);
        
//        ret.put("javax.swing.JApplet", "/javax/swing/beaninfo/images/JAppletColor16.gif");
//        ret.put("javax.swing.JDialog", "/javax/swing/beaninfo/images/JDialogColor16.gif");
//        ret.put("javax.swing.JFrame", "/javax/swing/beaninfo/images/JFrameColor16.gif");
        ret.put("java.applet.Applet", "/org/netbeans/modules/form/resources/applet.gif");
        ret.put("java.awt.Dialog", "/org/netbeans/modules/form/resources/dialog.gif");
        ret.put("java.awt.Frame", "/org/netbeans/modules/form/resources/frame.gif");
        return ret;
    }
    
    private static void include(Map ret, String[] compos, String[] icons) {
        for (int i = 0; i < compos.length; i++) {
            ret.put(compos[i], icons[i]);
        }
    }
}
