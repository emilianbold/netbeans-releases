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

import com.sun.data.provider.RowKey;

/**
 * <p>IndexRowKey uses an int index as the identifier for a data row in a
 * {@link com.sun.data.provider.TableDataProvider}.</p>
 *
 * @author Joe Nuxoll
 *         Winston Prakash (Buf Fixes and clean up)
 */
public class IndexRowKey extends RowKey {

    /**
     * Constructs a new IndexRowKey from the passed rowId String
     *
     * @param rowId The canonical row ID string to parse into an int
     * @return An IndexRowKey representing the passed rowId
     * @throws java.lang.NumberFormatException If the passed String is not
     *         parsable into an int
     */
    public static IndexRowKey create(String rowId) throws NumberFormatException {
        return new IndexRowKey(Integer.parseInt(rowId));
    }

    /**
     * Constructs an IndexRowKey using the specified index
     *
     * @param index The desired index
     */
    public IndexRowKey(int index) {
        super(String.valueOf(index));
        this.index = index;
    }

    /**
     * Returns the index of this IndexRowKey
     *
     * @return This IndexRowKey's index value
     */
    public int getIndex() {
        return index;
    }

    /**
     * <p>Compare this instance to another {@link IndexRowKey} instance.</p>
     *
     * {@inheritDoc}
     */
    public int compareTo(Object o) {
        IndexRowKey instance = (IndexRowKey) o;
        int oindex = instance.getIndex();
        if (this.index < oindex) {
            return -1;
        } else if (this.index > oindex) {
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * Standard equals implementation.  This method compares the IndexRowKey
     * index values for == equality.  If the passed Object is not an
     * IndexRowKey instance, the superclass (RowKey) gets a chance to evaluate
     * the Object for equality.
     *
     * {@inheritDoc}
     */
    public boolean equals(Object o) {
        if (o instanceof IndexRowKey) {
            return ((IndexRowKey)o).getIndex() == this.index;
        }
        return super.equals(o);
    }

    private int index;

    /**
     * <p>Return a printable version of this instance.</p>
     *
     * {@inheritDoc}
     */
    public String toString() {
        return "RowKey[" + index + "]"; //NOI18N
    }
}
