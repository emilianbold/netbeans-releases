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

package org.netbeans.modules.uml.project.ui.nodes;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.ui.support.DispatchHelper;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeDiagram;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem;
import org.netbeans.modules.uml.project.ui.NetBeansUMLProjectTreeModel;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.filechooser.FileFilter;
import org.netbeans.modules.uml.project.UMLProject;
import org.netbeans.modules.uml.project.UMLProjectHelper;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.modules.uml.project.ui.nodes.actions.NewDiagramType;
import java.awt.datatransfer.Transferable;
import java.util.Enumeration;
import org.netbeans.modules.uml.resources.images.ImageUtil;
import org.netbeans.modules.uml.ui.support.diagramsupport.IDrawingAreaEventsSink;
import org.openide.loaders.DataObject;
import org.openide.util.datatransfer.NewType;
import org.openide.util.datatransfer.PasteType;



/**
 * UMLDiagramsRootNode displays the UML diagrams
 * @author Mike Frisino
 */
// TODO: meteora
public final class UMLDiagramsRootNode extends UMLModelElementNode
    implements IDrawingAreaEventsSink
{
    
    private static final String ICON =
        ImageUtil.IMAGE_FOLDER + "diagrams-root-node.png"; //NOI18N
    
    static final RequestProcessor rp = new RequestProcessor();
    private static Icon folderIconCache;
    private static Icon openedFolderIconCache;
    
    private final String displayName;
    private UMLProjectHelper helper = null;
    
    /**
     * Creates new LibrariesNode named displayName displaying classPathProperty classpath
     * and optionaly Java platform.
     * @param displayName the display name of the node
     * @param eval {@link PropertyEvaluator} used for listening
     * @param helper {@link UpdateHelper} used for reading and updating project's metadata
     * @param refHelper {@link ReferenceHelper} used for destroying unused references
     * @param classPathProperty the ant property name of classpath which should be visualized
     * @param classPathIgnoreRef the array of ant property names which should not be displayed, may be
     * an empty array but not null
     * @param platformProperty the ant name property holding the J2SE platform system name or null
     * if the platform should not be displayed
     * @param diagramsNodeActions actions which should be available on the created node.
     */
    UMLDiagramsRootNode( UMLProject project, UMLProjectHelper helper,
        PropertyEvaluator eval)
        
    {
        super(new DiagramsChildren( eval, helper), Lookups.singleton(project));
        ((DiagramsChildren)this.getChildren()).setNode(this);
        
        this.setIconBaseWithExtension(ICON);
        setElement(helper.getProject());
        
        this.helper = helper;
        
        this.displayName = NbBundle.getMessage(
            UMLModelRootNode.class, "CTL_UMLDiagramsRootNode"); //NOI18N
        
        DispatchHelper dispatcher = new DispatchHelper();
        dispatcher.registerDrawingAreaEvents(this);
        
        // this.displayName = "DiagramsRootNode-Trey";
        // setName( ProjectUtils.getInformation( project ).getDisplayName() );
    }
    
    /**
     * Adds the model element to the lookup.
     *
     * @param element The Model element that represents the project.
     */
    protected void addElementCookie(IElement element)
    {
    }
  
    
    public String getDisplayName()
    {
        return this.displayName;
    }
    
    public String getName()
    {
        return this.getDisplayName();
    }
    
    	/**
	 * Get the new types that can be created in this node. For example, a node
	 * representing a class will permit attributes, operations, classes,
	 * interfaces, and enumerations to be added.
	 *
	 * @return An array of new type operations that are allowed.
	 */
    public NewType[] getNewTypes()
    {
		String elType = getElementType();
		NewType[] retVal = null;

		return new NewType[]
		{
			new NewDiagramType(getModelElement()),

		};
    }

    public Action[] getActions( boolean context )
    {
       return super.getNewMenuAction();
//       return new Action[]
//       {
//          SystemAction.get(NewAction.class),
//       };
    }

    
    /**
     * Retrieves the associated model element.  The model element will be the
     * IProject that represent the project.
     *
     * @return The IProject instance.
     * @see IProject
     */
    public IElement getModelElement()
    {
        return helper.getProject();
    }
    
    public boolean canCopy()
    {
        return false;
    }
    
    public boolean canCut()
    {
        return false;
    }
    
    public boolean canDestroy()
    {
        return false;
    }
    
    public boolean canRename()
    {
        return false;
    }
    
    /**
     * Check if the specified node is the same UMLDiagramsRootNode
     * instance.
     */
    public boolean equals(Object obj)
    {
        boolean retVal = false;
        
        // First check if the object is a UMLDiagramNode.  If we
        // did not make this check then the model node would look
        // like the same node.
        if(obj instanceof UMLDiagramsRootNode)
        {
            retVal = super.equals(obj);
        }
        
        return retVal;
    }
    
    private static class DiagramsChildren extends UMLChildren
    {
        private UMLDiagramsRootNode node = null;
        private final PropertyEvaluator eval;
        private final UMLProjectHelper helper;
        
        DiagramsChildren(PropertyEvaluator eval, UMLProjectHelper helper)
        {
            this.eval = eval;
            this.helper = helper;
        }
        
        public Node findChild(String name)
        {
            Node[] list = getNodes();
            
            if(list.length == 0)
            {
                return null;
            }
            
            if(name == null)
            {
                // return any node
                return list[0];
            }
            
            
            for(int i = 0; i < list.length; i++)
            {
                if(list[i] instanceof ITreeDiagram)
                {
                    ITreeDiagram diagram = (ITreeDiagram)list[i];
                    IProxyDiagram proxy = diagram.getDiagram();
                    if (name.equals(proxy.getFilename()))
                    {
                        // ok, we have found it
                        return list[i];
                    }
                }
            }
            return null;
            
        }
        
        /**
         * Notify listeners that a node is being expanded.  Listeners are able to add
         * nodes to the ProjectTreeItem.
         */
        protected void sendNodeExpandEvent()
        {
            NetBeansUMLProjectTreeModel model = UMLModelRootNode.getProjectTreeModel();
            
            // Debug.out.println("Firing node expand event");
            if(model != null)
            {
                model.fireDiagramsExpanding(node, helper.getProject());
                
            }
        }
        
        public void setNode(UMLDiagramsRootNode node)
        {
            this.node = node;
        }
    }
    
    
    
    private static class SimpleFileFilter extends FileFilter
    {
        private String description;
        private Collection extensions;
        
        
        public SimpleFileFilter(String description, String[] extensions)
        {
            this.description = description;
            this.extensions = Arrays.asList(extensions);
        }
        
        public boolean accept(File f)
        {
            return true; // mcf hack
            /*
            if (f.isDirectory())
                return true;
            try {
                return FileUtil.isArchiveFile(f.toURI().toURL());
            } catch (MalformedURLException mue) {
                ErrorManager.getDefault().notify(mue);
                return false;
            }
             */
        }
        
        public String getDescription()
        {
            return this.description;
        }
    }
    
    
    public void recalculateChildren()
    {
        UMLChildren children = (UMLChildren)getChildren();
        children.recalculateChildren();
    }

    
    // implmentations of interface IDrawingAreaEventsSink
    /////////////////////////////////////////////////////
    
//    public void onDrawingAreaTooltipPreDisplay(
//        IDiagram pParentDiagram, 
//        IPresentationElement pPE, 
//        IToolTipData pTooltip, 
//        IResultCell cell) 
//    {}

    public void onDrawingAreaPreSave(
        IProxyDiagram pParentDiagram, IResultCell cell)
    {}

    public void onDrawingAreaPrePropertyChange(
        IProxyDiagram pProxyDiagram, int nPropertyKindChanged, IResultCell cell)
    {}

    public void onDrawingAreaPreFileRemoved(String sFilename, IResultCell cell)
    {}

//    public void onDrawingAreaPreDrop(
//        IDiagram pParentDiagram, 
//        IDrawingAreaDropContext pContext, 
//        IResultCell cell)
//    {}
//
//    public void onDrawingAreaPreCreated(
//        IDrawingAreaControl pDiagramControl, IResultCell cell)
//    {}

    public void onDrawingAreaPostSave(
        IProxyDiagram pParentDiagram, IResultCell cell)
    {}

    public void onDrawingAreaPostPropertyChange(
        IProxyDiagram pProxyDiagram, int nPropertyKindChanged, IResultCell cell)
    {}

//    public void onDrawingAreaPostDrop(
//        IDiagram pParentDiagram, 
//        IDrawingAreaDropContext pContext, 
//        IResultCell cell)
//    {}
//
    public void onDrawingAreaPostCreated(DataObject obj, IResultCell cell)
    {}

    public void onDrawingAreaOpened(
        IDiagram pParentDiagram, IResultCell cell)
    {}

    public void onDrawingAreaKeyDown(
        IDiagram pParentDiagram, 
        int nKeyCode, 
        boolean bControlIsDown, 
        boolean bShiftIsDown, 
        boolean bAltIsDown, IResultCell cell)
    {}

    
    // cvc - CR 6302705
    public void onDrawingAreaFileRemoved(String sFilename, IResultCell cell)
    {
        Enumeration<ITreeItem> nodeKids = getNodeChildren();

        while (nodeKids.hasMoreElements())
            //ITreeItem treeItem: nodeKids)
        {
            ITreeItem treeItem = nodeKids.nextElement();

            if (treeItem != null &&
                treeItem.getData().getDiagram()
                .getFilename().equals(sFilename))
            {
                removeChild(treeItem);
            }
        }
    }

    public void onDrawingAreaClosed(
        IDiagram pParentDiagram, boolean bDiagramIsDirty, IResultCell cell)
    {}

    public void onDrawingAreaActivated(
        IDiagram pParentDiagram, IResultCell cell)
    {}
    
    
    public PasteType getDropType(Transferable t, int action, int index)
    {
        return null;
    }

}
