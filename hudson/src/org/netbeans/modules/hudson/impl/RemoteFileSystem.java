/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.hudson.impl;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.hudson.api.HudsonJob;
import org.netbeans.modules.hudson.api.ConnectionBuilder;
import org.netbeans.modules.hudson.api.HudsonJobBuild;
import org.openide.filesystems.AbstractFileSystem;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.URLMapper;
import org.openide.util.Enumerations;
import org.openide.util.NbCollections;
import org.openide.util.WeakSet;
import org.openide.util.lookup.ServiceProvider;

/**
 * Virtual filesystem representing the remote workspace of a job or artifacts of a build.
 */
final class RemoteFileSystem extends AbstractFileSystem implements
        AbstractFileSystem.Attr, AbstractFileSystem.Change, AbstractFileSystem.List, AbstractFileSystem.Info {

    private static final Logger LOG = Logger.getLogger(RemoteFileSystem.class.getName());

    /** base URL of filesystem */
    private final URL baseURL;
    /** display name for filesystem */
    private final String displayName;
    /** for {@link ConnectionBuilder#job} */
    private final HudsonJob job;

    private RemoteFileSystem(URL baseURL, String displayName, HudsonJob job) {
        this.baseURL = baseURL;
        this.displayName = displayName;
        this.job = job;
        attr = this;
        change = this;
        list = this;
        info = this;
        synchronized (Mapper.class) {
            if (Mapper.workspaces == null) {
                Mapper.workspaces = new WeakSet<RemoteFileSystem>();
            }
            Mapper.workspaces.add(this);
        }
    }

    RemoteFileSystem(HudsonJob job) throws MalformedURLException {
        this(new URL(job.getUrl() + "ws/"), job.getDisplayName(), job); // NOI18N
    }

    RemoteFileSystem(HudsonJobBuild build) throws MalformedURLException {
        this(new URL(build.getUrl() + "artifact/"), /*XXX I18N*/build.getJob().getDisplayName() + " #" + build, build.getJob());
    }

    /**
     * For {@link HudsonInstanceImpl} to refresh after the workspace has been synchronized.
     */
    void refreshAll() {
        synchronized (nonDirs) {
            nonDirs.clear();
            lastModified.clear();
            size.clear();
            headers.clear();
        }
        for (FileObject f : NbCollections.iterable(existingFileObjects(getRoot()))) {
            LOG.log(Level.FINE, "{0} refreshing {1}", new Object[] {baseURL, f.getPath()});
            f.refresh();
        }
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isReadOnly() {
        return true;
    }

    /**
     * List of paths known to be data files.
     * Also used as a general lock for metadata accesses.
     */
    private final Set<String> nonDirs = new HashSet<String>();
    private final Map<String,Long> lastModified = new HashMap<String,Long>();
    private final Map<String,Integer> size = new HashMap<String,Integer>();
    private final Map<String,byte[]> headers = new HashMap<String,byte[]>();

    public String[] children(String f) {
        String fSlash = f.length() > 0 ? f + "/" : ""; // NOI18N
        try {
            URL url = new URL(baseURL, fSlash + "*plain*"); // NOI18N
            URLConnection conn = new ConnectionBuilder().job(job).url(url).connection();
            String contentType = conn.getContentType();
            if (contentType == null || !contentType.startsWith("text/plain")) { // NOI18N
                // Missing workspace, or Hudson prior to SVN 13601 (i.e. 1.264).
                LOG.log(Level.FINE, "non-plain dir listing: {0}", url);
                return new String[0];
            }
            InputStream is = conn.getInputStream();
            BufferedReader r = new BufferedReader(new InputStreamReader(is, "UTF-8")); // NOI18N
            java.util.List<String> kids = new ArrayList<String>();
            String line;
            while ((line = r.readLine()) != null) {
                if (line.endsWith("/")) {
                    String n = line.substring(0, line.length() - 1);
                    kids.add(n);
                } else {
                    kids.add(line);
                    synchronized (nonDirs) {
                        nonDirs.add(fSlash + line);
                    }
                }
            }
            LOG.log(Level.FINE, "children: {0} -> {1}", new Object[] {url, kids});
            return kids.toArray(new String[kids.size()]);
        } catch (IOException x) {
            LOG.log(Level.FINE, "cannot list children of {0} in {1}: {2}", new Object[] {f, baseURL, x});
            return new String[0];
        }
    }

    public boolean folder(String name) {
        // #157062: if it was not encountered before, assume it was a folder,
        // as this is safest (children(name) will later return {}).
        // May happen for files which are deleted on the server.
        synchronized (nonDirs) {
            return !nonDirs.contains(name);
        }
    }

    private URLConnection connection(String name) throws IOException {
        assert Thread.holdsLock(nonDirs);
        LOG.log(Level.FINE, "metadata in {0}: {1}", new Object[] {baseURL, name});
        URLConnection conn = new ConnectionBuilder().job(job).url(new URL(baseURL, name)).connection();
        lastModified.put(name, conn.getLastModified());
        int contentLength = conn.getContentLength();
        size.put(name, Math.max(0, contentLength));
        if (contentLength >= 0 && contentLength < /* more than MIMEResolverImpl needs */ 4050) {
            InputStream is = conn.getInputStream();
            byte[] buf = new byte[contentLength];
            int p = 0;
            int read;
            while ((read = is.read(buf, p, contentLength - p)) != -1) {
                p += read;
            }
            if (p == contentLength) {
                headers.put(name, buf);
            } else {
                LOG.warning("incomplete read for " + name + " in " + baseURL + ": read up to " + p + " where reported length is " + contentLength);
            }
        } // for bigger files, just reread content later if requested
        return conn;
    }

    public Date lastModified(String name) {
        synchronized (nonDirs) {
            if (folder(name)) {
                return new Date(0);
            }
            if (!lastModified.containsKey(name)) {
                try {
                    connection(name);
                } catch (IOException x) {
                    LOG.log(Level.FINE, "cannot get metadata for " + name + " in " + baseURL, x);
                    return new Date(0);
                }
            }
            return new Date(lastModified.get(name));
        }
    }

    public long size(String name) {
        synchronized (nonDirs) {
            if (folder(name)) {
                return 0;
            }
            if (!size.containsKey(name)) {
                try {
                    connection(name);
                } catch (IOException x) {
                    LOG.log(Level.FINE, "cannot get metadata for " + name + " in " + baseURL, x);
                    return 0;
                }
            }
            return size.get(name);
        }
    }

    public InputStream inputStream(String name) throws FileNotFoundException {
        synchronized (nonDirs) {
            byte[] header = headers.get(name);
            if (header != null) {
                LOG.log(Level.FINE, "cached inputStream: {0}", name);
                return new ByteArrayInputStream(header);
            }
            LOG.log(Level.FINE, "inputStream: {0}", name);
            try {
                return connection(name).getInputStream();
            } catch (IOException x) {
                throw (FileNotFoundException) new FileNotFoundException(x.getMessage()).initCause(x);
            }
        }
    }

    public Object readAttribute(String name, String attrName) {
        return null;
    }

    public void writeAttribute(String name, String attrName, Object value) throws IOException {
        throw new IOException();
    }

    public Enumeration<String> attributes(String name) {
        return Enumerations.empty();
    }

    public void renameAttributes(String oldName, String newName) {}

    public void deleteAttributes(String name) {}

    public void createFolder(String name) throws IOException {
        throw new IOException();
    }

    public void createData(String name) throws IOException {
        throw new IOException();
    }

    public void rename(String oldName, String newName) throws IOException {
        throw new IOException();
    }

    public void delete(String name) throws IOException {
        throw new IOException();
    }

    public boolean readOnly(String name) {
        return true;
    }

    public String mimeType(String name) {
        return null; // web server's guess is generally bad
    }

    public OutputStream outputStream(String name) throws IOException {
        throw new IOException();
    }

    public void lock(String name) throws IOException {}

    public void unlock(String name) {}

    public void markUnimportant(String name) {}

    @ServiceProvider(service=URLMapper.class)
    public static class Mapper extends URLMapper {

        static Set<RemoteFileSystem> workspaces = null;

        public URL getURL(FileObject fo, int type) {
            synchronized (Mapper.class) {
                if (workspaces == null) { // shortcut
                    return null;
                }
            }
            return doGetURL(fo);
        }

        public FileObject[] getFileObjects(URL url) {
            synchronized (Mapper.class) {
                if (workspaces == null) { // shortcut
                    return null;
                }
            }
            return doGetFileObjects(url);
        }

        private static URL doGetURL(FileObject fo) {
            try {
                FileSystem fs = fo.getFileSystem();
                if (fs instanceof RemoteFileSystem) {
                    return new URL(((RemoteFileSystem) fs).baseURL, fo.getPath());
                }
            } catch (IOException x) {
                LOG.log(Level.INFO, "trying to get URL for " + fo, x);
            }
            return null;
        }

        private static FileObject[] doGetFileObjects(URL url) {
            RemoteFileSystem fs = null;
            String urlS = url.toString();
            synchronized (Mapper.class) {
                for (RemoteFileSystem _fs : workspaces) {
                    if (urlS.startsWith(_fs.baseURL.toString())) {
                        fs = _fs;
                        break;
                    }
                }
            }
            if (fs != null) {
                FileObject f = fs.findResource(urlS.substring(fs.baseURL.toString().length()));
                if (f != null) {
                    return new FileObject[] {f};
                }
            }
            return null;
        }

    }

}
