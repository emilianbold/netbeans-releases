/*
 * ValidatorSchemaFactoryRegistryTest.java
 * JUnit based test
 *
 * Created on February 6, 2007, 11:13 PM
 */

package org.netbeans.modules.xml.wsdl.validator;

import java.util.ArrayList;
import junit.framework.*;
import java.util.Collection;
import java.util.Hashtable;
import org.netbeans.modules.xml.wsdl.validator.spi.ValidatorSchemaFactory;
import org.openide.util.Lookup;

/**
 *
 * @author radval
 */
public class ValidatorSchemaFactoryRegistryTest extends TestCase {
    
    public ValidatorSchemaFactoryRegistryTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    /**
     * Test of getDefault method, of class org.netbeans.modules.xml.wsdl.validator.ValidatorSchemaFactoryRegistry.
     */
    public void testGetDefault() {
        System.out.println("getDefault");
        
        ValidatorSchemaFactoryRegistry result = ValidatorSchemaFactoryRegistry.getDefault();
        assertNotNull(result);
        
        
        
    }

    /**
     * Test of getValidatorSchemaFactory method, of class org.netbeans.modules.xml.wsdl.validator.ValidatorSchemaFactoryRegistry.
     */
    public void testGetValidatorSchemaFactory() {
        System.out.println("getValidatorSchemaFactory");
        
        String namespace = "";
        ValidatorSchemaFactoryRegistry instance = ValidatorSchemaFactoryRegistry.getDefault();
        
        ValidatorSchemaFactory expResult = null;
        ValidatorSchemaFactory result = instance.getValidatorSchemaFactory(namespace);
        assertEquals(expResult, result);
        
        
        
    }

    /**
     * Test of getAllValidatorSchemaFactories method, of class org.netbeans.modules.xml.wsdl.validator.ValidatorSchemaFactoryRegistry.
     */
    public void testGetAllValidatorSchemaFactories() {
        System.out.println("getAllValidatorSchemaFactories");
        
        ValidatorSchemaFactoryRegistry instance = ValidatorSchemaFactoryRegistry.getDefault();
        
        Collection<ValidatorSchemaFactory> expResult = new ArrayList<ValidatorSchemaFactory>();
        Collection<ValidatorSchemaFactory> result = instance.getAllValidatorSchemaFactories();
        assertEquals(expResult.size(), result.size());
        
       
        
    }
    
}
