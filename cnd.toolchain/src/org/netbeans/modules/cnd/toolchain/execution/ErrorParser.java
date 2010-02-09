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

package org.netbeans.modules.cnd.toolchain.execution;

import java.io.File;
import java.util.List;
import java.util.StringTokenizer;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.api.remote.HostInfoProvider;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.api.toolchain.Tool;
import org.netbeans.modules.cnd.toolchain.compilerset.ToolUtils;
import org.netbeans.modules.cnd.spi.toolchain.ErrorParserProvider;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

public abstract class ErrorParser implements ErrorParserProvider.ErrorParser {

    protected FileObject relativeTo;
    protected final ExecutionEnvironment execEnv;

    public ErrorParser(ExecutionEnvironment execEnv, FileObject relativeTo) {
        super();
        this.relativeTo = relativeTo;
        this.execEnv = execEnv;
    }

    protected FileObject resolveFile(String fileName) {
        if (Utilities.isWindows()) {
            //replace /cygdrive/<something> prefix with <something>:/ prefix:
            if (fileName.startsWith("/cygdrive/")) { // NOI18N
                fileName = fileName.substring("/cygdrive/".length()); // NOI18N
                fileName = "" + fileName.charAt(0) + ':' + fileName.substring(1);
            } else if (fileName.length() > 3 && fileName.charAt(0) == '/' && fileName.charAt(2) == '/') { // NOI18N
                // NOI18N
                fileName = "" + fileName.charAt(1) + ':' + fileName.substring(2); // NOI18N
            }
            if (fileName.startsWith("/") || fileName.startsWith(".")) { // NOI18N
                // NOI18N
                return null;
            }
            fileName = fileName.replace('/', '\\'); // NOI18N
        }
        fileName = HostInfoProvider.getMapper(execEnv).getLocalPath(fileName, true);
        File file = CndFileUtils.normalizeFile(new File(fileName));
        return FileUtil.toFileObject(file);
    }

    protected FileObject resolveRelativePath(FileObject relativeDir, String relativePath) {
        if (ToolUtils.isPathAbsolute(relativePath)) {
            // NOI18N
            if (execEnv.isRemote() || Utilities.isWindows()) {
                // See IZ 106841 for details.
                // On Windows the file path for system header files comes in as /usr/lib/abc/def.h
                // but the real path is something like D:/cygwin/lib/abc/def.h (for Cygwin installed
                // on D: drive). We need the exact compiler that produced this output to safely
                // convert the path but the compiler has been lost at this point. To work-around this problem
                // iterate over all defined compiler sets and test whether the file existst in a set.
                // If it does, convert it to a FileObject and return it.
                // FIXUP: pass exact compiler used to this method (would require API changes we
                // don't want to do now). Error/warning regular expressions should also be moved into
                // the compiler(set) and the output should only be scanned for those patterns.
                String absPath1 = relativePath;
                String absPath2 = null;
                if (absPath1.startsWith("/usr/lib")) { // NOI18N
                    absPath2 = absPath1.substring(4);
                }
                List<CompilerSet> compilerSets = CompilerSetManager.get(execEnv).getCompilerSets();
                for (CompilerSet set : compilerSets) {
                    Tool cCompiler = set.getTool(PredefinedToolKind.CCompiler);
                    if (cCompiler != null) {
                        String includePrefix = cCompiler.getIncludeFilePathPrefix();
                        File file = new File(includePrefix + absPath1);
                        if (!CndFileUtils.exists(file) && absPath2 != null) {
                            file = new File(includePrefix + absPath2);
                        }
                        if (CndFileUtils.exists(file)) {
                            FileObject fo = FileUtil.toFileObject(CndFileUtils.normalizeFile(file));
                            return fo;
                        }
                    }
                }
            }
            FileObject myObj = resolveFile(relativePath);
            if (myObj != null) {
                return myObj;
            }
            if (relativePath.startsWith(File.separator)) {
                // NOI18N
                relativePath = relativePath.substring(1);
            }
            try {
                FileSystem fs = relativeDir.getFileSystem();
                myObj = fs.findResource(relativePath);
                if (myObj != null) {
                    return myObj;
                }
                myObj = fs.getRoot();
                if (myObj != null) {
                    relativeDir = myObj;
                }
            } catch (FileStateInvalidException ex) {
            }
        }
        FileObject myObj = relativeDir;
        String delims = Utilities.isWindows() ? File.separator + '/' : File.separator; // NOI18N

        // NOI18N
        StringTokenizer st = new StringTokenizer(relativePath, delims);
        while ((myObj != null) && st.hasMoreTokens()) {
            String nameExt = st.nextToken();
            if ("..".equals(nameExt)) { // NOI18N
                myObj = myObj.getParent();
            } else if (".".equals(nameExt)) { // NOI18N
            } else {
                myObj = myObj.getFileObject(nameExt, null);
            }
        }
        return myObj;
    }
}
