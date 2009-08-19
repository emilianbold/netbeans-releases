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
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.util.ElementFilter;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.tools.Diagnostic;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
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
public final class ImplementAllAbstractMethods implements ErrorRule<Void> {

    private static final String PREMATURE_EOF_CODE = "compiler.err.premature.eof"; // NOI18N
    
    /** Creates a new instance of ImplementAllAbstractMethodsCreator */
    public ImplementAllAbstractMethods() {
    }

    public Set<String> getCodes() {
        return new HashSet<String>(Arrays.asList(
                "compiler.err.abstract.cant.be.instantiated", // NOI18N
                "compiler.err.does.not.override.abstract", // NOI18N
                "compiler.err.abstract.cant.be.instantiated")); // NOI18N
    }
    
    public List<Fix> run(final CompilationInfo info, String diagnosticKey, final int offset, TreePath treePath, Data<Void> data) {
        final List<Fix> result = new ArrayList<Fix>();

        analyze(info.getJavaSource(), offset, info, new Performer() {
            public void fixAllAbstractMethods(TreePath pathToModify, Tree toModify) {
                result.add(new FixImpl(info.getJavaSource(), offset, null));
            }
            public void makeClassAbstract(Tree toModify, String className) {
                result.add(new FixImpl(info.getJavaSource(), offset, className));
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
        public void makeClassAbstract(Tree toModify, String className);

    }

    private static void analyze(JavaSource js, int offset, CompilationInfo info, Performer performer) {
        final TreePath path = info.getTreeUtilities().pathFor(offset + 1);
        Element e = info.getTrees().getElement(path);
        boolean isUsableElement = e != null && (e.getKind().isClass() || e.getKind().isInterface()) && e.getKind() != ElementKind.ENUM;
        final Tree leaf = path.getLeaf();
        
        if (isUsableElement) {
            //#85806: do not propose implement all abstract methods when the current class contains abstract methods:
            for (ExecutableElement ee : ElementFilter.methodsIn(e.getEnclosedElements())) {
                if (ee.getModifiers().contains(Modifier.ABSTRACT)) {
                    performer.makeClassAbstract(leaf, e.getSimpleName().toString());
                    return;
                }
            }

            if (leaf.getKind() == Kind.CLASS) {
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
        } else {
            if (leaf.getKind() == Kind.NEW_CLASS) {
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
            }
        }
    }

    private static final class FixImpl implements Fix {

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
                        analyze(js, offset, copy, new Performer() {
                            public void fixAllAbstractMethods(TreePath pathToModify, Tree toModify) {
                                if (toModify.getKind() == Kind.NEW_CLASS) {
                                    int insertOffset = (int) copy.getTrees().getSourcePositions().getEndPosition(copy.getCompilationUnit(), toModify);
                                    if (insertOffset != (-1)) {
                                        try {
                                            copy.getDocument().insertString(insertOffset, " {}", null);
                                            offset = insertOffset + 1;
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
                                } else {
                                    GeneratorUtils.generateAllAbstractMethodImplementations(copy, pathToModify);
                                }
                            }
                            public void makeClassAbstract(Tree toModify, String className) {
                                //the toModify has to be a class tree:
                                if (toModify.getKind() == Kind.CLASS) {
                                    ClassTree clazz = (ClassTree) toModify;
                                    ModifiersTree modifiers = clazz.getModifiers();
                                    Set<Modifier> newModifiersSet = new HashSet<Modifier>(modifiers.getFlags());

                                    newModifiersSet.add(Modifier.ABSTRACT);

                                    copy.rewrite(modifiers, copy.getTreeMaker().Modifiers(newModifiersSet, modifiers.getAnnotations()));
                                }
                            }
                        });
                    }
                }).commit();
            }
            return null;
        }
        
    }
}
