/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.j2me.common.ant;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.util.FileUtils;

/**
 *
 * @author rsvitanic
 */
public class CopyLibletsTask extends Task {

    private static final String LIB = "lib"; //NOI18N
    private static final String JAR_EXT = ".jar"; //NOI18N
    private static final String JAD_EXT = ".jad"; //NOI18N
    private Path runtimePath;

    public void setRuntimeClassPath(final Path path) {
        assert path != null;
        this.runtimePath = path;
    }

    public Path getRuntimeClassPath() {
        return this.runtimePath;
    }

    @Override
    public void execute() throws BuildException {
        if (this.runtimePath == null) {
            throw new BuildException("RuntimeClassPath must be set."); // NOI18N
        }
        final String[] pathElements = this.runtimePath.list();
        final List<File> filesToCopy = new ArrayList<File>();
        final Map<String, Boolean> libletsInProject = LibletUtils.loadLibletsInProject(getProject());

        for (String element : pathElements) {
            if (element.toLowerCase().endsWith(JAR_EXT)) {
                String jadFilePath = element.substring(0, element.lastIndexOf(JAR_EXT)).concat(JAD_EXT);
                File jarFile = new File(element);
                File jadFile = new File(jadFilePath);
                if (jadFile.exists()) {
                    final Map<Object, Object> manifestAttributes = LibletUtils.getJarManifestAttributes(element);
                    if (libletsInProject.get(LibletUtils.getLibletDetails(manifestAttributes))) {
                        // do not copy this LIBlet in dist/lib
                        // it will be extracted into the application's JAR
                        continue;
                    }
                    filesToCopy.add(jarFile);
                    filesToCopy.add(jadFile);
                }
                File libFolder = new File(jarFile.getParent(), LIB);
                if (libFolder.exists()) {
                    filesToCopy.add(libFolder);
                }
            }
        }

        if (!filesToCopy.isEmpty()) {
            final File distDir = getProject().resolveFile(getProject().getProperty("dist.dir")); //NOI18N
            final File libFolder = new File(distDir, LIB);
            if (!libFolder.exists()) {
                libFolder.mkdir();
                this.log("Create lib folder " + libFolder.toString() + ".", Project.MSG_VERBOSE); // NOI18N
            }
            assert libFolder.canWrite();

            final FileUtils utils = FileUtils.getFileUtils();
            for (final File fileToCopy : filesToCopy) {
                if (fileToCopy.isDirectory()) {
                    if (fileToCopy.list().length > 0) {
                        File innerLibFolder = new File(libFolder, "lib"); //NOI18N
                        if (!innerLibFolder.exists()) {
                            innerLibFolder.mkdir();
                            this.log("Create lib folder for LIBlet " + innerLibFolder.toString() + ".", Project.MSG_VERBOSE); // NOI18N
                        }
                        Copy cp = (Copy) getProject().createTask("copy"); // NOI18N
                        cp.setTodir(innerLibFolder);
                        FileSet fset = new FileSet();
                        fset.setDir(fileToCopy);
                        cp.addFileset(fset);
                        cp.execute();
                    }
                } else {
                    File jadFile = new File(libFolder, fileToCopy.getName());
                    try {
                        utils.copyFile(fileToCopy, jadFile, null, true);
                    } catch (IOException ex) {
                        throw new BuildException(ex);
                    }
                }
            }
        }
    }

}
