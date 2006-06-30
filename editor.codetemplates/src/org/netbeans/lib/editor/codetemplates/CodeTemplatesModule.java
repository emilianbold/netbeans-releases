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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.lib.editor.codetemplates;
import javax.swing.Action;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsNames;
import org.netbeans.editor.SettingsUtil;
import org.openide.modules.ModuleInstall;


/**
 * Module installation class for editor.
 *
 * @author Miloslav Metelka
 */
public class CodeTemplatesModule extends ModuleInstall {

    public void restored () {
        synchronized (Settings.class) {
            SettingsUtil.updateListSetting(BaseKit.class,
                    SettingsNames.CUSTOM_ACTION_LIST,
                    new Object[] { AbbrevKitInstallAction.INSTANCE }
            );
            SettingsUtil.updateListSetting(BaseKit.class,
                    SettingsNames.KIT_INSTALL_ACTION_NAME_LIST,
                    new Object[] { AbbrevKitInstallAction.INSTANCE.getValue(Action.NAME) }
            );
            Settings.addInitializer(new AbbrevSettingsInitializer());
        }
        
    }
    
    /**
     * Called when all modules agreed with closing and the IDE will be closed.
     */
    public void close() {
        finish();
    }
    
    /**
     * Called when module is uninstalled.
     */
    public void uninstalled() {
        finish();
    }
    
    private void finish() {
        Settings.removeInitializer(AbbrevSettingsInitializer.NAME);
        Settings.reset();
        
        // Go through components and clear the AbbrevDetection.class property
    }

    private static final class AbbrevSettingsInitializer extends Settings.AbstractInitializer {
        
        static final String NAME = "bookmarks-settings-initializer"; // NOI18N
        
        AbbrevSettingsInitializer() {
            super(NAME);
        }

        public void updateSettingsMap(Class kitClass, java.util.Map settingsMap) {
            if (kitClass == BaseKit.class) {
                SettingsUtil.updateListSetting(settingsMap,
                        SettingsNames.CUSTOM_ACTION_LIST,
                        new Object[] { AbbrevKitInstallAction.INSTANCE }
                );
                SettingsUtil.updateListSetting(settingsMap,
                        SettingsNames.KIT_INSTALL_ACTION_NAME_LIST,
                        new Object[] { AbbrevKitInstallAction.INSTANCE.getValue(Action.NAME) }
                );
            }
        }
        
    }
    
}
