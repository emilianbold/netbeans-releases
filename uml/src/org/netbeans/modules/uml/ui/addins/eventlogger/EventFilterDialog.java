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


package org.netbeans.modules.uml.ui.addins.eventlogger;

import javax.swing.JDialog;

import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.swing.commondialogs.JCenterDialog;

/**
 * @author sumitabhk
 *
 */
public class EventFilterDialog extends JCenterDialog
{
	private EventLoggingAddin m_Addin = null;
	
	private boolean m_ModifierFilter = false;
	private boolean m_WorkspaceFilter = false;
	private boolean m_RoundTripFilter = false;
	private boolean m_ClassifierFilter = false;
	private boolean m_ProjectTreeFilter = false;
	private boolean m_ProjectTreeFilterFilter = false;
	private boolean m_CoreProductFilter = false;
	private boolean m_LifetimeFilter = false;
	private boolean m_ReleationshipFilter = false;
	private boolean m_EditControlFilter = false;
	private boolean m_MessengerFilter = false;
	private boolean m_VBAFilter = false;
	private boolean m_AddinFilter = false;
	private boolean m_DrawAreaFilter = false;


	/**
	 * 
	 */
	public EventFilterDialog()
	{
		super();
	}

	public EventFilterDialog(EventLoggingAddin addin)
	{
		super();
		m_Addin = addin;
	}

	/**
	 * @return
	 */
	public boolean isModifierFilter()
	{
		return m_ModifierFilter;
	}

	/**
	 * @return
	 */
	public boolean isProjectTreeFilterFilter()
	{
		return m_ProjectTreeFilterFilter;
	}

	/**
	 * @return
	 */
	public boolean isReleationshipFilter()
	{
		return m_ReleationshipFilter;
	}

	/**
	 * @return
	 */
	public boolean isRoundTripFilter()
	{
		return m_RoundTripFilter;
	}

	/**
	 * @return
	 */
	public boolean isVBAFilter()
	{
		return m_VBAFilter;
	}

	/**
	 * @return
	 */
	public boolean isWorkspaceFilter()
	{
		return m_WorkspaceFilter;
	}

	/**
	 * @param b
	 */
	public void setModifierFilter(boolean b)
	{
		m_ModifierFilter = b;
	}

	/**
	 * @param b
	 */
	public void setProjectTreeFilterFilter(boolean b)
	{
		m_ProjectTreeFilterFilter = b;
	}

	/**
	 * @param b
	 */
	public void setReleationshipFilter(boolean b)
	{
		m_ReleationshipFilter = b;
	}

	/**
	 * @param b
	 */
	public void setRoundTripFilter(boolean b)
	{
		m_RoundTripFilter = b;
	}

	/**
	 * @param b
	 */
	public void setVBAFilter(boolean b)
	{
		m_VBAFilter = b;
	}

	/**
	 * @param b
	 */
	public void setWorkspaceFilter(boolean b)
	{
		m_WorkspaceFilter = b;
	}

	/**
	 * @return
	 */
	public boolean isAddinFilter()
	{
		return m_AddinFilter;
	}

	/**
	 * @return
	 */
	public boolean isClassifierFilter()
	{
		return m_ClassifierFilter;
	}

	/**
	 * @return
	 */
	public boolean isCoreProductFilter()
	{
		return m_CoreProductFilter;
	}

	/**
	 * @return
	 */
	public boolean isDrawAreaFilter()
	{
		return m_DrawAreaFilter;
	}

	/**
	 * @return
	 */
	public boolean isEditControlFilter()
	{
		return m_EditControlFilter;
	}

	/**
	 * @return
	 */
	public boolean isLifetimeFilter()
	{
		return m_LifetimeFilter;
	}

	/**
	 * @return
	 */
	public boolean isMessengerFilter()
	{
		return m_MessengerFilter;
	}

	/**
	 * @return
	 */
	public boolean isProjectTreeFilter()
	{
		return m_ProjectTreeFilter;
	}

	/**
	 * @param b
	 */
	public void setAddinFilter(boolean b)
	{
		m_AddinFilter = b;
	}

	/**
	 * @param b
	 */
	public void setClassifierFilter(boolean b)
	{
		m_ClassifierFilter = b;
	}

	/**
	 * @param b
	 */
	public void setCoreProductFilter(boolean b)
	{
		m_CoreProductFilter = b;
	}

	/**
	 * @param b
	 */
	public void setDrawAreaFilter(boolean b)
	{
		m_DrawAreaFilter = b;
	}

	/**
	 * @param b
	 */
	public void setEditControlFilter(boolean b)
	{
		m_EditControlFilter = b;
	}

	/**
	 * @param b
	 */
	public void setLifetimeFilter(boolean b)
	{
		m_LifetimeFilter = b;
	}

	/**
	 * @param b
	 */
	public void setMessengerFilter(boolean b)
	{
		m_MessengerFilter = b;
	}

	/**
	 * @param b
	 */
	public void setProjectTreeFilter(boolean b)
	{
		m_ProjectTreeFilter = b;
	}

	public void onOKButtonClicked()
	{
		ICoreProduct prod = ProductHelper.getCoreProduct();
		if (prod != null)
		{
			//apply draw area filter
			if (m_DrawAreaFilter)
			{
				m_Addin.registerToDrawingAreaDispatcher();
			}
			else
			{
				m_Addin.unregisterToDrawingAreaDispatcher();
			}
			
			//ApplyAddinFilter(dialog, pCoreProduct);
			if (m_AddinFilter)
			{
				m_Addin.registerToAddInDispatcher();
			}
			else
			{
				m_Addin.unregisterToAddInDispatcher();
			}
			
			//ApplyVBAFilter(dialog, pCoreProduct);
			if (m_VBAFilter)
			{
				m_Addin.registerToVBADispatcher();
			}
			else
			{
				m_Addin.unregisterToVBADispatcher();
			}
			
			//ApplyMessengerFilter(dialog, pCoreProduct);
			if (m_MessengerFilter)
			{
				m_Addin.registerToMessengerDispatcher();
			}
			else
			{
				m_Addin.unregisterToMessengerDispatcher();
			}
			
			//ApplyEditControlFilter(dialog, pCoreProduct);
			if (m_EditControlFilter)
			{
				m_Addin.registerToEditCtrlDispatcher();
			}
			else
			{
				m_Addin.unregisterToEditCtrlDispatcher();
			}
			
			//ApplyReleationshipFilter(dialog, pCoreProduct);
			if (m_ReleationshipFilter)
			{
				m_Addin.registerToRelationDispatcher();
			}
			else
			{
				m_Addin.unregisterToRelationDispatcher();
			}
			
			//ApplyLifetimeFilter(dialog, pCoreProduct);
			if (m_LifetimeFilter)
			{
				m_Addin.registerToLifeTimeDispatcher();
			}
			else
			{
				m_Addin.unregisterToLifeTimeDispatcher();
			}
			
			//ApplyCoreProductFilter(dialog, pCoreProduct);
			if (m_CoreProductFilter)
			{
				m_Addin.registerToCoreProductDispatcher();
			}
			else
			{
				m_Addin.unregisterToCoreProductDispatcher();
			}
			
			//ApplyProjectTreeFilterFilter(dialog, pCoreProduct);
			if (m_ProjectTreeFilterFilter)
			{
				m_Addin.registerToProjectTreeFilterDialogDispatcher();
			}
			else
			{
				m_Addin.unregisterToProjectTreeFilterDialogDispatcher();
			}
			
			//ApplyProjectTreeFilter(dialog, pCoreProduct);
			if (m_ProjectTreeFilter)
			{
				m_Addin.registerToProjectTreeDispatcher();
			}
			else
			{
				m_Addin.unregisterToProjectTreeDispatcher();
			}
			
			//ApplyClassifierFilter(dialog, pCoreProduct);
			if (m_ClassifierFilter)
			{
				m_Addin.registerToClassifierDispatcher();
			}
			else
			{
				m_Addin.unregisterToClassifierDispatcher();
			}
			
			//ApplyRoundTripFilter(dialog, pCoreProduct);
			if (m_RoundTripFilter)
			{
				m_Addin.registerToRoundTripDispatcher();
			}
			else
			{
				m_Addin.unregisterToRoundTripDispatcher();
			}
			
			//ApplyWorkspaceFilter(dialog, pCoreProduct);
			if (m_WorkspaceFilter)
			{
				m_Addin.registerToWorkspaceDispatcher();
			}
			else
			{
				m_Addin.unregisterToWorkspaceDispatcher();
			}
			
			//ApplyModifierFilter(dialog, pCoreProduct);
			if (m_ModifierFilter)
			{
				m_Addin.registerToModifiedDispatcher();
			}
			else
			{
				m_Addin.unregisterToModifiedDispatcher();
			}
		}
	}

}



