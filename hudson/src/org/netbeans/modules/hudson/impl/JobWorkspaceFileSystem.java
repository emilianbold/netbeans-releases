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
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.hudson.api.HudsonJob;
import org.openide.filesystems.AbstractFileSystem;
import org.openide.util.Enumerations;

/**
 * Virtual filesystem representing the remote workspace of a job.
 * XXX what should caching behavior be for best network efficiency and best accuracy?
 * Should there be some command to refresh the FS?
 * XXX should have a matching URLMapper in case some code asks for a URL
 */
class JobWorkspaceFileSystem extends AbstractFileSystem implements
        AbstractFileSystem.Attr, AbstractFileSystem.Change, AbstractFileSystem.List, AbstractFileSystem.Info {

    private static final Logger LOG = Logger.getLogger(JobWorkspaceFileSystem.class.getName());

    private final HudsonJob job;

    JobWorkspaceFileSystem(HudsonJob job) {
        this.job = job;
        attr = this;
        change = this;
        list = this;
        info = this;
    }

    public String getDisplayName() {
        return job.getDisplayName();
    }

    public boolean isReadOnly() {
        return true;
    }

    private URL baseURL() throws MalformedURLException {
        return new URL(job.getUrl() + "ws/"); // NOI18N
    }

    private final Map<String,Long> lastModified = new HashMap<String,Long>();
    private final Map<String,Integer> size = new HashMap<String,Integer>();
    private final Map<String,Boolean> isDir = new HashMap<String,Boolean>();
    private final Map<String,byte[]> headers = new HashMap<String,byte[]>();

    public String[] children(String f) {
        String fSlash = f.length() > 0 ? f + "/" : ""; // NOI18N
        try {
            URL url = new URL(baseURL(), fSlash + "*plain*"); // NOI18N
            URLConnection conn = url.openConnection();
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
                    isDir.put(fSlash + n, true);
                } else {
                    kids.add(line);
                    isDir.put(fSlash + line, false);
                }
            }
            LOG.log(Level.FINE, "children: {0} -> {1}", new Object[] {url, kids});
            return kids.toArray(new String[kids.size()]);
        } catch (IOException x) {
            LOG.log(Level.FINE, "cannot list children of {0} in {1}: {2}", new Object[] {f, job, x});
            return new String[0];
        }
    }

    public boolean folder(String name) {
        assert isDir.containsKey(name) : name + " not in " + isDir;
        return isDir.get(name);
    }

    private URLConnection connection(String name) throws IOException {
        LOG.log(Level.FINE, "metadata in {0}: {1}", new Object[] {job, name});
        URLConnection conn = new URL(baseURL(), name).openConnection();
        lastModified.put(name, conn.getLastModified());
        int contentLength = conn.getContentLength();
        size.put(name, contentLength);
        if (contentLength >= 0 && contentLength < /* more than MIMEResolverImpl needs */ 4050) {
            InputStream is = conn.getInputStream();
            byte[] buf = new byte[contentLength];
            is.read(buf); // XXX readFully
            headers.put(name, buf);
        } // for bigger files, just reread content later if requested
        return conn;
    }

    public Date lastModified(String name) {
        if (name.equals("")) {
            return new Date(0);
        }
        assert isDir.containsKey(name) : name + " not in " + isDir;
        if (isDir.get(name)) {
            return new Date(0);
        }
        if (!lastModified.containsKey(name)) {
            try {
                connection(name);
            } catch (IOException x) {
                LOG.log(Level.INFO, "cannot get metadata for " + name + " in " + job, x);
                return new Date(0);
            }
        }
        return new Date(lastModified.get(name));
    }

    public long size(String name) {
        if (name.equals("")) {
            return 0;
        }
        assert isDir.containsKey(name) : name + " not in " + isDir;
        if (isDir.get(name)) {
            return 0;
        }
        if (!size.containsKey(name)) {
            try {
                connection(name);
            } catch (IOException x) {
                LOG.log(Level.INFO, "cannot get metadata for " + name + " in " + job, x);
                return 0;
            }
        }
        return size.get(name);
    }

    public InputStream inputStream(String name) throws FileNotFoundException {
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
}
