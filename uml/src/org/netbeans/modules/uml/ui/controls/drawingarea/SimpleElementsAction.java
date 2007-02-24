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


package org.netbeans.modules.uml.ui.controls.drawingarea;

//import org.netbeans.modules.uml.core.addinframework.IAddIn;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.ICoreRelationshipDiscovery;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramValidationResult;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.addins.diagramcreator.IDiagCreatorAddIn;
import org.netbeans.modules.uml.ui.addins.diagramcreator.IDiagCreatorAddIn.CRB;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.applicationmanager.IGraphPresentation;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import java.util.Iterator;
//import org.netbeans.modules.uml.core.addinframework.IAddInManager;

/**
 * @author brettb
 *
 */
public class SimpleElementsAction implements ISimpleElementsAction {

	/**
	 * 
	 */
	public SimpleElementsAction() {
		super();		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.ISimpleElementsAction#getKind()
	 */
	public int getKind() {
		return m_Kind;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.ISimpleElementsAction#setKind(int)
	 */
	public void setKind(int value) {
		m_Kind = value;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.ISimpleElementsAction#getElements()
	 */
	public ETList < IElement > getElements() {
		return m_Elements;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.ISimpleElementsAction#setElements()
	 */
	public void setElements(ETList < IElement > newVal) {
		m_Elements = newVal;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.ISimpleElementsAction#add(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement)
	 */
	public void add(IElement newVal) {
		if (null == m_Elements) {
			m_Elements = new ETArrayList < IElement > ();
		}
		if (m_Elements != null) {
			m_Elements.addIfNotInList(newVal);
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDelayedAction#getDescription()
	 */
	public String getDescription() {
		String strDescription = "CSimpleElementsAction : ";
		strDescription += ISimpleElementsAction.SEAK.getDescription(m_Kind);

		if (m_Elements != null) {
			strDescription += ", m_Elements count =";
			final int count = m_Elements.getCount();
			strDescription += String.valueOf(count);
		}

		return strDescription;
	}
   
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDelayedAction#execute()
	 */
	public void execute(IDrawingAreaControl control) {
		if (control != null) {
			switch (m_Kind) {
				case SEAK.DEEPSYNC_BROADCAST :
					{
						handleDeepSyncBroadcast(control, false);
					}
					break;
				case SEAK.DEEPSYNC_AND_RESIZE_BROADCAST :
					{
						handleDeepSyncBroadcast(control, true);
					}
					break;
				case SEAK.DISCOVER_RELATIONSHIPS :
					{
                  handleRelationshipDiscovery(control);						
					}
					break;                 
				case SEAK.RECONNECT_PRESENTATION_ELEMENTS :
					{
						reattachPresentationElements(control);
					}
					break;
				case SEAK.DELAYED_CDFS :
					{
						// Tell the CDFS addin to CDFS this list
//						IAddInManager man=ProductHelper.getAddInManager();
//						IAddIn addIn = null;
//						if(man!=null)
//							addIn = man.retrieveAddIn("org.netbeans.modules.uml.ui.addins.diagramcreator");
                                            
                                            IDiagCreatorAddIn addIn = ProductHelper.getDiagCreatorAddIn();                                            
						IDiagram diagram = control.getDiagram();
						INamespace namespace = control.getNamespace();

						if ((addIn != null) && (diagram != null) && (namespace != null)) {                                                
							IDiagCreatorAddIn diagramCreator = (IDiagCreatorAddIn) addIn;
							assert(diagramCreator != null);
							if (diagramCreator != null) {
								/* TODO
								                     CWaitCursor wait;
								                     CComPtr < IGUIBlocker > pInvalidateBlocker;
								                     pInvalidateBlocker.coCreateInstance(__uuidof(GUIBlocker));
								
								                     // Block all invalidates.  Those are killers for performance
								                     pInvalidateBlocker.setKind( GBK_DIAGRAM_INVALIDATE );
								                     MFCTimer timeStart( "SEAK.DELAYED_CDFS Time" );
								*/
								control.zoom(1.0f);
								diagramCreator.addElementsToDiagram(diagram, m_Elements, namespace, CRB.GET_STATE_CHILDREN);

								// Validate the diagram to make sure it's all good.  This also
								// does stuff like verifies the qualifiers have a presentation
								// reference to their edge.
								IDiagramValidationResult result = diagram.validateDiagram(false, null);

								// Pump the messages to make sure nothing modifies the diagram after
								// the save.
								diagram.pumpMessages(false);

								{
									/* TODO                        
									                        CComPtr < IGUIBlocker > pLabelLayoutBlocker;
									                        pLabelLayoutBlocker.coCreateInstance(__uuidof(GUIBlocker));
									
									                        // Block all layout layouts.  Those are killers for performance
									                        pLabelLayoutBlocker.setKind( GBK_DIAGRAM_LABEL_LAYOUT);
									*/
									// Added this refresh because the diagram looked blank
									// when none of the elements were created out of the diagram's view.
									diagram.refresh(false);
								}

								// Refresh in case the draw causes edges to flip parentage
								diagram.pumpMessages(false);

								// Layout all the labels in case a swap happened in the refresh above
								// TODO diagram.relayoutAllLabels();

								diagram.save();
							}
						}
						break;
					}

				default :
					assert(false);
					break;
			}

			// Repaint the window
			control.refresh(true);
		}
	}
   
   /**
    * Handles the relationship discovery action.
    */
   protected void handleRelationshipDiscovery(IDrawingAreaControl control)
   {
      ICoreRelationshipDiscovery pRelationshipDiscovery = control.getRelationshipDiscovery();

      if (pRelationshipDiscovery != null) {
         if (getElements() == null || getElements().size() == 0)
               pRelationshipDiscovery.discoverCommonRelations(false);
         else
            pRelationshipDiscovery.discoverCommonRelations(false, getElements());						
      }
   }
   
	/**
	 * Handles the Deep sync broadcast action
	 *
	 * @param pControl [in] The parent drawing area
	 * @param bSizeToContents [in] Should we resize to contents
	 */
	private void handleDeepSyncBroadcast(IDrawingAreaControl pControl, boolean bSizeToContents)
	{
		if (pControl != null)
		{
			IDiagramEngine pDiagramEngine = pControl.getDiagramEngine();
			IDiagram pDiagram = pControl.getDiagram();
			if (pDiagramEngine != null && pDiagram != null)
			{
				boolean bHandled = false;
				if (pDiagramEngine != null)
				{
					bHandled = pDiagramEngine.preDeepSyncBroadcast(m_Elements);
				}

				if (!bHandled)
				{
					IDiagramValidator pDiagramValidator =  new DiagramValidator();
					int count = 0;
					if (m_Elements != null)
					{
						count = m_Elements.getCount();
					}

					/*
					CComPtr < TSCOM::TSGraphEditor > pGraphEditor;

					_VH(pControl->GetGraphEditor(&pGraphEditor));

					// Pin the list of our items on the diagram so we only get once if the
					// draw engines need to be deep synched.
					CPinGraphObjectList pinList(pGraphEditor);
					*/

					// First force all elements to deep sync
					for (int i = 0 ; i < count ; i++)
					{
						IElement pElement = m_Elements.get(i);
						if (pElement != null)
						{
							if (pDiagramValidator != null && pElement != null)
							{
								pDiagramValidator.forceElementDeepSync(pDiagram, pElement);
								if (bSizeToContents)
								{
									int count2 = 0;
									ETList <IPresentationElement> pFoundObjects = pControl.getAllItems2(pElement);
									if (pFoundObjects != null)
									{
										count2 = pFoundObjects.getCount();
									}
									for (int j = 0 ; j < count2 ; j++)
									{
										IPresentationElement pPE = pFoundObjects.get(j);
										if (pPE != null)
										{
											IETGraphObject pETGraphObject = TypeConversions.getETGraphObject(pPE);
											if (pETGraphObject != null)
											{
												pETGraphObject.sizeToContents();
											}
										}
									}
								}
							}
						}
					}

					// Then validate the diagram
					pControl.validateDiagram(false, null);
         
					pDiagramEngine.postDeepSyncBroadcast(m_Elements);
				}
			}
		}
	}
	

   /**
    * Reattaches presentation elements
    *
    * @param pControl [in] The parent drawing area
    */
   protected void reattachPresentationElements(IDrawingAreaControl pControl)
   {
      if(pControl != null)
      {
//         long count = 0;
//         if (m_Elements)
//         {
//            _VH(m_Elements.getCount(&count));
//         }

         Iterator < IElement > iter = m_Elements.iterator();
         while(iter.hasNext() == true)
         {
            IElement pElement = iter.next();
            if (pElement != null)
            {
               ETList < IPresentationElement > pPEs = pControl.getAllItems();

               // First attach all the presentation elements to their
               // model elements
               ETList < IPresentationElement > foundElements = new ETArrayList < IPresentationElement >();
               ETList < IPresentationElement > notFoundElements = new ETArrayList < IPresentationElement >();

               Iterator < IPresentationElement > peIter = pPEs.iterator();
               while(peIter.hasNext() == true)
               {
                  IPresentationElement pPE = peIter.next();
                  if (pPE != null)
                  {
                     // Get the model element and see if its XMIID is the same as pVerElement
                     boolean bIsSame = pPE.isFirstSubject(pElement);
                     if (bIsSame == true)
                     {
                        foundElements.add(pPE);
                     }
                     else
                     {
                        notFoundElements.add(pPE);
                     }
                  }
               }

               Iterator < IPresentationElement > foundIter = foundElements.iterator();

               // Get the model element and see if its XMIID is the same as pVerElement
               while (foundIter.hasNext() == true)
               {
                  IPresentationElement curElement = foundIter.next();
                  curElement.removeSubject(pElement);
                  curElement.addSubject(pElement);
               }

               // Just clear out the PEs cached model element.  When the user checks in/out of SCC the elements
               // selected go into another document.  The SCCIntegrator will handle those element to make sure
               // any in memory objects get their dom node changed, but not children.  So, for instance, if
               // you check in an end of a generalization, the generalization will NOT get it's DOM node changed.
               // So we have to loop over all PE's during SCC events and clear out the ME cache on everything.
               Iterator < IPresentationElement > notFoundIter = notFoundElements.iterator();
               while (notFoundIter.hasNext() == true)
               {
                  IPresentationElement curPresentation = notFoundIter.next();
                  if(curPresentation instanceof IGraphPresentation)
                  {
                     IGraphPresentation pGraphPE = (IGraphPresentation)curPresentation;
                     pGraphPE.externalElementLoaded();
                  }
               }

               // Now fire the postload event so that the bridges can reparent themselves to
               // their owning presentation element
               Iterator < IPresentationElement > foundIter2= foundElements.iterator();
               while (foundIter2.hasNext() == true)
               {
                  IPresentationElement curElement = foundIter2.next();
                  IETGraphObject pETGraphObject = TypeConversions.getETGraphObject(curElement);
                  if (pETGraphObject != null)
                  {
                     pETGraphObject.postLoad();
                  }
               }
            }
         }
      }
   }

	private int m_Kind = SEAK.DEEPSYNC_BROADCAST; // SimpleElementsActionKind
	ETList < IElement > m_Elements;

	/// Used in for the memory detector addin
	private static long m_Instances = 0;
}
