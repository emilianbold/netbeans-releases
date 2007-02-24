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
import java.util.Iterator;
import java.util.List;

import org.netbeans.modules.uml.common.ETException;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.ui.products.ad.application.IMenuManager;
import org.netbeans.modules.uml.ui.products.ad.application.action.ContextMenuActionClass;
import org.netbeans.modules.uml.ui.products.ad.diagramengines.DiagramEngineResources;
import org.netbeans.modules.uml.ui.products.ad.diagramengines.IDrawingAreaAcceleratorKind;
import org.netbeans.modules.uml.ui.products.ad.drawengines.ETDrawEngineFactory;
import org.netbeans.modules.uml.ui.products.ad.drawengines.INodeDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import javax.swing.Action;
import javax.swing.KeyStroke;

/**
 * @author Embarcadero Technologies Inc.
 *
 * 
 */
public class ETOperationListCompartment extends ETNamedElementListCompartment implements IADOperationListCompartment
{

	public ETOperationListCompartment() {
		super();
		this.init();
	}

	public ETOperationListCompartment(IDrawEngine pDrawEngine) {
		super(pDrawEngine);
		this.init();
	}

	private void init() {
		this.initResources();
	}

	public void initResources() {
		this.setName(DiagramEngineResources.getString("IDS_OPERATIONSCOMPARTMENTTITLE"));
		super.initResources();
	}

	// TODO need to use IElement instead of ICompartment
	public boolean handleLeftMouseDrop(IETPoint pCurrentPos, List pCompartments, boolean bMoving) {
		boolean eventHandled = false;
		int insertionPoint = -1;

		if (this.getReadOnly())
			return true;

		INodeDrawEngine nodeDrawEngine = (INodeDrawEngine) this.getEngine();
		IClassifier targetClassifier = nodeDrawEngine.getParentClassifier();

		ICompartment targetCompartment = this.getCompartmentAtPoint(pCurrentPos);

		if (targetCompartment != null) {
			insertionPoint = this.getCompartmentIndex(targetCompartment);
		}

		Iterator iterator = pCompartments.iterator();
		while (iterator.hasNext()) {
			ICompartment sourceCompartment = (ICompartment) iterator.next();

			// Insert only compartments of the same kind
			if (sourceCompartment instanceof ETClassOperationCompartment) {
				try {

					IElement sourceElement = sourceCompartment.getModelElement();

					if (sourceElement instanceof IFeature) {

						IFeature sourceFeature = (IFeature) sourceElement;

						// check if we're dropping on ourselves
						IClassifier sourceClassifier = sourceFeature.getFeaturingClassifier();

						// dropping on ourselves, perform an index move instead
						if (sourceClassifier != null && targetClassifier != null && sourceClassifier.getXMIID().equals(targetClassifier.getXMIID())) {
							ICompartment foundCompartment = this.findCompartmentContainingElement(sourceElement);
							this.moveCompartment(foundCompartment, insertionPoint, false);

						} else {
							if (sourceFeature != null) {
								if (bMoving) {
									sourceFeature.moveToClassifier(targetClassifier);
									// refresh source node affected by the move operation
									sourceCompartment.getEngine().init();
									sourceCompartment.getEngine().invalidate();
								} else {
									sourceFeature.duplicateToClassifier(targetClassifier);
								}
							}
							//refresh the target node affected by the move/copy operation;
							this.getEngine().init();
							this.getEngine().invalidate();
						}
					}
					eventHandled = true;
				} catch (ETException e) {
					e.printStackTrace();
				}
			}
		}

		return eventHandled;
	}

	public void addModelElement(IElement pElement, int pIndex) {
		try {
			ICompartment newCompartment = ETDrawEngineFactory.createCompartment(ETDrawEngineFactory.CLASS_OPERATION_COMPARTMENT);
			if (newCompartment != null) {
				newCompartment.setEngine(this.getEngine());
				newCompartment.addModelElement(pElement, -1);
				this.addCompartment(newCompartment, pIndex, true);
			}
		} catch (ETException e) {
			e.printStackTrace();
		}
	}

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.products.ad.compartments.IADOperationListCompartment#insertParameterAtSelectedOperation()
    */
   public boolean insertParameterAtSelectedOperation()
   {
      // TODO Auto-generated method stub
      return false;
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
   			Point point = manager.getLocation();
                        
                        // (LLS) Adding the buildContext logic to support A11Y issues.  The
                        // user should be able to use the CTRL-F10 keystroke to activate
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
                        
                        if (buildMenu == true)
   			{
   				int count = getNumCompartments();
   				for (int i=0; i<count; i++)
   				{
   					ICompartment pComp = getCompartment(i);
   					pComp.onContextMenu(manager);
   				}
   				
   				manager.add(createMenuAction(loadString(
						"IDS_POPUP_INSERT_OPERATION"), "MBK_INSERT_OPERATION",
						IDrawingAreaAcceleratorKind.DAVK_CREATE_OPERATION));
   				if (count > 0)
   				{
   					manager.add(createMenuAction(loadString("IDS_POPUP_DELETE_OPERATION"), "MBK_DELETE_OPERATION"));
   				}
   			}
   		}
   }

   /**
	* Adds an operation compartment.  The compartment is always made visible and the in-place editor invoked.
	*
	* @param pCompartment The compartment to add.  If NULL then a new compartment is created and the editor invoked.
	* @param nIndex The position in the visible list to place this compartment.  If blank or -1 the compartment is
	* added to the bottom of the list
	*/
   public long addCompartment(ICompartment pCompartment, int nIndex, boolean bRedrawNow)
   {
		if (pCompartment instanceof IADClassOperationCompartment) {
			//call it from ListCompartmentImpl
			super.addCompartment(pCompartment, nIndex, bRedrawNow);
		} else {
			IDrawEngine pEngine = m_engine;
			IClassifier pClassifier = null;
			if (pEngine != null) {
				pClassifier = TypeConversions.getClassifier(pEngine);

				IOperation pOper = null;
				if (pClassifier != null) {
					// create operation
					pOper = pClassifier.createOperation3();

					// add the Operation to the classifer
					pClassifier.addOperation(pOper);
				}

				if (pOper != null) {
					// Call base class to create a new compartment at the insert position
					ICompartment pCreatedComp =
						createAndAddCompartment(
							"ADClassOperationCompartment",
							nIndex,
							bRedrawNow);
					if (pCreatedComp != null) {
						// This routine will initialize the compartment, make it visible and
						// begin the edit on it.
						finishAddCompartment(
							pEngine,
							pOper,
							pCreatedComp,
							bRedrawNow);
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
	   if (id.equals("MBK_INSERT_OPERATION"))
	   {
		   // Always sensitive, unless the diagram is readonly
		   bFlag = true;
	   }
	   else if (id.equals("MBK_DELETE_OPERATION"))
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
	   if (id.equals("MBK_INSERT_OPERATION"))
	   {
		   addCompartment(null, getRightMouseButtonIndex(), true);
	   }
	   else if (id.equals("MBK_DELETE_OPERATION"))
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
	   return "ADOperationListCompartment";
   }

   /**
	* Notifier that the model element has changed.
	*
	* @param pTargets [in] Information about what has changed.
	*/
   public long modelElementHasChanged(INotificationTargets pTargets) 
   {
	   if (pTargets != null)
	   {
		   IElement pSecEle = pTargets.getSecondaryChangedModelElement();
		   if (pSecEle != null)
		   {
			   String elemType = pSecEle.getElementType();
			   if (elemType != null && elemType.equals("Operation"))
			   {
				   modelElementHasChanged2(pTargets);
			   }
		   }
	   }
	   return 0;
   }

   public void deleteSelectedCompartments(boolean bPrompt) 
   {
	   String sTitle = DiagramEngineResources.getString("IDS_POPUP_DELETE_OPERATION_TITLE");
	   String sText = DiagramEngineResources.getString("IDS_DELETE_OPERATION");
	   deleteSelectedCompartments( sTitle, sText, bPrompt );
   }
}
