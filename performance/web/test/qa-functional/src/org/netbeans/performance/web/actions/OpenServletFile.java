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

package org.netbeans.performance.web.actions;

import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.modules.performance.guitracker.ActionTracker;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.logging.LogRecord;


/**
 * Test of opening files.
 *
 * @author  mmirilovic@netbeans.org
 */
public class OpenServletFile extends PerformanceTestCase {
    
    /** Node to be opened/edited */
    public static Node openNode ;
    
    /** Folder with data */
    public static String fileProject;
    
    /** Folder with data  */
    public static String filePackage;
    
    /** Name of file to open */
    public static String fileName;
    
    /** Menu item name that opens the editor */
    public static String menuItem;
    
    protected static String OPEN = "Open"; //NOI18N
    
    protected static String EDIT = "Edit"; //NOI18N
    
    public static final String suiteName="UI Responsiveness Web Actions suite";    
    
    
    /**
     * Creates a new instance of OpenFiles
     * @param testName the name of the test
     */
    public OpenServletFile(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }
    
    /**
     * Creates a new instance of OpenFiles
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public OpenServletFile(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
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
    
    public void testOpeningServletFile(){
        WAIT_AFTER_OPEN = 1000;
       //repaintManager().setOnlyEditor(true);
//        repaintManager().addRegionFilter(repaintManager().EDITOR_FILTER);
        setJavaEditorCaretFilteringOn();
        fileProject = "TestWebProject";
        filePackage = "test";
        fileName = "TestServlet.java";
        menuItem = OPEN;
        doMeasurement();
    }
    
    public void testOpeningJavaFile(){
        WAIT_AFTER_OPEN = 1000;
        //repaintManager().setOnlyEditor(true);
        fileProject = "TestWebProject";
        filePackage = "test";
        fileName = "Main.java";
        menuItem = OPEN;
        doMeasurement();
    }
    
    public void initialize(){
        EditorOperator.closeDiscardAll();
    }

    public void shutdown(){
        Logger.getLogger("TIMER").removeHandler(phaseHandler);
        EditorOperator.closeDiscardAll();
    }
    
    public void prepare(){
        Logger.getLogger("TIMER").setLevel(Level.FINE);
        Logger.getLogger("TIMER").addHandler(phaseHandler);
        this.openNode = new Node(new ProjectsTabOperator().getProjectRootNode(fileProject),"Source Packages" + '|' +  filePackage + '|' + fileName);
        
        if (this.openNode == null) {
            fail ("Cannot find node ["+"Source Packages" + '|' +  filePackage + '|' + fileName + "] in project [" + fileProject + "]");
        }
        log("========== Open file path ="+this.openNode.getPath());
    }
    
    public ComponentOperator open(){
        JPopupMenuOperator popup =  this.openNode.callPopup();
        if (popup == null) {
            fail ("Cannot get context menu for node ["+"Source Packages" + '|' +  filePackage + '|' + fileName + "] in project [" + fileProject + "]");
        }
        log("------------------------- after popup invocation ------------");
        try {
        repaintManager().addRegionFilter(repaintManager().EDITOR_FILTER);
        setJavaEditorCaretFilteringOn();
            popup.pushMenu(this.menuItem);
        }
        catch (org.netbeans.jemmy.TimeoutExpiredException tee) {
            fail ("Cannot push menu item "+this.menuItem+" of node ["+"Source Packages" + '|' +  filePackage + '|' + fileName + "] in project [" + fileProject + "]");
        }
        log("------------------------- after open ------------");
        return new EditorOperator(this.fileName);
    }
    
    public void close(){
        //repaintManager().setOnlyEditor(false);
        repaintManager().resetRegionFilters(); // added - was missing
        if (testedComponentOperator != null) {
            ((EditorOperator)testedComponentOperator).closeDiscard();
        }
        else {
            fail ("no component to close");
        }
    }
    
}
