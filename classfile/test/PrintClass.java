/*
 * PrintClass.java
 *
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2000-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * Contributor(s): Thomas Ball
 *
 * Version: $Revision$
 */

import org.netbeans.modules.classfile.*;
import java.io.*;
import java.util.*;

/**
 * PrintClass:  write a class as a println statement.
 *
 * @author Thomas Ball
 */
public class PrintClass {
    String thisClass;

    PrintClass(String spec) {
        thisClass = spec;
    }

    void print(PrintStream out) throws IOException {
        ClassName cn = ClassName.getClassName(thisClass.replace('.', '/'));
	InputStream is = findClassStream(cn.getType());
	if (is == null) {
	    System.err.println("couldn't find class: " + 
			       cn.getExternalName());
	    return;
	}
	ClassFile cfile = new ClassFile(is);
        out.println(cfile);
    }

    private InputStream findClassStream(String className) {
        InputStream is = 
            ClassLoader.getSystemClassLoader().getResourceAsStream(className + ".class");
        return is;
    }

    /**
     * An error routine which displays the command line usage
     * before exiting.
     */
    public static void usage() {
        System.err.println(
            "usage:  java PrintClass <class> [ <class> ...]");
        System.exit(1);
    }

    public static void main(String[] args) {
        if (args.length == 0)
            usage();

        for (int i = 0; i < args.length; i++) {
            if (args[i].charAt(0) == '-')
                usage();
            else {
                try {
                    PrintClass pc = new PrintClass(args[i]);
                    pc.print(System.out);
                } catch (IOException e) {
                    System.err.println("error accessing \"" + args[i] + 
                                       "\": " + e.toString());
                }
            }
        }
    }
}
