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

public class AccessibleDescriptionChooser extends AccessibilityChooser {
    String description;
    StringComparator comparator;
    public AccessibleDescriptionChooser(String description, StringComparator comparator) {
        this.description = description;
        this.comparator = comparator;
    }
    public AccessibleDescriptionChooser(String description) {
        this(description, Operator.getDefaultStringComparator());
    }
    public final boolean checkContext(AccessibleContext context) {
        return(comparator.equals(context.getAccessibleDescription(), description));
    }
    public String getDescription() {
        return("JComponent with \"" + description + "\" accessible description");
    }
}
