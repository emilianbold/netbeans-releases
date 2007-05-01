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

import org.netbeans.modules.mashup.db.common.PropertyKeys;
import org.netbeans.modules.mashup.db.model.FlatfileDBTable;
import org.openide.util.NbBundle;

import com.sun.sql.framework.utils.StringUtil;

/**
 * Concrete bean wrapper implementation for delimited instances of FlatfileDBTable,
 * exposing read-only properties for display in a Flatfile DB property sheet. TODO Extend
 * to a mutable class (adding setters as required) to allow editing of Flat File Database
 * properties.
 * 
 * @author Jonathan Giron
 * @author Ahimanikya Satapathy
 * @version $Revision$
 */
public class DelimitedFlatfile extends FlatfileTable {
    /**
     * Creates new instance of DelimitedFlatfile, wrapping the given FlatfileDBTable
     * instance.
     * 
     * @param dbTable FlatfileDBTable instance (delimited type) to be wrapped.
     */
    public DelimitedFlatfile(FlatfileDBTable dbTable) {
        super(dbTable);
    }

    /**
     * Gets the character(s) used to delimit fields in this file.
     * 
     * @return field delimiter string
     */
    public String getFieldDelimiter() {
        String delimiter = getProperty(PropertyKeys.FIELDDELIMITER);
        if (delimiter == null || delimiter.length() == 0) {
            delimiter = NbBundle.getMessage(FlatfileTable.class, "LBL_none_placeholder");
        } else {
            delimiter = StringUtil.escapeControlChars(delimiter);
        }
        return delimiter;
    }

    /**
     * Gets the text qualifier, if any, used to delimit text data in this file.
     * 
     * @return text qualifier
     */
    public String getTextQualifier() {
        String qualifier = getProperty(PropertyKeys.QUALIFIER);
        if (qualifier == null || qualifier.length() == 0) {
            qualifier = NbBundle.getMessage(FlatfileTable.class, "LBL_none_placeholder");
        } else {
            if("''".equals(qualifier)) {
                qualifier = "'"; 
            }
        }
        return qualifier;
    }

}

