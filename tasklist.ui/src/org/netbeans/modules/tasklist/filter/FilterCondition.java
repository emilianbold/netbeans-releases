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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.tasklist.filter;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.JComponent;


/**
 * A condition (type and relation pair) in the filter.
 * Don't forget to override sameType if you extend this class!
 *
 * @author Tor Norbye
 */
abstract class FilterCondition {
//    transient private String name = null;

    /**
     * Creates a condition with the given name.
     *
     * @param name user visible name of the filter condition
     */
    public FilterCondition() {
//      this.name = null;
    }

    /**
     * Copy constructor. Use from subclassed clone.
     */
    protected FilterCondition(final FilterCondition rhs) {
//        this.name = rhs.name;
    }

    /** 
     * Deep clone, please implement in subclass.
     */
    public abstract Object clone() ;
    
//    /**
//     * Returns the user visible name of this condition
//     *
//     * @return name of this condition
//     */
//    public String getName() {
//      if (this.name == null) this.name = getDisplayName();
//      return this.name;
//    }
    
    /**
     * Compares two objects.
     *
     * @param obj value of the property
     */
    public abstract boolean isTrue(Object obj);

    /**
     * Creates a component that will represent a constant within the 
     * filter dialog. It should support {@link #PROP_VALUE_VALID}
     * client property.
     *
     * @return created component or null if no component 
     */
    public JComponent createConstantComponent() {
        return null;
    }

    /**
     * Gets constant from the specified component and save it.
     * This method should be also implemented if createConstantComponent()
     * is implemented.
     *
     * @param cmp with createConstantComponent() create component
     */
    public void getConstantFrom(JComponent cmp) {
        assert cmp != null : "getConstantFrom() is not implemented!"; //NOI18N
    }

    final boolean isValueValid(JComponent cmp) {
        Boolean valid = (Boolean) cmp.getClientProperty(PROP_VALUE_VALID);
        if (valid == null) {
            return true;
        } else {
            return valid.booleanValue();
        }
    }

    /**
     * Checks whether fc is of the same type.
     * This method will be used to replace a condition created with
     * Filter.getConditionsFor(Node.Property) with one contained in a filter.
     * This method should return true also if this and fc have different 
     * constants for comparing with property values.
     *
     * @param fc another condition
     * @return true fc is of the same type as this
     */
     public boolean sameType(FilterCondition fc) {
       return fc.getClass() == getClass();
     }
    
    public String toString() {
        return getClass().getName() + 
            "[name=" + getDisplayName() + "]"; // NOI18N
//        return getClass().getName() + 
//            "[name=" + name + "]"; // NOI18N
    }

    /** Use this client property on value/contant components to indicate valid user data.*/
    public static final String PROP_VALUE_VALID = "value-valid"; //NOI18N


    protected abstract String getDisplayName();
    
    abstract void load( Preferences prefs, String prefix ) throws BackingStoreException;
    
    abstract void save( Preferences prefs, String prefix ) throws BackingStoreException;
}

