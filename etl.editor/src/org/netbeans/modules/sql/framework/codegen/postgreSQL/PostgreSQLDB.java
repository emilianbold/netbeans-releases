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
package org.netbeans.modules.sql.framework.codegen.postgreSQL;

import java.util.Map;

import org.netbeans.modules.sql.framework.codegen.AbstractGeneratorFactory;
import org.netbeans.modules.sql.framework.codegen.SQLOperatorFactory;
import org.netbeans.modules.sql.framework.codegen.Statements;
import org.netbeans.modules.sql.framework.codegen.TypeGenerator;
import org.netbeans.modules.sql.framework.codegen.base.BaseDB;


/**
 * For PostgreSQL Database code generations using from JDBC Code generation.
 * @author Radhika
 */
public class PostgreSQLDB extends BaseDB {

    /* Defines relative location of vendor-specific template configuration resource file. */
    private static final String TEMPLATE_FILE = "/org/netbeans/modules/sql/framework/codegen/postgreSQL/config/templates.xml";

    /* Reference to JDBC operatordef info file. */
    private static final String PostgreSQL_OPERATOR_DEFINITION_FILE = "org/netbeans/modules/sql/framework/codegen/postgreSQL/config/operator-script.xml";

    public Statements createStatements() {
        return new PostgreSQLStatements(this);
    }

    public String getEscapedName(String name) {
        StringBuffer escapedName = new StringBuffer(50);

        escapedName.append("\"");
        escapedName.append(name);
        escapedName.append("\"");

        return escapedName.toString();
    }

    public TypeGenerator createTypeGenerator() {
        return new PostgreSQLTypeGenerator();
    }

    protected Map loadTemplates() {
        super.loadTemplates();

        Map localMap = loadTemplates(TEMPLATE_FILE);
        this.templateMaps.putAll(localMap);

        return this.templateMaps;
    }

    public SQLOperatorFactory getOperatorFactory() {
        if (factory == null) {
            factory = new SQLOperatorFactory(PostgreSQL_OPERATOR_DEFINITION_FILE, super.getOperatorFactory());
        }
        return factory;
    }

    public AbstractGeneratorFactory createGeneratorFactory() {
        return new PostgreSQLGeneratorFactory(this);
    }

    public int getDBType(){
        return PostgreSQL;
    }
}