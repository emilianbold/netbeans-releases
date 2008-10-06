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

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.Doc;
import com.sun.javadoc.ExecutableMemberDoc;
import com.sun.javadoc.Tag;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
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

    public static Fix createAddTypeParamTagFix(Element elm,
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
            insertJavadoc();
            JavadocUtilities.open(file, openOffset);
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
            insertPosition = getParamInsertPosition(wc, doc, (ExecutableElement) elm, (ExecutableMemberDoc) jdoc, isLastTag);
            insertJavadoc = "@param " + paramName + " "; // NOI18N
            break;
        case TYPEPARAM:
            insertPosition = getTypeParamInsertPosition(wc, doc, elm, jdoc, isLastTag);
            insertJavadoc = "@param <" + paramName + "> "; // NOI18N
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

        if (insertPosition == null) {
            return;
        }
        // create tag string
        // resolve indentation
        // take start of javadoc and find /** and compute distance od \n and first *
        Position[] jdBounds = JavadocUtilities.findDocBounds(wc, doc, jdoc);
        int jdBeginLine = NbDocument.findLineNumber((StyledDocument) doc, jdBounds[0].getOffset());
        int jdEndLine = NbDocument.findLineNumber((StyledDocument) doc, jdBounds[1].getOffset());
        int insertLine = NbDocument.findLineNumber((StyledDocument) doc, insertPosition.getOffset());

        if (jdBeginLine == insertLine && insertLine == jdEndLine) {
            // one line javadoc
            insertJavadoc = '\n' + insertJavadoc;
            openOffset = insertJavadoc.length();
            insertJavadoc += '\n';
        } else if (insertLine == jdEndLine) {
            if (isLastTag[0]) {
                // /**\n* @return r |*/ or /**\n* \n*/
                // insert after the last block tag that ends with */
                if (!isEmptyLine(doc, insertPosition.getOffset())) {
                    insertJavadoc = '\n' + insertJavadoc;
                }
            }
            openOffset = insertJavadoc.length();
            insertJavadoc += '\n';
        } else if (insertLine == jdBeginLine) {
            /** |@return r\n*/
            insertJavadoc = '\n' + insertJavadoc;
            openOffset = insertJavadoc.length();
            if (!isLastTag[0]) {
                insertJavadoc += '\n';
            }
        } else if (isLastTag[0]) {
            // insert after the last block tag
            insertJavadoc = '\n' + insertJavadoc;
            openOffset = insertJavadoc.length();
        } else {
            // insert before some block tag
            openOffset = insertJavadoc.length();
            insertJavadoc = insertJavadoc + '\n';
        }
    }
    
    /**
     * checks if the chars before offset can be considered as an empty line.
     */
    private boolean isEmptyLine(Document doc, int offset) throws BadLocationException {
        CharSequence txt = (CharSequence) doc.getProperty(CharSequence.class);
        if (txt == null) {
            txt = doc.getText(0, offset + 1);
        }
        
        // line contains non white space other then '*'
        boolean isClean = true;
        int asterisks = 0;
        
        for (int i = offset; i >= 0 ; i--) {
            char c = txt.charAt(i);
            if (c == '\n') {
                break;
            } else if (c == '*') {
                if (asterisks > 0) {
                    isClean = false;
                    break;
                }
                ++asterisks;
            } else if (Character.isSpaceChar(c)) {
                continue;
            } else {
                isClean = false;
                break;
            }
        }

        return isClean;
    }

    private void insertJavadoc() throws BadLocationException {
        final Indent indent = Indent.get(doc);
        try {
            indent.lock();
            NbDocument.runAtomicAsUser((StyledDocument) doc, new Runnable() {
                public void run() {
                    try {
                        // insert indented string to text
                        int begin = insertPosition.getOffset();
                        int end = begin + insertJavadoc.length() + 1;
                        doc.insertString(begin, insertJavadoc, null);
                        Position openPos = doc.createPosition(begin + openOffset - 1);
                        indent.reindent(begin, end);
                        // insert space since the JavaFormatter cleans up trailing spaces :-(
                        doc.insertString(openPos.getOffset(), " ", null); // NOI18N
                        openOffset = openPos.getOffset();
                    } catch (BadLocationException ex) {
                        Logger.getLogger(AddTagFix.class.getName()).
                                log(Level.SEVERE, ex.getMessage(), ex);
                    }
                }
            });
        } finally {
            indent.unlock();
        }
    }

    public void cancel() {
    }

    private Position getDeprecatedInsertPosition(CompilationInfo wc, Document doc, Doc jdoc, boolean[] isLastTag) throws BadLocationException {
        // find last javadoc token position
        return getTagInsertPosition(wc, doc, jdoc, null, false, isLastTag);
    }

    private Position getTypeParamInsertPosition(CompilationInfo wc, Document doc, Element elm, Doc jdoc, boolean[] isLastTag) throws BadLocationException {
        // 1. find @param tags + find index of param and try to apply on @param array
        ElementKind elmkind = elm.getKind();
        Tag[] tags;
        if (elmkind.isClass() || elmkind.isInterface()) {
            tags = ((ClassDoc) jdoc).typeParamTags();
        } else if (elmkind == ElementKind.METHOD || elmkind == ElementKind.CONSTRUCTOR) {
            tags = ((ExecutableMemberDoc) jdoc).typeParamTags();
        } else {
            throw new IllegalStateException(elm + ", " + elmkind + "\n" + jdoc.getRawCommentText()); // NOI18N
        }
        Tag where = null;
        boolean insertBefore = true;
        if (tags.length > 0) {
            List<? extends TypeParameterElement> typeParameters =
                    elmkind == ElementKind.METHOD || elmkind == ElementKind.CONSTRUCTOR
                    ? ((ExecutableElement) elm).getTypeParameters()
                    : ((TypeElement) elm).getTypeParameters();
            int pindex = findParamIndex(typeParameters, paramName);
            where = pindex < tags.length? tags[pindex]: tags[tags.length - 1];
            insertBefore = pindex < tags.length;
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

    private Position getParamInsertPosition(CompilationInfo wc, Document doc, ExecutableElement elm, ExecutableMemberDoc jdoc, boolean[] isLastTag) throws BadLocationException {
        // 1. find @param tags + find index of param and try to apply on @param array
        Tag[] tags = jdoc.paramTags();
        Tag where = null;
        boolean insertBefore = true;
        if (tags.length > 0) {
            int pindex = findParamIndex(elm.getParameters(), paramName);
            where = pindex < tags.length? tags[pindex]: tags[tags.length - 1];
            insertBefore = pindex < tags.length;
        } else {
            tags = jdoc.typeParamTags();
            // 2. if not, find last type param tag + insert after
            if (tags.length > 0) {
                where = tags[tags.length - 1];
                insertBefore = false;
            } else {
                // 3. if not, find first tag + insert before
                tags = jdoc.tags();
                if (tags.length > 0) {
                    where = tags[0];
                }
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
            isLastTag[0] = true;
        }

        return bounds == null
                ? null
                : insertBefore ? bounds[0] : bounds[1];
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
