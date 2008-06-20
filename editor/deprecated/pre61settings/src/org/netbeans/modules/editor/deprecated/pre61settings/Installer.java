/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.editor.deprecated.pre61settings;

import org.netbeans.editor.LocaleSupport;
import org.netbeans.modules.editor.NbLocalizer;
import org.netbeans.modules.editor.options.AllOptions;
import org.netbeans.modules.editor.options.AllOptionsFolder;
import org.openide.modules.ModuleInstall;

/**
 * Manages a module's lifecycle. Remember that an installer is optional and
 * often not needed at all.
 */
public class Installer extends ModuleInstall {

    public @Override void restored() {
        LocaleSupport.addLocalizer(new NbLocalizer(AllOptions.class));
    }
    
    public @Override void uninstalled() {

        AllOptionsFolder.unregisterModuleRegListener();
    }
    
}
