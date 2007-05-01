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

import java.util.Map;

import org.netbeans.modules.mashup.db.model.FlatfileDBColumn;
import org.netbeans.modules.model.database.DBTable;
import org.openide.util.NbBundle;

import com.sun.sql.framework.utils.StringUtil;

/**
 * @author Jonathan Giron
 * @version $Revision$
 */
public class FlatfileColumn {

    private FlatfileDBColumn mDelegate;

    /**
     * 
     */
    public FlatfileColumn(FlatfileDBColumn column) {
        mDelegate = column;
    }

    public int getJdbcType() {
        return mDelegate.getJdbcType();
    }

    public String getProperty(String propName) {
        return mDelegate.getProperty(propName);
    }

    public Map getProperties() {
        return mDelegate.getProperties();
    }

    public boolean isIndexed() {
        return mDelegate.isIndexed();
    }

    public String getName() {
        return mDelegate.getName();
    }

    public boolean isPrimaryKey() {
        return mDelegate.isPrimaryKey();
    }

    public boolean isForeignKey() {
        return mDelegate.isForeignKey();
    }

    public boolean isNullable() {
        return mDelegate.isNullable();
    }

    public DBTable getParent() {
        return mDelegate.getParent();
    }

    public String getJdbcTypeString() {
        return mDelegate.getJdbcTypeString();
    }

    public int getScale() {
        return mDelegate.getScale();
    }

    public int getPrecision() {
        return mDelegate.getPrecision();
    }

    public String getDefaultValue() {
        String defaultVal = mDelegate.getDefaultValue();
        return (StringUtil.isNullString(defaultVal)) ? NbBundle.getMessage(FlatfileColumn.class, "LBL_none_placeholder") : defaultVal;
    }

    public int getOrdinalPosition() {
        return mDelegate.getOrdinalPosition();
    }
}

