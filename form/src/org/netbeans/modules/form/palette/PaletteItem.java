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
import java.util.List;
import java.io.File;

import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.openide.util.*;
import org.openide.filesystems.*;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.api.project.libraries.*;
import org.netbeans.api.project.ant.*;
import org.netbeans.api.project.*;

/**
 * PaletteItem holds important information about one component (item)
 * in the palette.
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

    // resolved data
    private Class componentClass;
    private int originType = -1;
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
    public static final int FROM_JAR = 1;
    public static final int FROM_LIBRARY = 2;
    public static final int FROM_PROJECT = 3;

    // -------

    PaletteItem(PaletteItemDataObject dobj) {
        itemDataObject = dobj;
    }

    /** @return a node visually representing this palette item */
    public Node getNode() {
        return itemDataObject.getNodeDelegate();
    }

    /** @return a String identifying this palette item */
    public String getId() {
        return componentClassName;
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

    /** @return the type of source where the component class of this palette
     * item is located. Can be JAR, LIBRARY or PROJECT. */
    public int getOriginType() {
        if (originType == -1)
            resolveOriginType();
        return originType;
    }

    /** @return the location of the component class source (according to the
     * origin type) */
    public String getOriginValue() {
        return originLocation;
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
        originType = -1;
        lastError = null;
//        componentIsContainer = null; 
        displayNameResolved = false;
        tooltipResolved = false;
        icon16Resolved = false;
        icon32Resolved = false;
        componentType = -1;
    }

    // -------

    private Class loadComponentClass() {
        d("Loading class: "+componentClassName); // NOI18N

        int origin = getOriginType();
        ClassLoader loader = null;

        if (origin == 0) { // no origin, use system class loader
            loader = (ClassLoader)Lookup.getDefault().lookup(ClassLoader.class);
        }
        else try { // the class comes from an external JAR, installed library,
                   // or some project output JAR
            URL[] roots = null;

            if (origin == FROM_JAR) { // NOI18N
                // expecting full path to the JAR file in 'originLocation'
                File jarFile = new File(originLocation);
                roots = new URL[] { FileUtil.getArchiveRoot(jarFile.toURI().toURL()) };
            }
            else if (origin == FROM_LIBRARY) { // NOI18N
                // expecting the library name in 'originLocation'
                Library lib = LibraryManager.getDefault().getLibrary(originLocation);
                if (lib != null) {
                    List content = lib.getContent("classpath"); // NOI18N
                    roots = new URL[content.size()];
                    content.toArray(roots);
                    for (int i=0; i < roots.length; i++)
                        if (FileUtil.isArchiveFile(roots[i]))
                            roots[i] = FileUtil.getArchiveRoot(roots[i]);
                }
            }
            else if (origin == FROM_PROJECT) { // NOI18N
                Project project = ProjectManager.getDefault().findProject(
                    FileUtil.toFileObject(new File(originLocation)));
                if (project != null) {
                    AntArtifact[] artifacts =
                        AntArtifactQuery.findArtifactsByType(project, "jar"); // NOI18N
                    roots = new URL[artifacts.length];
                    for (int i=0; i < artifacts.length; i++) {
                        File jarFile = new File(
                            originLocation + artifacts[i].getArtifactLocation().toString());
                        roots[i] = FileUtil.getArchiveRoot(jarFile.toURI().toURL());
                    }
                }
            }

            if (roots != null)
                loader = ClassPathSupport.createClassPath(roots).getClassLoader(true);
        }
        catch (Exception ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            lastError = ex;
            return null;
        }

        if (loader != null) {
            lastError = null;
            try {
                return loader.loadClass(componentClassName);
            }
            catch (Exception ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                lastError = ex;
            }
            catch (LinkageError ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
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

    private void resolveOriginType() {
        if ("jar".equals(originType_explicit)) // NOI18N
            originType = FROM_JAR;
        else if ("library".equals(originType_explicit)) // NOI18N
            originType = FROM_LIBRARY;
        else if ("project".equals(originType_explicit)) // NOI18N
            originType = FROM_PROJECT;
        else
            originType = 0;
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
