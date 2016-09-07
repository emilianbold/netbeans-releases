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
package org.netbeans.modules.cnd.antlr.build;

import java.io.*;

/** Simple class that uses build.Tool to compile ANTLR's Java stuff */
public class ANTLR {
    public static String compiler = "javac";
    public static String jarName = "org.netbeans.modules.cnd.antlr.jar";
    public static String root = ".";

    public static String[] srcdir = {
        "org/netbeans/modules/cnd/antlr",
        "org/netbeans/modules/cnd.antlr/actions/cpp",
        "org/netbeans/modules/cnd.antlr/actions/java",
        "org/netbeans/modules/cnd.antlr/actions/csharp",
        "org/netbeans/modules/cnd.antlr/collections",
        "org/netbeans/modules/cnd.antlr/collections/impl",
        "org/netbeans/modules/cnd.antlr/debug",
        "org/netbeans/modules/cnd.antlr/ASdebug",
        "org/netbeans/modules/cnd.antlr/debug/misc",
        "org/netbeans/modules/cnd.antlr/preprocessor"
    };

    public ANTLR() {
        compiler = System.getProperty("org.netbeans.modules.cnd.antlr.build.compiler", compiler);
        root = System.getProperty("org.netbeans.modules.cnd.antlr.build.root", root);
    }

    public String getName() { return "ANTLR"; }

    /** Build ANTLR.  action on cmd-line matches method name */
    public void build(Tool tool) {
        if ( !rootIsValidANTLRDir(tool) ) {
            return;
        }
        // run ANTLR on its own .g files
        tool.antlr(root+"/org/netbeans/modules/cnd/antlr/antlr.g");
        tool.antlr(root+"/org/netbeans/modules/cnd/antlr/tokdef.g");
        tool.antlr(root+"/org/netbeans/modules/cnd/antlr/preprocessor/preproc.g");
        tool.antlr(root+"/org/netbeans/modules/cnd/antlr/actions/java/action.g");
        tool.antlr(root+"/org/netbeans/modules/cnd/antlr/actions/cpp/action.g");
        tool.antlr(root+"/org/netbeans/modules/cnd/antlr/actions/csharp/action.g");
        for (int i=0; i<srcdir.length; i++) {
            String cmd = compiler+" -d "+root+" "+root+"/"+srcdir[i]+"/*.java";
            tool.system(cmd);
        }
    }

    /** Jar up all the .class files */
    public void jar(Tool tool) {
        if ( !rootIsValidANTLRDir(tool) ) {
            return;
        }
        StringBuffer cmd = new StringBuffer(2000);
        cmd.append("jar cvf "+root+"/"+jarName);
        for (int i=0; i<srcdir.length; i++) {
            cmd.append(" "+root+"/"+srcdir[i]+"/*.class");
        }
        tool.system(cmd.toString());
    }

    /** ANTLR root dir must contain an "org.netbeans.modules.cnd.antlr" dir and must have java
     *  files underneath etc...
     */
    protected boolean rootIsValidANTLRDir(Tool tool) {
        if ( root==null ) {
            return false;
        }
        File antlrRootDir = new File(root);
        if ( !antlrRootDir.exists() ) {
            tool.error("Property org.netbeans.modules.cnd.antlr.build.root=="+root+" does not exist");
            return false;
        }
        if ( !antlrRootDir.isDirectory() ) {
            tool.error("Property org.netbeans.modules.cnd.antlr.build.root=="+root+" is not a directory");
            return false;
        }
        String[] antlrDir = antlrRootDir.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return dir.isDirectory() && name.equals("org.netbeans.modules.cnd.antlr");
            }
        });
        if ( antlrDir==null || antlrDir.length==0 ) {
            tool.error("Property org.netbeans.modules.cnd.antlr.build.root=="+root+" does not appear to be a valid ANTLR project root (no org.netbeans.modules.cnd.antlr subdir)");
            return false;
        }
        File antlrPackageDir = new File(root+"/org.netbeans.modules.cnd.antlr");
        String[] antlrPackageJavaFiles = antlrPackageDir.list();
        if ( antlrPackageJavaFiles==null || antlrPackageJavaFiles.length==0 ) {
            tool.error("Property org.netbeans.modules.cnd.antlr.build.root=="+root+" does not appear to be a valid ANTLR project root (no .java files in org.netbeans.modules.cnd.antlr subdir");
            return false;
        }
        return true;
    }
}
