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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package gui.action;

import org.netbeans.performance.test.guitracker.LoggingRepaintManager.RegionFilter;
import org.netbeans.modules.editor.java.JavaKit;
import org.netbeans.modules.editor.options.BaseOptions;
import org.netbeans.modules.java.editor.options.JavaOptions;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.EditorWindowOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.modules.editor.CompletionJListOperator;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.performance.test.guitracker.ActionTracker;

/**
 * Test of java completion in opened source editor.
 *
 * @author  anebuzelsky@netbeans.org, mmirilovic@netbeans.org
 */
public class JavaCompletionInEditor extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    private static final int lineNumber = 39;
    private static final String ccText = "        System";
    
    private EditorOperator editorOperator;
    private int completionAutoPopupDelay, caretBlinkRate;
    private boolean javaDocAutoPopup;
    
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
    
    public void initialize() {
        MY_END_EVENT = ActionTracker.TRACK_COMPONENT_SHOW;
        // prepare editor/completion for measuring
        setCompletionForMeasuringOn();
        
        // open a java file in the editor
        new OpenAction().performAPI(new Node(new ProjectsTabOperator().getProjectRootNode("PerformanceTestData"), gui.Utilities.SOURCE_PACKAGES + "|org.netbeans.test.performance|Main.java"));
        editorOperator = EditorWindowOperator.getEditor("Main.java");
        
        // scroll to the place where we start
        editorOperator.setCaretPositionToLine(lineNumber);
        
        // insert the initial text
        editorOperator.insert(ccText);
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
        editorOperator.pushKey('.');
        // wait for the completion window
        return new CompletionJListOperator();
    }
    
    public void close() {
        super.close();
        editorOperator.setCaretPositionRelative(-1);
        editorOperator.delete(1);
    }
    
    public void shutdown() {
        // set default values after measuring
        setCompletionForMeasuringOff();
        
        editorOperator.closeDiscard();
        super.shutdown();
    }
    
    
    private void setCompletionForMeasuringOn(){
        // measure only paint events from QuietEditorPane
        repaintManager().setRegionFilter(COMPLETION_FILTER);
        
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
        
        // turn off the error hightlighting feature
        /* TODO doesn't work after retouche integration
        parsingErrors = JavaSettings.getDefault().getParsingErrors();
        JavaSettings.getDefault().setParsingErrors(0);        
         */
    }
    
    private void setCompletionForMeasuringOff(){
        // reset filter
        repaintManager().setRegionFilter(null);
        
        // set back the original font size for Editor
        Class kitClass = JavaKit.class;
        BaseOptions options = BaseOptions.getOptions (kitClass);
        if (options instanceof JavaOptions) {
            ((JavaOptions)options).setCompletionAutoPopupDelay(completionAutoPopupDelay);
            ((JavaOptions)options).setJavaDocAutoPopup(javaDocAutoPopup);
        }
        
        // set back the original blink rate
        options.setCaretBlinkRate(caretBlinkRate);
        
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
        System.setProperty("org.netbeans.performance.repeat", "3");
        junit.textui.TestRunner.run(new JavaCompletionInEditor("measureTime"));
    }
    
}