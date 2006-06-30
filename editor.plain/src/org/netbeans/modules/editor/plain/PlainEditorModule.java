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

package org.netbeans.modules.editor.plain;

import org.netbeans.editor.LocaleSupport;
import org.netbeans.editor.Settings;
import org.netbeans.modules.editor.NbLocalizer;
import org.netbeans.modules.editor.plain.options.PlainOptions;
import org.netbeans.modules.editor.plain.options.PlainPrintOptions;
import org.openide.modules.ModuleInstall;
import org.openide.options.SystemOption;
import org.openide.text.PrintSettings;
import org.openide.util.SharedClassObject;

/**
 * Module installation class for plain editor.
 *
 * @author Miloslav Metelka
 */
public class PlainEditorModule extends ModuleInstall {

    private NbLocalizer optionsLocalizer;

    /** Module installed again. */
    public void restored () {
        Settings.addInitializer(new NbPlainSettingsInitializer());
        Settings.reset();

        PrintSettings ps = (PrintSettings) SharedClassObject.findObject(PrintSettings.class, true);
        ps.addOption((SystemOption)SharedClassObject.findObject(PlainPrintOptions.class, true));

        // TODO - remove localizers completely
        optionsLocalizer = new NbLocalizer(PlainOptions.class);
        LocaleSupport.addLocalizer(optionsLocalizer);
    }

    /** Called when module is uninstalled. Overrides superclass method. */
    public void uninstalled() {
        // Options
        PrintSettings ps = (PrintSettings) SharedClassObject.findObject(PrintSettings.class, true);
        ps.removeOption((SystemOption)SharedClassObject.findObject(PlainPrintOptions.class, true));
        
        Settings.removeInitializer(NbPlainSettingsInitializer.NAME);
        Settings.reset();

        LocaleSupport.removeLocalizer(optionsLocalizer);
        optionsLocalizer = null;
    }

    
}
