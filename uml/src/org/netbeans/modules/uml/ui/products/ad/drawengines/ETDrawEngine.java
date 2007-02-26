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


package org.netbeans.modules.uml.ui.products.ad.drawengines;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.swing.SwingUtilities;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleComponent;
import javax.accessibility.AccessibleRelation;
import javax.accessibility.AccessibleRelationSet;
import javax.accessibility.AccessibleRole;
import javax.accessibility.AccessibleSelection;
import javax.accessibility.AccessibleState;
import javax.accessibility.AccessibleStateSet;

import org.netbeans.modules.uml.common.ETException;
import org.netbeans.modules.uml.common.generics.IteratorT;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ITaggedValue;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IGraphEventKind;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.support.umlsupport.ETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.ETRect;
import org.netbeans.modules.uml.core.support.umlsupport.ETSize;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETSize;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.products.ad.application.IMenuManager;
import org.netbeans.modules.uml.ui.products.ad.application.action.ContextMenuActionClass;
import org.netbeans.modules.uml.ui.products.ad.application.action.IETContextMenuHandler;
import org.netbeans.modules.uml.ui.products.ad.compartments.ETListCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.IADCommentBodyCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.IADExpressionCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.ITemplateParametersCompartment;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.IETGraphObjectUI;
import org.netbeans.modules.uml.ui.support.CreationFactoryHelper;
import org.netbeans.modules.uml.ui.support.DiagramAndPresentationNavigator;
import org.netbeans.modules.uml.ui.support.IDiagramAndPresentationNavigator;
import org.netbeans.modules.uml.ui.support.accessibility.AccessibleSelectionParent;
import org.netbeans.modules.uml.ui.support.applicationmanager.DrawingFactory;
import org.netbeans.modules.uml.ui.support.applicationmanager.IGraphPresentation;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchive;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveDefinitions;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem;
import org.netbeans.modules.uml.ui.support.drawingproperties.IColorProperty;
import org.netbeans.modules.uml.ui.support.drawingproperties.IDrawingPropertyProvider;
import org.netbeans.modules.uml.ui.support.drawingproperties.IFontProperty;
import org.netbeans.modules.uml.ui.support.umltsconversions.RectConversions;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ETRectEx;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ETTransformOwner;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETLabel;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IEventManager;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IGraphObjectManager;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IListCompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IResourceUserHelper;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ISetCursorEvent;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ISimpleListCompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ITSGraphObject;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IToolTipData;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.DrawEngineResourceUser;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TSLabelKind;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.swing.drawingarea.ADGraphWindow;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import org.netbeans.modules.uml.ui.swing.drawingarea.diagramtools.SmartDragTool;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.UIResources;
import org.netbeans.modules.uml.ui.support.drawingproperties.IDrawingProperty;
import org.netbeans.modules.uml.ui.support.helpers.ETSmartWaitCursor;
import com.tomsawyer.editor.TSEGraph;
import com.tomsawyer.editor.TSEObject;
import com.tomsawyer.graph.TSGraphObject;
import com.tomsawyer.drawing.geometry.TSConstPoint;
import com.tomsawyer.editor.TSTransform;
import java.util.StringTokenizer;

//import org.openide.cookies.SourceCookie;
import org.openide.explorer.ExplorerManager;
//import org.openide.src.SourceElement;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.netbeans.modules.uml.ui.swing.testbed.addin.menu.Separator;

/*
 *  Common base class for all drawEngines. 
 */
public abstract class ETDrawEngine extends ETTransformOwner implements IDrawEngine, IETContextMenuHandler, IDrawingPropertyProvider, IResourceUserHelper, Accessible
{
	protected DrawEngineResourceUser m_ResourceUser = new DrawEngineResourceUser((IResourceUserHelper)this);

	public static int TSE_NODE_RESIZE_ORIGINAL = 0;
	public static int TSE_NODE_RESIZE_ORIG_INTERACTIVE = 1;
		
	public static final int MK_LABELMANAGER = 0;
	public static final int MK_BRIDGEMANAGER = 1;
	public static final int MK_EVENTMANAGER = 2;

   /// Container variables
   boolean m_bFindNewContainer = true; /// set to true to get the container to be recalculated
   boolean m_bActiveLayout = false;    /// true during layout operations:  move, resize, layout
   IDrawEngine m_containerDE = null;

	private static final String BUNDLE_NAME = "org.netbeans.modules.uml.ui.products.ad.diagramengines.Bundle"; //$NON-NLS-1$
	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);
	private IETGraphObjectUI m_parentUI;
	private boolean m_bInitResources = false;
	protected ICompartment m_anchoredCompartment;
	protected ICompartment m_defaultCompartment;
	protected int m_LastResizeOriginator = TSE_NODE_RESIZE_ORIGINAL;

	/// mouse anchor position	

	/// All the compartments this draw engine owns
	private ETList <ICompartment> m_compartments = new ETArrayList <ICompartment>();

	//	/// This is the root of the typeproxy hierarchy. This allows us to get back to the COM interface
	//	ImpHolder*    m_COMInterface;

	protected boolean m_readOnly;

	//	TSENodeResizeOriginator m_LastResizeOriginator;

	/// This is the guy that managers the labels for this node or edge
	protected ILabelManager m_LabelManager;

	/// This is the guy that managers the edges for this node
	protected IEventManager m_EventManager;

	/// Should we check sync state when drawing.  This is set to true after create or attach.
	protected boolean m_checkSyncStateDuringDraw;

	private UIResources m_resources = null;
        private int compIndex = 0;

	/*	 
	 * Derived classes must implement this if no element is present.
	 */
	public String getElementType() {
		IETGraphObjectUI ui = getUI();
		return ui != null && ui.getModelElement() != null ? ui.getModelElement().getElementType() : null;
	}

	// **************************************************
	// Compartment Management
	//**************************************************

	public ETList <ICompartment> getCompartments() {
		return m_compartments;
	}

	/*
	 * Adds a compartment to the end of the compartments list.
	 */
	public void addCompartment(ICompartment pCompartment) {
		getCompartments().add(pCompartment);
	}

	/*
	 *  (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#addCompartment(org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment, int)
	 */
	public void addCompartment(ICompartment pCompartment, int nPos) {
		if (pCompartment != null) {
			pCompartment.setVisible(true);
			pCompartment.setEngine(this);
			//pCompartment.addModelElement(m_parentUI.getModelElement(), -1);

			if (nPos >= 0) {
				this.m_compartments.add(nPos, pCompartment);
			} else {
				m_compartments.add(pCompartment);
			}
		}
	}

	/**
		* Creates and adds the compartment to our drawengine.  The new compartment
		* is appended to the end of the compartment list.
		* 
		* @param sCompartmentID The compartment class name.  A new compartment of 
		*                       this type will be created.  ie ArrowheadCompartment
		* @return The newly created ICompartment.
		* @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#createAndAddCompartment(java.lang.String, org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment, int)
		*/
	public ICompartment createAndAddCompartment(String sCompartmentID) {
		return createAndAddCompartment(sCompartmentID, -1);
	}

	/**
		* Creates and adds the compartment to our drawengine.
		* 
		* @param sCompartmentID The compartment class name.  A new compartment of 
		*                       this type will be created.  ie ArrowheadCompartment
		* @param nPosThe Zero-based position within the list to insert the new 
		*                compartment.  If -1 the compartment is inserted at the end
		*                of the list, the list is scrolled if necessary.
		* @return The newly created ICompartment.
		* @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#createAndAddCompartment(java.lang.String, org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment, int)
		*/
	public ICompartment createAndAddCompartment(String sCompartmentID, int nPos) {
		if (sCompartmentID != null && sCompartmentID.length() > 0) {
			ICompartment compartment = CreationFactoryHelper.createCompartment(sCompartmentID);

			if (compartment != null) {
				compartment.setEngine(this);
				addCompartment(compartment, nPos);
				compartment.initResources();
			}
			return compartment;
		}

		return null;
	}

	/**
		* Create and initalizes all compartments.  This is an empty method.  It
		* must be implemented by derivied classes to do anything useful.
		* 
		* @throws ETException
		* @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#createCompartments()
		*/
	public void createCompartments() throws ETException {
	}

	public void clearCompartments() {
		if (m_compartments != null)
			this.m_compartments.clear();
	}

	/**
		* Remove this compartment to this list, optionally deletes its model element.
		* The compartments model element will not be removed from the active model.
		* @param pCompartment  The compartment to remove 
		* @see #removeCompartment(ICompartment, boolean)
		*/
	public void removeCompartment(ICompartment compartment) {
		removeCompartment(compartment, false);
	}

	/**
		* Remove this compartment to this list, optionally deletes its model element.
		*
		* @param pCompartment  The compartment to remove 
		* @param bDeleteElement Indicates whether the compartment's model element 
		*                       should be removed from the active model.  By default
		*                       this flag is FALSE.  If the model is unable to 
		*                       delete the element the compartment is removed 
		*                       anyway.  Not used in DrawEngineImpl - used in list 
		*                       compartments.
		*/
	public void removeCompartment(ICompartment compartment, boolean deleteElement) {
		if (compartment != null) {
			List compartments = getCompartments();

			int index = compartments != null ? compartments.indexOf(compartment) : -1;
			if (index >= 0) {
				compartment.setCollapsed(true);
				compartments.remove(index);
			}
		}
	}

	/**
		* Clears our list of visible compartments.
		*/
	public void clearVisibleCompartments() {
		ETList<ICompartment> compartments = getCompartments();
		if (compartments != null) {
			for (Iterator<ICompartment> iter = compartments.iterator(); iter.hasNext();) {
				ICompartment curCompartment = iter.next();
				if (curCompartment != null) {
					curCompartment.setVisible(false);
				}
			}
		}
	}

	/**
		* Adds a compartment to the end of our list of visible compartments.
		*
		* @param compartment The compartment to be made visible
		*/
	public void addVisibleCompartment(ICompartment compartment) {
		if (compartment != null) {
			compartment.setVisible(true);
		}
	}

	/**
		* Returns the number of compartments.
		*
		* @return The number of compartments
		*/
	public int getNumCompartments() {
		return m_compartments.size();
	}

	public int getNumVisibleCompartments() {
		
		int visComps = 0;
		Iterator<ICompartment> iterator = this.getCompartments().iterator();
		while (iterator.hasNext()) {
			if (iterator.next().getVisible()) {
				visComps++;
			}
		}
		return visComps;
	}
        
        /**
         * There are two categories of compartments.  The first category
         * is the visible components, which are the top level compartments 
         * that are owned by the draw engine.  The visible compartments does 
         * not include children of the top level compartments.  This is usally
         * what we want getNumSelectableCompartments to return.  For example
         * the class node uses Shift-Arrow keys to navigate through the 
         * name list compartment, attribute , and operation compartments.  The 
         * user can then use Up/Down keys to cycle through elements in these compartment lists.
         */
        protected int getNumSelectableCompartments()
        {
            return getNumVisibleCompartments();
        }
        
        protected ICompartment getSelectableCompartment (int index)
        {
            return getVisibleCompartment(index);
        }

	/**
		* Returns the compartment at the argument index.
		*
		* @param index The index into the list of the draw engines compartments
		* @return The compartment at this index, otherwise null
		*/
	public ICompartment getCompartment(int index) {
		return m_compartments.size() > index ? (ICompartment) m_compartments.get(index) : null;
	}

	/**
		* Does this draw engine have selected compartments.
		*
		* @return <code>true</code> if this draw engine has selected compartments
		*/
	public boolean hasSelectedCompartments() {
		if (this.getNumCompartments() == 0)
			return false;

		Iterator<ICompartment> iterator = this.getCompartments().iterator();
		while (iterator.hasNext()) {
			ICompartment currObject = iterator.next();
			if (currObject instanceof ISimpleListCompartment) {
				ISimpleListCompartment listCompartment = (ISimpleListCompartment) currObject;

				Iterator < ICompartment > compartmentIterator = listCompartment.getCompartments().iterator();
				while (compartmentIterator.hasNext()) {
					ICompartment foundCompartment = compartmentIterator.next();
					if (foundCompartment.isSelected()) {
						return true;
					}
				}
			}
		}

		return false;
	}

	public void selectAllCompartments(boolean bSelected) {
            if (this.getNumCompartments() == 0)
                return;
            
            Iterator iterator = this.getCompartments().iterator();
            while (iterator.hasNext()) {
                Object curObject = iterator.next();
                if (curObject instanceof ISimpleListCompartment) {
                    ISimpleListCompartment compartment = (ISimpleListCompartment) curObject;
                    
                    Iterator < ICompartment > compartmentIterator = compartment.getCompartments().iterator();
                    while (compartmentIterator.hasNext()) {
//					compartmentIterator.next().setSelected(bSelected);
                        ICompartment foundCompartment = compartmentIterator.next();
                        //for zonecompartments you have to go two levels deep...
                        if (foundCompartment  instanceof ISimpleListCompartment) {
                            ISimpleListCompartment foundListComp = (ISimpleListCompartment)foundCompartment;
                            Iterator <ICompartment> foundCompIter = foundListComp.getCompartments().iterator();
                            while (foundCompIter.hasNext()) {
                                ICompartment newFoundComp = foundCompIter.next();
                                if (newFoundComp != null) {
                                    newFoundComp.setSelected(bSelected);
                                }
                            }
                        } else {
                            foundCompartment.setSelected(bSelected);
                        }                        
                    }
                } else if (curObject instanceof IADCommentBodyCompartment) {
                    ((IADCommentBodyCompartment)curObject).setSelected(bSelected);
                } else if (curObject instanceof IADExpressionCompartment) {
                    ((IADExpressionCompartment)curObject).setSelected(bSelected);
                }
            }
        }

	/*
	 *  (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#getSelectedCompartments()
	 */
	public ETList <ICompartment> getSelectedCompartments() {
		if (this.getNumCompartments() == 0)
			return null;

		ETList <ICompartment> selectedCompartments = new ETArrayList <ICompartment>();
		Iterator iterator = this.getCompartments().iterator();
		while (iterator.hasNext()) {
			Object currObject = iterator.next();
			if (currObject instanceof ISimpleListCompartment) {
				ISimpleListCompartment listCompartment = (ISimpleListCompartment) currObject;

				Iterator < ICompartment > compartmentIterator = listCompartment.getCompartments().iterator();
				while (compartmentIterator.hasNext()) {
					ICompartment foundCompartment = compartmentIterator.next();
                                        //for zonecompartments you have to go two levels deep...
                                        if (foundCompartment  instanceof ISimpleListCompartment) {
                                            ISimpleListCompartment foundListComp = (ISimpleListCompartment)foundCompartment;
                                            Iterator <ICompartment> foundCompIter = foundListComp.getCompartments().iterator();
                                            while (foundCompIter.hasNext()) {
                                                ICompartment newFoundComp = foundCompIter.next();
                                                if (newFoundComp.isSelected()) {
                                                    selectedCompartments.add(newFoundComp);
                                                }
                                            }
                                        }
                                        else if (foundCompartment.isSelected()) {
						selectedCompartments.add(foundCompartment);
					}
				}
			}
                        else if (currObject instanceof IADCommentBodyCompartment) {
                            selectedCompartments.add((IADCommentBodyCompartment)currObject);
                        }
                        else if (currObject instanceof IADExpressionCompartment) {
                            if (((IADExpressionCompartment)currObject).isSelected())
                                selectedCompartments.add((IADExpressionCompartment)currObject);
                        }
		}
		return selectedCompartments;
	}

	/*
	 *  (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#getCompartmentAtPoint(org.netbeans.modules.uml.core.support.umlsupport.IETPoint)
	 */
	public ICompartment getCompartmentAtPoint(IETPoint pCurrentPos) {
		Iterator iterator = this.getCompartments().iterator();
		while (iterator.hasNext()) {
			Object curObject = iterator.next();
			if (curObject instanceof IListCompartment) {
				IListCompartment listCompartment = (IListCompartment) curObject;

				Iterator < ICompartment > compartmentIterator = listCompartment.getCompartments().iterator();
				while (compartmentIterator.hasNext()) {
					ICompartment foundCompartment = compartmentIterator.next();
					if (foundCompartment.isPointInCompartment(pCurrentPos)) {
						return foundCompartment;
					}
				}
			}
		}

		return null;
	}

	/*
	 * Returns the Init string.
	 */
	protected String getInitializationString() {
		IETGraphObjectUI parentUI = (IETGraphObjectUI) this.getParent();
		return parentUI != null ? parentUI.getInitStringValue() : null;
	}

	/* (non-Javadoc)
		* @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#affectModelElementDeletion()
		*/
	//Jyothi: modified this method to fix java.lang.IllegalStateException: Assertion failed. WindowsAPI is required to be called from AWT thread only
        // There a few NetBeans methods that require you to be in the AWT thread before you can execute the method.  
        //WindowManager.getDefault().findTopComponent("projectTabLogical_tc")  must be one of them.. so putting it in an awt thread.
        
	public void affectModelElementDeletion() {
            final IElement element1 = getFirstModelElement();
            if (element1 != null) {

                // workaround for #6286600
                if (element1 instanceof IClassifier) {

                    SwingUtilities.invokeLater( new Runnable() {
                        public void run() {
                            checkSelectedNodes((IClassifier) element1);
                        }
                    } );                        
                }                    
                element1.delete();                    
            }
    }

    private void checkSelectedNodes(IClassifier element) {
            TopComponent tc = WindowManager.getDefault().findTopComponent("projectTabLogical_tc");
            if (tc != null && (tc instanceof ExplorerManager.Provider)) {
                ExplorerManager em = ((ExplorerManager.Provider)tc).getExplorerManager();
                org.openide.nodes.Node[] nodes = em.getSelectedNodes();
                ArrayList list = new ArrayList(nodes.length);

                String qName = element.getQualifiedName();
                StringBuffer sf = new StringBuffer();
                StringTokenizer st = new StringTokenizer(qName, "::"); // NOI18N
                while(st.hasMoreTokens()) {
                    String str = st.nextToken();
                    if (st.hasMoreTokens()) {
                        sf.append(str);
                        sf.append('.');
                    }
                }
                String pkgName = sf.toString();
                if (pkgName.length() > 0) {
                    pkgName = pkgName.substring(0, pkgName.length() - 1);
                }

                String clsName = element.getName();
		/* NB60TBD
                org.openide.src.Identifier clsIdent = org.openide.src.Identifier.create(clsName, clsName);
                for (int x = 0; x < nodes.length; x++) {
                    if (!isUnderClass(pkgName, clsIdent, nodes[x])) {
                        list.add(nodes[x]);
                    }
                }
		*/ 
                try {
                    em.setSelectedNodes((org.openide.nodes.Node[])list.toArray(new org.openide.nodes.Node[list.size()]));
                } catch (java.beans.PropertyVetoException e) {
                }
            }
        }
        
	    /* NB60TBD
        private boolean isUnderClass(String pkgName, org.openide.src.Identifier clsName, org.openide.nodes.Node node) {
            SourceCookie cookie = null;
            while (node != null && cookie == null) {
                cookie = (SourceCookie)node.getCookie(SourceCookie.class);
                if (cookie == null) {
                    node = node.getParentNode();
                }
            }
            if (cookie != null) {
                SourceElement src = cookie.getSource();
                if (src != null) {
                    org.openide.src.Identifier pkgId = src.getPackage();
                    String elPkgName = pkgId != null ? pkgId.getFullName() : ""; // NOI18N
                    return pkgName.equals(elPkgName) && (src.getClass(clsName) != null);
                }
            }
            return false;
        }
	    */

	/* (non-Javadoc)
		* @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#anchorMouseEvent(org.netbeans.modules.uml.ui.support.viewfactorysupport.IMouseEvent, org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment)
		*/
	public long anchorMouseEvent(MouseEvent pMouseEvent, ICompartment pCompartment) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
		* @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#calculateOptimumSize(int, int, int, boolean)
		*/
	public IETSize calculateOptimumSize(IDrawInfo pDrawInfo, boolean bAt100Pct)
   {
      IETSize returnSize = new ETSize(0, 0);

      ETList < ICompartment > compartments = getCompartments();

      if (compartments != null && compartments.size() > 0) {

         ICompartment prevCompartment = null;
         IETSize prevSize = null;

         for (Iterator iter = compartments.iterator(); iter.hasNext();) {

            ICompartment curCompartment = (ICompartment) iter.next();

            if (curCompartment != null) {
               // Since the default assumption is that
               // all the compartments are stacked top to bottom,
               // set the logical offset with the left side being zero.
               IETPoint pointLogicalOffset = new ETPoint(0, returnSize.getHeight());
               curCompartment.setLogicalOffsetInDrawEngineRect(pointLogicalOffset);

               // Make sure all the but last compartment have a fixed height.
               if ((prevCompartment != null) && (prevSize != null)) {
                  prevCompartment.setTransformSize(ICompartment.EXPAND_TO_NODE, prevSize.getHeight());
               }

               prevSize = curCompartment.calculateOptimumSize(pDrawInfo, true);
               if (prevSize != null) {
                  final int maxWidth = returnSize.getWidth();

                  returnSize.setSize(Math.max(maxWidth, prevSize.getWidth()), returnSize.getHeight() + prevSize.getHeight());
               }
            }

            prevCompartment = curCompartment;
         }
      }
      
      return bAt100Pct || returnSize == null ? returnSize : scaleSize(returnSize, pDrawInfo != null ? pDrawInfo.getTSTransform() : getTransform());
	}

	/* (non-Javadoc)
		* @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#clone(org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine)
		*/
	public Object clone() {
		try {
			IDrawEngine de = ETDrawEngineFactory.createDrawEngine(this.getClass().getName());
			if (de != null)
				de.copy(this);

			return de;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

   public boolean copy(IDrawEngine pConstDrawEngine)
   {
      if (pConstDrawEngine == null)
         return false;

      m_parentUI = (IETGraphObjectUI)pConstDrawEngine.getParent().clone();
      m_LastResizeOriginator = ((ETDrawEngine)pConstDrawEngine).m_LastResizeOriginator;

      //		if (pConstDrawEngine instanceof ETDrawEngine) {
      //			ETDrawEngine pETConstDrawEngine = (ETDrawEngine) pConstDrawEngine;
      //			// m_defaultCompartment = pETConstDrawEngine.m_defaultCompartment.clone();
      //			// m_defaultCompartment = pETConstDrawEngine.m_anchoredCompartment.clone();
      //		}

      return true;
   }

	/**
		* The rectangle used for last drawing operation, in logical coordinates.
		*
		* @param bIncludeLabels When true, include the bounding rectangles for all 
		*                       the labels as well
		* @return The rectangle used for last drawing operation, in logical 
		*         coordinates
		* @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#getLogicalBoundingRect(org.netbeans.modules.uml.core.support.umlsupport.IETRect, boolean)
		*/
	public IETRect getLogicalBoundingRect(boolean bIncludeLabels) {
		IETRect rectLogicialBounding = getLogicalBoundingRect();
		IETRect retVal = rectLogicialBounding;
		if (rectLogicialBounding != null && bIncludeLabels) {
			ILabelManager labelManager = getLabelManager();
			if (labelManager != null) {
				IETRect labelRect = labelManager.getLogicalBoundingRectForAllLabels();
				if (labelRect != null && !labelRect.isZero()) {
					retVal = RectConversions.unionTSCoordinates(rectLogicialBounding, labelRect);
				}
			}
		}

		return retVal;
	}

	/* (non-Javadoc)
		* @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#delayedDeleteAndReinitializeAllLabels()
		*/
	public void delayedDeleteAndReinitializeAllLabels() {

	}

   /* (non-Javadoc)
      * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#delayedSizeToContents()
      */
   public void delayedSizeToContents()
   {
      sizeToContents();
   }

	/* (non-Javadoc)
		* @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#displayColorDialog(int, int)
		*/
	public boolean displayColorDialog(int nKind, int pCOLORREF) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
		* @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#displayFontDialog(int, java.lang.Object, boolean)
		*/
	public void displayFontDialog(int pCOLORREF, Object pUserSelectedFont, boolean bUserSelectedFontOrColor) {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
		* @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#doDraw(org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo)
		*/
	public abstract void doDraw(IDrawInfo pDrawInfo);

	/* (non-Javadoc)
		* @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#findCompartment(org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment)
		*/
	public boolean findCompartment(ICompartment pCompartment) {
		return getCompartments().contains(pCompartment);
	}

	/* (non-Javadoc)
		* @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#findCompartmentByCompartmentID(java.lang.String)
		*/
	public ICompartment findCompartmentByCompartmentID(String sCompartmentID)
        {
            ICompartment retVal = null;
            
            for(ICompartment compartment : getCompartments())
            {
                if(sCompartmentID.equals(compartment.getCompartmentID()) == true)
                { 
                    retVal = compartment;
                    break;
                }
            }
            
            return retVal;
        }

	/* (non-Javadoc)
		* @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#findCompartmentByTitle(java.lang.String)
		*/
	public ICompartment findCompartmentByTitle(String sName) {

		ICompartment retValue = null;
				
		Iterator<ICompartment> iterator = getCompartments().iterator();
		
		while (iterator.hasNext()) {
			
			ICompartment curObject = iterator.next();
			ICompartment compartment = curObject;
			
			if (compartment != null && compartment.getName().equals(sName))
			{
				retValue = curObject;
				break;
			}
		}
	
		return retValue;
	}

	/* (non-Javadoc)
		* @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#findCompartmentContainingElement(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement)
		*/
	public ICompartment findCompartmentContainingElement(IElement pElement) {

		ICompartment retValue = null;
		
		Iterator<ICompartment> iterator = getCompartments().iterator();
		while (iterator.hasNext())
      {
			ICompartment curObject = iterator.next();

         if (curObject instanceof ISimpleListCompartment)
         {
            ISimpleListCompartment listCompartment = (ISimpleListCompartment)curObject;
            ICompartment found = listCompartment.findCompartmentContainingElement( pElement );
            if( found != null )
            {
               retValue = found;
               break;
            }
         }         
         else
         {
            IElement compartmentElement = curObject.getModelElement();
            if (compartmentElement != null && compartmentElement.isSame(pElement))
            {
               retValue = curObject;
               break;
            }
         }
		}
		return retValue;
	}

	/* (non-Javadoc)
		* @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#findListCompartmentContainingCompartment(org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment)
		*/
	public IListCompartment findListCompartmentContainingCompartment(ICompartment pCompartment) {

		IListCompartment pListCompartment = null;

		ICompartment pCompartmentToFind = pCompartment;

		IElement pElement = null;

		pElement = pCompartmentToFind.getModelElement();

		if (pElement != null) {
			return findListCompartmentContainingElement(pElement);
		}

		Iterator < ICompartment > iterator = getCompartments().iterator();

		while (iterator.hasNext()) {

			ICompartment compartment = iterator.next();

			if (compartment != null && compartment instanceof IListCompartment) {

				if (compartment == pCompartmentToFind) {
					pListCompartment = (IListCompartment) compartment;
					break;
				}

				boolean bFound = ((ETListCompartment) compartment).findCompartment(pCompartmentToFind);

				if (bFound) {
					pListCompartment = (IListCompartment) compartment;
					break;
				}
			}

		}
		return pListCompartment;
	}

	public IListCompartment findListCompartmentContainingElement(IElement pElement) {

		IListCompartment pListCompartment = null;

		String sElementID = pElement.getXMIID();

		// check if this compartment already exists

		Iterator < ICompartment > iterator = getCompartments().iterator();



		while (iterator.hasNext()) {

			ICompartment pTestCompartment = iterator.next();

			if (pTestCompartment instanceof ETListCompartment) {
				ICompartment pFound = ((ETListCompartment)pTestCompartment).findCompartmentContainingElement(pElement);
				if (pFound != null) {
					pListCompartment = (IListCompartment) pTestCompartment;
					break;
				}
			}
		}

		return pListCompartment;
	}

	/* (non-Javadoc)
		* @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#getAnchoredCompartment()
		*/
	public ICompartment getAnchoredCompartment() {
		return this.m_anchoredCompartment;
	}

	/* (non-Javadoc)
		* @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#getBoundingRect()
		*/
	public IETRect getBoundingRect() {
		IETRect retVal = super.getBoundingRect();
                if (retVal != null)
                {
                    retVal.normalizeRect();
                }

		return retVal;
	}

	/* (non-Javadoc)
		* @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#getCheckSyncStateDuringDraw()
		*/
	public boolean getCheckSyncStateDuringDraw() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
		* @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#getDefaultCompartment()
		*/
	public ICompartment getDefaultCompartment() {
		return this.m_defaultCompartment;
	}

	/* (non-Javadoc)
		* @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#getDiagram()
		*/
	public IDiagram getDiagram() {
		return TypeConversions.getDiagram(this);
	}

	/* 
	* should be overwritten in derived classes
	*/
	abstract public String getDrawEngineID();

	/**
		* This is the string to be used when looking for other similar drawengines.
		*
		* @param sID [out,retval] The unique engine identifier
		*/
	public String getDrawEngineMatchID() {
		// The match is based on draw engine and element types
		String engineType = getDrawEngineID();
		IElement modEle = getFirstModelElement();
		String elemType = modEle != null ? modEle.getElementType() : ""; 

		return engineType + "," + elemType;
	}

	/**
	 * Retrieves the event manager that the draw engine uses.  The event manager
	 * that is used is determined by the return value of getManagerType().  
	 * 
	 * @see #getManagerType(int)
		* @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#getEventManager()
		*/
	public IEventManager getEventManager() {
		if (m_EventManager == null) {
			String eventManagerType = getManagerMetaType(MK_EVENTMANAGER);
			if ((eventManagerType != null) && (eventManagerType.length() > 0)) {
				m_EventManager = DrawingFactory.retrieveEventManager(eventManagerType);
				setManagerBackpointer(m_EventManager);
			}
		}

		return m_EventManager;
	}

	/**
	 * Returns the metatype of the manager we should use.  This implementation 
	 * always returns an empty string.
	 *
	 * @return The metatype in essentialconfig.etc that defines the label manager
	 * @param managerType The type of manager.
	 */
	public String getManagerMetaType(int nManagerKind) {
		return "";
	}

	/* (non-Javadoc)
		* @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#getIsGraphicalContainer()
		*/
	public boolean getIsGraphicalContainer() {
		return false;
	}

   /**
    Retrieves the smallest node draw engine that graphically contains this draw engine
    */
   public IDrawEngine getGraphicalContainer()
   {
      if (m_bActiveLayout || m_bFindNewContainer)
      {
         // Assume this draw engine is not contained
         m_containerDE = null;

         // Retrieve all the node presentation elements (PEs) on this PE's diagram
         ETList < IPresentationElement > allNodePEs = null;
         {
            IDrawingAreaControl control = getDrawingArea();
            if (control != null)
            {
               allNodePEs = control.getAllNodeItems( null );
            }
         }

         if ( allNodePEs != null )
         {
            // Remove this node from the list of all nodes
            {
               IPresentationElement thisPE = getPresentationElement();
               // When the node is 1st created it is possible for this PE to be null
               if ( thisPE != null )
               {
                  allNodePEs.removeItem( thisPE );
               }
            }

            final IETRect rectThisBounding = getLogicalBoundingRect();

            float fMinContainerArea = Float.MAX_VALUE;

            for (Iterator iter = allNodePEs.iterator(); iter.hasNext();)
            {
               IPresentationElement pe = (IPresentationElement)iter.next();
               
               IDrawEngine engine = TypeConversions.getDrawEngine( pe );
               if ( engine != null )
               {
                  if ( engine.getIsGraphicalContainer() )
                  {
                     final IETRect rectEngine = TypeConversions.getLogicalBoundingRect( engine );
                     if (rectEngine.contains(rectThisBounding))
                     {
                        // The true container is the container with the smallest area
                        float fEngineArea = (float)rectEngine.getWidth() * (float)rectEngine.getHeight();
                        assert(fEngineArea > 0);

                        if (fEngineArea < fMinContainerArea)
                        {
                           fMinContainerArea = fEngineArea;

                           m_containerDE = engine;
                        }
                     }
                  }
               }
            }
         }
      }

      m_bFindNewContainer = false;
      
      return m_containerDE;
   }

   /**
    * Clears the member variable that retains the graphical container
    */
   public void resetGraphicalContainer()
   {
      m_bFindNewContainer = true;
   }

	protected ILabelManager createLabelManager()
	{
		String sLabelManagerType = getManagerMetaType(MK_LABELMANAGER);
		ILabelManager labelManager  = null;
		
		// If we have a label manager type now go ahead and create it
		if (sLabelManagerType != null && sLabelManagerType.length() > 0) {

			String sForcedStereotypeText = getForcedStereotypeText();
			labelManager = DrawingFactory.retrieveLabelManager(sLabelManagerType);

			if (labelManager != null && sForcedStereotypeText != null && sForcedStereotypeText.length() > 0) {

				// If we're forcing the stereotype text then do that here.
				labelManager.setForcedStereotypeString(sForcedStereotypeText);
			}

			if (labelManager != null) {
				setManagerBackpointer(labelManager);
			}
		}

		return labelManager;
	}
	
	/* (non-Javadoc)
		* @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#getLabelManager()
		*/
	public ILabelManager getLabelManager() {

		if (m_LabelManager == null) 
		{
			m_LabelManager = createLabelManager(); 
			
		} else{
			// Refresh the forced stereotype
			m_LabelManager.setForcedStereotypeString(getForcedStereotypeText());
		} 

		return m_LabelManager;

	}

	protected String getForcedStereotypeText() {
		return "";
	}

	/* (non-Javadoc)
		* @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#getLastDrawPointY()
		*/
	public int getLastDrawPointY() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
		* @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#getOLEDragElements()
		*/
	public IElement[] getOLEDragElements() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
		* @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#getParent()
		*/
	public IETGraphObjectUI getParent() {
		return m_parentUI;
	}

	/* (non-Javadoc)
		* @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#getParentETElement()
		*/
	public ITSGraphObject getParentETElement() {
      IETGraphObjectUI ui = getParent();
      return ui != null && ui.getOwner() instanceof ITSGraphObject ? (ITSGraphObject) ui.getOwner() : null;
	}

	/* (non-Javadoc)
		* @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#getWindow()
		*/
	public int getWindow() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
		* @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#handleAccelerator(int)
		*/
	public boolean handleAccelerator(String accelerator) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
		* @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#handleSetCursor(org.netbeans.modules.uml.ui.support.viewfactorysupport.ISetCursorEvent)
		*/
	public boolean handleSetCursor( ISetCursorEvent event )
   {
      boolean bHandled = false;

      // this is the relative position within the node (topleft = 0,0)
      IETPoint point = getWinScaledOwnerCursorPosition( event );

      final int numCompartments = getNumVisibleCompartments();
      for (int i = 0 ; (bHandled == false) && (i < numCompartments) ; i++)
      {
         ICompartment compartment = getVisibleCompartment( i );
         if ( compartment != null )
         {
            bHandled = compartment.handleSetCursor( point, event );
         }
      }
      
      return bHandled;
	}

	/* (non-Javadoc)
		* @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#init()
		*/
	public void init() throws ETException {
		if (TypeConversions.getElement(this) != null) {
			IPresentationElement pPE = getPresentationElement();
			if (pPE != null) {
				this.clearCompartments();
				this.createCompartments();
				this.initCompartments(pPE);
			}

			if (this.m_bInitResources == false) {
				this.initResources();
			}
			this.sizeToContents();
		}
	}

	public void initCompartments(IPresentationElement pElement) {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
		* @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#initResources()
		*/
	public void initResources() {
		if (m_bInitResources == false) {
			m_bInitResources = true;
		}
	}

	/*
	 *  (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#invalidate()
	 */
	public long invalidate() {
		return invalidateRect(getLogicalBoundingRect(true));
	}

	/*
	 *  (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#invalidateRect(org.netbeans.modules.uml.core.support.umlsupport.IETRect)
	 */
	public long invalidateRect(IETRect rect) {
		if (rect != null)
		{
			IDrawingAreaControl control = getDrawingArea();
			if (control != null && control.getGraphWindow() != null)
			{
				control.refreshRect( rect ); 
			} 
		}
		return 0;
	}

	/* (non-Javadoc)
		* @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#layout()
		*/
	public void layout()
   {
      // create a draw info
      // One reason for not getting a draw info here it is because the window has closed.
      IDrawInfo drawInfo = getParent().getDrawInfo();
      if( drawInfo != null )
      {
         IETSize size = calculateOptimumSize( drawInfo, true );

         // Make sure the compartment, and its pieces are laid out properly
         IETRect rectEngineBounding = getLogicalBoundingRect();

         IETRect rectCompartmentInDE = new ETRect( 0, 0,
                                                   rectEngineBounding.getWidth(),
                                                   rectEngineBounding.getHeight() );

         ETList< ICompartment > compartments = getCompartments();
         for (Iterator< ICompartment > iter = compartments.iterator(); iter.hasNext();)
         {
            ICompartment compartment = iter.next();
         
            IETPoint ptOffset = compartment.getLogicalOffsetInDrawEngineRect();
         
            rectCompartmentInDE.setLeft( ptOffset.getX() );
            rectCompartmentInDE.setTop( ptOffset.getY() );
         
            compartment.layout( rectCompartmentInDE );
         }
      }
	}

	/* (non-Javadoc)
		* @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#modelElementDeleted(org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets)
		*/
	public long modelElementDeleted(INotificationTargets pTargets) {
		int count = getNumCompartments();
		for (int i = 0; i < count; i++) {
			ICompartment pComp = getCompartment(i);
			if (pComp != null) {
				pComp.modelElementDeleted(pTargets);
			}
		}
		return 0;
	}

	/**
		* Notifier that the model element has changed, if available the changed IFeature is passed along.
		*
		* @param pTargets[in] Information about what has changed
		*/
	public long modelElementHasChanged(INotificationTargets pTargets) {
		//dispatch model element has changed to compartments
		Iterator<ICompartment> iterator = this.getCompartments().iterator();
		while (iterator.hasNext()){
			iterator.next().modelElementHasChanged(pTargets);
		}
		return 0;
	}

	/* (non-Javadoc)
		* @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#nodeResized(int)
		*/
	public void onResized() {
		
	  m_LastResizeOriginator = TSE_NODE_RESIZE_ORIGINAL;
	}

	/* (non-Javadoc)
		* @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#onCompartmentCollapsed(org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment, boolean)
		*/
	public long onCompartmentCollapsed(ICompartment pCompartment, boolean bCollapsed) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
		* @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#onContextMenu(org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu, int, int)
		*/
	public long onContextMenu(IProductContextMenu pContextMenu, int logicalX, int logicalY) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
		* @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#onContextMenuHandleSelection(org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu, org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem)
		*/
	public long onContextMenuHandleSelection(IProductContextMenu pContextMenu, IProductContextMenuItem pMenuItem) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
		* @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#onGraphEvent(int)
		*/
	public void onGraphEvent(int nKind)
   {
		Iterator<ICompartment> iterator = this.getCompartments().iterator();
		while (iterator.hasNext()){
			iterator.next().onGraphEvent(nKind);
	    }

      switch( nKind )
      {
      case IGraphEventKind.GEK_PRE_MOVE:
      case IGraphEventKind.GEK_PRE_RESIZE:
      case IGraphEventKind.GEK_PRE_LAYOUT:
         m_bActiveLayout = true;
         break;

      case IGraphEventKind.GEK_POST_MOVE:
      case IGraphEventKind.GEK_POST_RESIZE:
      case IGraphEventKind.GEK_POST_LAYOUT:
         m_bActiveLayout = false;
         resetGraphicalContainer();
         break;
      }
	}

	/* (non-Javadoc)
		* @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#onKeydown(int, int)
		*/
	public boolean onKeydown(int nKeyCode, int nShift) {
       //dispatch the key down event to all compartments
		return dispatchKeyDownToCompartments(nKeyCode, nShift);
	}

	private boolean dispatchKeyDownToCompartments(int nKeyCode, int nShift) {
            boolean handled = false;
            int count = getNumVisibleCompartments();
            for (int i = count-1; i >=0; i--) {
                ICompartment pComp = getVisibleCompartment(i);
                if (pComp != null) {
                    handled = pComp.handleKeyDown(nKeyCode, nShift);
                    if (handled) {
                        break;
                    }
                }
            }
            
            //Jyothi: a11y work - cycle thru compartments when shift-RightArrow/LeftArrow is pressed
            if (!handled && nShift == 0 && (nKeyCode==KeyEvent.VK_RIGHT || nKeyCode==KeyEvent.VK_LEFT)) {
                ICompartment pComp = null;
                int selectableCompCount = getNumSelectableCompartments();
                for (int i=0; i<selectableCompCount; i++) {
                    pComp = getSelectableCompartment(compIndex);
                    
                    if (pComp != null) {
                        //Fixed issue 83794. Disabled the ability to select a
                        // TemplateParametersCompartment for editing.
                        if (pComp instanceof ITemplateParametersCompartment){
                            // skip ITemplateParametersCompartment compartment and
                            // process the next compartment instead.
                            if (nKeyCode==KeyEvent.VK_RIGHT){
                                // Advance the compartment index to the next compartment
                                compIndex = compIndex +1;
                                if (compIndex >= selectableCompCount)  compIndex = 0;
                            }else if (nKeyCode==KeyEvent.VK_LEFT) {
                                compIndex = compIndex -1;
                                if (compIndex < 0) compIndex = selectableCompCount-1;
                            }
                            pComp = getSelectableCompartment(compIndex);
                        }
                        //1. deselect all selected compartment
                        selectAllCompartments(false);
                        //2. select this compartment
                        pComp.setSelected(true);
                        
                        if (nKeyCode==KeyEvent.VK_RIGHT) {
                            // Advance the compartment index to the next compartment
                            compIndex = compIndex +1;
                            if (compIndex >= selectableCompCount)  compIndex = 0;
                        } else if (nKeyCode==KeyEvent.VK_LEFT) {
                            compIndex = compIndex -1;
                            if (compIndex < 0) compIndex = selectableCompCount-1;
                        }
                        invalidate();
                        getDrawingArea().refresh(true);
                        handled = true;
                        break;
                    } else {
//                            System.err.println(" pComp is null! ");
                    }
                    
                    if (handled)
                        break;
                }
                
            } //end of if(nshift==0)
            
            ICompartment oldDefault = null;
            //if nobody handled, pass on to the default compartment
            if (!handled && m_defaultCompartment == null) {
                for (int i = 0; i < count; i++) {
                    ICompartment pComp = getVisibleCompartment(i);
                    if (pComp != null) {
                        oldDefault = getDefaultCompartment();
                        setDefaultCompartment(pComp.getDefaultCompartment());
                        break;
                    }
                }
            }
            
            // leave it for ADDrawingAreaControl to handle
            if (!handled && nKeyCode == KeyEvent.VK_DELETE)
                return false;
            if (!handled && m_defaultCompartment != null) {
                // save selection state
                boolean selected = m_defaultCompartment.isSelected();
                // save anchored ref
                ICompartment oldAnchored = getAnchoredCompartment();
                
                // select compartment so the HandleKeyDown logic fires
                m_defaultCompartment.setSelected(true);
                handled = m_defaultCompartment.handleKeyDown(nKeyCode, nShift);
                
                // if not handled and the default wasn't selected, leave it unselected
                // KSL - we were seeing a situation where dropping a new class, then using auto-type to edit
                // the class name would leave the class name selected.  This was b/c the call to HandleKeyDown()
                // was returning *bHandled = true.  so we now just test if the default compartment was initially
                // unselected, to leave it unselected.  I'm leaving the commented out code in until we verify
                // no other types depend on this behaviour.
                if (!selected) {
                    m_defaultCompartment.setSelected(false);
                }
                setAnchoredCompartment(oldAnchored);
                if (!handled && oldDefault != null) {
                    setDefaultCompartment(oldDefault);
                }
            }
            return handled;
        }

	/* (non-Javadoc)
		* @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#onCharTyped(char)
		*/
	public boolean onCharTyped(char ch) {
		boolean handled = false;
		int count = getNumVisibleCompartments();
		for (int i = count-1; i >=0; i--) {
			ICompartment pComp = getVisibleCompartment(i);
			if (pComp != null) {
				handled = pComp.handleCharTyped(ch);
				if (handled) {
					break;
				}
			}
		}

		//if nobody handled, pass on to the default compartment
		if (!handled && m_defaultCompartment == null) {
			for (int i = 0; i < count; i++) {
				ICompartment pComp = getVisibleCompartment(i);
				if (pComp != null) {
					setDefaultCompartment(pComp.getDefaultCompartment());
					break;
				}
			}
		}

		if (!handled && m_defaultCompartment != null) {
			// save selection state
			boolean selected = m_defaultCompartment.isSelected();

			// select compartment so the HandleKeyDown logic fires
			m_defaultCompartment.setSelected(true);
			handled = m_defaultCompartment.handleCharTyped(ch);

			if (!selected) {
				m_defaultCompartment.setSelected(false);
			}
		}
		return handled;
	}


	/**
	 * Returns the visible compartment at the argument index.
	 *
	 * @param index [in] The index into the list of the draw engines visible compartments
	 * @param pCompartment [out,retval] The compartment at this index, otherwise NULL
	 */
	protected ICompartment getVisibleCompartment(int index) {
		ICompartment retObj = null;
		int count = getNumCompartments();
		int visiIndex = 0;
		for (int i = 0; i < count; i++) {
			ICompartment pComp = (ICompartment) m_compartments.get(i);
			if (pComp != null && pComp.getVisible()) {
				if (index == visiIndex) {
					retObj = pComp;
					break;
				}
				visiIndex++;
			}
		}
		return retObj;
	}

	/* (non-Javadoc)
		* @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#onKeyup(int, int)
		*/
	public boolean onKeyup(int KeyCode, int Shift) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
		* @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#performDeepSynch()
		*/
	public long performDeepSynch()
	{
		try
		{
			IPresentationElement pPE = getPresentationElement();
			if (pPE != null)
			{
            // Fix J1255:  Clearing and initializing the compartments resets the 
            //             divider data for the zones compartment.
            //             So, we don't want that to happen.
//				this.clearCompartments();
//				this.createCompartments();
				this.initCompartments(pPE);
			}
		}
		catch(Exception e)
		{
		}
		return 0;
	}

	/* (non-Javadoc)
		* @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#postLoad()
		*/
	public long postLoad()
   {
      ETList< ICompartment > compartments = getCompartments();
      for (Iterator iter = compartments.iterator(); iter.hasNext();)
      {
         ICompartment compartment = (ICompartment)iter.next();
         
         compartment.postLoad();
      }
      
      return 0;
	}

	/* (non-Javadoc)
		* @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#preHandleNameCollision(org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment, org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement, org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement)
		*/
	public boolean preHandleNameCollision(ICompartment pCompartmentBeingEdited, INamedElement pElement, INamedElement pFirstCollidingElement) {
		return true;
	}

	/* (non-Javadoc)
		* @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#queryToolTipData(org.netbeans.modules.uml.ui.support.viewfactorysupport.IToolTipData)
		*/
	public long queryToolTipData(IToolTipData pToolTipData) {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
		* Restore draw engine presentation attributes from the product archive.
		*
		* @param pProductArchive [in] The archive we're reading from
		* @param pEngineElement [in] The element where this draw engine's information should exist
		*/
	public long readFromArchive(IProductArchive pProductArchive, IProductArchiveElement pParentElement) {
		// perform any special read here for the engine.  Derived draw engines should perform their
		//  read before calling this base class.
		clearCompartments();

		// If hr is 0x80040111 then that means that the essential config does not contain
		// a compartment that is in the diagram file.  The drawengine should fix when
		// the draw happens because it's there we check to make sure the drawengine has at least
		// one compartment.
		DrawingFactory.createCompartments(this, pProductArchive, pParentElement);

		// after compartments are created we init resources
		initResources();

		// read any custom overrides now, after all default initialization
		m_ResourceUser.readResourcesFromArchive(pProductArchive, pParentElement);

		// Put the compartment reloading of resources in another try block so incase we throw
		// we don't leave static compartments in memory
		try {
			// Tell the compartments to read their resources from archive
			DrawingFactory.readCompartmentResourcesFromArchive(pProductArchive);

			// We should be fully loaded by now so draw a red border if we still can't get our
			// model element.
			setCheckSyncStateDuringDraw(true);
		} catch (Exception e) {
		}
		return 0;
	}

	/*
	 *  (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#reinitCompartments(org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement)
	 */
	public long reinitCompartments(IPresentationElement pElement) {
		// TODO Auto-generated method stub
		return 0;
	}


	/* (non-Javadoc)
		* @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#selectExtendCompartments(org.netbeans.modules.uml.ui.support.viewfactorysupport.IMouseEvent)
		*/
	public void selectExtendCompartments(MouseEvent pEvent) {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
		* @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#setAnchoredCompartment(org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment)
		*/
	public long setAnchoredCompartment(ICompartment pCompartment) {
		this.m_anchoredCompartment = pCompartment;
		return 0;
	}

	/* (non-Javadoc)
		* @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#setCheckSyncStateDuringDraw(boolean)
		*/
	public void setCheckSyncStateDuringDraw(boolean value) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
		* @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#setDefaultCompartment(org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment)
		*/
	public void setDefaultCompartment(ICompartment pCompartment) {
		this.m_defaultCompartment = pCompartment;
	}

	/*
	 *  (non-Javadoc)
	 * set isDirty to true when there is data that needs to be saved
	 */
	public long setIsDirty() {
		IDrawingAreaControl control = getDrawingArea();
		if (control != null) {
			control.setIsDirty(true);
		}
		return 0;
	}

	/* (non-Javadoc)
		* @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#setLastDrawPointY(int)
		*/
	public void setLastDrawPointY(int i) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
		* @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#setParent(com.tomsawyer.editor.TSEObjectUI)
		*/
	public void setParent(IETGraphObjectUI pParent) {
		this.m_parentUI = pParent;

		// Release the transform 'cause it's now pointing at a different object
		this.clearTransformOwner();

		// Clear the resource manager, which is indexed by our diagram
		//ClearResourceManager();
	}

	/* (non-Javadoc)
		* @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#getReadOnly()
		*/
	public boolean getReadOnly() {
		return m_readOnly;
	}

	/* (non-Javadoc)
		* @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#setReadOnly(boolean)
		*/
	public void setReadOnly(boolean value) {
		m_readOnly = value;
	}

	/* (non-Javadoc)
		* @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#setSensitivityAndCheck(org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu, org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem, int)
		*/
	public boolean setSensitivityAndCheck(IProductContextMenu pContextMenu, IProductContextMenuItem pMenuItem, int buttonKind) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
		* @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#setupOwner()
		*/
	public void setupOwner() {
		// TODO Auto-generated method stub
	}

   /* (non-Javadoc)
      * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#sizeToContents()
      */
   public void sizeToContents()
   {
   }

	/* (non-Javadoc)
		* @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#updateColorPreferenceToCurrent(int)
		*/
	public void updateColorPreferenceToCurrent(int nKind) {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
		* @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#updateLastDrawPointY(double)
		*/
	public void updateLastDrawPointY(double d) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
		* @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#validateNode()
		*/
	public boolean validateNode() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
		* @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#validateResize(int, int)
		*/
	public Dimension validateResize(int x, int y) {
		return new Dimension(x, y);
	}

	/**
		* Saves the draw engine and compartment stuff to the product archive.
		*
		* @param pProductArchive [in] The archive we're saving to
		* @param pElement [in] The current element, or parent for any new attributes or elements
		*/
	public long writeToArchive(IProductArchive pProductArchive, IProductArchiveElement pElement) {
		if (pProductArchive != null && pElement != null) {
			//  if none of the compartments have anything to save then no reason to save this engine
			String engName = getDrawEngineID();
			if (engName != null && engName.length() > 0) {
				IProductArchiveElement engEle = pElement.createElement(IProductArchiveDefinitions.ENGINENAMEELEMENT_STRING);
				if (engEle != null) {
					engEle.addAttribute(IProductArchiveDefinitions.ENGINENAMEATTRIBUTE_STRING, engName);
					writeResourcesToArchive(pProductArchive, engEle);
					
					m_ResourceUser.writeResourcesToArchive( pProductArchive, engEle);

					// Now create a element based on each compartment and write out each compartment
					int count = getNumCompartments();
					for (int i = 0; i < count; i++) {
						// Tell each compartment to write to the archive
						ICompartment pCompartment = getCompartment(i);
						if (pCompartment != null) {
							pCompartment.writeToArchive(pProductArchive, engEle);
						}
					}
				}
			}
		}
		return 0;
	}

	/**
		 * @param pProductArchive
		 * @param engEle
		 */
	private void writeResourcesToArchive(IProductArchive pProductArchive, IProductArchiveElement engEle) {
		// TODO Auto-generated method stub

	}

	// Abstract mouse event handlers.
	public abstract boolean handleLeftMouseButton(MouseEvent pEvent);
	public abstract boolean handleLeftMouseButtonDoubleClick(MouseEvent pEvent);
	public abstract boolean handleRightMouseButton(MouseEvent pEvent);
	public abstract boolean handleLeftMouseBeginDrag(IETPoint pStartPos, IETPoint pCurrentPos);
	public abstract boolean handleLeftMouseDrag(IETPoint pStartPos, IETPoint pCurrentPos);
	public abstract boolean handleLeftMouseDrop(IETPoint pCurrentPos, List pElements, boolean bMoving);

	public boolean handleLeftMouseButtonPressed(MouseEvent pEvent) {
		return false;
	}

	public abstract String getPresentationType();

	/* (non-Javadoc)
		* @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#getUI()
		*/
	public IETGraphObjectUI getUI() {
		return getParent();
	}

	/*
	 *  (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#getPresentation()
	 */
	public IGraphPresentation getPresentation() {
		IETGraphObjectUI ui = getUI();
		if (ui != null && ui.getTSObject() instanceof IETGraphObject) {
			return (IGraphPresentation) ((IETGraphObject) ui.getTSObject()).getPresentationElement();
		}
		return null;
	}

	/**
		* Returns the node's Presentation Element.
		*
		* @param pElement [out,retval] The presentation element that represents this edge draw engine
		*/
	protected IPresentationElement getPresentationElement() {
		IETGraphObject pETElement = (IETGraphObject) getParentETElement();
		return pETElement != null ? pETElement.getPresentationElement() : null;
	}

	/*
		protected IDrawingFactory getPresentationFactory()
		{
			if (m_presentationFactory == null){
				m_presentationFactory = new DrawingFactory();
			}
			return m_presentationFactory;
		}
	*/

	/* (non-Javadoc)
		* @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#getDrawingArea()
		*/
	public IDrawingAreaControl getDrawingArea() {
		return getParent() != null ? getParent().getDrawingArea() : null;
	}

	/*
	 * 
	 */
	public String getTaggedValuesText(IElement pElement) {
		String retValue = null;
		if (pElement != null) {
			ETList < ITaggedValue > taggedValues = pElement.getAllTaggedValues();
			if (taggedValues != null && taggedValues.size() > 0) {
				String finalName = "{";
				int count = taggedValues.size();
				for (int i = 0; i < count; i++) {
					ITaggedValue taggedValue = taggedValues.get(i);
					if (taggedValue != null) {
						String name = taggedValue.getName();
						String value = taggedValue.getDataValue();

						//throw out the documentation property
						if (name.toLowerCase().equals("documentation"))
							continue;

						if (i > 0) {
							finalName += ",";
						}
						finalName += name;
						finalName += "=";
						finalName += value;
					}
				}
				finalName += "}";
				retValue = finalName;
			}
		}
		return retValue;
	}

	public void onContextMenu(IMenuManager manager) {
	}

	/**
	 * Adds the menu items that are unique for packages.
	 *
	 * @param pContextMenu[in] The context menu about to be displayed
	 */
	public void addCustomizeMenuItems(IMenuManager manager) {
		IMenuManager subMenu = manager.createOrGetSubMenu(loadString("IDS_COMPARTMENT_TITLE"), "org.netbeans.modules.uml.view.drawingarea.layout.popup");
		if (subMenu != null) {
			addSeparatorMenuItem(subMenu);
			subMenu.add(createMenuAction(loadString("IDS_POPUPMENU_CUSTOMIZE"), "MBK_CUSTOMIZE"));
			//manager.add(subMenu);
		}
	}

	/**
	 * Adds Interface Edge specific stuff.
	 *
	 *  Link Type ->Aggregation
	 *              Association
	 *  Link End  ->Navigable
	 *
	 * @param pContextMenu
	 */
	public void addInterfaceEdgeMenuItems(IMenuManager manager) {
		IMenuManager subMenu = manager.createOrGetSubMenu(loadString("IDS_LABELS_TITLE"), "org.netbeans.modules.uml.view.drawingarea.layout.popup");
		if (subMenu != null) {
			addSeparatorMenuItem(subMenu);
			subMenu.add(createMenuAction(loadString("IDS_SHOW_INTERFACENAME"), "MBK_SHOW_INTERFACENAME"));
			//manager.add(subMenu);
		}
	}

	/**
	 * Adds the name label if the edge has a name.
	 *
	 * @param pEngine[in] The controlling draw engine
	 * @param pContextMenu[in] The context menu about to be displayed
	 */
	public void addNameLabelPullright(IDrawEngine pEngine, IMenuManager manager) {
		ILabelManager labelMgr = pEngine.getLabelManager();
		// See if the name is a valid label kind
		boolean isValid = labelMgr != null ? labelMgr.isValidLabelKind(TSLabelKind.TSLK_NAME) : false;
		
		if (isValid) {
			IMenuManager subMenu = manager.createOrGetSubMenu(loadString("IDS_LABELS_TITLE"), "org.netbeans.modules.uml.view.drawingarea.layout.popup");
			if (subMenu != null) {
				subMenu.add(createMenuAction(loadString("IDS_NAME_LABEL"), "MBK_SHOW_NAME_LABEL"));
				//manager.add(subMenu);
			}
		}
	}

	/**
	 * Adds the stereotype label if the edge has a stereotype.
	 *
	 * @param pEngine[in] The controlling draw engine
	 * @param pContextMenu[in] The context menu about to be displayed
	 */
	public void addStereotypeLabelPullright(IDrawEngine pEngine, IMenuManager manager) {
		ILabelManager labelMgr = pEngine.getLabelManager();
		String finalName;
		if (labelMgr != null) {
			// Use the label manager to get the stereotype text.  We do this because there are some 
			// label managers that force a stereotype text even though the model element doesn't
			// have actual stereotypes.
			finalName = labelMgr.getStereotypeText();
		}
		else
			finalName = "";

		if (finalName != null && finalName.length() > 0) {
			IMenuManager subMenu = manager.createOrGetSubMenu(loadString("IDS_LABELS_TITLE"), "org.netbeans.modules.uml.view.drawingarea.layout.popup");
			if (subMenu != null) {
				subMenu.add(createMenuAction(loadString("IDS_SHOW_STEREOTYPE"), "MBK_SHOW_STEREOTYPE"));
				//manager.add(subMenu);
			}
		}
	}

	/**
	 * Adds the bind label if the edge is a derivation.
	 *
	 * @param pContextMenu[in] The context menu about to be displayed
	 */
	public void addBindLabelPullright(IMenuManager manager) {
		IMenuManager subMenu = manager.createOrGetSubMenu(loadString("IDS_LABELS_TITLE"), "org.netbeans.modules.uml.view.drawingarea.layout.popup");
		if (subMenu != null) {
			subMenu.add(createMenuAction(loadString("IDS_SHOW_BINDING"), "MBK_SHOW_BINDING"));
			//manager.add(subMenu);
		}
	}

	/**
	 * Adds the Association set multiplicity items.
	 *
	 *  Set Multiplicity ->Association Name
	 *         Both Role Names
	 *         Both Multiplicities
	 *
	 * @param pContextMenu[in] The context menu about to be displayed
	 */
	public void addAssociationEndSetMultiplicityMenuItems(IMenuManager manager) {
		IMenuManager subMenu = manager.createOrGetSubMenu(loadString("IDS_SET_MULTIPLICITY"), "org.netbeans.modules.uml.view.drawingarea.layout.popup");
		if (subMenu != null) {
			subMenu.add(createMenuAction(loadString("IDS_SET_MULTIPLICITY_0_1"), "MBK_SET_MULTIPLICITY_0_1"));
			subMenu.add(createMenuAction(loadString("IDS_SET_MULTIPLICITY_0_STAR"), "MBK_SET_MULTIPLICITY_0_STAR"));
			subMenu.add(createMenuAction(loadString("IDS_SET_MULTIPLICITY_STAR"), "MBK_SET_MULTIPLICITY_STAR"));
			subMenu.add(createMenuAction(loadString("IDS_SET_MULTIPLICITY_1"), "MBK_SET_MULTIPLICITY_1"));
			subMenu.add(createMenuAction(loadString("IDS_SET_MULTIPLICITY_1_STAR"), "MBK_SET_MULTIPLICITY_1_STAR"));
			//manager.add(subMenu);
		}
	}

	/**
	 * Adds the Association menu items for controlling name, both ends and both multiplicities.
	 *
	 *  Show ->Association Name
	 *         Both Role Names
	 *         Both Multiplicities
	 *
	 * @param pContextMenu[in] The context menu about to be displayed
	 * @param bInMiddle[in] Is the context menu in the middle of the association (true), or on an end (false)
	 */
	public void addAssociationMultiLabelSelectionsPullright(IMenuManager manager, boolean bInMiddle) {
		IMenuManager subMenu = manager.createOrGetSubMenu(loadString("IDS_LABELS_TITLE"), "org.netbeans.modules.uml.view.drawingarea.layout.popup");
		if (subMenu != null) {
			subMenu.add(createMenuAction(loadString("IDS_SHOW_ASSOCIATION_NAME"), "MBK_SHOW_ASSOCIATION_NAME"));
			subMenu.add(createMenuAction(loadString("IDS_SHOW_BOTH_ROLENAMES"), "MBK_SHOW_BOTH_ROLENAMES"));
			subMenu.add(createMenuAction(loadString("IDS_SHOW_BOTH_MULTIPLICITIES"), "MBK_SHOW_BOTH_MULTIPLICITIES"));
			//manager.add(subMenu);
		}
	}

	/**
	 * Adds the Association menu items when the location is CMPK_END or CMPK_START.
	 *
	 *  Show Association Name
	 *  Show Role Name
	 *  Show Multiplicity
	 *
	 * @param pContextMenu[in] The context menu about to be displayed
	 */
	public void addAssociationEndLabelsPullright(IMenuManager manager) {
		IMenuManager subMenu = manager.createOrGetSubMenu(loadString("IDS_LABELS_TITLE"), "org.netbeans.modules.uml.view.drawingarea.layout.popup");
		if (subMenu != null) {
                        // J1923
			//subMenu.add(createMenuAction(loadString("IDS_SHOW_ASSOCIATION_NAME2"), "MBK_SHOW_ASSOCIATION_NAME"));
			subMenu.add(createMenuAction(loadString("IDS_SHOW_ROLENAME"), "MBK_SHOW_ROLENAME"));
			subMenu.add(createMenuAction(loadString("IDS_SHOW_MULTIPLICITY"), "MBK_SHOW_MULTIPLICITY"));
			//manager.add(subMenu);
		}
	}


	public void addActivityEdgeMenuItems(IMenuManager manager) {
		IMenuManager subMenu = manager.createOrGetSubMenu(loadString("IDS_LABELS_TITLE"), "org.netbeans.modules.uml.view.drawingarea.layout.popup");
		if (subMenu != null) {
			subMenu.add(createMenuAction(loadString("IDS_SHOW_GUARD_CONDITION"), "MBK_SHOW_GUARD_CONDITION"));
			subMenu.add(createMenuAction(loadString("IDS_SHOW_ACTIVITYEDGE_NAME"), "MBK_SHOW_ACTIVITYEDGE_NAME"));
			//manager.add(subMenu);
		}
	}

	/**
	 * Adds the qualifiers buttons
	 *
	 * @param pContextMenu[in] The context menu about to be displayed
	 */
	public void addQualifiersButton(IMenuManager manager) {
		manager.add(createMenuAction(loadString("IDS_QUALIFIERS"), "MBK_QUALIFIERS"));
	}

	/**
	 * Adds Association and Aggregation Edge specific items.
	 *
	 *  Transform -> To Ordinary Aggregate - accelerator (O)
	 *               To Composite Aggregate - accelerator (C)
	 *               Remove Aggregate - accelerator (e)
	 *               --------------------------------------------
	 *               Navigable - accelerator (N)
	 *               Reverse Ends - acclerator (R)
	 *
	 * @param pContextMenu[in] The context menu we're adding to
	 * @param pLinkElement[in] The link element that this context menu applies to
	 */
	public void addAssociationAndAggregationEdgeMenuItems(IMenuManager manager, IElement pLinkElement) {
		IMenuManager subMenu = manager.createOrGetSubMenu(loadString("IDS_EDGETRANSFORM_POPUP_TITLE"), "org.netbeans.modules.uml.view.drawingarea.layout.popup");
		if (subMenu != null) {
			subMenu.add(createMenuAction(loadString("IDS_POPUP_LINK_END_ORDINARY_AGGREGATE"), "MBK_LINK_END_ORDINARY_AGGREGATE"));
			subMenu.add(createMenuAction(loadString("IDS_POPUP_LINK_END_COMPOSITE_AGGREGATE"), "MBK_LINK_END_COMPOSITE_AGGREGATE"));
			subMenu.add(createMenuAction(loadString("IDS_POPUP_LINK_END_REMOVE_AGGREGATE"), "MBK_LINK_END_REMOVE_AGGREGATE"));
			subMenu.add(createMenuAction(loadString("IDS_POPUP_LINK_END_NAVIGABLE"), "MBK_LINK_END_NAVIGABLE"));
			subMenu.add(createMenuAction(loadString("IDS_POPUP_LINK_END_REVERSE_ENDS"), "MBK_LINK_END_REVERSE_ENDS"));
			//manager.add(subMenu);
		}
	}

	/*
	 * Loads a string from default resource bundle.
	 */
	public String loadString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}

	public void addSeparatorMenuItem(IMenuManager manager) {
		manager.add(new Separator());
	}
   
   /**
    * Is this draw engine valid for this model element?
    *
    * @return true if this draw engine can correctly represent the attached model element.
    */
   public boolean isDrawEngineValidForModelElement()
   { 
      // Base draw engine always returns true
      return true;
   }

	/**
	 * Returns the first IElement off the presentation element
	 */
	public IElement getFirstModelElement() {
		return TypeConversions.getElement(this);
	}

	public UIResources getResources() {
		if (m_resources == null)
			m_resources = new UIResources();
		return m_resources;
	}
    
    
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#getResourceName(int)
	 */
	public String getResourceName(int nKind) {
	 
	    String retValue = "";
	    
	    switch (nKind)
		{
		case UIResources.CK_FONT :
		    retValue = "name";
		    break;
		case UIResources.CK_TEXTCOLOR :
		    retValue = "name";
		    break;
		case UIResources.CK_FILLCOLOR :
		    ETList < IDrawingProperty > pDrawingProperties = getDrawingProperties();
		    for (int i = 0; i < pDrawingProperties.size(); i++) {
			IDrawingProperty pDrawingProperty = pDrawingProperties.get(i);
			if (pDrawingProperty.getResourceType().equals("color") && pDrawingProperty.getResourceName().indexOf("fill") > 0) {
			    retValue = pDrawingProperty.getResourceName();
			    break;
			}
		    }
		    break;
		case UIResources.CK_BORDERCOLOR :
		    pDrawingProperties = getDrawingProperties();
		    for (int i = 0; i < pDrawingProperties.size(); i++) {
			IDrawingProperty pDrawingProperty = pDrawingProperties.get(i);
			if (pDrawingProperty.getResourceType().equals("color") && pDrawingProperty.getResourceName().indexOf("border") > 0) {
			    retValue = pDrawingProperty.getResourceName();
			    break;
			}
		    }
		    break;
		}
	    return retValue;
	}


	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#setFontResource()
	 */
	public void setFontResource(int resourceKind, Font font) {
	    // code refactored from font menu handling in ADCoreEngine
	    if (font != null) { 
		int weight = 400;
		if (font.isBold()) {
		    weight = 700;
		}
		String resourceName = getResourceName(resourceKind);
		resetToDefaultResource(getDrawEngineID(), resourceName, "font");
		saveFont(getDrawEngineID(), resourceName, font.getName(), font.getSize(), weight, font.isItalic(), Color.BLACK.getRGB());
		invalidateProvider();
	    }
	}


	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#setColorResource()
	 */
        public void setColorResource(int resourceKind, Color color) {
	    // code refactored from color menu handling in ADCoreEngine
	    if (color != null) {
		String resourceName = getResourceName(resourceKind);
		if (resourceKind == UIResources.CK_TEXTCOLOR) {
		    resetToDefaultResource(getDrawEngineID(), resourceName, "color");
		}
		saveColor(getDrawEngineID(), resourceName, color.getRGB());
		invalidateProvider();
	    }
	}


	public boolean setSensitivityAndCheck(String id, ContextMenuActionClass pClass) {
		return false;
	}

	public boolean onHandleButton(ActionEvent e, String id) {
		return false;
	}

	/**
	 * Tests to determine if the associated parent diagram is readonly.
	 *
	 * @return true if the parent diagram is readonly
	 */
	protected boolean isParentDiagramReadOnly() {
		IDiagram dia = getDiagram();
		return dia != null ? dia.getReadOnly() : true;
	}

	/*
	 * Searches through all the compartments looking for the specified compartment 
	 */
	public < Type > Type getCompartmentByKind(Class interfacetype) {
		try {
			IteratorT < ICompartment > iter = new IteratorT < ICompartment > (this.getCompartments());
			while (iter.hasNext()) {
				ICompartment comp = iter.next();
				if (interfacetype.isAssignableFrom(comp.getClass()))
					return (Type) comp;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Returns the meta type of the IElement this draw engine represents.
	 *
	 * @return The metatype of the element this draw engine represents.
	 */
	protected String getMetaTypeOfElement() {
		IElement pEle = TypeConversions.getElement(this);
		return pEle != null ? pEle.getElementType() : null;
	}

	public double getLastDrawPointWorldY() {
		return 0;
	}

	public void setLastDrawPointWorldY(double i) {
	}

	public void updateLastDrawPointWorldY(double d) {
	}

	/*
	 * 
	 */
	public void setManagerBackpointer(IGraphObjectManager pManager) {
		// this is gross, but we aren't getting the same types as the cleaner C++ version, 
		// for whatever reason, so the conversion has to happen here
		if (pManager != null && getParentETElement() instanceof IETGraphObject) {
			IETGraphObject object = (IETGraphObject)getParentETElement();
			pManager.setParentETGraphObject(object);
		}
	}

	/**
	 * Posts and invalidate to the drawing area.
	 */
	protected void postInvalidate() {
		try {
			IDrawingAreaControl pDA = this.getDrawingArea();

			if (pDA != null) {
				// Find the presentation element associated with this draw engine
				IPresentationElement pPresentationElement = getPresentationElement();
				if (pPresentationElement != null) {
					pDA.postInvalidate(pPresentationElement);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * Dispatches draw to all the compartments
	 */
	public void dispatchDrawToCompartments(IDrawInfo pInfo, IETRect pDeviceBounds) {
		try {
			IteratorT < ICompartment > iter = new IteratorT < ICompartment > (this.getCompartments());
			while (iter.hasNext()) {
				iter.next().draw(pInfo, pDeviceBounds);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public ContextMenuActionClass createMenuAction(String text, String menuID) {
		return new ContextMenuActionClass(this, text, menuID);
	}

	public boolean parentDiagramIsReadOnly() {
		IDiagram pDiagram = getDiagram();
		return pDiagram != null ? pDiagram.getReadOnly() : true;
	}
	
	//	Resource user
	public ETList<IDrawingProperty> getDrawingProperties()
	{
		String sDrawEngineID = getDrawEngineID();
		if (sDrawEngineID != null && sDrawEngineID.length() > 0)
		{
			return m_ResourceUser.getDrawingProperties(this, sDrawEngineID);
		}
		
		return null;
	}

	public void saveColor(String sDrawEngineType, String sResourceName, int nColor)
	{
		setIsDirty();
		m_ResourceUser.saveColor(sDrawEngineType, sResourceName, nColor);
	}
	
	public void saveColor2(IColorProperty pProperty)
	{
		setIsDirty();
		m_ResourceUser.saveColor2(pProperty);
	}
	
	public void saveFont(  String sDrawEngineName,
						   String sResourceName,
						   String sFaceName,
						   int nHeight,
						   int nWeight,
						   boolean bItalic,
						   int nColor)
	{
		setIsDirty();
		m_ResourceUser.saveFont(sDrawEngineName,
								 sResourceName,
								 sFaceName,
								 nHeight,
								 nWeight,
								 bItalic,
								 nColor);
	}
	
	public void saveFont2(IFontProperty pProperty)
	{
		setIsDirty();
		m_ResourceUser.saveFont2(pProperty);
	 }

	 public void resetToDefaultResource( String sDrawEngineName, 
										String sResourceName,
										String sResourceType)
	 {
		setIsDirty();
		m_ResourceUser.resetToDefaultResource( sDrawEngineName, 
												sResourceName,
												sResourceType);
												
		// Reset our child compartments
		int numCompartments = getNumCompartments();
		for (int i = 0 ; i < numCompartments ; i++)
		{
			ICompartment cpCompartment = getCompartment(i);
			IDrawingPropertyProvider pCompProvider = (IDrawingPropertyProvider)cpCompartment;
			if (pCompProvider != null)
			{
				pCompProvider.resetToDefaultResource(sDrawEngineName, sResourceName, sResourceType);
			}
		}
		   
		initResources();
		//invalidateProvider();
	}
	
	public void resetToDefaultResources()
	{
		setIsDirty();
		m_ResourceUser.resetToDefaultResources();
		
		// Reset our child compartments
		int numCompartments = getNumCompartments();
		for (int i = 0 ; i < numCompartments ; i++)
		{
			ICompartment cpCompartment = getCompartment(i);
			IDrawingPropertyProvider pCompProvider = (IDrawingPropertyProvider)cpCompartment;
			if (pCompProvider != null)
			{
				pCompProvider.resetToDefaultResources();
			}
		}
		   
		initResources();
		invalidateProvider();
	}
	
	public void resetToDefaultResources2(String sDrawEngineName)
	{
		if (sDrawEngineName != null && sDrawEngineName.length() > 0)
		{
			String sDrawEngineID = getDrawEngineID();
			if (sDrawEngineID != null && sDrawEngineID.equals(sDrawEngineName))
			{
				resetToDefaultResources();
			}
		}
	}
	
	public void dumpToFile(String sFile, boolean bDumpChildren, boolean bAppendToExistingFile)
	{
		m_ResourceUser.dumpToFile(sFile, bAppendToExistingFile);
		
		if (bDumpChildren)
		{
			// Reset our child compartments
			int numCompartments = getNumCompartments();
			for (int i = 0 ; i < numCompartments ; i++)
			{
				ICompartment cpCompartment = getCompartment(i);
				IDrawingPropertyProvider pCompProvider = (IDrawingPropertyProvider)cpCompartment;
				if (pCompProvider != null)
				{
					pCompProvider.dumpToFile(sFile, true, true);
				}
			}
		}
	}
	
	public boolean displayFontDialog(IFontProperty pProperty)
	{
		return m_ResourceUser.displayFontDialog(pProperty);
	}
	
	public boolean displayColorDialog(IColorProperty pProperty)
	{
		return m_ResourceUser.displayColorDialog(pProperty);
	}
	
	public void invalidateProvider()
	{
		invalidate();
// This will enbale engine redraw immediatly, but with problems for some case. See J909
//		IDrawingAreaControl drawingArea = getDrawingArea();
//
//		if (drawingArea != null)
//			drawingArea.refresh(false);
	}
	
	// IResourceUserHelper
	public int getColorID(int nColorStringID)
	{
		int nID = -1;

		if (nColorStringID != -1)
		{
			Integer iterator = m_ResourceUser.m_Colors.get(new Integer(nColorStringID));
			if (iterator != null)
			{
				// Our color has been cached from the last time
				int nTempID = iterator.intValue();

				// Make sure the id is valid, if not then re-get a good
				// id from our draw engine.
				if (nTempID == -1 || m_ResourceUser.getResourceMgr().isValidColorID(nTempID) == false)
				{
					m_ResourceUser.m_Colors.remove(new Integer(nColorStringID));
				}
				else
				{
					nID = nTempID;
				}
			}
		}
         
		// Get it from the resource manager
		if (nID == -1 && m_ResourceUser.verifyDrawEngineStringID() )
		{
			// Get the id from the diagram
			nID = m_ResourceUser.getResourceMgr().getColorID(m_ResourceUser.m_nDrawEngineStringID, nColorStringID);
			if (nID != -1)
			{
				m_ResourceUser.m_Colors.put(new Integer(nColorStringID), new Integer(nID));

				// Reset the draw time so we re-get our draw information
				//setLastDrawTime(true);
			}
		}
		
		return nID;
	}
	
	public int getFontID(int nFontStringID)
	{
		int nID = -1;

		if (nFontStringID != -1)
		{
			Integer iterator = m_ResourceUser.m_Fonts.get(new Integer(nFontStringID));
			if (iterator != null)
			{
				// Our font has been cached from the last time
				int nTempID = iterator.intValue();

				// Make sure the id is valid, if not then re-get a good
				// id from our draw engine.
				if (nTempID == -1 || m_ResourceUser.getResourceMgr().isValidFontID(nTempID) == false)
				{
					m_ResourceUser.m_Fonts.remove(new Integer(nFontStringID));
				}
				else
				{
					nID = nTempID;
				}
			}
         
			// Get it from the resource manager
			if (nID == -1 && m_ResourceUser.verifyDrawEngineStringID())
			{
				// Get the id from the diagram
				nID = m_ResourceUser.getResourceMgr().getFontID(m_ResourceUser.m_nDrawEngineStringID, nFontStringID);

				if (nID != -1)
				{
					m_ResourceUser.m_Fonts.put(new Integer(nFontStringID), new Integer(nID));
				}
			}
		}
		return nID;
	}
	
	public boolean verifyDrawEngineStringID()
	{
		boolean bIDOK = true;

		if (m_ResourceUser.m_nDrawEngineStringID == -1)
		{
			String sDrawEngineID = getDrawEngineID();
			if (sDrawEngineID.length() > 0)
			{
				// Set the draw engine string id on the resource user
				m_ResourceUser.setDrawEngineStringID(sDrawEngineID);
				if (m_ResourceUser.m_nDrawEngineStringID == -1)
				{
					bIDOK = false;
				}
			}
		}

		return bIDOK;
	}
	

	/*
	 * Returns true if this Engine has been Initialized
	 */ 
	public boolean isInitialized()
	{
		return m_bInitResources;
	}

//	public int convertCPPColor( int cppColor)
//	{
//		int b = cppColor >> 16;
//		int g = (cppColor >> 8) - (b << 8);
//		int r = cppColor - (b << 16) - (g << 8);
//		Color pColor = new Color(r, g, b);
//		return pColor.getRGB();
//	}

	protected SmartDragTool createSmartDragTool(MouseEvent pEvent) {
		SmartDragTool dragTool = null;
		IDrawingAreaControl daCtrl = getDrawingArea();
		ADGraphWindow graphWindow = daCtrl != null ? daCtrl.getGraphWindow() : null;

		if (daCtrl != null && graphWindow != null) {
			TSGraphObject graphObject = getOwnerGraphObject();
			TSEObject tseObject = graphObject instanceof TSEObject ?  (TSEObject) graphObject : null;

			if (tseObject != null) {
				if (!pEvent.isControlDown()){
					graphWindow.deselectAll(true);
				}

				graphWindow.selectObject(tseObject, true);
			}
			
			IETPoint startPoint = daCtrl.deviceToLogicalPoint(pEvent.getX(),pEvent.getY());
			// Don't create the drag tool and then change the selection.
			dragTool = new SmartDragTool(new TSConstPoint(startPoint.getX(), startPoint.getY()),daCtrl,false);

			if (dragTool != null) {
				TSEGraph graph = graphWindow.getGraph();
				dragTool.setGraph(graph);
				dragTool.setDrawingAreaControl(daCtrl);
			}
		}
		return dragTool;
	}
	
	/*
	 * Returns the Scaled size in device units, you must pass a size calculated at zoom level 1.0
	 */
	public IETSize scaleSize(final IETSize sizeAtOneHundred, TSTransform windowTransform)
	{
		if (windowTransform != null && sizeAtOneHundred != null)
		{
			TSTransform transform = (TSTransform)windowTransform.clone();
			transform.setScale(1.0);
			double worldSizeX =  transform.widthToWorld(sizeAtOneHundred.getWidth());
			double worldSizeY =  transform.heightToWorld(sizeAtOneHundred.getHeight());
			Dimension d = windowTransform.sizeToDevice(worldSizeX, worldSizeY);
			
			return d != null ? new ETSize(d.width, d.height) : null;
		}
		return null;
	}	

	/**
	 * Calculates the node's device coordinates given a Tom Sawyer mouse event.
	 */
	public IETRect getLogicalBoundingRect() 
	{
		IETGraphObjectUI ui = this.getParent();
		return ui != null && ui.getOwner() != null ? new ETRectEx(ui.getBounds()) : null;
	}

   public int getLastResizeOriginator()
   {
      return m_LastResizeOriginator;
   }

   public void setLastResizeOriginator(int i)
   {
      m_LastResizeOriginator = i;
   }

	/**
	 * Displays the navigation dialog - used during doubleclicks
	 */
	public void displayNavigationDialog()
	{
		// Bring up a dialog allowing user to go to either diagrams or PE's
		IDiagramAndPresentationNavigator pNavigator = new DiagramAndPresentationNavigator();
		if (pNavigator != null)
		{ 
			boolean bMeHandled = false;
			ETSmartWaitCursor waitCursor = new ETSmartWaitCursor();
			IElement pFirstME = getFirstModelElement();
			if (pFirstME != null)
			{
				bMeHandled = pNavigator.handleNavigation(0, pFirstME, true);
			}
			waitCursor.stop();
		}
	}

	/*
	 *  (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#onDiscardParentETElement()
	 */
	public void onDiscardParentETElement()
	{
		clearCompartments();
		m_parentUI = null;
	}



    /////////////
    // Accessible
    /////////////

    AccessibleContext accessibleContext;

    public AccessibleContext getAccessibleContext() {
	if (accessibleContext == null) {
	    accessibleContext = new AccessibleETDrawEngine();
	} 
	return accessibleContext;
    }


    public class AccessibleETDrawEngine extends AccessibleContext 
	implements AccessibleComponent, AccessibleSelection  
    {

	public Locale getLocale() {
	    return getGraphWindow().getLocale();
	}

	public int getAccessibleIndexInParent() {
	    return 0;
	}

	public AccessibleStateSet getAccessibleStateSet() {
	    return new AccessibleStateSet(new AccessibleState[] {
		AccessibleState.SHOWING,
		AccessibleState.VISIBLE,
		AccessibleState.ENABLED, 		
		AccessibleState.FOCUSABLE,	
		AccessibleState.RESIZABLE,	
		AccessibleState.SELECTABLE	
	    });	    
	}


	public String getAccessibleName(){
	    String acName = "";
	    IElement pEle = TypeConversions.getElement(ETDrawEngine.this);	
	    if (pEle != null) {
		String eleType = pEle.getElementType();
		if (eleType != null && eleType.length() > 0) {
		    String expandedType = pEle.getExpandedElementType();
		    if (expandedType != null) {
			String captionKey = "IDS_" + expandedType.toUpperCase();
			String caption = loadString(captionKey);			
			if (!caption.equals("!" + captionKey + "!")) {
			    eleType = caption;
			}
		    }
		    acName = eleType;

		    String name = "";
		    if (pEle instanceof INamedElement) {
			name = ((INamedElement)pEle).getName();
		    } 	
		    acName += " " + name;
		}		
	    }
	    return acName;
	} 

	public String getAccessibleDescription(){
	    return getAccessibleName();
	}
	
	public AccessibleRole getAccessibleRole() {
	    return AccessibleRole.PANEL;
	}
	
	public int getAccessibleChildrenCount() {
	    return getAccessibleChildren().size(); 
	}

	
	public Accessible getAccessibleChild(int i) {	
	    if (i < getAccessibleChildrenCount()) {
		return getAccessibleChildren().get(i);
	    }
	    return null;	    
	}


	public AccessibleRelationSet getAccessibleRelationSet() {
	    AccessibleRelationSet relSet = new AccessibleRelationSet();
	    ILabelManager labelMgr = getLabelManager();	    
	    if (labelMgr != null) {
		ArrayList<IDrawEngine> engines = new ArrayList<IDrawEngine>();
		for (int lIndx = 0; /* break below */; lIndx++) {
		    IETLabel label = null;
		    label = labelMgr.getLabelByIndex(lIndx);
		    if (label != null) {
			IDrawEngine eng = label.getEngine();
			if (eng != null) {
			    engines.add(eng);
			}
		    } else {
			break;
		    } 
		}
		if (engines.size() > 0 ) {		    		    
		    AccessibleRelation labeledBy = 
			new AccessibleRelation(AccessibleRelation.LABELED_BY,
					       engines.toArray());
		    relSet.add(labeledBy);
		}
	    }
	    return relSet;
	}


	public AccessibleComponent getAccessibleComponent() {
	    return this;
	}

	public AccessibleSelection getAccessibleSelection() {
	    int childnum = getAccessibleChildrenCount(); 
	    for(int i = 0; i < getAccessibleChildrenCount(); i++) {
		if (isSelectable(getAccessibleChild(i))) {
		    return this;
		}
	    }
	    return null;
	}


	////////////////////////////////
	// interface AccessibleComponent
	////////////////////////////////

	public java.awt.Color getBackground() {
	    return null;
	}

	public void setBackground(java.awt.Color color) {
	    ;
	}

	public java.awt.Color getForeground() {
	    return null;
	}

	public void setForeground(java.awt.Color color) {
	    ;
	}

	public java.awt.Cursor getCursor() {
	    return getGraphWindow().getCursor();
	}
	
	public void setCursor(java.awt.Cursor cursor) {
	    ;
	}

	public java.awt.Font getFont() {
	    return null;
	}

	public void setFont(java.awt.Font font) {
	    ;
	}

	public java.awt.FontMetrics getFontMetrics(java.awt.Font font) {
	    return getGraphWindow().getFontMetrics(font);
	}
	public boolean isEnabled() {
	    return true;
	}

	public void setEnabled(boolean enabled) {

	}

	public boolean isVisible() {
	    return true;
	}

	public void setVisible(boolean visible) {
	    ;
	}

	public boolean isShowing() {
	    return true;
	}
	
	public boolean contains(java.awt.Point point) {
            Rectangle r = getBounds();
            return r.contains(point);
	}
	
	public java.awt.Point getLocationOnScreen() {
	    IETRect scRect = getWinScreenRect();
	    if (scRect != null) {
		return new java.awt.Point(scRect.getIntX(), scRect.getIntY());
	    }
	    return null;
	}
	
	public java.awt.Point getLocation() {
	    AccessibleComponent parentComponent 
		= accessibleParent.getAccessibleContext().getAccessibleParent().getAccessibleContext().getAccessibleComponent();
	    if (parentComponent != null) {
		java.awt.Point parentLocation = parentComponent.getLocationOnScreen();		
		java.awt.Point componentLocation = getLocationOnScreen();
		if (parentLocation != null && componentLocation != null) { 
		    return new java.awt.Point(parentLocation.x - componentLocation.x,
					      parentLocation.y - componentLocation.y);
		}
	    }
	    return null;
	}
	
	public void setLocation(java.awt.Point point) {
	    ;
	}
	
	public java.awt.Rectangle getBounds() {
	    IETRect clientRect = getWinClientRect();
	    java.awt.Point loc = getLocation();
	    if (clientRect != null && loc != null) {
		return new Rectangle(loc.x, loc.y, clientRect.getIntWidth(),clientRect.getIntHeight());
	    }
	    return null;
	}
	
	public void setBounds(java.awt.Rectangle bounds) {
	    setWinClientRectangle(new ETRect(bounds.x, bounds.y, bounds.width, bounds.height));
	}

	public java.awt.Dimension getSize() {
            Rectangle r = getBounds();
            return new Dimension(r.width, r.height);
	}
	
	public void setSize(java.awt.Dimension dim) {
	    setScaledSize(new ETSize(dim.width, dim.height));
	}
	
	public javax.accessibility.Accessible getAccessibleAt(java.awt.Point point) {
	    //getCompartmentAtPoint(IETPoint pCurrentPos)
 	    return null;
	}

	public boolean isFocusTraversable() {
	    return true;
	}
	public void requestFocus() {
	    ;
	}

	public void addFocusListener(java.awt.event.FocusListener listener) {
	    ;
	}

	public void removeFocusListener(java.awt.event.FocusListener listener) {
	    ;
	}

	
	////////////////////////////////
	// interface AccessibleSelection
	////////////////////////////////

	public int getAccessibleSelectionCount() {
	    List<Accessible> selected = getSelectedAccessibleChildren();
	    if (selected != null) {
		return selected.size();
	    }
	    return 0;
	}
	
	public Accessible getAccessibleSelection(int i) {
	    List<Accessible> selected = getSelectedAccessibleChildren();
	    if (selected != null && i < selected.size()) {
		return selected.get(i);
	    }
	    return null;
	}
	
	public boolean isAccessibleChildSelected(int i) {
	    Accessible child = getAccessibleChild(i);	   
	    if (child != null) {
		return isSelected(child);	    
	    }
	    return false;
	}
	
	public void addAccessibleSelection(int i) {
	    Accessible child = getAccessibleChild(i);	    
	    if (child != null) {
		selectChild(child, true, false);
	    }
	}
	
	public void removeAccessibleSelection(int i) {
	    List<Accessible> children = getAccessibleChildren();
	    if (children != null && i < children.size()) {
		selectChild(children.get(i), false, false); 
	    }
	}

	public void selectAllAccessibleSelection() {
	    List<Accessible> children = getAccessibleChildren();
	    if (children != null) {
		for(int i = 0; i < children.size(); i++) {
		    selectChild(children.get(i), true, false); 
		}
	    }	    
	}
	
	public void clearAccessibleSelection() {
	    List<Accessible> selected = getSelectedAccessibleChildren();
	    if (selected != null) {
		for(int i = 0; i < selected.size(); i++) {
		    selectChild(selected.get(i), false, true); 
		}
	    }
	}
	

	/////////////////
	// Helper methods
	/////////////////

	public void selectChild(Accessible child, boolean select, boolean replaceSelection) {	    
	    
	    if (child instanceof ICompartment) {
		if (replaceSelection) {
		    selectAllCompartments(false);
		} 
		((ICompartment)child).setSelected(select);
	    }
	    	    
	    if (accessibleParent != null 
		&& accessibleParent.getAccessibleContext() instanceof AccessibleSelectionParent) 
	    {
		((AccessibleSelectionParent)accessibleParent.getAccessibleContext())
		    .selectChild(ETDrawEngine.this, select, replaceSelection);	    
	    }

	}
	

	public boolean isSelectable(Accessible child) {
	    AccessibleStateSet stateSet = child.getAccessibleContext().getAccessibleStateSet();
	    if (stateSet != null && stateSet.contains(AccessibleState.SELECTABLE)) {
		return true;
	    }
	    return false;
	}
	

	public boolean isSelected(Accessible child) {
	    if (child instanceof ICompartment) {
		return ((ICompartment)child).isSelected();
	    }	
	    return false;
	}


	public List<Accessible> getAccessibleChildren() {
	    ArrayList<Accessible> children = new ArrayList<Accessible>();
	    for(int i = 0; i < m_compartments.size(); i++) {		
		ICompartment comp = m_compartments.get(i);
		if (comp instanceof Accessible) {
		    ((Accessible)comp).getAccessibleContext().setAccessibleParent(ETDrawEngine.this);
		    children.add((Accessible)comp);
		}
	       
	    } 
	    return children;
	}


	public List<Accessible> getSelectedAccessibleChildren() {
	    ArrayList<Accessible> selected = new ArrayList<Accessible>();
	    List<Accessible> children = getAccessibleChildren();
	    for(int i = 0; i < children.size(); i++) {
		Accessible child = children.get(i);
		if (isSelected(child)) {
		    selected.add(child);		
		}
	    }
	    return selected;
	}	
	    
	
    }


}
