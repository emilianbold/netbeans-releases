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

import java.beans.*;
import java.awt.Image;

import org.openide.ErrorManager;
import org.openide.nodes.Node;

import org.netbeans.modules.form.FormUtils;
import org.netbeans.modules.form.project.*;

/**
 * PaletteItem holds important information about one component (item)
 * in the palette.
 *
 * @author Tomas Pavek
 */

public final class PaletteItem implements Node.Cookie {

    private PaletteItemDataObject itemDataObject;

    // raw data (as read from the item file - to be resolved lazily)
    ClassSource componentClassSource;
//    Boolean isContainer_explicit;
    String componentType_explicit;

    // resolved data (derived from the raw data)
    private Class componentClass;
    private Throwable lastError; // error occurred when loading component class
//    private Boolean componentIsContainer;
    private int componentType = -1;

    // type of component constants
    private static final int LAYOUT = 1;
    private static final int BORDER = 2;
    private static final int VISUAL = 4; // bit flag
    private static final int MENU = 8; // bit flag
    private static final int TYPE_MASK = 15;

    // -------

    PaletteItem(PaletteItemDataObject dobj) {
        itemDataObject = dobj;
    }

    void setComponentClassSource(String className,
                                 String[] cpTypes,
                                 String[] cpNames)
    {
        componentClassSource = new ClassSource(className, cpTypes, cpNames);
    }

    void setComponentExplicitType(String type) {
        componentType_explicit = type;
    }

    // -------

    /** @return a node visually representing this palette item */
    public Node getNode() {
        return itemDataObject.getNodeDelegate();
    }

    /** @return a String identifying this palette item */
    public String getId() {
        return getComponentClassName();
    }

    public String getComponentClassName() {
        return componentClassSource.getClassName();
    }

    public ClassSource getComponentClassSource() {
        return componentClassSource;
    }

    /** @return the class of the component represented by this pallete item.
     * May return null - if class loading fails. */
    public Class getComponentClass() {
        if (componentClass == null && lastError == null)
            componentClass = loadComponentClass();
        return componentClass;
    }

    /** @return the exception occurred when trying to resolve the component
     *  class of this pallette item */
    public Throwable getError() {
        return lastError;
    }

    /** @return type of the component as String, e.g. "visual", "menu",
     * "layout", border */
    public String getExplicitComponentType() {
        return componentType_explicit;
    }

    /** @return whether the component of this palette item is a visual component
     * (java.awt.Component subclass) */
    public boolean isVisual() {
        if (componentType == -1)
            resolveComponentType();
        return (componentType & VISUAL) != 0;
    }

    /** @return whether the component of this palette item is a menu component */
    public boolean isMenu() {
        if (componentType == -1)
            resolveComponentType();
        return (componentType & MENU) != 0;
    }

    /** @return whether the component of this palette item is a layout mamanger
     * (java.awt.LayoutManager implementation) */
    public boolean isLayout() {
        if (componentType == -1)
            resolveComponentType();
        return (componentType & TYPE_MASK) == LAYOUT;
    }

    /** @return whether the component of this palette item is a border
     * (javax.swing.border.Border implementation) */
    public boolean isBorder() {
        if (componentType == -1)
            resolveComponentType();
        return (componentType & TYPE_MASK) == BORDER;
    }

//    public boolean isContainer() {
//        if (componentIsContainer == null) {
//            if (isContainer_explicit != null)
//                componentIsContainer = isContainer_explicit;
//            else {
//                Class compClass = getComponentClass();
//                if (compClass != null
//                    && java.awt.Container.class.isAssignableFrom(compClass))
//                {
//                    BeanDescriptor bd = getBeanDescriptor();
//                    componentIsContainer =
//                        bd != null && Boolean.FALSE.equals(bd.getValue("isContainer")) ? // NOI18N
//                            Boolean.FALSE : Boolean.TRUE;
//                }
//                else componentIsContainer = Boolean.FALSE;
//            }
//        }
//        return componentIsContainer.booleanValue();
//    }

    public String toString() {
        return PaletteUtils.getItemComponentDescription(this);
    }

    String getDisplayName() {
        BeanDescriptor bd = getBeanDescriptor();
        return bd != null ? bd.getDisplayName() : null;
    }

    String getTooltip() {
        BeanDescriptor bd = getBeanDescriptor();
        return bd != null ? bd.getShortDescription() : null;
    }

    Image getIcon(int type) {
        BeanInfo bi = getBeanInfo();
        return bi != null ? bi.getIcon(type) : null;
    }

    void reset() {
        componentClass = null;
        lastError = null;
//        componentIsContainer = null; 
        componentType = -1;

        itemDataObject.displayName = null;
        itemDataObject.tooltip = null;
        itemDataObject.icon16 = null;
        itemDataObject.icon32 = null;
    }

    // -------

    private Class loadComponentClass() {
        try {
            return FormUtils.loadSystemClass(getComponentClassName());   
        } catch (ClassNotFoundException cnfex) {}

        try {
            return ClassPathUtils.loadClass(getComponentClassSource());
        }
        catch (Exception ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            lastError = ex;
        }
        catch (LinkageError ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            lastError = ex;
        }
        return null;
    }

    private BeanInfo getBeanInfo() {
        Class compClass = getComponentClass();
        if (compClass != null) {
            try {
                return Introspector.getBeanInfo(compClass);
            }
            catch (Exception ex) {} // ignore failure
            catch (LinkageError ex) {}
        }
        return null;
    }

    private BeanDescriptor getBeanDescriptor() {
        Class compClass = getComponentClass();
        if (compClass != null) {
            try {
                return Introspector.getBeanInfo(compClass).getBeanDescriptor();
            }
            catch (Exception ex) {} // ignore failure
            catch (LinkageError ex) {}
        }
        return null;
    }

    private void resolveComponentType() {
        if (componentType_explicit == null) {
            componentType = 0;

            Class compClass = getComponentClass();
            if (compClass == null)
                return;

            if (java.awt.LayoutManager.class.isAssignableFrom(compClass)) {
                // PENDING LayoutSupportDelegate - should have special entry in pallette item file?
                componentType = LAYOUT;
                return;
            }

            if (javax.swing.border.Border.class.isAssignableFrom(compClass)) {
                componentType = BORDER;
                return;
            }

            if (java.awt.Component.class.isAssignableFrom(compClass))
                componentType |= VISUAL;

            if (java.awt.MenuComponent.class.isAssignableFrom(compClass)
                  || javax.swing.JMenuItem.class.isAssignableFrom(compClass)
                  || javax.swing.JMenuBar.class.isAssignableFrom(compClass)
                  || javax.swing.JPopupMenu.class.isAssignableFrom(compClass))
                componentType |= MENU;
        }
        else if ("visual".equalsIgnoreCase(componentType_explicit)) // NOI18N
            componentType = VISUAL;
        else if ("layout".equalsIgnoreCase(componentType_explicit)) // NOI18N
            componentType = LAYOUT;
        else if ("border".equalsIgnoreCase(componentType_explicit)) // NOI18N
            componentType = BORDER;
        else if ("menu".equalsIgnoreCase(componentType_explicit)) // NOI18N
            componentType = MENU | VISUAL;
        else
            componentType = 0;
    }
}
