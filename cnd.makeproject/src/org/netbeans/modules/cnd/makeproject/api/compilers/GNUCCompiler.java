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

import org.netbeans.modules.cnd.api.compilers.CompilerSet.CompilerFlavor;
import org.netbeans.modules.cnd.makeproject.api.configurations.BasicCompilerConfiguration;

public class GNUCCompiler extends GNUCCCCompiler {

    private static final String[] DEVELOPMENT_MODE_OPTIONS = {
        "",  // Fast Build // NOI18N
        "-g", // Debug" // NOI18N
        "-g -O", // Performance Debug" // NOI18N
        "-g", // Test Coverage // NOI18N
        "-g -O2", // Dianosable Release // NOI18N
        "-O2", // Release // NOI18N
        "-O3", // Performance Release // NOI18N
    };
    
    private static final String[] WARNING_LEVEL_OPTIONS = {
        "-w", // No Warnings // NOI18N
        "", // Default // NOI18N
        "-Wall", // More Warnings // NOI18N
        "-Werror", // Convert Warnings to Errors // NOI18N
    }; // FIXUP: from Bundle
    
    @Override
    public String getDevelopmentModeOptions(int value) {
        return DEVELOPMENT_MODE_OPTIONS[value];
    }
    
    /** Creates a new instance of GNUCCompiler */
    public GNUCCompiler(CompilerFlavor flavor, int kind, String name, String displayName, String path) {
        super(flavor, kind, name, displayName, path);
    }
    
    @Override
    public GNUCCompiler createCopy() {
        GNUCCompiler copy = new GNUCCompiler(getFlavor(), getKind(), "", getDisplayName(), getPath());
        copy.setName(getName());
        return copy;
    }
    
    @Override
    public String getWarningLevelOptions(int value) {
        if (value < WARNING_LEVEL_OPTIONS.length)
            return WARNING_LEVEL_OPTIONS[value];
        else
            return ""; // NOI18N
    }
    
    @Override
    public String getSixtyfourBitsOption(int value) {
        if (value == BasicCompilerConfiguration.BITS_DEFAULT)
            return ""; // NOI18N
        else if (value == BasicCompilerConfiguration.BITS_32)
            return "-m32"; // NOI18N
        else if (value == BasicCompilerConfiguration.BITS_64)
            return "-m64"; // NOI18N
        else
            return ""; // NOI18N
    }
    
    @Override
    public String getStripOption(boolean value) {
        return value ? "-s" : ""; // NOI18N
    }

    @Override
    protected String getDefaultPath() {
        return "gcc"; // NOI18N
    }
    
    @Override
    protected String getCompilerStderrCommand() {
        return " -x c -v -E"; // NOI18N
    }

    @Override
    protected String getCompilerStdoutCommand() {
        return " -x c -dM -E"; // NOI18N
    }
}
