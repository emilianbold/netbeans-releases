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

package org.netbeans.modules.form.palette;

import java.awt.*;
import javax.swing.*;

import org.openide.cookies.InstanceCookie;
import org.openide.loaders.InstanceDataObject;
import org.netbeans.modules.form.compat2.layouts.DesignLayout;
import org.netbeans.modules.form.compat2.border.DesignBorder;
import org.netbeans.modules.form.compat2.border.BorderInfo;

import java.lang.ref.WeakReference;

/** The PaletteItem encapsulate all objects that can be used as components in
 * the form editor
 *
 * @author   Ian Formanek
 */
public class PaletteItem implements java.io.Serializable {
    /** generated Serialized Version UID */
    //  static final long serialVersionUID = -2098259549820241091L;

    public final static String ATTR_IS_CONTAINER = "isContainer"; // NOI18N
    // -----------------------------------------------------------------------------
    // Global class variables

    /** Weak reference to shared instance of the JavaBean */
    private WeakReference sharedReference = null;

    /** The JavaBean Class represented by this PaletteItem */
    private Class beanClass;

    private InstanceCookie instanceCookie;
    private InstanceDataObject instanceDO;

    private Boolean expliciteIsContainer;

    // -----------------------------------------------------------------------------
    // Constructors

    /** Creates a new PaletteItem */
    public PaletteItem(InstanceCookie instanceCookie) throws ClassNotFoundException, java.io.IOException {
        this.instanceCookie = instanceCookie;
        this.beanClass = instanceCookie.instanceClass();
    }

    static final long serialVersionUID =6553170650531136255L;
    /** Creates a new PaletteItem */
    public PaletteItem(InstanceDataObject ido) throws ClassNotFoundException, java.io.IOException {
        this.beanClass = ido.instanceClass();
        this.instanceCookie = ido;
        this.instanceDO = ido;
    }

    /** Creates a new PaletteItem for specified JavaBean class
     * @param beanClass the string name of the Java Bean's classass
     */
    public PaletteItem(String beanName) throws ClassNotFoundException {
        this(Class.forName(beanName));
    }

    /** Creates a new PaletteItem for specified JavaBean class
     * @param beanClass the Java Bean's class
     */
    public PaletteItem(Class beanClass) {
        this(beanClass, Container.class.isAssignableFrom(beanClass));
    }

    /** Creates a new PaletteItem for specified JavaBean class
     * @param beanClass the Java Bean's class
     * @param isContainer allows to explicitly specify whether the item represents bean which can contain other beans
     */
    public PaletteItem(Class beanClass, boolean isContainer) {
        this.beanClass = beanClass;
        this.expliciteIsContainer = new Boolean(isContainer);
    }

    // -----------------------------------------------------------------------------
    // Class Methods

    public String getName() {
        String name;
        if (instanceDO != null)
            name = instanceDO.instanceName();
        else if (instanceCookie != null)
            name = instanceCookie.instanceName();
        else
            name = beanClass.getName();
        int i = name.lastIndexOf('.');
        if (i >= 0)
            name = name.substring(i+1);
        return name;
        //return org.openide.util.Utilities.getShortClassName(getItemClass());
    }

    public String getDisplayName() {
        String name = getName();
        if (name.endsWith("BorderInfo")) { // NOI18N
            return name.substring(0, name.length() - 4); // remove the "Info" from BorderInfo classes // NOI18N
        } else if (name.endsWith("Layout") && name.startsWith("Design")) { // NOI18N
            return name.substring(6); // remove the "Design" from DesignXXXLayout classes // NOI18N
        }
        return name;
    }

    public Object getSharedInstance() throws InstantiationException, IllegalAccessException {
        Object sharedObject;
        if ((sharedReference == null) ||((sharedObject = sharedReference.get()) == null)) {
            sharedObject = createInstance();
            sharedReference = new WeakReference(sharedObject);
        }

        return sharedObject;
    }

    public Object createInstance() throws InstantiationException, IllegalAccessException {
        if (beanClass == null) return null;
        try {
            if (instanceDO != null) {
                return instanceDO.instanceCreate();
            }

            if (instanceCookie != null) {
                return instanceCookie.instanceCreate();
            }

            return beanClass.newInstance();
        }catch (ClassNotFoundException e) {
            throw new InstantiationException(e.getMessage());
        } catch (java.io.IOException e) {
            throw new InstantiationException(e.getMessage());
        }
    }

    public Class getItemClass() {
        return beanClass;
    }

    public java.beans.BeanInfo getBeanInfo() {
        try {
            return java.beans.Introspector.getBeanInfo(beanClass);
        } catch (java.beans.IntrospectionException e) {
            return null;
        }
    }

    public DesignBorder createBorder() throws InstantiationException, IllegalAccessException {
        return new DesignBorder((BorderInfo)createInstance());
    }

    public boolean isBorder() {
        return BorderInfo.class.isAssignableFrom(beanClass);
    }

    public boolean isVisual() {
        return Component.class.isAssignableFrom(beanClass);
    }

    public boolean isDesignLayout() {
        return DesignLayout.class.isAssignableFrom(beanClass);
    }

    public boolean isContainer() {
        if (expliciteIsContainer != null)
            return expliciteIsContainer.booleanValue(); // explicitly set isContainer flag

        boolean isContainer = Container.class.isAssignableFrom(beanClass);
        if (instanceDO != null) {
            Object attr = instanceDO.getPrimaryFile().getAttribute(ATTR_IS_CONTAINER);
            if ((attr != null) &&(attr.equals(Boolean.FALSE))) {
                isContainer = false;
            }
        }
        return isContainer;
    }

    public boolean isMenu() {
        return MenuBar.class.isAssignableFrom(beanClass) ||
            PopupMenu.class.isAssignableFrom(beanClass) ||
            JMenuBar.class.isAssignableFrom(beanClass) ||
            JPopupMenu.class.isAssignableFrom(beanClass);
    }
}
