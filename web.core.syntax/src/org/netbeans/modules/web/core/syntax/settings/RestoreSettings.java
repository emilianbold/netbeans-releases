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

package org.netbeans.modules.web.core.syntax.settings;

import org.netbeans.modules.web.core.syntax.settings.JspMultiSettingsInitializer;
import org.netbeans.modules.web.core.syntax.*;
import org.netbeans.modules.web.core.syntax.settings.JSPPrintOptions;
import org.netbeans.editor.Settings;
import org.netbeans.modules.web.core.xmlsyntax.RestoreIEColoring;
import org.openide.modules.ModuleInstall;
import org.openide.options.SystemOption;
import org.openide.text.PrintSettings;

/**
 * @author Petr Jiricka
 */
public class RestoreSettings extends ModuleInstall {

    public void restored () {
        addInitializer ();
        installOptions ();
    }

    public void uninstalled () {
        uninstallOptions ();
    }

    private void addInitializer () {
        Settings.addInitializer (new JspMultiSettingsInitializer());

        // do the same for XML syntax
//        new RestoreIEColoring().addInitializer();
    }


    public void installOptions () {
        PrintSettings ps = (PrintSettings)PrintSettings.findObject (PrintSettings.class, true);
        ps.addOption ((JSPPrintOptions)SystemOption.findObject(JSPPrintOptions.class, true));
    }

    public void uninstallOptions () {
        PrintSettings ps = (PrintSettings)PrintSettings.findObject (PrintSettings.class, true);
        JSPPrintOptions jsppo = (JSPPrintOptions)SystemOption.findObject(JSPPrintOptions.class, false);
        if (jsppo != null) ps.removeOption (jsppo);
    }

} // end of clas RestoreColoring
