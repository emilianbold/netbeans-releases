/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is Forte for Java, Community Edition. The Initial
 * Developer of the Original Code is Sun Microsystems, Inc. Portions
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor;

import java.util.ResourceBundle;
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

    private ResourceBundle bundle;

    /** Construct new localizer that uses a bundle for the given class.
    * @param bundleClass class for which the bundle is retrieved by NbBundle.getBundle()
    */
    public NbLocalizer(Class bundleClass) {
        bundle = NbBundle.getBundle(bundleClass);
    }

    /** Get the localized string using the given key. */
    public String getString(String key) {
        try {
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            return null;
        }
    }

}

/*
 * Log
 *  2    Gandalf   1.1         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  1    Gandalf   1.0         9/30/99  Miloslav Metelka 
 * $
 */

