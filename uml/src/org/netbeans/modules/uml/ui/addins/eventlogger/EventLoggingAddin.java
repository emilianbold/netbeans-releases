/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */


package org.netbeans.modules.uml.ui.addins.eventlogger;

import java.awt.Frame;

//import org.netbeans.modules.uml.core.addinframework.AddInButton;
//import org.netbeans.modules.uml.core.addinframework.IAddIn;
//import org.netbeans.modules.uml.core.addinframework.IAddInButton;
//import org.netbeans.modules.uml.core.addinframework.IAddInButtonSupport;
//import org.netbeans.modules.uml.core.addinframework.IAddInButtonTargetKind;
//import org.netbeans.modules.uml.core.addinframework.IAddInEventsSink;
import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguageManager;
import org.netbeans.modules.uml.core.support.umlmessagingcore.IMessageService;
import org.netbeans.modules.uml.core.support.umlsupport.IStrings;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.InvalidArguments;
import org.netbeans.modules.uml.ui.support.DispatchHelper;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProductDiagramManager;

/**
 * @author sumitabhk
 *
 */
//public class EventLoggingAddin implements IAddIn, IAddInButtonSupport
public class EventLoggingAddin
{
	private IMessageService m_MessageService = null;
	private IProductDiagramManager m_DiagramManager = null;
	private AddinEventsSink m_EventsSink = null;
	private String m_version = "1";
	private DispatchHelper m_Helper = new DispatchHelper();
	private EventsDialog m_EventsDialog = null;
	private static int s_MainFrameButtonId = 1;

	/**
	 * 
	 */
	public EventLoggingAddin()
	{
		super();
	}

	/**
	 * Called when the addin is initialized.
	 */
	public long initialize(Object context)
	{
		// Get the current message service and diagram manager
		// The current message service and diagram manager is on our product.  We use the product helper
		// go automatically get the product we're attached to.
		m_MessageService = ProductHelper.getMessageService();
		m_DiagramManager = ProductHelper.getProductDiagramManager();
		
		// Create the events sink
		if (m_EventsSink == null)
		{
			m_EventsSink = new AddinEventsSink();
			m_EventsSink.setParent(this);

			registerToWorkspaceDispatcher();
			registerToDrawingAreaDispatcher();
			registerToMessengerDispatcher();
			registerToVBADispatcher();
			registerToProjectTreeDispatcher();
			registerToAddInDispatcher();
			registerToModifiedDispatcher();
			registerToEditCtrlDispatcher();
			registerToProjectTreeFilterDialogDispatcher();
			registerToRelationDispatcher();
			registerToLifeTimeDispatcher();
			registerToClassifierDispatcher();
			registerToCoreProductDispatcher();
			registerToRoundTripDispatcher();
			registerToPreferenceManagerDispatcher();
			registerToSCMDispatcher();
			registerToDynamicsDispatcher();
			registerToActivitiesDispatcher();
			registerToStructureDispatcher();
			registerToDesignPatternDispatcher();
		}
		return 0;
	}

	/**
	 * 
	 */
	public void registerToDesignPatternDispatcher()
	{
		// TODO Auto-generated method stub
		
	}

	/**
	 * registers to the structure dispatcher
	 */
	public void registerToStructureDispatcher()
	{
		if (m_EventsSink != null)
		{
			m_Helper.registerForProjectEvents(m_EventsSink);
			//m_Helper.registerForProjectUpgradEvents(m_EventsSink);
			m_Helper.registerForArtifactEvents(m_EventsSink);
		}
	}

	/**
	 * registers to activities dispatcher
	 */
	public void registerToActivitiesDispatcher()
	{
		if (m_EventsSink != null)
		{
			m_Helper.registerForActivityEdgeEvents(m_EventsSink);
		}
	}

	/**
	 * registers to the dynamics dispatcher
	 */
	public void registerToDynamicsDispatcher()
	{
		if (m_EventsSink != null)
		{
			m_Helper.registerForDynamicsEvents(m_EventsSink);
		}
	}

	/**
	 * registers to the SCM dispatcher
	 */
	public void registerToSCMDispatcher()
	{
		if (m_EventsSink != null)
		{
			m_Helper.registerForSCMEvents(m_EventsSink);
		}
	}

	/**
	 * registers to preference manager dispatcher
	 */
	public void registerToPreferenceManagerDispatcher()
	{
		if (m_EventsSink != null)
		{
			m_Helper.registerForPreferenceManagerEvents(m_EventsSink);
		}
	}

	/**
	 * registers to roundtrip dispatcher
	 */
	public void registerToRoundTripDispatcher()
	{
		// Since we have to register for each language that we wish to listen to
		// I will get the language manager to discover the languages that are supported.
		// Then I will register for each language.
		if (m_EventsSink != null)
		{
			ICoreProduct prod = ProductHelper.getCoreProduct();
			if (prod != null)
			{
				ILanguageManager langMgr = prod.getLanguageManager();
				if (langMgr != null)
				{
					IStrings langNames = langMgr.getSupportedLanguages();
					if (langNames != null)
					{
						int count = langNames.getCount();
						for (int i=0; i<count; i++)
						{
							String langName = langNames.item(i);
							if (langName != null && langName.length() > 0)
							{
								m_Helper.registerForRoundTripOperationEvents(m_EventsSink, langName);
								m_Helper.registerForRoundTripAttributeEvents(m_EventsSink, langName);
								m_Helper.registerForRoundTripClassEvents(m_EventsSink, langName);
								m_Helper.registerForRoundTripPackageEvents(m_EventsSink, langName);
								m_Helper.registerForRoundTripRelationEvents(m_EventsSink, langName);
							}
						}
					}
				}
			}
			m_Helper.registerForRoundTripRequestProcessorInitEvents(m_EventsSink);
		}
	}

	/**
	 * register for core product dispatcher
	 */
	public void registerToCoreProductDispatcher()
	{
		if (m_EventsSink != null)
		{
			m_Helper.registerForInitEvents(m_EventsSink);
		}
	}

	/**
	 * registers to classifier dispatcher
	 */
	public void registerToClassifierDispatcher()
	{
		if (m_EventsSink != null)
		{
			m_Helper.registerForClassifierFeatureEvents(m_EventsSink);
			m_Helper.registerForTransformEvents(m_EventsSink);
			m_Helper.registerForFeatureEvents(m_EventsSink);
			m_Helper.registerForStructuralFeatureEvents(m_EventsSink);
			m_Helper.registerForBehavioralFeatureEvents(m_EventsSink);
			m_Helper.registerForParameterEvents(m_EventsSink);
			m_Helper.registerForTypedElementEvents(m_EventsSink);
			m_Helper.registerForAttributeEvents(m_EventsSink);
			m_Helper.registerForOperationEvents(m_EventsSink);
			m_Helper.registerForAffectedElementEvents(m_EventsSink);
			m_Helper.registerForAssociationEndEvents(m_EventsSink);
		}
	}

	/**
	 * Registers to the lifetime dispatcher
	 */
	public void registerToLifeTimeDispatcher()
	{
		if (m_EventsSink != null)
		{
			m_Helper.registerForLifeTimeEvents(m_EventsSink);
			m_Helper.registerElementDisposalEvents(m_EventsSink);
		}
	}

	/**
	 * Registers to the relations dispatcher
	 */
	public void registerToRelationDispatcher()
	{
		if (m_EventsSink != null)
		{
			m_Helper.registerForRelationValidatorEvents(m_EventsSink);
			m_Helper.registerForRelationEvents(m_EventsSink);
		}
	}

	/**
	 * Registers to the project tree filter dialog dispatcher
	 */
	public void registerToProjectTreeFilterDialogDispatcher()
	{
		if (m_EventsSink != null)
		{
			m_Helper.registerProjectTreeFilterDialogEvents(m_EventsSink);
		}
	}

	/**
	 * Registers to the Edit control dispatcher
	 */
	public void registerToEditCtrlDispatcher()
	{
		if (m_EventsSink != null)
		{
			m_Helper.registerEditCtrlEvents(m_EventsSink);
		}
	}

	/**
	 * Registers to the Modified dispatcher
	 */
	public void registerToModifiedDispatcher()
	{
		if (m_EventsSink != null)
		{
			m_Helper.registerForElementModifiedEvents(m_EventsSink);
			m_Helper.registerForMetaAttributeModifiedEvents(m_EventsSink);
			m_Helper.registerForDocumentationModifiedEvents(m_EventsSink);
			m_Helper.registerForNamespaceModifiedEvents(m_EventsSink);
			m_Helper.registerForNamedElementEvents(m_EventsSink);
			m_Helper.registerForExternalElementEventsSink(m_EventsSink);
			m_Helper.registerForStereotypeEventsSink(m_EventsSink);
			m_Helper.registerForRedefinableElementModifiedEvents(m_EventsSink);
			m_Helper.registerForPackageEvents(m_EventsSink);
		}
	}

	/**
	 * Registers to the AddIn dispatcher
	 */
	public void registerToAddInDispatcher()
	{
		if (m_EventsSink != null)
		{
//			m_Helper.registerAddInEvents(m_EventsSink);
		}
	}

	/**
	 * Registers to the Project Tree dispatcher
	 */
	public void registerToProjectTreeDispatcher()
	{
		if (m_EventsSink != null)
		{
			m_Helper.registerProjectTreeEvents(m_EventsSink);
			m_Helper.registerProjectTreeContextMenuEvents(m_EventsSink);
		}
	}

	/**
	 * 
	 */
	public void registerToVBADispatcher()
	{
		// TODO Auto-generated method stub
		
	}

	/**
	 * Registers to the events dispatcher
	 */
	public void registerToMessengerDispatcher()
	{
		if (m_EventsSink != null)
		{
			m_Helper.registerMessengerEvents(m_EventsSink);
		}
	}

	/**
	 * Registers to the drawing area dispatcher
	 */
	public void registerToDrawingAreaDispatcher()
	{
		if (m_EventsSink != null)
		{
			DispatchHelper helper = new DispatchHelper();
                        // TODO: meteora
//			helper.registerDrawingAreaEvents(m_EventsSink);
//			helper.registerDrawingAreaSynchEvents(m_EventsSink);
//			helper.registerDrawingAreaContextMenuEvents(m_EventsSink);
//			helper.registerDrawingAreaSelectionEvents(m_EventsSink);
//			helper.registerDrawingAreaAddNodeEvents(m_EventsSink);
//			helper.registerDrawingAreaAddEdgeEvents(m_EventsSink);
//			helper.registerDrawingAreaReconnectEdgeEvents(m_EventsSink);
//			helper.registerDrawingAreaCompartmentEvents(m_EventsSink);
		}
	}

	/**
	 * Registers to the events dispatcher
	 */
	public void registerToWorkspaceDispatcher()
	{
		if (m_EventsSink != null)
		{
			m_Helper.registerForWorkspaceEvents(m_EventsSink);
			m_Helper.registerForWSProjectEvents(m_EventsSink);
			m_Helper.registerForWSElementEvents(m_EventsSink);
		}
	}

	/**
	 * Called when the addin is deinitialized.
	 */
	public long deInitialize(Object context)
	{
		// Close our dialog if its up
		if (m_EventsDialog != null)
		{
			m_EventsDialog.setVisible(false);
			m_EventsDialog = null;
		}
		
		// Release our message service
		m_MessageService = null;
		
		// Unregister from the dispatchers
		unregisterToWorkspaceDispatcher();
		unregisterToDrawingAreaDispatcher();
		unregisterToMessengerDispatcher();
		unregisterToVBADispatcher();
		unregisterToProjectTreeDispatcher();
		unregisterToAddInDispatcher();
		unregisterToModifiedDispatcher();
		unregisterToEditCtrlDispatcher();
		unregisterToProjectTreeFilterDialogDispatcher();
		unregisterToRelationDispatcher();
		unregisterToLifeTimeDispatcher();
		unregisterToClassifierDispatcher();
		unregisterToCoreProductDispatcher();
		unregisterToRoundTripDispatcher();
		unregisterToPreferenceManagerDispatcher();
		unregisterToDynamicsDispatcher();
		unregisterToActivitiesDispatcher();
		unregisterToStructureDispatcher();
		unregisterToDesignPatternDispatcher();
		unregisterToSCMDispatcher();

		// Delete our sink
		if (m_EventsSink != null)
		{
			m_EventsSink = null;
		}
		
		return 0;
	}

	/**
	 * unregister from SCM dispatcher
	 */
	public void unregisterToSCMDispatcher()
	{
		if (m_EventsSink != null)
		{
			m_Helper.revokeSCMSink(m_EventsSink);
		}
	}

	/**
	 * 
	 */
	public void unregisterToDesignPatternDispatcher()
	{
		// TODO Auto-generated method stub
		
	}

	/**
	 * unregisters to structure dispatcher
	 */
	public void unregisterToStructureDispatcher()
	{
		if (m_EventsSink != null)
		{
			try
			{
				m_Helper.revokeProjectSink(m_EventsSink);
				//m_Helper.revokeProjectUpgradeSink(m_EventsSink);
				m_Helper.revokeArtifactSink(m_EventsSink);
			}
			catch (InvalidArguments e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * unregisters from the activities dispatcher
	 */
	public void unregisterToActivitiesDispatcher()
	{
		if (m_EventsSink != null)
		{
			m_Helper.revokeActivityEdgeSink(m_EventsSink);
		}
	}

	/**
	 * unregisters from dynamics dispatcher
	 */
	public void unregisterToDynamicsDispatcher()
	{
		if (m_EventsSink != null)
		{
			m_Helper.revokeDynamicsSink(m_EventsSink);
		}
	}

	/**
	 * unregisters from preference manager dispatcher
	 */
	public void unregisterToPreferenceManagerDispatcher()
	{
		if (m_EventsSink != null)
		{
			try
			{
				m_Helper.revokePreferenceManagerSink(m_EventsSink);
			}
			catch (InvalidArguments e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * unregisters from round trip dispatchers
	 */
	public void unregisterToRoundTripDispatcher()
	{
		if (m_EventsSink != null)
		{
			m_Helper.revokeRoundTripOperationEvents(m_EventsSink);
			m_Helper.revokeRoundTripAttributeEvents(m_EventsSink);
			m_Helper.revokeRoundTripClassEvents(m_EventsSink);
			m_Helper.revokeRoundTripPackageEvents(m_EventsSink);
			m_Helper.revokeRoundTripRelationEvents(m_EventsSink);
			m_Helper.revokeRoundTripRequestProcessorInitEvents(m_EventsSink);
		}
	}

	/**
	 * unregisters from core product dispatcher
	 */
	public void unregisterToCoreProductDispatcher()
	{
		if (m_EventsSink != null)
		{
			try
			{
				m_Helper.revokeInitSink(m_EventsSink);
			}
			catch (InvalidArguments e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * unregisters classifier dispatcher
	 */
	public void unregisterToClassifierDispatcher()
	{
		if (m_EventsSink != null)
		{
			m_Helper.revokeClassifierFeatureSink(m_EventsSink);
			m_Helper.revokeTransformSink(m_EventsSink);
			m_Helper.revokeFeatureSink(m_EventsSink);
			m_Helper.revokeStructuralFeatureSink(m_EventsSink);
			m_Helper.revokeBehavioralFeatureSink(m_EventsSink);
			m_Helper.revokeParameterSink(m_EventsSink);
			m_Helper.revokeTypedElementSink(m_EventsSink);
			m_Helper.revokeAttributeSink(m_EventsSink);
			m_Helper.revokeOperationSink(m_EventsSink);
			m_Helper.revokeAffectedElementEvents(m_EventsSink);
			m_Helper.revokeAssociationEndEvents(m_EventsSink);
		}
	}

	/**
	 * Unregisters for life time dispatcher
	 */
	public void unregisterToLifeTimeDispatcher()
	{
		if (m_EventsSink != null)
		{
			m_Helper.revokeLifeTimeSink(m_EventsSink);
			m_Helper.revokeElementDisposalEventsSink(m_EventsSink);
		}
	}

	/**
	 * UnRegisters from the relation dispatcher
	 */
	public void unregisterToRelationDispatcher()
	{
		if (m_EventsSink != null)
		{
			m_Helper.revokeRelationValidatorSink(m_EventsSink);
			m_Helper.revokeRelationSink(m_EventsSink);
		}
	}

	/**
	 * UnRegisters from the Modified dispatcher
	 */
	public void unregisterToProjectTreeFilterDialogDispatcher()
	{
		if (m_EventsSink != null)
		{
			m_Helper.revokeProjectTreeFilterDialogSink(m_EventsSink);
		}
	}

	/**
	 * UnRegisters from the Modified dispatcher
	 */
	public void unregisterToEditCtrlDispatcher()
	{
		if (m_EventsSink != null)
		{
			m_Helper.revokeEditCtrlSink(m_EventsSink);
		}
	}

	/**
	 * UnRegisters from the Modified dispatcher
	 */
	public void unregisterToModifiedDispatcher()
	{
		if (m_EventsSink != null)
		{
			m_Helper.revokeElementModifiedSink(m_EventsSink);
			m_Helper.revokeMetaAttributeModifiedSink(m_EventsSink);
			m_Helper.revokeDocumentationModifiedSink(m_EventsSink);
			m_Helper.revokeNamespaceModifiedSink(m_EventsSink);
			m_Helper.revokeNamedElementSink(m_EventsSink);
			m_Helper.revokeExternalElementEventsSink(m_EventsSink);
			m_Helper.revokeStereotypeEventsSink(m_EventsSink);
			m_Helper.revokeRedefinableElementModifiedEvents(m_EventsSink);
			m_Helper.revokePackageEvents(m_EventsSink);
		}
	}

	/**
	 * UnRegisters from the AddIn dispatcher
	 */
	public void unregisterToAddInDispatcher()
	{
		if (m_EventsSink != null)
		{
//			m_Helper.revokeAddInSink(m_EventsSink);
		}
	}

	/**
	 * UnRegisters from the Project Tree dispatcher
	 */
	public void unregisterToProjectTreeDispatcher()
	{
		if (m_EventsSink != null)
		{
			m_Helper.revokeProjectTreeSink(m_EventsSink);
			m_Helper.revokeProjectTreeContextMenuSink(m_EventsSink);
		}
	}

	/**
	 * 
	 */
	public void unregisterToVBADispatcher()
	{
		// TODO Auto-generated method stub
		
	}

	/**
	 * UnRegisters from the events dispatcher
	 */
	public void unregisterToMessengerDispatcher()
	{
		if (m_EventsSink != null)
		{
			m_Helper.revokeMessengerSink(m_EventsSink);
		}
	}

	/**
	 * UnRegisters from the drawing area dispatcher
	 */
	public void unregisterToDrawingAreaDispatcher()
	{
		DispatchHelper helper = new DispatchHelper();
                // TODO: meteora
//		helper.revokeDrawingAreaSink(m_EventsSink);
//		helper.revokeDrawingAreaSynchSink(m_EventsSink);
//		helper.revokeDrawingAreaContextMenuSink(m_EventsSink);
//		helper.revokeDrawingAreaSelectionSink(m_EventsSink);
//		helper.revokeDrawingAreaAddNodeSink(m_EventsSink);
//		helper.revokeDrawingAreaAddEdgeSink(m_EventsSink);
//		helper.revokeDrawingAreaReconnectEdgeSink(m_EventsSink);
//		helper.revokeDrawingAreaCompartmentSink(m_EventsSink);
	}

	/**
	 * UnRegisters from the events dispatcher
	 */
	public void unregisterToWorkspaceDispatcher()
	{
		if (m_EventsSink != null)
		{
			try
			{
				m_Helper.revokeWorkspaceSink(m_EventsSink);
				m_Helper.revokeWSProjectSink(m_EventsSink);
				m_Helper.revokeWSElementSink(m_EventsSink);
			}
			catch (InvalidArguments e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Called when the addin is unloaded.
	 */
	public long unLoad(Object context)
	{
		return 0;
	}

	/**
	 * The version of the addin.
	 */
	public String getVersion()
	{
		return m_version;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.addinframework.IAddIn#getName()
	 */
	public String getName()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.addinframework.IAddIn#getID()
	 */
	public String getID()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.addinframework.IAddIn#getLocation()
	 */
	public String getLocation()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Retrieves all the buttons for this add-in.
	 */
//	public ETList<IAddInButton> getButtons()
//	{
//		ETList<IAddInButton> retObj = new ETArrayList<IAddInButton>();
//		IAddInButton mainButton = new AddInButton();
//		mainButton.setAddInButtonTargetKind(IAddInButtonTargetKind.DTK_MENU_ALL);
//		mainButton.setName("Show Events Dialog");
//		mainButton.setID(s_MainFrameButtonId);
//		mainButton.setLocation("File");
//		mainButton.setPlaceAbove("Exit");
//		mainButton.setSeparatorAfter(true);
//		retObj.add(mainButton);
//		return retObj;
//	}

	/**
	 * Called when one of the buttons added to the gui has been selected by the user.
	 */
//	public long execute(IAddInButton pButton, Frame nParentHWND)
//	{
//		//int i = pButton.getID();
//		if (m_EventsDialog != null)
//		{
//			m_EventsDialog = null;
//		}
//		m_EventsDialog = new EventsDialog(this);
//		//m_EventsDialog.create();
//		m_EventsDialog.setVisible(true);
//		return 0;
//	}

	public EventsDialog getDialog()
	{
		return m_EventsDialog;
	}

	/**
	 * Handles the onupdate coming from the gui.  The addin can set the sensitivity or checked status
	 * of the button.
	 */
//	public long update(IAddInButton pButton, Frame nParentHWND)
//	{
//		int id = pButton.getID();
//		if (id == s_MainFrameButtonId)
//		{
//			if (m_DiagramManager != null)
//			{
//				pButton.setSensitive(true);
//			}
//			else
//			{
//				pButton.setSensitive(false);
//			}
//		}
//		else
//		{
//			pButton.setSensitive(true);
//		}
//		return 0;
//	}

	/**
	 * Returns the clsid of this addin.
	 */
	public String getProgID()
	{
		return "org.netbeans.modules.uml.ui.addins.eventlogger.EventLoggingAddin";
	}

}



