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

/**
 * <p>TableCursorListener is an event listener interface that supports
 * processing cursor movement events produced by a corresponding
 * {@link TableDataProvider} instance.</p>
 *
 * @author Joe Nuxoll
 */
public interface TableCursorListener extends java.util.EventListener {

    /**
     * <p>Process an event indicating that the current cursor row of the
     * specified {@link TableDataProvider} is changing.  If an exception
     * is thrown be an event handler, the cursor change will be vetoed.</p>
     *
     * @param provider <code>TableDataProvider</code> whose cursor row is changing
     * @param oldRow The old cursor row key
     * @param newRow The new cursor row key
     * @throws TableCursorVetoException a cursor veto exception explicitly vetos
     *         the cursor change
     */
    public void cursorChanging(TableDataProvider provider,
        RowKey oldRow, RowKey newRow) throws TableCursorVetoException;

    /**
     * <p>Process an event indicating that the current cursor row of the
     * specified {@link TableDataProvider} has been successfully changed.</p>
     *
     * @param provider <code>TableDataProvider</code> whose cursor row index
     *                 has successfully changed
     * @param oldRow The old cursor row key
     * @param newRow The new cursor row key
     */
    public void cursorChanged(TableDataProvider provider,
        RowKey oldRow, RowKey newRow);
}
