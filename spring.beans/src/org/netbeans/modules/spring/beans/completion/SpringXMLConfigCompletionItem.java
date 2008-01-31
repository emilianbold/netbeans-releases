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

package org.netbeans.modules.spring.beans.completion;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.beans.BeanInfo;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.UIManager;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.editor.BaseDocument;
import org.netbeans.spi.editor.completion.CompletionDocumentation;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.nodes.Node;
import org.openide.util.Utilities;

/**
 * A completion item shown in a valid code completion request 
 * in a Spring XML Configuration file
 * 
 * @author Rohan Ranade (Rohan.Ranade@Sun.COM)
 */
public abstract class SpringXMLConfigCompletionItem implements CompletionItem {

    public static SpringXMLConfigCompletionItem createAttribValueItem(int substitutionOffset, String displayText, String docText) {
        return new AttribValueItem(substitutionOffset, displayText, docText);
    }
    
    public static SpringXMLConfigCompletionItem createFolderItem(int substitutionOffset, FileObject folder) {
        return new FolderItem(substitutionOffset, folder);
    }
    
    public static SpringXMLConfigCompletionItem createSpringXMLFileItem(int substitutionOffset, FileObject file) {
        return new FileItem(substitutionOffset, file);
    }
    
    protected int substitutionOffset;
    
    protected SpringXMLConfigCompletionItem(int substitutionOffset) {
        this.substitutionOffset = substitutionOffset;
    }
    
    public void defaultAction(JTextComponent component) {
        if (component != null) {
            Completion.get().hideDocumentation();
            Completion.get().hideCompletion();
            int caretOffset = component.getSelectionEnd();
            substituteText(component, substitutionOffset, caretOffset - substitutionOffset, null);
        }
    }
    
    protected void substituteText(JTextComponent c, int offset, int len, String toAdd) {
        BaseDocument doc = (BaseDocument) c.getDocument();
        CharSequence prefix = getInsertPrefix();
        String text = prefix.toString();
        
        doc.atomicLock();
        try {
            Position position = doc.createPosition(offset);
            doc.remove(offset, len);
            doc.insertString(position.getOffset(), text.toString(), null);
        } catch (BadLocationException ble) {
            // nothing can be done to update
        } finally {
            doc.atomicUnlock();
        }
    }

    public void processKeyEvent(KeyEvent evt) {
        
    }

    public int getPreferredWidth(Graphics g, Font defaultFont) {
        return CompletionUtilities.getPreferredWidth(getLeftHtmlText(), 
                getRightHtmlText(), g, defaultFont);
    }

    public void render(Graphics g, Font defaultFont, Color defaultColor, 
            Color backgroundColor, int width, int height, boolean selected) {
        CompletionUtilities.renderHtml(getIcon(), getLeftHtmlText(), 
                getRightHtmlText(), g, defaultFont, defaultColor, width, height, selected);
    }

    public CompletionTask createDocumentationTask() {
        return null;
    }

    public CompletionTask createToolTipTask() {
        return null;
    }

    public boolean instantSubstitution(JTextComponent component) {
        defaultAction(component);
        return true;
    }
    
    protected String getLeftHtmlText() {
        return null;
    }
    
    protected String getRightHtmlText() {
        return null;
    }
    
    protected ImageIcon getIcon() {
        return null;
    }
    
    private static class AttribValueItem extends SpringXMLConfigCompletionItem {

        private String displayText;
        private String docText;
        
        public AttribValueItem(int substitutionOffset, String displayText, String docText) {
            super(substitutionOffset);
            this.displayText = displayText;
            this.docText = docText;
        }

        public int getSortPriority() {
            return 50;
        }

        public CharSequence getSortText() {
            return displayText;
        }

        public CharSequence getInsertPrefix() {
            return displayText;
        }

        @Override
        protected String getLeftHtmlText() {
            return displayText;
        }

        @Override
        public CompletionTask createDocumentationTask() {
            return new AsyncCompletionTask(new AsyncCompletionQuery() {
                @Override
                protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
                    if(docText != null) {
                        CompletionDocumentation documentation = SpringXMLConfigCompletionDoc.getAttribValueDoc(docText);
                        resultSet.setDocumentation(documentation);
                    }
                    resultSet.finish();
                }
            });
        }        
    }
    
    private static class FolderItem extends SpringXMLConfigCompletionItem {

        private FileObject folder;
        
        public FolderItem(int substitutionOffset, FileObject folder) {
            super(substitutionOffset);
            this.folder = folder;
        }

        @Override
        public void processKeyEvent(KeyEvent evt) {
            if (evt.getID() == KeyEvent.KEY_TYPED) {
                if(evt.getKeyChar() == '/') {
                    Completion.get().hideDocumentation();
                    JTextComponent component = (JTextComponent)evt.getSource();
                    int caretOffset = component.getSelectionEnd();
                    substituteText(component, substitutionOffset, caretOffset - substitutionOffset, Character.toString(evt.getKeyChar()));
                    Completion.get().showCompletion();
                    evt.consume();
                }
            }
        }
        
        public int getSortPriority() {
            return 300;
        }

        public CharSequence getSortText() {
            return folder.getName();
        }

        public CharSequence getInsertPrefix() {
            return folder.getName();
        }

        @Override
        public boolean instantSubstitution(JTextComponent component) {
            return false;
        }
        
        @Override
        protected ImageIcon getIcon() {
            return new ImageIcon(getTreeFolderIcon());
        }

        @Override
        protected String getLeftHtmlText() {
            return folder.getName();
        }
        
        private static final String ICON_KEY_UIMANAGER = "Tree.closedIcon"; // NOI18N
        private static final String ICON_KEY_UIMANAGER_NB = "Nb.Explorer.Folder.icon"; // NOI18N
        
        /**
         * Returns default folder icon as {@link java.awt.Image}. Never returns
         * <code>null</code>.Adapted from J2SELogicalViewProvider
         */
        private static Image getTreeFolderIcon() {
            Image base = null;
            Icon baseIcon = UIManager.getIcon(ICON_KEY_UIMANAGER); // #70263
            if (baseIcon != null) {
                base = Utilities.icon2Image(baseIcon);
            } else {
                base = (Image) UIManager.get(ICON_KEY_UIMANAGER_NB); // #70263
                if (base == null) { // fallback to our owns                
                    final Node n = DataFolder.findFolder(Repository.getDefault().getDefaultFileSystem().getRoot()).getNodeDelegate();
                    base = n.getIcon(BeanInfo.ICON_COLOR_16x16);                                 
                }
            }
            assert base != null;
            return base;
        }
    }
    
    private static class FileItem extends SpringXMLConfigCompletionItem {

        private FileObject file;

        public FileItem(int substitutionOffset, FileObject file) {
            super(substitutionOffset);
            this.file = file;
        }
        
        public int getSortPriority() {
            return 100;
        }

        public CharSequence getSortText() {
            return file.getNameExt();
        }

        public CharSequence getInsertPrefix() {
            return file.getNameExt();
        }

        @Override
        protected ImageIcon getIcon() {
            return new ImageIcon(Utilities.loadImage(
                    "org/netbeans/modules/spring/beans/resources/spring.png")); // NOI18N
        }

        @Override
        protected String getLeftHtmlText() {
            return file.getNameExt();
        }
    }
    
    
}
