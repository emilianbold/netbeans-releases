/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.sql.framework.codegen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.sql.framework.model.SQLCondition;
import org.netbeans.modules.sql.framework.model.SQLDBTable;
import org.netbeans.modules.sql.framework.model.SourceTable;


/**
 * @author Ritesh Adval
 * @author Ahimanikya Satapathy
 * @version $Revision$
 */
public class StatementContext {

    public static final String USE_SOURCE_TEMP_TABLE_NAME = "USE_SOURCE_TEMP_TABLE_NAME";
    public static final String USE_SOURCE_TABLE_ALIAS_NAME = "USE_SOURCE_TABLE_ALIAS_NAME";
    public static final String USE_TARGET_TABLE_ALIAS_NAME = "USE_TARGET_TABLE_ALIAS_NAME";
    public static final String USE_SOURCE_COLUMN_ALIAS_NAME = "USE_SOURCE_COLUMN_ALIAS_NAME";
    public static final String USE_TARGET_COLUMN_ALIAS_NAME = "USE_TARGET_COLUMN_ALIAS_NAME";
    public static final String USE_WHERE_STATEMENT = "USE_WHERE_STATEMENT";
    public static final String USE_FULLY_QUALIFIED_TABLE = "USE_FULLY_QUALIFIED_TABLE";
    public static final String JOIN_OPERATOR = "joinOperator";
    public static final String WHERE_CONDITION_LIST = "whereList";
    public static final String IF_EXISTS = "ifExists";
    public static final String VALIDATION_CONDITIONS = "VALIDATION_CONDITIONS";
    public static final String SUPPRESS_TABLE_PREFIX_FOR_SOURCE_COL = "SUPPRESS_TABLE_PREFIX_FOR_SOURCE_COL";
    public static final String SUPPRESS_TABLE_PREFIX_FOR_TARGET_COL = "SUPPRESS_TABLE_PREFIX_FOR_TARGET_COL";
    public static final String USE_ORIGINAL_SOURCE_TABLE_NAME = "USE_ORIGINAL_SOURCE_TABLE_NAME";
    public static final String USE_ORIGINAL_TARGET_TABLE_NAME = "USE_ORIGINAL_TARGET_TABLE_NAME";
    public static final String USE_UNIQUE_TABLE_NAME = "USE_UNIQUE_TABLE_NAME";
    public static final String SET_TEMP_SOURCE_TABLES = "SET_TEMP_SOURCE_TABLES";
    public static final String SET_UNIQUE_TABLES = "SET_UNIQUE_TABLES";
    public static final String USER_FUNCTION_NAME = "USER_FUNCTION_NAME";
    private Map<Object, Object> propertyMap = new HashMap<Object, Object>();

    public StatementContext() {
        putClientProperty(VALIDATION_CONDITIONS, new ArrayList());
    }

    public void putClientProperty(Object key, Object value) {
        propertyMap.put(key, value);
    }

    public Object getClientProperty(Object key) {
        return propertyMap.get(key);
    }

    public void putAll(StatementContext context) {
        Iterator it = context.keys();
        while (it.hasNext()) {
            Object key = it.next();
            this.putClientProperty(key, context.getClientProperty(key));
        }
    }

    public Iterator keys() {
        return propertyMap.keySet().iterator();
    }

    @SuppressWarnings(value = "unchecked")
    public void setUsingTempTableName(SourceTable table, boolean use) {
        Set tempTableSet = (Set) this.getClientProperty(SET_TEMP_SOURCE_TABLES);
        if (tempTableSet == null) {
            tempTableSet = new HashSet();
            this.putClientProperty(SET_TEMP_SOURCE_TABLES, tempTableSet);
        }

        if (use) {
            tempTableSet.add(table);
        } else {
            tempTableSet.remove(table);
        }
    }

    public void clearAllUsingTempTableName() {
        Set tempTableSet = (Set) this.getClientProperty(SET_TEMP_SOURCE_TABLES);
        if (tempTableSet == null) {
            tempTableSet = new HashSet();
            this.putClientProperty(SET_TEMP_SOURCE_TABLES, tempTableSet);
        } else {
            tempTableSet.clear();
        }
    }

    public boolean isUsingTempTableName(SourceTable table) {
        Set tempTableSet = (Set) this.getClientProperty(SET_TEMP_SOURCE_TABLES);
        if (tempTableSet != null) {
            return tempTableSet.contains(table);
        }
        return false;
    }

    public boolean isUseSourceTableAliasName() {
        Boolean val = (Boolean) this.getClientProperty(USE_SOURCE_TABLE_ALIAS_NAME);
        if (val != null) {
            return val.booleanValue();
        }
        return false;
    }

    public void setUseSourceTableAliasName(boolean use) {
        this.putClientProperty(USE_SOURCE_TABLE_ALIAS_NAME, new Boolean(use));
    }

    public boolean isUseTargetTableAliasName() {
        Boolean val = (Boolean) this.getClientProperty(USE_TARGET_TABLE_ALIAS_NAME);
        if (val != null) {
            return val.booleanValue();
        }
        return false;
    }

    public void setUseTargetTableAliasName(boolean use) {
        this.putClientProperty(USE_TARGET_TABLE_ALIAS_NAME, Boolean.valueOf(use));
    }

    public boolean isUseSourceColumnAliasName() {
        Boolean val = (Boolean) this.getClientProperty(USE_SOURCE_COLUMN_ALIAS_NAME);
        if (val != null) {
            return val.booleanValue();
        }
        return false;
    }

    public void setUseSourceColumnAliasName(boolean use) {
        this.putClientProperty(USE_SOURCE_COLUMN_ALIAS_NAME, Boolean.valueOf(use));
    }

    public boolean isUseTargetColumnAliasName() {
        Boolean val = (Boolean) this.getClientProperty(USE_TARGET_COLUMN_ALIAS_NAME);
        if (val != null) {
            return val.booleanValue();
        }
        return false;
    }

    public void setUseTargetColumnAliasName(boolean use) {
        this.putClientProperty(USE_TARGET_COLUMN_ALIAS_NAME, Boolean.valueOf(use));
    }

    /**
     * @return
     */
    public boolean isUsingFullyQualifiedTablePrefix() {
        Boolean val = (Boolean) this.getClientProperty(USE_FULLY_QUALIFIED_TABLE);
        if (val != null) {
            return val.booleanValue();
        }
        return true;
    }

    public boolean hasValidationConditions() {
        return getValidationConditions().size() != 0;
    }

    @SuppressWarnings(value = "unchecked")
    public List getValidationConditions() {
        return new ArrayList((List) this.getClientProperty(VALIDATION_CONDITIONS));
    }

    @SuppressWarnings(value = "unchecked")
    public void addValidationCondition(SQLCondition newCondition) {
        if (newCondition != null) {
            List conditions = this.getValidationConditions();
            conditions.add(newCondition);
        }
    }

    public void removeValidationCondition(SQLCondition oldCondition) {
        if (oldCondition != null) {
            List conditions = this.getValidationConditions();
            conditions.remove(oldCondition);
        }
    }

    @SuppressWarnings(value = "unchecked")
    public void setValidationConditions(List newList) {
        List conditions = this.getValidationConditions();
        conditions.clear();
        if (newList != null) {
            conditions.addAll(newList);
        }
    }

    public void setUsingFullyQualifiedTablePrefix(boolean use) {
        this.putClientProperty(USE_FULLY_QUALIFIED_TABLE, Boolean.valueOf(use));
    }

    /**
     * @return
     */
    public boolean isSuppressingTablePrefixForTargetColumn() {
        Boolean suppress = (Boolean) this.getClientProperty(SUPPRESS_TABLE_PREFIX_FOR_TARGET_COL);
        if (suppress != null) {
            return suppress.booleanValue();
        }
        return false;
    }

    public void setSuppressingTablePrefixForTargetColumn(boolean newValue) {
        this.putClientProperty(SUPPRESS_TABLE_PREFIX_FOR_TARGET_COL, newValue ? Boolean.TRUE : Boolean.FALSE);
    }

    public boolean isSuppressingTablePrefixForSourceColumn() {
        Boolean suppress = (Boolean) this.getClientProperty(SUPPRESS_TABLE_PREFIX_FOR_SOURCE_COL);
        if (suppress != null) {
            return suppress.booleanValue();
        }
        return false;
    }

    public void setSuppressingTablePrefixForSourceColumn(boolean newValue) {
        this.putClientProperty(SUPPRESS_TABLE_PREFIX_FOR_SOURCE_COL, newValue ? Boolean.TRUE : Boolean.FALSE);
    }

    public boolean isUsingOriginalSourceTableName() {
        Boolean useOriginal = (Boolean) this.getClientProperty(USE_ORIGINAL_SOURCE_TABLE_NAME);
        return (useOriginal != null) ? useOriginal.booleanValue() : false;
    }

    public void setUsingOriginalSourceTableName(boolean newValue) {
        this.putClientProperty(USE_ORIGINAL_SOURCE_TABLE_NAME, newValue ? Boolean.TRUE : Boolean.FALSE);
    }

    public boolean isUsingOriginalTargetTableName() {
        Boolean useOriginal = (Boolean) this.getClientProperty(USE_ORIGINAL_TARGET_TABLE_NAME);
        return (useOriginal != null) ? useOriginal.booleanValue() : false;
    }

    public void setUsingOriginalTargetTableName(boolean newValue) {
        this.putClientProperty(USE_ORIGINAL_TARGET_TABLE_NAME, newValue ? Boolean.TRUE : Boolean.FALSE);
    }

    public boolean isUsingUniqueTableName() {
        Boolean useUnique = (Boolean) this.getClientProperty(USE_UNIQUE_TABLE_NAME);
        return (useUnique != null) ? useUnique.booleanValue() : false;
    }

    public void setUsingUniqueTableName(boolean newValue) {
        this.putClientProperty(USE_UNIQUE_TABLE_NAME, newValue ? Boolean.TRUE : Boolean.FALSE);
    }

    /**
     * Indicates whether given table should use unique table name in generator
     *
     * @param table Table to be flagged
     * @return true if table should use unique table name, false otherwise
     */
    public boolean isUsingUniqueTableName(SQLDBTable table) {
        Set tempTableSet = (Set) this.getClientProperty(SET_UNIQUE_TABLES);
        if (tempTableSet != null) {
            return tempTableSet.contains(table);
        }
        return false;
    }

    /**
     * Sets flag to indicate whether given table should use unique table name in
     * generator.
     *
     * @param table Table to be flagged
     * @param use true if table should use unique table name, false otherwise
     */
    @SuppressWarnings(value = "unchecked")
    public void setUsingUniqueTableName(SQLDBTable table, boolean use) {
        Set tempTableSet = (Set) this.getClientProperty(SET_UNIQUE_TABLES);
        if (tempTableSet == null) {
            tempTableSet = new HashSet();
            this.putClientProperty(SET_UNIQUE_TABLES, tempTableSet);
        }

        if (use) {
            tempTableSet.add(table);
        } else {
            tempTableSet.remove(table);
        }
    }

    /**
     * Clears set of tables flagged to use unique table name in generator.
     */
    public void clearAllUsingUniqueTableName() {
        Set tempTableSet = (Set) this.getClientProperty(SET_UNIQUE_TABLES);
        if (tempTableSet == null) {
            tempTableSet = new HashSet();
            this.putClientProperty(SET_UNIQUE_TABLES, tempTableSet);
        } else {
            tempTableSet.clear();
        }
    }
}