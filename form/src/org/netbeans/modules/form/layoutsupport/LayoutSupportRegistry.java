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
import java.lang.ref.*;

import org.openide.loaders.*;
import org.openide.filesystems.*;
import org.openide.ErrorManager;

import org.netbeans.modules.form.FormModel;
import org.netbeans.modules.form.FormUtils;
import org.netbeans.modules.form.palette.PaletteItem;
import org.netbeans.modules.form.palette.PaletteUtils;

/**
 * Registry and factory class for LayoutSupportDelegate implementations.
 *
 * @author Tomas Pavek
 */

public class LayoutSupportRegistry {

    private static Map containerToLayoutDelegate;
    private static Map layoutToLayoutDelegate;

    private static boolean needPaletteRescan = true;

    public static final String DEFAULT_SUPPORT = "<default>"; // NOI18N

    private static FileChangeListener paletteListener;

    private static Map instanceMap;

    private Reference formModelRef;

    // -------

    private LayoutSupportRegistry(FormModel formModel) {
        this.formModelRef = new WeakReference(formModel);
    }

    public static LayoutSupportRegistry getRegistry(FormModel formModel) {
        LayoutSupportRegistry reg;
        if (instanceMap == null) {
            instanceMap = new WeakHashMap(); 
            reg = null;
        }
        else reg = (LayoutSupportRegistry) instanceMap.get(formModel);

        if (reg == null) {
            reg = new LayoutSupportRegistry(formModel);
            instanceMap.put(formModel, reg);
        }

        return reg;
    }

    // --------------
    // get methods

    public Class getSupportClassForContainer(Class containerClass) {
        String className = (String)
                           getContainersMap().get(containerClass.getName());
        if (className == null) {
            className = findSuperClass(getContainersMap(), containerClass);
//            if (className == null && needPaletteRescan) {
//                className = scanPalette(containerClass.getName());
//                if (className == null) // try container superclass again
//                    className = findSuperClass(getContainersMap(),
//                                               containerClass);
//            }
        }

        return className != null ? loadClass(className) : null;
    }

    public String getSupportNameForContainer(String containerClassName) {
        String className = (String) getContainersMap().get(containerClassName);
        if (className == null) {
            Class containerClass = loadClass(containerClassName);
            if (containerClass != null)
                className = findSuperClass(getContainersMap(), containerClass);
//            if (className == null && needPaletteRescan) {
//                className = scanPalette(containerClassName);
//                if (className == null) // try container superclass again
//                    className = findSuperClass(getContainersMap(),
//                                               containerClass);
//            }
        }

        return className;
    }

    public Class getSupportClassForLayout(Class layoutClass) {
        String className = (String) getLayoutsMap().get(layoutClass.getName());
        if (className == null && needPaletteRescan)
            className = scanPalette(layoutClass.getName());
        if (className == null)
            className = findSuperClass(getLayoutsMap(), layoutClass);

        return className != null ? loadClass(className) : null;
    }

    public String getSupportNameForLayout(String layoutClassName) {
        String className = (String) getLayoutsMap().get(layoutClassName);
        if (className == null && needPaletteRescan)
            className = scanPalette(layoutClassName);
        if (className == null) {
            Class layoutClass = loadClass(layoutClassName);
            if (layoutClass != null)
                className = findSuperClass(getLayoutsMap(), layoutClass);
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
    // creation methods

    public LayoutSupportDelegate createSupportForContainer(Class containerClass)
        throws ClassNotFoundException,
               InstantiationException,
               IllegalAccessException
    {
        Class delegateClass = getSupportClassForContainer(containerClass);
        if (delegateClass == null)
            return null;

        return (LayoutSupportDelegate) delegateClass.newInstance();
    }

    public LayoutSupportDelegate createSupportForLayout(Class layoutClass)
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

    private String findSuperClass(Map map, Class subClass) {
        for (Iterator it=map.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry en = (Map.Entry) it.next();
            String className = (String) en.getKey();
            Class keyClass = loadClass(className);
            if (keyClass != null && keyClass.isAssignableFrom(subClass))
                return (String) en.getValue();
        }
        return null;
    }

    private static String scanPalette(String wantedClassName) {
        FileObject paletteFolder = PaletteUtils.getPaletteFolder();

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

            paletteFolder.addFileChangeListener(paletteListener);
        }

        String foundSupportClassName = null;

        FileObject[] paletteCategories = paletteFolder.getChildren();
        for (int i=0; i < paletteCategories.length; i++) {
            FileObject categoryFolder = paletteCategories[i];
            if (!categoryFolder.isFolder())
                continue;
           
            if (newPaletteListener)
                categoryFolder.addFileChangeListener(paletteListener);

            FileObject[] paletteItems = categoryFolder.getChildren();
            for (int j=0; j < paletteItems.length; j++) {
                DataObject itemDO = null;
                try {
                    itemDO = DataObject.find(paletteItems[j]);
                }
                catch (DataObjectNotFoundException ex) {
                    continue;
                }

                PaletteItem item = (PaletteItem)
                                   itemDO.getCookie(PaletteItem.class);
                if (item == null || !item.isLayout())
                    continue;

                Class itemClass = item.getComponentClass();
                if (itemClass == null)
                    continue; // cannot resolve class - ignore

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
                        org.openide.ErrorManager.getDefault().notify(
                            org.openide.ErrorManager.INFORMATIONAL, ex);
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

    private Class loadClass(String className) {
        try {
            return FormUtils.loadClass(className, (FormModel)formModelRef.get());
        }
        catch (Exception ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        catch (LinkageError ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        return null;
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
