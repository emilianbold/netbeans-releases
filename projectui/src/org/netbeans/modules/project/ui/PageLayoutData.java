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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.project.ui;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * Data model that represent page layouts defined via layer file
 * @author Winston Prakash
 */
public class PageLayoutData {

    FileObject pageLayoutFileObject;

    public PageLayoutData(FileObject fileObject) {
        pageLayoutFileObject = fileObject;
    }

    public String getName() {
        return (String) pageLayoutFileObject.getAttribute("name");
    }

    public String getDisplayName() {
        String bundleName = (String) pageLayoutFileObject.getAttribute("SystemFileSystem.localizingBundle");
        String nameKey = (String) pageLayoutFileObject.getAttribute("name");
        if (nameKey != null) {
            try {
                return NbBundle.getBundle(bundleName).getString(nameKey);
            } catch (Exception exc) {
                // OK no bundle key found, ignore the exception and just use key as name value
                // Exception not logged because it is not mandatory to specify value in the bundle
                return nameKey;
            }
        } else {
            return pageLayoutFileObject.getName();
        }
    }

    public int getPosition() {
        return (Integer) pageLayoutFileObject.getAttribute("position");
    }

    public FileObject getFileObject() {
        return pageLayoutFileObject;
    }

    public Image getIcon() {
        return loadImage("icon");
    }

    public Image getPreviewImage() {
        return loadImage("previewImage");
    }

    public String getDescription() {
        String bundleName = (String) pageLayoutFileObject.getAttribute("SystemFileSystem.localizingBundle");
        String descriptionKey = (String) pageLayoutFileObject.getAttribute("description");
        if (descriptionKey != null) {
            try {
                return NbBundle.getBundle(bundleName).getString(descriptionKey);
            } catch (Exception exc) {
                // OK no bundle key found, ignore the exception and just use key as description value
                // Exception not logged because it is not mandatory to specify value in the bundle
                return descriptionKey;
            }
        } else {
            return NbBundle.getMessage(PageLayoutData.class, "NO_PREVIEW_TEXT");
        }
    }

    public boolean isPageLayout() {
        if (pageLayoutFileObject.getAttribute("pageLayout") != null) {
            return (Boolean) pageLayoutFileObject.getAttribute("pageLayout");
        } else {
            return false;
        }
    }

    private Image loadImage(String attributeName) {
        Object value = pageLayoutFileObject.getAttribute(attributeName);
        if (value instanceof Image) {
            return (Image) value;
        }
        if (value instanceof URL) {
            try {
                return Toolkit.getDefaultToolkit().getImage((URL) value);
            } catch (Exception e) {
                ErrorManager.getDefault().notify(e);
            }
        }
        return null;
    }

    public void copyResources(FileObject resourceRoot) throws IOException {
        if (pageLayoutFileObject.getAttribute("resources") != null) {
            URL resourceZipUrl = (URL) pageLayoutFileObject.getAttribute("resources");
            unzip(resourceRoot, resourceZipUrl);
        }
    }
    
    /**
     * Unzip the zip from the URL in to the resource root folder
     * @param resourceRoot
     * @param resourceZipUrl
     * @throws java.io.IOException
     */
    private void unzip(FileObject resourceRoot, URL resourceZipUrl) throws IOException {
        //URL resourceZipUrl = zipFile.getURL();
        ZipInputStream zipInputStream = null;
        try {
            zipInputStream = new ZipInputStream(resourceZipUrl.openConnection().getInputStream());
            ZipEntry zipEntry;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                String entryName = zipEntry.getName();
                if (entryName.startsWith("__MACOSX") || entryName.contains("DS_Store")) {
                    continue;
                }
                if (zipEntry.isDirectory()) {
                    FileUtil.createFolder(resourceRoot, entryName);
                } else {
                    File file = new File(FileUtil.toFile(resourceRoot), entryName);
                    FileChannel channel = new RandomAccessFile(file, "rw").getChannel();
                    FileLock lock = channel.lock();
                    OutputStream out = Channels.newOutputStream(channel);
                    FileUtil.copy(zipInputStream, out);
                    lock.release();
                }
            }
        } finally {
            if (zipInputStream != null) {
                zipInputStream.close();
            }

        }
    }
}