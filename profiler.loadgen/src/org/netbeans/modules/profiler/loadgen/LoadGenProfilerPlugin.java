/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2015 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.profiler.loadgen;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingUtilities;
import org.netbeans.modules.loadgenerator.api.EngineManager;
import org.netbeans.modules.loadgenerator.spi.Engine;
import org.netbeans.modules.profiler.spi.LoadGenPlugin;
import org.netbeans.modules.profiler.v2.ProfilerPlugin;
import org.netbeans.modules.profiler.v2.SessionStorage;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jiri Sedlacek
 */
@NbBundle.Messages({
    "LoadGenerator_name=Load Generator",
    "LoadGenerator_enabled=&Enabled",
    "LoadGenerator_configure=&Configure..."
})
final class LoadGenProfilerPlugin extends ProfilerPlugin {
    
    private static final String PREFIX = "LoadGenerator."; // NOI18N
    private static final String PROP_ENABLED = PREFIX + "PROP_ENABLED"; // NOI18N
    private static final String PROP_SELECTED = PREFIX + "PROP_SELECTED"; // NOI18N
    private static final String PROP_CUSTOM = PREFIX + "PROP_CUSTOM"; // NOI18N
    private static final String PROP_MODE = PREFIX + "PROP_MODE"; // NOI18N
    
    private final LoadGenPlugin plugin;
    private final SessionStorage storage;
    private final Lookup.Provider project;
    
    private final boolean global;
    
    private int mode;
    private String[] scripts;
    private String selectedScript;
    
    private int commonPathLength;
    
    private boolean sessionRunning;
    
    
    LoadGenProfilerPlugin(LoadGenPlugin plugin, Lookup.Provider project, SessionStorage storage) {
        super(Bundle.LoadGenerator_name());
        
        this.plugin = plugin;
        this.storage = storage;
        this.project = project;
        
        this.global = project == null;
        
        mode = readMode();
        
        if (!global && (mode == 0 || mode == -1)) { 
            scripts = readProjectScripts();
            if (scripts.length > 0) mode = 0;
        }
        
        if (mode == 1 || mode == -1) { 
            scripts = readGlobalScripts();
            if (scripts.length > 0) mode = 1;
        }
        
        if (mode == 2 || mode == -1) { 
            scripts = readCustomScripts();
            if (scripts.length > 0) mode = 2;
        }
        
        if (scripts.length == 0) {
            mode = 2;
            commonPathLength = -1;
            selectedScript = null;
        } else {
            commonPathLength = mode == 0 ? commonPathLength(scripts) : -1;
            selectedScript = readSelected();
            if (selectedScript == null || !new File(selectedScript).isFile())
                selectedScript = scripts[0];
        }
        storeSelected(selectedScript);
    }
    
    
    private String[] readProjectScripts() {
        Collection<FileObject> scriptsFo = plugin.listScripts(project);
        String[] projectScripts = new String[scriptsFo.size()];
        int idx = 0;
        for (FileObject script : scriptsFo) {
            File scriptFile = FileUtil.normalizeFile(FileUtil.toFile(script));
            try {
                projectScripts[idx] = scriptFile.getCanonicalPath();
            } catch (IOException ex) {
                projectScripts[idx] = scriptFile.getAbsolutePath();
            }
            idx++;
        }
        Arrays.sort(projectScripts);
        return projectScripts;
    }
    
    private String[] readGlobalScripts() {
        Collection<FileObject> scriptsFo = plugin.listScripts(null);
        String[] globalScripts = new String[scriptsFo.size()];
        int idx = 0;
        for (FileObject script : scriptsFo) {
            File scriptFile = FileUtil.normalizeFile(FileUtil.toFile(script));
            try {
                globalScripts[idx] = scriptFile.getCanonicalPath();
            } catch (IOException ex) {
                globalScripts[idx] = scriptFile.getAbsolutePath();
            }
            idx++;
        }
        Arrays.sort(globalScripts);
        return globalScripts;
    }
    
    private String[] readCustomScripts() {
        String custom = readCustom();
        return custom == null ? new String[0] : new String[] { custom };
    }
    
    private Set<String> readSupportedExtensions() {
        EngineManager manager = Lookup.getDefault().lookup(EngineManager.class);
        Collection<Engine> engines = manager.findEngines();
        Set<String> extensions = new HashSet();
        for (Engine engine : engines)
            for (String ext : engine.getSupportedExtensions())
                extensions.add(ext.trim().toLowerCase());
        return extensions;
    }
    

    public void createMenu(JMenu menu) {
        if (scripts.length > 0) {
            JCheckBoxMenuItem enabledItem = new JCheckBoxMenuItem() {
                {
                    setEnabled(!sessionRunning && scripts.length > 0);
                    setSelected(scripts.length > 0 && readEnabled());
                }
                protected void fireActionPerformed(ActionEvent e) {
                    super.fireActionPerformed(e);
                    storeEnabled(isSelected());
                }
            };
            Mnemonics.setLocalizedText(enabledItem, Bundle.LoadGenerator_enabled());
            menu.add(enabledItem);

            menu.addSeparator();
            
            for (final String script : scripts) menu.add(new JRadioButtonMenuItem(scriptName(script)) {
                {
                    setEnabled(!sessionRunning);
                    setSelected(script.equals(selectedScript));
                    setToolTipText(script);
                }
                protected void fireActionPerformed(ActionEvent e) {
                    selectedScript = script;
                    storeSelected(selectedScript);
                }
            });
            
            menu.addSeparator();
        }
        
        JMenuItem configureItem = new JMenuItem() {
            {
                setEnabled(!sessionRunning);
            }
            protected void fireActionPerformed(ActionEvent e) {
                super.fireActionPerformed(e);
                configureScripts();
            }
        };
        Mnemonics.setLocalizedText(configureItem, Bundle.LoadGenerator_configure());
        menu.add(configureItem);
        
        menu.add(configureItem);
    }
    
    private void configureScripts() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                LoadGenConfig config = new LoadGenConfig() {
                    int readMode() { return mode; }
                    String[] readProjectScripts() { return LoadGenProfilerPlugin.this.readProjectScripts(); }
                    String[] readGlobalScripts()  { return LoadGenProfilerPlugin.this.readGlobalScripts(); }
                    String[] readCustomScripts()  { return LoadGenProfilerPlugin.this.readCustomScripts(); }
                    Set<String> readSupportedExtensions() { return LoadGenProfilerPlugin.this.readSupportedExtensions(); }
                };
                boolean configured = config.configure(global);
                
                if (configured) {
                    // Use selected mode
                    mode = config.getMode();
                    storeMode(mode);
                }
                
                if (mode == 0) {
                    // Use refreshed project scripts
                    String[] newScripts = config.getProjectScripts();
                    if (newScripts != null) scripts = newScripts;
                } else if (mode == 1) {
                    // Use refreshed global scripts
                    String[] newScripts = config.getGlobalScripts();
                    if (newScripts != null) scripts = newScripts;
                } else if (mode == 2) {
                    // Set new custom script if submitted or clear invalid custom script
                    String[] newScripts = config.getCustomScripts();
                    if (newScripts != null && (configured || newScripts.length == 0)) {
                        scripts = newScripts;
                        storeCustom(scripts.length == 0 ? null : scripts[0]);
                    }
                }
                commonPathLength = mode == 0 ? commonPathLength(scripts) : -1;
                
                boolean selectedFound = false;
                for (String script : scripts)
                    if (script.equals(selectedScript)) {
                        selectedFound = true;
                        break;
                    }
                if (!selectedFound && scripts.length > 0) {
                    selectedScript = scripts[0];
                    storeSelected(selectedScript);
                }
            }
        });
    }
    
    
    private String scriptName(String script) {
        return commonPathLength == -1 ? new File(script).getName() :
                                   script.substring(commonPathLength);
    }
    
    private static int commonPathLength(String[] paths) {
        int plength = paths.length;
        if (plength == 0) return 0;
        
        int commonIdx = paths[0].lastIndexOf(File.separatorChar);
        if (commonIdx == -1) return 0;
        
        String common = paths[0].substring(0, commonIdx);
        if (plength == 1) return common.length() + 1;
        
        boolean cycle = true;
        while (cycle) {
            cycle = false;
            for (int i = 1; i < plength; i++) {
                if (!paths[i].startsWith(common)) {
                    commonIdx = common.lastIndexOf(File.separatorChar);
                    if (commonIdx == -1) return 0;
                    common = common.substring(0, commonIdx);
                    cycle = true;
                    break;
                }
            }
        }
        return common.length() + 1;
    }
    
    
    protected void sessionStarting() {
        sessionRunning = true;
    }
    
    protected void sessionStarted()  {
        if (scripts.length > 0 && readEnabled()) processor().post(new Runnable() {
            public void run() { plugin.start(selectedScript, LoadGenPlugin.Callback.NULL); }
        });
    }
    
    protected void sessionStopping() {
        if (scripts.length > 0 && readEnabled()) processor().post(new Runnable() {
            public void run() { if (selectedScript != null) plugin.stop(selectedScript); }
        });
    }
    
    protected void sessionStopped() {
        sessionRunning = false;
    }
    
    
    private void storeEnabled(boolean enabled) {
        storage.storeFlag(PROP_ENABLED, enabled ? null : Boolean.FALSE.toString());
    }
    
    private boolean readEnabled() {
        return Boolean.parseBoolean(storage.readFlag(PROP_ENABLED, Boolean.TRUE.toString()));
    }
    
    private void storeSelected(String selected) {
        storage.storeFlag(PROP_SELECTED, selected);
    }
    
    private String readSelected() {
        return storage.readFlag(PROP_SELECTED, null);
    }
    
    private void storeCustom(String custom) {
        storage.storeFlag(PROP_CUSTOM, custom);
    }
    
    private String readCustom() {
        return storage.readFlag(PROP_CUSTOM, null);
    }
    
    private void storeMode(int mode) {
        storage.storeFlag(PROP_MODE, mode == -1 ? null : Integer.toString(mode));
    }
    
    private int readMode() {
        return Integer.parseInt(storage.readFlag(PROP_MODE, "-1")); // NOI18N
    }
    
    
    private static RequestProcessor PROCESSOR;
    private static RequestProcessor processor() {
        if (PROCESSOR == null) PROCESSOR = new RequestProcessor("Load Generator Processor"); // NOI18N
        return PROCESSOR;
    }
    
    
    @ServiceProvider(service=ProfilerPlugin.Provider.class, position=100)
    public static final class Provider extends ProfilerPlugin.Provider {
        public ProfilerPlugin createPlugin(Lookup.Provider project, SessionStorage storage) {
            LoadGenPlugin plugin = Lookup.getDefault().lookup(LoadGenPlugin.class);
            return plugin == null ? null : new LoadGenProfilerPlugin(plugin, project, storage);
        }
    }
    
}
