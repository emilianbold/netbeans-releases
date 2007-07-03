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

package org.netbeans.api.gsf;

import javax.swing.tree.TreeNode;
import org.netbeans.api.gsf.Element;


/**
 * Result from a Parser. Typically subclassed by each parser implementation
 * to stash additional information it might need, such as an AST root node,
 * for use by related clients of the parse tree such as the code completion
 * or declaration finders.
 * 
 * @todo Stash the errors on the parser result too? Sounds reasonable!
 *
 * @author Tor Norbye
 */
public abstract class ParserResult {
    protected final ParserFile file;

    /** Creates a new instance of ParserResult */
    public ParserResult(ParserFile file) {
        this.file = file;
    }
    
    // Todo: Rename to getRootObject
    public abstract Element getRoot();

    /** AST tree - optional; for debugging only */
    public abstract AstTreeNode getAst();

    // XXX Make top level, and document debugging purpose.
    // And remove all references to it in the client code.
    public interface AstTreeNode extends TreeNode {
        public Object getAstNode();
        public int getStartOffset();
        public int getEndOffset();
    }
    
    public ParserFile getFile() {
        return file;
    }
}
