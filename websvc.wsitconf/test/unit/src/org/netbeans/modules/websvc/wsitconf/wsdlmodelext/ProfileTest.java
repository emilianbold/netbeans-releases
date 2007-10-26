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
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.websvc.wsitconf.ui.ComboConstants;
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
public class ProfileTest extends NbTestCase {
    
    public ProfileTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
    }

    @Override
    protected void tearDown() throws Exception {
        TestCatalogModel.getDefault().setDocumentPooling(false);
    }

    public void testWrite() throws Exception {
        TestCatalogModel.getDefault().setDocumentPooling(true);
        WSDLModel model = TestUtil.loadWSDLModel("../wsdlmodelext/resources/policy.xml");
        WSDLComponentFactory fact = model.getFactory();
        
        Definitions d = model.getDefinitions();
        Binding b = (Binding) d.getBindings().toArray()[0];

        String[] profiles = new String[] {
            "",
            ComboConstants.PROF_TRANSPORT,
            ComboConstants.PROF_MSGAUTHSSL,
            ComboConstants.PROF_SAMLSSL,
            ComboConstants.PROF_USERNAME,
            ComboConstants.PROF_MUTUALCERT,
            ComboConstants.PROF_ENDORSCERT,
            ComboConstants.PROF_SAMLSENDER,
            ComboConstants.PROF_SAMLHOLDER,
            ComboConstants.PROF_KERBEROS,
            ComboConstants.PROF_STSISSUED,
            ComboConstants.PROF_STSISSUEDCERT,
            ComboConstants.PROF_STSISSUEDENDORSE
        };
        
        for (int i=1; i<profiles.length; i++) {
            String profile = profiles[i];

            //default profile set
            ProfilesModelHelper.setSecurityProfile(b, profile);

            File profDefaultFile = new File(getWorkDirPath() + File.separator + i + profile + ".wsdl");
            TestUtil.dumpToFile(model.getBaseDocument(), profDefaultFile);
//            assertFile(profDefaultFile, TestUtil.getGoldenFile(getDataDir(), "Profile"+ i + "Test", "testDefault"));

            ProfilesModelHelper.enableSecureConversation(b, true);     // enable SC

            File profSCFile = new File(getWorkDirPath() + File.separator + i + profile + "-SecureConversation.wsdl");
            TestUtil.dumpToFile(model.getBaseDocument(), profSCFile);
//            assertFile(profSCFile, TestUtil.getGoldenFile(getDataDir(), "Profile"+ i + "Test", "testSecureConversation"));            

            ProfilesModelHelper.enableSecureConversation(b, false);     // disable SC

            File profAfterSCFile = new File(getWorkDirPath() + File.separator + i + profile + "-After.wsdl");
            TestUtil.dumpToFile(model.getBaseDocument(), profAfterSCFile);
//            assertFile(profAfterSCFile, TestUtil.getGoldenFile(getDataDir(), "Profile"+ i + "Test", "testDefault"));
            
            readAndCheck(model, profile);
        }

    }

    private void readAndCheck(WSDLModel model, String profile) {
        
        // the model operation is not enclosed in transaction inorder to catch 
        // whether the operations do not try to create non-existing elements        
        
    }

    public String getTestResourcePath() {
        return "../wsdlmodelext/resources/policy.xml";
    }
    
}
