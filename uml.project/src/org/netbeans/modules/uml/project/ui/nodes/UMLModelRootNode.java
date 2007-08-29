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
 * UMLModelRootNode.java
 *
 * Created on March 1, 2005, 6:26 PM
 */

package org.netbeans.modules.uml.project.ui.nodes;

import java.util.ArrayList;
import java.util.List;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;

import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.actions.SystemAction;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.NewType;

import org.netbeans.modules.uml.project.UMLProjectHelper;
import org.netbeans.modules.uml.project.ui.NetBeansUMLProjectTreeModel;
import org.netbeans.modules.uml.project.UMLProject;
import org.netbeans.modules.uml.project.ui.nodes.actions.NewDiagramType;
import org.netbeans.modules.uml.project.ui.nodes.actions.NewPackageType;
import org.netbeans.modules.uml.project.ui.nodes.actions.NewElementType;
import org.netbeans.modules.uml.project.UMLProjectModule;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ProjectTreeComparable;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.ui.controls.filter.IFilterDialog;
import org.netbeans.modules.uml.ui.controls.filter.IProjectTreeFilterDialogEventsSink;
import org.netbeans.modules.uml.ui.support.DispatchHelper;
import org.netbeans.modules.uml.ui.swing.projecttree.JFilterDialog;
import org.netbeans.modules.uml.project.ui.nodes.actions.FilterAction;
import javax.swing.tree.DefaultTreeModel;
import org.netbeans.modules.uml.resources.images.ImageUtil;
import org.openide.actions.PropertiesAction;
import org.openide.actions.SaveAction;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node.Cookie;

/**
 *
 * @author Mike
 */
public class UMLModelRootNode extends UMLModelElementNode
        implements ModelRootNodeCookie, IProjectTreeFilterDialogEventsSink
{
    private Image mIcon = null;
    private UMLProjectHelper mHelper = null;
    
    private final String displayName;
    private IFilterDialog filterDialog = null;
    private DefaultTreeModel treeModelFilter = null;
    
    
        /* TODO - remove the 3 second sleep and fix this
         See HACK in static related to race condition
        private static ADProjectTreeEngine mEngine = new ADProjectTreeEngine();
        private static NetBeansUMLProjectTreeModel mModel = new NetBeansUMLProjectTreeModel();
         */
    //	private static ADProjectTreeEngine mEngine = null;
    //	private static NetBeansUMLProjectTreeModel mModel = null;
    //
    //	static
    //	{
    //		// TODO - remove HACK sleeps.
    //		// These were suggested by Alexi Mokeev re:
    //		// "Project saving and freeze issue"
    //		mEngine = new ADProjectTreeEngine();
    //		try
    //		{Thread.sleep(3000);}
    //		catch(Exception e)
    //		{}
    //		mModel = new NetBeansUMLProjectTreeModel();
    //		try
    //		{Thread.sleep(3000);}
    //		catch(Exception e)
    //		{}
    //
    //		mEngine.initialize(mModel);
    //	}
    
    public UMLModelRootNode(UMLProject project, UMLProjectHelper helper,
            PropertyEvaluator eval)
    {
        super();
//            super(obj, new UMLChildren(), new ProjectTreeItemImpl());
        UMLProjectModule.addModelNode(this, project);
        mHelper = helper;
        
        setElement(mHelper.getProject());
        getData().setSortPriority(1);
        getData().setDescription(IProjectTreeControl.PROJECT_DESCRIPTION);
        
        //mEngine.initialize(mModel);
        Children ch = getChildren();
        if (ch != null && ch instanceof UMLChildren)
        {
            final UMLChildren children = (UMLChildren)ch;
            ((UMLChildren)ch).setItem(this);
        }
        
        this.displayName = NbBundle.getMessage(
                UMLModelRootNode.class, "CTL_UMLModelRootNode"); //NOI18N
        
        setIconBaseWithExtension(ImageUtil.IMAGE_FOLDER + "model-root-node.png"); // NOI18N
        
        try
        {
            final DataObject dobj = DataObject.find(FileUtil.toFileObject(
                    new File(mHelper.getProject().getFileName())));
            
            if (dobj!=null)
            {
                if (mHelper.getProject().getDirty())
                {
                    Cookie cookie = dobj.getCookie(SaveCookie.class);
                    if (cookie != null)
                        getCookieSet().add(cookie);
                }
                    
                dobj.addPropertyChangeListener(new PropertyChangeListener()
                {
                    public void propertyChange(PropertyChangeEvent evt)
                    {
                        if (evt.getPropertyName().equals(DataObject.PROP_MODIFIED))
                        {
                            if (((Boolean)evt.getNewValue()).booleanValue())
                            {
                                if (getCookie(SaveCookie.class) == null)
                                    getCookieSet().add(dobj.getCookie(SaveCookie.class));
                            }
                            else
                            {
                                Cookie cookie = getCookie(SaveCookie.class);
                                if (cookie!=null)
                                    getCookieSet().remove(cookie);
                            }
                        }
                    }
                });
            }
        }
        catch (Exception e)
        {
            // project file object not found, 
        }
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
        
        if (getModelElement() instanceof INamespace)
        {
            // Package types: Java Package (or an organizing folder)
            // Project types: UML Modeling Project
            if (elType.equals(ELEMENT_TYPE_PROJECT))
            {
                return new NewType[]
                {
                    new NewDiagramType(this),
                    new NewPackageType(this),
                    new NewElementType(this)
                };
            }
        } // if getModelElement() instanceof INamespace
        
        // The NewAction code does not check for null.  Therefore, we have
        // to create a new object just to keep them from throwing.
        if (retVal == null)
        {
            retVal = new NewType[0];
        }
        
        return retVal;
    }
    
    public Action[] getActions( boolean context )
    {
        ArrayList<Action> actions = new ArrayList<Action>();
        
        // actions.add(CommonProjectActions.openSubprojectsAction());
        // actions.add(CommonProjectActions.closeProjectAction());
        
        //actions.add(SystemAction.get(NewAction.class));
        super.getNewMenuAction(actions);
        actions.add(SystemAction.get(FilterAction.class));
        addContextMenu(actions);
        
        actions.add(null);
        actions.add(SystemAction.get(SaveAction.class));
        actions.add(null);
        actions.add(SystemAction.get(PropertiesAction.class));
        
        Action[] retVal = new Action[actions.size()];
        actions.toArray(retVal);
        return retVal;
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
        if (mHelper==null)
            return null;
        
        return mHelper.getProject();
    }
    
    /**
     * Adds the model element to the lookup.
     *
     * @param element The Model element that represents the project.
     */
    protected void addElementCookie(IElement element)
    {
    }
    
    public Node.Cookie getCookie(Class type)
    {
        Node.Cookie cookie = super.getCookie(type);
        if(cookie == null)
        {
            IElement element = getModelElement();
            if(element instanceof Node.Cookie)
            {
                if(type.isAssignableFrom(element.getClass()) == true)
                {
                    cookie = (Node.Cookie)element;
                }
            }
        }
        // MCF experiment
        if(cookie == null)
        {
            if(type.isAssignableFrom(ModelRootNodeCookie.class) == true)
                return this;
            
        }
        return cookie;
    }
    
    
    //////////////////////////////////////
    
    //    public String getDisplayName()
    //    {
    //       return mHelper.getDisplayName();
    //    }
    
    
    public Image getIcon( int type )
    {
        Image original = super.getIcon( type );
        //return broken ? Utilities.mergeImages(original, brokenProjectBadge, 8, 0) : original;
        return original;
    }
    
    public Image getOpenedIcon( int type )
    {
        Image original = super.getOpenedIcon(type);
        //return broken ? Utilities.mergeImages(original, brokenProjectBadge, 8, 0) : original;
        return original;
    }
    
    public static NetBeansUMLProjectTreeModel getProjectTreeModel()
    {
        return UMLProjectModule.getProjectTreeModel();
    }
    
    
    
    public void recalculateChildren()
    {
        UMLChildren children = (UMLChildren)getChildren();
        children.recalculateChildren();
    }
    
    ///////////////////////////////////////////////////////////////////////////
    // Helper Methods
    
    /**
     * Initializes the node with the default cookies.  <b>Note:</b>  Do not
     * call this method if a looup is added.  When a lookup is added then the
     * lookup is used to find the cookies.
     */
    protected void initCookies()
    {
        
    }
    
    // Implementations for interface IProjectTreeFilterDialogEventsSink
    //////////////////////////////////////////////////////////////////
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.filter.IProjectTreeFilterDialogEventsSink#onProjectTreeFilterDialogInit(org.netbeans.modules.uml.ui.controls.filter.IFilterDialog, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onProjectTreeFilterDialogInit(
            IFilterDialog dialog,IResultCell cell)
    {}
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.filter.IProjectTreeFilterDialogEventsSink#onProjectTreeFilterDialogOKActivated(org.netbeans.modules.uml.ui.controls.filter.IFilterDialog, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onProjectTreeFilterDialogOKActivated(
            IFilterDialog dialog, IResultCell cell)
    {
        if (dialog != null && dialog instanceof JFilterDialog)
        {
            JFilterDialog filterDialog = (JFilterDialog)dialog;
            
            // setFilterDialog(dialog);
            setTreeModelFilter(filterDialog.getTreeModel());
            
            // Model root node children are refresh with filter applied
            // TODO - unfortunately, this "flattens" any expanded nodes
            // instead of leaving them in their current expanded state
	    SwingUtilities.invokeLater(new Runnable() 
	    {
		public void run() 
		{
		    recalculateChildren();
		}
	    });
        }
    }
    
    public void filterListenerRegistered(boolean register)
    {
        DispatchHelper dispatchHelper = new DispatchHelper();
        
        if (register)
            // cvc - CR#6271053
            // listen for Filter dialog OK action
            dispatchHelper.registerProjectTreeFilterDialogEvents(this);
        
        else
            // cvc - CR 6271328
            // only listen while Filter dialog is "alive" otherwise, all
            // projects will listen to each others' filter dialogs
            dispatchHelper.revokeProjectTreeFilterDialogSink(this);
    }
    
    
    public DefaultTreeModel getTreeModelFilter()
    {
        return treeModelFilter;
    }
    
    public void setTreeModelFilter(DefaultTreeModel val)
    {
        this.treeModelFilter = val;
    }
    
    // Gets the Context menu for the Model node from layer.xml
    protected void addContextMenu(List actions)
    {
        Action[] nodeActions = null;
//        UMLElementNode node = new UMLElementNode();
        nodeActions = getActionsFromRegistry(
              "contextmenu/uml/designpatternformodel");
//                node.getActionsFromRegistry("contextmenu/uml/designpatternformodel");
        
        for(Action curAction : nodeActions)
        {
            if (curAction == null)
                actions.add(null);
            
            else if (curAction.isEnabled())
                actions.add(curAction);
        }
        actions.add(null);
        
        nodeActions =
                getActionsFromRegistry("Actions/UML/Search");
        for(Action curAction : nodeActions)
        {
            if (curAction == null)
                actions.add(null);
            
            else if (curAction.isEnabled())
                actions.add(curAction);
        }
        
        actions.add(null);
        
        nodeActions =
                getActionsFromRegistry("contextmenu/uml/report");
        for(Action curAction : nodeActions)
        {
            if (curAction == null)
                actions.add(null);
            
            else if (curAction.isEnabled())
                actions.add(curAction);
        }
    }
    
    public boolean canCopy()
    {
        return false;
    }
    
    public boolean canCut()
    {
        return false;
    }
    
    public boolean canRename()
    {
        return false;
    }
    
    public boolean canDestroy()
    {
        return false;
    }
    

   /* (non-Javadoc)
    * @see java.lang.Object#equals(java.lang.Object)
    */
    public boolean equals(Object obj)
    {
	// NB60TBD special case for diagrams root node to make it work with NB60
	if (obj instanceof UMLDiagramsRootNode) {
	    return  false;
	}
	return super.equals(obj);
    }

}
