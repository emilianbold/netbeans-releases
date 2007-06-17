/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.lib.editor.codetemplates;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplate;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.spi.editor.completion.CompletionDocumentation;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Code template completion result item.
 *
 * @author Miloslav Metelka
 */
public final class CodeTemplateCompletionItem implements CompletionItem {
    
    private static ImageIcon icon;
    
    private final CodeTemplate codeTemplate;
    
    private String rightText;
    
    public CodeTemplateCompletionItem(CodeTemplate codeTemplate) {
        this.codeTemplate = codeTemplate;
    }
    
    private String getLeftText() {
        return CodeTemplateApiPackageAccessor.get().getSingleLineText(codeTemplate);
    }
    
    private String getRightText() {
        if (rightText == null) {
            rightText = ParametrizedTextParser.toHtmlText(codeTemplate.getAbbreviation());
        }
        return rightText;
    }
    
    public int getPreferredWidth(Graphics g, Font defaultFont) {
        return CompletionUtilities.getPreferredWidth(getLeftText(), getRightText(),
                g, defaultFont);
    }

    public void render(Graphics g, Font defaultFont, Color defaultColor,
    Color backgroundColor, int width, int height, boolean selected) {
        
        if (icon == null) {
            icon = new ImageIcon(Utilities.loadImage(
                "org/netbeans/lib/editor/codetemplates/resources/code_template.png")); // NOI18N
        }
        CompletionUtilities.renderHtml(icon, getLeftText(), getRightText(),
                g, defaultFont, defaultColor, width, height, selected);
    }

    public void defaultAction(JTextComponent component) {
        Completion.get().hideAll();
        // Remove the typed part
        Document doc = component.getDocument();
        int caretOffset = component.getSelectionStart();
        int initMatchLen = getInitialMatchLength(doc, caretOffset, codeTemplate.getParametrizedText());
        if (initMatchLen > 0) {
            try {
                // Remove the typed prefix
                doc.remove(caretOffset - initMatchLen, initMatchLen);
            } catch (BadLocationException ble) {
            }
        }
        codeTemplate.insert(component);
    }
    
    public void processKeyEvent(KeyEvent evt) {
    }
    
    public boolean instantSubstitution(JTextComponent component) {
        // defaultAction(component);
        return false;
    }
    
    public static int getInitialMatchLength(Document doc, int caretOffset, String text) {
        int matchLength = Math.min(text.length(), caretOffset);
        CharSequence docText = DocumentUtilities.getText((org.netbeans.editor.BaseDocument)doc);
        while (matchLength > 0) {
            int i;
            for (i = 1; i < matchLength; i++) {
                if (docText.charAt(caretOffset - i) != text.charAt(matchLength - i)) {
                    break;
                }
            }
            if (i == matchLength) {
                break;
            }
            matchLength--;
        }
        return matchLength;
    }
    
    public CompletionTask createDocumentationTask() {
        return new AsyncCompletionTask(new DocQuery(codeTemplate));
    }
    
    public CompletionTask createToolTipTask() {
        return null;
    }

    public int getSortPriority() {
        return 650;
    }        
    
    public CharSequence getSortText() {
        return ""; //NOI18N
    }

    public CharSequence getInsertPrefix() {
        String insertPrefix = codeTemplate.getParametrizedText();
        int dollarIndex = insertPrefix.indexOf("${"); // NOI18N
        if (dollarIndex >= 0) {
            insertPrefix = insertPrefix.substring(0, dollarIndex);
        }
        return insertPrefix;
    }

    private static final class DocQuery extends AsyncCompletionQuery {
        
        private CodeTemplate codeTemplate;
        
        DocQuery(CodeTemplate codeTemplate) {
            this.codeTemplate = codeTemplate;
        }

        protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
            StringBuffer sb = new StringBuffer(); // NOI18N

            sb.append("<html><pre>"); //NOI18N
            ParametrizedTextParser.parseToHtml(sb, codeTemplate.getParametrizedText());
            sb.append("</pre>"); // NOI18N

            String desc = codeTemplate.getDescription();
            if (desc != null && desc.length() > 0) {
                sb.append("<p>").append(desc).append("</p>"); //NOI18N
            }
            
            // Append abbreviation
            CodeTemplateManagerOperation operation = CodeTemplateApiPackageAccessor.get().getOperation(codeTemplate);
            sb.append("<p>"); //NOI18N
            sb.append(NbBundle.getMessage(CodeTemplateCompletionItem.class, 
                "DOC_ITEM_Abbreviation", //NOI18N
                ParametrizedTextParser.toHtmlText(codeTemplate.getAbbreviation()), 
                operation.getExpandKeyStrokeText()
            ));
            sb.append("<p>"); //NOI18N
            
            resultSet.setDocumentation(new DocItem(sb.toString()));
            resultSet.finish();
        }
        
    }
    
    private static final class DocItem implements CompletionDocumentation {
        
        private String text;
        
        DocItem(String text) {
            this.text = text;
        }
        
        public String getText() {
            return text;
        }
        
        public CompletionDocumentation resolveLink(String link) {
            return null;
        }

        public java.net.URL getURL() {
            return null;
        }


        public javax.swing.Action getGotoSourceAction() {
            return null;
        }
    } // End of DocItem class

}
