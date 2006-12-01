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
 *
 * $Id$
 */
package org.netbeans.installer.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.installer.Installer;
import org.netbeans.installer.downloader.DownloadManager;
import org.netbeans.installer.downloader.Pumping;
import org.netbeans.installer.downloader.Pumping.Section;
import org.netbeans.installer.downloader.PumpingsQueue;
import org.netbeans.installer.downloader.DownloadListener;
import org.netbeans.installer.utils.exceptions.DownloadException;
import org.netbeans.installer.utils.helper.Pair;
import org.netbeans.installer.utils.progress.Progress;

/**
 *
 * @author Danila_Dugurov
 */
public class FileProxy {
    
    private final File tmpDir = new File(Installer.DEFAULT_LOCAL_DIRECTORY_PATH, "tmp");
    private final Map<String, File> cache = new HashMap<String, File>();
    {
        tmpDir.mkdirs();
        tmpDir.deleteOnExit();
    }
    DownloadListener listener = new MyListener();
    {
        DownloadManager.instance.registerListener(listener);
    }
    
    public static final FileProxy proxy = new FileProxy();
    
    public static FileProxy getInstance() {
        return proxy;
    }
    
    public void deleteFile(String uri) throws IOException {
        final File file = cache.get(uri);
        if (uri != null && uri.startsWith("file")) return;
        if (file != null) FileUtils.deleteFile(file);
        cache.remove(uri);
    }
    
    public void deleteFile(URI uri) throws IOException {
        deleteFile(uri.toString());
    }
    public void deleteFile(URL url) throws IOException {
        deleteFile(url.toString());
    }
    
    public File getFile(URL url) throws DownloadException {
        return getFile(url, null, false);
    }
    public File getFile(String uri) throws DownloadException {
        return getFile(uri, null, null);
    }
    
    public File getFile(String uri, boolean deleteOnExit) throws DownloadException {
        return getFile(uri, null, null, deleteOnExit);
    }
    
    public File getFile(String uri, ClassLoader loader) throws DownloadException {
        return getFile(uri, null, loader);
    }
    
    public File getFile(URI uri, Progress progress)  throws DownloadException {
        return getFile(uri, progress, null, false);
    }
    
    public File getFile(String uri, Progress progress, ClassLoader loader) throws DownloadException{
        return getFile(uri, progress, loader, false);
    }
    
    public File getFile(String uri, Progress progress, ClassLoader loader, boolean deleteOnExit) throws DownloadException {
        final URI myUri;
        try {
            myUri = new URI(uri);
        } catch (URISyntaxException ex) {
            throw new DownloadException("uri:" + uri, ex);
        }
        return getFile(myUri, progress, loader, deleteOnExit);
    }
    
    public File getFile(URI uri, boolean deleteOnExit) throws DownloadException {
        return getFile(uri, null, null, deleteOnExit);
    }
    
    public File getFile(URI uri) throws DownloadException {
        return getFile(uri, null, null, false);
    }
    
    public File getFile(URI uri, Progress progress, ClassLoader loader, boolean deleteOnExit) throws DownloadException {
        if (cache.containsKey(uri.toString()) && cache.get(uri.toString()).exists()) {
            return cache.get(uri.toString());
        }
        if (uri.getScheme().equals("file")) {
            File file = new File(uri);
            if (!file.exists()) throw new DownloadException("file not exist: " + uri);
            return file;
        } else if (uri.getScheme().equals("resource")) {
            OutputStream out  = null;
            try {
                String path = uri.getSchemeSpecificPart();
                File file = new File(tmpDir, path.substring(path.lastIndexOf('/')));
                String fileName = file.getName();
                File parent = file.getParentFile();
                for (int i = 0; file.exists(); i++) {
                    file = new File(parent, fileName + "." + i);
                }
                file.createNewFile();
                file.deleteOnExit();
                final InputStream resource = (loader != null ? loader: getClass().getClassLoader()).getResourceAsStream(uri.getSchemeSpecificPart());
                out = new FileOutputStream(file);
                if (resource == null) throw new DownloadException("resource:" + uri + "not found");
                StreamUtils.transferData(resource, out);
                cache.put(uri.toString(), file);
                return file;
            } catch(IOException ex) {
                throw new DownloadException("I/O error has occures", ex);
            } finally {
                if (out != null)
                    try {
                        out.close();
                    } catch (IOException ignord) {}
            }
        } else if (uri.getScheme().startsWith("http")) {
            try {
                final File file = getFile(uri.toURL(), progress, deleteOnExit);
                cache.put(uri.toString(), file);
                return file;
            } catch(MalformedURLException ex) {
                throw new DownloadException("malformed url: " + uri, ex);
            }
        }
        throw new DownloadException("unsupported sheme: " + uri.getScheme());
    }
    
    Progress progress;
    URL url;
    protected synchronized File getFile(final URL url,final Progress progress, boolean deleteOnExit) throws DownloadException {
        this.progress = progress;
        this.url = url;
        if (!DownloadManager.instance.isActive())
            DownloadManager.instance.invoke();
        DownloadManager.instance.queue().add(url);
        try {
            wait();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        final File file = cache.get(url.toString());
        if (file == null) throw new DownloadException("faild to download" + url);
        if (deleteOnExit) file.deleteOnExit();
        this.progress = null;
        return file;
    }
    
    private class MyListener implements DownloadListener {
        public void pumpingUpdate(String id) {
            final Pumping pumping = DownloadManager.instance.queue().getById(id);
            if (pumping != null) {
                if (progress != null) {
                    progress.setDetail("downloding: " + pumping.declaredURL());
                    if (pumping.length() > 0) {
                        final long length = pumping.length();
                        long per = 0;
                        for (Section section: pumping.getSections()) {
                            final Pair<Long, Long> pair = section.getRange();
                            per += section.offset() - pair.getFirst();
                        }
                        progress.setPercentage((int)(per * 100 / length));
                    }
                }
            }
        }
        
        public void pumpingStateChange(String id) {
            final Pumping pumping = DownloadManager.instance.queue().getById(id);
            if (pumping != null) {
                if (progress != null) {
                    progress.setDetail(pumping.state().toString().toLowerCase() +": "
                        + pumping.declaredURL());
                }
                switch (pumping.state()) {
                    case FINISHED:
                        cache.put(url.toString(), pumping.outputFile());
                    case FAILED: synchronized (FileProxy.this) {
                        FileProxy.this.notify();
                    }
                }
            } else {
                synchronized (FileProxy.this) {
                    FileProxy.this.notify();
                }
            }
        }
        
        public void pumpingAdd(String id) {
            final Pumping pumping = DownloadManager.instance.queue().getById(id);
            if (pumping != null) {
                if (progress != null) {
                    progress.setDetail("scheduled: " + pumping.declaredURL());
                }
            }
        }
        
        
        public void pumpingDelete(String id) {
        }
        
        public void queueReset() {
        }
        
        public void pumpsInvoke() {
        }
        
        public void pumpsTerminate() {
        }
    }
}
