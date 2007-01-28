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


package com.sun.data.provider;


import java.io.Serializable;

/**
 * <p>FieldKey is a representation of an identifier for a specific data
 * element that may be retrieved from a {@link DataProvider}.  Specialized
 * implementations might also provide extra capabilities for navigation
 * between elements, or other value added services.</p>
 *
 * <p>FieldKey implements Comparable, to allow for sorting based on the
 * displayName of the FieldKey.  Note that the Comparable equals test may
 * not correspond to the FieldKey.equals(...) test, as the Comparable
 * implementation is working with the displayName, while the equals(...)
 * method works with the fieldId.</p>
 *
 * @author Joe Nuxoll
 * @author Craig McClanahan
 */
public class FieldKey implements Comparable, Serializable {

    /**
     * A convenient static empty array to use for no-op method returns
     */
    public static final FieldKey[] EMPTY_ARRAY = new FieldKey[0];

    /**
     * Constructs a new FieldKey with the specified canonical ID.
     *
     * @param fieldId The desired canonical ID String
     */
    public FieldKey(String fieldId) {
        this.fieldId = fieldId;
        this.displayName = this.fieldId;
    }

    /**
     * Constructs a new FieldKey with the specified canonical ID and display
     * name.
     *
     * @param fieldId The desired canonical ID String for this field
     * @param displayName The desired display name String
     */
    public FieldKey(String fieldId, String displayName) {
        this.fieldId = fieldId;
        this.displayName = displayName;
    }

    /**
     * @param fieldId the canonical internal identifier of this {@link FieldKey}
     */
    public void setFieldId(String fieldId) {
        this.fieldId = fieldId;
    }

    /**
     * @return the canonical internal identifier of this {@link FieldKey}
     */
    public String getFieldId() {
        return fieldId;
    }

    /**
     * @param displayName The display name for this data element, suitable for
     * inclusion in a menu of available options.
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * @return the display name for this data element, suitable for
     * inclusion in a menu of available options.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Standard equals implementation.  This method compares the FieldKey id
     * values for equality.
     *
     * @param o the Object to check equality
     * @return true if equal, false if not
     * @see Object#equals(Object)
     */
    public boolean equals(Object o) {
        if (o instanceof FieldKey) {
            String fkFieldId = ((FieldKey)o).getFieldId();
            String thisFieldId = getFieldId();
            return thisFieldId == fkFieldId || (thisFieldId != null && thisFieldId.equals(fkFieldId));
        }
        return false;
    }

    /**
     * Standard compareTo implementation (for {@link Comparable} interface).
     * This method compares FieldKeys for sorting by comparing the displayName
     * values.
     *
     * @param o Object to compare
     * @return a negative integer, zero, or a positive integer as this object
     *         is less than, equal to, or greater than the specified object.
     * @see Comparable#compareTo(Object)
     */
    public int compareTo(Object o) {
        String thisDisplayName = getDisplayName();
        if (o instanceof FieldKey && thisDisplayName != null) {
            String fkDisplayName = ((FieldKey)o).getDisplayName();
            return thisDisplayName.compareTo(fkDisplayName);
        }
        return 0;
    }

    private String fieldId;
    private String displayName;

    public String toString() {
        return "FieldKey[" + getFieldId() + "]"; // NOI18N
    }
}
