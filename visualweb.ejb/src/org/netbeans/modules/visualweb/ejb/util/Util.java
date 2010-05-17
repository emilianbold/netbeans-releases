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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
/*
 * Util.java
 *
 * Created on June 9, 2004, 2:56 PM
 */

package org.netbeans.modules.visualweb.ejb.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.logging.Logger;

import org.openide.filesystems.FileUtil;

/**
 * This class contains some utility methods
 * 
 * @author cao
 */
public class Util {

    /** Logger for visualweb EJB module */
    private static final Logger logger;

    /** Directory in userdir where this modules state is kept */
    private static File ejbStateDir;

    static {
        String utilPkg = Util.class.getPackage().getName();
        int lastIndex = utilPkg.lastIndexOf(".");
        String codeNameBase = utilPkg.substring(0, lastIndex);
        logger = Logger.getLogger(codeNameBase);
    }

    /**
     * Get the file name out of the path
     */
    public static String getFileName(String path) {
        return new File(path).getName();
    }

    public static String getClassName(String fullPackageClassName) {
        int i = fullPackageClassName.lastIndexOf(".");
        String beanClassName = fullPackageClassName.substring(i + 1);

        return beanClassName;
    }

    public static String getPackageName(String fullPackageClassName) {
        int i = fullPackageClassName.lastIndexOf(".");

        if (i == -1) // No package
            i = 0;

        String packageName = fullPackageClassName.substring(0, i);

        return packageName;
    }

    /*
     * Utility routine to paper over array type names
     */
    public static String getTypeName(Class type) {
        if (type.isArray()) {
            try {
                Class cl = type;
                int dimensions = 0;
                while (cl.isArray()) {
                    dimensions++;
                    cl = cl.getComponentType();
                }
                StringBuffer sb = new StringBuffer();
                sb.append(cl.getName());
                for (int i = 0; i < dimensions; i++) {
                    sb.append("[]");
                }
                return sb.toString();
            } catch (Throwable e) { /* FALLTHRU */
            }
        }
        return type.getName();
    }

    public static String capitalize(String name) {
        if (name == null || name.length() == 0) {
            return name;
        }

        char chars[] = name.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        return new String(chars);
    }

    public static String decapitalize(String name) {
        if (name == null || name.length() == 0) {
            return name;
        }

        char chars[] = name.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        return new String(chars);
    }

    public static boolean isAcronyn(String name) {
        // The name is consider as acronyn if it starts with two or more upper-case letters in a row

        if (name == null || name.length() == 0) {
            return false;
        }

        char chars[] = name.toCharArray();
        if (Character.isUpperCase(chars[0]) && Character.isUpperCase(chars[1]))
            return true;
        else
            return false;
    }

    public static Logger getLogger() {
        return logger;
    }

    /**
     * Returns the NetBeans module code name base.
     * 
     * @return
     */
    public static String getCodeNameBase() {
        // Code name base should be same as the package name of this class minus the last component
        String pkgName = Util.class.getPackage().getName();
        return pkgName.substring(0, pkgName.lastIndexOf('.'));
    }

    /**
     * Get the directory where state of this module is kept. This will be in the userdir and also
     * part of the NetBeans filesystem so that it will make it easier to migrate to future IDE
     * versions.
     * 
     * @return
     * @throws IOException
     */
    public static File getEjbStateDir() {
        if (ejbStateDir == null) {
            File root = FileUtil.toFile(FileUtil.getConfigRoot());
            // Follow NetBeans convention of replacing dots with dashes
            String stateDirName = getCodeNameBase().replace('.', '-');
            ejbStateDir = new File(root, stateDirName);
            if (!ejbStateDir.exists() && !ejbStateDir.mkdirs()) {
                IllegalStateException ex = new IllegalStateException("Unable to create dir: "
                        + ejbStateDir);
                throw ex;
            }
        }
        return ejbStateDir;
    }

    public static void copyFileRecursive(File source, File dest) throws IOException {
        File newItem = null;
        if (dest.isDirectory()) {
            newItem = new File(dest, source.getName());
        } else {
            newItem = dest;
        }

        if (source.isDirectory()) {
            newItem.mkdir();
            File[] contents = source.listFiles();

            for (int i = 0; i < contents.length; i++) {
                copyFileRecursive(contents[i], newItem);
            }
        } else {
            copyFile(source, newItem);
        }
    }

    /**
     * Copy files using nio
     * 
     * @param src
     *            source file
     * @param dst
     *            destination file
     * @throws IOException
     */
    public static void copyFile(File src, File dst) throws IOException {
        FileChannel srcChannel = new FileInputStream(src).getChannel();
        FileChannel dstChannel = new FileOutputStream(dst).getChannel();
        dstChannel.transferFrom(srcChannel, 0, srcChannel.size());
        srcChannel.close();
        dstChannel.close();
    }
}
