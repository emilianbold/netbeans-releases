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
import org.netbeans.modules.xml.axi.visitor.DefaultVisitor;


/**
 *
 * @author Ayub Khan
 */
public class DatatypeTest extends AbstractTestCase {
    
    public static final String TEST_XSD         = "resources/types.xsd";
    public static final String GLOBAL_ELEMENT   = "purchaseOrder";
    
    private List<AbstractAttribute> attList;;
    
    public DatatypeTest(String testName) {
        super(testName, TEST_XSD, GLOBAL_ELEMENT);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        attList = new ArrayList<AbstractAttribute>();
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(DatatypeTest.class);
        
        return suite;
    }
    
    /**
     * Test of createElement method, of class org.netbeans.modules.xml.axi.XAMFactory.
     */
    public void testCreateDatatype() {
        validateSchema(axiModel.getSchemaModel());
        Element element = globalElement;
        assertNotNull(element);
        String contentType;
        List<Attribute> attrs = checkElement(element, "string");
        for(Attribute attr:attrs) {
            if(attr.getName().equals("orderDate"))
                checkAttribute(attr, "date");
            else if(attr.getName().equals("shipDate"))
                checkAttribute(attr, "string");
            else if(attr.getName().equals("backOrder"))
                checkAttribute(attr, "string");
        }
        Collection<AbstractElement> addresses = element.getChildElements();
        for(AbstractElement ae:addresses) {
            if(ae instanceof Element) {
                Element ce = (Element)ae;
                if(ce.getName().equals("shipTo")) {
                    checkElement(ce, "string");
                    Collection<AbstractElement> addrs = ce.getChildElements();
                    for(AbstractElement addr:addrs) {
                        if(addr instanceof Element) {
                            Element address = (Element)addr;
                            if(address.getName().equals("zip")) {
                                attrs = checkElement(address, "decimal");
                            }
                        }
                    }
                } else if(ce.getName().equals("items")) {
                    Collection<AbstractElement> items = ce.getChildElements();
                    for(AbstractElement i:items) {
                        if(i instanceof Element) {
                            Element item = (Element)i;
                            if(item.getName().equals("item")) {
                                checkElement(item, "string");
                                for(Attribute attr:attrs) {
                                    if(attr.getName().equals("partNum"))
                                        checkAttribute(attr, "SKU");
                                }
                            }
                        }
                    }
                    printDebug("\n");
                }
            }
        }
        assertTrue("Should reach here", true);
        validateSchema(axiModel.getSchemaModel());
    }
    
    private List checkElement(final Element element, String elementType) {
        attList.clear();
        TestVisitor visitor = new TestVisitor();
        visitor.visit(element);
        if(element.getType() instanceof Datatype) {
            Datatype type = (Datatype) element.getType();
            String contentType = type != null ? type.getName() : "";
            printDebug("\n\n=================\nAXI Element: "+element.getName()+"["+contentType+"]");
            if(type != null) {
                assertEquals("Element datatype", elementType, contentType);
                if(type.hasFacets())
                    printFacets(type);
            }
        }
        return attList;
    }
    
    private void checkAttribute(Attribute attr, String attrType) {
        String attName = "";
        if(attr.getName() != null)
            attName = attr.getName();
        Datatype type = (Datatype) attr.getType();
        printDebug("\n=================\nAXI Attribute: "+attName+"["+type.getName()+"]");
        if(attrType != null)
            assertEquals("Attribute datatype", attrType, type.getName());
        if(type.hasFacets())
            printFacets(type);
    }
    
    private void printFacets(final Datatype type) {
        printDebug("\nlength: "+type.getLengths());
        printDebug("\nminLength: "+type.getMinLengths());
        printDebug("\nmaxLength: "+type.getMaxLengths());
        printDebug("\npattern: "+type.getPatterns());
        printDebug("\nenum: "+type.getEnumerations());
        printDebug("\nwhitespace: "+type.getWhiteSpaces());
    }
    
    private void printDebug(String message) {
//		printDebug(message);
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
