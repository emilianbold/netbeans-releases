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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.axi;

import junit.framework.*;
import org.netbeans.modules.xml.axi.visitor.DeepAXITreeVisitor;


/**
 * 
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class CyclicModelTest extends AbstractTestCase {
    
    public static final String SCHEMA_A         = "resources/schemaA.xsd";
    public static final String SCHEMA_B         = "resources/schemaB.xsd";
    public static final String SCHEMA_C         = "resources/schemaC.xsd";
    
    /**
     * CyclicModelTest
     */
    public CyclicModelTest(String testName) {
        super(testName, SCHEMA_A, null);
    }
    
    /**
     * CyclicModelTest
     */
    public CyclicModelTest(String testName, String schemaFile, String elementName) {
        super(testName, schemaFile, elementName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(CyclicModelTest.class);        
        return suite;
    }
    
    public void testCyclicModelsNotExpanded() {
        DeepAXITreeVisitor visitor = new DeepAXITreeVisitor();
        long startTime = System.currentTimeMillis();
        visitor.visit(getAXIModel().getRoot());
        long endTime = System.currentTimeMillis();
    }
    
    public void testCyclicModelsExpanded() throws Exception {
        AXIModel modelA = getAXIModel();
        AXIModel modelB = getModel(SCHEMA_B);
        AXIModel modelC = getModel(SCHEMA_C);
        DeepAXITreeVisitor visitor = new DeepAXITreeVisitor();
        visitor.visit(modelA.getRoot());
        visitor.visit(modelB.getRoot());
        visitor.visit(modelC.getRoot());
        modelA.sync();
        modelB.sync();
        modelC.sync();
    }
}
