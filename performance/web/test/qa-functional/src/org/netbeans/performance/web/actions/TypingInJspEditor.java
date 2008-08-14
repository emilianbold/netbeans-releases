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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006Sun
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

import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.EditorWindowOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.actions.Action.Shortcut;
import org.netbeans.jellytools.actions.OpenAction;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;/**
 * Test of typing in opened source editor.
 *
 * @author  anebuzelsky@netbeans.org
 */
public class TypingInJspEditor extends PerformanceTestCase {
    private String file;
    private int line;
    
    public static final String suiteName="UI Responsiveness Web Actions suite";    
    
    /** Creates a new instance of TypingInEditor */
    public TypingInJspEditor(String file, int line, String testName) {
        super(testName);
        this.file = file;
        this.line = line;
        init();
    }
    
    /** Creates a new instance of TypingInEditor */
    public TypingInJspEditor(String file, int line, String testName, String performanceDataName) {
        super(testName, performanceDataName);
        this.file = file;
        this.line = line;
        init();
    }
    
    protected void init() {
//        super.init();
        expectedTime = UI_RESPONSE;
        WAIT_AFTER_PREPARE = 3000;
        WAIT_AFTER_OPEN = 100;
    }
    
    private EditorOperator editorOperator;
    
    public void testTypingInJspEditor() {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    public void testTypingInJspEditorAtBeginningOfLargeFile() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void testTypingInJspEditorAtEndOfLargeFile() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void testTypingInJspEditorInTheMiddleOfLargeFile() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void testTypingInJspEditorWithLargeFile() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    protected void initialize() {
        System.out.println("=== " + this.getClass().getName() + " ===");
//        jspOptions().setCaretBlinkRate(0);
        // delay between the caret stops and the update of his position in status bar
//        jspOptions().setStatusBarCaretDelay(0);
//        jspOptions().setFontSize(20);
//        jspOptions().setCodeFoldingEnable(false);
        // open a java file in the editor
        new OpenAction().performAPI(new Node(new ProjectsTabOperator().getProjectRootNode("TestWebProject"),"Web Pages|"+file));
        editorOperator = new EditorWindowOperator().getEditor(file);
        // go to the right place
        editorOperator.setCaretPositionToLine(line);
        // make the file modified
        //XXX new ActionNoBlock(null, null, new Shortcut(KeyEvent.VK_ENTER)).perform(editorOperator);
        //wait painting pf folds in the editor
        new EventTool().waitNoEvent(1000);
    }
    
    public void prepare() {
   }
    
    public ComponentOperator open(){
        //repaintManager().setOnlyEditor(true);
        repaintManager().addRegionFilter(repaintManager().EDITOR_FILTER);
//        KeyStroke keyA = KeyStroke.getKeyStroke('a');
//        new ActionNoBlock(null, null, keyA).perform(editorOperator);
        editorOperator.typeKey('a');
        return null;
    }
    
    public void close() {
        //repaintManager().setOnlyEditor(false);
        repaintManager().resetRegionFilters();
       
    }
    
    protected void shutdown() {
        editorOperator.closeDiscard();
        super.shutdown();
    }
}
