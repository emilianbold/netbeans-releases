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
package org.netbeans.modules.javadoc.hints;

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
import org.netbeans.api.java.source.Comment;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.editor.indent.api.Reformat;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.filesystems.FileObject;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Pokorsky
 */
final class GenerateJavadocFix implements Fix {
    
    private static final int NOPOS = -2; // XXX copied from jackpot; should be in api
    private String name;
    private final ElementHandle handle;
    private final FileObject file;
    private Position position;
    private final SourceVersion spec;

    GenerateJavadocFix(String name, ElementHandle handle, FileObject file, SourceVersion spec) {
        this.name = name;
        this.handle = handle;
        this.file = file;
        this.spec = spec;
    }

    public String getText() {
        return NbBundle.getMessage(GenerateJavadocFix.class, "MISSING_JAVADOC_HINT", name); // NOI18N
    }

    public ChangeInfo implement() {
        return implement(true);
    }

    public ChangeInfo implement(final boolean open) {
        final String[] javadocForDocument = new String[1];
        final Document[] docs = new Document[1];
        final JavadocGenerator gen = new JavadocGenerator(spec);
        gen.updateSettings(file);
        JavaSource js = JavaSource.forFileObject(file);
        try {
            js.runModificationTask(new CancellableTask<WorkingCopy>() {
                public void cancel() {
                }

                public void run(WorkingCopy wc) throws Exception {
                    wc.toPhase(JavaSource.Phase.RESOLVED);
                    Element elm = handle.resolve(wc);
                    Tree t = null;
                    if (elm != null) {
                        t = wc.getTrees().getTree(elm);
                    }
                    if (t != null) {
                        String javadocTxt = gen.generateComment(elm, wc);
//                        Comment javadoc = Comment.create(Comment.Style.JAVADOC, NOPOS, NOPOS, 0, javadocTxt);
//                        wc.getTreeMaker().addComment(t, javadoc, true);

                        // XXX workaround until the generator start to do its job
                        javadocForDocument[0] = javadocTxt;
                        docs[0] = wc.getDocument();
                        if (docs[0] == null) {
                            return;
                        }
                        position = docs[0].createPosition((int) wc.getTrees().getSourcePositions().getStartPosition(wc.getCompilationUnit(), t));
                    }
                }

            }).commit();

        } catch (IOException ex) {
            Logger.getLogger(GenerateJavadocFix.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }

        if (docs[0] == null) {
            // nothing to do
            return null;
        }

        // XXX #90302; follows workaround until the generator starts to do its job
        final Indent indent = Indent.get(docs[0]);
        final Reformat reformat = Reformat.get(docs[0]);
        try {
            indent.lock();
            reformat.lock();
            NbDocument.runAtomicAsUser((StyledDocument) docs[0], new Runnable() {
                public void run() {
                    try {
                        String iJavadoc = javadocForDocument[0];
                        int begin = position.getOffset();
                        Position[] reformatSpan = null;
                        if (makeJavadocAloneOnLine(docs[0], begin)) {
                            // #124114
                            iJavadoc = '\n' + iJavadoc;
                            int[] span = findReformatSpan(docs[0], begin);
                            reformatSpan = new Position[] {
                                docs[0].createPosition(span[0]),
                                docs[0].createPosition(span[1])};
                        }
                        docs[0].insertString(begin, iJavadoc, null);
                        // move the caret to proper position
                        int offset = iJavadoc.indexOf("/**"); // NOI18N
                        offset = iJavadoc.indexOf("\n", offset + 1);
                        offset = iJavadoc.indexOf("\n", offset + 1);
                        Position openPos = docs[0].createPosition(begin + offset);
                        indent.reindent(begin, begin + iJavadoc.length() + 1);
                        if (reformatSpan != null) {
                            reformat.reformat(reformatSpan[0].getOffset(), reformatSpan[1].getOffset());
                        }
                        if (open) {
                            JavadocUtilities.open(file, openPos.getOffset());
                        }
                    } catch (BadLocationException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            });
        } catch (BadLocationException ex) {
            Logger.getLogger(GenerateJavadocFix.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            reformat.unlock();
            indent.unlock();
        }
        return null;
    }
    
    private static boolean makeJavadocAloneOnLine(Document doc, int javadocBegin) throws BadLocationException {
        CharSequence txt = (CharSequence) doc.getProperty(CharSequence.class);
        if (txt == null) {
            txt = doc.getText(0, javadocBegin);
        }
        
        if (javadocBegin - 1 >= txt.length()) {
            return false;
        }
        
        for (int i = javadocBegin - 1; i >= 0; i--) {
            char c = txt.charAt(i);
            if (Character.isWhitespace(c)) {
                if (c == '\n') {
                    // before javadocBegin are only white spaces
                    break;
                } else {
                    continue;
                }
            } else {
                return true;
            }
        }
        
        return false;
    }
    
    private static int[] findReformatSpan(Document doc, int javadocBegin) throws BadLocationException {
        int[] span = {javadocBegin, javadocBegin};
        CharSequence txt = (CharSequence) doc.getProperty(CharSequence.class);
        if (txt == null) {
            txt = doc.getText(0, doc.getLength());
        }
        
        for (; span[0] > 0; span[0]--) {
            char c = txt.charAt(span[0]);
            if (c == '\n') {
                break;
            }
        }
        
        for (; span[1] < txt.length() - 1; span[1]++) {
            char c = txt.charAt(span[1]);
            if (c == '\n') {
                break;
            }
        }
        
        return span;
    }
    
}
