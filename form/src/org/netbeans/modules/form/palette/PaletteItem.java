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
import org.netbeans.modules.form.CreationDescriptor;
import org.netbeans.modules.form.CreationFactory;

import java.lang.ref.WeakReference;

/** The PaletteItem encapsulate all objects that can be used as components in
 * the form editor
 *
 * @author   Ian Formanek
 */
public class PaletteItem {

    public final static String ATTR_IS_CONTAINER = "isContainer"; // NOI18N
    // -----------------------------------------------------------------------------
    // Global class variables

    /** Weak reference to shared instance of the JavaBean */
    private WeakReference sharedReference = null;

    /** The JavaBean Class represented by this PaletteItem */
    private boolean beanClassFailed;

    private Node itemNode;
    private InstanceCookie instanceCookie;
    private InstanceDataObject instanceDO;

    /** Creates a new PaletteItem */
    public PaletteItem(Node node) throws ClassNotFoundException,
                                         java.io.IOException,
                                         InstantiationException {
        itemNode = node;
        InstanceCookie ic =
            (InstanceDataObject)itemNode.getCookie(InstanceDataObject.class);
        if (ic != null) {
            instanceDO = (InstanceDataObject)ic;
        }
        else {
            ic = (InstanceCookie)itemNode.getCookie(InstanceCookie.class);
            if (ic == null)
                throw new InstantiationException();
        }
        instanceCookie = ic;
    }

    // -----------------------------------------------------------------------------
    // Class Methods

    public Node getItemNode() {
        return itemNode;
    }

    public Node getCategoryNode() {
        return itemNode.getParentNode();
    }

    public String getName() {
        if (itemNode instanceof PaletteItemNode) {
            String expName = ((PaletteItemNode)itemNode).getExplicitDisplayName();
            if (expName != null)
                return expName;
        }

        if (instanceDO != null) {
            String name = instanceCookie.instanceName();
            int i = name.lastIndexOf('.');
            if (i >= 0)
                name = name.substring(i+1);
            return name;
        }
        else return itemNode.getName();
    }

    public String getDisplayName() {
        if (itemNode instanceof PaletteItemNode) {
            String expName = ((PaletteItemNode)itemNode).getExplicitDisplayName();
            if (expName != null)
                return expName;
        }

        return instanceDO != null ?
               instanceDO.instanceName() : itemNode.getName();
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
        try {
            Class beanClass = getBeanClass();
            if (beanClass == null)
                return null;

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

    public Class getItemClass() {
        return getBeanClass();
    }

    public InstanceCookie getInstanceCookie() {
        return instanceCookie;
    }

    public java.beans.BeanInfo getBeanInfo() {
        try {
            return java.beans.Introspector.getBeanInfo(getBeanClass());
        } catch (java.beans.IntrospectionException e) {
            return null;
        }
    }

    public boolean isBorder() {
        Class beanClass = getBeanClass();
        if (beanClass == null)
            return false;

        return Border.class.isAssignableFrom(beanClass);
    }

    public boolean isVisual() {
        Class beanClass = getBeanClass();
        if (beanClass == null)
            return false;

        return Component.class.isAssignableFrom(beanClass);
    }

    public boolean isLayout() {
        Class beanClass = getBeanClass();
        if (beanClass == null)
            return false;

        return LayoutSupportDelegate.class.isAssignableFrom(beanClass)
               || LayoutManager.class.isAssignableFrom(beanClass);
    }

    public boolean isMenu() {
        Class beanClass = getBeanClass();
        if (beanClass == null)
            return false;

        return MenuBar.class.isAssignableFrom(beanClass) ||
            PopupMenu.class.isAssignableFrom(beanClass) ||
            JMenuBar.class.isAssignableFrom(beanClass) ||
            JPopupMenu.class.isAssignableFrom(beanClass);
    }

    private Class getBeanClass() {
        if (!beanClassFailed) {
            try {
                return instanceCookie.instanceClass();
            }
            catch (Exception ex) {
                if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                    ex.printStackTrace();
                beanClassFailed = true;
            }
        }
        return null;
    }

    // ------------------------------------

    public boolean equals(Object obj) {
        if (!(obj instanceof PaletteItem))
            return false;

        PaletteItem item = (PaletteItem)obj;
        if (getBeanClass() != item.getBeanClass())
            return false;

        if (instanceDO != null && item.instanceDO != null)
            return true;

        DataObject do1 = (DataObject)itemNode.getCookie(DataObject.class);
        DataObject do2 = (DataObject)item.itemNode.getCookie(DataObject.class);
        if (!(do1 instanceof DataShadow) || !(do2 instanceof DataShadow))
            return false;
        
        return ((DataShadow)do1).getOriginal() == ((DataShadow)do2).getOriginal();
    }
}
