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

package org.netbeans.modules.xslt.core.text.completion;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.openide.text.NbDocument;

/**
 * @author Alex Petrov (30.04.2008)
 */
public class XSLTCompletionResultItem implements CompletionItem, Runnable {
    private static final Logger _Logger = 
        Logger.getLogger(XSLTCompletionResultItem.class.getName());
    private static final Color ITEM_TEXT_COLOR = Color.decode("0x0000B2");
    
    private String itemText;
    private Document document;
    private int caretOffset;
    private JTextComponent textComponent;

    public XSLTCompletionResultItem(String itemText, Document document, int caretOffset) {
        this.itemText = itemText;
        this.document = document;
        this.caretOffset = caretOffset;
    }
    
    private void doSubstitute(JTextComponent component) {
        StyledDocument styledDocument = (StyledDocument) component.getDocument();
        this.textComponent = component;
        this.document = styledDocument;
        try {
            NbDocument.runAtomicAsUser(styledDocument, this);
        } catch (BadLocationException ex) {
            _Logger.log(Level.WARNING, null, ex);
        }
    }

    public void run() {
        if ((document == null) || (textComponent == null)) return;
        
        String insertedText = getText();
        if (insertedText == null) return;
        try {
            StyledDocument styledDocument = (StyledDocument) document;
            //styledDocument.remove(caretOffset, 0);
            styledDocument.insertString(caretOffset, insertedText, null);
            textComponent.setCaretPosition(textComponent.getCaretPosition());
        } catch (BadLocationException e) {
            _Logger.log(Level.WARNING, null, e);
        }
    }
    
    public void defaultAction(JTextComponent component) {
        this.textComponent = component;
        doSubstitute(component);
        Completion.get().hideAll();
    }
    
    public void processKeyEvent(KeyEvent evt) {}
    
    public int getPreferredWidth(Graphics g, Font defaultFont) {
        return CompletionUtilities.getPreferredWidth(
        itemText, null, g, defaultFont);
    }
    
    public void render(Graphics g, Font defaultFont, Color defaultColor, 
        Color backgroundColor, int width, int height, boolean selected) {
        CompletionUtilities.renderHtml(null, itemText, null, g, defaultFont, 
            (selected ? Color.white : ITEM_TEXT_COLOR), width, height, selected);
    }
    
    public CompletionTask createDocumentationTask() {return null;}
    
    public CompletionTask createToolTipTask() {return null;}
    
    public boolean instantSubstitution(JTextComponent component) {return true;}
    
    public int getSortPriority() {return 0;}
    
    public CharSequence getSortText() {return getText();}
    
    public CharSequence getInsertPrefix() {return getText();}
    
    public String getText() {return itemText;}    
}