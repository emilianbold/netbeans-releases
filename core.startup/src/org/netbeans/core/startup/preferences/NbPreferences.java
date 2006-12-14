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

import java.io.IOException;
import java.util.Properties;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.openide.ErrorManager;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Radek Matous
 */
public abstract class NbPreferences extends AbstractPreferences {
    private static Preferences USER_ROOT;
    private static Preferences SYSTEM_ROOT;
    
    /*private*/Properties properties;
    /*private*/FileStorage fileStorage;
    
    private static final RequestProcessor RP = new RequestProcessor();
    /*private*/final RequestProcessor.Task flushTask = RP.create(new Runnable() {
        public void run() {
            synchronized(lock) {
                try {
                    flushSpi();
                } catch (BackingStoreException ex) {
                    ErrorManager.getDefault().notify(ex);
                }
            }
        }
    },true);
    
    
    static Preferences userRootImpl() {
        if (USER_ROOT == null) {
            USER_ROOT = new NbPreferences.UserPreferences();
        }
        assert USER_ROOT != null;
        return USER_ROOT;
    }
    
    static Preferences systemRootImpl() {
        if (SYSTEM_ROOT == null) {
            SYSTEM_ROOT = new NbPreferences.SystemPreferences();
        }
        assert SYSTEM_ROOT != null;
        return SYSTEM_ROOT;
    }

    private NbPreferences(boolean user) {
        super(null, "");
        fileStorage = getFileStorage(absolutePath());
    }
    
    /** Creates a new instance of PreferencesImpl */
    private  NbPreferences(NbPreferences parent, String name)  {
        super(parent, name);
        fileStorage = getFileStorage(absolutePath());
        newNode = !fileStorage.existsNode();
    }
        
    protected final String getSpi(String key) {
        return (String)properties().getProperty(key);
    }
    
    protected final String[] childrenNamesSpi() throws BackingStoreException {
        //TODO: cache it if necessary
        return fileStorage.childrenNames();
    }
    
    protected final String[] keysSpi() throws BackingStoreException {
        return (String[])properties().keySet().toArray(new String[0]);
    }
    
    protected final void putSpi(String key, String value) {
        properties().put(key,value);
        fileStorage.markModified();
        asyncInvocationOfFlushSpi();
    }
    
    @Override
    public void put(String key, String value) {
        String oldValue = getSpi(key);
        if (value.equals(oldValue)) {return;}
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
    
    protected final void removeSpi(String key) {
        properties().remove(key);
        fileStorage.markModified();
        asyncInvocationOfFlushSpi();
    }
    
    protected final void removeNodeSpi() throws BackingStoreException {
        try {
            fileStorage.removeNode();
        } catch (IOException ex) {
            throw new BackingStoreException(ex);
        }
    }
    
    void asyncInvocationOfFlushSpi() {
        if (!fileStorage.isReadOnly()) {
            flushTask.schedule(200);
        }
    }
    
    protected  void flushSpi() throws BackingStoreException {
        try {
            fileStorage.save(properties());
        } catch (IOException ex) {
            throw new BackingStoreException(ex);
        }
    }
    
    protected void syncSpi() throws BackingStoreException {
        if (properties != null) {            
            try {
                properties.clear();
                properties().putAll(fileStorage.load());
                
            } catch (IOException ex) {
                throw new BackingStoreException(ex);
            }
        }
    }
    
    Properties properties()  {
        if (properties == null) {
            properties = new Properties(/*loadDefaultProperties()*/);
            try {
                properties().putAll(fileStorage.load());
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }
        return properties;
    }

    public final void removeNode() throws BackingStoreException {
        if (fileStorage.isReadOnly()) {
            throw new BackingStoreException("Unsupported operation: read-only storage");//NOI18N
        } else {
            properties().clear();
            super.removeNode();
        }
    }
    
    public final void flush() throws BackingStoreException {
        if (fileStorage.isReadOnly()) {
            throw new BackingStoreException("Unsupported operation: read-only storage");//NOI18N
        } else {
            super.flush();
        }
    }
    
    public final void sync() throws BackingStoreException {
        if (fileStorage.isReadOnly()) {
            throw new BackingStoreException("Unsupported operation: read-only storage");//NOI18N
        } else {
            flushTask.waitFinished();
            super.sync();
        }
    }

    protected abstract FileStorage getFileStorage(String absolutePath);

    public static class UserPreferences extends NbPreferences {
        public UserPreferences() {
            super(true);
        }
        
        /** Creates a new instance */
        private UserPreferences(NbPreferences parent, String name)  {
            super(parent, name);
        }
        
        protected AbstractPreferences childSpi(String name) {
            return new UserPreferences(this, name);
        }

        protected NbPreferences.FileStorage getFileStorage(String absolutePath) {
            return PropertiesStorage.instance(absolutePath());
        }
    }
    
    private static final class SystemPreferences extends NbPreferences {
        private SystemPreferences() {
            super(false);
        }
        
        private SystemPreferences(NbPreferences parent, String name) {
            super(parent, name);
        }
        
        protected AbstractPreferences childSpi(String name) {
            return new SystemPreferences(this, name);
        }

        protected NbPreferences.FileStorage getFileStorage(String absolutePath) {
            return PropertiesStorage.instanceReadOnly(absolutePath());            
        }
    }
    
    interface FileStorage {
        boolean isReadOnly();
        String[] childrenNames();
        boolean existsNode();
        void removeNode() throws IOException;
        void markModified();
        Properties load() throws IOException;
        void save(final Properties properties) throws IOException;
    }
}
