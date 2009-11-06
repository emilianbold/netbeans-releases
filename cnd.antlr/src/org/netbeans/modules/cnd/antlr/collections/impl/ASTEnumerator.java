package org.netbeans.modules.cnd.antlr.collections.impl;

/* ANTLR Translator Generator
 * Project led by Terence Parr at http://www.cs.usfca.edu
 * Software rights: http://www.antlr.org/license.html
 */

import org.netbeans.modules.cnd.antlr.collections.AST;

import java.util.NoSuchElementException;

public class ASTEnumerator implements org.netbeans.modules.cnd.antlr.collections.ASTEnumeration {
    /** The list of root nodes for subtrees that match */
    private final VectorEnumerator nodes;
    int i = 0;


    public ASTEnumerator(Vector v) {
        nodes = new VectorEnumerator(v);
    }

    public boolean hasMoreNodes() {
        synchronized (nodes) {
            return i <= nodes.vector.lastElement;
        }
    }

    public org.netbeans.modules.cnd.antlr.collections.AST nextNode() {
        synchronized (nodes) {
            if (i <= nodes.vector.lastElement) {
                return (AST)nodes.vector.data[i++];
            }
            throw new NoSuchElementException("ASTEnumerator");
        }
    }
}
