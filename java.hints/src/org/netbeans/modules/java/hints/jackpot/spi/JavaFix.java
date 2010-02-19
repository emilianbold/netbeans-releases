/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints.jackpot.spi;

import com.sun.javadoc.Doc;
import com.sun.javadoc.Tag;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.VariableTree;
import java.util.Collection;
import java.util.regex.Matcher;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
//import org.netbeans.modules.apisupport.project.NbModuleProject;
//import org.netbeans.modules.apisupport.project.ProjectXMLManager;
//import org.netbeans.modules.apisupport.project.spi.NbModuleProvider;
//import org.netbeans.modules.apisupport.project.ui.customizer.ModuleDependency;
import org.netbeans.modules.java.hints.jackpot.impl.Utilities;
import org.netbeans.modules.java.hints.jackpot.impl.JavaFixImpl;
import org.netbeans.modules.java.hints.jackpot.impl.pm.Pattern;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.filesystems.FileObject;
import org.openide.modules.SpecificationVersion;

/**
 *
 * @author Jan Lahoda
 */
public abstract class JavaFix {

    private final TreePathHandle handle;

    protected JavaFix(CompilationInfo info, TreePath tp) {
        this.handle = TreePathHandle.create(tp, info);
    }

    protected abstract String getText();

    protected abstract void performRewrite(WorkingCopy wc, TreePath tp, UpgradeUICallback callback);

    final ChangeInfo process(WorkingCopy wc, UpgradeUICallback callback) throws Exception {
        TreePath tp = handle.resolve(wc);

        if (tp == null) {
            Logger.getLogger(JavaFix.class.getName()).log(Level.SEVERE, "Cannot resolve handle={0}", handle);
            return null;
        }

        performRewrite(wc, tp, callback);

        return null;
    }

    final FileObject getFile() {
        return handle.getFileObject();
    }

    public static Fix rewriteFix(HintContext ctx, String displayName, TreePath what, final String to, String... imports) {
        return rewriteFix(ctx, displayName, what, to, Collections.<String, TypeMirror>emptyMap(), imports);
    }
    
    public static Fix rewriteFix(HintContext ctx, String displayName, TreePath what, final String to, Map<String, TypeMirror> constraints, String... imports) {
        return rewriteFix(ctx.getInfo(), displayName, what, to, ctx.getVariables(), ctx.getMultiVariables(), ctx.getVariableNames(), constraints, imports);
    }

    public static Fix rewriteFix(CompilationInfo info, String displayName, TreePath what, final String to, Map<String, TreePath> parameters, Map<String, Collection<? extends TreePath>> parametersMulti, final Map<String, String> parameterNames, Map<String, TypeMirror> constraints, String... imports) {
        final Map<String, TreePathHandle> params = new HashMap<String, TreePathHandle>();

        for (Entry<String, TreePath> e : parameters.entrySet()) {
            params.put(e.getKey(), TreePathHandle.create(e.getValue(), info));
        }

        final Map<String, Collection<TreePathHandle>> paramsMulti = new HashMap<String, Collection<TreePathHandle>>();

        for (Entry<String, Collection<? extends TreePath>> e : parametersMulti.entrySet()) {
            Collection<TreePathHandle> tph = new LinkedList<TreePathHandle>();

            for (TreePath tp : e.getValue()) {
                tph.add(TreePathHandle.create(tp, info));
            }

            paramsMulti.put(e.getKey(), tph);
        }

        final Map<String, TypeMirrorHandle<?>> constraintsHandles = new HashMap<String, TypeMirrorHandle<?>>();

        for (Entry<String, TypeMirror> c : constraints.entrySet()) {
            constraintsHandles.put(c.getKey(), TypeMirrorHandle.create(c.getValue()));
        }

        if (displayName == null) {
            displayName = defaultFixDisplayName(info, parameters, to);
        }

        return toEditorFix(new JavaFixRealImpl(info, what, displayName, to, params, paramsMulti, parameterNames, constraintsHandles, Arrays.asList(imports)));
    }

    private static boolean isFakeBlock(Tree t) {
        if (!(t instanceof BlockTree)) {
            return false;
        }

        BlockTree bt = (BlockTree) t;

        if (bt.getStatements().isEmpty()) {
            return false;
        }

        CharSequence wildcardTreeName = Utilities.getWildcardTreeName(bt.getStatements().get(0));

        if (wildcardTreeName == null) {
            return false;
        }

        return wildcardTreeName.toString().startsWith("$$");
    }

    private static boolean isFakeClass(Tree t) {
        if (!(t instanceof ClassTree)) {
            return false;
        }

        ClassTree ct = (ClassTree) t;

        if (ct.getMembers().isEmpty()) {
            return false;
        }

        CharSequence wildcardTreeName = Utilities.getWildcardTreeName(ct.getMembers().get(0));

        if (wildcardTreeName == null) {
            return false;
        }

        return wildcardTreeName.toString().startsWith("$$");
    }

    private static String defaultFixDisplayName(CompilationInfo info, Map<String, TreePath> variables, String replaceTarget) {
        Map<String, String> stringsForVariables = new HashMap<String, String>();

        for (Entry<String, TreePath> e : variables.entrySet()) {
            Tree t = e.getValue().getLeaf();
            SourcePositions sp = info.getTrees().getSourcePositions();
            int startPos = (int) sp.getStartPosition(info.getCompilationUnit(), t);
            int endPos = (int) sp.getEndPosition(info.getCompilationUnit(), t);

            if (startPos >= 0 && endPos >= 0) {
                stringsForVariables.put(e.getKey(), info.getText().substring(startPos, endPos));
            } else {
                stringsForVariables.put(e.getKey(), "");
            }
        }

        if (!stringsForVariables.containsKey("$this")) {
            //XXX: is this correct?
            stringsForVariables.put("$this", "this");
        }

        for (Entry<String, String> e : stringsForVariables.entrySet()) {
            String quotedVariable = java.util.regex.Pattern.quote(e.getKey());
            String quotedTarget = Matcher.quoteReplacement(e.getValue());
            replaceTarget = replaceTarget.replaceAll(quotedVariable, quotedTarget);
        }

        return "Rewrite to " + replaceTarget;
    }

    private static void checkDependency(WorkingCopy copy, Element e, UpgradeUICallback callback) {
        SpecificationVersion sv = computeSpecVersion(copy, e);

        while (sv == null && e.getKind() != ElementKind.PACKAGE) {
            e = e.getEnclosingElement();
            sv = computeSpecVersion(copy, e);
        }
        
        if (sv == null) {
            return ;
        }

        Project currentProject = FileOwnerQuery.getOwner(copy.getFileObject());

        if (currentProject == null) {
            return ;
        }

        FileObject file = getFile(copy, e);

        if (file == null) {
            return ;
        }

        Project referedProject = FileOwnerQuery.getOwner(file);

        if (referedProject == null || currentProject.getProjectDirectory().equals(referedProject.getProjectDirectory())) {
            return ;
        }

        resolveNbModuleDependencies(currentProject, referedProject, sv, callback);
    }

    private static java.util.regex.Pattern SPEC_VERSION = java.util.regex.Pattern.compile("[0-9]+(\\.[0-9]+)+");
    
    public static SpecificationVersion computeSpecVersion(CompilationInfo info, Element el) {
        Doc javaDoc = info.getElementUtilities().javaDocFor(el);

        if (javaDoc == null) return null;

        for (Tag since : javaDoc.tags("@since")) {
            String text = since.text();

            Matcher m = SPEC_VERSION.matcher(text);

            if (!m.find()) {
                continue;
            }

            return new SpecificationVersion(m.group()/*ver.toString()*/);
        }

        return null;
    }
    
    public static Fix toEditorFix(final JavaFix jf) {
        return new JavaFixImpl(jf);
    }

    private static void resolveNbModuleDependencies(Project currentProject, Project referedProject, SpecificationVersion sv, UpgradeUICallback callback) throws IllegalArgumentException {
//        NbModuleProvider currentNbModule = currentProject.getLookup().lookup(NbModuleProvider.class);
//
//        if (currentNbModule == null) {
//            return ;
//        }
//
//        NbModuleProvider referedNbModule = referedProject.getLookup().lookup(NbModuleProvider.class);
//
//        if (referedNbModule == null) {
//            return ;
//        }
//
//        try {
//            NbModuleProject currentNbModuleProject = currentProject.getLookup().lookup(NbModuleProject.class);
//
//            if (currentNbModuleProject == null) {
//                return ;
//            }
//
//            ProjectXMLManager m = new ProjectXMLManager(currentNbModuleProject);
//            ModuleDependency dep = null;
//
//            for (ModuleDependency md : m.getDirectDependencies()) {
//                if (referedNbModule.getCodeNameBase().equals(md.getModuleEntry().getCodeNameBase())) {
//                    dep = md;
//                    break;
//                }
//            }
//
//            if (dep == null) {
//                return ;
//            }
//
//            if (dep.getSpecificationVersion() == null) {
//                return ;
//            }
//
//            SpecificationVersion currentDep = new SpecificationVersion(dep.getSpecificationVersion());
//
//            if (currentDep == null || currentDep.compareTo(sv) < 0) {
//                String upgradeText = NbBundle.getMessage(JavaFix.class,
//                                                         "LBL_UpdateDependencyQuestion",
//                                                         new Object[] {
//                                                            ProjectUtils.getInformation(referedProject).getDisplayName(),
//                                                            currentDep.toString()
//                                                         });
//
//                if (callback.shouldUpgrade(upgradeText)) {
//                    ModuleDependency nue = new ModuleDependency(dep.getModuleEntry(),
//                                                                dep.getReleaseVersion(),
//                                                                sv.toString(),
//                                                                dep.hasCompileDependency(),
//                                                                dep.hasImplementationDepedendency());
//
//                    m.editDependency(dep, nue);
//                    ProjectManager.getDefault().saveProject(currentProject);
//                }
//            }
//        } catch (IOException ex) {
//            Exceptions.printStackTrace(ex);
//        }
    }

    @SuppressWarnings("deprecation")
    private static FileObject getFile(WorkingCopy copy, Element e) {
        return SourceUtils.getFile(e, copy.getClasspathInfo());
    }

    private static boolean isStaticElement(Element el) {
        if (el == null) return false;

        if (el.getModifiers().contains(Modifier.STATIC)) {
            //XXX:
            if (!el.getKind().isClass() && !el.getKind().isInterface()) {
                return false;
            }
            
            return true;
        }

        if (el.getKind().isClass() || el.getKind().isInterface()) {
            return el.getEnclosingElement().getKind() == ElementKind.PACKAGE;
        }

        return false;
    }

    public interface UpgradeUICallback {
        public boolean shouldUpgrade(String comment);
    }

    private static class JavaFixRealImpl extends JavaFix {
        private final String displayName;
        private final Map<String, TreePathHandle> params;
        private final Map<String, Collection<TreePathHandle>> paramsMulti;
        private final Map<String, String> parameterNames;
        private final Map<String, TypeMirrorHandle<?>> constraintsHandles;
        private final Iterable<? extends String> imports;
        private final String to;

        public JavaFixRealImpl(CompilationInfo info, TreePath what, String displayName, String to, Map<String, TreePathHandle> params, Map<String, Collection<TreePathHandle>> paramsMulti, final Map<String, String> parameterNames, Map<String, TypeMirrorHandle<?>> constraintsHandles, Iterable<? extends String> imports) {
            super(info, what);
            
            this.displayName = displayName;
            this.to = to;
            this.params = params;
            this.paramsMulti = paramsMulti;
            this.parameterNames = parameterNames;
            this.constraintsHandles = constraintsHandles;
            this.imports = imports;
        }

        @Override
        protected String getText() {
            return displayName;
        }

        @Override
        protected void performRewrite(final WorkingCopy wc, TreePath tp, final UpgradeUICallback callback) {
            final Map<String, TreePath> parameters = new HashMap<String, TreePath>();

            for (Entry<String, TreePathHandle> e : params.entrySet()) {
                TreePath p = e.getValue().resolve(wc);

                if (p == null) {
                    Logger.getLogger(JavaFix.class.getName()).log(Level.SEVERE, "Cannot resolve handle={0}", e.getValue());
                }

                parameters.put(e.getKey(), p);
            }

            final Map<String, Collection<TreePath>> parametersMulti = new HashMap<String, Collection<TreePath>>();

            for (Entry<String, Collection<TreePathHandle>> e : paramsMulti.entrySet()) {
                Collection<TreePath> tps = new LinkedList<TreePath>();

                for (TreePathHandle tph : e.getValue()) {
                    TreePath p = tph.resolve(wc);

                    if (p == null) {
                        Logger.getLogger(JavaFix.class.getName()).log(Level.SEVERE, "Cannot resolve handle={0}", e.getValue());
                    }

                    tps.add(p);
                }

                parametersMulti.put(e.getKey(), tps);
            }

            Map<String, TypeMirror> constraints = new HashMap<String, TypeMirror>();

            for (Entry<String, TypeMirrorHandle<?>> c : constraintsHandles.entrySet()) {
                constraints.put(c.getKey(), c.getValue().resolve(wc));
            }

            Tree parsed = Pattern.parseAndAttribute(wc, to, constraints, new Scope[1], imports);

            if (!isFakeBlock(parsed) && !isFakeClass(parsed) && (tp.getLeaf().getKind() != Kind.BLOCK || !parametersMulti.containsKey("$$1$") || parsed.getKind() == Kind.BLOCK)) {
                wc.rewrite(tp.getLeaf(), parsed);
            } else {
                if (isFakeBlock(parsed)) {
                    TreePath parent = tp.getParentPath();
                    List<? extends StatementTree> statements = ((BlockTree) parsed).getStatements();

                    statements = statements.subList(1, statements.size() - 1);

                    if (parent.getLeaf().getKind() == Kind.BLOCK) {
                        List<StatementTree> newStatements = new LinkedList<StatementTree>();

                        for (StatementTree st : ((BlockTree) parent.getLeaf()).getStatements()) {
                            if (st == tp.getLeaf()) {
                                newStatements.addAll(statements);
                            } else {
                                newStatements.add(st);
                            }
                        }

                        wc.rewrite(parent.getLeaf(), wc.getTreeMaker().Block(newStatements, ((BlockTree) parent.getLeaf()).isStatic()));
                    } else {
                        wc.rewrite(tp.getLeaf(), wc.getTreeMaker().Block(statements, false));
                    }
                } else if (isFakeClass(parsed)) {
                    TreePath parent = tp.getParentPath();
                    List<? extends Tree> members = ((ClassTree) parsed).getMembers();

                    members = members.subList(1, members.size());

                    assert parent.getLeaf().getKind() == Kind.CLASS;
                    
                    List<Tree> newMembers = new LinkedList<Tree>();

                    ClassTree ct = (ClassTree) parent.getLeaf();
                    
                    for (Tree t : ct.getMembers()) {
                        if (t == tp.getLeaf()) {
                            newMembers.addAll(members);
                        } else {
                            newMembers.add(t);
                        }
                    }

                    wc.rewrite(parent.getLeaf(), wc.getTreeMaker().Class(ct.getModifiers(), ct.getSimpleName(), ct.getTypeParameters(), ct.getExtendsClause(), ct.getImplementsClause(), newMembers));
                } else {
                    List<StatementTree> newStatements = new LinkedList<StatementTree>();

                    newStatements.add(wc.getTreeMaker().ExpressionStatement(wc.getTreeMaker().Identifier("$$1$")));
                    newStatements.add((StatementTree) parsed);
                    newStatements.add(wc.getTreeMaker().ExpressionStatement(wc.getTreeMaker().Identifier("$$2$")));

                    parsed = wc.getTreeMaker().Block(newStatements, ((BlockTree) tp.getLeaf()).isStatic());

                    wc.rewrite(tp.getLeaf(), parsed);
                }
            }

            new TreePathScanner<Void, Void>() {
                @Override
                public Void visitIdentifier(IdentifierTree node, Void p) {
                    String name = node.getName().toString();
                    TreePath tp = parameters.get(name);

                    if (tp != null) {
                        if (tp.getLeaf() instanceof Hacks.RenameTree) {
                            Hacks.RenameTree rt = (Hacks.RenameTree) tp.getLeaf();
                            Tree nue = wc.getTreeMaker().setLabel(rt.originalTree, rt.newName);

                            wc.rewrite(node, nue);

                            return null;
                        }
                        if (!parameterNames.containsKey(name)) {
                            wc.rewrite(node, tp.getLeaf());
                            return null;
                        }
                    }

                    String variableName = parameterNames.get(name);

                    if (variableName != null) {
                        wc.rewrite(node, wc.getTreeMaker().Identifier(variableName));
                        return null;
                    }

                    Element e = wc.getTrees().getElement(getCurrentPath());

                    if (e != null && isStaticElement(e)) {
                        wc.rewrite(node, wc.getTreeMaker().QualIdent(e));
                    }

                    return super.visitIdentifier(node, p);
                }
                @Override
                public Void visitMemberSelect(MemberSelectTree node, Void p) {
                    Element e = wc.getTrees().getElement(getCurrentPath());

                    if (e == null || (e.getKind() == ElementKind.CLASS && ((TypeElement) e).asType().getKind() == TypeKind.ERROR)) {
                        if (node.getExpression().getKind() == Kind.IDENTIFIER) {
                            String name = ((IdentifierTree) node.getExpression()).getName().toString();

                            if (name.startsWith("$") && parameters.get(name) == null) {
                                //XXX: unbound variable, use identifier instead of member select - may cause problems?
                                wc.rewrite(node, wc.getTreeMaker().Identifier(node.getIdentifier()));
                                return null;
                            }
                        }

                        return super.visitMemberSelect(node, p);
                    }

                    //check correct dependency:
                    checkDependency(wc, e, callback);

                    if (isStaticElement(e)) {
                        wc.rewrite(node, wc.getTreeMaker().QualIdent(e));

                        return null;
                    } else {
                        return super.visitMemberSelect(node, p);
                    }
                }

                @Override
                public Void visitVariable(VariableTree node, Void p) {
                    String name = node.getName().toString();

                    if (name.startsWith("$")) {
                        String nueName = parameterNames.get(name);

                        if (nueName != null) {
                            VariableTree nue = wc.getTreeMaker().Variable(node.getModifiers(), nueName, node.getType(), node.getInitializer());

                            wc.rewrite(node, nue);

                            return super.visitVariable(nue, p);
                        }
                    }

                    return super.visitVariable(node, p);
                }

                @Override
                public Void visitExpressionStatement(ExpressionStatementTree node, Void p) {
                    CharSequence name = Utilities.getWildcardTreeName(node);

                    if (name != null) {
                        TreePath tp = parameters.get(name.toString());

                        if (tp != null) {
                            wc.rewrite(node, tp.getLeaf());
                            return null;
                        }
                    }

                    return super.visitExpressionStatement(node, p);
                }
                @Override
                public Void visitBlock(BlockTree node, Void p) {
                    List<? extends StatementTree> nueStatement = resolveMultiParameters(node.getStatements());
                    BlockTree nue = wc.getTreeMaker().Block(nueStatement, node.isStatic());

                    wc.rewrite(node, nue);

                    return super.visitBlock(nue, p);
                }
                @Override
                public Void visitMethodInvocation(MethodInvocationTree node, Void p) {
                    List<? extends ExpressionTree> typeArgs = (List<? extends ExpressionTree>) resolveMultiParameters(node.getTypeArguments());
                    List<? extends ExpressionTree> args = resolveMultiParameters(node.getArguments());
                    MethodInvocationTree nue = wc.getTreeMaker().MethodInvocation(typeArgs, node.getMethodSelect(), args);

                    wc.rewrite(node, nue);

                    return super.visitMethodInvocation(nue, p);
                }
                @Override
                public Void visitNewClass(NewClassTree node, Void p) {
                    List<? extends ExpressionTree> typeArgs = (List<? extends ExpressionTree>) resolveMultiParameters(node.getTypeArguments());
                    List<? extends ExpressionTree> args = resolveMultiParameters(node.getArguments());
                    NewClassTree nue = wc.getTreeMaker().NewClass(node.getEnclosingExpression(), typeArgs, node.getIdentifier(), args, node.getClassBody());

                    wc.rewrite(node, nue);
                    return super.visitNewClass(nue, p);
                }
                @Override
                public Void visitParameterizedType(ParameterizedTypeTree node, Void p) {
                    List<? extends ExpressionTree> typeArgs = (List<? extends ExpressionTree>) resolveMultiParameters(node.getTypeArguments());
                    ParameterizedTypeTree nue = wc.getTreeMaker().ParameterizedType(node.getType(), typeArgs);

                    wc.rewrite(node, nue);
                    return super.visitParameterizedType(node, p);
                }
                private <T extends Tree> List<T> resolveMultiParameters(List<T> list) {
                    if (!Utilities.containsMultistatementTrees(list)) return list;

                    List<T> result = new LinkedList<T>();

                    for (T t : list) {
                        if (Utilities.isMultistatementWildcardTree(t)) {
                            Collection<TreePath> embedded = parametersMulti.get(Utilities.getWildcardTreeName(t).toString());

                            if (embedded != null) {
                                for (TreePath tp : embedded) {
                                    result.add((T) tp.getLeaf());
                                }
                            }
                        } else {
                            result.add(t);
                        }
                    }

                    return result;
                }
            }.scan(new TreePath(new TreePath(tp.getCompilationUnit()), parsed), null);
        }
    }

    static {
        JavaFixImpl.Accessor.INSTANCE = new JavaFixImpl.Accessor() {
            @Override
            public String getText(JavaFix jf) {
                return jf.getText();
            }
            @Override
            public ChangeInfo process(JavaFix jf, WorkingCopy wc, UpgradeUICallback callback) throws Exception {
                return jf.process(wc, callback);
            }
            @Override
            public FileObject getFile(JavaFix jf) {
                return jf.getFile();
            }
        };
    }
    
}
