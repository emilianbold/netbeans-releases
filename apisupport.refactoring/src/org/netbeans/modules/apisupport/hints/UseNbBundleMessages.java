/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.apisupport.hints;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.ClasspathInfo.PathKind;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import static org.netbeans.modules.apisupport.hints.Bundle.*;
import org.netbeans.modules.java.hints.spi.AbstractHint;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.EditableProperties;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;

public class UseNbBundleMessages extends AbstractHint {

    public UseNbBundleMessages() {
        super(true, true, AbstractHint.HintSeverity.CURRENT_LINE_WARNING);
    }

    public @Override String getId() {
        return UseNbBundleMessages.class.getName();
    }

    @Messages("UseNbBundleMessages.displayName=Use @NbBundle.Messages")
    public @Override String getDisplayName() {
        return UseNbBundleMessages_displayName();
    }

    @Messages("UseNbBundleMessages.description=Use @NbBundle.Messages in preference to Bundle.properties plus NbBundle.getMessage(...).")
    public @Override String getDescription() {
        return UseNbBundleMessages_description();
    }

    public @Override Set<Kind> getTreeKinds() {
        return EnumSet.of(Kind.METHOD_INVOCATION, Kind.ASSIGNMENT);
    }

    @Messages({
        "UseNbBundleMessages.error_text=Use of external bundle key",
        "UseNbBundleMessages.only_class_const=Use of NbBundle.getMessage without ThisClass.class syntax",
        "# {0} - top-level class name", "UseNbBundleMessages.wrong_class_name=Expected argument to be {0}.class",
        "UseNbBundleMessages.only_string_const=Use of NbBundle.getMessage with nonconstant key",
        "# {0} - resource path", "UseNbBundleMessages.no_such_bundle=Could not locate {0} in source path",
        "# {0} - bundle key", "UseNbBundleMessages.no_such_key=Bundle.properties does not contain any key ''{0}''",
        "UseNbBundleMessages.save_bundle=Save modifications to Bundle.properties before using this hint"
    })
    public @Override List<ErrorDescription> run(final CompilationInfo compilationInfo, final TreePath treePath) {
        Tree tree = treePath.getLeaf();
        int[] span;
        final String key;
        final FileObject src = compilationInfo.getFileObject();
        final MethodInvocationTree mit;
        if (tree.getKind() == Kind.METHOD_INVOCATION) {
            mit = (MethodInvocationTree) tree;
            ExpressionTree methodSelect = mit.getMethodSelect();
            if (methodSelect.getKind() != Kind.MEMBER_SELECT) {
                return null;
            }
            MemberSelectTree mst = (MemberSelectTree) methodSelect;
            if (!mst.getIdentifier().contentEquals("getMessage")) {
                return null;
            }
            TypeMirror invoker = compilationInfo.getTrees().getTypeMirror(new TreePath(treePath, mst.getExpression()));
            if (!String.valueOf(invoker).equals("org.openide.util.NbBundle")) {
                return null;
            }
            span = compilationInfo.getTreeUtilities().findNameSpan(mst);
            if (span == null) {
                return null;
            }
            List<? extends ExpressionTree> args = mit.getArguments();
            if (args.size() < 2) {
                return null; // something unexpected
            }
            if (args.get(0).getKind() != Kind.MEMBER_SELECT) {
                return warning(UseNbBundleMessages_only_class_const(), span, compilationInfo);
            }
            MemberSelectTree thisClassMST = (MemberSelectTree) args.get(0);
            if (!thisClassMST.getIdentifier().contentEquals("class")) {
                return warning(UseNbBundleMessages_only_class_const(), span, compilationInfo);
            }
            if (thisClassMST.getExpression().getKind() != Kind.IDENTIFIER) {
                return warning(UseNbBundleMessages_only_class_const(), span, compilationInfo);
            }
            if (!((IdentifierTree) thisClassMST.getExpression()).getName().contentEquals(src.getName())) {
                return warning(UseNbBundleMessages_wrong_class_name(src.getName()), span, compilationInfo);
            }
            if (args.get(1).getKind() != Kind.STRING_LITERAL) {
                return warning(UseNbBundleMessages_only_string_const(), span, compilationInfo);
            }
            key = ((LiteralTree) args.get(1)).getValue().toString();
        } else {
            if (treePath.getParentPath().getLeaf().getKind() != Kind.ANNOTATION) {
                return null;
            }
            final AssignmentTree at = (AssignmentTree) tree;
            if (at.getExpression().getKind() != Kind.STRING_LITERAL) {
                return null;
            }
            String literal = ((LiteralTree) at.getExpression()).getValue().toString();
            if (!literal.startsWith("#")) {
                return null;
            }
            key = literal.substring(1);
            // at.variable iof IdentifierTree, not VariableTree, so TreeUtilities.findNameSpan cannot be used
            SourcePositions sp = compilationInfo.getTrees().getSourcePositions();
            span = new int[] {(int) sp.getStartPosition(compilationInfo.getCompilationUnit(), tree), (int) sp.getEndPosition(compilationInfo.getCompilationUnit(), tree)};
            mit = null;
        }
        if (compilationInfo.getClasspathInfo().getClassPath(PathKind.COMPILE).findResource("org/openide/util/NbBundle$Messages.class") == null) {
            // Using an older version of NbBundle.
            return null;
        }
        final boolean isAlreadyRegistered = isAlreadyRegistered(treePath, key);
        final EditableProperties ep;
        final FileObject bundleProperties;
        if (isAlreadyRegistered) {
            if (mit == null) {
                return null; // nothing to do
            } // else still need to convert getMessage call
            ep = null; // unused
            bundleProperties = null;
        } else {
            String bundleResource = compilationInfo.getCompilationUnit().getPackageName().toString().replace('.', '/') + "/Bundle.properties";
            bundleProperties = compilationInfo.getClasspathInfo().getClassPath(PathKind.SOURCE).findResource(bundleResource);
            if (bundleProperties == null) {
                return warning(UseNbBundleMessages_no_such_bundle(bundleResource), span, compilationInfo);
            }
            ep = new EditableProperties(true);
            try {
                if (DataObject.find(bundleProperties).isModified()) {
                    // Using EditorCookie.document is quite difficult here due to encoding issues. Keep it simple.
                    return warning(UseNbBundleMessages_save_bundle(), span, compilationInfo);
                }
                InputStream is = bundleProperties.getInputStream();
                try {
                    ep.load(is);
                } finally {
                    is.close();
                }
            } catch (IOException x) {
                Exceptions.printStackTrace(x);
                return null;
            }
            if (!ep.containsKey(key)) {
                return warning(UseNbBundleMessages_no_such_key(key), span, compilationInfo);
            }
        }
        return Collections.singletonList(ErrorDescriptionFactory.createErrorDescription(getSeverity().toEditorSeverity(), UseNbBundleMessages_error_text(), Collections.<Fix>singletonList(new Fix() {
            public @Override String getText() {
                return UseNbBundleMessages_displayName();
            }
            public @Override ChangeInfo implement() throws Exception {
                JavaSource js = JavaSource.forFileObject(src);
                if (js == null) {
                    throw new Exception("No source info for " + src);
                }
                js.runModificationTask(new Task<WorkingCopy>() {
                    public @Override void run(WorkingCopy wc) throws Exception {
                        wc.toPhase(JavaSource.Phase.RESOLVED);
                        TreeMaker make = wc.getTreeMaker();
                        if (mit != null) {
                            CompilationUnitTree cut = wc.getCompilationUnit();
                            boolean imported = false;
                            String importBundleStar = cut.getPackageName() + ".Bundle.*";
                            for (ImportTree it : cut.getImports()) {
                                if (it.isStatic() && it.getQualifiedIdentifier().toString().equals(importBundleStar)) {
                                    imported = true;
                                    break;
                                }
                            }
                            if (!imported) {
                                wc.rewrite(cut, make.addCompUnitImport(cut, make.Import(make.Identifier(importBundleStar), true)));
                            }
                            List<? extends ExpressionTree> args = mit.getArguments();
                            List<? extends ExpressionTree> params;
                            if (args.size() == 3 && args.get(2).getKind() == Kind.NEW_ARRAY) {
                                params = ((NewArrayTree) args.get(2)).getInitializers();
                            } else {
                                params = args.subList(2, args.size());
                            }
                            wc.rewrite(mit, make.MethodInvocation(Collections.<ExpressionTree>emptyList(), make.Identifier(toIdentifier(key)), params));
                        } // else annotation value, nothing to change
                        if (!isAlreadyRegistered) {
                            Tree enclosing = findEnclosingElement(wc, treePath);
                            Tree modifiers;
                            if (enclosing.getKind() == Kind.METHOD) {
                                modifiers = ((MethodTree) enclosing).getModifiers();
                            } else if (enclosing.getKind() == Kind.COMPILATION_UNIT) {
                                modifiers = enclosing;
                            } else {
                                modifiers = ((ClassTree) enclosing).getModifiers();
                            }
                            List<ExpressionTree> lines = new ArrayList<ExpressionTree>();
                            for (String comment : ep.getComment(key)) {
                                lines.add(make.Literal(comment));
                            }
                            lines.add(make.Literal(key + '=' + ep.remove(key)));
                            wc.rewrite(modifiers, addMessage(wc, modifiers, lines));
                        }
                        // XXX remove NbBundle import if now unused
                    }
                    // borrowed from FindBugsHint:
                    private Tree addMessage(WorkingCopy wc, /*Modifiers|CompilationUnit*/Tree original, List<ExpressionTree> lines) throws Exception {
                        TreeMaker make = wc.getTreeMaker();
                        // First try to insert into a value list for an existing annotation:
                        List<? extends AnnotationTree> anns;
                        if (original.getKind() == Kind.COMPILATION_UNIT) {
                            anns = ((CompilationUnitTree) original).getPackageAnnotations();
                        } else {
                            anns = ((ModifiersTree) original).getAnnotations();
                        }
                        for (int i = 0; i < anns.size(); i++) {
                            AnnotationTree ann = anns.get(i);
                            Tree annotationType = ann.getAnnotationType();
                            // XXX clumsy and imprecise, but how to find the FQN of the annotation type given a Tree? Want a TypeMirror for it.
                            if (annotationType.toString().matches("((org[.]openide[.]util[.])?NbBundle[.])?Messages")) {
                                List<? extends ExpressionTree> args = ann.getArguments();
                                if (args.size() != 1) {
                                    throw new Exception("expecting just one arg for @Messages");
                                }
                                AssignmentTree assign = (AssignmentTree) args.get(0);
                                if (!assign.getVariable().toString().equals("value")) {
                                    throw new Exception("expected value=... for @Messages");
                                }
                                ExpressionTree arg = assign.getExpression();
                                NewArrayTree arr;
                                if (arg.getKind() == Tree.Kind.STRING_LITERAL) {
                                    arr = make.NewArray(null, Collections.<ExpressionTree>emptyList(), Collections.singletonList(arg));
                                } else if (arg.getKind() == Tree.Kind.NEW_ARRAY) {
                                    arr = (NewArrayTree) arg;
                                } else {
                                    throw new Exception("unknown arg kind " + arg.getKind() + ": " + arg);
                                }
                                for (ExpressionTree line : lines) {
                                    arr = make.addNewArrayInitializer(arr, line);
                                }
                                ann = make.Annotation(annotationType, Collections.singletonList(arr));
                                if (original.getKind() == Kind.COMPILATION_UNIT) {
                                    CompilationUnitTree cut = (CompilationUnitTree) original;
                                    List<AnnotationTree> newAnns = new ArrayList<AnnotationTree>(anns);
                                    newAnns.set(i, ann);
                                    return make.CompilationUnit(newAnns, cut.getPackageName(), cut.getImports(), cut.getTypeDecls(), cut.getSourceFile());
                                } else {
                                    return make.insertModifiersAnnotation(make.removeModifiersAnnotation((ModifiersTree) original, i), i, ann);
                                }
                            }
                        }
                        // Not found, so create a new annotation:
                        List<ExpressionTree> values;
                        if (lines.size() > 1) { // @Messages({"# ...", "k=v"})
                            values = Collections.<ExpressionTree>singletonList(make.NewArray(null, Collections.<ExpressionTree>emptyList(), lines));
                        } else { // @Messages("k=v")
                            values = lines;
                        }
                        AnnotationTree atMessages = make.Annotation(make.QualIdent("org.openide.util.NbBundle.Messages"), values);
                        Tree result;
                        if (original.getKind() == Kind.COMPILATION_UNIT) {
                            CompilationUnitTree cut = (CompilationUnitTree) original;
                            List<AnnotationTree> newAnns = new ArrayList<AnnotationTree>(anns);
                            newAnns.add(atMessages);
                            result = make.CompilationUnit(newAnns, cut.getPackageName(), cut.getImports(), cut.getTypeDecls(), cut.getSourceFile());
                        } else {
                            result = make.addModifiersAnnotation((ModifiersTree) original, atMessages);
                        }
                        return GeneratorUtilities.get(wc).importFQNs(result);
                    }
                    private Tree findEnclosingElement(WorkingCopy wc, TreePath treePath) {
                        Tree leaf = treePath.getLeaf();
                        Kind kind = leaf.getKind();
                        switch (kind) {
                        case CLASS:
                        case ENUM:
                        case INTERFACE:
                        case ANNOTATION_TYPE:
                        case METHOD: // (or constructor)
                            Element e = wc.getTrees().getElement(treePath);
                            if (e != null) {
                                TypeElement type = kind == Kind.METHOD ? wc.getElementUtilities().enclosingTypeElement(e) : (TypeElement) e;
                                if (type == null || !wc.getElementUtilities().isLocal(type)) {
                                    return leaf;
                                } // else part of an inner class
                            }
                            break;
                        case COMPILATION_UNIT:
                            return leaf;
                        }
                        TreePath parentPath = treePath.getParentPath();
                        if (parentPath == null) {
                            return null;
                        }
                        return findEnclosingElement(wc, parentPath);
                    }
                }).commit();
                if (!isAlreadyRegistered) {
                    OutputStream os = bundleProperties.getOutputStream();
                    try {
                        ep.store(os);
                    } finally {
                        os.close();
                    }
                }
                return null;
            }
        }), compilationInfo.getFileObject(), span[0], span[1]));
    }

    private List<ErrorDescription> warning(String text, int[] span, CompilationInfo compilationInfo) {
        return Collections.singletonList(ErrorDescriptionFactory.createErrorDescription(getSeverity().toEditorSeverity(), text, Collections.<Fix>emptyList(), compilationInfo.getFileObject(), span[0], span[1]));
    }

    public @Override void cancel() {
        // Probably nothing to do.
    }

    // Copied from NbBundleProcessor
    private String toIdentifier(String key) {
        if (Utilities.isJavaIdentifier(key)) {
            return key;
        } else {
            String i = key.replaceAll("[^\\p{javaJavaIdentifierPart}]+", "_");
            if (Utilities.isJavaIdentifier(i)) {
                return i;
            } else {
                return "_" + i;
            }
        }
    }

    private static boolean isAlreadyRegistered(TreePath treePath, String key) {
        ModifiersTree modifiers;
        Tree tree = treePath.getLeaf();
        switch (tree.getKind()) {
        case METHOD:
            modifiers = ((MethodTree) tree).getModifiers();
            break;
        case CLASS:
        case ENUM:
        case INTERFACE:
        case ANNOTATION_TYPE:
            modifiers = ((ClassTree) tree).getModifiers();
            break;
        default:
            modifiers = null;
        }
        if (modifiers != null) {
            for (AnnotationTree ann : modifiers.getAnnotations()) {
                Tree annotationType = ann.getAnnotationType();
                if (annotationType.toString().matches("((org[.]openide[.]util[.])?NbBundle[.])?Messages")) { // XXX see above
                    List<? extends ExpressionTree> args = ann.getArguments();
                    if (args.size() != 1) {
                        continue; // ?
                    }
                    AssignmentTree assign = (AssignmentTree) args.get(0);
                    if (!assign.getVariable().toString().equals("value")) {
                        continue; // ?
                    }
                    ExpressionTree arg = assign.getExpression();
                    if (arg.getKind() == Tree.Kind.STRING_LITERAL) {
                        if (isRegistered(key, arg)) {
                            return true;
                        }
                    } else if (arg.getKind() == Tree.Kind.NEW_ARRAY) {
                        for (ExpressionTree elt : ((NewArrayTree) arg).getInitializers()) {
                            if (isRegistered(key, elt)) {
                                return true;
                            }
                        }
                    } else {
                        // ?
                    }
                }
            }
        }
        TreePath parentPath = treePath.getParentPath();
        if (parentPath == null) {
            return false;
        }
        return isAlreadyRegistered(parentPath, key);
    }
    private static boolean isRegistered(String key, ExpressionTree expr) {
        return expr.getKind() == Kind.STRING_LITERAL && ((LiteralTree) expr).getValue().toString().startsWith(key + "=");
    }

}
