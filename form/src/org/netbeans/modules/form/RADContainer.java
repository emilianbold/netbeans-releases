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

import java.util.ArrayList;

/**
 *
 * @author Ian Formanek
 */
public class RADContainer extends RADComponent implements ComponentContainer {
    private ArrayList subComponents;

    public RADComponent[] getSubBeans() {
        RADComponent[] components = new RADComponent [subComponents.size()];
        subComponents.toArray(components);
        return components;
    }

    public void initSubComponents(RADComponent[] initComponents) {
        subComponents = new ArrayList(initComponents.length);
        for (int i = 0; i < initComponents.length; i++) {
            subComponents.add(initComponents[i]);
            initComponents[i].setParentComponent(this);
        }
    }

    public void reorderSubComponents(int[] perm) {
        for (int i = 0; i < perm.length; i++) {
            int from = i;
            int to = perm[i];
            if (from == to) continue;
            Object value = subComponents.remove(from);
            if (from < to) {
                subComponents.add(to - 1, value);
            } else {
                subComponents.add(to, value);
            }
        }
//        getFormModel().fireComponentsReordered(this);
    }

    public void add(RADComponent comp) {
        subComponents.add(comp);
        comp.setParentComponent(this);
    }

    public void remove(RADComponent comp) {
        if (subComponents.remove(comp))
            comp.setParentComponent(null);
    }

    public int getIndexOf(RADComponent comp) {
        return subComponents.indexOf(comp);
    }

    /**
     * Called to obtain a Java code to be used to generate code to access the
     * container for adding subcomponents.  It is expected that the returned
     * code is either ""(in which case the form is the container) or is a name
     * of variable or method call ending with
     * "."(e.g. "container.getContentPane().").  This implementation returns
     * "", as there is no sense to add visual components to non-visual
     * containers
     * @return the prefix code for generating code to add subcomponents to this container
     */
    public String getContainerGenName() {
        return ""; // NOI18N
    }
}
