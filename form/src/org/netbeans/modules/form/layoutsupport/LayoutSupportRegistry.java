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
 *
 * @author Tomas Pavek
 */

public class LayoutSupportRegistry {

    private static HashMap containerToLayoutSupport;
    private static HashMap layoutToLayoutSupport;

    // --------------
    // getting methods

    public static Class getLayoutSupportForContainer(Class containerClass) {
        String className = (String)
            getContainersMap().get(containerClass.getName());

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

    public static String getLayoutSupportForContainer(String containerClassName) {
        return (String) getContainersMap().get(containerClassName);
    }

    public static Class getLayoutSupportForLayout(Class layoutClass) {
        String className = (String)
            getLayoutsMap().get(layoutClass.getName());

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

    public static String getLayoutSupportForLayout(String layoutClassName) {
        return (String) getLayoutsMap().get(layoutClassName);
    }

    public static Node.Property[] getLayoutProperties(LayoutSupport ls) {
        Node.PropertySet[] propsets = ls.getPropertySets();
        if (propsets == null || propsets.length == 0)
            return new Node.Property[0];
        if (propsets.length == 1)
            return propsets[0].getProperties();

        ArrayList proplist = new ArrayList(10);
        for (int i=0; i < propsets.length; i++) {
            if ("properties".equals(propsets[i].getName()) // NOI18N
                    || "properties2".equals(propsets[i].getName())) { // NOI18N
                Node.Property[] props = propsets[i].getProperties();
                for (int j=0; j < props.length; j++)
                    proplist.add(props[j]);
            }
        }
        Node.Property[] properties = new Node.Property[proplist.size()];
        proplist.toArray(properties);
        return properties;
    }

    // ------------
    // registering methods

    public static void registerLayoutSupportForContainer(Class containerClass,
                                                    Class layoutSupportClass) {
        getContainersMap().put(containerClass.getName(),
                               layoutSupportClass.getName());
    }

    public static void registerLayoutSupportForContainer(
                    String containerClassName, String layoutSupportClassName) {
        getContainersMap().put(containerClassName, layoutSupportClassName);
    }

    public static void registerLayoutSupportForLayout(Class layoutClass,
                                                    Class layoutSupportClass) {
        getLayoutsMap().put(layoutClass.getName(),
                            layoutSupportClass.getName());
    }

    public static void registerLayoutSupportForLayout(String layoutClassName,
                                               String layoutSupportClassName) {
        getLayoutsMap().put(layoutClassName, layoutSupportClassName);
    }

    // ------------
    // creating methods

    public static LayoutSupport createLayoutSupport(Class layoutSupportClass)
    throws InstantiationException, IllegalAccessException {
        return (LayoutSupport) layoutSupportClass.newInstance();
    }

    public static LayoutSupport createLayoutSupport(String laysupClassName)
    throws ClassNotFoundException, InstantiationException,
           IllegalAccessException {

        Class layoutSupportClass = Class.forName(laysupClassName);
        return (LayoutSupport) layoutSupportClass.newInstance();
    }

    public static LayoutSupport copyLayoutSupport(LayoutSupport laysup,
                                                  RADVisualContainer targetCont)
    throws InstantiationException, IllegalAccessException {

        if (laysup == null) return null;
        LayoutSupport newLaysup = createLayoutSupport(laysup.getClass());
        if (newLaysup == null) return null;
        newLaysup.initialize(targetCont);

        Node.Property[] sourceProps = getLayoutProperties(laysup);
        Node.Property[] targetProps = getLayoutProperties(newLaysup);
        FormUtils.copyProperties(sourceProps, targetProps, true, false);

        return newLaysup;
    }

    // -----------
    // private methods

    private static Map getContainersMap() {
        if (containerToLayoutSupport == null) {
            containerToLayoutSupport = new HashMap();
            // fill in default containers
            containerToLayoutSupport.put(
                "javax.swing.JScrollPane",
                "org.netbeans.modules.form.layoutsupport.dedicated.JScrollPaneSupport");
            containerToLayoutSupport.put(
                "javax.swing.JSplitPane",
                "org.netbeans.modules.form.layoutsupport.dedicated.JSplitPaneSupport");
            containerToLayoutSupport.put(
                "javax.swing.JTabbedPane",
                "org.netbeans.modules.form.layoutsupport.dedicated.JTabbedPaneSupport");
        }
        return containerToLayoutSupport;
    }

    private static Map getLayoutsMap() {
        if (layoutToLayoutSupport == null) {
            layoutToLayoutSupport = new HashMap();
            // fill in default layouts
            layoutToLayoutSupport.put(
                "java.awt.BorderLayout",
                "org.netbeans.modules.form.layoutsupport.BorderLayoutSupport");
            layoutToLayoutSupport.put(
                "java.awt.FlowLayout",
                "org.netbeans.modules.form.layoutsupport.FlowLayoutSupport");
            layoutToLayoutSupport.put(
                "javax.swing.BoxLayout",
                "org.netbeans.modules.form.layoutsupport.BoxLayoutSupport");
            layoutToLayoutSupport.put(
                "java.awt.GridBagLayout",
                "org.netbeans.modules.form.layoutsupport.GridBagLayoutSupport");
            layoutToLayoutSupport.put(
                "java.awt.GridLayout",
                "org.netbeans.modules.form.layoutsupport.GridLayoutSupport");
            layoutToLayoutSupport.put(
                "java.awt.CardLayout",
                "org.netbeans.modules.form.layoutsupport.CardLayoutSupport");
            layoutToLayoutSupport.put(
                "org.netbeans.lib.awtextra.AbsoluteLayout",
                "org.netbeans.modules.form.layoutsupport.AbsoluteLayoutSupport");
        }
        return layoutToLayoutSupport;
    }
}
