/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.requirejs.editor;

import java.beans.BeanInfo;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;

/**
 *
 * @author ppisl
 */
public class FSCompletionItem implements CompletionProposal {

    private final FileObject file;
    private final ImageIcon icon;
    private final int anchor;
    private final String prefix;
    private final FSElementHandle element;
    
    public FSCompletionItem(final FileObject file, final String prefix, final int anchor) throws IOException {
        this.file = file;
        this.element = new FSElementHandle(file);
        DataObject od = DataObject.find(file);

        icon = new ImageIcon(od.getNodeDelegate().getIcon(BeanInfo.ICON_COLOR_16x16));

        this.anchor = anchor;

        this.prefix = prefix;
    }

    private void doSubstitute(final JTextComponent component, String toAdd, final int backOffset) {
        final BaseDocument doc = (BaseDocument) component.getDocument();
        final int caretOffset = component.getCaretPosition();
        final String value = getText() + (toAdd != null && (!toAdd.equals("/") || (toAdd.equals("/") && !getText().endsWith(toAdd))) ? toAdd : ""); //NOI18N
        // Update the text
        doc.runAtomic(new Runnable() {
            @Override
            public void run() {
                try {
                    String pfx = doc.getText(anchor, caretOffset - anchor);
                    doc.remove(caretOffset - pfx.length(), pfx.length());
                    doc.insertString(caretOffset - pfx.length(), value, null);
                    component.setCaretPosition(component.getCaretPosition() - backOffset);
                } catch (BadLocationException e) {
                    Exceptions.printStackTrace(e);
                }
            }
        });
    }

//        @Override
//        public void defaultAction(JTextComponent component) {
//            doSubstitute(component, null, 0);
//            if (!file.isFolder()) {
//                Completion.get().hideAll();
//            }
//        }
//        @Override
//        public void processKeyEvent(KeyEvent evt) {
//            if (evt.getID() == KeyEvent.KEY_TYPED) {
//                String strToAdd = "/";
//                if (evt.getKeyChar() == '/') {
//                    doSubstitute((JTextComponent) evt.getSource(), strToAdd, strToAdd.length() - 1);
//                    evt.consume();
//                }
//            }
//        }
//        @Override
//        public int getPreferredWidth(Graphics g, Font defaultFont) {
//            return CompletionUtilities.getPreferredWidth(file.getNameExt(), null, g, defaultFont);
//        }
//        @Override
//        public void render(Graphics g, Font defaultFont, Color defaultColor, Color backgroundColor, int width, int height, boolean selected) {
//            CompletionUtilities.renderHtml(icon, file.getNameExt(), null, g, defaultFont, defaultColor, width, height, selected);
//        }
//        @Override
//        public CompletionTask createDocumentationTask() {
//            return null;
//        }
//        @Override
//        public CompletionTask createToolTipTask() {
//            return null;
//        }
//        @Override
//        public boolean instantSubstitution(JTextComponent component) {
//            return false; //????
//        }
//        @Override
//        public int getSortPriority() {
//            return -1000;
//        }
//        @Override
//        public CharSequence getSortText() {
//            return getText();
//        }
    protected String getText() {
        return prefix + file.getNameExt() + (file.isFolder() ? "/" : "");
    }

    @Override
    public int hashCode() {
        return getText().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof FSCompletionItem)) {
            return false;
        }

        FSCompletionItem remote = (FSCompletionItem) o;

        return getText().equals(remote.getText());
    }

    @Override
    public ImageIcon getIcon() {
        return icon;
    }

    @Override
    public int getAnchorOffset() {
        return this.anchor;
    }

    @Override
    public ElementHandle getElement() {
        return this.element;
    }

    @Override
    public String getName() {
        return element.getName();
    }

    @Override
    public String getInsertPrefix() {
        return getName() + (file.isFolder() ? "/" : "");
    }

    @Override
    public String getSortText() {
        return getName();
    }

    @Override
    public String getLhsHtml(HtmlFormatter formatter) {
        return file.getNameExt() + " "; //NOI18N
    }

    @Override
    public String getRhsHtml(HtmlFormatter formatter) {
        return "";
    }

    @Override
    public ElementKind getKind() {
        return element.getKind();
    }

    @Override
    public Set<Modifier> getModifiers() {
        return Collections.emptySet();
    }

    @Override
    public boolean isSmart() {
        return false;
    }

    @Override
    public int getSortPrioOverride() {
        return -1000;
    }

    @Override
    public String getCustomInsertTemplate() {
        return null;
    }
    
    public static class FSElementHandle implements ElementHandle {
        
        private final FileObject fo;

        public FSElementHandle(FileObject fo) {
            this.fo = fo;
        }

        
        @Override
        public FileObject getFileObject() {
            return fo;
        }

        @Override
        public String getMimeType() {
            return fo.getMIMEType();
        }

        @Override
        public String getName() {
            return fo.isFolder() ? fo.getNameExt() : fo.getName();
        }

        @Override
        public String getIn() {
            return null;
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.FILE;
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Collections.EMPTY_SET;
        }

        @Override
        public boolean signatureEquals(ElementHandle handle) {
            return fo.equals(handle.getFileObject());
        }

        @Override
        public OffsetRange getOffsetRange(ParserResult result) {
            return OffsetRange.NONE;
        }
        
    }

}
