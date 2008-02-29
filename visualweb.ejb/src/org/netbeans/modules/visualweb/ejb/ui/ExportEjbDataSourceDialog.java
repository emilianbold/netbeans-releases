/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * EjbDataSourceSelectionDialog.java
 *
 * Created on August 31, 2004, 10:31 AM
 */

package org.netbeans.modules.visualweb.ejb.ui;
import org.netbeans.modules.visualweb.ejb.datamodel.EjbDataModel;
import org.netbeans.modules.visualweb.ejb.datamodel.EjbGroup;
import org.netbeans.modules.visualweb.ejb.load.EjbDataSourceXmlCreator;
import java.awt.Dialog;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import javax.swing.JButton;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * A dialog for exporting EJB datasources
 *
 * @author  cao
 */
public class ExportEjbDataSourceDialog implements ActionListener {
    
    private Dialog dialog;
    
    private JButton okButton;
    private JButton cancelButton;
    
    // The panel for exporting EJB datasources
    private ExportEjbDataSourcesPanel exportPanel;
    
    private PortableEjbDataSource[] ejbDatasources;
    
    public ExportEjbDataSourceDialog() {
        ejbDatasources = getPortableEjbDataSources( null );
        createDialog();
    }
    
    public ExportEjbDataSourceDialog( Collection ejbGroupNames ) {
        ejbDatasources = getPortableEjbDataSources( ejbGroupNames );
        createDialog();
    }
    
    private void createDialog() {
        
        exportPanel = new ExportEjbDataSourcesPanel();
        exportPanel.setEjbDataSources( ejbDatasources );
       
        DialogDescriptor dialogDescriptor = new DialogDescriptor( exportPanel, NbBundle.getMessage(ExportEjbDataSourceDialog.class, "EXPORT_EJB_DATASOURCES"), 
                                                 true, (ActionListener)this );
        
        okButton = new JButton( NbBundle.getMessage(ImportEjbDataSourcesDialog.class, "OK_BUTTON_LABEL") );
        
        okButton.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/ejb/ui/Bundle").getString("OK_BUTTON_DESC"));
        okButton.setMnemonic(NbBundle.getMessage(ImportEjbDataSourcesDialog.class, "OK_BUTTON_MNEMONIC").charAt(0));
        cancelButton = new JButton( NbBundle.getMessage(ExportEjbDataSourceDialog.class, "CANCEL_BUTTON_LABEL") );
        cancelButton.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/ejb/ui/Bundle").getString("CANCEL_BUTTON_DESC"));
        cancelButton.setMnemonic(NbBundle.getMessage(ImportEjbDataSourcesDialog.class, "CANCEL_BUTTON_MNEMONIC").charAt(0));
        dialogDescriptor.setOptions(new Object[] { okButton, cancelButton });
        dialogDescriptor.setClosingOptions(new Object[] {cancelButton});
        
        // help
        dialogDescriptor.setHelpCtx(new HelpCtx("projrave_ejb_howtoejbs_ejb_export")); // NOI18N
        
        dialog = DialogDisplayer.getDefault().createDialog( dialogDescriptor );
        dialog.setResizable(true);
    }
    
    public void showDialog()
    {
        dialog.pack();
        dialog.setVisible( true );
    }
    
    public void actionPerformed(java.awt.event.ActionEvent e) 
    {
        if( e.getSource() == okButton )
        {   
            // Before anything, save the changes the user made if any
            if( !exportPanel.saveChange() )
                return;
            
            // Get the selected EJB groups
            ArrayList selectedEjbGrps = new ArrayList();
            for( int i = 0; i < ejbDatasources.length; i ++ )
            {
                if( ejbDatasources[i].isPortable() )
                    selectedEjbGrps.add( ejbDatasources[i].getEjbGroup() );
            }
            
            // Make sure at least one ejb datasource is selected
            if( selectedEjbGrps.isEmpty() )
            {
                String msg = NbBundle.getMessage(ExportEjbDataSourceDialog.class, "NO_EXPORT_SELECTION");
                NotifyDescriptor d = new NotifyDescriptor.Message(msg, NotifyDescriptor.INFORMATION_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
                return;
            }
            
            String filePath = exportPanel.getFilePath();
            
            // Make sure the user has entered a file to export to
            if( filePath == null || filePath.length() == 0 )
            {
                String msg = NbBundle.getMessage(ExportEjbDataSourceDialog.class, "NO_EXPORT_FILE");
                NotifyDescriptor d = new NotifyDescriptor.Message(msg, NotifyDescriptor.INFORMATION_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
                return;
            }
            
            // If the filename is not ended with .jar, lets append .jar to it
            if( !filePath.toLowerCase().endsWith( ".jar" ) ) // NOI18N
                filePath = filePath + ".jar";   // NOI18N
            
            // Make the file name is not existed
            if( new File(filePath).exists() )
            {
                String msg = NbBundle.getMessage( ExportEjbDataSourceDialog.class, "FILE_EXISTS", filePath );
                NotifyDescriptor confDialog = new NotifyDescriptor.Confirmation(msg, NotifyDescriptor.OK_CANCEL_OPTION);
                if( !(DialogDisplayer.getDefault().notify(confDialog) == NotifyDescriptor.OK_OPTION) ) 
                    return;
            }
            
            // Looks like everything is OK
            // Lets start export
            if( export( selectedEjbGrps, filePath ) )
            {
                // Done. Notify the user
                
                String successMsg = null;
                if( selectedEjbGrps.size() == 1 )
                    successMsg = NbBundle.getMessage( ExportEjbDataSourceDialog.class, "EXPORT_SUCCESSFULLY_ONE", filePath );
                else
                    successMsg = NbBundle.getMessage( ExportEjbDataSourceDialog.class, "EXPORT_SUCCESSFULLY", Integer.toString(selectedEjbGrps.size()), filePath );
                
                NotifyDescriptor d = new NotifyDescriptor.Message( successMsg, NotifyDescriptor.INFORMATION_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
                
                dialog.dispose();
            }
        }
    }
    
    // Export the give EJB groups as a jar file
    private boolean export( Collection ejbGroups, String filePath )
    {
        // Make user this is called before createXmlFile() before createXmlFile() 
        // modifies the jar location
        Collection toBeJaredFiles = getJarFilePathes( ejbGroups );
        
        // Something is not right with at least one of the jar files
        if( toBeJaredFiles == null )
            return false;
        
        // First, write the metadata to a ejbsources.xml file in a tmp directory
        String xmlFile = createXmlFile( ejbGroups );
        if( xmlFile != null )
        {
            // Then, jar the ejbsources.xml with client jars and wrapper jars into the file the user entered
            
            toBeJaredFiles.add( xmlFile );

            if( jarThemUp( filePath, toBeJaredFiles ) )
                return true;
            else
                return false;
        }
        else
            return false;
    }
    
    // Writes the given EJB groups to a ejbsources.xml file
    private String createXmlFile( Collection ejbGroups )
    {
        String xmlFile = System.getProperty("java.io.tmpdir") + File.separator + "ejbsources.xml"; // NOI18N
        //String xmlFile = "d:/home/cao" + File.separator + "ejbsources.xml"; // NOI18N
        
        try
        {
            File file = new File( xmlFile );
            file.deleteOnExit();

            OutputStreamWriter outputStreamWriter = new OutputStreamWriter( new FileOutputStream( file), "UTF-8" );
            BufferedWriter bufferWriter = new BufferedWriter( outputStreamWriter );
            
            // going to only write the jar filenames (not path) to xml file
            for( Iterator iter = ejbGroups.iterator(); iter.hasNext(); )
            {
                EjbGroup grp = (EjbGroup)iter.next();
                grp.setClientJarFiles( grp.getClientJarFileNames() );
                grp.setClientWrapperBeanJar( org.netbeans.modules.visualweb.ejb.util.Util.getFileName( grp.getClientWrapperBeanJar() ) );
                grp.setDesignInfoJar( org.netbeans.modules.visualweb.ejb.util.Util.getFileName( grp.getDesignInfoJar() ) );
            }

            EjbDataSourceXmlCreator creator = new EjbDataSourceXmlCreator( ejbGroups, bufferWriter );
            creator.toXml();
            bufferWriter.flush();
            bufferWriter.close();
            
            return xmlFile;
        }
        catch( java.io.IOException ex )
        {
            ex.printStackTrace();
            String msg = NbBundle.getMessage(ExportEjbDataSourceDialog.class, "ERROR_EXPORT");
            NotifyDescriptor d = new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
            return null;
        }
    }
    
    private Collection getJarFilePathes( Collection ejbGroups )
    {
        // Use set to eliminate dup jars
        Set pathes = new HashSet();
        
        for( Iterator iter = ejbGroups.iterator(); iter.hasNext(); )
        {
            EjbGroup grp = (EjbGroup)iter.next();
            pathes.addAll( grp.getClientJarFiles() );
            pathes.add( grp.getClientWrapperBeanJar() );
            pathes.add( grp.getDesignInfoJar() );
        }
        
        // Make sure the files exists
        return checkJarFileExistence( pathes );
    }
    
    private Collection checkJarFileExistence( Collection files ) 
    {
        StringBuffer notFoundFiles = new StringBuffer();
        boolean first = true;
        for( Iterator iter = files.iterator(); iter.hasNext(); )
        {
            String path = (String)iter.next();
            
            if( !(new File(path)).exists() )
            {
                if( first )
                    first = false;
                else
                    notFoundFiles.append( ", " ); // NOI18N
                
                notFoundFiles.append( path );
            }   
        }
        
        if( notFoundFiles.length() != 0 )
        {
            // Failed to export the selected EJB set(s). File(s) {0} not found.
            String msg = NbBundle.getMessage( ExportEjbDataSourceDialog.class, "FILES_NOT_FOUND", notFoundFiles );
            NotifyDescriptor d = new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
            
            return null;
        }
        else
            return files;
    }
    
    // Jar the given collection of files to the given destJarFile
    private boolean jarThemUp ( String destJarFile, Collection files )
    {  
        try 
        {
            int BUFFER = 2048;
            
            BufferedInputStream origin = null;
            FileOutputStream dest = new FileOutputStream( destJarFile );
            JarOutputStream jarOutputStream = new JarOutputStream( new  BufferedOutputStream(dest) );
            jarOutputStream.setMethod( JarOutputStream.DEFLATED );
            
            byte data[] = new byte[BUFFER];
            
            Set alreadyAddedFileNames = new HashSet();
            
            for( Iterator iter = files.iterator(); iter.hasNext(); ) 
            {
                String filePath = (String)iter.next();
                
                // Since the entry name for the jar file is only the filename, not the whole path
                // need to make sure that we do not add the same jar file more than once.
                // It doesn't matter wheter the same file is from a different path or not
                String fileName = new File(filePath).getName();
                if( alreadyAddedFileNames.contains( fileName ) )
                    continue;
                else
                {
                    alreadyAddedFileNames.add( fileName );
                    
                    // Add it to the jar
                   
                    FileInputStream fileInputStream = new  FileInputStream( filePath );
                    origin = new  BufferedInputStream( fileInputStream, BUFFER );
                
                    JarEntry entry = new JarEntry( new File(filePath).getName() );
                    jarOutputStream.putNextEntry(entry);

                    int count;
                    while( (count = origin.read(data, 0, BUFFER)) != -1 ) 
                    {
                        jarOutputStream.write( data, 0, count );
                    }

                    origin.close();
                }
            }
            
            jarOutputStream.close();
            
            return true;
            
        } 
        catch( Exception e ) 
        {
            e.printStackTrace();
            String msg = NbBundle.getMessage(ExportEjbDataSourceDialog.class, "ERROR_EXPORT");
            NotifyDescriptor d = new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
            return false;
        }
    }
    
    private PortableEjbDataSource[] getPortableEjbDataSources( Collection defaultSelectedGroupNames )
    {
        Set ejbGrps = EjbDataModel.getInstance().getEjbGroups();
        
        if( ejbGrps != null )
        {
            PortableEjbDataSource[] ejbSrcs = new PortableEjbDataSource[ ejbGrps.size() ];
            
            int i = 0;
            for( Iterator iter = ejbGrps.iterator(); iter.hasNext(); )
            {
                EjbGroup grp = (EjbGroup)iter.next();
                
                // Should it be selected or not
                // If the defaultSelectedGroupNames is null, that means everything should be selected
                // Otherwise check whehter the group name is in the defaultSelectedGroupNames or not.
                boolean selected = false;
                if( defaultSelectedGroupNames == null )
                    selected = true;
                else if( defaultSelectedGroupNames.contains( grp.getName() ) )
                    selected = true;
                
                // Since the user can change some information on the ejb groups when exporting,
                // we'll clone the ejb group here
                ejbSrcs[i++] = new PortableEjbDataSource( (EjbGroup)grp.clone(), selected );
            }
            
            return ejbSrcs;
        }
        
        return null;
    }
}
