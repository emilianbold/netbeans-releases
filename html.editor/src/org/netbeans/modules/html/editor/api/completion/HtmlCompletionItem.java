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
package org.netbeans.modules.html.editor.api.completion;

import org.netbeans.modules.html.editor.completion.*;
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
import org.netbeans.modules.html.editor.javadoc.HelpManager;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.openide.xml.XMLUtil;

/**
 * Code completion result item base class
 *
 * @author  Dusan Balek, Marek Fukala
 */
public class HtmlCompletionItem implements CompletionItem {

    protected static final int DEFAULT_SORT_PRIORITY = 20;

    //----------- Factory methods --------------
    public static HtmlCompletionItem createTag(String name, int substitutionOffset, String helpId, boolean possible) {
        return new Tag(name, substitutionOffset, helpId, possible);
    }
 
    public static HtmlCompletionItem createEndTag(String name, int substitutionOffset, String helpId, int order, EndTag.Type type) {
        return new EndTag(name, substitutionOffset, helpId, order, type);
    }

    public static HtmlCompletionItem createAutocompleteEndTag(String name, int substitutionOffset) {
        return new AutocompleteEndTag(name, substitutionOffset);
    }

    public static HtmlCompletionItem createBooleanAttribute(String name, int substitutionOffset, boolean required, String helpId) {
        return new BooleanAttribute(name, substitutionOffset, required, helpId);
    }

    public static HtmlCompletionItem createAttribute(String name, int substitutionOffset, boolean required, String helpId) {
        return new Attribute(name, substitutionOffset, required, helpId);
    }

    public static HtmlCompletionItem createAttributeValue(String name, int substitutionOffset, boolean addQuotation) {
        return new AttributeValue(name, substitutionOffset, addQuotation);
    }

    public static HtmlCompletionItem createAttributeValue(String name, int substitutionOffset) {
        return createAttributeValue(name, substitutionOffset, false);
    }

    public static HtmlCompletionItem createCharacterReference(String name, char value, int substitutionOffset, String helpId) {
        return new CharRefItem(name, value, substitutionOffset, helpId);
    }

    public static HtmlCompletionItem createFileCompletionItem(String value, int substitutionOffset, Color color, ImageIcon icon) {
        return new FileAttributeValue(value, substitutionOffset, color, icon);
    }

    public static HtmlCompletionItem createGoUpFileCompletionItem(int substitutionOffset, Color color, ImageIcon icon) {
        return new GoUpFileAttributeValue(substitutionOffset, color, icon);
    }


    //------------------------------------------
    protected int substitutionOffset;
    protected String text, helpId;
    protected boolean shift;

    protected HtmlCompletionItem(String text, int substituteOffset) {
        this.substitutionOffset = substituteOffset;
        this.text = text;
    }

    protected HtmlCompletionItem(String text, int substituteOffset, String helpId) {
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
        CompletionUtilities.renderHtml(getIcon(), getLeftHtmlText(), getRightHtmlText(), g, defaultFont, defaultColor, width, height, selected);
    }

    protected ImageIcon getIcon() {
        return null;
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
        return HelpManager.getDefault().getHelp(helpId);
    }

    /** Returns whether the item has a help.
     */
    public boolean hasHelp() {
        return (helpId != null && helpId.length() > 0);
    }

    public CompletionTask createDocumentationTask() {
        return new AsyncCompletionTask(new HtmlCompletionProvider.DocQuery(this));
    }

    public CompletionTask createToolTipTask() {
        return null;
    }

    //------------------------------------------------------------------------------
    /** 
     * Completion item representing a JSP tag including its prefix eg. <jsp:useBean />
     */
    public static class Tag extends HtmlCompletionItem {

        private String GRAY_COLOR_CODE = hexColorCode(Color.GRAY);
        private boolean possible;

        protected Tag(String text, int substitutionOffset, String helpId, boolean possible) {
            super(text, substitutionOffset, helpId);
            this.possible = possible;
        }

        //end tag autocomplete handling
        @Override
        public void defaultAction(JTextComponent component) {
            //force the completion not to hide when the item text is completed
            super.shift = true;
            super.defaultAction(component);
        }

        @Override
        protected String getSubstituteText() {
            return "<" + getItemText() + ">";
        }

        @Override
        public int getSortPriority() {
            return super.getSortPriority() + (possible ? -10 : 0);
        }

        @Override
        protected String getLeftHtmlText() {
            return possible ? 
                "<font color=#0000ff>&lt;" + getItemText() + "&gt;</font>" : //NOI18N
                "<font color=#" + GRAY_COLOR_CODE + ">&lt;" + getItemText() + "&gt;</font>"; //NOI18N
        }
    }

    /**
     * Completion item representing a JSP tag including its prefix eg. <jsp:useBean />
     */
    public static class EndTag extends HtmlCompletionItem {
 
        public enum Type {
            DEFAULT(hexColorCode(Color.BLUE), false, DEFAULT_SORT_PRIORITY), //NOI18N
            OPTIONAL_EXISTING(hexColorCode(Color.GRAY), false, DEFAULT_SORT_PRIORITY),
            OPTIONAL_MISSING(hexColorCode(Color.BLUE), false, DEFAULT_SORT_PRIORITY - 10), //NOI18N
            REQUIRED_EXISTING(hexColorCode(Color.GRAY), false, DEFAULT_SORT_PRIORITY),
            REQUIRED_MISSING(hexColorCode(Color.BLUE), false, DEFAULT_SORT_PRIORITY - 10); //NOI18N
                    
            private String colorCode;
            private boolean bold;
            private int sortPriority;

            private Type(String colorCode, boolean bold, int sortPriority) {
                this.colorCode = colorCode;
                this.bold = bold;
                this.sortPriority = sortPriority;
            }

        }

        private int orderIndex;
        private Type type;

        EndTag(String text, int substitutionOffset, String helpId, int order, Type type) {
            super(text, substitutionOffset, helpId);
            this.orderIndex = order;
            this.type = type;
        }

        @Override
        public CharSequence getSortText() {
            if (orderIndex == -1) {
                return super.getSortText();
            } else {
                int zeros = orderIndex > 100 ? 0 : orderIndex > 10 ? 1 : 2;
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < zeros; i++) {
                    sb.append('0'); //NOI18N
                }
                sb.append("" + orderIndex);
                return sb;
            }
        }

        @Override
        protected String getSubstituteText() {
            return "</" + getItemText() + ">"; //NOI18N
        }

        @Override
        public int getSortPriority() {
            return this.type.sortPriority;
        }

        @Override
        protected String getLeftHtmlText() {
            return (type.bold ? "<b>" : "") + //NOI18N
                    "<font color=#" + type.colorCode + ">&lt;/" + getItemText() + "&gt;</font>" + //NOI18N
                    (type.bold ? "</b>" : ""); //NOI18N
        }
    }

    public static class AutocompleteEndTag extends EndTag {

        public AutocompleteEndTag(String text, int substitutionOffset) {
            super(text, substitutionOffset, null, -1, Type.DEFAULT);
        }

        @Override
        protected int getMoveBackLength() {
            return getSubstituteText().length(); //jump before the completed tag
        }

        @Override
        public boolean instantSubstitution(JTextComponent component) {
            return false;
        }

    }

    /**
     * Completion item representing html entity reference.
     */
    public static class CharRefItem extends HtmlCompletionItem {

        private char value;

        CharRefItem(String name, char value, int substitutionOffset, String helpId) {
            super(name, substitutionOffset, helpId);
            this.value = value;
        }

        @Override
        protected String getSubstituteText() {
            return "&" + getItemText() + ";"; //NOI18N
        }

        @Override
        protected String getLeftHtmlText() {
            return "<b>&amp;" + escape(getItemText()) + ";</b>"; //NOI18N
        }

        @Override
        protected String getRightHtmlText() {
            String strVal;
            if (value == '>') { //NOI18N
                strVal = "&gt;"; //NOI18N
            } else if (value == '<') { //NOI18N
                strVal = "&lt;"; //NOI18N
            } else {
                strVal = Character.toString(value);
            }
            return "<b><font color=#990000>" + strVal + "</font></b>"; //NOI18N
        }
    }

    /** Item representing a JSP attribute value. */
    public static class AttributeValue extends HtmlCompletionItem {

        private boolean addQuotation;

        public AttributeValue(String value, int offset, boolean addQuotation) {
            super(value, offset);
            this.addQuotation = addQuotation;
        }

        @Override
        protected String getSubstituteText() {
            return addQuotation ? "\"" + super.getSubstituteText() + "\"" : super.getSubstituteText();
        }


    }

    public static class Attribute extends HtmlCompletionItem {

        private boolean required;

        protected static final String ATTR_NAME_COLOR = hexColorCode(Color.green.darker());

        public Attribute(String value, int offset, boolean required, String helpId) {
            super(value, offset, helpId);
            this.required = required;
        }

        @Override
        protected String getSubstituteText() {
            return getItemText() + "=\"\""; //NOI18N
        }

        @Override
        protected int getMoveBackLength() {
            return 1; //last quotation
        }

        @Override
        public int getSortPriority() {
            return super.getSortPriority() - (required ? 1 : 0);
        }

        @Override
        protected String getLeftHtmlText() {
            return (required ? "<b>" : "") + //NOI18N
                    "<font color=#" + ATTR_NAME_COLOR + ">" + getItemText() + "</font>" + //NOI18N
                    (required ? "</b>" : ""); //NOI18N
        }
    }

    public static class BooleanAttribute extends HtmlCompletionItem {

        private boolean required;

        protected static final String ATTR_NAME_COLOR = hexColorCode(Color.green.darker());

        public BooleanAttribute(String value, int offset, boolean required, String helpId) {
            super(value, offset, helpId);
            this.required = required;
        }

        @Override
        protected String getLeftHtmlText() {
            return (required ? "<b>" : "") + //NOI18N
                    "<font color=#" + ATTR_NAME_COLOR + ">" + getItemText() + "</font>" + //NOI18N
                    (required ? "</b>" : ""); //NOI18N
        }
    }

    /** Item representing a File attribute */
    public static class FileAttributeValue extends HtmlCompletionItem {

        private javax.swing.ImageIcon icon;
        private Color color;

        FileAttributeValue(String text, int substitutionOffset, Color color, javax.swing.ImageIcon icon) {
            super(text, substitutionOffset);
            this.color = color;
            this.icon = icon;
        }

        @Override
        protected ImageIcon getIcon() {
            return icon;
        }

        @Override
        protected String getLeftHtmlText() {
            return "<font color='" + hexColorCode(color) + "'>" + getItemText() + "</font>"; //NOI18N
        }
    }

    public static class GoUpFileAttributeValue extends FileAttributeValue {

        GoUpFileAttributeValue(int substitutionOffset, Color color, javax.swing.ImageIcon icon) {
            super("../", substitutionOffset, color, icon); //NOI18N
        }

        @Override
        public int getSortPriority() {
            return super.getSortPriority() - 1; //be first of the file compl. items
        }
    }

    public static final String hexColorCode(Color c) {
        return Integer.toHexString(c.getRGB()).substring(2);
    }

    private static String escape(String s) {
        if (s != null) {
            try {
                return XMLUtil.toAttributeValue(s);
            } catch (Exception ex) {
            }
        }
        return s;
    }
}
