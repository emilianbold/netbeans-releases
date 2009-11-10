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
