/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.modelimpl.cache.impl;

import antlr.collections.AST;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Misc. static cache-related utilitiy finctions
 * @author Vladimir Kvasihn
 */
public class CacheUtil {

    public static String mangleName(String fileName, char filler) {
        StringBuffer sb = new StringBuffer();
        for( int i = 0; i < fileName.length(); i++ ) {
            char c = fileName.charAt(i);
            if( c == '\\' || c == '/' || c == ':' || c == ' ' ) {
                c = filler;
            }
            if( i > 0 || c != filler ) { // don't add first filler
                sb.append(c);
            }
        }
        return sb.toString();
    }
    
    static public void writeAST(ObjectOutputStream out, AST ast) throws IOException {
        out.writeObject(ast);
        if (ast != null) {
            // the tree structure has a lot of siblings =>
            // StackOverflow exceptions during serialization of "next" field
            // we try to prevent it by using own procedure of writing 
            // tree structure
            writeTree(out, ast);
        }
    }
    
    // symmetric to writeObject
    static public AST readAST(ObjectInputStream in) throws IOException, ClassNotFoundException {
        AST ast = (AST)in.readObject();
        if (ast != null) {
            // read tree structure into this node
            readTree(in, ast);
        }
        return ast;
    }

    ////////////////////////////////////////////////////////////////////////////
    // we have StackOverflow when serialize AST due to it's tree structure:
    // to many recurse calls to writeObject on writing "next" field
    // let's try to reduce depth of recursion by depth of tree
    
    private static final int CHILD = 1;
    private static final int SIBLING = 2;
    private static final int END_AST = 3;
    
    static private void writeTree(ObjectOutputStream out, AST root) throws IOException {
        assert (root != null) : "there must be something to write";
        AST node = root;
        do {
            AST child = node.getFirstChild();
            if (child != null) {
                // due to not huge depth of the tree                
                // write child without optimization
                out.writeInt(CHILD);
                writeAST(out, child);
            }
            node = node.getNextSibling();            
            if (node != null) {
                // we don't want to use recursion on writing sibling
                // to prevent StackOverflow, 
                // we use while loop for writing siblings
                out.writeInt(SIBLING);
                // write node data
                out.writeObject(node);                 
            }
        } while (node != null);
        out.writeInt(END_AST);
    }

    static private void readTree(ObjectInputStream in, AST root) throws IOException, ClassNotFoundException {
        assert (root != null) : "there must be something to read";
        AST node = root;
        do {
            int kind = in.readInt();
            switch (kind) {
                case END_AST:
                    return;
                case CHILD:
                    node.setFirstChild(readAST(in));
                    break;
                case SIBLING:
                    AST sibling = (AST) in.readObject();
                    node.setNextSibling(sibling);
                    node = sibling;
                    break;
                default:
                    assert(false);
            }            
        } while (node != null);
    }    
}
