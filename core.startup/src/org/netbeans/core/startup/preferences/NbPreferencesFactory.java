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

package org.netbeans.core.startup.preferences;

import java.util.prefs.Preferences;
import java.util.prefs.PreferencesFactory;

/**
 *
 * @author Radek Matous
 */
public class NbPreferencesFactory implements PreferencesFactory {
    private static final String FACTORY = "java.util.prefs.PreferencesFactory";//NOI18N
    
    /** Creates a new instance  */
    public NbPreferencesFactory() {}

    public Preferences userRoot() {
        return NbPreferences.userRootImpl();
    }
    
    public Preferences systemRoot() {
        return NbPreferences.systemRootImpl();
    }

    public static void doRegistration() {
        if (System.getProperty(FACTORY) == null) {
            System.setProperty(FACTORY,NbPreferencesFactory.class.getName());
        }
    }
}
