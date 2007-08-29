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


package org.netbeans.modules.uml.project.ui.nodes;

import org.netbeans.modules.uml.core.metamodel.structure.IProject;

import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import org.openide.actions.OpenAction;
import org.openide.cookies.OpenCookie;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.CookieSet;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;

import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.scm.ISCMIntegrator;
import org.netbeans.modules.uml.core.scm.SCMFeatureKind;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventDispatcher;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem;
import org.netbeans.modules.uml.ui.controls.projecttree.ProjectTreeItemImpl;
import org.netbeans.modules.uml.ui.support.DispatchHelper;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.commonresources.CommonResourceManager;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeDiagram;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeElement;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeFolder;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeRelElement;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ProjectTreeComparable;


/**
 *
 * @author Trey Spiva
 */
public class UMLElementNode extends AbstractModelElementNode
	implements ITreeItem, CookieSet.Factory, Comparable
{
	private IProjectTreeItem m_Data = null;
	private String mPathAsString = ""; // NOI18N
	private Node.Cookie mOpenCookie = null;
	private Node.Cookie mModelElementCookie = null;
	
	/** Determines if the node has been initialized with children yet. */
	private boolean mIsInitialized = false;
	
	//Cache to speed up performance
	private Image icon=null;
//	private Action[] actions=null;
	
	/**
	 * Create a new UMLElementNode
	 */
	public UMLElementNode()
	{
		this(new ProjectTreeItemImpl());
		initCookies();
	}
	
	/**
	 *
	 */
	public UMLElementNode(Lookup lookup)
	{
		this(lookup, new ProjectTreeItemImpl());
	}
	
	/**
	 * Create a new UMLElementNode
	 */
	public UMLElementNode(Children ch, Lookup lookup)
	{
		this(ch, lookup, new ProjectTreeItemImpl());
	}
	
	/**
	 * Create a new UMLElementNode and initialize its data with the information
	 * from an IProjectTreeItem.
	 *
	 * @param item The ITreeItem used to initialize the new node.
	 * @see IProjectTreeItem
	 */
	public UMLElementNode(IProjectTreeItem data)
	{
		super(new UMLChildren());
		setData(data);
		setUpChildren();
		addProjectTreeItemCookie(data);
	}
	
	/**
	 * Create a new UMLElementNode and initialize its data with the information
	 * from an ITreeItem.
	 *
	 * @param item The ITreeItem used to initialize the new node.
	 * @see ITreeItem
	 */
	public UMLElementNode(ITreeItem item)
	{
		this(item.getData());
	}
	
	/**
	 * Create a new UMLElementNode and initialize its data with the information
	 * from an IProjectTreeItem.
	 */
	public UMLElementNode(Lookup lookup, IProjectTreeItem data)
	{
		super(new UMLChildren(), lookup);
		setData(data);
		//addChild(this);
		setUpChildren();
	}
	
	public UMLElementNode(Children ch, Lookup lookup, IProjectTreeItem data)
	{
		super(ch, lookup);
		setData(data);
		setUpChildren();
		//addChild(this);
		//setChildren(new UMLChildren(this));
	}
	
	
	
	protected void addProjectTreeItemCookie(IProjectTreeItem projectTreeItem)
	{
		getCookieSet().add(projectTreeItem);
	}
	
	/**
	 * Gathers the nodes children.
	 */
	public void setUpChildren()
	{
		Children ch = getChildren();
		if (ch != null && ch instanceof UMLChildren)
		{
			final UMLChildren children = (UMLChildren)ch;
			children.setItem(this);
		}
	}
	
	//**************************************************
	// ITreeItem Implementation
	//**************************************************
	
	public String getName()
	{
		IElement element = getModelElement();
		
        if (element instanceof INamedElement)
		{
			// return unformatted name for attributes and operations, so that
			// rename action on those nodes will display the bare name without
			// visibility modifier and type information, since we only support 
			// rename the name part of these two types of elements from project
			// tree
			if (element.getElementType().equals(ELEMENT_TYPE_ATTRIBUTE) ||
				element.getElementType().equals(ELEMENT_TYPE_OPERATION))
            {
				return (((INamedElement)element).getName());
            }
		}
        
		return super.getName();
	}

    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem#setDisplayedName(java.lang.String)
    */
        public void setDisplayedName(String name)
        {
            setDisplayedName(name, true);
        }
        
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem#setDisplayedName(java.lang.String,V)
    */
        public void setDisplayedName(String name, boolean buildProperties)
        {
            if (getData() != null)
                getData().setItemText(name);
            
            IElement element = getModelElement();
            
            if (element instanceof INamedElement)
            {
                // set the name of the node as well, so in edit mode, the new name
                // of the element will be displayed
                super.setName(((INamedElement)element).getName());
            }
            
//        else
//            setName(name);
            
            setDisplayName(name, buildProperties);
        }
    
    
    public String getDisplayName()
    {
        String retVal = super.getDisplayName();
        
        if (retVal == null || retVal.length() <= 0)
        {
            if (getData() != null)
                retVal = getData().getItemText();
        }
        
        if (retVal == null || retVal.length() <= 0)
            retVal = getName();
        
        return retVal;
    }
    
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem#getDisplayedName()
    */
    public String getDisplayedName()
    {
        return getDisplayName();
	}
	
	
	
   /* (non-Javadoc)
	* @see org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem#getPath()
	*/
	public Object[] getPath()
	{
		ArrayList parents = new ArrayList();
		
		while (getParentNode() != null)
		{
			parents.add(0, getParentNode());
		}
		
		Object[] retVal = new Object[parents.size()];
		parents.toArray(retVal);
		return retVal;
	}
	
   /* (non-Javadoc)
	* @see org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem#getPathAsString()
	*/
	public String getPathAsString()
	{
		//      StringBuffer retVal = new StringBuffer(getDisplayName());
		//
		////      ArrayList parents = new ArrayList();
		////
		////      while (getParentNode() != null)
		////      {
		////         retVal.insert(0, "|");
		////         retVal.insert(0, getParentNode().getDisplayName());
		////      }
		//
		//      Node parent = getParentNode();
		//      if(parent != null)
		//      {
		//
		//      }
		//
		//      return retVal.toString();
		
		return mPathAsString;
	}
	
   /* (non-Javadoc)
	* @see org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem#setPathAsString(java.lang.String)
	*/
	public void setPathAsString(String str)
	{
		mPathAsString = str;
	}
	
   /* (non-Javadoc)
	* @see org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem#isSame(org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem)
	*/
	public boolean isSame(ITreeItem queryItem)
	{
		return equals(queryItem);
	}
	
   /* (non-Javadoc)
	* @see org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem#getParentItem()
	*/
	public ITreeItem getParentItem()
	{
		ITreeItem retVal = null;
		Node parentNode = getParentNode();
		
		if (parentNode instanceof ITreeItem)
			retVal = (ITreeItem)parentNode;
		
		return retVal;
	}
	
   /* (non-Javadoc)
	* @see org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem#setParentItem(org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem)
	*/
	public void setParentItem(ITreeItem parent)
	{
	}
	
   /* (non-Javadoc)
	* @see org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem#getTopParentItem()
	*/
	public ITreeItem getTopParentItem()
	{
		ITreeItem pTop = null;
		ITreeItem pItem = getParentItem();
		
		while (pItem != null)
		{
			pTop = pItem;
			ITreeItem pTemp = pTop.getParentItem();
			pItem = pTemp;
		}
		
		return pTop;
	}
	
   /* (non-Javadoc)
	* @see org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem#getOwningTreeElement()
	*/
	public ITreeElement getOwningTreeElement()
	{
		ITreeElement pOutEle = null;
		ITreeItem pLast = null;
		ITreeItem pItem = getParentItem();
		
		while (pItem != null)
		{
			pLast = pItem;
			
			if (pItem instanceof ITreeRelElement)
			{
				ITreeItem pTemp = pLast.getParentItem();
				pItem = pTemp;
			}
			
			else if (pItem instanceof ITreeElement)
				pItem = null;
			
			else
			{
				ITreeItem pTemp = pLast.getParentItem();
				pItem = pTemp;
			}
		}

		if (pLast != null)
		{
			if (pLast instanceof ITreeElement)
				pOutEle = (ITreeElement)pLast;
		}
		
		return pOutEle;
	}
	
   /* (non-Javadoc)
	* @see org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem#getType()
	*/
	public String getType()
	{
		return null;
	}
	
   /* (non-Javadoc)
	* @see org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem#addChild(org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem)
	*/
	public void addChild(ITreeItem item)
	{
        boolean canAdd = true;
		// <Sheryl> Do we really need this condition check? In case like 'Relationships' 
		// -> 'Specializations', these two folders both refer to the same 
		// model element, the sub-folder will not be added as a result, #6320478
		// to filter out the relationship end which refers to itself, it's temporary
		// we need to go back to visit the root cause in project tree impl
        ITreeItem parent = getParentItem();
        if(parent != null)
        {
            String yourID = item.getData().getModelElementXMIID();
            String parentID = parent.getData().getModelElementXMIID();
			if (item instanceof ITreeFolder == false)
				canAdd = !yourID.equals(parentID);
        }
        if(canAdd == true)
        {            
            if (item instanceof Node)
            {
                Node[] node ={(Node)item};
                Children children = getChildren();

                if (children != null)
                    children.add(node);
            }
        }
	}
	
   /* (non-Javadoc)
	* @see org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem#insertAt(org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem, int)
	*/
	public void insertAt(ITreeItem item, int index)
	{
		// The NetBeans API does not allow you to add a child to a specific
		// index.  The children are sorted so this is not neccessary.
		addChild(item);
	}
	
   /* (non-Javadoc)
	* @see org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem#removeChild(org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem)
	*/
	public void removeChild(ITreeItem item)
	{
		if (item instanceof Node)
		{
			Node[] node = {(Node)item};
			Children children = getChildren();
			
			if (children != null)
				children.remove(node);
		}
	}
	
   /* (non-Javadoc)
	* @see org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem#removeAllChildren()
	*/
	public void removeAllChildren()
	{
		Children children = getChildren();
		
		if ((children != null) && (children.getNodesCount() > 0))
			children.remove(children.getNodes());
	}
	
   /* (non-Javadoc)
	* @see org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem#getChild(int)
	*/
	public ITreeItem getChild(int index)
	{
		// TODO Auto-generated method stub
		return null;
	}
	
   /* (non-Javadoc)
	* @see org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem#sortChildren()
	*/
	public void sortChildren() 
	{
		((UMLChildren)getChildren()).recalculateChildren();
	}
	
   /* (non-Javadoc)
	* @see org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem#sortChildren(java.util.Comparator)
	*/
	public void sortChildren(Comparator compare) {}
	
   /* (non-Javadoc)
	* @see org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem#getData()
	*/
	public IProjectTreeItem getData()
	{
		return m_Data;
	}
	
	protected void setData(IProjectTreeItem data)
	{
		m_Data = data;
		
		if (data != null)
			m_Data.setProjectTreeSupportTreeItem(this);
	}
	
   /* (non-Javadoc)
	* @see org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem#getChildCount()
	*/
	public int getChildCount()
	{
		int retVal = 0;
		Children children = getChildren();
		
		if (children != null)
			retVal = children.getNodesCount();
		
		return retVal;
	}
	
   /* (non-Javadoc)
	* @see org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem#isInitalized()
	*/
	public boolean isInitalized()
	{
		// TODO Auto-generated method stub
		return mIsInitialized;
	}
	
   /* (non-Javadoc)
	* @see org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem#setIsInitalized(boolean)
	*/
	public void setIsInitalized(boolean value)
	{
		mIsInitialized = value;
	}
	
   /* (non-Javadoc)
	* @see org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem#getSortPriority()
	*/
	public long getSortPriority()
	{
		// TODO Auto-generated method stub
		return m_Data.getSortPriority();
	}
	
   /* (non-Javadoc)
	* @see org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem#setSortPriority(long)
	*/
	public void setSortPriority(long value)
	{
		m_Data.setSortPriority(value);
	}
	
   /* (non-Javadoc)
	* @see org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem#setExpanded(boolean)
	*/
	public void setExpanded(boolean value){}
	
   /* (non-Javadoc)
	* @see org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem#setSelected(boolean)
	*/
	public void setSelected(boolean value) {}
	
	/**
	 * Retrieve a collection that contains all of the children contained by the
	 * node.
	 * <br>
	 * <i>Note:</i> This collection should be treated as <b>read-only</b>.  If a
	 * node is added to the returned collection any associated view will not be
	 * updated until a refresh is performed.
	 *
	 * @return The collection of children.
	 */
	public Enumeration<ITreeItem> getNodeChildren()
	{
		Children children = getChildren();
		
		return new TreeItemEnumeration(children);
	}
	
	//**************************************************
	// AbstractModelElement Implementation
	//**************************************************
	
    /**
     * Retrieves the actions for the node.  This method only returns
     * the context sensitive actions.
     *
     * @param context Whether to find actions for context meaning or for the
     *                node itself
     * @return A list of actions (you may include nulls for separators)
     */
    public Action[] getActions(boolean context)
    {

// Source control Sub menu Actions are created based on the SCM status of the nodes (Class diagrams, classes) 
// so every time we need to get the actions afresh.  
		
//			if (actions == null)
		return super.getActions(context);
		
	}
	
	public Action getPreferredAction()
	{
		return SystemAction.get(OpenAction.class);
	}
	
        public Image getIcon(int type)
        {
            Image retVal = null;
            //	if(icon!=null) return icon;
            
            ITreeItem item = this;
            IProjectTreeItem data = item.getData();
            
            if (icon!=null)
            {
                retVal = icon;
            }
            
            else
            {
                
                if (item instanceof ITreeFolder)
                {
                    CommonResourceManager resource =
                        CommonResourceManager.instance();
                    
                    retVal = createImage(resource
                        .getIconDetailsForElementType(item.getName()));
                }
                
                else if (item instanceof ITreeDiagram)
                {
                    ITreeDiagram diagram = (ITreeDiagram)item;
                    
                    CommonResourceManager resource =
                        CommonResourceManager.instance();
                    
                    retVal = createImage(resource
                        .getIconDetailsForElementType(diagram.getDiagramType()));
                }
                
                else if (data.getModelElement() != null)
                {
                    if (data.getModelElement() instanceof IProject)
                        retVal = super.getIcon(type);
                    
                    else
                    {
                        CommonResourceManager resource =
                            CommonResourceManager.instance();
                        
                        retVal = createImage(
                            resource.getIconDetailsForDisp(data.getModelElement()));
                    }
                }
                
                else if (data.isProject())
                {
                    // CommonResourceManager resource = 
                    //     CommonResourceManager.instance();
                    // retVal = createImage(
                    //     resource.getIconDetailsForElementType("WSProject"));
                    
                    retVal = super.getIcon(type);
                }
                
                else if (data.isWorkspace())
                {
                    CommonResourceManager resource = CommonResourceManager.instance();
                    // special case for design pattern catalog
                    
                    if (data.getItemText().equals("DesignPatternCatalog")) // NOI18N
                    {
                        retVal = createImage(
                            resource.getIconDetailsForElementType(
                            "DesignPatternCatalog")); // NOI18N
                    }
                    
                    else
                    {
                        retVal = createImage(resource
                            .getIconDetailsForElementType("Workspace")); // NOI18N
                    }
                }
                
                else
                {
                    CommonResourceManager resource = 
                        CommonResourceManager.instance();
                    
                    retVal = createImage(
                        resource.getIconDetailsForElementType(item.getName()));
                }
            }
            
            if (retVal == null)
                retVal = super.getIcon(type);
            
            //return getIconWithOverlay(retVal, this);
            retVal=getIconWithOverlay(retVal, this);
            
            icon=retVal;
            return icon;
        }
	
	public Image getOpenedIcon(int type)
	{
		return getIcon(type);
	}
	
   /* (non-Javadoc)
	* @see org.netbeans.modules.uml.netbeans.umlproject.ui.AbstractModelElementNode#getModelElement()
	*/
	public IElement getModelElement()
	{
		IElement retVal = null;
		IProjectTreeItem item = getData();
		
		if (item != null)
			retVal = item.getModelElement();

		return retVal;
	}
	
	
	//**************************************************
	// CookieSet.Factory implementation
	//**************************************************
	
	public Node.Cookie createCookie(Class klass)
	{
		Node.Cookie retVal = null;
		
		if (OpenCookie.class.equals(klass) == true)
		{
			if (mOpenCookie == null)
				mOpenCookie = createOpenCookie();
			
			retVal = mOpenCookie;
		}
		
		return retVal;
	}
	
	
	//**************************************************
	// Helper Methods
	//**************************************************
	
	public String toString()
	{
		String retVal = getDisplayedName();
		
		if ((retVal == null) || (retVal.length() <= 0))
			retVal = getName();
		
		return retVal;
	}
	
   /* (non-Javadoc)
	* @see java.lang.Object#equals(java.lang.Object)
	*/
	public boolean equals(Object obj)
	{
		boolean retVal = false;
		
		if (this.hashCode() == obj.hashCode())
		    return true;
        
		IProjectTreeItem myItem = getData();
		
		if (obj instanceof IElement)
		{
			IElement myElement = myItem.getModelElement();
			
			if (myElement != null)
				retVal = myElement.isSame((IElement)obj);
		}

		else if (obj instanceof UMLElementNode)
		{
			UMLElementNode node = (UMLElementNode)obj;
			IProjectTreeItem testItem = node.getData();
			String testText = node.getDisplayedName();
			String myText   = getDisplayedName();
			
			if ((testText != null) && (myText != null))
				retVal = testText.equals(myText);
			
			else if ((testText == null) && (myText == null))
				retVal =  super.equals(obj);
			
			if (retVal == true)
			{
				String testXMIID = testItem.getModelElementXMIID();
				String myXMIID = myItem.getModelElementXMIID();
				
				if ((testXMIID != null) && (myXMIID != null))
					retVal = testXMIID.equals(myXMIID);
			}
		}
		
		else
		{  // Generic equals method.  This can be used to test
			// to ITreeItem(s).
			retVal =  super.equals(obj);
		}
		
		return retVal;
	}
	
	/**
	 * Initializes the node with the default cookies.  <b>Note:</b>  Do not
	 * call this method if a looup is added.  When a lookup is added then the
	 * lookup is used to find the cookies.
	 */
	protected void initCookies()
	{
		Class[] cookies = {OpenCookie.class};
		getCookieSet().add(cookies, this);
	}
	
	/**
	 * Creates the open cookie to use when opening the node.
	 */
	protected Node.Cookie createOpenCookie()
	{
		return new ModelElementOpen();
	}
	
	
	
	/**
	 * @param string
	 * @return
	 */
	protected Image createImage(String iconLocation)
	{
		return Utilities.loadImage( iconLocation, true );
	}
	
	/**
	 * Implementation of method from Comparable interface.
	 * @return
	 */
	public int compareTo(Object o)
	{
	    return ProjectTreeComparable.compareTo(this, o);
	}
	
	// to be overridden by subclasses as needed
	public void registerListeners()
	{}
	
	
	public class ModelElementOpen implements OpenCookie
	{
		public void open()
		{
			DispatchHelper helper = new DispatchHelper();
			IProjectTreeEventDispatcher disp = helper.getProjectTreeDispatcher();
			if (disp != null)
			{
				IEventPayload payload = 
					disp.createPayload("ProjectTreeDoubleClick"); // NOI18N
				
				helper.getProjectTreeDispatcher().fireDoubleClick(null,
					getData(), false, false, false, false, payload);
			}
			
			registerListeners();
		}
	}
	
	public class TreeItemEnumeration implements Enumeration < ITreeItem >
	{
		Enumeration m_ChildrenEnum = null;
		
		public TreeItemEnumeration(Children c)
		{
			m_ChildrenEnum = c.nodes();
		}
		
		public boolean hasMoreElements()
		{
			return m_ChildrenEnum.hasMoreElements();
		}
		
		public ITreeItem nextElement()
		{
			return (ITreeItem)m_ChildrenEnum.nextElement();
		}
	}
	
	
	
	public void vcsFeatureExecuted(/* SCMFeatureKind */ int kind)
	{
		if(kind == SCMFeatureKind.FK_CHECK_IN || 
			kind == SCMFeatureKind.FK_CHECK_OUT ||
			kind == SCMFeatureKind.FK_ADD_TO_SOURCE_CONTROL || 
			kind == SCMFeatureKind.FK_REMOVE_FROM_SOURCE_CONTROL ||
			kind == SCMFeatureKind.REFRESH_STATUS)
		{
			fireIconChange();
		}
	}
	
	protected Image getIconWithOverlay(Image image, ITreeItem item)
	{
		ImageIcon image1 = new ImageIcon(image);
		
		
		if(!(item instanceof ITreeFolder))
		{
			Icon overlay = getOverlayIcon(item.getData());
			
			if((image1 != null) && (overlay != null))
			{
				GraphicsDevice[] gs = GraphicsEnvironment
					.getLocalGraphicsEnvironment().getScreenDevices();
				
				GraphicsConfiguration[] gc = gs[0].getConfigurations();
				
				Image retImage = gc[0].createCompatibleVolatileImage(
					image1.getIconWidth(), image1.getIconHeight());
				
				Graphics g = retImage.getGraphics();
				
				image1.paintIcon(null, g, 0, 0);
				
				int overlayY = image1.getIconHeight() - overlay.getIconHeight();
				overlay.paintIcon(null, g, 0, overlayY);
				
				
				image1 = new ImageIcon(retImage);
			}
		}
		return image1.getImage();
	}
	
	protected Icon getOverlayIcon(IProjectTreeItem item)
	{
		Icon retVal = null;
		
		
		ISCMIntegrator gator = ProductHelper.getSCMIntegrator();
		if(gator != null)
		{
			int kind = gator.getSCMMaskKind(item);
			retVal = gator.getSCMMask(kind);
		}
		
		return retVal;
	}
}
