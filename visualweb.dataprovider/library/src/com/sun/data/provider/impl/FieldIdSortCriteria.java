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
 * <p>The FieldIdSortCriteria class is an implementation of SortCriteria that
 * simply retrieves the sort value from the {@link TableDataProvider} for the
 * current {@link RowKey} using the specified fieldId, which is used to
 * dynamically retrieve a {@link FieldKey}.</p>
 *
 * @author Joe Nuxoll
 */
public class FieldIdSortCriteria extends SortCriteria {

    /**
     * Constructs a FieldIdSortCriteria with no associated {@link FieldKey}.
     */
    public FieldIdSortCriteria() {}

    /**
     * Constructs a FieldIdSortCriteria with the specified fieldId to use to
     * fetch a {@link FieldKey}.
     *
     * @param fieldId The desired fieldId to use to fetch a FieldKey
     */
    public FieldIdSortCriteria(String fieldId) {
        this.fieldId = fieldId;
    }

    /**
     * Constructs a FieldIdSortCriteria with the specified fieldId and
     * ascending state.
     *
     * @param fieldId The desired fieldId to use to retrieve a FieldKey
     * @param ascending The desired boolean state for the ascending property
     */
    public FieldIdSortCriteria(String fieldId, boolean ascending) {
        this.fieldId = fieldId;
        super.setAscending(ascending);
    }

    /**
     * Returns the fieldId to use to retrieve a FieldKey for this sort criteria.
     *
     * @return The currently set fieldId for this sort criteria
     */
    public String getFieldId() {
        return fieldId;
    }

    /**
     * Sets the fieldId to use to fetch a FieldKey for this sort criteria.
     *
     * @param fieldId The desired fieldId for this sort criteria
     */
    public void setFieldId(String fieldId) {
        this.fieldId = fieldId;
    }

    /**
     * <p>If no display name is set, this returns the fieldId.</p>
     *
     * {@inheritDoc}
     */
    public String getDisplayName() {
        String name = super.getDisplayName();
        if ((name == null || "".equals(name)) && fieldId != null && !"".equals(fieldId)) {
            return fieldId;
        }
        return name;
    }

    /**
     * Returns the fieldId.
     *
     * {@inheritDoc}
     */
    public String getCriteriaKey() {
        return fieldId != null ? fieldId : ""; // NOI18N
    }

    /**
     * <p>Returns the value from the {@link TableDataProvider} stored under the
     * {@link FieldKey} (retrieved using the fieldId) and {@link RowKey}.</p>
     *
     * {@inheritDoc}
     */
    public Object getSortValue(TableDataProvider provider, RowKey row)
        throws DataProviderException {

        FieldKey fieldKey = provider.getFieldKey(fieldId);
        return provider.getValue(fieldKey, row);
    }

    private String fieldId;
}
