/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
 * Contributor(s): Thomas Ball
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

import java.io.InputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.netbeans.modules.classfile.ClassFile;

/**
 * Command-line tool to verify that all classes in one or more jar files have the
 * specified compilation target.
 * 
 * @author Tom Ball
 */
public class CheckTarget {
    static int targetVersion;
    
    public static void main(String[] args) {
        if (args.length < 2)
            usage();
        String target = args[0];
        if (target.equals("1.4"))
            targetVersion = 48;
        else if (target.equals("1.5"))
            targetVersion = 49;
        else if (target.equals("1.6"))
            targetVersion = 50;
        else
            usage();
        for (int i = 1; i < args.length; i++) {
            try {
                System.out.println("scanning " + args[i]);
                scan(args[i]);
            } catch (IOException e) {
                System.err.println("error accessing \"" + args[i] + 
                                   "\": " + e.toString());
            }
        }
        
    }

    private static String versionToTarget(int v) {
        switch (v) {
            case 48: return "1.4";
            case 49: return "1.5";
            case 50: return "1.6";
            default:
                return "major version: " + v;
        }
    }

    /**
     * Reads  class entries from the jar file into ClassFile instances.
     * Returns the number of classes scanned.
     */
    public static void scan(String jarName) throws IOException {
	ZipFile zf = new ZipFile(jarName);
	Enumeration files = zf.entries();
	while (files.hasMoreElements()) {
	    ZipEntry entry = (ZipEntry)files.nextElement();
	    String name = entry.getName();
	    if (name.endsWith(".class")) {
                InputStream in = zf.getInputStream(entry);
                ClassFile cf = new ClassFile(in, false);
                int version = cf.getMajorVersion();
                if (version > targetVersion) {
                    System.out.println(cf.getName().getExternalName() + 
                           " has invalid target: " + versionToTarget(version));
                }
                in.close();
	    }
	}
	zf.close();
    }
    
    public static void usage() {
        System.err.println("usage:  java CheckTarget < 1.4 | 1.5 | 1.6 > " + 
			   "<jar file> [ <jar file> ...]");
        System.exit(1);
    }
}
