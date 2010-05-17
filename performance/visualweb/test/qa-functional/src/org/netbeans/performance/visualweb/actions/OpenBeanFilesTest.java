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

package org.netbeans.performance.visualweb.actions;

import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.modules.performance.guitracker.ActionTracker;
import org.netbeans.performance.visualweb.setup.VisualWebSetup;

import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbModuleSuite;

import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 *
 * @author mkhramov@netbeans.org, mmirilovic@netbeans.org
 */
public class OpenBeanFilesTest  extends PerformanceTestCase {
    
    private String beanName;
    private String beanFileName;
    private String beanPath;
    public static Node openNode ;
    
    protected static String OPEN = org.netbeans.jellytools.Bundle.getStringTrimmed("org.openide.actions.Bundle", "Open");

    /** Creates a new instance of OpenBeanFiles */
    public OpenBeanFilesTest(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN=2000;
    }
    
    public OpenBeanFilesTest(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN=2000;
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(VisualWebSetup.class)
             .addTest(OpenBeanFilesTest.class)
             .enableModules(".*").clusters(".*")));
        return suite;
    }

        class PhaseHandler extends Handler {

            public boolean published = false;

            public void publish(LogRecord record) {

            if (record.getMessage().equals("Open Editor, phase 1, AWT [ms]"))
               ActionTracker.getInstance().stopRecording();
            }

            public void flush() {
            }

            public void close() throws SecurityException {
            }
        }

    PhaseHandler phaseHandler=new PhaseHandler();


    public void testApplicationBean() {
        beanName = "ApplicationBean1.java"; //NOI18N
        beanFileName = "ApplicationBean1.java";
        doMeasurement();
    }
    
    public void testRequestBean() {
        beanName = "RequestBean1.java"; //NOI18N
        beanFileName = "RequestBean1.java";
        doMeasurement();
    }
    
    public void testSessionBean() {
        beanName = "SessionBean1.java"; //NOI18N
        beanFileName = "SessionBean1.java";
        doMeasurement();
    }
    
    public void initialize() {
        EditorOperator.closeDiscardAll();
        repaintManager().addRegionFilter(repaintManager().EDITOR_FILTER);
    }
    
    public void prepare() {
        Logger.getLogger("TIMER").setLevel(Level.FINE);
        Logger.getLogger("TIMER").addHandler(phaseHandler);
        beanPath = "Source Packages"+"|"+"visualwebproject"+"|"+beanName;
        Node projectRoot = null;
        try {
            projectRoot = new ProjectsTabOperator().getProjectRootNode("UltraLargeWA");
            projectRoot.select();
            
        } catch (org.netbeans.jemmy.TimeoutExpiredException ex) {
            fail("Cannot find and select project root node");
        }

        try {
            this.openNode = new Node(projectRoot, beanPath);
        } catch (org.netbeans.jemmy.TimeoutExpiredException ex) {
            fail("Cannot find and select bean node"); 
        }
        
        if (this.openNode == null) {
            throw new Error("Cannot find node "+beanPath);
        }
    }
    
    public ComponentOperator open() {
        JPopupMenuOperator popup =  this.openNode.callPopup();
        if (popup == null) {
            throw new Error("Cannot get context menu for node "+beanPath);
        }
        try {
            popup.pushMenu(OPEN);
        } catch (org.netbeans.jemmy.TimeoutExpiredException tee) {
            throw new Error("Cannot push menu item Open on node "+beanPath);
        }
        return new EditorOperator(beanFileName);
    }
    
    public void close() {
        if(testedComponentOperator != null)
        {
            ((EditorOperator)testedComponentOperator).close();
            testedComponentOperator = null;
        }
    }
    
    protected void shutdown(){
        Logger.getLogger("TIMER").removeHandler(phaseHandler);
        EditorOperator.closeDiscardAll();
        repaintManager().resetRegionFilters();
    }
    
}
