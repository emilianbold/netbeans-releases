/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
/*
 * EjbDataSourceManager.java
 *
 * Created on May 4, 2004, 1:13 PM
 */

package org.netbeans.modules.visualweb.ejb;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.visualweb.ejb.datamodel.EjbDataModel;
import org.netbeans.modules.visualweb.ejb.load.EjbDataSourceXmlCreator;
import org.netbeans.modules.visualweb.ejb.load.EjbDataSourceXmlParser;
import org.netbeans.modules.visualweb.ejb.load.EjbLoadException;
import org.netbeans.modules.visualweb.ejb.nodes.EjbLibReferenceHelper;
import org.netbeans.modules.visualweb.ejb.util.Util;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;

/**
 * To load and save the data model to xml file
 * 
 * @author cao
 */
public class EjbDataSourceManager {
    public static final String EJB_DATA_SUB_DIR = "ejb-sources"; // NOI18N

    // Singlton
    private static EjbDataSourceManager mgr = new EjbDataSourceManager();

    public static EjbDataSourceManager getInstance() {
        return mgr;
    }

    private EjbDataSourceManager() {
    }

    /**
     * Load the Ejb modules from the saved xml files
     * 
     * @throws IOException
     */
    public void load() throws EjbLoadException, IOException {
        // Load all the information from the xml file if there is one

        String ejbDataSrcFileName = getUserDirDataSrcFileName();
        File file = new File(ejbDataSrcFileName);
        if (file.exists()) {
            EjbDataSourceXmlParser parser = new EjbDataSourceXmlParser(ejbDataSrcFileName);
            EjbDataModel.getInstance().addEjbGroups(parser.parse());
        }

        // Since everything just loaded, reset the modified flag in the data model
        EjbDataModel.getInstance().resetModifiedFlag();
        
        // Listen on the project open event so that we can check whether any ejb related
        // jar files needed to be updated
        OpenProjects.getDefault().addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (OpenProjects.PROPERTY_OPEN_PROJECTS.equals(evt.getPropertyName())) {
                    List<Project> oldOpenProjectsList = Arrays
                            .asList((Project[]) evt.getOldValue());
                    List<Project> newOpenProjectsList = Arrays
                            .asList((Project[]) evt.getNewValue());

                    Set<Project> openedProjectsSet = new LinkedHashSet<Project>(newOpenProjectsList);
                    openedProjectsSet.removeAll(oldOpenProjectsList);
                    Project[] openedProjectsArray = openedProjectsSet
                            .toArray(new Project[openedProjectsSet.size()]);
                    EjbLibReferenceHelper.syncArchiveRefs(openedProjectsArray);
                }
            }
        });
    }

    /**
     * Save the data model to the xml files
     */
    public void save() {
        // Do a save only if there is a modification in the data model
        if (!EjbDataModel.getInstance().isModified())
            return;

        String ejbDataSrcFileName;
        try {
            ejbDataSrcFileName = getUserDirDataSrcFileName();
        } catch (IOException e) {
            Util.getLogger().log(Level.SEVERE, "Unable to save EJB datasource state", e);
            return;
        }

        try {
            File file = new File(ejbDataSrcFileName);

            if (!file.exists())
                file.createNewFile();

            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(
                    file), "UTF-8"); // No I18N
            BufferedWriter bufferWriter = new BufferedWriter(outputStreamWriter);

            EjbDataSourceXmlCreator creator = new EjbDataSourceXmlCreator(EjbDataModel
                    .getInstance(), bufferWriter);
            creator.toXml();
            bufferWriter.flush();
            bufferWriter.close();
        } catch (Exception ex) {
            ErrorManager.getDefault().getInstance(
                    "org.netbeans.modules.visualweb.ejb.EjbDataSourceManager").log(
                    ErrorManager.ERROR,
                    "Failed to save ejb datasource to file: " + ejbDataSrcFileName);
            ex.printStackTrace();
        }
    }

    private static String getUserDirDataSrcFileName() throws IOException {
        File ejbDataSourceDir = new File(Util.getEjbStateDir(), "ejb-datasource");
        if (!ejbDataSourceDir.isDirectory()) {
            // XXX This is broken since it does not update an existing ejbsources.xml file
            // with the updated location
            
            // Try to migrate EJB DataSources from old to new location
            String userDir = System.getProperty("netbeans.user"); // NOI18N
            if (userDir != null) {
                File legacyDir = new File(userDir, "ejb-datasource"); // NOI18N
                if (legacyDir.exists()) {
                    Util.copyFileRecursive(legacyDir, ejbDataSourceDir);

                    // Try to remove the old directory
                    try {
                        FileUtil.toFileObject(legacyDir).delete();
                    } catch (Exception e) {
                        Util.getLogger().log(Level.WARNING,
                                "Unable to remove legacy ejb-datasource dir", e);
                    }
                }
            }
        }

        return new File(ejbDataSourceDir, "ejbsources.xml").getAbsolutePath();
    }
}
