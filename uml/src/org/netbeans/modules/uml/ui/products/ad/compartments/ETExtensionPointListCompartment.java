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

import org.netbeans.modules.uml.core.metamodel.core.constructs.IExtensionPoint;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IUseCase;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.ui.products.ad.application.IMenuManager;
import org.netbeans.modules.uml.ui.products.ad.application.action.ContextMenuActionClass;
import org.netbeans.modules.uml.ui.support.applicationmanager.DrawingFactory;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;

/**
 * @author sumitabhk
 *
 */
public class ETExtensionPointListCompartment extends ETNamedElementListCompartment implements IADExtensionPointListCompartment
{

	/**
	 * 
	 */
	public ETExtensionPointListCompartment()
	{
		super();
	}

	/**
	 * Called when the context menu is about to be displayed.  
	 * The compartment should add whatever buttons it might need.
	 *
	 * @param pContextMenu [in] The context menu about to be displayed
	 * @param logicalX [in] The logical x location of the context menu event
	 * @param logicalY [in] The logical y location of the context menu event
	 */
	public void onContextMenu(IMenuManager manager)
	{
		if (getEnableContextMenu())
		{
			Point point = manager.getLocation();
                        
                        // (LLS) Adding the buildContext logic to support A11Y issues.  The
                        // user should be able to use the Shift-F10 keystroke to activate
                        // the context menu.  In the case of the keystroke the location
                        // will not be valid.  Therefore, we have to just check if the
                        // compartment is selected.  
                        //
                        // A list compartment can not be selected.  Therefore, when
                        // CTRL-F10 is pressed, we must always show the list compartment
                        // menu items.
                        boolean buildMenu = true;
                        if(point != null)
                        {
                            buildMenu = containsPoint(point);
                        } 
                        else // point == null means, users user key stroke to bring up the context menu,    
                        {
                            // check if extension point exists, is not, set buildMenu for extension point to false
                            if (getNumCompartments() == 0)
                            {
                                buildMenu = false;
                            }
                        }
                        
                        if (buildMenu == true)
			{
				// Add any buttons this compartment needs and then pass the message
				// to the sub compartments
				int count = getNumCompartments();
				for (int i=0; i<count; i++)
				{
					ICompartment pComp = getCompartment(i);
					pComp.onContextMenu(manager);
				}
				
				//add other context menu items
				manager.add(createMenuAction(loadString("IDS_POPUP_INSERT_EXTENSIONPOINT"), "MBK_INSERT_EXTENSIONPOINT"));
				manager.add(createMenuAction(loadString("IDS_POPUP_DELETE_EXTENSIONPOINT"), "MBK_DELETE_EXTENSIONPOINT"));
			}
		}
	}

	/**
	 * Adds an extension point compartment.  The compartment is always made visible.
	 *
	 * @param pCompartment - The compartment to add.  If NULL then a new compartment is created and the editor invoked.
	 * If non-null the compartment is simply inserted into the list.
	 * @param nIndex - The position in the visible list to place this compartment.  If blank or -1 the compartment is
	 * added to the bottom of the list.
	 */
	public long addCompartment(ICompartment pCompartment, int nIndex, boolean bRedrawNow)
	{
		if (pCompartment instanceof IADExtensionPointCompartment)
		{
			//call super from ListCompartmentImpl
			super.addCompartment(pCompartment, nIndex, bRedrawNow);
		}
		else
		{
			IDrawEngine pEngine = m_engine;
			IUseCase pUseCase = null;
			if (pEngine != null)
			{
				IElement pElem = TypeConversions.getElement(pEngine);
				if (pElem instanceof IUseCase)
				{
					pUseCase = (IUseCase)pElem;
				}
			}
			
			if (pUseCase != null)
			{
				// create extension point
				IElement pCreatedEle = DrawingFactory.retrieveModelElement("ExtensionPoint");
				if (pCreatedEle != null && pCreatedEle instanceof IExtensionPoint)
				{
					IExtensionPoint pExtPoint = (IExtensionPoint)pCreatedEle;
					
					// now add extension point to the use case
					pUseCase.addExtensionPoint(pExtPoint);
					
					// Call base class to create a new compartment at the insert position
					ICompartment pCreatedComp = createAndAddCompartment("ADExtensionPointCompartment", nIndex, bRedrawNow);
					if (pCreatedComp != null)
					{
						// This routine will initialize the compartment, make it visible and
						// begin the edit on it.
						finishAddCompartment(pEngine, pExtPoint, pCreatedComp, bRedrawNow);
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
		if (id.equals("MBK_INSERT_EXTENSIONPOINT"))
		{
			// Always sensitive, unless the diagram is readonly
			bFlag = true;
		}
		else if (id.equals("MBK_DELETE_EXTENSIONPOINT"))
		{
			bFlag = getHasSelectedCompartments();
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
		if (id.equals("MBK_INSERT_EXTENSIONPOINT"))
		{
			addCompartment(null, getRightMouseButtonIndex(), true);
		}
		else if (id.equals("MBK_DELETE_EXTENSIONPOINT"))
		{
			deleteSelectedCompartments(true);
		}
		return retval;
	}

	/**
	 * This is the name of the drawengine used when storing and reading from the product archive.
	 *
	 * @param sID[out,retval] The unique name for this compartment.  Used when reading and writing the
	 * product archive (etlp file).
	 */
	public String getCompartmentID()
	{
		return "ADExtensionPointListCompartment";
	}
	
	public void addModelElement(IElement pElement, int pIndex)
	{
		// don't allow re-entrant adds
		if (m_engine != null)
		{
			IADExtensionPointCompartment pNewCompartment = new ETExtensionPointCompartment();
			if (pNewCompartment != null)
			{
				pNewCompartment.setEngine(m_engine);
				pNewCompartment.addModelElement( pElement,-1 );
				super.addCompartment( pNewCompartment, pIndex, false);
			}
		}
	}
	
	public long modelElementHasChanged(INotificationTargets pTargets)
	{
		if (pTargets != null)
		{
			IElement pSecondaryElement = pTargets.getSecondaryChangedModelElement();
			String sElementType = "";
			if (pSecondaryElement != null)
			{
				sElementType = pSecondaryElement.getElementType();
			}

			if( sElementType.equals("ExtensionPoint") )
			{
				modelElementHasChanged2(pTargets);
			}
		}
		
		return 0;
	}
}



