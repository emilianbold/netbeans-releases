/*
 * Closure.java
 *
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
 * Software is Sun Microsystems, Inc. Portions Copyright 2000-2001 Sun
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
 * Closure:  report all classes which this file references in one
 * way or another.  Note: this utility won't find classes which are
 * dynamically loaded.
 *
 * @author Thomas Ball
 */
public class Closure {
    String thisClass;
    Set closure;

    Closure(String spec) {
        thisClass = spec;
    }

    void buildClosure(boolean includeJDK)
      throws IOException {
        if (closure != null)
            return;
        closure = new HashSet();
        Set visited = new HashSet();
        Stack stk = new Stack();
        ClassName thisCN = ClassName.getClassName(thisClass.replace('.', '/'));
        stk.push(thisCN);
        visited.add(thisCN.getExternalName());

        while (!stk.empty()) {
            // Add class to closure.
            ClassName cn = (ClassName)stk.pop();
            InputStream is = findClassStream(cn.getType());
	    if (is == null) {
		System.err.println("couldn't find class: " + 
                                   cn.getExternalName());
		continue;
	    }
            ClassFile cfile = new ClassFile(is);
            closure.add(cfile.getName().getExternalName());
            
            ConstantPool pool = cfile.getConstantPool();
            Iterator refs = pool.getAllClassNames().iterator();
            while (refs.hasNext()) {
                ClassName cnRef = (ClassName)refs.next();
                String cname = cnRef.getExternalName();
                if (cname.indexOf('[') != -1) {
                    // skip arrays
                } else if (!includeJDK && 
                           (cname.startsWith("java.") || 
                            cname.startsWith("javax.") ||
                            cname.startsWith("sun.") ||
                            cname.startsWith("com.sun.corba") ||
                            cname.startsWith("com.sun.image") ||
                            cname.startsWith("com.sun.java.swing") ||
                            cname.startsWith("com.sun.naming") ||
                            cname.startsWith("com.sun.security"))) {
                    // if directed, skip JDK references
                } else {
                    boolean isNew = visited.add(cname);
                    if (isNew)
                        stk.push(cnRef);
                }
            }
        }
    }

    void dumpClosure(PrintStream out) {
        Iterator iter = new TreeSet(closure).iterator();
        while (iter.hasNext())
            out.println((String)iter.next());
    }

    Iterator dependencies() {
        return closure.iterator();
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
            "usage:  java Closure [-includejdk] <class> [ <class> ...]");
        System.exit(1);
    }

    public static void main(String[] args) {
        if (args.length == 0)
            usage();

        boolean includeJDK = false;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-includejdk"))
                includeJDK = true;
            else if (args[i].charAt(0) == '-')
                usage();
            else {
                try {
                    Closure c = new Closure(args[i]);
                    c.buildClosure(includeJDK);
                    c.dumpClosure(System.out);
                } catch (IOException e) {
                    System.err.println("error accessing \"" + args[i] + 
                                       "\": " + e.toString());
                }
            }
        }
    }
}
