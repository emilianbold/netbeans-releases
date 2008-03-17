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

package org.netbeans.modules.gsfret.editor.completion;


import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.util.*;
import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.modules.gsf.api.CompletionProposal;
import org.netbeans.modules.gsf.api.ElementHandle;
import org.netbeans.modules.gsf.api.Modifier;
import org.netbeans.napi.gsfret.source.CompilationInfo;
import org.netbeans.editor.BaseDocument;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplateManager;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;

/**
 * Code completion items originating from the language plugin.
 *
 * Based on JavaCompletionItem by Dusan Balek.
 * 
 * @author Tor Norbye
 */
public abstract class GsfCompletionItem implements CompletionItem {
    /** Cache for looking up tip proposal - usually null (shortlived) */
    static org.netbeans.modules.gsf.api.CompletionProposal tipProposal;
    
    protected CompilationInfo info;
    
    protected static int SMART_TYPE = 1000;
        
    private static class DelegatedItem extends GsfCompletionItem {
        private org.netbeans.modules.gsf.api.CompletionProposal item;
        private static ImageIcon icon[][] = new ImageIcon[2][4];
        
        private DelegatedItem(CompilationInfo info, org.netbeans.modules.gsf.api.CompletionProposal item) {
            super(item.getAnchorOffset());
            this.item = item;
            this.info = info;
        }
        
        public int getSortPriority() {
            switch (item.getKind()) {
            case ERROR: return -5000;
            case DB: return item.isSmart() ? 155 - SMART_TYPE : 155;
            case PARAMETER: return item.isSmart() ? 105 - SMART_TYPE : 105;
            case CALL: return item.isSmart() ? 110 - SMART_TYPE : 110;
            case CONSTRUCTOR: return item.isSmart() ? 400 - SMART_TYPE : 400;
            case PACKAGE:
            case MODULE: return item.isSmart() ? 900 - SMART_TYPE : 900;
            case CLASS: return item.isSmart() ? 800 - SMART_TYPE : 800;
            case ATTRIBUTE:
            case TAG: return item.isSmart() ? 480 - SMART_TYPE : 480;
            case PROPERTY:
            case METHOD: return item.isSmart() ? 500 - SMART_TYPE : 500;
            case FIELD: return item.isSmart() ? 300 - SMART_TYPE : 300;
            case CONSTANT:
            case GLOBAL:
            case VARIABLE: return item.isSmart() ? 200 - SMART_TYPE : 200;
            case KEYWORD: return item.isSmart() ? 600 - SMART_TYPE : 600;
            case OTHER: 
            default: 
                return item.isSmart() ? 999 - SMART_TYPE : 999;
            }
        }

        @Override
        public boolean instantSubstitution(JTextComponent component) {
//            ElementKind kind = item.getKind();
//            return !(kind == ElementKind.CLASS || kind == ElementKind.MODULE);
            return false;
        }
        
        public CharSequence getSortText() {
            return item.getSortText();
        }

        public CharSequence getInsertPrefix() {
            return item.getInsertPrefix();
        }

        @Override
        protected String getLeftHtmlText() {
            return item.getLhsHtml();
        }

        @Override
        public String toString() {
            return item.getName();
        }

        @Override
        protected String getRightHtmlText() {
            String rhs = item.getRhsHtml();

            // Count text length on LHS
            String lhs = item.getLhsHtml();
            boolean inTag = false;
            int length = 0;
            for (int i = 0, n = lhs.length(); i < n; i++) {
                char c = lhs.charAt(i);
                if (inTag) {
                    if (c == '>') {
                        inTag = false;
                    }
                } else if (c == '<') {
                    inTag = true;
                } else {
                    length++;
                }
            }
            
            return truncateRhs(rhs, length);
        }
        
        @Override
        public CompletionTask createDocumentationTask() {
            final ElementHandle element = item.getElement();
            if (element != null) {
                return GsfCompletionProvider.createDocTask(element,info);
            }
            
            return null;
        }
        
        protected ImageIcon getIcon() {
            ImageIcon ic = item.getIcon();
            if (ic != null) {
                return ic;
            }
            
            ImageIcon icon = org.netbeans.modules.gsfret.navigation.Icons.getElementIcon(item.getKind(), item.getModifiers());
            // TODO - cache!
            return icon;
//                    
//
//            ElementKind kind = item.getKind();
//            switch (kind) {
//            case CONSTRUCTOR:
//            case METHOD:
//                return getMethodIcon();
//            case ATTRIBUTE:
//            case FIELD:
//                return getFieldIcon();
//            case CLASS:
//                return getClassIcon();
//            case MODULE:
//                return getModuleIcon();
//            case CONSTANT:
//                return getConstantIcon();
//            case VARIABLE:
//                return getVariableIcon();
//            case KEYWORD:
//                return getKeywordIcon();
//            case OTHER:
//            }
//            
//            return null;
        }
            
//        protected ImageIcon getMethodIcon() {
//            Set<Modifier> modifiers = item.getModifiers();
//
//            boolean isStatic = modifiers.contains(Modifier.STATIC);
////            int level = getProtectionLevel(elem.getModifiers());
//
//            ImageIcon cachedIcon = icon[isStatic?1:0][level];
//            if (cachedIcon != null) {
//                return cachedIcon;   
//            }
//
//            String iconPath = METHOD_PUBLIC;            
//            if (isStatic) {
//                switch (level) {
//                    case PRIVATE_LEVEL:
//                        iconPath = METHOD_ST_PRIVATE;
//                        break;
//
//                    case PACKAGE_LEVEL:
//                        iconPath = METHOD_ST_PACKAGE;
//                        break;
//
//                    case PROTECTED_LEVEL:
//                        iconPath = METHOD_ST_PROTECTED;
//                        break;
//
//                    case PUBLIC_LEVEL:
//                        iconPath = METHOD_ST_PUBLIC;
//                        break;
//                }
//            }else{
//                switch (level) {
//                    case PRIVATE_LEVEL:
//                        iconPath = METHOD_PRIVATE;
//                        break;
//
//                    case PACKAGE_LEVEL:
//                        iconPath = METHOD_PACKAGE;
//                        break;
//
//                    case PROTECTED_LEVEL:
//                        iconPath = METHOD_PROTECTED;
//                        break;
//
//                    case PUBLIC_LEVEL:
//                        iconPath = METHOD_PUBLIC;
//                        break;
//                }
//            }
//            ImageIcon newIcon = new ImageIcon(org.openide.util.Utilities.loadImage(iconPath));
//            icon[isStatic?1:0][level] = newIcon;
//            return newIcon;
//        }

    
        @Override
        protected void substituteText(final JTextComponent c, int offset, int len, String toAdd) {
            String template = item.getCustomInsertTemplate();
            if (template != null) {
                BaseDocument doc = (BaseDocument)c.getDocument();
                CodeTemplateManager ctm = CodeTemplateManager.get(doc);
                if (ctm != null) {
                    doc.atomicLock();
                    try {
                        doc.remove(offset, len);
                        c.getCaret().setDot(offset);
                    } catch (BadLocationException e) {
                        // Can't update
                    } finally {
                        doc.atomicUnlock();
                    }
                
                    ctm.createTemporary(template).insert(c);
                    // TODO - set the actual method to be used here so I don't have to 
                    // work quite as hard...
                    //tipProposal = item;
                    Completion.get().showToolTip();
                }
                
                return;
            }
            
            List<String> params = item.getInsertParams();
            if (params == null || params.size() == 0) {
                // TODO - call getParamListDelimiters here as well!
                super.substituteText(c, offset, len, toAdd);
                return;
            }

            super.substituteText(c, offset, len, toAdd);
            
            BaseDocument doc = (BaseDocument)c.getDocument();
            CodeTemplateManager ctm = CodeTemplateManager.get(doc);
            if (ctm != null) {
                StringBuilder sb = new StringBuilder();
                String[] delimiters = item.getParamListDelimiters();
                assert delimiters.length == 2;
                sb.append(delimiters[0]);
                int id = 1;
                for (Iterator<String> it = params.iterator(); it.hasNext();) {
                    String paramDesc = it.next();
                    sb.append("${"); //NOI18N
                    // Ensure that we don't use one of the "known" logical parameters
                    // such that a parameter like "path" gets replaced with the source file
                    // path!
                    sb.append("gsf-cc-"); // NOI18N
                    sb.append(Integer.toString(id++));
                    sb.append(" default=\""); // NOI18N
                    sb.append(paramDesc);
                    sb.append("\""); // NOI18N
                    sb.append("}"); //NOI18N
                    if (it.hasNext())
                        sb.append(", "); //NOI18N
                }
                sb.append(delimiters[1]);
                sb.append("${cursor}");
                ctm.createTemporary(sb.toString()).insert(c);
                // TODO - set the actual method to be used here so I don't have to 
                // work quite as hard...
                //tipProposal = item;
                Completion.get().showToolTip();
            }
            
        }        
    }
    

    public static final GsfCompletionItem createItem(CompletionProposal proposal, CompilationInfo info) {
        return new DelegatedItem(info, proposal);
    }

    public static final String COLOR_END = "</font>"; //NOI18N
    public static final String STRIKE = "<s>"; //NOI18N
    public static final String STRIKE_END = "</s>"; //NOI18N
    public static final String BOLD = "<b>"; //NOI18N
    public static final String BOLD_END = "</b>"; //NOI18N

    protected int substitutionOffset;
    
    private GsfCompletionItem(int substitutionOffset) {
        this.substitutionOffset = substitutionOffset;
    }
    
    public void defaultAction(JTextComponent component) {
        if (component != null) {
            // Items with no insert prefix and no custom code template
            // are "read-only" (such as the method call items)
            if (getInsertPrefix().length() == 0) {
                return;
            }
            Completion.get().hideAll();
            int caretOffset = component.getSelectionEnd();
            substituteText(component, substitutionOffset, caretOffset - substitutionOffset, null);
        }
    }

    public void processKeyEvent(KeyEvent evt) {
        if (evt.getID() == KeyEvent.KEY_TYPED) {
            switch (evt.getKeyChar()) {
                case ';':
                case ',':
                case '(':
                case '.':
                case '\n':
                    Completion.get().hideAll();
//                case '.':
//                    JTextComponent component = (JTextComponent)evt.getSource();
//                    int caretOffset = component.getSelectionEnd();
//                    substituteText(component, substitutionOffset, caretOffset - substitutionOffset, Character.toString(evt.getKeyChar()));
//                    evt.consume();
//                    break;
            }
        }
    }

    public boolean instantSubstitution(JTextComponent component) {
        defaultAction(component);
        return true;
    }
    
    public CompletionTask createDocumentationTask() {
        return null;
    }
    
    public CompletionTask createToolTipTask() {
        return null;
    }
    
    public int getPreferredWidth(Graphics g, Font defaultFont) {
        return CompletionUtilities.getPreferredWidth(getLeftHtmlText(), getRightHtmlText(), g, defaultFont);
    }
    
    public void render(Graphics g, Font defaultFont, Color defaultColor, Color backgroundColor, int width, int height, boolean selected) {
        CompletionUtilities.renderHtml(getIcon(), getLeftHtmlText(), getRightHtmlText(), g, defaultFont, defaultColor, width, height, selected);
    }

    protected abstract ImageIcon getIcon();
    
    protected String getLeftHtmlText() {
        return null;
    }
    
    protected String getRightHtmlText() {
        return null;
    }

    protected void substituteText(JTextComponent c, int offset, int len, String toAdd) {
        BaseDocument doc = (BaseDocument)c.getDocument();
        String text = getInsertPrefix().toString();
        if (text != null) {
            //int semiPos = toAdd != null && toAdd.endsWith(";") ? findPositionForSemicolon(c) : -2; //NOI18N
            //if (semiPos > -2)
            //    toAdd = toAdd.length() > 1 ? toAdd.substring(0, toAdd.length() - 1) : null;
            //if (toAdd != null && !toAdd.equals("\n")) {//NOI18N
            //    TokenSequence<JavaTokenId> sequence = Utilities.getJavaTokenSequence(c, offset + len);
            //    if (sequence == null) {
            //        text += toAdd;
            //        toAdd = null;
            //    }
            //    boolean added = false;
            //    while(toAdd != null && toAdd.length() > 0) {
            //        String tokenText = sequence.token().text().toString();
            //        if (tokenText.startsWith(toAdd)) {
            //            len = sequence.offset() - offset + toAdd.length();
            //            text += toAdd;
            //            toAdd = null;
            //        } else if (toAdd.startsWith(tokenText)) {
            //            sequence.moveNext();
            //            len = sequence.offset() - offset;
            //            text += toAdd.substring(0, tokenText.length());
            //            toAdd = toAdd.substring(tokenText.length());
            //            added = true;
            //        } else if (sequence.token().id() == JavaTokenId.WHITESPACE && sequence.token().text().toString().indexOf('\n') < 0) {//NOI18N
            //            if (!sequence.moveNext()) {
            //                text += toAdd;
            //                toAdd = null;
            //            }
            //        } else {
            //            if (!added)
            //                text += toAdd;
            //            toAdd = null;
            //        }
            //    }
            //}
        
            int semiPos = -2;
            //  Update the text
            doc.atomicLock();
            try {
                String textToReplace = doc.getText(offset, len);
                if (text.equals(textToReplace)) {
                    if (semiPos > -1)
                        doc.insertString(semiPos, ";", null); //NOI18N
                    return;
                }                
                Position position = doc.createPosition(offset);
                Position semiPosition = semiPos > -1 ? doc.createPosition(semiPos) : null;
                doc.remove(offset, len);
                doc.insertString(position.getOffset(), text, null);
                if (semiPosition != null)
                    doc.insertString(semiPosition.getOffset(), ";", null);
            } catch (BadLocationException e) {
                // Can't update
            } finally {
                doc.atomicUnlock();
            }
        }
    }

    private static String truncateRhs(String rhs, int left) {
        if (rhs != null) {
            final int MAX_SIZE = 80;
            int size = MAX_SIZE-left;
            if (size < 10) {
                size = 10;
            }
            if (rhs != null && rhs.length() > size) {
                rhs = rhs.substring(0,size-3) + "<b>&gt;</b>";  // Add a ">" to indicate truncation
            }
        }
        return rhs;
    }

    
    // TODO: KeywordItem has a postfix:
//    private static class KeywordItem extends GsfCompletionItem {
//        private String postfix;
//        private KeywordItem(ComKeyword keyword, String postfix, int substitutionOffset) {
//            this.postfix = postfix;
//        }
//        protected void substituteText(JTextComponent c, int offset, int len, String toAdd) {
//            super.substituteText(c, offset, len, toAdd != null ? toAdd : postfix);
//        }
    
    

    private static final int PUBLIC_LEVEL = 3;
    private static final int PROTECTED_LEVEL = 2;
    private static final int PACKAGE_LEVEL = 1;
    private static final int PRIVATE_LEVEL = 0;
    
    private static int getProtectionLevel(Set<Modifier> modifiers) {
        if(modifiers.contains(Modifier.PUBLIC))
            return PUBLIC_LEVEL;
        if(modifiers.contains(Modifier.PROTECTED))
            return PROTECTED_LEVEL;
        if(modifiers.contains(Modifier.PRIVATE))
            return PRIVATE_LEVEL;
        return PACKAGE_LEVEL;
    }    
}
