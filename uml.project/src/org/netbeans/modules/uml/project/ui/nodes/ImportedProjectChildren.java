/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

import java.beans.PropertyChangeEvent;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElementImport;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackageImport;
import org.netbeans.modules.uml.core.metamodel.profiles.IProfile;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.project.UMLProjectHelper;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ExternalFileManager;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IDirectedRelationship;
import org.netbeans.modules.uml.core.metamodel.core.foundation.UMLXMLManip;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.project.ProjectUtil;
import org.netbeans.modules.uml.project.ui.cookies.ImportedElementCookie;
import org.netbeans.modules.uml.resources.images.ImageUtil;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeEvent;
import org.openide.nodes.NodeListener;
import org.openide.nodes.NodeMemberEvent;
import org.openide.nodes.NodeReorderEvent;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Administrator
 */
public class ImportedProjectChildren extends Children.Keys implements NodeListener
{

    private UMLProjectHelper mHelper = null;
    private HashMap<Object, AbstractNode> mNodeMap = new HashMap<Object, AbstractNode>();
    private HashMap<String, IProject> ownerMap = new HashMap<String, IProject>();

    public ImportedProjectChildren(UMLProjectHelper helper)
    {
        mHelper = helper;
    }

    public void addNewImportedElement(final IProject project,
            final IElement element,
            final IDirectedRelationship imported,
            final boolean refresh)
    {
        if (!(imported instanceof IElementImport) && !(imported instanceof IPackageImport))
        {
            return;
        }
        Project p = ProjectUtil.findNetBeansProjectForModel(project);
        if (SwingUtilities.isEventDispatchThread())
        {
            addImportNode(project, refresh, element, imported);
        } else
        {
            try
            {
                SwingUtilities.invokeLater(new Runnable()
                {

                    public void run()
                    {
                        addImportNode(project, refresh, element, imported);
                    }
                });
            } catch (Exception ex)
            {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    protected void addImportNode(IProject project,
            boolean refresh,
            IElement element,
            IElement elementImport)
    {
        // keep the logic for the moment to filter out duplicate elements, as UMLImportsUiSupport fires
        // multiple events for one import,
        // For some reason I am getting the event more than one time.  So, I have
        // to make sure that the node is only added to the collection once.
        Node projectNode = getOwningProject(project);

        boolean found = false;
        Children children = projectNode.getChildren();
        for (Node child : children.getNodes())
        {
            if (((ImportedElementNode) child).getElementXMIID().equals(element.getXMIID()))
            {
                found = true;
                break;
            }
        }
        if (!found)
        {
            Project p = ProjectUtil.findNetBeansProjectForModel(project);
            Node orig = ProjectUtil.findNodeInProjectTree(p, element);
            if (orig == null)
            {
                Logger.getLogger(ImportedProjectChildren.class.getName()).
                        log(Level.WARNING,
                        NbBundle.getMessage(ImportedProjectChildren.class,
                        "MSG_InvalidImportedElement", element.getElementType(),
                        element.toString()));
                return;
            }
            ImportedElementNode node = null;
            if (elementImport instanceof IElementImport)
            {
                node = new ImportedElementNode(mHelper.getProject(),
                        orig, (IElementImport) elementImport);
            } else if (elementImport instanceof IPackageImport)
            {
                node = new ImportedElementNode(mHelper.getProject(),
                        orig, (IPackageImport) elementImport);
            }
            Node[] newNodes =
            {
                (Node) node
            };

            if (children != null)
            {
                children.add(newNodes);
                refreshKeys();
            }
        }
    }

    public void removeImportElement(IProject project, IElement element)
    {
        IElement targetElement = element;
        if (element instanceof IElementImport)
        {
            IElementImport imported = (IElementImport) element;
            targetElement = imported.getImportedElement();
        } else if (element instanceof IPackageImport)
        {
            IPackageImport imported = (IPackageImport) element;
            targetElement = imported.getImportedPackage();
        }

        Node projectNode = mNodeMap.get(project);
        if (projectNode == null)
        {
            return;
        }
        removeNode(projectNode, targetElement);
    }

    private void removeNode(Node root, IElement targetElement)
    {
        Children children = root.getChildren();
        Node[] nodes = children.getNodes();
        for (int x = 0; x < nodes.length; x++)
        {
            ImportedElementCookie cookie = nodes[x].getCookie(ImportedElementCookie.class);
            if (cookie != null)
            {
                if (cookie.getElementXMIID().equals(targetElement.getXMIID()))
                {
                    try
                    {
                        ((ImportedElementNode) nodes[x]).destroy(false);
                        if (root.getChildren().getNodesCount() == 0)
                        {
                            IProject project = targetElement.getProject();
                            root.destroy();
                            mNodeMap.values().remove(root);
                            refreshKeys();
                            removeModelRootNodeRefreshListener(project);
                        }
                    } catch (Exception e)
                    {
                    }
                    return;
                }
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
        if (packages != null)
        {
            for (IPackageImport importPackage : packages)
            {
                INamespace ns = importPackage.getImportedPackage();

                // I do not want to show profiles under the imported project.
                // I want to have another node for profiles, that allows the
                // the user to apply profiles.
                if (!(ns instanceof IProfile))
                {
                    if (map.contains(ns.getXMIID()))
                    {
                        continue;
                    }
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

        ETList<IElementImport> elements = project.getElementImports();

        for (IElementImport importedElement : elements)
        {
            IElement elem = importedElement.getImportedElement();
            if (map.contains(elem.getXMIID()))
            {
                continue;
            }
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
    }

    protected IProject getOwningProjectOfImportedElement(IElement elem)
    {
        org.dom4j.Node node = null;
        IProject p = null;

        String ownerID = XMLManip.getAttributeValue(elem.getNode(), "owner");

        if (ownerMap.containsKey(ownerID))
        {
            return ownerMap.get(ownerID);
        }
        node = ExternalFileManager.getExternalNode(elem.getNode());

        INamespace sp = UMLXMLManip.getProject(node);
        if (sp != null)
        {
            p = sp.getProject();
        }
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
        if (project != null)
        {
            retVal = mNodeMap.get(project);
            if (retVal == null)
            {
                retVal = new AbstractNode(new Children.SortedArray());
                retVal.setName(project.getNameWithAlias());
                retVal.setIconBaseWithExtension(ImageUtil.IMAGE_FOLDER + "uml-project.png"); // NOI18N

                mNodeMap.put(project, retVal);
                addModelRootNodeRefreshListener(project);
            }
        }
        return retVal;
    }

    protected void refreshKeys()
    {
        setKeys(mNodeMap.keySet());
    }

    private void addModelRootNodeRefreshListener(IProject project)
    {
        for (Project p : ProjectUtil.getOpenUMLProjects())
        {
            UMLProjectHelper helper = p.getLookup().lookup(UMLProjectHelper.class);
            if (helper != null && helper.getProject() == project)
            {
                UMLPhysicalViewProvider provider = p.getLookup().lookup(UMLPhysicalViewProvider.class);
                if (provider != null)
                {
                    UMLModelRootNode node = provider.getModelRootNode();
                    node.addNodeListener(this);
                }
            }
        }
    }

    private void removeModelRootNodeRefreshListener(IProject project)
    {
        for (Project p : ProjectUtil.getOpenUMLProjects())
        {
            UMLProjectHelper helper = p.getLookup().lookup(UMLProjectHelper.class);
            if (helper != null && helper.getProject() == project)
            {
                UMLPhysicalViewProvider provider = p.getLookup().lookup(UMLPhysicalViewProvider.class);
                if (provider != null)
                {
                    UMLModelRootNode node = provider.getModelRootNode();
                    node.removeNodeListener(this);
                }
            }
        }
    }

    protected void refreshImportedElements()
    {
        setKeys(Collections.emptyList());
        mNodeMap.clear();
        ownerMap.clear();
        
        try
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    addNotify();
                }
            });
        } catch (Exception ex)
        {
            Exceptions.printStackTrace(ex);
        }
    }

    // capture source model node refresh event triggered by filtering function
    public void childrenAdded(NodeMemberEvent ev)
    {
    }

    public void childrenRemoved(NodeMemberEvent ev)
    {
    }

    public void childrenReordered(NodeReorderEvent ev)
    {
    }

    public void nodeDestroyed(NodeEvent ev)
    {
        refreshImportedElements();
    }

    public void propertyChange(PropertyChangeEvent evt)
    {
    }
}
