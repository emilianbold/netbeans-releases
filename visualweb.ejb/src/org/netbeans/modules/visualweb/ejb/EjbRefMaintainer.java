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
