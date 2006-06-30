/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.entres;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import javax.swing.Action;
import javax.swing.JButton;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
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
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
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
            JavaClass beanClass = JMIUtils.getJavaClassFromNode(nodes[0]);
            FileObject srcFile = JavaModel.getFileObject(beanClass.getResource());
            Project enterpriseProject = FileOwnerQuery.getOwner(srcFile);
            EnterpriseReferenceContainer erc = (EnterpriseReferenceContainer)
                enterpriseProject.getLookup().lookup(EnterpriseReferenceContainer.class);
        
            MessageDestinationPanel panel = 
                    new MessageDestinationPanel(okButton, erc.getServiceLocatorName());
            
            final DialogDescriptor nd = new DialogDescriptor(
                    panel,
                    NbBundle.getMessage(SendJMSMessageAction.class,"LBL_SelectMessageDestination"),
                    true,
                    DialogDescriptor.OK_CANCEL_OPTION,
                    DialogDescriptor.OK_OPTION,
                    DialogDescriptor.DEFAULT_ALIGN,
                    new HelpCtx(MessageDestinationPanel.class),
                    null
                    );
            
            panel.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    if (evt.getPropertyName().equals(MessageDestinationPanel.IS_VALID)) {
                        Object newvalue = evt.getNewValue();
                        if ((newvalue != null) && (newvalue instanceof Boolean)) {
                            nd.setValid(((Boolean)newvalue).booleanValue());
                        }
                    }
                }
            });
            panel.checkDestination();

            Object button = DialogDisplayer.getDefault().notify(nd);
            if (button != DialogDescriptor.OK_OPTION) {
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
        if (nodes == null || nodes.length != 1) {
            return false;
        }
	JavaClass jc = JMIUtils.getJavaClassFromNode(nodes[0]);
        if (jc == null) {
            return false;
        }
        FileObject srcFile = JavaModel.getFileObject(jc.getResource());
        Project project = FileOwnerQuery.getOwner(srcFile);
        J2eeModuleProvider j2eeModuleProvider = (J2eeModuleProvider) project.getLookup ().lookup (J2eeModuleProvider.class);
	String serverInstanceId = j2eeModuleProvider.getServerInstanceID();
	if (serverInstanceId == null) {
	    return true;
	}
	J2eePlatform platform = Deployment.getDefault().getJ2eePlatform(serverInstanceId);
	if (platform == null) {
	    return true;
	}
	if (!platform.getSupportedModuleTypes().contains(J2eeModule.EJB)) {
	    return false;
	}
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

    public Action createContextAwareInstance(Lookup actionContext) {
        boolean enable = enable((Node[])actionContext.lookup(new Lookup.Template (Node.class)).allInstances().toArray(new Node[0]));
        return enable ? this : null;
    }
    
}
