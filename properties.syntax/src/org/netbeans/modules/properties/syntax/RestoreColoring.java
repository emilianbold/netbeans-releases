/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.properties.syntax;

import java.util.MissingResourceException;

import org.netbeans.editor.LocaleSupport;
import org.netbeans.editor.Settings;

import org.openide.modules.ModuleInstall;
import org.openide.text.PrintSettings;
import org.openide.util.NbBundle;
import org.openide.util.SharedClassObject;

/**
 * Instalation class of module properties syntax.
 * 
 * @author Petr Jiricka, Libor Kramolis, Jesse Glick
 */
public class RestoreColoring extends ModuleInstall {

    /** <code>Localizer</code> passed to editor. */
    private static LocaleSupport.Localizer localizer;

    /** Registers properties editor, installs options and copies settings. Overrides superclass method.  */
    public void restored() {
        addInitializer();
        installOptions();
    }

    /** Uninstalls properties options. And cleans up editor settings copy. Overrides superclass method. */
    public void uninstalled() {
        uninstallOptions();
    }

    /** Adds initializer and registers editor kit. */
    public void addInitializer() {
        Settings.addInitializer(new PropertiesSettingsInitializer());
    }

    /** Installs properties editor and print options. */
    public void installOptions() {
        PrintSettings printSettings = (PrintSettings)SharedClassObject.findObject(PrintSettings.class, true);
        printSettings.addOption((PropertiesPrintOptions)SharedClassObject.findObject(PropertiesPrintOptions.class, true));
        
        
        // Adds localizer.
        LocaleSupport.addLocalizer(localizer = new LocaleSupport.Localizer() {
            public String getString(String key) {
                try {
                    return NbBundle.getBundle(RestoreColoring.class).getString(key);
                } catch(MissingResourceException mre) {
                    return null;
                }
            }
        });
    }

    /** Uninstalls properties editor and print options. */
    public void uninstallOptions() {
        PropertiesPrintOptions propertiesPrintOptions = (PropertiesPrintOptions)SharedClassObject.findObject(PropertiesPrintOptions.class, false);
        if(propertiesPrintOptions != null) {
            PrintSettings printSettings = (PrintSettings)SharedClassObject.findObject(PrintSettings.class, true);
            printSettings.removeOption(propertiesPrintOptions);
        }
        
        // remove localizer
        LocaleSupport.removeLocalizer(localizer);
    }
    
}
