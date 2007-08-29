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

    private void removeTag(final CompilationInfo ci, Element elm) throws IOException, BadLocationException {
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
                    } catch (BadLocationException ex) {
                        Logger.getLogger(JavadocHintProvider.class.getName()).
                                log(Level.SEVERE, ex.getMessage(), ex);
                    }
                }
            });
        }
    }

    private void removeTag() throws BadLocationException {
        if (tagBounds == null || doc == null) {
            return;
        }
        NbDocument.runAtomicAsUser((StyledDocument) doc, new Runnable() {
            public void run() {
                try {
                    doc.remove(tagBounds[0].getOffset(), tagBounds[1].getOffset() - tagBounds[0].getOffset());
                } catch (BadLocationException ex) {
                    Logger.getLogger(JavadocHintProvider.class.getName()).
                            log(Level.SEVERE, ex.getMessage(), ex);
                }
            }
        });
    }

    public ChangeInfo implement(final boolean open) {
        JavaSource js = JavaSource.forFileObject(file);
        try {
            js.runModificationTask(this).commit();
            // XXX follows workaround until the generator starts to do its job
            removeTag();
        } catch (BadLocationException ex) {
            Logger.getLogger(JavadocHintProvider.class.getName()).
                    log(Level.SEVERE, ex.getMessage(), ex);
        } catch (IOException ex) {
            Logger.getLogger(JavadocHintProvider.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
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
            removeTag(wc, elm);
        }
    }

}
