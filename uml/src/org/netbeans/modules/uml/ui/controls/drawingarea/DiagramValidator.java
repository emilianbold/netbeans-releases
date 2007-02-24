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

import java.util.Iterator;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.eventframework.IEventDispatcher;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.DiagramValidationResult;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramValidateKind;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramValidateResponse;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramValidateResult;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramValidation;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramValidationResult;
import org.netbeans.modules.uml.core.metamodel.diagrams.IGraphObjectValidation;
import org.netbeans.modules.uml.core.metamodel.diagrams.ISynchStateKind;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.support.DispatchHelper;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.applicationmanager.IEdgePresentation;
import org.netbeans.modules.uml.ui.support.applicationmanager.IGraphPresentation;
import org.netbeans.modules.uml.ui.support.applicationmanager.IPresentationTypesMgr;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.swing.drawingarea.DiagramPerformSyncContext;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramPerformSyncContext;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import org.netbeans.modules.uml.ui.swing.drawingarea.IPresentationElementPerformSyncContext;
import org.netbeans.modules.uml.ui.swing.drawingarea.IPresentationElementSyncState;
import org.netbeans.modules.uml.ui.swing.drawingarea.PresentationElementPerformSyncContext;
import com.tomsawyer.graph.TSEdge;
import com.tomsawyer.graph.TSGraphObject;
import com.tomsawyer.graph.TSNode;
import com.tomsawyer.util.TSObject;

/**
 * @author josephg
 *
 */
public class DiagramValidator implements IDiagramValidator {
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.IDiagramValidator#doPostSelectValidation(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject)
	 */
	public void doPostSelectValidation(IDiagram diagram,IETGraphObject etGraphObject) {
		if(diagram == null || etGraphObject == null)
			return;
			
		int nSynchState = ISynchStateKind.SSK_UNKNOWN_SYNCH_STATE;
		
		boolean proceed = dispatchPreRetrieveElementSynchState(diagram,etGraphObject);
		
		if(proceed) {
			nSynchState = etGraphObject.getSynchState();
			
			int nNewSynchState = nSynchState;
			ETPairT<Boolean,Integer> postRetrieveResult = dispatchPostRetrieveElementSynchState(diagram,etGraphObject,nNewSynchState);

			if(postRetrieveResult.getParamOne().booleanValue()) {
				nSynchState = postRetrieveResult.getParamTwo().intValue(); 
			}
		}
		
		if(nSynchState != ISynchStateKind.SSK_IN_SYNCH_DEEP && nSynchState != ISynchStateKind.SSK_OUT_OF_SYNCH) {
			IPresentationElement presentationElement = TypeConversions.getPresentationElement(etGraphObject);
			
			if(proceed) {
				if(presentationElement != null) {
					proceed = dispatchPrePresentationElementPerformSync(diagram,presentationElement);
				}
				
				if(proceed) {
					TSObject tempObject = null;
					if(etGraphObject instanceof TSObject)
						tempObject = (TSObject)etGraphObject;
						
					if(tempObject != null) {
						m_InvalidDeeps.add(tempObject);
						
						performDeepSynch();
						
						clear();
						
					}
					
					dispatchPostPresentationElementPerformSync(diagram,presentationElement);
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.IDiagramValidator#forceElementDeepSync(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.core.metamodel.core.foundation.IElement)
	 */
	public void forceElementDeepSync(IDiagram diagram,IElement elementToDeepSync) {
		ETList<IPresentationElement> pes = diagram.getAllItems2(elementToDeepSync);
		
		int count = 0;
		if(pes != null)
			count = pes.getCount();
			
		for(int index = 0; index < count; index++) {
			IPresentationElement pe = pes.item(index);
			
			if(pe != null) {
				IGraphPresentation graphPE = null;
				if(pe instanceof IGraphPresentation)
					graphPE = (IGraphPresentation)pe;
				
				if(graphPE != null) {
					//graphPE.clearModelElementCache();
               graphPE.setModelElement(null);
				}
				
				TSObject tempObject = TypeConversions.getTSObject(pe);
				
				if(tempObject != null) {
					m_InvalidDeeps.add(tempObject);
					
					performDeepSynch();
				}
			}
		}
		
		clear();
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.IDiagramValidator#validateDiagram(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, boolean, org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramValidation)
	 */
	public IDiagramValidationResult validateDiagram(IDiagram diagram,boolean onlySelectedElements,IDiagramValidation diagramValidation) {
		clear();
		
		IDiagramValidationResult tempResult = new DiagramValidationResult();
		
		boolean proceed = true;
		boolean validateNodes = false;
		boolean validateLinks = false;
		
		IDrawingAreaControl control = getControl(diagram);
		
		if(diagramValidation != null && control != null) {
			validateNodes = diagramValidation.getValidateNodes();
			validateLinks = diagramValidation.getValidateLinks();
			
			if(validateLinks || validateNodes) {
				if(!onlySelectedElements) {
					proceed = dispatchPreDiagramPerformSync(diagram,diagramValidation);
				}
				
				if(proceed) {
					// Start the busy state on the progress control
					// CBusyCtrlProxy busyState(AfxGetInstanceHandle(), IDS_VALIDATE_DIAGRAM);
					
					ETList<IETGraphObject> etGraphObjects = null;
					long numETGraphObjects = 0;
					
					if(onlySelectedElements) {
						etGraphObjects = control.getSelected3();
					}
					else {
						etGraphObjects = control.getAllItems6();
					}
					
					if(etGraphObjects != null) {
						numETGraphObjects = etGraphObjects.getCount();
						
						for(int index = 0; index < numETGraphObjects; index++) {
							IETGraphObject etGraphObject = etGraphObjects.item(index);
							
							if(etGraphObject != null) {
								doValidate(diagram,etGraphObject,diagramValidation,tempResult);
							}
						}
					}
					
					doResponses(diagram,diagramValidation);
					
					if(!onlySelectedElements) {
						dispatchPostDiagramPerformSync(diagram,diagramValidation);
					}
					
					clear();
				}
			}
			
			diagram.refresh(false);
		}
		
		return tempResult;
	}

	public void clear() {
		m_UnconnectedNodes.clear();
		m_UnconnectedEdges.clear();
		m_InvalidDrawEngines.clear();
		m_InvalidLinkEnds.clear();
		m_InvalidDeeps.clear();
	}

	protected boolean dispatchPreRetrieveElementSynchState(IDiagram diagram,IETGraphObject etGraphObject) {
		DispatchHelper helper = new DispatchHelper();
		
		IEventDispatcher dispatcher = helper.getDrawingAreaDispatcher();
		IDrawingAreaEventDispatcher drawingAreaEventDispatcher = null;
		if(dispatcher instanceof IDrawingAreaEventDispatcher)
			drawingAreaEventDispatcher = (IDrawingAreaEventDispatcher)dispatcher;
			
		IPresentationElement presentationElement = TypeConversions.getPresentationElement(etGraphObject);
		
		if(drawingAreaEventDispatcher != null && presentationElement != null) {
			IPresentationElementSyncState context = new PresentationElementSyncState(); 
			int originalSynchState = etGraphObject.getSynchState();
			
			if(context != null) {
				context.setDiagram(diagram);
				context.setPresentationElement(presentationElement);
				context.setOriginalSynchState(originalSynchState);
				context.setNewSynchState(originalSynchState);
				
				IEventPayload payload = drawingAreaEventDispatcher.createPayload("DrawingAreaPreRetrieveElementSynchState");
				return drawingAreaEventDispatcher.fireDrawingAreaPreRetrieveElementSynchState(context,payload);
			}
		}
		return true;
	}

	protected ETPairT<Boolean,Integer> dispatchPostRetrieveElementSynchState(IDiagram diagram,IETGraphObject etGraphObject,int newSynchState) {
		boolean synchStateChanged = false;
		int returnNewSynchState = newSynchState;
		
		DispatchHelper helper = new DispatchHelper();
		IEventDispatcher dispatcher = helper.getDrawingAreaDispatcher();
		IDrawingAreaEventDispatcher drawingAreaEventDispatcher = null;
		if(dispatcher instanceof IDrawingAreaEventDispatcher)
			drawingAreaEventDispatcher = (IDrawingAreaEventDispatcher)dispatcher;
			
		IPresentationElement presentationElement = TypeConversions.getPresentationElement(etGraphObject);
		
		if(drawingAreaEventDispatcher != null && presentationElement != null) {
			IPresentationElementSyncState context = new PresentationElementSyncState();
			int originalSynchState = etGraphObject.getSynchState();
			
			if(context != null) {
				context.setDiagram(diagram);
				context.setPresentationElement(presentationElement);
				context.setOriginalSynchState(originalSynchState);
				context.setNewSynchState(originalSynchState);
				
				IEventPayload payload = drawingAreaEventDispatcher.createPayload("DrawingAreaPostRetrieveElementSynchState");
				drawingAreaEventDispatcher.fireDrawingAreaPostRetrieveElementSynchState(context,payload);
				
				int localNewSynchState = context.getNewSynchState();
				if(localNewSynchState != originalSynchState) {
					returnNewSynchState = localNewSynchState;
					synchStateChanged = true;
				}
			}
		}
		
		return new ETPairT<Boolean,Integer>(new Boolean(synchStateChanged),new Integer(returnNewSynchState));
	}

	/// Fires FireDrawingAreaPrePresentationElementPerformSync    
	protected boolean dispatchPrePresentationElementPerformSync(IDiagram diagram,IPresentationElement presentationElement) {
		boolean proceed = true;
		DispatchHelper helper = new DispatchHelper();
		
		IEventDispatcher dispatcher = helper.getDrawingAreaDispatcher();
		
		IDrawingAreaEventDispatcher drawingAreaEventDispatcher = null;
		if(dispatcher instanceof IDrawingAreaEventDispatcher) {
			drawingAreaEventDispatcher = (IDrawingAreaEventDispatcher)dispatcher;
		}
		
		if(drawingAreaEventDispatcher != null) {
			IPresentationElementPerformSyncContext context = new PresentationElementPerformSyncContext();
			
			if(context != null) {
				context.setDiagram(diagram);
				context.setPresentationElement(presentationElement);
				
				IEventPayload payload = drawingAreaEventDispatcher.createPayload("DrawingAreaPrePresentationElementPerformSync");
				
				proceed = drawingAreaEventDispatcher.fireDrawingAreaPrePresentationElementPerformSync(context,payload);
			}
		}
		return proceed;
	}

	/// Fires FireDrawingAreaPostPresentationElementPerformSync     
	protected void dispatchPostPresentationElementPerformSync(IDiagram diagram,	IPresentationElement presentationElement) {
		DispatchHelper helper = new DispatchHelper();
		
		IEventDispatcher dispatcher = helper.getDrawingAreaDispatcher();
		
		IDrawingAreaEventDispatcher drawingAreaEventDispatcher = null;
		if(dispatcher instanceof IDrawingAreaEventDispatcher) {
			drawingAreaEventDispatcher = (IDrawingAreaEventDispatcher)dispatcher;
		}

		if(drawingAreaEventDispatcher != null) {
			IPresentationElementPerformSyncContext context = new PresentationElementPerformSyncContext();
			
			if(context != null) {
				context.setDiagram(diagram);
				context.setPresentationElement(presentationElement);
				
				IEventPayload payload = drawingAreaEventDispatcher.createPayload("DrawingAreaPostPresentationElementPerformSync");
				
				drawingAreaEventDispatcher.fireDrawingAreaPostPresentationElementPerformSync(context,payload);
			}
		}
				
	}

	/// Fires FireDrawingAreaPreDiagramPerformSync    
	protected boolean dispatchPreDiagramPerformSync(IDiagram diagram,IDiagramValidation diagramValidation) {
		boolean proceed = true;
		DispatchHelper helper = new DispatchHelper();
		
		IEventDispatcher dispatcher = helper.getDrawingAreaDispatcher();
		
		IDrawingAreaEventDispatcher drawingAreaEventDispatcher = null;
		if(dispatcher instanceof IDrawingAreaEventDispatcher) {
			drawingAreaEventDispatcher = (IDrawingAreaEventDispatcher)dispatcher;
		}
		
		if(drawingAreaEventDispatcher != null) {
			IDiagramPerformSyncContext context = new DiagramPerformSyncContext();
			
			if(context != null) {
				context.setDiagram(diagram);
				context.setDiagramValidation(diagramValidation);
				
				IEventPayload payload = drawingAreaEventDispatcher.createPayload("DrawingAreaPreDiagramPerformSync");
				proceed = drawingAreaEventDispatcher.fireDrawingAreaPreDiagramPerformSync(context,payload);
			}
		}
		return proceed;
	}

	/// Fires FireDrawingAreaPostDiagramPerformSync   
	protected void dispatchPostDiagramPerformSync(IDiagram diagram,IDiagramValidation diagramValidation) {
		DispatchHelper helper = new DispatchHelper();
		
		IEventDispatcher dispatcher = helper.getDrawingAreaDispatcher();
		
		IDrawingAreaEventDispatcher drawingAreaEventDispatcher = null;
		if(dispatcher instanceof IDrawingAreaEventDispatcher) {
			drawingAreaEventDispatcher = (IDrawingAreaEventDispatcher)dispatcher;
		}
		
		if(drawingAreaEventDispatcher != null) {
			IDiagramPerformSyncContext context = new DiagramPerformSyncContext();
			
			if(context != null) {
				context.setDiagram(diagram);
				context.setDiagramValidation(diagramValidation);
				
				IEventPayload payload = drawingAreaEventDispatcher.createPayload("DrawingAreaPostDiagramPerformSync");
				
				drawingAreaEventDispatcher.fireDrawingAreaPostDiagramPerformSync(context,payload);
			}
			
		}		
	}

	///
	//////////////////////////////////////////////////////////////////////////////

	/// Returns an IAxDrawingAreaControl
	protected IDrawingAreaControl getControl(IDiagram diagram) {
		IDrawingAreaControl control = null;
		if(diagram != null) {
			if(diagram instanceof IUIDiagram) {
				IUIDiagram uiDiagram = (IUIDiagram)diagram;
				
				control = uiDiagram.getDrawingArea();
			}
		}
		return control;
	}

	/// Validates this specific product element
	protected void doValidate(IDiagram diagram,IETGraphObject etGraphObject,IDiagramValidation diagramValidation,IDiagramValidationResult result) {
		boolean proceed = true;
		
		IPresentationElement presentationElement = TypeConversions.getPresentationElement(etGraphObject);
		
		if(presentationElement != null) {
			TSObject tempObject = etGraphObject.getGraphObject();
			
			int newSynchState = ISynchStateKind.SSK_UNKNOWN_SYNCH_STATE;
			
			IGraphObjectValidation graphObjectValidation = null;
			
			if(proceed) {
				graphObjectValidation = diagramValidation.createGraphObjectValidation();
				
				if(graphObjectValidation != null) {
					proceed = dispatchPrePresentationElementPerformSync(diagram,presentationElement);
					
					if(proceed) {
						etGraphObject.validate(graphObjectValidation);
						
						dispatchPostPresentationElementPerformSync(diagram,presentationElement);
					}
				}
			}
			
			if(graphObjectValidation != null) {
				if( getResult(IDiagramValidateKind.DVK_VALIDATE_CONNECTIONTOELEMENT,graphObjectValidation) == IDiagramValidateResult.DVR_INVALID ||
				    getResult(IDiagramValidateKind.DVK_VALIDATE_BRIDGES,graphObjectValidation) == IDiagramValidateResult.DVR_INVALID ) {
					
					if(TypeConversions.getOwnerNode(etGraphObject) != null) {
						m_UnconnectedNodes.add(tempObject);
					}   	
					else {
						m_UnconnectedEdges.add(tempObject);
					}
					newSynchState = ISynchStateKind.SSK_OUT_OF_SYNCH;
					
					if(getResult(IDiagramValidateKind.DVK_VALIDATE_CONNECTIONTOELEMENT,graphObjectValidation) == IDiagramValidateResult.DVR_INVALID) {
						result.incrementNumInvalidItems(IDiagramValidateKind.DVK_VALIDATE_CONNECTIONTOELEMENT);
					}
					if(getResult(IDiagramValidateKind.DVK_VALIDATE_BRIDGES,graphObjectValidation) == IDiagramValidateResult.DVR_INVALID ) {
						result.incrementNumInvalidItems(IDiagramValidateKind.DVK_VALIDATE_BRIDGES);
					}
				}
				
				if(getResult(IDiagramValidateKind.DVK_VALIDATE_DRAWENGINE,graphObjectValidation) == IDiagramValidateResult.DVR_INVALID ) {
					m_InvalidDrawEngines.add(tempObject);
					newSynchState = ISynchStateKind.SSK_OUT_OF_SYNCH;
					result.incrementNumInvalidItems(IDiagramValidateKind.DVK_VALIDATE_DRAWENGINE);
				}

				if(getResult(IDiagramValidateKind.DVK_VALIDATE_LINKENDS,graphObjectValidation) == IDiagramValidateResult.DVR_INVALID ) {
					m_InvalidLinkEnds.add(tempObject);
					newSynchState = ISynchStateKind.SSK_OUT_OF_SYNCH;
					result.incrementNumInvalidItems(IDiagramValidateKind.DVK_VALIDATE_LINKENDS);
				}

				if(getResult(IDiagramValidateKind.DVK_VALIDATE_RESYNC_DEEP,graphObjectValidation) == IDiagramValidateResult.DVR_INVALID ) {
					m_InvalidDeeps.add(tempObject);
					newSynchState = ISynchStateKind.SSK_OUT_OF_SYNCH;
					result.incrementNumInvalidItems(IDiagramValidateKind.DVK_VALIDATE_RESYNC_DEEP);
				}
				else {
					if(newSynchState == ISynchStateKind.SSK_UNKNOWN_SYNCH_STATE) {
						etGraphObject.setSynchState(ISynchStateKind.SSK_IN_SYNCH_SHALLOW);
					}
				}
				etGraphObject.setSynchState(newSynchState);
			}
		}
		else if(etGraphObject != null) {
			TSObject tempObject = etGraphObject.getGraphObject();
			
			if(TypeConversions.getOwnerNode(etGraphObject) != null) {
				m_UnconnectedNodes.add(tempObject);
			}
			else {
				m_UnconnectedEdges.add(tempObject);
			}
			etGraphObject.setSynchState(ISynchStateKind.SSK_OUT_OF_SYNCH);
		}
				
	}

	/// Do the responses
	protected void doResponses(IDiagram diagram,IDiagramValidation diagramValidation) {
		IDrawingAreaControl control = getControl(diagram);
		
		int numReset = resetDrawEngines(control,diagramValidation);
		
		int numLinksReconnected = reconnectLinks(control,diagramValidation);
		
		int numDeepSynchs = performDeepSynch();
		
		if(control != null) {
			deleteInvalidNodes(control,diagramValidation);
			
			deleteInvalidLinks(control,diagramValidation);
			
			deleteInvalidDrawEngines(control,diagramValidation);
		}
	}

	/// Reconnects links
	protected int reconnectLinks(IDrawingAreaControl control,IDiagramValidation diagramValidation) {
		int numReconnected = 0;
		
		boolean deleteInvalidLinks = diagramValidation.getValidationResponse(IDiagramValidateResponse.DVRSP_DELETE_INVALID_LINKS);
		boolean reconnectInvalidLinks = diagramValidation.getValidationResponse(IDiagramValidateResponse.DVRSP_RECONNECT_INVALID_LINKS);
		
		if(reconnectInvalidLinks && m_InvalidLinkEnds.size() > 0) {
			Iterator<TSObject> iter = m_InvalidLinkEnds.iterator();
			
			while(iter.hasNext()) {
				TSObject currentObject = iter.next();
				
				IPresentationElement presentationElement = TypeConversions.getPresentationElement(currentObject);
				
				IEdgePresentation edgePresentation = null;
				if(presentationElement instanceof IEdgePresentation) {
					edgePresentation = (IEdgePresentation)edgePresentation;
				}
				
				if(edgePresentation != null) {
					boolean successfullyReconnected = false;
					
					if(!deleteInvalidLinks){
						successfullyReconnected = edgePresentation.reconnectLinkToValidNodes();
					}
					
					if(!successfullyReconnected) {
						m_UnconnectedEdges.add(currentObject);
					}
					else {
						numReconnected++;
						
						IETGraphObject etGraphObject = TypeConversions.getETGraphObject(currentObject);
						
						if(etGraphObject != null) {
							etGraphObject.setSynchState(ISynchStateKind.SSK_IN_SYNCH_SHALLOW);
						}
					}
				}
			}
		}
		return numReconnected;				
	}

	/// Resets the draw engines
	protected int resetDrawEngines(IDrawingAreaControl control,IDiagramValidation diagramValidation) {
		int numReset = 0;
		
		boolean resetInvalidDrawEngines = diagramValidation.getValidationResponse(IDiagramValidateResponse.DVRSP_DELETE_INVALID_DRAW_ENGINES);
		boolean deleteObjectsWithInvalidDrawEngines = diagramValidation.getValidationResponse(IDiagramValidateResponse.DVRSP_RESET_INVALID_DRAW_ENGINES);
		
		if(resetInvalidDrawEngines && m_InvalidDrawEngines.size() > 0 && !deleteObjectsWithInvalidDrawEngines) {
			IPresentationTypesMgr presentationTypesMgr = ProductHelper.getPresentationTypesMgr();
			if(presentationTypesMgr != null) {
				Iterator<TSObject> iterator = m_InvalidDrawEngines.iterator();
				while(iterator.hasNext()) {
					TSObject currentObject = iterator.next();
					
					IETGraphObject etGraphObject = TypeConversions.getETGraphObject(currentObject);
					
					if(etGraphObject != null) {
						control.resetDrawEngine2(etGraphObject);
					}
				}
				numReset++;
			}
		}
		return numReset;
	}

	/// Deletes the objects with invalid draw engines
	protected void deleteInvalidDrawEngines(IDrawingAreaControl control,IDiagramValidation diagramValidation) {
		boolean deleteObjectsWithInvalidDrawEngines = diagramValidation.getValidationResponse(IDiagramValidateResponse.DVRSP_DELETE_INVALID_DRAW_ENGINES);
		boolean resetInvalidDrawEngines = diagramValidation.getValidationResponse(IDiagramValidateResponse.DVRSP_RESET_INVALID_DRAW_ENGINES);
		
		if(deleteObjectsWithInvalidDrawEngines && !resetInvalidDrawEngines) {
			Iterator<TSObject> iterator = m_InvalidDrawEngines.iterator();
			
			while(iterator.hasNext()) {
				TSObject currentObject = iterator.next();
				
				if(currentObject instanceof TSGraphObject) {
					TSGraphObject graphObject = (TSGraphObject)currentObject;
					
					control.postDeletePresentationElement(graphObject);
				}
			}
			m_InvalidDrawEngines.clear();
		}
	}

	/// Deletes the invalid links
	protected void deleteInvalidLinks(IDrawingAreaControl control,IDiagramValidation diagramValidation) {
		boolean deleteInvalidLinks = diagramValidation.getValidationResponse(IDiagramValidateResponse.DVRSP_DELETE_INVALID_LINKS);
		
		if(deleteInvalidLinks) {
			Iterator<TSObject> iterator = m_UnconnectedEdges.iterator();
			
			while(iterator.hasNext()) {
				TSObject currentObject = iterator.next();
				
				if(currentObject instanceof TSEdge) {
					TSEdge tsEdge = (TSEdge)currentObject;
					
					control.postDeletePresentationElement(tsEdge);
				}
			}
			m_UnconnectedEdges.clear();
		}
	}

	/// Deletes the invalid nodes
	protected void deleteInvalidNodes(IDrawingAreaControl control,IDiagramValidation diagramValidation) {
		boolean deleteInvalidNodes = diagramValidation.getValidationResponse(IDiagramValidateResponse.DVRSP_DELETE_INVALID_NODES);
		
		if(deleteInvalidNodes) {
			Iterator<TSObject> iter = m_UnconnectedNodes.iterator();
			
			while(iter.hasNext()) {
				TSObject currentObject = iter.next();
				
				if(currentObject instanceof TSNode) {
					TSNode tsNode = (TSNode)currentObject;
					control.postDeletePresentationElement(tsNode);
				}
			}
			m_UnconnectedNodes.clear();
		}
	}

	protected int performDeepSynch() {
		int numSynched = 0;
		
		if(m_InvalidDeeps.size() > 0) {
			Iterator<TSObject> iterator = m_InvalidDeeps.iterator();
			
			while(iterator.hasNext()) {
				TSObject currentObject = iterator.next();
				
				if(currentObject != null) {
					numSynched++;
					
					IETGraphObject etGraphObject = TypeConversions.getETGraphObject(currentObject);
					if(etGraphObject != null) {
						etGraphObject.performDeepSynch();
						etGraphObject.setSynchState(ISynchStateKind.SSK_IN_SYNCH_DEEP);
					}
				}
			}
			m_InvalidDeeps.clear();
		}
		return numSynched;
	}

	/// Send a message out the message service
	protected void sendMessage(int messageType, String message) {
		if(message != null && message.length() > 0) {
//			UMLMessagingHelper messageService = new UMLMessagingHelper(module.getModuleInstance(),IDS_MESSAGINFACILITY);
//			messagService.SendMessage(messageType,message);
		}
	}

	/// Send a message out the message service
	protected void sendMessage(int messageType, int nMessage) {
//		CString message;
//		VERIFY(message.LoadString(nMessage));
//		if (message.GetLength())
//		{
//		   xstring xMessage(message);
//		   SendMessage(messageType,xMessage);
//		}
	}
	
	public int getResult(/*DiagramValidateKind*/ int nKind, IGraphObjectValidation result) {
		return result.getValidationResult(nKind);
	}

	/// The lists that get created of elements that are invalid
	protected ETList < TSObject > m_UnconnectedNodes = new ETArrayList < TSObject > ();
	protected ETList < TSObject > m_UnconnectedEdges = new ETArrayList < TSObject > ();
	protected ETList < TSObject > m_InvalidDrawEngines = new ETArrayList < TSObject > ();
	protected ETList < TSObject > m_InvalidLinkEnds = new ETArrayList < TSObject > ();
	protected ETList < TSObject > m_InvalidDeeps = new ETArrayList < TSObject > ();
}


