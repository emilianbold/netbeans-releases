/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
        RADComponent[] components = new RADComponent[subComponents.size()];
        for (int i=0; i < perm.length; i++)
            components[perm[i]] = (RADComponent) subComponents.get(i);

        subComponents.clear();
        subComponents.addAll(java.util.Arrays.asList(components));
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
