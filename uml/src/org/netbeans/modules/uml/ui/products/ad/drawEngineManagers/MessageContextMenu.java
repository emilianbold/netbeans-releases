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

import org.netbeans.modules.uml.core.metamodel.core.primitivetypes.IMessageKind;
import org.netbeans.modules.uml.core.metamodel.dynamics.IMessage;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.metamodel.modelanalysis.ClassifierUtilities;
import org.netbeans.modules.uml.core.metamodel.structure.IComponent;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.core.support.umlutils.DataFormatter;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.IDataFormatter;
import org.netbeans.modules.uml.ui.products.ad.application.IMenuManager;
import org.netbeans.modules.uml.ui.products.ad.application.action.ContextMenuActionClass;
import org.netbeans.modules.uml.ui.products.ad.application.action.IETContextMenuHandler;
//import org.netbeans.modules.uml.ui.products.ad.application.action.Separator;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import javax.swing.JSeparator;
import org.netbeans.modules.uml.ui.swing.testbed.addin.menu.Separator;

/**
 * @author jingmingm
 *
 */
public class MessageContextMenu implements IMessageContextMenu
{
	protected IETContextMenuHandler m_ETContextMenuHandler = null;
	protected ETList<IOperation> m_cpOperations = null;
	protected IADLabelManager m_pParentLabelManager = null;
	protected IDataFormatter m_dataFormatter = ProductHelper.getDataFormatter();
	
	public MessageContextMenu(IETContextMenuHandler pETContextMenuHandler)
	{
		m_ETContextMenuHandler = pETContextMenuHandler;
	}
	
	public void addOperationsPullRight(IMessage pMessage, IMenuManager pContextMenu)
	{
		if( pMessage != null )
		{
			// All message kinds (except result message kind) have an operations pull right
			int kind = pMessage.getKind();
			if(kind != IMessageKind.MK_RESULT )
			{
				// Find the operation that is already associated with the message
				IOperation cpOperationInvoked = pMessage.getOperationInvoked();

				IClassifier cpReceiver = pMessage.getReceivingClassifier();
				if (cpReceiver != null)
				{
					IClassifier cpSender = pMessage.getSendingClassifier();
					// The sender can be NULL

					IMenuManager cpSubMenuItems = addOperationsPullRight(pContextMenu);
					if ( cpSubMenuItems != null )
					{
						// Only synchronous, and asynchronous messages are allowed to select/create operations
						if( IMessageKind.MK_SYNCHRONOUS == kind ||
						IMessageKind.MK_ASYNCHRONOUS == kind) 
						{
							addOperationsToContextMenu( cpSender,
													cpReceiver,
													cpSubMenuItems,
													cpOperationInvoked );
						}
						else
						{	
							addCreateOperationsToContextMenu( cpSender,
																cpReceiver,
																cpSubMenuItems,
																cpOperationInvoked );
						}
					}
				}
			}
		}
	}

	public void addOperationsPullRight( IClassifier pClassifier, IMenuManager pContextMenu)
	{
		IMenuManager cpSubMenuItems = addOperationsPullRight( pContextMenu);
		if ( cpSubMenuItems != null )
		{
			addOperationsToContextMenu( null,
										pClassifier,
										cpSubMenuItems,
										null );
		}
	}

	public IOperation selectOperation( long lOperationIndx)
	{
		IOperation pOperation = null;
	
		if( m_cpOperations != null)
		{
			int lCnt = m_cpOperations.size();
	
			if( (lOperationIndx >= 0) &&
				(lOperationIndx < lCnt) )
			{
				pOperation = m_cpOperations.get((int)lOperationIndx);
			}
		}
		
		return pOperation;
	}

	public IOperation guiSelectOperation()
	{
		IOperation pOperation = null;
	
		if( m_cpOperations != null)
		{
//			CDlgOperations dlgOperations( m_cpOperations, AfxGetMainWnd() );
//			if( IDOK == dlgOperations.DoModal() )
//			{
//				 CComPtr< IOperation > cpOperation( dlgOperations.GetSelectedOperation() );
//				 if( cpOperation )
//				 {
//					_VH( cpOperation.CopyTo( ppOperation ));
//				 }
//			}
		}
		
		return pOperation;
	}

	public String getMessagesOperationText(IMessage pMessage )
	{
		String bstrOperationText = "";
	
		if (pMessage != null)
		{
			 IOperation cpOperation = pMessage.getOperationInvoked();
			 if( cpOperation != null )
			 {
				bstrOperationText = m_dataFormatter.formatElement(cpOperation );
			 }
		}

		return bstrOperationText;
	}

	public void cleanUp()
	{
		m_cpOperations = null;
	}


	protected IADLabelManager getParentLabelManager()
	{
		return m_pParentLabelManager;
	}

	protected IMenuManager addOperationsPullRight( IMenuManager pContextMenu)
	{
		if( pContextMenu == null )
		{
			return null;
		}

		IMenuManager cpOpMenuItem = pContextMenu.createOrGetSubMenu(LabelManager.loadString("IDS_SQD_OPERATIONS_PULLRIGHT"), "");
		if (cpOpMenuItem != null)
		{
			cpOpMenuItem.removeAll();
			//pContextMenu.add(cpOpMenuItem);
		}
		return cpOpMenuItem;	
	}

	protected void addOperationsToContextMenu( IClassifier pSender,
												IClassifier pReceiver,
												IMenuManager pContextMenu,
												IOperation pOperationInvoked )
	{
		// pSender & pOperationInvoked are allowed to be NULL
		if( pReceiver != null && pContextMenu != null)
		{
			// Insert the "create new operation"
			if(!(pReceiver instanceof IComponent))
			{
				pContextMenu.add(createMenuAction(LabelManager.loadString("IDS_OPERATION_NEW"), "MBK_OPERATION_NEW"));
			}
	
			pContextMenu.add(new Separator());
	
			m_cpOperations = collectOperations(pReceiver);
			int lCnt = m_cpOperations.size();
			if (lCnt > 0)
			{
				OperationMenuHelper helper = new OperationMenuHelper(pSender,
																	pReceiver,
				m_ETContextMenuHandler,
																	pContextMenu,
																	pOperationInvoked);
				for( int lIndx=0;lIndx<lCnt;lIndx++)
				{
					IOperation cpOperation = m_cpOperations.get(lIndx);
					helper.addOperation(cpOperation, lIndx);
				}
			}
		}
	}

	protected void addCreateOperationsToContextMenu( IClassifier pSender,
													 IClassifier pReceiver,
													IMenuManager pContextMenu,
													IOperation pOperationInvoked )
	{
		// pSender & pOperationInvoked are allowed to be NULL
	
		if( pReceiver != null && pContextMenu != null)
		{
			pContextMenu.add(createMenuAction( LabelManager.loadString("IDS_NEW_CONSTRUCTOR"), "MBK_NEW_CONSTRUCTOR"));
			
			m_cpOperations = pReceiver.getOperations();
			if( m_cpOperations != null )
			{
				int lCnt = m_cpOperations.size();
	
				for( int lIndx=0;lIndx<lCnt;lIndx++)
				{
					IOperation cpOperation = m_cpOperations.get(lIndx);
	
					if( processOperation( cpOperation, pOperationInvoked ) )
					{
						boolean bIsConstructor = cpOperation.getIsConstructor();
						if( bIsConstructor )
						{
							String bstrOperation = m_dataFormatter.formatElement(cpOperation);
							if(bstrOperation != null && bstrOperation.length() > 0 )
							{
								String strOperationDscr = StringUtilities.replaceSubString(LabelManager.loadString("IDS_OPERATION_NUMBER"), "%d", (new Integer(lIndx)).toString());
								pContextMenu.add(createMenuAction(bstrOperation, strOperationDscr));
							}
						}
					}
				}
			}
		}
	}

	protected ETList<IOperation> collectOperations(IClassifier pReceiver)
	{
		ClassifierUtilities cpUtils = new ClassifierUtilities();
		return cpUtils.collectAllOperations(pReceiver);
	}

	protected boolean processOperation(IOperation pOperation, IOperation pOperationInvoked)
	{
		// check to see if the operation should be processed for display
		boolean bProcessOperation = false;
	
		if( pOperation != null )
		{
			if( pOperationInvoked != null )
			{
				 boolean bIsSame = pOperation.isSame(pOperationInvoked);
				 bProcessOperation = !bIsSame;
			}
			else
			{
				 bProcessOperation = true;
			}
		}
	
		return bProcessOperation;
	}
	
	protected ContextMenuActionClass createMenuAction(String text, String menuID)
	{
		 return new ContextMenuActionClass(m_ETContextMenuHandler, text, menuID);
	}
}



