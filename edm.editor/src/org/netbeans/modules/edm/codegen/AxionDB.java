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
package org.netbeans.modules.edm.codegen;

import java.util.Map;


/**
 * @author Ritesh Adval
 * @author Ahimanikya Satapathy
 */
public class AxionDB extends BaseDB {

    private static final String TEMPLATE_FILE = "/org/netbeans/modules/edm/codegen/config/templates.xml";

    private static final String AXION_OPERATOR_DEFINITION_FILE = "org/netbeans/modules/edm/codegen/config/operator-script.xml";
    private AxionPipelineStatements pipelineStatements;
    private boolean columnsAreCaseSensitive = false;

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
        templateMaps = loadTemplates(TEMPLATE_FILE);
        return templateMaps;
    }

    @Override
    public SQLOperatorFactory getOperatorFactory() {
        if (factory == null) {
            factory = new SQLOperatorFactory(AXION_OPERATOR_DEFINITION_FILE, null);
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

    public int getDBType() {
        return AXIONDB;
    }

    public AxionDB() {
    }
}
