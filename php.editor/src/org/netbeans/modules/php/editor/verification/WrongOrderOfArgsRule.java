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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.editor.verification;

import java.util.Set;
import org.netbeans.modules.gsf.api.Hint;
import org.netbeans.modules.gsf.api.HintSeverity;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.php.editor.parser.astnodes.FormalParameter;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.MethodDeclaration;
import org.openide.util.NbBundle;

/**
 * Function/method arguments with default value defined must be
 * on the right side
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class WrongOrderOfArgsRule extends PHPRule {

    public String getId() {
        return "wrong.order.of.args"; //NOI18N
    }

    public String getDescription() {
        return NbBundle.getMessage(WrongOrderOfArgsRule.class, "WrongOrderOfArgsRuleDesc");
    }

    public String getDisplayName() {
        return NbBundle.getMessage(WrongOrderOfArgsRule.class, "WrongOrderOfArgsRuleDispName");
    }

    public HintSeverity getDefaultSeverity() {
        return HintSeverity.WARNING;
    }

    @Override
    public void visit(FunctionDeclaration functionDeclaration) {
        boolean wasDefaultVar = false;

        for (FormalParameter param : functionDeclaration.getFormalParameters()) {
            if (param.getDefaultValue() != null) {
                wasDefaultVar = true;
            } else if (wasDefaultVar) {
                OffsetRange range = new OffsetRange(param.getStartOffset(), param.getEndOffset());

                Hint hint = new Hint(WrongOrderOfArgsRule.this, getDisplayName(),
                        context.compilationInfo.getFileObject(), range, null, 500);

                addResult(hint);
                break;
            }
        }
    }

    @Override
    public void visit(MethodDeclaration methodDeclaration) {
        visit(methodDeclaration.getFunction());
    }
}
