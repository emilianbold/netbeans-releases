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

import org.netbeans.lib.profiler.common.ProfilingSettings;
import org.netbeans.lib.profiler.common.ProfilingSettingsPresets;
import org.openide.ErrorManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import org.netbeans.modules.profiler.api.ProfilingSettingsManager.ProfilingSettingsDescriptor;
import org.netbeans.modules.profiler.api.project.ProjectStorage;
import org.netbeans.modules.profiler.api.project.ProfilingSettingsSupport;
import org.netbeans.modules.profiler.api.project.ProfilingSettingsSupport.SettingsCustomizer;
import org.netbeans.modules.profiler.spi.ProfilingSettingsManagerProvider;
import org.openide.filesystems.FileSystem;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;


/**
 *
 * @author Jiri Sedlacek
 */
@ServiceProvider(service=ProfilingSettingsManagerProvider.class)
public class NBProfilingSettingsManager extends ProfilingSettingsManagerProvider {
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    // --- Constants declaration -------------------------------------------------
    private static final String PROFILING_SETTINGS_STORAGE_FILENAME = "configurations"; // NOI18N
    private static final String PROFILING_SETTINGS_STORAGE_FILEEXT = "xml"; // NOI18N
    private static final String PROP_LAST_SELECTED_SETTINGS_INDEX = "profiler.settings.lastselected"; // NOI18N
    private static final int DEFAULT_SETTINGS_COUNT = 3; // Nimber of required default settings, currently 3: Monitor, CPU, Memory

    // --- Instance variables declaration ----------------------------------------

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    // --- Private implementation ------------------------------------------------
    public NBProfilingSettingsManager() {
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    // --- Public interface ------------------------------------------------------
    @Override
    public ProfilingSettingsDescriptor getProfilingSettings(Lookup.Provider project) {
        final List<ProfilingSettings> profilingSettings = new LinkedList();
        final int[] lastSelectedProfilingSettingsIndex = new int[] { -1 };

        try {
            // get settings folder used for resolving filesystem for atomic action
            FileObject settingsStorage = ProjectStorage.getSettingsFolder(project, false);
            if (settingsStorage != null) {
                // make final copies for atomic action
                final Lookup.Provider projectF = project;
//                final ProfilingSettings[] profilingSettingsF = profilingSettings;
//                final ProfilingSettings lastSelectedProfilingSettingsF = lastSelectedProfilingSettings;
                
                // access configurations.xml in atomic action
                FileSystem fs = settingsStorage.getFileSystem();
                fs.runAtomicAction(new FileSystem.AtomicAction() {
                    public void run() throws IOException {
                        final FileObject profilingSettingsStorage = getProfilingSettingsStorage(projectF);

                        if (profilingSettingsStorage != null) {
                            Properties properties = loadSettings(profilingSettingsStorage);

                            int index = 0;

                            while (properties.getProperty(index + "_" + ProfilingSettings.PROP_SETTINGS_NAME) != null) { // NOI18N

                                ProfilingSettings settings = new ProfilingSettings();
                                settings.load(properties, Integer.toString(index) + "_"); // NOI18N

                                if (settings != null) {
                                    profilingSettings.add(settings);
                                }

                                index++;
                            }

                            try {
                                lastSelectedProfilingSettingsIndex[0] = Integer.parseInt(properties.getProperty(PROP_LAST_SELECTED_SETTINGS_INDEX,
                                                                                                             "0")); // NOI18N
                            } catch (Exception e) {
                            }
                            
                            SettingsCustomizer customizer = ProfilingSettingsSupport.get(projectF).getSettingsCustomizer();
                            if (customizer != null) customizer.loadCustomSettings(properties);
                        }
                    }
                });
            }
        } catch (Exception e) {
            ErrorManager.getDefault().log(ErrorManager.ERROR, e.getMessage());
        }

        ProfilingSettings[] profilingSettingsArr = new ProfilingSettings[profilingSettings.size()];

        if (profilingSettingsArr.length < DEFAULT_SETTINGS_COUNT) {
            profilingSettingsArr = createDefaultSettings();
        } else {
            profilingSettings.toArray(profilingSettingsArr);
        }

        return new ProfilingSettingsDescriptor(profilingSettingsArr,
                                               (lastSelectedProfilingSettingsIndex[0] == -1) ? null
                                                                                          : profilingSettingsArr[lastSelectedProfilingSettingsIndex[0]]);
    }

    public ProfilingSettings createDuplicateSettings(ProfilingSettings originalSettings,
                                                     ProfilingSettings[] availableConfigurations) {
        return NewCustomConfiguration.createDuplicateConfiguration(originalSettings, availableConfigurations);
    }

    public ProfilingSettings createNewSettings(ProfilingSettings[] availableConfigurations) {
        return NewCustomConfiguration.createNewConfiguration(availableConfigurations);
    }

    public ProfilingSettings createNewSettings(int type, ProfilingSettings[] availableConfigurations) { // Use ProfilingSettings.getProfilingType() value

        return NewCustomConfiguration.createNewConfiguration(type, availableConfigurations);
    }

    public ProfilingSettings renameSettings(ProfilingSettings originalSettings, ProfilingSettings[] availableConfigurations) {
        return NewCustomConfiguration.renameConfiguration(originalSettings, availableConfigurations);
    }

    public void storeProfilingSettings(ProfilingSettings[] profilingSettings, ProfilingSettings lastSelectedProfilingSettings,
                                       Lookup.Provider project) {
        try {
            // ensure that default settings will be saved, should not happen
            if ((profilingSettings == null) || (profilingSettings.length < DEFAULT_SETTINGS_COUNT)) {
                profilingSettings = createDefaultSettings();
            }

            // first settings will be marked as lastSelectedProfilingSettings if not provided
            if (lastSelectedProfilingSettings == null) {
                lastSelectedProfilingSettings = profilingSettings[0];
            }
            
            // get settings folder used for resolving filesystem for atomic action
            FileObject settingsStorage = ProjectStorage.getSettingsFolder(project, true);
            if (settingsStorage == null) {
                ErrorManager.getDefault().log(ErrorManager.ERROR, "Cannot create project settings folder for " // NOI18N
                                              + project + ", settings cannot be saved."); // NOI18N
                return;
            }

            // make final copies for atomic action
            final Lookup.Provider projectF = project;
            final ProfilingSettings[] profilingSettingsF = profilingSettings;
            final ProfilingSettings lastSelectedProfilingSettingsF = lastSelectedProfilingSettings;
            
            // access configurations.xml in atomic action
            FileSystem fs = settingsStorage.getFileSystem();
            fs.runAtomicAction(new FileSystem.AtomicAction() {
                public void run() throws IOException {
                    // store all settings in one file, add information about lastSelectedProfilingSettings
                    FileObject profilingSettingsStorage = getProfilingSettingsStorage(projectF);

                    if (profilingSettingsStorage == null) {
                        profilingSettingsStorage = createProfilingSettingsStorage(projectF);
                    }

                    if (profilingSettingsStorage != null) { // should not happen

                        Properties properties = new Properties();
                        int lastSelectedProfilingSettingsIndex = -1;

                        for (int i = 0; i < profilingSettingsF.length; i++) {
                            ProfilingSettings settings = profilingSettingsF[i];

                            if (settings == lastSelectedProfilingSettingsF) {
                                lastSelectedProfilingSettingsIndex = i;
                            }

                            settings.store(properties, Integer.toString(i) + "_"); // NOI18N
                        }

                        properties.put(PROP_LAST_SELECTED_SETTINGS_INDEX, Integer.toString(lastSelectedProfilingSettingsIndex));

                        SettingsCustomizer customizer = ProfilingSettingsSupport.get(projectF).getSettingsCustomizer();
                        if (customizer != null) customizer.storeCustomSettings(properties);

                        storeSettings(profilingSettingsStorage, properties);
                    }
                }
            });
        } catch (Exception e) {
            ErrorManager.getDefault().log(ErrorManager.ERROR, e.getMessage());
        }
    }

    private FileObject getProfilingSettingsStorage(Lookup.Provider project)
                                            throws IOException {  
        FileObject projectSettingsFolder = ProjectStorage.getSettingsFolder(project, true);
        FileObject profilingSettingsStorage = projectSettingsFolder.getFileObject(PROFILING_SETTINGS_STORAGE_FILENAME,
                                                                                  PROFILING_SETTINGS_STORAGE_FILEEXT);

        return profilingSettingsStorage;
    }

    private ProfilingSettings[] createDefaultSettings() {
        return new ProfilingSettings[] {
                   ProfilingSettingsPresets.createMonitorPreset(), ProfilingSettingsPresets.createCPUPreset(),
                   ProfilingSettingsPresets.createMemoryPreset()
               };
    }

    private FileObject createProfilingSettingsStorage(Lookup.Provider project)
                                               throws IOException {   
        FileObject projectSettingsFolder = ProjectStorage.getSettingsFolder(project, true);
        FileObject profilingSettingsStorage = projectSettingsFolder.createData(PROFILING_SETTINGS_STORAGE_FILENAME,
                                                                               PROFILING_SETTINGS_STORAGE_FILEEXT);

        return profilingSettingsStorage;
    }

    private Properties loadSettings(final FileObject storage)
                             throws IOException {
        Properties properties = new Properties();

        final InputStream is = storage.getInputStream();
        final BufferedInputStream bis = new BufferedInputStream(is);
        properties.loadFromXML(bis);
        bis.close();

        return properties;
    }

    private void storeSettings(final FileObject storage, final Properties properties)
                        throws IOException {
        FileLock lock = null;

        try {
            lock = storage.lock();

            final OutputStream os = storage.getOutputStream(lock);
            final BufferedOutputStream bos = new BufferedOutputStream(os);
            properties.storeToXML(os, null);
            bos.close();
        } finally {
            if (lock != null) {
                lock.releaseLock();
            }
        }
    }
}
