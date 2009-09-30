/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.sql.framework.model.impl;

import org.netbeans.modules.sql.framework.model.DBColumn;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.SourceColumn;


/**
 * Concrete implementation of SourceColumn describing column metadata for source columns.
 * 
 * @author Sudhendra Seshachala, Jonathan Giron
 * @version $Revision$
 */
public class SourceColumnImpl extends AbstractDBColumn implements SourceColumn {

    /* Log4J category name */
    static final String LOG_CATEGORY = SourceColumnImpl.class.getName();

    /** Constructs default instance of SourceColumnImpl. */
    public SourceColumnImpl() {
        super();
        init();
    }

    /**
     * Constructs a new instance of SourceColumnImpl, cloning the contents of the given
     * DBColumn implementation instance.
     * 
     * @param src DBColumn instance to be cloned
     */
    public SourceColumnImpl(DBColumn src) {
        this();

        if (src == null) {
            throw new IllegalArgumentException("Must supply non-null DBColumn instance for src.");
        }

        copyFrom(src);
    }

    /**
     * Constructs a new instance of SourceColumnImpl using the given parameters and
     * assuming that the column is not part of a foreign key or primary key, and that it
     * accepts null values.
     * 
     * @param colName name of this column
     * @param sqlJdbcType JDBC type of this column
     * @param colScale scale of this column
     * @param colPrecision precision of this column
     * @param isNullable true if nullable, false otherwise
     * @see java.sql.Types
     */
    public SourceColumnImpl(String colName, int sqlJdbcType, int colScale, int colPrecision, boolean isNullable) {
        super(colName, sqlJdbcType, colScale, colPrecision, isNullable);
        init();
    }

    /**
     * @see org.netbeans.modules.sql.framework.model.impl.AbstractDBColumn
     *      (java.lang.String,int,int,boolean,int,boolean,boolean,boolean)
     */
    public SourceColumnImpl(String colName, int sqlJdbcType, int colScale, int colPrecision, boolean isPrimaryKey, boolean isForeignKey,
            boolean isIndexed, boolean isNullable) {
        super(colName, sqlJdbcType, colScale, colPrecision, isPrimaryKey, isForeignKey, isIndexed, isNullable);
        init();
    }

    /**
     * Clone a deep copy of SourceColumnImpl.
     * 
     * @return a copy of SourceColumnImpl.
     */
    public Object clone() {
        return new SourceColumnImpl(this);
    }

    /**
     * @see org.netbeans.modules.sql.framework.model.impl.AbstractDBColumn#equals(java.lang.Object)
     */
    public boolean equals(Object refObj) {
        if (!(refObj instanceof SourceColumn)) {
            return false;
        }

        return super.equals(refObj);
    }

    /**
     * @see org.netbeans.modules.sql.framework.model.impl.AbstractDBColumn#hashCode
     */
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * Overrides default implementation to return evaluated column name.
     * 
     * @return evaluated column name.
     */
    public String toString() {
        return super.toString();
    }

    /**
     * @see org.netbeans.modules.sql.framework.model.SQLObject#toXMLString
     */
    public String toXMLString(String prefix) {
        StringBuilder xml = new StringBuilder(50);

        xml.append(prefix).append("<").append(ELEMENT_TAG);

        // Allow superclass to write its attributes out first.
        appendXMLAttributes(xml);
        xml.append(" >\n");

        // write out attributes
        xml.append(super.toXMLAttributeTags(prefix));
        xml.append(prefix).append("</").append(ELEMENT_TAG).append(">\n");

        return xml.toString();
    }

    /**
     * @see org.netbeans.modules.sql.framework.model.impl.AbstractDBColumn#getElementTagName
     */
    protected String getElementTagName() {
        return ELEMENT_TAG;
    }

    /*
     * Performs sql framework initialization functions for constructors which cannot first
     * call this().
     */
    private void init() {
        type = SQLConstants.SOURCE_COLUMN;
    }

}

