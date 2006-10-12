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

import javax.swing.text.Document;
import junit.framework.*;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;


/**
 *
 * @author Ayub Khan
 */
public class SchemaGeneratorDatatypesTest extends AbstractTestCase {
    
    public static final String TEST_XSD         = "resources/types.xsd";
    public static final String GLOBAL_ELEMENT   = "purchaseOrder";
    
    private Document doc = null;
    
    public SchemaGeneratorDatatypesTest(String testName) {
        super(testName, TEST_XSD, GLOBAL_ELEMENT);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(SchemaGeneratorDatatypesTest.class);
        
        return suite;
    }
    
    /**
     * Test of createElement method, of class org.netbeans.modules.xml.axi.XAMFactory.
     */
    public void testGenerateSchema() throws Exception {
        Element element = globalElement;
        assertNotNull(element);
        SchemaModel sm = getSchemaModel();
        validateSchema(sm);
        doc = ((AbstractDocumentModel)sm).getBaseDocument();
        //global element name change (just to get the forward generation to work
        axiModel.startTransaction();
        for(Element e:axiModel.getRoot().getElements())
            if(e.getName().equals(GLOBAL_ELEMENT))
                e.setName(e.getName()+"_");
        axiModel.endTransaction();
        for(GlobalElement ge:sm.getSchema().getElements()) {
            if(ge.getName().startsWith(GLOBAL_ELEMENT))
                assertEquals("updated schemamodel", GLOBAL_ELEMENT+"_", ge.getName());
        }
        validateSchema(axiModel.getSchemaModel());
    }
}
