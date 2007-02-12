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
import org.netbeans.modules.websvc.wsitconf.util.TestCatalogModel;
import org.netbeans.modules.websvc.wsitconf.util.TestUtil;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.WSDLComponentFactory;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;

/**
 *
 * @author Martin Grebac
 */
public class RMTest extends TestCase {
    
    public RMTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception { }

    @Override
    protected void tearDown() throws Exception {
        TestCatalogModel.getDefault().setDocumentPooling(false);
    }

    public void testRM() throws Exception {
        TestCatalogModel.getDefault().setDocumentPooling(true);
        WSDLModel model = TestUtil.loadWSDLModel("../wsdlmodelext/resources/policy.xml");
        WSDLComponentFactory fact = model.getFactory();
        
        model.startTransaction();

        Definitions d = model.getDefinitions();
        Binding b = (Binding) d.getBindings().toArray()[0];
        
        assertFalse("RM enabled indicated on empty WSDL", RMModelHelper.isRMEnabled(b));

        RMModelHelper.enableRM(b);
        assertTrue("RM not enabled correctly", RMModelHelper.isRMEnabled(b));

        assertNull("Inactivity timeout set even when not specified", RMModelHelper.getInactivityTimeout(b));
        RMModelHelper.setInactivityTimeout(b, "112233");
        assertEquals("Inactivity Timeout Value Not Saved/Read Correctly", "112233", RMModelHelper.getInactivityTimeout(b));

        assertFalse("Flow Control enabled indicated", RMMSModelHelper.isFlowControlEnabled(b));
        RMMSModelHelper.enableFlowControl(b);
        RMMSModelHelper.disableFlowControl(b);
        RMMSModelHelper.enableFlowControl(b);
        assertTrue("Flow Control disabled indicated", RMMSModelHelper.isFlowControlEnabled(b));

        assertNull("Max Receive Buffer Size set even when not specified", RMMSModelHelper.getMaxReceiveBufferSize(b));
        RMMSModelHelper.setMaxReceiveBufferSize(b, "2233");
        assertEquals("Max Receive Buffer Size Value Not Saved/Read Correctly", "2233", RMMSModelHelper.getMaxReceiveBufferSize(b));

        assertFalse("Ordered enabled indicated", RMSunModelHelper.isOrderedEnabled(b));
        RMSunModelHelper.enableOrdered(b);
        assertTrue("Ordered disabled indicated", RMSunModelHelper.isOrderedEnabled(b));
        RMSunModelHelper.disableOrdered(b);
        assertFalse("Ordered enabled indicated", RMSunModelHelper.isOrderedEnabled(b));
        
        RMModelHelper.disableRM(b);
        assertFalse("RM not disabled correctly", RMModelHelper.isRMEnabled(b));
        assertNull("RM not disabled correctly", RMModelHelper.getInactivityTimeout(b));
        
        model.endTransaction();

        TestUtil.dumpToFile(model.getBaseDocument(), new File("C:\\RMService.wsdl"));
    }

    public String getTestResourcePath() {
        return "../wsdlmodelext/resources/policy.xml";
    }
    
}
