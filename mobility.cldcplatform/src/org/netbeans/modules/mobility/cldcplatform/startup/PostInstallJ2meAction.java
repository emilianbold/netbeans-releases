/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

// Based on the code sent by Martin Ryzel
package org.netbeans.modules.mobility.cldcplatform.startup;


import java.io.*;
import java.text.*;

import org.openide.util.Utilities;


public class PostInstallJ2meAction {
    
    private static final String DEFAULT_STARTUP_WIN = "{0} -Dkvem.home={1} -cp {1}/wtklib/kenv.zip;{1}/wtklib/ktools.zip {2}\n"; //NOI18N
    private static final String EMULATOR_STARTUP_WIN = "{0} -Dkvem.home={1} -cp {1}/wtklib/kenv.zip;{1}/wtklib/ktools.zip;{1}/wtklib/customjmf.jar {2}\n"; //NOI18N
    private static final String UTILS_STARTUP_WIN = "{0} -Dkvem.home={1} -cp {1}/wtklib/kenv.zip;{1}/wtklib/ktools.zip;{1}/bin/JadTool.jar;{1}/bin/MEKeyTool.jar {2}\n"; //NOI18N
    private static final String WSCOMP_STARTUP_WIN = "{0} -Dkvem.home={1} -cp {1}/wtklib/ktools.zip;{1}/wtklib/kenv.zip;{1}/bin/j2me_sg_ri.jar;{1}/bin/schema2beansdev.jar;" +
            "{1}/lib/j2me-ws.jar;{1}/bin/jaxrpc-impl.jar;{1}/bin/jaxrpc-api.jar;{1}/bin/jaxrpc-spi.jar;{1}/bin/activation.jar;" +
            "{1}/bin/mail.jar;{1}/bin/saaj-api.jar;{1}/bin/saaj-impl.jar;{1}/bin/xsdlib.jar {2} %CMD_LINE_ARGS%";
    
    private static final String WSCOMP_STATIC = "@echo off\n"+
            "rem Get command line arguments and save them\n"+
            "set CMD_LINE_ARGS=\n"+
            ":setArgs\n"+
            "if \"\"%1\"\"==\"\"\"\" goto doneSetArgs\n"+
            "set CMD_LINE_ARGS=%CMD_LINE_ARGS% %1\n"+
            "shift\n"+
            "goto setArgs\n"+
            ":doneSetArgs\n";
    
    
    private static final String DEFAULT_STARTUP_UNIX = "#!/bin/sh\njavapathtowtk={0}/bin\n\nPRG=$0\n\n# Resolve soft links\n" + //NOI18N
            "while [ -h \"$PRG\" ]; do\n    ls=`/bin/ls -ld \"$PRG\"`\n    link=`/usr/bin/expr \"$ls\" : ''.*-> \\(.*\\)$''`\n" +   //NOI18N
            "    if /usr/bin/expr \"$link\" : ''^/'' > /dev/null 2>&1; then\n        PRG=\"$link\"\n    else\n        PRG=\"`/usr/bin/dirname $PRG`/$link\"\n    fi\n" + //NOI18N
            "done\nKVEM_BIN=`dirname $PRG`\nKVEM_HOME=`cd $'{'KVEM_BIN'}'/.. ; pwd`\nKVEM_LIB=$'{'KVEM_HOME'}'/wtklib\n" + //NOI18N
            "\"$'{'javapathtowtk'}'/java\" -Dkvem.home=\"$'{'KVEM_HOME'}'\" \\\n    -cp \"$'{'KVEM_LIB'}'/kenv.zip:$'{'KVEM_LIB'}'/ktools.zip\" \\\n    {1} \"$@\" {2}\n"; //NOI18N
    private static final String EMULATOR_STARTUP_UNIX = "#!/bin/sh\njavapathtowtk={0}/bin\n\nPRG=$0\n\n# Resolve soft links\n" + //NOI18N
            "while [ -h \"$PRG\" ]; do\n    ls=`/bin/ls -ld \"$PRG\"`\n    link=`/usr/bin/expr \"$ls\" : ''.*-> \\(.*\\)$''`\n" + //NOI18N
            "    if /usr/bin/expr \"$link\" : ''^/'' > /dev/null 2>&1; then\n        PRG=\"$link\"\n    else\n        PRG=\"`/usr/bin/dirname $PRG`/$link\"\n    fi\n" + //NOI18N
            "done\nKVEM_BIN=`dirname \"$PRG\"`\nKVEM_HOME=`cd \"$'{'KVEM_BIN'}'/..\" ; pwd`\nKVEM_LIB=\"$'{'KVEM_HOME'}'/wtklib\"\n" + //NOI18N
            "\"$'{'javapathtowtk'}'/java\" -Xms18m -Dkvem.home=\"$'{'KVEM_HOME'}'\" \\\n    -Djava.library.path=\"$'{'KVEM_HOME'}'/bin\" \\\n" + //NOI18N
            "    -cp \"$'{'KVEM_LIB'}'/kenv.zip:$'{'KVEM_LIB'}'/ktools.zip:$'{'KVEM_LIB'}'/customjmf.jar\" \\\n    {1} \"$@\" {2}\n"; //NOI18N
    private static final String UTILS_STARTUP_UNIX = "#!/bin/sh\njavapathtowtk={0}/bin\n\nPRG=$0\n\n# Resolve soft links\n" + //NOI18N
            "while [ -h \"$PRG\" ]; do\n    ls=`/bin/ls -ld \"$PRG\"`\n    link=`/usr/bin/expr \"$ls\" : ''.*-> \\(.*\\)$''`\n" + //NOI18N
            "    if /usr/bin/expr \"$link\" : ''^/'' > /dev/null 2>&1; then\n        PRG=\"$link\"\n    else\n        PRG=\"`/usr/bin/dirname $PRG`/$link\"\n    fi\n" + //NOI18N
            "done\nKVEM_BIN=`dirname $PRG`\nKVEM_HOME=`cd $'{'KVEM_BIN'}'/.. ; pwd`\nKVEM_LIB=$'{'KVEM_HOME'}'/wtklib\nKVEM_API=$'{'KVEM_HOME'}'/lib\n" + // NOI18N
            "\"$'{'javapathtowtk'}'/java\" -Dkvem.home=\"$'{'KVEM_HOME'}'\" \\\n    -cp \"$'{'KVEM_LIB'}'/kenv.zip:$'{'KVEM_LIB'}'/ktools.zip:$'{'KVEM_BIN'}'/JadTool.jar:" + //NOI18N
            "$'{'KVEM_BIN'}'/MEKeyTool.jar:$'{'KVEM_LIB'}'/customjmf.jar:$'{'KVEM_API'}'/j2me-ws.jar:$'{'KVEM_BIN'}'/schema2beansdev.jar:$'{'KVEM_BIN'}'/j2me_sg_ri.jar:" + //NOI18N
            "$'{'KVEM_BIN'}'/jaxrpc-impl.jar:$'{'KVEM_BIN'}'/jaxrpc-api.jar:$'{'KVEM_BIN'}'/jaxrpc-spi.jar:$'{'KVEM_BIN'}'/activation.jar:$'{'KVEM_BIN'}'/mail.jar:" + //NOI18N
            "$'{'KVEM_BIN'}'/saaj-api.jar:$'{'KVEM_BIN'}'/saaj-impl.jar:$'{'KVEM_BIN'}'/xsdlib.jar\" \\\n    {1} \"$@\" {2}\n"; //NOI18N
    
    private PostInstallJ2meAction() {
        //To avoid instantiation
    }
    
    public static void installAction(final String targetDir) throws IOException {
        if (Utilities.isWindows()) {
            installWindows(targetDir);
        } else if (Utilities.getOperatingSystem() == Utilities.OS_LINUX || Utilities.getOperatingSystem() == Utilities.OS_SOLARIS) {
            installUnix(targetDir);
        }
    }
    
    private static void installWindows(final String targetDir) throws IOException {
        final File folder = new File(targetDir);
        final File binFolder = new File(folder, "bin");  //NOI18N
        
        final String java = quoteString(System.getProperty("java.home")+"\\bin\\java"); //NOI18N
        
        writeFile(binFolder, "emulator.bat", //NOI18N
                EMULATOR_STARTUP_WIN,
                new Object[] {java, quoteString(targetDir), "com.sun.kvem.environment.EmulatorWrapper" } //NOI18N
        );
        
        writeFile(binFolder, "DefaultDevice.bat", //NOI18N
                DEFAULT_STARTUP_WIN,
                new Object[] {java, quoteString(targetDir), "com.sun.kvem.preferences.DefaultDeviceWindow" } //NOI18N
        );
        
        writeFile(binFolder, "prefs.bat", //NOI18N
                DEFAULT_STARTUP_WIN,
                new Object[] {java, quoteString(targetDir), "com.sun.kvem.preferences.Preferences" } //NOI18N
        );
        
        writeFile(binFolder, "utils.bat", //NOI18N
                UTILS_STARTUP_WIN,
                new Object[] {java, quoteString(targetDir), "com.sun.kvem.preferences.Utilities" } //NOI18N
        );
        
        writeFile(binFolder, "mekeytool.bat", //NOI18N
                UTILS_STARTUP_WIN,
                new Object[] {java, quoteString(targetDir), "com.sun.midp.mekeytool.WTKMain" } //NOI18N
        );
        
        writeFile(binFolder, "wscompile.bat", //NOI18N
                WSCOMP_STATIC+WSCOMP_STARTUP_WIN,
                new Object[] {java, quoteString(targetDir), "com.sun.kvem.ktools.WSCompile" } //NOI18N
        );
        
    }
    
    private static void installUnix(final String targetDir) throws IOException {
        final File folder = new File(targetDir);
        final File binFolder = new File(folder, "bin"); //NOI18N
        final String java = System.getProperty("java.home"); //NOI18N
        
        writeFile(binFolder, "emulator", //NOI18N
                EMULATOR_STARTUP_UNIX,
                new Object[] {java, "com.sun.kvem.environment.EmulatorWrapper", " 0" } //NOI18N
        );
        setPermission(binFolder, "emulator"); //NOI18N
        
        writeFile(binFolder, "defaultdevice", //NOI18N
                DEFAULT_STARTUP_UNIX,
                new Object[] {java, "com.sun.kvem.preferences.DefaultDeviceWindow", "" } //NOI18N
        );
        setPermission(binFolder,  "defaultdevice"); //NOI18N
        
        writeFile(binFolder, "prefs", //NOI18N
                DEFAULT_STARTUP_UNIX,
                new Object[] {java, "com.sun.kvem.preferences.Preferences", " 0" } //NOI18N
        );
        setPermission(binFolder,  "prefs"); //NOI18N
        
        writeFile(binFolder, "utils", //NOI18N
                UTILS_STARTUP_UNIX,
                new Object[] {java, "com.sun.kvem.preferences.Utilities", " 0" } //NOI18N
        );
        setPermission(binFolder, "utils"); //NOI18N
        
        writeFile(binFolder, "mekeytool", //NOI18N
                UTILS_STARTUP_UNIX,
                new Object[] {java, "com.sun.midp.mekeytool.WTKMain", " 0" } //NOI18N
        );
        setPermission(binFolder, "mekeytool"); //NOI18N
        
        writeFile(binFolder, "wscompile", //NOI18N
                UTILS_STARTUP_UNIX,
                new Object[] {java, "com.sun.kvem.ktools.WSCompile", "" } //NOI18N
        );
        setPermission(binFolder, "wscompile"); //NOI18N
        
        setPermission(binFolder, "preverify"); //NOI18N
        setPermission(binFolder, "zayit"); //NOI18N
        
        setPermission(binFolder, "ktoolbar"); //NOI18N
        setPermission(binFolder, "preverify1.0"); //NOI18N
        setPermission(binFolder, "preverify1.1"); //NOI18N
        setPermission(binFolder, "runmidlet"); //NOI18N
        
    }
    
    private static void setPermission(final File folder, final String file) throws IOException {
        final File f= new File(folder, file);
        if (f.isFile()) {
            Runtime.getRuntime().exec(new String[] {
                "chmod", // NOI18N
                "+x", // NOI18N,
                f.getCanonicalPath()
            });
        }
    }
    
    private static String quoteString(final String s) {
        if (s.indexOf(' ') != -1) return '\"' + s + '\"';  //NOI18N
        return s;
    }
    
    /** Write file.
     * @param folder folder
     * @param name name of the file
     * @param text text, it can be a format suitable for java.text.MessageFormat
     * @param params additional parameter for MessageFormat
     */
    private static void writeFile(final File folder, final String name, final String text, final Object[] params) throws IOException {
        final File f = new File(folder, name);
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new FileWriter(f));
            final String s = MessageFormat.format(text, params);
            pw.write(s);
        } finally {
            if (pw != null) pw.close();
        }
    }
}
