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

package org.netbeans.modules.javacard.oberthur;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.javacard.ri.spi.RIPlatformUtils;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.modules.ModuleInstall;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.RequestProcessor;

/**
 * Tries to locate the "default" RI platform instance, and merge our
 * prototype properties with it to create a merged platform readable
 * by Ant scripts.
 * <p/>
 * All of this should be replaced with declarative registration at
 * some point in the future.
 * <p>
 * Or, if some directory w/ SDK files is needed, this can all be
 * eliminated, but the user will need to manually set up their platform.
 *
 * @author Tim Boudreau
 */
public final class PlatformInstaller extends ModuleInstall {
    private static final String SFS_PLATFORM_PROTOTYPE_PATH = "oberthur/Oberthur301.jcplatform"; //NOI18N
    private static final String SFS_PLATFORM_PATH = "Services/Platforms/org-netbeans-api-java-Platform/Oberthur301.jcplatform"; //NOI18N
    private static final String SFS_CARD_PATH = "org-netbeans-modules-javacard/servers/Oberthur301/OberthurSmartCard.jcard"; //NOI18N

    @Override
    public void restored() {
        new GlobalBuildPropsUpdater().launch();
    }

    private static final class GlobalBuildPropsUpdater implements Runnable, FileChangeListener {
        private final RequestProcessor rp = new RequestProcessor ("Oberthur card initializer", Thread.MIN_PRIORITY);

        void launch() {
            rp.post(this, 5000);
        }

        public void run() {
            //First, ensure the files really exist on disk
            FileObject platformFo = FileUtil.getConfigFile(SFS_PLATFORM_PATH);
            if (platformFo != null) {
                return;
            }
            File platformF = platformFo == null ? null : FileUtil.toFile(platformFo);
            FileObject cardFo = FileUtil.getConfigFile(SFS_CARD_PATH);
            File cardF = cardFo == null ? null : FileUtil.toFile(cardFo);
            try {
                boolean needWrite = platformF == null || cardF == null;
                if (platformF == null) {
                    if (!mergeDefaultRI(platformFo)) {
                        startListening();
                        return;
                    } else {
                        platformFo = FileUtil.getConfigFile(SFS_PLATFORM_PATH);
                        stopListening();
                    }
                    platformF = FileUtil.toFile(platformFo);
                    assert platformF != null;
                }
                if (cardF == null) {
                    touch (cardFo);
                    cardF = FileUtil.toFile(cardFo);
                    assert cardF != null;
                }
                if (needWrite) {
                    ProjectManager.mutex().writeAccess(new GlobalPropsWriter(
                            platformF, cardF, platformFo.getName()));
                }
            } catch (Exception ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }

        boolean listening = false;
        private void startListening() {
            if (listening) {
                return;
            }
            FileObject fo = FileUtil.getConfigFile("Services/Platforms/org-netbeans-api-java-Platform/");
            fo.addFileChangeListener(this);
        }

        private void stopListening() {
            FileObject fo = FileUtil.getConfigFile("Services/Platforms/org-netbeans-api-java-Platform/");
            fo.removeFileChangeListener(this);
            listening = false;
        }

        private boolean mergeDefaultRI(FileObject platformFo) throws IOException {
            //Merge in the default instance of the Java Card RI
            if (platformFo == null) {
                platformFo = FileUtil.createData(FileUtil.getConfigRoot(), SFS_PLATFORM_PATH);
            }
            FileObject prototype = FileUtil.getConfigFile (SFS_PLATFORM_PROTOTYPE_PATH);
            boolean result = RIPlatformUtils.mergeDefaultRI(prototype, platformFo);
            if (!result) {
                platformFo.delete();
            }
            return result;
        }

        private void touch(FileObject fo) throws IOException {
            //Ensure the file from the layer.xml is recreated as a file on
            //disk
            OutputStream out = new ByteArrayOutputStream((int) fo.getSize());
            byte[] b = ((ByteArrayOutputStream) out).toByteArray();
            InputStream in = new BufferedInputStream(fo.getInputStream());
            FileUtil.copy (in, out);
            out = new BufferedOutputStream(fo.getOutputStream());
            out.write(b);
            out.flush();
            out.close();
        }

        public void fileFolderCreated(FileEvent fe) {
            //do nothing
        }

        public void fileDataCreated(FileEvent fe) {
            rp.post(this, 1000);
        }

        public void fileChanged(FileEvent fe) {
            //do nothing
        }

        public void fileDeleted(FileEvent fe) {
            //do nothing
        }

        public void fileRenamed(FileRenameEvent fe) {
            //do nothing
        }

        public void fileAttributeChanged(FileAttributeEvent fe) {
            //do nothing
        }
    }

    private static final class GlobalPropsWriter implements Mutex.ExceptionAction<Void> {
        private final File platformFile;
        private final File deviceFile;
        private final String platformId;
        GlobalPropsWriter(File platformFile, File deviceFile, String platformId) {
            this.platformFile = platformFile;
            this.deviceFile = deviceFile;
            this.platformId = platformId;
        }

        public Void run() throws Exception {
            EditableProperties props = PropertyUtils.getGlobalProperties();
            props.setProperty("jcplatform." + platformId, platformFile.getAbsolutePath()); //NOI18N
            props.setProperty("jcplatform." + platformId + ".devicespath",
                    deviceFile.getParentFile().getAbsolutePath()); //NOI18N
            PropertyUtils.putGlobalProperties(props);
            return null;
        }
    }

}
