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

import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TypeCastTree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.spi.ErrorRule;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 */
public final class RemoveUselessCast implements ErrorRule<Void> {
    
    public RemoveUselessCast() {
    }
    
    public Set<String> getCodes() {
        return Collections.singleton("compiler.warn.redundant.cast"); // NOI18N
    }
    
    public List<Fix> run(CompilationInfo info, String diagnosticKey, int offset, TreePath treePath, Data<Void> data) {
        TreePath path = info.getTreeUtilities().pathFor(offset + 1);
        
        if (path != null && path.getLeaf().getKind() == Kind.TYPE_CAST) {
            TreePathHandle handle = TreePathHandle.create(path, info);
            
            return Collections.<Fix>singletonList(new FixImpl(info.getJavaSource(), handle));
        }
        
        return Collections.<Fix>emptyList();
    }

    public void cancel() {
        //XXX: not yet implemented
    }
    
    public String getId() {
        return RemoveUselessCast.class.getName();
    }
    
    public String getDisplayName() {
        return NbBundle.getMessage(RemoveUselessCast.class, "LBL_Remove_Useless_Cast_Fix");
    }
    
    public String getDescription() {
        return NbBundle.getMessage(RemoveUselessCast.class, "DSC_Remove_Useless_Cast_Fix");
    }

    private static final class FixImpl implements Fix {
        
        private JavaSource js;
        private TreePathHandle handle;

        public FixImpl(JavaSource js, TreePathHandle handle) {
            this.js = js;
            this.handle = handle;
        }

        public String getText() {
            return NbBundle.getMessage(RemoveUselessCast.class, "LBL_FIX_Remove_redundant_cast");
        }
        
        public ChangeInfo implement() {
            try {
                js.runModificationTask(new Task<WorkingCopy>() {
                    public void run(WorkingCopy copy) throws IOException {
                        copy.toPhase(Phase.RESOLVED);
                        TreePath path = handle.resolve(copy);
                        
                        if (path != null) {
                            TypeCastTree tct = (TypeCastTree) path.getLeaf();
                            
                            copy.rewrite(tct, tct.getExpression());
                        }
                    }

                }).commit();
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
            
            return null;
        }
    }
}
