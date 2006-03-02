/*
 * RefactorVisitorTest.java
 * JUnit based test
 *
 * Created on October 18, 2005, 3:57 PM
 */

package org.netbeans.modules.xml.schema.model.impl;

import java.io.IOException;
import java.util.Collections;
import junit.framework.*;
import org.netbeans.modules.xml.schema.model.Util;
import org.netbeans.modules.xml.schema.model.*;
import org.netbeans.modules.xml.schema.model.visitor.*;

/**
 *
 * @author Administrator
 */
public class RefactorVisitorTest extends TestCase {
    
    public static final String TEST_XSD     = "resources/PurchaseOrder.xsd";
    
    private Schema          schema                  = null;
    private GlobalElement   global_element          = null;
    private GlobalType      global_type             = null;
    private GlobalAttribute global_attribute        = null;
    private SchemaModel model;
    
    public RefactorVisitorTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
	model = Util.loadSchemaModel(TEST_XSD);
	schema = model.getSchema();
        
        for(GlobalType type : schema.getComplexTypes()) {
            if(type.getName().endsWith("USAddress")) {
                this.global_type = type;
            }
        }
        
        for(GlobalElement e : schema.getElements()) {
            if(e.getName().endsWith("comment")) {
                this.global_element = e;
            }
        }        
    }

    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(RefactorVisitorTest.class);
        return suite;
    }
        
    public void testRenameGlobalType() throws IOException{
        System.out.println("Renaming global type USAddress to MyAddress...");
        String oldVal = global_type.getName();
        String newVal = "MyAddress";
        FindUsageVisitor usage = new FindUsageVisitor();
        Preview preview_before = usage.findUsages(Collections.singletonList(schema), global_type);
        System.out.println(preview_before.getUsages().size() + " occurances of " + oldVal + " found!!!");
                
        RefactorVisitor visitor = new RefactorVisitor();
        model.startTransaction();
        global_type.setName(newVal);
        model.endTransaction();
        visitor.setRenamedElement(global_type);
        model.startTransaction();
        visitor.rename(preview_before);
        model.endTransaction();
        
        usage = new FindUsageVisitor();
        Preview preview_after = usage.findUsages(Collections.singletonList(schema), global_type);
        System.out.println(preview_after.getUsages().size() + " occurances of " + newVal + " found!!!");
        this.assertEquals(preview_before.getUsages().size(), preview_after.getUsages().size());        
    }
    
    public void testRenameGlobalElement() throws IOException{
        System.out.println("Renaming global element comment to xcomment...");
        String oldVal = global_element.getName();
        String newVal = "xcomment";
        FindUsageVisitor usage = new FindUsageVisitor();
        Preview preview_before = usage.findUsages(Collections.singletonList(schema), global_element);
        System.out.println(preview_before.getUsages().size() + " occurances of " + oldVal + " found!!!");
                
        RefactorVisitor visitor = new RefactorVisitor();
        model.startTransaction();
        global_element.setName(newVal);
        model.endTransaction();
        visitor.setRenamedElement(global_element);
        model.startTransaction();
        visitor.rename(preview_before);
        model.endTransaction();
        
        usage = new FindUsageVisitor();
        Preview preview_after = usage.findUsages(Collections.singletonList(schema), global_element);
        System.out.println(preview_after.getUsages().size() + " occurances of " + newVal + " found!!!");
        this.assertEquals(preview_before.getUsages().size(), preview_after.getUsages().size());
    }
    
    public void testRenameGlobalAttribute() {
    }
}
