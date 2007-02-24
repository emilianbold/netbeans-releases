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



package org.netbeans.modules.uml.ui.products.ad.drawEngineManagers;

import java.awt.Point;
import java.awt.event.ActionEvent;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.metamodel.core.foundation.BaseElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.primitivetypes.IInteractionOperator;
import org.netbeans.modules.uml.core.metamodel.core.primitivetypes.IMessageKind;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline;
import org.netbeans.modules.uml.core.metamodel.dynamics.IMessage;
import org.netbeans.modules.uml.core.metamodel.dynamics.IMessageConnector;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.products.ad.application.IMenuManager;
import org.netbeans.modules.uml.ui.products.ad.application.action.ContextMenuActionClass;
import org.netbeans.modules.uml.ui.products.ad.diagramengines.IADCollaborationDiagEngine;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETLabel;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TSLabelKind;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TSLabelPlacementKind;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import com.tomsawyer.editor.TSEEdge;
import com.tomsawyer.editor.TSEGraphWindow;
import com.tomsawyer.editor.TSENode;
//import com.tomsawyer.util.TSConstPoint;
import com.tomsawyer.drawing.geometry.TSConstPoint;
//import com.tomsawyer.util.TSConstRect;
import com.tomsawyer.drawing.geometry.TSConstRect;
//import com.tomsawyer.util.TSPoint;
import com.tomsawyer.drawing.geometry.TSPoint;
//import com.tomsawyer.util.TSRect;
import com.tomsawyer.drawing.geometry.TSRect;
//import com.tomsawyer.util.TSTransform;
import com.tomsawyer.editor.TSTransform;

public class MessageConnectorLabelManager extends ADLabelManager implements IMessageConnectorLabelManager {

	public static int CMPK_START = 0;
	public static int CMPK_END = 1;
	public static int CMPK_MIDDLE = 2;

	/// When a context menu is displayed this is where the point is located
	private int m_ContextMenuLocation;

	/// This is the node closest to the mouse
	private TSENode m_NodeClosestToPoint;
	private IMessageContextMenu m_MessageContextMenu = (IMessageContextMenu)(new MessageContextMenu(this));

	// Get the name of this interface
	public String getText() {
		return "";
	}

	public MessageConnectorLabelManager() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager#modelElementHasChanged(org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets)
	 */
	public void modelElementHasChanged(INotificationTargets pTargets) {
		/// For now just reset all the label text
		resetLabelsText();
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager#onContextMenu(org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu, int, int)
	 */
	public void onContextMenu(IMenuManager pContextMenu)
	{
		super.onContextMenu(pContextMenu);
		m_ContextMenuLocation = CMPK_MIDDLE;

		// Get location
		IDrawingAreaControl control = getDrawingArea();
		TSEGraphWindow window = control.getGraphWindow();
		TSTransform transform = window.getTransform();
		Point pointLoc = pContextMenu.getLocation();
                
                // (LLS) When the user should be able to use the SHIFT-F10 
                // keystroke to activate the context menu.  In the case of the 
                // keystroke the location  will not be valid.  Therefore, we 
                // have to just check if the compartment is selected.
                //
                // A list compartment can not be selected.  Therefore, when
                // SHIFT-F10 is pressed, we must always show the list compartment
                // menu items.
                double x = 0;
		double y = 0;
                if(pointLoc != null)
                {
                    x = transform.xToWorld(pointLoc.x);
		    y = transform.yToWorld(pointLoc.y);
                }		
		
		// See what side of the edge we're on
		TSEEdge pTSEEdge = this.getOwnerEdge();
		TSRect boundingRect = new TSRect(pTSEEdge.getLocalBounds());
		TSRect scaledBoundingRect;

		if (pTSEEdge != null && boundingRect != null) {
			scaledBoundingRect = boundingRect;
			scaledBoundingRect.setHeight(boundingRect.getHeight() / 4);
			scaledBoundingRect.setWidth(boundingRect.getWidth() / 4);

			// Get the current point
			TSPoint pt = new TSPoint(x, y);

			// We scale the bounding rect by 1/2.  If the point is inside the scaled rect
			// then we consider that we're too close to the middle to determine the edge
			if (scaledBoundingRect.contains(pt) == false) {
				// We're on the border of the edge.  Find what side.
				TSConstPoint fromPoint = pTSEEdge.getSourcePoint();
				TSConstPoint toPoint = pTSEEdge.getTargetPoint();

				if (fromPoint != null && toPoint != null) {
					double fromPointDistance = fromPoint.distanceSquared(pt);
					double toPointDistance = toPoint.distanceSquared(pt);

					if (fromPointDistance < toPointDistance) {
						m_ContextMenuLocation = CMPK_START;
						m_NodeClosestToPoint = (TSENode) pTSEEdge.getSourceNode();
					} else {
						m_ContextMenuLocation = CMPK_END;
						m_NodeClosestToPoint = (TSENode) pTSEEdge.getTargetNode();
					}
				}
			}
		}

		this.addOperationsToContextMenu(pContextMenu);

	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager#onContextMenuHandleSelection(org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu, org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem)
	 */
	public void onContextMenuHandleSelection(IProductContextMenu pContextMenu, IProductContextMenuItem pMenuItem) {

		int menuSelected = m_ButtonHandler.getMenuButtonClicked(pMenuItem);

		switch (menuSelected) {
			case MenuButtonKind.MBK_OPERATION_NEW :
				{
					createNewOperation(m_NodeClosestToPoint);
				}
				break;

			default :
				if (menuSelected >= MenuButtonKind.MBK_OPERATION_START) {
					selectOperation(menuSelected - MenuButtonKind.MBK_OPERATION_START);
				}
				break;
		}

		// Make sure the message's operations are cleared.  This is only a precaution
		m_MessageContextMenu.cleanUp();

	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager#createInitialLabels()
	 */
	public void createInitialLabels() {

		IMessageConnector pMessageConnector = getMessageConnector();

		if (pMessageConnector != null) {
			boolean bCurrentlyShown = isDisplayed(TSLabelKind.TSLK_STEREOTYPE);

			if (!bCurrentlyShown) {

				// Get the text to be displayed
				String sName = getText();

				if (sName != null && sName.length() > 0) {
					// We have an IMessageConnector!
					createLabelIfNotEmpty(sName, TSLabelKind.TSLK_STEREOTYPE, TSLabelPlacementKind.TSLPK_CENTER_BELOW, null);
				}
			}

			bCurrentlyShown = isDisplayed(TSLabelKind.TSLK_MESSAGECONNECTOR_OPERATION_NAME);

			if (!bCurrentlyShown) {
				// Now discover any messages on the message connector,
				// and create a label for each of them
				ETList < IMessage > cpMessages = pMessageConnector.getMessages();

				if (cpMessages != null) {
					int lCnt = 0;
					lCnt = cpMessages.size();

					for (int lIndx = 0; lIndx < lCnt; lIndx++) {
						IMessage cpMessage = cpMessages.get(lIndx);

						if (cpMessage != null) {
							// Fix W7336:  Don't create labels for result messages
							int kind  = cpMessage.getKind();
							if (IMessageKind.MK_RESULT != kind) {
								createNewLabel(cpMessage, false);
							}
						}
					}
				}
			}
		}

		// Make sure the text is ok
		resetLabelsText();

	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager#handleEditNoChange(org.netbeans.modules.uml.ui.support.viewfactorysupport.IETLabel, java.lang.String)
	 */
	public void handleEditNoChange(IETLabel pLabel, String sNewString) {

		// Fix W6534:  This ensures that if the text does not change we still see the message number
		resetLabelsText();

	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager#isValidLabelKind(int)
	 */
	public boolean isValidLabelKind(int nLabelKind) {
		return nLabelKind == TSLabelKind.TSLK_STEREOTYPE || nLabelKind == TSLabelKind.TSLK_MESSAGECONNECTOR_OPERATION_NAME;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager#resetLabels()
	 */
	public void resetLabels() {
		
		super.resetLabels();
		
		//		HRESULT hr = S_OK;
		//		try
		//		{
		//	 #if 0
//		resetLabelsText();

		// Relayout the labels
//		relayoutLabels();
		//	 #else
		//		   // Just delete all the existing labels and recreate.
		//		   _VH(DiscardAllLabels());
		//		   _VH(CreateInitialLabels());
		//		   _VH(Invalidate());
		//	 #endif
		//		}
		//		catch ( _com_error& err )
		//		{
		//		   hr = COMErrorManager::ReportError( err );
		//		}
		//		return hr;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager#resetLabelsText()
	 */
	public void resetLabelsText() {

		boolean done = false;
		int index = 0;
		boolean bDoLayout = false;

		// Go through all the product labels and re-get their text.
		while (!done) {
			IETLabel pETLabel = getETLabelbyIndex(index);

			if (pETLabel != null) {
				String sText = "";

				int nLabelKind = pETLabel.getLabelKind();

				if (nLabelKind == TSLabelKind.TSLK_STEREOTYPE) {
					sText = getStereotypeText();
				} else if (nLabelKind == TSLabelKind.TSLK_MESSAGECONNECTOR_OPERATION_NAME) {
					// Get the message of the operation
					IPresentationElement pPE = null;
					IElement pElement = null;

					pPE = pETLabel.getPresentationElement();

					if (pPE != null) {
						pElement = pPE.getFirstSubject();
					}

					IMessage pMessage = (IMessage) pElement;

					if (pMessage != null) {
						String text = getMessageNumberPrefix(pMessage);
						text += m_MessageContextMenu.getMessagesOperationText(pMessage);

						if (text.length() > 0) 
						{
							sText = text;
						}
					}
				}

				// Here's where we set the text of the label
				String sOldText = pETLabel.getText();

				if (sText.length() > 0) {
					if (!(sText.equals(sOldText))) {
						pETLabel.setText(sText);
						pETLabel.reposition();
						bDoLayout = true;
					}

					pETLabel.sizeToContents();
				} else {
					// If there is no text then remove the label
					removeETLabel(index);
				}
			} else {
				done = true;
			}
			index++;
		}

		if (bDoLayout) {
			// Relayout the labels
			relayoutLabels();
		}

		invalidate();

	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager#setSensitivityAndCheck(org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu, org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem, int)
	 */
	public boolean setSensitivityAndCheck(String menuID, ContextMenuActionClass pMenuAction)
	{
		boolean bEnabled = !this.isParentDiagramReadOnly();
		if (bEnabled)
		{
			String strOperationsGroup = StringUtilities.replaceSubString(LabelManager.loadString("IDS_OPERATIONS_GROUP_DSCR"), "%s", "");
			if (menuID.indexOf(strOperationsGroup) >= 0)
			{
				bEnabled = false;
			}
		}
		return bEnabled;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager#showLabel(int, boolean)
	 */
	public void showLabel(int nLabelKind, boolean bShow) {

		// See if it's already shown
		boolean bCurrentlyShown = isDisplayed(nLabelKind);

		if ((bCurrentlyShown && bShow) || (!bCurrentlyShown && !bShow)) {
			// We have nothing to do!
		} else {
			if (nLabelKind == TSLabelKind.TSLK_STEREOTYPE) {
				if (bShow) {
					showStereotypeLabel();
				} else {
					discardLabel(nLabelKind);
					invalidate();
				}
			}
		}

	}

	// Gets the message connector the edge represents
	protected IMessageConnector getMessageConnector() {
		IMessageConnector pConnector = null;

		IElement pElement = getModelElement();
		if (pElement != null) {
			pConnector = (IMessageConnector) pElement;
		}

		return pConnector;
	}

	// Add all the available operations to the input context menu.
	protected void addOperationsToContextMenu(IMenuManager pContextMenu) {

		boolean bDefaultSensitivity = !this.isParentDiagramReadOnly();

		if (m_NodeClosestToPoint != null && (m_ContextMenuLocation == CMPK_START || m_ContextMenuLocation == CMPK_END)) {
			// Get the classifier at the endpoint
			IClassifier cpClassifier = getClassiferAtNode(m_NodeClosestToPoint);

			if (cpClassifier != null) {
				try {
					m_MessageContextMenu.addOperationsPullRight(cpClassifier, pContextMenu);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}

	// Creates a new operation
	protected void createNewOperation(TSENode pNode) {

		IMessageConnector pMessageConnector = getMessageConnector();
		IClassifier pClassifier = getClassiferAtNode(pNode);

		if (pMessageConnector != null) {
			if (pClassifier != null) {
				// Create an operation and add it to the classifier
				IOperation cpOperation = pClassifier.createOperation3();

				if (cpOperation != null) {
					IMessage pCreatedMessage;

					pClassifier.addOperation(cpOperation);

					// Add the operation to a message and then add that message to the 
					// message connector.
					int nDirection = IMessage.MDK_FROM_TO;

					if (isFromLifeline(pNode)) {
						nDirection = IMessage.MDK_TO_FROM;
					}

					pCreatedMessage = pMessageConnector.addMessage(nDirection, cpOperation);
					if (pCreatedMessage != null) {
						createNewLabel(pCreatedMessage, true);
					}
				}
			}
		}

	}

	// Returns the classifier at the end point
	protected IClassifier getClassiferAtNode(TSENode pNode) {

		IClassifier pClassifier = null;

		if (pNode != null) {
			IElement pElement = TypeConversions.getElement(pNode);

			ILifeline pLifelineElement = pElement instanceof ILifeline ? (ILifeline) pElement : null;

			if (pLifelineElement != null) {
				pClassifier = pLifelineElement.getRepresentingClassifier();
			}
		}

		return pClassifier;
	}

	// Creates a new label
	protected void createNewLabel(IMessage pMessage, boolean bEditLabel) {

		if (pMessage != null) {
			String bstrOperationText = m_MessageContextMenu.getMessagesOperationText(pMessage);

			if (bstrOperationText != null && bstrOperationText.length() > 0) {
				// Create the label
				IETLabel cpETLabel = createLabel(bstrOperationText, TSLabelKind.TSLK_MESSAGECONNECTOR_OPERATION_NAME, TSLabelPlacementKind.TSLPK_CENTER_ABOVE, pMessage);

				if (bEditLabel && cpETLabel != null) {
					postEditLabel(cpETLabel);
				}
			}
		}
	}

	// Select the operation from the recieving operations to be associated with the message
	protected void selectOperation(long lOperationIndx) {

		IMessageConnector pConnector = getMessageConnector();

		if (pConnector != null) {

			IOperation cpOperation = null;
			try {
				cpOperation = m_MessageContextMenu.selectOperation(lOperationIndx);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (cpOperation != null) {
				int nDirection = IMessage.MDK_FROM_TO;

				if (isFromLifeline(m_NodeClosestToPoint)) {
					nDirection = IMessage.MDK_TO_FROM;
				}

				IMessage cpCreatedMessage = pConnector.addMessage(nDirection, cpOperation);
				if (cpCreatedMessage != null) {
					createNewLabel(cpCreatedMessage, false);
				}
			}
		}
	}

	// Returns true if this node is the from lifeline
	protected boolean isFromLifeline(TSENode pNode) {
		boolean bIsFromConnector = false;

		if (pNode != null) {
			IElement pElement = TypeConversions.getElement(pNode);
			IMessageConnector pMessageConnector = getMessageConnector();

			ILifeline pLifelineElement = (ILifeline) pElement;

			if (pMessageConnector != null && pLifelineElement != null) {
				ILifeline pFromLifeline = pMessageConnector.getFromLifeline();

				if (pFromLifeline != null) {
					boolean bIsSame = false;
					bIsSame = pFromLifeline.isSame(pLifelineElement);
					bIsFromConnector = bIsSame ? true : false;
				}
			}
		}

		return bIsFromConnector;
	}

	// Retrieves the value of the diagram's ShowMessageNumbers property
	protected boolean isMessageNumberShown() {

		boolean bIsMessageNumberShown = false;

		IDiagram cpDiagram = null;

		//	  #if _MSC_VER < 1300 // Remove after we've totally gone to .NET
		//		 _VH( CTypeConversions::GetDiagram( m_RawParentETElement, &cpDiagram ));
		//	  #else
		cpDiagram = TypeConversions.getDiagram(m_rawParentETGraphObject);
		//	  #endif
		if (cpDiagram != null) {
			IDiagramEngine cpDiagramEngine = TypeConversions.getDiagramEngine(cpDiagram);

			IADCollaborationDiagEngine cpCODEngine = (IADCollaborationDiagEngine) cpDiagramEngine;

			if (cpCODEngine != null) {
				boolean bShowMessageNumbers = false;
				bShowMessageNumbers = cpCODEngine.showMessageNumbers();
				bIsMessageNumberShown = (bShowMessageNumbers != false);
			}
		}

		return bIsMessageNumberShown;
	}

	// Retrieves the prefix to be displayed if the message should display a previx
	protected String getMessageNumberPrefix(IMessage pMessage) {
		String bstrPrefix = "";

		if( isMessageNumberShown() )
		{
		   String bstrNumber = getNumber( pMessage );
		   
		   if(bstrNumber != null && bstrNumber.length() > 0 )
		   {
			  bstrPrefix = bstrNumber + ": ";
		   }
		}

		return bstrPrefix;
	}

	// Returns the message number
	protected String getNumber(IMessage pMessage) {
		
		String bstrNumber = "";

		 if( pMessage != null)
		 {
			String bsAutoNumber = pMessage.getAutoNumber();
			
			if(bsAutoNumber != null && bsAutoNumber.length() > 0 )
			{

			   ETPairT < Integer, String > result = pMessage.getRecurrence();

			   int op = result.getParamOne().intValue();
			   String bsRecurrence = result.getParamTwo();
			   			   
			   if(bsRecurrence != null && bsRecurrence.length() > 0 )
			   {
				  String strNumber;

				  switch( op )
				  {
				  default:
//					 ATLASSERT( false );  // should we support this interaction operator differently?
					 // fall through

				  case IInteractionOperator.IO_ALT:
				  strNumber = "[" + bsRecurrence + "] " + bsAutoNumber;
					 break;

				  case IInteractionOperator.IO_LOOP:
					 strNumber = "*[" + bsRecurrence + "] " + bsAutoNumber;
					 break;

				  case IInteractionOperator.IO_PAR:
					 strNumber = "*||[" + bsRecurrence + "] " + bsAutoNumber;
					 break;
				  }

				  bstrNumber = strNumber;
			   }
			   else
			   {
				  bstrNumber = bsAutoNumber;
			   }
			}
		 }

		 return bstrNumber;
	}

	// This is the node closest to the mouse
	TSENode m_nodeClosestToPoint;

	//	 CMessageContextMenu m_MessageContextMenu;

	public boolean onHandleButton(ActionEvent e, String id)
	{
		boolean retval = super.onHandleButton(e, id);

		if (id != null && id.equals("MBK_OPERATION_NEW"))
		{
			createNewOperation(m_NodeClosestToPoint);
		}
		else
		{
			String token = StringUtilities.replaceSubString(LabelManager.loadString("IDS_OPERATION_NUMBER"), "%d", "");
			String strID = StringUtilities.replaceSubString(id, token, "");
			selectOperation(Integer.parseInt(strID));
		}

		// Make sure the message's operations are cleared.  This is only a precaution
		m_MessageContextMenu.cleanUp();

		return retval;
	}
}

