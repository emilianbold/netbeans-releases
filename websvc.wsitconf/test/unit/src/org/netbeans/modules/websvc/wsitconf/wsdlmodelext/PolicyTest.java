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
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.websvc.wsitconf.wsdlmodelext;

import java.io.File;
import java.util.Collection;
import junit.framework.*;
import org.netbeans.modules.websvc.wsitconf.util.TestCatalogModel;
import org.netbeans.modules.websvc.wsitconf.util.TestUtil;
import org.netbeans.modules.websvc.wsitmodelext.policy.All;
import org.netbeans.modules.websvc.wsitmodelext.policy.PolicyReference;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.WSDLComponentFactory;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;

/**
 *
 * @author Martin Grebac
 */
public class PolicyTest extends TestCase {
    
    public PolicyTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
    }

    @Override
    protected void tearDown() throws Exception {
        TestCatalogModel.getDefault().setDocumentPooling(false);
    }

    public void testWrite() throws Exception {
        TestCatalogModel.getDefault().setDocumentPooling(true);
        WSDLModel model = TestUtil.loadWSDLModel("../wsdlmodelext/resources/policy.xml");
        WSDLComponentFactory fact = model.getFactory();
        
        model.startTransaction();

        Definitions d = model.getDefinitions();
        Binding b = (Binding) d.getBindings().toArray()[0];
        
        PolicyModelHelper.createPolicy(b);
        
        Collection<BindingOperation> bindingops = b.getBindingOperations();
        for (BindingOperation bo : bindingops) {
            PolicyModelHelper.createPolicy(bo.getBindingInput());
            PolicyModelHelper.createPolicy(bo.getBindingOutput());
        }

        model.endTransaction();

        TestUtil.dumpToFile(model.getBaseDocument(), new File("C:\\HelloService.wsdl"));
        readAndCheck(model);
    }

    private void readAndCheck(WSDLModel model) {
        
        // the model operation is not enclosed in transaction inorder to catch 
        // whether the operations do not try to create non-existing elements
        
        Definitions d = model.getDefinitions();
        Binding b = (Binding) d.getBindings().toArray()[0];
        
        All all = PolicyModelHelper.createPolicy(b);
        assertNotNull("Top Level Policy", all);
        
        Collection<PolicyReference> polRefs = b.getExtensibilityElements(PolicyReference.class);
        assertEquals("Top Level Policy Ref Size", 1, polRefs.size());
        assertEquals("Top Level Policy Ref URI", "#NewWebServicePortBindingPolicy", polRefs.iterator().next().getPolicyURI());
        
        Collection<BindingOperation> bindingops = b.getBindingOperations();
        for (BindingOperation bo : bindingops) {
            all = PolicyModelHelper.createPolicy(bo.getBindingInput());
            assertNotNull("Binding Input Policy", all);
            
            all = PolicyModelHelper.createPolicy(bo.getBindingOutput());
            assertNotNull("Binding Output Policy", all);
        }
        
    }

    public String getTestResourcePath() {
        return "../wsdlmodelext/resources/policy.xml";
    }
    
}
