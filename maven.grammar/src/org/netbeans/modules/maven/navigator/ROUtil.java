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

    /**
     * make sure that pom files from repository are opened as read only,
     * sort of hack but works.
     * @param fo
     * @param file
     * @return
     */
    static FileObject checkPOMFileObjectReadOnly(FileObject fo, File file) {
        FileObject toRet = fo;
        if ("pom".equals(fo.getExt())) { //NOI18N
            LocalFileSystem lfs = new LocalFileSystem();
            lfs.setReadOnly(true);
            try {
                lfs.setRootDirectory(file.getParentFile());
                toRet = lfs.findResource(file.getName());
            } catch (PropertyVetoException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
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
}
