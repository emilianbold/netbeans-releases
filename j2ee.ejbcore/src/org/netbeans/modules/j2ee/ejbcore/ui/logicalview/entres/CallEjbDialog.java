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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.swing.Action;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.jmi.javamodel.JavaModelPackage;
import org.netbeans.jmi.javamodel.Method;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.dnd.EjbReference;
import org.netbeans.modules.javacore.api.JavaModel;
import org.netbeans.modules.javacore.internalapi.JavaMetamodel;
import org.openide.loaders.DataObject;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.netbeans.modules.j2ee.ejbcore.Utils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.j2ee.dd.api.common.EjbLocalRef;
import org.netbeans.modules.j2ee.dd.api.common.EjbRef;
import org.netbeans.modules.j2ee.api.ejbjar.EnterpriseReferenceContainer;
import org.netbeans.modules.j2ee.dd.api.ejb.DDProvider;
import org.netbeans.spi.project.ui.LogicalViewProvider;
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
    

    public boolean open(JavaClass beanClass, String title) {
        Project enterpriseProject = FileOwnerQuery.getOwner(JavaModel.getFileObject(beanClass.getResource()));
        
        Project[] allProjects = Utils.getCallableEjbProjects(enterpriseProject);
        List ejbProjectNodes = new LinkedList();
        
        for (int i = 0; i < allProjects.length; i++) {
            LogicalViewProvider lvp =
                    (LogicalViewProvider) allProjects[i].getLookup().lookup(LogicalViewProvider.class);
            Node projectView = lvp.createLogicalView();
            ejbProjectNodes.add(new FilterNode(projectView, new EjbChildren(projectView)) {
                public Action[] getActions(boolean context) {
                    return new Action[0];
                }
            });
        }
        
        Children.Array children = new Children.Array();
        children.add((Node[])ejbProjectNodes.toArray(new Node[ejbProjectNodes.size()]));
        Node root = new AbstractNode(children);
        String ejbSelector = NbBundle.getMessage(CallEjbDialog.class, "LBL_EJBSelectorTitle");
        root.setDisplayName(NbBundle.getMessage(CallEjbDialog.class, "LBL_EJBModules"));
        EnterpriseReferenceContainer erc = (EnterpriseReferenceContainer)
        enterpriseProject.getLookup().lookup(EnterpriseReferenceContainer.class);
        CallEjbPanel panel = new CallEjbPanel(root, erc.getServiceLocatorName(), beanClass);
        
        final NotifyDescriptor nd = new NotifyDescriptor(panel, title,
                NotifyDescriptor.OK_CANCEL_OPTION,
                NotifyDescriptor.PLAIN_MESSAGE,
                null, null
                );
        
        panel.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(CallEjbPanel.IS_VALID)) {
                    Object newvalue = evt.getNewValue();
                    if ((newvalue != null) && (newvalue instanceof Boolean)) {
                        nd.setValid(((Boolean)newvalue).booleanValue());
                    }
                }
            }
        });
        
        panel.validateReferences();
        
        Object button = DialogDisplayer.getDefault().notify(nd);
        if (button != NotifyDescriptor.OK_OPTION) {
            return false;
        }
        Node ejbNode = panel.getEjb();
        boolean throwExceptions = !panel.convertToRuntime();
        EjbReference ref = (EjbReference) ejbNode.getCookie(org.netbeans.modules.j2ee.api.ejbjar.EjbReference.class);
        String referenceNameFromPanel = panel.getReferenceName();
        if (referenceNameFromPanel != null && referenceNameFromPanel.trim().equals("")) {
            referenceNameFromPanel = null;
        }
        Utils.addReference(beanClass, ref, panel.getServiceLocator(), panel.isRemoteInterfaceSelected(), throwExceptions, referenceNameFromPanel);
        return true;
    }
    
}
