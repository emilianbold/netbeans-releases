/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Co pyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.editor.verification;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintSeverity;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.api.NameKind;
import org.netbeans.modules.php.editor.api.elements.FunctionElement;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionName;
import org.netbeans.modules.php.editor.parser.astnodes.MethodInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.StaticMethodInvocation;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class UnknownFunctionRule extends PHPRule{
    private static final Collection<String> IGNORED = new TreeSet<String>(Arrays.asList(
            "die", "empty", "eval", "exit", "isset", "print", "unset")); //NOI18N

    @Override
    public void visit(FunctionInvocation functionInvocation) {
        ASTNode parent = context.path.get(0);
        
        if (!(parent instanceof MethodInvocation 
                || parent instanceof StaticMethodInvocation)){
            
            String fname = CodeUtils.extractFunctionName(functionInvocation);
            
            if (fname != null && !IGNORED.contains(fname.toLowerCase())) {
                Set<FunctionElement> functions = context.getIndex().getFunctions(NameKind.exact(fname));                
                if (functions.size() == 0) {
                    FunctionName funcName = functionInvocation.getFunctionName();
                    OffsetRange range = new OffsetRange(funcName.getStartOffset(), funcName.getEndOffset());

                    Hint hint = new Hint(UnknownFunctionRule.this, getDisplayName(),
                            context.parserResult.getSnapshot().getSource().getFileObject(), range, null, 500);

                    addResult(hint);
                }
            }    
        }
        
        super.visit(functionInvocation);
    }
    
    public String getId() {
        return "unknown.function";
    }

    public String getDescription() {
        return NbBundle.getMessage(UnknownFunctionRule.class, "UnknownFunctionDesc");
    }

    public String getDisplayName() {
        return NbBundle.getMessage(UnknownFunctionRule.class, "UnknownFunctionDispName");
    }

    public HintSeverity getDefaultSeverity() {
        return HintSeverity.WARNING;
    }

    @Override
    public boolean getDefaultEnabled() {
        return false;
    }
}
