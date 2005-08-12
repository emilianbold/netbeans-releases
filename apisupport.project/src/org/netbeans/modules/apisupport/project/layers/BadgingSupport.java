/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.layers;

import java.awt.Image;
import java.awt.Toolkit;
import java.beans.BeanInfo;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.apisupport.project.Util;
import org.openide.ErrorManager;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStatusEvent;
import org.openide.filesystems.FileStatusListener;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.RequestProcessor;

/**
 * Handles addition of badges to a filesystem a la system filesystem.
 * Specifically interprets SystemFileSystem.localizingBundle and
 * SystemFileSystem.icon (and SystemFileSystem.icon32).
 * Largely copied from org.netbeans.core.projects.SystemFileSystem.
 * @author Jesse Glick
 */
final class BadgingSupport implements FileSystem.Status, FileChangeListener {
    
    /** for branding/localization like "_f4j_ce_ja"; never null, at worst "" */
    private String suffix = "";
    /** classpath in which to look up resources; may be null but then nothing will be found... */
    private ClassPath classpath;
    private final FileSystem fs;
    private final FileChangeListener fileChangeListener;
    private final List/*<FileStatusListener>*/ listeners = new ArrayList();
    
    public BadgingSupport(FileSystem fs) {
        this.fs = fs;
        fileChangeListener = FileUtil.weakFileChangeListener(this, null);
    }
    
    public void setClasspath(ClassPath classpath) {
        this.classpath = classpath;
    }
    
    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }
    
    public void addFileStatusListener(FileStatusListener l) {
        listeners.add(l);
    }
    
    public void removeFileStatusListener(FileStatusListener l) {
        listeners.remove(l);
    }
    
    private void fireFileStatusChanged(FileStatusEvent e) {
        Iterator it = listeners.iterator();
        while (it.hasNext()) {
            ((FileStatusListener) it.next()).annotationChanged(e);
        }
    }
    
    public String annotateName(String name, Set files) {
        return annotateNameGeneral(name, files, "<root folder>", suffix, fileChangeListener, classpath);
    }
    
    private static String annotateNameGeneral(String name, Set files, String rootname,
            String suffix, FileChangeListener fileChangeListener, ClassPath cp) {
        Iterator it = files.iterator();
        while (it.hasNext()) {
            FileObject fo = (FileObject) it.next();
            String bundleName = (String) fo.getAttribute("SystemFileSystem.localizingBundle"); // NOI18N
            if (bundleName != null) {
                try {
                    URL u = LayerUtils.currentify(new URL("nbresloc:/" + // NOI18N
                            bundleName.replace('.', '/') +
                            ".properties"), // NOI18N
                            suffix, cp);
                    InputStream is = u.openStream();
                    try {
                        Properties p = new Properties();
                        p.load(is);
                        String key = fo.getPath();
                        String val = p.getProperty(key);
                        // Listen to changes in the origin file if any...
                        FileObject ufo = URLMapper.findFileObject(u);
                        if (ufo != null) {
                            ufo.removeFileChangeListener(fileChangeListener);
                            ufo.addFileChangeListener(fileChangeListener);
                            // In case a sibling bundle is added, that may be relevant:
                            ufo.getParent().removeFileChangeListener(fileChangeListener);
                            ufo.getParent().addFileChangeListener(fileChangeListener);
                        }
                        if (val != null) return val;
                        // if null, fine--normal for key to not be found
                    } finally {
                        is.close();
                    }
                } catch (IOException ioe) {
                    // For debugging; SFS will rather notify a problem separately...
                    Util.err.notify(ErrorManager.INFORMATIONAL, ioe);
                    return name + " <no such bundle: " + bundleName + ">";
                }
            }
        }
        if (files.size() == 1 && ((FileObject) files.iterator().next()).isRoot()) {
            return rootname;
        }
        return name;
    }
    
    public Image annotateIcon(Image icon, int type, Set files) {
        return annotateIconGeneral(icon, type, files, suffix, fileChangeListener, classpath);
    }
    
    private static Image annotateIconGeneral(Image icon, int type, Set files, String suffix,
            FileChangeListener fileChangeListener, ClassPath cp) {
        String attr;
        if (type == BeanInfo.ICON_COLOR_16x16) {
            attr = "SystemFileSystem.icon"; // NOI18N
        } else if (type == BeanInfo.ICON_COLOR_32x32) {
            attr = "SystemFileSystem.icon32"; // NOI18N
        } else {
            return icon;
        }
        Iterator it = files.iterator();
        while (it.hasNext()) {
            FileObject fo = (FileObject) it.next();
            Object value = fo.getAttribute(attr);
            if (value instanceof Image) {
                // #18832
                return (Image)value;
            }
            if (value != null) {
                try {
                    URL u = LayerUtils.currentify((URL) value, suffix, cp);
                    FileObject ufo = URLMapper.findFileObject(u);
                    if (ufo != null) {
                        ufo.removeFileChangeListener(fileChangeListener);
                        ufo.addFileChangeListener(fileChangeListener);
                    }
                    return Toolkit.getDefaultToolkit().getImage(u);
                } catch (Exception e) {
                    //e.printStackTrace(LayerDataNode.getErr());
                    Util.err.notify(ErrorManager.INFORMATIONAL, e);
                }
            }
        }
        return icon;
    }
    
    // Listen to changes in
    // bundles & icons used to annotate names. If these change,
    // the filesystem needs to show something else. Properly we would
    // keep track of *which* file changed and thus which of our resources
    // is affected. Practically this would be a lot of work and gain
    // very little.
    public void fileDeleted(FileEvent fe) {
        // not ineresting here
    }
    public void fileFolderCreated(FileEvent fe) {
        // does not apply to us
    }
    public void fileDataCreated(FileEvent fe) {
        // In case a file was created that makes an annotation be available.
        // We are listening to the parent folder, so if e.g. a new branded variant
        // of a bundle is added, the display ought to be refreshed accordingly.
        someFileChange(fe);
    }
    public void fileAttributeChanged(FileAttributeEvent fe) {
        // don't care about attributes on included files...
    }
    public void fileRenamed(FileRenameEvent fe) {
        someFileChange(fe);
    }
    public void fileChanged(FileEvent fe) {
        someFileChange(fe);
    }
    private void someFileChange(FileEvent fe) {
        final boolean expected = fe.isExpected();
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                // If used as nbres: annotation, fire status change.
                fireFileStatusChanged(new FileStatusEvent(fs, true, true));
            }
        });
    }
    
}
