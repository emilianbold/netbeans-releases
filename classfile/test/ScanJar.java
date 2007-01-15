/*
 * ScanJar.java
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
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;

/**
 * ScanJar:  load all of the classes of a specified jar file,
 * useful for performance and regression testing.
 *
 * @author Thomas Ball
 */
public class ScanJar {
    String jarName;
    boolean includeCode;
    boolean toString;

    public static void main(String[] args) {
	boolean includeCode = false;
	boolean toString = false;
        if (args.length == 0)
            usage();
        for (int i = 0; i < args.length; i++) {
	    if (args[i].equals("-includeCode"))
		includeCode = true;
	    else if (args[i].equals("-toString"))
		toString = true;
            else if (args[i].charAt(0) == '-')
                usage();
            else {
                try {
                    ScanJar sj = new ScanJar(args[i], includeCode, toString);
		    System.out.print("scanning " + args[i]);
		    if (includeCode || toString) {
			System.out.print(": ");
			if (includeCode)
			    System.out.print("includeCode ");
			if (toString)
			    System.out.print("toString");
		    }
		    System.out.println();
		    ElapsedTimer timer = new ElapsedTimer();
		    int n = sj.scan();
		    System.out.println("scanned " + n + " files in " + 
				       timer.toString());
                } catch (IOException e) {
                    System.err.println("error accessing \"" + args[i] + 
                                       "\": " + e.toString());
                }
            }
        }
    }

    ScanJar(String name, boolean incl, boolean tos) {
	jarName = name;
	includeCode = incl;
	toString = tos;
    }

    /**
     * Reads  class entries from the jar file into ClassFile instances.
     * Returns the number of classes scanned.
     */
    public int scan() throws IOException {
	int n = 0;
	ZipFile zf = new ZipFile(jarName);
	Enumeration files = zf.entries();
	while (files.hasMoreElements()) {
	    ZipEntry entry = (ZipEntry)files.nextElement();
	    String name = entry.getName();
	    if (name.endsWith(".class")) {
                InputStream in = zf.getInputStream(entry);
                ClassFile cf = new ClassFile(in, includeCode);
                if (toString)
                    try {
                        cf.toString(); // forces loading of attributes.
                    } catch (InvalidClassFileAttributeException ex) {
                        System.out.println("error accessing " + name);
                        throw ex;
                    }
                in.close();
		n++;
	    }
	}
	zf.close();
	return n;
    }

    public static void usage() {
        System.err.println("usage:  java ScanJar [-includeCode] " + 
			   "[-toString] <jar file> [ <jar file> ...]");
        System.exit(1);
    }
}

