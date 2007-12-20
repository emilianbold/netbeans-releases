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
package org.netbeans.modules.sql.framework.codegen.base;

import java.util.Map;

import org.netbeans.modules.sql.framework.codegen.AbstractDB;
import org.netbeans.modules.sql.framework.codegen.AbstractGeneratorFactory;
import org.netbeans.modules.sql.framework.codegen.SQLOperatorFactory;
import org.netbeans.modules.sql.framework.codegen.Statements;
import org.netbeans.modules.sql.framework.codegen.TypeGenerator;

import com.sun.sql.framework.jdbc.DBConstants;

/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class BaseDB extends AbstractDB {

    private static final String BASE_TEMPLATE_FILE = "/org/netbeans/modules/sql/framework/codegen/base/config/templates.xml";

    private static final String BASE_OPERATOR_DEFINITION_FILE = "org/netbeans/modules/sql/framework/codegen/base/config/operator-script.xml";

    protected SQLOperatorFactory factory;

    public int getCastingRule(int sourceType, int targetType) {
        return 0;
    }

    public String getDefaultDateFormat() {
        return null;
    }

    public String getEscapedName(String name) {
        return name;
    }

    public String getUnescapedName(String name) {
        return name;
    }

    /**
     * get the name after applying DB specfic escaping.
     * 
     * @param name name which needs to be escaped.
     * @return name after escaping it.
     */
    public String getEscapedCatalogName(String name) {
        return this.getEscapedName(name);
    }

    /**
     * get the name after applying DB specfic escaping.
     * 
     * @param name name which needs to be escaped.
     * @return name after escaping it.
     */
    public String getEscapedSchemaName(String name) {
        return this.getEscapedName(name);
    }

    public int getMaxTableNameLength() {
        return 0;
    }

    public boolean isAnsiJoinSyntaxSupported() {
        return true;
    }

    public AbstractGeneratorFactory createGeneratorFactory() {
        return new BaseGeneratorFactory(this);
    }

    public Statements createStatements() {
        return new BaseStatements(this);
    }

    public TypeGenerator createTypeGenerator() {
        return new BaseTypeGenerator();
    }

    /**
     * Override this method in a concrete vendor-specific class to load the localized
     * template file and override any desired template mappings.
     */
    protected Map loadTemplates() {
        templateMaps = loadTemplates(BASE_TEMPLATE_FILE);
        return templateMaps;
    }

    /**
     * get the operator factory for this data base
     * 
     * @return operator factory
     */
    public SQLOperatorFactory getOperatorFactory() {
        if (factory == null) {
            factory = new SQLOperatorFactory(BASE_OPERATOR_DEFINITION_FILE, null);
        }
        return factory;
    }

    public int getDBType(){
        return DBConstants.ANSI92;
    }
}
