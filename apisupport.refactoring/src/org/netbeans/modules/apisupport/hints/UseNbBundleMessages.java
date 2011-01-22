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

import org.openide.loaders.DataObject;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.AnnotationTree;
import java.io.OutputStream;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.openide.util.Exceptions;
import java.io.IOException;
import java.io.InputStream;
import org.netbeans.api.java.source.GeneratorUtilities;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.CompilationUnitTree;
import org.openide.util.Utilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.ClasspathInfo.PathKind;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.spi.AbstractHint;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.filesystems.FileObject;
import org.openide.util.EditableProperties;
import org.openide.util.NbBundle.Messages;
import static org.netbeans.modules.apisupport.hints.Bundle.*;

public class UseNbBundleMessages extends AbstractHint {

    public UseNbBundleMessages() {
        super(true, true, AbstractHint.HintSeverity.WARNING);
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
        // XXX also check for e.g. displayName="#..." on registration annotations
        return Collections.singleton(Kind.METHOD_INVOCATION);
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
        final MethodInvocationTree mit = (MethodInvocationTree) treePath.getLeaf();
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
        if (compilationInfo.getClasspathInfo().getClassPath(PathKind.COMPILE).findResource("org/openide/util/NbBundle$Messages.class") == null) {
            // Using an older version of NbBundle.
            return null;
        }
        int[] span = compilationInfo.getTreeUtilities().findNameSpan(mst);
        if (span == null) {
            return null;
        }
        final List<? extends ExpressionTree> args = mit.getArguments();
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
        final FileObject src = compilationInfo.getFileObject();
        if (!((IdentifierTree) thisClassMST.getExpression()).getName().contentEquals(src.getName())) {
            return warning(UseNbBundleMessages_wrong_class_name(src.getName()), span, compilationInfo);
        }
        if (args.get(1).getKind() != Kind.STRING_LITERAL) {
            return warning(UseNbBundleMessages_only_string_const(), span, compilationInfo);
        }
        final String key = ((LiteralTree) args.get(1)).getValue().toString();
        String bundleResource = compilationInfo.getCompilationUnit().getPackageName().toString().replace('.', '/') + "/Bundle.properties";
        final FileObject bundleProperties = compilationInfo.getClasspathInfo().getClassPath(PathKind.SOURCE).findResource(bundleResource);
        if (bundleProperties == null) {
            return warning(UseNbBundleMessages_no_such_bundle(bundleResource), span, compilationInfo);
        }
        final EditableProperties ep = new EditableProperties(true);
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
                        List<? extends ExpressionTree> params;
                        if (args.size() == 3 && args.get(2).getKind() == Kind.NEW_ARRAY) {
                            params = ((NewArrayTree) args.get(2)).getInitializers();
                        } else {
                            params = args.subList(2, args.size());
                        }
                        wc.rewrite(mit, make.MethodInvocation(Collections.<ExpressionTree>emptyList(), make.Identifier(toIdentifier(key)), params));
                        Tree enclosing = findEnclosingElement(wc, treePath);
                        ModifiersTree modifiers;
                        if (enclosing.getKind() == Kind.METHOD) {
                            modifiers = ((MethodTree) enclosing).getModifiers();
                        } else {
                            modifiers = ((ClassTree) enclosing).getModifiers();
                        }
                        List<ExpressionTree> lines = new ArrayList<ExpressionTree>();
                        for (String comment : ep.getComment(key)) {
                            lines.add(make.Literal(comment));
                        }
                        lines.add(make.Literal(key + '=' + ep.remove(key)));
                        wc.rewrite(modifiers, addMessage(wc, modifiers, lines));
                        // XXX remove NbBundle import if now unused
                    }
                    // borrowed from FindBugsHint:
                    private ModifiersTree addMessage(WorkingCopy wc, ModifiersTree original, List<ExpressionTree> lines) throws Exception {
                        TreeMaker make = wc.getTreeMaker();
                        // First try to insert into a value list for an existing annotation:
                        List<? extends AnnotationTree> anns = original.getAnnotations();
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
                                return make.insertModifiersAnnotation(make.removeModifiersAnnotation(original, i), i, ann);
                            }
                        }
                        // Not found, so create a new annotation:
                        List<ExpressionTree> values;
                        if (lines.size() > 1) { // @Messages({"# ...", "k=v"})
                            values = Collections.<ExpressionTree>singletonList(make.NewArray(null, Collections.<ExpressionTree>emptyList(), lines));
                        } else { // @Messages("k=v")
                            values = lines;
                        }
                        return GeneratorUtilities.get(wc).importFQNs(make.addModifiersAnnotation(original, make.Annotation(make.QualIdent("org.openide.util.NbBundle.Messages"), values)));
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
                        }
                        TreePath parentPath = treePath.getParentPath();
                        if (parentPath == null) {
                            return null;
                        }
                        return findEnclosingElement(wc, parentPath);
                    }
                }).commit();
                OutputStream os = bundleProperties.getOutputStream();
                try {
                    ep.store(os);
                } finally {
                    os.close();
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

}
