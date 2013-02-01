/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.test.html5.debug;

import java.util.Map;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.modules.debugger.BreakpointsWindowOperator;
import org.netbeans.jellytools.modules.debugger.actions.ToggleBreakpointAction;
import org.netbeans.jellytools.modules.debugger.actions.DeleteAllBreakpointsAction;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.test.html5.GeneralHTMLProject;

/**
 *
 * @author Vladimir Riha
 */
public class JavaScriptDebugger extends GeneralHTMLProject {

    public static final int VARIABLES_TIMEOUTS = 2000;

    public JavaScriptDebugger(String arg0) {
        super(arg0);
    }

    /**
     * Sets breakpoint in editor on line with specified text.
     *
     * @param eo EditorOperator instance where to set breakpoint
     * @param text text to find for setting breakpoint
     * @return line number where breakpoint was set (starts from 1)
     */
    public int setLineBreakpoint(EditorOperator eo, String text) throws Exception {
        eo.select(text); // NOI18N
        final int line = eo.getLineNumber();
        // toggle breakpoint via pop-up menu
        new ToggleBreakpointAction().perform(eo.txtEditorPane());
        // wait breakpoint established
        new Waiter(new Waitable() {
            @Override
            public Object actionProduced(Object editorOper) {
                Object[] annotations = ((EditorOperator) editorOper).getAnnotations(line);
                for (int i = 0; i < annotations.length; i++) {
                    if ("Breakpoint".equals(EditorOperator.getAnnotationType(annotations[i]))) { // NOI18N
                        return Boolean.TRUE;
                    }
                }
                return null;
            }

            @Override
            public String getDescription() {
                return ("Wait breakpoint established on line " + line); // NOI18N
            }
        }).waitAction(eo);
        return line;
    }

    public void cleanBreakpoints() {
        BreakpointsWindowOperator window = BreakpointsWindowOperator.invoke();
        new DeleteAllBreakpointsAction().performPopup(window);
    }

    /**
     * Waits for variable to appear in Variables window (since Variables is
     * minimized by default, there could be "loading" message).
     *
     * @param expectedVariable
     * @param vo
     */
    public void waitForVariable(final String expectedVariable, final VariablesOperator vo) {
        try {
            Waiter waiter = new Waiter(new Waitable() {
                @Override
                public Object actionProduced(Object obj) {
                    try {
                        return ((Map<String, Variable>) vo.getVariables()).get(expectedVariable) != null ? Boolean.TRUE : null;
                    } catch (Exception ex) {
                        return null;
                    }
                }

                @Override
                public String getDescription() {
                    return ("Wait for Variables to contain " + expectedVariable);
                }
            });
            waiter.getTimeouts().setTimeout("Waiter.WaitingTime", VARIABLES_TIMEOUTS);
            waiter.waitAction(null);
        } catch (InterruptedException e) {
        }
    }
}
