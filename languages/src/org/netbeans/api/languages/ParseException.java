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


/**
 * Parse Exception.
 * 
 * @author Jan Jancura
 */
public class ParseException extends Exception {
       
    private ASTNode node;

    
    /**
     * Creates a new instance of ParseException.
     */
    public ParseException () {}
    
    /**
     * Creates a new instance of ParseException with given error message.
     * 
     * @param text a text of message
     */
    public ParseException (String text) {
        super (text);
    }
    
    /**
     * Creates a new instance of ParseException encapsulating some other Exception.
     * 
     * @param ex an internal exception
     */
    public ParseException (Exception ex) {
        super (ex);
        if (ex instanceof ParseException)
            node = ((ParseException) ex).getASTNode ();
    }
    
    /**
     * Creates a new instance of ParseException with given error message and AST tree.
     * 
     * @param text a text of message
     * @param root a root of AST tree
     */
    public ParseException (String text, ASTNode root) {
        super (text);
        this.node = root;
    }
    
    /**
     * Creates a new instance of ParseException with given exception and AST tree.
     * 
     * @param ex an internal exception
     * @param root a root of AST tree
     */
    public ParseException (Exception ex, ASTNode root) {
        super (ex);
        this.node = root;
    }

    /**
     * Returns root of AST tree.
     * 
     * @return a root of AST tree
     */
    public ASTNode getASTNode () {
        return node;
    }
}

