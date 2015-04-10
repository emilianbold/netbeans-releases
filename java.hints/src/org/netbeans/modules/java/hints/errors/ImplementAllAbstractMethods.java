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
package org.netbeans.modules.java.hints.errors;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.tools.Diagnostic;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.editor.GuardedException;
import org.netbeans.modules.java.editor.codegen.GeneratorUtils;
import org.netbeans.modules.java.hints.spi.ErrorRule;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 */
public final class ImplementAllAbstractMethods implements ErrorRule<Boolean>, OverrideErrorMessage<Boolean> {

    private static final String PREMATURE_EOF_CODE = "compiler.err.premature.eof"; // NOI18N
    
    /** Creates a new instance of ImplementAllAbstractMethodsCreator */
    public ImplementAllAbstractMethods() {
    }

    public Set<String> getCodes() {
        return new HashSet<String>(Arrays.asList(
                "compiler.err.abstract.cant.be.instantiated", // NOI18N
                "compiler.err.does.not.override.abstract", // NOI18N
                "compiler.err.abstract.cant.be.instantiated", // NOI18N
                "compiler.err.enum.constant.does.not.override.abstract")); // NOI18N
    }

    @NbBundle.Messages({
        "ERR_CannotOverrideAbstractMethods=Inherited abstract methods are not accessible and could not be implemented"
    })
    @Override
    public String createMessage(CompilationInfo info, String diagnosticKey, int offset, TreePath treePath, Data<Boolean> data) {
        TreePath path = deepTreePath(info, offset);
        Element e = info.getTrees().getElement(path);
        if (e == null || !e.getKind().isClass()) {
            TypeMirror tm = info.getTrees().getTypeMirror(path);
            if (tm == null || tm.getKind() != TypeKind.DECLARED) {
                if (path.getLeaf().getKind() == Tree.Kind.NEW_CLASS) {
                    tm = info.getTrees().getTypeMirror(new TreePath(path, ((NewClassTree)path.getLeaf()).getIdentifier()));
                }
            }
            if (tm != null && tm.getKind() == TypeKind.DECLARED) {
                e = ((DeclaredType)tm).asElement();
            } else {
                return null;
            }
        }
        if (e == null) {
            return null;
        }
        List<? extends ExecutableElement> lee = info.getElementUtilities().findUnimplementedMethods((TypeElement)e);
        Scope s = info.getTrees().getScope(path);
        for (ExecutableElement ee : lee) {
            if (!info.getTrees().isAccessible(s, ee, (DeclaredType)e.asType())) {
                data.setData(true);
                return Bundle.ERR_CannotOverrideAbstractMethods();
                
            }
        }
        return null;
    }
    
    public List<Fix> run(final CompilationInfo info, String diagnosticKey, final int offset, TreePath treePath, Data<Boolean> data) {
        final List<Fix> result = new ArrayList<Fix>();
        if (data != null && Boolean.TRUE == data.getData()) {
            return null;
        }
        analyze(offset, info, new Performer() {
            @Override
            public void fixAllAbstractMethods(TreePath pathToModify, Tree toModify) {
                result.add(new FixImpl(info.getJavaSource(), offset, null));
            }
            @Override
            public void makeClassAbstract(TreePath pathToModify, String className) {
                Tree toModify = pathToModify.getLeaf();
                Element el = info.getTrees().getElement(pathToModify);
                if (el == null) {
                    return;
                }
                if (el.getKind() == ElementKind.ENUM) {
                    result.add(new ImplementOnEnumValues(info.getJavaSource(), offset));
                } else {
                    result.add(new FixImpl(info.getJavaSource(), offset, className));
                }
            }
            public void inaccessibleMethod(ExecutableElement ee) {
                
            }
        });
        
        return result;
    }
    
    public void cancel() {
        //XXX: not done yet
    }

    public String getId() {
        return ImplementAllAbstractMethods.class.getName();
    }
    
    public String getDisplayName() {
        return NbBundle.getMessage(ImplementAllAbstractMethods.class, "LBL_Impl_Abstract_Methods"); // NOI18N
    }
    
    public String getDescription() {
        return NbBundle.getMessage(ImplementAllAbstractMethods.class, "DSC_Impl_Abstract_Methods"); // NOI18N
    }
    
    private static interface Performer {

        public void fixAllAbstractMethods(TreePath pathToModify, Tree toModify);
        public void makeClassAbstract(TreePath toModify, String className);
//        public void inaccessibleMethod(ExecutableElement ee);

    }

    private static TreePath deepTreePath(CompilationInfo info, int offset) {
        TreePath basic = info.getTreeUtilities().pathFor(offset);
        TreePath plusOne = info.getTreeUtilities().pathFor(offset + 1);
        
        if (plusOne.getParentPath() != null && plusOne.getParentPath().getLeaf() == basic.getLeaf()) {
            return plusOne;
        }
        
        return basic;
    }
    
    private static void analyze(int offset, CompilationInfo info, Performer performer) {
        analyze(deepTreePath(info, offset), info, performer);
    }
    
    private static void analyze(TreePath path, CompilationInfo info, Performer performer) {
        Element e = info.getTrees().getElement(path);
        boolean isUsableElement = e != null && (e.getKind().isClass() || e.getKind().isInterface());
        final Tree leaf = path.getLeaf();
        
        if (isUsableElement) {
            //#85806: do not propose implement all abstract methods when the current class contains abstract methods:
            for (ExecutableElement ee : ElementFilter.methodsIn(e.getEnclosedElements())) {
                if (ee.getModifiers().contains(Modifier.ABSTRACT)) {
                    performer.makeClassAbstract(path, e.getSimpleName().toString());
                    return;
                }
            }

            if (TreeUtilities.CLASS_TREE_KINDS.contains(leaf.getKind())) {
                CompilationUnitTree cut = info.getCompilationUnit();
                // do not offer for class declarations without body
                long start = info.getTrees().getSourcePositions().getStartPosition(cut, leaf);
                long end = info.getTrees().getSourcePositions().getEndPosition(cut, leaf);
                for (Diagnostic d : info.getDiagnostics()) {
                    long position = d.getPosition();
                    if (d.getCode().equals(PREMATURE_EOF_CODE) && position > start && position < end) {
                        return;
                    }
                }
            }
            
            performer.fixAllAbstractMethods(path, leaf);
            
            if (e.getKind() == ElementKind.CLASS && e.getSimpleName() != null && !e.getSimpleName().contentEquals(""))
                performer.makeClassAbstract(path, e.getSimpleName().toString());
        } else if (leaf.getKind() == Kind.NEW_CLASS) {
            //if the parent of path.getLeaf is an error, the situation probably is like:
            //new Runnable {}
            //(missing '()' for constructor)
            //do not propose the hint in this case:
            final boolean[] parentError = new boolean[] {false};
            new TreePathScanner() {
                @Override
                public Object visitNewClass(NewClassTree nct, Object o) {
                    if (leaf == nct) {
                        parentError[0] = getCurrentPath().getParentPath().getLeaf().getKind() == Kind.ERRONEOUS;
                    }
                    return super.visitNewClass(nct, o);
                }
            }.scan(path.getParentPath(), null);
            if (!parentError[0]) {
                performer.fixAllAbstractMethods(path, leaf);
            }
        } else if (e != null && e.getKind() == ElementKind.ENUM_CONSTANT && leaf.getKind() == Kind.VARIABLE) {
            VariableTree var = (VariableTree) leaf;
            if (var.getInitializer() != null && var.getInitializer().getKind() == Kind.NEW_CLASS) {
                NewClassTree nct = (NewClassTree) var.getInitializer();
                TreePath toModify = new TreePath(path, var.getInitializer());
                if (nct.getClassBody() != null) {
                    performer.fixAllAbstractMethods(new TreePath(toModify, nct.getClassBody()), nct.getClassBody());
                } else {
                    performer.fixAllAbstractMethods(toModify, leaf);
                }
            }
        }
    }
    
    /**
     * Implements the abstract methods on each declared value of an enum, if not alredy implemented. The enum may not
     * derive from any superclass, nor enum so anything abstract (or derived/not implemneted) in the enum type 
     * must be implemented by individual values.
     */
    @NbBundle.Messages({
        "LBL_FIX_Impl_Methods_Enum_Values=Implement abstract methods on all enum values"
    })
    static final class ImplementOnEnumValues implements Fix {
        private final JavaSource js;
        private final int offset;

        public ImplementOnEnumValues(JavaSource js, int offset) {
            this.js = js;
            this.offset = offset;
        }

        @Override
        public String getText() {
            return Bundle.LBL_FIX_Impl_Methods_Enum_Values();
        }
        
        @Override
        public ChangeInfo implement() throws IOException {
            final boolean[] repeat = new boolean[] { false };
            final int[] offsetArr = {offset};
            js.runModificationTask(new Task<WorkingCopy>() {

                public void run(final WorkingCopy copy) throws IOException {
                    copy.toPhase(Phase.RESOLVED);

                    TreePath enumPath = deepTreePath(copy, offset);
                    if (enumPath.getLeaf().getKind() != Tree.Kind.ENUM) {
                        return;
                    }
                    Element el = copy.getTrees().getElement(enumPath);
                    if (el == null) {
                        // TODO: report to user
                        return;
                    }
                    ArrayList<? extends Element> al = new ArrayList(el.getEnclosedElements());
                    Collections.reverse(al);
                    for (VariableElement e : ElementFilter.fieldsIn(al)) {
                        if (e.getKind() != ElementKind.ENUM_CONSTANT) {
                            continue;
                        }
                        analyze(copy.getTrees().getPath(e), copy, new Performer() {

                            @Override
                            public void fixAllAbstractMethods(TreePath pathToModify, Tree toModify) {
                                fixClassOrVariable(copy, pathToModify, toModify, offsetArr, repeat);
                            }

                            @Override
                            public void makeClassAbstract(TreePath toModify, String className) {
                                // no op
                            }

                        });
                    }
                }
            }).commit();
            if (!repeat[0]) {
                return null;
            }
            // second pass, actually implement all the methods
            js.runModificationTask(new Task<WorkingCopy>() {

                public void run(final WorkingCopy copy) throws IOException {
                    copy.toPhase(Phase.RESOLVED);

                    TreePath enumPath = deepTreePath(copy, offset);
                    if (enumPath.getLeaf().getKind() != Tree.Kind.ENUM) {
                        return;
                    }
                    Element el = copy.getTrees().getElement(enumPath);
                    if (el == null) {
                        // TODO: report to user
                        return;
                    }
                    for (VariableElement e : ElementFilter.fieldsIn(el.getEnclosedElements())) {
                        if (e.getKind() != ElementKind.ENUM_CONSTANT) {
                            continue;
                        }
                        analyze(copy.getTrees().getPath(e), copy, new Performer() {

                            @Override
                            public void fixAllAbstractMethods(TreePath pathToModify, Tree toModify) {
                                fixClassOrVariable(copy, pathToModify, toModify, offsetArr, repeat);
                            }

                            @Override
                            public void makeClassAbstract(TreePath toModify, String className) {
                                // no op
                            }

                        });
                    }
                }
            }).commit();
            return null;
        }
    }
    
    // copy from GeneratorUtils, need to change the processing a little.
    public static Map<? extends ExecutableElement, ? extends ExecutableElement> generateAllAbstractMethodImplementations(
            WorkingCopy wc, TreePath path) {
        assert TreeUtilities.CLASS_TREE_KINDS.contains(path.getLeaf().getKind());
        TypeElement te = (TypeElement)wc.getTrees().getElement(path);
        if (te == null) {
            return null;
        }
        Map<? extends ExecutableElement, ? extends ExecutableElement> ret;
        ClassTree clazz = (ClassTree)path.getLeaf();
        GeneratorUtilities gu = GeneratorUtilities.get(wc);
        ElementUtilities elemUtils = wc.getElementUtilities();
        List<? extends ExecutableElement> toImplement = elemUtils.findUnimplementedMethods(te);
        ret = Utilities.findConflictingMethods(wc, te, toImplement);
        if (ret.size() < toImplement.size()) {
            toImplement.removeAll(ret.keySet());
            List<? extends MethodTree> res = gu.createAbstractMethodImplementations(te, toImplement);
            clazz = gu.insertClassMembers(clazz, res);
            wc.rewrite(path.getLeaf(), clazz);
        }
        if (ret.isEmpty()) {
            return ret;
        }
        // should be probably elsewhere: UI separation
        String msg = ret.size() == 1 ?
                NbBundle.getMessage(ImplementAllAbstractMethods.class, "WARN_FoundConflictingMethods1", 
                        ret.keySet().iterator().next().getSimpleName()) :
                NbBundle.getMessage(ImplementAllAbstractMethods.class, "WARN_FoundConflictingMethodsMany", 
                        ret.keySet().size());
        
        StatusDisplayer.getDefault().setStatusText(msg, StatusDisplayer.IMPORTANCE_ERROR_HIGHLIGHT);
        return ret;
    }

    private static void fixClassOrVariable(WorkingCopy copy, TreePath pathToModify, Tree toModify,
            int[] offset, boolean[] repeat) {
        if (TreeUtilities.CLASS_TREE_KINDS.contains(toModify.getKind())) {
            generateAllAbstractMethodImplementations(copy, pathToModify);
            return;
        } else if (!(toModify.getKind() == Kind.NEW_CLASS || toModify.getKind() == Kind.VARIABLE)) {
            return;
        }
        int insertOffset = (int) copy.getTrees().getSourcePositions().getEndPosition(copy.getCompilationUnit(), toModify);
        if (insertOffset != (-1)) {
            try {
                copy.getDocument().insertString(insertOffset, " {}", null);
                offset[0] = insertOffset + 1;
                repeat[0] = true;
            } catch (GuardedException e) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        String message = NbBundle.getMessage(ImplementAllAbstractMethods.class, "ERR_CannotApplyGuarded");
                        StatusDisplayer.getDefault().setStatusText(message);
                    }
                });
            } catch (BadLocationException e) {
                Exceptions.printStackTrace(e);
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
        }
    }

    static final class FixImpl implements Fix {

        private JavaSource js;
        private int offset;
        private String makeClassAbstractName;
        
        public FixImpl(JavaSource js, int offset, String makeClassAbstractName) {
            this.js   = js;
            this.offset = offset;
            this.makeClassAbstractName = makeClassAbstractName;
        }
        
        public String getText() {
            return makeClassAbstractName == null ? 
                NbBundle.getMessage(ImplementAllAbstractMethods.class, "LBL_FIX_Impl_Abstract_Methods") : // MOI18N 
                NbBundle.getMessage(ImplementAllAbstractMethods.class, "LBL_FIX_Make_Class_Abstract", makeClassAbstractName); // MOI18N 
        }

        public ChangeInfo implement() throws IOException {
            final boolean[] repeat = new boolean[] {true};

            while (repeat[0]) {
                repeat[0] = false;
                js.runModificationTask(new Task<WorkingCopy>() {

                    public void run(final WorkingCopy copy) throws IOException {
                        copy.toPhase(Phase.RESOLVED);
                        final int[] offsetArr = new int[] { offset };
                        analyze(offset, copy, new Performer() {
                            public void fixAllAbstractMethods(TreePath pathToModify, Tree toModify) {
                                if (makeClassAbstractName != null) return;
                                fixClassOrVariable(copy, pathToModify, toModify, offsetArr, repeat);
                            }
                            public void makeClassAbstract(TreePath pathToModify, String className) {
                                Tree toModify = pathToModify.getLeaf();
                                if (makeClassAbstractName == null) return;
                                //the toModify has to be a class tree:
                                if (TreeUtilities.CLASS_TREE_KINDS.contains(toModify.getKind())) {
                                    ClassTree clazz = (ClassTree) toModify;
                                    ModifiersTree modifiers = clazz.getModifiers();
                                    Set<Modifier> newModifiersSet = new HashSet<Modifier>(modifiers.getFlags());

                                    newModifiersSet.add(Modifier.ABSTRACT);

                                    copy.rewrite(modifiers, copy.getTreeMaker().Modifiers(newModifiersSet, modifiers.getAnnotations()));
                                }
                            }
                        });
                        offset = offsetArr[0];
                    }
                }).commit();
            }
            return null;
        }

        String toDebugString() {
            return makeClassAbstractName == null ? "IAAM" : "MA:" + makeClassAbstractName;
        }

    }
}
