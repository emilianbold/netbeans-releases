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
package org.netbeans.modules.versioning.system.cvss;

import org.netbeans.spi.queries.VisibilityQueryImplementation2;
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.modules.versioning.util.VersioningEvent;
import org.netbeans.modules.versioning.util.VersioningListener;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.RequestProcessor;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.util.*;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

/**
 * Hides folders that have 'Localy removed' status.
 * 
 * @author Maros Sandor
 */
public class CvsVisibilityQuery implements VisibilityQueryImplementation2, VersioningListener {

    private static CvsVisibilityQuery instance;
    private static final String MARKER_CVS_REMOVED = "CVS/.nb-removed";

    private List<ChangeListener> listeners = new ArrayList<ChangeListener>();
    private final RequestProcessor.Task refreshVisibilityTask;

    public CvsVisibilityQuery() {
        CvsVersioningSystem.getInstance().getStatusCache().addVersioningListener(this);
        instance = this;
        refreshVisibilityTask = Utils.createTask(new Runnable() {
            public void run() {
                fireVisibilityChanged();
            }
        });        
    }

    public boolean isVisible(FileObject fileObject) {
        long t = System.currentTimeMillis();
        CvsVersioningSystem.LOG.log(Level.FINE, "isVisible {0}", new Object[] { fileObject });
        boolean ret = true;
        try {
            if (fileObject.isData()) return ret;
            File file = FileUtil.toFile(fileObject);
            ret = isVisible(file);
            return ret;
        } finally {
            if(CvsVersioningSystem.LOG.isLoggable(Level.FINE)) {
                CvsVersioningSystem.LOG.log(Level.FINE, "isVisible returns {0} in {1} millis", new Object[] { ret, System.currentTimeMillis() - t });
            }
        }
    }
    
    public boolean isVisible(File file) {
        return file == null || file.isFile() || !isHiddenFolder(file);
    }

    public synchronized void addChangeListener(ChangeListener l) {
        ArrayList<ChangeListener> newList = new ArrayList<ChangeListener>(listeners);
        newList.add(l);
        listeners = newList;
    }

    public synchronized void removeChangeListener(ChangeListener l) {
        ArrayList<ChangeListener> newList = new ArrayList<ChangeListener>(listeners);
        newList.remove(l);
        listeners = newList;
    }

    public void versioningEvent(VersioningEvent event) {
        if (event.getId() == FileStatusCache.EVENT_FILE_STATUS_CHANGED) {
            File file = (File) event.getParams()[0];
            if (file == null) return;
            File parent = file.getParentFile();
            if (parent == null) return;
            File marker = new File(parent, MARKER_CVS_REMOVED);
            if (marker.exists()) {
                if (file.lastModified() > marker.lastModified() && !CvsVersioningSystem.FILENAME_CVS.equals(file.getName())) {
                    makeVisible(parent);
                }
                fireVisibilityChanged();
            }
        }
    }

    private static void makeVisible(File file) {
        if (file == null) return;
        new File(file, MARKER_CVS_REMOVED).delete();
        makeVisible(file.getParentFile());
    }

    static boolean isHiddenFolder(File file) {
        File marker = new File(file, MARKER_CVS_REMOVED);
        if (marker.exists()) {
            File [] files = file.listFiles();
            for (File child : files) {
                if (child.lastModified() > marker.lastModified() && !CvsVersioningSystem.FILENAME_CVS.equals(child.getName())) {
                    makeVisible(file);
                    refreshVisibility();
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }
    
    private synchronized void fireVisibilityChanged() {
        ChangeEvent event = new ChangeEvent(this);
        for (ChangeListener listener : listeners) {
            listener.stateChanged(event);          
        }          
    }

    private static void refreshVisibility() {
        if (instance != null) {
            instance.refreshVisibilityTask.schedule(100);
        }
   }

    static void hideFolder(File file) throws IOException {
        new File(file, MARKER_CVS_REMOVED).createNewFile();
        refreshVisibility();
    }
}
