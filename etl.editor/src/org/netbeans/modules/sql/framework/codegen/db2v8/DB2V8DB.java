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
package org.netbeans.modules.sql.framework.codegen.db2v8;

import java.util.Map;
import org.netbeans.modules.sql.framework.codegen.AbstractGeneratorFactory;
import org.netbeans.modules.sql.framework.codegen.SQLOperatorFactory;
import org.netbeans.modules.sql.framework.codegen.Statements;
import org.netbeans.modules.sql.framework.codegen.TypeGenerator;
import org.netbeans.modules.sql.framework.codegen.db2v7.DB2V7DB;
import org.netbeans.modules.sql.framework.codegen.db2v7.DB2V7GeneratorFactory;
import org.netbeans.modules.sql.framework.codegen.db2v7.DB2V7TypeGenerator;


/**
 * IBM DB2-specific concrete implementation of DB interface.
 *
 * @author Ritesh Adval
 * @version $Revision$
 */
public class DB2V8DB extends DB2V7DB {

    /* Defines relative location of vendor-specific template configuration resource file. */
    private static final String TEMPLATE_FILE = "/org/netbeans/modules/sql/framework/codegen/db2v8/config/templates.xml";
    /* Reference to db2 operatordef info file. */
    private static final String DB2V8_OPERATOR_DEFINITION_FILE = "org/netbeans/modules/sql/" + "framework/codegen/db2v7/config/operator-script.xml";

    @Override
    public Statements createStatements() {
        return new DB2V8Statements(this);
    }

    @Override
    public TypeGenerator createTypeGenerator() {
        return new DB2V7TypeGenerator();
    }

    @Override
    @SuppressWarnings(value = "unchecked")
    protected Map loadTemplates() {
        super.loadTemplates();

        Map localMap = loadTemplates(TEMPLATE_FILE);
        this.templateMaps.putAll(localMap);

        return this.templateMaps;
    }

    @Override
    public SQLOperatorFactory getOperatorFactory() {
        if (factory == null) {
            factory = new SQLOperatorFactory(DB2V8_OPERATOR_DEFINITION_FILE, super.getOperatorFactory());
        }
        return factory;
    }

    @Override
    public AbstractGeneratorFactory getGeneratorFactory() {
        return new DB2V7GeneratorFactory(this);
    }

    @Override
    public int getDBType() {
        return DB2V8DB;
    }
}
