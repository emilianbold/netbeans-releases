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
package org.netbeans.modules.visualweb.project.jsf.ui;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.IOException;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.openide.ErrorManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author winstonp
 */
public class PageLayoutData {

    FileObject templateFileObject;

    public PageLayoutData(FileObject fileObject) {
        templateFileObject = fileObject;
    }

    public String getName() {
        String bundleName = (String) templateFileObject.getAttribute("SystemFileSystem.localizingBundle");
        String nameKey = (String) templateFileObject.getAttribute("name");
        if (nameKey != null) {
            try {
                return NbBundle.getBundle(bundleName).getString(nameKey);
            } catch (Exception exc) {
                // OK no bundle key found, ignore the exception and just use key as name value
                // Exception not logged because it is not mandatory to specify value in the bundle
                return nameKey;
            }
        } else {
            return templateFileObject.getName();
        }
    }

    public int getPosition() {
        return (Integer) templateFileObject.getAttribute("position");
    }

    public FileObject getFileObject() {
        return templateFileObject;
    }

    public Image getIcon() {
        return loadImage("icon");
    }

    public Image getPreviewImage() {
        return loadImage("previewImage");
    }

    public String getDescription() {
        String bundleName = (String) templateFileObject.getAttribute("SystemFileSystem.localizingBundle");
        String descriptionKey = (String) templateFileObject.getAttribute("description");
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

    public boolean isPageLayoutTemplate() {
        if (templateFileObject.getAttribute("pageLayoutTemplate") != null) {
            return (Boolean) templateFileObject.getAttribute("pageLayoutTemplate");
        } else {
            return false;
        }
    }

    private Image loadImage(String attributeName) {
        Object value = templateFileObject.getAttribute(attributeName);
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
        if (templateFileObject.getAttribute("resources") != null) {
            URL resourceZipUrl = (URL) templateFileObject.getAttribute("resources");
            ZipInputStream zipInputStream = null;
            try {
                zipInputStream = new ZipInputStream(resourceZipUrl.openConnection().getInputStream());
                ZipEntry zipEntry;
                while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                    String entryName = zipEntry.getName();
                    if (zipEntry.isDirectory()) {
                        FileUtil.createFolder(resourceRoot, entryName);
                    } else {
                        FileObject file = FileUtil.createData(resourceRoot, entryName);
                        FileLock lock = file.lock();
                        FileUtil.copy(zipInputStream, file.getOutputStream(lock));
                        lock.releaseLock();
                    }
                }
            } finally {
                if (zipInputStream != null) {
                    zipInputStream.close();
                }
            }
        }
    }
}
