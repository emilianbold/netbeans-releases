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
import javax.swing.border.Border;

import org.openide.nodes.Node;
import org.openide.cookies.InstanceCookie;
import org.openide.loaders.InstanceDataObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataShadow;

import org.netbeans.modules.form.layoutsupport.*;
import org.netbeans.modules.form.compat2.layouts.DesignLayout;
import org.netbeans.modules.form.CreationDescriptor;
import org.netbeans.modules.form.CreationFactory;
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

    private Node itemNode;
    private InstanceCookie instanceCookie;
    private InstanceDataObject instanceDO;

    static final long serialVersionUID =6553170650531136255L;
    // -----------------------------------------------------------------------------
    // Constructors

    /** Creates a new PaletteItem */
    public PaletteItem(Node node) throws ClassNotFoundException,
                                         java.io.IOException,
                                         InstantiationException {
        itemNode = node;
        InstanceCookie ic =
            (InstanceDataObject)itemNode.getCookie(InstanceDataObject.class);
        if (ic != null) {
            instanceDO = (InstanceDataObject)ic;
            beanClass = instanceDO.instanceClass();
        }
        else {
            ic = (InstanceCookie)itemNode.getCookie(InstanceCookie.class);
            if (ic == null)
                throw new InstantiationException();
            beanClass = ic.instanceClass();
        }
        instanceCookie = ic;
    }

    /** Creates a new PaletteItem for specified JavaBean class
     * @param beanClass the string name of the Java Bean's classass
     */
//    public PaletteItem(String beanName) throws ClassNotFoundException {
//        this(Class.forName(beanName));
//    }

    /** Creates a new PaletteItem for specified JavaBean class
     * @param beanClass the Java Bean's class
     */
//    public PaletteItem(Class beanClass) {
//        this(beanClass, Container.class.isAssignableFrom(beanClass));
//    }

    /** Creates a new PaletteItem for specified JavaBean class
     * @param beanClass the Java Bean's class
     * @param isContainer allows to explicitly specify whether the item represents bean which can contain other beans
     */
//    public PaletteItem(Class beanClass, boolean isContainer) {
//        this.beanClass = beanClass;
//        this.expliciteIsContainer = new Boolean(isContainer);
//    }

    // -----------------------------------------------------------------------------
    // Class Methods

    public Node getItemNode() {
        return itemNode;
    }

    public Node getCategoryNode() {
        return itemNode.getParentNode();
    }

    public String getName() {
        String name = instanceCookie.instanceName();
/*        String name;
        if (instanceDO != null)
            name = instanceDO.instanceName();
        else if (instanceCookie != null)
            name = instanceCookie.instanceName();
        else
            name = beanClass.getName(); */
        int i = name.lastIndexOf('.');
        if (i >= 0)
            name = name.substring(i+1);
        return name;
        //return org.openide.util.Utilities.getShortClassName(getItemClass());
    }

    public String getDisplayName() {
        return itemNode.getDisplayName();
/*        String name = getName();
        if (name.endsWith("BorderInfo")) { // NOI18N
            return name.substring(0, name.length() - 4); // remove the "Info" from BorderInfo classes // NOI18N
        } else if (name.endsWith("Layout") && name.startsWith("Design")) { // NOI18N
            return name.substring(6); // remove the "Design" from DesignXXXLayout classes // NOI18N
        } else if (name.endsWith("LayoutSupport")) { // NOI18N
            return name.substring(0, name.length() - "Support".length()); // NOI18N
        }
        return name; */
    }

    public Object getSharedInstance() throws InstantiationException, IllegalAccessException {
        Object sharedObject;
        if ((sharedReference == null) ||((sharedObject = sharedReference.get()) == null)) {
            sharedObject = createInstance();
            sharedReference = new WeakReference(sharedObject);
        }

        return sharedObject;
    }

    public Object createInstance() throws InstantiationException,
                                          IllegalAccessException {
        if (beanClass == null) return null;

        try {
            if (instanceDO != null || instanceCookie == null) {
                CreationDescriptor cd
                    = CreationFactory.getDescriptor(beanClass);
                return cd != null ? cd.createDefaultInstance() :
                                    beanClass.newInstance();
//                return instanceDO.instanceCreate();
            }
            else
//            if (instanceCookie != null) {
                return instanceCookie.instanceCreate();
//            }
//            return beanClass.newInstance();
        }
        catch (ClassNotFoundException e1) {
            throw new InstantiationException(e1.getMessage());
        }
        catch (java.lang.reflect.InvocationTargetException e2) {
            throw new InstantiationException(e2.getTargetException().getMessage());
        }
        catch (java.io.IOException e3) {
            throw new InstantiationException(e3.getMessage());
        }
    }

    public LayoutSupport createLayoutSupportInstance()
    throws InstantiationException, IllegalAccessException {

        LayoutSupport layoutSupport = null;

        if (LayoutManager.class.isAssignableFrom(beanClass)) {
            // LayoutManager -> find LayoutSupport for it
            Class laysupClass = LayoutSupportRegistry
                        .getLayoutSupportForLayout(beanClass);
            if (laysupClass != null) layoutSupport =
                LayoutSupportRegistry.createLayoutSupport(laysupClass);
        }
        else if (LayoutSupport.class.isAssignableFrom(beanClass)) {
            // LayoutSupport -> use it directly
            layoutSupport = (LayoutSupport) createInstance();
        }
        else if (DesignLayout.class.isAssignableFrom(beanClass)) {
            // DesignLayout -> convert to LayoutSupport
            DesignLayout dl = (DesignLayout) createInstance();
            layoutSupport = Compat31LayoutFactory.createCompatibleLayoutSupport(dl);
        }

        return layoutSupport;
    }

    public Class getItemClass() {
        return beanClass;
    }

    public InstanceCookie getInstanceCookie() {
        return instanceCookie;
    }

    public java.beans.BeanInfo getBeanInfo() {
        try {
            return java.beans.Introspector.getBeanInfo(beanClass);
        } catch (java.beans.IntrospectionException e) {
            return null;
        }
    }

    public boolean isBorder() {
        return BorderInfo.class.isAssignableFrom(beanClass)
               || Border.class.isAssignableFrom(beanClass);
    }

    public boolean isVisual() {
        return Component.class.isAssignableFrom(beanClass);
    }

//    public boolean isDesignLayout() {
//        return DesignLayout.class.isAssignableFrom(beanClass);
//    }

    public boolean isLayout() {
        return LayoutSupport.class.isAssignableFrom(beanClass)
               || LayoutManager.class.isAssignableFrom(beanClass)
               || DesignLayout.class.isAssignableFrom(beanClass);
    }

    public boolean isContainer() {
//        if (expliciteIsContainer != null)
//            return expliciteIsContainer.booleanValue(); // explicitly set isContainer flag

        if (!Container.class.isAssignableFrom(beanClass))
            return false;

        DataObject dobj = (DataObject) itemNode.getCookie(DataObject.class);
        if (dobj != null) {
            Object attr = dobj.getPrimaryFile().getAttribute(PaletteItem.ATTR_IS_CONTAINER);
            if (attr instanceof Boolean)
                return ((Boolean)attr).booleanValue();
        }

        return PaletteItemNode.canBeContainer(instanceCookie);
    }

    public boolean isMenu() {
        return MenuBar.class.isAssignableFrom(beanClass) ||
            PopupMenu.class.isAssignableFrom(beanClass) ||
            JMenuBar.class.isAssignableFrom(beanClass) ||
            JPopupMenu.class.isAssignableFrom(beanClass);
    }

    // ------------------------------------

    public boolean equals(Object obj) {
        if (!(obj instanceof PaletteItem)) return false;

        PaletteItem item = (PaletteItem)obj;
        if (beanClass != item.beanClass) return false;

        if (instanceDO != null && item.instanceDO != null)
            return true;

        DataObject do1 = (DataObject)itemNode.getCookie(DataObject.class);
        DataObject do2 = (DataObject)item.itemNode.getCookie(DataObject.class);
        if (!(do1 instanceof DataShadow) || !(do2 instanceof DataShadow))
            return false;
        
        return ((DataShadow)do1).getOriginal() == ((DataShadow)do2).getOriginal();
    }
}
