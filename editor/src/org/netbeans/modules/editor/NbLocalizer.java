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
    
    public String toString() {
        return "NbLocalizer(" + bundleClass + ")"; // NOI18N
    }

}
