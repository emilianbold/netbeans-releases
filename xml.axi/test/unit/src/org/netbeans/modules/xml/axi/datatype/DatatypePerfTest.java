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
import java.util.List;
import junit.framework.*;
import org.netbeans.modules.xml.axi.*;
import org.netbeans.modules.xml.axi.visitor.DefaultVisitor;


/**
 *
 * @author Ayub Khan
 */
public class DatatypePerfTest extends AbstractTestCase {
    
    public static final String TEST_XSD         = "resources/OTA_TravelItinerary.xsd";
    public static final String GLOBAL_ELEMENT   = "OTA_TravelItineraryRS";
    
    private List<AbstractAttribute> attList;
    
    public DatatypePerfTest(String testName) {
        super(testName, TEST_XSD, GLOBAL_ELEMENT);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        attList = new ArrayList<AbstractAttribute>();
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(DatatypePerfTest.class);
        
        return suite;
    }
    
    /**
     * Test of createElement method, of class org.netbeans.modules.xml.axi.XAMFactory.
     */
    public void testCreateDatatype() {
        validateSchema(axiModel.getSchemaModel());
        Element element = globalElement;
        assertNotNull(element);
        
        attList.clear();
        TestVisitor visitor = new TestVisitor();
        visitor.visit(element);
        
        String contentType = "";
        if(element.getType() instanceof Datatype)
            contentType = element.getType().getName();
        print("\n\n=================\nGE: "+element.getName()+"["+contentType+"]");
    }
    
    private void printAttributes(List<Attribute> attList) {
        for(Attribute attr:attList) {
            String attName = "";
            if(attr.getName() != null)
                attName = attr.getName();
            Datatype type = (Datatype) attr.getType();
            if(type != null) {
                print("\n=================\n"+attName+"["+type.getName()+"]");
                printFacets(type);
            }
        }
    }
    
    private void printFacets(final Datatype type) {
        print("\nlength: "+type.getLengths());
        print("\nminLength: "+type.getMinLengths());
        print("\nmaxLength: "+type.getMaxLengths());
        print("\npattern: "+type.getPatterns());
        print("\nenum: "+type.getEnumerations());
        print("\nwhitespace: "+type.getWhiteSpaces());
    }
    
    private class TestVisitor extends DefaultVisitor {
        
        private int depth = 0;
        
        /**
         * Creates a new instance of TestVisitor
         */
        public TestVisitor() {
            try {
            } catch(Exception ex) {
                //ex.printStackTrace();
                assertTrue("Should not be here", false);
            }
        }
        
        public void visit(Element element) {
            for(AbstractAttribute attr : element.getAttributes()) {
                visit(attr);
            }
            visitChildren(element);
        }
        
        public void visit(AbstractAttribute attribute) {
            attList.add(attribute);
        }
        
        protected void visitChildren(AXIComponent component) {
            for(AXIComponent child: component.getChildren()) {
                child.accept(this);
            }
        }
    }
    
}
