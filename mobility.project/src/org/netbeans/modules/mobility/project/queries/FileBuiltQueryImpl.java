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

package org.netbeans.modules.mobility.project.queries;
import org.netbeans.modules.mobility.project.ProjectConfigurationsHelper;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.queries.FileBuiltQuery;
import org.netbeans.modules.mobility.project.DefaultPropertiesDescriptor;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.queries.FileBuiltQueryImplementation;
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
 * @author Adam Sotona
 */
public class FileBuiltQueryImpl implements FileBuiltQueryImplementation, PropertyChangeListener, Runnable {
    
    protected final AntProjectHelper helper;
    private static final Object NONE = "NONE"; // NOI18N
    private final Map<FileObject,Object> statuses = new WeakHashMap<FileObject,Object>();
    private FileObject srcRoot = null;
    
    public FileBuiltQueryImpl(AntProjectHelper helper, ProjectConfigurationsHelper confs) {
        this.helper = helper;
        confs.addPropertyChangeListener(this);
    }
    
    protected FileObject getSrcRoot() {
        if (srcRoot == null) {
            final String dir = helper.getStandardPropertyEvaluator().getProperty(DefaultPropertiesDescriptor.SRC_DIR);
            if (dir != null) srcRoot = helper.resolveFileObject(dir);
        }
        return srcRoot;
    }
    
    public synchronized FileBuiltQuery.Status getStatus(final FileObject file) {
        final Object o = statuses.get(file);
        if (o == NONE) {
            return null;
        }
        final Reference r = (Reference)o;
        StatusImpl status = (r != null) ? (StatusImpl)r.get() : null;
        if (status == null) {
            status = createStatus(file);
            if (status != null) {
                statuses.put(file, new WeakReference<StatusImpl>(status));
            } else {
                statuses.put(file, NONE);
            }
        }
        return status;
    }
    
    public void propertyChange(@SuppressWarnings("unused")
	final PropertyChangeEvent evt) {
        RequestProcessor.getDefault().post(this);
    }
    
    public void run() {
        FileObject files[];
        synchronized (this) {
            files = statuses.keySet().toArray(new FileObject[statuses.size()]);
        }
        for (int i=0; i<files.length; i++) {
            final StatusImpl status = (StatusImpl) getStatus(files[i]);
            if (status != null) status.isBuilt();
        }
    }
    
    private StatusImpl createStatus(final FileObject file) {
        final FileObject root = getSrcRoot();
        if (root != null && FileUtil.isParentOf(root, file) && file.getExt().equals("java")) try { //NOI18N
            return new StatusImpl(file);
        } catch (DataObjectNotFoundException dnfe) {}
        return null;
    }
    
    private final class StatusImpl implements FileBuiltQuery.Status, PropertyChangeListener/*<DataObject>*/, FileChangeListener, Runnable {
        
        private final List<ChangeListener> listeners = new ArrayList<ChangeListener>();
        private Boolean built = null;
        private final DataObject source;
        private FileObject oldTarget = null;
        private FileChangeListener weekListener = null;
        
        StatusImpl(FileObject source) throws DataObjectNotFoundException {
            this.source = DataObject.find(source);
            this.source.addPropertyChangeListener(WeakListeners.propertyChange(this, this.source));
            source.addFileChangeListener(FileUtil.weakFileChangeListener(this, source));
        }
        
        private synchronized File getTarget() {
            final FileObject root = getSrcRoot();
            final FileObject srcFile = source.getPrimaryFile();
            if (root == null || srcFile == null) return null;
            final String path = FileUtil.getRelativePath(root, srcFile);
            final String buildClasses = helper.getStandardPropertyEvaluator().getProperty("build.classes.dir"); //NOI18N
            final File target = (path == null || !path.endsWith(".java") || buildClasses == null) ? null : helper.resolveFile(buildClasses + "/" + path.substring(0, path.length() - 4) + "class"); //NOI18N
            FileObject newTarget = null;
            File f = target;
            while ((newTarget == null || !newTarget.isValid()) && f != null) {
                newTarget = FileUtil.toFileObject(f);
                f = f.getParentFile();
            }
            if (!Utilities.compareObjects(oldTarget, newTarget)) {
                if (oldTarget != null && weekListener != null) oldTarget.removeFileChangeListener(weekListener);
                if (newTarget != null) {
                    weekListener = FileUtil.weakFileChangeListener(this, newTarget);
                    newTarget.addFileChangeListener(weekListener);
                    newTarget.getChildren(); //to kick the folder to listen for a new stuff
                }
                oldTarget = newTarget;
            }
            return target;
        }
        
        // Side effect is to update its cache and maybe fire changes.
        public boolean isBuilt() {
            boolean doFire = false;
            boolean b;
            synchronized (FileBuiltQueryImpl.this) {
                b = isReallyBuilt();
                if (built != null && built.booleanValue() != b) {
                    doFire = true;
                }
                built = Boolean.valueOf(b);
            }
            if (doFire) {
                fireChange();
            }
            return b;
        }
        
        private boolean isReallyBuilt() {
            final File target = getTarget();
            final FileObject srcFile = source.getPrimaryFile();
            if (!source.isValid() || source.isModified() || target == null || srcFile == null) return false;
            return target.lastModified() >= srcFile.lastModified().getTime();
        }
        
        public void addChangeListener(final ChangeListener l) {
            synchronized (listeners) {
                listeners.add(l);
            }
        }
        
        public void removeChangeListener(final ChangeListener l) {
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
                _listeners = listeners.toArray(new ChangeListener[listeners.size()]);
            }
            final ChangeEvent ev = new ChangeEvent(this);
            for (int i = 0; i < _listeners.length; i++) {
                _listeners[i].stateChanged(ev);
            }
        }
        
        private void update() {
            RequestProcessor.getDefault().post(StatusImpl.this);
        }
        
        public void run() {
            isBuilt();
        }
        
        public void propertyChange(final PropertyChangeEvent evt) {
            assert evt.getSource() instanceof DataObject;
            if (DataObject.PROP_MODIFIED.equals(evt.getPropertyName())) {
                update();
            }
        }
        
        public void fileChanged(@SuppressWarnings("unused")
		final FileEvent fe) {
            update();
        }
        
        public void fileDeleted(@SuppressWarnings("unused")
		final FileEvent fe) {
            update();
        }
        
        public void fileRenamed(@SuppressWarnings("unused")
		final FileRenameEvent fe) {
            update();
        }
        
        public void fileDataCreated(@SuppressWarnings("unused")
		final FileEvent fe) {
            update();
        }
        
        public void fileFolderCreated(@SuppressWarnings("unused")
		final FileEvent fe) {
            update();
        }
        
        public void fileAttributeChanged(@SuppressWarnings("unused")
		final FileAttributeEvent fe) {
            // ignore
        }
        
        public String toString() {
            return "FBQI.StatusImpl[" + source.getPrimaryFile() + " -> " + getTarget() + "]"; // NOI18N
        }
    }
}
