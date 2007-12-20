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
package org.netbeans.modules.sql.framework.codegen.jdbc;

import java.util.Map;

import org.netbeans.modules.sql.framework.codegen.AbstractGeneratorFactory;
import org.netbeans.modules.sql.framework.codegen.SQLOperatorFactory;
import org.netbeans.modules.sql.framework.codegen.Statements;
import org.netbeans.modules.sql.framework.codegen.TypeGenerator;
import org.netbeans.modules.sql.framework.codegen.base.BaseDB;


/**
 * For JDBC or ANSI standard Database code generations.
 * @author Girish Patil
 * @version $Revision$
 */
public class JdbcDB extends BaseDB {

    /* Defines relative location of vendor-specific template configuration resource file. */
    private static final String TEMPLATE_FILE = "/org/netbeans/modules/sql/framework/codegen/jdbc/config/templates.xml";

    /* Reference to JDBC operatordef info file. */
    private static final String JDBC_OPERATOR_DEFINITION_FILE = "org/netbeans/modules/sql/framework/codegen/jdbc/config/operator-script.xml";

    public Statements createStatements() {
        return new JdbcStatements(this);
    }

    public String getEscapedName(String name) {
        StringBuilder escapedName = new StringBuilder(50);

        escapedName.append("\"");
        escapedName.append(name);
        escapedName.append("\"");

        return escapedName.toString();
    }

    public TypeGenerator createTypeGenerator() {
        return new JdbcTypeGenerator();
    }

    protected Map loadTemplates() {
        super.loadTemplates();

        Map localMap = loadTemplates(TEMPLATE_FILE);
        this.templateMaps.putAll(localMap);

        return this.templateMaps;
    }

    public SQLOperatorFactory getOperatorFactory() {
        if (factory == null) {
            factory = new SQLOperatorFactory(JDBC_OPERATOR_DEFINITION_FILE, super.getOperatorFactory());
        }
        return factory;
    }

    public AbstractGeneratorFactory createGeneratorFactory() {
        return new JdbcGeneratorFactory(this);
    }

    public int getDBType(){
        return JDBCDB;
    }

}
