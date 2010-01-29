/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

import org.netbeans.modules.cnd.toolchain.api.Tool;
import org.netbeans.modules.cnd.toolchain.api.CompilerProvider;
import org.netbeans.modules.cnd.toolchain.api.CompilerFlavor;
import org.netbeans.modules.cnd.toolchain.api.ToolKind;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;

/**
 * Override the cnd default compiler type "Tool". MakeProjects uses classes derived from Tool but cnd/core
 * can't depend on makeproject classes. So this allows makeproject to provide a tool creator factory.
 *
 * @author gordonp
 */
@org.openide.util.lookup.ServiceProvider(service = org.netbeans.modules.cnd.toolchain.api.CompilerProvider.class, position=1000)
public class MakeProjectCompilerProvider extends CompilerProvider {

    /**
     * Create a class derived from Tool
     *
     * Thomas: If you want/need different informatio to choose which Tool derived class to create we can change
     * this method. We can also add others, if desired. This was mainly a proof-of-concept that tool creation
     * could be deferred to makeproject.
     */
    public Tool createCompiler(ExecutionEnvironment env, CompilerFlavor flavor, int kind, String name, String displayName, String path) {
        if (flavor.isSunStudioCompiler()) {
            if (kind == ToolKind.CCompiler.ordinal()) {
                return SunCCompiler.create(env, flavor, kind, name, displayName, path);
            } else if (kind == ToolKind.CCCompiler.ordinal()) {
                return SunCCCompiler.create(env, flavor, kind, name, displayName, path);
            } else if (kind == ToolKind.FortranCompiler.ordinal()) {
                return SunFortranCompiler.create(env, flavor, kind, name, displayName, path);
            } else if (kind == ToolKind.MakeTool.ordinal()) {
                return SunMaketool.create(env, flavor, name, displayName, path);
            } else if (kind == ToolKind.DebuggerTool.ordinal()) {
                return SunDebuggerTool.create(env, flavor, name, displayName, path);
            } else if (kind == ToolKind.Assembler.ordinal()) {
                return Assembler.create(env, flavor, kind, name, displayName, path);
            }
        } else /* if (flavor.isGnuCompiler()) */ { // Assume GNU (makeproject system doesn't handle Unknown)
           if (kind == ToolKind.CCompiler.ordinal()) {
               if ("MSVC".equals(flavor.toString())) { // NOI18N
                   return MsvcCompiler.create(env, flavor, kind, name, displayName, path);
               } else {
                   return GNUCCompiler.create(env, flavor, kind, name, displayName, path);
               }
           } else if (kind == ToolKind.CCCompiler.ordinal()) {
               if ("MSVC".equals(flavor.toString())) { // NOI18N
                   return new MsvcCompiler(env, flavor, kind, name, displayName, path);
               } else {
                   return GNUCCCompiler.create(env, flavor, kind, name, displayName, path);
               }
            } else if (kind == ToolKind.FortranCompiler.ordinal()) {
                return GNUFortranCompiler.create(env, flavor, kind, name, displayName, path);
            } else if (kind == ToolKind.MakeTool.ordinal()) {
                return GNUMaketool.create(env, flavor, name, displayName, path);
            } else if (kind == ToolKind.DebuggerTool.ordinal()) {
                return GNUDebuggerTool.create(env, flavor, name, displayName, path);
            } else if (kind == ToolKind.Assembler.ordinal()) {
                return Assembler.create(env, flavor, kind, name, displayName, path);
            }
        }
        if (kind == ToolKind.CustomTool.ordinal()) {
            return CustomTool.create(env);
        } else if (kind == ToolKind.QMakeTool.ordinal() || kind == ToolKind.CMakeTool.ordinal()) {
            return GeneralTool.create(env, kind, flavor, name, displayName, path);
        }
        return null;
    }
}
