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
        
        public ChangeInfo implement() throws IOException {
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
            
            return null;
        }
    }
}
