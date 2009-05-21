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

import com.sun.javadoc.AnnotationTypeElementDoc;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.Doc;
import com.sun.javadoc.ExecutableMemberDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.ParamTag;
import com.sun.javadoc.Tag;
import com.sun.javadoc.ThrowsTag;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.guards.GuardedSection;
import org.netbeans.api.editor.guards.GuardedSectionManager;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.support.CaretAwareJavaSourceTaskFactory;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.java.hints.spi.AbstractHint.HintSeverity;
import org.netbeans.modules.javadoc.hints.JavadocUtilities.TagHandle;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.LazyFixList;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * Checks:
 *      - missing javadoc
 *      - @param duplicate, missing, unknown
 *      - @throws duplicate, missing, unknown
 *      - @return duplicate, missing, void
 *      - if @Deprecated annotation, check for @deprecated tag
 *      - if inheritance in place check for superclass javadoc;
 *          - javadoc and its parts may be inherited
 * @see <a href="http://java.sun.com/javase/6/docs/technotes/tools/solaris/javadoc.html#inheritingcomments">Automatic Copying of Method Comments</a>
 * @see <a href="http://java.sun.com/javase/6/docs/technotes/tools/solaris/javadoc.html#javadoctags">Javadoc Tags</a>
 * @see <a href="http://java.sun.com/javase/6/docs/technotes/tools/solaris/javadoc.html#wheretags">Where Tags Can Be Used</a>
 * @see <a href="http://java.sun.com/javase/6/docs/technotes/guides/javadoc/deprecation/index.html">Deprecation of APIs</a>
 * 
 * @author Jan Pokorsky
 */
final class Analyzer {

    private static final String ERROR_IDENT = "<error>";
    private final CompilationInfo javac;
    private final SourceVersion spec;
    private final FixAll fixAll = new FixAll();
    private final Document doc;
    private final FileObject file;
    private final Severity severity;
    private final HintSeverity hintSeverity;
    private final TreePath currentPath;
    private final boolean createJavadocKind;
    private final Access access;

    Analyzer(CompilationInfo javac, Document doc, TreePath currentPath,
            Severity severity, HintSeverity hintSeverity,
            boolean createJavadocKind, Access access) {
        
        this.javac = javac;
        this.doc = doc;
        this.file = javac.getFileObject();
        this.currentPath = currentPath;
        this.severity = severity;
        this.hintSeverity = hintSeverity;
        this.spec = resolveSourceVersion(javac.getFileObject());
        this.createJavadocKind = createJavadocKind;
        this.access = access;
    }

    private ErrorDescription createErrorDescription(String message, LazyFixList fixes, Position[] positions) {
        if (hintSeverity == HintSeverity.CURRENT_LINE_WARNING) {
            return ErrorDescriptionFactory.createErrorDescription(severity,
                    message,
                    fixes,
                    file,
                    CaretAwareJavaSourceTaskFactory.getLastPosition(file),
                    CaretAwareJavaSourceTaskFactory.getLastPosition(file));
        } else {
            return ErrorDescriptionFactory.createErrorDescription(severity,
                    message,
                    fixes,
                    doc,
                    positions[0],
                    positions[1]);
        }
    }

    private ErrorDescription createErrorDescription(String message, List<Fix> fixes, Position[] positions) {
        return createErrorDescription(message, ErrorDescriptionFactory.lazyListForFixes(fixes), positions);
    }

    public List<ErrorDescription> analyze() {
        List<ErrorDescription> errors = Collections.<ErrorDescription>emptyList();
        Tree node = currentPath.getLeaf();

        if (javac.getTreeUtilities().isSynthetic(currentPath) || !isValid(currentPath)) {
            return errors;
        }
        // check javadoc
        Element elm = javac.getTrees().getElement(currentPath);

        if (elm == null) {
            Logger.getLogger(Analyzer.class.getName()).log(
                    Level.INFO, "Cannot resolve element for " + node + " in " + file); // NOI18N
            return errors;
        } else if (isGuarded(node)) {
            return errors;
        }

        String jdText = javac.getElements().getDocComment(elm);
        // create hint descriptor + prepare javadoc
        if (jdText == null || jdText.length() == 0 && null == JavadocUtilities.findTokenSequence(javac, elm)) {
            if (!createJavadocKind)
                return errors;

            if (hasErrors(node) || JavadocUtilities.hasInheritedDoc(javac, elm)) {
                return errors;
            }

            try {
                Position[] positions = createSignaturePositions(node);
                if (positions == null) {
                    return errors;
                }
                ErrorDescription err = createErrorDescription(
                        NbBundle.getMessage(Analyzer.class, "MISSING_JAVADOC_DESC"), // NOI18N
                        createGenerateFixes(elm),
                        positions);
                errors = new ArrayList<ErrorDescription>();
                errors.add(err);
            } catch (BadLocationException ex) {
                Logger.getLogger(Analyzer.class.getName()).log(Level.INFO, ex.getMessage(), ex);
            }
        } else {
            if (createJavadocKind || hasErrors(node))
                return errors;

            errors = new ArrayList<ErrorDescription>();
            Doc jDoc = javac.getElementUtilities().javaDocFor(elm);
            if (jDoc.isMethod() || jDoc.isConstructor()) {
                ExecutableMemberDoc methDoc = (ExecutableMemberDoc) jDoc;
                ExecutableElement methodEl = (ExecutableElement) elm;
                MethodTree methodTree = (MethodTree) node;
                processTypeParameters(methodEl, methodTree, methDoc, errors);
                processParameters(methodEl, methodTree, methDoc, errors);
                processReturn(methodEl, methodTree, methDoc, errors);
                processThrows(methodEl, methodTree, methDoc, errors);
            } else if(jDoc.isClass() || jDoc.isInterface()) {
                TypeElement classEl = (TypeElement) elm;
                ClassDoc classDoc = (ClassDoc) jDoc;
                ClassTree classTree = (ClassTree) node;
                processTypeParameters(classEl, classTree, classDoc, errors);
                processParameters(classEl, classTree, classDoc, errors);
            } else if (jDoc.isAnnotationType()) {
                processAnnTypeParameters(elm, node, jDoc, errors);
            } else if (jDoc.isAnnotationTypeElement()) {
                AnnotationTypeElementDoc annDoc = (AnnotationTypeElementDoc) jDoc;
                ExecutableElement methodEl = (ExecutableElement) elm;
                MethodTree methodTree = (MethodTree) node;
                processAnnTypeParameters(methodEl, methodTree, annDoc, errors);
                processReturn(methodEl, methodTree, annDoc, errors);
                processAnnTypeThrows(methodEl, methodTree, annDoc, errors);
            }

            processDeprecatedAnnotation(elm, jDoc, errors);
        }
        return errors;
    }

    /**
     * has syntax errors preventing to generate javadoc?
     */
    static boolean hasErrors(Tree leaf) {
        switch (leaf.getKind()) {
            case METHOD:
                MethodTree mt = (MethodTree) leaf;
                Tree rt = mt.getReturnType();
                if (rt != null && rt.getKind() == Kind.ERRONEOUS) {
                    return true;
                }
                if (ERROR_IDENT.contentEquals(mt.getName())) {
                    return true;
                }
                for (VariableTree vt : mt.getParameters()) {
                    if (ERROR_IDENT.contentEquals(vt.getName())) {
                        return true;
                    }
                }
                for (Tree t : mt.getThrows()) {
                    if (t.getKind() == Kind.ERRONEOUS ||
                            (t.getKind() == Kind.IDENTIFIER && ERROR_IDENT.contentEquals(((IdentifierTree) t).getName()))) {
                        return true;
                    }
                }
                break;

            case VARIABLE:
                VariableTree vt = (VariableTree) leaf;
                return vt.getType().getKind() == Kind.ERRONEOUS
                        || ERROR_IDENT.contentEquals(vt.getName());

            case CLASS:
                ClassTree ct = (ClassTree) leaf;
                if (ERROR_IDENT.contentEquals(ct.getSimpleName())) {
                    return true;
                }
                for (TypeParameterTree tpt : ct.getTypeParameters()) {
                    if (ERROR_IDENT.contentEquals(tpt.getName())) {
                        return true;
                    }
                }
                break;

        }
        return false;
    }

    private boolean isValid(TreePath path) {
        Tree leaf = path.getLeaf();
        int caret = CaretAwareJavaSourceTaskFactory.getLastPosition(javac.getFileObject());
        boolean onLine = hintSeverity == HintSeverity.CURRENT_LINE_WARNING;
        switch (leaf.getKind()) {
        case CLASS:
            return access.isAccessible(javac, path, false)
                    && (!onLine || isInHeader(javac, (ClassTree) leaf, caret));
        case METHOD:
            return access.isAccessible(javac, path, false)
                    && (!onLine || isInHeader(javac, (MethodTree) leaf, caret));
        case VARIABLE:
            return access.isAccessible(javac, path, false);
        }
        return false;
    }

    private void processDeprecatedAnnotation(Element elm, Doc jDoc, List<ErrorDescription> errors) {
        if (SourceVersion.RELEASE_5.compareTo(spec) > 0) {
            // jdks older than 1.5 do not support annotations
            return;
        }

        Tag[] deprTags = jDoc.tags("@deprecated"); // NOI18N
        AnnotationMirror annMirror = JavadocUtilities.findDeprecated(javac, elm);

        if (annMirror != null) {
            // is deprecated
            if (deprTags.length == 0) {
                // missing tag
                try {
                    Position[] poss = createPositions(javac.getTrees().getTree(elm, annMirror));
                    ErrorDescription err = createErrorDescription(
                            NbBundle.getMessage(Analyzer.class, "MISSING_DEPRECATED_DESC"), // NOI18N
                            Collections.<Fix>singletonList(AddTagFix.createAddDeprecatedTagFix(elm, file, spec)),
                            poss);
                    addTagHint(errors, err);
                } catch (BadLocationException ex) {
                    Logger.getLogger(Analyzer.class.getName()).log(Level.INFO, ex.getMessage(), ex);
                }
            } else if (deprTags.length > 1) {
                // duplicates
                boolean isFirst = true;
                for (Tag tag : deprTags) {
                    if (isFirst) {
                        isFirst = false;
                        continue;
                    }
                    addRemoveTagFix(tag,
                            NbBundle.getMessage(Analyzer.class, "DUPLICATE_DEPRECATED_DESC"), // NOI18N
                            elm, errors);
                }
            }
        } else {
            // not annotated
            if (deprTags.length > 1) {
                // duplicates
                boolean isFirst = true;
                for (Tag tag : deprTags) {
                    if (isFirst) {
                        isFirst = false;
                        continue;
                    }
                    addRemoveTagFix(tag,
                            NbBundle.getMessage(Analyzer.class, "DUPLICATE_DEPRECATED_DESC"), // NOI18N
                            elm, errors);
                }
            }
            if (deprTags.length > 0) {
                // XXX ignore for now; we could offer to annotate the element if @deprecate tag exists
                // or remove tag
            }
        }
    }

    private void processReturn(ExecutableElement exec, MethodTree node, ExecutableMemberDoc jdoc, List<ErrorDescription> errors) {
        final TypeMirror returnType = exec.getReturnType();
        final Tree returnTree = node.getReturnType();
        final Tag[] tags = jdoc.tags("@return"); // NOI18N

        if (returnType.getKind() == TypeKind.VOID) {
            // void has @return
            for (int i = 0; i < tags.length; i++) {
                Tag tag = tags[i];
                addRemoveTagFix(tag,
                        NbBundle.getMessage(Analyzer.class,
                        jdoc.isMethod()? "WRONG_RETURN_DESC": "WRONG_CONSTRUCTOR_RETURN_DESC"), // NOI18N
                        exec, errors);
            }
        } else {
            for (int i = 0; i < tags.length; i++) {
                // check duplicate @return
                Tag tag = tags[i];
                if (i > 0) {
                    addRemoveTagFix(tag,
                            NbBundle.getMessage(Analyzer.class, "DUPLICATE_RETURN_DESC"), // NOI18N
                            exec, errors);
                }
            }
        }

        if (returnType.getKind() != TypeKind.VOID && tags.length == 0 &&
                JavadocUtilities.findReturnTag(javac, (MethodDoc) jdoc, true) == null) {
            // missing @return
            try {
                Position[] poss = createPositions(returnTree);
                ErrorDescription err = createErrorDescription(
                        NbBundle.getMessage(Analyzer.class, "MISSING_RETURN_DESC"), // NOI18N
                        Collections.<Fix>singletonList(AddTagFix.createAddReturnTagFix(exec, file, spec)),
                        poss);
                addTagHint(errors, err);
            } catch (BadLocationException ex) {
                Logger.getLogger(Analyzer.class.getName()).log(Level.INFO, ex.getMessage(), ex);
            }
        }

    }

    private void processThrows(ExecutableElement exec, MethodTree node, ExecutableMemberDoc jdoc, List<ErrorDescription> errors) {
        final List<? extends ExpressionTree> throwz = node.getThrows();
        final ThrowsTag[] tags = jdoc.throwsTags();

        Map<String, ThrowsTag> tagNames = new HashMap<String, ThrowsTag>();
        for (ThrowsTag throwsTag : tags) {
            com.sun.javadoc.Type tagType = throwsTag.exceptionType();
            String tagFQN = null;
            if (tagType != null) { // unresolvable type
                tagFQN = throwsTag.exceptionType().qualifiedTypeName();
            } else {
                tagFQN = throwsTag.exceptionName();
            }
            if (tagNames.containsKey(tagFQN)) {
                // duplicate throws error
                addRemoveTagFix(throwsTag,
                        NbBundle.getMessage(Analyzer.class, "DUPLICATE_THROWS_DESC", throwsTag.name(), throwsTag.exceptionName()), // NOI18N
                        exec, errors);
            } else {
                tagNames.put(tagFQN, throwsTag);
            }
        }

        // resolve existing and missing tags
        int index = 0;
        for (ExpressionTree throwTree : throwz) {
            TreePath path = new TreePath(currentPath, throwTree);
            Element el = javac.getTrees().getElement(path);
            String fqn;
            if (ElementKind.CLASS == el.getKind()) {
                TypeElement tel = (TypeElement) el;
                fqn = tel.getQualifiedName().toString();
            } else if (ElementKind.TYPE_PARAMETER == el.getKind()) {
                // ExceptionType of throws clause may contain TypeVariable see JLS 8.4.6
                fqn = el.getSimpleName().toString();
            } else {
                // skip processing of invalid throws declaration
                Logger.getLogger(Analyzer.class.getName()).log(
                        Level.FINE,
                        "Illegal throw kind: {0} of {1} in {2}", // NOI18N
                        new Object[] {el.getKind(), el, exec});
                return;
            }

            boolean exists = tagNames.remove(fqn) != null;
            if (!exists && (jdoc.isConstructor() ||
                    jdoc.isMethod() &&
                    JavadocUtilities.findThrowsTag(javac, (MethodDoc) jdoc, fqn, true) == null)) {
                // missing @throws
                try {
                    Position[] poss = createPositions(throwTree);
                    String insertName = resolveThrowsName(el, fqn, throwTree);
                    ErrorDescription err = createErrorDescription(
                            NbBundle.getMessage(Analyzer.class, "MISSING_THROWS_DESC", fqn), // NOI18N
                            Collections.<Fix>singletonList(AddTagFix.createAddThrowsTagFix(exec, insertName, index, file, spec)),
                            poss);
                    addTagHint(errors, err);
                } catch (BadLocationException ex) {
                    Logger.getLogger(Analyzer.class.getName()).log(Level.INFO, ex.getMessage(), ex);
                }
            }
            ++index;
        }

        TypeMirror rtException = javac.getElements().getTypeElement("java.lang.RuntimeException").asType(); // NOI18N

        // resolve leftovers
        for (ThrowsTag throwsTag : tagNames.values()) {
            // redundant @throws
            com.sun.javadoc.Type throwsType = throwsTag.exceptionType();
            Doc throwClassDoc = null;
            if (throwsType != null) {
                throwClassDoc = throwsType.asClassDoc();
            }
            if (throwClassDoc != null) {
                Element throwEl = javac.getElementUtilities().elementFor(throwClassDoc);
                if (throwEl != null && javac.getTypes().isSubtype(throwEl.asType(), rtException)) {
                    // ignore RuntimeExceptions
                    break;
                }
            }
            addRemoveTagFix(throwsTag,
                    NbBundle.getMessage(Analyzer.class, "UNKNOWN_THROWABLE_DESC", throwsTag.name(), throwsTag.exceptionName()), // NOI18N
                    exec, errors);
        }

    }

    private void processAnnTypeThrows(ExecutableElement exec, MethodTree node, AnnotationTypeElementDoc jdoc, List<ErrorDescription> errors) {
        // this surprisingly gets both @throws and @exception tags
        Tag[] tags = jdoc.tags("@throws"); //NOI18N

        for (Tag tag : tags) {
            // annotation type element cannot contain throwables
            ThrowsTag throwsTag = (ThrowsTag) tag;
            addRemoveTagFix(throwsTag,
                    NbBundle.getMessage(Analyzer.class, "ILLEGAL_ANNOTATION_TYPE_THROWS_DESC", // NOI18N
                    throwsTag.name(),
                    throwsTag.exceptionName()),
                    exec, errors);
        }
    }

    private void processAnnTypeParameters(Element elm, Tree node, Doc jdoc, List<ErrorDescription> errors) {
        final Tag[] tags = jdoc.tags("@param"); //NOI18N

        for (Tag tag : tags) {
            // annotation type element cannot contain params
            ParamTag paramTag = (ParamTag) tag;
            addRemoveTagFix(paramTag,
                    NbBundle.getMessage(Analyzer.class, "ILLEGAL_ANNOTATION_TYPE_PARAM_DESC", paramTag.parameterName()), // NOI18N
                    elm, errors);
        }
    }

    private void processParameters(ExecutableElement exec, MethodTree node, ExecutableMemberDoc jdoc, List<ErrorDescription> errors) {
        final List<? extends VariableTree> params = node.getParameters();
        final ParamTag[] tags = jdoc.paramTags();

        Map<String, ParamTag> tagNames = new HashMap<String, ParamTag>();
        // create param tag names set and reveal duplicates
        for (ParamTag paramTag : tags) {
            if (tagNames.containsKey(paramTag.parameterName())) {
                // duplicate @param error
                addRemoveTagFix(paramTag,
                        NbBundle.getMessage(Analyzer.class, "DUPLICATE_PARAM_DESC", paramTag.parameterName()), // NOI18N
                        exec, errors);
            } else {
                tagNames.put(paramTag.parameterName(), paramTag);
            }
        }

        // resolve existing and missing tags
        for (VariableTree param : params) {
            boolean exists = tagNames.remove(param.getName().toString()) != null;
            if (!exists && (jdoc.isConstructor() ||
                    jdoc.isMethod() &&
                    JavadocUtilities.findParamTag(javac, (MethodDoc) jdoc, param.getName().toString(), false, true) == null)) {
                // missing @param
                try {
                    Position[] poss = createPositions(param);
                    ErrorDescription err = createErrorDescription(
                            NbBundle.getMessage(Analyzer.class, "MISSING_PARAM_DESC", param.getName()), // NOI18N
                            Collections.<Fix>singletonList(AddTagFix.createAddParamTagFix(exec, param.getName().toString(), file, spec)),
                            poss);
                    addTagHint(errors, err);
                } catch (BadLocationException ex) {
                    Logger.getLogger(Analyzer.class.getName()).log(Level.INFO, ex.getMessage(), ex);
                }
            }
        }

        // resolve leftovers
        for (ParamTag paramTag : tagNames.values()) {
            // redundant @param
            addRemoveTagFix(paramTag,
                    NbBundle.getMessage(Analyzer.class, "UNKNOWN_PARAM_DESC", paramTag.parameterName()), // NOI18N
                    exec, errors);
        }

    }

    private void processParameters(TypeElement elm, ClassTree node, ClassDoc jdoc, List<ErrorDescription> errors) {
        // other than type parameters are unsupported in class javadoc
        for (Tag tag : jdoc.tags("@param")) { // NOI18N
            ParamTag paramTag = (ParamTag) tag;
            if (!paramTag.isTypeParameter()) {
                // redundant @param
                addRemoveTagFix(paramTag,
                        NbBundle.getMessage(Analyzer.class, "UNKNOWN_PARAM_DESC", paramTag.parameterName()), // NOI18N
                        elm, errors);
            }
        }
    }

    private void processTypeParameters(TypeElement elm, ClassTree node, ClassDoc jdoc, List<ErrorDescription> errors) {
        processTypeParameters(elm, node.getTypeParameters(), jdoc.typeParamTags(), jdoc, errors);
    }

    private void processTypeParameters(ExecutableElement elm, MethodTree node, ExecutableMemberDoc jdoc, List<ErrorDescription> errors) {
        processTypeParameters(elm, node.getTypeParameters(), jdoc.typeParamTags(), jdoc, errors);
    }
    
    private void processTypeParameters(Element elm, List<? extends TypeParameterTree> params, ParamTag[] tags, Doc jdoc, List<ErrorDescription> errors) {
        Map<String, ParamTag> tagNames = new HashMap<String, ParamTag>();
        // create param tag names set and reveal duplicates
        for (ParamTag paramTag : tags) {
            if (tagNames.containsKey(paramTag.parameterName())) {
                // duplicate @param error
                String typeParamName = '<' + paramTag.parameterName() + '>';
                addRemoveTagFix(paramTag,
                        NbBundle.getMessage(Analyzer.class, "DUPLICATE_TYPEPARAM_DESC", typeParamName), // NOI18N
                        elm, errors);
            } else {
                tagNames.put(paramTag.parameterName(), paramTag);
            }
        }

        // resolve existing and missing tags
        for (TypeParameterTree param : params) {
            boolean exists = tagNames.remove(param.getName().toString()) != null;
            if (!exists && (!jdoc.isMethod() || jdoc.isMethod() &&
                    JavadocUtilities.findParamTag(javac, (MethodDoc) jdoc, param.getName().toString(), true, true) == null)) {
                // missing @param
                try {
                    Position[] poss = createPositions(param);
                    String paramName = param.getName().toString();
                    String typeParamName = '<' + paramName + '>';
                    ErrorDescription err = createErrorDescription(
                            NbBundle.getMessage(Analyzer.class, "MISSING_TYPEPARAM_DESC", typeParamName), // NOI18N
                            Collections.<Fix>singletonList(AddTagFix.createAddTypeParamTagFix(elm, paramName, file, spec)),
                            poss);
                    addTagHint(errors, err);
                } catch (BadLocationException ex) {
                    Logger.getLogger(Analyzer.class.getName()).log(Level.INFO, ex.getMessage(), ex);
                }
            }
        }

        // resolve leftovers
        for (ParamTag paramTag : tagNames.values()) {
            // redundant @param
            String typeParamName = '<' + paramTag.parameterName() + '>';
            addRemoveTagFix(paramTag,
                    NbBundle.getMessage(Analyzer.class, "UNKNOWN_TYPEPARAM_DESC", typeParamName), // NOI18N
                    elm, errors);
        }
    }

    Position[] createPositions(Tree t) throws BadLocationException {
        final Position[] poss = new Position[2];
        final int start = (int) javac.getTrees().getSourcePositions().
                getStartPosition(javac.getCompilationUnit(), t);
        final int end = (int) javac.getTrees().getSourcePositions().
                getEndPosition(javac.getCompilationUnit(), t);

        // XXX needs document lock?
        poss[0] = doc.createPosition(start);
        poss[1] = doc.createPosition(end);
        return poss;
    }

    /**
     * creates start and end positions of the tree
     */
    Position[] createSignaturePositions(final Tree t) throws BadLocationException {
        final Position[] pos = new Position[2];
        final BadLocationException[] blex = new BadLocationException[1];
        doc.render(new Runnable() {
            public void run() {
                try {
                    int[] span = null;
                    if (t.getKind() == Tree.Kind.METHOD) { // method + constructor
                        span = javac.getTreeUtilities().findNameSpan((MethodTree) t);
                    } else if (t.getKind() == Tree.Kind.CLASS) {
                        span = javac.getTreeUtilities().findNameSpan((ClassTree) t);
                    } else if (Tree.Kind.VARIABLE == t.getKind()) {
                        span = javac.getTreeUtilities().findNameSpan((VariableTree) t);
                    }

                    if (span != null) {
                        pos[0] = doc.createPosition(span[0]);
                        pos[1] = doc.createPosition(span[1]);
                    }
                } catch (BadLocationException ex) {
                    blex[0] = ex;
                }
            }
        });
        if (blex[0] != null)
            throw (BadLocationException) new BadLocationException(blex[0].getMessage(), blex[0].offsetRequested()).initCause(blex[0]);
        return pos[0] != null ? pos : null;
    }

    private boolean isGuarded(Tree node) {
        GuardedSectionManager guards = GuardedSectionManager.getInstance((StyledDocument) doc);
        if (guards != null) {
            try {
                final int startOff = (int) javac.getTrees().getSourcePositions().
                        getStartPosition(javac.getCompilationUnit(), node);
                final Position startPos = doc.createPosition(startOff);

                for (GuardedSection guard : guards.getGuardedSections()) {
                    if (guard.contains(startPos, false)) {
                        return true;
                    }
                }
            } catch (BadLocationException ex) {
                Logger.getLogger(Analyzer.class.getName()).log(Level.INFO, ex.getMessage(), ex);
                // consider it as guarded
                return true;
            }
        }
        return false;
    }

    private void addRemoveTagFix(Tag tag, String description, Element elm, List<ErrorDescription> errors) {
        try {
            Position[] poss = JavadocUtilities.findTagNameBounds(javac, doc, tag);
            if (poss == null) {
                throw new BadLocationException("no position for " + tag, -1); // NOI18N
            }
            ErrorDescription err = createErrorDescription(
                    description,
                    Collections.<Fix>singletonList(new RemoveTagFix(tag.name(), TagHandle.create(tag), ElementHandle.create(elm), file, spec)),
                    poss);
            addTagHint(errors, err);
        } catch (BadLocationException ex) {
            Logger.getLogger(Analyzer.class.getName()).log(Level.INFO, ex.getMessage(), ex);
        }
    }

    JavadocLazyFixList createGenerateFixes(Element elm) {
        List<Fix> fixes = new ArrayList<Fix>(3);
        ElementHandle handle = ElementHandle.create(elm);

        String description;
        if (elm.getKind() == ElementKind.CONSTRUCTOR) {
            description = elm.getEnclosingElement().getSimpleName().toString();
        } else {
            description = elm.getSimpleName().toString();
        }

        JavadocLazyFixList fixList = new JavadocLazyFixList(fixes, fixAll);

        GenerateJavadocFix jdFix = new GenerateJavadocFix(description, handle, javac.getFileObject(), this.spec);

        fixes.add(jdFix);
//            fixAll.addFix(jdFix);

        // XXX add Inherit javadoc

        //            Fix fixInherit = new JavadocFix("Inherit javadoc");
        //            fixes.add(fixInherit);
        //            fixes.add(new JavadocFix("Create missing javadoc"));
        //            fixes.add(new JavadocFix("Fix all missing javadocs"));
        return fixList;
    }

    private void addTagHint(List<ErrorDescription> errors, ErrorDescription desc) {
        errors.add(desc);
    }
    
    private static boolean isInHeader(CompilationInfo info, ClassTree tree, int offset) {
        CompilationUnitTree cut = info.getCompilationUnit();
        SourcePositions sp = info.getTrees().getSourcePositions();
        long lastKnownOffsetInHeader = sp.getStartPosition(cut, tree);
        
        List<? extends Tree> impls = tree.getImplementsClause();
        List<? extends TypeParameterTree> typeparams;
        if (impls != null && !impls.isEmpty()) {
            lastKnownOffsetInHeader= sp.getEndPosition(cut, impls.get(impls.size() - 1));
        } else if ((typeparams = tree.getTypeParameters()) != null && !typeparams.isEmpty()) {
            lastKnownOffsetInHeader= sp.getEndPosition(cut, typeparams.get(typeparams.size() - 1));
        } else if (tree.getExtendsClause() != null) {
            lastKnownOffsetInHeader = sp.getEndPosition(cut, tree.getExtendsClause());
        } else if (tree.getModifiers() != null) {
            lastKnownOffsetInHeader = sp.getEndPosition(cut, tree.getModifiers());
        }
        
        TokenSequence<JavaTokenId> ts = info.getTreeUtilities().tokensFor(tree);
        
        ts.move((int) lastKnownOffsetInHeader);
        
        while (ts.moveNext()) {
            if (ts.token().id() == JavaTokenId.LBRACE) {
                return offset < ts.offset();
            }
        }
        
        return false;
    }
    
    private static boolean isInHeader(CompilationInfo info, MethodTree tree, int offset) {
        CompilationUnitTree cut = info.getCompilationUnit();
        SourcePositions sp = info.getTrees().getSourcePositions();
        long lastKnownOffsetInHeader = sp.getStartPosition(cut, tree);
        
        List<? extends ExpressionTree> throwz;
        List<? extends VariableTree> params;
        List<? extends TypeParameterTree> typeparams;
        
        if ((throwz = tree.getThrows()) != null && !throwz.isEmpty()) {
            lastKnownOffsetInHeader = sp.getEndPosition(cut, throwz.get(throwz.size() - 1));
        } else if ((params = tree.getParameters()) != null && !params.isEmpty()) {
            lastKnownOffsetInHeader = sp.getEndPosition(cut, params.get(params.size() - 1));
        } else if ((typeparams = tree.getTypeParameters()) != null && !typeparams.isEmpty()) {
            lastKnownOffsetInHeader = sp.getEndPosition(cut, typeparams.get(typeparams.size() - 1));
        } else if (tree.getReturnType() != null) {
            lastKnownOffsetInHeader = sp.getEndPosition(cut, tree.getReturnType());
        } else if (tree.getModifiers() != null) {
            lastKnownOffsetInHeader = sp.getEndPosition(cut, tree.getModifiers());
        }
        
        TokenSequence<JavaTokenId> ts = info.getTreeUtilities().tokensFor(tree);
        
        ts.move((int) lastKnownOffsetInHeader);
        
        while (ts.moveNext()) {
            if (ts.token().id() == JavaTokenId.LBRACE || ts.token().id() == JavaTokenId.SEMICOLON) {
                return offset < ts.offset();
            }
        }
        
        return false;
    }
    
    static SourceVersion resolveSourceVersion(FileObject file) {
        String sourceLevel = SourceLevelQuery.getSourceLevel(file);
        if (sourceLevel == null) {
            return SourceVersion.latest();
        } else if (sourceLevel.startsWith("1.6")) {
            return SourceVersion.RELEASE_6;
        } else if (sourceLevel.startsWith("1.5")) {
            return SourceVersion.RELEASE_5;
        } else if (sourceLevel.startsWith("1.4")) {
            return SourceVersion.RELEASE_4;
        } else if (sourceLevel.startsWith("1.3")) {
            return SourceVersion.RELEASE_3;
        } else if (sourceLevel.startsWith("1.2")) {
            return SourceVersion.RELEASE_2;
        } else if (sourceLevel.startsWith("1.1")) {
            return SourceVersion.RELEASE_1;
        } else if (sourceLevel.startsWith("1.0")) {
            return SourceVersion.RELEASE_0;
        }
        
        return SourceVersion.latest();
        
    }
    
    private static final class JavadocLazyFixList implements LazyFixList {
        
        private List<Fix> contexFixes;
        private FixAll fixAll;
        
        public JavadocLazyFixList(List<Fix> contexFixes, FixAll fixAll) {
            this.contexFixes = contexFixes;
            this.fixAll = fixAll;
        }
        
        public void addPropertyChangeListener(PropertyChangeListener l) {
        }
        
        public void removePropertyChangeListener(PropertyChangeListener l) {
        }
        
        public boolean probablyContainsFixes() {
            return true;
        }
        
        public List<Fix> getFixes() {
            if (fixAll.isReady()) {
                contexFixes.add(fixAll);
            }
            return contexFixes;
        }
        
        public boolean isComputed() {
            return true;
        }
        
    }

    /**
     * computes name of throws clause to work around
     * <a href="http://www.netbeans.org/issues/show_bug.cgi?id=160414">issue 160414</a>.
     */
    private String resolveThrowsName(Element el, String fqn, ExpressionTree throwTree) {
        boolean nestedClass = ElementKind.CLASS == el.getKind()
                && NestingKind.TOP_LEVEL != ((TypeElement) el).getNestingKind();
        String insertName = nestedClass ? fqn : throwTree.toString();
        return insertName;
    }
    
}
