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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
import org.openide.util.Exceptions;

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
        return "Make Variable Final";
    }

    public String getDescription() {
        return "Make Variable Final";
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
            return "Make " + variableName + " final";
        }

        public ChangeInfo implement() {
            JavaSource js = JavaSource.forFileObject(file);
            
            try {
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
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
            
            return null;
        }
}
}
