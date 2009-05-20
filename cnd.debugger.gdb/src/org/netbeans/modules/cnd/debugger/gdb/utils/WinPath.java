/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.debugger.gdb.utils;

/**
 * Utilities to work with paths on Windows (cygwin, mingw)
 * @author Egor Ushakov
 */
public class WinPath {
    public static final String CYGDRIVE_PREFIX = "/cygdrive/"; // NOI18N

    private WinPath() {
    }

    /**
     * Converts path from cygwin type into regular window (/cygdrive/c/... -> c:\...)
     * @param path
     * @return
     */
    public static String cyg2win(String path) {
        if (path.startsWith(CYGDRIVE_PREFIX)) {
            return path.charAt(CYGDRIVE_PREFIX.length())
                    + ":" // NOI18N
                    + path.substring(CYGDRIVE_PREFIX.length()+1).replace('/', '\\');
        }
        return path;
    }

    /**
     * * Converts path from regular windows type into cygwin type (c:\... -> /cygdrive/c/...)
     * @param path
     * @return
     */
    public static String win2cyg(String path) {
        if (isWinPath(path)) {
            return CYGDRIVE_PREFIX + path.charAt(0) + path.substring(2).replace('\\', '/'); // NOI18N
        }
        return path;
    }

    /**
     * Converts path from mingw type into regular window (/c/... -> c:\...)
     * @param path
     * @return
     */
    public static String ming2win(String path) {
        if (path.charAt(0) == '/' && path.charAt(2) == '/') {
            return path.charAt(1) + ":" + path.substring(2).replace('/', '\\'); // NOI18N
        }
        return path;
    }

    /**
      * Converts path from regular windows type into mingw type (c:\... -> /c/...)
     * @param path
     * @return
     */
    public static String win2ming(String path) {
        if (isWinPath(path)) {
            return "/" + path.charAt(0) + "/" + path.substring(2).replace('\\', '/'); // NOI18N
        }
        return path;
    }

    public static boolean isWinPath(String path) {
        return path.length() > 2 && Character.isLetter(path.charAt(0)) && path.charAt(1) == ':';
    }
}
