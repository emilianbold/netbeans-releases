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
 * Created on Feb 20, 2004
 *
 */
package org.netbeans.modules.uml.ui.products.ad.drawEngineManagers;

import java.awt.event.ActionEvent;

import org.dom4j.Node;

import org.netbeans.modules.uml.core.coreapplication.IPreferenceManager2;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage;
import org.netbeans.modules.uml.core.metamodel.core.primitivetypes.IVisibilityKind;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.metamodel.modelanalysis.ClassifierUtilities;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.IDataFormatter;
import org.netbeans.modules.uml.ui.products.ad.application.IMenuManager;
import org.netbeans.modules.uml.ui.products.ad.application.action.ContextMenuActionClass;
import org.netbeans.modules.uml.ui.products.ad.application.action.IETContextMenuHandler;
import org.netbeans.modules.uml.ui.support.ProductHelper;

/**
 * @author jingmingm
 *
 */
public class OperationMenuHelper
{
	protected static final int TS_UNKNOWN = -1;
	protected static final int TS_FALSE = 0;
	protected static final int TS_TRUE = 1;
	protected static final int VK_NOT_VISIBLE = -1;
	protected static final int VISIBILITY_CNT = 4;
	
	protected IClassifier m_cpSender  = null;
	protected IMenuManager m_cpContextMenu = null;
	protected IOperation m_cpOperationInvoked = null;
	protected IETContextMenuHandler m_ETContextMenuHandler = null;

	// Prepare the submenus for separating the operations by visibility kind
	protected IMenuManager[] m_cpVisibilitySubMenus = new IMenuManager[VISIBILITY_CNT];

	// These are valid during an AddOperation call
	protected IClassifier m_cpFeaturingClassifier = null;
	protected ETList<IClassifier> m_cpGeneralizations = null;
	protected ETList<IClassifier> m_cpInterfaces = null;
	protected String m_bstrClassifierLabel = "";
	protected boolean[] m_bIsLabelSet = new boolean[VISIBILITY_CNT];
	protected boolean m_bUserPrefOperationMenusGrouped = false;
	protected boolean m_bUserPrefDisableVisibilities = false;
	protected int m_lOperationStartIndx = 0;
	protected int m_lOpGrpStart = 0;
	protected int m_lOpGrpIndx = 0;
	protected int m_tsIsSenderGeneralization = 0;
	protected int m_tsIsSenderInterface = 0;
	protected int m_tsIsSenderInSamePackage = 0;
	protected IPackage m_cpSenderPackage = null;
	protected int m_lVisibilityFlags = 0;
	
	protected ContextMenuActionClass createMenuAction(String text, String menuID)
	{
		 return new ContextMenuActionClass(m_ETContextMenuHandler, text, menuID);
	}
	
	public OperationMenuHelper(IClassifier pSender,
								IClassifier pReceiver,
								IETContextMenuHandler pETContextMenuHandler,
								IMenuManager pContextMenu,
								IOperation pOperationInvoked)
	{
		m_cpSender = pSender;
		m_cpFeaturingClassifier = pReceiver;
		m_cpContextMenu = pContextMenu;
		m_ETContextMenuHandler = pETContextMenuHandler;
		m_cpOperationInvoked = pOperationInvoked;
		m_bUserPrefOperationMenusGrouped = getPreferenceValue( "Diagrams|SequenceDiagram", "GroupOperations" ).equals("PSK_YES");
		m_bUserPrefDisableVisibilities = getPreferenceValue( "Diagrams|SequenceDiagram", "RestrictOperationsByVisibility" ).equals("PSK_YES");
		m_lOpGrpIndx = 0;
		m_tsIsSenderGeneralization = TS_UNKNOWN;
		m_tsIsSenderInterface = TS_UNKNOWN;
		m_tsIsSenderInSamePackage = TS_UNKNOWN;
		
		// m_cpSender & m_cpOperationInvoked are allowed to be NULL

		if(m_cpFeaturingClassifier != null && m_cpContextMenu != null)
		{
			m_cpContextMenu = pContextMenu;
			m_cpVisibilitySubMenus[IVisibilityKind.VK_PUBLIC] = pContextMenu;
			m_cpVisibilitySubMenus[IVisibilityKind.VK_PROTECTED] = pContextMenu.createOrGetSubMenu(LabelManager.loadString("IDS_PROTECTED_TITLE"), "");
			pContextMenu.add(m_cpVisibilitySubMenus[IVisibilityKind.VK_PROTECTED]);
			m_cpVisibilitySubMenus[IVisibilityKind.VK_PRIVATE] = pContextMenu.createOrGetSubMenu(LabelManager.loadString("IDS_PRIVATE_TITLE"), "");
			pContextMenu.add(m_cpVisibilitySubMenus[IVisibilityKind.VK_PRIVATE]);
			m_cpVisibilitySubMenus[IVisibilityKind.VK_PACKAGE] = pContextMenu.createOrGetSubMenu(LabelManager.loadString("IDS_PACKAGE_TITLE"), "");
			pContextMenu.add(m_cpVisibilitySubMenus[IVisibilityKind.VK_PACKAGE]);
	
			// Keeps this class from creating labels for the initial featuring classifier
			for( int lIndx=0; lIndx<VISIBILITY_CNT; lIndx++ )
			{
			   m_bIsLabelSet[lIndx] = true;
			}
	
			m_lOpGrpIndx = m_lOpGrpStart;
	
			updateVisibilityFlags();
		}
	}
	
	public String formatOperation(IOperation pOperation )
	{
		String bcsOperation = "";

		if( pOperation != null)
		{
			// Get the data formatter off the product where it caches up
			// the various factories per language
			IDataFormatter pFormatter = ProductHelper.getDataFormatter();
			if (pFormatter != null)
			{
				bcsOperation = pFormatter.formatElement(pOperation);
			}
		}

		return bcsOperation;
	}

	public int getNumOperationsShown()
	{
		return Integer.parseInt(getPreferenceValue( "Diagrams|SequenceDiagram", "RestrictOperationsShown" ));
	}

	public boolean addOperation( IOperation pOperation, int lIndx )
	{
		boolean bSuccess = true;
	
		String bstrOperation = formatOperation(pOperation);
		if(bstrOperation!= null && bstrOperation.length() >0)
		{
			updateFeaturingClassifier( pOperation );
	
			int lVisibilityIndx = determineVisibilityIndex( pOperation );
			if( IVisibilityKind.VK_PUBLIC <= lVisibilityIndx && lVisibilityIndx <= IVisibilityKind.VK_PACKAGE )
			{
				boolean bIsChecked = isInvokedOperation( pOperation );
				bSuccess = addOperation( lVisibilityIndx, bstrOperation, bIsChecked, lIndx );
			}
		}
	
		// We only return false if the AddOperation fails, which is because the index is too big
		return bSuccess;
	}

	public String getPreferenceValue(String bstrPath, String bstrName)
	{
		String bsValue = "";
	
		IPreferenceManager2 cpMgr = ProductHelper.getPreferenceManager();
		if (cpMgr != null)
		{
			bsValue = cpMgr.getPreferenceValue( bstrPath, bstrName);
		}

		return bsValue;
	}

	public void createPullRight(IMenuManager pSubMenuItems, int nID, int lVisibilityIndx )
	{
		if(pSubMenuItems != null && lVisibilityIndx >= 0 && lVisibilityIndx <= VISIBILITY_CNT)
		{
			String xsTemp = (new Integer(nID)).toString();
	
			IMenuManager cpMenuItem = m_cpVisibilitySubMenus[lVisibilityIndx];
			pSubMenuItems.add(createMenuAction(xsTemp, xsTemp));
		}
	}

	public void updateFeaturingClassifier(IOperation pOperation)
	{
		if(pOperation == null)
		{
			return;
		}
	
		IClassifier cpFeaturingClassifier = pOperation.getFeaturingClassifier();
		if( cpFeaturingClassifier != null)
		{
			boolean bIsSame = cpFeaturingClassifier.isSame( m_cpFeaturingClassifier);
			if( !bIsSame )
			{
				// Make sure the featuring classifier label is updated
				m_bstrClassifierLabel = "";
	
				for( int lIndx=0; lIndx<VISIBILITY_CNT; lIndx++ )
				{
					m_bIsLabelSet[lIndx] = false;
				}
	
				m_cpFeaturingClassifier = cpFeaturingClassifier;
				m_tsIsSenderGeneralization = TS_UNKNOWN;
				m_tsIsSenderInterface = TS_UNKNOWN;
				m_tsIsSenderInSamePackage = TS_UNKNOWN;
	
				updateVisibilityFlags();
			}
		}
	}

	public void updateVisibilityFlags()
	{
		m_lVisibilityFlags = IVisibilityFlags.VF_ALL;
	
		if( m_cpSender != null && m_bUserPrefDisableVisibilities )
		{
			if( !isSenderNested() )
			{
				 m_lVisibilityFlags &= ~IVisibilityFlags.VF_PRIVATE;
	
				 if( !isSenderGeneralization() && !IsSenderInterface() )
				 {
					m_lVisibilityFlags &= ~IVisibilityFlags.VF_PROTECTED;
				 }
	
				 if( !isSenderInSamePackage() )
				 {
					m_lVisibilityFlags &= ~IVisibilityFlags.VF_PACKAGE;
				 }
			}
		}
	}

	public boolean isSenderNested()
	{
		boolean bIsSenderNested = false;
	
		if( m_cpSender != null && m_cpFeaturingClassifier != null)
		{
			String bsXMIID = m_cpFeaturingClassifier.getXMIID();
	
			Node cpSenderNode = m_cpSender.getNode();
			if( cpSenderNode != null)
			{
				String bstrQuery = "ancestor-or-self::*[@xmi.id='" + bsXMIID + "']";
				Node cpFoundNode = cpSenderNode.selectSingleNode( bstrQuery);
				bIsSenderNested = (cpFoundNode != null);
			}
		}
	
		return bIsSenderNested;
	}

	public boolean isSenderGeneralization()
	{
		if( TS_UNKNOWN == m_tsIsSenderGeneralization )
		{
			m_tsIsSenderGeneralization = TS_FALSE;
	
			if( m_cpSender != null && m_cpFeaturingClassifier != null)
			{
				if( m_cpGeneralizations == null)
				{
					ClassifierUtilities pClassifierUtilities = new ClassifierUtilities();
					m_cpGeneralizations = pClassifierUtilities.collectGeneralizingClassifiers(m_cpSender);
					if( m_cpGeneralizations != null)
					{
						m_cpGeneralizations.add( m_cpSender );
					}
				}
	
				if( m_cpGeneralizations != null)
				{
					int lCnt = m_cpGeneralizations.size();
	
					for( int lIndx=0; lIndx<lCnt; lIndx++ )
					{
						IClassifier cpClassifier = m_cpGeneralizations.get(lIndx);
						if( cpClassifier != null)
						{
							boolean bIsSame = cpClassifier.isSame(m_cpFeaturingClassifier);
							if( bIsSame )
							{
								 m_tsIsSenderGeneralization = TS_TRUE;
								 break;
							}
						}
					}
				}
			}
		}
	
		return (TS_TRUE == m_tsIsSenderGeneralization);
	}

	public boolean IsSenderInterface()
	{
		if( TS_UNKNOWN == m_tsIsSenderInterface )
		{
			m_tsIsSenderInterface = TS_FALSE;
	
			if( m_cpSender != null && m_cpFeaturingClassifier != null)
			{
				if( m_cpInterfaces == null)
				{
					ClassifierUtilities pClassifierUtilities = new ClassifierUtilities();
					m_cpInterfaces = pClassifierUtilities.collectImplementedInterfaces(m_cpSender);
					if( m_cpInterfaces != null)
					{
						m_cpInterfaces.add( m_cpSender);
					}
				}
	
				if( m_cpInterfaces != null)
				{
					int lCnt = m_cpInterfaces.size();
	
					for( int lIndx=0; lIndx<lCnt; lIndx++ )
					{
						IClassifier cpClassifier = m_cpInterfaces.get(lIndx);
						if( cpClassifier != null)
						{
							boolean bIsSame = cpClassifier.isSame( m_cpFeaturingClassifier);
							if( bIsSame )
							{
								 m_tsIsSenderInterface = TS_TRUE;
								 break;
							}
						}
					}
				 }
			}
		}
	
		return (TS_TRUE == m_tsIsSenderInterface);
	}

	public boolean isSenderInSamePackage()
	{
		if( TS_UNKNOWN == m_tsIsSenderInSamePackage )
		{
			m_tsIsSenderInSamePackage = TS_FALSE;
	
			if( m_cpSender != null && m_cpFeaturingClassifier != null)
			{
				if( m_cpSenderPackage == null)
				{
					m_cpSenderPackage = m_cpSender.getOwningPackage();
				}
	
				IPackage cpPackage = m_cpFeaturingClassifier.getOwningPackage();
	
				if(m_cpSenderPackage != null && cpPackage != null)
				{
					m_tsIsSenderInSamePackage = TS_TRUE;
				}
				else if( m_cpSenderPackage != null)
				{
					boolean bIsSame = m_cpSenderPackage.isSame(cpPackage);
					if( bIsSame )
					{
						m_tsIsSenderInSamePackage = TS_TRUE;
					}
				}
			}
		}
	
		return (TS_TRUE == m_tsIsSenderInSamePackage);
	}

	public boolean isInvokedOperation( IOperation pOperation )
	{
		boolean bIsInvokedOperation = false;
	
		if( pOperation != null && m_cpOperationInvoked != null)
		{
			boolean bIsSame = pOperation.isSame( m_cpOperationInvoked);
			bIsInvokedOperation = (bIsSame != false);
		}
	
		return bIsInvokedOperation;
	}

	public int determineVisibilityIndex( IOperation pOperation )
	{
		if( pOperation == null)
		{
			return 0;
		}
		
	
		int visibility = pOperation.getVisibility();
	
		boolean bIsVisible = true;
		if( m_cpSender != null)
		{
			switch( visibility )
			{
			default:
				 break;
			case IVisibilityKind.VK_PUBLIC:
				 // all public operations are visible
				 break;
	
			case IVisibilityKind.VK_PROTECTED:
				 bIsVisible = (IVisibilityFlags.VF_PROTECTED & m_lVisibilityFlags) != 0;
				 break;
	
			case IVisibilityKind.VK_PRIVATE:
				 bIsVisible = (IVisibilityFlags.VF_PRIVATE & m_lVisibilityFlags) != 0;
				 break;
	
			case IVisibilityKind.VK_PACKAGE:
				 bIsVisible = (IVisibilityFlags.VF_PACKAGE & m_lVisibilityFlags) != 0;
				 break;
			}
		}
	
		return bIsVisible ? visibility : VK_NOT_VISIBLE;
	}

	public boolean addOperation( int lVisibilityIndx,
								String  bstrOperation,
								boolean bIsChecked,
								int lIndx )
	{
		addGrouping( lVisibilityIndx );
	
		String strOperationDscr = StringUtilities.replaceSubString(LabelManager.loadString("IDS_OPERATION_NUMBER"), "%d", (new Integer(lIndx)).toString());
	
		int lBtnIndx = m_lOperationStartIndx + lIndx;
		if( lBtnIndx >= m_lOpGrpStart )
		{
			//return false;
		}
	
		m_cpVisibilitySubMenus[lVisibilityIndx].add(createMenuAction(bstrOperation, strOperationDscr));
	
		return true;
	}

	public void addGrouping( int lVisibilityIndx )
	{
		if( m_bUserPrefOperationMenusGrouped && !m_bIsLabelSet[lVisibilityIndx])
		{
			String bsName = m_cpFeaturingClassifier.getName();
			if(bsName != null && bsName.length() > 0)
			{
				String strOperationsGroup = StringUtilities.replaceSubString(LabelManager.loadString("IDS_OPERATIONS_GROUP"), "%s", bsName);
				String strOperationsGroupDscr = StringUtilities.replaceSubString(LabelManager.loadString("IDS_OPERATIONS_GROUP_DSCR"), "%s", bsName);;
            
            // Fix J2596:  we pass null here to disable the menu item
            ContextMenuActionClass menuItem = new ContextMenuActionClass( null, strOperationsGroup, strOperationsGroupDscr );
				m_cpContextMenu.add( menuItem );
				m_lOpGrpIndx++;
			}
	
			m_bIsLabelSet[lVisibilityIndx] = true;
		}
	}
}



