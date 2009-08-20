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
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.groovy.grails;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

/**
 *
 * @author Petr Hejl
 */
public final class RuntimeHelper {

    public static final String GRAILS_HOME_PROPERTY = "GRAILS_HOME"; // NOI18N

    public static final String WIN_EXECUTABLE_FILE = "grails.bat"; // NOI18N

    public static final String WIN_EXECUTABLE = "\\bin\\" + WIN_EXECUTABLE_FILE; // NOI18N

    public static final String WIN_DEBUG_EXECUTABLE = "\\bin\\grails-debug.bat"; // NOI18N

    public static final String NIX_EXECUTABLE_FILE = "grails"; // NOI18N

    public static final String NIX_EXECUTABLE = "/bin/" + NIX_EXECUTABLE_FILE; // NOI18N

    public static final String NIX_DEBUG_EXECUTABLE = "/bin/grails-debug"; // NOI18N

    public static final String DEB_EXECUTABLE = "/usr/bin/" + NIX_EXECUTABLE_FILE; // NOI18N

    public static final String DEB_DEBUG_EXECUTABLE = "/usr/bin/grails-debug"; // NOI18N

    public static final String DEB_START_FILE = "/bin/startGrails"; // NOI18N

    public static final String DEB_LOCATION = "/usr/share/grails"; // NOI18N

    private static final Logger LOGGER = Logger.getLogger(RuntimeHelper.class.getName());

    private RuntimeHelper() {
        super();
    }

    public static boolean isValidRuntime(File grailsBase) {
        String pathToBinary = Utilities.isWindows() ? WIN_EXECUTABLE : NIX_EXECUTABLE;
        return new File(grailsBase, pathToBinary).isFile() || isDebian(grailsBase);
// leave following for future
//                && new File(grailsBase, "dist").isDirectory() // NOI18N
//                && new File(grailsBase, "lib").isDirectory() // NOI18N
//                && new File(grailsBase, "build.properties").isFile(); // NOI18N
    }

    public static boolean isDebian(File grailsBase) {
        return new File(grailsBase, DEB_START_FILE).isFile()
                && new File(DEB_EXECUTABLE).isFile()
                && FileUtil.normalizeFile(grailsBase).equals(new File(DEB_LOCATION));
    }

    public static File getGrailsExecutable(File grailsBase, boolean debug) {
        assert grailsBase != null;
        if (grailsBase == null) {
            return null;
        }

        File grailsExecutable = null;
        if (debug) {
            if (Utilities.isWindows()) {
              grailsExecutable = new File(grailsBase, RuntimeHelper.WIN_DEBUG_EXECUTABLE);
            } else {
                if (RuntimeHelper.isDebian(grailsBase)) {
                    grailsExecutable = new File(RuntimeHelper.DEB_DEBUG_EXECUTABLE);
                } else {
                    grailsExecutable = new File(grailsBase, RuntimeHelper.NIX_DEBUG_EXECUTABLE);
                }
            }
        } else {
            if (Utilities.isWindows()) {
              grailsExecutable = new File(grailsBase, RuntimeHelper.WIN_EXECUTABLE);
            } else {
                if (RuntimeHelper.isDebian(grailsBase)) {
                    grailsExecutable = new File(RuntimeHelper.DEB_EXECUTABLE);
                } else {
                    grailsExecutable = new File(grailsBase, RuntimeHelper.NIX_EXECUTABLE);
                }
            }
        }
        return grailsExecutable;
    }

    public static String getRuntimeVersion(File grailsBase) {
        if (!isValidRuntime(grailsBase)) {
            return null;
        }
        Properties props = new Properties();
        try {
            BufferedInputStream is = new BufferedInputStream(new FileInputStream(
                    new File(grailsBase, "build.properties"))); // NOI18N
            try {
                props.load(is);
            } finally {
                try {
                    is.close();
                } catch (IOException ex) {
                    LOGGER.log(Level.INFO, null, ex);
                }
            }
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, null, ex);
            return null;
        }

        return props.getProperty("grails.version"); // NOI18N
    }

}
