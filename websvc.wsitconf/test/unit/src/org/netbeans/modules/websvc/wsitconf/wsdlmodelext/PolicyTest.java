/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.websvc.wsitconf.wsdlmodelext;

import java.io.File;
import java.util.Collection;
import junit.framework.*;
import org.netbeans.modules.websvc.wsitconf.util.TestCatalogModel;
import org.netbeans.modules.websvc.wsitconf.util.TestUtil;
import org.netbeans.modules.websvc.wsitmodelext.policy.All;
import org.netbeans.modules.websvc.wsitmodelext.policy.PolicyReference;
import org.netbeans.modules.websvc.wsitmodelext.versioning.ConfigVersion;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.Definitions;
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

    public void testWrite10() throws Exception {
        TestCatalogModel.getDefault().setDocumentPooling(true);
        WSDLModel model = TestUtil.loadWSDLModel("../wsdlmodelext/resources/policy.xml");
        
        model.startTransaction();

        Definitions d = model.getDefinitions();
        Binding b = (Binding) d.getBindings().toArray()[0];
        
        PolicyModelHelper pmh = PolicyModelHelper.getInstance(ConfigVersion.CONFIG_1_0);
        
        pmh.createPolicy(b, true);
        
        Collection<BindingOperation> bindingops = b.getBindingOperations();
        for (BindingOperation bo : bindingops) {
            pmh.createPolicy(bo.getBindingInput(), false);
            pmh.createPolicy(bo.getBindingOutput(), false);
        }

        model.endTransaction();

        TestUtil.dumpToFile(model.getBaseDocument(), new File("C:\\HelloService.wsdl"));
        readAndCheck10(model);
    }

    private void readAndCheck10(WSDLModel model) {
        
        // the model operation is not enclosed in transaction inorder to catch 
        // whether the operations do not try to create non-existing elements
        
        Definitions d = model.getDefinitions();
        Binding b = (Binding) d.getBindings().toArray()[0];
        
        ConfigVersion cfgVersion = PolicyModelHelper.getConfigVersion(b);
        assertEquals(cfgVersion, ConfigVersion.CONFIG_1_0);
        
        PolicyModelHelper pmh = PolicyModelHelper.getInstance(cfgVersion);

        All all = pmh.createPolicy(b, true);
        assertNotNull("Top Level Policy", all);
        
        Collection<PolicyReference> polRefs = b.getExtensibilityElements(PolicyReference.class);
        assertEquals("Top Level Policy Ref Size", 1, polRefs.size());
        assertEquals("Top Level Policy Ref URI", "#NewWebServicePortBindingPolicy", polRefs.iterator().next().getPolicyURI());
        
        Collection<BindingOperation> bindingops = b.getBindingOperations();
        for (BindingOperation bo : bindingops) {
            all = pmh.createPolicy(bo.getBindingInput(), false);
            assertNotNull("Binding Input Policy", all);
            
            all = pmh.createPolicy(bo.getBindingOutput(), false);
            assertNotNull("Binding Output Policy", all);
        }
        
    }

    public String getTestResourcePath() {
        return "../wsdlmodelext/resources/policy.xml";
    }
    
}
