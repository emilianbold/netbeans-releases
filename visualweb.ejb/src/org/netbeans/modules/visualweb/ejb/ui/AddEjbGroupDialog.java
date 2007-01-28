/*
 * AddEjbGroupDialog.java
 *
 * Created on May 5, 2004, 12:31 PM
 */

package org.netbeans.modules.visualweb.ejb.ui;

import org.netbeans.modules.visualweb.ejb.datamodel.EjbDataModel;
import org.netbeans.modules.visualweb.ejb.datamodel.EjbGroup;
import org.netbeans.modules.visualweb.ejb.datamodel.EjbInfo;
import org.netbeans.modules.visualweb.ejb.datamodel.MethodInfo;
import org.netbeans.modules.visualweb.ejb.load.EjbLoadException;
import org.netbeans.modules.visualweb.ejb.load.EjbLoader;
import java.awt.Dialog;
import java.text.MessageFormat;
import java.util.*;
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
    private static final String PROP_AUTO_WIZARD_STYLE = "WizardPanel_autoWizardStyle"; // NOI18N
    /** See org.openide.WizardDescriptor.PROP_CONTENT_DISPLAYED
     */
    private static final String PROP_CONTENT_DISPLAYED = "WizardPanel_contentDisplayed"; // NOI18N
    /** See org.openide.WizardDescriptor.PROP_CONTENT_NUMBERED
     */
    private static final String PROP_CONTENT_NUMBERED = "WizardPanel_contentNumbered"; // NOI18N
    /** See org.openide.WizardDescriptor.PROP_CONTENT_SELECTED_INDEX
     */
    private static final String PROP_CONTENT_SELECTED_INDEX = "WizardPanel_contentSelectedIndex"; // NOI18N
    /** See org.openide.WizardDescriptor.PROP_HELP_DISPLAYED
     */
    private static final String PROP_HELP_DISPLAYED = "WizardPanel_helpDisplayed"; // NOI18N
    /** See org.openide.WizardDescriptor.PROP_CONTENT_DATA
     */
    private static final String PROP_CONTENT_DATA = "WizardPanel_contentData"; // NOI18N
    
    private WizardDescriptor wizardDescriptor;
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
        
        // The second wizard panel
        configWizardPanel = new ConfigureMethodWizardPanel();
        
        // Create the wizard descriptor
        WizardDescriptor.Panel[] wizardPanels = new WizardDescriptor.Panel[] { addWizardPanel, configWizardPanel  };
        wizardDescriptor = new WizardDescriptor( wizardPanels );
        
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
    
    private boolean loadingEjbGroup() {
        // Make sure the user has entered all the required data
        StringBuffer errorMessage = new StringBuffer();
        if( !addPanel.validateData( errorMessage ) ) {
            NotifyDescriptor d = new NotifyDescriptor.Message( errorMessage.toString(), NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify( d );
            return false;
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
        if( !checkClientJarInfo( ejbGroup ) )
            return false;
        
        try {
            // Try to load the EjbGroup
            ejbLoader = new EjbLoader( ejbGroup );
            ejbLoader.load();
            
            // Good, the ejbs are loaded ok
            return true;
        }
        catch( EjbLoadException ex ) {

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
    
    private boolean checkClientJarInfo( EjbGroup grp ) {
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
            NotifyDescriptor d = new NotifyDescriptor.Message( msg, NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify( d );
            return false;
        }
        else
            return true;
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
    
    /**
     * The wizard panel for gathering the EJB information
     */
    private class AddEjbsWizardPanel implements WizardDescriptor.ValidatingPanel {
       
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
            
            // Throw WizardValidationException will cause the wizard to stay at the same step
            if( !loadingEjbGroup() )
                throw new org.openide.WizardValidationException( addPanel,  "not valid", "not valid" ); // TODO I18N
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
