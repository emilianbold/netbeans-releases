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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.editor.parser.astnodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents a class instanciation.
 * This class holds the class name as an expression and
 * array of constructor parameters
 * <pre>e.g.<pre> new MyClass(),
 * new $a('start'),
 * new foo()(1, $a)
 */
public class ClassInstanceCreation extends Expression {

    private ClassName className;
    private ArrayList<Expression> ctorParams = new ArrayList<Expression>();

    public ClassInstanceCreation(int start, int end, ClassName className, Expression[] ctorParams) {
        super(start, end);
        this.className = className;
        this.ctorParams.addAll(Arrays.asList(ctorParams));
    }

    public ClassInstanceCreation(int start, int end, ClassName className, List<Expression> ctorParams) {
		this(start, end, className, ctorParams == null ? null : (Expression[]) ctorParams.toArray(new Expression[ctorParams.size()]));
	}

    /**
     * Class name of this instance creation node
     *
     * @return class name
     */
    public ClassName getClassName() {
        return className;
    }

    /**
     * List of expressions that were given to the the constructor
     *
     * @return list of expressions that were given to the the constructor
     */
    public List<Expression> ctorParams() {
        return this.ctorParams;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
