/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.xml.schema.model.validation;

import java.util.List;
import junit.framework.*;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelReference;
import org.netbeans.modules.xml.schema.model.TestCatalogModel;
import org.netbeans.modules.xml.schema.model.Util;
import org.netbeans.modules.xml.xam.spi.Validation;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;

/**
 *
 * @author nn136682
 */
public class SchemaXsdBasedValidatorTest extends TestCase {
    
    public SchemaXsdBasedValidatorTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
    }
    
    protected void tearDown() throws Exception {
        TestCatalogModel.getDefault().clearDocumentPool();
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(SchemaXsdBasedValidatorTest.class);
        
        return suite;
    }
    
    public void testResolveResource() throws Exception {
        Validation validation = new Validation();
        SchemaModel model = Util.loadSchemaModel("validation/SynchronousSample.xsd");
        SchemaModelReference imported = model.getSchema().getSchemaReferences().iterator().next();
        SchemaModel importedModel = imported.resolveReferencedModel();
        String expected1 = "s4s-att-not-allowed: Attribute 'nameXXXX' cannot appear in element 'attribute'.";
        String expected2 = "s4s-att-must-appear: Attribute 'name' must appear in element 'attribute'.";

        validation.validate(importedModel, Validation.ValidationType.COMPLETE);
        List<ResultItem> results0 = validation.getValidationResult();
        assertEquals(2, results0.size());
        assertEquals("from imported model", importedModel, results0.get(0).getModel());
        assertEquals(expected1, results0.get(0).getDescription());
        assertEquals("from imported model", importedModel, results0.get(1).getModel());
        assertEquals(expected2, results0.get(1).getDescription()); 
        
        Validation validation2 = new Validation();
        validation2.validate(model, Validation.ValidationType.COMPLETE);
        List<ResultItem> results = validation2.getValidationResult();
        assertEquals(2, results.size());
        assertEquals("from imported model", importedModel, results.get(0).getModel());
        assertEquals(expected1, results.get(0).getDescription());
        assertEquals("from imported model", importedModel, results.get(1).getModel());
        assertEquals(expected2, results.get(1).getDescription()); 
    }
    
}
