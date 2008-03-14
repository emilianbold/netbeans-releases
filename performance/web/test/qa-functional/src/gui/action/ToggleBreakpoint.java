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

package gui.action;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.EditorWindowOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.actions.Action.Shortcut;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.test.web.performance.WebPerformanceTestCase;
import org.netbeans.jellytools.modules.debugger.actions.BreakpointsWindowAction;


/**
 * Test of Paste text to opened source editor.
 *
 * @author  anebuzelsky@netbeans.org, mmirilovic@netbeans.org
 */
public class ToggleBreakpoint extends WebPerformanceTestCase {
    private String file;
    private List bpList = new ArrayList();
    /** Creates a new instance of ToggleBreakpoint */
    public ToggleBreakpoint(String testName) {
        super(testName);
        init();
    }
    
    /** Creates a new instance of ToggleBreakpoint */
    public ToggleBreakpoint(String file, String testName, String performanceDataName) {
        super(testName, performanceDataName);
        this.file = file;
    }
    
    protected void init() {
        super.init();
        expectedTime = UI_RESPONSE;
        WAIT_AFTER_PREPARE = 1000;
        WAIT_AFTER_OPEN = 100;
    }
    private EditorOperator editorOperator1;
    
    protected void initialize() {
        EditorOperator.closeDiscardAll();
        jspOptions().setCaretBlinkRate(0);
        // delay between the caret stops and the update of his position in status bar
        jspOptions().setStatusBarCaretDelay(0);
        // open file in the editor
        new OpenAction().performAPI(new Node(new ProjectsTabOperator().getProjectRootNode("TestWebProject"),"Web Pages|"+file));
        editorOperator1 = new EditorWindowOperator().getEditor(file);
//        eventTool().waitNoEvent(500);
//        waitNoEvent(1000);
        repaintManager().addRegionFilter(repaintManager().EDITOR_FILTER);
    }
    
    public void prepare() {
        System.out.println("=== " + this.getClass().getName() + " ===");
        editorOperator1.makeComponentVisible();
        editorOperator1.setCaretPosition(7,1);
//        eventTool().waitNoEvent(100);
    }
    
    public ComponentOperator open(){
        // Toggle Breakpoint
        new ActionNoBlock(null, null, new Shortcut(KeyEvent.VK_F8, KeyEvent.CTRL_MASK)).perform(editorOperator1);
        return null;
    }
    
    public void close() {
        deleteAllBreakpoints();
    }
    
    protected void shutdown() {
        repaintManager().resetRegionFilters();
        editorOperator1.closeDiscard();
        super.shutdown();
    }
    
    private void deleteAllBreakpoints() {
        new ActionNoBlock(null, null, new Shortcut(KeyEvent.VK_5, KeyEvent.ALT_MASK | KeyEvent.SHIFT_MASK)).perform();
        //new BreakpointsWindowAction().perform();
        //new Action("Window|Debugging|Breakpoints",null).perform();
        TopComponentOperator tco = new TopComponentOperator("Breakpoints");
        new Action(null,"Delete All").perform(tco);
        tco.close();
    }
}
