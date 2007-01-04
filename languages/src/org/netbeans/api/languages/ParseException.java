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
 *
 * @author Jan Jancura
 */
public class ParseException extends Exception {
        
    public ParseException () {}
    
    public ParseException (String text) {
        super (text);
    }
    
    public ParseException (Exception ex) {
        super (ex);
        if (ex instanceof ParseException)
            node = ((ParseException) ex).getASTNode ();
    }
    
    private ASTNode node;
    
    public ParseException (String text, ASTNode node) {
        super (text);
        this.node = node;
    }
    
    public ParseException (Exception ex, ASTNode node) {
        super (ex);
        this.node = node;
    }
    
    public ASTNode getASTNode () {
        return node;
    }
    
    void setASTNode (ASTNode node) {
        this.node = node;
    }
}

