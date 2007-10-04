/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
 * ImportedProjectChildren.java
 *
 * Created on May 15, 2005, 7:03 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.netbeans.modules.uml.project.ui.nodes;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElementImport;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackageImport;
import org.netbeans.modules.uml.core.metamodel.profiles.IProfile;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeElement;
import org.netbeans.modules.uml.project.UMLProjectHelper;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ExternalFileManager;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IDirectedRelationship;
import org.netbeans.modules.uml.core.metamodel.core.foundation.UMLXMLManip;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.resources.images.ImageUtil;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author Administrator
 */
public class ImportedProjectChildren extends Children.Keys //Children.Array
{
    private UMLProjectHelper mHelper = null;
    private HashMap < Object, AbstractNode > mNodeMap = new HashMap < Object, AbstractNode >();
    private HashMap <String, IProject> ownerMap = new HashMap();
    
    
    /** Creates a new instance of ImportedProjectChildren */
    public ImportedProjectChildren(UMLProjectHelper helper)
    {
        mHelper = helper;
    }
    
    public void addNewImportedElement(IProject project,
            IElement element,
            IDirectedRelationship imported,
            boolean refresh)
    {
        ITreeElement node = null;
        if (imported instanceof IElementImport)
            node = new ImportedElementNode((IElementImport)imported);
        else if (imported instanceof IPackageImport)
            node = new PackageImportNode((IPackageImport)imported);
        else
            return;
        
        node.setElement(element);
        
        addImportNode(project, refresh, node);
    }
    
    
    protected void addImportNode(final IProject project,
            final boolean refresh,
            final ITreeElement node)
    {
        // keep the logic for the moment to filter out duplicate elements, as UMLImportsUiSupport fires
        // multiple events for one import, 
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
    
    public void removeImportElement(IProject project, IElement element)
    {
        IElement targetElement = element;
        if(element instanceof IElementImport)
        {
            IElementImport imported = (IElementImport)element;
            targetElement = imported.getImportedElement();
        }
        else if(element instanceof IPackageImport)
        {
            IPackageImport imported = (IPackageImport)element;
            targetElement = imported.getImportedPackage();
        }
        else
        {
            return;
        }
        
        Node projectNode = mNodeMap.get(project);
        if (projectNode == null)
            return;
        
        Children children = projectNode.getChildren();
        Node[] nodes = children.getNodes();
        for(int x = 0; x < nodes.length; x++)
        {
            IElement elem = ((ITreeElement) nodes[x]).getElement();
            if (targetElement.equals(elem))
            {
                children.remove(new Node[] {nodes[x]});
                if (children.getNodesCount()==0)
                    mNodeMap.remove(project);
                break;
            }
        }
    }
    
   /* retreives the imported projects from the project
    * @see org.openide.nodes.Children#addNotify()
    */
    protected void addNotify()
    {
        IProject project = mHelper.getProject();
        HashSet<String> map = new HashSet();
        
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
                    if (map.contains(ns.getXMIID()))
                        continue;
                    
                    map.add(ns.getXMIID());
                    
                    IProject p = getOwningProjectOfImportedElement(ns);
                    
                    if (p == null)
                    {
                        Logger.getLogger(ImportedProjectChildren.class.getName()).
                                log(Level.WARNING, 
                                NbBundle.getMessage(ImportedProjectChildren.class, 
                                "MSG_InvalidOwner", ns.getElementType(), ns.toString()));
                        continue;
                    }
                    addNewImportedElement(p, ns, importPackage, false);
                }
            }
        }
        
        ETList < IElementImport > elements = project.getElementImports();
        
        for(IElementImport importedElement : elements)
        {
            IElement elem = importedElement.getImportedElement();
            if (map.contains(elem.getXMIID()))
                continue;
            
            map.add(elem.getXMIID());
            IProject owner = getOwningProjectOfImportedElement(elem);
            if (owner == null)
            {
                Logger.getLogger(ImportedProjectChildren.class.getName()).
                        log(Level.WARNING, 
                        NbBundle.getMessage(ImportedProjectChildren.class, 
                        "MSG_InvalidOwner", elem.getElementType(), elem.toString()));
                continue;
            }
            
            addNewImportedElement(owner, elem, importedElement, false);
            
        }
        refreshKeys();
    }
    
    
    private IProject getOwningProjectOfImportedElement(IElement elem)
    {
        org.dom4j.Node node = null;
        IProject p = null;
        
        String ownerID = XMLManip.getAttributeValue(elem.getNode(), "owner");
        
        if (ownerMap.containsKey(ownerID))
            return ownerMap.get(ownerID);
        
        node = ExternalFileManager.getExternalNode(elem.getNode());
        
        INamespace sp = UMLXMLManip.getProject(node);
        if (sp!=null)
            p = sp.getProject();
        
        ownerMap.put(ownerID, p);
        return p;
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
    
    
    protected Node getOwningProject(IProject project)
    {
        AbstractNode retVal = null;
        if(project != null)
        {
            retVal = mNodeMap.get(project);
            if(retVal == null)
            {
                retVal = new AbstractNode(new Children.SortedArray());
                retVal.setName(project.getNameWithAlias());
                retVal.setIconBaseWithExtension(ImageUtil.IMAGE_FOLDER + "uml-project.png"); // NOI18N
                mNodeMap.put(project, retVal);
            }
        }
        return retVal;
    }
    
    protected void refreshKeys()
    {
        setKeys(mNodeMap.keySet());
    }
}
