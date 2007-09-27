/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

