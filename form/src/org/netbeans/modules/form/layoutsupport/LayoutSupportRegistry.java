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

import org.openide.nodes.Node;
import org.netbeans.modules.form.*;

/**
 * @author Tomas Pavek
 */

public class LayoutSupportRegistry {

    private static HashMap containerToLayoutDelegate;
    private static HashMap layoutToLayoutDelegate;

    // --------------
    // getting methods

    public static Class getLayoutDelegateForContainer(Class containerClass) {
        String className = (String)
            getContainersMap().get(containerClass.getName());
        if (className == null)
            className = findSuperClass(getContainersMap(), containerClass);

        if (className != null) {
            try {
                return Class.forName(className);
            }
            catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static String getLayoutDelegateForContainer(String containerClassName) {
        String className = (String) getContainersMap().get(containerClassName);
        if (className == null) {
            try {
                className = findSuperClass(getContainersMap(),
                                           Class.forName(containerClassName));
            }
            catch (ClassNotFoundException e) {} // ignore
        }
        return className;
    }

    public static Class getLayoutDelegateForLayout(Class layoutClass) {
        String className = (String)
            getLayoutsMap().get(layoutClass.getName());
        if (className == null)
            className = findSuperClass(getLayoutsMap(), layoutClass);

        if (className != null) {
            try {
                return Class.forName(className);
            }
            catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
//        else return NullLayoutSupport.class;

        return null;
    }

    public static String getLayoutDelegateForLayout(String layoutClassName) {
        String className = (String) getLayoutsMap().get(layoutClassName);
        if (className == null) {
            try {
                className = findSuperClass(getLayoutsMap(),
                                           Class.forName(layoutClassName));
            }
            catch (ClassNotFoundException e) {} // ignore
        }
        return className;
    }

    // ------------
    // registering methods

    public static void registerLayoutDelegateForContainer(
                           Class containerClass,
                           Class layoutDelegateClass)
    {
        getContainersMap().put(containerClass.getName(),
                               layoutDelegateClass.getName());
    }

    public static void registerLayoutDelegateForContainer(
                           String containerClassName,
                           String layoutDelegateClassName)
    {
        getContainersMap().put(containerClassName, layoutDelegateClassName);
    }

    public static void registerLayoutDelegateForLayout(
                           Class layoutClass,
                           Class layoutDelegateClass)
    {
        getLayoutsMap().put(layoutClass.getName(),
                            layoutDelegateClass.getName());
    }

    public static void registerLayoutDelegateForLayout(
                           String layoutClassName,
                           String layoutDelegateClassName)
    {
        getLayoutsMap().put(layoutClassName, layoutDelegateClassName);
    }

    // ------------
    // creating methods

    public static LayoutSupportDelegate createLayoutDelegate(Class delegateClass)
        throws InstantiationException, IllegalAccessException
    {
        return (LayoutSupportDelegate) delegateClass.newInstance();
    }

    public static LayoutSupportDelegate createLayoutDelegate(
                                            String delegateClassName)
        throws ClassNotFoundException,
               InstantiationException,
               IllegalAccessException
    {
        Class delegateClass = Class.forName(delegateClassName);
        return (LayoutSupportDelegate) delegateClass.newInstance();
    }

    public static LayoutSupportDelegate createLayoutDelegate(
                                            Container container,
                                            Container containerDelegate)
    {
        Class layoutDelegateClass = getLayoutDelegateForContainer(
                                        container.getClass());

        if (layoutDelegateClass == null) {
            // the container must be empty by default, it cannot be designed
            // as container otherwise
            if (containerDelegate.getComponentCount() > 0)
                return null;
            
            // try to find support for LayoutManager used by the container
            LayoutManager lm = containerDelegate.getLayout();
            if (lm == null)
                return null; // not NullLayoutSupport

            layoutDelegateClass = getLayoutDelegateForLayout(lm.getClass());
            if (layoutDelegateClass == null)
                return null;
        }

        try {
            return (LayoutSupportDelegate) layoutDelegateClass.newInstance();
            // [shouldn't we use CreationFactory ??]
        }
        catch (Exception ex) {
            if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                System.err.println("[WARNING] Cannot create layout manager instance: " // NOI18N
                                   + layoutDelegateClass.getName());
                ex.printStackTrace();
            }
        }
        return null;
    }

    // -----------
    // private methods

    private static String findSuperClass(Map map, Class subClass) {
        for (Iterator it=map.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry en = (Map.Entry) it.next();
            String className = (String) en.getKey();
            try {
                Class keyClass = Class.forName(className);
                if (keyClass.isAssignableFrom(subClass))
                    return (String) en.getValue();
            }
            catch (Exception ex) {}
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
