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

package org.netbeans.modules.masterfs;

import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import org.netbeans.spi.queries.VisibilityQueryImplementation;
import org.openide.filesystems.FileObject;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;
import org.openide.util.NbPreferences;

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
    static GlobalVisibilityQueryImpl INSTANCE;
    private final List/*<ChangeListener>*/ listeners = new ArrayList();
    
    /**
     * Keep it synchronized with IDESettings.PROP_IGNORED_FILES
     */ 
    private static final String PROP_IGNORED_FILES = "IgnoredFiles"; // NOI18N
    private static Method mIgnoredFiles;
    private Pattern ignoreFilesPattern = null;

    /** Default instance for lookup. */
    public GlobalVisibilityQueryImpl() {
        INSTANCE = this;
    }

    private static Preferences getPreferences() {
        return NbPreferences.root().node("/org/netbeans/core");
    }
    
    public boolean isVisible(FileObject file) {
        return isVisible(file.getNameExt());
    }

    boolean isVisible(final String fileName) {
        Pattern ignoreFilesPattern = getIgnoreFilesPattern();
        return (ignoreFilesPattern != null) ? !(ignoreFilesPattern.matcher(fileName).find()) : true;
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

    protected String getIgnoredFiles() {
        // XXX probably matching \.(cvsignore|svn|DS_Store) is pointless as would anyway match ^\..*$
        String retval =  getPreferences().get(PROP_IGNORED_FILES, "^(CVS|SCCS|vssver\\.scc|#.*#|%.*%|\\.(cvsignore|svn|DS_Store)|_svn)$|~$|^\\..*$");//NOI18N;
        getPreferences().addPreferenceChangeListener(new PreferenceChangeListener() {
            public void preferenceChange(PreferenceChangeEvent evt) {
                if (PROP_IGNORED_FILES.equals(evt.getKey())) {
                    ignoreFilesPattern = null;
                    fireChange();
                }
                
            }
        });                
        return retval;
    }    
}
