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

import com.sun.javadoc.AnnotationTypeElementDoc;
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
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.guards.GuardedSection;
import org.netbeans.api.editor.guards.GuardedSectionManager;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.queries.AccessibilityQuery;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.Comment;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.java.source.support.CaretAwareJavaSourceTaskFactory;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.java.hints.spi.AbstractHint;
import org.netbeans.modules.javadoc.hints.JavadocUtilities.TagHandle;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.LazyFixList;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.awt.Mnemonics;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
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
 * @author Jan Pokorsky
 */
public final class JavadocHintProvider extends AbstractHint {
    
    private static final int NOPOS = -2; // XXX copied from jackpot; should be in api
    
    private static final String DEFAULT_PROFILE = "default"; // NOI18N
    private static final String HINTS = "hints"; // NOI18N
    
    public static final String SCOPE_KEY = "scope";             // NOI18N
    public static final String SCOPE_DEFAULT = "protected"; // NOI18N

    private boolean createJavadocKind;
    
    private JavadocHintProvider(boolean createJavadocKind) {
        super( false, true, createJavadocKind ? AbstractHint.HintSeverity.CURRENT_LINE_WARNING : AbstractHint.HintSeverity.WARNING );
        this.createJavadocKind = createJavadocKind;
    }
    
    public Set<Kind> getTreeKinds() {
        return EnumSet.of(Kind.METHOD, Kind.CLASS, Kind.VARIABLE);
    }
    
    public List<ErrorDescription> run(CompilationInfo javac, TreePath path) {
        if (Boolean.FALSE.equals(AccessibilityQuery.isPubliclyAccessible(javac.getFileObject().getParent()))) {
            return null;
        }
        
        HintSeverity hintSeverity = getSeverity();
        Severity severity = hintSeverity.toEditorSeverity();
                
        Document doc = null;
        
        try {
            doc = javac.getDocument();
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }
        
        if (doc == null) {
            return null;
        }
        
        Analyzer a = new Analyzer(javac, doc, path, severity, hintSeverity);
        return a.analyze();
    }

    public void cancel() {
        //XXX implement me;
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
    
    private static SourceVersion resolveSourceVersion(FileObject file) {
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
    
    private final class Analyzer {
        
        private static final String ERROR_IDENT = "<error>"; // NOI18N
        
        private final CompilationInfo javac;
        private final SourceVersion spec;
        private final FixAll fixAll = new FixAll();
        private final Document doc;
        private final FileObject file;
        private final Severity severity;
        private final HintSeverity hintSeverity;
        private final TreePath currentPath;
        
        Analyzer(CompilationInfo javac, Document doc, TreePath currentPath, Severity severity, HintSeverity hintSeverity) {
            this.javac = javac;
            this.doc = doc;
            this.file = javac.getFileObject();
            this.currentPath = currentPath;
            this.severity = severity;
            this.hintSeverity = hintSeverity;
            this.spec = resolveSourceVersion(javac.getFileObject());
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
                Logger.getLogger(JavadocHintProvider.class.getName()).log(
                        Level.INFO, "Cannot resolve element for " + node + " in " + file); // NOI18N
                return errors;
            } else if (isGuarded(node)) {
                return errors;
            }
            
            String jdText = javac.getElements().getDocComment(elm);
            // create hint descriptor + prepare javadoc
            if (jdText == null) {
                if (!createJavadocKind)
                    return errors;
                
                if (hasErrors(node) || JavadocUtilities.hasInheritedDoc(javac, elm)) {
                    return errors;
                }
                
                try {
                    Position[] positions = createSignaturePositions(node);
                    ErrorDescription err = createErrorDescription(
                            NbBundle.getMessage(JavadocHintProvider.class, "MISSING_JAVADOC_DESC"), // NOI18N
                            createGenerateFixes(elm),
                            positions);
                    errors = new ArrayList<ErrorDescription>();
                    errors.add(err);
                } catch (BadLocationException ex) {
                    Logger.getLogger(JavadocHintProvider.class.getName()).log(Level.INFO, ex.getMessage(), ex);
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
                    processParameters(methodEl, methodTree, methDoc, errors);
                    processReturn(methodEl, methodTree, methDoc, errors);
                    processThrows(methodEl, methodTree, methDoc, errors);
                } else if(jDoc.isClass() || jDoc.isInterface()) {
                    TypeElement classEl = (TypeElement) elm;
                    ClassDoc classDoc = (ClassDoc) jDoc;
                    ClassTree classTree = (ClassTree) node;
                    processTypeParameters(classEl, classTree, classDoc, errors);
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
        private boolean hasErrors(Tree leaf) {
            switch (leaf.getKind()) {
                case METHOD:
                    MethodTree mt = (MethodTree) leaf;
                    Tree rt = mt.getReturnType();
                    if (rt != null && rt.getKind() == Kind.ERRONEOUS) {
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
            Access access = Access.resolve(getPreferences(null).get(SCOPE_KEY, SCOPE_DEFAULT));
            Tree leaf = path.getLeaf();
            int caret = CaretAwareJavaSourceTaskFactory.getLastPosition(javac.getFileObject());
            boolean onLine = hintSeverity == HintSeverity.CURRENT_LINE_WARNING;
            switch (leaf.getKind()) {
            case CLASS:
                return access.isAccessible(javac, path, !onLine)
                        && (!onLine || isInHeader(javac, (ClassTree) leaf, caret));
            case METHOD:
                return access.isAccessible(javac, path, !onLine)
                        && (!onLine || isInHeader(javac, (MethodTree) leaf, caret));
            case VARIABLE:
                return access.isAccessible(javac, path, !onLine);
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
                                NbBundle.getMessage(JavadocHintProvider.class, "MISSING_DEPRECATED_DESC"), // NOI18N
                                Collections.<Fix>singletonList(AddTagFix.createAddDeprecatedTagFix(elm, file, spec)),
                                poss);
                        addTagHint(errors, err);
                    } catch (BadLocationException ex) {
                        Logger.getLogger(JavadocHintProvider.class.getName()).log(Level.INFO, ex.getMessage(), ex);
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
                                NbBundle.getMessage(JavadocHintProvider.class, "DUPLICATE_DEPRECATED_DESC"), // NOI18N
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
                                NbBundle.getMessage(JavadocHintProvider.class, "DUPLICATE_DEPRECATED_DESC"), // NOI18N
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
                            NbBundle.getMessage(JavadocHintProvider.class,
                            jdoc.isMethod()? "WRONG_RETURN_DESC": "WRONG_CONSTRUCTOR_RETURN_DESC"), // NOI18N
                            exec, errors);
                }
            } else {
                for (int i = 0; i < tags.length; i++) {
                    // check duplicate @return
                    Tag tag = tags[i];
                    if (i > 0) {
                        addRemoveTagFix(tag,
                                NbBundle.getMessage(JavadocHintProvider.class, "DUPLICATE_RETURN_DESC"), // NOI18N
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
                            NbBundle.getMessage(JavadocHintProvider.class, "MISSING_RETURN_DESC"), // NOI18N
                            Collections.<Fix>singletonList(AddTagFix.createAddReturnTagFix(exec, file, spec)),
                            poss);
                    addTagHint(errors, err);
                } catch (BadLocationException ex) {
                    Logger.getLogger(JavadocHintProvider.class.getName()).log(Level.INFO, ex.getMessage(), ex);
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
                            NbBundle.getMessage(JavadocHintProvider.class, "DUPLICATE_THROWS_DESC", throwsTag.name(), throwsTag.exceptionName()), // NOI18N
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
                    throw new IllegalStateException("Illegal kind: " + el.getKind()); // NOI18N
                }
                
                boolean exists = tagNames.remove(fqn) != null;
                if (!exists && (jdoc.isConstructor() ||
                        jdoc.isMethod() &&
                        JavadocUtilities.findThrowsTag(javac, (MethodDoc) jdoc, fqn, true) == null)) {
                    // missing @throws
                    try {
                        Position[] poss = createPositions(throwTree);
                        ErrorDescription err = createErrorDescription(
                                NbBundle.getMessage(JavadocHintProvider.class, "MISSING_THROWS_DESC", fqn), // NOI18N
                                Collections.<Fix>singletonList(AddTagFix.createAddThrowsTagFix(exec, fqn, index, file, spec)),
                                poss);
                        addTagHint(errors, err);
                    } catch (BadLocationException ex) {
                        Logger.getLogger(JavadocHintProvider.class.getName()).log(Level.INFO, ex.getMessage(), ex);
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
                        NbBundle.getMessage(JavadocHintProvider.class, "UNKNOWN_THROWABLE_DESC", throwsTag.name(), throwsTag.exceptionName()), // NOI18N
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
                        NbBundle.getMessage(JavadocHintProvider.class, "ILLEGAL_ANNOTATION_TYPE_THROWS_DESC", // NOI18N
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
                        NbBundle.getMessage(JavadocHintProvider.class, "ILLEGAL_ANNOTATION_TYPE_PARAM_DESC", paramTag.parameterName()), // NOI18N
                        elm, errors);
            }
        }
        
        private void processParameters(ExecutableElement exec, MethodTree node, ExecutableMemberDoc jdoc, List<ErrorDescription> errors) {
            final List<? extends VariableTree> params = node.getParameters();
            //            final ParamTag[] tags = doc.paramTags();
            final Tag[] tags = jdoc.tags("@param"); //NOI18N
            
            Map<String, ParamTag> tagNames = new HashMap<String, ParamTag>();
            // create param tag names set and reveal duplicates
            for (Tag tag : tags) {
                ParamTag paramTag = (ParamTag) tag;
                if (paramTag.isTypeParameter()) {
                    // javadoc does not support type parameters of methods yet
                    // and isTypeParameter does not seem to be working. Let's
                    // work around this as leftover params below.
                    continue;
                }
                
                if (tagNames.containsKey(paramTag.parameterName())) {
                    // duplicate @param error
                    addRemoveTagFix(paramTag,
                            NbBundle.getMessage(JavadocHintProvider.class, "DUPLICATE_PARAM_DESC", paramTag.parameterName()), // NOI18N
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
                        JavadocUtilities.findParamTag(javac, (MethodDoc) jdoc, param.getName().toString(), true) == null)) {
                    // missing @param
                    try {
                        Position[] poss = createPositions(param);
                        ErrorDescription err = createErrorDescription(
                                NbBundle.getMessage(JavadocHintProvider.class, "MISSING_PARAM_DESC", param.getName()), // NOI18N
                                Collections.<Fix>singletonList(AddTagFix.createAddParamTagFix(exec, param.getName().toString(), file, spec)),
                                poss);
                        addTagHint(errors, err);
                    } catch (BadLocationException ex) {
                        Logger.getLogger(JavadocHintProvider.class.getName()).log(Level.INFO, ex.getMessage(), ex);
                    }
                }
            }
            
            // resolve leftovers
            for (ParamTag paramTag : tagNames.values()) {
                // XXX workaround: check if not type param
                boolean isTypeParam = false;
                for (TypeParameterElement typeParameterElement : exec.getTypeParameters()) {
                    if (paramTag.parameterName().equals(typeParameterElement.getSimpleName().toString())) {
                        isTypeParam = true;
                        break;
                    }
                }
                if (isTypeParam) {
                    continue;
                }
                // end of workaround
                
                // redundant @param
                addRemoveTagFix(paramTag,
                        NbBundle.getMessage(JavadocHintProvider.class, "UNKNOWN_PARAM_DESC", paramTag.parameterName()), // NOI18N
                        exec, errors);
            }
            
        }
        
        private void processTypeParameters(TypeElement elm, ClassTree node, ClassDoc jdoc, List<ErrorDescription> errors) {
            final List<? extends TypeParameterTree> params = node.getTypeParameters();
            //            final ParamTag[] tags = doc.typeParamTags();
            final Tag[] tags = jdoc.tags("@param"); // NOI18N
            
            Map<String, ParamTag> tagNames = new HashMap<String, ParamTag>();
            // create param tag names set and reveal duplicates
            for (Tag tag : tags) {
                ParamTag paramTag = (ParamTag) tag;
                if (tagNames.containsKey(paramTag.parameterName())) {
                    // duplicate @param error
                    addRemoveTagFix(paramTag,
                            NbBundle.getMessage(JavadocHintProvider.class, "DUPLICATE_TYPEPARAM_DESC", paramTag.parameterName()), // NOI18N
                            elm, errors);
                } else {
                    tagNames.put(paramTag.parameterName(), paramTag);
                }
            }
            
            // resolve existing and missing tags
            for (TypeParameterTree param : params) {
                boolean exists = tagNames.remove(param.getName().toString()) != null;
                if (!exists /*&& doc.isMethod() &&
                        JavadocUtilities.findParamTag((MethodDoc) doc, param.getName().toString(), true) == null*/) {
                    // missing @param
                    try {
                        Position[] poss = createPositions(param);
                        ErrorDescription err = createErrorDescription(
                                NbBundle.getMessage(JavadocHintProvider.class, "MISSING_TYPEPARAM_DESC", param.getName()), // NOI18N
                                Collections.<Fix>singletonList(AddTagFix.createAddTypeParamTagFix(elm, param.getName().toString(), file, spec)),
                                poss);
                        addTagHint(errors, err);
                    } catch (BadLocationException ex) {
                        Logger.getLogger(JavadocHintProvider.class.getName()).log(Level.INFO, ex.getMessage(), ex);
                    }
                }
            }
            
            // resolve leftovers
            for (ParamTag paramTag : tagNames.values()) {
                // redundant @param
                addRemoveTagFix(paramTag,
                        NbBundle.getMessage(JavadocHintProvider.class, "UNKNOWN_TYPEPARAM_DESC", paramTag.parameterName()), // NOI18N
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
            NbDocument.runAtomic((StyledDocument) doc, new Runnable() {
                public void run() {
                    try {
                        TokenSequence<JavaTokenId> tseq = null;
                        if (t.getKind() == Tree.Kind.METHOD) { // method + constructor
                            tseq = JavadocUtilities.findMethodNameToken(javac, (ClassTree) currentPath.getParentPath().getLeaf(), (MethodTree) t);
                        } else if (t.getKind() == Tree.Kind.CLASS) {
                            tseq = JavadocUtilities.findClassNameToken(javac, (ClassTree) t);
                        } else if (Tree.Kind.VARIABLE == t.getKind()) {
                            tseq = JavadocUtilities.findVariableNameToken(javac, (VariableTree) t,
                                    javac.getTreeUtilities().isEnum((ClassTree) currentPath.getParentPath().getLeaf()));
                        }
                        
                        if (tseq != null) {
                            pos[0] = doc.createPosition(tseq.offset());
                            pos[1] = doc.createPosition(tseq.offset() + tseq.token().length());
                            return;
                        }
                        
                        assert true: t.toString();
                    } catch (BadLocationException ex) {
                        blex[0] = ex;
                    }
                }
            });
            if (blex[0] != null)
                throw (BadLocationException) new BadLocationException(blex[0].getMessage(), blex[0].offsetRequested()).initCause(blex[0]);
            return pos;
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
                    Logger.getLogger(JavadocHintProvider.class.getName()).log(Level.INFO, ex.getMessage(), ex);
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
                Logger.getLogger(JavadocHintProvider.class.getName()).log(Level.INFO, ex.getMessage(), ex);
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
    
    private static final class FixAll implements Fix {
        
        private List<GenerateJavadocFix> allJavadocFixes = new ArrayList<GenerateJavadocFix>();
        
        public void addFix(GenerateJavadocFix f) {
            allJavadocFixes.add(f);
        }
        
        public boolean isReady() {
            return allJavadocFixes.size() > 1;
        }
        
        public String getText() {
            return NbBundle.getMessage(JavadocHintProvider.class, "FIX_ALL_HINT"); // NOI18N
        }
        
        public ChangeInfo implement() {
            for (GenerateJavadocFix javadocFix : allJavadocFixes) {
                javadocFix.implement(false);
            }
            
            return null;
        }
        
    }
    
    private static final class GenerateJavadocFix implements Fix {
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
            return NbBundle.getMessage(JavadocHintProvider.class, "MISSING_JAVADOC_HINT", name); // NOI18N
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
                Logger.getLogger(JavadocHintProvider.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
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
                            //                            System.out.println("javadoc:'" + iJavadoc + '\'');
                            int offset = iJavadoc.indexOf("\n");
                            //                            System.out.println("offset1: " + offset);
                            offset = iJavadoc.indexOf("\n", offset + 1);
                            //                            System.out.println("offset2: " + offset);
                            offset = position.getOffset() + offset - iJavadoc.length();
                            if (open) doOpen(file, offset);
                        } catch (BadLocationException ex) {
                            ex.printStackTrace();
                        }
                    }
                });
            } catch (BadLocationException ex) {
                Logger.getLogger(JavadocHintProvider.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
            return null;
        }
    }
    
    private static final class RemoveTagFix implements Fix, CancellableTask<WorkingCopy> {
        
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
                
                final Document doc = ci.getDocument();
                if (doc == null) {
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
            if (t != null) {
                removeTag(wc, elm);
                doc = wc.getDocument();
            }
        }
        
    }
    
    private static final class AddTagFix implements Fix, CancellableTask<WorkingCopy> {
        
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
            return NbBundle.getMessage(JavadocHintProvider.class, descKey, this.paramName);
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
                doOpen(file, open);
            } catch (BadLocationException ex) {
                Logger.getLogger(JavadocHintProvider.class.getName()).
                        log(Level.SEVERE, ex.getMessage(), ex);
            } catch (IOException ex) {
                Logger.getLogger(JavadocHintProvider.class.getName()).
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
                        Logger.getLogger(JavadocHintProvider.class.getName()).
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
                        Logger.getLogger(JavadocHintProvider.class.getName()).
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
    
    private static void doOpen(final FileObject fo, final int offset) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                doOpenImpl(fo, offset);
            }
        });
    }
    
    private static boolean doOpenImpl(FileObject fo, int offset) {
        try {
            DataObject od = DataObject.find(fo);
            EditorCookie ec = od.getCookie(EditorCookie.class);
            LineCookie lc = od.getCookie(LineCookie.class);
            
            if (ec != null && lc != null && offset != -1) {
                StyledDocument doc = ec.openDocument();
                if (doc != null) {
                    int line = NbDocument.findLineNumber(doc, offset);
                    int lineOffset = NbDocument.findLineOffset(doc, line);
                    int column = offset - lineOffset;
                    
                    if (line != -1) {
                        Line l = lc.getLineSet().getCurrent(line);
                        
                        if (l != null) {
                            l.show(Line.SHOW_GOTO, column);
                            return true;
                        }
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(JavadocHintProvider.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return false;
    }
    
    /**
     * according to http://java.sun.com/javase/6/docs/technotes/tools/solaris/javadoc.html#javadocoptions
     */
    private enum Access {
        PUBLIC, PROTECTED, PACKAGE, PRIVATE;
        
        /**
         * accept [public|protected|package|private], default(null or other) is protected
         */
        public static Access resolve(String s) {
            if (s != null) {
                s = s.trim().toLowerCase();
                if ("public".equals(s)) { // NOI18N
                    return Access.PUBLIC;
                } else if ("protected".equals(s)) { // NOI18N
                    return Access.PROTECTED;
                } else if ("private".equals(s)) { // NOI18N
                    return Access.PRIVATE;
                } else if ("package".equals(s)) { // NOI18N
                    return Access.PACKAGE;
                }
            }
            return Access.PROTECTED;
        }
        
        public boolean isAccessible(Set<Modifier> flags) {
            switch(this) {
            case PRIVATE:
                return true;
            case PACKAGE:
                return !flags.contains(Modifier.PRIVATE);
            case PROTECTED:
                return flags.contains(Modifier.PUBLIC) || flags.contains(Modifier.PROTECTED);
            case PUBLIC:
                return flags.contains(Modifier.PUBLIC);
            default:
                throw new IllegalStateException();
            }
        }
        
        /**
         * @param path path to check
         * @param alwaysAccessible true means to check if the path contains only class members;
         *                         false means to check besides class members also their modifiers
         * @return is accessible
         * @see #isAccessible(Set)
         */
        public boolean isAccessible(CompilationInfo javac, TreePath path, boolean alwaysAccessible) {
            TreePath parent = path.getParentPath();
            Tree leaf = path.getLeaf();
            if (parent != null) {
                Tree.Kind parentKind = parent.getLeaf().getKind();
                if (parentKind != Tree.Kind.CLASS && parentKind != Tree.Kind.COMPILATION_UNIT) {
                    // not class member
                    return false;
                }
                
                if (!isAccessible(javac, parent, alwaysAccessible)) {
                    return false;
                }
            }
            
            Set<Modifier> flags;
            switch (leaf.getKind()) {
            case COMPILATION_UNIT: return true;
            case CLASS: flags = ((ClassTree) leaf).getModifiers().getFlags(); break;
            case METHOD: flags = ((MethodTree) leaf).getModifiers().getFlags(); break;
            case VARIABLE: flags = ((VariableTree) leaf).getModifiers().getFlags(); break;
            default: return false;
            }
            
            // all members of interface and annotation type are public by definition (JLS 9.1.5)
            return alwaysAccessible || isInterfaceMember(javac, path) || isAccessible(flags);
        }
        
        /**
         * @return is member of interface or annotatin type
         */
        private boolean isInterfaceMember(CompilationInfo javac, TreePath path) {
            TreePath parentPath = path.getParentPath();
            if (parentPath == null) {
                return false;
            }
            Tree parent = parentPath.getLeaf();
            TreeUtilities utils = javac.getTreeUtilities();
            return Tree.Kind.CLASS == parent.getKind()
                    && (utils.isInterface((ClassTree) parent) || utils.isAnnotation((ClassTree) parent));
        }
    }
    
    public String getId() {
        return createJavadocKind ? "create-javadoc" : "error-in-javadoc"; //NOI18N //NOI18N
    }
    
    public String getDisplayName() {
        return NbBundle.getMessage(JavadocHintProvider.class, createJavadocKind ? "DN_CREATE_JAVADOC_HINT" : "DN_ERROR_IN_JAVADOC_HINT"); // NOI18N
    }
    
    public String getDescription() {
        return NbBundle.getMessage(JavadocHintProvider.class, createJavadocKind ? "DESC_CREATE_JAVADOC_HINT" : "DESC_ERROR_IN_JAVADOC_HINT"); // NOI18N
    }
        
    
    @Override
    public JComponent getCustomizer(final Preferences node) {
        JPanel outerPanel = new JPanel( new GridBagLayout() );
        outerPanel.setOpaque( false );
        
        JPanel res = new JPanel( new GridBagLayout() );
        res.setOpaque( false );
        res.setBorder( BorderFactory.createTitledBorder(NbBundle.getMessage(JavadocHintProvider.class, "LBL_SCOPE") ) ); //NOI18N
        ActionListener l = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JRadioButton rb = (JRadioButton)e.getSource();
                node.put( SCOPE_KEY, rb.getText() );
            }
        };
        ButtonGroup group = new ButtonGroup();
        
        int row = 0;
        JRadioButton radio = new JRadioButton();
        Mnemonics.setLocalizedText(radio, NbBundle.getMessage(JavadocHintProvider.class, "CTL_PUBLIC_OPTION")); // NOI18N
        radio.addActionListener( l );
        group.add( radio );
        radio.setSelected( radio.getText().equals( node.get(SCOPE_KEY, SCOPE_DEFAULT) ) );
        radio.setOpaque(false);
        res.add( radio, new GridBagConstraints(0,row++,1,1,0.0,0.0,GridBagConstraints.WEST,GridBagConstraints.HORIZONTAL, new Insets(8,8,0,8),0,0 ) );
        
        radio = new JRadioButton();
        Mnemonics.setLocalizedText(radio, NbBundle.getMessage(JavadocHintProvider.class, "CTL_PROTECTED_OPTION")); // NOI18N
        radio.addActionListener( l );
        group.add( radio );
        radio.setSelected( radio.getText().equals( node.get(SCOPE_KEY, SCOPE_DEFAULT) ) );
        radio.setOpaque(false);
        res.add( radio, new GridBagConstraints(0,row++,1,1,0.0,0.0,GridBagConstraints.WEST,GridBagConstraints.HORIZONTAL, new Insets(8,8,0,8),0,0 ) );
        
        radio = new JRadioButton();
        Mnemonics.setLocalizedText(radio, NbBundle.getMessage(JavadocHintProvider.class, "CTL_PACKAGE_OPTION")); // NOI18N
        radio.addActionListener( l );
        group.add( radio );
        radio.setSelected( radio.getText().equals( node.get(SCOPE_KEY, SCOPE_DEFAULT) ) );
        radio.setOpaque(false);
        res.add( radio, new GridBagConstraints(0,row++,1,1,0.0,0.0,GridBagConstraints.WEST,GridBagConstraints.HORIZONTAL, new Insets(8,8,0,8),0,0 ) );
        
        radio = new JRadioButton();
        Mnemonics.setLocalizedText(radio, NbBundle.getMessage(JavadocHintProvider.class, "CTL_PRIVATE_OPTION")); // NOI18N
        radio.addActionListener( l );
        group.add( radio );
        radio.setSelected( radio.getText().equals( node.get(SCOPE_KEY, SCOPE_DEFAULT) ) );
        radio.setOpaque(false);
        res.add( radio, new GridBagConstraints(0,row++,1,1,0.0,0.0,GridBagConstraints.WEST,GridBagConstraints.HORIZONTAL, new Insets(8,8,0,8),0,0 ) );
        
        outerPanel.add( res, new GridBagConstraints(0,0,1,1,0.0,0.0,GridBagConstraints.NORTHWEST,GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0 ) );
        outerPanel.add( new JLabel(), new GridBagConstraints(1,1,1,1,1.0,1.0,GridBagConstraints.NORTHWEST,GridBagConstraints.NONE, new Insets(0,0,0,0),0,0 ) );
        return outerPanel;
    }
    
    private static final String INITIALIZED = "javadoc-hint-initialized"; // NOI18N
    
    public static JavadocHintProvider createCreateJavadoc() {
        return new JavadocHintProvider(true);
    }
    
    public static JavadocHintProvider createErrorInJavadoc() {
        return new JavadocHintProvider(false);
    }
    
}
