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
package org.netbeans.modules.sql.framework.codegen.axion;

import java.util.Map;

import org.netbeans.modules.sql.framework.codegen.AbstractGeneratorFactory;
import org.netbeans.modules.sql.framework.codegen.SQLOperatorFactory;
import org.netbeans.modules.sql.framework.codegen.Statements;
import org.netbeans.modules.sql.framework.codegen.TypeGenerator;
import org.netbeans.modules.sql.framework.codegen.base.BaseDB;


/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class AxionDB extends BaseDB {

    /* Defines relative location of vendor-specific template configuration resource file. */
    private static final String TEMPLATE_FILE = "/org/netbeans/modules/sql/framework/codegen/axion/config/templates.xml";

    /* Reference to oracle8 operatordef info file. */
    private static final String AXION_OPERATOR_DEFINITION_FILE = "org/netbeans/modules/sql/framework/codegen/axion/config/operator-script.xml";

    private static final String START_ESCAPE_CHAR = "\"";
    private static final String END_ESCAPE_CHAR = "\"";

    private AxionPipelineStatements pipelineStatements;
    private boolean columnsAreCaseSensitive = false;
    
    @Override
    public String getEscapedName(String name) {
        StringBuilder escapedName = new StringBuilder(50);

        escapedName.append(START_ESCAPE_CHAR);
        escapedName.append(columnsAreCaseSensitive ? name : name.toUpperCase());
        escapedName.append(END_ESCAPE_CHAR);

        return escapedName.toString();
    }

    @Override
    public String getUnescapedName(String name) {
        if (name.startsWith(START_ESCAPE_CHAR)) {
            name = name.substring(START_ESCAPE_CHAR.length());
        }

        if (name.endsWith(END_ESCAPE_CHAR)) {
            name = name.substring(0, name.length() - END_ESCAPE_CHAR.length());
        }

        return name.toUpperCase();
    }

    @Override
    public Statements createStatements() {
        return new AxionStatements(this);
    }

    @Override
    public AbstractGeneratorFactory createGeneratorFactory() {
        return new AxionGeneratorFactory(this);
    }

    @Override
    public TypeGenerator createTypeGenerator() {
        return new AxionTypeGenerator();
    }

    @Override
    protected Map loadTemplates() {
        super.loadTemplates();

        Map localMap = loadTemplates(TEMPLATE_FILE);
        this.templateMaps.putAll(localMap);

        return this.templateMaps;
    }

    /**
     * Gets Operator factory for Axion
     * 
     * @return SQLOperatorFactory
     */
    @Override
    public SQLOperatorFactory getOperatorFactory() {
        if (factory == null) {
            factory = new SQLOperatorFactory(AXION_OPERATOR_DEFINITION_FILE, super.getOperatorFactory());
        }
        return factory;
    }

    public AxionPipelineStatements getAxionPipelineStatements() {
        if (pipelineStatements == null) {
            pipelineStatements = new AxionPipelineStatements(this);
        }
        return pipelineStatements;
    }
    
    public void setColumnsAreCaseSensitive(boolean caseSensitive) {
        this.columnsAreCaseSensitive = caseSensitive;
    }

    @Override
    public int getDBType(){
        return AXIONDB;
    }

}
