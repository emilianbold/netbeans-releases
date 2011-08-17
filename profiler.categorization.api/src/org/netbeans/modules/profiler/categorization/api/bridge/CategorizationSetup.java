/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.profiler.categorization.api.bridge;

import java.util.Collection;
import org.netbeans.lib.profiler.common.ProfilingSettings;
import org.netbeans.lib.profiler.results.cpu.marking.MarkingEngine;
import org.netbeans.lib.profiler.ui.cpu.statistics.StatisticalModule;
import org.netbeans.lib.profiler.ui.cpu.statistics.StatisticalModuleContainer;
import org.netbeans.modules.profiler.categorization.api.Categorization;
import org.netbeans.modules.profiler.categorization.api.ProjectAwareStatisticalModule;
import org.netbeans.modules.profiler.spi.SessionListener;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jaroslav Bachorik
 */
@ServiceProvider(service = SessionListener.class)
public class CategorizationSetup extends SessionListener.Adapter {

    @Override
    public void onStartup(ProfilingSettings ps, Lookup.Provider project) {
        setupMarks(ps, project);
        setupStatistics(ps, project);
    }

    @Override
    public void onShutdown() {
        StatisticalModuleContainer statModulesContainer = Lookup.getDefault().lookup(StatisticalModuleContainer.class);

        if (statModulesContainer != null) {
            statModulesContainer.removeAllModules();
        }
    }

    private void setupMarks(ProfilingSettings ps, Lookup.Provider project) {
        boolean isMarksEnabled = (ps.getProfilingType() == ProfilingSettings.PROFILE_CPU_ENTIRE) || (ps.getProfilingType() == ProfilingSettings.PROFILE_CPU_PART);
        isMarksEnabled &= Categorization.isAvailable(project);

        if (isMarksEnabled) {
            Categorization ctg = new Categorization(project);
            ctg.reset();
            MarkingEngine.getDefault().configure(ctg.getMappings(), Lookup.getDefault().lookupAll(MarkingEngine.StateObserver.class));
        } else {
            MarkingEngine.getDefault().deconfigure();
        }
    }

    private void setupStatistics(ProfilingSettings ps, Lookup.Provider project) {
        StatisticalModuleContainer statModulesContainer = Lookup.getDefault().lookup(StatisticalModuleContainer.class);
        Collection<? extends StatisticalModule> modules = Lookup.getDefault().lookupAll(StatisticalModule.class);

        if ((statModulesContainer != null) && (modules != null)) {
            for (StatisticalModule module : modules) {
                /* Using workaround here
                 * For some reasons when the lookupAll is called the second time it returns ALL subtypes as well
                 * So I must check for the proper type and check for project support eventually
                 */

                if (module instanceof ProjectAwareStatisticalModule) {
                    if (((ProjectAwareStatisticalModule) module).supportsProject(project)) {
                        statModulesContainer.addModule(module);
                    }
                } else {
                    statModulesContainer.addModule(module);
                }
            }
        }

        Collection<? extends ProjectAwareStatisticalModule> pmodules = Lookup.getDefault().lookupAll(ProjectAwareStatisticalModule.class);

        if (pmodules != null) {
            for (ProjectAwareStatisticalModule module : pmodules) {
                if (module.supportsProject(project)) {
                    statModulesContainer.addModule(module);
                }
            }
        }
    }
}
