/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.lib.editor.codetemplates;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplate;
import org.netbeans.spi.editor.completion.CompletionDocumentation;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.openide.ErrorManager;
import org.openide.util.Utilities;

/**
 * Code template completion result item.
 *
 * @author Miloslav Metelka
 */
public final class CodeTemplateCompletionItem implements CompletionItem {
    
    private static ImageIcon icon;
    
    private CodeTemplate codeTemplate;
    
    private String leftText;
    
    public static String toHtmlText(String text) {
        StringBuffer htmlText = null;
        for (int i = 0; i < text.length(); i++) {
            String rep; // replacement string
            char ch = text.charAt(i);
            switch (ch) {
                case '<':
                    rep = "&lt;";
                    break;
                case '>':
                    rep = "&gt;";
                    break;
                case '\n':
                    rep = "<br>";
                    break;
                default:
                    rep = null;
                    break;
            }

            if (rep != null) {
                if (htmlText == null) {
                    // Expect 20% of text to be html tags text
                    htmlText = new StringBuffer(120 * text.length() / 100);
                    if (i > 0) {
                        htmlText.append(text.substring(0, i));
                    }
                }
                htmlText.append(rep);

            } else { // no replacement
                if (htmlText != null) {
                    htmlText.append(ch);
                }
            }
        }
        return (htmlText != null) ? htmlText.toString() : text;
    }
    
    public CodeTemplateCompletionItem(CodeTemplate codeTemplate) {
        this.codeTemplate = codeTemplate;
    }
    
    private String getLeftText() {
        // Temporarily - return just the description - already in html
        // return toHtmlText(codeTemplate.getDescription());
        return codeTemplate.getDescription();
    }
    
    private String getRightText() {
        if (leftText == null) {
            leftText = toHtmlText(codeTemplate.getAbbreviation());
        }
        return leftText;
    }
    
    public int getPreferredWidth(Graphics g, Font defaultFont) {
        return CompletionUtilities.getPreferredWidth(getLeftText(), getRightText(),
                g, defaultFont);
    }

    public void render(Graphics g, Font defaultFont, Color defaultColor,
    Color backgroundColor, int width, int height, boolean selected) {
        
        if (icon == null) {
            icon = new ImageIcon(Utilities.loadImage(
                "org/netbeans/lib/editor/codetemplates/resources/code_template.png"));
        }
        CompletionUtilities.renderHtml(icon, getLeftText(), getRightText(),
                g, defaultFont, defaultColor, width, height, selected);
    }

    public void defaultAction(JTextComponent component) {
        // Remove the typed part
        Document doc = component.getDocument();
        int caretOffset = component.getCaretPosition();
        int initMatchLen = getInitialMatchLength(doc, caretOffset, codeTemplate.getAbbreviation());
        if (initMatchLen > 0) {
            // Select the typed prefix so that it gets removed
            //   by code template insertion
            // Caret position should correspond to the insertion point
            //   so move dot to lower position
            component.setCaretPosition(caretOffset);
            component.moveCaretPosition(caretOffset - initMatchLen);
        }

        codeTemplate.insert(component);
        Completion.get().hideAll();
    }
    
    public void processKeyEvent(KeyEvent evt) {
    }
    
    public boolean instantSubstitution(JTextComponent component) {
        // defaultAction(component);
        return false;
    }
    
    public static int getInitialMatchLength(Document doc, int caretOffset, String text) {
        int matchLength = Math.min(text.length(), caretOffset);
        org.netbeans.editor.CharSeq docText = ((org.netbeans.editor.BaseDocument)doc).getText();
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
        return 50;
    }        
    
    public CharSequence getSortText() {
        return "";
    }

    private static final class DocQuery extends AsyncCompletionQuery {
        
        private CodeTemplate codeTemplate;
        
        DocQuery(CodeTemplate codeTemplate) {
            this.codeTemplate = codeTemplate;
        }

        protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
            resultSet.setDocumentation(new DocItem(codeTemplate));
            resultSet.finish();
        }
        
    }
    
    private static final class DocItem implements CompletionDocumentation {
        
        private CodeTemplate codeTemplate;
        
        private String text;
        
        DocItem(CodeTemplate codeTemplate) {
            this.codeTemplate = codeTemplate;
            text = createText();
        }
        
        public String getText() {
            return text;
        }
        
        private String createText() {
            // Parametrized text - parsed; parameters in bold
            StringBuffer htmlText = new StringBuffer("<html><pre>");
            ParametrizedTextParser parser = new ParametrizedTextParser(null, codeTemplate.getParametrizedText());
            parser.parse();
            parser.appendHtmlText(htmlText);
            htmlText.append("</pre>");

            // Append abbreviation
            htmlText.append("<br>Abbreviation: &nbsp;");
            htmlText.append(toHtmlText(codeTemplate.getAbbreviation()));
            htmlText.append("</html>");
            return htmlText.toString();
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
        
    }

}
