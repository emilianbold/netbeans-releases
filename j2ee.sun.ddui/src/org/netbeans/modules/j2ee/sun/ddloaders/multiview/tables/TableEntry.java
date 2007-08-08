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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.sun.ddloaders.multiview.tables;

import java.util.ResourceBundle;
import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;


/**
 *
 * @author Peter Williams
 */
public abstract class TableEntry {

    protected final ResourceBundle bundle;
    protected final String resourceBase;
    protected final String parentPropertyName;
    protected final String propertyName;
    protected final String columnName;
    protected final int columnWidth;
    protected final boolean requiredFieldFlag;
    protected final boolean nameFieldFlag;

    public TableEntry(String pn, String c, int w) {
        this(pn, c, w, false);
    }

    public TableEntry(String pn, String c, int w, boolean required) {
        this(null, pn, c, w, required);
    }

    public TableEntry(String ppn, String pn, String c, int w, boolean required) {
        this(ppn, pn, c, w, required, false);
    }

    public TableEntry(String ppn, String pn, String c, int w, boolean required, boolean isName) {
        parentPropertyName = ppn;
        bundle = null;
        resourceBase = null;
        propertyName = pn;
        columnName = c;
        columnWidth = w;
        requiredFieldFlag = required;
        nameFieldFlag = isName;
    }

    public TableEntry(String ppn, String pn, ResourceBundle resBundle,
            String base, int w, boolean required, boolean isName) {
        parentPropertyName = ppn;
        propertyName = pn;
        bundle = resBundle;
        resourceBase = base;
        columnName = bundle.getString("LBL_" + resourceBase);	// NOI18N
        columnWidth = w;
        requiredFieldFlag = required;
        nameFieldFlag = isName;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public String getColumnName() {
        return columnName;
    }

    public int getColumnWidth() {
        return columnWidth;
    }

    public boolean isRequiredField() {
        return requiredFieldFlag;
    }

    public boolean isNameField() {
        return nameFieldFlag;
    }

    public String getLabelName() {
        return columnName + " :";	// NOI18N
    }

    public char getLabelMnemonic() {
        assert bundle != null : "Coding error: incorrect column definition for " + columnName;	// NOI18N
        return bundle.getString("MNE_" + resourceBase).charAt(0);	// NOI18N
    }

    public String getAccessibleName() {
        assert bundle != null : "Coding error: incorrect column definition for " + columnName;	// NOI18N
        return bundle.getString("ACSN_" + resourceBase);	// NOI18N
    }

    public String getAccessibleDescription() {
        assert bundle != null : "Coding error: incorrect column definition for " + columnName;	// NOI18N
        return bundle.getString("ACSD_" + resourceBase);	// NOI18N
    }

    public abstract Object getEntry(CommonDDBean parent);
    public abstract void setEntry(CommonDDBean parent, Object value);

    public abstract Object getEntry(CommonDDBean parent, int row);
    public abstract void setEntry(CommonDDBean parent, int row, Object value);

}
