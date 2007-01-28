/*
 * EjbRefMaintainer.java
 *
 * Created on June 16, 2004, 12:15 PM
 */

package org.netbeans.modules.visualweb.ejb;

import org.netbeans.modules.visualweb.ejb.datamodel.EjbGroup;
import org.netbeans.modules.visualweb.ejb.load.EjbDataSourceXmlCreator;
import org.netbeans.modules.visualweb.ejb.load.EjbDataSourceXmlParser;
import org.netbeans.modules.visualweb.ejb.util.Util;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.*;
import org.openide.ErrorManager;

/**
 * This class is to maintain the ejb ref xml file the project-data/ejb-sources directory
 *
 * @author  cao
 */
public class EjbRefMaintainer 
{
    // The ejb-ref.xml 
    private String xmlFile;
    
    public EjbRefMaintainer( String ejbRefXmlFile ) 
    {
        this.xmlFile = ejbRefXmlFile;
    }
    
    /**
     * Add the given ejb group to the ejb-refs.xml if not there yet
     *
     * @param ejbGroup the ejb group to be added to the xml file
     */
    public void addToEjbRefXml( EjbGroup ejbGroup )
    {
        try
        {
            // It is possible that this is the very first group we are adding
            Collection referredEjbGrps = null;
            if( (new File(xmlFile).exists()) )
            {
                EjbDataSourceXmlParser parser = new EjbDataSourceXmlParser( xmlFile );
                referredEjbGrps = parser.parse();
            }
            else
                referredEjbGrps = new HashSet();

            // Check whether this group is already added. We can check the existence based on the client wrapper bean jar 
            // name since it does not change
            for( Iterator iter = referredEjbGrps.iterator(); iter.hasNext(); )
            {
                if( Util.getFileName(((EjbGroup)iter.next()).getClientWrapperBeanJar()).equals( Util.getFileName(ejbGroup.getClientWrapperBeanJar()) ) )
                    // Found it
                    return;
            }
            
            referredEjbGrps.add( ejbGroup );

            writeToFile( referredEjbGrps );
        }
        catch( Exception e )
        {
            ErrorManager.getDefault().getInstance("org.netbeans.modules.visualweb.ejb.EjbRefMaintainer").log( ErrorManager.ERROR,  e.getMessage() );
            e.printStackTrace();
        }
    }
    
    /**
     * Remove the ejb groups from the ejb-refs.xml if all the client jars in the group 
     * are in given removed jars
     *
     * @param jars a list of jar files. The jar files in the list should be
     *             only the filename (not the whole path)
     */
    public void removeFromEjbRefXml( ArrayList removedJars )
    {
        try
        {
            EjbDataSourceXmlParser parser = new EjbDataSourceXmlParser( xmlFile );
            Collection referredEjbGrps = parser.parse();

            for( Iterator iter = referredEjbGrps.iterator(); iter.hasNext(); )
            {
                EjbGroup grp = (EjbGroup)iter.next();
                
                // Only if all the client jars in this group got removed, then this
                // group will get removed from the ejb-refs.xml
                boolean derefIt = true;
                for( Iterator jarIter = grp.getClientJarFiles().iterator(); jarIter.hasNext(); )
                {
                    String jarFileName = org.netbeans.modules.visualweb.ejb.util.Util.getFileName( (String)jarIter.next() );
                    if( !removedJars.contains( jarFileName ) )
                    {
                        derefIt = false;
                        break;
                    }
                }

                if( derefIt )
                    iter.remove();
            }

            // Write the remaining groups back the file
            writeToFile( referredEjbGrps );
        }
        catch( Exception e )
        {
            ErrorManager.getDefault().getInstance("org.netbeans.modules.visualweb.ejb.EjbRefMaintainer").log( ErrorManager.ERROR,  e.getMessage() );
            e.printStackTrace();
        }
    }
    
    public Collection getRefferedEjbGroups()
    {
        try
        {
            EjbDataSourceXmlParser parser = new EjbDataSourceXmlParser( xmlFile );
            return parser.parse();
        }
        catch( Exception e )
        {
            ErrorManager.getDefault().getInstance("org.netbeans.modules.visualweb.ejb.EjbRefMaintainer").log( ErrorManager.ERROR,  e.getMessage() );
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Updates the given ejb group
     *
     * @param group the ejb group to be updated
     */
    public void updateEjbRefXml( EjbGroup oldGrp, EjbGroup modGrp )
    {
        try
        {
            // Get the ejb groups in the ejb-refs.xml
            EjbDataSourceXmlParser parser = new EjbDataSourceXmlParser( xmlFile );
            Collection referredEjbGrps = parser.parse();
            
            // Remove the old one; then add the modified one

            for( Iterator iter = referredEjbGrps.iterator(); iter.hasNext(); )
            {
                EjbGroup grp = (EjbGroup)iter.next();
                
                if( grp.getName().equals( oldGrp.getName() ) )
                {
                    // Remove the out of date one
                    iter.remove();
                    break;
                }
            }

            // Add the modified one
            referredEjbGrps.add( modGrp );

            // Wirte the updated groups back to file
            writeToFile( referredEjbGrps );
        }
        catch( Exception e )
        {
            ErrorManager.getDefault().getInstance("org.netbeans.modules.visualweb.ejb.EjbRefMaintainer").log( ErrorManager.WARNING, e.getMessage() );
            e.printStackTrace();
        }
    }
    
     public void updateEjbRefXml( Collection ejbGroups )
    {
        writeToFile( ejbGroups );
    }
    
    /**
     * Wirte the ejb groups to the xml file
     *
     * @param ejbGroups a collection of ejb groups to be written to the xml file
     */
    private void writeToFile( Collection ejbGroups )
    {
        try
        {
            File file = new File( this.xmlFile );
            if( !file.exists() )
                file.createNewFile();
            
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter( new FileOutputStream( file), "UTF-8" );
            BufferedWriter bufferWriter = new BufferedWriter( outputStreamWriter );
            
            EjbDataSourceXmlCreator xmlCreator = new EjbDataSourceXmlCreator( ejbGroups, bufferWriter );
            xmlCreator.toXml();
            bufferWriter.flush();
            bufferWriter.close();
        }
        catch( java.io.IOException ex )
        {
            // What to do
            ex.printStackTrace();
        }
    }
    
    /**
     * Finds the ejb group in the ejb-refs.xml for the given jar file
     *
     * @param jar should be only the jar filename, not the whole path
     * @return returns the ejb group which contains the given jar file
     */
    public EjbGroup findReferredEjbGroup( String jar )
    {
        try
        {
            EjbDataSourceXmlParser parser = new EjbDataSourceXmlParser( xmlFile );
            Collection referredEjbGrps = parser.parse();

            for( Iterator iter = referredEjbGrps.iterator(); iter.hasNext(); )
            {
                EjbGroup grp = (EjbGroup)iter.next();
                
                for( Iterator jarIter = grp.getClientJarFiles().iterator(); jarIter.hasNext(); )
                {
                    String jarFileName = org.netbeans.modules.visualweb.ejb.util.Util.getFileName( (String)jarIter.next() );
                    
                    if( jarFileName.equals( jar ) )
                        return grp;
                }
            }

            return null;
        }
        catch( Exception e )
        {
            // Having problem find the ejb group. It's ok. Return null;
            e.printStackTrace();
            return null;
        }
    }
}
