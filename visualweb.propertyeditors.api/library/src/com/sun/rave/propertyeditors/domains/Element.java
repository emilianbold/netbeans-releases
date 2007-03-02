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
package com.sun.rave.propertyeditors.domains;

import com.sun.rave.propertyeditors.util.JavaInitializer;
import java.lang.Comparable;

/**
 * An individual item in the set of items returned by a {@link Domain}. An
 * element consists of a value, an optional label (for display purposes), and
 * an optional description. Any component of an element may be null.
 */

public class Element implements Comparable {

    /**
     * An empty array of elements.
     */
    public static final Element[] EMPTY_ARRAY = new Element[0];

    String description = null;
    String label = null;
    Object value = null;

    /**
     * Construct a new instance that uses value.toString() as the label, and
     * no description.
     *
     * @param value Property value that should be set if this element
     *  is selected
     */
    public Element(Object value) {
        this(value, (value == null ? "" : value.toString()), null);
    }


    /**
     * Construct a new instance that has an element value and localized
     * label, but no description.
     *
     * @param value Property value that should be set if this element
     *  is selected
     * @param label Localized label for this element value
     */
    public Element(Object value, String label) {
        this(value, label, null);
    }


    /**
     * Construct a new, fully configured, instance.
     *
     * @param value Property value that should be set if this element
     *  is selected
     * @param label Localized label for this element value
     * @param description Optional localized description of this element
     */
    public Element(Object value, String label, String description) {
        this.value = value;
        this.label = label;
        this.description = description;
    }

    /**
     * Return the localized description for this element value. Returns null if
     * there is no description.
     */
    public String getDescription() {
        return this.description;
    }


    /**
     * Return the localized label for this element value. If not specified,
     * the Stringified version of the object value is used instead. If no label
     * or value is present, returns null.
     */
    public String getLabel() {
        return this.label;
    }


    /**
     * Return the element value for this element. Returns null if there is no
     * value.
     */
    public Object getValue() {
        return this.value;
    }

    /**
     * An element is equal to another if their values are equal.
     */
    public boolean equals( Object obj ) {
        if (this == obj)
            return true;
        if (!(obj instanceof Element))
            return false;
        if (this.value == null)
            return ((Element) obj).value == null ? true : false;
        return this.value.equals(((Element) obj).value);
    }

    /**
     * Returns the element's label's hash code.
     */
    public int hashCode() {
        return this.label == null? 0 : this.label.hashCode();
    }

    /**
     * Compare this element to another element. If the elements' values implement
     * {@link java.lang.Comparable}, then their values are compared. Otherwise, their
     * labels are compared.
     */
    public int compareTo(Object obj) {
        if (this == obj)
            return 0;
        if (!(obj instanceof Element))
            return 1;
        Element that = (Element) obj;
        if (this.value == null)
            return that.value == null ? 0 : -1;
        if (that.value == null)
            return 1;
        if (this.value instanceof Comparable)
            return ((Comparable) this.value).compareTo(that.value);
        return this.label.compareTo(that.label);
    }

    /**
     * By default, this method passes the element's value to
     * {@link com.sun.rave.propertyeditors.util.JavaInitializer#toJavaInitializationString(Object)}.
     * Classes that need special behavior should override this method.
     */
    public String getJavaInitializationString() {
        return JavaInitializer.toJavaInitializationString(this.getValue());
    }

}
