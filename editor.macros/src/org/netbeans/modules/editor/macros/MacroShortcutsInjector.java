/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.editor.macros;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.KeyStroke;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.MultiKeyBinding;
import org.netbeans.modules.editor.macros.storage.MacroDescription;
import org.netbeans.modules.editor.macros.storage.MacrosStorage;
import org.netbeans.modules.editor.settings.storage.api.EditorSettingsStorage;
import org.netbeans.modules.editor.settings.storage.spi.StorageFilter;
import org.openide.util.WeakListeners;

/**
 *
 * @author Vita Stejskal
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.editor.settings.storage.spi.StorageFilter.class)
public final class MacroShortcutsInjector extends StorageFilter<Collection<KeyStroke>, MultiKeyBinding> implements PropertyChangeListener {

    public static void refreshShortcuts() {
        assert instance != null;
        LOG.fine("Shortcuts refresh forced, notifying 'Keybindings' storage..."); //NOI18N
        instance.notifyChanges();
    }
    
    public MacroShortcutsInjector() {
        super("Keybindings"); //NOI18N
        assert instance == null;
        instance = this;
    }
    
    // ------------------------------------------------------------------------
    // StorageFilter implementation
    // ------------------------------------------------------------------------
    
    @Override
    public void afterLoad(Map<Collection<KeyStroke>, MultiKeyBinding> map, MimePath mimePath, String profile, boolean defaults) {
        Map<String, MacroDescription> macros = new HashMap<String, MacroDescription>();
        collectMacroActions(mimePath, macros);
        
        for(MacroDescription macro : macros.values()) {
            List<? extends MultiKeyBinding> shortcuts = macro.getShortcuts();
            for(MultiKeyBinding shortcut : shortcuts) {
                Collection<KeyStroke> keys = shortcut.getKeyStrokeList();

                // A macro shortcut never replaces shortcuts for ordinary editor actions
                if (!map.containsKey(keys)) {
                    map.put(keys, shortcut);
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine("afterLoad: injecting " + keys + " for macro '" //NOI18N
                            + macro.getName() + "'; mimePath='" + mimePath.getPath() + "'"); //NOI18N
                    }
                } else {
                    LOG.warning("Shortcut " + keys + " is bound to '" + map.get(keys).getActionName() //NOI18N
                        + "' for '" + mimePath.getPath() + "' and will not be assigned to '" + macro.getName() + "' macro!"); //NOI18N
                }
            }
        }
    }

    @Override
    public void beforeSave(Map<Collection<KeyStroke>, MultiKeyBinding> map, MimePath mimePath, String profile, boolean defaults) {
        Set<Collection<KeyStroke>> keysToFilterOut = new HashSet<Collection<KeyStroke>>();
        
        for(Collection<KeyStroke> keys : map.keySet()) {
            MultiKeyBinding shortcut = map.get(keys);
            if (shortcut.getActionName().equals(MacroDialogSupport.RunMacroAction.runMacroAction)) {
                keysToFilterOut.add(keys);
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("beforeSave: filtering out macro shortcut " + keys + "; mimePath='" + mimePath.getPath() + "'"); //NOI18N
                }
            }
        }
        
        map.keySet().removeAll(keysToFilterOut);
    }

    // ------------------------------------------------------------------------
    // PropertyChangeListener implementation
    // ------------------------------------------------------------------------
    
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName() == null || EditorSettingsStorage.PROP_DATA.equals(evt.getPropertyName())) {
            LOG.fine("Macros storage changed, notifying 'Keybindings' storage...");
            notifyChanges();
        }
    }
    
    // ------------------------------------------------------------------------
    // private implementation
    // ------------------------------------------------------------------------
    
    private static final Logger LOG = Logger.getLogger(MacroShortcutsInjector.class.getName());
    
    private static MacroShortcutsInjector instance = null;
    private EditorSettingsStorage<String, MacroDescription> storage = null;
    
    private void collectMacroActions(MimePath mimePath, Map<String, MacroDescription> macros) {
        if (storage == null) {
            storage = EditorSettingsStorage.<String, MacroDescription>get(MacrosStorage.ID);
            storage.addPropertyChangeListener(WeakListeners.propertyChange(this, storage));
        }

        try {
            macros.putAll(storage.load(mimePath, null, false));
        } catch (IOException ioe) {
            LOG.log(Level.WARNING, null, ioe);
        }
    }

}
