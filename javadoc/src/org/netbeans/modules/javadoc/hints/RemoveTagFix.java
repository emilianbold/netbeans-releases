/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.javadoc.hints;

import com.sun.javadoc.Doc;
import com.sun.javadoc.Tag;
import com.sun.source.tree.Tree;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.editor.indent.api.Indent;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.javadoc.hints.JavadocUtilities.TagHandle;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.filesystems.FileObject;
import org.openide.text.NbDocument;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Pokorsky
 */
final class RemoveTagFix implements Fix, CancellableTask<WorkingCopy> {

    private String tagName;
    private final TagHandle tagHandle;
    private final ElementHandle handle;
    private final FileObject file;
    private final SourceVersion spec;

    private Position[] tagBounds;
    private boolean isFormattingTouched = false;
    private Document doc;

    RemoveTagFix(String tagName, TagHandle tagHandle, ElementHandle elmHandle, FileObject file, SourceVersion spec) {
        this.tagName = tagName;
        this.tagHandle = tagHandle;
        this.handle = elmHandle;
        this.file = file;
        this.spec = spec;
    }

    public String getText() {
        return NbBundle.getMessage(JavadocHintProvider.class, "REMOVE_TAG_HINT", tagName); // NOI18N
    }

    public ChangeInfo implement() {
        return implement(true);
    }

    private void prepareRemoveTag(final CompilationInfo ci, Element elm) throws IOException, BadLocationException {
        final Doc jdoc = ci.getElementUtilities().javaDocFor(elm);
        if (jdoc != null) {
            final Tag tag = tagHandle.resolve(jdoc);
            if (tag == null) {
                return;
            }

            NbDocument.runAtomicAsUser((StyledDocument) doc, new Runnable() {
                public void run() {
                    try {
                        tagBounds = JavadocUtilities.findTagBounds(ci, doc, tag);
                        Position[] jdBounds = JavadocUtilities.findDocBounds(ci, doc, jdoc);
                        tagBounds = expand2LineBounds(doc, tagBounds, jdBounds);
                    } catch (BadLocationException ex) {
                        Logger.getLogger(JavadocHintProvider.class.getName()).
                                log(Level.SEVERE, ex.getMessage(), ex);
                    }
                }
            });
        }
    }

    private void removeTag(final Indent indent) throws BadLocationException {
        if (tagBounds == null || doc == null) {
            return;
        }
        NbDocument.runAtomicAsUser((StyledDocument) doc, new Runnable() {
            public void run() {
                try {
                    doc.remove(tagBounds[0].getOffset(), tagBounds[1].getOffset() - tagBounds[0].getOffset());
                    if (indent != null) {
                        indent.reindent(tagBounds[1].getOffset());
                    }
                } catch (BadLocationException ex) {
                    Logger.getLogger(JavadocHintProvider.class.getName()).
                            log(Level.SEVERE, ex.getMessage(), ex);
                }
            }
        });
    }

    public ChangeInfo implement(final boolean open) {
        JavaSource js = JavaSource.forFileObject(file);
        Indent indent = null;
        try {
            js.runModificationTask(this).commit();
            
            // XXX follows workaround until the generator starts to do its job
            indent = isFormattingTouched? Indent.get(doc): null;
            if (indent != null) {
                indent.lock();
            }
            removeTag(indent);
        } catch (BadLocationException ex) {
            Logger.getLogger(JavadocHintProvider.class.getName()).
                    log(Level.SEVERE, ex.getMessage(), ex);
        } catch (IOException ex) {
            Logger.getLogger(JavadocHintProvider.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            if (indent != null) {
                indent.unlock();
            }
        }

        return null;
    }

    public void cancel() {
    }

    public void run(WorkingCopy wc) throws Exception {
        wc.toPhase(JavaSource.Phase.RESOLVED);
        Element elm = handle.resolve(wc);
        Tree t = null;
        if (elm != null) {
            t = wc.getTrees().getTree(elm);
        }
        
        doc = wc.getDocument();
        
        if (t != null && doc != null) {
            prepareRemoveTag(wc, elm);
        }
    }
    
    private static CharSequence getCharSequence(Document doc, int jdBegin, int jdEnd) throws BadLocationException {
        CharSequence cs = (CharSequence) doc.getProperty(CharSequence.class);
        if (cs == null) {
            cs = doc.getText(jdBegin, jdEnd - jdBegin + 1);
        }
        return cs;
    }

    private Position[] expand2LineBounds(Document doc, Position[] tagBounds, Position[] jdBounds) throws BadLocationException {
        int jdBeginLine = NbDocument.findLineNumber((StyledDocument) doc, jdBounds[0].getOffset());
        int jdEndLine = NbDocument.findLineNumber((StyledDocument) doc, jdBounds[1].getOffset());
        int tagBeginLine = NbDocument.findLineNumber((StyledDocument) doc, tagBounds[0].getOffset());
        int tagEndLine = NbDocument.findLineNumber((StyledDocument) doc, tagBounds[0].getOffset());
        
        CharSequence cs = getCharSequence(doc, jdBounds[0].getOffset(), jdBounds[1].getOffset());
        if (jdBeginLine != tagBeginLine) {
            int lineBegin = findLineHead(cs, tagBounds[0].getOffset(), jdBounds[0].getOffset());
            if (lineBegin != tagBounds[0].getOffset()) {
                tagBounds[0] = doc.createPosition(lineBegin);
                isFormattingTouched = tagBeginLine == jdEndLine;
            }
        }
        
        if (jdEndLine != tagEndLine && jdBeginLine != tagBeginLine) {
            int lineEnd = findLineTail(cs, tagBounds[0].getOffset(),
                    tagBounds[1].getOffset(), jdBounds[1].getOffset());
            if (lineEnd != tagBounds[1].getOffset()) {
                tagBounds[1] = doc.createPosition(lineEnd);
            }
        }
        
        
        return tagBounds;
    }

    private static int findLineHead(CharSequence cs, int offset, int stop) {
        for (int i = offset; i > stop; i--) {
            char c = cs.charAt(i);
            if (c == '\n') {
                return i + 1;
            }
        }

        return offset;
    }

    private static int findLineTail(CharSequence cs, int tagBegin, int tagEnd, int stop) {
        if (cs.charAt(tagEnd) != '@') {
            for (int i = tagEnd; i < stop; i++) {
                char c = cs.charAt(i);
                if (c == '\n') {
                    return i + 1;
                }
            }
        } else {
            for (int i = tagEnd - 1; i > tagBegin; i--) {
                char c = cs.charAt(i);
                if (c == '\n') {
                    return i + 1;
                }
            }
        }

        return tagEnd;
    }
    
}
