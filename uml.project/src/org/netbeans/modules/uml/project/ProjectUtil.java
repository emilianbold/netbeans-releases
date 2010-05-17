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
 * ProjectUtil.java
 *
 * Created on May 17, 2005, 7:22 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.netbeans.modules.uml.project;

import java.beans.PropertyVetoException;
import java.text.MessageFormat;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import java.awt.Cursor;
import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import javax.swing.SwingUtilities;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.uml.common.RelationshipCookie;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.project.ui.UMLProjectSettings;
import org.netbeans.modules.uml.project.ui.customizer.UMLProjectProperties;
import org.netbeans.modules.uml.project.ui.nodes.AbstractModelElementNode;
import org.netbeans.modules.uml.project.ui.nodes.ModelRootNodeCookie;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.awt.StatusDisplayer;
import org.openide.explorer.ExplorerManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author Administrator
 */
public class ProjectUtil
{
    public static final Comparator PROJECT_BY_DISPLAYNAME = new ProjectByDisplayNameComparator();
    
    public static Project findElementOwner(IElement element)
    {
        Project retVal = null;
        if(element != null)
        {
            //IProject project = element.getProject();
            IElement owningElement = element.getOwner();
            if (owningElement != null)
            { //JM: Fix an NPE
                IProject project = owningElement.getProject();
                
                retVal = findNetBeansProjectForModel(project);
            }
            else
            {
                retVal = findNetBeansProjectForModel(element.getProject());
            }
        }
        
        return retVal;
    }
    
    public static Project findReferencingProject(IElement element)
    {
        Project retVal = null;
        if(element != null)
        {
            IProject project = element.getProject();
            
            retVal = findNetBeansProjectForModel(project);
        }
        
        return retVal;
    }
    
    public static Project findNetBeansProjectForModel(IProject project)
    {
        Project retVal = null;
        
        if(project != null)
        {
            String filename = project.getFileName();
            if((filename != null) && (filename.length() > 0))
            {
                FileObject fo = FileUtil.toFileObject(new File(filename));
                if (fo != null) {
                    retVal = FileOwnerQuery.getOwner(fo);
                }
            }
        }
        
        return retVal;
    }
    
    public static boolean findElementInProjectTree(IElement element)
    {
        Node selectedNode = findNodeInProjectTree(element);
        if (selectedNode != null)
        {
            selectNodeAsync(selectedNode);
            return true; 
        }
        return false;
    }
    
    
    public static Node findNodeInProjectTree(IElement element)
    {
        Project project = findReferencingProject(element);
        
        return findNodeInProjectTree(project, element);
    }
    
    
    public static Node findNodeInProjectTree(Project project, IElement element)
    {
        TopComponent tc = WindowManager.getDefault().findTopComponent( "projectTabLogical_tc" );
        if (tc==null)
            return null;
        
        ExplorerManager explorerManager =
                ((ExplorerManager.Provider)tc).getExplorerManager();
        Node root = explorerManager.getRootContext();
        Children c = root.getChildren();
        Node[] projectNodes = c.getNodes(true);
        for (int i=0; i<projectNodes.length; i++)
        {
            Project p = (Project) projectNodes[i].getLookup().lookup(Project.class);
            if (p==project)
            {
                Node selectedNode = findNodeQuick(projectNodes[i],  element);
                if (selectedNode == null)
                {
                    selectedNode = findNode(projectNodes[i],  element);
                }
                return selectedNode;
            }
        }
        return null;
    }
    
    
    public static AntProjectHelper getAntProjectHelper(UMLProject umlProject)
    {
        return (AntProjectHelper)((UMLProjectHelper)umlProject
                .getLookup().lookup(UMLProjectHelper.class)).getAntProjectHelper();
    }
    
    public static String getTargetJavaProjectName(UMLProject umlProject)
    {
        AntProjectHelper umlAntProjectHelper = getAntProjectHelper(umlProject);
        
        EditableProperties editableProperties =
                umlAntProjectHelper.getProperties(
                AntProjectHelper.PROJECT_PROPERTIES_PATH);
        
        return (String)editableProperties
                .getProperty(UMLProjectProperties.REFERENCED_JAVA_PROJECT);
    }
    
    
    private static String getTargetJavaProjectName(IProject iProject)
    {
        return ProjectUtil.getTargetJavaProjectName((UMLProject)ProjectUtil
                .findNetBeansProjectForModel(iProject));
    }
    
    
    public static String getTargetJavaProjectName(IElement element)
    {
        UMLProject umlProject =
                (UMLProject)ProjectUtil.findReferencingProject(element);
        
        return ProjectUtil.getTargetJavaProjectName(umlProject);
    }
    
    public static String getTargetJavaProjectName(Node node)
    {
        IElement element = (IElement)node.getCookie(IElement.class);
        if (element == null)
            return null;
        
        return ProjectUtil.getTargetJavaProjectName(element);
    }
    
    
    public static boolean hasTargetJavaProject(Node node)
    {
        String name = ProjectUtil.getTargetJavaProjectName(node);
        return  name == null || name.length() == 0 ? false : true;
    }
    
    public static boolean hasTargetJavaProject(IProject umlProject)
    {
        String name = ProjectUtil.getTargetJavaProjectName(umlProject);
        return  name == null || name.length() == 0 ? false : true;
    }
    
    
    public static Node findNode(Node root, IElement element)
    {
        
        if (root.isLeaf())
            return null;
        
        Children children = root.getChildren();
        
        Node[] nodes = children.getNodes(true);
        for (int j=0; j<nodes.length; j++)
        {
            IProjectTreeItem item = (IProjectTreeItem)nodes[j].
                    getCookie(IProjectTreeItem.class);
            if (item != null)
            {
                IElement modelElement = item.getModelElement();
                if (modelElement==null) // could be a diagram node
                {
                    if (element instanceof IDiagram)
                    {
                        // the unique diagram file name is used to determine
                        // if two diagram objects are same
                        if (item.getDiagram()!=null && item.getDescription().
                                equals(((IDiagram)element).getFilename()))
                            return nodes[j];
                    }
                }
                if (modelElement!=null &&
                        element.getXMIID().equals(modelElement.getXMIID()))
                {
                    if (nodes[j].getCookie(RelationshipCookie.class) != null)
                        continue;
                    return nodes[j];
                }
            }
        }
        for (int j=0; j<nodes.length; j++)
        {
            if (nodes[j].isLeaf())
                continue;
            Node val = findNode(nodes[j],  element);
            if (val!=null)
            {
                return val;
            }
        }
        return null;
    }
    
    
    private static Node findNodeQuick(Node root, IElement element)
    {
        if (root.isLeaf())
            return null;
        
        if (element == null)
        {
            return null;
        }
        
        IElement owner = element.getOwner();
        if (owner == null)
        {
            return findNode(root, element);
        }
        else
        {
            Node ownerNode = findNodeQuick(root, owner);
            if (ownerNode != null)
            {
                return findNode(ownerNode, element);
            }
        }
        return null;
    }
    
    
    public static Project[] getSelectedProjects(Class projectClass)
    {
        Set result = new HashSet();
        Lookup lruLookupLocal = Utilities.actionsGlobalContext();
        // First find out whether there is a project directly in the Lookup
        Collection currentProjects = lruLookupLocal.lookup( new Lookup.Template( projectClass ) ).allInstances();
        
        for( Iterator it = currentProjects.iterator(); it.hasNext(); )
        {
            Project p = (Project)it.next();
            result.add(p);
        }
        
        // Now try to guess the project from dataobjects
        Collection currentDataObjects = lruLookupLocal.lookup( new Lookup.Template( DataObject.class ) ).allInstances();
        for( Iterator it = currentDataObjects.iterator(); it.hasNext(); )
        {
            
            DataObject dObj = (DataObject)it.next();
            FileObject fObj = dObj.getPrimaryFile();
            if (fObj != null) 
            {
                Project p = FileOwnerQuery.getOwner(fObj);
                if ( p != null )
                {
                    result.add( p );
                }
            }
        }
        Project[] projects = new Project[ result.size() ];
        result.toArray( projects );
        Arrays.sort(projects, PROJECT_BY_DISPLAYNAME);
        return projects;
    }
    
    
    public static Project[] getOpenUMLProjects()
    {
        Project[] projects = OpenProjects.getDefault().getOpenProjects();
        Set result = new HashSet();
        for (Project p: projects)
        {
            if (p.getLookup().lookup(UMLProjectHelper.class)!=null)
                result.add(p);
        }
        
        Project[] umlprojects = new Project[ result.size() ];
        result.toArray( umlprojects );
        Arrays.sort(umlprojects, PROJECT_BY_DISPLAYNAME);
        return umlprojects;
    }
    
    
    public static Project[] getOpenJavaProjects()
    {
        List result = new ArrayList();
        Project openProjects[] = OpenProjects.getDefault().getOpenProjects();
        for (Project p: openProjects)
        {
            Sources srcs = (Sources)p.getLookup().lookup(Sources.class);
            
            if (srcs != null)
            {
                // now check for Java sources
                SourceGroup[] javaSrcGrps =
                        srcs.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
                if (javaSrcGrps.length > 0)
                {
                    ClassPathProvider cpProvider = (ClassPathProvider)
                            p.getLookup().lookup(ClassPathProvider.class);
                    if (cpProvider!=null)
                        result.add(p);
                }
            }
        }
        Project[] projects = (Project[])result.toArray(new Project[result.size()]);
        Arrays.sort(projects, PROJECT_BY_DISPLAYNAME);
        return projects;
    }
    
    public final static String DEFAULT_PACKAGE_DISPLAY_NAME =
            "<default package>"; // NOI18N
    
    
    public static void selectInModel(List<Project> projects, DataObject obj)
    {
        String resourceName = "";
        String className = "";
        String packageName = DEFAULT_PACKAGE_DISPLAY_NAME;
        boolean isPackage = obj instanceof DataFolder;
        
        if (obj==null)
            return;
        
        FileObject fObj = obj.getPrimaryFile();
        ClassPath path = ClassPath.getClassPath(fObj, ClassPath.SOURCE);
        if (path!=null)
            resourceName = path.getResourceName(fObj);
        
        if (resourceName.indexOf(".")!=-1) // it's a class, otherwise package
        {
            resourceName = resourceName.substring(0, resourceName.indexOf("."));
            className = resourceName.substring(resourceName.lastIndexOf("/") + 1);
            
            // cvc - CR 6409539
            // if class is in default package, the packageName has no slashes
            // and therefore, the substring call breaks
            int lastSlashIndex = resourceName.lastIndexOf("/");
            if (lastSlashIndex > -1)
                packageName = resourceName.substring(0, lastSlashIndex);
        }
        
        else
        {
            packageName = resourceName;
        }
        
        TopComponent tc = WindowManager.getDefault()
                .findTopComponent("projectTabLogical_tc");
        
        if (tc==null)
            return ;
        
        ExplorerManager explorerManager =
                ((ExplorerManager.Provider)tc).getExplorerManager();
        
        Node root = explorerManager.getRootContext();
        Children c = root.getChildren();
        Node[] projectNodes = c.getNodes(true);
        
        for (int i=0; i<projectNodes.length; i++)
        {
            Project p = (Project) projectNodes[i]
                    .getLookup().lookup(Project.class);
            
            Node selected = null;
            
            if (projects.contains(p))
            {
                Node selectedNode = null;
                
                StringTokenizer st = new StringTokenizer(packageName, "/");
                if (st.hasMoreTokens())
                {
                    String token = st.nextToken();
                    selectedNode = findNodeByName(projectNodes[i],  token, true );
                    
                    while (selectedNode!=null && st.hasMoreTokens())
                    {
                        selectedNode = findNodeByName(selectedNode,  st.nextToken(), true);
                    }
                }
                
                if (!isPackage && selectedNode!=null)
                {
                    selectedNode = findNodeByName(selectedNode, className, false);
                }
                
                final Node node = selectedNode;
                if (node != null) {
                    selectNodeAsync(node);
                    return;
                }
            }
        }
        return;
    }
    
    
    public static Node findNodeByName(Node root, String name, boolean isPackage)
    {
        if (root == null || root.isLeaf())
            return null;
        
        Children children = root.getChildren();
        Node[] nodes = children.getNodes(true);
        
        for (int j=0; j<nodes.length; j++)
        {
            Node curNode = nodes[j];
            
            if (curNode.getCookie(ModelRootNodeCookie.class) != null)
            {
                // cvc - CR 6409539
                // if the class source is in the default package, the
                // findNodeByName doesn't know that Model root node
                // is the default package
                if (name.equals(DEFAULT_PACKAGE_DISPLAY_NAME))
                    return curNode;
                
                else
                    return findNodeByName(curNode, name, isPackage);
            }
            
            IElement element = (IElement)curNode.getCookie(IElement.class);
            
            if (element == null)
                continue;
            
            String type = element.getElementType();
            
            if (isPackage)
            {
                if (curNode.getName().equals(name) &&
                        type.equals(AbstractModelElementNode.ELEMENT_TYPE_PACKAGE))
                {
                    return curNode;
                }
            }
            
            else
            {
                if (curNode.getName().equals(name) &&
                        (type.equals(AbstractModelElementNode.ELEMENT_TYPE_CLASS)) ||
                        (type.equals(AbstractModelElementNode.ELEMENT_TYPE_INTERFACE)))
                {
                    return nodes[j];
                }
            }
        }
        
        return null;
    }
    
    private static final RequestProcessor RP = new RequestProcessor();
    
    public static void selectNodeAsync(final Node selectedNode)
    {
        
        final TopComponent tc = WindowManager.getDefault().findTopComponent( "projectTabLogical_tc" );
        if (tc==null)
            return ;
        
        final ExplorerManager manager =
                ((ExplorerManager.Provider)tc).getExplorerManager();
        tc.setCursor( Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) );
        tc.open();
        tc.requestActive();
        
        // Do it in different thread than AWT
        RP.post( new Runnable()
        {
            public void run()
            {
                
                // Back to AWT
                SwingUtilities.invokeLater( new Runnable()
                {
                    public void run()
                    {
                        if ( selectedNode != null )
                        {
                            try
                            {
                                manager.setSelectedNodes( new Node[] { selectedNode } );
                                StatusDisplayer.getDefault().setStatusText( "" ); // NOI18N
                            }
                            catch ( PropertyVetoException e )
                            {
                                // Bad day node found but can't be selected
                            }
                        }
                        else
                        {
                            StatusDisplayer.getDefault().setStatusText(
                                    NbBundle.getMessage( ProjectUtil.class,  "MSG_NodeNotFound" ));
                        }
                        tc.setCursor( null );
                    }
                } );
            }
        } );
        
    }
    
    public static String createUniqueProjectName(
            File location, String baseName, boolean tryNoIndexFirst)
    {
        String projectName = null;
        
        if (baseName == null || baseName.length() == 0)
        {
            baseName = NbBundle.getMessage(
                    ProjectUtil.class, "TXT_UMLProject"); // NOI18N
        }
        
        int baseCount =
                UMLProjectSettings.getDefault().getNewProjectCount() + 1;
        
        if (tryNoIndexFirst)
            projectName = validFreeProjectName(location, baseName, -1);
        
        while (projectName == null)
        {
            projectName = validFreeProjectName(
                    location, baseName+"{0}", baseCount);
            
            baseCount++;
        }
        
        return projectName;
    }
    
    private static String validFreeProjectName(
            final File parentFolder,
            final String formatter,
            final int index)
    {
        String name = "";
        
        if (index == -1)
            name = formatter;
        
        else
        {
            name = MessageFormat.format(
                    formatter, new Object[]{new Integer(index)});
        }
        
        File file = new File(parentFolder, name);
        return file.exists() ? null : name;
    }
    
    public static IProject getOwningProjectOfImportedElement(IElement imported)
    {
        IElement owner = imported.getOwner();
        if (!(owner instanceof IProject))
            return getOwningProjectOfImportedElement(owner);
        return (IProject)owner;
    }
    
    public static class ProjectByDisplayNameComparator implements Comparator
    {
        
        private static Comparator COLLATOR = Collator.getInstance();
        
        public int compare(Object o1, Object o2)
        {
            
            if ( !( o1 instanceof Project ) )
            {
                return 1;
            }
            if ( !( o2 instanceof Project ) )
            {
                return -1;
            }
            
            Project p1 = (Project)o1;
            Project p2 = (Project)o2;
            
            return COLLATOR.compare(ProjectUtils.getInformation(p1).getDisplayName(), ProjectUtils.getInformation(p2).getDisplayName());
        }
    }
}
