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
import java.util.Collection;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
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
    "LoadGenerator_noProjectScripts=No scripts found in project folder",
    "LoadGenerator_noServicesScripts=No scripts found in Services | Load Generators",
    "LoadGenerator_currentlyRunning=Currently running {0}",
    "LoadGenerator_selectedScript=&Script: {0}"
})
final class LoadGenProfilerPlugin extends ProfilerPlugin {
    
    private static final String PREFIX = "LoadGenerator."; // NOI18N
    private static final String PROP_ENABLED = PREFIX + "PROP_ENABLED"; // NOI18N
    private static final String PROP_SELECTED = PREFIX + "PROP_SELECTED"; // NOI18N
    
    private final LoadGenPlugin plugin;
    private final SessionStorage storage;
    
    private final boolean global;
    
    private final String[] scripts;
    private String selectedScript;
    
    private final int commonPathLength;
    
    private boolean sessionRunning;
    
    
    LoadGenProfilerPlugin(LoadGenPlugin plugin, Lookup.Provider project, SessionStorage storage) {
        super(Bundle.LoadGenerator_name());
        
        this.plugin = plugin;
        this.storage = storage;
        
        this.global = project == null;
        
        Collection<FileObject> scriptsFo = plugin.listScripts(project);
        scripts = new String[scriptsFo.size()];
        int idx = 0;
        for (FileObject script : scriptsFo) {
            File scriptFile = FileUtil.normalizeFile(FileUtil.toFile(script));
            try {
                scripts[idx] = scriptFile.getCanonicalPath();
            } catch (IOException ex) {
                scripts[idx] = scriptFile.getAbsolutePath();
            }
            idx++;
        }
        
        if (scripts.length > 0) {
            selectedScript = readSelected();
            if (selectedScript == null || !new File(selectedScript).isFile())
                selectedScript = scripts[0];
            commonPathLength = commonPathLength(scripts);
        } else {
            commonPathLength = 0;
        }
    }

    public void createMenu(JMenu menu) {
        if (scripts.length == 0) {
            String msg = global ? Bundle.LoadGenerator_noServicesScripts() :
                                  Bundle.LoadGenerator_noProjectScripts();
            JMenuItem noItems = new JMenuItem(msg);
            noItems.setEnabled(false);
            menu.add(noItems);
        } else if (sessionRunning) {
            JMenuItem noItems = new JMenuItem(Bundle.LoadGenerator_currentlyRunning(scriptName(selectedScript)));
            noItems.setEnabled(false);
            menu.add(noItems);
        } else {
            JCheckBoxMenuItem enabledItem = new JCheckBoxMenuItem() {
                protected void fireActionPerformed(ActionEvent e) {
                    super.fireActionPerformed(e);
                    storeEnabled(isSelected());
                }
            };
            Mnemonics.setLocalizedText(enabledItem, Bundle.LoadGenerator_enabled());
            enabledItem.setSelected(readEnabled());
            enabledItem.setEnabled(scripts.length > 0);
            menu.add(enabledItem);

            menu.addSeparator();
            
            if (scripts.length == 1) {
                JMenuItem oneItem = new JMenuItem();
                Mnemonics.setLocalizedText(oneItem, Bundle.LoadGenerator_selectedScript(scriptName(selectedScript)));
                oneItem.setEnabled(false);
                menu.add(oneItem);
            } else {
                JMenu scriptsMenu = new JMenu();
                Mnemonics.setLocalizedText(scriptsMenu, Bundle.LoadGenerator_selectedScript(scriptName(selectedScript)) + "  "); // NOI18N
                for (final String script : scripts) scriptsMenu.add(new JRadioButtonMenuItem(scriptName(script)) {
                    {
                        setSelected(script.equals(selectedScript));
                    }
                    protected void fireActionPerformed(ActionEvent e) {
                        selectedScript = script;
                        storeSelected(selectedScript);
                    }
                });
                menu.add(scriptsMenu);
            }
        }
    }
    
    
    private String scriptName(String script) {
        return script.substring(commonPathLength);
    }
    
    private static int commonPathLength(String[] paths) {
        int plength = paths.length;
        if (plength == 0) return 0;
        
        int commonIdx = paths[0].lastIndexOf(File.separatorChar);
        if (commonIdx == -1) return 0;
        
        String common = paths[0].substring(0, commonIdx);
        if (plength == 1) return common.length() + 1;
        
        while (true) {
            for (int i = 1; i < plength; i++) {
                if (!paths[i].startsWith(common)) {
                    commonIdx = common.lastIndexOf(File.separatorChar);
                    if (commonIdx == -1) return 0;
                    common = common.substring(0, commonIdx);
                }
            }
            return common.length() + 1;
        }
    }
    
    
    protected void sessionStarted()  {
        if (!readEnabled()) return;
        sessionRunning = true;
        processor().post(new Runnable() {
            public void run() {
                plugin.start(selectedScript, LoadGenPlugin.Callback.NULL);
            }
        });
    }
    
    protected void sessionStopping() {
        if (!readEnabled()) return;
        sessionRunning = false;
        processor().post(new Runnable() {
            public void run() {
                if (selectedScript != null) plugin.stop(selectedScript);
            }
        });
    }
    
    
    private void storeEnabled(boolean enabled) {
        storage.storeFlag(PROP_ENABLED, enabled ? null : Boolean.FALSE.toString());
    }
    
    private boolean readEnabled() {
        return scripts.length > 0 && Boolean.parseBoolean(storage.readFlag(PROP_ENABLED, Boolean.TRUE.toString()));
    }
    
    private void storeSelected(String selected) {
        storage.storeFlag(PROP_SELECTED, selected);
    }
    
    private String readSelected() {
        return storage.readFlag(PROP_SELECTED, null);
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
