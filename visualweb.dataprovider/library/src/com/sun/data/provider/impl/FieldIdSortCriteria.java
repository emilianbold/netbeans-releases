/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
