/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.editor.lib2.actions;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.text.DefaultEditorKit;

/**
 * Class handling macro recording of editor actions.
 *
 * @author Miloslav Metelka
 * @since 1.13
 */
public final class MacroRecording {

    /** Action's property for disabling recording of the action as part of a macro. */
    public static final String NO_MACRO_RECORDING_PROPERTY = "NoMacroRecording";
    
    private static final MacroRecording INSTANCE = new MacroRecording();

    public static MacroRecording get() {
        return INSTANCE;
    }

    private StringBuilder macroBuffer;

    private StringBuilder textBuffer;

    private MacroRecording() {
    }

    /**
     * Start recording a macro.
     *
     * @return true if macro recording started successfully or false otherwise.
     */
    public synchronized boolean startRecording() {
        if (isRecording()) {
            return false;
        }
        macroBuffer = new StringBuilder(100);
        textBuffer = new StringBuilder(20);
        return true;
    }

    /**
     * Stop macro recording.
     *
     * @return string describing the macro or null if no recording takes place currently.
     */
    public synchronized String stopRecording() {
        if (!isRecording()) {
            return null;
        }
        if (textBuffer.length() > 0) {
            if (macroBuffer.length() > 0) {
                macroBuffer.append( ' ' );
            }
            appendEncodedText(macroBuffer, textBuffer);
        }
        String completeMacroText = macroBuffer.toString();
        textBuffer = null;
        macroBuffer = null;
        return completeMacroText;
    }

    /**
     * Record given action into a macro buffer.
     *
     * @param action non-null action to record
     * @param evt non-null evt used when recording typed text of default key-typed action.
     */
    public synchronized void recordAction(Action action, ActionEvent evt) {
        if (isRecording() && !Boolean.TRUE.equals(action.getValue(NO_MACRO_RECORDING_PROPERTY))) {
            String actionName = actionName(action);
            if (DefaultEditorKit.defaultKeyTypedAction.equals(actionName)) {
                textBuffer.append(getFilteredActionCommand(evt.getActionCommand()));
            } else {
                if (textBuffer.length() > 0) {
                    if (macroBuffer.length() > 0) {
                        macroBuffer.append( ' ' );
                    }
                    appendEncodedText(macroBuffer, textBuffer);
                    textBuffer.setLength(0);
                }
                if (macroBuffer.length() > 0) {
                    macroBuffer.append(' ');
                }
                // Append encoded action name
                for (int i = 0; i < actionName.length(); i++) {
                    char c = actionName.charAt(i);
                    if (Character.isWhitespace(c) || c == '\\') {
                        macroBuffer.append('\\');
                    }
                    macroBuffer.append(c);
                }
            }
        }
    }

    private boolean isRecording() {
        return (macroBuffer != null);
    }

    private static String actionName(Action action) {
        return (String) action.getValue(Action.NAME);
    }

    private static String getFilteredActionCommand(String cmd) {
        if (cmd == null || cmd.length() == 0) {
            return "";
        }
        char ch = cmd.charAt(0);
        if ((ch >= 0x20) && (ch != 0x7F)) {
            return cmd;
        } else {
            return "";
        }
    }

    private static void appendEncodedText(StringBuilder sb, StringBuilder text) {
        sb.append('"');
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '"' || c == '\\') {
                sb.append('\\');
            }
            sb.append(c);
        }
        sb.append('"');
    }

}
