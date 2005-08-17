/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.db.sql.editor;
import org.netbeans.editor.LocaleSupport;
import org.netbeans.editor.Settings;
import org.openide.modules.ModuleInstall;
import org.openide.util.NbBundle;


/**
 * Instalation class of module properties syntax.
 * 
 * @author Jesse Beaumont based on code by
 * Petr Jiricka, Libor Kramolis, Jesse Glick
 */
public class RestoreColoring extends ModuleInstall {
    
    /** 
     * <code>Localizer</code> passed to editor. 
     */
    private static LocaleSupport.Localizer localizer;

    /** 
     * Registers properties editor, installs options and copies settings. 
     * Overrides superclass method.  
     */
    public void restored() {
        addInitializer();
        installOptions();
    }

    /** 
     * Uninstalls properties options. 
     * And cleans up editor settings copy. 
     * Overrides superclass method. 
     */
    public void uninstalled() {
        uninstallOptions();
    }

    /** 
     * Adds initializer and registers editor kit. 
     */
    public void addInitializer() {
        Settings.addInitializer(new SQLSettingsInitializer());
    }

    /** 
     * Installs properties editor and print options. 
     */
    public void installOptions() {
        // Adds localizer.
        LocaleSupport.addLocalizer(localizer = new LocaleSupport.Localizer() {
            public String getString(String key) {
                return NbBundle.getMessage(RestoreColoring.class, key);
            }
        });
    }

    /** Uninstalls properties editor and print options. */
    public void uninstallOptions() {
        // remove localizer
        LocaleSupport.removeLocalizer(localizer);
    }
    
}
