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

package org.netbeans.modules.form.layoutsupport;

import java.awt.*;
import java.util.*;

import org.openide.TopManager;
import org.openide.loaders.*;
import org.openide.filesystems.*;
import org.openide.cookies.InstanceCookie;

/**
 * @author Tomas Pavek
 */

public class LayoutSupportRegistry {

    private static HashMap containerToLayoutDelegate;
    private static HashMap layoutToLayoutDelegate;

    private static boolean needPaletteRescan = true;

    public static final String DEFAULT_SUPPORT = "<default>"; // NOI18N

    private static DataFolder paletteFolder;

    private static FileChangeListener paletteListener;

    // --------------
    // getting methods

    public static Class getSupportClassForContainer(Class containerClass) {
        String className = (String)
                           getContainersMap().get(containerClass.getName());
        if (className == null) {
            className = findSuperClass(getContainersMap(), containerClass);
            if (className == null && needPaletteRescan) {
                className = scanPalette(containerClass.getName());
                if (className == null) // try container superclass again
                    className = findSuperClass(getContainersMap(),
                                               containerClass);
            }
        }

        if (className != null) {
            try {
                return loadClass(className);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public static String getSupportNameForContainer(String containerClassName) {
        String className = (String) getContainersMap().get(containerClassName);
        if (className == null) {
            Class containerClass;
            try {
                containerClass = loadClass(containerClassName);
            }
            catch (ClassNotFoundException e) {
                e.printStackTrace();
                return null;
            }

            className = findSuperClass(getContainersMap(), containerClass);
            if (className == null && needPaletteRescan) {
                className = scanPalette(containerClassName);
                if (className == null) // try container superclass again
                    className = findSuperClass(getContainersMap(),
                                               containerClass);
            }
        }

        return className;
    }

    public static Class getSupportClassForLayout(Class layoutClass) {
        String className = (String) getLayoutsMap().get(layoutClass.getName());
        if (className == null) {
            className = findSuperClass(getLayoutsMap(), layoutClass);
            if (className == null && needPaletteRescan) {
                className = scanPalette(layoutClass.getName());
                if (className == null) // try container superclass again
                    className = findSuperClass(getContainersMap(),
                                               layoutClass);
            }
        }

        if (className != null) {
            try {
                return loadClass(className);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public static String getSupportNameForLayout(String layoutClassName) {
        String className = (String) getLayoutsMap().get(layoutClassName);
        if (className == null) {
            Class layoutClass;
            try {
                layoutClass = loadClass(layoutClassName);
            }
            catch (ClassNotFoundException e) {
                e.printStackTrace();
                return null;
            }

            className = findSuperClass(getLayoutsMap(), layoutClass);
            if (className == null && needPaletteRescan) {
                className = scanPalette(layoutClassName);
                if (className == null) // try container superclass again
                    className = findSuperClass(getContainersMap(),
                                               layoutClass);
            }
        }

        return className;
    }

    // ------------
    // registering methods

    public static void registerSupportForContainer(
                           Class containerClass,
                           Class layoutDelegateClass)
    {
        getContainersMap().put(containerClass.getName(),
                               layoutDelegateClass.getName());
    }

    public static void registerSupportForContainer(
                           String containerClassName,
                           String layoutDelegateClassName)
    {
        getContainersMap().put(containerClassName, layoutDelegateClassName);
    }

    public static void registertSupportForLayout(
                           Class layoutClass,
                           Class layoutDelegateClass)
    {
        getLayoutsMap().put(layoutClass.getName(),
                            layoutDelegateClass.getName());
    }

    public static void registerSupportForLayout(
                           String layoutClassName,
                           String layoutDelegateClassName)
    {
        getLayoutsMap().put(layoutClassName, layoutDelegateClassName);
    }

    // ------------
    // creating methods

    public static LayoutSupportDelegate createSupportForContainer(
                                            Class containerClass)
        throws ClassNotFoundException,
               InstantiationException,
               IllegalAccessException
    {
        Class delegateClass = getSupportClassForContainer(containerClass);
        if (delegateClass == null)
            return null;

        return (LayoutSupportDelegate) delegateClass.newInstance();
    }

    public static LayoutSupportDelegate createSupportForLayout(
                                            Class layoutClass)
        throws ClassNotFoundException,
               InstantiationException,
               IllegalAccessException
    {
        String delegateClassName = getSupportNameForLayout(layoutClass.getName());
        if (delegateClassName == null)
            return null;

        if (delegateClassName == DEFAULT_SUPPORT)
            return new DefaultLayoutSupport(layoutClass);

        return (LayoutSupportDelegate)
               loadClass(delegateClassName).newInstance();
    }

    public static LayoutSupportDelegate createSupportInstance(
                                            Class layoutDelegateClass)
        throws InstantiationException, IllegalAccessException
    {
        return (LayoutSupportDelegate) layoutDelegateClass.newInstance();
    }

    // -----------
    // private methods

    private static String findSuperClass(Map map, Class subClass) {
        for (Iterator it=map.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry en = (Map.Entry) it.next();
            String className = (String) en.getKey();
            try {
                Class keyClass = loadClass(className);
                if (keyClass.isAssignableFrom(subClass))
                    return (String) en.getValue();
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    private static String scanPalette(String wantedClassName) {
        if (getPaletteFolder() == null)
            return null; // Palette folder does not exist - should not happen

        // create palette content listener - only once
        boolean newPaletteListener = paletteListener == null;
        if (newPaletteListener) {
            paletteListener = new FileChangeAdapter() {
                public void fileDataCreated(FileEvent fe) {
                    needPaletteRescan = true;
                }
                public void fileFolderCreated(FileEvent fe) {
                    needPaletteRescan = true;
                    fe.getFile().addFileChangeListener(this);
                }
                public void fileDeleted(FileEvent fe) {
                    fe.getFile().removeFileChangeListener(this);
                }
            };

            paletteFolder.getPrimaryFile().addFileChangeListener(paletteListener);
        }

        String foundSupportClassName = null;

        DataObject[] paletteCategories = paletteFolder.getChildren();
        for (int i=0; i < paletteCategories.length; i++) {
            DataFolder categoryFolder =
                paletteCategories[i] instanceof DataFolder ?
                    (DataFolder) paletteCategories[i] : null;

            if (categoryFolder == null)
                continue;
           
            if (newPaletteListener)
                categoryFolder.getPrimaryFile().addFileChangeListener(
                                                       paletteListener);

            DataObject[] items = categoryFolder.getChildren();
            for (int j=0; j < items.length; j++) {
                InstanceCookie ic = (InstanceCookie)
                                    items[j].getCookie(InstanceCookie.class);
                if (ic == null)
                    continue;

                Class itemClass = null;
                try {
                    itemClass = ic.instanceClass();
                }
                catch (Exception ex) {} // ignnore

                if (itemClass == null)
                    continue; // invalid class - ignore

                Class delegateClass = null;
                Class supportedClass = null;

                if (LayoutSupportDelegate.class.isAssignableFrom(itemClass)) {
                    // register LayoutSupportDelegate directly
                    delegateClass = itemClass;
                    try {
                        LayoutSupportDelegate delegate =
                            (LayoutSupportDelegate) delegateClass.newInstance();
                        supportedClass = delegate.getSupportedClass();
                    }
                    catch (Exception ex) {
                        if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                            ex.printStackTrace();
                        continue; // invalid - ignore
                    }
                }
                else if (LayoutManager.class.isAssignableFrom(itemClass)) {
                    // register default support for layout
                    supportedClass = itemClass;
                }

                if (supportedClass != null) {
                    Map map;
                    if (Container.class.isAssignableFrom(supportedClass))
                        map = getContainersMap();
                    else if (LayoutManager.class.isAssignableFrom(supportedClass))
                        map = getLayoutsMap();
                    else continue; // invalid - ignore

                    String supportedClassName = supportedClass.getName();
                    if (map.get(supportedClassName) == null) {
                        String delegateClassName = delegateClass != null ?
                                                     delegateClass.getName():
                                                     DEFAULT_SUPPORT;

                        map.put(supportedClassName, delegateClassName);

                        if (supportedClassName.equals(wantedClassName))
                            foundSupportClassName = delegateClassName;
                    }
                }
            }
        }

        needPaletteRescan = false;
        return foundSupportClassName;
    }

    private static DataFolder getPaletteFolder() {
        if (paletteFolder == null) {
            FileObject fo = TopManager.getDefault().getRepository()
                              .getDefaultFileSystem().findResource("Palette");
            if (fo != null)
                paletteFolder = DataFolder.findFolder(fo);
        }
        return paletteFolder;
    }

    private static Class loadClass(String className)
        throws ClassNotFoundException
    {
        return TopManager.getDefault().currentClassLoader().loadClass(className);
    }

    private static Map getContainersMap() {
        if (containerToLayoutDelegate == null) {
            containerToLayoutDelegate = new HashMap();
            // fill in default containers
            containerToLayoutDelegate.put(
                "javax.swing.JScrollPane", // NOI18N
                "org.netbeans.modules.form.layoutsupport.delegates.JScrollPaneSupport"); // NOI18N
            containerToLayoutDelegate.put(
                "java.awt.ScrollPane", // NOI18N
                "org.netbeans.modules.form.layoutsupport.delegates.ScrollPaneSupport"); // NOI18N
            containerToLayoutDelegate.put(
                "javax.swing.JSplitPane", // NOI18N
                "org.netbeans.modules.form.layoutsupport.delegates.JSplitPaneSupport"); // NOI18N
            containerToLayoutDelegate.put(
                "javax.swing.JTabbedPane", // NOI18N
                "org.netbeans.modules.form.layoutsupport.delegates.JTabbedPaneSupport"); // NOI18N
            containerToLayoutDelegate.put(
                "javax.swing.JLayeredPane", // NOI18N
                "org.netbeans.modules.form.layoutsupport.delegates.JLayeredPaneSupport"); // NOI18N
            containerToLayoutDelegate.put(
                "javax.swing.JToolBar", // NOI18N
                "org.netbeans.modules.form.layoutsupport.delegates.JToolBarSupport"); // NOI18N
        }
        return containerToLayoutDelegate;
    }

    private static Map getLayoutsMap() {
        if (layoutToLayoutDelegate == null) {
            layoutToLayoutDelegate = new HashMap();
            // fill in default layouts
            layoutToLayoutDelegate.put(
                "java.awt.BorderLayout", // NOI18N
                "org.netbeans.modules.form.layoutsupport.delegates.BorderLayoutSupport"); // NOI18N
            layoutToLayoutDelegate.put(
                "java.awt.FlowLayout", // NOI18N
                "org.netbeans.modules.form.layoutsupport.delegates.FlowLayoutSupport"); // NOI18N
            layoutToLayoutDelegate.put(
                "javax.swing.BoxLayout", // NOI18N
                "org.netbeans.modules.form.layoutsupport.delegates.BoxLayoutSupport"); // NOI18N
            layoutToLayoutDelegate.put(
                "java.awt.GridBagLayout", // NOI18N
                "org.netbeans.modules.form.layoutsupport.delegates.GridBagLayoutSupport"); // NOI18N
            layoutToLayoutDelegate.put(
                "java.awt.GridLayout", // NOI18N
                "org.netbeans.modules.form.layoutsupport.delegates.GridLayoutSupport"); // NOI18N
            layoutToLayoutDelegate.put(
                "java.awt.CardLayout", // NOI18N
                "org.netbeans.modules.form.layoutsupport.delegates.CardLayoutSupport"); // NOI18N
            layoutToLayoutDelegate.put(
                "org.netbeans.lib.awtextra.AbsoluteLayout", // NOI18N
                "org.netbeans.modules.form.layoutsupport.delegates.AbsoluteLayoutSupport"); // NOI18N
        }
        return layoutToLayoutDelegate;
    }
}
