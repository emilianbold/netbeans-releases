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

import com.sun.rave.propertyeditors.util.Bundle;
import java.util.Locale;

/**
 * Abstract representation of a set of binary tuples that represents
 * the domain of values for a particular property. Domain elements are
 * represented by instances of class {@link Element}. Concrete subclasses of
 * this class may provide a static list of items, or calculate the list
 * dynamically based on the current state of the associated components. If the
 * number of elements in a domain is large, it is recommended that the elements
 * be returned in sorted order, according to the natural ordering of the element
 * labels.
 *
 * <p>A {@link Domain} implementation that needs access to the dynamic
 * context should extend {@link AttachedDomain} instead of this class.
 *
 * <p>{@link EditableDomain} should be used to represent domains which users may
 * modify by the addition or removal or modification of elements.
 *
 * <p><strong>Nota Bena:</strong> No check is made for duplicate elements.
 */
public abstract class Domain  {

    /**
     * The localizing <code>Bundle</code> for this package. Classes in other
     * packages that extend <code>Domain</code> must provide their own
     * localization solution.
     */
    static final Bundle bundle = Bundle.getBundle(Domain.class);

    /**
     * Returns the display name of the element type that this domain represents.
     * This name may be used in messages, labels, and window titles. By default,
     * returns null. If null is returned, editors will generally use the property's
     * display name.
     */
    public String getDisplayName() {
        return null;
    }

    /**
     * Returns an array of {@link Element}s that represent the legal values
     * to which our associated property may be set. If there are no such
     * legal values, a zero-length array is returned. This method must never
     * return null.
     *
     * <p>The returned {@link Element}s should be in the order most natural
     * for presentation to a user in a property editor.
     */
    abstract public Element[] getElements();

    /**
     * Returns the element at the index indicated, or null if there is no element
     * at the index indicated, or if the index is out of bounds.
     */
    public Element getElementAt(int index) {
        if (index < 0 || index > getElements().length)
            return null;
        return getElements()[index];
    }

    /**
     * Returns the index of the element indicated, of -1 if the element does
     * not exist in this domain. If an element is present more than once, the
     * index of the first one encountered is returned.
     */
    public int getIndexOf(Element element) {
        Element[] elements = getElements();
        for (int i = 0; i < elements.length; i++) {
            if (elements[i].equals(element))
                return i;
        }
        return -1;
    }

    /**
     * Returns the number of elements in the domain.
     */
    public int getSize() {
        return getElements().length;
    }

    /**
     * Returns true if this domain corresponds to a required property. A
     * required property is one for which a "null" or "empty" value is not valid.
     * If this method returns false, property editors must make a "null" or
     * "unset" option available. By default, this method returns <code>false</code>.
     */
    public boolean isRequired() {
        return false;
    }

    /**
     * Returns the unique property help id that maps to the help topic for this
     * property editor. By default, returns null. Extending classes that provide
     * help should override this method.
     */
    public String getPropertyHelpId() {
        return null;
    }

}
