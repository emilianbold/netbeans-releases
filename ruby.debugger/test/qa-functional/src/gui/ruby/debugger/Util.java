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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.modules.debugger.actions.ToggleBreakpointAction;

/**
 * Utils for testing debugger
 * @author Tomas Musil, Jiri Skrivanek
 */
public final class Util {

    /** Collection of static methods. */
    private Util() {
    }

    /** Sets breakpoint in editor on line with specified text.
     * @param eo EditorOperator instance where to set breakpoint
     * @param text text to find for setting breakpoint
     * @return line number where breakpoint was set (starts from 1)
     */
    public static int setBreakpoint(EditorOperator eo, String text) throws Exception {
        eo.select(text); // NOI18N

        final int line = eo.getLineNumber();
        // toggle breakpoint via pop-up menu
        new ToggleBreakpointAction().perform(eo.txtEditorPane());
        // wait breakpoint established
        new Waiter(new Waitable() {

            public Object actionProduced(Object editorOper) {
                Object[] annotations = ((EditorOperator) editorOper).getAnnotations(line);
                for (int i = 0; i < annotations.length; i++) {
                    if ("Breakpoint".equals(EditorOperator.getAnnotationType(annotations[i]))) { // NOI18N
                        return Boolean.TRUE;
                    }
                }
                return null;
            }

            public String getDescription() {
                return ("Wait breakpoint established on line " + line); // NOI18N

            }
        }).waitAction(eo);
        return line;
    }

    /** Waits debugger stopped and green annotation is in editor.
     * @param eo EditorOperator instance
     * @return line number where breakpoint was reached (starts from 1)
     */
    public static int waitStopped(EditorOperator eo) throws Exception {
        return waitStopped(eo, 0);
    }

    /** Waits debugger stopped and green annotation is in editor.
     * @param eo EditorOperator instance
     * @param lineNumber line number where to wait for annotation (starts at 1)
     * @return line number where breakpoint was reached (starts at 1)
     */
    public static int waitStopped(EditorOperator eo, final int lineNumber) throws Exception {
        // wait breakpoint reached
        new Waiter(new Waitable() {
            public Object actionProduced(Object editorOper) {
                Object[] annotations;
                if (lineNumber > 0) {
                    annotations = ((EditorOperator) editorOper).getAnnotations(lineNumber);
                } else {
                    annotations = ((EditorOperator) editorOper).getAnnotations();
                }
                for (int i = 0; i < annotations.length; i++) {
                    if ("CurrentPC".equals(EditorOperator.getAnnotationType(annotations[i]))) { // NOI18N
                        return Boolean.TRUE;
                    }
                }
                return null;
            }

            public String getDescription() {
                return ("Wait debugger stopped and green annotation in editor."); // NOI18N

            }
        }).waitAction(eo);
        return eo.getLineNumber();
    }
}