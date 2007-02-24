/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

/*
 * ImportedProjectChildren.java
 *
 * Created on May 15, 2005, 7:03 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.netbeans.modules.uml.project.ui.nodes;

import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElementImport;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackageImport;
import org.netbeans.modules.uml.core.metamodel.profiles.IProfile;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeElement;
import org.netbeans.modules.uml.project.ProjectUtil;
import org.netbeans.modules.uml.project.UMLProjectHelper;
import java.util.Collections;
import java.util.HashMap;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.openide.filesystems.FileObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author Administrator
 */
public class ImportedProjectChildren extends Children.Keys //Children.Array
{
    private UMLProjectHelper mHelper = null;
    private HashMap < Object, AbstractNode > mNodeMap = new HashMap < Object, AbstractNode >();
    
    /** Creates a new instance of ImportedProjectChildren */
    public ImportedProjectChildren(UMLProjectHelper helper)
    {
        mHelper = helper;
    }
    
    public void addNewImportedElement(Project project, 
                                    IElement element, 
                                    IElementImport importedElement,
                                    boolean refresh)
    {   

        ITreeElement node = new ImportedElementNode(importedElement);
        
        // The call to getImportElement acutally retrieves the
        // cloned element that lives in the importing project, not the
        // element that is in the original project.
        String origID = element.getXMIID();
        IElement origOwner = element.getOwner();
        IElement orig = FactoryRetriever.instance().findElementByID(origOwner, origID);

        node.setElement(orig);
        if(element instanceof INamedElement)
        {
            INamedElement nElem = (INamedElement)element;
            node.setDisplayedName(nElem.getNameWithAlias());
        }
        addImportNode(project, refresh, node);
    }
    
    public void addNewImportedPackage(Project project, 
                                     IElement element, 
                                     IPackageImport importedElement,
                                     boolean refresh)
    {   
        ITreeElement node = new PackageImportNode(importedElement);
        
        // The call to getImportElement acutally retrieves the
        // cloned element that lives in the importing project, not the
        // element that is in the original project.
        String origID = element.getXMIID();
        IElement origOwner = element.getOwner();
        IElement orig = FactoryRetriever.instance().findElementByID(origOwner, origID);
        
        node.setElement(orig);
        if(element instanceof INamedElement)
        {
            INamedElement nElem = (INamedElement)element;
            node.setDisplayedName(nElem.getNameWithAlias());
        }
        addImportNode(project, refresh, node);
    }

    protected void addImportNode(final Project project, 
                                 final boolean refresh, 
                                 final ITreeElement node)
    {
        
        // For some reason I am getting the event more than one time.  So, I have
        // to make sure that the node is only added to the collection once.
        Node projectNode = getOwningProject(project);
        
        boolean foundOne = false;
        Children children = projectNode.getChildren();
        for(Node child : children.getNodes())
        {
            if(node.equals(child) == true)
            {
                foundOne = true;
                /*
                children.remove(new Node[] {child});
                children.add(new Node[] {(Node)node});
                if (refresh)
                    refreshKeys();
                 */
                break;
            }
        }
        
        if(foundOne == false)
        {
            Node[] nodes = { (Node)node };
            if(children != null)
            {
                children.add(nodes);
                if(refresh == true)
                {
                    refreshKeys();
                }
            }
        }
    }
    
    public void removeImportElement(Project project, IElement element) {
        Object projectDir = project.getProjectDirectory();
        
        IElement targetElement = element;
        if(element instanceof IElementImport)
        {
            IElementImport imported = (IElementImport)element;
            targetElement = imported.getImportedElement();
            
            Project foundProject = ProjectUtil.findElementOwner(targetElement);
            projectDir = foundProject.getProjectDirectory();
        }
        else if(element instanceof IPackageImport)
        {
            IPackageImport imported = (IPackageImport)element;
            targetElement = imported.getImportedPackage();

            Project foundProject = ProjectUtil.findElementOwner(targetElement);
            projectDir = foundProject.getProjectDirectory();
        }
        
        Node projectNode = mNodeMap.get(projectDir);
        if (projectNode == null)
            return;
        
        boolean nodeDeleted = false;
        Children children = projectNode.getChildren();
        Node[] nodes = children.getNodes();
        for(int x = 0; x < nodes.length; x++) {
            IElement elem = ((ITreeElement) nodes[x]).getElement();
            if (targetElement.equals(elem)) {
                children.remove(new Node[] {nodes[x]});
                nodeDeleted = true;
                break;
            }
        }
        
        if (nodeDeleted) {
            if (nodes.length == 1) {
                mNodeMap.remove(projectDir);
            }
            refreshKeys();
        }
    }
    
   /* retreives the imported projects from the project
    * @see org.openide.nodes.Children#addNotify()
    */
    protected void addNotify()
    {
        super.addNotify();
        
        NBNodeFactory factory = new NBNodeFactory();
        
        IProject project = mHelper.getProject();
//        ETList < INamespace > packages = project.getImportedPackages();
        ETList<IPackageImport> packages = project.getPackageImports();
        if(packages != null)
        {
            for(IPackageImport importPackage : packages)
            {
                INamespace ns = importPackage.getImportedPackage();
                
                // I do not want to show profiles under the imported project.
                // I want to have another node for profiles, that allows the
                // the user to apply profiles.
                if(!(ns instanceof IProfile))
                {
//                    Node projectNode = getOwningProject(ns);
//                    
//                    ITreeElement node = factory.createElementNode();
//                    node.setElement(ns);
//                    node.setDisplayedName(ns.getNameWithAlias());
//                    if(node instanceof Node)
//                    {
//                        //add(new Node[] {(Node)node});
//                        projectNode.getChildren().add(new Node[] {(Node)node});
//                    }
                    Project owningProject = ProjectUtil.findElementOwner(ns);
                    addNewImportedPackage(owningProject, ns, importPackage, false);
                }
            }
        }
        
//        ETList < IElement > elements = project.getImportedElements();
        ETList < IElementImport > elements = project.getElementImports();
        if(elements != null)
        {
            for(IElementImport importedElement : elements)
            {
                IElement elem = importedElement.getImportedElement();
                if (elem.isDeleted() || (elem.getOwner() == null)) {
                    continue;
                }
                Project owningProject = ProjectUtil.findElementOwner(elem);
                addNewImportedElement(owningProject, elem, importedElement, false);
            }
        }
        
        refreshKeys();
    }
    
    protected void removeNotify()
    {
        setKeys(Collections.EMPTY_SET);
    }
    
    protected Node[] createNodes(Object key)
    {
        Node[] retVal = { mNodeMap.get(key) };
        return retVal;
    }
    
    protected Node getOwningProject(IElement element)
    {   
        Project project = ProjectUtil.findElementOwner(element);
        return getOwningProject(project);
    }
    
    protected Node getOwningProject(Project project)
    {
        AbstractNode retVal = null;
        if(project != null)
        {
            FileObject object = project.getProjectDirectory();
            retVal = mNodeMap.get(object);
            if(retVal == null)
            {
                ProjectInformation info = (ProjectInformation)project.getLookup().lookup(ProjectInformation.class);
                
                retVal = new AbstractNode(new Children.SortedArray());
                retVal.setName(info.getDisplayName());
                retVal.setIconBaseWithExtension(
                    "org/netbeans/modules/uml/project/ui/resources/umlProject.gif");
                mNodeMap.put(object, retVal);
            }
        }
        return retVal;
    }
    
    protected void refreshKeys()
    {
        setKeys(mNodeMap.keySet());
    }
}
