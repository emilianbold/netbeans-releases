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

package org.netbeans.modules.cnd.makeproject.ui.wizards;

import java.io.File;
import org.netbeans.modules.cnd.actions.AbstractExecutorRunAction;
import org.netbeans.modules.cnd.execution.ShellExecSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;

/**
 *
 * @author Alexander Simon
 */
public final class ConfigureUtils {
    private ConfigureUtils() {
    }

    public static String findConfigureScript(String folder){
        String pattern[] = new String[]{"configure"}; // NOI18N
        File file = new File(folder);
        if (!(file.isDirectory() && file.canRead() && file.canWrite())) {
            return null;
        }
        for (String name : pattern) {
            file = new File(folder+File.separator+name); // NOI18N
            if (isRunnable(file)){
                return file.getAbsolutePath();
            }
        }
        String res = detectQTProject(new File(folder));
        if (res != null) {
            return res;
        }
        res = detectCMake(folder);
        if (res != null) {
            return res;
        }
        return null;
    }

    private static String detectQTProject(File folder){
        for(File file : folder.listFiles()){
            if (file.getAbsolutePath().endsWith(".pro")){ // NOI18N
                if (AbstractExecutorRunAction.findTools("qmake") != null){ // NOI18N
                    return file.getAbsolutePath();
                }
                break;
            }
        }
        return null;
    }

    private static String detectCMake(String path){
        File configure = new File(path+File.separator+"CMakeLists.txt"); // NOI18N
        if (configure.exists()) {
            if (AbstractExecutorRunAction.findTools("cmake") != null) { // NOI18N
                return configure.getAbsolutePath();
            }
        }
        return null;
    }

    public static boolean isRunnable(File file) {
        if (file.exists() && file.isFile() && file.canRead()) {
            FileObject configureFileObject = FileUtil.toFileObject(file);
            if (configureFileObject == null || !configureFileObject.isValid()) {
                return false;
            }
            DataObject dObj;
            try {
                dObj = DataObject.find(configureFileObject);
            } catch (DataObjectNotFoundException ex) {
                return false;
            }
            if (dObj == null) {
                return false;
            }
            Node node = dObj.getNodeDelegate();
            if (node == null) {
                return false;
            }
            ShellExecSupport ses = node.getCookie(ShellExecSupport.class);
            if (ses != null) {
                return true;
            }
            if (file.getAbsolutePath().endsWith("CMakeLists.txt")){ // NOI18N
                return AbstractExecutorRunAction.findTools("cmake") != null; // NOI18N
            }
            if (file.getAbsolutePath().endsWith(".pro")){ // NOI18N
                return AbstractExecutorRunAction.findTools("qmake") != null; // NOI18N
            }
        }
        return false;
    }

    public static String findMakefile(String folder){
        String pattern[] = new String[]{"GNUmakefile", "makefile", "Makefile",}; // NOI18N
        File file = new File(folder);
        if (!(file.isDirectory() && file.canRead() && file.canWrite())) {
            return null;
        }
        for (String name : pattern) {
            file = new File(folder+File.separator+name); // NOI18N
            if (file.exists() && file.isFile() && file.canRead()) {
                return file.getAbsolutePath();
            }
        }
        return null;
    }

    public static String getConfigureArguments(String configure){
        if (configure.endsWith("configure")) { // NOI18N
            return "CFLAGS=\"-g3 -gdwarf-2\" CXXFLAGS=\"-g3 -gdwarf-2\""; // NOI18N
        } else if (configure.endsWith("CMakeLists.txt")) { // NOI18N
            return "-G \"Unix Makefiles\" -DCMAKE_BUILD_TYPE=Debug -DCMAKE_CXX_FLAGS_DEBUG=\"-g3 -gdwarf-2\" -DCMAKE_C_FLAGS_DEBUG=\"-g3 -gdwarf-2\""; // NOI18N
        } else if (configure.endsWith(".pro")) { // NOI18N
            return "QMAKE_CFLAGS=\"-g3 -gdwarf-2\" QMAKE_CXXFLAGS=\"-g3 -gdwarf-2\""; // NOI18N
        }
        return ""; // NOI18N
    }

}
