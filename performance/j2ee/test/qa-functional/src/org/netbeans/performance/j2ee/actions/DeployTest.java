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

package org.netbeans.performance.j2ee.actions;

import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.modules.performance.utilities.CommonUtilities;
import org.netbeans.performance.j2ee.setup.J2EESetup;

import java.io.InputStream;
import java.net.URL;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.RuntimeTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbModuleSuite;


/**
 * Test create projects
 *
 * @author  lmartinek@netbeans.org
 */
public class DeployTest extends PerformanceTestCase {
    
    private Node node;

  
    /**
     * Creates a new instance of CreateJ2EEProject
     * @param testName the name of the test
     */
    public DeployTest(String testName) {
        super(testName);
        expectedTime = 60000;
        WAIT_AFTER_OPEN=120000;
    }
    
    /**
     * Creates a new instance of CreateJ2EEProject
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public DeployTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = 60000;
        WAIT_AFTER_OPEN=120000;
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(J2EESetup.class)
             .addTest(DeployTest.class)
             .enableModules(".*").clusters(".*")));
        return suite;
    }

    public void testDeploy() {
        doMeasurement();
    }
    
    @Override
    public void initialize(){
        CommonUtilities.startApplicationServer();
        
        JTreeOperator tree = ProjectsTabOperator.invoke().tree();
        tree.setComparator(new Operator.DefaultStringComparator(true, true));
        node = new ProjectRootNode(tree, "DeployTest");
        node.performPopupAction("Build");
        MainWindowOperator.getDefault().getTimeouts().setTimeout("Waiter.WaitingTime", 60000);
        MainWindowOperator.getDefault().waitStatusText("Finished building build.xml (dist)");
    }
    
    @Override
    public void shutdown() {
        RuntimeTabOperator runtimeTab = RuntimeTabOperator.invoke();
        node = new Node(runtimeTab.getRootNode(), Bundle.getStringTrimmed("org.netbeans.modules.j2ee.deployment.impl.ui.Bundle", "SERVER_REGISTRY_NODE")
                +"|Glassfish V2|"
                + Bundle.getStringTrimmed("org.netbeans.modules.j2ee.sun.ide.j2ee.runtime.nodes.Bundle", "LBL_Applications") + "|"
                + Bundle.getStringTrimmed("org.netbeans.modules.j2ee.sun.ide.j2ee.runtime.nodes.Bundle", "LBL_AppModules") + "|"
                + "DeployTest");
        node.performPopupAction(Bundle.getStringTrimmed("org.netbeans.modules.j2ee.sun.ide.j2ee.runtime.nodes.Bundle", "LBL_Undeploy"));
        //node.waitNotPresent();
        
        CommonUtilities.stopApplicationServer();
    }
    
    public void prepare(){
        
    }
    
    public ComponentOperator open(){
        node.performPopupAction("Deploy");
        return null;
    }
    
    @Override
    public void close() {
        MainWindowOperator.getDefault().waitStatusText("Finished building build.xml (run-deploy).");
        try {
            URL url = new URL("http://localhost:8080/DeployTest-WebModule/TestServlet");
            InputStream stream = url.openStream();
            stream.close();
        } catch (Exception e) {
            throw new RuntimeException("Deployed application unavailable.",e);
        }
    }
    
    
}
