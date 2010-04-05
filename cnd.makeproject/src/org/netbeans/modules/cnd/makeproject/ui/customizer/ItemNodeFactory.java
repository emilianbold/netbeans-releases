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

package org.netbeans.modules.cnd.makeproject.ui.customizer;

import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.makeproject.api.configurations.ui.CustomizerNode;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author as204739
 */
public class ItemNodeFactory {
    private ItemNodeFactory() {
    }

    public static Node createRootNodeItem(Lookup lookup) {
        MakeContext context = lookup.lookup(MakeContext.class);
        CustomizerNode descriptions[];

        PredefinedToolKind tool = context.getItemTool();

        int count = 1;
        if (tool != PredefinedToolKind.UnknownTool) {
            count++;
        }
        descriptions = new CustomizerNode[count];
        int index = 0;
        descriptions[index++] = createGeneralItemDescription(lookup);
        if (tool != PredefinedToolKind.UnknownTool) {
            if (tool == PredefinedToolKind.CCompiler) {
                descriptions[index++] = createCCompilerDescription(lookup);
            } else if (tool == PredefinedToolKind.CCCompiler) {
                descriptions[index++] = createCCCompilerDescription(lookup);
            } else if (tool == PredefinedToolKind.FortranCompiler) {
                descriptions[index++] = createFortranCompilerDescription(lookup);
            } else if (tool == PredefinedToolKind.Assembler) {
                descriptions[index++] = createAssemblerDescription(lookup);
            } else if (tool == PredefinedToolKind.CustomTool) {
                descriptions[index++] = createCustomBuildItemDescription(lookup);
            } else {
                descriptions[index++] = createCustomBuildItemDescription(lookup); // FIXUP
            }
        }

        CustomizerNode rootDescription = new CustomizerNode(
                "Configuration Properties", getString("CONFIGURATION_PROPERTIES"), descriptions, lookup); // NOI18N

        return new PropertyNode(rootDescription);
    }

    private static CustomizerNode createGeneralItemDescription(Lookup lookup) {
        return new GeneralItemCustomizerNode(
                "GeneralItem", getString("LBL_Config_General"), null, lookup); // NOI18N
    }

    // Fortran Compiler Node
    public static CustomizerNode createFortranCompilerDescription(Lookup lookup) {
        String compilerName = "fortran"; // NOI18N
        String compilerDisplayName = PredefinedToolKind.FortranCompiler.getDisplayName();
        CustomizerNode fortranCompilerCustomizerNode = new FortranCompilerCustomizerNode(
                compilerName,  compilerDisplayName, null, lookup);
        return fortranCompilerCustomizerNode;
    }

    // Assembler Compiler Node
    public static CustomizerNode createAssemblerDescription(Lookup lookup) {
        String compilerName = "as"; // NOI18N
        String compilerDisplayName = PredefinedToolKind.Assembler.getDisplayName();
        CustomizerNode assemblerCustomizerNode = new AssemblerCustomizerNode(
                compilerName, compilerDisplayName, null, lookup);
        return assemblerCustomizerNode;
    }

    // CC Compiler Node
    public static CustomizerNode createCCCompilerDescription(Lookup lookup) {
        String compilerName = "cpp"; // NOI18N
        String compilerDisplayName = PredefinedToolKind.CCCompiler.getDisplayName();
        CustomizerNode ccCompilerCustomizerNode = new CCCompilerCustomizerNode(
                compilerName, compilerDisplayName, null, lookup);
        return ccCompilerCustomizerNode;
    }

    public static CustomizerNode createCustomBuildItemDescription(Lookup lookup) {
        return new CustomBuildItemCustomizerNode(
                "Custom Build Step", getString("LBL_Config_Custom_Build"), null, lookup); // NOI18N
    }

    // C Compiler Node
    public static CustomizerNode createCCompilerDescription(Lookup lookup) {
        String compilerName = "c"; // NOI18N
        String compilerDisplayName = PredefinedToolKind.CCompiler.getDisplayName();
        CustomizerNode cCompilerCustomizerNode = new CCompilerCustomizerNode(
                compilerName, compilerDisplayName, null, lookup);
        return cCompilerCustomizerNode;
    }

    private static String getString(String s) {
        return NbBundle.getBundle(MakeCustomizer.class).getString(s);
    }
}
