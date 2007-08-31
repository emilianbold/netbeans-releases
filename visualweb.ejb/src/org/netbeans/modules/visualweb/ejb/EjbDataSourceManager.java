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
