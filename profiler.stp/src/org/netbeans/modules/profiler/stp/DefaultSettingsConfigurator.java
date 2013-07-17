/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.profiler.stp;

import java.util.Collection;
import org.netbeans.lib.profiler.client.ClientUtils;
import org.netbeans.lib.profiler.common.ProfilingSettings;
import org.netbeans.lib.profiler.common.ProfilingSettingsPresets;
import org.netbeans.modules.profiler.ppoints.ui.ProfilingPointsDisplayer;
import org.openide.filesystems.FileObject;
import java.util.Properties;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArraySet;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.lib.profiler.common.filters.SimpleFilter;
import org.netbeans.modules.profiler.api.ProjectUtilities;
import org.netbeans.modules.profiler.api.project.ProfilingSettingsSupport;
import org.netbeans.modules.profiler.api.project.ProjectContentsSupport;
import org.openide.util.Lookup;


/**
 *
 * @author Jiri Sedlacek
 */
final class DefaultSettingsConfigurator implements SelectProfilingTask.SettingsConfigurator {
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    public static class CPUContents implements SettingsContainerPanel.Contents, ChangeListener {
        //~ Static fields/initializers -------------------------------------------------------------------------------------------

//        private static final boolean ENABLE_THREAD_CPU_TIMER = Boolean.getBoolean("org.netbeans.lib.profiler.enableThreadCPUTimer"); // NOI18N

        //~ Instance fields ------------------------------------------------------------------------------------------------------

        private CPUSettingsAdvancedPanel advancedSettingsPanel;
        private CPUSettingsBasicPanel basicSettingsPanel;
        private FileObject profiledFile;
        private ProfilingSettings settings;
        private Lookup.Provider project;
        private ProfilingSettingsSupport pss;
        private Collection<ChangeListener> changeListeners = new CopyOnWriteArraySet<ChangeListener>();
        private boolean internalChange = false;
        private boolean isPreset;
        final private static boolean useCPUTimer = true; // always enabled

        //~ Constructors ---------------------------------------------------------------------------------------------------------

        public CPUContents() {
            basicSettingsPanel = new CPUSettingsBasicPanel();
            advancedSettingsPanel = new CPUSettingsAdvancedPanel();

            basicSettingsPanel.addChangeListener(this);
            advancedSettingsPanel.addChangeListener(this);
        }

        //~ Methods --------------------------------------------------------------------------------------------------------------

        public JPanel getAdvancedSettingsPanel() {
            return advancedSettingsPanel;
        }

        public JPanel getBasicSettingsPanel() {
            return basicSettingsPanel;
        }

        public void setContext(Lookup.Provider project, FileObject profiledFile, boolean isAttach, boolean isModify) {
            this.project = project;
            pss = ProfilingSettingsSupport.get(project);
            this.profiledFile = profiledFile;
        }

        public float getProfilingOverhead() {
            if (settings == null) {
                return 0f;
            }

            synchronizeSettings();
            return pss.getProfilingOverhead(settings);
        }

        public void setSettings(ProfilingSettings settings) {
            this.settings = settings;
            isPreset = settings.isPreset();

            internalChange = true;

            // basicSettingsPanel
            basicSettingsPanel.setContext(project, SelectProfilingTask.getDefault().getPredefinedInstrFilterKeys(),
                                          new Runnable() {
                    public void run() {
                        synchronizeSettings();
                        ProfilingPointsDisplayer.displayProfilingPoints(project, CPUContents.this.settings);
                    }
                });
            basicSettingsPanel.setProfilingType(settings.getProfilingType());
            basicSettingsPanel.setRootMethods(settings.getInstrumentationRootMethods());
            basicSettingsPanel.setQuickFilter(settings.getQuickFilter());
            basicSettingsPanel.setInstrumentationFilter(settings.getSelectedInstrumentationFilter());
            basicSettingsPanel.setUseProfilingPoints(settings.useProfilingPoints() && (project != null));

            // advancedSettingsPanel
            if (!settings.isPreset()) {
                advancedSettingsPanel.enableAll();
            }

            advancedSettingsPanel.setProfilingType(settings.getProfilingType());
            advancedSettingsPanel.setCPUProfilingType(settings.getCPUProfilingType());
            advancedSettingsPanel.setSamplingFrequency(settings.getSamplingFrequency());
            advancedSettingsPanel.setSamplingInterval(settings.getSamplingInterval());
            advancedSettingsPanel.setExcludeThreadTime(settings.getExcludeWaitTime());
            advancedSettingsPanel.setProfileSpawnedThreads(settings.getInstrumentSpawnedThreads());
            advancedSettingsPanel.setUseCPUTimer(settings.getThreadCPUTimerOn(), useCPUTimer);
            advancedSettingsPanel.setInstrumentMethodInvoke(settings.getInstrumentMethodInvoke());
            advancedSettingsPanel.setInstrumentGettersSetters(settings.getInstrumentGetterSetterMethods());
            advancedSettingsPanel.setInstrumentEmptyMethods(settings.getInstrumentEmptyMethods());
            advancedSettingsPanel.setInstrumentationScheme(settings.getInstrScheme());
            advancedSettingsPanel.setProfiledThreadsLimit(settings.getNProfiledThreadsLimit());
            advancedSettingsPanel.setProfileFramework(settings.getProfileUnderlyingFramework());

            advancedSettingsPanel.setThreadsMonitoring(settings.getThreadsMonitoringEnabled());
            advancedSettingsPanel.setThreadsSampling(settings.getThreadsSamplingEnabled());
            advancedSettingsPanel.setLockContentionMonitoring(settings.getLockContentionMonitoringEnabled());

            if (settings.isPreset()) {
                advancedSettingsPanel.disableAll();
            }

            internalChange = false;

            fireSettingsChanged();
        }

        public void addChangeListener(ChangeListener listener) {
            changeListeners.add(listener);
        }

        public ProfilingSettings createFinalSettings() {
            ProfilingSettings finalSettings = ProfilingSettingsPresets.createCPUPreset(settings.getProfilingType());

            finalSettings.setIsPreset(settings.isPreset());
            finalSettings.setSettingsName(settings.getSettingsName());

            // basicSettingsPanel
            finalSettings.setProfilingType(basicSettingsPanel.getProfilingType());
//            finalSettings.setInstrumentationRootMethods(basicSettingsPanel.getRootMethods());
//            finalSettings.setSelectedInstrumentationFilter(basicSettingsPanel.getInstrumentationFilter());
            finalSettings.setUseProfilingPoints(basicSettingsPanel.getUseProfilingPoints());

            // advancedSettingsPanel
            finalSettings.setCPUProfilingType(advancedSettingsPanel.getCPUProfilingType());
            finalSettings.setSamplingFrequency(advancedSettingsPanel.getSamplingFrequency());
            finalSettings.setSamplingInterval(advancedSettingsPanel.getSamplingInterval());
            finalSettings.setExcludeWaitTime(advancedSettingsPanel.getExcludeThreadTime());
            finalSettings.setProfileUnderlyingFramework(advancedSettingsPanel.getProfileFramework());
            finalSettings.setInstrumentSpawnedThreads(advancedSettingsPanel.getProfileSpawnedThreads());
            finalSettings.setThreadCPUTimerOn(useCPUTimer && advancedSettingsPanel.getUseCPUTimer());
            finalSettings.setInstrumentMethodInvoke(advancedSettingsPanel.getInstrumentMethodInvoke());
            finalSettings.setInstrumentGetterSetterMethods(advancedSettingsPanel.getInstrumentGettersSetters());
            finalSettings.setInstrumentEmptyMethods(advancedSettingsPanel.getInstrumentEmptyMethods());
            finalSettings.setInstrScheme(advancedSettingsPanel.getInstrumentationScheme());
            finalSettings.setNProfiledThreadsLimit(advancedSettingsPanel.getProfiledThreadsLimit());

            finalSettings.setThreadsMonitoringEnabled(advancedSettingsPanel.getThreadsMonitoring());
            finalSettings.setThreadsSamplingEnabled(advancedSettingsPanel.getThreadsSampling());
            finalSettings.setLockContentionMonitoringEnabled(advancedSettingsPanel.getLockContentionMonitoring());
            
            // generated settings
//            ClientUtils.SourceCodeSelection[] emptyRoots = new ClientUtils.SourceCodeSelection[0];
            if (finalSettings.getProfileUnderlyingFramework()) {
                finalSettings.setInstrumentationRootMethods(new ClientUtils.SourceCodeSelection[0]);
            } else if (finalSettings.getProfilingType() == ProfilingSettings.PROFILE_CPU_ENTIRE) {
                finalSettings.setInstrumentationRootMethods(ProjectContentsSupport.get(project).
                        getProfilingRoots(profiledFile, ProjectUtilities.hasSubprojects(project)));
            } else {
                finalSettings.setInstrumentationRootMethods(basicSettingsPanel.getRootMethods());
            }
            
            Object selectedFilter = basicSettingsPanel.getInstrumentationFilter();
            if (SelectProfilingTask.getDefault().isPredefinedFilter(selectedFilter)) {
                 finalSettings.setSelectedInstrumentationFilter(SelectProfilingTask.getDefault().
                    getResolvedPredefinedFilter((SimpleFilter)selectedFilter));
            } else {
                finalSettings.setSelectedInstrumentationFilter(selectedFilter);
            }
            

//            // generated settings
//            if (finalSettings.getProfilingType() == ProfilingSettings.PROFILE_CPU_ENTIRE) {
//                finalSettings.setInstrumentationRootMethods(new ClientUtils.SourceCodeSelection[0]);
//                finalSettings.instrRootMethodsPending = true;
//            } else {
//                finalSettings.setInstrumentationRootMethods(basicSettingsPanel.getRootMethods());
//            }

            return finalSettings;
        }

        public void removeChangeListener(ChangeListener listener) {
            changeListeners.remove(listener);
        }

        public void reset() {
            settings = null;
            project = null;
            pss = null;
            profiledFile = null;
            isPreset = false;
        }

        public void stateChanged(ChangeEvent e) {
            fireSettingsChanged();
        }

        public void synchronizeBasicAdvancedPanels() {
            //      if (isPreset()) {
            if (basicSettingsPanel.getProfilingType() == ProfilingSettings.PROFILE_CPU_ENTIRE) {
                advancedSettingsPanel.setEntireAppDefaults(isPreset);
            } else {
                advancedSettingsPanel.setPartOfAppDefaults(isPreset);
            }
            advancedSettingsPanel.setProfilingType(basicSettingsPanel.getProfilingType());

            //      }
        }

        public void synchronizeSettings() {
            synchronizeBasicAdvancedPanels();

            // basicSettingsPanel
            settings.setProfilingType(basicSettingsPanel.getProfilingType());
            settings.setInstrumentationRootMethods(basicSettingsPanel.getRootMethods());
            settings.setQuickFilter(basicSettingsPanel.getQuickFilter());
            settings.setSelectedInstrumentationFilter(basicSettingsPanel.getInstrumentationFilter());
            settings.setUseProfilingPoints(basicSettingsPanel.getUseProfilingPoints());

            // advancedSettingsPanel
            settings.setCPUProfilingType(advancedSettingsPanel.getCPUProfilingType());
            settings.setSamplingFrequency(advancedSettingsPanel.getSamplingFrequency());
            settings.setSamplingInterval(advancedSettingsPanel.getSamplingInterval());
            settings.setExcludeWaitTime(advancedSettingsPanel.getExcludeThreadTime());
            settings.setProfileUnderlyingFramework(advancedSettingsPanel.getProfileFramework());
            settings.setInstrumentSpawnedThreads(advancedSettingsPanel.getProfileSpawnedThreads());
            settings.setThreadCPUTimerOn(advancedSettingsPanel.getUseCPUTimer());
            settings.setInstrumentMethodInvoke(advancedSettingsPanel.getInstrumentMethodInvoke());
            settings.setInstrumentGetterSetterMethods(advancedSettingsPanel.getInstrumentGettersSetters());
            settings.setInstrumentEmptyMethods(advancedSettingsPanel.getInstrumentEmptyMethods());
            settings.setInstrScheme(advancedSettingsPanel.getInstrumentationScheme());
            settings.setNProfiledThreadsLimit(advancedSettingsPanel.getProfiledThreadsLimit());

            settings.setThreadsMonitoringEnabled(advancedSettingsPanel.getThreadsMonitoring());
            settings.setThreadsSamplingEnabled(advancedSettingsPanel.getThreadsSampling());
            settings.setLockContentionMonitoringEnabled(advancedSettingsPanel.getLockContentionMonitoring());
        }

        private void fireSettingsChanged() {
            if (!internalChange) {
                for (ChangeListener listener : changeListeners) {
                    listener.stateChanged(new ChangeEvent(this));
                }
            }
        }
    }

    public static class MemoryContents implements SettingsContainerPanel.Contents, ChangeListener {
        //~ Instance fields ------------------------------------------------------------------------------------------------------

        private FileObject profiledFile;
        private MemorySettingsAdvancedPanel advancedSettingsPanel = new MemorySettingsAdvancedPanel();
        private MemorySettingsBasicPanel basicSettingsPanel = new MemorySettingsBasicPanel();
        private ProfilingSettings settings;
        private Lookup.Provider project;
        private ProfilingSettingsSupport pss;
        private Collection<ChangeListener> changeListeners = new CopyOnWriteArraySet<ChangeListener>();
        private boolean enableOverride;
        private boolean internalChange = false;
        private boolean isAttach;
        private boolean isModify;
        private boolean isPreset;

        //~ Constructors ---------------------------------------------------------------------------------------------------------

        public MemoryContents() {
            basicSettingsPanel = new MemorySettingsBasicPanel();
            advancedSettingsPanel = new MemorySettingsAdvancedPanel();

            basicSettingsPanel.addChangeListener(this);
            advancedSettingsPanel.addChangeListener(this);
        }

        //~ Methods --------------------------------------------------------------------------------------------------------------

        public JPanel getAdvancedSettingsPanel() {
            return advancedSettingsPanel;
        }

        public JPanel getBasicSettingsPanel() {
            return basicSettingsPanel;
        }

        public void setContext(Lookup.Provider project, FileObject profiledFile, boolean isAttach, boolean isModify) {
            this.project = project;
            pss = ProfilingSettingsSupport.get(project);
            this.profiledFile = profiledFile;
            this.enableOverride = enableOverride;
            this.isAttach = isAttach;
            this.isModify = isModify;
        }

        public float getProfilingOverhead() {
            if (settings == null) {
                return 0f;
            }

            synchronizeSettings();
            return pss.getProfilingOverhead(settings);
        }

        public void setSettings(ProfilingSettings settings) {
            this.settings = settings;
            isPreset = settings.isPreset();

            internalChange = true;

            SelectProfilingTask.getDefault().enableSubmitButton();

            // basicSettingsPanel
            basicSettingsPanel.setContext(project,
                                          new Runnable() {
                    public void run() {
                        synchronizeSettings();
                        ProfilingPointsDisplayer.displayProfilingPoints(project, MemoryContents.this.settings);
                    }
                });
            basicSettingsPanel.setProfilingType(settings.getProfilingType());
            basicSettingsPanel.setRecordStackTrace(settings.getAllocStackTraceLimit() != 0);
            basicSettingsPanel.setUseProfilingPoints(settings.useProfilingPoints() && (project != null));

            // advancedSettingsPanel
            if (!settings.isPreset()) {
                advancedSettingsPanel.enableAll();
            }
            
            advancedSettingsPanel.setProfilingType(settings.getProfilingType());
            advancedSettingsPanel.setTrackEvery(settings.getAllocTrackEvery());

            advancedSettingsPanel.setAllocStackTraceLimit(settings.getAllocStackTraceLimit());
            advancedSettingsPanel.setRunGC(settings.getRunGCOnGetResultsInMemoryProfiling());

            advancedSettingsPanel.setThreadsMonitoring(settings.getThreadsMonitoringEnabled());
            advancedSettingsPanel.setThreadsSampling(settings.getThreadsSamplingEnabled());
            advancedSettingsPanel.setLockContentionMonitoring(settings.getLockContentionMonitoringEnabled());

            if (settings.isPreset()) {
                advancedSettingsPanel.disableAll();
            }

            internalChange = false;

            fireSettingsChanged();
        }

        public void addChangeListener(ChangeListener listener) {
            changeListeners.add(listener);
        }

        public ProfilingSettings createFinalSettings() {
            ProfilingSettings finalSettings = ProfilingSettingsPresets.createMemoryPreset();

            finalSettings.setIsPreset(settings.isPreset());
            finalSettings.setSettingsName(settings.getSettingsName());

            // basicSettingsPanel
            finalSettings.setProfilingType(basicSettingsPanel.getProfilingType());
            finalSettings.setUseProfilingPoints(basicSettingsPanel.getUseProfilingPoints());

            // advancedSettingsPanel
            finalSettings.setAllocTrackEvery(advancedSettingsPanel.getTrackEvery());
            finalSettings.setAllocStackTraceLimit(basicSettingsPanel.getRecordStackTrace()
                                                  ? advancedSettingsPanel.getAllocStackTraceLimit() : 0);
            finalSettings.setRunGCOnGetResultsInMemoryProfiling(advancedSettingsPanel.getRunGC());

            finalSettings.setThreadsMonitoringEnabled(advancedSettingsPanel.getThreadsMonitoring());
            finalSettings.setThreadsSamplingEnabled(advancedSettingsPanel.getThreadsSampling());
            finalSettings.setLockContentionMonitoringEnabled(advancedSettingsPanel.getLockContentionMonitoring());

            return finalSettings;
        }

        public void removeChangeListener(ChangeListener listener) {
            changeListeners.remove(listener);
        }

        public void reset() {
            settings = null;
            project = null;
            pss = null;
            profiledFile = null;
            enableOverride = false;
            isAttach = false;
            isModify = false;
            isPreset = false;
        }

        public void stateChanged(ChangeEvent e) {
            fireSettingsChanged();
        }

        public void synchronizeBasicAdvancedPanels() {
            boolean recordStackTrace = basicSettingsPanel.getRecordStackTrace();
            advancedSettingsPanel.setProfilingType(basicSettingsPanel.getProfilingType());
            advancedSettingsPanel.setRecordStackTrace(recordStackTrace);
            advancedSettingsPanel.updateRunGC(basicSettingsPanel.getProfilingType() ==
                                              ProfilingSettings.PROFILE_MEMORY_ALLOCATIONS);
        }

        public void synchronizeSettings() {
            synchronizeBasicAdvancedPanels();

            // basicSettingsPanel
            settings.setProfilingType(basicSettingsPanel.getProfilingType());
            settings.setUseProfilingPoints(basicSettingsPanel.getUseProfilingPoints());

            // advancedSettingsPanel
            settings.setAllocTrackEvery(advancedSettingsPanel.getTrackEvery());
            settings.setAllocStackTraceLimit(basicSettingsPanel.getRecordStackTrace()
                                             ? advancedSettingsPanel.getAllocStackTraceLimit() : 0);
            settings.setRunGCOnGetResultsInMemoryProfiling(advancedSettingsPanel.getRunGC());

            settings.setThreadsMonitoringEnabled(advancedSettingsPanel.getThreadsMonitoring());
            settings.setThreadsSamplingEnabled(advancedSettingsPanel.getThreadsSampling());
            settings.setLockContentionMonitoringEnabled(advancedSettingsPanel.getLockContentionMonitoring());
        }

        private void fireSettingsChanged() {
            if (!internalChange) {
                for (ChangeListener listener : changeListeners) {
                    listener.stateChanged(new ChangeEvent(this));
                }
            }
        }
    }

    // --- Public contents to be reused by SelectProfilingTask.SettingsConfigurator implementors
    public static class MonitorContents implements SettingsContainerPanel.Contents, ChangeListener {
        //~ Instance fields ------------------------------------------------------------------------------------------------------

        private FileObject profiledFile;
        private JPanel advancedSettingsPanel;
        private MonitorSettingsBasicPanel basicSettingsPanel;
        private ProfilingSettings settings;
        private Lookup.Provider project;
        private ProfilingSettingsSupport pss;
        private Collection<ChangeListener> changeListeners = new CopyOnWriteArraySet<ChangeListener>();
        private boolean internalChange = false;
        private boolean isAttach;
        private boolean isModify;
        private boolean isPreset;

        //~ Constructors ---------------------------------------------------------------------------------------------------------

        public MonitorContents() {
            basicSettingsPanel = new MonitorSettingsBasicPanel();
            advancedSettingsPanel = new JPanel();
            advancedSettingsPanel.setVisible(false);
            
            basicSettingsPanel.addChangeListener(this);
        }

        //~ Methods --------------------------------------------------------------------------------------------------------------

        public JPanel getAdvancedSettingsPanel() {
            return advancedSettingsPanel;
        }

        public JPanel getBasicSettingsPanel() {
            return basicSettingsPanel;
        }

        public void setContext(Lookup.Provider project, FileObject profiledFile, boolean isAttach, boolean isModify) {
            this.project = project;
            pss = ProfilingSettingsSupport.get(project);
            this.profiledFile = profiledFile;
            this.isAttach = isAttach;
            this.isModify = isModify;
        }

        public float getProfilingOverhead() {
            if (settings == null) {
                return 0f;
            }

            synchronizeSettings();
            return pss.getProfilingOverhead(settings);
        }

        public void setSettings(ProfilingSettings settings) {
            this.settings = settings;
            isPreset = settings.isPreset();

            internalChange = true;

            SelectProfilingTask.getDefault().enableSubmitButton();

            // basicSettingsPanel
            basicSettingsPanel.setThreadsMonitoring(settings.getThreadsMonitoringEnabled());
            basicSettingsPanel.setThreadsSampling(settings.getThreadsSamplingEnabled());
            basicSettingsPanel.setLockContentionMonitoring(settings.getLockContentionMonitoringEnabled());

            internalChange = false;

            fireSettingsChanged();
        }

        public void addChangeListener(ChangeListener listener) {
            changeListeners.add(listener);
        }

        public ProfilingSettings createFinalSettings() {
            ProfilingSettings finalSettings = ProfilingSettingsPresets.createMonitorPreset();

            finalSettings.setIsPreset(settings.isPreset());
            finalSettings.setSettingsName(settings.getSettingsName());

            // basicSettingsPanel
            finalSettings.setThreadsMonitoringEnabled(basicSettingsPanel.getThreadsMonitoring());
            finalSettings.setThreadsSamplingEnabled(basicSettingsPanel.getThreadsSampling());
            finalSettings.setLockContentionMonitoringEnabled(basicSettingsPanel.getLockContentionMonitoring());

            // advancedSettingsPanel

            return finalSettings;
        }

        public void removeChangeListener(ChangeListener listener) {
            changeListeners.remove(listener);
        }

        public void reset() {
            settings = null;
            project = null;
            pss = null;
            profiledFile = null;
            isAttach = false;
            isModify = false;
            isPreset = false;
        }

        public void stateChanged(ChangeEvent e) {
            fireSettingsChanged();
        }

        public void synchronizeBasicAdvancedPanels() {
        }

        public void synchronizeSettings() {
            synchronizeBasicAdvancedPanels();

            // basicSettingsPanel
            settings.setThreadsMonitoringEnabled(basicSettingsPanel.getThreadsMonitoring());
            settings.setThreadsSamplingEnabled(basicSettingsPanel.getThreadsSampling());
            settings.setLockContentionMonitoringEnabled(basicSettingsPanel.getLockContentionMonitoring());

            // advancedSettingsPanel
        }

        private void fireSettingsChanged() {
            if (!internalChange) {
                for (ChangeListener listener : changeListeners) {
                    listener.stateChanged(new ChangeEvent(this));
                }
            }
        }
    }

    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    // --- Shared instance -------------------------------------------------------
    public static final DefaultSettingsConfigurator SHARED_INSTANCE = new DefaultSettingsConfigurator();

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private CPUContents cpuContents = new CPUContents();
    private FileObject profiledFile;
    private MemoryContents memoryContents = new MemoryContents();
    private MonitorContents monitorContents = new MonitorContents();

    // --- Instance variables ----------------------------------------------------
    private ProfilingSettings settings;
    private Lookup.Provider project;
    private boolean isAttach;
    private boolean isModify;
    private boolean isPreset;

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public SettingsContainerPanel.Contents getCPUConfigurator() {
        return cpuContents;
    }

    public void setContext(Lookup.Provider project, FileObject profiledFile, boolean isAttach, boolean isModify, boolean enableOverride) {
        this.project = project;
        this.profiledFile = profiledFile;
        this.isAttach = isAttach;
        this.isModify = isModify;

        monitorContents.setContext(project, profiledFile, isAttach, isModify);
        cpuContents.setContext(project, profiledFile, isAttach, isModify);
        memoryContents.setContext(project, profiledFile, isAttach, isModify);
    }

    public JPanel getCustomSettingsPanel() {
        return null;
    }

    public SettingsContainerPanel.Contents getMemoryConfigurator() {
        return memoryContents;
    }

    // --- SettingsConfigurator implementation -----------------------------------
    public SettingsContainerPanel.Contents getMonitorConfigurator() {
        return monitorContents;
    }

    // Initializes UI according to the settings
    public void setSettings(ProfilingSettings settings) {
        this.settings = settings;
        isPreset = settings.isPreset();

        if (ProfilingSettings.isMonitorSettings(settings)) {
            monitorContents.setSettings(settings);
        } else if (ProfilingSettings.isCPUSettings(settings)) {
            cpuContents.setSettings(settings);
        } else if (ProfilingSettings.isMemorySettings(settings)) {
            memoryContents.setSettings(settings);
        }
    }

    public ProfilingSettings getSettings() {
        return settings;
    }

    public ProfilingSettings createFinalSettings() {
        //////    return getSettings(); // TODO: create settings to be used for profiling
        if (ProfilingSettings.isMonitorSettings(settings)) {
            return monitorContents.createFinalSettings();
        } else if (ProfilingSettings.isCPUSettings(settings)) {
            return cpuContents.createFinalSettings();
        } else if (ProfilingSettings.isMemorySettings(settings)) {
            return memoryContents.createFinalSettings();
        }

        return null;
    }

    public void loadCustomSettings(Properties properties) {
    }

    public void reset() {
        settings = null;
        project = null;
        profiledFile = null;
        isAttach = false;
        isModify = false;
        isPreset = false;
        monitorContents.reset();
        cpuContents.reset();
        memoryContents.reset();
    }

    public void storeCustomSettings(Properties properties) {
    }

    // Updates settings according to the UI
    public void synchronizeSettings() {
        if (ProfilingSettings.isMonitorSettings(settings)) {
            monitorContents.synchronizeSettings();
        } else if (ProfilingSettings.isCPUSettings(settings)) {
            cpuContents.synchronizeSettings();
        } else if (ProfilingSettings.isMemorySettings(settings)) {
            memoryContents.synchronizeSettings();
        }
    }

    protected boolean isAttach() {
        return isAttach;
    }

    protected boolean isModify() {
        return isModify;
    }

    protected boolean isPreset() {
        return isPreset;
    }

    protected FileObject getProfiledFile() {
        return profiledFile;
    }

    // --- Protected interface ---------------------------------------------------
    protected Lookup.Provider getProject() {
        return project;
    }
}
