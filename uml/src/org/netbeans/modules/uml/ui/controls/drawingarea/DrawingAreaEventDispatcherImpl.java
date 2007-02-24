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
 * @author Trey Spiva
 */
package org.netbeans.modules.uml.ui.controls.drawingarea;

import org.netbeans.modules.uml.core.eventframework.EventDispatcher;
import org.netbeans.modules.uml.core.eventframework.EventFunctor;
import org.netbeans.modules.uml.core.eventframework.IEventContext;
import org.netbeans.modules.uml.core.eventframework.IEventFrameworkEventsSink;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IToolTipData;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import org.netbeans.modules.uml.ui.swing.drawingarea.IChangeNotificationTranslatorSink;
import org.netbeans.modules.uml.ui.swing.drawingarea.ICompartmentEventsSink;
import org.netbeans.modules.uml.ui.swing.drawingarea.ICreateNodeContext;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramPerformSyncContext;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDraggingNodeContext;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaAddEdgeEventsSink;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaAddNodeEventsSink;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaContextMenuEventsSink;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaDropContext;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaReconnectEdgeEventsSink;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaSelectionEventsSink;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaSynchEventsSink;
import org.netbeans.modules.uml.ui.swing.drawingarea.IEdgeCreateBendContext;
import org.netbeans.modules.uml.ui.swing.drawingarea.IEdgeCreateContext;
import org.netbeans.modules.uml.ui.swing.drawingarea.IEdgeFinishContext;
import org.netbeans.modules.uml.ui.swing.drawingarea.IEdgeMouseMoveContext;
import org.netbeans.modules.uml.ui.swing.drawingarea.IPresentationElementPerformSyncContext;
import org.netbeans.modules.uml.ui.swing.drawingarea.IPresentationElementSyncState;
import org.netbeans.modules.uml.ui.swing.drawingarea.IReconnectEdgeContext;

import org.netbeans.modules.uml.core.eventframework.EventManager;
import java.util.ArrayList;
import java.util.HashMap;

import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * 
 * @author Trey Spiva
 */
public class DrawingAreaEventDispatcherImpl extends EventDispatcher implements IDrawingAreaEventDispatcher {
	private EventManager < IDrawingAreaSelectionEventsSink > m_DrawingAreaSelectionEventManager = null;
	private EventManager < IDrawingAreaEventsSink > m_DrawingAreaEventManager = null;
	private EventManager < IDrawingAreaSynchEventsSink > m_DrawingAreaSynchEventManager = null;

	private EventManager < IDrawingAreaContextMenuEventsSink > m_DrawingAreaContextMenuEventManager = null;
	private EventManager < IDrawingAreaAddNodeEventsSink > m_DrawingAreaAddNodeEventManager = null;
	private EventManager < IDrawingAreaAddEdgeEventsSink > m_DrawingAreaAddEdgeEventManager = null;
	private EventManager < IDrawingAreaReconnectEdgeEventsSink > m_DrawingAreaReconnectEdgeEventManager = null;
	private EventManager < ICompartmentEventsSink > m_DrawingAreaCompartmentEventManager = null;
	private EventManager < IChangeNotificationTranslatorSink > m_ChangeNotificationTranslatorEventManager = null;
	protected static final String DRAWINGAREA_INTERFACE_PACKAGE = "org.netbeans.modules.uml.ui.swing.drawingarea";

   protected EventFunctor createFunctor(final String fullPathToInterface, final String methodName) {
	   return new EventFunctor(fullPathToInterface, methodName);
   }

	public DrawingAreaEventDispatcherImpl() {
		m_DrawingAreaSelectionEventManager = new EventManager < IDrawingAreaSelectionEventsSink > ();
		m_DrawingAreaEventManager = new EventManager < IDrawingAreaEventsSink > ();
		m_DrawingAreaSynchEventManager = new EventManager < IDrawingAreaSynchEventsSink >();

		m_DrawingAreaContextMenuEventManager = new EventManager < IDrawingAreaContextMenuEventsSink > ();
		m_DrawingAreaAddNodeEventManager = new EventManager < IDrawingAreaAddNodeEventsSink > ();
		m_DrawingAreaAddEdgeEventManager = new EventManager < IDrawingAreaAddEdgeEventsSink > ();
		m_DrawingAreaReconnectEdgeEventManager = new EventManager < IDrawingAreaReconnectEdgeEventsSink > ();
		m_DrawingAreaCompartmentEventManager = new EventManager < ICompartmentEventsSink > ();
		m_ChangeNotificationTranslatorEventManager = new EventManager < IChangeNotificationTranslatorSink > ();
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.IDrawingAreaEventDispatcher#registerDrawingAreaSelectionEvents(org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaSelectionEventsSink)
	 */
	public int registerDrawingAreaSelectionEvents(IDrawingAreaSelectionEventsSink handler) {
		m_DrawingAreaSelectionEventManager.addListener(handler, null);

		return 0;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.IDrawingAreaEventDispatcher#revokeDrawingAreaSelectionSink(org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaSelectionEventsSink)
	 */
	public void revokeDrawingAreaSelectionSink(IDrawingAreaSelectionEventsSink handler) {
		m_DrawingAreaSelectionEventManager.removeListener(handler);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.IDrawingAreaEventDispatcher#fireSelect(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, com.embarcadero.describe.foundationcollections.IPresentationElements, org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment, org.netbeans.modules.uml.core.eventframework.IEventPayload)
	 */
	public void fireSelect(IDiagram pParentDiagram, ETList < IPresentationElement > selectedItems, ICompartment pCompartment, IEventPayload payload) {
		ArrayList < Object > collection = new ArrayList < Object > ();
		collection.add(pParentDiagram);
		collection.add(selectedItems);
		collection.add(pCompartment);

		if (validateEvent("DrawingAreaSelect", collection)) {
			IResultCell cell = prepareResultCell(payload);
			EventFunctor drawSelectFunc = createFunctor(DRAWINGAREA_INTERFACE_PACKAGE + ".IDrawingAreaSelectionEventsSink", "onSelect");
			Object[] parms = new Object[4];
			parms[0] = pParentDiagram;
			parms[1] = selectedItems;
			parms[2] = pCompartment;
			parms[3] = cell;
			drawSelectFunc.setParameters(parms);
			m_DrawingAreaSelectionEventManager.notifyListeners(drawSelectFunc);
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.IDrawingAreaEventDispatcher#fireUnselect(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, com.embarcadero.describe.foundationcollections.IPresentationElements, org.netbeans.modules.uml.core.eventframework.IEventPayload)
	 */
	public void fireUnselect(IDiagram pParentDiagram, ETList < IPresentationElement > unselectedItems, IEventPayload payload) {
		ArrayList < Object > collection = new ArrayList < Object > ();
		collection.add(pParentDiagram);
		collection.add(unselectedItems);

		if (validateEvent("DrawingAreaUnselect", collection)) {
			IResultCell cell = prepareResultCell(payload);
			EventFunctor functor = createFunctor(DRAWINGAREA_INTERFACE_PACKAGE + ".IDrawingAreaSelectionEventsSink", "onUnselect");
			Object[] parms = new Object[3];
			parms[0] = pParentDiagram;
			parms[1] = unselectedItems;
			parms[2] = cell;
			functor.setParameters(parms);
			m_DrawingAreaSelectionEventManager.notifyListeners(functor);
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.IDrawingAreaEventDispatcher#registerDrawingAreaSynchEvents(org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaSynchEventsSink)
	 */
	public int registerDrawingAreaSynchEvents(IDrawingAreaSynchEventsSink handler) {
		m_DrawingAreaSynchEventManager.addListener(handler,null);
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.IDrawingAreaEventDispatcher#revokeDrawingAreaSynchSink(org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaSynchEventsSink)
	 */
	public void revokeDrawingAreaSynchSink(IDrawingAreaSynchEventsSink handler) {
		m_DrawingAreaSynchEventManager.removeListener(handler);

	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.IDrawingAreaEventDispatcher#fireDrawingAreaPreRetrieveElementSynchState(org.netbeans.modules.uml.ui.swing.drawingarea.IPresentationElementSyncState, org.netbeans.modules.uml.core.eventframework.IEventPayload, boolean)
	 */
	public boolean fireDrawingAreaPreRetrieveElementSynchState(IPresentationElementSyncState pPresentationElementSyncState, IEventPayload payload) {
		boolean proceed = true;
		ArrayList < Object > collection = new ArrayList < Object > ();
		collection.add(pPresentationElementSyncState);

		if (validateEvent("DrawingAreaPreRetrieveElementSynchState", collection)) {
			IResultCell cell = prepareResultCell(payload);
			EventFunctor functor = createFunctor(DRAWINGAREA_INTERFACE_PACKAGE + ".IDrawingAreaEventsSink", "onDrawingAreaPreRetrieveElementSynchState");
			Object[] parms = new Object[2];
			parms[0] = pPresentationElementSyncState;
			parms[1] = cell;
			functor.setParameters(parms);
			m_DrawingAreaSynchEventManager.notifyListenersWithQualifiedProceed(functor);
			proceed = cell.canContinue();
		}
		return proceed;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.IDrawingAreaEventDispatcher#fireDrawingAreaPostRetrieveElementSynchState(org.netbeans.modules.uml.ui.swing.drawingarea.IPresentationElementSyncState, org.netbeans.modules.uml.core.eventframework.IEventPayload)
	 */
	public void fireDrawingAreaPostRetrieveElementSynchState(IPresentationElementSyncState pPresentationElementSyncState, IEventPayload payload) {
		ArrayList < Object > collection = new ArrayList < Object > ();
		collection.add(pPresentationElementSyncState);

		if (validateEvent("DrawingAreaPostRetrieveElementSynchState", collection)) {
			IResultCell cell = prepareResultCell(payload);
			EventFunctor functor = createFunctor(DRAWINGAREA_INTERFACE_PACKAGE + ".IDrawingAreaEventsSink", "onDrawingAreaPostRetrieveElementSynchState");
			Object[] parms = new Object[2];
			parms[0] = pPresentationElementSyncState;
			parms[1] = cell;
			functor.setParameters(parms);
			m_DrawingAreaSynchEventManager.notifyListenersWithQualifiedProceed(functor);
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.IDrawingAreaEventDispatcher#fireDrawingAreaPrePresentationElementPerformSync(org.netbeans.modules.uml.ui.swing.drawingarea.IPresentationElementPerformSyncContext, org.netbeans.modules.uml.core.eventframework.IEventPayload, boolean)
	 */
	public boolean fireDrawingAreaPrePresentationElementPerformSync(IPresentationElementPerformSyncContext pPresentationElementSyncContext, IEventPayload payload) {
		boolean proceed = true;
		ArrayList < Object > collection = new ArrayList < Object > ();
		collection.add(pPresentationElementSyncContext);

		if (validateEvent("DrawingAreaPrePresentationElementPerformSync", collection)) {
			IResultCell cell = prepareResultCell(payload);
			EventFunctor functor = createFunctor(DRAWINGAREA_INTERFACE_PACKAGE + ".IDrawingAreaEventsSink", "onDrawingAreaPrePresentationElementPerformSync");
			Object[] parms = new Object[2];
			parms[0] = pPresentationElementSyncContext;
			parms[1] = cell;
			functor.setParameters(parms);
			m_DrawingAreaSynchEventManager.notifyListenersWithQualifiedProceed(functor);
			proceed = cell.canContinue();
		}
		return proceed;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.IDrawingAreaEventDispatcher#fireDrawingAreaPostPresentationElementPerformSync(org.netbeans.modules.uml.ui.swing.drawingarea.IPresentationElementPerformSyncContext, org.netbeans.modules.uml.core.eventframework.IEventPayload)
	 */
	public void fireDrawingAreaPostPresentationElementPerformSync(IPresentationElementPerformSyncContext pPresentationElementSyncContext, IEventPayload payload) {
		ArrayList < Object > collection = new ArrayList < Object > ();
		collection.add(pPresentationElementSyncContext);

		if (validateEvent("DrawingAreaPostPresentationElementPerformSync", collection)) {
			IResultCell cell = prepareResultCell(payload);
			EventFunctor functor = createFunctor(DRAWINGAREA_INTERFACE_PACKAGE + ".IDrawingAreaEventsSink", "onDrawingAreaPostPresentationElementPerformSync");
			Object[] parms = new Object[2];
			parms[0] = pPresentationElementSyncContext;
			parms[1] = cell;
			functor.setParameters(parms);
			m_DrawingAreaSynchEventManager.notifyListenersWithQualifiedProceed(functor);
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.IDrawingAreaEventDispatcher#fireDrawingAreaPreDiagramPerformSync(org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramPerformSyncContext, org.netbeans.modules.uml.core.eventframework.IEventPayload)
	 */
	public boolean fireDrawingAreaPreDiagramPerformSync(IDiagramPerformSyncContext pDiagramSyncContext, IEventPayload payload) {
		boolean proceed = true;
		ArrayList < Object > collection = new ArrayList < Object > ();
		collection.add(pDiagramSyncContext);

		if (validateEvent("DrawingAreaPreDiagramPerformSync", collection)) {
			IResultCell cell = prepareResultCell(payload);
			EventFunctor functor = createFunctor(DRAWINGAREA_INTERFACE_PACKAGE + ".IDrawingAreaEventsSink", "onDrawingAreaPreDiagramPerformSync");
			Object[] parms = new Object[2];
			parms[0] = pDiagramSyncContext;
			parms[1] = cell;
			functor.setParameters(parms);
			m_DrawingAreaSynchEventManager.notifyListenersWithQualifiedProceed(functor);
			proceed = cell.canContinue();
		}
		return proceed;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.IDrawingAreaEventDispatcher#fireDrawingAreaPostDiagramPerformSync(org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramPerformSyncContext, org.netbeans.modules.uml.core.eventframework.IEventPayload)
	 */
	public void fireDrawingAreaPostDiagramPerformSync(IDiagramPerformSyncContext pDiagramSyncContext, IEventPayload payload) {
		ArrayList < Object > collection = new ArrayList < Object > ();
		collection.add(pDiagramSyncContext);

		if (validateEvent("DrawingAreaPostDiagramPerformSync", collection)) {
			IResultCell cell = prepareResultCell(payload);
			EventFunctor functor = createFunctor(DRAWINGAREA_INTERFACE_PACKAGE + ".IDrawingAreaEventsSink", "onDrawingAreaPostDiagramPerformSync");
			Object[] parms = new Object[2];
			parms[0] = pDiagramSyncContext;
			parms[1] = cell;
			functor.setParameters(parms);
			m_DrawingAreaSynchEventManager.notifyListenersWithQualifiedProceed(functor);
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.IDrawingAreaEventDispatcher#registerDrawingAreaContextMenuEvents(org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaContextMenuEventsSink)
	 */
	public int registerDrawingAreaContextMenuEvents(IDrawingAreaContextMenuEventsSink handler) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.IDrawingAreaEventDispatcher#revokeDrawingAreaContextMenuSink(org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaContextMenuEventsSink)
	 */
	public void revokeDrawingAreaContextMenuSink(IDrawingAreaContextMenuEventsSink handler) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.IDrawingAreaEventDispatcher#fireDrawingAreaContextMenuPrepare(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu, org.netbeans.modules.uml.core.eventframework.IEventPayload)
	 */
	public void fireDrawingAreaContextMenuPrepare(IDiagram pParentDiagram, IProductContextMenu contextMenu, IEventPayload payload) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.IDrawingAreaEventDispatcher#fireDrawingAreaContextMenuPrepared(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu, org.netbeans.modules.uml.core.eventframework.IEventPayload)
	 */
	public void fireDrawingAreaContextMenuPrepared(IDiagram pParentDiagram, IProductContextMenu contextMenu, IEventPayload payload) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.IDrawingAreaEventDispatcher#fireDrawingAreaContextMenuHandleDisplay(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu, org.netbeans.modules.uml.core.eventframework.IEventPayload)
	 */
	public void fireDrawingAreaContextMenuHandleDisplay(IDiagram pParentDiagram, IProductContextMenu contextMenu, IEventPayload payload) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.IDrawingAreaEventDispatcher#fireDrawingAreaContextMenuSelected(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu, org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem, org.netbeans.modules.uml.core.eventframework.IEventPayload)
	 */
	public void fireDrawingAreaContextMenuSelected(IDiagram pParentDiagram, IProductContextMenu contextMenu, IProductContextMenuItem selectedItem, IEventPayload payload) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.IDrawingAreaEventDispatcher#registerDrawingAreaEvents(org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink)
	 */
	public int registerDrawingAreaEvents(IDrawingAreaEventsSink handler) {
		m_DrawingAreaEventManager.addListener(handler, null);
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.IDrawingAreaEventDispatcher#revokeDrawingAreaSink(org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink)
	 */
	public void revokeDrawingAreaSink(IDrawingAreaEventsSink handler) {
		m_DrawingAreaEventManager.removeListener(handler);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.IDrawingAreaEventDispatcher#fireDrawingAreaPreCreated(org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl, org.netbeans.modules.uml.core.eventframework.IEventPayload)
	 */
	public boolean fireDrawingAreaPreCreated(IDrawingAreaControl pDiagramControl, IEventPayload payload) {
		boolean proceed = true;
		ArrayList < Object > collection = new ArrayList < Object > ();
		collection.add(pDiagramControl);

		if (validateEvent("FireDrawingAreaPreCreated", collection)) {
			IResultCell cell = prepareResultCell(payload);
			EventFunctor functor = createFunctor(DRAWINGAREA_INTERFACE_PACKAGE + ".IDrawingAreaEventsSink", "onDrawingAreaPreCreated");
			Object[] parms = new Object[2];
			parms[0] = pDiagramControl;
			parms[1] = cell;
			functor.setParameters(parms);
			m_DrawingAreaEventManager.notifyListenersWithQualifiedProceed(functor);
			proceed = cell.canContinue();
		}
		return proceed;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.IDrawingAreaEventDispatcher#fireDrawingAreaPostCreated(org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl, org.netbeans.modules.uml.core.eventframework.IEventPayload)
	 */
	public void fireDrawingAreaPostCreated(IDrawingAreaControl pDiagramControl, IEventPayload payload) {
		ArrayList < Object > collection = new ArrayList < Object > ();
		collection.add(pDiagramControl);

		if (validateEvent("FireDrawingAreaPostCreated", collection)) {
			IResultCell cell = prepareResultCell(payload);
			EventFunctor functor = createFunctor(DRAWINGAREA_INTERFACE_PACKAGE + ".IDrawingAreaEventsSink", "onDrawingAreaPostCreated");
			Object[] parms = new Object[2];
			parms[0] = pDiagramControl;
			parms[1] = cell;
			functor.setParameters(parms);
			m_DrawingAreaEventManager.notifyListenersWithQualifiedProceed(functor);
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.IDrawingAreaEventDispatcher#fireDrawingAreaOpened(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.core.eventframework.IEventPayload)
	 */
	public void fireDrawingAreaOpened(IDiagram pParentDiagram, IEventPayload payload) {
		ArrayList < Object > collection = new ArrayList < Object > ();
		collection.add(pParentDiagram);

		if (validateEvent("DrawingAreaOpened", collection)) {
			IResultCell cell = prepareResultCell(payload);
			EventFunctor functor = createFunctor(DRAWINGAREA_INTERFACE_PACKAGE + ".IDrawingAreaEventsSink", "onDrawingAreaOpened");
			Object[] parms = new Object[2];
			parms[0] = pParentDiagram;
			parms[1] = cell;
			functor.setParameters(parms);
			m_DrawingAreaEventManager.notifyListenersWithQualifiedProceed(functor);
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.IDrawingAreaEventDispatcher#fireDrawingAreaClosed(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, boolean, org.netbeans.modules.uml.core.eventframework.IEventPayload)
	 */
	public void fireDrawingAreaClosed(IDiagram pParentDiagram, boolean bDiagramIsDirty, IEventPayload payload) {
		ArrayList < Object > collection = new ArrayList < Object > ();
		collection.add(pParentDiagram);

		if (validateEvent("DrawingAreaClosed", collection)) {
			IResultCell cell = prepareResultCell(payload);
			EventFunctor functor = createFunctor(DRAWINGAREA_INTERFACE_PACKAGE + ".IDrawingAreaEventsSink", "onDrawingAreaClosed");
			Object[] parms = new Object[3];
			parms[0] = pParentDiagram;
			parms[1] = new Boolean(bDiagramIsDirty);
			parms[2] = cell;
			functor.setParameters(parms);
			m_DrawingAreaEventManager.notifyListenersWithQualifiedProceed(functor);
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.IDrawingAreaEventDispatcher#fireDrawingAreaPreSave(org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram, org.netbeans.modules.uml.core.eventframework.IEventPayload)
	 */
	public boolean fireDrawingAreaPreSave(IProxyDiagram pParentDiagram, IEventPayload payload) {
		boolean proceed = true;
		ArrayList < Object > collection = new ArrayList < Object > ();
		collection.add(pParentDiagram);

		if (validateEvent("FireDrawingAreaPreSave", collection)) {
			IResultCell cell = prepareResultCell(payload);
			EventFunctor functor = createFunctor(DRAWINGAREA_INTERFACE_PACKAGE + ".IDrawingAreaEventsSink", "onDrawingAreaPreSave");
			Object[] parms = new Object[2];
			parms[0] = pParentDiagram;
			parms[1] = cell;
			functor.setParameters(parms);
			m_DrawingAreaEventManager.notifyListenersWithQualifiedProceed(functor);
			proceed = cell.canContinue();
		}
		return proceed;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.IDrawingAreaEventDispatcher#fireDrawingAreaPostSave(org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram, org.netbeans.modules.uml.core.eventframework.IEventPayload)
	 */
	public void fireDrawingAreaPostSave(IProxyDiagram pParentDiagram, IEventPayload payload) {
		ArrayList < Object > collection = new ArrayList < Object > ();
		collection.add(pParentDiagram);

		if (validateEvent("FireDrawingAreaPostSave", collection)) {
			IResultCell cell = prepareResultCell(payload);
			EventFunctor functor = createFunctor(DRAWINGAREA_INTERFACE_PACKAGE + ".IDrawingAreaEventsSink", "onDrawingAreaPostSave");
			Object[] parms = new Object[2];
			parms[0] = pParentDiagram;
			parms[1] = cell;
			functor.setParameters(parms);
			m_DrawingAreaEventManager.notifyListenersWithQualifiedProceed(functor);
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.IDrawingAreaEventDispatcher#fireOnDrawingAreaKeyDown(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, int, boolean, boolean, boolean, org.netbeans.modules.uml.core.eventframework.IEventPayload)
	 */
	public void fireOnDrawingAreaKeyDown(IDiagram pParentDiagram, int nKeyCode, boolean bControlIsDown, boolean bShiftIsDown, boolean bAltIsDown, IEventPayload payload) {
		ArrayList < Object > collection = new ArrayList < Object > ();
		collection.add(pParentDiagram);

		if (validateEvent("DrawingAreaKeyDown", collection)) {
			IResultCell cell = prepareResultCell(payload);
			EventFunctor functor = createFunctor(DRAWINGAREA_INTERFACE_PACKAGE + ".IDrawingAreaEventsSink", "onDrawingAreaKeyDown");
			Object[] parms = new Object[6];
			parms[0] = pParentDiagram;
			parms[1] = new Integer(nKeyCode);
			parms[2] = new Boolean(bControlIsDown);
			parms[3] = new Boolean(bShiftIsDown);
			parms[4] = new Boolean(bAltIsDown);
			parms[5] = cell;
			functor.setParameters(parms);
			m_DrawingAreaEventManager.notifyListenersWithQualifiedProceed(functor);
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.IDrawingAreaEventDispatcher#fireDrawingAreaPrePropertyChange(org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram, int, org.netbeans.modules.uml.core.eventframework.IEventPayload)
	 */
	public boolean fireDrawingAreaPrePropertyChange(IProxyDiagram pProxyDiagram, int nPropertyKindChanged, IEventPayload payload) {
		boolean proceed = true;
		ArrayList < Object > collection = new ArrayList < Object > ();
		Integer parm2 = new Integer(nPropertyKindChanged);
		collection.add(pProxyDiagram);
		collection.add(parm2);

		if (validateEvent("DrawingAreaPrePropertyChanged", collection)) {
			IResultCell cell = prepareResultCell(payload);
			EventFunctor functor = createFunctor(DRAWINGAREA_INTERFACE_PACKAGE + ".IDrawingAreaEventsSink", "onDrawingAreaPrePropertyChange");
			Object[] parms = new Object[3];
			parms[0] = pProxyDiagram;
			parms[1] = parm2;
			parms[2] = cell;
			functor.setParameters(parms);
			m_DrawingAreaEventManager.notifyListenersWithQualifiedProceed(functor);
			proceed = cell.canContinue();
		}
		return proceed;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.IDrawingAreaEventDispatcher#fireDrawingAreaPostPropertyChange(org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram, int, org.netbeans.modules.uml.core.eventframework.IEventPayload)
	 */
	public void fireDrawingAreaPostPropertyChange(IProxyDiagram pProxyDiagram, int nPropertyKindChanged, IEventPayload payload) {
		ArrayList < Object > collection = new ArrayList < Object > ();
		Integer parm2 = new Integer(nPropertyKindChanged);
		collection.add(pProxyDiagram);
		collection.add(parm2);

		if (validateEvent("DrawingAreaPostPropertyChanged", collection)) {
			IResultCell cell = prepareResultCell(payload);
			EventFunctor functor = createFunctor(DRAWINGAREA_INTERFACE_PACKAGE + ".IDrawingAreaEventsSink", "onDrawingAreaPostPropertyChange");
			Object[] parms = new Object[3];
			parms[0] = pProxyDiagram;
			parms[1] = parm2;
			parms[2] = cell;
			functor.setParameters(parms);
			m_DrawingAreaEventManager.notifyListenersWithQualifiedProceed(functor);
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.IDrawingAreaEventDispatcher#fireDrawingAreaTooltipPreDisplay(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, com.embarcadero.describe.foundation.IPresentationElement, org.netbeans.modules.uml.ui.support.viewfactorysupport.IToolTipData, org.netbeans.modules.uml.core.eventframework.IEventPayload)
	 */
	public void fireDrawingAreaTooltipPreDisplay(IDiagram pParentDiagram, IPresentationElement pPE, IToolTipData pTooltip, IEventPayload payload) {
		ArrayList < Object > collection = new ArrayList < Object > ();
		collection.add(pParentDiagram);
		collection.add(pPE);
		collection.add(pTooltip);

		if (validateEvent("DrawingAreaTooltipPreDisplay", collection)) {
			IResultCell cell = prepareResultCell(payload);
			EventFunctor functor = createFunctor(DRAWINGAREA_INTERFACE_PACKAGE + ".IDrawingAreaEventsSink", "onDrawingAreaTooltipPreDisplay");
			Object[] parms = new Object[4];
			parms[0] = pParentDiagram;
			parms[1] = pPE;
			parms[2] = pTooltip;
			parms[3] = cell;
			functor.setParameters(parms);
			m_DrawingAreaEventManager.notifyListenersWithQualifiedProceed(functor);
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.IDrawingAreaEventDispatcher#fireDrawingAreaActivated(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.core.eventframework.IEventPayload)
	 */
	public void fireDrawingAreaActivated(IDiagram pParentDiagram, IEventPayload payload) {
		ArrayList < Object > collection = new ArrayList < Object > ();
		collection.add(pParentDiagram);

		if (validateEvent("DrawingAreaActivated", collection)) {
			IResultCell cell = prepareResultCell(payload);
			EventFunctor functor = createFunctor(DRAWINGAREA_INTERFACE_PACKAGE + ".IDrawingAreaEventsSink", "onDrawingAreaActivated");
			Object[] parms = new Object[2];
			parms[0] = pParentDiagram;
			parms[1] = cell;
			functor.setParameters(parms);
			m_DrawingAreaEventManager.notifyListenersWithQualifiedProceed(functor);
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.IDrawingAreaEventDispatcher#fireDrawingAreaPreDrop(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaDropContext, org.netbeans.modules.uml.core.eventframework.IEventPayload)
	 */
	public void fireDrawingAreaPreDrop(IDiagram pParentDiagram, IDrawingAreaDropContext pContext, IEventPayload payload) {
		ArrayList < Object > collection = new ArrayList < Object > ();
		collection.add(pParentDiagram);
		collection.add(pContext);

		if (validateEvent("DrawingAreaPreDrop", collection)) {
			IResultCell cell = prepareResultCell(payload);
			EventFunctor functor = createFunctor(DRAWINGAREA_INTERFACE_PACKAGE + ".IDrawingAreaEventsSink", "onDrawingAreaPreDrop");
			Object[] parms = new Object[3];
			parms[0] = pParentDiagram;
			parms[1] = pContext;
			parms[2] = cell;
			functor.setParameters(parms);
			m_DrawingAreaEventManager.notifyListenersWithQualifiedProceed(functor);
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.IDrawingAreaEventDispatcher#fireDrawingAreaPostDrop(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaDropContext, org.netbeans.modules.uml.core.eventframework.IEventPayload)
	 */
	public void fireDrawingAreaPostDrop(IDiagram pParentDiagram, IDrawingAreaDropContext pContext, IEventPayload payload) {
		ArrayList < Object > collection = new ArrayList < Object > ();
		collection.add(pParentDiagram);
		collection.add(pContext);

		if (validateEvent("DrawingAreaPostDrop", collection)) {
			IResultCell cell = prepareResultCell(payload);
			EventFunctor functor = createFunctor(DRAWINGAREA_INTERFACE_PACKAGE + ".IDrawingAreaEventsSink", "onDrawingAreaPostDrop");
			Object[] parms = new Object[3];
			parms[0] = pParentDiagram;
			parms[1] = pContext;
			parms[2] = cell;
			functor.setParameters(parms);
			m_DrawingAreaEventManager.notifyListenersWithQualifiedProceed(functor);
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.IDrawingAreaEventDispatcher#fireDrawingAreaPreFileRemoved(java.lang.String, org.netbeans.modules.uml.core.eventframework.IEventPayload)
	 */
	public boolean fireDrawingAreaPreFileRemoved(String sFilename, IEventPayload payload) {
		boolean proceed = true;
		ArrayList < Object > collection = new ArrayList < Object > ();
		collection.add(sFilename);

		if (validateEvent("DrawingAreaPreFileRemoved", collection)) {
			IResultCell cell = prepareResultCell(payload);
			EventFunctor functor = createFunctor(DRAWINGAREA_INTERFACE_PACKAGE + ".IDrawingAreaEventsSink", "onDrawingAreaPreFileRemoved");
			Object[] parms = new Object[2];
			parms[0] = sFilename;
			parms[1] = cell;
			functor.setParameters(parms);
			m_DrawingAreaEventManager.notifyListenersWithQualifiedProceed(functor);
			proceed = cell.canContinue();
		}
		return proceed;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.IDrawingAreaEventDispatcher#fireDrawingAreaFileRemoved(java.lang.String, org.netbeans.modules.uml.core.eventframework.IEventPayload)
	 */
	public void fireDrawingAreaFileRemoved(String sFilename, IEventPayload payload) {
		ArrayList < Object > collection = new ArrayList < Object > ();
		collection.add(sFilename);

		if (validateEvent("DrawingAreaFileRemoved", collection)) {
			IResultCell cell = prepareResultCell(payload);
			EventFunctor functor = createFunctor(DRAWINGAREA_INTERFACE_PACKAGE + ".IDrawingAreaEventsSink", "onDrawingAreaFileRemoved");
			Object[] parms = new Object[2];
			parms[0] = sFilename;
			parms[1] = cell;
			functor.setParameters(parms);
			m_DrawingAreaEventManager.notifyListenersWithQualifiedProceed(functor);
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.IDrawingAreaEventDispatcher#registerDrawingAreaAddNodeEvents(org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaAddNodeEventsSink)
	 */
	public int registerDrawingAreaAddNodeEvents(IDrawingAreaAddNodeEventsSink handler) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.IDrawingAreaEventDispatcher#revokeDrawingAreaAddNodeSink(org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaAddNodeEventsSink)
	 */
	public void revokeDrawingAreaAddNodeSink(IDrawingAreaAddNodeEventsSink handler) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.IDrawingAreaEventDispatcher#fireDrawingAreaCreateNode(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.ui.swing.drawingarea.ICreateNodeContext, org.netbeans.modules.uml.core.eventframework.IEventPayload)
	 */
	public void fireDrawingAreaCreateNode(IDiagram pParentDiagram, ICreateNodeContext pContext, IEventPayload payload) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.IDrawingAreaEventDispatcher#fireDrawingAreaDraggingNode(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.ui.swing.drawingarea.IDraggingNodeContext, org.netbeans.modules.uml.core.eventframework.IEventPayload)
	 */
	public void fireDrawingAreaDraggingNode(IDiagram pParentDiagram, IDraggingNodeContext pContext, IEventPayload payload) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.IDrawingAreaEventDispatcher#registerDrawingAreaAddEdgeEvents(org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaAddEdgeEventsSink)
	 */
	public int registerDrawingAreaAddEdgeEvents(IDrawingAreaAddEdgeEventsSink handler) {
      m_DrawingAreaAddEdgeEventManager.addListener(handler, null);
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.IDrawingAreaEventDispatcher#revokeDrawingAreaAddEdgeSink(org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaAddEdgeEventsSink)
	 */
	public void revokeDrawingAreaAddEdgeSink(IDrawingAreaAddEdgeEventsSink handler) {
      m_DrawingAreaAddEdgeEventManager.removeListener(handler);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.IDrawingAreaEventDispatcher#fireDrawingAreaStartingEdge(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.ui.swing.drawingarea.IEdgeCreateContext, org.netbeans.modules.uml.core.eventframework.IEventPayload)
	 */
	public void fireDrawingAreaStartingEdge(IDiagram pParentDiagram, IEdgeCreateContext pContext, IEventPayload payload) {
		ArrayList < Object > collection = new ArrayList < Object > ();
		collection.add(pContext);

		if (validateEvent("FireDrawingAreaStartingEdge", collection)) {
			IResultCell cell = prepareResultCell(payload);
			EventFunctor functor = createFunctor(DRAWINGAREA_INTERFACE_PACKAGE + ".IDrawingAreaAddEdgeEventsSink", "onDrawingAreaStartingEdge");

			Object[] parms = new Object[3];
			parms[0] = pParentDiagram;
			parms[1] = pContext;
			parms[2] = cell;
			functor.setParameters(parms);
			m_DrawingAreaAddEdgeEventManager.notifyListenersWithQualifiedProceed(functor);
		}

	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.IDrawingAreaEventDispatcher#fireDrawingAreaEdgeShouldCreateBend(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.ui.swing.drawingarea.IEdgeCreateBendContext, org.netbeans.modules.uml.core.eventframework.IEventPayload)
	 */
	public void fireDrawingAreaEdgeShouldCreateBend(IDiagram pParentDiagram, IEdgeCreateBendContext pContext, IEventPayload payload) {
		ArrayList < Object > collection = new ArrayList < Object > ();
		collection.add(pContext);
		if (validateEvent("FireDrawingAreaEdgeShouldCreateBend", collection)) {
			IResultCell cell = prepareResultCell(payload);
			EventFunctor functor = createFunctor(DRAWINGAREA_INTERFACE_PACKAGE + ".IDrawingAreaAddEdgeEventsSink", "onDrawingAreaEdgeShouldCreateBend");

			Object[] parms = new Object[3];
			parms[0] = pParentDiagram;
			parms[1] = pContext;
			parms[2] = cell;
			functor.setParameters(parms);
			m_DrawingAreaAddEdgeEventManager.notifyListenersWithQualifiedProceed(functor);
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.IDrawingAreaEventDispatcher#fireDrawingAreaEdgeMouseMove(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.ui.swing.drawingarea.IEdgeMouseMoveContext, org.netbeans.modules.uml.core.eventframework.IEventPayload)
	 */
	public void fireDrawingAreaEdgeMouseMove(IDiagram pParentDiagram, IEdgeMouseMoveContext pContext, IEventPayload payload) {
		ArrayList < Object > collection = new ArrayList < Object > ();
		collection.add(pContext);

		if (validateEvent("FireDrawingAreaEdgeMouseMove", collection)) {
			IResultCell cell = prepareResultCell(payload);
			EventFunctor functor = createFunctor(DRAWINGAREA_INTERFACE_PACKAGE + ".IDrawingAreaAddEdgeEventsSink", "onDrawingAreaEdgeMouseMove");

			Object[] parms = new Object[3];
			parms[0] = pParentDiagram;
			parms[1] = pContext;
			parms[2] = cell;
			functor.setParameters(parms);
			m_DrawingAreaAddEdgeEventManager.notifyListenersWithQualifiedProceed(functor);
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.IDrawingAreaEventDispatcher#fireDrawingAreaFinishEdge(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.ui.swing.drawingarea.IEdgeFinishContext, org.netbeans.modules.uml.core.eventframework.IEventPayload)
	 */
	public void fireDrawingAreaFinishEdge(IDiagram pParentDiagram, IEdgeFinishContext pContext, IEventPayload payload) {
		ArrayList < Object > collection = new ArrayList < Object > ();
		collection.add(pContext);

		if (validateEvent("FireDrawingAreaFinishEdge", collection)) {
			IResultCell cell = prepareResultCell(payload);
			EventFunctor functor = createFunctor(DRAWINGAREA_INTERFACE_PACKAGE + ".IDrawingAreaAddEdgeEventsSink",
				 "onDrawingAreaFinishEdge");

			Object[] parms = new Object[3];
			parms[0] = pParentDiagram;
			parms[1] = pContext;
			parms[2] = cell;
			functor.setParameters(parms);
			m_DrawingAreaAddEdgeEventManager.notifyListenersWithQualifiedProceed(functor);
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.IDrawingAreaEventDispatcher#registerDrawingAreaReconnectEdgeEvents(org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaReconnectEdgeEventsSink)
	 */
	public int registerDrawingAreaReconnectEdgeEvents(IDrawingAreaReconnectEdgeEventsSink handler) {
		m_DrawingAreaReconnectEdgeEventManager.addListener(handler, null);
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.IDrawingAreaEventDispatcher#revokeDrawingAreaReconnectEdgeSink(org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaReconnectEdgeEventsSink)
	 */
	public void revokeDrawingAreaReconnectEdgeSink(IDrawingAreaReconnectEdgeEventsSink handler) {
		m_DrawingAreaReconnectEdgeEventManager.removeListener(handler);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.IDrawingAreaEventDispatcher#fireDrawingAreaReconnectEdgeStart(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.ui.swing.drawingarea.IReconnectEdgeContext, org.netbeans.modules.uml.core.eventframework.IEventPayload)
	 */
	public void fireDrawingAreaReconnectEdgeStart(IDiagram pParentDiagram, IReconnectEdgeContext pContext, IEventPayload payload) {
		ArrayList < Object > collection = new ArrayList < Object > ();
		collection.add(pContext);

		if (validateEvent("FireDrawingAreaReconnectEdgeStart", collection)) {
			IResultCell cell = prepareResultCell(payload);
			EventFunctor functor = createFunctor(DRAWINGAREA_INTERFACE_PACKAGE + ".IDrawingAreaReconnectEdgeEventsSink", "onDrawingAreaReconnectEdgeStart");

			Object[] parms = new Object[3];
			parms[0] = pParentDiagram;
			parms[1] = pContext;
			parms[2] = cell;
			functor.setParameters(parms);
			m_DrawingAreaReconnectEdgeEventManager.notifyListenersWithQualifiedProceed(functor);
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.IDrawingAreaEventDispatcher#fireDrawingAreaReconnectEdgeMouseMove(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.ui.swing.drawingarea.IReconnectEdgeContext, org.netbeans.modules.uml.core.eventframework.IEventPayload)
	 */
	public void fireDrawingAreaReconnectEdgeMouseMove(IDiagram pParentDiagram, IReconnectEdgeContext pContext, IEventPayload payload) {
		ArrayList < Object > collection = new ArrayList < Object > ();
		collection.add(pContext);

		if (validateEvent("FireDrawingAreaReconnectEdgeMouseMove", collection)) {
			IResultCell cell = prepareResultCell(payload);
			EventFunctor functor = createFunctor(DRAWINGAREA_INTERFACE_PACKAGE + ".IDrawingAreaReconnectEdgeEventsSink", "onDrawingAreaReconnectEdgeMouseMove");

			Object[] parms = new Object[3];
			parms[0] = pParentDiagram;
			parms[1] = pContext;
			parms[2] = cell;
			functor.setParameters(parms);
			m_DrawingAreaReconnectEdgeEventManager.notifyListenersWithQualifiedProceed(functor);
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.IDrawingAreaEventDispatcher#fireDrawingAreaReconnectEdgeFinish(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.ui.swing.drawingarea.IReconnectEdgeContext, org.netbeans.modules.uml.core.eventframework.IEventPayload)
	 */
	public void fireDrawingAreaReconnectEdgeFinish(IDiagram pParentDiagram, IReconnectEdgeContext pContext, IEventPayload payload) {
	    ArrayList < Object > collection = new ArrayList < Object > ();
		collection.add(pContext);

		if (validateEvent("FireDrawingAreaReconnectEdgeFinish", collection)) {
			IResultCell cell = prepareResultCell(payload);
			EventFunctor functor = createFunctor(DRAWINGAREA_INTERFACE_PACKAGE + ".IDrawingAreaReconnectEdgeEventsSink", "onDrawingAreaReconnectEdgeFinish");

			Object[] parms = new Object[3];
			parms[0] = pParentDiagram;
			parms[1] = pContext;
			parms[2] = cell;
			functor.setParameters(parms);
			m_DrawingAreaReconnectEdgeEventManager.notifyListenersWithQualifiedProceed(functor);
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.IDrawingAreaEventDispatcher#registerDrawingAreaCompartmentEvents(org.netbeans.modules.uml.ui.swing.drawingarea.ICompartmentEventsSink)
	 */
	public int registerDrawingAreaCompartmentEvents(ICompartmentEventsSink handler) {
		m_DrawingAreaCompartmentEventManager.addListener(handler, null);
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.IDrawingAreaEventDispatcher#revokeDrawingAreaCompartmentSink(org.netbeans.modules.uml.ui.swing.drawingarea.ICompartmentEventsSink)
	 */
	public void revokeDrawingAreaCompartmentSink(ICompartmentEventsSink handler) {
		m_DrawingAreaCompartmentEventManager.removeListener(handler);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.IDrawingAreaEventDispatcher#fireCompartmentSelected(org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment, boolean, org.netbeans.modules.uml.core.eventframework.IEventPayload)
	 */
	public void fireCompartmentSelected(ICompartment pCompartment, boolean bSelected, IEventPayload payload) {
		ArrayList < Object > collection = new ArrayList < Object > ();
		Boolean parm2 = new Boolean(bSelected);
		collection.add(pCompartment);
		collection.add(parm2);

		if (validateEvent("CompartmentSelected", collection)) {
			IResultCell cell = prepareResultCell(payload);
			EventFunctor functor = createFunctor(DRAWINGAREA_INTERFACE_PACKAGE + ".ICompartmentEventsSink", "onCompartmentSelected");
			Object[] parms = new Object[3];
			parms[0] = pCompartment;
			parms[1] = parm2;
			parms[2] = cell;
			functor.setParameters(parms);
			m_DrawingAreaCompartmentEventManager.notifyListenersWithQualifiedProceed(functor);
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.IDrawingAreaEventDispatcher#fireCompartmentCollapsed(org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment, boolean, org.netbeans.modules.uml.core.eventframework.IEventPayload)
	 */
	public void fireCompartmentCollapsed(ICompartment pCompartment, boolean bCollapsed, IEventPayload payload) {
		ArrayList < Object > collection = new ArrayList < Object > ();
		Boolean parm2 = new Boolean(bCollapsed);
		collection.add(pCompartment);
		collection.add(parm2);

		if (validateEvent("CompartmentCollapsed", collection)) {
			IResultCell cell = prepareResultCell(payload);
			EventFunctor functor = createFunctor(DRAWINGAREA_INTERFACE_PACKAGE + ".ICompartmentEventsSink", "onCompartmentCollapsed");
			Object[] parms = new Object[3];
			parms[0] = pCompartment;
			parms[1] = parm2;
			parms[2] = cell;
			functor.setParameters(parms);
			m_DrawingAreaCompartmentEventManager.notifyListenersWithQualifiedProceed(functor);
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.IDrawingAreaEventDispatcher#registerChangeNotificationTranslatorEvents(org.netbeans.modules.uml.ui.swing.drawingarea.IChangeNotificationTranslatorSink)
	 */
	public int registerChangeNotificationTranslatorEvents(IChangeNotificationTranslatorSink handler) {
		m_ChangeNotificationTranslatorEventManager.addListener(handler,null);
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.IDrawingAreaEventDispatcher#revokeChangeNotificationTranslatorSink(org.netbeans.modules.uml.ui.swing.drawingarea.IChangeNotificationTranslatorSink)
	 */
	public void revokeChangeNotificationTranslatorSink(IChangeNotificationTranslatorSink handler) {
		m_ChangeNotificationTranslatorEventManager.removeListener(handler);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.IDrawingAreaEventDispatcher#fireGetNotificationTargets(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets, org.netbeans.modules.uml.core.eventframework.IEventPayload)
	 */
	public void fireGetNotificationTargets(IDiagram pDiagram, INotificationTargets pTargets, IEventPayload payload) {
		ArrayList < Object > collection = new ArrayList < Object > ();
		
		collection.add(pDiagram);
		collection.add(pTargets);
		
		if (validateEvent("GetNotificationTargets", collection)) {
			IResultCell cell = prepareResultCell(payload);
			EventFunctor functor = createFunctor(DRAWINGAREA_INTERFACE_PACKAGE + ".IChangeNotificationTranslatorSink",
				"onGetNotificationTargets");
			Object[] parms = new Object[3];
			parms[0] = pDiagram;
			parms[1] = pTargets;
			parms[2] = cell;
			functor.setParameters(parms);
			m_ChangeNotificationTranslatorEventManager.notifyListenersWithQualifiedProceed(functor);
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.eventframework.IEventDispatcher#getNumRegisteredSinks()
	 */
	public int getNumRegisteredSinks() {
		int count =
			m_DrawingAreaSelectionEventManager.getNumListeners()
				+ m_DrawingAreaContextMenuEventManager.getNumListeners()
				+ m_DrawingAreaEventManager.getNumListeners()
				+ m_DrawingAreaAddNodeEventManager.getNumListeners()
				+ m_DrawingAreaAddEdgeEventManager.getNumListeners()
				+ m_DrawingAreaReconnectEdgeEventManager.getNumListeners()
				+ m_DrawingAreaCompartmentEventManager.getNumListeners();
		return count;
	}

}
