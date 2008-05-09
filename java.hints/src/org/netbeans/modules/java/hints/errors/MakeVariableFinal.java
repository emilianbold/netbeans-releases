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

import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.spi.ErrorRule;
import org.netbeans.modules.java.hints.spi.ErrorRule.Data;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 */
public class MakeVariableFinal implements ErrorRule<Void> {
    
    public MakeVariableFinal() {
    }
    
    private static final Set<String> CODES = new HashSet<String>(Arrays.asList(
            "compiler.err.local.var.accessed.from.icls.needs.final"
    ));
    
    public Set<String> getCodes() {
        return CODES;
    }

    public List<Fix> run(CompilationInfo compilationInfo, String diagnosticKey, int offset, TreePath treePath, Data<Void> data) {
        Tree leaf = treePath.getLeaf();
        
        if (leaf.getKind() == Kind.IDENTIFIER) {
            Element el = compilationInfo.getTrees().getElement(treePath);
            TreePath declaration = compilationInfo.getTrees().getPath(el);
            
            if (declaration != null) {
                return Collections.singletonList((Fix) new FixImpl(compilationInfo.getFileObject(), el.getSimpleName().toString(), TreePathHandle.create(declaration, compilationInfo)));
            }
        }
        
        return Collections.<Fix>emptyList();
    }

    public void cancel() {
    }

    public String getId() {
        return MakeVariableFinal.class.getName();
    }

    public String getDisplayName() {
        return NbBundle.getMessage(MakeVariableFinal.class, "DN_MakeVariableFinal");
    }

    public String getDescription() {
        return NbBundle.getMessage(MakeVariableFinal.class, "DESC_MakeVariableFinal");
    }

    private static final class FixImpl implements Fix {
        
        private String variableName;
        private TreePathHandle variable;
        private FileObject file;
        
        public FixImpl(FileObject file, String variableName, TreePathHandle variable) {
            this.file = file;
            this.variableName = variableName;
            this.variable = variable;
        }
        public String getText() {
            return NbBundle.getMessage(MakeVariableFinal.class, "FIX_MakeVariableFinal", new Object[]{String.valueOf(variableName)});
        }

        public ChangeInfo implement() throws IOException {
            JavaSource js = JavaSource.forFileObject(file);
            
            js.runModificationTask(new Task<WorkingCopy>() {
                public void run(WorkingCopy wc) throws IOException {
                    wc.toPhase(Phase.RESOLVED);
                    TreePath tp = variable.resolve(wc);

                    if (tp == null)
                        return ;

                    VariableTree vt = (VariableTree) tp.getLeaf();
                    ModifiersTree mt = vt.getModifiers();
                    Set<Modifier> modifiers = EnumSet.noneOf(Modifier.class);

                    modifiers.addAll(mt.getFlags());
                    modifiers.add(Modifier.FINAL);

                    ModifiersTree newMod = wc.getTreeMaker().Modifiers(modifiers, mt.getAnnotations());

                    wc.rewrite(mt, newMod);
                }
            }).commit();
            
            return null;
        }
    }
}
