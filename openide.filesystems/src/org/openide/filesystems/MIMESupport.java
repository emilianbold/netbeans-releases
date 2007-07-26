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

package org.openide.filesystems;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Union2;

/**
 * This class is intended to enhance MIME resolving. This class offers
 * only one method: findMIMEType(FileObject fo). If this method is called, then
 * registered subclasses of MIMEResolver are asked one by one to resolve MIME type of this FileObject.
 * Resolving is finished right after first resolver is able to resolve this FileObject or if all registered
 * resolvers returns null (not recognized).
 *
 *Resolvers are registered if they have their record in IDE_HOME\system\Services
 * in form *.instance e.g.: org-some-package-JavaResolver.instance
 *
 * @author  rmatous
 */
final class MIMESupport extends Object {
    /* The following two fields represent a single-entry cache, which proved
     * to be as effective as any other more complex caching due to typical
     * access pattern from DataSystems.
     */
    private static final Reference<FileObject> EMPTY = new WeakReference<FileObject>(null);
    private static Reference<FileObject> lastFo = EMPTY;
    private static Reference<FileObject> lastCfo = EMPTY;
    private static Object lock = new Object();
    
    /** for logging and test interaction */
    private static Logger ERR = Logger.getLogger(MIMESupport.class.getName());

    private MIMESupport() {
    }

    /** Asks all registered subclasses of MIMEResolver to resolve FileObject passed as parameter.
     * @param fo is FileObject, whose MIME should be resolved
     * @param def the default value to return or null
     * @return  MIME type or null if not resolved*/
    static String findMIMEType(FileObject fo, String def) {
        if (!fo.isValid() || fo.isFolder()) {
            return null;
        }

        if ((def != null) && !CachedFileObject.isAnyResolver()) {
            return def;
        }

        CachedFileObject cfo = null;

        try {
            synchronized (lock) {
                CachedFileObject lcfo = (CachedFileObject)lastCfo.get();
                if (lcfo == null || fo != lastFo.get() || timeOf(fo) != timeOf(lcfo)) {
                    cfo = new CachedFileObject(fo);
                } else {
                    cfo = lcfo;
                }

                lastCfo = EMPTY;
            }

            return cfo.getMIMEType(def);
        } finally {
            synchronized (lock) {
                lastFo = new WeakReference<FileObject>(fo);
                lastCfo = new WeakReference<FileObject>(cfo);
            }
        }
    }
    private static long timeOf(FileObject fo) {
        if (fo == null) {
            throw new NullPointerException();
        }
        Date d = fo.lastModified();
        assert d != null : "Null lastModified from " + fo;
        return d.getTime();
    }
    
    /** Testing purposes.
     */
    static MIMEResolver[] getResolvers() {
        return CachedFileObject.getResolvers();
    }

    private static class CachedFileObject extends FileObject implements FileChangeListener {
        static Lookup.Result<MIMEResolver> result;
        private static Union2<MIMEResolver[],Set<Thread>> resolvers; // call getResolvers instead 
        /** resolvers that were here before we cleaned them */
        private static MIMEResolver[] previousResolvers;
        
        String mimeType;
        java.util.Date lastModified;
        CachedInputStream fixIt;

        /*All calls delegated to this object.
         Except few methods, that returns cached values*/
        FileObject fileObj;

        CachedFileObject(FileObject fo) {
            fileObj = fo;
            lastModified = fileObj.lastModified();
            fileObj.addFileChangeListener(FileUtil.weakFileChangeListener(this, fileObj));
        }

        private static MIMEResolver[] getResolvers() {
            Set<Thread> creators;
            synchronized (CachedFileObject.class) {
                if (resolvers != null && resolvers.hasFirst()) {
                    return resolvers.first();
                }
                if (resolvers != null) {
                    creators = resolvers.second();
                    if (creators.contains (Thread.currentThread())) {
                        // prevent stack overflow
                        ERR.fine("Stack Overflow prevention. Returning previousResolvers: " + previousResolvers);
                        MIMEResolver[] toRet = previousResolvers;
                        if (toRet == null) {
                            toRet = new MIMEResolver[0];
                        }
                        return toRet;
                    }
                } else {
                    creators = new HashSet<Thread>();
                    resolvers = Union2.createSecond(creators);
                }

                if (result == null) {
                    result = Lookup.getDefault().lookupResult(MIMEResolver.class);
                    result.addLookupListener(
                        new LookupListener() {
                            public void resultChanged(LookupEvent evt) {
                                synchronized (CachedFileObject.class) {
                                    ERR.fine("Clearing cache"); // NOI18N
                                    Union2<MIMEResolver[],Set<Thread>> prev = resolvers;
                                    if (prev != null && prev.hasFirst()) {
                                        previousResolvers = prev.first();
                                    }
                                    resolvers = null;
                                    lastFo = EMPTY;
                                    lastCfo = EMPTY;
                                }
                            }
                        }
                    );
                }

                // ok, let's compute the value
                creators.add(Thread.currentThread());
            }

            ERR.fine("Computing resolvers"); // NOI18N
            
            MIMEResolver[] toRet = result.allInstances().toArray(new MIMEResolver[0]);

            ERR.fine("Resolvers computed"); // NOI18N

            synchronized (CachedFileObject.class) {
                if (resolvers != null && resolvers.hasSecond() && resolvers.second() == creators) {
                    // ok, we computed the value and nobody cleared it till now
                    resolvers = Union2.createFirst(toRet);
                    previousResolvers = null;
                    ERR.fine("Resolvers assigned"); // NOI18N
                } else {
                    ERR.fine("Somebody else computes resolvers: " + resolvers); // NOI18N
                }


                return toRet;
            }
        }

        public static boolean isAnyResolver() {
            return getResolvers().length > 0;
        }

        public void freeCaches() {
            fixIt = null;
            mimeType = null;
            lastModified = null;
        }

        public String getMIMEType() {
            return getMIMEType(null);
        }

        public String getMIMEType(String def) {
            if (mimeType == null) {
                mimeType = resolveMIME(def);
            }

            return mimeType;
        }

        private String resolveMIME(String def) {
            String retVal = null;
            MIMEResolver[] local = getResolvers();

            try {
                for (int i = 0; i < local.length; i++) {
                    retVal = local[i].findMIMEType(this);

                    if (retVal != null) {
                        return retVal;
                    }
                }

                if (def != null) {
                    return def;
                }

                return "content/unknown"; // NOI18N
            } finally {
                if (fixIt != null) {
                    fixIt.internalClose();
                }

                fixIt = null;
            }
        }

        public java.util.Date lastModified() {
            if (lastModified != null) {
                return lastModified;
            }

            return lastModified = fileObj.lastModified();
        }

        public InputStream getInputStream() throws java.io.FileNotFoundException {
            if (fixIt == null) {
                InputStream is = fileObj.getInputStream();

                if (!(is instanceof BufferedInputStream)) {
                    is = new BufferedInputStream(is);
                }

                fixIt = new CachedInputStream(is);
            }

            fixIt.cacheToStart();

            return fixIt;
        }

        public void fileChanged(FileEvent fe) {
            freeCaches();
        }

        public void fileDeleted(FileEvent fe) {
            freeCaches();

            //removeFromCache (fe.getFile ());
        }

        public void fileRenamed(FileRenameEvent fe) {
            freeCaches();
        }

        /*All other methods only delegate to fileObj*/
        public FileObject getParent() {
            return fileObj.getParent();
        }

        @Deprecated // have to override for compat
        public String getPackageNameExt(char separatorChar, char extSepChar) {
            return fileObj.getPackageNameExt(separatorChar, extSepChar);
        }

        public FileObject copy(FileObject target, String name, String ext)
        throws IOException {
            return fileObj.copy(target, name, ext);
        }

        protected void fireFileDeletedEvent(Enumeration<FileChangeListener> en, FileEvent fe) {
            fileObj.fireFileDeletedEvent(en, fe);
        }

        protected void fireFileFolderCreatedEvent(Enumeration<FileChangeListener> en, FileEvent fe) {
            fileObj.fireFileFolderCreatedEvent(en, fe);
        }

        @Deprecated // have to override for compat
        public void setImportant(boolean b) {
            fileObj.setImportant(b);
        }

        public boolean isData() {
            return fileObj.isData();
        }

        public Object getAttribute(String attrName) {
            return fileObj.getAttribute(attrName);
        }

        public Enumeration<? extends FileObject> getFolders(boolean rec) {
            return fileObj.getFolders(rec);
        }

        public void delete(FileLock lock) throws IOException {
            fileObj.delete(lock);
        }

        public boolean isRoot() {
            return fileObj.isRoot();
        }

        public Enumeration<? extends FileObject> getData(boolean rec) {
            return fileObj.getData(rec);
        }

        public FileObject[] getChildren() {
            return fileObj.getChildren();
        }

        public String getNameExt() {
            return fileObj.getNameExt();
        }

        public boolean isValid() {
            return fileObj.isValid();
        }

        @Deprecated // have to override for compat
        public boolean isReadOnly() {
            return fileObj.isReadOnly();
        }

        public String getExt() {
            return fileObj.getExt();
        }

        public String getName() {
            return fileObj.getName();
        }

        public void removeFileChangeListener(FileChangeListener fcl) {
            fileObj.removeFileChangeListener(fcl);
        }

        protected void fireFileRenamedEvent(Enumeration<FileChangeListener> en, FileRenameEvent fe) {
            fileObj.fireFileRenamedEvent(en, fe);
        }

        public void refresh(boolean expected) {
            fileObj.refresh(expected);
        }

        protected void fireFileAttributeChangedEvent(Enumeration<FileChangeListener> en, FileAttributeEvent fe) {
            fileObj.fireFileAttributeChangedEvent(en, fe);
        }

        public long getSize() {
            return fileObj.getSize();
        }

        public Enumeration<String> getAttributes() {
            return fileObj.getAttributes();
        }

        public void rename(FileLock lock, String name, String ext)
        throws IOException {
            fileObj.rename(lock, name, ext);
        }

        protected void fireFileChangedEvent(Enumeration<FileChangeListener> en, FileEvent fe) {
            fileObj.fireFileChangedEvent(en, fe);
        }

        public FileObject getFileObject(String name, String ext) {
            return fileObj.getFileObject(name, ext);
        }

        public void refresh() {
            fileObj.refresh();
        }

        public FileObject createData(String name, String ext)
        throws IOException {
            return fileObj.createData(name, ext);
        }

        public void addFileChangeListener(FileChangeListener fcl) {
            fileObj.addFileChangeListener(fcl);
        }

        protected void fireFileDataCreatedEvent(Enumeration<FileChangeListener> en, FileEvent fe) {
            fileObj.fireFileDataCreatedEvent(en, fe);
        }

        public boolean isFolder() {
            return fileObj.isFolder();
        }

        public FileObject createFolder(String name) throws IOException {
            return fileObj.createFolder(name);
        }

        public Enumeration<? extends FileObject> getChildren(boolean rec) {
            return fileObj.getChildren(rec);
        }

        public void setAttribute(String attrName, Object value)
        throws IOException {
            fileObj.setAttribute(attrName, value);
        }

        @Deprecated // have to override for compat
        public String getPackageName(char separatorChar) {
            return fileObj.getPackageName(separatorChar);
        }

        public FileSystem getFileSystem() throws FileStateInvalidException {
            return fileObj.getFileSystem();
        }

        public OutputStream getOutputStream(FileLock lock)
        throws java.io.IOException {
            return fileObj.getOutputStream(lock);
        }

        public boolean existsExt(String ext) {
            return fileObj.existsExt(ext);
        }

        public FileObject move(FileLock lock, FileObject target, String name, String ext)
        throws IOException {
            return fileObj.move(lock, target, name, ext);
        }

        @Override
        public synchronized boolean isLocked() {
            return fileObj.isLocked();
        }
        
        public FileLock lock() throws IOException {
            return fileObj.lock();
        }

        public void fileFolderCreated(FileEvent fe) {
        }

        public void fileDataCreated(FileEvent fe) {
        }

        public void fileAttributeChanged(FileAttributeEvent fe) {
        }

        /** MIMEResolvers should not cache this FileObject. But they can cache
         * resolved patterns in Map with this FileObject as key.*/
        public int hashCode() {
            return fileObj.hashCode();
        }

        public boolean equals(java.lang.Object obj) {
            if (obj instanceof CachedFileObject) {
                return ((CachedFileObject) obj).fileObj.equals(fileObj);
            }

            return super.equals(obj);
        }
    }

    private static class CachedInputStream extends InputStream {
        private InputStream inputStream;
        private byte[] buffer = null;
        private int len = 0;
        private int pos = 0;
        private boolean eof = false;

        CachedInputStream(InputStream is) {
            inputStream = is;
        }

        /** This stream can be closed only from MIMESupport. That`s why
         * internalClose was added*/
        public void close() throws java.io.IOException {
        }

        void internalClose() {
            try {
                inputStream.close();
            } catch (IOException ioe) {
            }
        }

        protected void finalize() {
            internalClose();
        }

        public int read() throws IOException {
            if (eof) {
                return -1;
            }

            int c;
            int n;

            if (pos < len) {
                c = buffer[pos++];
                c = (c < 0) ? (c + 256) : c;

                return c;
            }

            int buflen = (len > 0) ? (len * 2) : 256;
            byte[] buf = new byte[buflen];

            if (len > 0) {
                System.arraycopy(buffer, 0, buf, 0, len);
            }

            n = inputStream.read(buf, len, buflen - len);

            if (n <= 0) {
                eof = true;

                return -1;
            }

            buffer = buf;
            len += n;

            c = buffer[pos++];
            c = (c < 0) ? (c + 256) : c;

            return c;
        }

        void cacheToStart() {
            pos = 0;
            eof = false;
        }

        /** for debug purposes. Returns buffered content. */
        public String toString() {
            String retVal = super.toString() + '[' + inputStream.toString() + ']' + '\n'; //NOI18N
            retVal += new String(buffer);

            return retVal;
        }
    }
}
