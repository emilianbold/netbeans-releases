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
import junit.framework.*;
import org.netbeans.modules.websvc.wsitconf.ui.ComboConstants;
import org.netbeans.modules.websvc.wsitconf.util.TestCatalogModel;
import org.netbeans.modules.websvc.wsitconf.util.TestUtil;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.WSDLComponentFactory;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;

/**
 *
 * @author Martin Grebac
 */
public class TxTest extends TestCase {
    
    public TxTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception { }

    @Override
    protected void tearDown() throws Exception {
        TestCatalogModel.getDefault().setDocumentPooling(false);
    }

    public void testTx() throws Exception {
        TestCatalogModel.getDefault().setDocumentPooling(true);
        WSDLModel model = TestUtil.loadWSDLModel("../wsdlmodelext/resources/policy.xml");
        WSDLComponentFactory fact = model.getFactory();
        
        model.startTransaction();

        Definitions d = model.getDefinitions();
        Binding b = (Binding) d.getBindings().toArray()[0];
        
        BindingOperation bop = (BindingOperation) b.getBindingOperations().toArray()[0];
        assertEquals("Tx enabled indicated on empty WSDL", ComboConstants.TX_NOTSUPPORTED, TxModelHelper.getTx(bop, null));

        TxModelHelper.setTx(bop, null, ComboConstants.TX_MANDATORY);
        assertEquals("TxValue", ComboConstants.TX_MANDATORY, TxModelHelper.getTx(bop, null));
        TxModelHelper.setTx(bop, null, ComboConstants.TX_NEVER);
        assertEquals("TxValue", ComboConstants.TX_NOTSUPPORTED, TxModelHelper.getTx(bop, null));
        TxModelHelper.setTx(bop, null, ComboConstants.TX_REQUIRED);
        assertEquals("TxValue", ComboConstants.TX_REQUIRED, TxModelHelper.getTx(bop, null));
        TxModelHelper.setTx(bop, null, ComboConstants.TX_REQUIRESNEW);
        assertEquals("TxValue", ComboConstants.TX_REQUIRESNEW, TxModelHelper.getTx(bop, null));
        TxModelHelper.setTx(bop, null, ComboConstants.TX_SUPPORTED);
        assertEquals("TxValue", ComboConstants.TX_SUPPORTED, TxModelHelper.getTx(bop, null));
        
        model.endTransaction();

        TestUtil.dumpToFile(model.getBaseDocument(), new File("C:\\TestService.wsdl"));
    }

    public String getTestResourcePath() {
        return "../wsdlmodelext/resources/policy.xml";
    }
    
}
