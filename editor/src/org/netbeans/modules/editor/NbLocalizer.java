/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor;

import java.util.MissingResourceException;
import org.openide.util.NbBundle;
import org.netbeans.editor.LocaleSupport;

/**
* Locale support localizer that uses the bundle to get
* the localized string for the given key.
*
* @author Miloslav Metelka
* @version 1.00
*/

public class NbLocalizer implements LocaleSupport.Localizer {
// Fix of #36754 - localizers memory consumption
//    private ResourceBundle bundle;
    private Class bundleClass;

    /** Construct new localizer that uses a bundle for the given class.
    * @param bundleClass class for which the bundle is retrieved by NbBundle.getBundle()
    */
    public NbLocalizer(Class bundleClass) {
        this.bundleClass = bundleClass;
    }

    /** Get the localized string using the given key. */
    public String getString(String key) {
        try {
            return NbBundle.getBundle(bundleClass).getString(key);
        } catch (MissingResourceException e) {
            return null;
        }
    }

}
