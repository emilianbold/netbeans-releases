/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package dwarfvsmodel;

import java.util.*;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.deep.*;

/**
 * Misc. model-related utility functions
 * @author vk155633
 */
public class ModelTree {
    
    public static Node<CsmDeclaration> createModelNode(CsmFunctionDefinition modelDefinition) {
	Node<CsmDeclaration> root = new Node<CsmDeclaration>();
	addCompoundStatement(root, modelDefinition.getBody());	
	Comparator<CsmDeclaration> comparator = new ComparisonUtils.CsmDeclarationComparator();
	root.flatten();
	root.sort(comparator);
//	for( Node<CsmDeclaration> child : root.getSubnodes() ) {
//	    child.flatten();
//	    child.sort(comparator);
//	}
	return root;
    }
    
    private static Node<CsmDeclaration> createModelNode(Iterable<CsmStatement> modelStatements) {
	Node<CsmDeclaration> node = new Node<CsmDeclaration>();
	for( CsmStatement stmt : modelStatements) {
	    addStatement(node, stmt);
	}
	return node;
    }
    
    private static void addStatement(Node<CsmDeclaration> node, CsmStatement stmt) {
	if( stmt != null ) {
	    CsmStatement.Kind kind = stmt.getKind();
	    if( kind == CsmStatement.Kind.DECLARATION ) {
		addDeclarationStatement(node, (CsmDeclarationStatement) stmt);
	    }
	    else if( kind == CsmStatement.Kind.COMPOUND ) {
		addCompoundStatement(node, (CsmCompoundStatement) stmt);
	    }
	    else if( kind == CsmStatement.Kind.DO_WHILE ) {
		addLoopStatement(node, (CsmLoopStatement) stmt);
	    }
	    else if( kind == CsmStatement.Kind.WHILE ) {
		addLoopStatement(node, (CsmLoopStatement) stmt);
	    }
	    else if( kind == CsmStatement.Kind.FOR ) {
		addForStatement(node, (CsmForStatement) stmt);
	    }
	    else if( kind == CsmStatement.Kind.SWITCH ) {
		addSwitchStatement(node, (CsmSwitchStatement) stmt);
	    }
	    else if( kind == CsmStatement.Kind.TRY_CATCH ) {
		addTryCatchStatement(node, (CsmTryCatchStatement) stmt);
	    }
	    else if( kind == CsmStatement.Kind.IF ) {
		addIfStatement(node, (CsmIfStatement) stmt);
	    }
	}
    }
	    
    private static void addCompoundStatement(Node<CsmDeclaration> node, CsmCompoundStatement stmt) {
	List<CsmStatement> statements = (stmt == null) ? new ArrayList<CsmStatement>(1) : stmt.getStatements();
	node.addSubnode(createModelNode(statements));
    }
    
    private static void addDeclarationStatement(Node<CsmDeclaration> node, CsmDeclarationStatement stmt) {
	for( CsmDeclaration decl : (Iterable<CsmDeclaration>) stmt.getDeclarators() ) {
	    node.addDeclaration(decl);
	}
    }
    
    private static void addCondition(Node<CsmDeclaration> node, CsmCondition condition) {
	if( condition != null ) {
	    if( condition.getKind() == CsmCondition.Kind.DECLARATION ) {
		node.addDeclaration(condition.getDeclaration());
	    }
	}
    }
    
    private static void addLoopOrCondBody(Node<CsmDeclaration> node, CsmStatement body) {
	if( body != null ) {
	    if( body.getKind() == CsmStatement.Kind.COMPOUND ) {
		//addCompoundStatement(node, (CsmCompoundStatement) body);
		List<CsmStatement> statements = (List<CsmStatement>) ((CsmCompoundStatement) body).getStatements();
		if( statements != null ) {
		    for( CsmStatement stmt : statements) {
			addStatement(node, stmt);
		    }
		}
	    }
	    else {
		Node<CsmDeclaration> subnode = new Node<CsmDeclaration>();
		addStatement(subnode, body);
		node.addSubnode(subnode);
	    }
	}
    }
    
    private static void addLoopStatement(Node<CsmDeclaration> node, CsmLoopStatement stmt) {
	
	boolean nested = stmt.getCondition() != null && stmt.getCondition().getKind() == CsmCondition.Kind.DECLARATION;
	Node<CsmDeclaration> subnode = nested ? new Node<CsmDeclaration>() : node;
	addCondition(subnode, stmt.getCondition());
	addStatementNestedIfNeed(subnode, stmt.getBody());
	if( nested ) {
	    node.addSubnode(subnode);
	}
    }
    
    private static void addForStatement(Node<CsmDeclaration> node, CsmForStatement stmt) {
	boolean nested = stmt.getInitStatement() != null && stmt.getInitStatement().getKind() == CsmStatement.Kind.DECLARATION;
	Node<CsmDeclaration> subnode = nested ? new Node<CsmDeclaration>() : node;
	addStatement(subnode, stmt.getInitStatement());
	addCondition(subnode, stmt.getCondition());
	addStatementNestedIfNeed(subnode, stmt.getBody());
	if( nested ) {
	    node.addSubnode(subnode);
	}
    }
    
    private static void addSwitchStatement(Node<CsmDeclaration> node, CsmSwitchStatement stmt) {
	
	boolean nested = stmt.getCondition() != null && stmt.getCondition().getKind() == CsmCondition.Kind.DECLARATION;
	Node<CsmDeclaration> subnode = nested ? new Node<CsmDeclaration>() : node;
	
	addCondition(subnode, stmt.getCondition());
	addLoopOrCondBody(subnode, stmt.getBody());

	if( nested ) {
	    node.addSubnode(subnode);
	}
    }

    private static void addTryCatchStatement(Node<CsmDeclaration> node, CsmTryCatchStatement stmt) {
	addStatement(node, stmt.getTryStatement());
	for( CsmExceptionHandler handler : (List<CsmExceptionHandler>) stmt.getHandlers() ) {
	    addExceptionHandler(node, handler);
	}
    }
    
    private static void addExceptionHandler(Node<CsmDeclaration> node, CsmExceptionHandler stmt) {
	if( stmt.isCatchAll() ) {
	    addCompoundStatement(node, stmt);
	}
	else {
	    Node<CsmDeclaration> subnode = new Node<CsmDeclaration>();
	    subnode.addDeclaration(stmt.getParameter());
	    addCompoundStatement(subnode, stmt);
	    node.addSubnode(subnode);
	}
    }

    private static void addIfStatement(Node<CsmDeclaration> node, CsmIfStatement stmt) {
	boolean nested = stmt.getCondition() != null && stmt.getCondition().getKind() == CsmCondition.Kind.DECLARATION;
	Node<CsmDeclaration> subnode = nested ? new Node<CsmDeclaration>() : node;
	addCondition(subnode, stmt.getCondition());
	addStatementNestedIfNeed(subnode, stmt.getThen());
	addStatementNestedIfNeed(subnode, stmt.getElse());
	if( nested ) {
	    node.addSubnode(subnode);
	}
    }
    
    private static void addStatementNestedIfNeed(Node<CsmDeclaration> node, CsmStatement stmt) {
	if( stmt != null ) {
	    if( stmt.getKind() == CsmStatement.Kind.COMPOUND ) {
		addStatement(node, stmt);
	    }
	    else {
		Node<CsmDeclaration> subnode = new Node<CsmDeclaration>();
		addStatement(subnode, stmt);
		node.addSubnode(subnode);
	    }
	}
    }
}
