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
import org.netbeans.modules.editor.NbLocalizer;
import org.netbeans.modules.editor.plain.options.PlainOptions;
import org.openide.modules.ModuleInstall;

/**
 * Module installation class for plain editor.
 *
 * @author Miloslav Metelka
 * @deprecated If you need this class you are doing something wrong, 
 *   please ask on nbdev@netbeans.org.
 */
public class PlainEditorModule extends ModuleInstall {

    private NbLocalizer optionsLocalizer;

    /** Module installed again. */
    public void restored () {
        // TODO - remove localizers completely
        optionsLocalizer = new NbLocalizer(PlainOptions.class);
        LocaleSupport.addLocalizer(optionsLocalizer);
    }

    /** Called when module is uninstalled. Overrides superclass method. */
    public void uninstalled() {
        LocaleSupport.removeLocalizer(optionsLocalizer);
        optionsLocalizer = null;
    }
}
