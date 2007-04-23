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
import org.netbeans.modules.j2ee.dd.api.ejb.DDProvider;
import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.ejbcore._RetoucheUtil;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
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
    
    public boolean open(FileObject fileObject, String className, String title) throws IOException {
        Project enterpriseProject = FileOwnerQuery.getOwner(fileObject);
        
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
        CallEjbPanel panel = new CallEjbPanel(fileObject, root, isJavaEE5orHigher ? null : erc.getServiceLocatorName(), className);
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
        EjbReference ref = (EjbReference) ejbNode.getCookie(EjbReference.class);
        String referenceNameFromPanel = panel.getReferenceName();
        if (referenceNameFromPanel != null && referenceNameFromPanel.trim().equals("")) {
            referenceNameFromPanel = null;
        }
        DataObject dataObject = (DataObject) ejbNode.getCookie(DataObject.class);
        Project nodeProject = FileOwnerQuery.getOwner(dataObject.getPrimaryFile());
        
        Utils.addReference(fileObject, className, ref, panel.getServiceLocator(), 
                panel.isRemoteInterfaceSelected(), throwExceptions, 
                referenceNameFromPanel, nodeProject);
        
        // generate the server-specific resources

        String referencedEjbClassName = _RetoucheUtil.getJavaClassFromNode(ejbNode).getQualifiedName();
        String referencedEjbName = getEjbName(dataObject.getPrimaryFile(), referencedEjbClassName);

        J2eeModuleProvider j2eeModuleProvider = enterpriseProject.getLookup().lookup(J2eeModuleProvider.class);
        try {
            if (j2eeModuleProvider.getJ2eeModule().getModuleType().equals(J2eeModule.WAR)) {
                j2eeModuleProvider.getConfigSupport().bindEjbReference(referenceNameFromPanel,referencedEjbName);
            }
            else
            if (j2eeModuleProvider.getJ2eeModule().getModuleType().equals(J2eeModule.EJB)) {
                String ejbName = getEjbName(fileObject, className);
                String ejbType = getEjbType(fileObject, className);
                j2eeModuleProvider.getConfigSupport().bindEjbReferenceForEjb(
                        ejbName, ejbType, referenceNameFromPanel, referencedEjbName);
            }
        }
        catch (ConfigurationException ce) {
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ce);
        }

        return true;
    }

    private String getEjbName(FileObject fileObject, String className) {

        EjbJar dd = null;
        try {
            dd = findDDRoot(fileObject);
        }
        catch (IOException ioe) {
            // TODO
        }
        if (dd == null) {
            return null;
        }

        EnterpriseBeans beans = dd.getEnterpriseBeans();
        if (beans == null) {
            return null;
        }

        if (className == null) {
            return null;
        }

        return getEjbName(beans, className);
    }    

    private String getEjbType(FileObject fileObject, String className) {

        EjbJar dd = null;
        try {
            dd = findDDRoot(fileObject);
        }
        catch (IOException ioe) {
            // TODO
        }
        if (dd == null) {
            return null;
        }

        EnterpriseBeans beans = dd.getEnterpriseBeans();
        if (beans == null) {
            return null;
        }

        if (className == null) {
            return null;
        }

        return getEjbType(beans, className);
    }

    private EjbJar findDDRoot(FileObject fileObject) throws IOException {
        org.netbeans.modules.j2ee.api.ejbjar.EjbJar ejbJar = org.netbeans.modules.j2ee.api.ejbjar.EjbJar.getEjbJar(fileObject);
        assert ejbJar != null;
        return DDProvider.getDefault().getMergedDDRoot(ejbJar.getMetadataUnit());
    }

    private String getEjbName(EnterpriseBeans beans, String className) {
        Ejb ejb = (Ejb) beans.findBeanByName(EnterpriseBeans.SESSION, Ejb.EJB_CLASS, className);
        if (ejb == null) {
            ejb = (Ejb) beans.findBeanByName(EnterpriseBeans.ENTITY, Ejb.EJB_CLASS, className);
        }
        if (ejb == null) {
            ejb = (Ejb) beans.findBeanByName(EnterpriseBeans.MESSAGE_DRIVEN, Ejb.EJB_CLASS, className);
        }

        return ejb.getEjbName();
    }

    private String getEjbType(EnterpriseBeans beans, String className) {
        String type = null;

        if (beans.findBeanByName(EnterpriseBeans.SESSION, Ejb.EJB_CLASS, className) != null) {
            type = EnterpriseBeans.SESSION;
        }
        else
        if (beans.findBeanByName(EnterpriseBeans.ENTITY, Ejb.EJB_CLASS, className) != null) {
            type = EnterpriseBeans.ENTITY;
        }
        else
        if (beans.findBeanByName(EnterpriseBeans.MESSAGE_DRIVEN, Ejb.EJB_CLASS, className) != null) {
            type = EnterpriseBeans.MESSAGE_DRIVEN;
        }

        return type;
    }

    private class EjbsNode extends AbstractNode {
        public EjbsNode(Project project) {
            super(new EJBListViewChildren(project));
            setIconBaseWithExtension( "org/netbeans/modules/j2ee/ejbjarproject/ui/resources/ejbjarProjectIcon.gif" ); // NOI18N
            super.setName( ProjectUtils.getInformation( project ).getDisplayName() );
        }
    }
    
}
