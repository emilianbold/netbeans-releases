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
package org.netbeans.modules.dlight.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;

public class Util {

    public static String getFullPath(String relpath) {
        File file = InstalledFileLocator.getDefault().locate(relpath, null, false);
        if (file != null && file.exists()) {
            return file.getAbsolutePath();
        }
        return relpath;
    }

    public static String copyResource(Class clazz, String resourceFileName) {
        try {
            InputStream is = clazz.getClassLoader().getResourceAsStream(resourceFileName);
            if (is == null) {
                return null;
            }
            String prefix = "_dlight_" + getBriefName(resourceFileName);
            File result_file = File.createTempFile(prefix, ".d");
            OutputStream os = new FileOutputStream(result_file);
            FileUtil.copy(is, os);
            is.close();
            os.flush();
            os.close();
            return result_file.getCanonicalPath();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;

    }

    private static String getBriefName(String resourceFileName) {
        int pos = resourceFileName.lastIndexOf('.');
        String result = (pos > 0) ? resourceFileName.substring(0, pos) : resourceFileName;
        pos = resourceFileName.lastIndexOf('/');
        result = (pos >= 0) ? resourceFileName.substring(pos + 1) : resourceFileName;
        return result;
    }

    public static void setExecutionPermissions(final List<String> files) {
        if (files.isEmpty()) {
            return;
        }

        List<String> paths = new ArrayList<String>();
        for (String f : files) {
            String fullPath = f;
            if (!(new File(f)).exists()) {
                fullPath = getFullPath(f);
            }

            if (new File(fullPath).exists()) {
                paths.add(fullPath);
            }
        }

        List<String> commands = new ArrayList<String>();
        commands.add("/bin/chmod"); // NOI18N
        commands.add("755"); // NOI18N
        commands.addAll(paths);
        ProcessBuilder pb = new ProcessBuilder(commands);
        try {
            pb.start();
        } catch (IOException ex) {
            ex.printStackTrace();
        //Gizmo.err.log("Cannot set execution permissions of files! " + ex.getMessage()); // NOI18N
        }
    }

    /** gets a base path for a class: org.nebeabs.modules.dlight.MyClass -> org/nebeabs/modules/dlight */
    public static String getBasePath(Class cls) {
        String path = cls.getName().replace('.', '/');
        int pos = path.lastIndexOf('/');
        return (pos > 0) ? path.substring(0, pos) : path;
    }

    public static boolean getBoolean(String name, boolean defaultValue) {
        String value = System.getProperty(name);
        return (value == null) ? defaultValue : Boolean.parseBoolean(value);
    }

    public static boolean deleteLocalDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteLocalDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return (path.delete());
    }
}
