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

package gui.action;

import org.netbeans.modules.editor.options.BaseOptions;

import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.EditorWindowOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jellytools.modules.web.nodes.WebPagesNode;

import org.netbeans.jemmy.operators.ComponentOperator;

import org.netbeans.junit.NbTestSuite;

/**
 * Test of typing in opened source editor.
 *
 * @author  anebuzelsky@netbeans.org, mmirilovic@netbeans.org
 */
public class TypingInEditor extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    private EditorOperator editorOperator;
    
    private int fontSize, caretBlinkRate;
    protected String fileName;
    protected int caretPositionX, caretPositionY;
    
    protected Class kitClass,optionsClass;
    
    Node fileToBeOpened;
    
    /** Creates a new instance of TypingInEditor */
    public TypingInEditor(String testName) {
        super(testName);
        WAIT_AFTER_PREPARE = 3000;
        WAIT_AFTER_OPEN = 100;

        HEURISTIC_FACTOR = -1; // use default WaitAfterOpen for all iterations
    }
    
    /** Creates a new instance of TypingInEditor */
    public TypingInEditor(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        WAIT_AFTER_PREPARE = 3000;
        WAIT_AFTER_OPEN = 100;

        HEURISTIC_FACTOR = -1; // use default WaitAfterOpen for all iterations
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new TypingInEditor("testJavaEditor", "Type a character in Java Editor"));
        suite.addTest(new TypingInEditor("testTxtEditor", "Type a character in Txt Editor"));
//        suite.addTest(new TypingInEditor("testJspEditor", "Type a character in Jsp Editor"));
        return suite;
    }
    
    public void testTxtEditor() {
        fileName = "textfile.txt";
        caretPositionX = 2;
        caretPositionY = 1;
        kitClass = org.netbeans.modules.editor.plain.PlainKit.class;
        optionsClass = org.netbeans.modules.editor.plain.options.PlainOptions.class;
        fileToBeOpened = new Node(new SourcePackagesNode("PerformanceTestData"), "org.netbeans.test.performance|" + fileName);
        doMeasurement();
    }
    
    public void testJavaEditor() {
        fileName = "Main.java";
        caretPositionX = 38;
        caretPositionY = 19;
        kitClass = org.netbeans.modules.editor.java.JavaKit.class;
        optionsClass = org.netbeans.modules.java.editor.options.JavaOptions.class;
        fileToBeOpened = new Node(new SourcePackagesNode("PerformanceTestData"), "org.netbeans.test.performance|" + fileName);
        doMeasurement();
    }
/*   
    public void testJspEditor() {
        fileName = "Test.jsp";
        caretPositionX = 6;
        caretPositionY = 9;
        kitClass = org.netbeans.modules.web.core.syntax.JSPKit.class;
        optionsClass = org.netbeans.modules.web.core.syntax.settings.JSPOptions.class;
        fileToBeOpened = new Node(new WebPagesNode("PerformanceTestWebApplication"), fileName);
        doMeasurement();
    }
*/   
    public void initialize() {
        // open a java file in the editor
        new OpenAction().performAPI(fileToBeOpened);
        editorOperator = EditorWindowOperator.getEditor(fileName);
        
        //wait painting pf folds in the editor
        waitNoEvent(2000);
        
        // go to the right place
        editorOperator.setCaretPosition(caretPositionX,caretPositionY);
        
        setEditorForMeasuringOn();
    }
    
    public void prepare() {
    }
    
    public ComponentOperator open(){
        //new Action(null, null, new Shortcut(KeyEvent.VK_A)).perform(editorOperator);
        editorOperator.typeKey('a');
        return null;
    }
    
    public void close() {
        // do nothing
    }
    
    
    public void shutdown() {
        setEditorForMeasuringOff();
        editorOperator.closeDiscard();
        super.shutdown();
    }

    private void setEditorForMeasuringOn(){
        // measure only paint events from QuietEditorPane
        repaintManager().addRegionFilter(repaintManager().EDITOR_FILTER);
        
        // set large font size for Editor
/*        BaseOptions options = BaseOptions.getOptions(kitClass);
        if (options.getClass().isInstance(optionsClass)) {
            fontSize = options.getFontSize();
            options.setFontSize(20);
        }
        
        caretBlinkRate = options.getCaretBlinkRate();
        //disable caret blinkering
        options.setCaretBlinkRate(0);
*/    }
    
    private void setEditorForMeasuringOff(){
        // measure only paint events from QuietEditorPane
        repaintManager().resetRegionFilters();
        
        // set back the original font size for Editor
/*        BaseOptions options = BaseOptions.getOptions(kitClass);
        if (options.getClass().isInstance(optionsClass)) {
            options.setFontSize(fontSize);
        }
        
        // set back the original blink rate
        options.setCaretBlinkRate(caretBlinkRate);
*/    }
    
    public static void main(java.lang.String[] args) {
        repeat = 3;
        junit.textui.TestRunner.run(new TypingInEditor("testJspEditor", "Type a character in Jsp Editor"));
    }
}