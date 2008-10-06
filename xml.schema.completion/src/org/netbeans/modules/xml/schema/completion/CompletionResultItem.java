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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.xml.schema.completion;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import javax.swing.text.*;
import javax.swing.Icon;
import javax.xml.XMLConstants;
import org.netbeans.editor.BaseDocument;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.schema.completion.spi.CompletionContext;
import org.netbeans.modules.xml.schema.completion.util.CompletionContextImpl;
import org.netbeans.modules.xml.schema.completion.util.CompletionUtil;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;

/**
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public abstract class CompletionResultItem implements CompletionItem {

    /**
     * Creates a new instance of CompletionUtil
     */
    public CompletionResultItem(AXIComponent component, CompletionContext context) {
        this.context = (CompletionContextImpl)context;
        this.axiComponent = component;
        this.typedChars = context.getTypedChars();
    }

    Icon getIcon(){
        return icon;
    }

    public AXIComponent getAXIComponent() {
        return axiComponent;
    }

    /**
     * The completion item's name.
     */
    public String getItemText() {
        return itemText;
    }

    /**
     * The text user sees in the CC list. Normally some additional info
     * such as cardinality etc. are added to the item's name.
     * 
     */
    public abstract String getDisplayText();

    /**
     * Replacement text is the one that gets inserted into the document when
     * user selects this item from the CC list.
     */
    public abstract String getReplacementText();

    /**
     * Returns the relative caret position.
     * The caller must call this w.r.t. the offset
     * e.g. component.setCaretPosition(offset + getCaretPosition())
     */
    public abstract int getCaretPosition();

    public String toString() {
        return getItemText();
    }

    Color getPaintColor() { return Color.BLUE; }

    /**
     * Actually replaces a piece of document by passes text.
     * @param component a document source
     * @param text a string to be inserted
     * @param offset the target offset
     * @param len a length that should be removed before inserting text
     */
    private void replaceText(final JTextComponent component, final String text, final int offset, final int len) {
        final BaseDocument doc = (BaseDocument) component.getDocument();
        doc.runAtomic(new Runnable() {

            public void run() {
                try {
                    if (context.canReplace(text)) {
                        doc.remove(offset, len);
                        doc.insertString(offset, text, null);
                    }
                    //position the caret
                    component.setCaretPosition(offset + getCaretPosition());
                    String prefix = CompletionUtil.getPrefixFromTag(text);
                    if (prefix == null) {
                        return;
                    //insert namespace declaration for the new prefix
                    }
                    if (!context.isSpecialCompletion() && !context.isPrefixBeingUsed(prefix)) {
                        String tns = context.getTargetNamespaceByPrefix(prefix);
                        doc.insertString(CompletionUtil.getNamespaceInsertionOffset(doc), " " +
                                XMLConstants.XMLNS_ATTRIBUTE + ":" + prefix + "=\"" +
                                tns + "\"", null);
                    }
                } catch (BadLocationException exc) {
                    //shouldn't come here
                }
            }
        });
    }

    ////////////////////////////////////////////////////////////////////////////////
    ///////////////////methods from CompletionItem interface////////////////////////
    ////////////////////////////////////////////////////////////////////////////////
    public CompletionTask createDocumentationTask() {
        return new AsyncCompletionTask(new DocumentationQuery(this));
    }

    public CompletionTask createToolTipTask() {
        return new AsyncCompletionTask(new ToolTipQuery(this));
    }

    public void defaultAction(JTextComponent component) {
        int charsToRemove = (typedChars==null)?0:typedChars.length();
        int substOffset = component.getCaretPosition() - charsToRemove;
        if(!shift) Completion.get().hideAll();
        if(getReplacementText().equals(typedChars))
            return;
        replaceText(component, getReplacementText(), substOffset, charsToRemove);
    }

    public CharSequence getInsertPrefix() {
        return getItemText();
    }

    public abstract CompletionPaintComponent getPaintComponent();

    public int getPreferredWidth(Graphics g, Font defaultFont) {
        CompletionPaintComponent renderComponent = getPaintComponent();
        return renderComponent.getPreferredSize().width;
    //return getPaintComponent().getWidth(getItemText(), defaultFont);
    }

    public int getSortPriority() {
        return 0;
    }

    public CharSequence getSortText() {
        return getItemText();
    }

    public boolean instantSubstitution(JTextComponent component) {
        defaultAction(component);
        return true;
    }

    public void processKeyEvent(KeyEvent e) {
        shift = (e.getKeyCode() == KeyEvent.VK_ENTER && e.getID() == KeyEvent.KEY_PRESSED && e.isShiftDown());
    }

    public void render(Graphics g, Font defaultFont,
            Color defaultColor, Color backgroundColor,
            int width, int height, boolean selected) {
        CompletionPaintComponent renderComponent = getPaintComponent();
        renderComponent.setFont(defaultFont);
        renderComponent.setForeground(defaultColor);
        renderComponent.setBackground(backgroundColor);
        renderComponent.setBounds(0, 0, width, height);
        renderComponent.setSelected(selected);
        renderComponent.paintComponent(g);
    }
        
    protected boolean shift = false;
    protected String typedChars;
    protected String itemText;
    protected javax.swing.Icon icon;
    protected CompletionPaintComponent component;
    protected AXIComponent axiComponent;
    private CompletionContextImpl context;

    public static final String ICON_ELEMENT    = "element.png";     //NOI18N
    public static final String ICON_ATTRIBUTE  = "attribute.png";   //NOI18N
    public static final String ICON_VALUE      = "value.png";       //NOI18N
    public static final String ICON_LOCATION   = "/org/netbeans/modules/xml/schema/completion/resources/"; //NOI18N
}
