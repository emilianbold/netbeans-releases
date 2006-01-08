/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.spi.project.support.ant;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.queries.FileBuiltQuery;
import org.netbeans.modules.project.ant.FileChangeSupport;
import org.netbeans.modules.project.ant.FileChangeSupportEvent;
import org.netbeans.modules.project.ant.FileChangeSupportListener;
import org.netbeans.spi.queries.FileBuiltQueryImplementation;
import org.openide.ErrorManager;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;

/**
 * Simple file built query based on glob patterns.
 * @see AntProjectHelper#createGlobFileBuiltQuery
 * @author Jesse Glick
 */
final class GlobFileBuiltQuery implements FileBuiltQueryImplementation {
    
    private static final ErrorManager err = ErrorManager.getDefault().getInstance("org.netbeans.spi.project.support.ant.GlobFileBuiltQuery"); // NOI18N
    
    private final AntProjectHelper helper;
    private final PropertyEvaluator eval;
    private final String[] fromPrefixes;
    private final String[] fromSuffixes;
    private final String[] toPrefixes;
    private final String[] toSuffixes;
    private static final Object NONE = "NONE"; // NOI18N
    private final Map/*<FileObject,Reference<StatusImpl>|NONE>*/ statuses = new WeakHashMap();

    /**
     * Create a new query implementation based on an Ant-based project.
     * @see AntProjectHelper#createGlobFileBuiltQuery
     */
    public GlobFileBuiltQuery(AntProjectHelper helper, PropertyEvaluator eval, String[] from, String[] to) throws IllegalArgumentException {
        this.helper = helper;
        this.eval = eval;
        int l = from.length;
        if (to.length != l) {
            throw new IllegalArgumentException("Non-matching lengths"); // NOI18N
        }
        fromPrefixes = new String[l];
        fromSuffixes = new String[l];
        toPrefixes = new String[l];
        toSuffixes = new String[l];
        for (int i = 0; i < l; i++) {
            int idx = from[i].indexOf('*');
            if (idx == -1 || idx != from[i].lastIndexOf('*')) {
                throw new IllegalArgumentException("Zero or multiple asterisks in " + from[i]); // NOI18N
            }
            fromPrefixes[i] = from[i].substring(0, idx);
            fromSuffixes[i] = from[i].substring(idx + 1);
            idx = to[i].indexOf('*');
            if (idx == -1 || idx != to[i].lastIndexOf('*')) {
                throw new IllegalArgumentException("Zero or multiple asterisks in " + to[i]); // NOI18N
            }
            toPrefixes[i] = to[i].substring(0, idx);
            toSuffixes[i] = to[i].substring(idx + 1);
            // XXX check that none of the pieces contain two slashes in a row, and
            // the path does not start with or end with a slash, etc.
        }
        // XXX add properties listener to evaluator... if anything changes, refresh all
        // status objects and clear the status cache; can then also keep a cache of
        // evaluated path prefixes & suffixes
    }
    
    public synchronized FileBuiltQuery.Status getStatus(FileObject file) {
        Object o = statuses.get(file);
        if (o == NONE) {
            return null;
        }
        Reference r = (Reference)o;
        StatusImpl status = (r != null) ? (StatusImpl)r.get() : null;
        if (status == null) {
            status = createStatus(file);
            if (status != null) {
                statuses.put(file, new WeakReference(status));
            } else {
                statuses.put(file, NONE);
            }
        }
        return status;
    }
    
    private File findTarget(FileObject file) {
        File sourceF = FileUtil.toFile(file);
        if (sourceF == null) {
            if (err.isLoggable(ErrorManager.INFORMATIONAL)) {
                err.log("Not a disk file: " + file);
            }
            return null;
        }
        String source = sourceF.getAbsolutePath();
        for (int i = 0; i < fromPrefixes.length; i++) {
            String prefixEval = eval.evaluate(fromPrefixes[i]);
            if (prefixEval == null) {
                if (err.isLoggable(ErrorManager.INFORMATIONAL)) {
                    err.log(fromPrefixes[i] + " evaluates to null");
                }
                continue;
            }
            String suffixEval = eval.evaluate(fromSuffixes[i]);
            if (suffixEval == null) {
                if (err.isLoggable(ErrorManager.INFORMATIONAL)) {
                    err.log(fromSuffixes[i] + " evaluates to null");
                }
                continue;
            }
            boolean endsWithSlash = prefixEval.endsWith("/"); // NOI18N
            String prefixF = helper.resolveFile(prefixEval).getAbsolutePath();
            if (endsWithSlash && !prefixF.endsWith(File.separator)) {
                prefixF += File.separatorChar;
            }
            if (!source.startsWith(prefixF)) {
                continue;
            }
            String remainder = source.substring(prefixF.length());
            if (!remainder.endsWith(suffixEval.replace('/', File.separatorChar))) {
                continue;
            }
            String particular = remainder.substring(0, remainder.length() - suffixEval.length());
            String toPrefixEval = eval.evaluate(toPrefixes[i]);
            if (toPrefixEval == null) {
                if (err.isLoggable(ErrorManager.INFORMATIONAL)) {
                    err.log(toPrefixes[i] + " evaluates to null");
                }
                continue;
            }
            String toSuffixEval = eval.evaluate(toSuffixes[i]);
            if (toSuffixEval == null) {
                if (err.isLoggable(ErrorManager.INFORMATIONAL)) {
                    err.log(toSuffixes[i] + " evaluates to null");
                }
                continue;
            }
            File target = helper.resolveFile(toPrefixEval + particular + toSuffixEval);
            if (err.isLoggable(ErrorManager.INFORMATIONAL)) {
                err.log("Found target for " + source + ": " + target);
            }
            return target;
        }
        if (err.isLoggable(ErrorManager.INFORMATIONAL)) {
            err.log("No match for path " + source + " among " + Arrays.asList(fromPrefixes) + " " + Arrays.asList(fromSuffixes));
        }
        return null;
    }
    
    private StatusImpl createStatus(FileObject file) {
        File target = findTarget(file);
        if (target != null) {
            try {
                DataObject source = DataObject.find(file);
                
                return new StatusImpl(source, file, target);
            } catch (DataObjectNotFoundException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                return null;
            }   
        } else {
            return null;
        }
    }
    
    private final class StatusImpl implements FileBuiltQuery.Status, PropertyChangeListener/*<DataObject>*/, FileChangeListener, FileChangeSupportListener, Runnable {
        
        private final List/*<ChangeListener>*/ listeners = new ArrayList();
        private Boolean built = null;
        private final DataObject source;
        private File target;
        
        StatusImpl(DataObject source, FileObject sourceFO, File target) {
            this.source = source;
            this.source.addPropertyChangeListener(WeakListeners.propertyChange(this, this.source));
            sourceFO.addFileChangeListener(FileUtil.weakFileChangeListener(this, sourceFO));
            this.target = target;
            FileChangeSupport.DEFAULT.addListener(this, target);
        }
        
        // Side effect is to update its cache and maybe fire changes.
        public boolean isBuilt() {
            boolean doFire = false;
            boolean b;
            synchronized (GlobFileBuiltQuery.this) {
                b = isReallyBuilt();
                if (built != null && built.booleanValue() != b) {
                    doFire = true;
                }
                built = Boolean.valueOf(b);
                if (err.isLoggable(ErrorManager.INFORMATIONAL)) {
                    err.log("isBuilt: " + b + " from " + this);
                }
            }
            if (doFire) {
                fireChange();
            }
            return b;
        }
        
        private boolean isReallyBuilt() {
            if (!source.isValid()) {
                if (err.isLoggable(ErrorManager.INFORMATIONAL)) {
                    err.log("invalid: " + this);
                }
                return false; // whatever
            }
            if (source.isModified()) {
                if (err.isLoggable(ErrorManager.INFORMATIONAL)) {
                    err.log("modified: " + this);
                }
                return false;
            }
            if (target == null) {
                if (err.isLoggable(ErrorManager.INFORMATIONAL)) {
                    err.log("no target matching " + this);
                }
                return false;
            }
            long targetTime = target.lastModified();
            long sourceTime = source.getPrimaryFile().lastModified().getTime();
            if (targetTime >= sourceTime) {
                return true;
            } else {
                if (err.isLoggable(ErrorManager.INFORMATIONAL)) {
                    err.log("out of date (target: " + targetTime + " vs. source: " + sourceTime + "): " + this);
                }
                return false;
            }
        }
        
        public void addChangeListener(ChangeListener l) {
            synchronized (listeners) {
                listeners.add(l);
            }
        }
        
        public void removeChangeListener(ChangeListener l) {
            synchronized (listeners) {
                listeners.remove(l);
            }
        }
        
        private void fireChange() {
            ChangeListener[] _listeners;
            synchronized (listeners) {
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
        
        private void update() {
            RequestProcessor.getDefault().post(this);
        }
        
        public void run() {
            isBuilt();
        }
        
        public void propertyChange(PropertyChangeEvent evt) {
            assert evt.getSource() instanceof DataObject;
            if (DataObject.PROP_MODIFIED.equals(evt.getPropertyName())) {
                update();
            }
        }
        
        public void fileChanged(FileEvent fe) {
            update();
        }
        
        public void fileDeleted(FileEvent fe) {
            update();
        }
        
        public void fileRenamed(FileRenameEvent fe) {
            File target2 = findTarget(source.getPrimaryFile());
            if (!Utilities.compareObjects(target, target2)) {
                // #45694: source file moved, recalculate target.
                if (target != null) {
                    FileChangeSupport.DEFAULT.removeListener(this, target);
                }
                if (target2 != null) {
                    FileChangeSupport.DEFAULT.addListener(this, target2);
                }
                target = target2;
            }
            update();
        }
        
        public void fileDataCreated(FileEvent fe) {
            // ignore
        }
        
        public void fileFolderCreated(FileEvent fe) {
            // ignore
        }
        
        public void fileAttributeChanged(FileAttributeEvent fe) {
            // ignore
        }
        
        public void fileCreated(FileChangeSupportEvent event) {
            update();
        }

        public void fileDeleted(FileChangeSupportEvent event) {
            update();
        }

        public void fileModified(FileChangeSupportEvent event) {
            update();
        }
        
        public String toString() {
            return "GFBQ.StatusImpl[" + source.getPrimaryFile() + " -> " + target + "]"; // NOI18N
        }

    }
    
}
