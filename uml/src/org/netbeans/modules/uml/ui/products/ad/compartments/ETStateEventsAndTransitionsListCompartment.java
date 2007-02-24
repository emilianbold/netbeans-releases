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



package org.netbeans.modules.uml.ui.products.ad.compartments;

import java.awt.Point;
import java.awt.event.ActionEvent;

import org.netbeans.modules.uml.core.metamodel.basic.basicactions.IProcedure;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IState;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.ITransition;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.ui.products.ad.application.IMenuManager;
import org.netbeans.modules.uml.ui.products.ad.application.action.ContextMenuActionClass;
import org.netbeans.modules.uml.ui.products.ad.application.action.ContextMenuBaseAction;
import org.netbeans.modules.uml.ui.support.applicationmanager.DrawingFactory;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;

/**
 * @author sumitabhk
 *
 */
public class ETStateEventsAndTransitionsListCompartment extends ETNamedElementListCompartment 
										implements IADStateEventsAndTransitionsListCompartment
{

	/**
	 * 
	 */
	public ETStateEventsAndTransitionsListCompartment()
	{
		super();
		this.setName("Events");
	}

	/**
	 * Called when the context menu is about to be displayed.  The compartment should add whatever buttons
	 * it might need.
	 *
	 * @param pContextMenu[in] The context menu about to be displayed
	 * @param logicalX[in] The logical x location of the context menu event
	 * @param logicalY[in] The logical y location of the context menu event
	 */
	public void onContextMenu(IMenuManager manager)
	{
            if (getEnableContextMenu())
            {
                addStateEventsAndTransitionsButton(manager);
            }
        }

	/**
	 * Adds an transition compartment.  The compartment is always made visible.
	 *
	 * @param sCompartmentType [in] The type of element to add
	 * @param pCompartment [in] The compartment to add.  If NULL then a new compartment is created and the editor invoked.
	 * If non-null the compartment is simply inserted into the list.
	 * @param nIndex [in] The position in the visible list to place this compartment.  If blank or -1 the compartment is
	 * added to the bottom of the list.
	 * @param bRedrawNow [in] Should we redraw this compartment?
	 */
	public long addCompartment(ICompartment pCompartment, int nIndex, boolean bRedrawNow)
	{
		if (pCompartment instanceof IADStateEventsAndTransitionsCompartment)
		{
			//call super from ListCompartmentImpl
			super.addCompartment(pCompartment, nIndex, bRedrawNow);
		}
		else
		{
			IDrawEngine pEngine = m_engine;
			IState pState = null;
			if (pEngine != null)
			{
				IElement pElem = TypeConversions.getElement(pEngine);
				if (pElem instanceof IState)
				{
					pState = (IState)pElem;
				}
			}
			
			if (pState != null)
			{
				// create transition
				IElement pCreatedEle = DrawingFactory.retrieveModelElement("Transition");
				if (pCreatedEle != null && pCreatedEle instanceof ITransition)
				{
					ITransition pTransition = (ITransition)pCreatedEle;

					// Make it internal
					pTransition.setIsInternal(true);
					
					// now add transition to the state
					pState.addOutgoingTransition(pTransition);
					
					// Call base class to create a new compartment at the insert position
					ICompartment pCreatedComp = createAndAddCompartment("ADStateEventsAndTransitionsCompartment", nIndex, bRedrawNow);
					if (pCreatedComp != null)
					{
						// This routine will initialize the compartment, make it visible and
						// begin the edit on it.
						finishAddCompartment(pEngine, pTransition, pCreatedComp, bRedrawNow);
					}
				}
			}
		}
		return 0;
	}

	/**
	 * Sets the sensitivities and check states of the buttons created and owned by this implementor.  By default the
	 * buttons are created with sensitivity == true which means they are enabled.
	 *
	 * @param pContextMenu [in] The parent context menu that was displayed
	 * @param pMenuItem [in] The button that the sensitivity is being requested for
	 * @param buttonKind [in] The ID of the button above.  This ID is the one used when creating the button.
	 */
	public boolean setSensitivityAndCheck(String id, ContextMenuActionClass pMenuAction)
	{
		boolean bFlag = super.setSensitivityAndCheck(id, pMenuAction);
		if (id.equals("MBK_INSERT_ENTRY") || id.equals("MBK_DELETE_ENTRY") ||
			id.equals("MBK_INSERT_EXIT") || id.equals("MBK_DELETE_EXIT") ||
			id.equals("MBK_INSERT_DOACTIVITY") || id.equals("MBK_DELETE_DOACTIVITY") ||
			id.equals("MBK_INSERT_INCOMING_INTERNALTRANSITION") || 
			id.equals("MBK_INSERT_OUTGOING_INTERNALTRANSITION") )
		{
			// Always sensitive, unless the diagram is readonly
			bFlag = true;
		}
		else if (id.equals("MBK_DELETE_INTERNALTRANSITION"))
		{
			boolean foundTransition = false;
			// See if any of the internal transitions are internal
			ETList<ICompartment> pCompartments = getSelectedCompartments();
			if (pCompartments != null)
			{
				int count = pCompartments.size();
				for (int i=0; i<count; i++)
				{
					ICompartment pComp = pCompartments.get(i);
					IElement pEle = TypeConversions.getElement(pComp);
					if (pEle != null && pEle instanceof ITransition)
					{
						foundTransition = true;
						break;
					}
				}
			}
			bFlag = foundTransition;
		}
		
		return isParentDiagramReadOnly() ? false : bFlag;
	}
	
	/**
	 * Called when the context menu button has been selected.  The compartment should handle the selection.
	 *
	 * @param pContextMenu [in] The context menu that was displayed to the user
	 * @param pMenuItem [in] The menu that was just selected
	 */
	public boolean onHandleButton(ActionEvent e, String id)
	{
		boolean retval = super.onHandleButton(e, id);
		IState pState = getState();
		if (id.equals("MBK_INSERT_ENTRY"))
		{
			addCompartment("Entry", null, getRightMouseButtonIndex(), true);
		}
		else if (id.equals("MBK_DELETE_ENTRY"))
		{
			if (pState != null)
			{
				IProcedure pProc = pState.getEntry();
				if (pProc != null)
				{
					pProc.delete();
				}
			}
		}
		else if (id.equals("MBK_INSERT_EXIT"))
		{
			addCompartment("Exit", null, getRightMouseButtonIndex(), true);
		}
		else if (id.equals("MBK_DELETE_EXIT"))
		{
			if (pState != null)
			{
				IProcedure pProc = pState.getExit();
				if (pProc != null)
				{
					pProc.delete();
				}
			}
		}
		else if (id.equals("MBK_INSERT_DOACTIVITY"))
		{
			addCompartment("DoActivity", null, getRightMouseButtonIndex(), true);
		}
		else if (id.equals("MBK_DELETE_DOACTIVITY"))
		{
			if (pState != null)
			{
				IProcedure pProc = pState.getDoActivity();
				if (pProc != null)
				{
					pProc.delete();
				}
			}
		}
		else if (id.equals("MBK_INSERT_INCOMING_INTERNALTRANSITION"))
		{
			addCompartment("Incoming Transition", null, getRightMouseButtonIndex(), true);
		}
		else if (id.equals("MBK_INSERT_OUTGOING_INTERNALTRANSITION"))
		{
			addCompartment("Outgoing Transition", null, getRightMouseButtonIndex(), true);
		}
		else if (id.equals("MBK_DELETE_INTERNALTRANSITION"))
		{
			deleteSelectedCompartments(true);
		}
		return retval;
	}

	/**
	 * Adds an transition compartment.  The compartment is always made visible.
	 *
	 * @param sCompartmentType [in] The type of element to add
	 * @param pCompartment [in] The compartment to add.  If NULL then a new compartment is created and the editor invoked.
	 * If non-null the compartment is simply inserted into the list.
	 * @param nIndex [in] The position in the visible list to place this compartment.  If blank or -1 the compartment is
	 * added to the bottom of the list.
	 * @param bRedrawNow [in] Should we redraw this compartment?
	 */
	private void addCompartment(String sCompartmentType, ICompartment pCompartment, 
								int nIndex, boolean bRedrawNow)
	{
		//if (pCompartment != null )
		{
			if (pCompartment != null && pCompartment instanceof IADStateEventsAndTransitionsCompartment)
			{
				super.addCompartment(pCompartment, nIndex, bRedrawNow);
			}
			else
			{
				IState pState = getState();
				IDrawEngine pEngine = getEngine();
				if (pState != null)
				{
					IElement createdEle = null;
					if (sCompartmentType.equals("Incoming Transition") || 
						sCompartmentType.equals("Outgoing Transition"))
					{
						createdEle = DrawingFactory.retrieveModelElement("Transition");
						if (createdEle != null && createdEle instanceof ITransition)
						{
							ITransition pTransition = (ITransition)createdEle;
							if (sCompartmentType.equals("Incoming Transition"))
							{
								// Make the transition internal
								pTransition.setIsInternal(true);
								
								// now add transition to the state
								pState.addIncomingTransition(pTransition);
							}
							else
							{
								// Make the transition internal
								pTransition.setIsInternal(true);
								
								// now add transition to the state
								pState.addOutgoingTransition(pTransition);
							}
						}
					}
					else if (sCompartmentType.equals("Entry") || 
							 sCompartmentType.equals("Exit") || 
							 sCompartmentType.equals("DoActivity"))
					{
						createdEle = DrawingFactory.retrieveModelElement("Procedure");
						if (createdEle != null && createdEle instanceof IProcedure)
						{
							IProcedure pProc = (IProcedure)createdEle;
							if (sCompartmentType.equals("Entry"))
							{
								pState.setEntry(pProc);
							}
							else if (sCompartmentType.equals("Exit"))
							{
								pState.setExit(pProc);
							}
							else if (sCompartmentType.equals("DoActivity"))
							{
								pState.setDoActivity(pProc);
							}
						}
					}
					
					if (createdEle != null)
					{
						// Call base class to create a new compartment at the insert position
						ICompartment createdComp = createAndAddCompartment("ADStateEventsAndTransitionsCompartment", nIndex, bRedrawNow);
						if (createdComp != null)
						{
							// This routine will initialize the compartment, make it visible and
							// begin the edit on it.
							finishAddCompartment(pEngine, createdEle, createdComp, bRedrawNow);
						}
					}
				}
			}
		}
	}

	/**
	 * Returns the IState attached to this compartment
	 *
	 * @param pState [out,retval] The state attached to this compartment
	 */
	private IState getState()
	{
		IState pState = null;
		IDrawEngine pEngine = getEngine();
		if (pEngine != null)
		{
			IElement pEle = TypeConversions.getElement(pEngine);
			if (pEle != null && pEle instanceof IState)
			{
				pState = (IState)pEle;
			}
		}
		return pState;
	}

	/**
	 * This is the name of the drawengine used when storing and reading from the product archive.
	 *
	 * @param sID[out,retval] The unique name for this compartment.  Used when reading and writing the
	 * product archive (etlp file).
	 */
	public String getCompartmentID()
	{
		return "ADStateEventsAndTransitionsListCompartment";
	}
	
	public void addModelElement(IElement pElement, int nIndex)
	{
		super.addModelElement(pElement, nIndex);
		this.setName("");
		
		if (pElement instanceof IState)
		{
			IState pState = (IState)pElement;
			
			IProcedure pEntry = pState.getEntry();
			IProcedure pExit = pState.getExit();
			IProcedure pDoActivity = pState.getDoActivity();
			ETList<ITransition> pIncomingTransitions = pState.getIncomingTransitions();
			ETList<ITransition> pOutgoingTransitions = pState.getOutgoingTransitions();
			
			// Go through the state and add the various events and internal transitions,
			// if we have any leftover compartments when we're done then we need to delete those
			ETList<ICompartment> pAllCompartments = new ETArrayList<ICompartment>();
			int numCompartments = this.getNumCompartments();
			for (int nCompIndex = 0; nCompIndex < numCompartments; nCompIndex++)
			{
				ICompartment pCompartment = this.getCompartment(nCompIndex);
				if (pCompartment != null)
				{
					pAllCompartments.add(pCompartment);
				}
			}
			
			if (pIncomingTransitions != null)
			{
				int numIncomingTransitions = pIncomingTransitions.size();
				for (int i = 0 ; i < numIncomingTransitions ; i++)
				{
					ITransition pIncomingTransition = pIncomingTransitions.get(i);
					if (pIncomingTransition != null)
					{
						boolean bIsInternal = pIncomingTransition.getIsInternal();
						if (!bIsInternal)
						{
							// Not internal, so don't show
							pIncomingTransition = null;
						}
					}
					
					// If we have a compartment here then display it
					if (pIncomingTransition != null)
					{
						ICompartment pFoundCompartment = FindCompartmentContainingElement(pIncomingTransition);
						if (pFoundCompartment == null)
						{
							AddProcedureOrTransitionCompartment(pIncomingTransition);
						}
						else if (pAllCompartments != null)
						{
							pAllCompartments.remove(pIncomingTransition);
						}
					}
				}
			}
			
			if (pOutgoingTransitions != null)
			{
				int numOutgoingTransitions = pOutgoingTransitions.size();
				for (int i = 0 ; i < numOutgoingTransitions ; i++)
				{
					ITransition pOutgoingTransition = pOutgoingTransitions.get(i);
					if (pOutgoingTransition != null)
					{
						boolean bIsInternal = pOutgoingTransition.getIsInternal();
						if (!bIsInternal)
						{
							// Not internal, so don't show
							pOutgoingTransition = null;
						}
					}
		
					// If we have a compartment here then display it
					if (pOutgoingTransition != null)
					{
						ICompartment pFoundCompartment = FindCompartmentContainingElement(pOutgoingTransition);
						if (pFoundCompartment == null)
						{
							AddProcedureOrTransitionCompartment(pOutgoingTransition);
						}
						else if (pAllCompartments != null)
						{
							pAllCompartments.remove(pFoundCompartment);
						}
					}
				}
			}
			
			if (pEntry != null)
			{
				ICompartment pFoundCompartment = FindCompartmentContainingElement(pEntry);
				if (pFoundCompartment == null)
				{
					AddProcedureOrTransitionCompartment(pEntry);
				}
				else if (pAllCompartments != null)
				{
					pAllCompartments.remove(pFoundCompartment);
				}
			}
			
			if (pExit != null)
			{
				ICompartment pFoundCompartment = FindCompartmentContainingElement(pExit);
				if (pFoundCompartment == null)
				{
					AddProcedureOrTransitionCompartment(pExit);
				}
				else if (pAllCompartments != null)
				{
					pAllCompartments.remove(pFoundCompartment);
				}
			}
			
			if (pDoActivity != null)
			{
				ICompartment pFoundCompartment = FindCompartmentContainingElement(pDoActivity);
				if (pFoundCompartment == null)
				{
					AddProcedureOrTransitionCompartment(pDoActivity);
				}
				else if (pAllCompartments != null)
				{
					pAllCompartments.remove(pFoundCompartment);
				}
			}
			
			// Now remove all the compartments the were not found
			if (pAllCompartments != null)
			{
				int count = pAllCompartments.size();

				for (int i = 0 ; i < count ; i++)
				{
					ICompartment pCompartment = pAllCompartments.get(i);
					if (pCompartment != null)
					{
						this.removeCompartment(pCompartment, false);
					}
				}
			}
		 }
		 else
		 {
			AddProcedureOrTransitionCompartment(pElement);
		 }
	}
	
	protected ICompartment FindCompartmentContainingElement(IElement pElement)
	{
		ICompartment pCompartment = null;
		
		if (pElement instanceof ITransition || pElement instanceof IProcedure)
		{
			pCompartment = super.findCompartmentContainingElement(pElement);
		}
		
		return pCompartment;
	}
	
	protected void AddProcedureOrTransitionCompartment(IElement pModelElement)
	{
		if (pModelElement instanceof ITransition || pModelElement instanceof IProcedure)
		{
			ETStateEventsAndTransitionsCompartment pStateEventsAndTransitionsCompartment = new ETStateEventsAndTransitionsCompartment();
			pStateEventsAndTransitionsCompartment.setEngine(this.getEngine());
			pStateEventsAndTransitionsCompartment.addModelElement(pModelElement, -1);
			ICompartment pCompartment = (ICompartment)pStateEventsAndTransitionsCompartment;
			super.addCompartment(pCompartment, 0, false);
		}
	}
	
	public void addStateEventsAndTransitionsButton(IMenuManager manager)
	{
            IElement pElem = getDrawEngineModelElement();
            if (pElem != null && pElem instanceof IState)
            {
                IState pState = (IState)pElem;
                IProcedure pEntry = pState.getEntry();
                IProcedure pExit = pState.getExit();
                IProcedure pDoActivity = pState.getDoActivity();
                
                IMenuManager subMenu = manager.createOrGetSubMenu(loadString("IDS_POPUP_STATE_EVENTS"), "");
                if (subMenu != null)
                {
                    subMenu.removeAll();
                    
                    if (pEntry == null)
                    {
                        subMenu.add(createMenuAction(loadString("IDS_INSERT_ENTRY"), "MBK_INSERT_ENTRY"));
                    }
                    else
                    {
                        subMenu.add(createMenuAction(loadString("IDS_DELETE_ENTRY"), "MBK_DELETE_ENTRY"));
                    }
                    
                    if (pExit == null)
                    {
                        subMenu.add(createMenuAction(loadString("IDS_INSERT_EXIT"), "MBK_INSERT_EXIT"));
                    }
                    else
                    {
                        subMenu.add(createMenuAction(loadString("IDS_DELETE_EXIT"), "MBK_DELETE_EXIT"));
                    }
                    
                    if (pDoActivity == null)
                    {
                        subMenu.add(createMenuAction(loadString("IDS_INSERT_DOACTIVITY"), "MBK_INSERT_DOACTIVITY"));
                    }
                    else
                    {
                        subMenu.add(createMenuAction(loadString("IDS_DELETE_DOACTIVITY"), "MBK_DELETE_DOACTIVITY"));
                    }
                    
                    subMenu.add(createMenuAction(loadString("IDS_INSERT_INCOMING_INTERNALTRANSITION"), "MBK_INSERT_INCOMING_INTERNALTRANSITION"));
                    subMenu.add(createMenuAction(loadString("IDS_INSERT_OUTGOING_INTERNALTRANSITION"), "MBK_INSERT_OUTGOING_INTERNALTRANSITION"));
                    subMenu.add(createMenuAction(loadString("IDS_DELETE_INTERNALTRANSITION"), "MBK_DELETE_INTERNALTRANSITION"));
                    
                    //manager.add(subMenu);
                }
            }
        }
}


