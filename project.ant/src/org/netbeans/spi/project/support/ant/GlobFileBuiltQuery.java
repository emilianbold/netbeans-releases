/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.spi.project.support.ant;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import org.openide.util.ChangeSupport;
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
    private static final Reference<StatusImpl> NONE = new WeakReference<StatusImpl>(null);
    private final Map<FileObject,Reference<StatusImpl>> statuses = new WeakHashMap<FileObject,Reference<StatusImpl>>();

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
    
    public @Override synchronized FileBuiltQuery.Status getStatus(FileObject file) {
        Reference<StatusImpl> r = statuses.get(file);
        if (r == NONE) {
            return null;
        }
        StatusImpl status = (r != null) ? r.get() : null;
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
                Logger.getLogger(GlobFileBuiltQuery.class.getName()).log(Level.FINE, null, e);
                return null;
            }   
        } else {
            return null;
        }
    }

    private static final RequestProcessor RP = new RequestProcessor(StatusImpl.class.getName());
    
    private final class StatusImpl implements FileBuiltQuery.Status, PropertyChangeListener/*<DataObject>*/, FileChangeListener, Runnable {
        
        private final ChangeSupport cs = new ChangeSupport(this);
        private Boolean built = null;
        private final DataObject source;
        private File target;
        private FileChangeListener targetListener;
        
        @SuppressWarnings("LeakingThisInConstructor")
        StatusImpl(DataObject source, FileObject sourceFO, File target) {
            this.source = source;
            this.source.addPropertyChangeListener(WeakListeners.propertyChange(this, this.source));
            sourceFO.addFileChangeListener(FileUtil.weakFileChangeListener(this, sourceFO));
            this.target = target;
            targetListener = new FileChangeListener() {
                public @Override void fileFolderCreated(FileEvent fe) {
                    // N/A for file
                }
                public @Override void fileDataCreated(FileEvent fe) {
                    update();
                }
                public @Override void fileChanged(FileEvent fe) {
                    update();
                }
                public @Override void fileDeleted(FileEvent fe) {
                    update();
                }
                public @Override void fileRenamed(FileRenameEvent fe) {
                    update();
                }
                public @Override void fileAttributeChanged(FileAttributeEvent fe) {
                    update();
                }
            };
            FileUtil.addFileChangeListener(targetListener, target);
        }
        
        // Side effect is to update its cache and maybe fire changes.
        public @Override boolean isBuilt() {
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
                cs.fireChange();
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
        
        public @Override void addChangeListener(ChangeListener l) {
            cs.addChangeListener(l);
        }
        
        public @Override void removeChangeListener(ChangeListener l) {
            cs.removeChangeListener(l);
        }
        
        private void update() {
            // XXX should this maintain a single Task and schedule() it?
            RP.post(this);
        }
        
        public @Override void run() {
            isBuilt();
        }
        
        public @Override void propertyChange(PropertyChangeEvent evt) {
            assert evt.getSource() instanceof DataObject;
            if (DataObject.PROP_MODIFIED.equals(evt.getPropertyName())) {
                update();
            }
        }
        
        public @Override void fileChanged(FileEvent fe) {
            update();
        }
        
        public @Override void fileDeleted(FileEvent fe) {
            update();
        }
        
        public @Override void fileRenamed(FileRenameEvent fe) {
            File target2 = findTarget(source.getPrimaryFile());
            if (!Utilities.compareObjects(target, target2)) {
                // #45694: source file moved, recalculate target.
                if (target != null) {
                    FileUtil.removeFileChangeListener(targetListener, target);
                }
                if (target2 != null) {
                    FileUtil.addFileChangeListener(targetListener, target2);
                }
                target = target2;
            }
            update();
        }
        
        public @Override void fileDataCreated(FileEvent fe) {
            // ignore
        }
        
        public @Override void fileFolderCreated(FileEvent fe) {
            // ignore
        }
        
        public @Override void fileAttributeChanged(FileAttributeEvent fe) {
            // ignore
        }
        
        @Override
        public String toString() {
            return "GFBQ.StatusImpl[" + source.getPrimaryFile() + " -> " + target + "]"; // NOI18N
        }

    }
    
}
