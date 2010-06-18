/*
 * WSDLSemanticsVisitorTest.java
 * JUnit based test
 *
 * Created on January 29, 2007, 10:39 AM
 */

package org.netbeans.modules.xml.wsdl.validator.visitor;

import junit.framework.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import org.netbeans.modules.xml.schema.model.Annotation;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingFault;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Documentation;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.Fault;
import org.netbeans.modules.xml.wsdl.model.Import;
import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.NotificationOperation;
import org.netbeans.modules.xml.wsdl.model.OneWayOperation;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.Output;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.RequestResponseOperation;
import org.netbeans.modules.xml.wsdl.model.Service;
import org.netbeans.modules.xml.wsdl.model.SolicitResponseOperation;
import org.netbeans.modules.xml.wsdl.model.Types;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.xsd.WSDLSchema;
import org.netbeans.modules.xml.wsdl.model.spi.GenericExtensibilityElement.StringAttribute;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.netbeans.modules.xml.wsdl.validator.WSDLSemanticValidator;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.xam.spi.Validation;
import org.netbeans.modules.xml.xam.spi.ValidationResult;
import org.netbeans.modules.xml.xam.spi.Validator;
import org.netbeans.modules.xml.xam.spi.Validation.ValidationType;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;

/**
 *
 * @author radval
 */
public class WSDLSemanticsVisitorTest extends TestCase {
    
    public WSDLSemanticsVisitorTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    /**
     * Test of getResultItems method, of class org.netbeans.modules.xml.wsdl.validator.visitor.WSDLSemanticsVisitor.
     */
    public void testGetResultItems() {
        System.out.println("getResultItems");
        
        WSDLSemanticValidator wv = new WSDLSemanticValidator(); 
        Validation v = new Validation();
        List<Model> models = new ArrayList<Model>();
        WSDLSemanticsVisitor instance = new WSDLSemanticsVisitor(wv, v, models);
        
        List<ResultItem> expResult = new ArrayList<ResultItem>();
        List<ResultItem> result = instance.getResultItems();
        assertEquals(expResult, result);
       
    }

    /**
     * Test of getValidation method, of class org.netbeans.modules.xml.wsdl.validator.visitor.WSDLSemanticsVisitor.
     */
    public void testGetValidation() {
        System.out.println("getValidation");
        
        WSDLSemanticValidator wv = new WSDLSemanticValidator(); 
        Validation v = new Validation();
        List<Model> models = new ArrayList<Model>();
        WSDLSemanticsVisitor instance = new WSDLSemanticsVisitor(wv, v, models);
        
        
        Validation expResult = v;
        Validation result = instance.getValidation();
        assertEquals(expResult, result);
        
    }
}
