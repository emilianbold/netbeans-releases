/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.api.languages;

import org.netbeans.api.languages.ParserManager.State;

/**
 * Listens on AST changes. Use {@link ParserManager.addASTEvaluator} to register
 * instance of ASTEvaluator.
 * 
 * @author Jan Jancura
 */
public abstract class ASTEvaluator {

    /**
     * Called when AST is changed before evaluation of AST tree.
     * 
     * @param state state of parser
     * @param root root node of ast tree
     */
    public abstract void beforeEvaluation (State state, ASTNode root);

    /**
     * Called when AST is changed after evaluation of tree.
     * 
     * @param state state of parser
     * @param root root node of ast tree
     */
    public abstract void afterEvaluation (State state, ASTNode root);

    /**
     * Called when AST is changed for all different ASTPaths.
     * 
     * @param state state of parser
     * @param path path to the current {@link ASTItem}
     */
    public abstract void evaluate (State state, ASTPath path);
}
