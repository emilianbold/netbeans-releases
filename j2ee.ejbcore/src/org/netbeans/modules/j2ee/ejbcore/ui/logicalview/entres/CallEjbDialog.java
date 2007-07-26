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
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.j2ee.api.ejbjar.EjbReference;
import org.openide.DialogDescriptor;
import org.openide.loaders.DataObject;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.netbeans.modules.j2ee.ejbcore.Utils;
import org.netbeans.modules.j2ee.api.ejbjar.EnterpriseReferenceContainer;
import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.dd.api.ejb.MessageDriven;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.ejbcore._RetoucheUtil;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.FilterNode;

/**
 * Handling of CallEjbDialog used from CallEjbAction and
 * EjbReferenceTableModel from DDLoaders
 *
 * @author Martin Adamek
 */
public class CallEjbDialog {
    
    public boolean open(FileObject referencingFileObject, String referencingClassName, String title) throws IOException {
        Project enterpriseProject = FileOwnerQuery.getOwner(referencingFileObject);
        
        Project[] allProjects = Utils.getCallableEjbProjects(enterpriseProject);
        List<Node> ejbProjectNodes = new LinkedList<Node>();
        
        for (int i = 0; i < allProjects.length; i++) {
            Node projectView = new EjbsNode(allProjects[i]);
            ejbProjectNodes.add(new FilterNode(projectView, new EjbChildren(projectView)) {
                public Action[] getActions(boolean context) {
                    return new Action[0];
                }
            });
        }
        
        Children.Array children = new Children.Array();
        children.add(ejbProjectNodes.toArray(new Node[ejbProjectNodes.size()]));
        Node root = new AbstractNode(children);
        root.setDisplayName(NbBundle.getMessage(CallEjbDialog.class, "LBL_EJBModules"));
        EnterpriseReferenceContainer erc = (EnterpriseReferenceContainer)
        enterpriseProject.getLookup().lookup(EnterpriseReferenceContainer.class);
        boolean isJavaEE5orHigher = Utils.isJavaEE5orHigher(enterpriseProject);
        CallEjbPanel panel = new CallEjbPanel(referencingFileObject, root, isJavaEE5orHigher ? null : erc.getServiceLocatorName(), referencingClassName);
        if (isJavaEE5orHigher) {
            panel.disableServiceLocator();
        }
        
        final DialogDescriptor dialogDescriptor = new DialogDescriptor(
                panel,
                title,
                true,
                DialogDescriptor.OK_CANCEL_OPTION,
                DialogDescriptor.OK_OPTION,
                DialogDescriptor.DEFAULT_ALIGN,
                new HelpCtx(CallEjbPanel.class),
                null
                );
        
        panel.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(CallEjbPanel.IS_VALID)) {
                    Object newvalue = evt.getNewValue();
                    if ((newvalue != null) && (newvalue instanceof Boolean)) {
                        dialogDescriptor.setValid(((Boolean)newvalue).booleanValue());
                    }
                }
            }
        });
        
        panel.validateReferences();
        
        Object button = DialogDisplayer.getDefault().notify(dialogDescriptor);
        if (button != NotifyDescriptor.OK_OPTION) {
            return false;
        }
        Node ejbNode = panel.getEjb();
        boolean throwExceptions = !panel.convertToRuntime();
        EjbReference ref = ejbNode.getLookup().lookup(EjbReference.class);
        String referenceNameFromPanel = panel.getReferenceName();
        if (referenceNameFromPanel != null && referenceNameFromPanel.trim().equals("")) {
            referenceNameFromPanel = null;
        }
        DataObject dataObject = ejbNode.getCookie(DataObject.class);
        Project nodeProject = FileOwnerQuery.getOwner(dataObject.getPrimaryFile());
        
        boolean remoteInterfaceSelected = panel.isRemoteInterfaceSelected();
        
        Utils.addReference(referencingFileObject, referencingClassName, ref, panel.getServiceLocator(), 
                remoteInterfaceSelected, throwExceptions, referenceNameFromPanel, nodeProject);
        
        // generate the server-specific resources

        if (remoteInterfaceSelected) {
            String referencedEjbClassName = _RetoucheUtil.getJavaClassFromNode(ejbNode).getQualifiedName();
            String referencedEjbName = getEjbName(dataObject.getPrimaryFile(), referencedEjbClassName);

            J2eeModuleProvider j2eeModuleProvider = enterpriseProject.getLookup().lookup(J2eeModuleProvider.class);
            try {
                if (j2eeModuleProvider.getJ2eeModule().getModuleType().equals(J2eeModule.WAR)) {
                    j2eeModuleProvider.getConfigSupport().bindEjbReference(referenceNameFromPanel,referencedEjbName);
                } else if (j2eeModuleProvider.getJ2eeModule().getModuleType().equals(J2eeModule.EJB)) {
                    String ejbName = getEjbName(referencingFileObject, referencingClassName);
                    String ejbType = getEjbType(referencingFileObject, referencingClassName);
                    j2eeModuleProvider.getConfigSupport().bindEjbReferenceForEjb(
                            ejbName, ejbType, referenceNameFromPanel, referencedEjbName);
                }
            } catch (ConfigurationException ce) {
                Logger.getLogger("global").log(Level.WARNING, null, ce);
            }
        }

        return true;
    }

    private String getEjbName(FileObject fileObject,final String className) throws IOException {

        MetadataModel<EjbJarMetadata> metadataModel = org.netbeans.modules.j2ee.api.ejbjar.EjbJar.getEjbJar(fileObject).getMetadataModel();
        return metadataModel.runReadAction(new MetadataModelAction<EjbJarMetadata, String>() {
            public String run(EjbJarMetadata metadata) throws Exception {
                Ejb ejb = metadata.findByEjbClass(className);
                return ejb.getEjbName();
            }
        });

    }    

    private String getEjbType(FileObject fileObject, final String className) throws IOException {
        
        MetadataModel<EjbJarMetadata> metadataModel = org.netbeans.modules.j2ee.api.ejbjar.EjbJar.getEjbJar(fileObject).getMetadataModel();
        return metadataModel.runReadAction(new MetadataModelAction<EjbJarMetadata, String>() {
            public String run(EjbJarMetadata metadata) throws Exception {
                String result = null;
                Ejb ejb = metadata.findByEjbClass(className);
                if (ejb instanceof Session) {
                    result = EnterpriseBeans.SESSION;
                } else if (ejb instanceof Entity) {
                    result = EnterpriseBeans.ENTITY;
                } else if (ejb instanceof MessageDriven) {
                    result = EnterpriseBeans.MESSAGE_DRIVEN;
                }
                return result;
            }
        });
        
    }

    private class EjbsNode extends AbstractNode {
        public EjbsNode(Project project) {
            super(new EJBListViewChildren(project));
            setIconBaseWithExtension( "org/netbeans/modules/j2ee/ejbjarproject/ui/resources/ejbjarProjectIcon.gif" ); // NOI18N
            super.setName( ProjectUtils.getInformation( project ).getDisplayName() );
        }
    }
    
}
