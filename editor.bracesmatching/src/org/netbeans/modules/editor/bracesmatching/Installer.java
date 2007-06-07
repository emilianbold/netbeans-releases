/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.editor.bracesmatching;

import org.netbeans.editor.BaseKit;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsNames;
import org.netbeans.editor.SettingsUtil;
import org.openide.modules.ModuleInstall;

/**
 * Manages a module's lifecycle. Remember that an installer is optional and
 * often not needed at all.
 */
public class Installer extends ModuleInstall {
    
    @Override
    public void restored() {
        Settings.addInitializer(new BracesMatcherSettingsInitializer());
        Settings.reset();
    }

    @Override
    public void uninstalled() {
        die();
    }

    @Override
    public void close() {
        die();
    }
    
    private void die() {
        Settings.removeInitializer(BracesMatcherSettingsInitializer.NAME);
        Settings.reset();
    }
    
    private static final class BracesMatcherSettingsInitializer extends Settings.AbstractInitializer {
        
        static final String NAME = "bracesmatcher-settings-initializer"; // NOI18N
        
        BracesMatcherSettingsInitializer() {
            super(NAME);
        }

        public void updateSettingsMap(Class kitClass, java.util.Map settingsMap) {
            if (kitClass == BaseKit.class) {
                SettingsUtil.updateListSetting(settingsMap,
                    SettingsNames.CUSTOM_ACTION_LIST,new Object[] { 
//                        new ControlPanelAction(),
                        new BracesMatchAction(false), // an ordinary navigation
                        new BracesMatchAction(true)   // navigates and selects a block
                       // navigates and selects a block
                    }
                );
            }
        }
        
    } // End of BracesMatcherSettingsInitializer class
    
}
