/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.entres;

import java.io.IOException;
import javax.swing.JButton;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.jmi.javamodel.Feature;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.modules.j2ee.common.JMIUtils;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.javacore.api.JavaModel;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.netbeans.modules.j2ee.api.ejbjar.EnterpriseReferenceContainer;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.actions.NodeAction;


/**
 * Provide action for sending a JMS Message
 * @author Chris Webster
 * @author Martin Adamek
 */
public class SendJMSMessageAction extends NodeAction {
    
    protected void performAction(Node[] nodes) {
       try {           
            JButton okButton = new JButton();
            okButton.setText(NbBundle.getMessage(SendJMSMessageAction.class, "LBL_Ok"));
            okButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SendJMSMessageAction.class, "ACSD_Ok"));
            JButton cancelButton = new JButton();
            cancelButton.setText(NbBundle.getMessage(SendJMSMessageAction.class, "LBL_Cancel"));
            cancelButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SendJMSMessageAction.class, "ACSD_Cancel"));
            Feature feature = (Feature) nodes[0].getLookup().lookup(Feature.class);
            JavaClass beanClass = JMIUtils.getDeclaringClass(feature);
            FileObject srcFile = JavaModel.getFileObject(beanClass.getResource());
            Project enterpriseProject = FileOwnerQuery.getOwner(srcFile);
            EnterpriseReferenceContainer erc = (EnterpriseReferenceContainer)
                enterpriseProject.getLookup().lookup(EnterpriseReferenceContainer.class);
        
            MessageDestinationPanel panel = 
                    new MessageDestinationPanel(okButton, erc.getServiceLocatorName());
            
            NotifyDescriptor nd = new NotifyDescriptor(panel, 
                        NbBundle.getMessage(SendJMSMessageAction.class,"LBL_SelectMessageDestination"), 
                        NotifyDescriptor.DEFAULT_OPTION, NotifyDescriptor.DEFAULT_OPTION, 
                        new Object[] {okButton, cancelButton}, null);
            
            Object button = DialogDisplayer.getDefault().notify(nd);
            if (button != okButton) {
                return;
            }
            
            JMSDestination destination = panel.getSelectedDestination();
            String serviceLocator = panel.getServiceLocator();
            ServiceLocatorStrategy serviceLocatorStrategy = null;
            if (serviceLocator != null) {
                serviceLocatorStrategy = 
                        ServiceLocatorStrategy.create(enterpriseProject, srcFile, 
                                                      serviceLocator);
            }
            destination.genMethods(erc, beanClass.getName(), 
                                   enterpriseProject, beanClass, 
                                   serviceLocatorStrategy);
            if (serviceLocator != null) {
                erc.setServiceLocatorName(serviceLocator);
            }
        } 
        catch (IOException ioe) {
            NotifyDescriptor nd = new NotifyDescriptor.Message(ioe.getMessage(),
            NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
        } 
    }
    
    public String getName() {
        return NbBundle.getMessage(CallEjbAction.class, "LBL_SendJMSMessageAction");
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
        // If you will provide context help then use:
        // return new HelpCtx(CallEjbAction.class);
    }
    
    protected boolean asynchronous() {
        return false;
    }
    
    protected boolean enable(Node[] nodes) {
        if (nodes.length != 1) {
            return false;
        }
	JavaClass jc = JMIUtils.getJavaClassFromNode(nodes[0]);
        FileObject srcFile = JavaModel.getFileObject(jc.getResource());
        Project project = FileOwnerQuery.getOwner(srcFile);
        J2eeModuleProvider j2eeModuleProvider = (J2eeModuleProvider) project.getLookup ().lookup (J2eeModuleProvider.class);
        String j2eeVersion = j2eeModuleProvider.getJ2eeModule().getModuleVersion();
        Object moduleType = j2eeModuleProvider.getJ2eeModule().getModuleType();
        if ((J2eeModule.WAR.equals(moduleType) && !WebApp.VERSION_2_4.equals(j2eeVersion)) || 
           (J2eeModule.EJB.equals(moduleType) && !EjbJar.VERSION_2_1.equals(j2eeVersion)))  {
            return false;
        }
        return !jc.isInterface();
    }
    
    /** Perform extra initialization of this action's singleton.
     * PLEASE do not use constructors for this purpose!
     * protected void initialize() {
     * super.initialize();
     * putProperty(Action.SHORT_DESCRIPTION, NbBundle.getMessage(CallEjbAction.class, "HINT_Action"));
     * }
     */
}
