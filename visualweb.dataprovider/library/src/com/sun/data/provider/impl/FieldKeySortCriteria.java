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

package com.sun.data.provider.impl;

import com.sun.data.provider.DataProviderException;
import com.sun.data.provider.FieldKey;
import com.sun.data.provider.RowKey;
import com.sun.data.provider.SortCriteria;
import com.sun.data.provider.TableDataProvider;

/**
 * <p>The FieldKeySortCriteria class is an implementation of SortCriteria that
 * simply retrieves the sort value from the {@link TableDataProvider} for the
 * current {@link RowKey} using the specified {@link FieldKey}.</p>
 *
 * @author Joe Nuxoll
 */
public class FieldKeySortCriteria extends SortCriteria {

    /**
     * Constructs a FieldKeySortCriteria with no associated {@link FieldKey}.
     */
    public FieldKeySortCriteria() {}

    /**
     * Constructs a FieldKeySortCriteria with the specified {@link FieldKey}.
     *
     * @param fieldKey The desired FieldKey
     */
    public FieldKeySortCriteria(FieldKey fieldKey) {
        this.fieldKey = fieldKey;
    }

    /**
     * Constructs a FieldKeySortCriteria with the specified {@link FieldKey} and
     * ascending state.
     *
     * @param fieldKey The desired FieldKey
     * @param ascending The desired boolean state for the ascending property
     */
    public FieldKeySortCriteria(FieldKey fieldKey, boolean ascending) {
        this.fieldKey = fieldKey;
        this.setAscending(ascending);
    }

    /**
     * Returns the FieldKey to use for this sort criteria.
     *
     * @return The currently set FieldKey for this sort criteria
     */
    public FieldKey getFieldKey() {
        return fieldKey;
    }

    /**
     * Sets the FieldKey for this sort criteria.
     *
     * @param fieldKey The desired FieldKey for this sort criteria
     */
    public void setFieldKey(FieldKey fieldKey) {
        this.fieldKey = fieldKey;
    }

    /**
     * <p>If no display name is set, this returns the {@link FieldKey}'s
     * display name.</p>
     *
     * {@inheritDoc}
     */
    public String getDisplayName() {
        String name = super.getDisplayName();
        if ((name == null || "".equals(name)) && fieldKey != null) {
            return fieldKey.getDisplayName();
        }
        return name;
    }

    /**
     * Returns the FieldKey's fieldId.
     *
     * {@inheritDoc}
     */
    public String getCriteriaKey() {
        return fieldKey != null ? fieldKey.getFieldId() : ""; // NOI18N
    }

    /**
     * <p>Returns the value from the {@link TableDataProvider} stored under the
     * {@link FieldKey} and {@link RowKey}.</p>
     *
     * {@inheritDoc}
     */
    public Object getSortValue(TableDataProvider provider, RowKey row)
        throws DataProviderException {

        return provider.getValue(fieldKey, row);
    }

    private FieldKey fieldKey;
}
