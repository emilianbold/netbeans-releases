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

package org.netbeans.modules.encoder.coco.ui.wizard;

import java.io.IOException;
import org.openide.filesystems.FileObject;

/**
 *
 * @author sun
 */
public final class FileObjectUtil {

    /**
     * Create (either create anew or overwrite existing) a new file with
     * extension if given in targetFileName, or default extension.
     * If not overwrite and file already exists, then it tries to use same
     * basename but with a number starting from 1, and same extension as a
     * new file name.
     * 
     * @param dir
     * @param targetFileName
     * @param defaultExt
     * @param overwrite
     * @return
     * @throws IOException
     */
    public static FileObject createFileObject(FileObject dir,
            String targetFileName, String defaultExt, boolean overwrite)
            throws IOException {
        int dotPos = targetFileName.lastIndexOf('.');
        String baseName = targetFileName;
        String ext = defaultExt;
        if (dotPos >= 0) {
            baseName = targetFileName.substring(0, dotPos);
            ext = targetFileName.substring(dotPos + 1);
        }
        FileObject fObj = dir.getFileObject(baseName, ext);
        if (fObj != null && !overwrite) {
            // if not overwrite, and the filename already exists, then try to put
            // a number (incremental) after the name and keep its extension.
            int i = 1;
            String oriBaseName = baseName;
            while (fObj != null) {
                baseName = oriBaseName + (i++);
                fObj = dir.getFileObject(baseName, ext);
            }
        }
        if (fObj != null) {
            fObj.delete();
        }
        fObj = dir.createData(baseName, ext);
        return fObj;
    }

    public static String getBaseName(String fileName) {
        int dotPos = fileName.lastIndexOf('.');
        String baseName = fileName;
        if (dotPos >= 0) {
            baseName = fileName.substring(0, dotPos);
        }
        return baseName;
    }
}