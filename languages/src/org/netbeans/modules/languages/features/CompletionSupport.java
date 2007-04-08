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

package org.netbeans.modules.languages.features;

import org.netbeans.api.languages.LanguageDefinitionNotFoundException;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;

import org.netbeans.api.editor.completion.Completion;
import org.netbeans.api.languages.CompletionItem;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.languages.Feature;
import org.netbeans.modules.languages.Language;
import org.netbeans.modules.languages.LanguagesManager;
import org.netbeans.api.languages.LanguageDefinitionNotFoundException;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.openide.ErrorManager;
import org.openide.util.Utilities;


/*
 * CompletionSupport.
 * 
 * @author Jan Jancura
 */


/*
 * CompletionSupport.
 * 
 * @author Jan Jancura
 */
public class CompletionSupport implements org.netbeans.spi.editor.completion.CompletionItem {
    
    private static Map<String,ImageIcon> icons = new HashMap<String,ImageIcon> ();
    
    private static ImageIcon getCIcon (String resourceName) {
        if (resourceName == null)
            resourceName = "org/netbeans/modules/languages/resources/node.gif";
        if (!icons.containsKey (resourceName)) {
            Image image = Utilities.loadImage (resourceName);
            if (image == null)
                image = Utilities.loadImage (
                    "org/netbeans/modules/languages/resources/node.gif"
                );
            icons.put (
                resourceName,
                new ImageIcon (image)
            );
        }
        return icons.get (resourceName);
    }

    private String      text;
    private String      description;
    private String      rightText;
    private String      icon;
    private int         priority;


    CompletionSupport (CompletionItem item) {
        text = item.getText ();
        rightText = item.getLibrary ();
        priority = item.getPriority ();
        
        String color = "000000";
        String type = item.getType ();
        boolean bold = false;
        String key = item.getText ();
        if ("keyword".equals (type)) {
            color = "000099";
            icon = "/org/netbeans/modules/languages/resources/keyword.jpg";
            bold = true;
        } else
        if ("interface".equals (type)) {
            color = "560000";
            icon = "/org/netbeans/modules/languages/resources/class.gif";
        } else
        if ("attribute".equals (type)) {
            icon = "/org/netbeans/modules/languages/resources/variable.gif";
        } else
        if ("function".equals (type)) {
            icon = "/org/netbeans/modules/languages/resources/method.gif";
            bold = true;
            key = key + "()";
        }

        if (item.getDescription () == null)
            description = 
                "<html>" + (bold ? "<b>" : "") + 
                "<font color=#" + color + ">" + key + 
                "</font>" + (bold ? "</b>" : "") + 
                "</html>";
        else
            description = 
                "<html>" + (bold ? "<b>" : "") + 
                "<font color=#" + color + ">" + key + 
                ": </font>" + (bold ? "</b>" : "") + 
                "<font color=#000000> " + 
                item.getDescription () + "</font></html>";
    }

    CompletionSupport (
        String text,
        String description,
        String rightText,
        String icon,
        int    priority
    ) {
        this.text = text;
        this.description = description;
        this.rightText = rightText;
        this.icon = icon;
        this.priority = priority;
    }

    public void defaultAction (JTextComponent component) {
        NbEditorDocument doc = (NbEditorDocument) component.getDocument ();
        int offset = component.getCaret ().getDot ();
        try {
            TokenHierarchy tokenHierarchy = TokenHierarchy.get (doc);
            TokenSequence sequence = tokenHierarchy.tokenSequence ();

            //find most embedded token sequence on the specified offset
            while(true) {
                sequence.move (offset - 1);
                sequence.moveNext ();
                TokenSequence embedded = sequence.embedded ();
                if (embedded == null) break;
                sequence = embedded;
            }
            Token token = sequence.token ();
            String tokenType = token.id ().name ();
            String mimeType = sequence.language ().mimeType ();
            Language l = LanguagesManager.getDefault ().getLanguage (mimeType);
            Feature feature = l.getFeature (Language.COMPLETION, tokenType);
            String t = text;
            String start = token.text().toString ();
            start = start.substring (0, offset - sequence.offset ()).trim ();
            int delta = feature != null && "true".equals(feature.getValue("doNotUsePrefix")) ? start.length() : 0;
            if (!l.getSkipTokenTypes ().contains (token.id ().name ()))
                t = text.substring (offset - sequence.offset () - delta);
            doc.insertString (offset, t, null);
        } catch (BadLocationException ex) {
            ErrorManager.getDefault ().notify (ex);
        } catch (LanguageDefinitionNotFoundException ex) {
            ErrorManager.getDefault ().notify (ex);
        }
        Completion.get ().hideAll ();
    }

    public void processKeyEvent (KeyEvent evt) {
    }

    public int getPreferredWidth (Graphics g, Font defaultFont) {
        return CompletionUtilities.getPreferredWidth (
            description, rightText, g, defaultFont
        );
    }

    public void render (
        Graphics g, 
        Font defaultFont, 
        Color defaultColor, 
        Color backgroundColor, 
        int width, int height, boolean selected
    ) {
        CompletionUtilities.renderHtml (
            getCIcon (icon), 
            description, 
            rightText, g, defaultFont, defaultColor, width, height, selected
        );
//            label.setText (selected ? highlightedDesc : description);
//            label.setForeground (defaultColor);
//            label.setBackground (backgroundColor);
//            label.setFont (defaultFont);
//            label.setIcon (getCIcon (icon));
//            label.setBounds (g.getClipBounds ());
//            label.paint (g);
    }

    public CompletionTask createDocumentationTask () {
        return null;
    }

    public CompletionTask createToolTipTask () {
        return null;
    }

    public boolean instantSubstitution (JTextComponent component) {
        return false;
    }

    public int getSortPriority () {
        return priority;
    }

    public CharSequence getSortText () {
        return text;
    }

    public CharSequence getInsertPrefix () {
        return text;
    }
}


