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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.sql.framework.model.DBColumn;
import org.openide.util.Exceptions;
import org.w3c.dom.Element;
import com.sun.sql.framework.exception.BaseException;
import com.sun.sql.framework.utils.StringUtil;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.ResourceBundle;
import org.netbeans.modules.sql.framework.model.DBTable;
import org.netbeans.modules.sql.framework.model.ForeignKey;
import org.netbeans.modules.sql.framework.model.PrimaryKey;

/**
 * Implements PrimaryKey interface.
 *
 * @author Jonathan Giron
 * @version $Revision$
 */
public class PrimaryKeyImpl implements Cloneable, PrimaryKey {

    /** Name of attribute used for marshalling out PK column names to XML */
    public static final String COLUMNS_ATTR = "columns"; // NOI18N
    /** Document element tag name for marshalling out this object to XML */
    public static final String ELEMENT_TAG = "primaryKey"; // NOI18N
    /** Name of attribute used for marshalling out primary key name to XML */
    public static final String NAME_ATTR = "name"; // NOI18N
    /**DatabaseMetaData ResultSet column name used to decode name of associated primary key     */
    protected static final String RS_KEY_NAME = "PK_NAME"; // NOI18N
    private static final String RS_COLUMN_NAME = "COLUMN_NAME"; // NOI18N
    /** DatabaseMetaData ResultSet column name used to decode key sequence number*/
    protected static final String RS_SEQUENCE_NUM = "KEY_SEQ";
    /* List of column names in key sequence order. */
    private List<String> columnNames;
    /* (optional) DOM element used to construct this instance of PrimaryKey */
    private transient Element element;
    /* Name of this key; may be null */
    private String name;
    /* DBTable to which this PK belongs */
    private DBTable parent;

    public PrimaryKeyImpl(ResultSet rs){// throws SQLException {
        this();
        try {
            if (rs == null) {
                Locale locale = Locale.getDefault();
                ResourceBundle cMessages = ResourceBundle.getBundle("org/netbeans/modules/sql/framework/model/impl/Bundle", locale); // NO i18n
                throw new IllegalArgumentException(cMessages.getString("ERROR_VALID_RS") + "(ERROR_VALID_RS)"); // NO i18n
            }
            while (rs.next()) {                
                columnNames.add(rs.getString(RS_COLUMN_NAME));
            }
        } catch (SQLException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * Creates a new instance of PrimaryKeyImpl, using the keyElement as a source for
     * reconstituting its contents. Caller must invoke parseXml() after this constructor
     * returns in order to unmarshal and reconstitute the instance object.
     *
     * @param keyElement DOM element containing XML marshalled version of a PrimaryKeyImpl
     *        instance
     */
    public PrimaryKeyImpl(Element keyElement) {
        this();
        element = keyElement;
    }

    /**
     * Creates a new instance of PrimaryKeyImpl, cloning the contents of the given
     * PrimaryKey implementation instance.
     *
     * @param src PrimaryKey to be cloned
     */
    public PrimaryKeyImpl(PrimaryKey src) {
        this();
        if (src == null) {
            throw new IllegalArgumentException("Must supply non-null PrimaryKey instance for src.");
        }
        copyFrom(src);
    }

    /**
     * Creates a new instance of PrimaryKey with the given key name and referencing the
     * column names in the given List.
     *
     * @param keyName name, if any, of this PrimaryKey
     * @param keyColumnNames List of Column objects, or column names in key sequence
     *        order, depending on state of isStringList
     * @param isStringList true if keyColumnName contains column names in key sequence
     *        order, false if it contains Column objects which need to be sorted in key
     *        sequence order.
     */
    public PrimaryKeyImpl(String keyName, List<String> keyColumnNames) {
        this();
        name = keyName;
        columnNames.addAll(keyColumnNames);
    }

    private PrimaryKeyImpl() {
        name = null;
        columnNames = new ArrayList<String>();
    }

    /**
     * Create a clone of this PrimaryKeyImpl.
     *
     * @return cloned copy of DBColumn.
     */
    @Override
    public Object clone() {
        try {
            PrimaryKeyImpl impl = (PrimaryKeyImpl) super.clone();
            impl.columnNames = new ArrayList<String>(this.columnNames);
            return impl;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e.toString());
        }
    }

    /**
     * @see org.netbeans.modules.model.database.PrimaryKey#contains(DBColumn)
     */
    public boolean contains(DBColumn col) {
        return contains(col.getName());
    }

    /**
     * @see org.netbeans.modules.model.database.PrimaryKey#contains(java.lang.String)
     */
    public boolean contains(String columnName) {
        return columnNames.contains(columnName);
    }

    /**
     * Overrides default implementation to return value based on memberwise comparison.
     *
     * @param refObj Object against which we compare this instance
     * @return true if refObj is functionally identical to this instance; false otherwise
     */
    @Override
    public boolean equals(Object refObj) {
        if (this == refObj) {
            return true;
        }

        if (!(refObj instanceof PrimaryKeyImpl)) {
            return false;
        }

        PrimaryKeyImpl ref = (PrimaryKeyImpl) refObj;
        boolean result = (getName() != null) ? name.equals(ref.name) : (ref.name == null);
        result &= (columnNames != null) ? columnNames.equals(ref.columnNames) : (ref.columnNames != null);
        return result;
    }

    /**
     * @see org.netbeans.modules.model.database.PrimaryKey#getColumnCount
     */
    public int getColumnCount() {
        return columnNames.size();
    }

    /**
     * @see org.netbeans.modules.model.database.PrimaryKey#getColumnNames
     */
    public List<String> getColumnNames() {
        return Collections.unmodifiableList(columnNames);
    }

    /**
     * @see org.netbeans.modules.model.database.PrimaryKey#getDBColumnName
     */
    public String getDBColumnName(int iColumn) {
        return columnNames.get(iColumn);
    }

    /**
     * @see org.netbeans.modules.model.database.PrimaryKey#getName
     */
    public String getName() {
        if (name == null && parent != null) {
            name = "PK_" + parent.getName();
        }
        return name;
    }

    /**
     * @see org.netbeans.modules.model.database.PrimaryKey#getParent
     */
    public DBTable getParent() {
        return parent;
    }

    /**
     * @see org.netbeans.modules.model.database.PrimaryKey#getSequence(DBColumn)
     */
    public int getSequence(DBColumn col) {
        if (col == null || col.getName() == null) {
            return -1;
        }

        return getSequence(col.getName().trim());
    }

    /**
     * @see org.netbeans.modules.model.database.PrimaryKey#getSequence(java.lang.String)
     */
    public int getSequence(String columnName) {
        return columnNames.indexOf(columnName);
    }

    /**
     * Overrides default implementation to compute hashCode value for those members used
     * in equals() for comparison.
     *
     * @return hash code for this object
     * @see java.lang.Object#hashCode
     */
    @Override
    public int hashCode() {
        int myHash = (getName() != null) ? name.hashCode() : 0;
        myHash += (columnNames != null) ? columnNames.hashCode() : 0;

        return myHash;
    }

    /**
     * @see org.netbeans.modules.model.database.PrimaryKey#isReferencedBy
     */
    public boolean isReferencedBy(ForeignKey fk) {
        return (fk != null) ? fk.references(this) : false;
    }

    /**
     * Parses the XML content, if any, represented by the DOM element member variable.
     *
     * @exception BaseException thrown while parsing XML, or if member variable element is
     *            null
     */
    @SuppressWarnings("unchecked")
    public void parseXML() throws BaseException {
        if (this.element == null) {
            throw new BaseException("No <" + ELEMENT_TAG + "> element found.");
        }

        this.name = element.getAttribute(NAME_ATTR);

        String colNames = element.getAttribute(COLUMNS_ATTR);
        columnNames.addAll(StringUtil.createStringListFrom(colNames));
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(100);
        for (int i = 0; i < columnNames.size(); i++) {
            if (i != 0) {
                buf.append(",");
            }
            buf.append((columnNames.get(i)).trim());
        }
        return buf.toString();
    }

    /**
     * Replaces the current List of column names with the contents of the given String
     * array.
     *
     * @param newColNames array of names to supplant current list of column names
     */
    public void setColumnNames(String[] newColNames) {
        if (newColNames == null) {
            throw new IllegalArgumentException("Must supply non-null String[] for param newColNames.");
        }

        columnNames.clear();
        for (int i = 0; i < newColNames.length; i++) {
            columnNames.add(newColNames[i]);
        }
    }

    /**
     * Writes contents of this PrimaryKeyImpl instance out as an XML element, using the
     * default prefix.
     *
     * @return String containing XML representation of this PrimaryKeyImpl instance
     */
    public synchronized String toXMLString() {
        return toXMLString(null);
    }

    /**
     * Writes contents of this PrimaryKeyImpl instance out as an XML element, using the
     * given prefix String.
     *
     * @param prefix String used to prefix each new line of the XML output
     * @return String containing XML representation of this PrimaryKeyImpl instance
     */
    public synchronized String toXMLString(String prefix) {
        if (prefix == null) {
            prefix = "";
        }

        StringBuilder buf = new StringBuilder(100);

        buf.append(prefix).append("<").append(ELEMENT_TAG).append(" ");
        if (name != null && name.trim().length() != 0) {
            buf.append(NAME_ATTR).append("=\"").append(name.trim()).append("\" ");
        }

        if (columnNames.size() != 0) {
            buf.append(COLUMNS_ATTR).append("=\"");
            for (int i = 0; i < columnNames.size(); i++) {
                if (i != 0) {
                    buf.append(",");
                }
                buf.append((columnNames.get(i)).trim());
            }
            buf.append("\" ");
        }

        buf.append("/>\n");

        return buf.toString();
    }

    /**
     * Sets reference to DBTable that owns this primary key.
     *
     * @param newParent new parent of this primary key.
     */
    void setParent(DBTable newParent) {
        parent = newParent;
    }

    /*
     * Copies contents of given PrimaryKey implementation. @param src PrimaryKey whose
     * contents are to be copied
     */
    private void copyFrom(PrimaryKey src) {
        name = src.getName();
        parent = src.getParent();

        columnNames.clear();
        columnNames.addAll(src.getColumnNames());
    }
}
