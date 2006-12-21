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

package org.openide.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.netbeans.modules.openide.util.PreferencesProvider;

/**
 * Provides an implementation of the Preferences API which may be backed by
 * a NetBeans-specific implementation.
 * @see <a href="doc-files/preferences.html">Preferences API in NetBeans</a>
 * @since org.openide.util 7.4
 * @author Radek Matous
 */
public final class NbPreferences {
    private static PreferencesProvider PREFS_IMPL;
    
    private  NbPreferences() {}
    
    /**
     * Returns user preference node . {@link Preferences#absolutePath} of such
     * a node depends whether class provided as a parameter was loaded as a part of any module
     * or not. If so, then absolute path corresponds to slashified code name base of module.
     * If not, then absolute path corresponds to class's package.
     *
     * @param cls the class for which a user preference node is desired.
     * @return the user preference node
     */
    public static Preferences forModule(Class cls) {
          if (PREFS_IMPL == null) {
                PREFS_IMPL = getPreferencesProvider();
          }
          return PREFS_IMPL.preferencesForModule(cls);
    }
    
    /**
     * Returns the root preference node.
     *
     * @return the root preference node.
     */
    public static Preferences root() {
          if (PREFS_IMPL == null) {
                PREFS_IMPL = getPreferencesProvider();
          }
          return PREFS_IMPL.preferencesRoot();
    }    
         
    private static PreferencesProvider getPreferencesProvider() {
        PreferencesProvider retval = Lookup.getDefault().lookup(PreferencesProvider.class);
        if (retval == null) {
             retval = new PreferencesProvider() {
                  public Preferences preferencesForModule(Class cls) {
                       return Preferences.userNodeForPackage(cls);
                  }

                  public Preferences preferencesRoot() {
                       return Preferences.userRoot();
                  }                         
             };
             // Avoid warning in case it is set (e.g. from NbTestCase).
             Logger logger = Logger.getLogger(NbPreferences.class.getName());
             ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
             new Exception().printStackTrace(new PrintStream(bos));
             logger.log(System.getProperty("java.util.prefs.PreferencesFactory") == null ? Level.WARNING : Level.FINE,
                     "NetBeans implementation of Preferences not found: " + bos.toString() );
        }
        return retval;
    }    
}
