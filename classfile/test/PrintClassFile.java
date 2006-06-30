/*
 * PrintClassFile.java
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
 * PrintClassFile:  write a class as a println statement.
 *
 * @author Thomas Ball
 */
public class PrintClassFile {
    String thisClass;

    PrintClassFile(String spec) {
        thisClass = spec;
    }

    void print(PrintStream out) throws IOException {
	InputStream is = new FileInputStream(thisClass);
	ClassFile cfile = new ClassFile(is);
        out.println(cfile);
    }

    /**
     * An error routine which displays the command line usage
     * before exiting.
     */
    public static void usage() {
        System.err.println(
            "usage:  java PrintClassFile <file> [ <file> ...]");
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
                    PrintClassFile pc = new PrintClassFile(args[i]);
                    pc.print(System.out);
                } catch (IOException e) {
                    System.err.println("error accessing \"" + args[i] + 
                                       "\": " + e.toString());
                }
            }
        }
    }
}
