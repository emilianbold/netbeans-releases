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
package org.netbeans.modules.web.client.javascript.debugger.filesystem;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Level;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.util.Enumerations;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author quynguyen
 */
public class URLFileObject extends FileObject {
    private static final long serialVersionUID = -129282139186842072L;
    private static final String EXC_STRING = "FileObject is read-only"; // NOI18N

    private URL actualURL;
    private final URL sourceURL;
    private final String name;
    private final String ext;
    private final URLFileSystem filesystem;
    private final URLFileObject parent;
    private transient URLContent cachedContent;
    private final Object cacheLock = new Object();
    private boolean cacheInvalid = false;

    private final String relativePath;
    private Hashtable<String,Object> attributes;

    private transient List<FileChangeListener> listeners;

    protected URLFileObject(URLFileSystem filesystem) {
        super();

        this.name = "";
        this.ext = "";
        this.filesystem = filesystem;
        this.parent = null;
        this.sourceURL = null;
        this.actualURL = null;
        this.relativePath = "";
    }

    public URLFileObject(URL sourceURL, URLFileSystem filesystem, URLFileObject parent) {
        super();

        this.filesystem = filesystem;
        this.parent = parent;

        this.sourceURL = sourceURL;
        this.actualURL = sourceURL;

        String path = this.sourceURL.toExternalForm();
        
        if (path.endsWith("/")) {
            // remove trailing slash to maintain consistency
            this.relativePath = path.substring(0, path.length()-1);
        }else {
            this.relativePath = path;
        }
        
        String nameExt = this.relativePath.substring(this.relativePath.lastIndexOf("/") + 1);
        int dot = nameExt.lastIndexOf(".");

        // the '.' separates extension if it is not the first nor the last character
        if (dot > 0 && dot < nameExt.length() - 1) {
            name = nameExt.substring(0, dot);
            ext = nameExt.substring(dot + 1, nameExt.length());
        } else {
            name = nameExt;
            ext = "";
        }
    }
    
    URL getSourceURL() {
        return sourceURL;
    }

    // For ignore-query-string support
    public URL getActualURL() {
        synchronized (cacheLock) {
            return actualURL;
        }
    }

    public String getDisplayName() {
        return getActualURL().toExternalForm();
    }

    public void setActualURL(URL url) {
        boolean changed = false;
        synchronized (cacheLock) {
            if (!actualURL.toExternalForm().equals(url.toExternalForm())) {
                actualURL = url;
                changed = true;
            }
        }

        if (changed) {
            invalidate();
            try {
                ((URLFileSystem) getFileSystem()).fireStatusChange(this);
            } catch (Exception ex) {
                Log.getLogger().log(Level.INFO, "Unexpected exception while changing URLFileObject URL", ex);
            }
        }
    }

    @Override
    public FileSystem getFileSystem() throws FileStateInvalidException {
        return filesystem;
    }

    @Override
    public FileObject getParent() {
        return parent;
    }

    @Override
    public boolean isRoot() {
        return false;
    }

    @Override
    public boolean isData() {
        return true;
    }

    @Override
    public boolean isFolder() {
        return false;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public Date lastModified() {
        return new Date(0);
    }

    @Override
    public Object getAttribute(String attrName) {
        if (attributes == null) {
            return null;
        }else {
            return attributes.get(attrName);
        }
    }

    @Override
    public void setAttribute(String attrName, Object value) throws IOException {
        if (attributes == null) {
            attributes = new Hashtable<String, Object>();
        }

        attributes.put(attrName, value);
    }

    @Override
    public Enumeration<String> getAttributes() {
        if (attributes == null) {
            return Enumerations.empty();
        }else {
            return attributes.keys();
        }
    }

    @Override
    public synchronized void addFileChangeListener(FileChangeListener fcl) {
        if (listeners == null) {
            listeners = new ArrayList<FileChangeListener>();
        }

        listeners.add(fcl);
    }

    @Override
    public synchronized void removeFileChangeListener(FileChangeListener fcl) {
        if (listeners != null) {
            listeners.remove(fcl);
        }
    }

    @Override
    public FileLock lock() throws IOException {
        return FileLock.NONE;
    }

    @Override
    public FileObject[] getChildren() {
        return new FileObject[0];
    }

    @Override
    public FileObject getFileObject(String name, String ext) {
        return null;
    }

    @Override
    public InputStream getInputStream() throws FileNotFoundException {
        if (isData()) {
            URLContentProvider provider = filesystem.getContentProvider();
            synchronized (cacheLock) {
                if ( (cacheInvalid || cachedContent == null) && provider != null) {
                    if (cachedContent != null) {
                        URLContent content = provider.getContent(actualURL);
                        InputStream is = null;
                        try {
                            is = content.getInputStream();
                        } catch (IOException ex) {
                            is = null;
                        }
                        
                        if (is != null) {
                            cacheInvalid = false;
                            cachedContent = new BufferedURLContent(is);
                        }
                    } else {
                        cachedContent = new BufferedURLContent(provider.getContent(actualURL));
                    }
                } else if (provider == null && cachedContent == null) {
                    String defaultMsg = NbBundle.getMessage(URLFileObject.class, "NO_CONTENT_MSG");
                    return new ByteArrayInputStream(defaultMsg.getBytes());
                }
            }
            try {
                return cachedContent.getInputStream();
            } catch (IOException ex) {
                throw new FileNotFoundException("Could not open InputStream for URL: " + actualURL.toExternalForm());
            }
        } else {
            return null;
        }
    }

    @Override
    public long getSize() {
        return 0L;
    }

    @Override
    @Deprecated
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public boolean canWrite() {
        return false;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getExt() {
        return ext;
    }

    @Override
    public String getPath() {
        return relativePath;
    }

    @Override
    public String getMIMEType() {
        String mime = super.getMIMEType();
        
        if (URLFileSystem.HTML_MIMETYPE.equals(mime) ||
                URLFileSystem.JAVASCRIPT_MIMETYPE.equals(mime) ||
                URLFileSystem.CSS_MIMETYPE.equals(mime)) {
            return mime;
        }else {
            return URLFileSystem.HTML_MIMETYPE;
        }
    }    
    
    @Override
    @Deprecated
    public void setImportant(boolean b) {
    }

    @Override
    public OutputStream getOutputStream(FileLock lock) throws IOException {
        throw new IOException(EXC_STRING);
    }

    @Override
    public void delete(FileLock lock) throws IOException {
        throw new IOException(EXC_STRING);
    }

    @Override
    public void rename(FileLock lock, String name, String ext) throws IOException {
        throw new IOException(EXC_STRING);
    }

    @Override
    public FileObject createFolder(String name) throws IOException {
        throw new IOException(EXC_STRING);
    }

    @Override
    public FileObject createData(String name, String ext) throws IOException {
        throw new IOException(EXC_STRING);
    }

    public void invalidate() {
        synchronized (cacheLock) {
            cacheInvalid = true;
        }
        
        FileChangeListener[] listenersArr = null;
        synchronized (this) {
            if (listeners == null) {
                return;
            }
            listenersArr = listeners.toArray(new FileChangeListener[listeners.size()]);
        }
        
        FileEvent event = new FileEvent(this, this, true);
        for (FileChangeListener listener : listenersArr) {
            listener.fileChanged(event);
        }
    }
}
