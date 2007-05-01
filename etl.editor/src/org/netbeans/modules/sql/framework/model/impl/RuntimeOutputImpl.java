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
package org.netbeans.modules.sql.framework.model.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.netbeans.modules.model.database.DBTable;
import org.netbeans.modules.sql.framework.model.RuntimeOutput;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.SQLDBColumn;
import org.netbeans.modules.sql.framework.model.SQLLiteral;
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.netbeans.modules.sql.framework.model.TargetColumn;

import com.sun.sql.framework.exception.BaseException;
import com.sun.sql.framework.utils.RuntimeAttribute;

/**
 * @author radval
 */
public class RuntimeOutputImpl extends TargetTableImpl implements RuntimeOutput {

    /** Creates a new instance of OutputTableImpl */
    public RuntimeOutputImpl() {
        super();
        init();
    }

    /**
     * New instance
     * 
     * @param src - src
     */
    public RuntimeOutputImpl(DBTable src) {
        super(src);
        init();
    }

    public Map getRuntimeAttributeMap() {
        Map inputAttrs = new HashMap();

        Iterator attrIter = getColumnList().iterator();
        while (attrIter.hasNext()) {
            SQLDBColumn col = (SQLDBColumn) attrIter.next();
            final String varName = col.getName();
            final String defaultValue = col.getDefaultValue();
            int jdbcType = col.getJdbcType();

            RuntimeAttribute attr = new RuntimeAttribute();
            attr.setAttributeName(varName);
            attr.setJdbcType(jdbcType);
            if (defaultValue != null) {
                attr.setAttributeValue(defaultValue);
            }

            if (col instanceof TargetColumn) {
                TargetColumn tgtColumn = (TargetColumn) col;
                SQLObject inputObject = tgtColumn.getValue();

                if (inputObject != null && inputObject.getObjectType() == SQLConstants.VISIBLE_LITERAL) {
                    SQLLiteral literal = (SQLLiteral) inputObject;
                    attr.setAttributeValue(literal.getValue());
                }
            }
            inputAttrs.put(varName, attr);
        }

        return inputAttrs;
    }

    /**
     * Returns XML representation of table metadata.
     * 
     * @param prefix prefix for the xml.
     * @param tableOnly flag for generating table only metadata.
     * @return XML representation of the table metadata.
     * @exception BaseException - exception
     */
    public String toXMLString(String prefix, boolean tableOnly) throws BaseException {
        StringBuffer xml = new StringBuffer(INIT_XMLBUF_SIZE);

        xml.append(prefix).append("<").append(TAG_RUNTIME_OUTPUT);
        xml.append(" ").append(TABLE_NAME_ATTR).append("=\"").append(name).append("\"");

        xml.append(" ").append(ID_ATTR).append("=\"").append(id).append("\"");

        if (displayName != null && displayName.trim().length() != 0) {
            xml.append(" ").append(DISPLAY_NAME_ATTR).append("=\"").append(displayName).append("\"");
        }

        xml.append(">\n");

        xml.append(toXMLAttributeTags(prefix));

        if (!tableOnly) {
            writeColumns(prefix, xml);
        }

        if (guiInfo != null) {
            xml.append(guiInfo.toXMLString(prefix + INDENT));
        }

        xml.append(prefix).append("</").append(TAG_RUNTIME_OUTPUT).append(">\n");

        return xml.toString();
    }

    /**
     * Gets String representing tag name for this table class.
     * 
     * @return String representing element tag for this class
     */
    protected String getElementTagName() {
        return TAG_RUNTIME_OUTPUT;
    }

    private void init() {
        type = SQLConstants.RUNTIME_OUTPUT;
        this.setName(TAG_RUNTIME_OUTPUT);
    }
}

