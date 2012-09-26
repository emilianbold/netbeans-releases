/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.editor.verification;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.text.BadLocationException;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.HintSeverity;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.model.FileScope;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.TypeScope;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.verification.PHPHintsProvider.Kind;
import org.openide.util.NbBundle.Messages;

/**
 * @author Radek Matous
 */
public class TypeRedeclarationHint extends AbstractRule {
    @Override
    @Messages({
        "# {0} - Type name",
        "TypeRedeclarationDesc=Type \"{0}\" has been already declared"
    })
    void computeHintsImpl(PHPRuleContext context, List<Hint> hints, Kind kind) throws BadLocationException {
        PHPParseResult phpParseResult = (PHPParseResult) context.parserResult;
        if (phpParseResult.getProgram() == null) {
            return;
        }
        FileScope fileScope = context.fileScope;
        if (fileScope != null) {
            Collection<? extends TypeScope> declaredTypes = ModelUtils.getDeclaredTypes(fileScope);
            Set<String> typeNames = new HashSet<String>();
            for (TypeScope typeScope : declaredTypes) {
                final QualifiedName qualifiedName = typeScope.getNamespaceName().append(typeScope.getName()).toFullyQualified();
                final String name = qualifiedName.toString();
                if (typeNames.contains(name)) {
                    continue;
                }
                typeNames.add(name);
                List<? extends TypeScope> instances = ModelUtils.filter(declaredTypes, qualifiedName);
                if (instances.size() > 1) {
                    TypeScope firstDeclaredInstance = null;
                    for (TypeScope typeInstance : instances) {
                        if (firstDeclaredInstance == null) {
                            firstDeclaredInstance = typeInstance;
                        } else if (firstDeclaredInstance.getOffset() > typeInstance.getOffset()) {
                            firstDeclaredInstance = typeInstance;
                        }
                    }
                    for (TypeScope typeInstance : instances) {
                        if (typeInstance != firstDeclaredInstance) {
                            hints.add(new Hint(this, Bundle.TypeRedeclarationDesc(firstDeclaredInstance.getName()),
                                    context.parserResult.getSnapshot().getSource().getFileObject(),
                                    typeInstance.getNameRange(), Collections.<HintFix>emptyList(), 500));

                        }
                    }
                }
            }
        }
    }

    @Override
    public String getId() {
        return "Type.Redeclaration.Rule";//NOI18N
    }

    @Override
    @Messages("TypeRedeclarationRuleDesc=Type Redeclaration")
    public String getDescription() {
        return Bundle.TypeRedeclarationRuleDesc();
    }

    @Override
    @Messages("TypeRedeclarationRuleDispName=Type Redeclaration")
    public String getDisplayName() {
        return Bundle.TypeRedeclarationRuleDispName();
    }

    @Override
    public HintSeverity getDefaultSeverity() {
        return HintSeverity.ERROR;
    }

}
