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

package org.netbeans.bluej.classpath;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import org.netbeans.bluej.BluejProject;
import org.netbeans.bluej.options.BlueJSettings;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.WeakListeners;

/**
 *
 * @author mkleint
 */
public class CPImpl implements ClassPathImplementation {
    
    private List resources = null;
    private List listeners = new ArrayList();

    private BluejProject project;
    private FileObject userLib;
    private PropertyChangeListener settingsListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent arg0) {
            fireChange();
        }
    };
    
    private FileChangeListener fileListener = new FileChangeAdapter() {
        public void fileDataCreated(FileEvent arg0) {
            fireChange();
        }

        public void fileDeleted(FileEvent arg0) {
            fireChange();
        }

        public void fileRenamed(FileRenameEvent arg0) {
            fireChange();
        }
    };
    /** Creates a new instance of CPImpl */
    public CPImpl(BluejProject prj) {
        project = prj;
        BlueJSettings.getDefault().addPropertyChangeListener(
                WeakListeners.propertyChange(settingsListener, BlueJSettings.getDefault()));
    }

    public synchronized List getResources() {
        if (resources == null) {
            resources = new ArrayList();
            FileObject libs = project.getProjectDirectory().getFileObject("+libs");  // NOI18N
            if (libs != null) {
                FileObject[] fos = libs.getChildren();
                for (int i = 0; i < fos.length; i++) {
                    if (FileUtil.isArchiveFile(fos[i])) {
                        resources.add(ClassPathSupport.createResource(URLMapper.findURL(FileUtil.getArchiveRoot(fos[i]), URLMapper.INTERNAL)));
                    }
                }
            }
            File home = BlueJSettings.getDefault().getHome();
            if (home != null) {
                FileObject fo = FileUtil.toFileObject(BluejProject.getUserLibPath(home));
                if (fo != null) {
                    FileObject[] fos = fo.getChildren();
                    for (int i = 0; i < fos.length; i++) {
                        if (FileUtil.isArchiveFile(fos[i])) {
                            resources.add(ClassPathSupport.createResource(URLMapper.findURL(FileUtil.getArchiveRoot(fos[i]), URLMapper.INTERNAL)));
                        }
                    }
                } 
                if (userLib != null && !userLib.equals(fo)) {
                    //remove
                    userLib.removeFileChangeListener(fileListener);
                }
                userLib = fo;
                //add
                userLib.addFileChangeListener(fileListener);
            } else if (userLib != null) {
                //remove listener
                userLib.removeFileChangeListener(fileListener);
                userLib = null;
            }
            String userPath = BlueJSettings.getDefault().getUserLibrariesAsClassPath();
            if (userPath.length() > 0) {
                StringTokenizer tokens = new StringTokenizer(userPath, ":", false);  // NOI18N
                while (tokens.hasMoreTokens()) {
                    File fil = new File(tokens.nextToken());
                    FileObject fo = FileUtil.toFileObject(fil);
                    if (fo != null && FileUtil.isArchiveFile(fo)) {
                        resources.add(ClassPathSupport.createResource(URLMapper.findURL(FileUtil.getArchiveRoot(fo), URLMapper.INTERNAL)));
                    }
                }
            }
        }
        return resources;
    }

    public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
        listeners.add(listener);
    }

    public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
        listeners.remove(listener);
    }
    
    public synchronized void fireChange() {
        resources = null;
        List lst = new ArrayList();
        lst.addAll(listeners);
        Iterator it = lst.iterator();
        PropertyChangeEvent evnt = new PropertyChangeEvent(this, ClassPathImplementation.PROP_RESOURCES, null, null);
        while (it.hasNext()) {
            PropertyChangeListener listener = (PropertyChangeListener)it.next();
            listener.propertyChange(evnt);
        }
    }
}
