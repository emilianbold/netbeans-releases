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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.performance.visualweb.memory;

//import org.netbeans.performance.visualweb.dialogs.WebFormDesignerOperator;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.CloseAllDocumentsAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.junit.NbTestSuite;
/**
 *
 * @author mkhramov@@netbeans.org
 */
public class BigPageCyclicOpen extends org.netbeans.modules.performance.utilities.MemoryFootprintTestCase {
    
    private Node pagesRoot = null;
    private long oldTimeout;
    
    public BigPageCyclicOpen(String testName) {
        super(testName);
        repeat_memory = 1;
    }
    public BigPageCyclicOpen(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        repeat_memory = 1;        
    }
    @Override
    public void initialize() {
      
//        ProjectSupport.openProject(System.getProperty("xtest.tmpdir")+ java.io.File.separatorChar +"UltraLargeWA");
    }
    
    @Override
    public void prepare() {
        EditorOperator.closeDiscardAll();
        ProjectsTabOperator.invoke();
        Node projectRoot = new ProjectsTabOperator().getProjectRootNode("UltraLargeWA");
        pagesRoot = new Node(projectRoot, "Web Pages");
        
        oldTimeout = JemmyProperties.getCurrentTimeouts().getTimeout("ComponentOperator.WaitComponentTimeout");
        JemmyProperties.getCurrentTimeouts().setTimeout("ComponentOperator.WaitComponentTimeout",120000);

    }

    @Override
    public ComponentOperator open() {
        log("Begin cyclic page open test");

        
        for(int bigloop=0; bigloop<3; bigloop++)
        {
            log(bigloop+" pass");
            for(int innerloop=0;innerloop<100;innerloop++)
            {
                doPageOpenCloseAttempt(innerloop);
            }
            log(bigloop+" pass completed");
        }
        return null;
    }
    private void doPageOpenCloseAttempt(int attempt) {
        String openPage = "Page1_"+(attempt+1);
        log("Opening "+openPage+".jsp ...");
        System.out.println(" Opening Page1_"+(attempt+1));
        
        Node PageNode = new Node(pagesRoot, openPage+".jsp");
        PageNode.select();
        long timestart = System.currentTimeMillis();
        PageNode.performPopupActionNoBlock("Open");
        try {
//            WebFormDesignerOperator.findWebFormDesignerOperator(openPage);
        } catch(TimeoutExpiredException tex) {
            log("timeout for Opening page expired");
        }
        long timestop = System.currentTimeMillis();
        System.out.println("Page opened in: "+(timestop-timestart)+" ms");
        new CloseAllDocumentsAction().performAPI();
    }
    @Override
    public void close() {
        JemmyProperties.getCurrentTimeouts().setTimeout("ComponentOperator.WaitComponentTimeout",oldTimeout);        
        new CloseAllDocumentsAction().performAPI();                
    }
    @Override
    public void shutdown() {
//        ProjectSupport.closeProject("UltraLargeWA");
        
    }
    public void testMem() {
        doMeasurement();
    }
    /** Creates suite from particular test cases. You can define order of testcases here. */
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new BigPageCyclicOpen("testMem","Memory footprint test"));
        return suite;
    } 
    /* Method allowing test execution directly from the IDE. */
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());

    }    
   
}
