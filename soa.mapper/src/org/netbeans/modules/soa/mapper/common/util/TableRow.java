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

package org.netbeans.modules.soa.mapper.common.util;

import java.awt.datatransfer.Transferable;

/**
 * version  2002.08.02
 * author   Copyright 2002 by UltiMeth Systems.  Permission for
 *    use is granted if this copyright notice is preserved.
 *
 * @author    htung
 * @created   October 4, 2002
 */
public interface TableRow
     extends Transferable {

    /**
     * Sets a column Object.
     *
     * @param column  the column index.
     * @param obj     The new valueAt value
     */
    void setValueAt(Object obj, int column);

    /**
     * Get a column Object (for displaying, etc).
     *
     * @param column  the column index.
     * @return        Object - the Object.
     */
    Object getValueAt(int column);

    /**
     * Get a column Comparable Object (for sorting, etc).
     *
     * @param column  the column index.
     * @return        Comparable - a Comparable equivalent of the Object.
     */
    Comparable getSortableAt(int column);

    /**
     * Gets the user object which is associated with the row
     *
     * @return Object
     */
    Object getDataObject();

    /**
     * Gets the componet object that the row data object associated to.
     *
     * @return   object
     */
    Object getContainerDataObject();
}
