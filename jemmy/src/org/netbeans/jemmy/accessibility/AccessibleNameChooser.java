/*
 * Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License Version
 * 1.0 (the "License"). You may not use this file except in compliance with
 * the License. A copy of the License is available at http://www.sun.com/
 * 
 * The Original Code is the Jemmy library.
 * The Initial Developer of the Original Code is Alexandre Iline.
 * All Rights Reserved.
 * 
 * Contributor(s): Alexandre Iline.
 * 
 * $Id$ $Revision$ $Date$
 * 
 */

package org.netbeans.jemmy.accessibility;

import javax.accessibility.AccessibleContext;

import org.netbeans.jemmy.operators.Operator;
import org.netbeans.jemmy.operators.Operator.StringComparator;

public class AccessibleNameChooser extends AccessibilityChooser {
    String name;
    StringComparator comparator;
    public AccessibleNameChooser(String name, StringComparator comparator) {
        this.name = name;
        this.comparator = comparator;
    }
    public AccessibleNameChooser(String name) {
        this(name, Operator.getDefaultStringComparator());
    }
    public final boolean checkContext(AccessibleContext context) {
        return(comparator.equals(context.getAccessibleName(), name));
    }
    public String getDescription() {
        return("JComponent with \"" + name + "\" accessible name");
    }
}
