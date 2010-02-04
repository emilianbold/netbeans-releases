/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.toolchain.api;

import org.netbeans.modules.cnd.toolchain.api.ToolchainManager.ToolDescriptor;
import org.netbeans.modules.cnd.toolchain.compilers.impl.APIAccessor;
import org.netbeans.modules.cnd.toolchain.compilers.impl.ToolUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.util.Utilities;

/**
 *
 * @author Alexander Simon
 */
public class Tool {

    static {
        APIAccessor.register(new APIAccessorImpl());
    }
    
    private final ExecutionEnvironment executionEnvironment;
    private final CompilerFlavor flavor;
    private final ToolKind kind;
    private String name;
    private final String displayName;
    private String path;
    private CompilerSet compilerSet;

    /** Creates a new instance of GenericCompiler */
    protected Tool(ExecutionEnvironment executionEnvironment, CompilerFlavor flavor, ToolKind kind, String name, String displayName, String path) {
        this.executionEnvironment = executionEnvironment;
        this.flavor = flavor;
        this.kind = kind;
        this.name = name;
        this.displayName = displayName;
        this.path = path;
    }

    public ToolDescriptor getDescriptor() {
        return null;
    }

    public Tool createCopy() {
        return new Tool(executionEnvironment, flavor, kind, name, displayName, path);
    }

    public final ExecutionEnvironment getExecutionEnvironment() {
        return executionEnvironment;
    }

    public final CompilerFlavor getFlavor() {
        return flavor;
    }

    /**
     * Some tools may require long initialization
     * (e.g. compiler tools call compilers to get include search path, etc).
     *
     * Such initialization is usually moved out of constructors.
     * This methods allows to check whether the tool was initialized or not.
     *
     * @return true in the case this tool is ready, otherwise false
     */
    public boolean isReady() {
        return true;
    }

    /**
     * Some tools may require long initialization
     * (e.g. compiler tools call compilers to get include search path, etc).
     * This method should
     * - check whether the tool is initialized
     * - if it is not, start initialization
     * - wait until it is done
     * NB: Should never be called from AWT thread
     * @param reset pass true if expect getting fresh
     */
    public void waitReady(boolean reset) {
    }

    public final ToolKind getKind() {
        return kind;
    }

    public final String getName() {
        return name;
    }

    public final String getPath() {
        return path;
    }

    public final String getDisplayName() {
        return displayName;
    }

    public String getIncludeFilePathPrefix() {
        // TODO: someone put this here only because OutputWindowWriter in core
        // wants to get information about compilers which are defined in makeprojects.
        // abstract Tool shouldn't care about include paths for compilers
        throw new UnsupportedOperationException();
    }

    public final CompilerSet getCompilerSet() {
        return compilerSet;
    }

    @Override
    public String toString() {
        String n = getName();
        if (Utilities.isWindows() && n.endsWith(".exe")) { // NOI18N
            return n.substring(0, n.length() - 4);
        } else {
            return n;
        }
    }

    private void setPath(String p) {
        if (p != null) {
            path = p;
            name = ToolUtils.getBaseName(path);
        }
    }

    private void setCompilerSet(CompilerSet compilerSet) {
        this.compilerSet = compilerSet;
    }

    private static Tool createTool(ExecutionEnvironment executionEnvironment, CompilerFlavor flavor, ToolKind kind, String name, String displayName, String path) {
        return new Tool(executionEnvironment, flavor, kind, name, displayName, path);
    }

    private static final class APIAccessorImpl extends APIAccessor {

        @Override
        public Tool createTool(ExecutionEnvironment env, CompilerFlavor flavor, ToolKind kind, String name, String displayName, String path) {
            return Tool.createTool(env, flavor, kind, name, displayName, path);
        }

        @Override
        public void setCompilerSet(Tool tool, CompilerSet cs) {
            tool.setCompilerSet(cs);
        }

        @Override
        public void setToolPath(Tool tool, String p) {
            tool.setPath(p);
        }
    }
}
