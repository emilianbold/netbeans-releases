/*
 * ScanJar.java
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
		    cf.toString(); // forces loading of attributes.
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

