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

package org.netbeans.modules.xml.xpath.ext;

import java.util.HashMap;
import org.netbeans.modules.xml.xpath.ext.metadata.OperationMetadata;
import org.netbeans.modules.xml.xpath.ext.metadata.impl.OperationMetadataImpl.AdditionOperationMetadata;
import org.netbeans.modules.xml.xpath.ext.metadata.impl.OperationMetadataImpl.AndOperationMetadata;
import org.netbeans.modules.xml.xpath.ext.metadata.impl.OperationMetadataImpl.DivOperationMetadata;
import org.netbeans.modules.xml.xpath.ext.metadata.impl.OperationMetadataImpl.EqualOperationMetadata;
import org.netbeans.modules.xml.xpath.ext.metadata.impl.OperationMetadataImpl.GEOperationMetadata;
import org.netbeans.modules.xml.xpath.ext.metadata.impl.OperationMetadataImpl.GTOperationMetadata;
import org.netbeans.modules.xml.xpath.ext.metadata.impl.OperationMetadataImpl.LEOperationMetadata;
import org.netbeans.modules.xml.xpath.ext.metadata.impl.OperationMetadataImpl.LTOperationMetadata;
import org.netbeans.modules.xml.xpath.ext.metadata.impl.OperationMetadataImpl.ModOperationMetadata;
import org.netbeans.modules.xml.xpath.ext.metadata.impl.OperationMetadataImpl.MultOperationMetadata;
import org.netbeans.modules.xml.xpath.ext.metadata.impl.OperationMetadataImpl.NegativeOperationMetadata;
import org.netbeans.modules.xml.xpath.ext.metadata.impl.OperationMetadataImpl.NotEqualOperationMetadata;
import org.netbeans.modules.xml.xpath.ext.metadata.impl.OperationMetadataImpl.OrOperationMetadata;
import org.netbeans.modules.xml.xpath.ext.metadata.impl.OperationMetadataImpl.SubtractionOperationMetadata;
import org.netbeans.modules.xml.xpath.ext.metadata.impl.OperationMetadataImpl.UnionOperationMetadata;

/**
 * Types of the core XPath operations.
 * 
 * @author Enrico Lelina
 * @version 
 */
public enum CoreOperationType {

    OP_SUM(new AdditionOperationMetadata()), // Operator code: sum
    OP_MINUS(new SubtractionOperationMetadata()), // Operator code: minus
    OP_MULT(new MultOperationMetadata()), // Operator code: multiplication
    OP_DIV(new DivOperationMetadata()), // Operator code: division
    OP_MOD(new ModOperationMetadata()), // Operator code: mod
    OP_NEGATIVE(new NegativeOperationMetadata()), // Operator code: negative/unary minus
    OP_AND(new AndOperationMetadata()), // Operator code: and
    OP_OR(new OrOperationMetadata()), // Operator code: or
    OP_EQ(new EqualOperationMetadata()), // Operator code: equal
    OP_NE(new NotEqualOperationMetadata()), // Operator code: not equal
    OP_LT(new LTOperationMetadata()), // Operator code: less than
    OP_LE(new LEOperationMetadata()), // Operator code: less than or equal
    OP_GT(new GTOperationMetadata()), // Operator code: greater than
    OP_GE(new GEOperationMetadata()), // Operator code: greater than or equal
    OP_UNION(new UnionOperationMetadata()); // Operator code: union "|"

    private OperationMetadata myMetadata;

    private CoreOperationType(OperationMetadata metadata) {
        myMetadata = metadata;
    }
    
    public OperationMetadata getMetadata() {
        return myMetadata;
    }
    
    //==========================================================================
    
    private static HashMap<String, CoreOperationType> sNameTypeMap;
    
    public static synchronized CoreOperationType getTypeByName(String operName) {
        if (sNameTypeMap == null) {
            sNameTypeMap = new HashMap<String, CoreOperationType>();
            //
            for(CoreOperationType type : CoreOperationType.values()) {
                String typeName = type.getMetadata().getName();
                sNameTypeMap.put(typeName, type);
            }
        }
        //
        return sNameTypeMap.get(operName);
    }
}
