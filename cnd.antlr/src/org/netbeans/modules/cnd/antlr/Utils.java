/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.antlr;

import org.netbeans.modules.cnd.antlr.collections.AST;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Utils {

    /**
     * Constructor is private to prevent creating instances of this class.
     */
    private Utils() {
    }

    private static boolean useSystemExit = true;
	private static boolean useDirectClassLoading = false;
	static {
		if ("true".equalsIgnoreCase(System.getProperty("ANTLR_DO_NOT_EXIT", "false"))) {
			useSystemExit = false;
        }
		if ("true".equalsIgnoreCase(System.getProperty("ANTLR_USE_DIRECT_CLASS_LOADING", "false"))) {
			useDirectClassLoading = true;
        }
	}

	/** Thanks to Max Andersen at JBOSS and Scott Stanchfield */
	public static Class loadClass(String name) throws ClassNotFoundException {
		try {
			ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
			if (!useDirectClassLoading && contextClassLoader!=null ) {
				return contextClassLoader.loadClass(name);
			}
			return Class.forName(name);
		}
		catch (Exception e) {
			return Class.forName(name);
		}
	}

	public static Object createInstanceOf(String name) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		return loadClass(name).newInstance();
	}

	public static void error(String message) {
		if (useSystemExit) {
			System.exit(1);
        }
		throw new RuntimeException("ANTLR Panic: " + message);
	}

	public static void error(String message, Throwable t) {
		if (useSystemExit) {
			System.exit(1);
        }
		throw new RuntimeException("ANTLR Panic", t);
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
