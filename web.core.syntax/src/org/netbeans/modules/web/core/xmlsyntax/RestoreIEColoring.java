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

package org.netbeans.modules.web.core.xmlsyntax;

import java.util.MissingResourceException;

import org.openide.util.NbBundle;

import org.netbeans.editor.Settings;
import org.netbeans.editor.LocaleSupport;

/**
 * @author Petr Jiricka
 */

@Deprecated()
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

