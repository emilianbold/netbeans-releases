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
package org.netbeans.modules.editor.macros;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.KeyStroke;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.MultiKeyBinding;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsNames;
import org.netbeans.editor.SettingsUtil;
import org.netbeans.modules.editor.lib.KitsTracker;
import org.netbeans.modules.editor.macros.storage.MacroDescription;
import org.netbeans.modules.editor.macros.storage.MacrosStorage;
import org.netbeans.modules.editor.settings.storage.api.EditorSettingsStorage;
import org.openide.modules.ModuleInstall;

/**
 * Manages a module's lifecycle. Remember that an installer is optional and
 * often not needed at all.
 */
public final class Installer extends ModuleInstall {
    
    private static final Logger LOG = Logger.getLogger(Installer.class.getName());
    
    public @Override void restored() {
        Settings.addInitializer(new MacrosSettingsInitializer());
    }
    
    public @Override void close() {
        finish();
    }
    
    public @Override void uninstalled() {
        finish();
    }
    
    private void finish() {
        Settings.removeInitializer(MacrosSettingsInitializer.NAME);
        Settings.reset();
    }

    private static final class MacrosSettingsInitializer extends Settings.AbstractInitializer {
        
        static final String NAME = "macros-settings-initializer"; // NOI18N
        
        MacrosSettingsInitializer() {
            super(NAME);
        }

        public void updateSettingsMap(Class kitClass, java.util.Map settingsMap) {
            Map<String, Action> actions = new HashMap<String, Action>();
            List<MultiKeyBinding> keybindings = new ArrayList<MultiKeyBinding>();
            
            collectMacroActions(kitClass, actions, keybindings);
            
            if (kitClass == BaseKit.class) {
                actions.put(BaseKit.startMacroRecordingAction, new MacroDialogSupport.StartMacroRecordingAction());
                actions.put(BaseKit.stopMacroRecordingAction, new MacroDialogSupport.StopMacroRecordingAction());
            }
            
            if (!actions.isEmpty()) {
                SettingsUtil.updateListSetting(settingsMap, SettingsNames.CUSTOM_ACTION_LIST, actions.values().toArray());
            }
            if (!keybindings.isEmpty()) {
                List list = new ArrayList(keybindings.size());
                for(MultiKeyBinding mkb : keybindings) {
                    list.add(new org.netbeans.editor.MultiKeyBinding(
                        mkb.getKeyStrokeList().toArray(new KeyStroke[mkb.getKeyStrokeList().size()]), 
                        mkb.getActionName()));
                }
                settingsMap.put(SettingsNames.KEY_BINDING_LIST, list);
                
//                new Throwable("macro keybindings added").printStackTrace();
            }
        }

        private void collectMacroActions(Class kitClass, Map<String, Action> actions, List<MultiKeyBinding> keybindings) {
            String mimeType = KitsTracker.getInstance().findMimeType(kitClass);
//            LOG.warning("~~~ " + kitClass + " -> " + mimeType);
            
            if (mimeType != null) {
                EditorSettingsStorage<String, MacroDescription> ess = EditorSettingsStorage.<String, MacroDescription>get(MacrosStorage.ID);
                
                try {
                    Map<String, MacroDescription> macros = ess.load(MimePath.EMPTY, null, false);
                    collectMacrosWithShortcuts(macros, actions, keybindings);
                } catch (IOException ioe) {
                    LOG.log(Level.WARNING, null, ioe);
                }

                try {
                    Map<String, MacroDescription> macros = ess.load(MimePath.parse(mimeType), null, false);
                    collectMacrosWithShortcuts(macros, actions, keybindings);
                } catch (IOException ioe) {
                    LOG.log(Level.WARNING, null, ioe);
                }
            }
        }
        
        private void collectMacrosWithShortcuts(Map<String, MacroDescription> macros, Map<String, Action> actions, List<MultiKeyBinding> keybindings) {
            for(MacroDescription macro : macros.values()) {
                List<? extends MultiKeyBinding> shortcuts = macro.getShortcuts();
                if (shortcuts != null && shortcuts.size() > 0) {
                    // Create an action to run the macro
                    MacroDialogSupport.RunMacroAction action = new MacroDialogSupport.RunMacroAction(macro.getName());
                    String actionName = (String) action.getValue(Action.NAME);
                    actions.put(actionName, action);
                    
                    // Bind all shortcuts to the action
                    keybindings.addAll(shortcuts);
                    
//                    LOG.warning("~~~ adding RunMacroAction: '" + action + "' bound to [" + shortcuts + "]");
                }
            }
        }
    } // End of MacrosSettingsInitializer class
    
}
