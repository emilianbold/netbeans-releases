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

import java.awt.event.ActionEvent;

import org.netbeans.modules.uml.core.coreapplication.IPreferenceManager2;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.primitivetypes.IMessageKind;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IGraphEventKind;
import org.netbeans.modules.uml.core.metamodel.dynamics.IMessage;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.preferenceframework.PreferenceAccessor;
import org.netbeans.modules.uml.core.support.umlsupport.ETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.core.support.umlutils.IDataFormatter;
import org.netbeans.modules.uml.ui.controls.drawingarea.DiagramAreaEnumerations;
import org.netbeans.modules.uml.ui.controls.drawingarea.ITopographyChangeAction;
import org.netbeans.modules.uml.ui.controls.drawingarea.TopographyChangeAction;
import org.netbeans.modules.uml.ui.products.ad.application.IMenuManager;
import org.netbeans.modules.uml.ui.products.ad.application.action.BaseAction;
import org.netbeans.modules.uml.ui.products.ad.application.action.ContextMenuActionClass;
//import org.netbeans.modules.uml.ui.products.ad.application.action.Separator;
import org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.lifelinepieces.ConnectorPiece;
import org.netbeans.modules.uml.ui.products.ad.diagramengines.IADSequenceDiagEngine;
import org.netbeans.modules.uml.ui.products.ad.diagramengines.sequencediagram.IMessageEdgeDrawEngine;
import org.netbeans.modules.uml.ui.products.ad.diagramengines.sequencediagram.IShowMessageType;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.applicationmanager.IEdgePresentation;
import org.netbeans.modules.uml.ui.support.applicationmanager.ILabelPresentation;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETLabel;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TSLabelKind;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TSLabelPlacementKind;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import com.tomsawyer.drawing.TSLabel;
import com.tomsawyer.editor.TSEEdge;
//import com.tomsawyer.util.TSConstRect;
import com.tomsawyer.drawing.geometry.TSConstRect;

public class MessageLabelManager extends ADLabelManager {

	private IMessageContextMenu m_MessageContextMenu;


   public MessageLabelManager()
   {
      m_MessageContextMenu = new MessageContextMenu(this);
   }
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager#modelElementHasChanged(org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets)
	 */
	public void modelElementHasChanged(INotificationTargets pTargets) {
		resetLabelsText();
	}

	public ContextMenuActionClass createMenuAction(String text, String menuID, int style) {
		ContextMenuActionClass menu = new ContextMenuActionClass(this, text, menuID);
		if (menu != null) {
			menu.setStyle(style);
		}

		return menu;
	}

	public ContextMenuActionClass createMenuAction(String text, String menuID) {
		return new ContextMenuActionClass(this, text, menuID);
	}
	
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager#onContextMenu(org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu, int, int)
	 */
	public void onContextMenu(IMenuManager pContextMenu) {

		super.onContextMenu(pContextMenu);

		IETGraphObject cpParentETElement = this.getParentETGraphObject();

		if (cpParentETElement != null)
		{
			IElement cpElement = TypeConversions.getElement(cpParentETElement);

			IMessage cpMessage = (IMessage) cpElement;

			if (cpMessage != null)
			{
				try
				{
					m_MessageContextMenu.addOperationsPullRight(cpMessage, pContextMenu);
				} catch (Exception e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				// Add the buttons to show either the message or operation
				//m_ButtonHandler.addMessageLabelButtons(pContextMenu, bDefaultSensitivity);
				IMenuManager subMenu = pContextMenu.createOrGetSubMenu(loadString("IDS_LABELS_TITLE"), "");
				if (subMenu != null)
				{
					subMenu.add(createMenuAction(loadString("IDS_SHOW_OPERATION_NAME"), "MBK_SHOW_OPERATION_NAME", BaseAction.AS_CHECK_BOX));
					subMenu.add(createMenuAction(loadString("IDS_SHOW_MESSAGE_NAME"), "MBK_SHOW_MESSAGE_NAME", BaseAction.AS_CHECK_BOX));
					//subMenu.add(new Separator());
				}

				// Show/Hide the return message
				int nKind = IMessageKind.MK_UNKNOWN;

				cpMessage.getKind();

				if ((IMessageKind.MK_SYNCHRONOUS == nKind) || (IMessageKind.MK_RESULT == nKind)) {
//						m_ButtonHandler.addMenuItem(
//							pContextMenu,
//							MenuButtonKind.MBK_SHOW_RETURN,
//							m_ButtonHandler.loadString("IDS_SHOW_RETURN"),
//							m_ButtonHandler.loadString("IDS_SHOW_RETURN_DSCR"),
//							"MBK_SHOW_RETURN",
//							bDefaultSensitivity,
//							null);
				}
			}
		}
	}

	public boolean onHandleButton(ActionEvent e, String menuID)
	{
		if (menuID.equals("MBK_OPERATION_NEW"))
		{
			this.createNewOperation(true);
		}
		else if (menuID.equals("MBK_NEW_CONSTRUCTOR"))
		{
			createNewConstructor();
		}
		else if (menuID.equals("MBK_OPERATION_MORE"))
		{
			//	AfxMessageBox(_T("Selected more operations pick list, not yet implemented."));
		}
		else if (menuID.equals("MBK_SHOW_OPERATION_NAME"))
		{
			setShowMessageType((IShowMessageType.SMT_OPERATION == getShowMessageType()) ? IShowMessageType.SMT_NONE : IShowMessageType.SMT_OPERATION);

			resetLabels();
		}
		else if (menuID.equals("MBK_SHOW_MESSAGE_NAME"))
		{
			setShowMessageType((IShowMessageType.SMT_NAME == getShowMessageType()) ? IShowMessageType.SMT_NONE : IShowMessageType.SMT_NAME);

			resetLabels();
		}
		else if (menuID.equals("MBK_SHOW_RETURN"))
		{
			IMessageEdgeDrawEngine cpThisEngine = getDrawEngine();

			if (cpThisEngine != null)
			{
				IEdgePresentation cpEdgePresentation = cpThisEngine.getAssociatedResultMessage();

				if (cpEdgePresentation != null)
				{
					IDrawEngine cpResultEngine = TypeConversions.getDrawEngine(cpEdgePresentation);

					IMessageEdgeDrawEngine cpEngine = (IMessageEdgeDrawEngine) cpResultEngine;

					if (cpEngine != null)
					{
						boolean bShow = false;

						bShow = cpEngine.getShow();
						cpEngine.setShow(!bShow);

						// Tell the label manager to manage the labels based on the show state.
						// Note that this may be this object, or if we're the sync message it
						// might be the label manager of the return message.
						ILabelManager pLabelManager = cpEngine.getLabelManager();

						if (pLabelManager != null) {
							bShow = cpEngine.getShow();

							if (bShow) {
								pLabelManager.resetLabels();
							} else {
								pLabelManager.discardAllLabels();
							}
						}

						IETGraphObject cpProductETReturn = TypeConversions.getETGraphObject(cpEngine);

						if (cpProductETReturn != null)
						{
							cpProductETReturn.invalidate();
						}
					}
				}
			}
		}
		else
		{
			String token = StringUtilities.replaceSubString(LabelManager.loadString("IDS_OPERATION_NUMBER"), "%d", "");
			String strID = StringUtilities.replaceSubString(menuID, token, "");
			selectOperation(Integer.parseInt(strID));
		}

		// Make sure the message's operations are cleared.  This is only a precaution
		m_MessageContextMenu.cleanUp();
		
		return true;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager#createInitialLabels()
	 */
	public void createInitialLabels() {

		discardAllLabels();

		// Now make sure this element is a IMessage
		IMessage cpMessage = getMessage();
		IElement cpElement = null;

		if (cpMessage != null) {
			cpElement = (IElement) cpMessage;

			String bstrLabelText = "";

			boolean bPostEditLabel = false;

			int showmessagetype = getShowMessageType();

			if (IShowMessageType.SMT_NAME == showmessagetype) {
				// When the message's name is blank and is shown for the first time,
				// make sure the default message name is put in the label and
				// the label is put in edit mode.

				bstrLabelText = getMessageName(false);

				if (bstrLabelText == null || bstrLabelText.length() <= 0) {
					bstrLabelText = getMessageName(true);
					bPostEditLabel = true;
				}
			} else if (IShowMessageType.SMT_OPERATION == showmessagetype) {
				cpElement = null;

				IOperation cpOperation = cpMessage.getOperationInvoked();

				if (cpOperation != null) {
					// Make sure the proper element is associated with the label
					cpElement = cpOperation;
				} else if (getNewMessageAction().equals("PSK_CREATEOPERATION")) {
					cpElement = createNewOperation(false);
					bPostEditLabel = true;
				}

				bstrLabelText = getOperationText();
			} else {
				//				 ATLASSERT( SMT_NONE == showmessagetype );
			}

			// Create the auto-number label when the diagram has requested them
			{
				String bstrNewText = getMessageNumberPrefix() + bstrLabelText;

				bstrLabelText = bstrNewText;
			}

			if (bstrLabelText != null && bstrLabelText.length() > 0) {
				// Create the label
				IETLabel cpETLabel = createLabel(bstrLabelText, TSLabelKind.TSLK_MESSAGE_OPERATION_NAME, TSLabelPlacementKind.TSLPK_CENTER_ABOVE, cpElement);

				// Need to update the track bar with the new label
				if (cpETLabel != null) {
					updateTrackBar(cpETLabel);

					if (bPostEditLabel) {
						postEditLabel(cpETLabel);
					}
				}
			}
		}

		// Make sure the text is ok
		resetLabelsText();
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager#handleEditChange(org.netbeans.modules.uml.ui.support.viewfactorysupport.IETLabel, java.lang.String)
	 */
	public void handleEditChange(IETLabel pLabel, String sNewString) {

		int nLabelKind = TSLabelKind.TSLK_UNKNOWN;

		nLabelKind = pLabel.getLabelKind();

		switch (nLabelKind) {
			case TSLabelKind.TSLK_MESSAGE_OPERATION_NAME :
				updateLabelsText(pLabel);
				updateTrackBar(pLabel);
				break;

			case TSLabelKind.TSLK_MESSAGE_NUMBER :
				break;

			default :
				//			  ATLASSERT(0 && "Edited unknown label!");
				break;
		}

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

		boolean bIsValid = false;

		if (nLabelKind == TSLabelKind.TSLK_MESSAGE_OPERATION_NAME) {
			bIsValid = true;
		}
		return bIsValid;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IGraphObjectManager#onGraphEvent(int)
	 */
	public void onGraphEvent(int nKind) {

		switch (nKind) {
			case IGraphEventKind.GEK_PRE_DELETEGATHERSELECTED :
				onPreDeleteGatherSelected();
				break;

			case IGraphEventKind.GEK_PRE_DELETE :
				onPreDelete();
				break;

			default :
				break;
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager#relayoutLabels()
	 */
	public void relayoutLabels() {

			for (int lIndx = 0; /* break below */; lIndx++) {
			IETLabel cpETLabel = getETLabelbyIndex(lIndx);

			if (cpETLabel == null) {
				break;
			}

			relayoutThisLabel(cpETLabel);
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager#resetLabelsText()
	 */
	public void resetLabelsText() {

		boolean bDoLayout = false;

		// When the label's text is reset, it may be so that the message number will now get displayed
		// If so, and there is no label displayed then call ShowLabel to display a label, and return
		if (isMessageNumberShown()) {
			IETLabel cpETLabel = getETLabelbyIndex(0);

			if (cpETLabel == null) {
				IMessage cpMessage = getMessage();

				if (cpMessage != null) {
					// Result messages never have a number
					int nKind = IMessageKind.MK_UNKNOWN;

					nKind = cpMessage.getKind();

					if (nKind != IMessageKind.MK_RESULT) {
						showLabel(TSLabelKind.TSLK_MESSAGE_NUMBER, true);
					}
				}
			}
		}

			for (int lIndx = 0; /* break below */; lIndx++) {
			IETLabel cpETLabel = getETLabelbyIndex(lIndx);

			if (cpETLabel == null) {
				break;
			}

			// Get the text to be displayed
			String bstrText = getMessageNumberPrefix();

			int nLabelKind = TSLabelKind.TSLK_UNKNOWN;

			nLabelKind = cpETLabel.getLabelKind();

			switch (nLabelKind) {
				case TSLabelKind.TSLK_MESSAGE_OPERATION_NAME :
					bstrText += getText();
					break;

				case TSLabelKind.TSLK_MESSAGE_NUMBER :
					break;

				default :
					//				 ATLASSERT( false );  // Unknown Label Kind!
					break;
			}

			if (bstrText.length() > 0) {
				// Here's where we set the text of the label
				String sOldText = cpETLabel.getText();

            // For some reason on the Java side these string match during initialization
            // So, we just always set the text.
            cpETLabel.setText(bstrText);
				if (!bstrText.equals(sOldText))
            {
					cpETLabel.reposition();
					bDoLayout = true;
				}

				cpETLabel.sizeToContents();
			} else if (TSLabelKind.TSLK_MESSAGE_OPERATION_NAME == nLabelKind) {
				// If there is no text then remove the label
				removeETLabel(lIndx);
			}
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
		if (this.isParentDiagramReadOnly())
		{
			return false;
		}
		
		boolean bEnabled = true;

		if (menuID.equals("MBK_SHOW_OPERATION_NAME"))
		{
			bEnabled = !(IShowMessageType.SMT_OPERATION == getShowMessageType());
			pMenuAction.setChecked(!bEnabled);
		}
		else if (menuID.equals("MBK_SHOW_MESSAGE_NAME"))
		{
			bEnabled = !(IShowMessageType.SMT_NAME == getShowMessageType());
			pMenuAction.setChecked(!bEnabled);
		}
		else if (menuID.equals("MBK_SHOW_RETURN"))
		{
			bEnabled = isResultDrawEngineShown();
		}
		
		return bEnabled;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager#showLabel(int, boolean)
	 */
	public void showLabel(int nLabelKind, boolean bShow) {

		if (TSLabelKind.TSLK_MESSAGE_NUMBER == nLabelKind) {
			discardLabel(TSLabelKind.TSLK_MESSAGE_NUMBER);

			if (bShow) {
				// Make sure the label for the number exist
				IETLabel cpETLabel = getETLabelbyKind(TSLabelKind.TSLK_MESSAGE_OPERATION_NAME);

				if (cpETLabel == null) {
					createInitialLabels();
				}
			} else if (getText().length() <= 0) {
				// Remove the label if it about to be blank
				discardLabel(TSLabelKind.TSLK_MESSAGE_OPERATION_NAME);
			}
		}
	}

	// Layouts out just this layout
	protected void relayoutThisLabel(IETLabel pETLabel) {

		TSEEdge pTSEEdge = getOwnerEdge();

		if (pTSEEdge != null) {
			int nLabelKind = TSLabelKind.TSLK_UNKNOWN;

			nLabelKind = pETLabel.getLabelKind();

			switch (nLabelKind) {
				case TSLabelKind.TSLK_MESSAGE_OPERATION_NAME :
					{
						// There was a problem with the TS layout.  Often times the label
						// would be place above other elements on the diagram.
						// This was solved by doing our own label layout here.

						boolean bIsMessageToSelf = false;

						IMessageEdgeDrawEngine cpMessageEdgeDrawEngine = getDrawEngine();

						if (cpMessageEdgeDrawEngine != null) {
							bIsMessageToSelf = cpMessageEdgeDrawEngine.isMessageToSelf();
						}

						TSLabel cpLabel = pETLabel.getLabel();

						if (cpLabel != null) {
							TSConstRect tsrectEdge = pTSEEdge.getLocalBounds();

							IETPoint ptCenter = new ETPoint((int) tsrectEdge.getCenterX(), (int) tsrectEdge.getCenterY());

							if (bIsMessageToSelf) {
								// assume that this is a message to self, so put the label on the right side
								double lLabelWidth = 0;
								lLabelWidth = cpLabel.getWidth();
								ptCenter.setX((int) (ptCenter.getX() + ((tsrectEdge.getWidth() + lLabelWidth) / 2)));
							} else {
								double lLabelHeight = 0;
								lLabelHeight = cpLabel.getHeight();

								ptCenter.setY((int) (ptCenter.getY() + (lLabelHeight / 2 + 5)));
							}

							delayedMoveTo(pETLabel, ptCenter);
						}
					}
					break;

				default :
					//				 ATLASSERT( false );  // Unknown Label Kind!
					// no break;
				case TSLabelKind.TSLK_MESSAGE_NUMBER :
					{
						super.relayoutThisLabel(pETLabel);
					}
					break;
			}
		}

	}

	// Get the text for the message label based on the user's settings
	protected String getText() {
		String bstrText = "";

		if (isDrawEngineShown()) {
			switch (getShowMessageType()) {
				case IShowMessageType.SMT_OPERATION :
					bstrText = getOperationText();
					break;

				case IShowMessageType.SMT_NAME :
					bstrText = getMessageName(true);
					break;

				case IShowMessageType.SMT_NONE :
					break;

				case IShowMessageType.SMT_UNKNOWN :
				default :
					break;
			}
		}

		return bstrText;
	}

	protected String getMessageName(boolean bIfBlankUseDefault) {

		String bcsMessageName = null;

		IMessage cpMessage = getMessage();

		if (cpMessage != null) {
			// Get the data formatter off the product where it caches up
			// the various factories per language
			IDataFormatter cpFormatter = ProductHelper.getDataFormatter();

			if (cpFormatter != null) {
				bcsMessageName = cpFormatter.formatElement(cpMessage);

				if (bIfBlankUseDefault && (bcsMessageName == null || bcsMessageName.length() <= 0)) {
					// forces the default name for the message
					String sDefaultName = PreferenceAccessor.instance().getDefaultElementName();

					if (sDefaultName.length() > 0) {
						cpMessage.setName(sDefaultName);

						// Get the default name
						bcsMessageName = cpFormatter.formatElement(cpMessage);
					}
				}
			}
		}

		return bcsMessageName;
	}

	protected String getOperationText() {

		String bstrOperationText = "";

		IMessage cpMessage = getMessage();

		if (cpMessage != null) {
			bstrOperationText = m_MessageContextMenu.getMessagesOperationText(cpMessage);
		}

		return bstrOperationText;

	}

	protected String getNumber() {
		String bsAutoNumber = "";

		IMessage cpMessage = getMessage();

		if (cpMessage != null) {
			bsAutoNumber = cpMessage.getAutoNumber();
		}

		return bsAutoNumber;

	}

	// Retrieve the preference values
	protected String getNewMessageAction() {

		return getPreferenceValue("Diagrams|SequenceDiagram", "NewMessageAction");

	}

	protected String getPreferenceValue(String bstrPath, String bstrName) {

		String bsValue = "";

		IPreferenceManager2 cpMgr = ProductHelper.getPreferenceManager();

		if (cpMgr != null) {
			bsValue = cpMgr.getPreferenceValue(bstrPath, bstrName);
		}

		return bsValue;
	}

	// Handles the various dispatchers
	protected void onPreDeleteGatherSelected() {

	}

	protected void onPreDelete() {

		IETGraphObject cpParentETElement = this.getParentETGraphObject();

		if (cpParentETElement != null) {
			ConnectorPiece.deleteEdge( cpParentETElement );
		}

	}

	// Creates a new operation and its associated label
	protected IElement createNewOperation(boolean bUpdateOperationLabel) {

		IElement retValue = null;

		IMessage cpMessage = getMessage();

		if (cpMessage != null) {
			IClassifier cpClassifier = cpMessage.getReceivingClassifier();

			if (cpClassifier != null) {
				IOperation cpOperation = cpClassifier.createOperation3();

				if (cpOperation != null) {
					cpClassifier.addOperation(cpOperation);

					cpMessage.setOperationInvoked(cpOperation);

					if (bUpdateOperationLabel) {
						updateOperationLabel(cpOperation, true);
					}

					retValue = cpOperation;
				}
			}
		}

		return retValue;

	}

	// Creates a new constructor operation and its associated label
	protected void createNewConstructor() {

		IMessage cpMessage = getMessage();

		if (cpMessage != null) {
			IClassifier cpClassifier = cpMessage.getReceivingClassifier();

			if (cpClassifier != null) {
				IOperation cpOperation = cpClassifier.createConstructor();

				if (cpOperation != null) {
					cpClassifier.addOperation(cpOperation);

					cpMessage.setOperationInvoked(cpOperation);

					updateOperationLabel(cpOperation, true);
				}
			}
		}
	}

	// Creates the label for the message number
	protected void createMessageNumberLabel(IMessage pMessage) {

		IMessage cpMessage = (IMessage) pMessage;

		if (cpMessage == null) {
			cpMessage = getMessage();
		}

		if (cpMessage != null) {
			int eKind = IMessageKind.MK_UNKNOWN;

			eKind = cpMessage.getKind();

			if (IMessageKind.MK_RESULT != eKind) {
				IETLabel cpETLabel = createLabelIfNotEmpty(getNumber(), TSLabelKind.TSLK_MESSAGE_NUMBER, TSLabelPlacementKind.TSLPK_FROM_NODE_ABOVE, cpMessage);

				// Make the label read-only
				IDrawEngine cpDrawEngine = TypeConversions.getDrawEngine(cpETLabel);

				if (cpDrawEngine != null) {
					cpDrawEngine.setReadOnly(true);
				}
			}
		}
	}

	// Select the operation from the recieving operations to be associated with the message
	protected void selectOperation(int lOperationIndx) {

		IMessage cpMessage = getMessage();

		if (cpMessage != null) {
			IOperation cpOperation = null;
			try {
				cpOperation = m_MessageContextMenu.selectOperation(lOperationIndx);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (cpOperation != null) {
				cpMessage.setOperationInvoked(cpOperation);
				updateOperationLabel(cpOperation, false);
			}
		}
	}

	// Convert the parent product element to a message
	protected IMessage getMessage() {

		IMessage ppMessage = null;

		IETGraphObject cpParentETElement = this.getParentETGraphObject();

		if (cpParentETElement != null) {
			IElement cpElement = TypeConversions.getElement(cpParentETElement);

			IMessage cpMessage = (IMessage) cpElement;

			if (cpMessage != null) {
				ppMessage = cpMessage;

				if (IShowMessageType.SMT_UNKNOWN == getShowMessageType()) {
					int kind = IMessageKind.MK_UNKNOWN;

					kind = cpMessage.getKind();

					if (IMessageKind.MK_RESULT == kind) {
						this.setShowMessageType(IShowMessageType.SMT_NONE);
					} else {
						String bstrNewMessageAction = getNewMessageAction();

						if (bstrNewMessageAction.equals("PSK_NAMEMESSAGE")) {
							setShowMessageType(IShowMessageType.SMT_NAME);
						} else {
							setShowMessageType(IShowMessageType.SMT_OPERATION);
						}
					}
				}
			}
		}

		return ppMessage;

	}

	// Convert the parent product element to a message edge draw engine
	protected IMessageEdgeDrawEngine getDrawEngine() {

		IMessageEdgeDrawEngine ppEngine = null;

		IETGraphObject cpParentETElement = this.getParentETGraphObject();

		if (cpParentETElement != null) {
			IDrawEngine cpDrawEngine = TypeConversions.getDrawEngine(cpParentETElement);
			IMessageEdgeDrawEngine cpEngine = (IMessageEdgeDrawEngine) cpDrawEngine;

			if (cpEngine != null) {
				ppEngine = cpEngine;
			}
		}

		return ppEngine;
	}

	// Retrieves the value of the diagram's ShowMessageNumbers property
	protected boolean isMessageNumberShown() {

		boolean bIsMessageNumberShown = false;

		IDiagram cpDiagram = TypeConversions.getDiagram(m_rawParentETGraphObject);

		if (cpDiagram != null) {
			IDiagramEngine cpDiagramEngine = TypeConversions.getDiagramEngine(cpDiagram);

			IADSequenceDiagEngine cpSQDEngine = (IADSequenceDiagEngine) cpDiagramEngine;

			if (cpSQDEngine != null) {
				boolean bShowMessageNumbers = false;

				bShowMessageNumbers = cpSQDEngine.isShowMessageNumbers();

				bIsMessageNumberShown = (bShowMessageNumbers != false);
			}
		}

		return bIsMessageNumberShown;
	}

	// Retrieves the prefix to be displayed if the message should display a previx
	protected String getMessageNumberPrefix() {
		String bstrPrefix = "";

		if (isMessageNumberShown()) {
			String bstrNumber = getNumber();

			if (bstrNumber != null && bstrNumber.length() > 0) {
				bstrPrefix = bstrNumber + ": ";
			}
		}

		return bstrPrefix;
	}

	// Tests the draw engine's Show property
	protected boolean isDrawEngineShown() {

		boolean bIsDrawEngineShown = true;

		IMessageEdgeDrawEngine cpEngine = getDrawEngine();

		if (cpEngine != null) {
			boolean bShow = false;

			bShow = cpEngine.getShow();

			bIsDrawEngineShown = (bShow != false);
		}

		return bIsDrawEngineShown;
	}

	// Tests the result message draw engine's Show property
	protected boolean isResultDrawEngineShown() {

		boolean bIsResultDrawEngineShown = true;

		IMessageEdgeDrawEngine cpThisEngine = getDrawEngine();

		if (cpThisEngine != null) {
			IEdgePresentation cpEdgePresentation = cpThisEngine.getAssociatedResultMessage();

			if (cpEdgePresentation != null) {
				IDrawEngine cpResultEngine = TypeConversions.getDrawEngine(cpEdgePresentation);

				IMessageEdgeDrawEngine cpEngine = (IMessageEdgeDrawEngine) cpResultEngine;

				if (cpEngine != null) {
					boolean bShow = false;
					bShow = cpEngine.getShow();
					bIsResultDrawEngineShown = (bShow != false);
				}
			}
		}

		return bIsResultDrawEngineShown;

	}

	// Get the label associated with the operation/name of the message
	protected IETLabel getOperationLabel() {
		return null;
	}

	// Update the operation on the message, and the label
	protected void updateOperationLabel(IOperation pOperation, boolean bEditLabel) {

		if (pOperation != null) {
			IETLabel cpETLabel = null;

			// Fix W5436:  The label may have been the message name, this cleans that up
			setShowMessageType(IShowMessageType.SMT_OPERATION);

			createInitialLabels();

			cpETLabel = getETLabelbyKind(TSLabelKind.TSLK_MESSAGE_OPERATION_NAME);

			if (cpETLabel != null) {
				IPresentationElement cpPresentationElement = TypeConversions.getPresentationElement(cpETLabel);

				ILabelPresentation cpLabelPresentation = (ILabelPresentation) cpPresentationElement;

				if (cpLabelPresentation != null) {
					cpLabelPresentation.setModelElement(pOperation);

					updateTrackBar(cpETLabel);

					if (bEditLabel) {
						this.postEditLabel(cpLabelPresentation);
					}
				}
			}
		}
	}

	// Make sure the product label's text is updated with the latest operation
	protected void updateLabelsText(IETLabel pETLabel) {

		if (pETLabel != null) {
			// Get the model element and product label
			IElement cpElement = getModelElement();

			if (cpElement != null) {
				pETLabel.setText(getText());
			}

			invalidate();
		}
	}

	// Inform the track bar that the label has changed
	protected void updateTrackBar(IETLabel pETLabel) {

		if (pETLabel != null) {
			// Inform the diagram of the change
			IDrawingAreaControl pControl = this.getDrawingArea();

			if (pControl != null) {
				IPresentationElement cpPresentationElement = TypeConversions.getPresentationElement(pETLabel);

				if (cpPresentationElement != null) {
					pControl.postSimplePresentationDelayedAction(cpPresentationElement, DiagramAreaEnumerations.SPAK_UPDATE_TRACKBAR);
				}
			}
		}

	}

	// Access the IMessageEdgeDrawEngine's ShowMessageType property
	protected int getShowMessageType() {

		int type = IShowMessageType.SMT_UNKNOWN;

		IMessageEdgeDrawEngine cpEngine = getDrawEngine();

		if (cpEngine != null) {
			type = cpEngine.getShowMessageType();
		}

		return type;

	}

	protected void setShowMessageType(int type) {

		IMessageEdgeDrawEngine cpEngine = getDrawEngine();

		if (cpEngine != null) {
			cpEngine.setShowMessageType(type);
		}
	}

	// Post a moveto action because the method is dangerous in its current callstack.
	protected void delayedMoveTo(IETLabel pETLabel, IETPoint ptCenter) {

		//		if( pETLabel == NULL )
		//		{
		//		   throw _com_error( E_INVALIDARG );
		//		}

		IPresentationElement cpPE = TypeConversions.getPresentationElement(pETLabel);

		if (cpPE != null) {
			IDiagram cpDiagram = pETLabel.getDiagram();

			if (cpDiagram != null) {
				// Post a moveto action because the method is dangerous in this callstack.
				ITopographyChangeAction cpAction = new TopographyChangeAction();

				if (cpAction != null) {
					cpAction.setX(ptCenter.getX());
					cpAction.setY(ptCenter.getY());
					cpAction.setKind(DiagramAreaEnumerations.TAK_MOVETO);
					cpAction.setPresentationElement(cpPE);

					cpDiagram.postDelayedAction(cpAction);
				}
			}
		}
	}

	//	private:
	//	   CMessageContextMenu m_MessageContextMenu;

	//	class OperationCmp
	//	{
	//	   public:
	//		  bool operator() ( const CComPtr< IOperation > & cpOp1, const CComPtr< IOperation > & cpOp2 )
	//		  {
	//			 bool bLessThan = false;
	//
	//			 if( (cpOp1 != NULL) &&
	//				 (cpOp2 != NULL) )
	//			 {
	//				VisibilityKind visibilty1 = VK_PUBLIC;
	//				_VH( cpOp1->get_Visibility( &visibilty1 ));
	//
	//				VisibilityKind visibilty2 = VK_PUBLIC;
	//				_VH( cpOp2->get_Visibility( &visibilty2 ));
	//
	//				if( visibilty1 == visibilty2 )
	//				{
	//				   // sort by name
	//				   CComBSTR bsName1;
	//				   _VH( cpOp1->get_Name( &bsName1 ));
	//
	//				   CComBSTR bsName2;
	//				   _VH( cpOp2->get_Name( &bsName2 ));
	//
	//				   int iResult = 0;
	//               
	//				   if (bsName1.Length() && bsName2.Length())
	//				   {
	//					  iResult = _wcsicmp( bsName1, bsName2 );
	//
	//					  if( iResult == 0 )
	//					  {
	//						 bLessThan = (wcscmp( bsName1, bsName2 ) < 0);
	//					  }
	//					  else
	//					  {
	//						 bLessThan = (iResult < 0);
	//					  }
	//				   }
	//				}
	//				else
	//				{
	//				   bLessThan = (visibilty1 < visibilty2);
	//				}
	//			 }
	//
	//			 return bLessThan;
	//		  }
	//	};

}
