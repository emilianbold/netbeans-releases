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

package org.netbeans.modules.form;

import java.awt.*;
import java.beans.*;
import java.lang.reflect.Method;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.modules.form.fakepeer.FakePeerSupport;

/**
 * BeanSupport is a utility class with various static methods supporting
 * operations with JavaBeans.
 *
 * @author Ian Formanek, Jan Stola
 */
public class BeanSupport
{
    public static final Object NO_VALUE = new Object();

    // -----------------------------------------------------------------------------
    // Private variables

    private static Map errorEmptyMap = new HashMap(3);
    private static Map instancesCache = new HashMap(30);

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
            Logger.getLogger(BeanSupport.class.getName())
                    .log(Level.INFO, "[WARNING] Cannot create default instance of: "+beanClass.getName(), ex); // NOI18N
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
            if (defInstance instanceof Component) {
                FakePeerSupport.attachFakePeer((Component)defInstance);
                if (defInstance instanceof Container)
                    FakePeerSupport.attachFakePeerRecursively(
                                                    (Container)defInstance);
            }
            instancesCache.put(beanClass, defInstance);
        }
        return defInstance;
    }

    /** Utility method that obtains icon for a bean class.
     * (This method is currently used only for obtaining default icons for AWT
     *  components. Other icons should be provided by BeanInfo.)
     */
    public static Image getBeanIcon(Class beanClass, int iconType) {
        return getIconForDefault(beanClass);
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

    // -----------------------------------------------------------------------------
    // Private methods

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
                                 BeanSupport.class.getResource((String)img));
            icons.put(name, image);
            return image;
        }
    }

    private static Map createImageCache() {
        Map map = new HashMap();
        
        map.put("java.awt.Label", "/org/netbeans/modules/form/beaninfo/awt/label.gif"); // NOI18N
        map.put("java.awt.Button", "/org/netbeans/modules/form/beaninfo/awt/button.gif"); // NOI18N
        map.put("java.awt.TextField", "/org/netbeans/modules/form/beaninfo/awt/textfield.gif"); // NOI18N
        map.put("java.awt.TextArea", "/org/netbeans/modules/form/beaninfo/awt/textarea.gif"); // NOI18N
        map.put("java.awt.Checkbox", "/org/netbeans/modules/form/beaninfo/awt/checkbox.gif"); // NOI18N
        map.put("java.awt.Choice", "/org/netbeans/modules/form/beaninfo/awt/choice.gif"); // NOI18N
        map.put("java.awt.List", "/org/netbeans/modules/form/beaninfo/awt/list.gif"); // NOI18N
        map.put("java.awt.Scrollbar", "/org/netbeans/modules/form/beaninfo/awt/scrollbar.gif"); // NOI18N
        map.put("java.awt.ScrollPane", "/org/netbeans/modules/form/beaninfo/awt/scrollpane.gif"); // NOI18N
        map.put("java.awt.Panel", "/org/netbeans/modules/form/beaninfo/awt/panel.gif"); // NOI18N
        map.put("java.awt.Canvas", "/org/netbeans/modules/form/beaninfo/awt/canvas.gif"); // NOI18N
        map.put("java.awt.MenuBar", "/org/netbeans/modules/form/beaninfo/awt/menubar.gif"); // NOI18N
        map.put("java.awt.PopupMenu", "/org/netbeans/modules/form/beaninfo/awt/popupmenu.gif"); // NOI18N
        map.put("java.awt.Menu", "/org/netbeans/modules/form/resources/menu.gif"); // NOI18N
        map.put("java.awt.MenuItem", "/org/netbeans/modules/form/resources/menuItem.gif"); // NOI18N
        map.put("java.awt.CheckboxMenuItem", "/org/netbeans/modules/form/resources/menuItemCheckbox.gif"); // NOI18N
        map.put("org.netbeans.modules.form.Separator", "/org/netbeans/modules/form/resources/menuSeparator.gif"); // NOI18N

        map.put("java.applet.Applet", "/org/netbeans/modules/form/resources/applet.gif"); // NOI18N
        map.put("java.awt.Dialog", "/org/netbeans/modules/form/resources/dialog.gif"); // NOI18N
        map.put("java.awt.Frame", "/org/netbeans/modules/form/resources/frame.gif"); // NOI18N

        return map;
    }

}
