/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.repl;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.util.stream.Collectors;
import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.api.editor.document.AtomicLockDocument;
import org.netbeans.api.editor.document.LineDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;
import org.netbeans.modules.jshell.model.ConsoleModel;
import org.netbeans.modules.jshell.model.ConsoleSection;
import org.netbeans.modules.jshell.support.ShellSession;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author sdedic
 */
// must be registered for java also, to give completions on the 1st line
@MimeRegistrations({
    @MimeRegistration(mimeType="text/x-repl", service=CompletionProvider.class),
    @MimeRegistration(mimeType="text/x-java", service=CompletionProvider.class)
})
public class HistoryCompletionProvider implements CompletionProvider {

    @Override
    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        return 0;
    }
    
    private boolean isFirstJavaLine(JTextComponent component) {
        ShellSession s = ShellSession.get(component.getDocument());
        if (s == null) {
            return false;
        }
        ConsoleSection sec = s.getModel().getInputSection();
        if (sec == null) {
            return false;
        }
        LineDocument ld = LineDocumentUtils.as(component.getDocument(), LineDocument.class);
        if (ld == null) {
            return false;
        }

        int off = sec.getStart();
        int caret = component.getCaretPosition();
        int s1 = LineDocumentUtils.getLineStart(ld, caret);
        int s2 = LineDocumentUtils.getLineStart(ld, off);
        return s1 == s2;
    }
    
    @Override
    public CompletionTask createTask(int queryType, final JTextComponent component) {
        if ((queryType != COMPLETION_ALL_QUERY_TYPE && queryType != COMPLETION_QUERY_TYPE) || 
                !isFirstJavaLine(component)) {
            System.err.println("Completion type: " + queryType);
            return null;
        }
        // check that the caret is at the first line of the editable area:
        Document doc = component.getDocument();
        ShellSession session = ShellSession.get(doc);
        if (session == null) {
            return null;
        }
        ConsoleModel model = session.getModel();
        if (model == null) {
            return null;
        }
        ConsoleSection is = model.getInputSection();
        if (is == null) {
            return null;
        }
        LineDocument ld = LineDocumentUtils.as(doc, LineDocument.class);
        if (ld == null) {
            return null;
        }

        int caret = component.getCaretPosition();
        int lineStart = is.getPartBegin();
        try {
            int lineEnd = LineDocumentUtils.getLineEnd(ld, caret);
            if (caret < lineStart || caret > lineEnd) {
                return null;
            }
        } catch (BadLocationException ex) {
            return null;
        }
        
        return new AsyncCompletionTask(new T(
            model,
            is
        ), component);
    }
    
    private static class T extends AsyncCompletionQuery {
        private final ConsoleModel model;
        private final ConsoleSection input;
        private int counter = 1;
        
        public T(ConsoleModel model, ConsoleSection input) {
            this.model = model;
            this.input = input;
        }
        
        private CompletionItem createSectionItem(ConsoleSection s) {
            String text = s.getContents(model.getDocument());
            return new ItemImpl(counter++, text);
        }
        
        @Override
        protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
            if (model.getDocument() != doc) {
                resultSet.finish();
                return;
            }
            resultSet.addAllItems(
                    model.getSections().stream().
                            filter(s -> shouldDisplay(s)).
                            map(s -> createSectionItem(s)).
                            collect(Collectors.toList())
            );
            resultSet.finish();
        }
        
        private boolean shouldDisplay(ConsoleSection item) {
            if (!item.getType().input || item == input) {
                return false;
            }
            String text = item.getContents(model.getDocument());
            return !text.trim().isEmpty();
        }
    }
    
    
    @NbBundle.Messages({
        "# {0} - item number in the history",
        "History_ItemIndex_html=<b><i>#{0}</i></b>"
    })
    private static class ItemImpl implements CompletionItem {
        private final int index;
        private final String text;

        public ItemImpl(int index, String text) {
            this.index = index;
            this.text = text;
        }
        
        private String getLeftText() {
            return text;
        }
        
        private String getRightText() {
            return Bundle.History_ItemIndex_html(index);
        }
        
        @Override
        public void defaultAction(JTextComponent component) {
            if (component == null) {
                return;
            }
            int last = text.length() - 1;
            while (last > 0 && 
                   Character.isWhitespace(text.charAt(last))) {
                last--;
            }
            if (last < 0) {
                Completion.get().hideAll();
                return;
            }
            
            final Document d = component.getDocument();
            final ShellSession s = ShellSession.get(d);
            if (s == null) {
                Completion.get().hideAll();
                return;
            }
            final ConsoleModel mdl = s.getModel();
            ConsoleSection is = mdl.getInputSection();
            final int from = is.getPartBegin();
            final int l = last + 1;
            AtomicLockDocument ald = LineDocumentUtils.asRequired(d, AtomicLockDocument.class);
            ald.runAtomicAsUser(() -> {
                try {
                    d.remove(from, d.getLength() - from);
                    d.insertString(from, text.substring(0, l), null);
                } catch (BadLocationException ble) {
                    ble.printStackTrace();
                }
            });
            Completion.get().hideAll();
        }

        @Override
        public void processKeyEvent(KeyEvent evt) {
        }

        @Override
        public int getPreferredWidth(Graphics g, Font defaultFont) {
            return CompletionUtilities.getPreferredWidth(
                    getLeftText(), 
                    getRightText(), g, defaultFont);
        }

        @Override
        public void render(Graphics g, Font defaultFont, Color defaultColor, Color backgroundColor, int width, int height, boolean selected) {
            ImageIcon icon = ImageUtilities.loadImageIcon(
                    "org/netbeans/modules/jshell/resources/historyItem.png", true);
            CompletionUtilities.renderHtml(
                    icon, 
                    getLeftText(), 
                    getRightText(), 
                    g, 
                    defaultFont, 
                    defaultColor, 
                    width, 
                    height, 
                    selected);
        }

        @Override
        public CompletionTask createDocumentationTask() {
            return null;
        }

        @Override
        public CompletionTask createToolTipTask() {
            // probably show the whole completion item
            return null;
        }

        @Override
        public boolean instantSubstitution(JTextComponent component) {
            return false;
        }

        @Override
        public int getSortPriority() {
            return index;
        }

        @Override
        public CharSequence getSortText() {
            return getRightText();
        }

        @Override
        public CharSequence getInsertPrefix() {
            return "";
        }
        
    }
}
