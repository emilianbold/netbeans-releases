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


import org.netbeans.modules.performance.guitracker.LoggingRepaintManager.RegionFilter;
import org.netbeans.modules.performance.guitracker.ActionTracker;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
//import org.netbeans.modules.editor.options.BaseOptions;
//import org.netbeans.modules.java.editor.options.JavaOptions;

import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.EditorWindowOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.modules.editor.CompletionJListOperator;
import org.netbeans.jellytools.nodes.SourcePackagesNode;

import org.netbeans.jemmy.operators.ComponentOperator;

/**
 * Test of java completion in opened source editor.
 *
 * @author  anebuzelsky@netbeans.org, mmirilovic@netbeans.org
 */
public class JavaCompletionInEditor extends PerformanceTestCase {
    
    private static final int lineNumber = 39;
    private static final String ccText = "        System";
    
    private EditorOperator editorOperator;
    private int completionAutoPopupDelay, caretBlinkRate;
    private boolean javaDocAutoPopup;
    
    public static final String suiteName="UI Responsiveness J2SE Actions";    
    
    /** Creates a new instance of JavaCompletionInEditor */
    public JavaCompletionInEditor(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }
    
    /** Creates a new instance of JavaCompletionInEditor */
    public JavaCompletionInEditor(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
    }
    
    public void testJavaCompletionInEditor(){
        doMeasurement();
    }
    
    @Override
    public void initialize() {
        // prepare editor/completion for measuring
        setCompletionForMeasuringOn();
        
        // open a java file in the editor
        new OpenAction().performAPI(new Node(new SourcePackagesNode("PerformanceTestData"), "org.netbeans.test.performance|Main.java"));
        editorOperator = EditorWindowOperator.getEditor("Main.java");
        
        // scroll to the place where we start
        editorOperator.setCaretPositionToLine(lineNumber);
        
        // insert the initial text
        editorOperator.insert(ccText);
        
        // track component show (no paints from Code Completion)
        MY_END_EVENT = ActionTracker.TRACK_COMPONENT_SHOW;
    }
    
    public void prepare() {
        // scroll to the place where we start
        EditorWindowOperator.getEditor("Main.java");
        editorOperator.setCaretPositionToEndOfLine(lineNumber);
        
        // wait
        waitNoEvent(1000);
   }
    
    public ComponentOperator open(){
        // invoke the completion dialog
        editorOperator.typeKey('.');
        
        // wait for the completion window
        return new CompletionJListOperator();
    }
    
    @Override
    public void close() {
        super.close();
        editorOperator.setCaretPositionRelative(-1);
        editorOperator.delete(1);
    }
    
    @Override
    public void shutdown() {
        // set default values after measuring
        setCompletionForMeasuringOff();
        
        editorOperator.closeDiscard();
        super.shutdown();
    }
    
    
    private void setCompletionForMeasuringOn(){
        // measure only paint events from QuietEditorPane
        repaintManager().addRegionFilter(COMPLETION_FILTER);
/*        
        // set large font size for Editor
        BaseOptions options = BaseOptions.getOptions (JavaKit.class);
        if (options instanceof JavaOptions) {
            completionAutoPopupDelay = ((JavaOptions)options).getCompletionAutoPopupDelay();
            ((JavaOptions)options).setCompletionAutoPopupDelay(0);
            javaDocAutoPopup = ((JavaOptions)options).getJavaDocAutoPopup();
            ((JavaOptions)options).setJavaDocAutoPopup(false);
        }
        
        caretBlinkRate = options.getCaretBlinkRate();
        //disable caret blinkering
        options.setCaretBlinkRate(0);
    */    
        // turn off the error hightlighting feature
        /* TODO doesn't work after retouche integration
        parsingErrors = JavaSettings.getDefault().getParsingErrors();
        JavaSettings.getDefault().setParsingErrors(0);        
         */
    }
    
    private void setCompletionForMeasuringOff(){
        // reset filter
        repaintManager().resetRegionFilters();
        
        // set back the original font size for Editor
/*        Class kitClass = JavaKit.class;
        BaseOptions options = BaseOptions.getOptions (kitClass);
        if (options instanceof JavaOptions) {
            ((JavaOptions)options).setCompletionAutoPopupDelay(completionAutoPopupDelay);
            ((JavaOptions)options).setJavaDocAutoPopup(javaDocAutoPopup);
        }
        
        // set back the original blink rate
        options.setCaretBlinkRate(caretBlinkRate);
  */      
        /* doesn't work after retouche integration
        JavaSettings.getDefault().setParsingErrors(parsingErrors);        
         */
    }
    
    private static final RegionFilter COMPLETION_FILTER =
            new RegionFilter() {

                public boolean accept(javax.swing.JComponent c) {
                    return c.getClass().getName().equals("org.netbeans.modules.editor.completion.CompletionScrollPane") ||
                           c.getClass().getName().equals("org.openide.text.QuietEditorPane");
                }

                public String getFilterName() {
                    return "Accept paints from org.netbeans.modules.editor.completion.CompletionScrollPane || org.openide.text.QuietEditorPane";
                }
            };

    public static void main(java.lang.String[] args) {
        repeat =3;
        junit.textui.TestRunner.run(new JavaCompletionInEditor("measureTime"));
    }
    
}