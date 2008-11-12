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

import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;

import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 *
 * @author mkhramov@netbeans.org, mmirilovic@netbeans.org
 */
public class OpenBeanFiles  extends PerformanceTestCase {
    
    private String beanName;
    private String beanFileName;
    private String beanPath;
    /** Node to be opened/edited */
    public static Node openNode ;
    
    protected static String OPEN = org.netbeans.jellytools.Bundle.getStringTrimmed("org.openide.actions.Bundle", "Open");
    public static final String suiteName="UI Responsiveness VisualWeb Actions suite";
    /** Creates a new instance of OpenBeanFiles */
    public OpenBeanFiles(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN=2000;
        WAIT_AFTER_PREPARE=3000;
    }
    
    public OpenBeanFiles(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN=2000;
        WAIT_AFTER_PREPARE=3000;        
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
        setJavaEditorCaretFilteringOn();
        doMeasurement();
    }
    
    public void testRequestBean() {
        beanName = "RequestBean1.java"; //NOI18N
        beanFileName = "RequestBean1.java";
        setJavaEditorCaretFilteringOn();
        doMeasurement();
    }
    
    public void testSessionBean() {
        beanName = "SessionBean1.java"; //NOI18N
        beanFileName = "SessionBean1.java";
        setJavaEditorCaretFilteringOn();
        doMeasurement();
    }
    
    public void initialize() {
        log("::initialize");
        EditorOperator.closeDiscardAll();
        repaintManager().addRegionFilter(repaintManager().EDITOR_FILTER);
    }
    
    public void prepare() {
        log("::prepare");
        Logger.getLogger("TIMER").setLevel(Level.FINE);
        Logger.getLogger("TIMER").addHandler(phaseHandler);
        beanPath = "Source Packages"+"|"+"visualwebproject"+"|"+beanName;
        log("Bean Path = "+beanPath);
        Node projectRoot = null;
        try {
            projectRoot = new ProjectsTabOperator().getProjectRootNode("VisualWebProject");
            projectRoot.select();
            
        } catch (org.netbeans.jemmy.TimeoutExpiredException ex) {
            fail("Cannot find and select project root node");
        }

        try {
            this.openNode = new Node(projectRoot, beanPath);
            this.openNode.select();
            
        } catch (org.netbeans.jemmy.TimeoutExpiredException ex) {
            fail("Cannot find and select bean node"); 
        }
        
        if (this.openNode == null) {
            throw new Error("Cannot find node "+beanPath);
        }
        log("========== Open file path ="+this.openNode.getPath());
    }
    
    public ComponentOperator open() {
        JPopupMenuOperator popup =  this.openNode.callPopup();
        if (popup == null) {
            throw new Error("Cannot get context menu for node "+beanPath);
        }
        log("------------------------- after popup invocation ------------");
        try {
            popup.pushMenu(OPEN);
        } catch (org.netbeans.jemmy.TimeoutExpiredException tee) {
            throw new Error("Cannot push menu item Open on node "+beanPath);
        }
        log("------------------------- after open ------------");
        
        return new EditorOperator(beanFileName);
    }
    
    public void close() {
        log(":: close");
        if(testedComponentOperator != null)
        {
            ((EditorOperator)testedComponentOperator).close();
            testedComponentOperator = null;
        }
        
    }
    
    protected void shutdown(){
        log(":: shutdown");
        Logger.getLogger("TIMER").removeHandler(phaseHandler);
        EditorOperator.closeDiscardAll();
        repaintManager().resetRegionFilters();
    }
    
}
