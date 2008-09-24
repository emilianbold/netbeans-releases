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
package org.netbeans.modules.php.editor.parser.astnodes;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a function declaration
 * <pre>e.g.<pre>
 * function foo() {}
 * 
 * function &foo() {}
 * 
 * function foo($a, int $b, $c = 5, int $d = 6) {}
 * 
 * function foo(); -abstract function in class declaration
 */
public class FunctionDeclaration extends Statement {

    private boolean isReference;
    private Identifier name;
    private final ArrayList<FormalParameter> formalParameters = new ArrayList<FormalParameter>();
    private Block body;

    private FunctionDeclaration(int start, int end, Identifier functionName, FormalParameter[] formalParameters, Block body, final boolean isReference) {
        super(start, end);
        this.isReference = isReference;
        this.name = functionName;
        for (FormalParameter formalParameter : formalParameters) {
            this.formalParameters.add(formalParameter);
        }
        this.body = body;
    }

    public FunctionDeclaration(int start, int end, Identifier functionName, List<FormalParameter> formalParameters, Block body, final boolean isReference) {
        this(start, end, functionName, (FormalParameter[]) formalParameters.toArray(new FormalParameter[formalParameters.size()]), body, isReference);
    }

    /**
     * Body of this function declaration
     * 
     * @return Body of this function declaration
     */
    public Block getBody() {
        return body;
    }

    /**
     * List of the formal parameters of this function declaration
     * 
     * @return the parameters of this declaration   
     */
    public List<FormalParameter> getFormalParameters() {
        return this.formalParameters;
    }

    /**
     * Function name of this declaration
     *   
     * @return Function name of this declaration
     */
    public Identifier getFunctionName() {
        return name;
    }

    /**
     * True if this function's return variable will be referenced 
     * @return True if this function's return variable will be referenced
     */
    public boolean isReference() {
        return isReference;
    }
    
    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
