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
            if( refresh || doModification( origCopy, ejbGroup ) )
            {
                // Sometihng has changed
                EjbDataModel.getInstance().touchModifiedFlag();
                
                // Regenerate the classes
                                
                EjbLoader ejbLoader = new EjbLoader( ejbGroup );
                
                try {
                    ejbLoader.createWrapperClientBeans();    
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
    
    private boolean doModification( EjbGroup origGroup, EjbGroup modGroup )
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
                                origParam.setName( modParam.getName() );
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
