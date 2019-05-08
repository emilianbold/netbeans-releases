/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.ui.MakeProjectCustomizerEx;
import org.netbeans.modules.cnd.makeproject.api.ui.configurations.CustomizerNode;
import org.netbeans.modules.cnd.makeproject.api.ui.configurations.CustomizerRootNodeProvider;
import org.netbeans.modules.cnd.makeproject.api.ui.configurations.DebuggerCustomizerNode;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 */
public class ProjectNodeFactory {
    private ProjectNodeFactory() {
    }

    public static Node createRootNodeProject(Lookup lookup) {
        MakeContext context = lookup.lookup(MakeContext.class);
        boolean includeMakefileDescription = true;
        boolean includeRunDebugDescriptions = true;
        Configuration[] selectedConfigurations = context.getSelectedConfigurations();
        // calculate the greatest common subset of project properties
        for (int i = 0; i < selectedConfigurations.length; i++) {
            MakeConfiguration makeConfiguration = (MakeConfiguration) selectedConfigurations[i];
            includeMakefileDescription &= makeConfiguration.isMakefileConfiguration();
            //includeRunDebugDescriptions &= !makeConfiguration.isLibraryConfiguration();
        }
        
        List<CustomizerNode> uncheckedCustomizers = CustomizerRootNodeProvider.getInstance().getCustomizerNodes(lookup);
        List<CustomizerNode> descriptions = new ArrayList<>();
        CustomizerNode node = createGeneralDescription(lookup);
        if (node != null) {
            descriptions.add(node);
        }
        node = createBuildDescription(lookup);
        if (node != null) {
            descriptions.add(node);
        }

        // Run customizers
        List<CustomizerNode> runCustomizers = CustomizerRootNodeProvider.getInstance().getCustomizerNodes("Run", lookup); // NOI18N
        if (includeRunDebugDescriptions) {
            if (!descriptions.addAll(runCustomizers)) {
                descriptions.add(createNotFoundNode("Run")); // NOI18N
            }
        }
        uncheckedCustomizers.removeAll(runCustomizers);

        // Profile customizers
        List<CustomizerNode> profileCustomizers = CustomizerRootNodeProvider.getInstance().getCustomizerNodes("Profile", lookup); // NOI18N
        if (includeRunDebugDescriptions) {
            descriptions.addAll(profileCustomizers);
        }
        uncheckedCustomizers.removeAll(profileCustomizers);

        // Debug customizers
        List<CustomizerNode> debugCustomizers = CustomizerRootNodeProvider.getInstance().getCustomizerNodes("Debug", lookup); // NOI18N
        if (includeRunDebugDescriptions) {
            descriptions.addAll(getVisibleDebuggerNodes(debugCustomizers, lookup));
        }
        uncheckedCustomizers.removeAll(debugCustomizers);

        // Add remainder nodes direcrtly under root and not under Advanced. Don't need Advanced node anymore, right?
        descriptions.addAll(uncheckedCustomizers);
//        CustomizerNode advanced = getAdvancedCustomizerNode(uncheckedCustomizers, lookup);
//        if (advanced != null) {
//            descriptions.add(advanced);
//        }

        descriptions.add(createRequiredProjectsDescription(lookup));
        if (includeMakefileDescription) {
            descriptions.add(createCodeAssistantDescription(lookup));
        }

        descriptions.add(createFormattingDescription(lookup));

        descriptions.add(createLicenceDescription(lookup));

        descriptions.add(createLaunchersDescription(lookup));

        CustomizerNode rootDescription = new CustomizerNode(
                "Configuration Properties", getString("CONFIGURATION_PROPERTIES"), descriptions.toArray(new CustomizerNode[descriptions.size()]), lookup);  // NOI18N

        
        MakeConfigurationDescriptor makeConfigurationDescriptor = (MakeConfigurationDescriptor)context.getConfigurationDescriptor();
        if (makeConfigurationDescriptor.hasProjectCustomizer()) {
            MakeProjectCustomizerEx makeprojectCustomizer = (MakeProjectCustomizerEx) makeConfigurationDescriptor.getProjectCustomizer();
            rootDescription = makeprojectCustomizer.getRootPropertyNode(rootDescription);
        }

        return new PropertyNode(rootDescription);
    }

    private static CustomizerNode createGeneralDescription(Lookup lookup) {
        return new GeneralCustomizerNode(
                "General", getString("LBL_Config_General"), null, lookup); // NOI18N
    }

    private static CustomizerNode createFormattingDescription(Lookup lookup) {
        return new FormattingCustomizerNode(
                "Formatting", getString("LBL_Formatting"), null, lookup); // NOI18N
    }

    private static CustomizerNode createLicenceDescription(Lookup lookup) {
        return new LicenseCustomizerNode(
                "License", getString("LBL_License"), null, lookup); // NOI18N
    }

    private static CustomizerNode createLaunchersDescription(Lookup lookup) {
        return new LaunchersCustomizerNode(
                "Launchers", getString("LBL_Launchers"), null, lookup); // NOI18N
    }

    private static CustomizerNode createBuildDescription(Lookup lookup) {
        MakeContext context = lookup.lookup(MakeContext.class);

        boolean includeMakefileDescription = true;
        boolean includeQtDescription = true;
        boolean includeCompilerDescription = true;
        boolean includeLinkerDescription = true;
        boolean includeArchiverDescription = true;
        boolean isCompileConfiguration =context.isCompilerConfiguration();

        CompilerSet compilerSet = null;
        Configuration[] selectedConfigurations = context.getSelectedConfigurations();
        for (int i = 0; i < selectedConfigurations.length; i++) {
            MakeConfiguration makeConfiguration = (MakeConfiguration) selectedConfigurations[i];
            CompilerSet compilerSet2 = makeConfiguration.getCompilerSet().getCompilerSet();
            if (compilerSet != null && compilerSet2 != compilerSet) {
                includeCompilerDescription = false;
            }
            compilerSet = compilerSet2;

            if ((isCompileConfiguration && !makeConfiguration.isCompileConfiguration()) || (!isCompileConfiguration && makeConfiguration.isCompileConfiguration())) {
                includeCompilerDescription = false;
            }

            includeMakefileDescription &= makeConfiguration.isMakefileConfiguration();
            includeQtDescription &= makeConfiguration.isQmakeConfiguration();
            includeCompilerDescription &= !makeConfiguration.isMakefileConfiguration();
            includeLinkerDescription &= makeConfiguration.isApplicationConfiguration() || makeConfiguration.isDynamicLibraryConfiguration();
            includeArchiverDescription &= makeConfiguration.isLibraryConfiguration() && !makeConfiguration.isDynamicLibraryConfiguration() && !makeConfiguration.isQmakeConfiguration();
        }

        ArrayList<CustomizerNode> descriptions = new ArrayList<>();
        if (includeMakefileDescription) {
            descriptions.add(createPreBuildDescription(lookup));
            descriptions.add(createMakefileDescription(lookup));
            descriptions.add(createCompileDescription(lookup));
        }
        if (includeQtDescription) {
            descriptions.add(createQtDescription(lookup));
        }
        if (includeCompilerDescription) {
            descriptions.addAll(createCompilerNodes(lookup));
        }
        if (includeLinkerDescription) {
            descriptions.add(createLinkerDescription(lookup));
        }
        if (includeArchiverDescription) {
            descriptions.add(createArchiverDescription(lookup));
        }

        if (((MakeConfigurationDescriptor) context.getConfigurationDescriptor()).getActiveConfiguration() != null && ((MakeConfigurationDescriptor) context.getConfigurationDescriptor()).getActiveConfiguration().getConfigurationType().getValue() != MakeConfiguration.TYPE_MAKEFILE) {
            descriptions.add(createPackagingDescription(lookup));
        }

        return new BuildCustomizerNode(
                "Build", getString("LBL_Config_Build"), descriptions.toArray(new CustomizerNode[descriptions.size()]), lookup); // NOI18N
    }

    // Pre-Build Node
    private static CustomizerNode createPreBuildDescription(Lookup lookup) {
        return new PreBuildCustomizerNode(
                "PreBuild", getString("LBL_PRE_BUILD_NODE"), null, lookup); // NOI18N
    }

    // Make Node
    private static CustomizerNode createMakefileDescription(Lookup lookup) {
        return new MakefileCustomizerNode(
                "Make", getString("LBL_MAKE_NODE"), null, lookup); // NOI18N
    }

    // Compile Node
    private static CustomizerNode createCompileDescription(Lookup lookup) {
        return new CompileCustomizerNode(
                "Compile", getString("LBL_COMPILE_NODE"), null, lookup); // NOI18N
    }

    // C/C++/Fortran Node
    private static ArrayList<CustomizerNode> createCompilerNodes(Lookup lookup) {
        MakeContext context = lookup.lookup(MakeContext.class);
        ArrayList<CustomizerNode> descriptions = new ArrayList<>();
        if (!context.isQtMode()) {
            descriptions.add(ItemNodeFactory.createCCompilerDescription(lookup));
        }
        descriptions.add(ItemNodeFactory.createCCCompilerDescription(lookup));
        if (!context.isQtMode()) {
            descriptions.add(ItemNodeFactory.createFortranCompilerDescription(lookup));
            descriptions.add(ItemNodeFactory.createAssemblerDescription(lookup));
        }
        return descriptions;
    }

    // Linker
    private static CustomizerNode createLinkerDescription(Lookup lookup) {
        return new LinkerGeneralCustomizerNode("Linker", getString("LBL_LINKER_NODE"), null, lookup); // NOI18N
    }

    // Archiver
    private static CustomizerNode createArchiverDescription(Lookup lookup) {
        return new ArchiverGeneralCustomizerNode("Archiver", getString("LBL_ARCHIVER_NODE"), null, lookup); // NOI18N
    }

    private static CustomizerNode createQtDescription(Lookup lookup) {
        return new QtCustomizerNode("Qt", getString("LBL_QT_NODE"), null, lookup); // NOI18N
    }

    // Packaging
    private static CustomizerNode createPackagingDescription(Lookup lookup) {
        return new PackagingCustomizerNode("Packaging", getString("LBL_PACKAGING_NODE"), null, lookup); // NOI18N
    }

    // Required Projects Node
    private static CustomizerNode createRequiredProjectsDescription(Lookup lookup) {
        return new RequiredProjectsCustomizerNode(
                "RequiredProjects", getString("LBL_REQUIRED_PROJECTS_NODE"), null, lookup); // NOI18N
    }

    private static List<CustomizerNode> getVisibleDebuggerNodes(List<CustomizerNode> debuggerNodes, Lookup lookup) {
        MakeContext context = lookup.lookup(MakeContext.class);
        Configuration[] selectedConfigurations = context.getSelectedConfigurations();
        List<CustomizerNode> res = new ArrayList<>();
        if (debuggerNodes.size() > 1) {
            // Figure out toolchain families
            Set<String> families = new HashSet<>();
            for (Configuration conf : selectedConfigurations) {
                MakeConfiguration makeConfiguration = (MakeConfiguration) conf;
                CompilerSet compilerSet = makeConfiguration.getCompilerSet().getCompilerSet();
                if (compilerSet == null) {
                    continue;
                }
                families.addAll(Arrays.asList(compilerSet.getCompilerFlavor().getToolchainDescriptor().getFamily()));
            }

            // Find correct debugger customizer
            debuggerNodes.forEach((dNode) -> {
                if (dNode instanceof DebuggerCustomizerNode) {
                    if (families.contains(((DebuggerCustomizerNode)dNode).getFamily())) {
                        res.add(dNode);
                    }
                } else {
                    res.add(dNode);
                }
            });
        } else if (debuggerNodes.size() == 1) {
            res.addAll(debuggerNodes);
        } else {
            //res.add(createNotFoundNode("Debug")); // NOI18N
        }
        return res;
    }

    // Code Assistant Node
    private static CustomizerNode createCodeAssistantDescription(Lookup lookup) {
        ArrayList<CustomizerNode> descriptions = new ArrayList<>();
        descriptions.add(ItemNodeFactory.createCCompilerDescription(lookup));
        descriptions.add(ItemNodeFactory.createCCCompilerDescription(lookup));
        String nodeLabel = getString("LBL_PARSER_NODE");
        return new CodeAssistanceCustomizerNode("CodeAssistance", nodeLabel,  descriptions.toArray(new CustomizerNode[descriptions.size()]), lookup); // NOI18N
    }

    private static CustomizerNode createNotFoundNode(String nodeName) {
        return new CustomizerNode(nodeName, nodeName + " - not found", null, null); // NOI18N
    }


    private static String getString(String s) {
        return NbBundle.getMessage(ProjectNodeFactory.class, s);
    }
}
