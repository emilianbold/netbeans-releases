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

package org.netbeans.modules.form;

import java.util.*;
import java.lang.reflect.*;
import javax.swing.*;
import org.openide.util.Mutex;

/** Factory for creating objects, registering CreationDescriptor classes
 * and related utility methods.
 *
 * @author Tomas Pavek
 */

public class CreationFactory {

    private static HashMap registry;

    private static boolean defaultDescriptorsCreated = false;

    private CreationFactory() {}

    // -----------
    // registry methods

    public static CreationDescriptor getDescriptor(Class cls) {
        CreationDescriptor cd = (CreationDescriptor)
                                getRegistry().get(cls.getName());
        if (cd == null && !defaultDescriptorsCreated
                && (cls.getName().startsWith("javax.swing.")
                    || cls.getName().startsWith("java.awt."))) {
            createDefaultDescriptors();
            cd = (CreationDescriptor)getRegistry().get(cls.getName());
        }
        return cd;
    }

    public static void registerDescriptor(CreationDescriptor desc) {
        getRegistry().put(desc.getDescribedClass().getName(), desc);
    }

    public static void unregisterDescriptor(CreationDescriptor desc) {
        if (registry != null)
            registry.remove(desc.getDescribedClass().getName());
    }

    // -----------
    // creation methods

    public static Object createDefaultInstance(final Class cls)
        throws Exception
    {
        final CreationDescriptor cd = getDescriptor(cls);
        
        return FormLAF.executeWithLookAndFeel(
            UIManager.getLookAndFeel().getClass().getName(),
            new Mutex.ExceptionAction () {
                public Object run() throws Exception {
                    return cd != null ? cd.createDefaultInstance() : cls.newInstance();
                }
            });
    }

    public Object createInstance(Class cls, final FormProperty[] props, int style)
        throws Exception
    {
        CreationDescriptor cd = getDescriptor(cls);
        if (cd != null) {
            final CreationDescriptor.Creator creator = cd.findBestCreator(props, style);
            if (creator != null) {
                return FormLAF.executeWithLookAndFeel(
                    UIManager.getLookAndFeel().getClass().getName(),
                    new Mutex.ExceptionAction () {
                        public Object run() throws Exception {
                            return creator.createInstance(props);
                        }
                    });
            }
        }
        return null;
    }

    public String getJavaCreationCode(Class cls, FormProperty[] props, int style) {
        CreationDescriptor cd = getDescriptor(cls);
        if (cd != null) {
            CreationDescriptor.Creator creator = cd.findBestCreator(props, style);
            if (creator != null)
                   creator.getJavaCreationCode(props);
        }
        return null;
    }

    // ------------
    // utility methods

    public static FormProperty[] getPropertiesForCreator(
                                           CreationDescriptor.Creator creator,
                                           FormProperty[] properties) {

        String[] propNames = creator.getPropertiesNames();
        FormProperty[] crProps = new FormProperty[propNames.length];

        for (int i=0; i < propNames.length; i++) {
            String propName = propNames[i];
            for (int j=0; j < properties.length; j++)
                if (propName.equals(properties[j].getName())) {
                    crProps[i] = properties[j];
                    break;
                }
            if (crProps[i] == null) return null; // missing property, should not happen
        }

        return crProps;
    }

    public static FormProperty[] getRemainingProperties(
                                           CreationDescriptor.Creator creator,
                                           FormProperty[] properties) {

        String[] propNames = creator.getPropertiesNames();
        FormProperty[] remProps = new FormProperty[properties.length - propNames.length];
        if (remProps.length == 0) return remProps;

        int ii = 0;
        for (int i=0; i < properties.length; i++) {
            String propName = properties[i].getName();
            for (int j=0; j < propNames.length; j++) {
                if (propName.equals(propNames[j])) break;
                if (j+1 == propNames.length) {
                    if (ii > remProps.length) return null; // should not happen
                    remProps[ii++] = properties[i];
                }
            }
        }

        return remProps;
    }

    public static boolean containsProperty(CreationDescriptor desc, String propName) {
        CreationDescriptor.Creator[] creators = desc.getCreators();
        if (creators == null) return false;

        for (int i=0; i < creators.length; i++) {
            String[] propNames = creators[i].getPropertiesNames();
            for (int j=0; j < propNames.length; j++)
                if (propNames[j].equals(propName))
                    return true;
        }
        return false;
    }

    public static FormProperty findProperty(String propName,
                                            FormProperty[] properties) {
        for (int i=0; i < properties.length; i++)
            if (properties[i].getName().equals(propName))
                return properties[i];
        return null;
    }

    /** Evaluates creators for array of properties.
     * (Useful for CreationDescriptor.findBestCreator(...) implementation.)
     * @return array of int - for each creator a count of placed properties
     */
    public static int[] evaluateCreators(CreationDescriptor.Creator[] creators,
                                         FormProperty[] properties,
                                         boolean changedOnly) {

        if (creators == null || creators.length == 0) return null;

        int[] placed = new int[creators.length];
        for (int i=0; i < properties.length; i++) {
            if (!changedOnly || properties[i].isChanged()) {
                String name = properties[i].getName();

                for (int j=0; j < creators.length; j++) {
                    String[] crNames = creators[j].getPropertiesNames();
                    for (int k=0; k < crNames.length; k++)
                        if (name.equals(crNames[k]))
                            placed[j]++;
                }
            }
        }
        return placed;
    }

    /** Finds the best creator upon given evaluation.
     * (Useful for CreationDescriptor.findBestCreator(...) implementation.)
     * @return index of most suitable creator
     */
    public static int getBestCreator(CreationDescriptor.Creator[] creators,
                                     int[] placed, boolean placeAllProps) {

        if (creators == null || creators.length == 0) return -1;

        int best = 0;
        int[] sizes = new int[creators.length];
        sizes[0] = creators[0].getPropertiesCount();

        if (placeAllProps)
            // find shortest creator with all properties placed
            for (int i=1; i < placed.length; i++) {
                sizes[i] = creators[i].getPropertiesCount();
                if (placed[i] > placed[best]
                    || (placed[i] == placed[best]
                        && sizes[i] < sizes[best]))
                    best = i;
            }
        else
            // find longest creator with all parameters provided by properties
            for (int i=1; i < placed.length; i++) {
                sizes[i] = creators[i].getPropertiesCount();
                int iDiff = sizes[i] - placed[i];
                int bestDiff = sizes[best] - placed[best];
                if (iDiff < bestDiff
                    || (iDiff == bestDiff
                        && sizes[i] > sizes[best]))
                    best = i;
            }

        return best;
    }

    // -----------
    // non-public methods

    static HashMap getRegistry() {
        if (registry == null)
            registry = new HashMap(40);
        return registry;
    }

    // ---------------------------------------------------
    // constructors descriptor for some "special" classes...

    private static void createDefaultDescriptors() {
        // borders ------------

        // LineBorder
        String[][] lineBorderConstructors = { 
            { "lineColor" },
            { "lineColor", "thickness" },
            { "lineColor", "thickness", "roundedCorners" }
        };
        Object[] defaultLineBorderParams = { java.awt.Color.black };
        registerDescriptor(new ConstructorsDescriptor(
                javax.swing.border.LineBorder.class,
                lineBorderConstructors, defaultLineBorderParams));

        // EtchedBorder
        String[][] etchedBorderConstructors = {
            { },
            { "highlightColor", "shadowColor" },
            { "etchType" },
            { "etchType", "highlightColor", "shadowColor" }
        };
//        EtchedBorder defEB = new EtchedBorder();
//        java.awt.Component auxComp = new javax.swing.JPanel();
        Object[] defaultEtchedBorderParams = { };
//            defEB.getHighlightColor(auxComp),
//            defEB.getShadowColor(auxComp)
//        };
        registerDescriptor(new ConstructorsDescriptor(
                javax.swing.border.EtchedBorder.class,
                etchedBorderConstructors, defaultEtchedBorderParams));

        // EmptyBorder
        String[][] emptyBorderConstructors = {
            { "borderInsets" }
        };
        Object[] defaultEmptyBorderParams = { new java.awt.Insets(1,1,1,1) };
        registerDescriptor(new ConstructorsDescriptor(
            javax.swing.border.EmptyBorder.class,
            emptyBorderConstructors, defaultEmptyBorderParams));

        // TitledBorder
        String[][] titledBorderConstructors = {
            { "title" },
            { "border", "title" },
            { "border", "title", "titleJustification", "titlePosition" },
            { "border", "title", "titleJustification", "titlePosition", "titleFont" },
            { "border", "title", "titleJustification", "titlePosition", "titleFont", "titleColor" },
            { "border" }
        };
        Object[] defaultTitledBorderParams = { null, "", new Integer(0), new Integer(0) };
        registerDescriptor(new ConstructorsDescriptor(
            javax.swing.border.TitledBorder.class,
            titledBorderConstructors, defaultTitledBorderParams));

        // CompoundBorder
        String[][] compoundBorderConstructors = {
            { },
            { "outsideBorder", "insideBorder" }
        };
        registerDescriptor(new ConstructorsDescriptor(
            javax.swing.border.CompoundBorder.class,
            compoundBorderConstructors, new Object[0]));

        // BevelBorder
        String[][] bevelBorderConstructors = {
            { "bevelType" },
            { "bevelType", "highlightOuterColor", "shadowOuterColor" },
            { "bevelType", "highlightOuterColor", "highlightInnerColor",
                           "shadowOuterColor", "shadowInnerColor" }
        };
//        BevelBorder defBB = new BevelBorder(BevelBorder.RAISED);
        Object[] defaultBevelBorderParams = {
            new Integer(javax.swing.border.BevelBorder.RAISED)
//            defBB.getHighlightOuterColor(auxComp),
//            defBB.getHighlightInnerColor(auxComp),
//            defBB.getShadowOuterColor(auxComp),
//            defBB.getShadowInnerColor(auxComp)
        };
        registerDescriptor(new ConstructorsDescriptor(
            javax.swing.border.BevelBorder.class,
            bevelBorderConstructors, defaultBevelBorderParams));

        // SoftBevelBorder
        registerDescriptor(new ConstructorsDescriptor(
            javax.swing.border.SoftBevelBorder.class,
            bevelBorderConstructors, defaultBevelBorderParams));

        // MatteBorder
        String[][] matteBorderConstructors = {
            { "tileIcon" },
            { "borderInsets", "tileIcon" },
            { "borderInsets", "matteColor" }
        };
        Object[] defaultMatteBorderParams = { new java.awt.Insets(1,1,1,1),
                                              java.awt.Color.black };
        registerDescriptor(new ConstructorsDescriptor(
            javax.swing.border.MatteBorder.class,
            matteBorderConstructors, defaultMatteBorderParams));

        // layouts --------------

        // BorderLayout
        String[][] borderLayoutConstructors = {
            { },
            { "hgap", "vgap" }
        };
        Object[] defaultLayoutParams = { };
        registerDescriptor(new ConstructorsDescriptor(
            java.awt.BorderLayout.class,
            borderLayoutConstructors, defaultLayoutParams));

        // FlowLayout
        String[][] flowLayoutConstructors = {
            { },
            { "alignment" },
            { "alignment", "hgap", "vgap" },
        };
        registerDescriptor(new ConstructorsDescriptor(
            java.awt.FlowLayout.class,
            flowLayoutConstructors, defaultLayoutParams));

        // GridBagLayout
        String[][] gridBagLayoutConstructors = {
            { }
        };
        registerDescriptor(new ConstructorsDescriptor(
            java.awt.GridBagLayout.class,
            gridBagLayoutConstructors, defaultLayoutParams));

        // GridLayout
        String[][] gridLayoutConstructors = {
            { "rows", "columns" },
            { "rows", "columns", "hgap", "vgap" }
        };
        registerDescriptor(new ConstructorsDescriptor(
            java.awt.GridLayout.class,
            gridLayoutConstructors, defaultLayoutParams));

        // CardLayout
        String[][] cardLayoutConstructors = {
            { },
            { "hgap", "vgap" }
        };
        registerDescriptor(new ConstructorsDescriptor(
            java.awt.CardLayout.class,
            cardLayoutConstructors, defaultLayoutParams));

        // AWT --------

        // Dialog
        Object[] defaultDialogParams = { new java.awt.Frame() };
        registerDescriptor(new ConstructorsDescriptor(
            java.awt.Dialog.class,
            null, defaultDialogParams));

        defaultDescriptorsCreated = true;
    }
}
