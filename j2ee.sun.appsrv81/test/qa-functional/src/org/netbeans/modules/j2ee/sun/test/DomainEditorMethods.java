/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
public class DomainEditorMethods extends NbTestCase {
    
    private final int SLEEP = 10000;
    
    public DomainEditorMethods(String testName) {
        super(testName);
    }
    
    public void poundOnEditor() {
        ServerInstance inst = ServerRegistry.getInstance().getServerInstance(Util._URL);
        DeploymentManager dm = inst.getDeploymentManager();
        DomainEditor de = new DomainEditor(dm);
        
        String loc = de.getDomainLocation();
        Document doc2 = de.getDomainDocument(loc);
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

    public void checkProfilerInsertion() {
        ServerInstance inst = ServerRegistry.getInstance().getServerInstance(Util._URL);
        DeploymentManager dm = inst.getDeploymentManager();
        DomainEditor de = new DomainEditor(dm);
        
        String loc = de.getDomainLocation();
        Document doc = de.getDomainDocument();
        char quote = '"';
        String value = "-agentpath:" + quote + "C:\\Program Files\\NetBeans 6.0\\profiler2\\lib\\deployed\\jdk16\\windows\\profilerinterface.dll=\\" + quote + "C:\\Program Files\\NetBeans 6.0\\profiler2\\lib\\" + quote + ",5140";
        boolean added = de.addProfilerElements(doc,"/foo/bar/bas", new String[] { "aaaaaaaaaa", "bbbbbbbbbb", value});
        if (! added) {
            this.fail("Could not add profiler elements");
        }
        de.removeProfilerElements(doc);        
    }

//    public static NbTestSuite suite() {
//        NbTestSuite suite = new NbTestSuite("DomainEditorMethods");
//        suite.addTest(new AddRemoveSjsasInstanceTest("addSjsasInstance"));  
//        suite.addTest(new DomainEditorMethods("poundOnEditor"));
//        suite.addTest(new DomainEditorMethods("checkProfilerInsertion"));
//        suite.addTest(new AddRemoveSjsasInstanceTest("removeSjsasInstance"));        
//        return suite;
//    }
}
