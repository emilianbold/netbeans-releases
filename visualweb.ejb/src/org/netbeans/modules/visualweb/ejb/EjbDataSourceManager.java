/*
 * EjbDataSourceManager.java
 *
 * Created on May 4, 2004, 1:13 PM
 */

package org.netbeans.modules.visualweb.ejb;

import org.netbeans.modules.visualweb.ejb.datamodel.EjbDataModel;
import org.netbeans.modules.visualweb.ejb.load.EjbDataSourceXmlCreator;
import org.netbeans.modules.visualweb.ejb.load.EjbDataSourceXmlParser;
import org.netbeans.modules.visualweb.ejb.load.EjbLoadException;
import org.netbeans.modules.visualweb.ejb.nodes.EjbLibReferenceHelper;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.ErrorManager;

/**
 * To load and save the data model to xml file
 * @author  cao
 */
public class EjbDataSourceManager
{
    private final String ejbDataSrcFileName = System.getProperty("netbeans.user") + "/ejb-datasource/ejbsources.xml"; // NOI18N
    public static final String EJB_DATA_SUB_DIR = "ejb-sources"; // NOI18N

    // Singlton
    private static EjbDataSourceManager mgr = new EjbDataSourceManager();
    
    private OpenProjectListKeeper openProjectList;


    public static EjbDataSourceManager getInstance()
    {
        return mgr;
    }

    private EjbDataSourceManager() {}

    /**
     * Load the Ejb modules from the saved xml files
     */
    public void load() throws EjbLoadException
    {
        // Load all the information from the xml file if there is one

        File file = new File( ejbDataSrcFileName );
        if( file.exists() )
        {
            EjbDataSourceXmlParser parser = new EjbDataSourceXmlParser( ejbDataSrcFileName );
            EjbDataModel.getInstance().addEjbGroups( parser.parse() );
        }
        
        // Since everything just loaded, reset the modified flag in the data model
        EjbDataModel.getInstance().resetModifiedFlag();
        
        openProjectList = new OpenProjectListKeeper( OpenProjects.getDefault().getOpenProjects() );
        
        // Listen on the project open event so that we can check whehther any ejb related
        // jar files needed to be updated
        OpenProjects.getDefault().addPropertyChangeListener( new OpenProjectListener() );
    }

    /**
     * Save the data model to the xml files
     */
    public void save()
    {
        // Do a save only if there is a modification in the data model
        if( !EjbDataModel.getInstance().isModified() )
            return;
        
        try
        {
            File file = new File( ejbDataSrcFileName );

            if( !file.exists() )
                file.createNewFile();
            
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter( new FileOutputStream( file), "UTF-8" ); // No I18N
            BufferedWriter bufferWriter = new BufferedWriter( outputStreamWriter );

            EjbDataSourceXmlCreator creator = new EjbDataSourceXmlCreator( EjbDataModel.getInstance(), bufferWriter );
            creator.toXml();
            bufferWriter.flush();
            bufferWriter.close();
        }
        catch( Exception ex )
        {
            ErrorManager.getDefault().getInstance( "org.netbeans.modules.visualweb.ejb.EjbDataSourceManager").log( ErrorManager.ERROR,
                        "Failed to save ejb datasource to file: " + ejbDataSrcFileName );
            ex.printStackTrace();
        }
    }
    
    private class OpenProjectListener implements PropertyChangeListener 
    {
        public void propertyChange( PropertyChangeEvent evt ) {
            
            if( evt.getPropertyName().equals(OpenProjects.PROPERTY_OPEN_PROJECTS) )
            {
                Project[] projects = openProjectList.getNewlyOpenedProjects( OpenProjects.getDefault().getOpenProjects() );
                EjbLibReferenceHelper.syncArchiveRefs( projects );
            }
        }
        
    }
}
