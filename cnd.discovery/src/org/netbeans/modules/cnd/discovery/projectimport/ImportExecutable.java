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

package org.netbeans.modules.cnd.discovery.projectimport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmListeners;
import org.netbeans.modules.cnd.api.model.CsmModel;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmProgressAdapter;
import org.netbeans.modules.cnd.api.model.CsmProgressListener;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.discovery.api.DiscoveryExtensionInterface.Applicable;
import org.netbeans.modules.cnd.discovery.wizard.DiscoveryExtension;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.modelimpl.csm.core.ModelImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.csm.core.Utils;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Alexander Simon
 */
public class ImportExecutable {
    private static final RequestProcessor RP = new RequestProcessor(ImportExecutable.class.getName(), 2);
    final Map<String, Object> map;
    final Project lastSelectedProject;
    final String functionToOpen;

    public ImportExecutable(Map<String, Object> map, Project lastSelectedProject, String functionToOpen) {
        this.map = map;
        this.lastSelectedProject = lastSelectedProject;
        this.functionToOpen = functionToOpen;
    }

    public void process(final DiscoveryExtension extension){
        switchModel(false, lastSelectedProject);
        RP.post(new Runnable() {

            @Override
            public void run() {
                ConfigurationDescriptorProvider provider = lastSelectedProject.getLookup().lookup(ConfigurationDescriptorProvider.class);
                MakeConfigurationDescriptor configurationDescriptor = provider.getConfigurationDescriptor(true);
                Applicable applicable = extension.isApplicable(map, lastSelectedProject);
                if (applicable.isApplicable()) {
                    resetCompilerSet(configurationDescriptor.getActiveConfiguration(), applicable);
                    if (extension.canApply(map, lastSelectedProject)) {
                        try {
                            extension.apply(map, lastSelectedProject);
                            saveMakeConfigurationDescriptor();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
                switchModel(true, lastSelectedProject);
                if (functionToOpen != null) {
                    openFunction(functionToOpen, lastSelectedProject);
                }
            }
        });
    }

    private static final List<CsmProgressListener> listeners = new ArrayList<CsmProgressListener>(1);

    private void openFunction(final String functionName, Project makeProject) {
        if (makeProject != null) {
            final NativeProject np = makeProject.getLookup().lookup(NativeProject.class);
            CsmProgressListener listener = new CsmProgressAdapter() {

                @Override
                public void projectParsingFinished(CsmProject project) {
                    if (project.getPlatformProject().equals(np)) {
                        CsmListeners.getDefault().removeProgressListener(this);
                        listeners.remove(this);
                        if (project instanceof ProjectBase) {
                            String from = Utils.getCsmDeclarationKindkey(CsmDeclaration.Kind.FUNCTION_DEFINITION) + ':' + functionName + '('; // NOI18N
                            Collection<CsmOffsetableDeclaration> decls = ((ProjectBase)project).findDeclarationsByPrefix(from);
                            for(CsmOffsetableDeclaration decl : decls){
                                CsmUtilities.openSource(decl);
                                break;
                            }
                        }
                    }
                }
            };
            listeners.add(listener);
            CsmListeners.getDefault().addProgressListener(listener);
        }
    }

    private void switchModel(boolean state, Project makeProject) {
        CsmModel model = CsmModelAccessor.getModel();
        if (model instanceof ModelImpl && makeProject != null) {
            NativeProject np = makeProject.getLookup().lookup(NativeProject.class);
            if (state) {
                ((ModelImpl) model).enableProject(np);
            } else {
                ((ModelImpl) model).disableProject(np);
            }
        }
    }

    private void resetCompilerSet(MakeConfiguration configuration, Applicable applicable){
        if (configuration != null) {
            CompilerSetManager manager = CompilerSetManager.get(configuration.getDevelopmentHost().getExecutionEnvironment());
            if (applicable.isSunStudio()) {
                CompilerSet def = manager.getDefaultCompilerSet();
                if (def != null && def.getCompilerFlavor().isSunStudioCompiler()) {
                    return;
                }
                List<CompilerSet> compilerSets = CompilerSetManager.get(configuration.getDevelopmentHost().getExecutionEnvironment()).getCompilerSets();
                def = null;
                for(CompilerSet set : compilerSets) {
                    if (set.getCompilerFlavor().isSunStudioCompiler()) {
                        if ("OracleSolarisStudio".equals(set.getName())) { // NOI18N
                            def = set;
                        }
                        if (def == null) {
                            def = set;
                        }
                    }
                }
                if (def != null) {
                    configuration.getCompilerSet().setValue(def.getName());
                }
            } else {
                // It seems GNU compiler. applicable.getCompilerName() contains a producer information.
                // Unfortunately producer field does not give information about flavor, only compiler version
                CompilerSet def = manager.getDefaultCompilerSet();
                if (def != null && !def.getCompilerFlavor().isSunStudioCompiler()) {
                    return;
                }
                List<CompilerSet> compilerSets = CompilerSetManager.get(configuration.getDevelopmentHost().getExecutionEnvironment()).getCompilerSets();
                def = null;
                for(CompilerSet set : compilerSets) {
                    if (!set.getCompilerFlavor().isSunStudioCompiler()) {
                        if (def == null) {
                            def = set;
                        }
                    }
                }
                if (def != null) {
                    configuration.getCompilerSet().setValue(def.getName());
                }
            }
        }
    }

    private void saveMakeConfigurationDescriptor() {
        ConfigurationDescriptorProvider pdp = lastSelectedProject.getLookup().lookup(ConfigurationDescriptorProvider.class);
        final MakeConfigurationDescriptor makeConfigurationDescriptor = pdp.getConfigurationDescriptor();
        makeConfigurationDescriptor.setModified();
        makeConfigurationDescriptor.save();
        makeConfigurationDescriptor.checkForChangedItems(lastSelectedProject, null, null);
    }
}
