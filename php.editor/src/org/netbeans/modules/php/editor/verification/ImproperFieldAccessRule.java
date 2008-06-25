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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.gsf.api.Hint;
import org.netbeans.modules.gsf.api.HintSeverity;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.FieldAccess;
import org.netbeans.modules.php.editor.parser.astnodes.FieldsDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.Program;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.openide.util.NbBundle;

/**
 *
 * @author Radek Matous
 */
public class ImproperFieldAccessRule extends PHPRule {
    private List<String> fieldNames = Collections.emptyList();

    public HintSeverity getDefaultSeverity() {
        return HintSeverity.WARNING;
    }

    public String getId() {
        return "improper.field.access"; //NOI18N
    }

    @Override
    public void visit(Program program) {
        fieldNames = new ArrayList<String>();
        super.visit(program);
    }

    @Override
    public void visit(FieldsDeclaration fieldsDeclaration) {
        super.visit(fieldsDeclaration);
        Variable[] variableNames = fieldsDeclaration.getVariableNames();
        for (Variable variable : variableNames) {
            fieldNames.add(extractVariableName(variable));
        }
        super.visit(fieldsDeclaration);
    }

    @Override
    public void visit(FieldAccess fieldAccess) {
        super.visit(fieldAccess);
        Variable field = fieldAccess.getField();
        if (field.isDollared()) {
            boolean addHint = false;
            if (fieldNames.contains(extractVariableName(field))) {
                addHint = true;
            } else if (context.variableStack.isVariableDefined(extractVariableName(field))) {
                addHint = false;
            } else {
                addHint = true;
            }
            if (addHint) {
                OffsetRange range = new OffsetRange(field.getStartOffset(), field.getEndOffset());

                Hint hint = new Hint(ImproperFieldAccessRule.this, getDescription(),
                        context.compilationInfo.getFileObject(), range, null, 500);

                addResult(hint);
            }
        }
        super.visit(fieldAccess);
    }

    private static String extractVariableName(Variable var) {
        if (var.getName() instanceof Identifier) {
            Identifier id = (Identifier) var.getName();
            return id.getName();
        } else {
            if (var.getName() instanceof Variable) {
                Variable name = (Variable) var.getName();
                return extractVariableName(name);
            }
        }

        return null;
    }

    public String getDescription() {
        return NbBundle.getMessage(ImproperFieldAccessRule.class, "ImproperFieldAccessDesc");
    }

    public String getDisplayName() {
        return getDescription();
    }
}