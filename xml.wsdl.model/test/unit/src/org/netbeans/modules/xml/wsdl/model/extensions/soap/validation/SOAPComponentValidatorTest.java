/*
 * SOAPComponentValidatorTest.java
 * JUnit based test
 *
 * Created on June 12, 2006, 6:37 PM
 */

package org.netbeans.modules.xml.wsdl.model.extensions.soap.validation;

import junit.framework.*;
import java.io.File;
import java.io.FileFilter;
import java.net.URI;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ResourceBundle;

import org.netbeans.modules.xml.wsdl.model.Util;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.spi.Validation;
import org.netbeans.modules.xml.xam.spi.Validation.ValidationType;
import org.netbeans.modules.xml.xam.spi.ValidationResult;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;

import org.netbeans.modules.xml.wsdl.model.TestCatalogModel;

/**
 *
 * @author afung
 */
public class SOAPComponentValidatorTest extends TestCase {

    private static final ResourceBundle mMessages =
        ResourceBundle.getBundle("org.netbeans.modules.xml.wsdl.model.extensions.soap.validation.Bundle");

    public SOAPComponentValidatorTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
        TestCatalogModel.getDefault().clearDocumentPool();
    }

    private ValidationResult validate(String relativePath) throws Exception {
        WSDLModel model = Util.loadWSDLModel(relativePath);
        Validation validation = new Validation();
        ValidationType validationType = Validation.ValidationType.COMPLETE;
        SOAPComponentValidator instance = new SOAPComponentValidator();
        ValidationResult result = 
            instance.validate(model, validation, validationType);
        return result;
    }
    
    private void validate(String relativePath, HashSet<String> expectedErrors)
        throws Exception {
        System.out.println(relativePath);
        ValidationResult result = validate(relativePath);
        Iterator<ResultItem> it = result.getValidationResult().iterator();
        while (it.hasNext()) {
            ResultItem item = it.next();
            System.out.println("    " + item.getDescription());
            assertTrue(item.getDescription(), expectedErrors.contains(item.getDescription()));
        }
        if (result.getValidationResult().size() == 0 && expectedErrors.size() > 0) {
            fail("Expected at least " + expectedErrors.size() + " error(s).  Got 0 errors instead");
        }
    }
    /**
     * Test of getName method, of class org.netbeans.modules.xml.wsdl.model.extensions.soap.validation.SOAPComponentValidator.
     */
    public void testGetName() throws Exception {
        SOAPComponentValidator instance = new SOAPComponentValidator();
        
        String expResult = instance.getClass().getName();
        String result = instance.getName();
        assertEquals(expResult, result);
    }

    /**
     * Test of validate method, of class org.netbeans.modules.xml.wsdl.model.extensions.soap.validation.SOAPComponentValidator.
     */
    public void testValidWSDLs() throws Exception {
        // Grab all our WSDL files to test using a known WSDL
        URI resource = Util.getResourceURI("extensions/soap/validation/resources/valid/AccountTransaction.wsdl");
        File resourceFile = new File(resource);
        File[] wsdls = resourceFile.getParentFile().listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".wsdl");
            }});
        for (int ii = 0; ii < wsdls.length; ii++) {
            String relativePath = 
                "extensions/soap/validation/resources/valid/" + 
                wsdls[ii].getName();
            ValidationResult result = validate(relativePath);
            assertTrue(result.getValidationResult().size() == 0);
        }
    }
    
    public void testSOAPAddressBadLocation() throws Exception {
        HashSet<String> expectedErrors = new HashSet<String>();
        expectedErrors.add(mMessages.getString("SOAPAddressValidator.Unsupported_location_attribute"));
        
        String relativePath = "extensions/soap/validation/resources/invalid/SOAPAddressBadLocation.wsdl";
        validate(relativePath, expectedErrors);        
    }
    
    public void testSOAPAddressMissingLocation() throws Exception {
        HashSet<String> expectedErrors = new HashSet<String>();
        expectedErrors.add(mMessages.getString("SOAPAddressValidator.Missing_location"));
        
        String relativePath = "extensions/soap/validation/resources/invalid/SOAPAddressMissingLocation.wsdl";
        validate(relativePath, expectedErrors);          
    }
    
    public void testSOAPBindingBadStyle() throws Exception {
        HashSet<String> expectedErrors = new HashSet<String>();
        expectedErrors.add(mMessages.getString("SOAPBindingValidator.Unsupported_style_attribute"));
        expectedErrors.add(mMessages.getString("SOAPOperationValidator.Unsupported_style_attribute"));
                
        String relativePath = "extensions/soap/validation/resources/invalid/SOAPBindingBadStyle.wsdl";
        validate(relativePath, expectedErrors);          
    }

    public void testSOAPBindingBadTransportURI() throws Exception {
        HashSet<String> expectedErrors = new HashSet<String>();
        expectedErrors.add(mMessages.getString("SOAPBindingValidator.Unsupported_transport"));
                
        String relativePath = "extensions/soap/validation/resources/invalid/SOAPBindingBadTransportURI.wsdl";
        validate(relativePath, expectedErrors);        
    }
    
    public void testSOAPBindingMissingTransportURI() throws Exception {
        HashSet<String> expectedErrors = new HashSet<String>();
        expectedErrors.add(mMessages.getString("SOAPBindingValidator.Transport_URI_required"));
                
        String relativePath = "extensions/soap/validation/resources/invalid/SOAPBindingMissingTransportURI.wsdl";
        validate(relativePath, expectedErrors);        
    }
    
    public void testSOAPBindingMissingAddress() throws Exception {
        HashSet<String> expectedErrors = new HashSet<String>();
        expectedErrors.add(mMessages.getString("SOAPAddressValidator.Missing_SoapAddress"));
        
        String relativePath = "extensions/soap/validation/resources/invalid/SOAPBindingInvalidAddress.wsdl";
        validate(relativePath, expectedErrors);
    }
    
    public void testSOAPBindingMultipleAddresses() throws Exception {
        HashSet<String> expectedErrors = new HashSet<String>();
        expectedErrors.add(mMessages.getString("SOAPAddressValidator.Only_one_SoapAddress_allowed"));
        
        String relativePath = "extensions/soap/validation/resources/invalid/SOAPBindingMultipleSoapAddress.wsdl";
        validate(relativePath, expectedErrors);
    }
    
    public void testSOAPBodyBadUse() throws Exception {
        HashSet<String> expectedErrors = new HashSet<String>();
        expectedErrors.add(mMessages.getString("SOAPBodyValidator.Unsupported_use_attribute"));
                
        String relativePath = "extensions/soap/validation/resources/invalid/SOAPBodyBadUse.wsdl";
        validate(relativePath, expectedErrors);          
    }

    public void testSOAPBodyMultipleElements() throws Exception {
        HashSet<String> expectedErrors = new HashSet<String>();
        expectedErrors.add(mMessages.getString("SOAPBodyValidator.Only_one_body_allowed"));
        expectedErrors.add(java.text.MessageFormat.format(
                mMessages.getString("SOAPBodyValidator.Part_already_in_use_by_elem"),
                new Object[] {
                    "result",
                    "HelloIF_sayHelloResponse",
                    "{http://schemas.xmlsoap.org/wsdl/soap/}body"
                }));
        expectedErrors.add(java.text.MessageFormat.format(
                mMessages.getString("SOAPBodyValidator.Part_already_in_use_by_elem"),
                new Object[] {
                    "body",
                    "HelloIF_sayHello",
                    "{http://schemas.xmlsoap.org/wsdl/soap/}body"
                }));

        String relativePath = "extensions/soap/validation/resources/invalid/SOAPBodyMultipleElements.wsdl";
        validate(relativePath, expectedErrors);
        
        relativePath = "extensions/soap/validation/resources/invalid/SOAPBodyMultipleElements1.wsdl";
        validate(relativePath, expectedErrors);
    }
        
    public void testSOAPFaultBadUse() throws Exception {
        HashSet<String> expectedErrors = new HashSet<String>();
        expectedErrors.add(mMessages.getString("SOAPFaultValidator.Unsupported_use_attribute"));
        expectedErrors.add(mMessages.getString("SOAPFaultValidator.Fault_name_not_match"));
                
        String relativePath = "extensions/soap/validation/resources/invalid/SOAPFaultBadUse.wsdl";
        validate(relativePath, expectedErrors);         
    }
    
    public void testSOAPFaultMissingName() throws Exception {
        HashSet<String> expectedErrors = new HashSet<String>();
        expectedErrors.add(mMessages.getString("SOAPFaultValidator.Missing_name"));
                
        String relativePath = "extensions/soap/validation/resources/invalid/SOAPFaultMissingName.wsdl";
        validate(relativePath, expectedErrors);        
    }

    public void testSOAPFaultMultipleElements() throws Exception {
        HashSet<String> expectedErrors = new HashSet<String>();
        expectedErrors.add(mMessages.getString("SOAPFaultValidator.Only_one_fault_allowed"));
        expectedErrors.add(mMessages.getString("SOAPFaultValidator.Fault_name_not_match"));
                
        String relativePath = "extensions/soap/validation/resources/invalid/SOAPFaultMultipleElements.wsdl";
        validate(relativePath, expectedErrors);        
    }
        
    public void testSOAPHeaderBadUse() throws Exception {
        HashSet<String> expectedErrors = new HashSet<String>();
        expectedErrors.add(mMessages.getString("SOAPHeaderValidator.Unsupported_header_use_attribute"));
                
        String relativePath = "extensions/soap/validation/resources/invalid/SOAPHeaderBadUse.wsdl";
        validate(relativePath, expectedErrors);         
    }
 
    public void testSOAPHeaderMissingMessage() throws Exception {
        HashSet<String> expectedErrors = new HashSet<String>();
        expectedErrors.add(mMessages.getString("SOAPHeaderValidator.Missing_message"));
                
        String relativePath = "extensions/soap/validation/resources/invalid/SOAPHeaderMissingMessage.wsdl";
        validate(relativePath, expectedErrors);          
    }
    
    public void testSOAPHeaderMissingPart() throws Exception {
        HashSet<String> expectedErrors = new HashSet<String>();
        expectedErrors.add(mMessages.getString("SOAPHeaderValidator.Missing_part"));
                
        String relativePath = "extensions/soap/validation/resources/invalid/SOAPHeaderMissingPart.wsdl";
        validate(relativePath, expectedErrors);         
    }
    
    public void testSOAPHeaderMissingUse() throws Exception {
        HashSet<String> expectedErrors = new HashSet<String>();
        expectedErrors.add(mMessages.getString("SOAPHeaderValidator.Missing_use"));
                
        String relativePath = "extensions/soap/validation/resources/invalid/SOAPHeaderMissingUse.wsdl";
        validate(relativePath, expectedErrors);         
    }
    
    public void testSOAPHeaderFaultBadUse() throws Exception {
        HashSet<String> expectedErrors = new HashSet<String>();
        expectedErrors.add(mMessages.getString("SOAPHeaderFaultValidator.Unsupported_header_fault_use_attribute"));
        expectedErrors.add(java.text.MessageFormat.format(
                mMessages.getString("SOAPHeaderValidator.Part_already_in_use_by_elem"),
                new Object[] {
                    "tns:body",
                    "HelloIF_sayHello",
                    "{http://schemas.xmlsoap.org/wsdl/soap/}header"
                }));
                
        String relativePath = "extensions/soap/validation/resources/invalid/SOAPHeaderFaultBadUse.wsdl";
        validate(relativePath, expectedErrors);              
    }
    
    public void testSOAPHeaderFaultMissingMessage() throws Exception {
        HashSet<String> expectedErrors = new HashSet<String>();
        expectedErrors.add(mMessages.getString("SOAPHeaderFaultValidator.Missing_header_fault_message"));
                
        String relativePath = "extensions/soap/validation/resources/invalid/SOAPHeaderFaultMissingMessage.wsdl";
        validate(relativePath, expectedErrors);         
    }
    
    public void testSOAPHeaderFaultMissingPart() throws Exception {
        HashSet<String> expectedErrors = new HashSet<String>();
        expectedErrors.add(mMessages.getString("SOAPHeaderFaultValidator.Missing_header_fault_part"));
                
        String relativePath = "extensions/soap/validation/resources/invalid/SOAPHeaderFaultMissingPart.wsdl";
        validate(relativePath, expectedErrors);           
    }
    
    public void testSOAPHeaderFaultMissingUse() throws Exception {
        HashSet<String> expectedErrors = new HashSet<String>();
        expectedErrors.add(mMessages.getString("SOAPHeaderFaultValidator.Missing_header_fault_use"));
        expectedErrors.add(java.text.MessageFormat.format(
                mMessages.getString("SOAPHeaderValidator.Part_already_in_use_by_elem"),
                new Object[] {
                    "tns:body",
                    "HelloIF_sayHello",
                    "{http://schemas.xmlsoap.org/wsdl/soap/}header"
                }));
                
        String relativePath = "extensions/soap/validation/resources/invalid/SOAPHeaderFaultMissingUse.wsdl";
        validate(relativePath, expectedErrors);         
    }
    
    public void testSOAPOperationBadStyle() throws Exception {
        HashSet<String> expectedErrors = new HashSet<String>();
        expectedErrors.add(mMessages.getString("SOAPOperationValidator.Unsupported_style_attribute"));
                
        String relativePath = "extensions/soap/validation/resources/invalid/SOAPOperationBadStyle.wsdl";
        validate(relativePath, expectedErrors);
        
        relativePath = "extensions/soap/validation/resources/invalid/SOAPOperationBadStyle1.wsdl";
        validate(relativePath, expectedErrors);     
    }   
    
    public void testSOAPOperationMissingBody() throws Exception {
        HashSet<String> expectedErrors = new HashSet<String>();
        
        String relativePath = "extensions/soap/validation/resources/valid/SOAPOperationMissingBody.wsdl";
        validate(relativePath, expectedErrors);
    }
    
    /**
     * Test of validate method, of class org.netbeans.modules.xml.wsdl.model.extensions.soap.validation.SOAPComponentValidator.
     */
    public void test6399367() throws Exception {
        HashSet<String> expectedErrors = new HashSet<String>();
        expectedErrors.add(mMessages.getString("SOAPOperationValidator.Unsupported_style_attribute"));
        expectedErrors.add(mMessages.getString("SOAPBindingValidator.Unsupported_style_attribute"));
        
        String relativePath = "extensions/soap/validation/resources/invalid/6399367.wsdl";
        validate(relativePath, expectedErrors);
    }
    
    /**
     * Test of validate method, of class org.netbeans.modules.xml.wsdl.model.extensions.soap.validation.SOAPComponentValidator.
     */
    public void test6400567() throws Exception {
        HashSet<String> expectedErrors = new HashSet<String>();
        expectedErrors.add(mMessages.getString("SOAPBodyValidator.Unsupported_use_attribute"));
        
        String relativePath = "extensions/soap/validation/resources/invalid/6400567.wsdl";
        validate(relativePath, expectedErrors);
    }
    
    /**
     * Test of validate method, of class org.netbeans.modules.xml.wsdl.model.extensions.soap.validation.SOAPComponentValidator.
     */
    public void test6400569() throws Exception {
        HashSet<String> expectedErrors = new HashSet<String>();
        expectedErrors.add(mMessages.getString("SOAPAddressValidator.Unsupported_location_attribute"));
        
        String relativePath = "extensions/soap/validation/resources/invalid/6400569.wsdl";
        validate(relativePath, expectedErrors);
    }
    
    /**
     * Test of validate method, of class org.netbeans.modules.xml.wsdl.model.extensions.soap.validation.SOAPComponentValidator.
     */
//    public void test6400573() throws Exception {
//        HashSet<String> expectedErrors = new HashSet<String>();
//        expectedErrors.add(mMessages.getString("SOAPBodyValidator.No_abstract_message"));
//        
//        String relativePath = "extensions/soap/validation/resources/invalid/6400573.wsdl";
//        validate(relativePath, expectedErrors);
//    }
    
    /**
     * Test of validate method, of class org.netbeans.modules.xml.wsdl.model.extensions.soap.validation.SOAPComponentValidator.
     */
    public void test6400574() throws Exception {
        HashSet<String> expectedErrors = new HashSet<String>();
        expectedErrors.add(mMessages.getString("SOAPBindingValidator.Only_one_binding_allowed"));
        
        String relativePath = "extensions/soap/validation/resources/invalid/6400574.wsdl";
        validate(relativePath, expectedErrors);
    }
    
    /**
     * Test of validate method, of class org.netbeans.modules.xml.wsdl.model.extensions.soap.validation.SOAPComponentValidator.
     */
//    public void test6400597() throws Exception {
//        HashSet<String> expectedErrors = new HashSet<String>();
//        String relativePath = "extensions/soap/validation/resources/invalid/6400597.wsdl";
//        validate(relativePath, expectedErrors);
//    }
    
    /**
     * Test of validate method, of class org.netbeans.modules.xml.wsdl.model.extensions.soap.validation.SOAPComponentValidator.
     */
//    public void test6400598() throws Exception {
//        String relativePath = "extensions/soap/validation/resources/invalid/6400598.wsdl";
//        ValidationResult result = validate(relativePath);
//        Iterator<ResultItem> it = result.getValidationResult().iterator();
//        System.out.println("6400598");
//        while (it.hasNext()) {
//            ResultItem item = it.next();
//            System.out.println(item.getDescription());
//        }
//    }
    
    /**
     * Test of validate method, of class org.netbeans.modules.xml.wsdl.model.extensions.soap.validation.SOAPComponentValidator.
     */
//    public void test6400610() throws Exception {
//        HashSet<String> expectedErrors = new HashSet<String>();
//        String relativePath = "extensions/soap/validation/resources/invalid/6400610.wsdl";
//        validate(relativePath, expectedErrors);
//    }
}
