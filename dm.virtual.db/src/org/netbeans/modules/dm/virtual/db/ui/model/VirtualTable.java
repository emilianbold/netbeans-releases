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
package org.netbeans.modules.dm.virtual.db.ui.model;

import java.util.List;

import org.netbeans.modules.dm.virtual.db.bootstrap.PropertyKeys;
import org.netbeans.modules.dm.virtual.db.model.VirtualDBTable;
import org.netbeans.modules.dm.virtual.db.model.VirtualDBColumn;

import org.netbeans.modules.dm.virtual.db.model.VirtualDBUtil;
import org.netbeans.modules.dm.virtual.db.model.ForeignKey;
import org.netbeans.modules.dm.virtual.db.model.PrimaryKey;
import org.openide.util.NbBundle;

/**
 * Abstract bean wrapper for VirtualDBTable instances to expose common read-only
 * properties for display in a Virtual Database definition property sheet.
 * 
 * @author Ahimanikya Satapathy
 */
public abstract class VirtualTable {
    private VirtualDBTable mDelegate;
    public static final Integer ZERO = new Integer(0);

    protected VirtualTable(VirtualDBTable dbTable) {
        mDelegate = dbTable;
    }

    public final VirtualDBTable getSource() {
        return mDelegate;
    }

    public String getFileName() {
        return mDelegate.getFileName();
    }

    public String getTableName() {
        return mDelegate.getTableName();
    }

    public String getProperty(String propName) {
        return mDelegate.getProperty(propName);
    }

    public String getName() {
        return mDelegate.getName();
    }

    public String getDescription() {
        String desc = mDelegate.getDescription();
        return (VirtualDBUtil.isNullString(desc)) ? NbBundle.getMessage(VirtualTable.class, "LBL_none_placeholder") : desc;
    }

    public String getSchema() {
        String schema = mDelegate.getSchema();
        return (VirtualDBUtil.isNullString(schema)) ? NbBundle.getMessage(VirtualTable.class, "LBL_none_placeholder") : schema;
    }

    public String getCatalog() {
        String catalog = mDelegate.getCatalog();
        return (VirtualDBUtil.isNullString(catalog)) ? NbBundle.getMessage(VirtualTable.class, "LBL_none_placeholder") : catalog;
    }

    public VirtualDBColumn getColumn(String name) {
        return mDelegate.getColumn(name);
    }

    public List getColumnList() {
        return mDelegate.getColumnList();
    }

    public PrimaryKey getPrimaryKey() {
        return mDelegate.getPrimaryKey();
    }

    public List getForeignKeys() {
        return mDelegate.getForeignKeys();
    }

    public ForeignKey getForeignKey(String keyName) {
        return mDelegate.getForeignKey(keyName);
    }

    public String getEncodingScheme() {
        return mDelegate.getEncodingScheme();
    }

    public String getFileType() {
        return mDelegate.getProperty(PropertyKeys.LOADTYPE);
    }

    public Integer getRowsToSkip() {
        String valStr = mDelegate.getProperty(PropertyKeys.ROWSTOSKIP);
        return (valStr != null) ? Integer.valueOf(valStr) : ZERO;
    }

    public Integer getMaxFaults() {
        String valStr = mDelegate.getProperty(PropertyKeys.MAXFAULTS);
        return (valStr != null) ? Integer.valueOf(valStr) : ZERO;
    }

    public String getRecordDelimiter() {
        String delimiter = getProperty(PropertyKeys.RECORDDELIMITER);
        if (delimiter == null || delimiter.length() == 0) {
            delimiter = NbBundle.getMessage(VirtualTable.class, "LBL_none_placeholder");
        } else {
            delimiter = VirtualDBUtil.escapeControlChars(delimiter);
        }
        return delimiter;
    }

    public boolean isFirstLineHeader() {
        return Boolean.valueOf(getProperty(PropertyKeys.ISFIRSTLINEHEADER)).booleanValue();
    }

    public boolean enableWhiteSpaceTrimming() {
        String whiteSpaceStr = getProperty(PropertyKeys.TRIMWHITESPACE);
	    whiteSpaceStr = (whiteSpaceStr == null ) ? "true" : whiteSpaceStr;
	    return Boolean.valueOf(whiteSpaceStr).booleanValue();
    }

    public String getSelectStatementSQL(int rows) {
        return mDelegate.getSelectStatementSQL(rows);
    }
}
