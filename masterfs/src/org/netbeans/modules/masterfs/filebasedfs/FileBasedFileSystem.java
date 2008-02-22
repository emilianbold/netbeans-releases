/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.masterfs.filebasedfs;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import org.netbeans.modules.masterfs.ProvidedExtensionsProxy;
import org.netbeans.modules.masterfs.filebasedfs.fileobjects.FileObjectFactory;
import org.netbeans.modules.masterfs.filebasedfs.utils.FileInfo;
import org.netbeans.modules.masterfs.providers.AnnotationProvider;
import org.netbeans.modules.masterfs.providers.ProvidedExtensions;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;

/**
 * @author Radek Matous
 */
public final class FileBasedFileSystem extends FileSystem {
    private static Map allInstances = new HashMap();

    private transient final FileObjectFactory factory;
    transient private final StatusImpl status = new StatusImpl();
    public static boolean WARNINGS = true;
    private ThreadLocal<Boolean> refreshIsOn = new ThreadLocal<Boolean>();

    public boolean isWarningEnabled() {
        Boolean isRefreshOn = refreshIsOn.get();
        return WARNINGS && !Utilities.isMac() &&(isRefreshOn == null || !isRefreshOn.booleanValue());
    }    

    //only for tests purposes
    public static void reinitForTests() {
        FileBasedFileSystem.allInstances = new HashMap();
    }

    public static FileBasedFileSystem getInstance(final File file) {
        return getInstance(file, true);
    }
    
    public static FileBasedFileSystem getInstance(final File file, boolean addMising) {
        FileBasedFileSystem retVal = null;
        final FileInfo rootInfo = new FileInfo(file).getRoot();
        final File rootFile = rootInfo.getFile();

        synchronized (FileBasedFileSystem.allInstances) {
            retVal = (FileBasedFileSystem) FileBasedFileSystem.allInstances.get(rootFile);
        }
        if (retVal == null && addMising) {
            if (rootInfo.isConvertibleToFileObject()) {           
                synchronized (FileBasedFileSystem.allInstances) {
                    retVal = (FileBasedFileSystem) FileBasedFileSystem.allInstances.get(rootFile);
                    if (retVal == null) {
                        retVal = new FileBasedFileSystem(rootFile);
                        FileBasedFileSystem.allInstances.put(rootFile, retVal);
                    }
                }
            }
        }
        return retVal;
    }
    
    public static final FileObject getFileObject(final File file) {
        FileBasedFileSystem fs = getInstance(file);
        return (fs != null) ? fs.findFileObject(file,FileObjectFactory.Caller.GetFileObject) : null;
    }
    

    static Collection getInstances() {
        synchronized (FileBasedFileSystem.allInstances) {
            return new ArrayList(allInstances.values());
        }
    }

    public Status getStatus() {
        return status;
    }
            
    static int getSize () {
        synchronized (FileBasedFileSystem.allInstances) {
            return allInstances.size();
        }        
    }
    
    private FileBasedFileSystem(final File rootFile) {
        this.factory = FileObjectFactory.getInstance(new FileInfo(rootFile));
    }

    public final org.openide.filesystems.FileObject findResource(final String name) {
        return getFactory().getRoot().getRealRoot().getFileObject(name); 
    }

    public final FileObject findFileObject(final File f) {
        return findFileObject(new FileInfo (f), FileObjectFactory.Caller.Others);
    }

    public final FileObject findFileObject(final FileInfo fInfo) {
        return findFileObject(fInfo, FileObjectFactory.Caller.Others);
    }
    
    
    public final FileObject findFileObject(final File f, FileObjectFactory.Caller caller) {
        return findFileObject(new FileInfo (f), caller);
    }
    
    public final FileObject findFileObject(final FileInfo fInfo, FileObjectFactory.Caller caller) {
        final FileObject retVal = (getFactory().findFileObject(fInfo, this, caller));
        return (retVal != null && retVal.isValid()) ? retVal : null;
    }

    public final org.openide.filesystems.FileObject getRoot() {
        return getFactory().getRoot();
    }

    public final String getDisplayName() {
        return getFactory().getRoot().getRealRoot().getPath();
    }

    public final SystemAction[] getActions() {
        return new SystemAction[] {};
    }

    public final SystemAction[] getActions(final Set/*<FileObject>*/ foSet) {
        SystemAction[] some = status.getActions (foSet);
        if (some != null) {
            return some;
        }        
        return new SystemAction[] {};

    }

    public final void refresh(final boolean expected) {
        Statistics.StopWatch stopWatch = Statistics.getStopWatch(Statistics.REFRESH_FS);
        stopWatch.start();
        try {
            try {
                refreshIsOn.set(true);            
                this.runAtomicAction(new FileSystem.AtomicAction() {
                    public void run() throws IOException {
                        getFactory().refreshAll(expected);
                    }            
                });
            } finally {
                refreshIsOn.set(false);
            }
        } catch(IOException iex) {/*method refreshAll doesn't throw IOException*/}
        stopWatch.stop();
	
        // print refresh stats unconditionally in trunk
        Logger.getLogger("org.netbeans.modules.masterfs.REFRESH").fine(
            "FS.refresh statistics (" + Statistics.fileObjects() + "FileObjects):\n  " +
            Statistics.REFRESH_FS.toString() + "\n  " +
            Statistics.LISTENERS_CALLS.toString() + "\n  " + 
            Statistics.REFRESH_FOLDER.toString() + "\n  " + 
            Statistics.REFRESH_FILE.toString() + "\n"
        );

        Statistics.REFRESH_FS.reset();
        Statistics.LISTENERS_CALLS.reset();
        Statistics.REFRESH_FOLDER.reset();
        Statistics.REFRESH_FILE.reset();
    }
    
    public final void refreshFor(final File file) {
        Statistics.StopWatch stopWatch = Statistics.getStopWatch(Statistics.REFRESH_FS);
        stopWatch.start();
        try {
            try {
                refreshIsOn.set(true);            
                this.runAtomicAction(new FileSystem.AtomicAction() {
                    public void run() throws IOException {
                        getFactory().refreshFor(file);
                    }            
                });
            } finally {
                refreshIsOn.set(false);
            }
        } catch(IOException iex) {/*method refreshAll doesn't throw IOException*/}
        stopWatch.stop();        
	
        // print refresh stats unconditionally in trunk
        Logger.getLogger("org.netbeans.modules.masterfs.REFRESH").fine(
            "FS.refresh statistics (" + Statistics.fileObjects() + "FileObjects):\n  " +
            Statistics.REFRESH_FS.toString() + "\n  " +
            Statistics.LISTENERS_CALLS.toString() + "\n  " + 
            Statistics.REFRESH_FOLDER.toString() + "\n  " + 
            Statistics.REFRESH_FILE.toString() + "\n"
        );

        Statistics.REFRESH_FS.reset();
        Statistics.LISTENERS_CALLS.reset();
        Statistics.REFRESH_FOLDER.reset();
        Statistics.REFRESH_FILE.reset();
    }
    

    public final boolean isReadOnly() {
        return false;
    }

    public final String toString() {
        return getDisplayName();
    }

    public final FileObjectFactory getFactory() {
        return factory;
    }
    
    public Object writeReplace() throws ObjectStreamException {
        return new SerReplace(this);
    }
        
    private static class SerReplace implements Serializable {
        /** serial version UID */
        static final long serialVersionUID = -3714631266626840241L;        
        private File root;
        SerReplace(FileSystem fs) {
            root = FileUtil.toFile(fs.getRoot());
            assert root != null;
        }        
        
        public Object readResolve() throws ObjectStreamException {
            return FileBasedFileSystem.getInstance(root);
        }        
    }
        
    public final class StatusImpl implements FileSystem.HtmlStatus,
    org.openide.util.LookupListener, org.openide.filesystems.FileStatusListener {
        /** result with providers */
        private org.openide.util.Lookup.Result annotationProviders;
        private Collection previousProviders;
        {
            annotationProviders = org.openide.util.Lookup.getDefault ().lookup (
                new org.openide.util.Lookup.Template (AnnotationProvider.class)
            );
            annotationProviders.addLookupListener (this);
            resultChanged (null);
        }

        public ProvidedExtensions getExtensions() {
            Collection c = (previousProviders != null) ?
                Collections.unmodifiableCollection(previousProviders) : Collections.EMPTY_LIST;
            return new ProvidedExtensionsProxy(c);
        }
        
        public void resultChanged (org.openide.util.LookupEvent ev) {
            java.util.Collection now = annotationProviders.allInstances ();
            java.util.Collection add;
            
            if (previousProviders != null) {
                add = new HashSet (now);
                add.removeAll (previousProviders);
                
                HashSet toRemove = new HashSet(previousProviders);
                toRemove.removeAll (now);
                java.util.Iterator it = toRemove.iterator ();
                while (it.hasNext ()) {
                    AnnotationProvider ap = (AnnotationProvider)it.next ();
                    ap.removeFileStatusListener (this);
                }
            
            } else {
                add = now;
            }

            
            
            java.util.Iterator it = add.iterator ();
            while (it.hasNext ()) {
                AnnotationProvider ap = (AnnotationProvider)it.next ();
                try {
                    ap.addFileStatusListener (this);
                } catch (java.util.TooManyListenersException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            
            previousProviders = now;
        }

        public SystemAction[] getActions(java.util.Set foSet) {
            
            javax.swing.Action[] retVal = null;
            java.util.Iterator it = annotationProviders.allInstances ().iterator ();
            while (retVal == null && it.hasNext ()) {
                AnnotationProvider ap = (AnnotationProvider)it.next ();
                retVal = ap.actions (foSet);
            }
            if (retVal != null) {
                // right now we handle just SystemAction, it can be changed if necessary
                SystemAction[] ret = new SystemAction[retVal.length];
                for (int i = 0; i < retVal.length; i++) {
                    if (retVal[i] instanceof SystemAction) {
                        ret[i] = (SystemAction)retVal[i];
                    }
                }
                return ret;
            }
            return null;
        }
        
        public void annotationChanged (org.openide.filesystems.FileStatusEvent ev) {
            fireFileStatusChanged (ev);
        }
                
        public Image annotateIcon(Image icon, int iconType, Set files) {
            Image retVal = null;            
            
            Iterator it = annotationProviders.allInstances ().iterator ();
            while (retVal == null && it.hasNext ()) {
                AnnotationProvider ap = (AnnotationProvider)it.next ();
                retVal = ap.annotateIcon (icon, iconType, files);
            }
            if (retVal != null) {
                return retVal;
            }
                        
            return icon;
        }

        public String annotateName(String name, Set files) {
            String retVal = null;
            Iterator it = annotationProviders.allInstances ().iterator ();
            while (retVal == null && it.hasNext ()) {
                AnnotationProvider ap = (AnnotationProvider)it.next ();
                retVal = ap.annotateName (name, files);
            }
            if (retVal != null) {
                return retVal;
            }
            return name;
        }

        public String annotateNameHtml(String name, Set files) {
            String retVal = null;
            Iterator it = annotationProviders.allInstances ().iterator ();
            while (retVal == null && it.hasNext ()) {
                AnnotationProvider ap = (AnnotationProvider)it.next ();
                retVal = ap.annotateNameHtml (name, files);
            }
            return retVal;
        }
    }        
}
