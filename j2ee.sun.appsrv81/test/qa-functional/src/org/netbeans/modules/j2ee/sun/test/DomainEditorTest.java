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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.sun.test;

import java.io.File;
import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.j2ee.deployment.impl.ServerInstance;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;
import org.netbeans.modules.j2ee.sun.ide.j2ee.DomainEditor;
import org.netbeans.modules.j2ee.sun.ide.j2ee.HttpProxyUpdater;
import org.netbeans.modules.j2ee.sun.ide.j2ee.ui.AddDomainWizardIterator;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.w3c.dom.Document;

/**
 *
 * @author Michal Mocnak
 */
public class DomainEditorTest extends NbTestCase {
    
    private final int SLEEP = 10000;
    
    public DomainEditorTest(String testName) {
        super(testName);
    }
    
    public void poundOnEditor() {
        ServerInstance inst = ServerRegistry.getInstance().getServerInstance(Util._URL);
        DeploymentManager dm = inst.getDeploymentManager();
        DomainEditor de = new DomainEditor(dm);
        
        String loc = de.getDomainLocation();
        Document doc = de.getDomainDocument();
        Document doc2 = de.getDomainDocument(loc);
        de.addProfilerElements(doc,"/foo/bar/bas", new String[] { "aaaaaaaaaa", "bbbbbbbbbb", "ccccccccccc"});
        de.removeProfilerElements(doc);
        String oldProxyValues[] = de.getHttpProxyOptions();
        String foo[] = new String[] { 
            HttpProxyUpdater.HTTPS_PROXY_HOST+"yyyyyyyyy",
            HttpProxyUpdater.HTTP_PROXY_HOST+"zzzzzzzzz" };
        de.setHttpProxyOptions(foo);
        String tmp[] = de.getHttpProxyOptions();
        de.setHttpProxyOptions(oldProxyValues);
        if (tmp == null || tmp.length != 2) {
            fail("not right length");
        }
        if (!foo[0].equals(tmp[0]) && !foo[0].equals(tmp[1])) {
            fail(foo[0]);
        }
        if (!foo[1].equals(tmp[0]) && !foo[1].equals(tmp[1])) {
            fail(foo[1]);
        }
        de.getSunDatasourcesFromXml();
        de.getConnPoolsFromXml();        
        
    }
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite("DomainEditorTest");
        suite.addTest(new AddRemoveSjsasInstanceTest("addSjsasInstance"));  
        suite.addTest(new DomainEditorTest("poundOnEditor"));
        suite.addTest(new AddRemoveSjsasInstanceTest("removeSjsasInstance"));        
        return suite;
    }
}