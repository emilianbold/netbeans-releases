/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.masterfs;

import org.netbeans.spi.queries.VisibilityQueryImplementation;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.options.SystemOption;
import org.openide.util.Lookup;
import org.openide.util.SharedClassObject;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

// XXX - one would expect this class in core but there is problem that 
//core can't depened on project/queries at the moment 

/**
 * 
 * Implemenent VisibilityQueryImplementation based on regular expression provided
 * by users via  property in IDESettings with property name IDESettings.PROP_IGNORED_FILES 
 * in Tools/Options.  
 * 
 * This class has hidden dependency on IDESettings in module org.netbeans.core.
 */ 
public class GlobalVisibilityQueryImpl implements VisibilityQueryImplementation {
    private SystemOption ideSettings;
    private final List/*<ChangeListener>*/ listeners = new ArrayList();
    
    /**
     * Keep it synchronized with IDESettings.PROP_IGNORED_FILES
     */ 
    private static final String PROP_IGNORED_FILES = "IgnoredFiles"; // NOI18N
    private static Method mIgnoredFiles;
    private Pattern ignoreFilesPattern = null;

    public GlobalVisibilityQueryImpl() {
    }

    public boolean isVisible(FileObject file) {
        Pattern ignoreFilesPattern = getIgnoreFilesPattern();
        return (ignoreFilesPattern != null) ? !(ignoreFilesPattern.matcher(file.getNameExt()).find()) : true;
    }

    /**
     * Add a listener to changes.
     * @param l a listener to add
     */
    public synchronized void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }

    /**
     * Stop listening to changes.
     * @param l a listener to remove
     */
    public synchronized void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }

    private void fireChange() {
        ChangeListener[] _listeners;
        synchronized (this) {
            if (listeners.isEmpty()) {
                return;
            }
            _listeners = (ChangeListener[]) listeners.toArray(new ChangeListener[listeners.size()]);
        }
        ChangeEvent ev = new ChangeEvent(this);
        for (int i = 0; i < _listeners.length; i++) {
            _listeners[i].stateChanged(ev);
        }
    }

    private Pattern getIgnoreFilesPattern() {
        if (ignoreFilesPattern == null) {
            String ignoredFiles = getIgnoredFiles();
            ignoreFilesPattern = (ignoredFiles != null && ignoredFiles.length() > 0) ? Pattern.compile(ignoredFiles) : null;
        }
        return ignoreFilesPattern;
    }

    private String getIgnoredFiles() {
        String retVal = "";//NOI18N
        try {
            if (ideSettings == null) {
                ClassLoader l = (ClassLoader) Lookup.getDefault().lookup(ClassLoader.class);
                Class clazz = l.loadClass("org.netbeans.core.IDESettings"); // NOI18N
                ideSettings = (SystemOption) SharedClassObject.findObject(clazz, true);
                if (ideSettings != null) {
                    mIgnoredFiles = clazz.getMethod("getIgnoredFiles", null); // NOI18N
                    ideSettings.addPropertyChangeListener(new PropertyChangeListener() {
                        public void propertyChange(PropertyChangeEvent evt) {
                            if (PROP_IGNORED_FILES.equals(evt.getPropertyName())) {
                                ignoreFilesPattern = null;
                                fireChange();
                            }
                        }
                    });
                }
            }
            retVal = (ideSettings != null && mIgnoredFiles != null) ? 
                    (String) mIgnoredFiles.invoke(ideSettings, new Object[0]) : "";//NOI18N
        } catch (ClassNotFoundException e) {
            // OK, e.g. in a unit test.
            ideSettings = null;
        } catch (Exception e) {
            ideSettings = null;
            ErrorManager.getDefault().notify(e);
        }
        return retVal;
    }


}
