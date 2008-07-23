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

package org.netbeans.performance.j2se.actions;

import java.awt.Component;
import java.awt.Container;
import javax.swing.JEditorPane;
import javax.swing.text.Document;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.modules.performance.guitracker.ActionTracker;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.LogRecord;

/**
 * Test of opening files.
 *
 * @author  mmirilovic@netbeans.org
 */
public class OpenFiles extends PerformanceTestCase {
    
    private static final Object EDITOR_REFS = new Object();
    
    public static final String suiteName="UI Responsiveness J2SE Actions";
    
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
    
    protected static String OPEN = org.netbeans.jellytools.Bundle.getStringTrimmed("org.openide.actions.Bundle", "Open");
    
    protected static String EDIT = org.netbeans.jellytools.Bundle.getStringTrimmed("org.openide.actions.Bundle", "Edit");
    
    /**
     * Creates a new instance of OpenFiles
     * @param testName the name of the test
     */
    public OpenFiles(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }
    
    /**
     * Creates a new instance of OpenFiles
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public OpenFiles(String testName, String performanceDataName) {
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

    
    public void testOpening20kBJavaFile(){
        WAIT_AFTER_OPEN = 1000;
        setJavaEditorCaretFilteringOn();
        fileProject = "PerformanceTestData";
        filePackage = "org.netbeans.test.performance";
        fileName = "Main20kB.java";
        menuItem = OPEN;
        doMeasurement();
    }
    
    public void testOpening20kBTxtFile(){
        WAIT_AFTER_OPEN = 1000;
        setPlainTextEditorCaretFilteringOn();
        fileProject = "PerformanceTestData";
        filePackage = "org.netbeans.test.performance";
        fileName = "textfile20kB.txt";
        menuItem = OPEN;
        doMeasurement();
    }
    
    public void testOpening20kBXmlFile(){
        WAIT_AFTER_OPEN = 1000;
        setXMLEditorCaretFilteringOn();
        fileProject = "PerformanceTestData";
        filePackage = "org.netbeans.test.performance";
        fileName = "xmlfile20kB.xml";
        menuItem = EDIT;
        doMeasurement();
    }
    
    @Override
    protected void initialize(){
        EditorOperator.closeDiscardAll();
//        repaintManager().addRegionFilter(repaintManager().EDITOR_FILTER);
    }
    
    public void prepare(){
        Logger.getLogger("TIMER").addHandler(phaseHandler);
        this.openNode = new Node(new SourcePackagesNode(fileProject), filePackage + '|' + fileName);
        log("========== Open file path ="+this.openNode.getPath());
    }
    
    public ComponentOperator open(){
        JPopupMenuOperator popup =  this.openNode.callPopup();
        if (popup == null) {
            throw new Error("Cannot get context menu for node ["+ openNode.getPath() + "] in project [" + fileProject + "]");
        }
        log("------------------------- after popup invocation ------------");
        
        try {
            repaintManager().addRegionFilter(repaintManager().EDITOR_FILTER);
            popup.pushMenu(this.menuItem);
        } catch (org.netbeans.jemmy.TimeoutExpiredException tee) {
            tee.printStackTrace(getLog());
            throw new Error("Cannot push menu item ["+this.menuItem+"] of node [" + openNode.getPath() + "] in project [" + fileProject + "]");
        }
        log("------------------------- after open ------------");
        return new EditorOperator(this.fileName);
    }
    
    @Override
    public void close(){
        if (testedComponentOperator != null) {
            hookEditorDocument(testedComponentOperator.getSource());
            ((EditorOperator)testedComponentOperator).closeDiscard();
        } else {
            throw new Error("no component to close");
        }
    }
    
    @Override
    protected void shutdown(){
        Logger.getLogger("TIMER").removeHandler(phaseHandler);
        testedComponentOperator = null; // allow GC of editor and documents
        EditorOperator.closeDiscardAll();
        repaintManager().resetRegionFilters();
    }
    
    private void hookEditorDocument(Component comp) {
        if (comp instanceof Container) {
            Container cont = (Container)comp;
            for (Component c: cont.getComponents()) {
                hookEditorDocument(c);
            }
            if ("org.openide.text.QuietEditorPane".equals(comp.getClass().getName())) {
                JEditorPane pane = (JEditorPane)comp;
                Document doc = pane.getDocument();
                if (doc != null)
                    reportReference("Editor document from test "+getName(), doc, EDITOR_REFS);
            }
        }
    }
    
    /** Tests if created and later dclosed projects can be GCed from memory.
     */
/*    public void testGC() throws Exception {
        Thread.sleep(60*1000);
        runTestGC(EDITOR_REFS);
    }
  */  
}
