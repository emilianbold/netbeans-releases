/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009-2010 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.glassfish.common;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.glassfish.spi.Utils;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author vkraemer
 */
public final class Util {

    private Util() {
    }

    public static final String GF_LOOKUP_PATH = "Servers/GlassFish"; // NOI18N
    
    private static String INDICATOR = File.separatorChar == '/' ? "jrunscript" : "jrunscript.exe";
    private static FilenameFilter JDK6_DETECTION_FILTER = new FilenameFilter() {
        @Override
            public boolean accept(File arg0, String arg1) {
                if (arg1.equalsIgnoreCase(INDICATOR)) {
                    return true;
                }
                return false;
            }
    };

    public static boolean appearsToBeJdk6OrBetter(File javaExecutable) {
        File dir = javaExecutable.getParentFile();
        if (null != dir) {
            String[] hits = dir.list(Util.JDK6_DETECTION_FILTER);
            if (null != hits) {
                return hits.length > 0;
            }
        }
        return false;
    }


    /**
     * Add quotes to string if and only if it contains space characters.
     *
     * Note: does not handle generalized white space (tabs, localized white
     * space, etc.)
     *
     * @param path file path in string form.
     * @return quote path if it contains any space characters, otherwise same.
     */
    public static final String quote(String path) {
        return path.indexOf(' ') == -1 ? path : "\"" + path + "\""; // NOI18N
    }

    /**
     * Add escape characters for backslash and dollar sign characters in
     * path field.
     *
     * @param path file path in string form.
     * @return adjusted path with backslashes and dollar signs escaped with
     *   backslash character.
     * @deprecated use spi.Utils.escapePath(String)
     */
    @Deprecated
    public static final String escapePath(String path) {
        return Utils.escapePath(path);
    }

    /**
     * Convert classpath fragment using standard separator to a list of
     * normalized files (nonexistent jars will be removed).
     *
     * @param cp classpath string
     * @param root root folder for expanding relative path names
     * @return list of existing jars, normalized
     */
    public static final  List<File> classPathToFileList(String cp, File root) {
        List<File> result = new ArrayList<File>();
        if(cp != null && cp.length() > 0) {
            String [] jars = cp.split(File.pathSeparator);
            for(String jar: jars) {
                File jarFile = new File(jar);
                if(!jarFile.isAbsolute() && root != null) {
                    jarFile = new File(root, jar);
                }
                if(jarFile.exists()) {
                    result.add(FileUtil.normalizeFile(jarFile));
                }
            }
        }
        return result;
    }
    
}
