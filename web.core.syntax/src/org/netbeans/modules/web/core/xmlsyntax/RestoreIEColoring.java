/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.core.xmlsyntax;

import javax.swing.JEditorPane;
import java.util.MissingResourceException;

import org.openide.util.NbBundle;

import org.netbeans.editor.Settings;
import org.netbeans.editor.LocaleSupport;

/**
 * @author Petr Jiricka
 */
public class RestoreIEColoring {

    public void addInitializer () {
        Settings.addInitializer (new JspXMLSettingsInitializer(), Settings.EXTENSION_LEVEL);

        // add a localizer to editor
        LocaleSupport.addLocalizer(new LocaleSupport.Localizer() {
            public String getString(String key) {
                try {
                    if (key.startsWith("EXAMPLE_coloring_jsp-xml")
                    ||  key.startsWith("NAME_coloring_jsp-xml")) {
                        return NbBundle.getMessage (RestoreIEColoring.class, key);
                    }
                } catch (MissingResourceException e) {
                    // harmless
                }
                return null;
            }
        });

    }


} // end of clas RestoreIEColoring

