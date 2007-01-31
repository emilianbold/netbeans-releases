/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.makeproject.api.platforms;

import org.netbeans.modules.cnd.makeproject.api.compilers.CompilerSet;
import org.netbeans.modules.cnd.makeproject.api.configurations.LibraryItem;

public class PlatformSolaris  extends Platform {
    public static final LibraryItem.StdLibItem[] standardLibrariesSolaris = {
	new LibraryItem.StdLibItem("Motif", "Motif", new String[] {"Xm", "Xt", "Xext", "X11"}), // NOI18N
	new LibraryItem.StdLibItem("Mathematics", "Mathematics", new String[] {"m"}), // NOI18N
	new LibraryItem.StdLibItem("Yacc", "Yacc", new String[] {"y"}), // NOI18N
	new LibraryItem.StdLibItem("Lex", "Lex", new String[] {"l"}), // NOI18N
	new LibraryItem.StdLibItem("SocketsNetworkServices", "Sockets and Network Services Library", new String[] {"socket", "nsl"}), // NOI18N
	new LibraryItem.StdLibItem("SolarisThreads", "Solaris Threads", new String[] {"thread"}), // NOI18N
	new LibraryItem.StdLibItem("PosixThreads", "Posix Threads", new String[] {"pthread"}), // NOI18N
	new LibraryItem.StdLibItem("Posix4", "Posix 4", new String[] {"possix4"}), // NOI18N
	new LibraryItem.StdLibItem("Internationalization", "Internationalization", new String[] {"intl"}), // NOI18N
	new LibraryItem.StdLibItem("PatternMatching", "Pattern Matching and Pathname Manipulation", new String[] {"gen"}), // NOI18N
	new LibraryItem.StdLibItem("Curses", "Curses: CRT Screen Handling", new String[] {"curses"}), // NOI18N
    };
    
    public PlatformSolaris(String name, String displayName, int id) {
        super(name, displayName, id);
    }
    
    public LibraryItem.StdLibItem[] getStandardLibraries() {
        return standardLibrariesSolaris;
    }
    
    public String getLibraryName(String baseName) {
        return "lib" + baseName + ".so"; // NOI18N
    }
    
    public String getLibraryLinkOption(String libName, String libDir, String libPath, CompilerSet compilerSet) {
        if (libName.endsWith(".so")) { // NOI18N
            int i = libName.indexOf(".so"); // NOI18N
            if (i > 0)
                libName = libName.substring(0, i);
            if (libName.startsWith("lib")) // NOI18N
                libName = libName.substring(3);
            return compilerSet.getDynamicLibrarySearchOption() + libDir + " " + compilerSet.getLibrarySearchOption() + libDir + " " + compilerSet.getLibraryOption() + libName; // NOI18N
        } else {
            return libPath;
        }
    }
}
