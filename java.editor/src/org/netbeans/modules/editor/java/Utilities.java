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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.editor.java;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ErroneousTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import com.sun.source.util.TreeScanner;

import java.awt.Color;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.TypeUtilities.TypeNameOptions;
import org.netbeans.api.java.source.support.ReferencesCount;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.java.editor.javadoc.JavadocImports;
import org.netbeans.modules.java.editor.options.CodeCompletionPanel;
import org.netbeans.swing.plaf.LFCustoms;
import org.openide.filesystems.FileObject;
import org.openide.util.Pair;
import org.openide.util.WeakListeners;

/**
 *
 * @author Dusan Balek
 * @author Sam Halliday
 */
public final class Utilities {
    
    private static boolean guessMethodArguments = CodeCompletionPanel.GUESS_METHOD_ARGUMENTS_DEFAULT;
    private static boolean autoPopupOnJavaIdentifierPart = CodeCompletionPanel.JAVA_AUTO_POPUP_ON_IDENTIFIER_PART_DEFAULT;
    private static String javaCompletionAutoPopupTriggers = CodeCompletionPanel.JAVA_AUTO_COMPLETION_TRIGGERS_DEFAULT;
    private static String javaCompletionSelectors = CodeCompletionPanel.JAVA_COMPLETION_SELECTORS_DEFAULT;
    private static String javadocCompletionAutoPopupTriggers = CodeCompletionPanel.JAVADOC_AUTO_COMPLETION_TRIGGERS_DEFAULT;
    private static String javadocCompletionSelectors = CodeCompletionPanel.JAVADOC_COMPLETION_SELECTORS_DEFAULT;
    
    private static final AtomicBoolean inited = new AtomicBoolean(false);
    private static Preferences preferences;
    private static final PreferenceChangeListener preferencesTracker = new PreferenceChangeListener() {
        @Override
        public void preferenceChange(PreferenceChangeEvent evt) {
            String settingName = evt == null ? null : evt.getKey();
            if (settingName == null || CodeCompletionPanel.GUESS_METHOD_ARGUMENTS.equals(settingName)) {
                guessMethodArguments = preferences.getBoolean(CodeCompletionPanel.GUESS_METHOD_ARGUMENTS, CodeCompletionPanel.GUESS_METHOD_ARGUMENTS_DEFAULT);
            }
            if (settingName == null || CodeCompletionPanel.JAVA_AUTO_POPUP_ON_IDENTIFIER_PART.equals(settingName)) {
                autoPopupOnJavaIdentifierPart = preferences.getBoolean(CodeCompletionPanel.JAVA_AUTO_POPUP_ON_IDENTIFIER_PART, CodeCompletionPanel.JAVA_AUTO_POPUP_ON_IDENTIFIER_PART_DEFAULT);
            }
            if (settingName == null || CodeCompletionPanel.JAVA_AUTO_COMPLETION_TRIGGERS.equals(settingName)) {
                javaCompletionAutoPopupTriggers = preferences.get(CodeCompletionPanel.JAVA_AUTO_COMPLETION_TRIGGERS, CodeCompletionPanel.JAVA_AUTO_COMPLETION_TRIGGERS_DEFAULT);
            }
            if (settingName == null || CodeCompletionPanel.JAVA_COMPLETION_SELECTORS.equals(settingName)) {
                javaCompletionSelectors = preferences.get(CodeCompletionPanel.JAVA_COMPLETION_SELECTORS, CodeCompletionPanel.JAVA_COMPLETION_SELECTORS_DEFAULT);
            }
            if (settingName == null || CodeCompletionPanel.JAVADOC_AUTO_COMPLETION_TRIGGERS.equals(settingName)) {
                javadocCompletionAutoPopupTriggers = preferences.get(CodeCompletionPanel.JAVADOC_AUTO_COMPLETION_TRIGGERS, CodeCompletionPanel.JAVADOC_AUTO_COMPLETION_TRIGGERS_DEFAULT);
            }
            if (settingName == null || CodeCompletionPanel.JAVADOC_COMPLETION_SELECTORS.equals(settingName)) {
                javadocCompletionSelectors = preferences.get(CodeCompletionPanel.JAVADOC_COMPLETION_SELECTORS, CodeCompletionPanel.JAVADOC_COMPLETION_SELECTORS_DEFAULT);
            }
        }
    };
    
    public static boolean guessMethodArguments() {
        lazyInit();
        return guessMethodArguments;
    }

    public static boolean autoPopupOnJavaIdentifierPart() {
        lazyInit();
        return autoPopupOnJavaIdentifierPart;
    }

    public static String getJavaCompletionAutoPopupTriggers() {
        lazyInit();
        return javaCompletionAutoPopupTriggers;
    }

    public static String getJavaCompletionSelectors() {
        lazyInit();
        return javaCompletionSelectors;
    }

    public static String getJavadocCompletionAutoPopupTriggers() {
        lazyInit();
        return javadocCompletionAutoPopupTriggers;
    }

    public static String getJavadocCompletionSelectors() {
        lazyInit();
        return javadocCompletionSelectors;
    }

    private static void lazyInit() {
        if (inited.compareAndSet(false, true)) {
            preferences = MimeLookup.getLookup(JavaKit.JAVA_MIME_TYPE).lookup(Preferences.class);
            preferences.addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, preferencesTracker, preferences));
            preferencesTracker.preferenceChange(null);
        }
    }

    public static int getImportanceLevel(CompilationInfo info, ReferencesCount referencesCount, @NonNull Element element) {
        boolean isType = element.getKind().isClass() || element.getKind().isInterface();
        
        return getImportanceLevel(referencesCount, isType ? ElementHandle.create((TypeElement) element) : ElementHandle.create((TypeElement) element.getEnclosingElement()));
    }
    
    public static int getImportanceLevel(ReferencesCount referencesCount, ElementHandle<TypeElement> handle) {
        int typeRefCount = 999 - Math.min(referencesCount.getTypeReferenceCount(handle), 999);
        int pkgRefCount = 999;
        String binaryName = SourceUtils.getJVMSignature(handle)[0];
        int idx = binaryName.lastIndexOf('.');
        if (idx > 0) {
            ElementHandle<PackageElement> pkgElement = ElementHandle.createPackageElementHandle(binaryName.substring(0, idx));
            pkgRefCount -= Math.min(referencesCount.getPackageReferenceCount(pkgElement), 999);
        }
        return typeRefCount * 100000 + pkgRefCount * 100 + getImportanceLevel(binaryName);
    }
    
    public static int getImportanceLevel(String fqn) {
        int weight = 50;
        if (fqn.startsWith("java.lang") || fqn.startsWith("java.util")) { // NOI18N
            weight -= 10;
        } else if (fqn.startsWith("org.omg") || fqn.startsWith("org.apache")) { // NOI18N
            weight += 10;
        } else if (fqn.startsWith("com.sun") || fqn.startsWith("com.ibm") || fqn.startsWith("com.apple")) { // NOI18N
            weight += 20;
        } else if (fqn.startsWith("sun") || fqn.startsWith("sunw") || fqn.startsWith("netscape")) { // NOI18N
            weight += 30;
        }
        return weight;
    }
    
    public static String getHTMLColor(int r, int g, int b) {
        Color c = LFCustoms.shiftColor(new Color(r, g, b));
        return "<font color=#" //NOI18N
                + LFCustoms.getHexString(c.getRed())
                + LFCustoms.getHexString(c.getGreen())
                + LFCustoms.getHexString(c.getBlue())
                + ">"; //NOI18N
    }
    
    public static boolean isJavaContext(final JTextComponent component, final int offset, final boolean allowInStrings) {
        Document doc = component.getDocument();
        if (doc instanceof AbstractDocument) {
            ((AbstractDocument)doc).readLock();
        }
        try {
        if (doc.getLength() == 0 && "text/x-dialog-binding".equals(doc.getProperty("mimeType"))) { //NOI18N
            InputAttributes attributes = (InputAttributes) doc.getProperty(InputAttributes.class);
            LanguagePath path = LanguagePath.get(MimeLookup.getLookup("text/x-dialog-binding").lookup(Language.class)); //NOI18N
            Document d = (Document) attributes.getValue(path, "dialogBinding.document"); //NOI18N
            if (d != null) {
                return "text/x-java".equals(NbEditorUtilities.getMimeType(d)); //NOI18N
            }
            FileObject fo = (FileObject)attributes.getValue(path, "dialogBinding.fileObject"); //NOI18N
            return "text/x-java".equals(fo.getMIMEType()); //NOI18N
        }
        TokenSequence<JavaTokenId> ts = SourceUtils.getJavaTokenSequence(TokenHierarchy.get(doc), offset);
        if (ts == null) {
            return false;
        }        
        if (!ts.moveNext() && !ts.movePrevious()) {
            return true;
        }
        if (offset == ts.offset()) {
            return true;
        }
        switch(ts.token().id()) {
            case DOUBLE_LITERAL:
            case FLOAT_LITERAL:
            case FLOAT_LITERAL_INVALID:
            case LONG_LITERAL:
                if (ts.token().text().charAt(0) == '.') {
                    break;
                }
            case CHAR_LITERAL:
            case INT_LITERAL:
            case INVALID_COMMENT_END:
            case JAVADOC_COMMENT:
            case LINE_COMMENT:
            case BLOCK_COMMENT:
                return false;
            case STRING_LITERAL:
                return allowInStrings;
        }
        return true;
        } finally {
            if (doc instanceof AbstractDocument) {
                ((AbstractDocument) doc).readUnlock();
            }
        }
    }
    
    public static CharSequence getTypeName(CompilationInfo info, TypeMirror type, boolean fqn) {
        return getTypeName(info, type, fqn, false);
    }

    public static CharSequence getTypeName(CompilationInfo info, TypeMirror type, boolean fqn, boolean varArg) {
        Set<TypeNameOptions> options = EnumSet.noneOf(TypeNameOptions.class);
        if (fqn) {
            options.add(TypeNameOptions.PRINT_FQN);
        }
        if (varArg) {
            options.add(TypeNameOptions.PRINT_AS_VARARG);
        }
        return info.getTypeUtilities().getTypeName(type, options.toArray(new TypeNameOptions[0]));
    }
    
    public static boolean inAnonymousOrLocalClass(TreePath path) {
        if (path == null) {
            return false;
        }
        TreePath parentPath = path.getParentPath();
        if (TreeUtilities.CLASS_TREE_KINDS.contains(path.getLeaf().getKind()) && parentPath.getLeaf().getKind() != Tree.Kind.COMPILATION_UNIT && !TreeUtilities.CLASS_TREE_KINDS.contains(parentPath.getLeaf().getKind())) {
            return true;
        }
        return inAnonymousOrLocalClass(parentPath);
    }

    public static Set<Element> getUsedElements(final CompilationInfo info) {
        final Set<Element> ret = new HashSet<>();
        final Trees trees = info.getTrees();
        new TreePathScanner<Void, Void>() {

            @Override
            public Void visitIdentifier(IdentifierTree node, Void p) {
                addElement(trees.getElement(getCurrentPath()));
                return null;
            }

            @Override
            public Void visitClass(ClassTree node, Void p) {
                for (Element element : JavadocImports.computeReferencedElements(info, getCurrentPath())) {
                    addElement(element);
                }
                return super.visitClass(node, p);
            }

            @Override
            public Void visitMethod(MethodTree node, Void p) {
                for (Element element : JavadocImports.computeReferencedElements(info, getCurrentPath())) {
                    addElement(element);
                }
                return super.visitMethod(node, p);
            }

            @Override
            public Void visitVariable(VariableTree node, Void p) {
                for (Element element : JavadocImports.computeReferencedElements(info, getCurrentPath())) {
                    addElement(element);
                }
                return super.visitVariable(node, p);
            }

            @Override
            public Void visitCompilationUnit(CompilationUnitTree node, Void p) {
                scan(node.getPackageAnnotations(), p);
                return scan(node.getTypeDecls(), p);
            }
            
            private void addElement(Element element) {
                if (element != null) {
                    ret.add(element);
                }
            }
        }.scan(info.getCompilationUnit(), null);
        return ret;                
    }
    
    public static boolean containErrors(Tree tree) {
        final AtomicBoolean containsErrors = new AtomicBoolean();
        new TreeScanner<Void, Void>() {
            @Override
            public Void visitErroneous(ErroneousTree node, Void p) {
                containsErrors.set(true);
                return null;
            }
            
            @Override
            public Void scan(Tree node, Void p) {
                if (containsErrors.get()) {
                    return null;
                }
                return super.scan(node, p);
            }
        }.scan(tree, null);
        return containsErrors.get();
    }

    /**
     * @since 2.12
     */
    public static @NonNull List<ExecutableElement> fuzzyResolveMethodInvocation(CompilationInfo info, TreePath path, List<TypeMirror> proposed, int[] index) {
        assert path.getLeaf().getKind() == Kind.METHOD_INVOCATION || path.getLeaf().getKind() == Kind.NEW_CLASS;
        
        if (path.getLeaf().getKind() == Kind.METHOD_INVOCATION) {
            List<TypeMirror> actualTypes = new LinkedList<>();
            MethodInvocationTree mit = (MethodInvocationTree) path.getLeaf();

            for (Tree a : mit.getArguments()) {
                TreePath tp = new TreePath(path, a);
                actualTypes.add(info.getTrees().getTypeMirror(tp));
            }

            String methodName;
            List<Pair<TypeMirror, Boolean>> on = new ArrayList<>();

            switch (mit.getMethodSelect().getKind()) {
                case IDENTIFIER:
                    methodName = ((IdentifierTree) mit.getMethodSelect()).getName().toString();
                    Scope s = info.getTrees().getScope(path);
                    TypeElement enclosingClass = s.getEnclosingClass();
                    while (enclosingClass != null) {
                        on.add(Pair.of(enclosingClass.asType(), false));
                        enclosingClass = info.getElementUtilities().enclosingTypeElement(enclosingClass);
                    }
                    CompilationUnitTree cut = info.getCompilationUnit();
                    for (ImportTree imp : cut.getImports()) {
                        if (!imp.isStatic() || imp.getQualifiedIdentifier() == null || imp.getQualifiedIdentifier().getKind() != Kind.MEMBER_SELECT) {
                            continue;
                        }
                        Name selected = ((MemberSelectTree) imp.getQualifiedIdentifier()).getIdentifier();
                        if (!selected.contentEquals("*") && !selected.contentEquals(methodName)) {
                            continue;
                        }
                        TreePath tp = new TreePath(new TreePath(new TreePath(new TreePath(cut), imp), imp.getQualifiedIdentifier()), ((MemberSelectTree) imp.getQualifiedIdentifier()).getExpression());
                        Element el = info.getTrees().getElement(tp);
                        if (el != null) {
                            on.add(Pair.of(el.asType(), true));
                        }
                    }
                    break;
                case MEMBER_SELECT:
                    on.add(Pair.of(info.getTrees().getTypeMirror(new TreePath(path, ((MemberSelectTree) mit.getMethodSelect()).getExpression())), false));
                    methodName = ((MemberSelectTree) mit.getMethodSelect()).getIdentifier().toString();
                    break;
                default:
                    throw new IllegalStateException();
            }

            List<ExecutableElement> result = new ArrayList<>();
            
            for (Pair<TypeMirror, Boolean> type : on) {
                if (type.first() == null || type.first().getKind() != TypeKind.DECLARED) {
                    continue;
                }
                result.addAll(resolveMethod(info, actualTypes, (DeclaredType) type.first(), type.second(), false, methodName, proposed, index));
            }
            
            return result;
        }
        
        if (path.getLeaf().getKind() == Kind.NEW_CLASS) {
            List<TypeMirror> actualTypes = new LinkedList<>();
            NewClassTree nct = (NewClassTree) path.getLeaf();

            for (Tree a : nct.getArguments()) {
                TreePath tp = new TreePath(path, a);
                actualTypes.add(info.getTrees().getTypeMirror(tp));
            }

            TypeMirror on = info.getTrees().getTypeMirror(new TreePath(path, nct.getIdentifier()));
            
            if (on == null || on.getKind() != TypeKind.DECLARED) {
                return Collections.emptyList();
            }
            
            return resolveMethod(info, actualTypes, (DeclaredType) on, false, true, null, proposed, index);
        }
        
        return Collections.emptyList();
    }

    private static Iterable<ExecutableElement> execsIn(CompilationInfo info, TypeElement e, boolean constr, String name) {
        if (constr) {
            return ElementFilter.constructorsIn(info.getElements().getAllMembers(e));
        }
        
        List<ExecutableElement> result = new LinkedList<>();
        
        for (ExecutableElement ee : ElementFilter.methodsIn(info.getElements().getAllMembers(e))) {
            if (name.equals(ee.getSimpleName().toString())) {
                result.add(ee);
            }
        }
        
        return result;
    }
    
    private static List<ExecutableElement> resolveMethod(CompilationInfo info, List<TypeMirror> foundTypes, DeclaredType on, boolean onlyStatic, boolean constr, String name, List<TypeMirror> candidateTypes, int[] index) {
        if (on.asElement() == null) {
            return Collections.emptyList();
        }
        
        List<ExecutableElement> found = new LinkedList<>();
        
        OUTER:
        for (ExecutableElement ee : execsIn(info, (TypeElement) on.asElement(), constr, name)) {
            TypeMirror currType = ((TypeElement) ee.getEnclosingElement()).asType();
            if (!info.getTypes().isSubtype(on, currType) && !on.asElement().equals(((DeclaredType)currType).asElement())) { //XXX: fix for #132627, a clearer fix may exist
                continue;
            }
            if (onlyStatic && !ee.getModifiers().contains(Modifier.STATIC)) {
                continue;
            }
            if (ee.getParameters().size() == foundTypes.size() /*XXX: variable arg count*/) {
                TypeMirror innerCandidate = null;
                int innerIndex = -1;
                ExecutableType et = (ExecutableType) info.getTypes().asMemberOf(on, ee);
                Iterator<? extends TypeMirror> formal = et.getParameterTypes().iterator();
                Iterator<? extends TypeMirror> actual = foundTypes.iterator();
                boolean mismatchFound = false;
                int i = 0;

                while (formal.hasNext() && actual.hasNext()) {
                    TypeMirror currentFormal = formal.next();
                    TypeMirror currentActual = actual.next();

                    if (!info.getTypes().isAssignable(currentActual, currentFormal) || currentActual.getKind() == TypeKind.ERROR) {
                        if (mismatchFound) {
                            //only one mismatch supported:
                            continue OUTER;
                        }
                        mismatchFound = true;
                        innerCandidate = currentFormal;
                        innerIndex = i;
                    }

                    i++;
                }

                if (mismatchFound) {
                    if (candidateTypes.isEmpty()) {
                        index[0] = innerIndex;
                        candidateTypes.add(innerCandidate);
                        found.add(ee);
                    } else {
                        //see testFuzzyResolveConstructor2:
                        if (index[0] == innerIndex) {
                            boolean add = true;
                            for (TypeMirror tm : candidateTypes) {
                                if (info.getTypes().isSameType(tm, innerCandidate)) {
                                    add = false;
                                    break;
                                }
                            }
                            if (add) {
                                candidateTypes.add(innerCandidate);
                                found.add(ee);
                            }
                        }
                    }
                }
            }
        }

        return found;
    }

    private Utilities() {
    }
}
