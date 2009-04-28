/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.navigator;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.logging.Logger;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;

/**
 *
 * @author mkleint
 */
public class ROUtil {

    private static final HashMap<File, WeakReference<LocalFileSystem>> cache =
            new HashMap<File, WeakReference<LocalFileSystem>>();
    /**
     * make sure that pom files from repository are opened as read only,
     * sort of hack but works.
     * Warn: assumes a local repository layout for the files passed and recognized as read-only.
     *
     * @param fo
     * @param file
     * @return
     */
    static FileObject checkPOMFileObjectReadOnly(FileObject fo, File file) {
        FileObject toRet = fo;
        if ("pom".equals(fo.getExt())) { //NOI18N
            LocalFileSystem lfs = checkFSCache(file);
            toRet = lfs.findResource(getPath(file));
            if (toRet == null) {
                Logger.getLogger(ROUtil.class.getName()).info("Could not find a ReadOnly fileobject for " + getPath(file) + " in " + lfs.getRootDirectory() + ": " + lfs);
                toRet = fo;
            }
        }
        return toRet;
    }

    static DataObject checkPOMFileObjectReadOnly(DataObject dobj) {
        DataObject toRet = dobj;
        FileObject fo = dobj.getPrimaryFile();
        File fl = FileUtil.toFile(fo);
        if (fl != null) {
            fo = checkPOMFileObjectReadOnly(fo, fl);
            try {
                toRet = DataObject.find(fo);
            } catch (DataObjectNotFoundException ex) {
                //ignore
            }
        }
        return toRet;
    }

    private static String getPath(File file) {
        File versionFolder = file.getParentFile();
        assert versionFolder != null : "wrong parent for " + file;
        File artifactFolder = versionFolder.getParentFile();
        assert artifactFolder != null : "wrong parent for " + versionFolder;
        return artifactFolder.getName() + "/" + versionFolder.getName() + "/" + file.getName();
    }

    private static LocalFileSystem checkFSCache(File file) {
        File versionFolder = file.getParentFile();
        assert versionFolder != null : "wrong parent for " + file;
        File artifactFolder = versionFolder.getParentFile();
        assert artifactFolder != null : "wrong parent for " + versionFolder;
        File groupFolder = artifactFolder.getParentFile();
        assert groupFolder != null : "wrong parent for " + artifactFolder;
        LocalFileSystem fs = null;
        synchronized (cache) {
            WeakReference<LocalFileSystem> ref = cache.get(groupFolder);
            if (ref != null) {
                fs = ref.get();
            }
            if (fs == null) {
                fs = new LocalFileSystem();
                fs.setReadOnly(true);
                try {
                    fs.setRootDirectory(groupFolder);
                    cache.put(groupFolder, new WeakReference<LocalFileSystem>(fs));
                } catch (PropertyVetoException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        return fs;
    }
}
