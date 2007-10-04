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

import org.openide.util.Utilities;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.JTextField;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.OptionsOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.TreeTableOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.modules.debugger.actions.ToggleBreakpointAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

/**
 * Utils for testing debugger
 * @author Tomas.Musil@sun.com
 */
public final class Util {
    
    //collection of static methods
    private Util(){}

    static void waitForDebuggingActions() {
        Action a = new Action("Run|Step Over", null);
        while (!a.isEnabled());
    }
    
    static String detectNativeRuby() {
        String rubyPath = null;
        String path = System.getenv("PATH"); // NOI18N
        if (path == null) {
            path = System.getenv("Path"); // NOI18N
        }
        if (path != null) {
            final Set<String> rubies = new TreeSet<String>();
            Set<String> dirs = new TreeSet<String>(Arrays.asList(path.split(File.pathSeparator)));
            for (String dir : dirs) {
                File f = null;
                if (Utilities.isWindows()) {
                    f = new File(dir, "ruby.exe");//NOI18N
                } else {
                    f = new File(dir, "ruby"); // NOI18N
                    // Don't include /usr/bin/ruby on the Mac - it's no good
                    if (Utilities.isMac() && "/usr/bin/ruby".equals(f.getPath())) {
                            continue;
                    }
                }
                if (f.exists()) {
                    try {
                        rubies.add(f.getCanonicalPath());
                    } catch (IOException e) {
                        rubies.add(f.getPath());
                    }
                }
            }
            
            if(!rubies.isEmpty()) rubyPath = (String) rubies.toArray()[0]; //use first ruby on $PATH
        }
    return rubyPath;
    }
    
    static void setRuby(String rubyPath) {
        OptionsOperator optionsOper = OptionsOperator.invoke();
        optionsOper.selectCategory("Ruby"); //NOI18N
//        try{
//            String chooseRubyTitle = Bundle.getString("org.netbeans.api.ruby.platform.Bundle", "ChooseRuby");
//            new NbDialogOperator(chooseRubyTitle).closeByButton();
//        } catch (TimeoutExpiredException exc) {
//            // never mind
//        }
        JLabelOperator jloRI = new JLabelOperator(optionsOper, "Ruby Interpreter:");//NOI18N
        new JTextFieldOperator((JTextField)jloRI.getLabelFor()).setText(rubyPath);
        optionsOper.ok();
    }

    static boolean isLocalVariablePresent(String variableName) {
        TopComponentOperator lvo = new TopComponentOperator("Local Variables");//NOI18N
        JTableOperator lvtableOp = new JTableOperator(lvo);
        TreeTableOperator tto = new TreeTableOperator((javax.swing.JTable) lvtableOp.getSource());

        try {
            Node varNode = new Node(tto.tree(), variableName);
        } catch (TimeoutExpiredException timeoutExpiredException) {
            return false;
        }
        return true;
    }
    
    static boolean isOpenedEditorTab(String sourceName){
        try {
        EditorOperator eo = new EditorOperator(sourceName);
        } catch (TimeoutExpiredException timeoutExpiredException) {
            return false;
        }
        return true;

    }
    
    static int getCallStackSize() {
        TopComponentOperator callStackOp = new TopComponentOperator("Call Stack");//NOI18N
        JTableOperator tableOp = new JTableOperator(callStackOp);
        return tableOp.getRowCount();
    }

    
    static void putBreakpointToLine(EditorOperator eo, int lineNumber) {
        eo.setCaretPositionToLine(lineNumber);
        new ToggleBreakpointAction().perform();
    }

    static void invokeDebugMainProject() {
        new Action("Run|Debug Main Project", null).perform();//NOI18N
    }

    static void invokeFinishDebuggerSession() {
        new Action("Run|Finish Debugger Session", null).perform();//NOI18N
    }
    
    static void invokeStepOver(){
        new Action("Run|Step Over", null).perform();//NOI18N
    }
    
    static void invokeStepInto(){
        new Action("Run|Step Into", null).perform();//NOI18N
    }

    static void invokeStepOut(){
        new Action("Run|Step Out", null).perform();//NOI18N
    }

    static void invokeContinue() {
        new Action("Run|Continue", null).perform();//NOI18N
    }
}