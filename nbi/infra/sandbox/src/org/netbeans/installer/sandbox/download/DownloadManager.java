/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */

package org.netbeans.installer.sandbox.download;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.installer.sandbox.download.Download.DownloadAdapter;
import org.netbeans.installer.sandbox.download.Download.DownloadState;
import org.netbeans.installer.sandbox.download.Download.DownloadEvent;
import org.netbeans.installer.sandbox.download.Download.DownloadListener;
import org.netbeans.installer.sandbox.download.proxy.Proxy.ProxyType;
import org.netbeans.installer.sandbox.download.proxy.Proxy;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.exceptions.DownloadException;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.progress.Progress;

/**
 * This class serves as a single entry point for the download module. Clients should
 * use the instance of this class to initiate downloads instead of addressing the
 * <code>Download</code> class directly. <code>DownloadManager</code> provides means
 * to initiate downloads in both blocking and non blocking modes.
 * This class is also responsible for managing proxies. Connections are expected to
 * address the <code>DownloadManager</code> in order to get the list of proxies that
 * they should use to connect to remote machines.
 *
 * @author Kirill Sorokin
 */
public class DownloadManager {
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    /**
     * The name of the system property which holds the host of the system SOCKS
     * proxy.
     */
    private static final String SOCKS_PROXY_HOST_PROPERTY =
            "socksProxyHost"; // NOI18N
    
    /**
     * The name of the system property which holds the port of the system SOCKS
     * proxy.
     */
    private static final String SOCKS_PROXY_PORT_PROPERTY =
            "socksProxyPort"; // NOI18N
    
    /**
     * The name of the system property which holds the host of the system HTTP
     * proxy.
     */
    private static final String HTTP_PROXY_HOST_PROPERTY =
            "http.proxyHost"; // NOI18N
    
    /**
     * The name of the system property which holds the port of the system HTTP
     * proxy.
     */
    private static final String HTTP_PROXY_PORT_PROPERTY =
            "http.proxyPort"; // NOI18N
    
    /**
     * The name of the system property which holds the list of hosts for which the
     * system HTTP proxy should be bypassed.
     */
    private static final String HTTP_NON_PROXY_HOSTS_PROPERTY =
            "http.nonProxyHosts"; // NOI18N
    
    /**
     * The name of the system property which holds the host of the system HTTPS
     * proxy.
     */
    private static final String HTTPS_PROXY_HOST_PROPERTY =
            "https.proxyHost"; // NOI18N
    
    /**
     * The name of the system property which holds the port of the system HTTPS
     * proxy.
     */
    private static final String HTTPS_PROXY_PORT_PROPERTY =
            "https.proxyPort"; // NOI18N
    
    /**
     * The name of the system property which holds the list of hosts for which the
     * system HTTPS proxy should be bypassed.
     */
    private static final String HTTPS_NON_PROXY_HOSTS_PROPERTY =
            "https.nonProxyHosts"; // NOI18N
    
    /**
     * The name of the system property which holds the host of the system FTP
     * proxy.
     */
    private static final String FTP_PROXY_HOST_PROPERTY =
            "ftp.proxyHost"; // NOI18N
    
    /**
     * The name of the system property which holds the port of the system FTP
     * proxy.
     */
    private static final String FTP_PROXY_PORT_PROPERTY =
            "ftp.proxyPort"; // NOI18N
    
    /**
     * The name of the system property which holds the list of hosts for which the
     * system FTP proxy should be bypassed.
     */
    private static final String FTP_NON_PROXY_HOSTS_PROPERTY =
            "ftp.nonProxyHosts"; // NOI18N
    
    /**
     * Resource bundle key name - warning message yelded when the source URI for a
     * requested download cannot be parsed.
     */
    private static final String KEY_CANNOT_PARSE_URI =
            "DownloadManager.exception.cannotParseURI"; // NOI18N
    
    /**
     * Resource bundle key name - warning message yelded when the destination file
     * for a requested download cannot be parsed.
     */
    private static final String KEY_CANNOT_CREATE_TEMP_FILE =
            "DownloadManager.exception.cannotCreateTempFile"; // NOI18N
    
    /////////////////////////////////////////////////////////////////////////////////
    // Static
    /**
     * The only instance of the <code>DownloadManager</code>. It's lazily
     * initialized.
     */
    private static DownloadManager instance;
    
    /**
     * Returns the instance of <code>DownloadManager</code>. If the instance does
     * not yet exist it is constructed.
     *
     * @return The instance of <code>DownloadManager</code>
     */
    public static synchronized DownloadManager getInstance() {
        if (instance == null) {
            instance = new DownloadManager();
        }
        
        return instance;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    /**
     * The list of proxies registered with the <code>DownloadManager</code>.
     * <code>Connection</code>s are expected to take this list into account when
     * connecting to remote machines.
     */
    private List<Proxy> proxies = new ArrayList<Proxy>();
    
    /**
     * Completed downloads cache. If a client requests a download from a specified
     * URI and states that a cached version will suffice, the cache is checked for
     * the URI being requested and if it present, the cached version is returned.
     * Caching is only supported for downloads in blocking mode and the client must
     * explicitly state that the download he is requesting can be placed into cache.
     */
    private Map<URI, File> downloadsCache = new HashMap<URI, File>();
    
    /**
     * Constructs a new instance of <code>DownloadManager</code>. Upon construction,
     * the system properties are checked for presence of system proxies and there
     * are any, they are registered with the <code>DownloadManager</code>.
     */
    private DownloadManager() {
        LogManager.log(ErrorLevel.MESSAGE, "initializing download manager subsystem");
        
        LogManager.log(ErrorLevel.DEBUG, "    parsing proxy servers defined by the system");
        Proxy proxy;
        
        LogManager.log(ErrorLevel.DEBUG, "        parsing system socks proxy");
        proxy = Proxy.parseProxy(ProxyType.SOCKS, SOCKS_PROXY_HOST_PROPERTY,
                SOCKS_PROXY_PORT_PROPERTY, null);
        if (proxy != null) {
            proxies.add(proxy);
        }
        
        LogManager.log(ErrorLevel.DEBUG, "        parsing system http proxy");
        proxy = Proxy.parseProxy(ProxyType.HTTP, HTTP_PROXY_HOST_PROPERTY,
                HTTP_PROXY_PORT_PROPERTY, HTTP_NON_PROXY_HOSTS_PROPERTY);
        if (proxy != null) {
            proxies.add(proxy);
        }
        
        LogManager.log(ErrorLevel.DEBUG, "        parsing system https proxy");
        proxy = Proxy.parseProxy(ProxyType.HTTPS, HTTPS_PROXY_HOST_PROPERTY,
                HTTPS_PROXY_PORT_PROPERTY, HTTPS_NON_PROXY_HOSTS_PROPERTY);
        if (proxy != null) {
            proxies.add(proxy);
        }
        
        LogManager.log(ErrorLevel.DEBUG, "        parsing system ftp proxy");
        proxy = Proxy.parseProxy(ProxyType.FTP, FTP_PROXY_HOST_PROPERTY,
                FTP_PROXY_PORT_PROPERTY, FTP_NON_PROXY_HOSTS_PROPERTY);
        if (proxy != null) {
            proxies.add(proxy);
        }
        
        LogManager.log(ErrorLevel.MESSAGE, "... download manager subsystem initialized correctly");
    }
    
    /**
     * Constructs an instance of <code>Download</code>. This method should be used
     * by the clients wishing to initiate a download in non-blocking mode.
     *
     * @param uri The source URI.
     * @param file The destination file.
     * @param options The options for the download.
     * @param listener The initial download listener.
     * @return An instance of <code>Download</code> constructed according to the
     *      supplied parameters.
     */
    public Download getDownload(URI uri, File file, DownloadOptions options,
            DownloadListener listener) {
        return new Download(uri, file, options, listener);
    }
    
    /**
     * Constructs an instance of <code>Download</code>. This method should be used
     * by the clients wishing to initiate a download in non-blocking mode.
     *
     * @param uri The source URI.
     * @param file The destination file.
     * @param options The options for the download.
     * @return An instance of <code>Download</code> constructed according to the
     *      supplied parameters.
     */
    public Download getDownload(URI uri, File file, DownloadOptions options) {
        return getDownload(uri, file, options, null);
    }
    
    /**
     * Constructs an instance of <code>Download</code>. This method should be used
     * by the clients wishing to initiate a download in non-blocking mode.
     *
     * @param uri The source URI.
     * @param filename The destination file name.
     * @param options The options for the download.
     * @param listener The initial download listener.
     * @return An instance of <code>Download</code> constructed according to the
     *      supplied parameters.
     * @throws java.net.URISyntaxException if the supplied string cannot be parsed
     *      into an <code>URI</code>.
     */
    public Download getDownload(String uri, String filename, 
            DownloadOptions options, DownloadListener listener) 
            throws URISyntaxException {
        return getDownload(new URI(uri), new File(filename), options, listener);
    }
    
    /**
     * Constructs an instance of <code>Download</code>. This method should be used
     * by the clients wishing to initiate a download in non-blocking mode.
     *
     * @param uri The source URI.
     * @param filename The destination file name.
     * @param options The options for the download.
     * @return An instance of <code>Download</code> constructed according to the
     *      supplied parameters.
     * @throws java.net.URISyntaxException if the supplied string cannot be parsed
     *      into an <code>URI</code>.
     */
    public Download getDownload(String uri, String filename, 
            DownloadOptions options) throws URISyntaxException {
        return getDownload(uri, filename, options, null);
    }
    
    /**
     * Performs a download in blocking mode. This method provides support for
     * caching. To enable it the client must explicitly define it in the download
     * options. The same download options property is used for both enabling cache
     * hits and completed download registration in the cache.
     *
     * @param uri The source URI.
     * @param file The destination file.
     * @param options The options for the download.
     * @throws org.netbeans.installer.utils.exceptions.DownloadException if the
     *      download fails for whatever reason.
     * @return The destination file.
     */
    public File download(URI uri, File file, DownloadOptions options)
            throws DownloadException {
        boolean cachingEnabled =
                options.getBoolean(DownloadOptions.CACHING_ENABLED);
        
        // check the cache for the given URI, if the URI is present in the cache -
        // validate the destination file corresponding to this URI
        File cacheHit = downloadsCache.get(uri);
        boolean cacheHitValid =
                (cacheHit != null) && cacheHit.exists() && cacheHit.isFile();
        
        // if we are allowed to return the cached file and the file is valid -
        // return it
        if (cachingEnabled && cacheHitValid) {
            try {
                FileUtils.copyFile(cacheHit, file);
                return file;
            } catch (IOException ex) {
                LogManager.log("faild transfer file from cache to dist",ex);
                return null;
            }
        }
        
        // otherwise perform the download
        File result = new DownloadHandler(uri, file, options).download();
        
        if (cachingEnabled) {
            downloadsCache.put(uri, result);
        }
        
        return result;
    }
    
    /**
     * Performs a download in blocking mode.
     *
     * @param uri The source URI.
     * @param file The destination file.
     * @param options The options for the download.
     * @throws org.netbeans.installer.utils.exceptions.DownloadException if the
     *      download fails for whatever reason.
     * @return The destination file.
     */
    public File download(String uri, File file, DownloadOptions options)
            throws DownloadException {
        try {
            return download(new URI(uri), file, options);
        } catch (URISyntaxException e) {
            String message = ResourceUtils.
                    getString(DownloadManager.class, KEY_CANNOT_PARSE_URI, uri);
            throw new DownloadException(message, e);
        }
    }
    
    public File download(String uri, File file) throws DownloadException {
        return download(uri, file, DownloadOptions.getDefaults());
    }
    
    /**
     * Performs a download in blocking mode.
     *
     * @param uri The source URI.
     * @param options The options for the download.
     * @throws org.netbeans.installer.utils.exceptions.DownloadException if the
     *      download fails for whatever reason.
     * @return The destination file.
     */
    public File download(URI uri, DownloadOptions options) 
            throws DownloadException {
        try {
            return download(uri, FileUtils.createTempFile(), options);
        } catch (IOException e) {
            String message = ResourceUtils.
                    getString(DownloadManager.class, KEY_CANNOT_CREATE_TEMP_FILE);
            throw new DownloadException(message, e);
        }
    }
    
    /**
     * Performs a download in blocking mode.
     *
     * @param uri The source URI.
     * @param options The options for the download.
     * @throws org.netbeans.installer.utils.exceptions.DownloadException if the
     *      download fails for whatever reason.
     * @return The destination file.
     */
    public File download(String uri, DownloadOptions options)
            throws DownloadException {
        try {
            return download(new URI(uri), options);
        } catch (URISyntaxException e) {
            String message = ResourceUtils.
                    getString(DownloadManager.class, KEY_CANNOT_PARSE_URI, uri);
            throw new DownloadException(message, e);
        }
    }
    
    /**
     * Performs a download in blocking mode.
     *
     * @param uri The source URI.
     * @throws org.netbeans.installer.utils.exceptions.DownloadException if the
     *      download fails for whatever reason.
     * @return The destination file.
     */
    public File download(URI uri) throws DownloadException {
        return download(uri, DownloadOptions.getDefaults());
    }
    
    /**
     * Performs a download in blocking mode.
     *
     * @param uri The source URI.
     * @throws org.netbeans.installer.utils.exceptions.DownloadException if the
     *      download fails for whatever reason.
     * @return The destination file.
     */
    public File download(String uri) throws DownloadException {
        return download(uri, DownloadOptions.getDefaults());
    }
    
    /**
     * Returns a list of registered proxies of the given type.
     *
     * @param type The type of proxies to return.
     * @return The list of proxies of the given type.
     */
    public List<Proxy> getProxies(ProxyType type) {
        List<Proxy> filteredProxies = new ArrayList<Proxy>();
        
        synchronized (proxies) {
            for (Proxy proxy: proxies) {
                if (proxy.getType() == type) {
                    filteredProxies.add(proxy);
                }
            }
        }
        
        return filteredProxies;
    }
    
    /**
     * Registers a new proxy with the <code>DownloadManager</code>.
     *
     * @param proxy The proxy to register.
     */
    public void addProxy(Proxy proxy) {
        synchronized (proxies) {
            proxies.add(proxy);
        }
    }
    
    /**
     * Removes a proxy from the list of registered proxies.
     *
     * @param proxy The proxy to remove.
     */
    public void removeProxy(Proxy proxy) {
        synchronized (proxies) {
            proxies.remove(proxy);
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Inner classes
    /**
     * A helper class which is used by the <code>DownloadManager</code> in order to
     * perform downloads in blocking mode.
     *
     * @author Kirill Sorokin
     */
    public static class DownloadHandler extends DownloadAdapter {
        /**
         * The <code>Download</code> object, which actually performs the download.
         */
        private Download download;
        
        /**
         * Indicates whether the download has completed/failed or is still running. 
         * The initial value is true, will be reset to false as soon as the 
         * download is completed or failed.
         */
        private boolean downloading = true;
        
        /**
         * The resulting state of the download, should be either COMPLETED or
         * FAILED.
         */
        private DownloadState downloadState;
        
        /**
         * The message with which the download failed, used as a message for the
         * <code>DownloadException</code> thrown in this case.
         */
        private String downloadMessage;
        
        /**
         * The exception with which the download failed, used as a cause for the
         * <code>DownloadException</code> thrown in this case.
         */
        private Throwable downloadException;
        
        /**
         * A Progress object to which to report download progress.
         */
        private Progress progress;
        
        /**
         * Constructs a new instance of <code>DownloadHandler</code>. It in its turn
         * creates a new instance of <code>Download</code>.
         *
         * @param uri The source URI.
         * @param file The destination file.
         * @param options The options for the download.
         */
        public DownloadHandler(URI uri, File file, DownloadOptions options) {
            download = DownloadManager.getInstance().
                    getDownload(uri, file, options, this);
        }
        
        /**
         * Starts the download and waits for it to complete or fail. If the download
         * fails, a <code>DownloadException</code> is thrown.
         * 
         * @return The destination file.
         * @throws org.netbeans.installer.utils.exceptions.DownloadException if the
         *      download fails.
         */
        public File download() throws DownloadException {
            download.start();
            
            while (downloading) {
                try {
                    Thread.sleep(DELAY);
                } catch (InterruptedException e) {
                    continue;
                }
            }
            
            // if the download completed successully return the file, throw an
            // exception otherwise
            if (downloadState == DownloadState.COMPLETED) {
                return download.getDestination();
            } else {
                throw new DownloadException(downloadMessage, downloadException);
            }
        }
        
        /**
         * Starts the download and waits for it to complete or fail. If the download
         * fails, a <code>DownloadException</code> is thrown. The progress is 
         * reported to the supplied Progress object.
         * 
         * @return The destination file.
         * @throws org.netbeans.installer.utils.exceptions.DownloadException if the
         *      download fails.
         */
        public File download(final Progress progress) throws DownloadException {
            this.progress = progress;
            return download();
        }
        
        /**
         * {@inheritDoc}
         */
        public void downloadRunning(DownloadEvent event) {
            if (progress != null) {
                progress.setPercentage(event.getSource().getPercentage());
            }
        }
        
        /**
         * {@inheritDoc}
         */
        public void downloadCompleted(DownloadEvent event) {
            downloading = false;
            downloadState = DownloadState.COMPLETED;
            
            if (progress != null) {
                progress.setPercentage(event.getSource().getPercentage());
            }
        }
        
        /**
         * {@inheritDoc}
         */
        public void downloadFailed(DownloadEvent event) {
            downloading = false;
            downloadState = DownloadState.FAILED;
            downloadMessage = event.getMessage();
            downloadException = event.getException();
            
            if (progress != null) {
                progress.setPercentage(event.getSource().getPercentage());
            }
        }
        
        /**
         * The amount of time in milliseconds, that we should sleep between checks 
         * for download state.
         */
        private static final int DELAY = 15;
    }
}
