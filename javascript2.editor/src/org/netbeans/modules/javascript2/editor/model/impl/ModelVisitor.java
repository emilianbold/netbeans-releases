/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.editor.model.impl;

import com.oracle.nashorn.ir.FunctionNode;
import com.oracle.nashorn.ir.FunctionNode.Kind;
import com.oracle.nashorn.ir.Node;
import com.oracle.nashorn.ir.NodeVisitor;
import com.oracle.nashorn.ir.ObjectNode;
import com.oracle.nashorn.parser.Token;
import org.netbeans.modules.javascript2.editor.parser.JsParserResult;

/**
 *
 * @author Petr Pisl
 */
public class ModelVisitor extends NodeVisitor {

    private JsParserResult parserResult;
    private final FileScopeImpl fileScope;
    private final ModelBuilder modelBuilder;
    
    public ModelVisitor(JsParserResult parserResult) {
        this.parserResult = parserResult;
        this.fileScope = new FileScopeImpl(parserResult);
        this.modelBuilder = new ModelBuilder(this.fileScope);
    }
    
    @Override
    public Node visit(FunctionNode functionNode, boolean onset) {
        if (functionNode.getKind() != Kind.SCRIPT) {
            if (onset) {
                System.out.println("FunctionNode: " + functionNode.getName());
                System.out.println("    indnetNode: " + functionNode.getIdent());
                System.out.println("    varNode: " + functionNode.getVarArgsSymbol());
                System.out.println("    offsetRange: " + functionNode.getStart() + ", " + (Token.descPosition(functionNode.getLastToken()) + Token.descLength(functionNode.getLastToken())));
                ScopeImpl scope = modelBuilder.getCurrentScope();
                FunctionScopeImpl fncScope = ModelElementFactory.create(functionNode, modelBuilder);

                modelBuilder.setCurrentScope(scope = fncScope);
            } else {
                modelBuilder.reset();
            }
        }
        return super.visit(functionNode, onset);
    }

    public FileScopeImpl getFileScope() {
        return fileScope;
    }

    @Override
    public Node visit(ObjectNode objectNode, boolean onset) {
        if (onset) {
            ScopeImpl scope = modelBuilder.getCurrentScope();
            ObjectScopeImpl objectScope = ModelElementFactory.create(objectNode, modelBuilder);

            modelBuilder.setCurrentScope(scope = objectScope);
        } else {
            modelBuilder.reset();
        }

        return super.visit(functionNode, onset);
    }
    
    
    
}
