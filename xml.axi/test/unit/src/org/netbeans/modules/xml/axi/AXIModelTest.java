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
import org.netbeans.modules.xml.axi.util.FileUtil;
import org.netbeans.modules.xml.axi.util.ModelValidator;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;


/**
 * The unit test covers the integrity of the AXI model.
 * In reverseEngineer(), it reads a schema file and creates the model.
 * In forwardEngineer(), it reads a xml file creates the model and then
 * code generates a schema. Generated code may not be available.
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class AXIModelTest extends AbstractTestCase {
    
    public static final String TEST_XSD         = "resources/po.xsd";
    public static final String GLOBAL_ELEMENT   = "purchaseOrder";
    
    
    /**
     * AXIModelTest
     */
    public AXIModelTest(String testName) {
        super(testName, TEST_XSD, GLOBAL_ELEMENT);
    }
    
    /**
     * AXIModelTest
     */
    public AXIModelTest(String testName, String schemaFile, String elementName) {
        super(testName, schemaFile, elementName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(AXIModelTest.class);
        
        return suite;
    }
    
    public void testAXIModel() {
        reverseEngineer();
        axiModel.setSchemaDesignPattern(SchemaGenerator.Pattern.GARDEN_OF_EDEN);
        forwardEngineer();
    }
    
    /**
     * Tests forward engineering of AXI model.
     * Creates an AXI tree by parsing an XML input file
     * and then compares it against the DOM tree for the
     * same XML.
     */
    private void forwardEngineer() {
        if(referenceXML == null) return;
        FileUtil.parseXMLAndPopulateAXIModel(
                referenceXML, getAXIModel());
        ModelValidator visitor = new ModelValidator(referenceXML);
        Element po = getAXIModel().getRoot().getElements().get(0);
        Element first = (Element)po.getChildElements().get(0);
        assert(first.getParentElement() == po);
        boolean result = visitor.visitAndCompareAgainstDOMElement(po);
        this.assertEquals(visitor.getErrorMessage(), true, result);
    }
    
    /**
     * Tests reverse engineering of AXI model.
     * Creates an AXI tree for a schema global element and
     * compares it against the DOM tree.
     */
    public void reverseEngineer() {
        assertNotNull(globalElement);
        assertNotNull(getAXIModel().getRoot());
        //visit each node in the AXI tree and compare against
        //corresponding DOM node.
        ModelValidator visitor = new ModelValidator(referenceXML);
        boolean result = visitor.visitAndCompareAgainstDOMElement(globalElement);
        this.assertEquals(visitor.getErrorMessage(),
                true, result);
    }    
}
