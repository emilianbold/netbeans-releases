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
 * The AST root node for PHP program (meaning a PHP file).
 * The program holds array of statements such as Class, Function and evaluation statement.
 * The program also holds the PHP file comments.
 * 
 */
public class Program extends ASTNode {

    private final ArrayList<Statement> statements = new ArrayList<Statement>();
    
    /**
     * Comments array of the php program
     */
    private final ArrayList<Comment> comments = new ArrayList<Comment>();

    private Program(int start, int end, Statement[] statements, List<Comment> commentsList) {
        super(start, end);
        for (Statement statement : statements) {
//            statement.setParent(this);
            this.statements.add(statement);
        }
        for (Comment comment : commentsList) {
//            comment.setParent(this);
            this.comments.add((Comment) comment);
        }

    }

    public Program(int start, int end, List<Statement> statements, List<Comment> commentsList) {
        this(start, end, (Statement[]) statements.toArray(new Statement[statements.size()]), commentsList);
    }

    public List<Comment> getComments() {
            return comments;
    }
    /**
     * Retrieves the statement list of this program 
     * @return statement parts of this program
     */
    public List<Statement> getStatements() {
        return this.statements;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
