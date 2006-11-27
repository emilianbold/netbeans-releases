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

/*
 * MIDletScanner.java
 *
 * Created on 27. May 2004, 15:44
 */
package org.netbeans.modules.mobility.project.ui.customizer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.mobility.project.ui.customizer.ProjectProperties;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.mobility.project.DefaultPropertiesDescriptor;
import org.netbeans.modules.mobility.project.ui.customizer.VisualClassPathItem;
import org.netbeans.spi.mobility.project.ui.customizer.support.VisualPropertySupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.RequestProcessor;

/**
 *
 * @author  Adam Sotona
 */
public class MIDletScanner implements Runnable {
    
    private final ProjectProperties props;
    private final HashMap<FileObject,HashSet<String>> roots2icons = new HashMap<FileObject,HashSet<String>>();
    private final HashMap<FileObject,HashSet<String>> roots2midlets = new HashMap<FileObject,HashSet<String>>();
    private final HashMap<FileObject,HashSet<DefaultComboBoxModel>> roots2iconModels = new HashMap<FileObject,HashSet<DefaultComboBoxModel>>();
    private final HashMap<FileObject,HashSet<DefaultComboBoxModel>> roots2midletModelts = new HashMap<FileObject,HashSet<DefaultComboBoxModel>>();
    private final HashSet<ChangeListener> listeners = new HashSet<ChangeListener>();
    private boolean parsing = false;
    private static Reference<MIDletScanner> cache = new WeakReference(null);
    
    
    public static MIDletScanner getDefault(ProjectProperties props) {
        MIDletScanner sc = cache.get();
        if (sc == null || sc.props != props) {
            sc = new MIDletScanner(props);
            cache = new WeakReference(sc);
        }
        return sc;
    }
    
    private MIDletScanner(ProjectProperties props) {
        this.props = props;
    }
    
    public boolean isScanning() {
        return parsing;
    }
    
    public void scan(final DefaultComboBoxModel midlets, final DefaultComboBoxModel icons, final String configuration, final ChangeListener l) {
        synchronized (this) {
            for ( final FileObject root : getRootsFor(configuration) ) {
                if (icons != null) {
                    icons.removeAllElements();
                    HashSet<DefaultComboBoxModel> models = roots2iconModels.get(root);
                    if (models == null) {
                        models = new HashSet<DefaultComboBoxModel>();
                        roots2iconModels.put(root, models);
                    }
                    models.add(icons);
                }
                if (midlets != null) {
                    midlets.removeAllElements();
                    HashSet<DefaultComboBoxModel> models = roots2midletModelts.get(root);
                    if (models == null) {
                        models = new HashSet<DefaultComboBoxModel>();
                        roots2midletModelts.put(root, models);
                    }
                    models.add(midlets);
                }
            }
            listeners.add(l);
            if (!parsing) {
                parsing = true;
                RequestProcessor.getDefault().post(this);
            }
        }
    }
    
//------------------------------------------------------------------------------
    
    private void fireStateChanged() {
        final ChangeEvent e = new ChangeEvent(this);
        for (ChangeListener chl:listeners) {
            chl.stateChanged(e);
        }
        listeners.clear();
    }
    
    private Collection<FileObject> getArchiveRoots(final FileObject fo[]) {
        final ArrayList<FileObject> files = new ArrayList<FileObject>(fo.length);
        for (FileObject foi:fo) {
            files.add(FileUtil.isArchiveFile(foi) ? FileUtil.getArchiveRoot(foi) : foi);
        }
        return files;
    }
    
    private HashSet<FileObject> getRootsFor(final String configuration) {
        final HashSet<FileObject> roots = new HashSet<FileObject>();
        roots.add(props.getSourceRoot());
        List<VisualClassPathItem> cpItems = (List<VisualClassPathItem>)props.get(VisualPropertySupport.translatePropertyName(configuration, DefaultPropertiesDescriptor.LIBS_CLASSPATH, true));
        if (cpItems == null) cpItems = (List<VisualClassPathItem>)props.get(DefaultPropertiesDescriptor.LIBS_CLASSPATH);
        if (cpItems == null) return roots;
        for (final VisualClassPathItem item:cpItems) {
            if (VisualClassPathItem.TYPE_ARTIFACT == item.getType()) {
                final AntArtifact aa = (AntArtifact) item.getElement();
                if (aa != null) roots.addAll(getArchiveRoots(aa.getArtifactFiles()));
            } else if (VisualClassPathItem.TYPE_JAR == item.getType() || VisualClassPathItem.TYPE_FOLDER == item.getType()) {
                final File f = (File)item.getElement();
                if (f != null) try {
                    final FileObject fo = FileUtil.toFileObject(f);
                    if (fo != null) roots.add(FileUtil.isArchiveFile(fo) ? FileUtil.getArchiveRoot(fo) : fo);
                } catch (IllegalArgumentException iae) {}
            } else if (VisualClassPathItem.TYPE_LIBRARY == item.getType()) {
                final Library l = (Library)item.getElement();
                if (l != null) {
                    final Iterator iter = l.getContent("classpath").iterator(); //NOI18N
                    while (iter.hasNext()) {
                        final FileObject fo = URLMapper.findFileObject((URL)iter.next());
                        if (fo != null) roots.add(FileUtil.isArchiveFile(fo) ? FileUtil.getArchiveRoot(fo) : fo);
                    }
                }
            }
        }
        roots.remove(null);
        return roots;
    }
    
    public void run() {
        FileObject root = null;
        HashSet<String> midlets, icons;
        while (true) {
            synchronized (this) {
                HashSet<DefaultComboBoxModel> models = roots2iconModels.remove(root);
                if (models != null) {
                    fillModels(models, roots2icons.get(root));
                }
                models = roots2midletModelts.remove(root);
                if (models != null) {
                    fillModels(models, roots2midlets.get(root));
                }
                root = getNextRoot();
                if (root == null) {
                    parsing = false;
                    fireStateChanged();
                    return;
                }
                if (roots2icons.containsKey(root)) continue;
                icons = new HashSet<String>();
                midlets = new HashSet<String>();
                roots2icons.put(root, icons);
                roots2midlets.put(root, midlets);
            }
            try {
                scanForMIDletsAndIcons(root, icons, midlets);
            } catch (Exception e) {
                //don't allow to leave run() this way
            }
        }
    }
    
    private FileObject getNextRoot() {
        if (!roots2iconModels.isEmpty()) return roots2iconModels.keySet().iterator().next();
        if (!roots2midletModelts.isEmpty()) return roots2midletModelts.keySet().iterator().next();
        return null;
    }
    
    private void fillModels(final HashSet<DefaultComboBoxModel> models, final HashSet<String> elements) {
        for (final DefaultComboBoxModel m : models) {
            for (final String s : elements) {
                if (m.getIndexOf(s) < 0) m.addElement(s);
            }
        }
    }
    
    private void scanForMIDletsAndIcons(final FileObject root, final HashSet<String> icons, final HashSet<String> midlets) {
        final String rootPath = root.getPath();
        final int rootLength = rootPath.length();
        final Enumeration en = root.getChildren(true);
        while (en.hasMoreElements()) {
            final FileObject fo = (FileObject)en.nextElement();
            if (fo.isData()) {
                final String ext = fo.getExt().toLowerCase();
                if ("png".equals(ext)) { // NOI18N
                    String name = fo.getPath().substring(rootLength);
                    if (!name.startsWith("/")) name = "/" + name; //NOI18N
                    synchronized (this) {
                        icons.add(name);
                    }
                } else if (("java".equals(ext) || "class".equals(ext))) { // NOI18N
                    if (isMIDlet(fo)) synchronized (this) {
                            String name = FileUtil.getRelativePath(root, fo);
                            midlets.add(name.substring(0, name.length() - ext.length() - 1).replace('/', '.').replace('\\', '.'));
                    }
                }
            }
        }
    }
    
    private static Pattern p = Pattern.compile("\\s+extends\\s+(javax\\.microedition\\.midlet\\.)?MIDlet");//NOI18N
    
    private boolean isMIDlet(FileObject fo) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(fo.getInputStream()));
            String s;
            while((s = br.readLine()) != null) {
                if (s.indexOf("javax/microedition/midlet/MIDlet") >= 0 || p.matcher(s).find()) return true; //NOI18N
            }
        } catch (IOException ioe) {
        } finally {
            if (br != null) try {br.close();} catch (IOException ioe) {}
        } 
        return false;
    }
}

