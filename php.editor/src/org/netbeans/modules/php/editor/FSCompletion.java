/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
package org.netbeans.modules.php.editor;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.beans.BeanInfo;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.php.editor.nav.NavUtils;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.Include;
import org.netbeans.modules.php.editor.parser.astnodes.ParenthesisExpression;
import org.netbeans.modules.php.editor.parser.astnodes.Scalar;
import org.netbeans.modules.php.editor.parser.astnodes.Scalar.Type;
import org.netbeans.modules.php.project.api.PhpSourcePath;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;

/**
 * Base on code from contrib/editor.fscompletion
 * @author Jan Lahoda
 */
public class FSCompletion implements CompletionProvider {
    
    public FSCompletion() {
    }

    public CompletionTask createTask(int queryType, JTextComponent component) {
        return new AsyncCompletionTask(new AsyncCompletionQuery() {
            protected void query(final CompletionResultSet resultSet, final Document doc, final int caretOffset) {
                try {
                    FileObject file = NavUtils.getFile(doc);

                    if (file == null || caretOffset == -1) {
                        return;
                    }

                    final List<FileObject> includePath = PhpSourcePath.getIncludePath(file);
                    try {
                        ParserManager.parse(Collections.singleton(Source.create(file)), new UserTask() {

                            @Override
                            public void run(ResultIterator resultIterator) throws Exception {
                                ParserResult parameter = (ParserResult) resultIterator.getParserResult();
                                if (parameter == null) {
                                    return;
                                }
                                List<ASTNode> path = NavUtils.underCaret(parameter, caretOffset);
                                if (path.size() < 2) {
                                    return;
                                }
                                ASTNode d1 = path.get(path.size() - 1);
                                ASTNode d2 = path.get(path.size() - 2);
                                if (d2 instanceof ParenthesisExpression) {
                                    if (path.size() < 3) {
                                        return;
                                    }
                                    d2 = path.get(path.size() - 3);
                                }
                                if (!(d1 instanceof Scalar) || !(d2 instanceof Include)) {
                                    return;
                                }
                                Scalar s = (Scalar) d1;
                                if (s.getScalarType() != Type.STRING || !NavUtils.isQuoted(s.getStringValue())) {
                                    return;
                                }
                                int startOffset = s.getStartOffset() + 1;
                                final String prefix = parameter.getSnapshot().getText().subSequence(startOffset, caretOffset).toString();
                                List<FileObject> relativeTo = new LinkedList<FileObject>();
                                relativeTo.addAll(includePath);
                                final PHPIncludesFilter filter = new PHPIncludesFilter(parameter.getSnapshot().getSource().getFileObject());
                                final FileObject parent = parameter.getSnapshot().getSource().getFileObject().getParent();
                                if (parent != null) {
                                    relativeTo.add(parent);
                                }
                                resultSet.addAllItems(computeRelativeItems(relativeTo, prefix, startOffset,filter));
                            }
                        });
                    } catch (ParseException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                } finally {
                    resultSet.finish();
                }
            }
        }, component);
    }

    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        return 0;
    }

    private static List<? extends CompletionItem> computeRelativeItems(Collection<? extends FileObject> relativeTo, final String prefix, int anchor, FileObjectFilter filter) throws IOException {
        final String GO_UP = "../";
        assert relativeTo != null;
        
        List<CompletionItem> result = new LinkedList<CompletionItem>();
                
        int lastSlash = prefix.lastIndexOf('/');
        String pathPrefix;
        String filePrefix;
        
        if (lastSlash != (-1)) {
            pathPrefix = prefix.substring(0, lastSlash);
            filePrefix = prefix.substring(lastSlash + 1);
        } else {
            pathPrefix = null;
            filePrefix = prefix;
        }
        
        Set<FileObject> directories = new HashSet<FileObject>();
        File prefixFile = null;
        if (pathPrefix != null && !pathPrefix.startsWith(".")) {//NOI18N
            if (pathPrefix.length() == 0 && prefix.startsWith("/")) {
                prefixFile = new File("/");//NOI18N
            } else {
                prefixFile = new File(pathPrefix);
            }
        }
        if (prefixFile != null && prefixFile.exists()) {
            //absolute path
            File normalizeFile = FileUtil.normalizeFile(prefixFile);
            FileObject fo = FileUtil.toFileObject(normalizeFile);
            if (fo != null) {
                directories.add(fo);
            }
        } else {
            //relative path
            for (FileObject f : relativeTo) {
                if (pathPrefix != null) {
                    File toFile = FileUtil.toFile(f);
                    if (toFile != null) {
                        URI resolve = toFile.toURI().resolve(pathPrefix);
                        f = FileUtil.toFileObject(new File(resolve));
                    } else {
                        f = f.getFileObject(pathPrefix);
                    }
                }

                if (f != null) {
                    directories.add(f);
                }
            }
        }
        
        for (FileObject dir : directories) {
            FileObject[] children = dir.getChildren();

            for (int cntr = 0; cntr < children.length; cntr++) {
                FileObject current = children[cntr];

                if (VisibilityQuery.getDefault().isVisible(current) && current.getNameExt().toLowerCase().startsWith(filePrefix.toLowerCase()) && filter.accept(current)) {
                    result.add(new FSCompletionItem(current, pathPrefix != null ? pathPrefix + "/" : "", anchor));
                }
            }
        }
        if (GO_UP.startsWith(filePrefix) && directories.size() == 1) {
            final FileObject parent = directories.iterator().next();
            if (parent.getParent() != null && VisibilityQuery.getDefault().isVisible(parent.getParent()) && filter.accept(parent.getParent())) {
                result.add(new FSCompletionItem(parent, "", anchor) {
                    @Override
                    public void render(Graphics g, Font defaultFont, Color defaultColor, Color backgroundColor, int width, int height, boolean selected) {
                        CompletionUtilities.renderHtml(super.icon,GO_UP, null, g, defaultFont, defaultColor, width, height, selected);
                    }

                    @Override
                    protected String getText() {
                        return prefix + GO_UP;//NOI18N
                    }
                });
            }
        }
        
        return result;
    }
    
    private static class PHPIncludesFilter implements FileObjectFilter {
        private FileObject currentFile;

        public PHPIncludesFilter(FileObject currentFile) {
            this.currentFile = currentFile;
        }

        public boolean accept(FileObject file) {
            if (file.equals(currentFile) || isNbProjectMetadata(file)){
                return false; //do not include self in the cc result
            }

            if (file.isFolder()) {
                return true;
            }
            
            String mimeType = FileUtil.getMIMEType(file);
            
            return mimeType != null && mimeType.startsWith("text/");
        }

        private static boolean isNbProjectMetadata(FileObject fo) {
            final String metadataName = "nbproject";//NOI18N
            if (fo.getPath().indexOf(metadataName) != -1) {
                while(fo != null) {
                    if (fo.isFolder()) {
                        if (metadataName.equals(fo.getNameExt())) {
                            return true;
                        }
                    }
                    fo = fo.getParent();
                }
            }
            return false;
        }
    }

    static class FSCompletionItem implements CompletionItem {

        private FileObject file;
        private ImageIcon  icon;
        private int        anchor;
        private String     toAdd;
        private String     prefix;

        public FSCompletionItem(FileObject file, String prefix, int anchor) throws IOException {
            this.file = file;

            DataObject od = DataObject.find(file);

            icon = new ImageIcon(od.getNodeDelegate().getIcon(BeanInfo.ICON_COLOR_16x16));

            this.anchor = anchor;

            this.prefix = prefix;
        }

        private void doSubstitute(JTextComponent component, String toAdd, int backOffset) {
            BaseDocument doc = (BaseDocument) component.getDocument();
            int caretOffset = component.getCaretPosition();
            String value = getText();

            if (toAdd != null) {
                value += toAdd;
            }

            // Update the text
            doc.atomicLock();
            try {
                String pfx = doc.getText(anchor, caretOffset - anchor);

                doc.remove(caretOffset - pfx.length(), pfx.length());
                doc.insertString(caretOffset - pfx.length(), value, null);

                component.setCaretPosition(component.getCaretPosition() - backOffset);
            } catch (BadLocationException e) {
                Exceptions.printStackTrace(e);
            } finally {
                doc.atomicUnlock();
            }
        }

        public void defaultAction(JTextComponent component) {
            doSubstitute(component, null, 0);
            if (!file.isFolder()) {
                Completion.get().hideAll();
            }
        }

        public void processKeyEvent(KeyEvent evt) {
            if (evt.getID() == KeyEvent.KEY_TYPED) {
                String strToAdd = null;

                switch (evt.getKeyChar()) {
                    case '/': if (strToAdd == null) strToAdd = "/";
                    doSubstitute((JTextComponent) evt.getSource(), strToAdd, strToAdd.length() - 1);
                    evt.consume();
                    break;
                }
            }
        }

        public int getPreferredWidth(Graphics g, Font defaultFont) {
            return CompletionUtilities.getPreferredWidth(file.getNameExt(), null, g, defaultFont);
        }

        public void render(Graphics g, Font defaultFont, Color defaultColor, Color backgroundColor, int width, int height, boolean selected) {
            CompletionUtilities.renderHtml(icon, file.getNameExt(), null, g, defaultFont, defaultColor, width, height, selected);
        }

        public CompletionTask createDocumentationTask() {
            return null;
        }

        public CompletionTask createToolTipTask() {
            return null;
        }

        public boolean instantSubstitution(JTextComponent component) {
            return false; //????
        }

        public int getSortPriority() {
            return -1000;
        }

        public CharSequence getSortText() {
            return getText();
        }

        public CharSequence getInsertPrefix() {
            return getText();
        }

        protected String getText() {
            return prefix + file.getNameExt() + (file.isFolder() ? "/" : "");
        }

        @Override
        public int hashCode() {
            return getText().hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof FSCompletionItem)) 
                return false;

            FSCompletionItem remote = (FSCompletionItem) o;

            return getText().equals(remote.getText());
        }

    }

    interface FileObjectFilter {

        public boolean accept(FileObject file);

    }
}
