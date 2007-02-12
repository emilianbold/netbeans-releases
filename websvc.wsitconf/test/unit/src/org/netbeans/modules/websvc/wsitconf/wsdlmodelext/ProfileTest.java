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

            ProfilesModelHelper.enableSecureConversation(b, true, profile);     // enable SC

            File profSCFile = new File(getWorkDirPath() + File.separator + i + profile + "-SecureConversation.wsdl");
            TestUtil.dumpToFile(model.getBaseDocument(), profSCFile);
//            assertFile(profSCFile, TestUtil.getGoldenFile(getDataDir(), "Profile"+ i + "Test", "testSecureConversation"));            

            ProfilesModelHelper.enableSecureConversation(b, false, profile);     // disable SC

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
