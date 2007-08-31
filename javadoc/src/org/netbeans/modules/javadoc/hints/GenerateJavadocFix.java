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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.Comment;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.filesystems.FileObject;
import org.openide.text.NbDocument;
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
                        JavadocGenerator gen = new JavadocGenerator(GenerateJavadocFix.this.spec);
                        String javadocTxt = gen.generateComment(elm, wc);
                        Comment javadoc = Comment.create(Comment.Style.JAVADOC, NOPOS, NOPOS, 0, javadocTxt);
                        wc.getTreeMaker().addComment(t, javadoc, true);

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

        // XXX follows workaround until the generator starts to do its job
        try {
            if (docs[0] == null) {
                // nothing to do; TreeMaker did his job likely.
                return null;
            }

            NbDocument.runAtomicAsUser((StyledDocument) docs[0], new Runnable() {
                public void run() {
                    try {
                        String tab = JavadocGenerator.guessIndentation(docs[0], position);
                        String iJavadoc = JavadocGenerator.indentJavadoc(javadocForDocument[0], tab);
                        docs[0].insertString(position.getOffset(), iJavadoc, null);
                        // move the caret to proper position
                        int offset = iJavadoc.indexOf("/**"); // NOI18N
                        offset = iJavadoc.indexOf("\n", offset + 1);
                        offset = iJavadoc.indexOf("\n", offset + 1);
                        offset = position.getOffset() + offset - iJavadoc.length();
                        if (open) {
                            JavadocUtilities.open(file, offset);
                        }
                    } catch (BadLocationException ex) {
                        ex.printStackTrace();
                    }
                }
            });
        } catch (BadLocationException ex) {
            Logger.getLogger(GenerateJavadocFix.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return null;
    }
    
}
