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
package org.netbeans.modules.sql.framework.model.impl;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.netbeans.modules.sql.framework.model.RuntimeInput;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.SQLDBColumn;

import com.sun.etl.exception.BaseException;
import com.sun.etl.utils.RuntimeAttribute;
import org.netbeans.modules.sql.framework.model.DBTable;

/**
 * @author radval
 */
public class RuntimeInputImpl extends SourceTableImpl implements RuntimeInput {

    /** Array of Strings reprsenting available SQL datatypes */
    protected static final List<String> VALID_TYPE_NAMES = new ArrayList<String>();

    static {

        VALID_TYPE_NAMES.add(String.valueOf(Types.CHAR));
        VALID_TYPE_NAMES.add(String.valueOf(Types.DECIMAL));
        VALID_TYPE_NAMES.add(String.valueOf(Types.DOUBLE));
        VALID_TYPE_NAMES.add(String.valueOf(Types.FLOAT));
        VALID_TYPE_NAMES.add(String.valueOf(Types.INTEGER));
        VALID_TYPE_NAMES.add(String.valueOf(Types.TIMESTAMP));
        VALID_TYPE_NAMES.add(String.valueOf(Types.VARCHAR));

        Collections.sort(VALID_TYPE_NAMES);
    }

    /** Creates a new instance of InputTableImpl */
    public RuntimeInputImpl() {
        super();
        init();
    }

    /**
     * New instance
     * 
     * @param src - src
     */
    public RuntimeInputImpl(DBTable src) {
        super(src);
        init();
    }

    public Map getRuntimeAttributeMap() {
        Map<String, RuntimeAttribute> inputAttrs = new HashMap<String, RuntimeAttribute>();

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

            inputAttrs.put(varName, attr);
        }

        return inputAttrs;
    }

    /**
     * Construct XML string
     * 
     * @param prefix - prefix
     * @param tableOnly - table only
     * @return XML string
     * @throws BaseException - exception
     */
    @Override
    public String toXMLString(String prefix, boolean tableOnly) throws BaseException {
        StringBuilder xml = new StringBuilder(INIT_XMLBUF_SIZE);

        xml.append(prefix).append("<").append(TAG_RUNTIME_INPUT);
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

        xml.append(prefix).append("</").append(TAG_RUNTIME_INPUT).append(">\n");

        return xml.toString();
    }

    /**
     * Gets String representing tag name for this table class.
     * 
     * @return String representing element tag for this class
     */
    @Override
    protected String getElementTagName() {
        return TAG_RUNTIME_INPUT;
    }

    private void init() {
        type = SQLConstants.RUNTIME_INPUT;
        this.setName(TAG_RUNTIME_INPUT);
    }

}

