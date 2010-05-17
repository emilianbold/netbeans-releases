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

/*
 *
 * Created on Jun 19, 2003
 * @author Trey Spiva
 */
package org.netbeans.modules.uml.ui.support.diagramsupport;

import org.netbeans.modules.uml.core.eventframework.IEventDispatcher;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.openide.loaders.DataObject;

/**
 * 
 * @author Trey Spiva
 */
public interface IDrawingAreaEventDispatcher extends IEventDispatcher
{
    // TODO: Determine what events and how they should look like
    
//   /**
//    * Registers a sink with the drawing area selection events dispatcher.
//   */
//   public int registerDrawingAreaSelectionEvents( IDrawingAreaSelectionEventsSink handler );
//
//   /**
//    * Revokes a drawing area selection sink.
//   */
//   public void revokeDrawingAreaSelectionSink( IDrawingAreaSelectionEventsSink handler );
//
//   /**`
//    * Fires a select event out the dispatch interface.
//   */
//   public void fireSelect( IDiagram pParentDiagram, 
//                           ETList<IPresentationElement> selectedItems, 
//                           ICompartment pCompartment, 
//                           IEventPayload payload );
//
//   /**
//    * Fires a select event out the dispatch interface.
//   */
//   public void fireUnselect( IDiagram pParentDiagram, 
//                             ETList<IPresentationElement> unselectedItems, 
//                             IEventPayload payload );
//
//   /**
//    * Registers a sink with the drawing area synchronization events dispatcher.
//   */
//   public int registerDrawingAreaSynchEvents( IDrawingAreaSynchEventsSink handler );
//
//   /**
//    * Revokes a drawing area synchronization sink.
//   */
//   public void revokeDrawingAreaSynchSink( IDrawingAreaSynchEventsSink handler );
//
//   /**
//    * Fired when we need to get the sync state of a particular presentation element..
//   */
//   public boolean fireDrawingAreaPreRetrieveElementSynchState( IPresentationElementSyncState pPresentationElementSyncState, 
//                                                            IEventPayload payload);
//
//   /**
//    * Fired when we need to get the sync state of a particular presentation element..
//   */
//   public void fireDrawingAreaPostRetrieveElementSynchState( IPresentationElementSyncState pPresentationElementSyncState, 
//                                                             IEventPayload payload );
//
//   /**
//    * Fired when a presentation element is about to by synched..
//   */
//   public boolean fireDrawingAreaPrePresentationElementPerformSync( IPresentationElementPerformSyncContext pPresentationElementSyncContext, 
//                                                                 IEventPayload payload);
//
//   /**
//    * Fired when a presentation element has been synched..
//   */
//   public void fireDrawingAreaPostPresentationElementPerformSync( IPresentationElementPerformSyncContext pPresentationElementSyncContext, 
//                                                                  IEventPayload payload );
//
//   /**
//    * Fired when a diagram is about to by synched..
//   */
//   public boolean fireDrawingAreaPreDiagramPerformSync( IDiagramPerformSyncContext pDiagramSyncContext, 
//                                                        IEventPayload payload );
//
//   /**
//    * Fired when a diagram has been synched..
//   */
//   public void fireDrawingAreaPostDiagramPerformSync( IDiagramPerformSyncContext pDiagramSyncContext, 
//                                                      IEventPayload payload );
//
//   /**
//    * Registers a sink with the drawing area context menu events dispatcher.
//   */
//   public int registerDrawingAreaContextMenuEvents( IDrawingAreaContextMenuEventsSink handler );
//
//   /**
//    * Revokes a drawing area context menu sink.
//   */
//   public void revokeDrawingAreaContextMenuSink( IDrawingAreaContextMenuEventsSink handler );
//
//   /**
//    * Fires an event out the interface saying that the context menu should be prepared.
//   */
//   public void fireDrawingAreaContextMenuPrepare( IDiagram pParentDiagram, 
//                                                  IProductContextMenu contextMenu, 
//                                                  IEventPayload payload );
//
//   /**
//    * Fires an event out the interface saying that the context menu has been prepared.
//   */
//   public void fireDrawingAreaContextMenuPrepared( IDiagram pParentDiagram, 
//                                                   IProductContextMenu contextMenu, 
//                                                   IEventPayload payload );
//
//   /**
//    * Fires an event out the interface saying that someone can handle the display of the context menu
//   */
//   public void fireDrawingAreaContextMenuHandleDisplay( IDiagram pParentDiagram, 
//                                                        IProductContextMenu contextMenu, 
//                                                        IEventPayload payload );
//
//   /**
//    * Fires an event out the interface saying that a context menu button has been selected.
//   */
//   public void fireDrawingAreaContextMenuSelected( IDiagram pParentDiagram, 
//                                                   IProductContextMenu contextMenu, 
//                                                   IProductContextMenuItem selectedItem, 
//                                                   IEventPayload payload );

   /**
    * Registers a sink with the drawing area event dispatcher
   */
   public int registerDrawingAreaEvents( IDrawingAreaEventsSink handler );

   /**
    * Revokes a drawing area sink
   */
   public void revokeDrawingAreaSink( IDrawingAreaEventsSink handler );

   /**
//    * Fired when a drawing area is created.
//   */
//   public boolean fireDrawingAreaPreCreated( IDrawingAreaControl pDiagramControl, 
//                                             IEventPayload payload );

   /**
    * Fired when a drawing area is created.
    */
   public void fireDrawingAreaPostCreated( DataObject dataobject, 
                                           IEventPayload payload );

   /**
//    * Fires an event out the interface saying that the drawing area has been opened.
//   */
//   public void fireDrawingAreaOpened( IDiagram pParentDiagram, 
//                                      IEventPayload payload );
//
//   /**
//    * Fires an event out the interface saying that the drawing area has been closed.
//   */
//   public void fireDrawingAreaClosed( IDiagram pParentDiagram, 
//                                      boolean bDiagramIsDirty, 
//                                      IEventPayload payload );
//
//   /**
//    * Fired when a drawing area is saved.
//   */
//   public boolean fireDrawingAreaPreSave( IProxyDiagram pParentDiagram, 
//                                          IEventPayload payload );
//
//   /**
//    * Fired when a drawing area is saved.
//   */
//   public void fireDrawingAreaPostSave( IProxyDiagram pParentDiagram, 
//                                        IEventPayload payload );
//
//   /**
//    * Fires an event out the interface saying that the drawing area has received a keydown.
//   */
//   public void fireOnDrawingAreaKeyDown( IDiagram pParentDiagram, 
//                                        int nKeyCode, 
//                                        boolean bControlIsDown, 
//                                        boolean bShiftIsDown, 
//                                        boolean bAltIsDown, 
//                                        IEventPayload payload );
//
   /**
//    * Fired when a drawing area property has changed.
   */
   public boolean fireDrawingAreaPrePropertyChange( IProxyDiagram pProxyDiagram, 
                                                     int nPropertyKindChanged, /* DrawingAreaPropertyKind */  
                                                     IEventPayload payload );

   /**
    * Fired when a drawing area property has changed.
   */
   public void fireDrawingAreaPostPropertyChange( IProxyDiagram pProxyDiagram, 
                                                  int nPropertyKindChanged, /* DrawingAreaPropertyKind */  
                                                  IEventPayload payload );

//   /**
//    * Fired when a drawing area namespace has changed.
//   */
//   public void fireDrawingAreaTooltipPreDisplay( IDiagram pParentDiagram, 
//                                                 IPresentationElement pPE, 
//                                                 IToolTipData pTooltip, 
//                                                 IEventPayload payload );
//
//   /**
//    * Fires an event out the interface saying that the drawing area has been activated.
//   */
//   public void fireDrawingAreaActivated( IDiagram pParentDiagram, IEventPayload payload );
//
//   /**
//    * Fires an event out the interface saying that an OLE drop has happened on the drawing area.
//   */
//   public void fireDrawingAreaPreDrop( IDiagram pParentDiagram, 
//                                       IDrawingAreaDropContext pContext, 
//                                       IEventPayload payload );
//
//   /**
//    * Fires an event out the interface after an OLE drop has happened on the drawing area.
//   */
//   public void fireDrawingAreaPostDrop( IDiagram pParentDiagram, 
//                                        IDrawingAreaDropContext pContext, 
//                                        IEventPayload payload );
//
//   /**
//    * Fired right before a diagram file is removed from disk.
//   */
//   public boolean fireDrawingAreaPreFileRemoved( String sFilename, 
//                                                 IEventPayload payload );
//
   /**
    * Fired after a diagram file is removed from disk.
   */
   public void fireDrawingAreaFileRemoved( String sFilename, 
                                           IEventPayload payload );

//   /**
//    * Registers a dispatcher to receive add node events.
//   */
//   public int registerDrawingAreaAddNodeEvents( IDrawingAreaAddNodeEventsSink handler );
//
//   /**
//    * Revokes a drawing area add node sink
//   */
//   public void revokeDrawingAreaAddNodeSink( IDrawingAreaAddNodeEventsSink handler );
//
//   /**
//    * Tells listeners that a node is about to be created.
//   */
//   public void fireDrawingAreaCreateNode( IDiagram pParentDiagram, 
//                                          ICreateNodeContext pContext, 
//                                          IEventPayload payload );
//
//   /**
//    * Tells listeners that a node was created and is now being dragged.
//   */
//   public void fireDrawingAreaDraggingNode( IDiagram pParentDiagram, 
//                                            IDraggingNodeContext pContext, 
//                                            IEventPayload payload );
//
//   /**
//    * Registers a dispatcher to receive add edge events.
//   */
//   public int registerDrawingAreaAddEdgeEvents( IDrawingAreaAddEdgeEventsSink handler );
//
//   /**
//    * Revokes a drawing area add edge sink
//   */
//   public void revokeDrawingAreaAddEdgeSink( IDrawingAreaAddEdgeEventsSink handler );
//
//   /**
//    * Tells listeners that an edge is about to be started.
//   */
//   public void fireDrawingAreaStartingEdge( IDiagram pParentDiagram, 
//                                            IEdgeCreateContext pContext, 
//                                            IEventPayload payload );
//
//   /**
//    * Tells listeners that an edge is about to be started.
//   */
//   public void fireDrawingAreaEdgeShouldCreateBend( IDiagram pParentDiagram, 
//                                                    IEdgeCreateBendContext pContext, 
//                                                    IEventPayload payload );
//
//   /**
//    * Tells listeners that an edge is being moved around during the creation process..
//   */
//   public void fireDrawingAreaEdgeMouseMove( IDiagram pParentDiagram, 
//                                             IEdgeMouseMoveContext pContext, 
//                                             IEventPayload payload );
//
//   /**
//    * Tells listeners that an edge is about to be finished.
//   */
//   public void fireDrawingAreaFinishEdge( IDiagram pParentDiagram, 
//                                          IEdgeFinishContext pContext, 
//                                          IEventPayload payload );
//
//   /**
//    * Registers a dispatcher to receive reconnect edge events.
//   */
//   public int registerDrawingAreaReconnectEdgeEvents( IDrawingAreaReconnectEdgeEventsSink handler );
//
//   /**
//    * Revokes a drawing area reconnect edge sink
//   */
//   public void revokeDrawingAreaReconnectEdgeSink( IDrawingAreaReconnectEdgeEventsSink handler );
//
//   /**
//    * Tells listeners that an edge reconnect is about to start.
//   */
//   public void fireDrawingAreaReconnectEdgeStart( IDiagram pParentDiagram, 
//                                                  IReconnectEdgeContext pContext, 
//                                                  IEventPayload payload );
//
//   /**
//    * Tells listeners that an edge reconnect mousemove happened.
//   */
//   public void fireDrawingAreaReconnectEdgeMouseMove( IDiagram pParentDiagram, 
//                                                      IReconnectEdgeContext pContext, 
//                                                      IEventPayload payload );
//
//   /**
//    * Tells listeners that an edge reconnect is about to end.
//   */
//   public void fireDrawingAreaReconnectEdgeFinish( IDiagram pParentDiagram, 
//                                                   IReconnectEdgeContext pContext, 
//                                                   IEventPayload payload );
//
//   /**
//    * Registers a dispatcher to receive compartment events.
//   */
//   public int registerDrawingAreaCompartmentEvents( ICompartmentEventsSink handler );
//
//   /**
//    * Revokes a drawing area compartment sink
//   */
//   public void revokeDrawingAreaCompartmentSink( ICompartmentEventsSink handler );
//
//   /**
//    * Tells listeners that a compartment has been selected.
//   */
//   public void fireCompartmentSelected( ICompartment pCompartment, 
//                                        boolean bSelected, 
//                                        IEventPayload payload );
//
//   /**
//    * Tells listeners that a compartment has been Collapsed.
//   */
//   public void fireCompartmentCollapsed( ICompartment pCompartment, 
//                                         boolean bCollapsed, 
//                                         IEventPayload payload );
//
//   /**
//    * Registers a dispatcher to receive element change translator events.
//   */
//   public int registerChangeNotificationTranslatorEvents( IChangeNotificationTranslatorSink handler );
//
//   /**
//    * Revokes an element change translator sink
//   */
//   public void revokeChangeNotificationTranslatorSink( IChangeNotificationTranslatorSink handler );
//
//   /**
//    * Tells listeners that an element should change - which elements should get notified?
//   */
//   public void fireGetNotificationTargets( IDiagram pDiagram, 
//                                           INotificationTargets pTargets, 
//                                           IEventPayload payload );

}
