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

package org.netbeans.modules.apisupport.project.layers;

import java.awt.Image;
import java.awt.Toolkit;
import java.beans.BeanInfo;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JSeparator;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.apisupport.project.Util;
import org.openide.ErrorManager;
import org.openide.awt.Actions;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileStatusEvent;
import org.openide.filesystems.FileStatusListener;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.Presenter;

/**
 * Handles addition of badges to a filesystem a la system filesystem.
 * Specifically interprets the following attributes:
 * <ul>
 * <li><tt>SystemFileSystem.localizingBundle</tt></li>
 * <li><tt>displayName</tt></li>
 * <li><tt>SystemFileSystem.icon</tt></li>
 * <li><tt>SystemFileSystem.icon32</tt></li>
 * </ul>
 * Also tries to provide display labels for InstanceDataObject's.
 * Parts copied from <tt>org.netbeans.core.projects.SystemFileSystem</tt>.
 * @author Jesse Glick
 */
final class BadgingSupport implements FileSystem.Status, FileChangeListener {

    /** for branding/localization like "_f4j_ce_ja"; never null, but may be "" */
    private String suffix = "";
    /** classpath in which to look up resources; may be null but then nothing will be found... */
    private ClassPath classpath;
    private final FileSystem fs;
    private final FileChangeListener fileChangeListener;
    private final List<FileStatusListener> listeners = new ArrayList<FileStatusListener>();
    
    public BadgingSupport(FileSystem fs) {
        this.fs = fs;
        fileChangeListener = FileUtil.weakFileChangeListener(this, null);
        fs.addFileChangeListener(fileChangeListener);
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
        return annotateNameGeneral(name, files, suffix, fileChangeListener, classpath);
    }
    
    private static String annotateNameGeneral(String name, Set files, String suffix, FileChangeListener fileChangeListener, ClassPath cp) {
        Iterator it = files.iterator();
        while (it.hasNext()) {
            FileObject fo = (FileObject) it.next();
            // #168446: try <attr name="displayName" bundlevalue="Bundle#key"/> first
            String bundleKey = (String) fo.getAttribute("literal:displayName"); // NOI18N
            String bundleName;
            if (bundleKey != null) {
                String[] arr = bundleKey.split(":", 2); // NOI18N
                assert arr[0].equals("bundle") : "Literal displayName value should start with 'bundle:'";    // NOI18N
                arr = arr[1].split("#", 2);    // NOI18N
                bundleName = arr[0];
                bundleKey = arr[1];
            } else {
                bundleName = (String) fo.getAttribute("SystemFileSystem.localizingBundle"); // NOI18N
                bundleKey = fo.getPath();
            }
            if (bundleName != null) {
                try {
                    URL[] u = LayerUtils.currentify(LayerUtils.urlForBundle(bundleName), suffix, cp);
                    for (int i = 0; i < u.length; i++) {
                    InputStream is = u[i].openStream();
                    try {
                        Properties p = new Properties();
                        p.load(is);
                        String val = p.getProperty(bundleKey);
                        // Listen to changes in the origin file if any...
                        FileObject ufo = URLMapper.findFileObject(u[i]);
                        if (ufo != null) {
                            ufo.removeFileChangeListener(fileChangeListener);
                            ufo.addFileChangeListener(fileChangeListener);
                            // In case a sibling bundle is added, that may be relevant:
                            ufo.getParent().removeFileChangeListener(fileChangeListener);
                            ufo.getParent().addFileChangeListener(fileChangeListener);
                        }
                        if (val != null) {
                            if (fo.getPath().startsWith("Menu/")) { // NOI18N
                                // Special-case menu folders to trim the mnemonics, since they are ugly.
                                return Actions.cutAmpersand(val);
                            } else {
                                return val;
                            }
                        }
                        // if null, fine--normal for key to not be found
                    } finally {
                        is.close();
                    }
                    }
                } catch (IOException ioe) {
                    // For debugging; SFS will rather notify a problem separately...
                    Util.err.notify(ErrorManager.INFORMATIONAL, ioe);
                    return NbBundle.getMessage(BadgingSupport.class, "LBL_no_such_bundle", name, bundleName);
                }
            }
            if (fo.hasExt("instance")) { // NOI18N
                return getInstanceLabel(fo);
            }
            if (fo.hasExt("shadow")) { // NOI18N
                Object originalFile = fo.getAttribute("originalFile"); // NOI18N
                if (originalFile != null && originalFile instanceof String) {
                    FileObject orig;
                    try {
                        orig = fo.getFileSystem().findResource((String) originalFile);
                    } catch (FileStateInvalidException e) {
                        orig = null;
                    }
                    if (orig != null && orig.hasExt("instance")) { // NOI18N
                        return annotateNameGeneral((String) originalFile, Collections.singleton(orig), suffix, fileChangeListener, cp);
                    }
                }
            }
        }
        return name;
    }

    private static String getInstanceLabel(FileObject fo) {
        try {
            // First try to load it in current IDE, as this handles most platform cases OK.
            InstanceCookie ic = DataObject.find(fo).getCookie(InstanceCookie.class);
            if (ic != null) {
                Object o;
                Logger fslogger = Logger.getLogger("org.openide.filesystems"); // NOI18N
                Logger cachelogger = Logger.getLogger("org.netbeans.core.startup.layers.BinaryFS"); // NOI18N
                Level fsLevel = fslogger.getLevel();
                Level cacheLevel = cachelogger.getLevel();
                fslogger.setLevel(Level.OFF); // #99744
                cachelogger.setLevel(Level.OFF); // #166199
                try {
                    o = ic.instanceCreate();
                    if (o instanceof Action) {
                        String name = (String) ((Action) o).getValue(Action.NAME);
                        if (name != null) {
                            return Actions.cutAmpersand(name);
                        } else {
                            return toStringOf(o);
                        }
                    } else if (o instanceof Presenter.Menu) {
                        return ((Presenter.Menu) o).getMenuPresenter().getText();
                    } else if (o instanceof JSeparator) {
                        return NbBundle.getMessage(BadgingSupport.class, "LBL_separator");
                    } else {
                        return toStringOf(o);
                    }
                } finally {
                    fslogger.setLevel(fsLevel);
                    cachelogger.setLevel(cacheLevel);
                }
            }
        } catch (Exception e) {
            // ignore, OK
            Logger.getLogger(BadgingSupport.class.getName()).log(Level.FINE, "Ignored exception: (" + e.getClass().getSimpleName() + ") " + e.getMessage());
        }
        // OK, probably a developed module, so take a guess.
        String clazz = (String) fo.getAttribute("instanceClass"); // NOI18N
        if (clazz == null) {
            clazz = fo.getName().replace('-', '.');
        }
        String instanceCreate = (String) fo.getAttribute("literal:instanceCreate"); // NOI18N
        if (instanceCreate != null && instanceCreate.startsWith("new:")) { // NOI18N
            clazz = instanceCreate.substring("new:".length()); // NOI18N
        } else if (instanceCreate != null && instanceCreate.startsWith("method:")) { // NOI18N
            String factoryDisplayLabel = instanceCreate.substring(instanceCreate.lastIndexOf('.', instanceCreate.lastIndexOf('.') - 1) + 1);
            return NbBundle.getMessage(BadgingSupport.class, "LBL_instance_from", factoryDisplayLabel);
        }
        String clazzDisplayLabel = clazz.substring(clazz.lastIndexOf('.') + 1);
        return NbBundle.getMessage(BadgingSupport.class, "LBL_instance_of", clazzDisplayLabel);
    }
    private static String toStringOf(Object o) {
        String s = o.toString();
        if ((o.getClass().getName() + "@" + Integer.toHexString(o.hashCode())).equals(s)) {
            // Does not override toString, so no point in using pkg.Clazz@123456.
            String clazz = o.getClass().getName();
            String clazzDisplayLabel = clazz.substring(clazz.lastIndexOf('.') + 1);
            return NbBundle.getMessage(BadgingSupport.class, "LBL_instance_of", clazzDisplayLabel);
        } else {
            return s;
        }
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
                    URL[] u = LayerUtils.currentify((URL) value, suffix, cp);
                    FileObject ufo = URLMapper.findFileObject(u[0]);
                    if (ufo != null) {
                        ufo.removeFileChangeListener(fileChangeListener);
                        ufo.addFileChangeListener(fileChangeListener);
                    }
                    return Toolkit.getDefaultToolkit().getImage(u[0]);
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
        someFileChange();
    }
    public void fileAttributeChanged(FileAttributeEvent fe) {
        someFileChange();
    }
    public void fileRenamed(FileRenameEvent fe) {
        someFileChange();
    }
    public void fileChanged(FileEvent fe) {
        someFileChange();
    }
    private void someFileChange() {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                // If used as nbres: annotation, fire status change.
                fireFileStatusChanged(new FileStatusEvent(fs, true, true));
            }
        });
    }

}
