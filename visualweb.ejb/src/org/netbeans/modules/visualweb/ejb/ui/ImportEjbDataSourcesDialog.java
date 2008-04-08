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
 * ImportEjbDataSourcesDialog.java
 *
 * Created on September 1, 2004, 10:42 AM
 */

package org.netbeans.modules.visualweb.ejb.ui;

import org.netbeans.modules.visualweb.ejb.datamodel.EjbDataModel;
import org.netbeans.modules.visualweb.ejb.datamodel.EjbGroup;
import java.awt.Dialog;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.*;
import javax.swing.JButton;
import org.netbeans.modules.visualweb.ejb.util.Util;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import org.netbeans.modules.visualweb.extension.openide.io.RaveFileCopy;

/**
 * A dialog for import EJB datasources
 *
 * @author  cao
 */
public class ImportEjbDataSourcesDialog implements ActionListener {
    
    private Dialog dialog;
    
    private JButton okButton;
    private JButton cancelButton;
    
    // The panel for importing EJB datasources
    private ImportEjbDataSourcesPanel importPanel;
    
    public ImportEjbDataSourcesDialog() {
        
        importPanel = new ImportEjbDataSourcesPanel();
       
        DialogDescriptor dialogDescriptor = new DialogDescriptor( importPanel, NbBundle.getMessage(ImportEjbDataSourcesDialog.class, "IMPORT_EJB_DATASOURCES"), 
                                                                  true, (ActionListener)this );
        
        okButton = new JButton( NbBundle.getMessage(ImportEjbDataSourcesDialog.class, "OK_BUTTON_LABEL") );
        
        okButton.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/ejb/ui/Bundle").getString("OK_BUTTON_DESC"));
        okButton.setMnemonic(NbBundle.getMessage(ImportEjbDataSourcesDialog.class, "OK_BUTTON_MNEMONIC").charAt(0));
        cancelButton = new JButton( NbBundle.getMessage(ImportEjbDataSourcesDialog.class, "CANCEL_BUTTON_LABEL") );
        cancelButton.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/ejb/ui/Bundle").getString("CANCEL_BUTTON_DESC"));
        cancelButton.setMnemonic(NbBundle.getMessage(ImportEjbDataSourcesDialog.class, "CANCEL_BUTTON_MNEMONIC").charAt(0));
        dialogDescriptor.setOptions(new Object[] { okButton, cancelButton });
        dialogDescriptor.setClosingOptions(new Object[] {cancelButton});
        
        // TODO: no help for preview feature
        dialogDescriptor.setHelpCtx(new HelpCtx("projrave_ui_elements_server_nav_import_ejb_db"));
        
        dialog = DialogDisplayer.getDefault().createDialog( dialogDescriptor );
        dialog.setResizable(true);
    }
    
    public void showDialog()
    {
        // First, the user has to choose a file to import from
        
        ImportExportFileChooser fileChooser = new ImportExportFileChooser( importPanel );
        String selectedFile = fileChooser.getImportFile();
        if( selectedFile != null )
        {
            // First, need to check the file existence here 
            if( !(new File(selectedFile)).exists() ) 
            {
                String msg = NbBundle.getMessage(ImportEjbDataSourcesPanel.class, "IMPORT_FILE_NOT_FOUND", selectedFile );
                NotifyDescriptor d = new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
                return;
            }
           
            fileChooser.setCurrentFilePath( selectedFile );
            importPanel.setImportFilePath( selectedFile );

            // Read in the data from the import file
            
            PortableEjbDataSource[] ejbDataSources = ImportEjbDataSourcesHelper.readDataSourceImports( importPanel.getImportFilePath() );
            if( ejbDataSources != null && ejbDataSources.length != 0 )
                importPanel.setEjbDataSources( ejbDataSources );
            else
            {
                String msg = NbBundle.getMessage(ImportEjbDataSourcesPanel.class, "NO_EJB_SET_FOR_IMPORT", selectedFile );
                NotifyDescriptor d = new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
                return;
            }
            
            // Bring up the dialog with the to-be-imported EJB datasources
            dialog.pack();
            dialog.setVisible( true );
        }
    }
   
    public void actionPerformed(java.awt.event.ActionEvent e) 
    {
        if( e.getSource() == okButton )
        {  
            // Before anything, save the changes the user made if any
            if( !importPanel.saveChange() )
                return;
            
            PortableEjbDataSource[] ejbDataSources = importPanel.getEjbDataSources();
            
            // Added the selected groups to the EjbDataModel
            // and copy over the client jars and wrapper jars

            boolean noneSelected = true;
            for( int i = 0; i < ejbDataSources.length; i ++ )
            {
                if( ejbDataSources[i].isPortable() )
                {
                    // Copy the client jars and wrapper jar to the user dir
                    copyJars( ejbDataSources[i].getEjbGroup() );
                    EjbDataModel.getInstance().addEjbGroup( ejbDataSources[i].getEjbGroup() );
                    
                    noneSelected = false;
                }
            }
            
            if( noneSelected )
            {
                String msg = NbBundle.getMessage(ImportEjbDataSourcesDialog.class, "NO_IMPORT_SELECTION");
                NotifyDescriptor d = new NotifyDescriptor.Message(msg, NotifyDescriptor.INFORMATION_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
                return;
            }
            
            dialog.dispose();
        }
    }
    
    private void copyJars( EjbGroup ejbGroup )
    {
        try {
            String ejbDir = Util.getEjbStateDir().getAbsolutePath() + File.separator + "ejb-datasource"; // NOI18N
            
            // Client jars
            for( Iterator iter = ejbGroup.getClientJarFiles().iterator(); iter.hasNext(); )
            {
                String jarPath = (String)iter.next();
                String jarName = org.netbeans.modules.visualweb.ejb.util.Util.getFileName( jarPath );
                RaveFileCopy.fileCopy( new File(jarPath), new File(ejbDir, jarName ) );
            }
            
            // Client Wrapper bean jar
            String wrapperJarPath = ejbGroup.getClientWrapperBeanJar();
            String wrapperJarName = org.netbeans.modules.visualweb.ejb.util.Util.getFileName( wrapperJarPath );
            RaveFileCopy.fileCopy( new File(wrapperJarPath), new File(ejbDir, wrapperJarName ) );
            
            // DesignInfo jar
            String designInfoJarPath = ejbGroup.getDesignInfoJar();
            String designInfoJarName = org.netbeans.modules.visualweb.ejb.util.Util.getFileName( designInfoJarPath );
            RaveFileCopy.fileCopy( new File(designInfoJarPath), new File(ejbDir, designInfoJarName ) );
            
            // Fix the jar locations 
            ejbGroup.fixJarDir( ejbDir );
            
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
        }
    
    }
}
