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

package com.sun.data.provider;

import java.io.Serializable;

/**
 * <p>RowKey is a representation of an identifier for a specific data row that
 * may be retrieved from a {@link TableDataProvider}.  Specialized
 * implementations might also provide extra capabilities for navigation
 * between rows, or other value added services.</p>
 *
 * <p>A RowKey (and rowId) is expected to remain valid for as long as possible -
 * meaning when a RowKey is fetched from a TableDataProvider, it is considered
 * an address of a particular row in that TableDataProvider.  If rows have been
 * added or removed from the TableDataProvider, a previously fetched RowKey
 * should still represent the row it did when it was first retrieved.  A common
 * strategy for TableDataProvider implementations is to store intrinsicly
 * &quot;primary key-like&quot; data from the underlying data source inside of a
 * specialized RowKey implementation.  Another strategy is to store a random
 * hash index in the RowKeys, and maintain a map inside the TableDataProvider
 * implementation to resolve the RowKeys back to the underlying data rows.  This
 * insolates consumers of the TableDataProvider implementation from row index
 * changes (due to inserts, deletes, etc) in the underlying data source.</p>
 *
 * <p>At any point a user might call {@link TableDataProvider#getRowKey(String)}
 * in order to fetch a valid RowKey for a particular rowId, so the
 * TableDataProvider must be capable of resolving a rowId back to a unique
 * RowKey.</p>
 *
 * <p>RowKey implements Comparable so that batched deletes and inserts can be
 * done in reverse order to help ensure consistency of row order.  This is only
 * for blind operations implemented where there is no knowledge of a specific
 * RowKey or TableDataProvider implementations.  A RowKey implementation might
 * not support intrinsic ordering of any type.</p>
 *
 * @author Joe Nuxoll
 */
public class RowKey implements Serializable, Comparable {

    /**
     * A convenient static empty array to use for no-op method returns
     */
    public static final RowKey[] EMPTY_ARRAY = new RowKey[0];

    /**
     * Constructs an uninitialized RowKey.
     */
    public RowKey() { }

    /**
     * Constructs a new RowKey with the specified canonical ID.
     *
     * @param rowId The desired canonical ID String
     */
    public RowKey(String rowId) {
        this.rowId = rowId;
    }

    /**
     * @param rowId the canonical internal identifier of this {@link RowKey}
     */
    public void setRowId(String rowId) {
        this.rowId = rowId;
    }

    /**
     * @return the canonical internal identifier of this {@link RowKey}
     */
    public String getRowId() {
        return rowId;
    }

    /**
     * Standard equals implementation.  This method compares the RowKey id
     * values for equality.
     *
     * @param o the Object to check equality
     * @return true if equal, false if not
     * @see Object#equals(Object)
     */
    public boolean equals(Object o) {
        if (o instanceof RowKey) {
            String rkRowId = ((RowKey)o).getRowId();
            String thisRowId = getRowId();
            return thisRowId == rkRowId || (thisRowId != null && thisRowId.equals(rkRowId));
        }
        return false;
    }
    
    /**
     * @return the hashCode of a blank String if the RowKey id is null, or the
     * hashCode of the RowKey id otherwise.
     * @see Object#hashCode()
     */
    public int hashCode() {
        String thisRowId = getRowId();
        if (thisRowId == null) {
            return "".hashCode();
        }
        return thisRowId.hashCode();
    }

    /**
     * <p>Standard implementation of compareTo(Object).  This checks for
     * equality first (using equals(Object)), then compares the rowId strings.
     * This should be overridden by RowKey implementations that have a notion of
     * order.  This allows for deletions and insertions to be done in reverse
     * order to help ensure the longevity of valid RowKeys.</p>
     *
     * {@inheritDoc}
     */
    public int compareTo(Object o) {
        if (this.equals(o)) {
            return 0;
        }
        RowKey rk = (RowKey)o;
        String thisRowId = getRowId();
        if (thisRowId == null) {
            thisRowId = "";
        }
        return thisRowId.compareTo(rk.getRowId());
    }

    private String rowId;

    public String toString() {
        return "RowKey[" + getRowId() + "]"; // NOI18N
    }
}
