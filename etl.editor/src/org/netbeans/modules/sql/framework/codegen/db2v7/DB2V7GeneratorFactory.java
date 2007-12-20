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
package org.netbeans.modules.sql.framework.codegen.db2v7;

import java.util.Map;
import org.netbeans.modules.sql.framework.codegen.AbstractDB;
import org.netbeans.modules.sql.framework.codegen.base.BaseGeneratorFactory;
import org.netbeans.modules.sql.framework.model.SQLConstants;


/**
 * Extends BaseGeneratorFactory to vend DB2-specific generator.
 *
 * @author Jonathan Giron
 * @version $Revision$
 */
public class DB2V7GeneratorFactory extends BaseGeneratorFactory {

    /**
     * @param database
     */
    public DB2V7GeneratorFactory(AbstractDB database) {
        super(database);
    }

    @Override
    public Map<Integer, String> initializeObjectToGeneratorMap() {
        Map<Integer, String> map = super.initializeObjectToGeneratorMap();

        // Override with DB2-specific handlers
        map.put(new Integer(SQLConstants.CASE), "org.netbeans.modules.sql.framework.codegen.db2v7.DB2V7CaseGenerator");
        map.put(new Integer(SQLConstants.GENERIC_OPERATOR), "org.netbeans.modules.sql.framework.codegen.db2v7.DB2V7OperatorGenerator");
        return map;
    }
}