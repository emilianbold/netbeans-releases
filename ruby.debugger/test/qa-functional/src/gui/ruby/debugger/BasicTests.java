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
package gui.ruby.debugger;

import junit.textui.TestRunner;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.ide.ProjectSupport;
import org.openide.util.Utilities;

/**
 * Basic ruby debugger test.
 *
 * @author Tomas.Musil@sun.com
 */
public class BasicTests extends JellyTestCase {
    
    /** Need to be defined because of JUnit */
    public BasicTests(String name) {
        super(name);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new BasicTests("testDebuggerStartStop"));
        suite.addTest(new BasicTests("testStepInOutOver"));
        suite.addTest(new BasicTests("testNativeRubyDebugging"));

        return suite;
    }
    
    /** Use for execution inside IDE */
    public static void main(java.lang.String[] args) {
        // run whole suite
        TestRunner.run(suite());
        // run only selected test case
        //junit.textui.TestRunner.run(new RubyValidation("testCreateRubyProject"));
    }
    
    public @Override void setUp() {
        System.out.println("########  "+getName()+"  #######");
    }
    
    // name of sample projects
    private static final String SAMPLE_RUBY_PROJECT_NAME = "SampleRubyApplication";  //NOI18N
    //line number with puts "helloworld"
    //TODO get rid of hardcoding line number
    private static final int breakpointLineNumber = 6;
    
    public void testDebuggerStartStop() throws InterruptedException {
        createRubyProject(SAMPLE_RUBY_PROJECT_NAME);
        EditorOperator debuggeeOp = new EditorOperator("main.rb");
        Util.putBreakpointToLine(debuggeeOp, breakpointLineNumber);
        Util.invokeDebugMainProject();
        OutputTabOperator outOp = new OutputTabOperator(SAMPLE_RUBY_PROJECT_NAME + " (debug)");
        outOp.waitText("ruby 1.8.5 debugger listens");
        Util.waitForDebuggingActions();
        if (Utilities.isWindows()){
            // FIXME: when problems of issue #113007 are solved -> cannot kill session, so invoking Continue for now
            Util.invokeContinue();
        } else {
            Util.invokeFinishDebuggerSession();
        }
        outOp.waitText("");
        Thread.sleep(2000);
        assertFalse("debugger is not running now", new Action("Run|Continue", null).isEnabled());
    }

    public void testStepInOutOver() throws InterruptedException{
        EditorOperator debuggeeOp = new EditorOperator("main.rb");
        debuggeeOp.setCaretPositionToLine(breakpointLineNumber+1);
        debuggeeOp.insert("require 'date' \n d = Date.today \n puts d");
        Util.invokeDebugMainProject();
        Util.waitForDebuggingActions();
        Util.invokeStepOver(); //move to puts helloworld
        Util.invokeStepOver(); //move to d = Date.new
        Thread.sleep(500);
        Util.invokeStepInto();
        assertTrue("After step into PC should move to date.rb", Util.isOpenedEditorTab("date.rb"));
        assertEquals("callstack size", 2, Util.getCallStackSize());
        Util.invokeStepOut();
        assertTrue("Local variables should contain d", Util.isLocalVariablePresent("d")); 
        assertEquals("callstack size", 1, Util.getCallStackSize());
        Util.invokeStepOver();
    }

    public void testNativeRubyDebugging() throws InterruptedException{
        String nativeRuby = Util.detectNativeRuby();
        ProjectSupport.waitScanFinished();
        if (nativeRuby != null){
            OutputTabOperator outOp = new OutputTabOperator(SAMPLE_RUBY_PROJECT_NAME + " (debug)");
            Util.setRuby(nativeRuby);
            Util.invokeDebugMainProject();
            Util.waitForDebuggingActions();
            Util.invokeStepOver(); 
            outOp.waitText("Hello World");
            Util.invokeContinue();
        } else {
            fail ("did not found native ruby on $PATH;");
        }
    }
    
    //project generation method
    private void createRubyProject(String projectName) {
        // create new web application project
        NewProjectWizardOperator npwo = NewProjectWizardOperator.invoke();
        // "Ruby"
        String rubyLabel = Bundle.getString("org.netbeans.modules.ruby.rubyproject.ui.wizards.Bundle", "Templates/Project/Ruby");
        npwo.selectCategory(rubyLabel);
        // "Ruby Application"
        String rubyApplicationLabel = Bundle.getString("org.netbeans.modules.ruby.rubyproject.ui.wizards.Bundle", "TXT_NewJavaApp");
        npwo.selectProject(rubyApplicationLabel);
        npwo.btNext().pushNoBlock();
        try {
            // "Choose Ruby Interpreter"
            String chooseRubyTitle = Bundle.getString("org.netbeans.api.ruby.platform.Bundle", "ChooseRuby");
            new NbDialogOperator(chooseRubyTitle).cancel();
        } catch (TimeoutExpiredException e) {
            // ignore - Choose Ruby Interpreter dialog is opened only when native Ruby is available
        }
        NewProjectNameLocationStepOperator npnlso = new NewProjectNameLocationStepOperator();
        npnlso.txtProjectName().setText(projectName);
        npnlso.txtProjectLocation().setText(System.getProperty("netbeans.user")); // NOI18N
        npnlso.finish();
        // wait project appear in projects view
        // wait 30 second
        JemmyProperties.setCurrentTimeout("JTreeOperator.WaitNextNodeTimeout", 30000); // NOI18N
        new ProjectsTabOperator().getProjectRootNode(projectName);
        // wait classpath scanning finished
        ProjectSupport.waitScanFinished();
    }
    
}
