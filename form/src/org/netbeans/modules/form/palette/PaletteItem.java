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
import java.awt.Toolkit;
import java.net.URL;

import org.openide.nodes.Node;
import org.openide.util.*;


/**
 * PaletteItem holds all important information needed by the Form  about one
 * component (item) in the palette.
 *
 * @author Tomas Pavek
 */

public final class PaletteItem implements Node.Cookie {

    private PaletteItemDataObject itemDataObject;

    // raw data (as read from the file - to be resolved lazily)
    String componentClassName;
    String originType_explicit;
    String originLocation;
//    Boolean isContainer_explicit;
    String displayName_key;
    String tooltip_key;
    String bundleName;
    String icon16URL;
    String icon32URL;
    String componentType_explicit;

    // resolution markers
    private boolean displayNameResolved;
    private boolean tooltipResolved;
    private boolean icon16Resolved;
    private boolean icon32Resolved;
    private boolean componentTypeResolved;

    // resolved data
    private Class componentClass;
    private Throwable lastError;
//    private Boolean componentIsContainer;
    private String displayName;
    private String tooltip;
    private Image icon16;
    private Image icon32;
    private int componentType = -1;

    // type of component constants
    private static final int LAYOUT = 1;
    private static final int BORDER = 2;
    private static final int VISUAL = 4; // bit flag
    private static final int MENU = 8; // bit flag
    private static final int TYPE_MASK = 15;

    // type of item origin constants
    public static final int JAR = 1;
    public static final int LIBRARY = 2;
    public static final int PROJECT = 3;

//    private static final String DEFAULT_ICON = "org/openide/resources/pending.gif"; // NOI18N

    // -------

    PaletteItem(PaletteItemDataObject dobj) {
        itemDataObject = dobj;
    }

    public Node getNode() {
        return itemDataObject.getNodeDelegate();
    }

    public String getId() {
        return componentClassName;
    }

    public Class getComponentClass() {
        if (componentClass == null && lastError == null)
            componentClass = loadComponentClass();
        return componentClass;
    }

    public Throwable getError() {
        return lastError;
    }

    public String getExplicitComponentType() {
        return componentType_explicit;
    }

    public boolean isVisual() {
        if (!componentTypeResolved)
            resolveComponentType();
        return (componentType & VISUAL) != 0;
    }

    public boolean isLayout() {
        if (!componentTypeResolved)
            resolveComponentType();
        return (componentType & TYPE_MASK) == LAYOUT;
    }

    public boolean isBorder() {
        if (!componentTypeResolved)
            resolveComponentType();
        return (componentType & TYPE_MASK) == BORDER;
    }

    public boolean isMenu() {
        if (!componentTypeResolved)
            resolveComponentType();
        return (componentType & MENU) != 0;
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

    String getDisplayName() {
        if (!displayNameResolved) {
            // first try the explicit display name from the item definition file
            if (displayName_key != null) {
                if (bundleName != null) {
                    try {
                        displayName = NbBundle.getBundle(bundleName)
                                                .getString(displayName_key);
                    }
                    catch (Exception ex) {} // ignore failure
                }
                if (displayName == null)
                    displayName = displayName_key;
            }
            else { // try BeanDescriptor
                BeanDescriptor bd = getBeanDescriptor();
                if (bd != null) {
                    displayName = bd.getDisplayName();
                    if (tooltip == null && tooltip_key == null)
                        tooltip = bd.getShortDescription();
                }
//                if (displayName == null && componentClassName != null) {
//                    // use short class name
//                    int i = componentClassName.lastIndexOf('$'); // NOI18N
//                    if (i < 0)
//                        i = componentClassName.lastIndexOf('.'); // NOI18N
//                    displayName = i > 0 && i+1 < componentClassName.length() ?
//                        componentClassName.substring(i+1) : componentClassName;
//                }
            }
            displayNameResolved = true;
        }
        return displayName;
    }

    String getTooltip() {
        if (!tooltipResolved) {
            // first try the explicit tooltip from the item definition file
            if (tooltip_key != null) {
                if (bundleName != null) {
                    try {
                        tooltip = NbBundle.getBundle(bundleName)
                                            .getString(tooltip_key);
                    }
                    catch (Exception ex) {} // ignore failure
                }
                if (tooltip == null)
                    tooltip = tooltip_key;
            }
            else { // try BeanDescriptor
                BeanDescriptor bd = getBeanDescriptor();
                if (bd != null) {
                    tooltip = bd.getShortDescription();
                    if (displayName == null && displayName_key == null)
                        displayName = bd.getDisplayName();
                }
//                if (tooltip == null && componentClassName != null) // use the class name
//                    tooltip = componentClassName;
            }
            tooltipResolved = true;
        }
        return tooltip;
    }

    Image getIcon(int type) {
        if (type == BeanInfo.ICON_COLOR_32x32 || type == BeanInfo.ICON_MONO_32x32) {
            if (!icon32Resolved) {
                if (icon32URL != null) { // explicit item icon specified
                    try {
                        icon32 = Toolkit.getDefaultToolkit().getImage(
                                                 new java.net.URL(icon32URL));
                    }
                    catch (java.net.MalformedURLException ex) {} // ignore
                }
                else {
                    BeanInfo bi = getBeanInfo();
                    if (bi != null)
                        icon32 = bi.getIcon(type);
                }
//                if (icon32 == null)
//                    icon32 = Toolkit.getDefaultToolkit().getImage(DEFAULT_ICON); //Utilities.loadImage
                icon32Resolved = true;
            }
            return icon32;
        }
        else { // get small icon in other cases
            if (!icon16Resolved) {
                if (icon16URL != null) { // explicit item icon specified
                    try {
                        icon16 = Toolkit.getDefaultToolkit().getImage(
                                                 new java.net.URL(icon16URL));
                    }
                    catch (java.net.MalformedURLException ex) {} // ignore
                }
                else {
                    BeanInfo bi = getBeanInfo();
                    if (bi != null)
                        icon16 = bi.getIcon(type);
                }
//                if (icon16 == null)
//                    icon16 = Toolkit.getDefaultToolkit().getImage(DEFAULT_ICON); //Utilities.loadImage
                icon16Resolved = true;
            }
            return icon16;
        }
    }

    void reset() {
        componentClass = null;
        lastError = null;
//        componentIsContainer = null; 
        displayNameResolved = false;
        tooltipResolved = false;
        icon16Resolved = false;
        icon32Resolved = false;
        componentTypeResolved = false;
    }

    // -------

    private Class loadComponentClass() {
        d("Loading class: "+componentClassName); // NOI18N

        ClassLoader loader = null;
        if (originType_explicit == null)
            loader = (ClassLoader)Lookup.getDefault().lookup(ClassLoader.class);
//        else TBD

        if (loader != null) {
            lastError = null;
            try {
                return loader.loadClass(componentClassName);
            }
            catch (Exception ex) {
                lastError = ex;
            }
            catch (LinkageError ex) {
                lastError = ex;
            }
        }

        return null;
    }

    private BeanInfo getBeanInfo() {
        Class compClass = getComponentClass();
        if (compClass != null) {
            try {
                return Introspector.getBeanInfo(compClass);
            }
            catch (IntrospectionException ex) {} // ignore failure
        }
        return null;
    }

    private BeanDescriptor getBeanDescriptor() {
        Class compClass = getComponentClass();
        if (compClass != null) {
            try {
                return Introspector.getBeanInfo(compClass).getBeanDescriptor();
            }
            catch (IntrospectionException ex) {} // ignore failure
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

        componentTypeResolved = true;
    }

    // -------

    /** For debugging purposes only. */
    private static final boolean TRACE = true;
    
    /** For debugging purposes only. */
    private static void t(String str) {
        if (TRACE)
            if (str != null)
                System.out.println("PaletteItem: " + str); // NOI18N
            else
                System.out.println(""); // NOI18N
    }

    private static void d(String str) {
        if (TRACE) {
            if (str != null)
                System.out.println("PaletteItem: " + str); // NOI18N
            Thread.dumpStack();
        }
    }

}
