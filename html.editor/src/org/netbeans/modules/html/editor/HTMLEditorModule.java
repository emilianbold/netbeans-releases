/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.html.editor;

import org.netbeans.editor.LocaleSupport;
import org.netbeans.editor.Settings;
import org.netbeans.editor.ext.html.HTMLSettingsInitializer;
import org.netbeans.modules.editor.NbLocalizer;
import org.netbeans.modules.editor.html.HTMLKit;
import org.netbeans.modules.editor.html.NbHTMLSettingsInitializer;
import org.netbeans.modules.html.editor.options.HTMLOptions;
import org.netbeans.modules.html.editor.options.HTMLPrintOptions;
import org.openide.modules.ModuleInstall;
import org.openide.options.SystemOption;
import org.openide.text.PrintSettings;
import org.openide.util.SharedClassObject;

/**
 * Module installation class for editor.
 *
 * @author Miloslav Metelka
 */
public class HTMLEditorModule extends ModuleInstall {

    private NbLocalizer optionsLocalizer;

    /** Module installed again. */
    public void restored () {
        PrintSettings ps = (PrintSettings) SharedClassObject.findObject(PrintSettings.class, true);
        ps.addOption((SystemOption)SharedClassObject.findObject(HTMLPrintOptions.class, true));

        Settings.addInitializer(new HTMLSettingsInitializer(HTMLKit.class));
        Settings.addInitializer(new NbHTMLSettingsInitializer());
        Settings.reset();

        optionsLocalizer = new NbLocalizer(HTMLOptions.class);
        LocaleSupport.addLocalizer(optionsLocalizer);
    }

    /** Called when module is uninstalled. Overrides superclass method. */
    public void uninstalled() {
        // Options
        PrintSettings ps = (PrintSettings) SharedClassObject.findObject(PrintSettings.class, true);
        ps.removeOption((SystemOption)SharedClassObject.findObject(HTMLPrintOptions.class, true));
    
        Settings.removeInitializer(HTMLSettingsInitializer.NAME);
        Settings.removeInitializer(NbHTMLSettingsInitializer.NAME);
        Settings.reset();

        LocaleSupport.removeLocalizer(optionsLocalizer);
        optionsLocalizer = null;
    }

    
}
