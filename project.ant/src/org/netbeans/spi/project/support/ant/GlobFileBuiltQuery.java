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
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.WeakHashMap;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.queries.FileBuiltQuery;
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
    private final FileObject projectDir;
    private final File projectDirF;
    private final String[] fromPrefixes;
    private final String[] fromSuffixes;
    private final String[] toPrefixes;
    private final String[] toSuffixes;
    private static final Object NONE = "NONE"; // NOI18N
    private final Map/*<FileObject,Reference<StatusImpl>|NONE>*/ statuses = new WeakHashMap();
    private RequestProcessor.Task refreshTask = null;
    private final FileL fileL;
    private final FileChangeListener weakFileL;

    /**
     * Create a new query implementation based on an Ant-based project.
     * @see AntProjectHelper#createGlobFileBuiltQuery
     */
    public GlobFileBuiltQuery(AntProjectHelper helper, PropertyEvaluator eval, String[] from, String[] to) throws IllegalArgumentException {
        this.helper = helper;
        this.eval = eval;
        projectDir = helper.getProjectDirectory();
        projectDirF = FileUtil.toFile(projectDir);
        assert projectDirF != null;
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
        fileL = new FileL();
        /* XXX because of #33162 (no listening to file trees), cannot just do:
        projectDir.addFileChangeListener(FileUtil.weakFileChangeListener(fileL, projectDir));
         */
        weakFileL = FileUtil.weakFileChangeListener(fileL, null);
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
    
    private StatusImpl createStatus(FileObject file) {
        String path = FileUtil.getRelativePath(projectDir, file);
        if (path == null) {
            // XXX support external source roots
            if (err.isLoggable(ErrorManager.INFORMATIONAL)) {
                err.log("No relative path to " + file + " in " + projectDir + ", skipping");
            }
            return null;
        }
        for (int i = 0; i < fromPrefixes.length; i++) {
            String prefixEval = eval.evaluate(fromPrefixes[i]);
            if (prefixEval == null) {
                if (err.isLoggable(ErrorManager.INFORMATIONAL)) {
                    err.log(fromPrefixes[i] + " evaluates to null");
                }
                continue;
            }
            if (!path.startsWith(prefixEval)) {
                continue;
            }
            String remainder = path.substring(prefixEval.length());
            String suffixEval = eval.evaluate(fromSuffixes[i]);
            if (suffixEval == null) {
                if (err.isLoggable(ErrorManager.INFORMATIONAL)) {
                    err.log(fromSuffixes[i] + " evaluates to null");
                }
                continue;
            }
            if (!remainder.endsWith(suffixEval)) {
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
            String targetPath = toPrefixEval + particular + toSuffixEval;
            if (err.isLoggable(ErrorManager.INFORMATIONAL)) {
                err.log("Made status object for " + file + ": " + targetPath);
            }
            return new StatusImpl(file, targetPath);
        }
        if (err.isLoggable(ErrorManager.INFORMATIONAL)) {
            err.log("No match for path " + path + " among " + Arrays.asList(fromPrefixes) + " " + Arrays.asList(fromSuffixes));
        }
        return null;
    }
    
    private synchronized void updateAll() {
        // Need to post a fresh task since otherwise there can be lock
        // order conflicts with masterfs.
        // Use just one task, i.e. try to coalesce change events.
        if (refreshTask == null) {
            refreshTask = RequestProcessor.getDefault().create(fileL);
        }
        // Give it a small timeout to allow a bunch of events to be coalesced.
        ((RequestProcessor.Task)refreshTask).schedule(100);
    }
    
    private final class FileL implements FileChangeListener, Runnable {
        
        FileL() {}
        
        public void fileChanged(FileEvent fe) {
            updateAll();
        }
        
        public void fileDataCreated(FileEvent fe) {
            updateAll();
        }
        
        public void fileDeleted(FileEvent fe) {
            updateAll();
        }
        
        public void fileFolderCreated(FileEvent fe) {
            updateAll();
        }
        
        public void fileRenamed(FileRenameEvent fe) {
            updateAll();
        }
        
        public void fileAttributeChanged(FileAttributeEvent fe) {
            // ignore
        }
        
        public void run() {
            synchronized (GlobFileBuiltQuery.this) {
                Iterator/*<Reference<StatusImpl>|NONE>*/ it = statuses.values().iterator();
                while (it.hasNext()) {
                    Object o = it.next();
                    if (o == NONE) {
                        continue;
                    }
                    Reference r = (Reference)o;
                    if (r == null) {
                        continue;
                    }
                    StatusImpl status = (StatusImpl)r.get();
                    if (status == null) {
                        continue;
                    }
                    status.isBuilt();
                }
            }
        }
        
    }
    
    private final class StatusImpl implements FileBuiltQuery.Status, PropertyChangeListener/*<DataObject>*/, FileChangeListener {
        
        private final List/*<ChangeListener>*/ listeners = new ArrayList();
        private Boolean built = null;
        private final DataObject source;
        private final String[] targetPath;
        /**
         * A Filesystems representation of the current target file, or if it does not
         * exist, the lowest ancestor in the project directory which does.
         * We don't do anything with it - intentionally; its only purpose is to not
         * be garbage collected, so that changes will still be fired in it when
         * appropriate. Every time we check the timestamp on disk, we also update
         * this file object, to force the Filesystems infrastructure to keep on
         * listening to it. Wouldn't be necessary if all changes to the target file
         * went through the Filesystems API, but more typically they will occur on
         * disk and cause a refresh of some high-up parent directory.
         * Also because of the lack of hierarchical listeners (#33162), we need to
         * keep a file change listener on the last available parent.
         */
        private FileObject lastTargetApproximation;
        
        StatusImpl(FileObject source, String targetPath) {
            try {
                this.source = DataObject.find(source);
            } catch (DataObjectNotFoundException e) {
                throw new Error(e);
            }
            this.source.addPropertyChangeListener(WeakListeners.propertyChange(this, this.source));
            source.addFileChangeListener(FileUtil.weakFileChangeListener(this, source));
            StringTokenizer tok = new StringTokenizer(targetPath, "/"); // NOI18N
            this.targetPath = new String[tok.countTokens()];
            int i = 0;
            while (tok.hasMoreTokens()) {
                this.targetPath[i++] = tok.nextToken();
            }
        }
        
        // Side effect is to update its cache and maybe fire changes.
        public synchronized boolean isBuilt() {
            boolean b = isReallyBuilt();
            if (built != null && built.booleanValue() != b) {
                // XXX do not fire change from within synch block
                fireChange();
            }
            built = Boolean.valueOf(b);
            if (err.isLoggable(ErrorManager.INFORMATIONAL)) {
                err.log("isBuilt: " + b + " from " + this);
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
            if (lastTargetApproximation != null) {
                lastTargetApproximation.removeFileChangeListener(weakFileL);
            }
            lastTargetApproximation = projectDir;
            for (int i = 0; i < targetPath.length; i++) {
                String piece = targetPath[i];
                FileObject lta2 = lastTargetApproximation.getFileObject(piece);
                if (lta2 == null) {
                    lastTargetApproximation.addFileChangeListener(weakFileL);
                    if (err.isLoggable(ErrorManager.INFORMATIONAL)) {
                        err.log("did not find " + piece + " in " + lastTargetApproximation + ": " + this);
                    }
                    return false;
                }
                lastTargetApproximation = lta2;
            }
            lastTargetApproximation.addFileChangeListener(weakFileL);
            long targetTime = lastTargetApproximation.lastModified().getTime();
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
        
        public synchronized void addChangeListener(ChangeListener l) {
            listeners.add(l);
        }
        
        public synchronized void removeChangeListener(ChangeListener l) {
            listeners.remove(l);
        }
        
        private void fireChange() {
            if (listeners.isEmpty()) {
                return;
            }
            ChangeListener[] _listeners = (ChangeListener[])listeners.toArray(new ChangeListener[listeners.size()]);
            ChangeEvent ev = new ChangeEvent(this);
            for (int i = 0; i < _listeners.length; i++) {
                _listeners[i].stateChanged(ev);
            }
        }
        
        private void update() {
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    isBuilt();
                }
            });
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
        
        public String toString() {
            return "GFBQ.StatusImpl[" + source.getPrimaryFile() + " -> " + Arrays.asList(targetPath) + "]"; // NOI18N
        }
        
    }
    
}
