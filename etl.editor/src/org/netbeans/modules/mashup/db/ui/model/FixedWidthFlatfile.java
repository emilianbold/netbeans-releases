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
package org.netbeans.modules.mashup.db.ui.model;

import java.util.Iterator;

import org.netbeans.modules.mashup.db.common.PropertyKeys;
import org.netbeans.modules.mashup.db.model.FlatfileDBColumn;
import org.netbeans.modules.mashup.db.model.FlatfileDBTable;


/**
 * Concrete bean wrapper implementation for fixed-width instances of FlatfileDBTable,
 * exposing read-only properties for display in a Flatfile DB property sheet. <br>
 * <br>
 * TODO Extend to a mutable class (adding setters as required) to allow editing of Flat
 * file database definition properties.
 * 
 * @author Jonathan Giron
 * @version $Revision$
 */
public class FixedWidthFlatfile extends FlatfileTable {
    /**
     * Creates new instance of FixedWidthFlatfile, wrapping the given FlatfileDBTable
     * instance.
     * 
     * @param dbTable FlatfileDBTable instance (fixed-width type) to be wrapped.
     */
    public FixedWidthFlatfile(FlatfileDBTable dbTable) {
        super(dbTable);
    }

    /**
     * Gets number of bytes at start of flatfile to be regarded as header information to
     * be skipped by the parser.
     * 
     * @return Integer representing number of bytes at start to be regarded as header
     *         information.
     */
    public Integer getHeaderBytesOffset() {
        String valStr = getProperty(PropertyKeys.HEADERBYTESOFFSET);
        return (valStr != null) ? Integer.valueOf(valStr) : FlatfileTable.ZERO;
    }

    /**
     * Gets total record length for this file, accumulating the value from
     * precision/length values of all fields in the table.
     * 
     * @return Integer representing record length
     */
    public Integer getRecordLength() {
        Iterator iter = getSource().getColumnList().iterator();

        int length = 0;
        while (iter.hasNext()) {
            FlatfileDBColumn column = (FlatfileDBColumn) iter.next();
            length += column.getPrecision();
        }

        return (length != 0) ? new Integer(length) : FlatfileTable.ZERO;
    }
}

