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

import org.netbeans.modules.java.hints.errors.AddCast;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.Tree;
import java.io.IOException;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;


/**
 *
 * @author Jan Lahoda
 */
final class AddCastFix implements Fix {
    
    private JavaSource js;
    private String treeName;
    private String type;
    private int position;
    private boolean wrapWithBrackets;
    
    public AddCastFix(JavaSource js, String treeName, String type, int position, boolean wrapWithBrackets) {
        this.js = js;
        this.treeName = treeName;
        this.type = type;
        this.position = position;
        this.wrapWithBrackets = wrapWithBrackets;
    }
    
    public ChangeInfo implement() {
        try {
            js.runModificationTask(new Task<WorkingCopy>() {

                public void run(final WorkingCopy working) throws IOException {
                    working.toPhase(Phase.RESOLVED);
                    TypeMirror[] tm = new TypeMirror[1];
                    ExpressionTree[] expression = new ExpressionTree[1];
                    Tree[] leaf = new Tree[1];
                    
                    AddCast.computeType(working, position, tm, expression, leaf);
                    
                    if (tm[0] == null) {
                        //cannot resolve anymore:
                        return ;
                    }
                    
                    TreeMaker make = working.getTreeMaker();
                    ExpressionTree toCast = expression[0];
                    
                    if (wrapWithBrackets) {
                        toCast = make.Parenthesized(toCast);
                    }
                    
                    ExpressionTree cast = make.TypeCast(make.Type(tm[0]), toCast);
                    
                    working.rewrite(expression[0], cast);
                }
            }).commit();
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }
        
        return null;
    }
    
    public String getText() {
        return NbBundle.getMessage(AddCastFix.class, "LBL_FIX_Add_Cast", treeName, type); // NOI18N
    }
    
    public String toDebugString() {
        return "[AddCastFix:" + treeName + ":" + type + "]";
    }

}
