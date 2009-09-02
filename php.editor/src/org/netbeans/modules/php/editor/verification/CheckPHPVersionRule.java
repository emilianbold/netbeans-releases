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

import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintSeverity;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.php.editor.model.QualifiedName;
import org.netbeans.modules.php.editor.model.QualifiedNameKind;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.GotoLabel;
import org.netbeans.modules.php.editor.parser.astnodes.GotoStatement;
import org.netbeans.modules.php.editor.parser.astnodes.LambdaFunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.NamespaceDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.NamespaceName;
import org.netbeans.modules.php.editor.parser.astnodes.UseStatement;
import org.netbeans.modules.php.project.api.PhpLanguageOptions;
import org.netbeans.modules.php.project.api.PhpLanguageOptions.Properties;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class CheckPHPVersionRule extends PHPRule {
    public HintSeverity getDefaultSeverity() {
        return HintSeverity.ERROR;
    }

    public String getId() {
        return "check.ver"; //NOI18N
    }

    public String getDescription() {
        return NbBundle.getMessage(CheckPHPVersionRule.class, "CheckPHPVerDesc");
    }

    public String getDisplayName() {
        return NbBundle.getMessage(CheckPHPVersionRule.class, "CheckPHPVerDispName");
    }

    @Override
    public boolean appliesTo(RuleContext context) {
        FileObject fobj = NbEditorUtilities.getFileObject(context.doc);

        if (fobj != null){
            Properties props = PhpLanguageOptions.getProperties(fobj);

            if (props.getPhpVersion() == PhpLanguageOptions.PhpVersion.PHP_5) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void visit(NamespaceDeclaration declaration) {
        createWarning(declaration);
    }

    @Override
    public void visit(LambdaFunctionDeclaration declaration) {
        createWarning(declaration);
    }
    
    @Override
    public void visit(GotoLabel label) {
        createWarning(label);
    }

    @Override
    public void visit(GotoStatement statement) {
        createWarning(statement);
    }

    @Override
    public void visit(UseStatement statement) {
        createWarning(statement);
    }

    @Override
    public void visit(NamespaceName namespaceName) {

        QualifiedName qname = QualifiedName.create(namespaceName);
        if (qname.getKind() != QualifiedNameKind.UNQUALIFIED){
            createWarning(namespaceName);
        }
    }
    
    private void createWarning(ASTNode node){
        OffsetRange range = new OffsetRange(node.getStartOffset(), node.getEndOffset());

        Hint hint = new Hint(CheckPHPVersionRule.this, getDisplayName(),
                context.parserResult.getSnapshot().getSource().getFileObject(),
                range, null, 500);

        addResult(hint);
        super.visit(node);
    }


}