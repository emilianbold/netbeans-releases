/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.cnd.makeproject.api.compilers;

import java.io.File;
import java.util.List;
import java.util.Vector;
import org.netbeans.modules.cnd.api.compilers.CompilerSet.CompilerFlavor;
import org.netbeans.modules.cnd.api.compilers.Tool;
import org.netbeans.modules.cnd.api.compilers.ToolchainManager.CompilerDescriptor;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;

public abstract class BasicCompiler extends Tool {

    /** Creates a new instance of GenericCompiler */
    protected BasicCompiler(ExecutionEnvironment env, CompilerFlavor flavor, int kind, String name, String displayName, String path) {
        super(env, flavor, kind, name, displayName, path);
        if (!env.isLocal()) {
            includeFilePrefix = getIncludeFilePrefix(env);
        } else {
            includeFilePrefix = null;
        }
    }
    private String includeFilePrefix;

    // FIXUP: still a fixup. Think over, who is responsible for this
    public static String getIncludeFilePrefix(ExecutionEnvironment env) {
        return System.getProperty("netbeans.user") + "/var/cache/cnd2/includes-cache/" + env.getHost() + "/"; //NOI18N
    }

    // FIXUP: still a fixup. Think over, who is responsible for this
    public static String getIncludeFileBase() {
        return System.getProperty("netbeans.user") + "/var/cache/cnd2/includes-cache/"; //NOI18N
    }

    @Override
    public String getIncludeFilePathPrefix() {
        if (includeFilePrefix == null) {
            if (getExecutionEnvironment().isLocal()) {
                includeFilePrefix = ""; // NOI18N
                CompilerDescriptor c = getDescriptor();
                if (c != null) {
                    String path = getPath().replaceAll("\\\\", "/"); // NOI18N
                    if (c.getRemoveIncludePathPrefix() != null) {
                        int i = path.toLowerCase().indexOf("/bin"); // NOI18N
                        if (i > 0) {
                            includeFilePrefix = path.substring(0, i);
                        }
                    }
                }
            }
        }
        return includeFilePrefix;
    }

    @Override
    public abstract CompilerDescriptor getDescriptor();

    public String getDevelopmentModeOptions(int value) {
        CompilerDescriptor compiler = getDescriptor();
        if (compiler != null && compiler.getDevelopmentModeFlags() != null && compiler.getDevelopmentModeFlags().length > value){
            return compiler.getDevelopmentModeFlags()[value];
        }
        return ""; // NOI18N
    }

    public String getWarningLevelOptions(int value) {
        CompilerDescriptor compiler = getDescriptor();
        if (compiler != null && compiler.getWarningLevelFlags() != null && compiler.getWarningLevelFlags().length > value){
            return compiler.getWarningLevelFlags()[value];
        }
        return ""; // NOI18N
    }

    public String getSixtyfourBitsOption(int value) {
        CompilerDescriptor compiler = getDescriptor();
        if (compiler != null && compiler.getArchitectureFlags() != null && compiler.getArchitectureFlags().length > value){
            return compiler.getArchitectureFlags()[value];
        }
        return ""; // NOI18N
    }

    public String getStripOption(boolean value) {
        CompilerDescriptor compiler = getDescriptor();
        if (compiler != null && value){
            return compiler.getStripFlag();
        }
        return ""; // NOI18N
    }

    public String getDependencyGenerationOption() {
        CompilerDescriptor compiler = getDescriptor();
        if (compiler != null && compiler.getDependencyGenerationFlags() != null) {
            return compiler.getDependencyGenerationFlags();
        }
        return ""; // NOI18N
    }

    public List<String> getSystemPreprocessorSymbols() {
        return new Vector<String>();
    }

    public List<String> getSystemIncludeDirectories() {
        return new Vector<String>();
    }

    /**
     * @return true if settings were really replaced by new one
     */
    public boolean setSystemPreprocessorSymbols(List<String> values) {
        return false;
    }

    /**
     * @return true if settings were really replaced by new one
     */
    public boolean setSystemIncludeDirectories(List<String> values) {
        return false;
    }

    protected void normalizePaths(List<String> paths) {
        for (int i = 0; i < paths.size(); i++) {
            paths.set(i, normalizePath(paths.get(i)));
        }
    }

    protected String normalizePath(String path) {
        if (getExecutionEnvironment().isLocal()) {
            return CndFileUtils.normalizeAbsolutePath(new File(path).getAbsolutePath());
        } else {
            // TODO: remote paths would love to be normalized too
            return path;
        }
    }

    protected String applyPathPrefix(String path) {
        String prefix = getIncludeFilePathPrefix();
        return normalizePath( prefix != null ? prefix + path : path );
    }
}