/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2010 Sun
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
package org.netbeans.modules.maven.samples;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.libraries.Library;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.NbBundle;

public class MavenCRUDSampleWizardIterator extends MavenSamplesWizardIterator {

    private static final long serialVersionUID = 1L;
    
    private CRUDSampleDbPersistencePanel configurationPanel;
    
    public MavenCRUDSampleWizardIterator() {}
    
    public static MavenCRUDSampleWizardIterator createIterator() {
        return new MavenCRUDSampleWizardIterator();
    }
    
    @Override
    protected WizardDescriptor.Panel[] createPanels() {
        configurationPanel = new CRUDSampleDbPersistencePanel();
        return new WizardDescriptor.Panel[] {
            new MavenSamplesWizardPanel(configurationPanel.isValid()),
            configurationPanel
        };
    }
    
    @Override
    protected String[] createSteps() {
        return new String[] {
            NbBundle.getMessage(MavenCRUDSampleWizardIterator.class, "LBL_CreateProjectStep"),
            NbBundle.getMessage(MavenCRUDSampleWizardIterator.class, "LBL_CreatePersistenceStep"),
        };
    }

    @Override
    protected void configureProject(FileObject dir) throws IOException {
//        try {
//            // copy persistence libraries
//            copyPersistenceLibraries(configurationPanel.getSelectedLibrary(), dir);
//        } catch (URISyntaxException ex) {
//            throw new IOException(ex);
//        } catch (IllegalStateException ex) {
//            throw new IOException(ex);
//        }
//
        // set DB location in Derby module
        configureDerby(configurationPanel.getDerbyLocation(), dir);

    }
    
    private void copyPersistenceLibraries(Library l, FileObject projectRoot) throws URISyntaxException, IllegalStateException, FileNotFoundException, IOException {
        // 1) source libraries
        List<FileObject> libs = new ArrayList<FileObject>();
        for (URL url : l.getContent("classpath")) { //NOI18N
            FileObject fo = URLMapper.findFileObject(url);
            Logger.getLogger(MavenCRUDSampleWizardIterator.class.getName()).log(Level.FINE, "Libary {0} has jar: {1}", new Object[]{l.getName(), fo});
            FileObject jarFO = null;
            if ("jar".equals(url.getProtocol())) {  //NOI18N
                jarFO = FileUtil.getArchiveFile(fo);
            }
            if (jarFO == null) {
                throw new IllegalStateException("No file object on " + url);
            }
            libs.add(jarFO);
        }

        // 2) target place
        File targetFile = new File(FileUtil.toFile(projectRoot), "persistence-library" + File.separator + "external");
        targetFile.mkdirs();
        FileObject targetFO = FileUtil.toFileObject(targetFile);

        // 3) copying
        for (FileObject fo : libs) {
            File f = FileUtil.toFile(fo);
            Logger.getLogger(MavenCRUDSampleWizardIterator.class.getName()).log(Level.FINE, "Copy {0} in {1} as {2}", new Object[]{fo, targetFO, f.getName()});
            FileUtil.copyFile(fo, targetFO, fo.getName());
        }

        // 4) modify project.properties
        // XXX: better to modify project.xml with native jars names
        File projectPropertiesFile = new File(FileUtil.toFile(projectRoot), "persistence-library" + File.separator +
                "nbproject" + File.separator + "project.properties");
        FileObject projectPropertiesFO = FileUtil.toFileObject(projectPropertiesFile);
        Properties projectProperties = new Properties();
        InputStream inputStream = null;
        try {
            inputStream = projectPropertiesFO.getInputStream();
            projectProperties.load(inputStream);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
        assert libs.size() == 2 : "Just two libraries are processed but " + libs;
        projectProperties.setProperty("persistence-library1.jar", "external/" + libs.get(0).getNameExt());
        projectProperties.setProperty("persistence-library2.jar", "external/" + libs.get(1).getNameExt());
        OutputStream outputStream = null;
        try {
            outputStream = projectPropertiesFO.getOutputStream();
            projectProperties.store(outputStream, null);
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
        }
        Logger.getLogger(MavenCRUDSampleWizardIterator.class.getName()).log(Level.FINE, "project.properties file written : {0}", new Object[]{projectProperties});

        // 5)  modify $persistencelibrary.xml to persistence.xml
        File libraryConfFile = new File(FileUtil.toFile(projectRoot), "CustomerDBAccess" + File.separator +
                "src" + File.separator + "META-INF" + File.separator + l.getName() + ".xml");
        Logger.getLogger(MavenCRUDSampleWizardIterator.class.getName()).log(Level.FINE, "META-INF/peristence.xml found at {0}", new Object[]{libraryConfFile});
        assert libraryConfFile.exists() : libraryConfFile + " exists.";
        File persistenceConfFile = new File(libraryConfFile.getParent(), "persistence.xml"); // NOI18N
        libraryConfFile.renameTo(persistenceConfFile);
    }

    private void configureDerby(String loc, FileObject projectRoot) throws FileNotFoundException, IOException {
//        File projectPropertiesFile = new File(FileUtil.toFile(projectRoot), "derbyclient-library" + File.separator +
//                "nbproject" + File.separator + "project.properties");
//        FileObject projectPropertiesFO = FileUtil.toFileObject(projectPropertiesFile);
//        Properties projectProperties = new Properties();
//        InputStream inputStream = null;
//        try {
//            inputStream = projectPropertiesFO.getInputStream();
//            projectProperties.load(inputStream);
//        } finally {
//            if (inputStream != null) {
//                inputStream.close();
//            }
//        }
//        projectProperties.setProperty("derbyclient.jar", loc);
//        OutputStream outputStream = null;
//        try {
//            outputStream = projectPropertiesFO.getOutputStream();
//            projectProperties.store(outputStream, null);
//        } finally {
//            if (outputStream != null) {
//                outputStream.close();
//            }
//        }
//        Logger.getLogger(MavenCRUDSampleWizardIterator.class.getName()).log(Level.FINE, "Derby location in project.properties is {0}", new Object[]{loc});

        // maven.samples/samples_src/MavenCRUDSample/crudsample/src/main/resources/org/netbeans/modules/crudsampleapplication/dbaccess/Bundle.properties
        File bundleFile = new File(FileUtil.toFile(projectRoot), "crudsample" + File.separator +
                "src" + File.separator +
                "main" + File.separator +
                "resources" + File.separator +
                "org" + File.separator +
                "netbeans" + File.separator +
                "modules" + File.separator +
                "crudsampleapplication" + File.separator +
                "dbaccess" + File.separator +
                "Bundle.properties");
        FileObject bundleFO = FileUtil.toFileObject(bundleFile);
        Properties bundleProperties = new Properties();
        InputStream inputStream = null;
        try {
            inputStream = bundleFO.getInputStream();
            bundleProperties.load(inputStream);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
        bundleProperties.setProperty("javadb.home", loc);
        OutputStream outputStream = null;
        try {
            outputStream = bundleFO.getOutputStream();
            bundleProperties.store(outputStream, null);
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
        }
        Logger.getLogger(MavenCRUDSampleWizardIterator.class.getName()).log(Level.FINE, "JavaDB home is {0}", new Object[]{loc});
    }
    
}
