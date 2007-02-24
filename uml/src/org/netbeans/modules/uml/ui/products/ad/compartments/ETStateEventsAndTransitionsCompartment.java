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

import java.awt.Color;

import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IState;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.preferenceframework.PreferenceAccessor;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETSize;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.products.ad.drawengines.IStateDrawEngine;

/**
 * @author jingmingm
 *
 */
public class ETStateEventsAndTransitionsCompartment extends ETNameCompartment implements IADStateEventsAndTransitionsCompartment
{
	protected final String ENTRY_STRING = "entry / ";
	protected final String EXIT_STRING = "exit / ";
	protected final String DO_STRING = "do / ";
	protected final String INCOMING_STRING = "incoming / ";
	protected final String OUTGOING_STRING = "outgoing / ";
	
	public ETStateEventsAndTransitionsCompartment()
	{
		super();
		this.init();
	}

	public ETStateEventsAndTransitionsCompartment(IDrawEngine pDrawEngine)
	{
		super(pDrawEngine);
		this.init();
	}

	private void init()
	{		
		this.setFontString("Arial-12");
		this.initResources();
	}

	public void initResources()
	{
		setResourceID("stateevents", Color.BLACK);
		super.initResources();
		this.setName(PreferenceAccessor.instance().getDefaultElementName());
	}
	
	public String getCompartmentID()
	{
		return "ADStateEventsAndTransitionsCompartment";
	}
	
	protected IState getState()
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
/*
	public String getName()
	{
		String sName = super.getName();

		IDrawEngine pEngine = getEngine();
		if (pEngine != null && pEngine instanceof IStateDrawEngine)
		{
			IStateDrawEngine pStateDrawEngine = (IStateDrawEngine)pEngine;
			IElement pEle = TypeConversions.getElement(pEngine);
			if (pEle != null)
			{
				String sOnlyName = sName;
				sOnlyName = StringUtilities.replaceSubString(sOnlyName, ENTRY_STRING, "");
				sOnlyName = StringUtilities.replaceSubString(sOnlyName, EXIT_STRING, "");
				sOnlyName = StringUtilities.replaceSubString(sOnlyName, DO_STRING, "");
				sOnlyName = StringUtilities.replaceSubString(sOnlyName, INCOMING_STRING, "");
				sOnlyName = StringUtilities.replaceSubString(sOnlyName, OUTGOING_STRING, "");

				int nType = IStateDrawEngine.SPTT_UNKNOWN;
				nType = pStateDrawEngine.GetProcedureOrTransitionType(pEle);
				
				if (nType == IStateDrawEngine.SPTT_ENTRY)
				{
					sName = ENTRY_STRING + sOnlyName;
				}
				else if (nType == IStateDrawEngine.SPTT_EXIT)
				{
					sName = EXIT_STRING + sOnlyName;
				}
				else if (nType == IStateDrawEngine.SPTT_DOACTIVITY)
				{
					sName = DO_STRING + sOnlyName;
				}
				else if (nType == IStateDrawEngine.SPTT_INCOMING_TRANSITION)
				{
					sName = INCOMING_STRING + sOnlyName;
				}
				else if (nType == IStateDrawEngine.SPTT_OUTGOING_TRANSITION)
				{
					sName = OUTGOING_STRING + sOnlyName;
				}
			}
		}
		
		return sName;
	}
	
	public void setName(String pNewName)
	{
		String sOnlyName = pNewName, sNameToSet = pNewName;
		sOnlyName = StringUtilities.replaceSubString(sOnlyName, ENTRY_STRING, "");
		sOnlyName = StringUtilities.replaceSubString(sOnlyName, EXIT_STRING, "");
		sOnlyName = StringUtilities.replaceSubString(sOnlyName, DO_STRING, "");
		sOnlyName = StringUtilities.replaceSubString(sOnlyName, INCOMING_STRING, "");
		sOnlyName = StringUtilities.replaceSubString(sOnlyName, OUTGOING_STRING, "");
		
		IDrawEngine pEngine = getEngine();
		if (pEngine != null && pEngine instanceof IStateDrawEngine)
		{
			IStateDrawEngine pStateDrawEngine = (IStateDrawEngine)pEngine;
			IElement pEle = TypeConversions.getElement(pEngine);
			if (pEle != null)
			{
				int nType = IStateDrawEngine.SPTT_UNKNOWN;
				nType = pStateDrawEngine.GetProcedureOrTransitionType(pEle);
				
				if (nType == IStateDrawEngine.SPTT_ENTRY)
				{
					sNameToSet = ENTRY_STRING + sOnlyName;
				}
				else if (nType == IStateDrawEngine.SPTT_EXIT)
				{
					sNameToSet = EXIT_STRING + sOnlyName;
				}
				else if (nType == IStateDrawEngine.SPTT_DOACTIVITY)
				{
					sNameToSet = DO_STRING + sOnlyName;
				}
				else if (nType == IStateDrawEngine.SPTT_INCOMING_TRANSITION)
				{
					sNameToSet = INCOMING_STRING + sOnlyName;
				}
				else if (nType == IStateDrawEngine.SPTT_OUTGOING_TRANSITION)
				{
					sNameToSet = OUTGOING_STRING + sOnlyName;
				}
				
				super.setName(sNameToSet);
			}
		}
	}
*/
	/*
	 * 
	 */
	public void draw(IDrawInfo pDrawInfo, IETRect pBoundingRect)
	{
		String oldName = super.getName();

		try
		{					
			this.setName(this.getDrawName());
			super.draw(pDrawInfo, pBoundingRect);
		}
		finally
		{
			this.setName(oldName);
		}
	}
	
	/*
	 * 
	 */
	protected String getDrawName()
	{
		// Get draw namw
		String tempName = this.getName();
		IDrawEngine pEngine = getEngine();
		if (pEngine != null && pEngine instanceof IStateDrawEngine)
		{
			IStateDrawEngine pStateDrawEngine = (IStateDrawEngine)pEngine;
			IElement pEle = getModelElement();
			if (pEle != null)
			{
				int nType = pStateDrawEngine.getProcedureOrTransitionType(pEle);
				if (nType == IStateDrawEngine.SPTT_ENTRY)
				{
					tempName = ENTRY_STRING + tempName;
				}
				else if (nType == IStateDrawEngine.SPTT_EXIT)
				{
					tempName = EXIT_STRING + tempName;
				}
				else if (nType == IStateDrawEngine.SPTT_DOACTIVITY)
				{
					tempName = DO_STRING + tempName;
				}
				else if (nType == IStateDrawEngine.SPTT_INCOMING_TRANSITION)
				{
					tempName = INCOMING_STRING + tempName;
				}
				else if (nType == IStateDrawEngine.SPTT_OUTGOING_TRANSITION)
				{
					tempName = OUTGOING_STRING + tempName;
				}
			}
		}
		
		return tempName;
	}
	
	public IETSize calculateOptimumSize(IDrawInfo pDrawInfo, boolean bAt100Pct)
	{
		IETSize pETSize = null;
		String name = getName();
		setName(this.getDrawName());
		pETSize = super.calculateOptimumSize(pDrawInfo, bAt100Pct);
		setName(name);
		return pETSize;
	}
}
