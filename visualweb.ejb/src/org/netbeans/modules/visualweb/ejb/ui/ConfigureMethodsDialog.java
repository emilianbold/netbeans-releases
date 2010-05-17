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
import org.netbeans.modules.visualweb.ejb.datamodel.EjbInfo;
import org.netbeans.modules.visualweb.ejb.datamodel.MethodInfo;
import org.netbeans.modules.visualweb.ejb.datamodel.MethodParam;
import org.netbeans.modules.visualweb.ejb.load.EjbLoadException;
import org.netbeans.modules.visualweb.ejb.load.EjbLoader;
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
public class ConfigureMethodsDialog implements ActionListener
{
    private boolean cancelled = false;
    private boolean refresh = false;
    
    private DialogDescriptor dialogDescriptor;
    private Dialog dialog;
    
    private ConfigureMethodsPanel configurePanel;
    
    private JButton okButton;
    private JButton cancelButton;
    
    private EjbGroup ejbGroup;
    private EjbGroup origCopy;
    
    public ConfigureMethodsDialog(EjbGroup group, boolean refresh )
    {
        this( group );
        this.refresh = refresh;
    }
    
    public ConfigureMethodsDialog(EjbGroup group)
    {   
        origCopy = group;
        ejbGroup = (EjbGroup)group.clone();
        
        configurePanel = new ConfigureMethodsPanel( ejbGroup );
       
        dialogDescriptor = new DialogDescriptor( configurePanel, NbBundle.getMessage(ConfigureMethodsDialog.class, "CONFIGURE_METHODS"),
                                                 true, (ActionListener)this );
        
        okButton = new JButton( NbBundle.getMessage(ConfigureMethodsDialog.class, "OK") );
        okButton.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/ejb/ui/Bundle").getString("OK"));
        cancelButton = new JButton( NbBundle.getMessage(ConfigureMethodsDialog.class, "CANCEL_BUTTON_LABEL") );
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
    
    public boolean isCancelled() {
        return cancelled;
    }
    
    public EjbGroup getEjbGroup () {
        return this.ejbGroup;
    }
    
    public void enableAddButton( boolean enable )
    {
        this.okButton.setEnabled( enable );
    }
    
    public void actionPerformed(java.awt.event.ActionEvent e) {
        
        if( e.getSource() == okButton ) {
            
            // Programmatically stop the CellEditor so that we do not lose the very last editting value
            configurePanel.getMethodDetailPanel().stopLastCellEditing();
            
            // Get the last class name input from the user in the method detail panel
            configurePanel.getMethodDetailPanel().updateColElemClassName();
            
            // If anything has changed, the all the classes will be regenerated
            if( refresh || isModified( origCopy, ejbGroup ) )
            {
                
                // Regenerate the classes
                                
                EjbLoader ejbLoader = new EjbLoader( ejbGroup );
                
                try {
                    ejbLoader.createWrapperClientBeans();    
                    
                    // (IZ 199266) Propagate modification here so compilation
                    // errors do not corrupt the data model
                    doModification(origCopy, ejbGroup);
                    
                    EjbDataModel.getInstance().touchModifiedFlag();
                }catch( EjbLoadException ex ) {
                    // Popup error message here to ask the them give correct information

                    String msg = ex.getMessage();
                    if( ex.getExceptionType() == EjbLoadException.SYSTEM_EXCEPTION )
                        msg = NbBundle.getMessage( ConfigureMethodsDialog.class, "FAILED_TO_MODIFY_METHODS_CONFIG", ejbGroup.getName() );

                    NotifyDescriptor d = new NotifyDescriptor.Message( msg, NotifyDescriptor.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notify( d );
                    return;
                }
            }
            
            dialog.dispose();
        }
        else if( e.getSource() == cancelButton ) {
            cancelled = true;
        }
    }
    
    private boolean isModified(EjbGroup origGroup, EjbGroup modGroup) {
        return findModifications(origGroup, modGroup, false);
    }
    
    private void doModification(EjbGroup origGroup, EjbGroup modGroup) {
        findModifications(origGroup, modGroup, true);
    }
    
    private boolean findModifications( EjbGroup origGroup, EjbGroup modGroup, boolean modifyOriginal )
    {
        boolean foundModification = false;
        
        for( Iterator iter = origGroup.getSessionBeans().iterator(); iter.hasNext(); )
        {
            EjbInfo origEjbInfo = (EjbInfo)iter.next();
            EjbInfo modEjbInfo = modGroup.getEjbInfo( origEjbInfo.getCompInterfaceName() );
            
            // Compare mehtod by method
            ArrayList origMethods = origEjbInfo.getMethods(); 
            ArrayList modMethods = modEjbInfo.getMethods(); 
            for( int i = 0; i < origMethods.size(); i ++ )
            {
                MethodInfo origMethod = (MethodInfo)origMethods.get( i );
                
                if( origMethod.isMethodConfigurable() )
                {
                    // Anything changed
                    MethodInfo modMethod = (MethodInfo)modMethods.get( i );
                    
                    if( origMethod.getReturnType().isCollection() )
                    {
                        // Check whehter the collection element class type change
                        if( !origMethod.getReturnType().getElemClassName().equals( modMethod.getReturnType().getElemClassName() ) )
                        {
                            origMethod.getReturnType().setElemClassName( modMethod.getReturnType().getElemClassName() );
                            foundModification = true;
                        }
                    }
                    
                    // Check parameter names
                    ArrayList origParamNames = origMethod.getParameters();
                    if( origParamNames != null && !origParamNames.isEmpty() )
                    {
                        ArrayList modParamNames = modMethod.getParameters();
                        
                        for( int pi = 0; pi < origParamNames.size(); pi ++ )
                        {
                            MethodParam origParam = (MethodParam)origParamNames.get( pi );
                            MethodParam modParam = (MethodParam)modParamNames.get( pi );
                            
                            if( !origParam.getName().equals( modParam.getName() ) ) 
                            {
                                if (modifyOriginal) {
                                    origParam.setName( modParam.getName() );
                                }
                                
                                foundModification = true;
                            }
                        }
                    }
                }
            }
            
        }
        
        return foundModification;
    }
}
