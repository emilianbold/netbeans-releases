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
import java.util.Set;
import java.util.regex.Pattern;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Types;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.mobility.project.ui.customizer.ProjectProperties;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.mobility.cldcplatform.J2MEPlatform;
import org.netbeans.modules.mobility.project.DefaultPropertiesDescriptor;
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
    private J2MEPlatform activePlatform;
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
            String platform = (String) props.get(VisualPropertySupport.translatePropertyName(configuration, DefaultPropertiesDescriptor.PLATFORM_ACTIVE, true));
            final JavaPlatform[] platforms = JavaPlatformManager.getDefault().getPlatforms(null, new Specification(J2MEPlatform.SPECIFICATION_NAME, null));
            J2MEPlatform pl;
            for (int i = 0; platform != null && i < platforms.length; i++) {
                pl = (J2MEPlatform) platforms[i];
                if (platform.equals(pl.getDisplayName())){
                    activePlatform = pl;
                    break;
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
                    //apply brute force (classpath independent if platform is not available)
                    if (activePlatform != null ? isMIDlet2(root, fo): isMIDlet(fo)) synchronized (this) {
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
    
    private boolean isMIDlet2(FileObject root, final FileObject file){
        final Boolean[] result = new Boolean[]{false};
        ClassPath boot = activePlatform.getBootstrapLibraries();
        ClassPath rtm2 = ClassPath.getClassPath(root, ClassPath.EXECUTE);
        ClassPath rtm1 = ClassPath.getClassPath(root, ClassPath.COMPILE);
        ClassPath rtm = org.netbeans.spi.java.classpath.support.ClassPathSupport.createProxyClassPath(new ClassPath[]{rtm1, rtm2});
        final ClassPath clp =  org.netbeans.spi.java.classpath.support.ClassPathSupport.createProxyClassPath(
                new ClassPath[]{ClassPath.getClassPath(root, ClassPath.SOURCE), rtm1});

        ClasspathInfo cpInfo = ClasspathInfo.create(boot, rtm, clp);
        JavaSource js = JavaSource.create(cpInfo);
        try {

            js.runUserActionTask(new CancellableTask<CompilationController>() {

                public void run(CompilationController control) throws Exception {
                    TypeElement type = control.getElements().getTypeElement("javax.microedition.midlet.MIDlet");
                    if (type == null) {
                        return;
                    }
                    String name = clp.getResourceName(file);
                    if (name == null)
                        return;
                    name = name.substring(0, name.lastIndexOf('.')).replace('/', '.');
                    TypeElement xtype = control.getElements().getTypeElement(name);
                    if (xtype == null) {
                        return;
                    }
                    Set<Modifier> modifiers = xtype.getModifiers();
                    if (modifiers.contains(Modifier.ABSTRACT) || !modifiers.contains(Modifier.PUBLIC) ){
                        return;
                    }
                    Types types = control.getTypes();
                    result[0] = types.isSubtype(types.erasure(xtype.asType()), types.erasure(type.asType()));
                }

                public void cancel() {
                }
            }, true);
        } catch (IOException ioe) {
        }
        
        return result[0];
    }
}

