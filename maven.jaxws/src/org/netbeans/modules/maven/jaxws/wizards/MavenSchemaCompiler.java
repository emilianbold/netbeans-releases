/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.jaxws.wizards;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.modules.xml.jaxb.spi.JAXBWizModuleConstants;
import org.netbeans.modules.xml.jaxb.spi.SchemaCompiler;
import org.netbeans.modules.xml.retriever.Retriever;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author mkuchtiak
 */
public class MavenSchemaCompiler implements SchemaCompiler {
    private static final String JAVA_SE_CONFIG_DIR = "resources/jaxb"; //NOI18N

    private Project project;
    MavenSchemaCompiler(Project project) {
        this.project = project;
    }

    @Override
    public void compileSchema(WizardDescriptor wiz) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void importResources(WizardDescriptor wiz) throws IOException {
        List<String> xsdFileList = (List<String>) wiz.getProperty(
                JAXBWizModuleConstants.XSD_FILE_LIST );

        if (xsdFileList != null) {
            String schemaName = (String) wiz.getProperty(JAXBWizModuleConstants.SCHEMA_NAME);

            List<String> bindingFileList = (List<String>) wiz.getProperty(
                    JAXBWizModuleConstants.JAXB_BINDING_FILES);

            String catlogFile = (String) wiz.getProperty(
                    JAXBWizModuleConstants.CATALOG_FILE);

            boolean srcLocTypeUrl = JAXBWizModuleConstants.SRC_LOC_TYPE_URL.equals(
                    (String) wiz.getProperty(
                    JAXBWizModuleConstants.SOURCE_LOCATION_TYPE));

            // import schemas
            if (srcLocTypeUrl) {
                // URL
                for (int i = 0; i < xsdFileList.size(); i++) {
                    String url = xsdFileList.get(i);
                    URL schemaURL = new URL(url);
                    try {
                        FileObject newFileFO = retrieveResource(
                               getSchemaFolder(schemaName),
                               schemaURL.toURI());
                    } catch (URISyntaxException ex) {
                        throw new IOException(ex.getMessage());
                    }
                }
            } else {
                // Local File
                FileObject projFO = project.getProjectDirectory();
                File projDir = FileUtil.toFile(projFO);
                for (int i = 0; i < xsdFileList.size(); i++) {
                    File srcFile = Relative2AbsolutePath(projDir,
                                xsdFileList.get(i));
                    FileObject newFileFO = retrieveResource(
                           getSchemaFolder(schemaName),
                           srcFile.toURI());
                }
            }
        }
    }

    private static FileObject retrieveResource(FileObject targetFolder,
            URI source){
        Retriever retriever = Retriever.getDefault();
        FileObject result = null;
        try {
            result = retriever.retrieveResource(targetFolder, source);
        } catch (UnknownHostException ex) {
            Exceptions.printStackTrace(ex);
        } catch (URISyntaxException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        if (result == null) {
            // XXX TODO Handle or log exception.
            // Map map = retriever.getRetrievedResourceExceptionMap();
        }
        return result;
    }

    public FileObject getSchemaFolder(String schemaName) throws IOException {
        FileObject mainFolder = project.getProjectDirectory().getFileObject("src/main"); //NOI18N
        if (mainFolder != null) {
            FileObject resourcesFolder = mainFolder.getFileObject("resources"); //NOI18N
            if (resourcesFolder == null) {
                resourcesFolder = mainFolder.createFolder("resources"); //NOI18N
            }
            if (resourcesFolder != null) {
                FileObject jaxbFolder = resourcesFolder.getFileObject("jaxb"); //NOI18N
                if (jaxbFolder == null) {
                    jaxbFolder = resourcesFolder.createFolder("jaxb"); //NOI18N
                }
                if (jaxbFolder != null) {
                    FileObject schemaFolder = jaxbFolder.getFileObject(schemaName); //NOI18N
                    if (schemaFolder == null) {
                        schemaFolder = jaxbFolder.createFolder(schemaName); //NOI18N
                    }
                    return schemaFolder;
                }
            }
        }
        return null;
    }

    private static File Relative2AbsolutePath(File base, String relPath){
        File relPathFile = new File(relPath);
        File absPath = null;
        if (!relPathFile.isAbsolute()){
            absPath = new File(base, relPath);
        } else {
            absPath = relPathFile;
        }

        return absPath;
    }

}
