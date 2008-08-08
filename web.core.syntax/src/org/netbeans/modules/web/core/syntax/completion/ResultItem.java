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

package org.netbeans.modules.web.core.syntax.completion;

import java.awt.Font;
import java.awt.Graphics;
import java.net.URL;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.ext.CompletionQuery;
import org.netbeans.spi.editor.completion.*;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.KeyEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.*;


/**
 * Code completion result item base class
 *
 * @author  Dusan Balek, Marek Fukala
 */

public abstract class ResultItem implements CompletionQuery.ResultItem, CompletionItem {
    
    static String toAdd;
    public static final String COMPLETION_SUBSTITUTE_TEXT= "completion-substitute-text"; //NOI18N
    
    protected int selectionStartOffset = -1;
    protected int selectionEndOffset = -1;
    private int substituteOffset = -1; //stores the substituteOffset
    
    protected boolean shift = false;
    
    public int getSubstituteOffset() {
        return substituteOffset;
    }
    
    public void setSubstituteOffset(int offset) {
        this.substituteOffset = offset;
    }
    
    public abstract String getItemText();
    
    public abstract Component getPaintComponent(boolean isSelected);
    
    public abstract int getSortPriority();
    
    public CharSequence getSortText() {
        return getItemText();
    }
    
    public void processKeyEvent(KeyEvent e) {
        shift = (e.getKeyCode() == KeyEvent.VK_ENTER && e.getID() == KeyEvent.KEY_PRESSED && e.isShiftDown());
    }
    
    public boolean substituteCommonText(JTextComponent c, int offset, int len, int subLen) {
        // [PENDING] not enough info in parameters...
        // commonText
        // substituteExp
        return false;
    }
    
    //afaik called only from abbrevs result item
    public boolean substituteText(final JTextComponent c, final int offset, final int len, final boolean shift) {
        final BaseDocument doc = (BaseDocument) c.getDocument();
        String text = getItemText();

        if (text == null) {
            return false;
        }

        if (toAdd != null && !toAdd.equals("\n")) { //NOI18N
            text += toAdd;
        }

        final String text2 = text;
        final boolean[] retval = new boolean[1];
        retval[0] = true;
        
        // Update the text
        doc.runAtomic(new Runnable() {

            public void run() {
                try {
                    String textToReplace = doc.getText(offset, len);
                    if (text2.equals(textToReplace)) {
                        retval[0] = false;
                    }

                    doc.remove(offset, len);
                    doc.insertString(offset, text2, null);
                    if (selectionStartOffset >= 0) {
                        c.select(offset + selectionStartOffset,
                                offset + selectionEndOffset);
                    }
                } catch (BadLocationException ex) {
                    retval[0] = false;
                }

            }
        });

        return retval[0];

    }
    
    public Component getPaintComponent(javax.swing.JList list, boolean isSelected, boolean cellHasFocus) {
        Component ret = getPaintComponent(isSelected);
        if (ret==null) return null;
        if (isSelected) {
            ret.setBackground(list.getSelectionBackground());
            ret.setForeground(list.getSelectionForeground());
        } else {
            ret.setBackground(list.getBackground());
            ret.setForeground(list.getForeground());
        }
        ret.getAccessibleContext().setAccessibleName(getItemText());
        ret.getAccessibleContext().setAccessibleDescription(getItemText());
        return ret;
    }
    
    public int getPreferredWidth(Graphics g, Font defaultFont) {
        ResultItemPaintComponent renderComponent = (ResultItemPaintComponent)getPaintComponent(false);
        return renderComponent.getPreferredWidth(g, defaultFont);
    }
    
    public void render(Graphics g, Font defaultFont, Color defaultColor,
            Color backgroundColor, int width, int height, boolean selected) {
        Component renderComponent = getPaintComponent(selected);
        renderComponent.setFont(defaultFont);
        renderComponent.setForeground(defaultColor);
        renderComponent.setBackground(backgroundColor);
        renderComponent.setBounds(0, 0, width, height);
        ((ResultItemPaintComponent)renderComponent).paintComponent(g);
    }
    
    public boolean instantSubstitution(JTextComponent c) {
        defaultAction(c);
        return true;
    }
    
    public CompletionTask createDocumentationTask() {
        return new AsyncCompletionTask(new JspCompletionProvider.DocQuery(this));
    }
    
    public abstract URL getHelpURL();
    
    /** Returns help for the item. It can be only url. If the item doesn't have a help
     *  than returns null. The class can overwrite this method and compounds the help realtime.
     */
    public abstract String getHelp();
    
    /** Returns whether the item has a help. */
    public abstract boolean hasHelp();
    
    public CompletionTask createToolTipTask() {
        return null;
    }
    
    public int getImportance() {
        return 0;
    }
    
    public void defaultAction(JTextComponent component) {
        int substOffset = getSubstituteOffset();
        if (substOffset == -1)
            substOffset = component.getCaret().getDot();
        
        if(!shift) Completion.get().hideAll();
        substituteText(component, substOffset, component.getCaret().getDot() - substOffset, shift);
    }
    
}
