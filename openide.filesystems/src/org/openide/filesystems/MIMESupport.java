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

package org.openide.filesystems;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.modules.openide.filesystems.declmime.MIMEResolverImpl;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.Union2;

/**
 * This class is intended to enhance MIME resolving. This class offers
 * only one method: findMIMEType(FileObject fo). If this method is called, then
 * registered subclasses of MIMEResolver are asked one by one to resolve MIME type of this FileObject.
 * Resolving is finished right after first resolver is able to resolve this FileObject or if all registered
 * resolvers returns null (not recognized).
 * <p>
 * Resolvers are registered if they have their record in the Lookup area.
 * E.g. in form : org-some-package-JavaResolver.instance file.
 * <p>
 * MIME resolvers can also be registered in the <code>Services/MIMEResolver</code>
 * folder as <code>*.xml</code> files obeying a <a href="doc-files/HOWTO-MIME.html">certain format</a>.
 * These will be interpreted before resolvers in lookup (in the order specified in that folder).
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
    private static final Object lock = new Object();
    
    /** for logging and test interaction */
    private static Logger ERR = Logger.getLogger(MIMESupport.class.getName());

    private MIMESupport() {
    }

    /** Asks all registered subclasses of MIMEResolver to resolve FileObject passed as parameter.
     * @param fo is FileObject, whose MIME should be resolved
     * @param withinMIMETypes an array of MIME types which only should be considered
     * @return  MIME type or null if not resolved*/
    static String findMIMEType(FileObject fo, String... withinMIMETypes) {
        if (!fo.isValid() || fo.isFolder()) {
            return null;
        }

        CachedFileObject cfo = null;

        try {
            synchronized (lock) {
                CachedFileObject lcfo = (CachedFileObject)lastCfo.get();
                if (lcfo == null || fo != lastFo.get()) {
                    cfo = new CachedFileObject(fo);
                } else {
                    cfo = lcfo;
                }

                lastCfo = EMPTY;
            }

            return cfo.getMIMEType(withinMIMETypes);
        } finally {
            synchronized (lock) {
                lastFo = new WeakReference<FileObject>(fo);
                lastCfo = new WeakReference<FileObject>(cfo);
            }
        }
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
        /** Set used to print just one warning per resolver. */
        private static final Set<String> warningPrinted = new HashSet<String>();

        String mimeType;
        java.util.Date lastModified;
        Long size;
        CachedInputStream fixIt;
        String ext;

        /*All calls delegated to this object.
         Except few methods, that returns cached values*/
        FileObject fileObj;

        CachedFileObject(FileObject fo) {
            fileObj = fo;
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
                        if (ERR.isLoggable(Level.FINE)) ERR.fine("Stack Overflow prevention. Returning previousResolvers: " + previousResolvers);
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
                                resetCache();
                            }
                        }
                    );
                }

                // ok, let's compute the value
                creators.add(Thread.currentThread());
            }

            ERR.fine("Computing resolvers"); // NOI18N

            List<MIMEResolver> all = new ArrayList<MIMEResolver>(declarativeResolvers());
            all.addAll(result.allInstances());
            MIMEResolver[] toRet = all.toArray(new MIMEResolver[all.size()]);

            ERR.fine("Resolvers computed"); // NOI18N

            synchronized (CachedFileObject.class) {
                if (resolvers != null && resolvers.hasSecond() && resolvers.second() == creators) {
                    // ok, we computed the value and nobody cleared it till now
                    resolvers = Union2.createFirst(toRet);
                    previousResolvers = null;
                    ERR.fine("Resolvers assigned"); // NOI18N
                } else {
                    if (ERR.isLoggable(Level.FINE)) ERR.fine("Somebody else computes resolvers: " + resolvers); // NOI18N
                }


                return toRet;
            }
        }

        private static synchronized void resetCache() {
            ERR.fine("Clearing cache"); // NOI18N
            Union2<MIMEResolver[],Set<Thread>> prev = resolvers;
            if (prev != null && prev.hasFirst()) {
                previousResolvers = prev.first();
            }
            resolvers = null;
            lastFo = EMPTY;
            lastCfo = EMPTY;
        }

        private static final FileChangeListener declarativeFolderListener = new FileChangeAdapter() {
            public @Override void fileDataCreated(FileEvent fe) {
                resetCache();
            }
            public @Override void fileDeleted(FileEvent fe) {
                resetCache();
            }
        };
        private static final FileChangeListener weakDeclarativeFolderListener = FileUtil.weakFileChangeListener(declarativeFolderListener, null);
        private static final Map<FileObject,MIMEResolver> declarativeResolverCache = new WeakHashMap<FileObject,MIMEResolver>();
        // holds reference to not loose FileChangeListener
        private static FileObject declarativeFolder = null;

        private static synchronized List<MIMEResolver> declarativeResolvers() {
            List<MIMEResolver> declmimes = new ArrayList<MIMEResolver>();
            if (declarativeFolder == null) {
                declarativeFolder = FileUtil.getConfigFile("Services/MIMEResolver"); // NOI18N
            }
            if (declarativeFolder != null) {
                for (FileObject f : Ordering.getOrder(Arrays.asList(declarativeFolder.getChildren()), true)) {
                    if (f.hasExt("xml")) { // NOI18N
                        // For now, just assume it has the right DTD. Could check this if desired.
                        MIMEResolver r = declarativeResolverCache.get(f);
                        if (r == null) {
                            r = MIMEResolverImpl.forDescriptor(f);
                            declarativeResolverCache.put(f, r);
                        }
                        declmimes.add(r);
                    }
                }
                declarativeFolder.removeFileChangeListener(weakDeclarativeFolderListener);
                declarativeFolder.addFileChangeListener(weakDeclarativeFolderListener);
            }
            return declmimes;
        }

        public static boolean isAnyResolver() {
            return getResolvers().length > 0;
        }

        public void freeCaches() {
            fixIt = null;
            mimeType = null;
            lastModified = null;
            ext = null;
        }

        @Override
        public String getMIMEType() {
            return getMIMEType((String[]) null);
        }

        public String getMIMEType(String... withinMIMETypes) {
            String resolvedMimeType = mimeType;
            if (resolvedMimeType == null) {
                resolvedMimeType = resolveMIME(withinMIMETypes);
                if (resolvedMimeType == null) {
                    // fallback for xml files to be recognized e.g. in platform without any MIME resolver registered
                    if (getExt().toLowerCase().equals("xml")) {  //NOI18N
                        resolvedMimeType = "text/xml"; // NOI18N
                    } else {
                        // general fallback
                        resolvedMimeType = "content/unknown"; // NOI18N
                    }
                } else if (withinMIMETypes.length == 0) {
                    // cache resolved MIME type only for unrestricted query
                    mimeType = resolvedMimeType;
                }
            }
            return resolvedMimeType;
        }

        /** Decides whether given MIMEResolver is capable to resolve at least
         * one of given MIME types.
         * @param resolver MIMEResolver to be examined
         * @param desiredMIMETypes an array of MIME types
         * @return true if at least one of given MIME types can be resolved by
         * given resolver or if array is empty or resolver.getMIMETypes() doesn't
         * return non empty array, false otherwise.
         */
        private boolean canResolveMIMETypes(MIMEResolver resolver, String... desiredMIMETypes) {
            if(desiredMIMETypes.length == 0) {
                return true;
            }
            String[] resolvableMIMETypes = null;
            if (MIMEResolverImpl.isDeclarative(resolver)) {
                resolvableMIMETypes = MIMEResolverImpl.getMIMETypes(resolver);
            } else {
                resolvableMIMETypes = resolver.getMIMETypes();
            }
            if(resolvableMIMETypes == null || resolvableMIMETypes.length == 0) {
                if(warningPrinted.add(resolver.getClass().getName())) {
                    ERR.warning(resolver.getClass().getName() + "'s constructor should call super(String...) with list of resolvable MIME types.");  //NOI18N
                }
                return true;
            }
            for (int i = 0; i < desiredMIMETypes.length; i++) {
                for (int j = 0; j < resolvableMIMETypes.length; j++) {
                    if(resolvableMIMETypes[j].equals(desiredMIMETypes[i])) {
                        return true;
                    }
                }
            }
            return false;
        }
        
        private String resolveMIME(String... withinMIMETypes) {
            String retVal = null;
            MIMEResolver[] local = getResolvers();

            try {
                for (int i = 0; i < local.length; i++) {
                    MIMEResolver resolver = local[i];
                    if(canResolveMIMETypes(resolver, withinMIMETypes)) {
                        retVal = resolver.findMIMEType(this);
                    }
                    if (retVal != null) {
                        return retVal;
                    }
                }
            } finally {
                if (fixIt != null) {
                    fixIt.internalClose();
                }

                fixIt = null;
            }
            return retVal;
        }

        public java.util.Date lastModified() {
            if (lastModified == null) {
                lastModified = fileObj.lastModified();
            }
            return lastModified;
        }

        public InputStream getInputStream() throws java.io.FileNotFoundException {
            if (fixIt == null) {
                LogRecord rec = new LogRecord(Level.FINE, "MSG_CACHED_INPUT_STREAM");
                rec.setParameters(new Object[] { this });
                rec.setResourceBundle(NbBundle.getBundle(MIMESupport.class));
                ERR.log(rec);
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
        @Override
        public String getPackageNameExt(char separatorChar, char extSepChar) {
            return fileObj.getPackageNameExt(separatorChar, extSepChar);
        }

        @Override
        public FileObject copy(FileObject target, String name, String ext)
        throws IOException {
            return fileObj.copy(target, name, ext);
        }

        @Override
        protected void fireFileDeletedEvent(Enumeration<FileChangeListener> en, FileEvent fe) {
            fileObj.fireFileDeletedEvent(en, fe);
        }

        @Override
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

        @Override
        public Enumeration<? extends FileObject> getFolders(boolean rec) {
            return fileObj.getFolders(rec);
        }

        public void delete(FileLock lock) throws IOException {
            fileObj.delete(lock);
        }

        public boolean isRoot() {
            return fileObj.isRoot();
        }

        @Override
        public Enumeration<? extends FileObject> getData(boolean rec) {
            return fileObj.getData(rec);
        }

        public FileObject[] getChildren() {
            return fileObj.getChildren();
        }

        @Override
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

        @Override
        public boolean canRead() {
            return fileObj.canRead();
        }

        @Override
        public boolean canWrite() {
            return fileObj.canWrite();
        }

        public String getExt() {
            if(ext == null) {
                ext = fileObj.getExt();
            }
            return ext;
        }

        public String getName() {
            return fileObj.getName();
        }

        public void removeFileChangeListener(FileChangeListener fcl) {
            fileObj.removeFileChangeListener(fcl);
        }

        @Override
        protected void fireFileRenamedEvent(Enumeration<FileChangeListener> en, FileRenameEvent fe) {
            fileObj.fireFileRenamedEvent(en, fe);
        }

        @Override
        public void refresh(boolean expected) {
            fileObj.refresh(expected);
        }

        @Override
        protected void fireFileAttributeChangedEvent(Enumeration<FileChangeListener> en, FileAttributeEvent fe) {
            fileObj.fireFileAttributeChangedEvent(en, fe);
        }

        public long getSize() {
            if (size != null) {
                return size;
            }
            
            return size = fileObj.getSize();
        }

        public Enumeration<String> getAttributes() {
            return fileObj.getAttributes();
        }

        public void rename(FileLock lock, String name, String ext)
        throws IOException {
            fileObj.rename(lock, name, ext);
        }

        @Override
        protected void fireFileChangedEvent(Enumeration<FileChangeListener> en, FileEvent fe) {
            fileObj.fireFileChangedEvent(en, fe);
        }

        public FileObject getFileObject(String name, String ext) {
            return fileObj.getFileObject(name, ext);
        }

        @Override
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

        @Override
        protected void fireFileDataCreatedEvent(Enumeration<FileChangeListener> en, FileEvent fe) {
            fileObj.fireFileDataCreatedEvent(en, fe);
        }

        public boolean isFolder() {
            return fileObj.isFolder();
        }

        public FileObject createFolder(String name) throws IOException {
            return fileObj.createFolder(name);
        }

        @Override
        public Enumeration<? extends FileObject> getChildren(boolean rec) {
            return fileObj.getChildren(rec);
        }

        public void setAttribute(String attrName, Object value)
        throws IOException {
            fileObj.setAttribute(attrName, value);
        }

        @Deprecated // have to override for compat
        @Override
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

        @Override
        public boolean existsExt(String ext) {
            return fileObj.existsExt(ext);
        }

        @Override
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
        @Override
        public int hashCode() {
            return fileObj.hashCode();
        }

        @Override
        public boolean equals(java.lang.Object obj) {
            if (obj instanceof CachedFileObject) {
                return ((CachedFileObject) obj).fileObj.equals(fileObj);
            }

            return super.equals(obj);
        }

        @Override
        public String getPath() {
            return fileObj.getPath();
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
        @Override
        public void close() throws java.io.IOException {
        }

        void internalClose() {
            try {
                inputStream.close();
            } catch (IOException ioe) {
            }
        }

        @Override
        protected void finalize() {
            internalClose();
        }

       private boolean ensureBufferLength(int newLen) throws IOException {
           if (!eof && newLen > len) {
                byte[] tmpBuffer = new byte[newLen];
                if (len > 0) {
                    System.arraycopy(buffer, 0, tmpBuffer, 0, len);
                }
                int readLen = inputStream.read(tmpBuffer, len, newLen - len);
                if ((readLen > 0)) {
                    buffer = tmpBuffer;
                    len += readLen;
                } else {
                    eof = true;
                }
           }
           return len >= newLen;
        }

        @Override
        public int read(byte[] b, int off, int blen) throws IOException {
            ensureBufferLength(pos + blen);
            int readPos = Math.min(len, pos + blen);
            int retval = (readPos > pos) ? (readPos - pos) : -1;
            if (retval != -1) {
                System.arraycopy(buffer, pos, b, off, retval);
                pos += retval;
            }
            return retval;
        }


        public int read() throws IOException {
            int retval = -1;
            ensureBufferLength(pos + 1);
            if (len > pos) {
                retval = buffer[pos++];
                retval = (retval < 0) ? (retval + 256) : retval;
            }
            return retval;
        }

        void cacheToStart() {
            pos = 0;
            eof = false;
        }

        /** for debug purposes. Returns buffered content. */
        @Override
        public String toString() {
            String retVal = super.toString() + '[' + inputStream.toString() + ']' + '\n'; //NOI18N
            retVal += new String(buffer);

            return retVal;
        }
    }
}
