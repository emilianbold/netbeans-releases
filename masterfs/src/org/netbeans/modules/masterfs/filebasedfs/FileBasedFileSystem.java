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
package org.netbeans.modules.masterfs.filebasedfs;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.masterfs.ProvidedExtensionsProxy;
import org.netbeans.modules.masterfs.filebasedfs.fileobjects.BaseFileObj;
import org.netbeans.modules.masterfs.filebasedfs.fileobjects.FileObjectFactory;
import org.netbeans.modules.masterfs.filebasedfs.fileobjects.RootObj;
import org.netbeans.modules.masterfs.filebasedfs.fileobjects.RootObjWindows;
import org.netbeans.modules.masterfs.providers.AnnotationProvider;
import org.netbeans.modules.masterfs.providers.ProvidedExtensions;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;

/**
 * @author Radek Matous
 */
public final class FileBasedFileSystem extends FileSystem {
    private static FileBasedFileSystem INSTANCE = new FileBasedFileSystem();
    transient private RootObj<? extends FileObject> root;
    transient private final StatusImpl status = new StatusImpl();
    transient private static  int modificationInProgress;
    
    public FileBasedFileSystem() {
        if (Utilities.isWindows()) {
            RootObjWindows realRoot = new RootObjWindows();
            root = new RootObj<RootObjWindows>(realRoot);
        } else {
            FileObjectFactory factory = FileObjectFactory.getInstance(new File("/"));//NOI18N
            root = new RootObj<BaseFileObj>(factory.getRoot());
        }
    }
   
    public synchronized static boolean isModificationInProgress() {
        return modificationInProgress == 0 ? false : true;
    }

    private synchronized static void setModificationInProgress(boolean started) {
        if (started) {
            modificationInProgress++;
        } else {
            modificationInProgress--;
        }
    }

    public static void runAsInconsistent(Runnable r)   {
        try {
            setModificationInProgress(true);
            r.run();
        } finally {
            setModificationInProgress(false);
        }
    }
    
    public static <Retval> Retval runAsInconsistent(FSCallable<Retval> r)  throws IOException {
        Retval retval = null;
        try {
            setModificationInProgress(true);
            retval = r.call();
        } finally {
            setModificationInProgress(false);
        }
        return retval;
    }
    
    public static Map<File, ? extends FileObjectFactory> factories() {
        return FileObjectFactory.factories();
    }        

    public static final FileObject getFileObject(final File file) {
        return getFileObject(file, FileObjectFactory.Caller.GetFileObject);
    }
    
    public static final FileObject getFileObject(final File file, FileObjectFactory.Caller caller) {
        FileObjectFactory fs = FileObjectFactory.getInstance(file);
        FileObject retval = null;
        if (fs != null) {
            if (file.getParentFile() == null && Utilities.isUnix()) {
                retval = FileBasedFileSystem.getInstance().getRoot();
            } else {
                retval = fs.getValidFileObject(file,caller);
            }                
        }         
        return retval;
    }
    
    
    public static FileBasedFileSystem getInstance() {
        return INSTANCE;
    }

    @Override
    public void refresh(final boolean expected) {                        
        final Runnable r = new Runnable() {
            public void run() {
                refreshImpl(expected);
            }            
        };
        try {
            FileBasedFileSystem.getInstance().runAtomicAction(new FileSystem.AtomicAction() {
                public void run() throws IOException {
                    FileBasedFileSystem.runAsInconsistent(r);
                }
            });
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    public void refreshImpl(boolean expected) {                        
        FileObject fo = root.getRealRoot();
        if (fo instanceof BaseFileObj) {
            ((BaseFileObj)fo).getFactory().refresh(null, expected);
        } else if (fo instanceof RootObjWindows) {
            Collection<? extends FileObjectFactory> fcs =  factories().values();
            for (FileObjectFactory fileObjectFactory : fcs) {
                fileObjectFactory.refresh(null, expected);
            }
        }
    }
        
    @Override
    public String getDisplayName() {
        return getClass().getName();
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public FileObject getRoot() {
        return root;
    }

    @Override
    public FileObject findResource(String name) {
        if (Utilities.isWindows()) {
            if ("".equals(name)) {//NOI18N
                return FileBasedFileSystem.getInstance().getRoot();
            }
        }  else {
            name = (name.startsWith("/")) ? name : ("/"+name);    
        }               
        return getFileObject(new File(name));
    }

    @Override
    public SystemAction[] getActions() {
        return new SystemAction[] {};
    }

    @Override
    public final SystemAction[] getActions(final Set<FileObject> foSet) {
        SystemAction[] some = status.getActions (foSet);
        if (some != null) {
            return some;
        }        
        return new SystemAction[] {};

    }
    
    @Override
    public Status getStatus() {
        return status;
    }

    public final class StatusImpl implements FileSystem.HtmlStatus,
            org.openide.util.LookupListener, org.openide.filesystems.FileStatusListener {

        /** result with providers */
        private org.openide.util.Lookup.Result<AnnotationProvider> annotationProviders;
        private Collection<? extends AnnotationProvider> previousProviders;
        

        {
            annotationProviders = Lookup.getDefault().lookup(new Lookup.Template<AnnotationProvider>(AnnotationProvider.class));
            annotationProviders.addLookupListener(this);
            resultChanged(null);
        }

        public ProvidedExtensions getExtensions() {
            Collection<? extends AnnotationProvider> c;
            if (previousProviders != null) {
                c = Collections.unmodifiableCollection(previousProviders);
            } else {
                c = Collections.emptyList();
            }
            return new ProvidedExtensionsProxy(c);
        }

        public void resultChanged(org.openide.util.LookupEvent ev) {
            Collection<? extends AnnotationProvider> now = annotationProviders.allInstances();
            Collection<? extends AnnotationProvider> add;

            if (previousProviders != null) {
                add = new HashSet<AnnotationProvider>(now);
                add.removeAll(previousProviders);

                HashSet<AnnotationProvider> toRemove = new HashSet<AnnotationProvider>(previousProviders);
                toRemove.removeAll(now);
                for (AnnotationProvider ap : toRemove) {
                    ap.removeFileStatusListener(this);
                }

            } else {
                add = now;
            }



            for (AnnotationProvider ap : add) {
                try {
                    ap.addFileStatusListener(this);
                } catch (java.util.TooManyListenersException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }

            previousProviders = now;
        }

        public SystemAction[] getActions(Set<FileObject> foSet) {

            javax.swing.Action[] retVal = null;
            java.util.Iterator<? extends AnnotationProvider> it = annotationProviders.allInstances().iterator();
            while (retVal == null && it.hasNext()) {
                AnnotationProvider ap = it.next();
                retVal = ap.actions(foSet);
            }
            if (retVal != null) {
                // right now we handle just SystemAction, it can be changed if necessary
                SystemAction[] ret = new SystemAction[retVal.length];
                for (int i = 0; i < retVal.length; i++) {
                    if (retVal[i] instanceof SystemAction) {
                        ret[i] = (SystemAction) retVal[i];
                    }
                }
                return ret;
            }
            return null;
        }

        public void annotationChanged(org.openide.filesystems.FileStatusEvent ev) {
            fireFileStatusChanged(ev);
        }

        public Image annotateIcon(Image icon, int iconType, Set<? extends FileObject> files) {
            Image retVal = null;

            Iterator<? extends AnnotationProvider> it = annotationProviders.allInstances().iterator();
            while (retVal == null && it.hasNext()) {
                AnnotationProvider ap = it.next();
                retVal = ap.annotateIcon(icon, iconType, files);
            }
            if (retVal != null) {
                return retVal;
            }

            return icon;
        }

        public String annotateName(String name, Set<? extends FileObject> files) {
            String retVal = null;
            Iterator<? extends AnnotationProvider> it = annotationProviders.allInstances().iterator();
            while (retVal == null && it.hasNext()) {
                AnnotationProvider ap = it.next();
                retVal = ap.annotateName(name, files);
            }
            if (retVal != null) {
                return retVal;
            }
            return name;
        }

        public String annotateNameHtml(String name, Set<? extends FileObject> files) {
            String retVal = null;
            Iterator<? extends AnnotationProvider> it = annotationProviders.allInstances().iterator();
            while (retVal == null && it.hasNext()) {
                AnnotationProvider ap = it.next();
                retVal = ap.annotateNameHtml(name, files);
            }
            return retVal;
        }
    }

    public Object writeReplace() throws ObjectStreamException {
        return new SerReplace();
    }

    private static class SerReplace implements Serializable {
        /** serial version UID */
        static final long serialVersionUID = -3714631266626840241L;
        public Object readResolve() throws ObjectStreamException {
            return FileBasedFileSystem.getInstance();
        }
    }
    
    public static interface  FSCallable<V>  {
        public V call() throws IOException;                
    }
}
