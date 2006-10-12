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

package org.netbeans.modules.xml.axi.datatype;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import junit.framework.*;
import org.netbeans.modules.xml.axi.*;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.Length;
import org.netbeans.modules.xml.schema.model.Pattern;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.TotalDigits;


/**
 *
 * @author Ayub Khan
 */
public class DatatypeFactoryTest extends AbstractTestCase {
    
    public static final String TEST_XSD         = "resources/types.xsd";
    public static final String GLOBAL_ELEMENT   = "purchaseOrder";
    
    private List<Attribute> attList;;
    
    public DatatypeFactoryTest(String testName) {
        super(testName, TEST_XSD, GLOBAL_ELEMENT);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        attList = new ArrayList<Attribute>();
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(DatatypeFactoryTest.class);
        
        return suite;
    }
    
    /**
     * Test of createElement method, of class org.netbeans.modules.xml.axi.XAMFactory.
     */
    public void testFindApplicableFacets() {
        validateSchema(axiModel.getSchemaModel());
        Collection<GlobalSimpleType> types = getSchemaModel().getSchema().getSimpleTypes();
        assertEquals("primitiveTypes", 7, types.size());
        
        for(GlobalSimpleType type:types) {
            if(type.getName().equals("myDate")) {
                long start = System.currentTimeMillis();
                List<Class<? extends SchemaComponent>> facets =
                        DatatypeFactory.getDefault().getApplicableSchemaFacets(type);
                long end = System.currentTimeMillis();
                print("time taken to find facets from GlobalSimpleType: "+(end-start)+"ms");
                assertEquals("Facets", 6, facets.size());
                start = System.currentTimeMillis();
                facets =
                        DatatypeFactory.getDefault().getApplicableSchemaFacets(type);
                end = System.currentTimeMillis();
                print("time taken to find same facets (second time) from GlobalSimpleType: "+(end-start)+"ms");
            } else if(type.getName().equals("myDate1")) {
                assertEquals("Facets", 7, DatatypeFactory.getDefault().getApplicableSchemaFacets(type).size());
                assertEquals("Facets", Pattern.class, DatatypeFactory.getDefault().getApplicableSchemaFacets(type).get(0));
            } else if(type.getName().equals("SKU")) {
                assertEquals("Facets", 6, DatatypeFactory.getDefault().getApplicableSchemaFacets(type).size());
                assertEquals("Facets", Length.class, DatatypeFactory.getDefault().getApplicableSchemaFacets(type).get(0));
            } else if(type.getName().equals("ListOfMyDate")) {
                assertEquals("Facets", 6, DatatypeFactory.getDefault().getApplicableSchemaFacets(type).size());
                //test same instance is returned
                assertEquals("Facets", DatatypeFactory.getDefault().getApplicableSchemaFacets(type), DatatypeFactory.getDefault().getApplicableSchemaFacets(type));
            } else if(type.getName().equals("Cost")) {
                assertEquals("Facets", 9, DatatypeFactory.getDefault().getApplicableSchemaFacets(type).size());
                assertEquals("Facets", TotalDigits.class, DatatypeFactory.getDefault().getApplicableSchemaFacets(type).get(0));
            }
        }
    }
    
}
