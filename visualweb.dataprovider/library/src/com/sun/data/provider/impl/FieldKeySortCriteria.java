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
