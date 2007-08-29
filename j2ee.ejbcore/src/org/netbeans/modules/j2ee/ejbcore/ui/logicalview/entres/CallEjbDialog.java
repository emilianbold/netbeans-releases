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
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.netbeans.modules.j2ee.ejbcore.Utils;
import org.netbeans.modules.j2ee.api.ejbjar.EnterpriseReferenceContainer;
import org.netbeans.modules.j2ee.ejbcore._RetoucheUtil;
import org.netbeans.modules.j2ee.ejbcore.action.CallEjbGenerator;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.FilterNode;

/**
 * Handling of Call EJB dialog; used from CallEjbAction
 *
 * @author Martin Adamek
 */
public class CallEjbDialog {
    
    public boolean open(FileObject referencingFO, String referencingClassName, String title) throws IOException {
        Project enterpriseProject = FileOwnerQuery.getOwner(referencingFO);
        
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
        EnterpriseReferenceContainer erc = enterpriseProject.getLookup().lookup(EnterpriseReferenceContainer.class);
        boolean isJavaEE5orHigher = Utils.isJavaEE5orHigher(enterpriseProject);
        CallEjbPanel panel = new CallEjbPanel(referencingFO, root, isJavaEE5orHigher ? null : erc.getServiceLocatorName(), referencingClassName);
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
        FileObject fileObject = ejbNode.getLookup().lookup(FileObject.class);
        Project nodeProject = FileOwnerQuery.getOwner(fileObject);
        
        boolean remoteInterfaceSelected = panel.isRemoteInterfaceSelected();
        boolean isDefaultRefName = panel.isDefaultRefName();
        String referencedClassName = _RetoucheUtil.getJavaClassFromNode(ejbNode).getQualifiedName();

        CallEjbGenerator generator = CallEjbGenerator.create(ref, referenceNameFromPanel, isDefaultRefName);
        generator.addReference(
                referencingFO, 
                referencingClassName, 
                fileObject, 
                referencedClassName, 
                panel.getServiceLocator(), 
                remoteInterfaceSelected, 
                throwExceptions, 
                nodeProject
                );
        
        return true;
    }

    private class EjbsNode extends AbstractNode {
        public EjbsNode(Project project) {
            super(new EJBListViewChildren(project));
            setIconBaseWithExtension( "org/netbeans/modules/j2ee/ejbjarproject/ui/resources/ejbjarProjectIcon.gif" ); // NOI18N
            super.setName( ProjectUtils.getInformation( project ).getDisplayName() );
        }
    }
    
}
