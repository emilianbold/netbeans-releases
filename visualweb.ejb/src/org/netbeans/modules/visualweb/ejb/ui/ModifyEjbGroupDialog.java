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
 * ModifyEjbGroupDialog.java
 *
 * Created on June 10, 2004, 2:51 PM
 */

package org.netbeans.modules.visualweb.ejb.ui;

import org.netbeans.modules.visualweb.ejb.datamodel.EjbDataModel;
import org.netbeans.modules.visualweb.ejb.datamodel.EjbGroup;
import java.awt.Dialog;
import java.awt.event.ActionListener;
import java.util.*;
import javax.swing.JButton;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 * This class is to handle the modification of an existing EJB group
 *
 * @author  cao
 */
public class ModifyEjbGroupDialog implements ActionListener
{
    private DialogDescriptor dialogDescriptor;
    private Dialog dialog;
    
    private ModifyEjbGroupPanel grpPanel;
    
    private JButton okButton;
    private JButton cancelButton;
    
    private EjbGroup ejbGroup;
    private EjbGroup origCopy;
    
    public ModifyEjbGroupDialog( EjbGroup group )
    {   
        origCopy = group;
        ejbGroup = (EjbGroup)group.clone();
        
        grpPanel = new ModifyEjbGroupPanel( group );
       
        dialogDescriptor = new DialogDescriptor( grpPanel, NbBundle.getMessage(AddEjbGroupDialog.class, "MODIFY_EJB_GROUP"),
                                                 true, (ActionListener)this );
        
        okButton = new JButton( NbBundle.getMessage(ModifyEjbGroupDialog.class, "OK") );
        okButton.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/ejb/ui/Bundle").getString("OK"));
        cancelButton = new JButton( NbBundle.getMessage(ModifyEjbGroupDialog.class, "CANCEL_BUTTON_LABEL") );
        cancelButton.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/ejb/ui/Bundle").getString("CANCEL_BUTTON_DESC"));
        dialogDescriptor.setOptions(new Object[] { okButton, cancelButton });
        dialogDescriptor.setClosingOptions(new Object[] {cancelButton});
        
        // TODO: no help for preview feature
        //dialogDescriptor.setHelpCtx(new HelpCtx("projrave_ui_elements_server_nav_add_datasourcedb"));
        
        dialog = DialogDisplayer.getDefault().createDialog( dialogDescriptor );
        dialog.setResizable(true);
        dialog.pack();
    }
    
    public void showDialog()
    {
        dialog.setVisible( true );
    }
    
    public void enableAddButton( boolean enable )
    {
        this.okButton.setEnabled( enable );
    }
    
    public void actionPerformed(java.awt.event.ActionEvent e) 
    {
        if( e.getSource() == okButton )
        {
            // Make sure the user has entered all the required data
            StringBuffer errorMessage = new StringBuffer();
            if( !grpPanel.validateData( errorMessage ) )
            {
                NotifyDescriptor d = new NotifyDescriptor.Message( errorMessage.toString(), NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify( d );
                return;
            }
            
            // Get all the user input from the inner panel
            ejbGroup.setName( grpPanel.getGroupName() );
            ejbGroup.setServerHost( grpPanel.getServerHost() );
            ejbGroup.setIIOPPort( Integer.parseInt( grpPanel.getIIOPPort() ) );
            
            // In case of the same client jar file is added in  more than
            // one ejb group, warn the user to modify the other groups too
            if( checkClientJarInfo( ejbGroup ) )
            {
                // Take care the modification on the rest of the fields
                EjbDataModel.getInstance().modifyEjbGroup( origCopy, ejbGroup );

                dialog.dispose();
            }
        }
    }
    
    private boolean checkClientJarInfo( EjbGroup grp )
    {    
        ArrayList grpNames = new ArrayList();
        
        for( Iterator iter = grp.getClientJarFileNames().iterator(); iter.hasNext(); )
        {
            String jar = (String)iter.next();
            
            Collection grps = EjbDataModel.getInstance().findEjbGroupsForJar( jar);
            
            for( Iterator grpIter = grps.iterator(); grpIter.hasNext(); )
            {
                EjbGroup existingGrpWithJar = (EjbGroup)grpIter.next();
                
                if( existingGrpWithJar != null && 
                    ( !existingGrpWithJar.getServerHost().equals( grp.getServerHost() ) ||
                       existingGrpWithJar.getIIOPPort() != grp.getIIOPPort() ) )
                {
                    if( !grpNames.contains( existingGrpWithJar.getName() ) &&
                        !existingGrpWithJar.getName().equals( grp.getName() ) ) //Not itself
                        grpNames.add( existingGrpWithJar.getName() );
                }
            }
        }
        
        if( grpNames.size() != 0 )
        {
            // The server host and/or RMI-IIOP port modification will cause EJB Set {0} to
            // contain incorrect information. Would like to preceed?
            StringBuffer nameStr = new StringBuffer();
            boolean first = true;
            for( Iterator iter = grpNames.iterator(); iter.hasNext(); )
            {
                if( first )
                    first = false;
                else
                    nameStr.append( ", " );
                
                nameStr.append( (String)iter.next() );
                
            }
            String msg = NbBundle.getMessage( ModifyEjbGroupDialog.class, "MISMATCH_INFO_JAR_Q", nameStr.toString() );
            NotifyDescriptor confDialog = new NotifyDescriptor.Confirmation(msg, NotifyDescriptor.OK_CANCEL_OPTION);
            if( !(DialogDisplayer.getDefault().notify(confDialog) == NotifyDescriptor.OK_OPTION) ) 
                return false;
            else
                return true;
        }
        else
            return true;
    }
}
