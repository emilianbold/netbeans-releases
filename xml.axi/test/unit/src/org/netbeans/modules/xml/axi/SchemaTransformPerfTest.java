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

import java.io.IOException;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import junit.framework.*;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;


/**
 *
 * @author Ayub Khan
 */
public class SchemaTransformPerfTest extends AbstractTestCase {
    
//	public static final String OTA_SIMPLE_XSD   = "resources/OTA_TravelItinerary.xsd";
    public static final String OTA_SIMPLE_XSD   = "resources/OTA_TI_simple.xsd";
    public static final String GLOBAL_ELEMENT   = "Line";
    
    private Document doc = null;
    
    public SchemaTransformPerfTest(String testName) {
        super(testName, OTA_SIMPLE_XSD, GLOBAL_ELEMENT);
    }
    
    public static Test suite() {
//        TestSuite suite = new TestSuite(DesignPatternTest.class);
        TestSuite suite = new TestSuite();
//		suite.addTest(new SchemaTransformPerfTest("testTransformPerf"));
        return suite;
    }
    
    public void testTransformPerf() {
        print("testTransformPerf");
        try {
            loadModel(OTA_SIMPLE_XSD);
        } catch (Exception ex) {
            //ex.printStackTrace();
            assertTrue("Should not be here", false);
        }
        
        assertEquals("global complex types",3,getSchemaModel().getSchema().getComplexTypes().size());
        assertEquals("global attribute groups",2,getSchemaModel().getSchema().getAttributeGroups().size());
        assertEquals("global groups",0,getSchemaModel().getSchema().getGroups().size());
        assertEquals("global attributes",0,getSchemaModel().getSchema().getAttributes().size());
        assertEquals("global simple types",3,getSchemaModel().getSchema().getSimpleTypes().size());
        assertEquals("global elements",3,getSchemaModel().getSchema().getElements().size());
        
        //Transform from Venetian Blind to Russian Doll
        try {
            long start = System.currentTimeMillis();
            SchemaGeneratorFactory.getDefault().transformSchema(
                    axiModel.getSchemaModel(), SchemaGenerator.Pattern.RUSSIAN_DOLL);
            long end = System.currentTimeMillis();
            print("Time taken to transform from VB to RD: "+(end-start)+"ms");
        } catch (IOException ex) {
            //ex.printStackTrace();
            assertTrue("Should not be here", false);
        }
        
//		printDocument();
        assertEquals("global complex types",0,getSchemaModel().getSchema().getComplexTypes().size());
        assertEquals("global simple types",0,getSchemaModel().getSchema().getSimpleTypes().size());
        assertEquals("global elements",2,getSchemaModel().getSchema().getElements().size());
        
        //Transform from Russian Doll to Venetian Blind
        try {
            long start = System.currentTimeMillis();
            SchemaGeneratorFactory.getDefault().transformSchema(
                    axiModel.getSchemaModel(), SchemaGenerator.Pattern.VENITIAN_BLIND);
            long end = System.currentTimeMillis();
            print("Time taken to transform from RD to VB: "+(end-start)+"ms");
        } catch (IOException ex) {
            //ex.printStackTrace();
            assertTrue("Should not be here", false);
        }
        
//		printDocument();
        assertEquals("global complex types",2,getSchemaModel().getSchema().getComplexTypes().size());
        assertEquals("global simple types",5,getSchemaModel().getSchema().getSimpleTypes().size());
        assertEquals("global elements",2,getSchemaModel().getSchema().getElements().size());
        
        //Transform from Venetian Blind to Salami Slice
        try {
            long start = System.currentTimeMillis();
            SchemaGeneratorFactory.getDefault().transformSchema(
                    axiModel.getSchemaModel(), SchemaGenerator.Pattern.SALAMI_SLICE);
            long end = System.currentTimeMillis();
            print("Time taken to transform from VB to SS: "+(end-start)+"ms");
        } catch (IOException ex) {
            //ex.printStackTrace();
            assertTrue("Should not be here", false);
        }
        
//		printDocument();
        assertEquals("global complex types",0,getSchemaModel().getSchema().getComplexTypes().size());
        assertEquals("global simple types",0,getSchemaModel().getSchema().getSimpleTypes().size());
        assertEquals("global elements",9,getSchemaModel().getSchema().getElements().size());
        
        //Transform from Salami Slice to Venetian Blind
        try {
            long start = System.currentTimeMillis();
            SchemaGeneratorFactory.getDefault().transformSchema(
                    axiModel.getSchemaModel(), SchemaGenerator.Pattern.VENITIAN_BLIND);
            long end = System.currentTimeMillis();
            print("Time taken to transform from SS to VB: "+(end-start)+"ms");
        } catch (IOException ex) {
            //ex.printStackTrace();
            assertTrue("Should not be here", false);
        }
        
//		printDocument();
        assertEquals("global complex types",2,getSchemaModel().getSchema().getComplexTypes().size());
        assertEquals("global simple types",5,getSchemaModel().getSchema().getSimpleTypes().size());
        assertEquals("global elements",2,getSchemaModel().getSchema().getElements().size());
        
        
        //Transform from Venetian Blind to Salami Slice
        try {
            long start = System.currentTimeMillis();
            SchemaGeneratorFactory.getDefault().transformSchema(
                    axiModel.getSchemaModel(), SchemaGenerator.Pattern.GARDEN_OF_EDEN);
            long end = System.currentTimeMillis();
            print("Time taken to transform from VB to GE: "+(end-start)+"ms");
        } catch (IOException ex) {
            //ex.printStackTrace();
            assertTrue("Should not be here", false);
        }
        
//		printDocument();
        assertEquals("global complex types",2,getSchemaModel().getSchema().getComplexTypes().size());
        assertEquals("global simple types",5,getSchemaModel().getSchema().getSimpleTypes().size());
        assertEquals("global elements",9,getSchemaModel().getSchema().getElements().size());
        
        //Transform from Salami Slice to Venetian Blind
        try {
            long start = System.currentTimeMillis();
            SchemaGeneratorFactory.getDefault().transformSchema(
                    axiModel.getSchemaModel(), SchemaGenerator.Pattern.VENITIAN_BLIND);
            long end = System.currentTimeMillis();
            print("Time taken to transform from GE to VB: "+(end-start)+"ms");
        } catch (IOException ex) {
            //ex.printStackTrace();
            assertTrue("Should not be here", false);
        }
        
//		printDocument();
        assertEquals("global complex types",2,getSchemaModel().getSchema().getComplexTypes().size());
        assertEquals("global simple types",5,getSchemaModel().getSchema().getSimpleTypes().size());
        assertEquals("global elements",2,getSchemaModel().getSchema().getElements().size());
    }
    
    private void printDocument() {
        try {
            SchemaModel sm = getSchemaModel();
            doc = ((AbstractDocumentModel)sm).getBaseDocument();
            print("doc: "+doc.getText(0, doc.getLength()));
        } catch (BadLocationException ex) {
            //ex.printStackTrace();
            assertTrue("Should not be here", false);
        }
    }
    
}
