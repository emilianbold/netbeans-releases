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

package org.netbeans.modules.javacard;

import org.netbeans.modules.javacard.common.Utils;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javacard.common.CommonSystemFilesystemPaths;
import org.netbeans.modules.javacard.common.JCConstants;
import org.netbeans.modules.javacard.spi.BrokenJavacardPlatform;
import org.netbeans.modules.javacard.spi.JavacardPlatform;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MultiFileSystem;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;

/**
 *
 * @author Tim Boudreau
 */
public final class JCUtil {
    private JCUtil(){}

    public static DataObject createFakeJavacardPlatform(String name) {
        FileSystem fs = FileUtil.createMemoryFileSystem();
        MultiFileSystem mfs;
        try {
            mfs = new MultiFileSystem(new FileSystem[]{FileUtil.getConfigRoot().getFileSystem(), fs});
            FileObject fo = FileUtil.createData(fs.getRoot(),
                    CommonSystemFilesystemPaths.SFS_JAVA_PLATFORMS_FOLDER + '/' +
                    name + '.' + JCConstants.JAVACARD_PLATFORM_FILE_EXTENSION);
            fo = mfs.getRoot().getFileObject(fo.getPath());
            return DataObject.find(fo);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    public static JavacardPlatform findPlatformNamed(String name) {
        DataObject dob = JCUtil.findPlatformDataObjectNamed(name);
        JavacardPlatform result = null;
        if (dob != null) {
            result = dob.getLookup().lookup(JavacardPlatform.class);
        }
        if (result == null) {
            result = new BrokenJavacardPlatform(name);
        }
        return result;
    }


    public static File eepromFileForDevice(JavacardPlatform platform, String deviceName, boolean create) {
        String realName = platform.getSystemName();
        if (realName == null) {
            for (FileObject fo : Utils.sfsFolderForRegisteredJavaPlatforms().getChildren()) {
                try {
                    DataObject dob = DataObject.find(fo);
                    JavacardPlatform impl = dob.getLookup().lookup(JavacardPlatform.class);
                    if (impl != null && impl.equals(platform)) {
                        realName = dob.getName();
                    }
                } catch (IOException ioe) {
                    Exceptions.printStackTrace(ioe);
                }
            }
        }
        if (realName == null) {
            realName = platform.getDisplayName();
        }
        return Utils.eepromFileForDevice(realName, deviceName, create);
    }


    public static DataObject findPlatformDataObjectNamed(String name) {
        if (name == null || "".equals(name)) { //NOI18N
            return null;
        }
        for (FileObject fo : Utils.sfsFolderForRegisteredJavaPlatforms().getChildren()) {
            if (name.equals(fo.getName())) {
                try {
                    return DataObject.find(fo);
                } catch (DataObjectNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        return createFakeJavacardPlatform(name);
    }

    public static FileObject findBuildXml(Project project) {
        return project.getProjectDirectory().getFileObject(
                GeneratedFilesHelper.BUILD_XML_PATH);
    }

    public static URL getBuildXslTemplate() {
        FileObject file = FileUtil.getConfigFile(CommonSystemFilesystemPaths.SFS_PATH_TO_BUILD_XSL);
        Parameters.notNull(CommonSystemFilesystemPaths.SFS_PATH_TO_BUILD_XSL + " missing " + //NOI18N
                "from system filesystem", file); //NOI18N
        return URLMapper.findURL(file, URLMapper.INTERNAL);
    }

    public static URL getBuildImplXslTemplate() {
        FileObject file = FileUtil.getConfigFile(CommonSystemFilesystemPaths.SFS_PATH_TO_BUILD_IMPL_XSL);
        Parameters.notNull(CommonSystemFilesystemPaths.SFS_PATH_TO_BUILD_IMPL_XSL + " missing " + //NOI18N
                "from system filesystem", file); //NOI18N
        return URLMapper.findURL(file, URLMapper.INTERNAL);
    }
}
