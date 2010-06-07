/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.php.smarty.editor.completion;

import java.util.Arrays;
import java.awt.Font;
import java.awt.Graphics;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.text.Caret;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.indent.api.Indent;
import org.netbeans.spi.editor.completion.*;
import java.awt.Color;
import java.awt.event.KeyEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.openide.xml.XMLUtil;

/**
 * Code completion result item base class
 *
 * @author Martin Fousek
 */
public class TplCompletionItem implements CompletionItem {

    protected static final int DEFAULT_SORT_PRIORITY = 22;

    //------------------------------------------
    protected int substitutionOffset;
    protected String text, helpId;
    protected boolean shift;

    protected TplCompletionItem(String text, int substituteOffset) {
        this.substitutionOffset = substituteOffset;
        this.text = text;
    }

    protected TplCompletionItem(String text, int substituteOffset, String helpId) {
        this(text, substituteOffset);
        this.helpId = helpId;
    }

    public String getItemText() {
        return text;
    }

    public int getSortPriority() {
        return DEFAULT_SORT_PRIORITY;
    }

    public CharSequence getSortText() {
        return getItemText();
    }

    public CharSequence getInsertPrefix() {
        return getItemText();
    }

    public void processKeyEvent(KeyEvent e) {
        shift = (e.getKeyCode() == KeyEvent.VK_ENTER && e.getID() == KeyEvent.KEY_PRESSED && e.isShiftDown());
    }

    public void defaultAction(JTextComponent component) {
        if (component != null) {
            if (!shift) {
                Completion.get().hideDocumentation();
                Completion.get().hideCompletion();
            }
            int caretOffset = component.getSelectionEnd();
            substituteText(component, caretOffset - substitutionOffset);
        }

    }

    protected int getMoveBackLength() {
        return 0; //default
    }

    /** 
     * Subclasses may override to customize the completed text 
     * if they do not want to override the substituteText method. 
     */
    protected String getSubstituteText() {
        return getItemText();
    }

    protected boolean substituteText(JTextComponent c, int len) {
        return substituteText(c, len, getMoveBackLength());
    }

    protected boolean substituteText(final JTextComponent c, final int len, int moveBack) {
        return substituteText(c, getSubstituteText(), len, moveBack);
    }

    protected boolean substituteText(final JTextComponent c, final String substituteText, final int len, int moveBack) {
        final BaseDocument doc = (BaseDocument) c.getDocument();
        final boolean[] result = new boolean[1];
        result[0] = true;

        doc.runAtomic(new Runnable() {

            public void run() {
                try {
                    //test whether we are trying to insert sg. what is already present in the text
                    String currentText = doc.getText(substitutionOffset, (doc.getLength() - substitutionOffset) < substituteText.length() ? (doc.getLength() - substitutionOffset) : substituteText.length());
                    if (!substituteText.equals(currentText)) {
                        //remove common part
                        doc.remove(substitutionOffset, len);
                        doc.insertString(substitutionOffset, substituteText, null);
                    } else {
                        c.setCaretPosition(c.getCaret().getDot() + substituteText.length() - len);
                        }
                } catch (BadLocationException ex) {
                    result[0] = false;
                }

            }
        });

        //format the inserted text
        reindent(c);

        if (moveBack != 0) {
            Caret caret = c.getCaret();
            int dot = caret.getDot();
            caret.setDot(dot - moveBack);
        }

        return result[0];
    }

    private void reindent(JTextComponent component) {

        final BaseDocument doc = (BaseDocument) component.getDocument();
        final int dotPos = component.getCaretPosition();
        final Indent indent = Indent.get(doc);
        indent.lock();
        try {
            doc.runAtomic(new Runnable() {

                public void run() {
                    try {
                        int startOffset = Utilities.getRowStart(doc, dotPos);
                        int endOffset = Utilities.getRowEnd(doc, dotPos);
                        indent.reindent(startOffset, endOffset);
                    } catch (BadLocationException ex) {
                        //ignore
                        }
                }
            });
        } finally {
            indent.unlock();
        }

    }

    public boolean instantSubstitution(JTextComponent component) {
        if (component != null) {
            try {
                int caretOffset = component.getSelectionEnd();
                if (caretOffset > substitutionOffset) {
                    String currentText = component.getDocument().getText(substitutionOffset, caretOffset - substitutionOffset);
                    if (!getSubstituteText().toString().startsWith(currentText)) {
                        return false;
                    }
                }
            } catch (BadLocationException ble) {
            }
        }
        defaultAction(component);
        return true;
    }

    public int getPreferredWidth(Graphics g, Font defaultFont) {
        return CompletionUtilities.getPreferredWidth(getLeftHtmlText(), getRightHtmlText(), g, defaultFont);
    }

    public void render(Graphics g, Font defaultFont, Color defaultColor, Color backgroundColor, int width, int height, boolean selected) {
        CompletionUtilities.renderHtml(getIcon(), getLeftHtmlText(), getRightHtmlText(), g, defaultFont, Color.BLACK, width, height, selected);
    }

    protected ImageIcon getIcon() {
        return new ImageIcon(getClass().getResource("/org/netbeans/modules/php/smarty/resources/tpl-cc-icon.png"));
    }

    protected String getLeftHtmlText() {
        return getItemText();
    }

    protected String getRightHtmlText() {
        return null;
    }

    public String getHelpId() {
        return this.helpId;
    }

    /** Returns a url or null, if the help is not URL or the help is not defined.
     */
    public URL getHelpURL() {
        if (helpId == null || helpId.equals("")) {
            return null;
        }
        try {
            return new URL(helpId);
        } catch (java.io.IOException e) {
        }
        return null;
    }

    /** Returns help for the item. It can be only url. If the item doesn't have a help
     *  than returns null. The class can overwrite this method and compounds the help realtime.
     */
    public String getHelp() {
        return null;
    }

    /** Returns whether the item has a help.
     */
    public boolean hasHelp() {
        return (helpId != null && helpId.length() > 0);
    }

    public CompletionTask createDocumentationTask() {
        return new AsyncCompletionTask(new TplCompletionProvider.DocQuery(this));
    }

    public CompletionTask createToolTipTask() {
        return null;
    }

    public static class BuiltInFunction extends TplCompletionItem {

        protected static final String ATTR_NAME_COLOR = hexColorCode(Color.green.darker());

        public BuiltInFunction(String value, int offset, String helpId) {
            super(value, offset, helpId);
        }

        @Override
        protected String getLeftHtmlText() {
            return "<font color=#" + ATTR_NAME_COLOR + ">" + getItemText() + "</font>"; //NOI18N
        }

        @Override
        public int getSortPriority() {
            return 20;
        }

    }

    public static class VariableModifiers extends TplCompletionItem {

        protected static final String ATTR_NAME_COLOR = hexColorCode(Color.blue.darker());

        public VariableModifiers(String value, int offset, String helpId) {
            super(value, offset, helpId);
        }

        @Override
        protected String getLeftHtmlText() {
            return "<font color=#" + ATTR_NAME_COLOR + ">" + getItemText() + "</font>"; //NOI18N
        }

        @Override
        public int getSortPriority() {
            return 25;
        }
    }

    public static final String hexColorCode(Color c) {
        return Integer.toHexString(c.getRGB()).substring(2);
    }

}
