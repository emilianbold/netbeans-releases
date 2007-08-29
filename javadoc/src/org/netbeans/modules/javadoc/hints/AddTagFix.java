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
import com.sun.javadoc.ExecutableMemberDoc;
import com.sun.javadoc.Tag;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationInfo;
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
final class AddTagFix implements Fix, CancellableTask<WorkingCopy> {

    private enum Kind {PARAM, RETURN, THROWS, TYPEPARAM, DEPRECATED}
    private final ElementHandle methodHandle;
    private final String paramName;
    /** index of throwable in throwables list */
    private final int index;
    private final FileObject file;
    private final SourceVersion spec;
    private final String descKey;
    private final Kind kind;

    private Position insertPosition;
    String insertJavadoc;
    private int openOffset;
    Document doc;

    private AddTagFix(ElementHandle methodHandle, String paramName, int index,
            FileObject file, SourceVersion spec, String descKey, Kind tagKind) {
        this.methodHandle = methodHandle;
        this.paramName = paramName;
        this.index = index;
        this.file = file;
        this.spec = spec;
        this.descKey = descKey;
        this.kind = tagKind;
    }

    public static Fix createAddParamTagFix(ExecutableElement elm,
            String paramName, FileObject file, SourceVersion spec) {
        return new AddTagFix(ElementHandle.create(elm), paramName, -1, file, spec, "MISSING_PARAM_HINT", Kind.PARAM); // NOI18N
    }

    public static Fix createAddTypeParamTagFix(TypeElement elm,
            String paramName, FileObject file, SourceVersion spec) {
        return new AddTagFix(ElementHandle.create(elm), paramName, -1, file, spec, "MISSING_TYPEPARAM_HINT", Kind.TYPEPARAM); // NOI18N
    }

    public static Fix createAddReturnTagFix(ExecutableElement elm,
            FileObject file, SourceVersion spec) {
        return new AddTagFix(ElementHandle.create(elm), "", -1, file, spec, "MISSING_RETURN_HINT", Kind.RETURN); // NOI18N
    }

    public static Fix createAddThrowsTagFix(ExecutableElement elm,
            String fqn, int throwIndex, FileObject file, SourceVersion spec) {
        return new AddTagFix(ElementHandle.create(elm), fqn, throwIndex, file, spec, "MISSING_THROWS_HINT", Kind.THROWS); // NOI18N
    }

    public static Fix createAddDeprecatedTagFix(Element elm,
            FileObject file, SourceVersion spec) {
        return new AddTagFix(ElementHandle.create(elm), "", -1, file, spec, "MISSING_DEPRECATED_HINT", Kind.DEPRECATED); // NOI18N
    }

    public String getText() {
        return NbBundle.getMessage(AddTagFix.class, descKey, this.paramName);
    }

    public ChangeInfo implement() {
        JavaSource js = JavaSource.forFileObject(file);
        try {
            js.runModificationTask(this).commit();
            if (doc == null || insertPosition == null || insertJavadoc == null) {
                return null;
            }
            int open = insertPosition.getOffset() + openOffset;
            insertJavadoc();
            JavadocUtilities.open(file, open);
        } catch (BadLocationException ex) {
            Logger.getLogger(AddTagFix.class.getName()).
                    log(Level.SEVERE, ex.getMessage(), ex);
        } catch (IOException ex) {
            Logger.getLogger(AddTagFix.class.getName()).
                    log(Level.SEVERE, ex.getMessage(), ex);
        }
        return null;
    }

    public void run(final WorkingCopy wc) throws Exception {
        wc.toPhase(JavaSource.Phase.RESOLVED);
        final Element elm = methodHandle.resolve(wc);
        if (elm == null) {
            return;
        }

        final Doc jdoc = wc.getElementUtilities().javaDocFor(elm);
        doc = wc.getDocument();
        if (doc == null) {
            return;
        }

        NbDocument.runAtomicAsUser((StyledDocument) doc, new Runnable() {
            public void run() {
                try {
                    computeInsertPositionAndJavadoc(wc, elm, jdoc);
                } catch (BadLocationException ex) {
                    Logger.getLogger(AddTagFix.class.getName()).
                            log(Level.SEVERE, ex.getMessage(), ex);
                }
            }
        });
    }

    private void computeInsertPositionAndJavadoc(CompilationInfo wc, Element elm, Doc jdoc) throws BadLocationException {
        // find position where to add
        boolean[] isLastTag = new boolean[1];
        switch (this.kind) {
        case PARAM:
            insertPosition = getParamInsertPosition(wc, doc, (ExecutableElement) elm, jdoc, isLastTag);
            insertJavadoc = "@param " + paramName + " "; // NOI18N
            break;
        case TYPEPARAM:
            insertPosition = getTypeParamInsertPosition(wc, doc, (TypeElement) elm, jdoc, isLastTag);
            insertJavadoc = "@param " + paramName + " "; // NOI18N
            break;
        case RETURN:
            insertPosition = getReturnInsertPosition(wc, doc, jdoc, isLastTag);
            insertJavadoc = "@return "; // NOI18N
            break;
        case THROWS:
            insertPosition = getThrowsInsertPosition(wc, doc, (ExecutableMemberDoc) jdoc, isLastTag);
            insertJavadoc = "@throws " + paramName + " "; // NOI18N
            break;
        case DEPRECATED:
            insertPosition = getDeprecatedInsertPosition(wc, doc, jdoc, isLastTag);
            insertJavadoc = "@deprecated "; // NOI18N
            break;
        default:
            throw new IllegalStateException();
        }

        // create tag string
        // resolve indentation
        // take start of javadoc and find /** and compute distance od \n and first *
        Position[] jdBounds = JavadocUtilities.findDocBounds(wc, doc, jdoc);
        int jdBeginLine = NbDocument.findLineNumber((StyledDocument) doc, jdBounds[0].getOffset());
        int jdEndLine = NbDocument.findLineNumber((StyledDocument) doc, jdBounds[1].getOffset());
        int insertLine = NbDocument.findLineNumber((StyledDocument) doc, insertPosition.getOffset());

        String indentation = JavadocGenerator.guessJavadocIndentation(wc, doc, jdoc); // NOI18N
        if (jdBeginLine == insertLine && insertLine == jdEndLine) {
            // one line javadoc
            insertJavadoc = '\n' + indentation + "* " + insertJavadoc; // NOI18N
            openOffset = insertJavadoc.length();
            insertJavadoc += '\n' + indentation;
        } else if (insertLine == jdEndLine && !isLastTag[0]) {
            // multiline javadoc but empty
            openOffset = 2 + insertJavadoc.length();
            insertJavadoc = "* " + insertJavadoc + '\n' + indentation; // NOI18N
        } else if (isLastTag[0]) {
            // insert after the last block tag
            insertJavadoc = '\n' + indentation + "* " + insertJavadoc; // NOI18N
            openOffset = insertJavadoc.length();
        } else {
            // insert before some block tag
            openOffset = insertJavadoc.length();
            insertJavadoc = insertJavadoc + '\n' + indentation + "* "; // NOI18N
        }
    }

    private void insertJavadoc() throws BadLocationException {
        NbDocument.runAtomicAsUser((StyledDocument) doc, new Runnable() {
            public void run() {
                try {
                    // insert indented string to text
                    doc.insertString(insertPosition.getOffset(), insertJavadoc, null);
                } catch (BadLocationException ex) {
                    Logger.getLogger(AddTagFix.class.getName()).
                            log(Level.SEVERE, ex.getMessage(), ex);
                }
            }
        });
    }

    public void cancel() {
    }

    private Position getDeprecatedInsertPosition(CompilationInfo wc, Document doc, Doc jdoc, boolean[] isLastTag) throws BadLocationException {
        // find last javadoc token position
        return getTagInsertPosition(wc, doc, jdoc, null, false, isLastTag);
    }

    private Position getTypeParamInsertPosition(CompilationInfo wc, Document doc, TypeElement elm, Doc jdoc, boolean[] isLastTag) throws BadLocationException {
        // 1. find @param tags + find index of param and try to apply on @param array
        Tag[] tags = jdoc.tags("@param"); // NOI18N
        Tag where = null;
        boolean insertBefore = true;
        if (tags.length > 0) {
            int index = findParamIndex(elm.getTypeParameters(), paramName);
            where = index < tags.length? tags[index]: tags[tags.length - 1];
            insertBefore = index < tags.length;
        } else {
            // 2. if not, find first tag + insert before
            tags = jdoc.tags();
            if (tags.length > 0) {
                where = tags[0];
            }
        }
        return getTagInsertPosition(wc, doc, jdoc, where, insertBefore, isLastTag);
    }

    private Position getThrowsInsertPosition(CompilationInfo wc, Document doc, ExecutableMemberDoc jdoc, boolean[] isLastTag) throws BadLocationException {
        // 1. find @param tags + find index of param and try to apply on @param array
        Tag[] tags = jdoc.throwsTags(); // NOI18N
        // XXX filter type params?
        Tag where = null;
        boolean insertBefore = true;
        if (tags.length > 0) {
            where = index < tags.length? tags[index]: tags[tags.length - 1];
            insertBefore = index < tags.length;
        } else {
            // 2. if not, find first tag + insert before
            tags = jdoc.tags("@return"); // NOI18N
            if (tags.length == 0) {
                tags = jdoc.tags("@param"); // NOI18N
            }
            if (tags.length == 0) {
                tags = jdoc.tags();
            } else {
                // in case @return or @param
                insertBefore = false;
            }
            if (tags.length > 0) {
                where = tags[tags.length - 1];
            }
        }
        return getTagInsertPosition(wc, doc, jdoc, where, insertBefore, isLastTag);
    }

    private Position getReturnInsertPosition(CompilationInfo wc, Document doc, Doc jdoc, boolean[] isLastTag) throws BadLocationException {
        // 1. find @param tags
        Tag[] tags = jdoc.tags("@param"); // NOI18N
        Tag where = null;
        boolean insertBefore = true;
        if (tags.length > 0) {
            where = tags[tags.length - 1];
            insertBefore = false;
        } else {
            // 2. if not, find first tag + insert before
            tags = jdoc.tags();
            if (tags.length > 0) {
                where = tags[0];
            }
        }
        return getTagInsertPosition(wc, doc, jdoc, where, insertBefore, isLastTag);
    }

    private Position getParamInsertPosition(CompilationInfo wc, Document doc, ExecutableElement elm, Doc jdoc, boolean[] isLastTag) throws BadLocationException {
        // 1. find @param tags + find index of param and try to apply on @param array
        Tag[] tags = jdoc.tags("@param"); // NOI18N
        // XXX filter type params?
        Tag where = null;
        boolean insertBefore = true;
        if (tags.length > 0) {
            int index = findParamIndex(elm.getParameters(), paramName);
            where = index < tags.length? tags[index]: tags[tags.length - 1];
            insertBefore = index < tags.length;
        } else {
            // 2. if not, find first tag + insert before
            tags = jdoc.tags();
            if (tags.length > 0) {
                where = tags[0];
            }
        }
        return getTagInsertPosition(wc, doc, jdoc, where, insertBefore, isLastTag);
    }

    private Position getTagInsertPosition(CompilationInfo wc, Document doc, Doc jdoc, Tag where, boolean insertBefore, boolean[] isLastTag) throws BadLocationException {
        // find insert position
        Position[] bounds = null;
        if (where != null) {
            bounds = JavadocUtilities.findTagBounds(wc, doc, where, isLastTag);
            if (insertBefore) {
                isLastTag[0] = false;
            }
        } else {
            // 3. if not, insert at the last token; resolve \n and /***/ cases
            bounds = JavadocUtilities.findLastTokenBounds(wc, doc, jdoc);
            insertBefore = false;
            isLastTag[0] = false;
        }

        return insertBefore? bounds[0]: bounds[1];
    }

    private int findParamIndex(List<? extends Element> params, String name) {
        int i = 0;
        for (Element param : params) {
            if (name.contentEquals(param.getSimpleName())) {
                return i;
            }
            i++;
        }
        throw new IllegalArgumentException("Unknown param: " + name); // NOI18N
    }
}
