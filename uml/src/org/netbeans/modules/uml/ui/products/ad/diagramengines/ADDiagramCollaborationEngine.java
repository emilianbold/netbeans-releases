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


package org.netbeans.modules.uml.ui.products.ad.diagramengines;

import java.awt.event.ActionEvent;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.coreapplication.IPreferenceManager2;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IAutonomousElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.MetaLayerRelationFactory;
import org.netbeans.modules.uml.core.metamodel.core.foundation.TypedFactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramKind;
import org.netbeans.modules.uml.core.metamodel.dynamics.ICombinedFragment;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteraction;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionOccurrence;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionOperand;
import org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature;
import org.netbeans.modules.uml.core.metamodel.structure.Comment;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl;
import org.netbeans.modules.uml.ui.products.ad.application.action.ContextMenuActionClass;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchive;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import com.tomsawyer.editor.TSEGraph;

//import com.tomsawyer.layout.property.*;
//import com.tomsawyer.layout.glt.property.*;

/**
 * 
 * @author Trey Spiva
 */
public class ADDiagramCollaborationEngine extends ADCoreEngine implements IADCollaborationDiagEngine
{
	public final static String COD_SHOW_MESSAGE_NUMBERS = "ShowMessageNumbers";

	private boolean m_ShowMessageNumbers = isDefaultShowMessageNumbers();
	
	/**
	 * Looks in the preference to see if we should show message numbers by default
	 *
	 * @return true if the preference indicates that we should show message numbers by default
	 */
	private boolean isDefaultShowMessageNumbers()
	{
		boolean bDefaultShowMessageNumbers = false;
   
		IPreferenceManager2 pPrefMgr = ProductHelper.getPreferenceManager();
		if (pPrefMgr != null)
		{
			String sPrefValue = pPrefMgr.getPreferenceValue("Diagrams|CollaborationDiagram", "DefaultShowMessageNumbers");
			if (sPrefValue.equals("PSK_YES"))
			{
				bDefaultShowMessageNumbers = true;
			}
		}

		return bDefaultShowMessageNumbers;
	}
	
	public boolean setSensitivityAndCheck(String id, ContextMenuActionClass pClass)
	{
		boolean bFlag = super.setSensitivityAndCheck(id, pClass);;
		if (id.equals("MBK_SQD_SHOW_MESSAGE_NUMBERS"))
		{
			pClass.setChecked(m_ShowMessageNumbers);
		}
		return bFlag;
	}
	
	public boolean onHandleButton(ActionEvent e, String id)
	{
		if (id.equals("MBK_SQD_SHOW_MESSAGE_NUMBERS"))
		{
			m_ShowMessageNumbers = !m_ShowMessageNumbers;
			IDrawingAreaControl control = getDrawingArea();
			if (control != null)
			{
				refreshMessageNumbers();
				control.setIsDirty(true);
				return true;
			}
		}
		else
		{
			return super.onHandleButton(e, id);
		}
		return false;
	}

	/**
	 * Show/hide the message numbers
	 */
	private void refreshMessageNumbers()
	{
		IDrawingAreaControl control = getDrawingArea();
		if (control != null)
		{
			ETList<IPresentationElement> elements = control.getAllByType("MessageConnector");
			if (elements != null)
			{
				int count = elements.size();
				for (int i=0; i<count; i++)
				{
					IPresentationElement pEle = elements.get(i);
					IDrawEngine pEngine = TypeConversions.getDrawEngine(pEle);
					if (pEngine != null)
					{
						ILabelManager labelMgr = pEngine.getLabelManager();
						if (labelMgr != null)
						{
							labelMgr.resetLabelsText();
						}
					}
				}
			}
		}
	}

	public void initializeNewDiagram()
	{
		if (getDrawingArea() != null)
		{
			getCollaborationDiagramInteraction();
			getDrawingArea().setIsDirty(true);
		}
	}
	
	protected IInteraction getCollaborationDiagramInteraction()
	{
		IInteraction pInteractionForCollaborationDiagram = null;
		
		IDrawingAreaControl control = getDrawingArea();
		IDiagram pDiagram = control.getDiagram();
		
		INamespace pNamespace = null;
		String name = null;
		if (pDiagram != null)
		{
			pNamespace = pDiagram.getNamespace();
			name = pDiagram.getName();
		}
		
		if (pNamespace != null && pNamespace instanceof IInteraction)
		{
			pInteractionForCollaborationDiagram = (IInteraction)pNamespace;
		}
		
		// If we did not find an interaction, create an interaction, and create the associated presentation reference
		if (pInteractionForCollaborationDiagram == null)
		{
			// Create the activity for the activity diagram.
			TypedFactoryRetriever<IInteraction> factory = new TypedFactoryRetriever<IInteraction>();
			pInteractionForCollaborationDiagram = factory.createType("Interaction");
			
			if (pInteractionForCollaborationDiagram != null)
			{
				// Give the activity the same name as the diagram
				pInteractionForCollaborationDiagram.setName(name);
				
				// Associate the activity with the diagram's current namespace
				pInteractionForCollaborationDiagram.setNamespace(pNamespace);
				
				// Move the activity diagram under the activity
				control.setNamespace(pInteractionForCollaborationDiagram);
			}
		}
		
		return pInteractionForCollaborationDiagram;
	}
	
	public ETPairT < Boolean,IElement > processOnDropElement(IElement pElementBeingDropped)
	{
		boolean bCancelThisElement = false;
		IElement pChangedElement = null;
		
		boolean establishImport = false;
	
		ICombinedFragment combinedFragment = null;
		if (pElementBeingDropped instanceof ICombinedFragment)
			combinedFragment = (ICombinedFragment) pElementBeingDropped;
	
		IInteraction interaction = null;
		if (pElementBeingDropped instanceof IInteraction)
			interaction = (IInteraction)pElementBeingDropped;
	
		IAttribute attribute = null;
		if (pElementBeingDropped instanceof IAttribute)
			attribute = (IAttribute)pElementBeingDropped;
	
		IClassifier classifier = null;
		if (pElementBeingDropped instanceof IClassifier)
			classifier = (IClassifier)pElementBeingDropped;
                
		if(pElementBeingDropped instanceof ILifeline ||
                        pElementBeingDropped instanceof Comment) 
                {
                    establishImport = true;
                }
                
		if (combinedFragment != null) {
			// Add the InteractionOperand, if necessary
			boolean bCreateInteractioOperand = true;
	
			ETList<IInteractionOperand> interactionOperands = combinedFragment.getOperands();
	
			if (interactionOperands != null) {
				int lCnt = interactionOperands.getCount();
				bCreateInteractioOperand = (lCnt < 1);
			}
	
			if (bCreateInteractioOperand) {
				// Create the InteractionOperand
				TypedFactoryRetriever<IInteractionOperand> factory = new TypedFactoryRetriever<IInteractionOperand>();

				IInteractionOperand interactionOperand = factory.createType("InteractionOperand");
				if (interactionOperand != null) {
					combinedFragment.addOperand(interactionOperand);
					establishImport = true;
				}
			}
		} else if (interaction != null) {
			bCancelThisElement = true;
	
			// Fix W1762:  Make sure the interaction is not the same as this diagram's parent interaction
			INamespace namespace = getDrawingArea().getNamespace();
			IInteraction diagramsInteraction = null;
			if(namespace instanceof IInteraction)
				diagramsInteraction = (IInteraction)namespace;
			
			if (diagramsInteraction != interaction && namespace != null) {
				// Create the IInteractionOccurrence, and attach the interaction
				TypedFactoryRetriever<IInteractionOccurrence> factory = new TypedFactoryRetriever<IInteractionOccurrence>();
				IInteractionOccurrence interactionOccurrence = factory.createType("InteractionOccurrence");
			
				if (interactionOccurrence != null) {
					interactionOccurrence.setInteraction(interaction);

					// Use the diagram's namespace for the interaction occurrence
					interactionOccurrence.setNamespace(namespace);

					// Set the rcpElement to be the interaction occurrence so,
					// the rest of the attach stuff works
					pChangedElement = interactionOccurrence;
					bCancelThisElement = false;
					establishImport = true;
				}
			}
		} 
		else if (classifier != null)
		{
			ILifeline cpLifeline = createLifeline(classifier);
			if( cpLifeline != null )
			{
				pChangedElement = cpLifeline;
				establishImport = true;
			}
		}
		else if (attribute != null)
		{
			classifier = attribute.getType(); // The attribute's type is the lifeline's representing classifier
			if (classifier != null)
			{
				ILifeline cpLifeline = createLifeline(classifier);
				if( cpLifeline != null )
				{
				   // UPDATE:  for now we name the lifeline based on the attribute name
				   String bsName = attribute.getName();
				   cpLifeline.setName( bsName );

				   pChangedElement = cpLifeline;
				   establishImport = true;
				}
			}
		}
		// add logic to import elements #6283783
		IElement owner = getOwner();
		if (owner != null) 
                {
                    // The below check has been replaced by check for the same project. We are interested in the topmost owner
                    //	i.e project rather than just the owner of an element.
                    
                    boolean isSame = owner.inSameProject(pElementBeingDropped);
                    if (!isSame) 
                    {
                        // Only AutonomousElements can be imported across Projects
                        if (pElementBeingDropped instanceof IAutonomousElement) 
                        {
                            MetaLayerRelationFactory.instance().establishImportIfNeeded(owner, pElementBeingDropped);
                        }
                    }
                }
		bCancelThisElement = !establishImport;
		return new ETPairT<Boolean,IElement>(new Boolean(bCancelThisElement),pChangedElement);
	}
	
//	protected IInteraction getCollaborationDiagramInteraction()
//	{
//		IInteraction pInteraction = null;
//
//		if (m_DrawingArea != null)
//		{
//			INamespace pNamespace = m_DrawingArea.getNamespace();
//			String sName = m_DrawingArea.getName();
//
//			if (pNamespace instanceof IInteraction)
//			{
//				pInteraction = (IInteraction)pNamespace;
//			}
//
//			// If we did not find an interaction,
//			// create an interaction, and create the associated presentation reference
//			if( pInteraction == null )
//			{
//				TypedFactoryRetriever<IInteraction> factory = new TypedFactoryRetriever<IInteraction>();
//				pInteraction = factory.createType("Interaction");
//									
//				if (pInteraction != null)
//				{
//					// Give the activity the same name as the diagram
//					pInteraction.setName( sName );
//
//					// Associate the activity with the diagram's current namespace
//					pInteraction.setNamespace( pNamespace);
//
//					// Move the activity diagram under the activity
//					INamespace pNamespaceInteraction = (INamespace)pInteraction;
//					m_DrawingArea.setNamespace( pNamespaceInteraction );
//				}
//			}
//		}
//		return pInteraction;
//	}
	
	protected void updateLifelineData(IElement pElement)
	{
		if ( pElement instanceof ILifeline)
		{
			ILifeline pLifeline = (ILifeline)pElement;
			IInteraction cpInteraction = pLifeline.getInteraction();

			if( cpInteraction == null )
			{
				// Use the diagram's associated interaction to attach the lifeline
				cpInteraction = getCollaborationDiagramInteraction();
				pLifeline.setInteraction( cpInteraction );

				// Add the lifeline to the interaction
				if( cpInteraction != null )
				{
					cpInteraction.addLifeline( pLifeline );
				}
			}
		}
	}
	
	protected ILifeline createLifeline( IClassifier pClassifier)
	{
		ILifeline pLifeline = null;

		// Create the ILifeline
		TypedFactoryRetriever<ILifeline> factory = new TypedFactoryRetriever<ILifeline>();
		pLifeline = factory.createType("Lifeline");
		if ( pLifeline != null )
		{
			updateLifelineData( pLifeline );
			pLifeline.initializeWith( pClassifier );

			// Fix W2438:  Need to refresh the project tree
			IProjectTreeControl projectTree = ProductHelper.getProjectTree();
			if(projectTree != null)
			{
				projectTree.refresh(true); 
			}
		}

		return pLifeline;
	}
	
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.diagramengines.IADCollaborationDiagEngine#showMessageNumbers()
	 */
	public boolean showMessageNumbers() {
		return this.m_ShowMessageNumbers;
	}

	public void registerAccelerators()
	{
		ETList<String> accelsToRegister = new ETArrayList<String>();

		// Add the normal accelerators
		addNormalAccelerators(accelsToRegister, true);

		// Toggle orthogonality
		accelsToRegister.add(IDrawingAreaAcceleratorKind.DAVK_TOGGLE_ORTHOGONALITY );

		registerAcceleratorsByType(accelsToRegister);
	}
	
	protected void getNotificationTargets(INotificationTargets pTargets)
	{
		IElement pChangedME = null;
		IElement pSecondaryChangedME = null;
		IFeature pChangedFeature = null;
		IDiagram pDiagram = null;
		int nDiagramKind = IDiagramKind.DK_DIAGRAM;

		if (getDrawingArea() != null)
		{
			nDiagramKind = getDrawingArea().getDiagramKind();
			pDiagram = getDrawingArea().getDiagram();
		}

		pChangedME = pTargets.getChangedModelElement();
		pSecondaryChangedME = pTargets.getSecondaryChangedModelElement();
		if (pSecondaryChangedME instanceof IFeature)
		{
			pChangedFeature = (IFeature)pSecondaryChangedME;
		}

		if (pChangedME != null)
		{
			// The Labels on the connectors are associated with IMessages, not the IOperations
			// so if an IOperation changes we need to notify all the messages associated with
			// that operation

			IFeature pTempFeature = null;
			if (pChangedME instanceof IFeature)
			{
				pTempFeature = (IFeature)pChangedME;
			}

			if (pTempFeature != null || pChangedFeature != null)
			{
				// See if any of the messages on this diagram relate to this feature
				// Must call GetAllItems, not get the presentation elements off the ME because
				// the PE's could be across diagrams.
				ETList<IPresentationElement> pPresentationElements = getDrawingArea().getAllByType("Message");
				pTargets.addNotifiedElements( pPresentationElements );
			}
		}

		// Call the base class
		super.getNotificationTargets(pTargets);
	}
	
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine#setupLayoutSettings(boolean)
	 */
	public void setupLayoutSettings(boolean bNewDiagram)
	{	
		super.setupLayoutSettings(bNewDiagram);
		try{
			if (bNewDiagram && this.getDrawingArea() != null && getDrawingArea().getCurrentGraph() != null)
			{
				TSEGraph graph = getDrawingArea().getCurrentGraph();
                                /* jyothi
				TSBooleanLayoutProperty property = new TSBooleanLayoutProperty(TSTailorProperties.HIERARCHICAL_CALCULATED_SIZES);
				property.setCurrentValue(false);

				// Move the nodes further appart so the labels don't overlap
				TSIntLayoutProperty labelsoverlap = new TSIntLayoutProperty(TSTailorProperties.ALLOWED_LABEL_OVERLAP_PER_MIL);

				labelsoverlap.setCurrentValue(1000);

				graph.setTailorProperty(labelsoverlap);				
				
				// We experimented with numbers until this layout style gave us 

				TSIntLayoutProperty horzNodeSpacing = new TSIntLayoutProperty(TSTailorProperties.ORTHOGONAL_HORIZONTAL_NODE_SPACING);
				horzNodeSpacing.setCurrentValue(130);
				graph.setTailorProperty(horzNodeSpacing);	
				TSIntLayoutProperty vertNodeSpacing = new TSIntLayoutProperty(TSTailorProperties.ORTHOGONAL_VERTICAL_NODE_SPACING);
				vertNodeSpacing.setCurrentValue(130);
				graph.setTailorProperty(vertNodeSpacing);
				
				//_VH(SetLayoutStyleUsingPreference(CComBSTR("ActivityDiagram")));
                                 */
			}
		}
		catch(Exception e)
		{
		}
   }

	public void readFromArchive(IProductArchive pProductArchive, IProductArchiveElement pParentElement)
	{
		if ((pProductArchive != null) && (pParentElement != null))
		{
			boolean bShowMessageNumbers = isDefaultShowMessageNumbers();
			bShowMessageNumbers = pParentElement.getAttributeBool(COD_SHOW_MESSAGE_NUMBERS);

			m_ShowMessageNumbers = bShowMessageNumbers;
		}
	}

	public void writeToArchive(IProductArchive pProductArchive, IProductArchiveElement pParentElement)
	{
		if ((pProductArchive != null) && (pParentElement != null))
		{
			pParentElement.addAttributeBool(COD_SHOW_MESSAGE_NUMBERS, m_ShowMessageNumbers);
		}
	}

}
