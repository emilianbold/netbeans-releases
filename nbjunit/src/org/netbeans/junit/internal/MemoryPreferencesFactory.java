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

package org.netbeans.junit.internal;

import java.util.Properties;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.prefs.PreferencesFactory;

/**
 *
 * @author Radek Matous
 */
public class MemoryPreferencesFactory implements PreferencesFactory {
    /** Creates a new instance  */
    public MemoryPreferencesFactory() {}
    
    public Preferences userRoot() {
        return NbPreferences.userRootImpl();
    }
    
    public Preferences systemRoot() {
        return NbPreferences.systemRootImpl();
    }
        
    private static class NbPreferences extends AbstractPreferences {
        private static Preferences USER_ROOT;
        private static Preferences SYSTEM_ROOT;
        
        /*private*/Properties properties;
        
        static Preferences userRootImpl() {
            if (USER_ROOT == null) {
                USER_ROOT = new NbPreferences();
            }
            return USER_ROOT;
        }
        
        static Preferences systemRootImpl() {
            if (SYSTEM_ROOT == null) {
                SYSTEM_ROOT = new NbPreferences();
            }
            return SYSTEM_ROOT;
        }
        
        
        private NbPreferences() {
            super(null, "");
        }
        
        /** Creates a new instance of PreferencesImpl */
        private  NbPreferences(NbPreferences parent, String name)  {
            super(parent, name);
            newNode = true;
        }
        
        protected final String getSpi(String key) {
            return properties().getProperty(key);
        }
        
        protected final String[] childrenNamesSpi() throws BackingStoreException {
            return new String[0];
        }
        
        protected final String[] keysSpi() throws BackingStoreException {
            return properties().keySet().toArray(new String[0]);
        }
        
        protected final void putSpi(String key, String value) {
            properties().put(key,value);
        }
        
        protected final void removeSpi(String key) {
            properties().remove(key);
        }
        
        protected final void removeNodeSpi() throws BackingStoreException {}
        protected  void flushSpi() throws BackingStoreException {}
        protected void syncSpi() throws BackingStoreException {
            properties().clear();
        }
        
        @Override
        public void put(String key, String value) {
            try {
                super.put(key, value);
            } catch (IllegalArgumentException iae) {
                if (iae.getMessage().contains("too long")) {
                    // Not for us!
                    putSpi(key, value);
                } else {
                    throw iae;
                }
            }
        }
        
        Properties properties()  {
            if (properties == null) {
                properties = new Properties();
            }
            return properties;
        }
        
        protected AbstractPreferences childSpi(String name) {
            return new NbPreferences(this, name);
        }
    }
    
}
