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
 * AddEjbGroupDialog.java
 *
 * Created on May 5, 2004, 12:31 PM
 */

package org.netbeans.modules.visualweb.ejb.ui;

import javax.swing.event.ChangeEvent;
import org.netbeans.modules.visualweb.ejb.datamodel.EjbDataModel;
import org.netbeans.modules.visualweb.ejb.datamodel.EjbGroup;
import org.netbeans.modules.visualweb.ejb.datamodel.EjbInfo;
import org.netbeans.modules.visualweb.ejb.datamodel.MethodInfo;
import org.netbeans.modules.visualweb.ejb.load.EjbLoadException;
import org.netbeans.modules.visualweb.ejb.load.EjbLoader;
import java.awt.Dialog;
import java.text.MessageFormat;
import java.util.*;
import javax.swing.event.ChangeListener;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle; 

/**
 * The dialog for adding a new EJB group. The dialog contains a two steps wizard.
 * The first step gathers data for the EJBs to be added. The second step is to configure
 * the business methods of the EJBs.
 *
 * @author  cao
 */
public class AddEjbGroupDialog {
    
    /** See org.openide.WizardDescriptor.PROP_AUTO_WIZARD_STYLE
     */
    private static final String PROP_AUTO_WIZARD_STYLE = WizardDescriptor.PROP_AUTO_WIZARD_STYLE; // NOI18N
    /** See org.openide.WizardDescriptor.PROP_CONTENT_DISPLAYED
     */
    private static final String PROP_CONTENT_DISPLAYED = WizardDescriptor.PROP_CONTENT_DISPLAYED; // NOI18N
    /** See org.openide.WizardDescriptor.PROP_CONTENT_NUMBERED
     */
    private static final String PROP_CONTENT_NUMBERED = WizardDescriptor.PROP_CONTENT_NUMBERED; // NOI18N
    /** See org.openide.WizardDescriptor.PROP_CONTENT_SELECTED_INDEX
     */
    private static final String PROP_CONTENT_SELECTED_INDEX = WizardDescriptor.PROP_CONTENT_SELECTED_INDEX; // NOI18N
    /** See org.openide.WizardDescriptor.PROP_HELP_DISPLAYED
     */
    private static final String PROP_HELP_DISPLAYED = WizardDescriptor.PROP_HELP_DISPLAYED; // NOI18N
    /** See org.openide.WizardDescriptor.PROP_CONTENT_DATA
     */
    private static final String PROP_CONTENT_DATA = WizardDescriptor.PROP_CONTENT_DATA; // NOI18N
    
    private EjbWizardDescriptor wizardDescriptor;
    private Dialog dialog;
    
    private EjbGroupPanel addPanel;
    private ConfigureMethodsPanel configureMethodsPanel;
    
    private EjbGroup ejbGroup;
    private EjbLoader ejbLoader;
    
    private boolean valid = true;
    private AddEjbsWizardPanel addWizardPanel;
    private ConfigureMethodWizardPanel configWizardPanel;
    
    public AddEjbGroupDialog() {
        
        // The first wizard panel
        addPanel = new EjbGroupPanel();
        addWizardPanel =  new AddEjbsWizardPanel();
        
        addPanel.addChangeListener(addWizardPanel);
        // The second wizard panel
        configWizardPanel = new ConfigureMethodWizardPanel();
        
        // Create the wizard descriptor
        WizardDescriptor.Panel[] wizardPanels = new WizardDescriptor.Panel[] { addWizardPanel, configWizardPanel  };
        wizardDescriptor = new EjbWizardDescriptor( wizardPanels );
        
        // The following properties are need in order to get the content panel on the left side
        wizardDescriptor.putProperty( PROP_AUTO_WIZARD_STYLE, Boolean.TRUE);
        wizardDescriptor.putProperty( PROP_CONTENT_DISPLAYED, Boolean.TRUE );
        wizardDescriptor.putProperty( PROP_CONTENT_NUMBERED, Boolean.TRUE);
        wizardDescriptor.putProperty( PROP_CONTENT_DATA, new String[] { NbBundle.getMessage(AddEjbGroupDialog.class, "ADD_EJB_GROUP" ), 
                              NbBundle.getMessage(AddEjbGroupDialog.class, "CONFIGURE_METHODS" ) } );
        wizardDescriptor.setTitleFormat(new MessageFormat("{0}")); // NOI18N
        
        // Create the dialog 
        dialog = DialogDisplayer.getDefault().createDialog( wizardDescriptor );
        dialog.setTitle( NbBundle.getMessage(AddEjbGroupDialog.class, "ADD_EJB_GROUP" ) );
        dialog.setResizable(true);
        dialog.pack();
    }   
    
    public void showDialog() {
        dialog.setVisible( true );
    }
    
    private String loadingEjbGroup() {
        // Make sure the user has entered all the required data
        StringBuffer errorMessage = new StringBuffer();
        if( !addPanel.validateData( errorMessage ) ) {
            return errorMessage.toString();
        }
        
        // Get all the user input from the inner panel
        ejbGroup = new EjbGroup();
        ejbGroup.setName( addPanel.getGroupName() );
        ejbGroup.setClientJarFiles( addPanel.getClientJars() );
        ejbGroup.setAppServerVendor( addPanel.getContainerType() );
        ejbGroup.setServerHost( addPanel.getServerHost() );
        ejbGroup.setIIOPPort( Integer.parseInt( addPanel.getIIOPPort() ) );
        ejbGroup.setDDLocationFile( addPanel.getDDLocationFile() );
        
        // Check whether the client jar files are already added with
        // different information (i.e. hostname, iiop port)
        String check = checkClientJarInfo( ejbGroup );
        if (check != null) {
            return check;
        }
        
        try {
            // Try to load the EjbGroup
            ejbLoader = new EjbLoader( ejbGroup );
            ejbLoader.load();
            
            // Good, the ejbs are loaded ok
            return null;
        }
        catch( EjbLoadException ex ) {            
            String msg = ex.getMessage();
            
            // SYSTEM_EXCEPTION means something out of user's control. It should never happen. But it did
            if( ex.getExceptionType() == EjbLoadException.SYSTEM_EXCEPTION )
                msg = NbBundle.getMessage( AddEjbGroupDialog.class, "FAILED_TO_LOAD_EJBS", ejbGroup.getName() );
            
            return msg;
        }
    }
    
    private boolean createEjbGroup() {
        try {
            
            ejbLoader.createWrapperClientBeans();
            EjbDataModel.getInstance().addEjbGroup( ejbGroup );
            
            return true;
            
        }catch( EjbLoadException ex ) {
            // Popup error message here to ask the them give correct information
            
            String msg = ex.getMessage();
            
            // SYSTEM_EXCEPTION means something out of user's control. It should never happen. But it did
            if( ex.getExceptionType() == EjbLoadException.SYSTEM_EXCEPTION )
                msg = NbBundle.getMessage( AddEjbGroupDialog.class, "FAILED_TO_LOAD_EJBS", ejbGroup.getName() );
            
            if( ex.getExceptionType() != EjbLoadException.WARNING ) {
                NotifyDescriptor d = new NotifyDescriptor.Message( msg, NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify( d );
            }
            else {
                // Just a warning. Need to continue on with the operation
                NotifyDescriptor d = new NotifyDescriptor.Message( msg, NotifyDescriptor.INFORMATION_MESSAGE);
                DialogDisplayer.getDefault().notify( d );
            }
            
            return false;
        }
    }
    
    private String checkClientJarInfo( EjbGroup grp ) {
        StringBuffer msg = new StringBuffer();
        
        for( Iterator iter = grp.getClientJarFileNames().iterator(); iter.hasNext(); ) {
            String jar = (String)iter.next();
            
            EjbGroup existingGrpWithJar = EjbDataModel.getInstance().findEjbGroupForJar( jar);
            
            if( existingGrpWithJar != null &&
                ( !existingGrpWithJar.getAppServerVendor().equals( grp.getAppServerVendor() )  ||
                ( !existingGrpWithJar.getServerHost().equals( grp.getServerHost() ) ||
                  existingGrpWithJar.getIIOPPort() != grp.getIIOPPort() ) ) ) {
                // Found a group containing the client jar with different information
                // Client Jar {0} was added in EJB set {1} with different server host name and/or RMI IIOP port.
                msg.append( NbBundle.getMessage( AddEjbGroupDialog.class, "MISMATCH_INFO_JAR", jar, existingGrpWithJar.getName() ) );
                msg.append( "\n" );
            }
        }
        
        if( msg.length() != 0 ) {
            return msg.toString();
        }
        else
            return null;
    }
    
    private boolean checkColElemClasses()
    {
        boolean allSet = true;
        ArrayList invalidMethodNames = new ArrayList();
        
        // Make sure all the collection element classes are specified
        for( Iterator iter = ejbGroup.getSessionBeans().iterator(); iter.hasNext(); )
        {
            EjbInfo ejbInfo = (EjbInfo)iter.next();
            
            for( Iterator mIter = ejbInfo.getMethods().iterator(); mIter.hasNext(); )
            {
                MethodInfo methodInfo = (MethodInfo)mIter.next();
                
                if( !methodInfo.isBusinessMethod() ||
                    (methodInfo.isBusinessMethod() && !methodInfo.getReturnType().isCollection() ) )
                    continue;
                else
                {
                    // Check whether the element class is set
                    if( methodInfo.getReturnType().getElemClassName() == null )
                    {
                        invalidMethodNames.add( methodInfo.getName() );
                        allSet = false;
                    }
                }
            }
            
        }
        
        // TODO: need to figure out the method names and have the first one selected
        if( !allSet ) {
            NotifyDescriptor d = new NotifyDescriptor.Message( "One or more collection element types are not specified: " + invalidMethodNames.toString() );
            DialogDisplayer.getDefault().notify( d );
        }
       
        
        return allSet;
    }
    
    // Expose updateState() method to disable next/finish buttons
    private static class EjbWizardDescriptor extends WizardDescriptor {
        public EjbWizardDescriptor(WizardDescriptor.Panel[] panels) {
            super(panels);
        }
        
        public void updateNavigatingState() {
            super.updateState();
        }
        
    }
    
    /**
     * The wizard panel for gathering the EJB information
     */
    private class AddEjbsWizardPanel implements WizardDescriptor.ValidatingPanel, ChangeListener {
        
        private boolean valid = true;
        
        public AddEjbsWizardPanel() {
        }
        
        public void addChangeListener(javax.swing.event.ChangeListener l) {
        }
        
        public java.awt.Component getComponent() {
            // Set the selected index so that the proper entry is selected in the content panel 
            addPanel.putClientProperty( PROP_CONTENT_SELECTED_INDEX, new Integer(0) );
            return addPanel;
        }
        
        public HelpCtx getHelp() {
            return new HelpCtx("projrave_ejb_howtoejbs_ejb_add_to_IDE");
        }
        
        public boolean isValid() {
            String groupName = addPanel.getGroupName();
            return groupName != null && EjbDataModel.getInstance().getEjbGroup(groupName) == null;
        }
        
        public void readSettings(Object settings) {
        }
        
        public void removeChangeListener(javax.swing.event.ChangeListener l) {
        }
        
        public void storeSettings(Object settings) {
            
        }
        
        public void validate() throws org.openide.WizardValidationException {            
            if (!isValid()) {
                String groupName = (addPanel.getGroupName() != null) ? addPanel.getGroupName() : "";
                String errorMsg = NbBundle.getMessage(AddEjbGroupDialog.class, "NAME_NOT_UNIQUE", groupName);
                throw new org.openide.WizardValidationException( addPanel,  errorMsg, errorMsg );
            }
            
            // Throw WizardValidationException will cause the wizard to stay at the same step
            String loadResult = loadingEjbGroup();
            if( loadResult != null )
                throw new org.openide.WizardValidationException( addPanel,  NbBundle.getMessage(AddEjbGroupDialog.class, "IMPORT_SET_ERROR" ), loadResult );
        }

        public void stateChanged(ChangeEvent e) {
            if (!isValid()) {
                valid = false;
                wizardDescriptor.updateNavigatingState();

                String groupName = (addPanel.getGroupName() != null) ? addPanel.getGroupName() : "";
                String errorMsg = NbBundle.getMessage(AddEjbGroupDialog.class, "NAME_NOT_UNIQUE", groupName);
                
                wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, errorMsg); // NOI18N
            }else if (!valid) {
                valid = true;
                wizardDescriptor.updateNavigatingState();
                wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, null); // NOI18N
            }
        }
        
    }
    
    /**
     * The wizard panel for configuring the ejb business method.
     * It is the last panel in the wizard
     */
    private class ConfigureMethodWizardPanel implements WizardDescriptor.ValidatingPanel, WizardDescriptor.FinishablePanel {
        
        public ConfigureMethodWizardPanel( ) {
            
        }
        
        public void addChangeListener(javax.swing.event.ChangeListener l) {
        }
        
        public java.awt.Component getComponent() {
            configureMethodsPanel = new ConfigureMethodsPanel( ejbGroup );
            
            // Set the selected index so that the proper entry is selected in the content panel 
            configureMethodsPanel.putClientProperty( PROP_CONTENT_SELECTED_INDEX, new Integer(1) );
            return configureMethodsPanel;
        }
        
        public HelpCtx getHelp() {
            // todo help
            //return new HelpCtx("projrave_ui_elements_server_nav_ejb_node");
            return HelpCtx.DEFAULT_HELP;
        }
        
        public boolean isValid() {
            return true;
        }
        
        public void readSettings(Object settings) {
        }
        
        public void removeChangeListener(javax.swing.event.ChangeListener l) {
        }
        
        public void storeSettings(Object settings) {
        }
        
        public void validate() throws org.openide.WizardValidationException {
            
            // Get the last class name input from the user in the method detail panel
            configureMethodsPanel.getMethodDetailPanel().updateColElemClassName();
            
            // Programmatically stop the CellEditor so that we do not lose the very last editting value
            configureMethodsPanel.getMethodDetailPanel().stopLastCellEditing();
            
            // First, make sure the collection element classes are specified
            if( ! checkColElemClasses() )
                throw new org.openide.WizardValidationException( addPanel,  "not valid", "not valid" ); // TODO I18N
            
            if( !createEjbGroup() )
                throw new org.openide.WizardValidationException( addPanel,  "not valid", "not valid" ); // TODO I18N
        }
        
        public boolean isFinishPanel() {
            return true;
        }
        
    }
    
}
