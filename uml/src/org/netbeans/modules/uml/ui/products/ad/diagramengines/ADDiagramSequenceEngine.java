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

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ResourceBundle;

import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.eventframework.IEventDispatcher;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IDirectedRelationship;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IReference;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IUnknownClassifierEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.MetaLayerRelationFactory;
import org.netbeans.modules.uml.core.metamodel.core.foundation.TypedFactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.primitivetypes.IMessageKind;
import org.netbeans.modules.uml.core.metamodel.diagrams.DiagramEnums;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramKind;
import org.netbeans.modules.uml.core.metamodel.diagrams.ILayoutKind;
import org.netbeans.modules.uml.core.metamodel.dynamics.ICombinedFragment;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteraction;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionFragment;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionOccurrence;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionOperand;
import org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline;
import org.netbeans.modules.uml.core.metamodel.dynamics.IMessage;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.metamodel.structure.IComment;
import org.netbeans.modules.uml.core.support.umlsupport.ETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.ETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.ElementLocator;
import org.netbeans.modules.uml.core.support.umlutils.IElementLocator;
import org.netbeans.modules.uml.ui.controls.drawingarea.DiagramAreaEnumerations;
import org.netbeans.modules.uml.ui.controls.drawingarea.GetHelper;
import org.netbeans.modules.uml.ui.controls.drawingarea.ITopographyChangeAction;
import org.netbeans.modules.uml.ui.controls.drawingarea.TopographyChangeAction;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl;
import org.netbeans.modules.uml.ui.products.ad.application.action.ContextMenuActionClass;
import org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.IConnectMessageKind;
import org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.IADLifelineCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.IADNameCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.IConnectorsCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.LifelineConnectorLocation;
import org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.lifelinepieces.ConnectorPiece;
import org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.lifelinepieces.LifelinePiece;
import org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.lifelinepieces.LifelinePiecesKind;
import org.netbeans.modules.uml.ui.products.ad.diagramengines.sequencediagram.ICombinedFragmentDrawEngine;
import org.netbeans.modules.uml.ui.products.ad.diagramengines.sequencediagram.ILifelineDrawEngine;
import org.netbeans.modules.uml.ui.products.ad.diagramengines.sequencediagram.IMessageEdgeDrawEngine;
import org.netbeans.modules.uml.ui.products.ad.diagramengines.sequencediagram.LifelineDrawEngine;
import org.netbeans.modules.uml.ui.support.DispatchHelper;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.SimpleQuestionDialogKind;
import org.netbeans.modules.uml.ui.support.SimpleQuestionDialogResultKind;
import org.netbeans.modules.uml.ui.support.SwingPreferenceQuestionDialog;
import org.netbeans.modules.uml.ui.support.applicationmanager.IMessageEdgePresentation;
import org.netbeans.modules.uml.ui.support.applicationmanager.INodePresentation;
import org.netbeans.modules.uml.ui.support.applicationmanager.IPresentationTypesMgr;
import org.netbeans.modules.uml.ui.support.applicationmanager.MoveToFlags;
import org.netbeans.modules.uml.ui.support.applicationmanager.PresentationTypeDetails;
import org.netbeans.modules.uml.ui.support.applicationmanager.TSGraphObjectKind;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchive;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement;
import org.netbeans.modules.uml.ui.support.commondialogs.IPreferenceQuestionDialog;
import org.netbeans.modules.uml.ui.support.commondialogs.MessageIconKindEnum;
import org.netbeans.modules.uml.ui.support.helpers.GUIBlocker;
import org.netbeans.modules.uml.ui.support.helpers.IGUIBlocker;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ETPointEx;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETEdge;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.PointConversions;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.swing.drawingarea.ADGraphWindow;
import org.netbeans.modules.uml.ui.swing.drawingarea.DrawingAreaAddEdgeEventsSinkAdapter;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import org.netbeans.modules.uml.ui.swing.drawingarea.IEdgeCreateBendContext;
import org.netbeans.modules.uml.ui.swing.drawingarea.IEdgeCreateContext;
import org.netbeans.modules.uml.ui.swing.drawingarea.IEdgeFinishContext;
import org.netbeans.modules.uml.ui.swing.drawingarea.IEdgeMouseMoveContext;
import org.netbeans.modules.uml.ui.swing.drawingarea.IReconnectEdgeContext;
import org.netbeans.modules.uml.ui.swing.trackbar.JTrackBar;
import com.tomsawyer.drawing.TSConnector;
import com.tomsawyer.drawing.TSDNode;
import com.tomsawyer.editor.TSEConnector;
import com.tomsawyer.editor.TSEEdge;
import com.tomsawyer.editor.TSEGraph;
import com.tomsawyer.editor.TSENode;
import com.tomsawyer.graph.TSEdge;
import com.tomsawyer.graph.TSNode;
//import com.tomsawyer.jnilayout.TSHandleLocation;
//import com.tomsawyer.util.TSConstPoint;
import com.tomsawyer.drawing.geometry.TSConstPoint;
//import com.tomsawyer.util.TSConstRect;
import com.tomsawyer.drawing.geometry.TSConstRect;
import com.tomsawyer.util.TSObject;

/**
 *
 * @author Trey Spiva
 */
public class ADDiagramSequenceEngine extends ADCoreEngine implements IADSequenceDiagEngine, IUnknownClassifierEventsSink
{
    public final static String SQD_SHOW_ALL_RETURN_MESSAGES = "ShowAllReturnMessages";
    public final static String SQD_SHOW_MESSAGE_NUMBERS = "ShowMessageNumbers";
    
    private JTrackBar m_TrackBar = null;
    private boolean m_ShowAllReturnMessages = true;
    private boolean m_ShowMessageNumbers = false;
    
    // Listener helpers
    private EdgeEventListener m_EdgeListener = new EdgeEventListener();
    
    private static final String BUNDLE_NAME = "org.netbeans.modules.uml.ui.products.ad.diagramengines.sequencediagram.Bundle"; //$NON-NLS-1$
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);
    
    /// Piece to be reconnected to another lifeline
    LifelinePiece m_reconnectPiece = null;
    
    
    public ADDiagramSequenceEngine()
    {
        super();
        
        //m_TrackBar.setBorder(new LineBorder(Color.black));
        m_TrackBar = new JTrackBar(this);
        m_TrackBar.setBorder(new BevelBorder(BevelBorder.LOWERED));
        
    }
    
    public void attach(IDrawingAreaControl pParentControl)
    {
        super.attach(pParentControl);
        
        DispatchHelper helper = getDispatchHelper();
        helper.registerDrawingAreaAddEdgeEvents(m_EdgeListener);
        helper.registerUnknownClassifierEvents( this );
        
        if( pParentControl != null )
        {
            ADGraphWindow graphWindow = pParentControl.getGraphWindow();
            if( graphWindow != null )
            {
                // We discovered that the C++ can turn on/off drawing during
                // a move & resize via a call in the derived NodeDrawEngine Draw() call.
                // By calling NodeDrawEngineImpl::DrawMoving() the draw engine could
                // determine that it is being moved and then not draw.
                // The SQD drawing engines did not use this call, so they are always drawn.
                
                // Fix J1619:  We want the presentation elements to draw during
                //             moving and resizing.
                graphWindow.setDrawFullUIOnDragging( true );
            }
        }
        
        m_ShowMessageNumbers = isDefaultShowMessageNumbers();
    }
    
    /**
     * Called after a new diagram is initialized.  The sequence diagram creates a new
     * IInteraction and then places the diagram under that IElement.
     */
    public void initializeNewDiagram()
    {
        if (getDrawingArea() != null)
        {
            getSequenceDiagramInteraction();
            getDrawingArea().setIsDirty(true);
        }
        
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine#initializeTrackBar()
    */
    public JTrackBar initializeTrackBar()
    {
        return m_TrackBar;
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine#readFromArchive(org.netbeans.modules.uml.ui.support.archivesupport.IProductArchive, org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement)
    */
    public void readFromArchive(IProductArchive pProductArchive, IProductArchiveElement pParentElement)
    {
        if ((pProductArchive != null) && (pParentElement != null))
        {
            boolean showReturn = pParentElement.getAttributeBool(SQD_SHOW_ALL_RETURN_MESSAGES);
            boolean showNumbers = pParentElement.getAttributeBool(SQD_SHOW_MESSAGE_NUMBERS);
            
            setShowAllReturnMessages(showReturn);
            setShowMessageNumbers(showNumbers);
        }
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine#writeToArchive(org.netbeans.modules.uml.ui.support.archivesupport.IProductArchive, org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement)
    */
    public void writeToArchive(IProductArchive pProductArchive, IProductArchiveElement pParentElement)
    {
        if ((pProductArchive != null) && (pParentElement != null))
        {
            pParentElement.addAttributeBool(SQD_SHOW_ALL_RETURN_MESSAGES, isShowAllReturnMessages());
            pParentElement.addAttributeBool(SQD_SHOW_MESSAGE_NUMBERS, isShowMessageNumbers());
        }
    }
    
    public boolean onHandleButton(ActionEvent event, String id)
    {
        if( (null == event) || (null == id) ) throw new IllegalArgumentException();
        
        if (id.equals("MBK_SQD_SHOW_INTERACTION_BOUNDARY"))
        {
            if( getDrawingArea() != null )
            {
                boolean bShowing = isInteractionBoundaryShowing();
                showInteractionBoundary( !bShowing );
                
                getDrawingArea().setIsDirty(true);
            }
        }
        else if(id.equals("MBK_SQD_SHOW_MESSAGE_NUMBERS"))
        {
            m_ShowMessageNumbers = !m_ShowMessageNumbers;
            IDrawingAreaControl pDrawingAreaControl = getDrawingArea();
            if(pDrawingAreaControl != null)
            {
                refreshMessageNumbers();
                pDrawingAreaControl.setIsDirty(true);
            }
        }
        else if (id.equals("MBK_SQD_SHOW_ALL_RETURN_MESSAGES"))
        {
            m_ShowAllReturnMessages = !m_ShowAllReturnMessages;
            IDrawingAreaControl pDrawingAreaControl = getDrawingArea();
            if(pDrawingAreaControl != null)
            {
                setShowAllReturnMessages( m_ShowAllReturnMessages );
                pDrawingAreaControl.setIsDirty(true);
            }
        }
        else if (id.equals("MBK_CHANGE_SIMILAR_FONT"))
        {
            boolean retVal = super.onHandleButton(event, id);
            
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    getDrawingArea().immediatelySetLayoutStyle(ILayoutKind.LK_SEQUENCEDIAGRAM_LAYOUT, true);
                }
            });
            
            return retVal;
        }
        else
        {
            return super.onHandleButton( event, id );
        }
        return false;
    }
    
    public boolean setSensitivityAndCheck(String id, ContextMenuActionClass pClass)
    {
        boolean bFlag = super.setSensitivityAndCheck(id, pClass);
        if (id.equals("MBK_SQD_SHOW_MESSAGE_NUMBERS"))
        {
            pClass.setChecked(m_ShowMessageNumbers);
        }
        else if (id.equals("MBK_SQD_SHOW_INTERACTION_BOUNDARY"))
        {
            pClass.setChecked(isInteractionBoundaryShowing());
        }
        else if (id.equals("MBK_SQD_SHOW_ALL_RETURN_MESSAGES"))
        {
            pClass.setChecked(m_ShowAllReturnMessages);
        }
        return bFlag;
    }
    
    //**************************************************
    // Implementation of IADSequenceDiagEngine
    //**************************************************
    
    /**
     * Determines if the return messages should be displayed.
     *
     * @return <code>true</code> if the return message should be displayed.
     */
    public boolean isShowAllReturnMessages()
    {
        return m_ShowAllReturnMessages;
    }
    
    /**
     * Set if the return messages should be displayed.
     *
     * @param b <code>true</code> if the return message should be displayed.
     */
    public void setShowAllReturnMessages( boolean bShowAllReturnMessages )
    {
        m_ShowAllReturnMessages = bShowAllReturnMessages;
        
        IDrawingAreaControl ctrl = getDrawingArea();
        if (ctrl != null)
        {
            ETList < IPresentationElement > cpPEs = ctrl.getAllByType("Message");
            if (cpPEs != null)
            {
                for (Iterator < IPresentationElement > iter = cpPEs.iterator(); iter.hasNext();)
                {
                    IPresentationElement cpPresentationElement = iter.next();
                    if (cpPresentationElement != null)
                    {
                        // Make sure the presentation element represents a return message
                        IElement cpElement = TypeConversions.getElement(cpPresentationElement);
                        if (cpElement instanceof IMessage)
                        {
                            IMessage cpMessage = (IMessage)cpElement;
                            int kind = cpMessage.getKind();
                            if (kind == IMessageKind.MK_RESULT)
                            {
                                IDrawEngine engine = TypeConversions.getDrawEngine(cpPresentationElement);
                                if (engine instanceof IMessageEdgeDrawEngine)
                                {
                                    IMessageEdgeDrawEngine cpEngine = (IMessageEdgeDrawEngine)engine;
                                    if (cpEngine != null)
                                    {
                                        cpEngine.setShow( bShowAllReturnMessages );
                                        cpEngine.invalidate();
                                    }
                                }
                            }
                        }
                    }
                }
            }
            ctrl.refresh( false );
        }
    }
    
    /**
     * Determines if the messages numbers should be displayed.
     *
     * @return <code>true</code> if the message numbers should be displayed.
     */
    public boolean isShowMessageNumbers()
    {
        return m_ShowMessageNumbers;
    }
    
    /**
     * Determines if the messages numbers should be displayed.
     *
     * @param b <code>true</code> if the message numbers should be displayed.
     */
    public void setShowMessageNumbers(boolean b)
    {
        m_ShowMessageNumbers = b;
    }
    
    /**
     * Sequence Diagram specific call from the CDrawingAreaButtonHandler
     *
     * Indicates wether the interaction boundary is already being shown, or not
     *
     * @return <code>true</code> if the interaction boundary is showing.
     * @see org.netbeans.modules.uml.ui.products.ad.diagramengines.IADSequenceDiagEngine#isInteractionBoundaryShowing()
     */
    public boolean isInteractionBoundaryShowing()
    {
        return getInteractionBoundry() != null;
    }
    
    /**
     * Gets the owning interaction presentation element, or the interaction
     * boundary if it exists
     *
     * @return Returns the interaction boundary on this diagram, if there is one
     */
    public IPresentationElement getInteractionBoundry()
    {
        IPresentationElement retVal = null;
        
        IElement ownerElement = getOwner();
        
        if ((ownerElement != null) && (ownerElement.getElementType().equals("Interaction") == true))
        {
            IDrawingAreaControl ctrl = getDrawingArea();
            if (ctrl != null)
            {
                ETList < IPresentationElement > presElements = ctrl.getAllItems2(ownerElement);
                if ((presElements != null) && (presElements.size() > 0))
                {
                    assert presElements.size() == 1 : "there should only be one presentation element";
                    
                    retVal = presElements.get(0);
                }
            }
        }
        
        return retVal;
    }
    
    /**
     * Sequence Diagram specific call from the CDrawingAreaButtonHandler
     *
     * Process the Show Interaction Boundary request
     *
     * @param bShowOuterGate Set to <code>true</code> to show the outer gate as well.
     * @see org.netbeans.modules.uml.ui.products.ad.diagramengines.IADSequenceDiagEngine#setShowInteractionBoundary(boolean)
     */
    public void showInteractionBoundary(boolean bShowOuterGate)
    {
        if (getDrawingArea() != null)
        {
            ETPairT < IPresentationElement, IElement > result = getInteractionBoundary();
            IPresentationElement interactionBoundaryPE = result.getParamOne();
            IElement element = result.getParamTwo();
            
            if (bShowOuterGate)
            {
                if (interactionBoundaryPE != null)
                {
                    positionInteractionBoundary();
                }
                else
                {
                    // Add the node that is the interaction
                    final IETRect rectBounding = calculateInteractionBoundarySize(true);
                    
                    final Point ptCenter = rectBounding.getCenterPoint();
                    IETPoint etPointCenter = PointConversions.newETPoint(ptCenter);
                    if (etPointCenter != null)
                    {
                        // Make sure that the postaddobject event
                        // attaches the node/edge to this element rather than create a new one.
                        // It's a IETGraphObject.Attach rather than an IETGraphObject.Create.
                        getDrawingArea().setModelElement(element);
                        
                        TSNode tsNode = null;
                        try
                        {
                            tsNode = getDrawingArea().addNode("org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Interaction", etPointCenter, false, false);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                        
                        // Reset the model element so creates happen as normal
                        // This reset is for the put_ModelElement() addNode() calls above
                        getDrawingArea().setModelElement(null);
                        
                        result = getInteractionBoundary();
                        interactionBoundaryPE = result.getParamOne();
                        if (interactionBoundaryPE != null)
                        {
                            delayedPositionInteractionBoundary(interactionBoundaryPE, rectBounding);
                            updateInteractionBoundary(interactionBoundaryPE);
                        }
                    }
                }
            }
            else
            {
                if (interactionBoundaryPE != null)
                {
                    // Remove the node that is the interaction
                    
                    getDrawingArea().postDeletePresentationElement(interactionBoundaryPE);
                }
            }
        }
    }
    
    /**
     * Calculates the size for the interaction boundary
     */
    public IETRect calculateInteractionBoundarySize(boolean bInflateForNew)
    {
        IETRect rectBounding = null;
        
        TSEGraph tsGraph = getDrawingArea().getCurrentGraph();
        if (tsGraph != null)
        {
            TSConstRect tsRect = tsGraph.getUI().getBounds();
            if (tsRect != null)
            {
                // UPDATE:  avoiding RectConversions.newETRect() because it is broken
                rectBounding = new ETRect(tsRect.getLeft(), tsRect.getTop(), tsRect.getWidth(), tsRect.getHeight());
                
                if (bInflateForNew)
                {
                    rectBounding.inflate( 40, 40 );
                }
            }
        }
        
        return rectBounding;
    }
    
    /**
     * Ensures that the interaction boundary is the proper size.
     */
    public void positionInteractionBoundary()
    {
        ETPairT < IPresentationElement, IElement > result = getInteractionBoundary();
        IPresentationElement pe = result.getParamOne();
        
        if (pe instanceof INodePresentation)
        {
            INodePresentation nodePE = (INodePresentation)pe;
            
            final IETRect rectBounding = calculateInteractionBoundarySize(false);
            
            final Point ptCenter = rectBounding.getCenterPoint();
            
            nodePE.invalidate();
            nodePE.moveTo(ptCenter.x, ptCenter.y, (int) (MoveToFlags.MTF_MOVEX | MoveToFlags.MTF_MOVEY | MoveToFlags.MTF_LOGICALCOORD));
            nodePE.resize(rectBounding.getIntWidth(), rectBounding.getIntHeight(), false);
            nodePE.invalidate();
        }
    }
    
    /**
     * Ensures that the interaction boundary is the proper size.
     */
    public void delayedPositionInteractionBoundary(IPresentationElement pe, final IETRect rectBounding)
    {
        if (null == pe)
            throw new IllegalArgumentException();
        
        if (pe instanceof INodePresentation)
        {
            INodePresentation nodePE = (INodePresentation)pe;
            
            // Post a resize and moveto action because we need to resize after all is done.
            ITopographyChangeAction action = new TopographyChangeAction();
            if (action != null)
            {
                final Point ptCenter = rectBounding.getCenterPoint();
                
                action.setKind(DiagramAreaEnumerations.TAK_RESIZETO);
                action.setWidth(rectBounding.getIntWidth());
                action.setHeight(rectBounding.getIntHeight());
                action.setX(ptCenter.x);
                action.setY(ptCenter.y);
                action.setPresentationElement(pe);
                
                getDrawingArea().postDelayedAction(action);
            }
        }
    }
    
    /**
     * Ensures that the interaction boundary is moved behind, and relationships are discovered.
     */
    public void updateInteractionBoundary(IPresentationElement pe)
    {
        if (null == pe)
            throw new IllegalArgumentException();
        
        if (pe instanceof INodePresentation)
        {
            INodePresentation nodePE = (INodePresentation)pe;
            
            // Post a move behind contained delayed action
            getDrawingArea().postSimplePresentationDelayedAction(pe, DiagramAreaEnumerations.SPAK_MOVEBEHINDCONTAINED);
            
            // Post a discover relationships delayed action to get the messages to be displayed
            getDrawingArea().postSimplePresentationDelayedAction(pe, DiagramAreaEnumerations.SPAK_DISCOVER_RELATIONSHIPS);
        }
    }
    
    /**
     * Gets the owning interaction presentation element, or the interaction boundary if it exists
     *
     * @param pe [out,retval] Returns the interaction boundary on this diagram, if there is one
     * @param pElement [out,retval] Returnes the interaction boundary (as an IElement) on this
     * diagram, if there is one.  This parameter can be null.
     */
    protected ETPairT < IPresentationElement, IElement > getInteractionBoundary()
    {
        ETPairT < IPresentationElement, IElement > result = new ETPairT < IPresentationElement, IElement > ();
        
        IElement element = getOwner();
        if (element != null)
        {
            String strMetaType = element.getElementType();
            if (strMetaType.equals("Interaction"))
            {
                result.setParamTwo(element);
                
                ETList < IPresentationElement > pes = getDrawingArea().getAllItems2(element);
                if (pes != null)
                {
                    int nCnt = pes.getCount();
                    if (nCnt > 0)
                    {
                        assert(1 == nCnt); // there should only be one presentation element
                        
                        IPresentationElement presentationElement = pes.get(0);
                        if (presentationElement != null)
                        {
                            result.setParamOne(presentationElement);
                        }
                    }
                }
            }
        }
        
        return result;
    }
    
    /**
     * Retreive the combined fragment's interaction operand, when the edge is inside a combined fragment
     *
     * @param tsEdge TS edge that may be contained by an interaction operand
     * @return Interaction operand that contains the edge, may be <b>null</b>
     * @see org.netbeans.modules.uml.ui.products.ad.diagramengines.IADSequenceDiagEngine#getEdgesInteractionOperand(com.tomsawyer.graph.TSEdge)
     */
    public ETPairT < IInteractionOperand, ICompartment > getEdgesInteractionOperand(IETEdge edge)
    {
        ETPairT < IInteractionOperand, ICompartment > retVal = new ETPairT < IInteractionOperand, ICompartment > ();
        
        IDrawingAreaControl ctrl = getDrawingArea();
        if (ctrl != null)
        {
            ETList < IPresentationElement > elements = ctrl.getAllByType("CombinedFragment");
            if (elements != null)
            {
                for (Iterator < IPresentationElement > iter = elements.iterator(); iter.hasNext();)
                {
                    IPresentationElement curElement = iter.next();
                    if (curElement != null)
                    {
                        IDrawEngine engine = TypeConversions.getDrawEngine(curElement);
                        if (engine instanceof ICombinedFragmentDrawEngine)
                        {
                            ICombinedFragmentDrawEngine fragDE = (ICombinedFragmentDrawEngine)engine;
                            retVal = fragDE.getEdgesInteractionOperand(edge);
                            if (retVal != null)
                            {
                                // found the interaction operand, no need to look for more
                                break;
                            }
                        }
                    }
                }
            }
        }
        
        return retVal;
    }
    
    /**
     * Find 1st element below the logicial vertical location on this diagram
     *
     * @param sMetaType Indicated the desired model element's meta type
     * @param lY Logical diagram vertical location to search below
     * @return The element found, <code>null</code> if none is found
     * @see org.netbeans.modules.uml.ui.products.ad.diagramengines.IADSequenceDiagEngine#findFirstElementBelow(java.lang.String, int)
     */
    public IElement findFirstElementBelow(String sMetaType, int lY)
    {
        return getDrawingArea() != null ? TypeConversions.getElement(findFirstDrawEngineBelow(sMetaType, lY)) : null;
    }
    
    /**
     * Find 1st element above the logicial vertical location on this diagram
     *
     * @param sMetaType Indicated the desired model element's meta type
     * @param lY Logical diagram vertical location to search above
     * @return The element found, <code>null</code> if none is found
     * @see org.netbeans.modules.uml.ui.products.ad.diagramengines.IADSequenceDiagEngine#findFirstElementAbove(java.lang.String, int)
     */
    public IElement findFirstElementAbove(String sMetaType, int lY)
    {
        return  getDrawingArea() != null ? TypeConversions.getElement(findFirstDrawEngineAbove(sMetaType, lY)) : null;
    }
    
    /**
     * Find 1st draw engine above the logicial vertical location on this diagram
     *
     * @param sMetaType Indicated the desired model element's meta type
     * @param lY Logical diagram vertical location to search above
     * @return The draw engine found, <code>null</code> if none is found above lY
     * @see org.netbeans.modules.uml.ui.products.ad.diagramengines.IADSequenceDiagEngine#findFirstDrawEngineAbove(java.lang.String, int)
     */
    public IDrawEngine findFirstDrawEngineAbove(String sMetaType, int lY)
    {
        IDrawEngine retVal = null;
        
        IDrawingAreaControl ctrl = getDrawingArea();
        if (ctrl != null)
        {
            // Get the list of all the elements of the specified metatype on the
            // diagram, and search for the first element below the input vertical
            //location
            ETList < IPresentationElement > elements = ctrl.getAllByType(sMetaType);
            if (elements != null)
            {
                long minDelta = Long.MAX_VALUE;
                for (Iterator < IPresentationElement > iter = elements.iterator(); iter.hasNext();)
                {
                    IPresentationElement curElement = iter.next();
                    if (curElement != null)
                    {
                        IDrawEngine curEngine = TypeConversions.getDrawEngine(curElement);
                        if (curEngine != null)
                        {
                            IETRect rect = curEngine.getLogicalBoundingRect(false);
                            if (rect != null)
                            {
                                long top = rect.getBottom();
                                long delta = top - lY;
                                
                                if (delta > 0 && delta < minDelta)
                                {
                                    minDelta = delta;
                                    retVal = curEngine;
                                }
                            }
                        }
                    }
                }
            }
        }
        
        return retVal;
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.products.ad.diagramengines.IADSequenceDiagEngine#findFirstDrawEngineBelow(java.lang.String, int)
    */
    public IDrawEngine findFirstDrawEngineBelow(String sMetaType, int lY)
    {
        IDrawEngine retVal = null;
        
        IDrawingAreaControl ctrl = getDrawingArea();
        if (ctrl != null)
        {
            // Get the list of all the elements of the specified metatype on the
            // diagram, and search for the first element below the input vertical
            //location
            ETList < IPresentationElement > elements = ctrl.getAllByType(sMetaType);
            if (elements != null)
            {
                long minDelta = Long.MAX_VALUE;
                for (Iterator < IPresentationElement > iter = elements.iterator(); iter.hasNext();)
                {
                    IPresentationElement curElement = iter.next();
                    if (curElement != null)
                    {
                        IDrawEngine curEngine = TypeConversions.getDrawEngine(curElement);
                        if (curEngine != null)
                        {
                            IETRect rect = curEngine.getLogicalBoundingRect(false);
                            if (rect != null)
                            {
                                long top = rect.getTop();
                                long delta = lY - top;
                                
                                if ((delta > 0) && (delta < minDelta))
                                {
                                    minDelta = delta;
                                    retVal = curEngine;
                                }
                            }
                        }
                    }
                }
            }
        }
        
        return retVal;
    }
    
    /**
     * Fired when an edge reconnect is about to occur.
     *
     * @param context [in] The details about the reconnect
     */
    public void onDrawingAreaReconnectEdgeStart( IDiagram parentDiagram, IReconnectEdgeContext context, IResultCell cell )
    {
        if( null == context ) throw new IllegalArgumentException();
        
        // Call the base draw engine then handle the unique behaviors of reconnecting
        // a message and moving the piece.
        super.onDrawingAreaReconnectEdgeStart( parentDiagram, context, cell );
        
        TSEEdge tsEdge = TypeConversions.getOwnerEdge( context.getEdge() );
        if ( tsEdge != null )
        {
            // Make sure we have a message edge not something like a dependency or comment edge
            IPresentationElement pe = TypeConversions.getPresentationElement( tsEdge );
            if (pe instanceof IMessageEdgePresentation)
            {
                IETEdge pETEdge = tsEdge instanceof IETEdge ?  (IETEdge)tsEdge : null;
                
                if (pETEdge != null && ConnectorPiece.getReturnEdge(pETEdge) == tsEdge)
                {
                    // Don't let them reconnect the return edge! The C++ Doesn't. I'm not sure how they
                    // are figuring that out. (Kevin).
                    context.setCancel(true);
                    return;
                }
                
                // Determine the connector atached to the edge's end
                TSConnector connector = context.getReconnectTarget() ? tsEdge.getTargetConnector() : tsEdge.getSourceConnector();
                
                if( connector != null )
                {
                    m_reconnectPiece = new LifelinePiece( connector );
                }
            }
        }
    }
    
    /**
     * Fired when an edge has finished being connected.
     *
     * @param context [in] The details about the reconnect
     */
    public void onDrawingAreaReconnectEdgeFinish( IDiagram parentDiagram, IReconnectEdgeContext context, IResultCell cell)
    {
        if( null == context ) throw new IllegalArgumentException();
        
        boolean bCancel = true;
        
        if( m_reconnectPiece != null )
        {
            TSENode proposedEndNode = TypeConversions.getOwnerNode( context.getProposedEndNode() );
            if( proposedEndNode != null )
            {
                // Make sure we are not connecting to the starting lifeline
                TSENode anchoredNode = TypeConversions.getOwnerNode( context.getAnchoredNode() );
                if( !TypeConversions.areSameTSObjects( proposedEndNode, anchoredNode ) )
                {
                    // Make sure we are not reconnecting to the same lifeline
                    TSENode preConnectNode = TypeConversions.getOwnerNode( context.getPreConnectNode() );
                    if( !TypeConversions.areSameTSObjects( proposedEndNode, preConnectNode ) )
                    {
                        IADLifelineCompartment compartment = (IADLifelineCompartment)TypeConversions.getCompartment( proposedEndNode, IADLifelineCompartment.class );
                        if( compartment != null )
                        {
                            TSEEdge tsEdge = TypeConversions.getOwnerEdge( context.getEdge() );
                            if( tsEdge != null )
                            {
                                // Ensure that that the move will be valid on the new lifeline.
                                boolean bCanFinish = false;
                                {
                                    IETPoint etPoint = context.getLogicalPoint();
                                    if( etPoint != null )
                                    {
                                        TSConnector sourceConnector = tsEdge.getSourceConnector();
                                        if( sourceConnector != null )
                                        {
                                            etPoint.setY( (int)sourceConnector.getCenterY() );
                                            
                                            bCanFinish = compartment.canFinishMessage( etPoint );
                                        }
                                    }
                                }
                                
                                if( bCanFinish )
                                {
                                    // Ask the user how to handle the invoked operation,
                                    // if the target end of the message is being moved, and
                                    // the invoked operation exists.
                                    
                                    boolean bReconnectTarget = context.getReconnectTarget();
                                    if( bReconnectTarget )
                                    {
                                        bCancel = !processInvokedOperation( tsEdge, proposedEndNode );
                                    }
                                }
                            }
                            
                            if( !bCancel )
                            {
                                // Make sure the edge gets a chance to process it's meta data
                                super.onDrawingAreaReconnectEdgeFinish( parentDiagram, context, cell );
                                
                                // Move the saved piece to the new compartment,
                                // which posts a validate for both nodes to perform clean-up
                                
                                TSConnector connector = compartment.copyLifelinePiece( m_reconnectPiece );
                                if( connector != null )
                                {
                                    context.setAssociatedConnector( connector );
                                }
                                
                                // Make sure the graphics get cleaned up
                                IDiagram diagram = getDiagram();
                                if( diagram != null )
                                {
                                    diagram.refresh(false);
                                }
                            }
                        }
                    }
                }
            }
            
            m_reconnectPiece = null;
        }
        else
        {
            // We're not reconnecting a piece.  We could be reconnecting something
            // like a note so call the base draw engine to handle simpler edges
            super.onDrawingAreaReconnectEdgeFinish( parentDiagram, context, cell );
        }
        
        context.setCancel( bCancel );
    }
    
    /**
     * Looks in the preference to see if we should show message numbers by default
     *
     * @return true if the preference indicates that we should show message numbers by default
     */
    protected boolean isDefaultShowMessageNumbers()
    {
        String sShowList = getPreferenceValue( "Diagrams|SequenceDiagram",
                "DefaultShowMessageNumbers" );
        
        return sShowList != null && sShowList.equals("PSK_YES");
    }
    
    
    /**
     * When a message has been moved from one lifeline to another, process its invoked operation
     *
     * @param edge    The TS edge containing the a message, which may have an invoked operation
     * @param endNode The node the edge is moving to
     */
    protected boolean processInvokedOperation( TSEdge tsEdge, TSNode endNode )
    {
        if( null == tsEdge ) throw new IllegalArgumentException();
        if( null == endNode ) throw new IllegalArgumentException();
        
        boolean bProcessInvokedOperation = true;
        
        IElement edgeElement = TypeConversions.getElement( tsEdge );
        if (edgeElement instanceof IMessage)
        {
            IMessage message = (IMessage)edgeElement;
            
            IOperation operation = message.getOperationInvoked();
            if( operation != null )
            {
                // Determine the new invoked operation, the default is null
                IOperation operationInvoked = null;
                
                IElement nodeElement = TypeConversions.getElement( endNode );
                if (nodeElement instanceof ILifeline)
                {
                    ILifeline lifeline = (ILifeline)nodeElement;
                    
                    IClassifier classifier = lifeline.getRepresentingClassifier();
                    if( classifier != null )
                    {
                        operationInvoked = classifier.findMatchingOperation( operation );
                        
                        if( null == operationInvoked )
                        {
                            // Determine if the user wants to copy, move or cancel
                            IPreferenceQuestionDialog questionDialog = new SwingPreferenceQuestionDialog();
                            if( questionDialog != null )
                            {
                                String strQuestion = RESOURCE_BUNDLE.getString( "IDS_Q_INVOKED_OPERATION" );
                                String strQuestionTitle = RESOURCE_BUNDLE.getString( "IDS_Q_INVOKED_OPERATION_TITLE" );
                                
                                int nResult =
                                        questionDialog.displayFromStrings( "Default",
                                        "Diagrams|SequenceDiagram",
                                        "ProcessInvokedOperation",
                                        "PSK_ALWAYS",
                                        "PSK_NEVER",
                                        "PSK_ASK",
                                        strQuestion,
                                        SimpleQuestionDialogResultKind.SQDRK_RESULT_YES,
                                        strQuestionTitle,
                                        SimpleQuestionDialogKind.SQDK_YESNOCANCEL,
                                        MessageIconKindEnum.EDIK_ICONQUESTION,
                                        null );
                                
                                switch( nResult )
                                {
                                    case SimpleQuestionDialogResultKind.SQDRK_RESULT_YES:
                                        operation.moveToClassifier( classifier );
                                        operationInvoked = operation;
                                        break;
                                        
                                    case SimpleQuestionDialogResultKind.SQDRK_RESULT_NO:
                                    {
                                        IFeature feature = operation.duplicateToClassifier( classifier );
                                        if (feature instanceof IOperation)
                                        {
                                            operationInvoked = (IOperation)feature;
                                        }
                                    }
                                    break;
                                    
                                    default:
                                        assert ( false );
                                    case SimpleQuestionDialogResultKind.SQDRK_RESULT_CANCEL:
                                        bProcessInvokedOperation = false;
                                        break;
                                }
                            }
                        }
                    }
                }
                
                if( bProcessInvokedOperation )
                {
                    message.setOperationInvoked( operationInvoked );
                }
            }
        }
        
        return bProcessInvokedOperation;
    }
    
    public boolean onCreateMessageByKeyboard(boolean before)
    {
        boolean retVal = false;
        IDrawingAreaControl drawingDiagram  = getDrawingArea();
        if ( drawingDiagram == null)
        {
            return retVal;
        }
        
        IPresentationTypesMgr mgr = drawingDiagram.getPresentationTypesMgr();
        boolean bReadOnly = drawingDiagram.getReadOnly();
        String buttonID = drawingDiagram.getSelectedPaletteButton();
        int diagramKind = drawingDiagram.getDiagramKind();
        
        String initString = "";
        initString = mgr.getButtonInitString(buttonID, diagramKind);
        PresentationTypeDetails details = mgr.getInitStringDetails(initString, diagramKind);
        int objectKind = details.getObjectKind();
        
        int elementType = IMessageKind.MK_UNKNOWN;
        if(buttonID.equals("ID_VIEWNODE_UML_MESSAGE_ASYNCHRONOUS") == true)
        {
            elementType = IMessageKind.MK_ASYNCHRONOUS;
        }
        else if(buttonID.equals("ID_VIEWNODE_UML_MESSAGE") == true)
        {
            elementType = IMessageKind.MK_SYNCHRONOUS;
        }
        else if(buttonID.equals("ID_VIEWNODE_UML_COMMENT") ||
            buttonID.equals("ID_VIEWNODE_UML_COMMENTLINK"))
        {
            return super.onCreateNewNodeByKeyboard();
        }
        
        
        if ((bReadOnly == false) && 
            (objectKind == TSGraphObjectKind.TSGOK_EDGE) &&
            (elementType != IMessageKind.MK_UNKNOWN))
        {
            retVal = true;
            
            ETList<IPresentationElement> selectedElems = drawingDiagram.getSelected();
            
            // To prevent NPE later in the code check for 
            //  - null when no lifeline/actor is selected and 
            //  - the number of selecled elements < 2
            if (selectedElems == null || selectedElems.size() < 2)
            {
                return false;
            }
            
            ILifelineDrawEngine from = null;
            ILifelineDrawEngine to = null;
            IMessageEdgeDrawEngine msgEngine = null;
            
            for(IPresentationElement curElement : selectedElems)
            {
                IDrawEngine engine = TypeConversions.getDrawEngine(curElement);
                if(engine instanceof ILifelineDrawEngine)
                {
                    if(from == null)
                    {
                        from = (ILifelineDrawEngine)engine;
                    }
                    else if(to == null)
                    {
                        to = (ILifelineDrawEngine)engine;
                    }
                    else
                    {
                        // TODO: We have to many engines selected
                        retVal = false;
                        break;
                    }
                }
                else if(engine instanceof IMessageEdgeDrawEngine)
                {
                    if(msgEngine == null)
                    {
                        msgEngine = (IMessageEdgeDrawEngine)engine;
                    }
                    else
                    {
                        // TODO: we have to many message engines selected
                        retVal = false;
                        break;
                    }
                }
            }
            
            if(retVal == true)
            {
                if(before == true)
                {
                    from.insertMessageBefore(to, elementType, msgEngine);
                }
                else
                {
                    from.insertMessageAfter(to, elementType, msgEngine);
                }
            }
            
        }
        
        return retVal;
    }
    
    public boolean onCreateNewNodeByKeyboard()
    {
        boolean retVal = false;
        
        String buttonID = getDrawingArea().getSelectedPaletteButton();
        if((buttonID != null) && (buttonID.length() > 0))
        {
            retVal = onCreateMessageByKeyboard(true);

            if(retVal == false)
            {
                retVal = handleSpecialSelection(true);

                if(retVal == false)            
                {           
                    retVal = super.onCreateNewNodeByKeyboard();
                }
            }
        }
        return retVal;
    }

    private boolean handleSpecialSelection(boolean before)
    {
        boolean retVal = false;
        
        IDrawingAreaControl ctrl = getDrawingArea();
        
        ETList<IPresentationElement> selected = ctrl.getSelected();
        
        if (selected == null) return false ;
        
        ILifelineDrawEngine target = null;
        IMessageEdgeDrawEngine targetMsg = null;
        if(selected.size() == 1)
        {
            IDrawEngine engine = TypeConversions.getDrawEngine(selected.get(0));
            if(engine instanceof ILifelineDrawEngine)
            {
                target = (ILifelineDrawEngine)engine;  
                retVal = true;
            }
            else
            {
                // Have to do something about an error.
            }
        }
        else if(selected.size() == 2)
        {
            for(IPresentationElement curElement : selected)
            {
                IDrawEngine engine = TypeConversions.getDrawEngine(curElement);
                if((engine instanceof ILifelineDrawEngine) &&
                    (target == null))
                {
                    target = (ILifelineDrawEngine)engine;  
                    retVal = true;
                }
                else if((engine instanceof IMessageEdgeDrawEngine) &&
                        (targetMsg == null)) 
                {
                    targetMsg = (IMessageEdgeDrawEngine)engine;
                    retVal = true;
                }
                else
                {
                    // Have to do something about an error.
                    retVal = false;
                    break;
                }
            }            
        }
        
        if(retVal == true)
        {
            // At most I can have a lifeline and a message selected.
            if(ctrl.getSelectedPaletteButton().equals("ID_VIEWNODE_UML_DESTROY_LIFELINE"))
            {
                // Only want to have a lifline selected.
                target.addDestroyMessage(); 
            }    
            else if(ctrl.getSelectedPaletteButton().equals("ID_VIEWNODE_UML_MESSAGE_SELF"))
            {
                // Can have lifeline and message selected.  The message
                // is optional.
                target.addMessageToSelf(targetMsg, before); 
            }
            else if(ctrl.getSelectedPaletteButton().equals("ID_VIEWNODE_UML_MESSAGE_CREATE"))
            {
                // Can have lifeline and message selected.  The message
                // is optional.
                target.addCreateMessage(targetMsg, before); 
            }
            else
            {
                retVal = false;
            }
        }
        
        return retVal;
    }
    
    //**************************************************
    // Helper Methods
    //**************************************************
    
    /**
     * Uses the presentation reference to determine the interaction associated
     * with this diagram
     *
     * @return The 1st interaction from the list of presentation
     *         references.   The default value of NULL will create
     *         an interaction, if necessary.
     */
    protected IInteraction getSequenceDiagramInteraction()
    {
        IInteraction retVal = null;
        
        IDrawingAreaControl ctrl = getDrawingArea();
        if (ctrl != null)
        {
            IDiagram diagram = ctrl.getDiagram();
            
            ETList < IReference > references = diagram.getReferredReferences();
            if (references != null)
            {
                for (Iterator < IReference > iter = references.iterator(); iter.hasNext();)
                {
                    IReference curRef = (IReference)iter.next();
                    if (curRef instanceof IInteraction)
                    {
                        retVal = (IInteraction)curRef;
                        break; // found the interaction.
                    }
                }
            }
            
            // If we did not find an interaction,
            // create an interaction, and create the associated presentation reference
            if ((diagram != null) && (retVal == null))
            {
                if (ctrl.getNamespace() instanceof IInteraction)
                {
                    retVal = (IInteraction)ctrl.getNamespace();
                }
                else
                {
                    TypedFactoryRetriever < IInteraction > retriever = new TypedFactoryRetriever < IInteraction > ();
                    retVal = retriever.createType("Interaction");
                    
                    retVal.setName(ctrl.getName());
                    retVal.setNamespace(ctrl.getNamespace());
                    
                    // Move the sequence diagram under the interaction
                    ctrl.setNamespace(retVal);
                }
            }
        }
        
        return retVal;
    }
    
    /**
     * Tests the diagram to determine if it is a sequence diagram.
     *
     * @param diagram The diagram to test.
     * @return <code>true</code> if the diagram is a sequence diagram.
     */
    protected boolean isSequenceDiagram(IDiagram diagram)
    {
        return diagram != null && diagram.getDiagramKind() == IDiagramKind.DK_SEQUENCE_DIAGRAM;
    }
    
    /**
     * @param node
     * @param point
     * @return
     */
    private boolean canStartMessage(IETNode node, IETPoint point)
    {
        boolean retVal = true;
        
        IDrawEngine engine = TypeConversions.getDrawEngine(node);
        if (engine instanceof LifelineDrawEngine)
        {
            LifelineDrawEngine lifelineEngine = (LifelineDrawEngine)engine;
            IADLifelineCompartment compartment = lifelineEngine.getLifelineCompartment();
            if (compartment != null)
            {
                retVal = compartment.canStartMessage(point);
            }
        }
        
        return retVal;
    }
    
    /**
     * @param finishNode
     * @param xStartCenter
     * @param yStartCenter
     * @return
     */
    protected boolean canFinishMessage(IETNode finishNode, double xStartCenter, double yStartCenter)
    {
        boolean retVal = false;
        
        IElement element = TypeConversions.getElement(finishNode);
        
        if (element instanceof ILifeline)
        {
            ILifeline lifeLine = (ILifeline)element;
            
            LifelineDrawEngine lifelineEngine = getLifelineDrawEngine(finishNode);
            if (lifelineEngine != null)
            {
                IADLifelineCompartment lifelineCompartment = lifelineEngine.getLifelineCompartment();
                
                if (lifelineCompartment != null)
                {
                    retVal = lifelineCompartment.canFinishMessage(xStartCenter, yStartCenter);
                }
            }
            
        }
        else if (element instanceof IInteractionFragment)
        {
            retVal = true;
        }
        else if (element instanceof IComment)
        {
            retVal = true;
        }
        return retVal;
    }
    
    /**
     * Indicates that a message edge can be finished on the current logical location.
     *
     * @param pNode
     */
    protected boolean canReallyFinishMessage( int messageKind,
            IETNode fromNode,
            IETNode toNode,
            IETPoint ptLogical )
    {
       IElement element = TypeConversions.getElement( toNode );
       {
          if( (element instanceof IInteractionFragment) ||
                  (element instanceof IComment) )
          {
             return true;
          }
       }
       
       boolean bCanFinish = true;
       
       IADLifelineCompartment fromCompartment = (IADLifelineCompartment)TypeConversions.getCompartment( fromNode.getGraphObject(), IADLifelineCompartment.class );
       IADLifelineCompartment toCompartment = (IADLifelineCompartment)TypeConversions.getCompartment( toNode.getGraphObject(), IADLifelineCompartment.class );
       if( (fromCompartment != null) && (toCompartment != null) )
       {
          bCanFinish = toCompartment.canReallyFinishMessage( messageKind, fromCompartment, ptLogical);
       }
       
       return bCanFinish;
    }
    
    /**
     * Retreives the LifelineDrawEngine from the specified node.
     *
     * @param node The node that contains the draw engien.
     * @return The lifeline draw engine if the node has one, otherwise
     *         <b>null</b> is returned.
     */
    protected LifelineDrawEngine getLifelineDrawEngine(IETNode node)
    {
        LifelineDrawEngine retVal = null;
        
        IDrawEngine engine = TypeConversions.getDrawEngine(node);
        if (engine instanceof LifelineDrawEngine)
        {
            retVal = (LifelineDrawEngine)engine;
        }
        
        return retVal;
    }
    
    /**
     * @param pParentDiagram
     * @param context
     * @return
     */
    protected boolean decorateStartFinshNodes(IDiagram pParentDiagram, IEdgeFinishContext context)
    {
        boolean decoratedSucessfully = true;
        
        int messageKind = determineMessageKind(context);
        if (messageKind != IMessageKind.MK_UNKNOWN)
        {
            IETNode startNode = context.getStartNode();
            IETNode finishNode = context.getFinishNode();
            if ((startNode != null) && (finishNode != null))
            {
                IETPoint finishPoint = context.getLogicalPoint();
                
                double xStartCenter = 0;
                double yStartCenter = 0;
                TSConnector startConnector = context.getStartConnector();
                
//            if(startConnector == null)
//            {
//                startConnector = createStartingConnector(startNode);
//            }
                
                if (startConnector != null)
                {
                    xStartCenter = startConnector.getCenterX();
                    yStartCenter = startConnector.getCenterY();
                    
                    if(finishPoint == null)
                    {
                        finishPoint = new ETPoint(0, (int)yStartCenter);
                    }
                    else
                    {
                        finishPoint.setY((int)yStartCenter);
                    }
                }
                
                IETPoint ptStart = PointConversions.newETPoint( startConnector.getCenter() );
                
                // Fix W3790:  Verify that we can indeed decorate the nodes, and
                // create the edge.
                if ( ! canReallyFinishMessage( messageKind, startNode, finishNode, ptStart ) )
                {
                    return false;
                }
                
                finishPoint.setY( ptStart.getY() );
                
                IElement finishElement = TypeConversions.getElement(finishNode);
                if (finishElement instanceof ILifeline)
                {
                    //if(changeMessageKindIfFinishedInNameCompartment(finishNode, finishPoint, messageKind) == true)
                    if (isFinishedInNameCompartment(finishNode, finishPoint, messageKind) == true)
                    {
                        // Fix W3356:  For now, prevent the message type from
                        //             changing to a create, see other Fix W3356
                        //
                        // UPDATE: allow the message type to create, and change the presentation type as well
                        messageKind = IMessageKind.MK_CREATE;
                        decoratedSucessfully = false;
                    }
                }
                
                TSConnector finishConnector = null;
                if (messageKind == IMessageKind.MK_CREATE)
                {
                    // Fix W3356:  For now, prevent the message type from changing
                    // to a create, see other Fix W3356
                    if (decoratedSucessfully == true)
                    {
                        finishConnector = constructCreateMessage(context);
                    }
                }
                else
                {
                    boolean messageIsLeftToRight = xStartCenter < finishPoint.getX();
                    
                    IElement startElement = TypeConversions.getElement(startNode);
                    
                    ICompartment startCompartment = null;
                    if (startElement instanceof ILifeline)
                    {
                        startCompartment = constructStartLifeline(startNode, startConnector, messageKind, messageIsLeftToRight);
                        
                        finishPoint.setY((int)startConnector.getCenterY());
                    }
                    else if (startElement instanceof IInteractionFragment)
                    {
                        startCompartment = constructStartFragment(startNode, startConnector, messageKind, messageIsLeftToRight);
                        finishPoint.setY((int)startConnector.getCenterY());
                    }
                    
                    if (finishElement instanceof ILifeline)
                    {
                        finishConnector = constructFinishLifeline(finishNode, finishPoint, messageIsLeftToRight);
                        
                        if ((messageKind != IMessageKind.MK_CREATE) && (finishConnector != null))
                        {
                            // Verify that the finish connector got attached to a
                            // lifeline piece
                            Object userObject = finishConnector.getUserObject();
                            if (userObject == null)
                            {
                                decoratedSucessfully = false;
                            }
                        }
                    }
                    else if (finishElement instanceof IInteractionFragment)
                    {
                        finishConnector = constructFinishFragment(finishNode, finishPoint, messageKind, messageIsLeftToRight);
                        
                        // Make sure the message is exactly horizontal
                        final int finishY = finishPoint.getY();
                        final int startY = (int)Math.round(xStartCenter);
                        if (finishY != startY)
                        {
                            if (startCompartment instanceof IConnectorsCompartment)
                            {
                                IConnectorsCompartment compartment = (IConnectorsCompartment)startCompartment;
                                compartment.moveConnector(startConnector, finishY, false, true);
                            }
                        }
                    }
                }
                
                if (finishConnector != null)
                {
                    context.setFinishConnector(finishConnector);
                    if (decoratedSucessfully == false)
                    {
                        validateMessage(pParentDiagram, startNode);
                        validateMessage(pParentDiagram, finishNode);
                    }
                }
            }
        }
        
        return decoratedSucessfully;
    }
    
    /**
     * Calls validateNode() on the draw engine associated with the input node.
     *
     * @param pNode The node whose draw engine is being validated
     */
    private void validateMessage(IDiagram pParentDiagram, IETNode node)
    {
        IDrawEngine engine = TypeConversions.getDrawEngine(node);
        if (engine != null)
        {
            engine.validateNode();
        }
    }
    
    /**
     * For the finishing interaction occurrence, create and move the finishing
     * connector appropriately.
     *
     * @param finishNode The node where the message ends.
     * @param finishPoint The location of the finish point
     * @param messageKind The type of message to be created.
     * @param messageIsLeftToRight The direction of the edge.
     *
     * @return The created connector for the end of the message.
     */
    private TSEConnector constructFinishFragment(IETNode finishNode, IETPoint ptFinish, int messageKind, boolean bMessageIsLeftToRight)
    {
        TSEConnector finishConnector = null;
        
        IConnectorsCompartment compartment = (IConnectorsCompartment)TypeConversions.getCompartment(finishNode.getGraphObject(), IConnectorsCompartment.class);
        if (compartment != null)
        {
            // Add the connector for the finish location of the edge
            finishConnector = addConnector(finishNode, ptFinish, false);
            
            final int cmk = IConnectMessageKind.CMK_FINISH | (bMessageIsLeftToRight ? IConnectMessageKind.CMK_LEFT_TO_RIGHT : IConnectMessageKind.CMK_RIGHT_TO_LEFT);
            
            compartment.connectMessage(ptFinish, messageKind, cmk, finishConnector);
            
            final TSConstPoint ptFinishCenter = finishConnector.getCenter();
            ptFinish.setPoints((int)Math.round(ptFinishCenter.getX()), (int)Math.round(ptFinishCenter.getY()));
        }
        
        return finishConnector;
    }
    
    /**
     * For the finishing lifeline, add the proper lifeline pieces, and move the
     * connector accordingly.
     *
     * @param finishNode The node where the message ends
     * @param startConnector The connector of the start edge.
     * @param messageKind
     * @param messageIsLeftToRight <code>true</code> when the message is drawn
     *                             from left to right.
     * @return The created connector for the end of the message.
     */
    private TSEConnector constructFinishLifeline(IETNode finishLifeline, IETPoint finishPoint, boolean messageIsLeftToRight)
    {
        TSEConnector retVal = null;
        
        if (finishLifeline != null)
        {
            int finishLoc = (messageIsLeftToRight == true ? LifelineConnectorLocation.LCL_TOPLEFT : LifelineConnectorLocation.LCL_TOPRIGHT);
            
            // Create the proper element for the start of the edge
            LifelineDrawEngine engine = getLifelineDrawEngine(finishLifeline);
            if (engine != null)
            {
                // CreateElement() expects the coordinates to be in the component's
                // client coordinates
                IADLifelineCompartment compartment = engine.getLifelineCompartment();
                if (compartment != null)
                {
                    retVal = addConnector(finishLifeline, finishPoint, false);
                    
                    compartment.createElement(LifelinePiecesKind.LPK_ACTIVATION, retVal, finishLoc);
                }
            }
        }
        
        return retVal;
    }
    
    /**
     * For the starting interaction, move the starting connector appropriatly.
     *
     * @param finishNode The node containing the lifeline to construct
     * @param startConnector The connector that starts the message
     */
    private ICompartment constructStartFragment(IETNode startNode, TSConnector startConnector, int messageKind, boolean messageIsLeftToRight)
    {
        ICompartment retVal = null;
        
        if ((startNode != null) && (startConnector != null))
        {
            LifelineDrawEngine engine = getLifelineDrawEngine(startNode);
            if (engine != null)
            {
                IConnectorsCompartment compartment = (IConnectorsCompartment)engine.getLifelineCompartment();
                if (compartment != null)
                {
                    int dir = (messageIsLeftToRight == true ? IConnectMessageKind.CMK_LEFT_TO_RIGHT : IConnectMessageKind.CMK_RIGHT_TO_LEFT);
                    int cmk = IConnectMessageKind.CMK_START | dir;
                    IETPoint ptCenter = PointConversions.newETPoint(startConnector.getCenter());
                    compartment.connectMessage(ptCenter, messageKind, cmk, null);
                    
                    if (compartment instanceof ICompartment)
                    {
                        retVal = (ICompartment)compartment;
                    }
                }
            }
        }
        
        return retVal;
    }
    
    /**
     * Adds the proper lifeline pieces, and move the connector accordingly.
     *
     * @param startLifeline The node containing the lifeline to construct.
     * @param startConnector The connector that starts the message.
     * @param messageKind The type of message being created.
     * @param messageIsLeftToRight <code>true</code> when the message is drawn
     *                             from left to right
     * @return The found lifeline compartment on the start node.
     */
    private ICompartment constructStartLifeline(IETNode startLifeline, TSConnector startConnector, int messageKind, boolean messageIsLeftToRight)
    {
        ICompartment retVal = null;
        
        if ((startLifeline != null) && (startConnector != null))
        {
            int startLoc = (messageIsLeftToRight == true ? LifelineConnectorLocation.LCL_TOPRIGHT : LifelineConnectorLocation.LCL_TOPLEFT);
            
            // Create the proper element for the start of the edge
            LifelineDrawEngine engine = getLifelineDrawEngine(startLifeline);
            if (engine != null)
            {
                IADLifelineCompartment compartment = engine.getLifelineCompartment();
                if (compartment != null)
                {
                    if (messageKind == IMessageKind.MK_SYNCHRONOUS)
                    {
                        compartment.createElement(LifelinePiecesKind.LPK_SUSPENSION, startConnector, startLoc);
                    }
                    else if (messageKind == IMessageKind.MK_ASYNCHRONOUS)
                    {
                        compartment.createElement(LifelinePiecesKind.LPK_ATOMIC_FRAGMENT, startConnector, startLoc);
                    }
                    else
                    {
                        //assert false; // Did we add another message type?
                    }
                    
                    retVal = compartment;
                }
            }
        }
        
        return retVal;
    }
    
    /**
     * Performs all the necessary actions to construct the create message.
     *
     * @param context The context that has information need to construct the
     *                 create message.
     * @return The connector to associate with the edge.
     */
    protected TSConnector constructCreateMessage(IEdgeFinishContext context)
    {
        TSConnector retVal = null;
        
        IETNode startNode = context.getStartNode();
        IETNode finishNode = context.getFinishNode();
        TSConnector startConnector = context.getStartConnector();
        
        if ((startConnector != null) && (startNode != null) && (finishNode != null))
        {
            // Create the proper element for the start of the edge
            LifelineDrawEngine startEngine = getLifelineDrawEngine(startNode);
            if (startEngine != null)
            {
                IADLifelineCompartment compartment = startEngine.getLifelineCompartment();
                if (compartment != null)
                {
                    compartment.createElement(LifelinePiecesKind.LPK_ATOMIC_FRAGMENT, startConnector, LifelineConnectorLocation.LCL_TOPRIGHT);
                }
            }
            
            // Get the connector for the finish of the edge
            final LifelineDrawEngine finishEngine = getLifelineDrawEngine(finishNode);
            if (finishEngine != null)
            {
                retVal = finishEngine.getConnectorForCreateMessage();
                
                // I have to do the invoke later because the connector will not
                // be attached to the edge until after the call is completed.
                // So make the create message horizontal after the processing
                // is complete.
                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        finishEngine.makeCreateMessageHorizontal();
                    }
                });
            }
        }
        
        return retVal;
    }
    
    /**
     * Determines if the message kind should change to create
     * based on the location of the finish point within the finish node.
     *
     * @param finishNode The finish node to test
     * @param finishPoint The logical point to test, if it is within the name
     *                    compartment
     * @return Then known message kind, which may change to a create message kind
     */
    protected boolean isFinishedInNameCompartment(IETNode finishNode, IETPoint finishPoint, int messageKind)
    {
        boolean retVal = false;
        
        if ((finishNode != null) && (messageKind != IMessageKind.MK_CREATE))
        {
            LifelineDrawEngine lifelineEngine = getLifelineDrawEngine(finishNode);
            if (lifelineEngine != null)
            {
                IADNameCompartment compartment = lifelineEngine.getNameCompartment();
                if (compartment != null)
                {
//               IETPoint devicePt = getDrawingArea().deviceToLogicalPoint(finishPoint);
                    retVal = compartment.isPointInCompartment(finishPoint);
                }
            }
        }
        
        return retVal;
    }
    
    /**
     * Determines the message type from the given edge finish context.
     *
     * @return The message kind.  One of the IMessageKind values.
     * @see IMessageKind
     */
    protected int determineMessageKind(IEdgeFinishContext context)
    {
        int retVal = IMessageKind.MK_UNKNOWN;
        
        String initStr = context.getViewDescription();
        int pos = initStr.indexOf(' ');
        initStr = initStr.substring(pos + 1);
        
        if (initStr.equals("Message create") == true)
        {
            retVal = IMessageKind.MK_CREATE;
        }
        else if (initStr.equals("Message asynchronous") == true)
        {
            retVal = IMessageKind.MK_ASYNCHRONOUS;
        }
        else if (initStr.equals("Message result") == true)
        {
            retVal = IMessageKind.MK_RESULT;
        }
        else if (initStr.equals("Message") == true)
        {
            retVal = IMessageKind.MK_SYNCHRONOUS;
        }
        
        return retVal;
    }
    
    /**
     * @param node
     * @param point
     * @param b
     * @return
     */
    protected TSEConnector addConnector(IETNode node, IETPoint point, boolean adjustX)
    {
        TSEConnector retVal = null;
        
        TSConstRect bounds = node.getBounds();
        if (bounds != null)
        {
            double top = point.getY() - bounds.getTop();
            
            double perMilX = 0;
            double left = 0;
            
            if (adjustX == true)
            {
                IElement element = TypeConversions.getElement(node);
                if (element instanceof ILifeline)
                {
                    // update the logical point with the new horizontal location
                    point.setX((int)Math.round(bounds.getCenterX()));
                }
                else
                {
                    perMilX = (point.getX() < bounds.getCenterX() ? -0.5 : 0.5);
                }
            }
            else
            {
                left -= point.getX() - bounds.getLeft();
            }
            
            // Make a new connection point for the edge
            if (node instanceof TSDNode)
            {
                TSDNode tsNode = (TSDNode)node;
                TSConnector connector = tsNode.addConnector();
                if (connector instanceof TSEConnector)
                {
                    retVal = (TSEConnector)connector;
                    retVal.setProportionalXOffset(perMilX);
                    retVal.setProportionalYOffset(0.5);
                    retVal.setConstantYOffset(top);
                    retVal.setVisible(false);
                }
            }
        }
        
        return retVal;
    }
    
    //**************************************************
    // Helper Classes
    //**************************************************
    
    public class EdgeEventListener extends DrawingAreaAddEdgeEventsSinkAdapter
    {
        
      /* (non-Javadoc)
       * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaAddEdgeEventsSink#onDrawingAreaEdgeMouseMove(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.ui.swing.drawingarea.IEdgeMouseMoveContext, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
       */
        public void onDrawingAreaEdgeMouseMove(IDiagram pParentDiagram, IEdgeMouseMoveContext context, IResultCell cell)
        {
            if( (null == pParentDiagram) ||
                    (null == context) ||
                    (null == cell) ) throw new IllegalArgumentException();
            
            if( isParent( pParentDiagram ) &&
                    isSequenceDiagram( pParentDiagram ))
            {
                boolean bIsValid = false;
                
                IETNode etToNode = context.getNodeUnderMouse();
                if( etToNode != null )
                {
                    IETNode etFromNode = context.getStartNode();
                    if( etFromNode != etToNode )
                    {
                        IETPoint ptLogical = context.getLogicalPoint();
                        
                        if( etFromNode == null )
                        {
                            // Test case for when we haven't started, yet
                            bIsValid = canStartMessage( etToNode, ptLogical );
                        }
                        else
                        {
                            bIsValid = canFinishMessage( etToNode, ptLogical.getX(), ptLogical.getY() );
                        }
                    }
                }
                
                context.setValid( bIsValid );
            }
        }
        
      /* (non-Javadoc)
       * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaAddEdgeEventsSink#onDrawingAreaEdgeShouldCreateBend(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.ui.swing.drawingarea.IEdgeCreateBendContext, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
       */
        public void onDrawingAreaEdgeShouldCreateBend(IDiagram pParentDiagram, IEdgeCreateBendContext context, IResultCell cell)
        {
            if (isParent(pParentDiagram) && isSequenceDiagram(pParentDiagram))
            {
                context.setCancel(true);
            }
        }
        
      /* (non-Javadoc)
       * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaAddEdgeEventsSink#onDrawingAreaFinishEdge(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.ui.swing.drawingarea.IEdgeFinishContext, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
       */
        public void onDrawingAreaFinishEdge(IDiagram pParentDiagram, IEdgeFinishContext context, IResultCell cell)
        {
            if (isParent(pParentDiagram) && isSequenceDiagram(pParentDiagram))
            {
                if (decorateStartFinshNodes(pParentDiagram, context) == false)
                {
                    context.setCancel(true);
                }
            }
        }
        
      /* (non-Javadoc)
       * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaAddEdgeEventsSink#onDrawingAreaStartingEdge(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.ui.swing.drawingarea.IEdgeCreateContext, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
       */
        public void onDrawingAreaStartingEdge(IDiagram pParentDiagram, IEdgeCreateContext context, IResultCell cell)
        {
            if (isParent(pParentDiagram) && isSequenceDiagram(pParentDiagram))
            {
                IETNode node = context.getNode();
                if (node != null)
                {
                    IETPoint point = context.getLogicalPoint();
                    
                    if(point == null)
                    {
                        IDrawEngine engine = node.getEngine();
                        if(engine instanceof ILifelineDrawEngine)
                        {
                            ILifelineDrawEngine lifelineEngine = (ILifelineDrawEngine)engine;
                            IADLifelineCompartment compartment = lifelineEngine.getLifelineCompartment();
                            int nextY = compartment.getLocationOfNextMessage();
                            
                            point = new ETPoint(0, nextY);
                        }
                    }
                    
                    //if(canStartMessage(node, getDrawingArea().deviceToLogicalPoint(point)) == true)
                    if (canStartMessage(node, point) == true)
                    {
                        TSEConnector connector = addConnector(node, point, true);
                        if (connector != null)
                        {
                            context.setConnector(connector);
                            context.setLogicalPoint(new ETPointEx(connector.getCenter()));
                        }
                    }
                    else // if can not start message, then set context.cancel to true
                    {
                       context.setCancel(true);
                    }
                }
            }
        }
    }
    
    /**
     * Fired when a new classifier is about to be created as specified by the unknown classifier preference.
     */
    public long onPreUnknownCreate( String strTypeToCreate, IResultCell cell )
    {
        // If there are multiple sequence diagrams up we want to make sure to
        // only bring up this dialog once.  So only do it if this listener is
        // the first instance created.
        if (weAreActiveDiagram())
        {
            if( (null == strTypeToCreate) ||
                    (strTypeToCreate.length() <= 0 ) ) throw new IllegalArgumentException();
            
            DispatchHelper helper = getDispatchHelper();
            IEventDispatcher eventDispatcher = helper.getLifeTimeDispatcher();
            if ( eventDispatcher != null )
            {
                String strContextName = eventDispatcher.getCurrentContextName();
                if( strContextName.equals("RepresentingClassifier") )
                {
                    // Determine if the user wants to create a new classifier
                    IPreferenceQuestionDialog questionDialog = new SwingPreferenceQuestionDialog();
                    if( questionDialog != null )
                    {
                        String strQuestion = RESOURCE_BUNDLE.getString( "IDS_Q_CREATE_CLASS" );
                        String strQuestionTitle = RESOURCE_BUNDLE.getString( "IDS_Q_CREATE_CLASS_TITLE" );
                        
                        int nResult =
                                questionDialog.displayFromStrings( "Default",
                                "Diagrams|SequenceDiagram",
                                "CreateClassSymbols",
                                "PSK_ALWAYS",
                                "PSK_NEVER",
                                "PSK_ASK",
                                strQuestion,
                                SimpleQuestionDialogResultKind.SQDRK_RESULT_YES,
                                strQuestionTitle,
                                SimpleQuestionDialogKind.SQDK_YESNO,
                                MessageIconKindEnum.EDIK_ICONQUESTION,
                                null );
                        if( nResult != SimpleQuestionDialogResultKind.SQDRK_RESULT_YES )
                        {
                            cell.setContinue( false );
                        }
                    }
                }
            }
        }
        
        return 0;
    }
    
    /**
     * Fired when a new classifier has been created as specified by the unknown classifier preference..
     */
    public long onUnknownCreated( INamedElement newType, IResultCell cell )
    {
        return 1;
    }
    
    public ETPairT < Boolean, IElement > processOnDropElement(IElement pElementBeingDropped)
    {
        boolean bCancelThisElement = false;
        IElement pChangedElement = null;
        
        ICombinedFragment combinedFragment = null;
        if (pElementBeingDropped instanceof ICombinedFragment)
            combinedFragment = (ICombinedFragment)pElementBeingDropped;
        
        IInteraction interaction = null;
        if (pElementBeingDropped instanceof IInteraction)
            interaction = (IInteraction)pElementBeingDropped;
        
        IAttribute attribute = null;
        if (pElementBeingDropped instanceof IAttribute)
            attribute = (IAttribute)pElementBeingDropped;
        
        IClassifier classifier = null;
        if (pElementBeingDropped instanceof IClassifier)
            classifier = (IClassifier)pElementBeingDropped;
        
        if (combinedFragment != null)
        {
            // Add the InteractionOperand, if necessary
            boolean bCreateInteractioOperand = true;
            
            ETList < IInteractionOperand > interactionOperands = combinedFragment.getOperands();
            
            if (interactionOperands != null)
            {
                int nCnt = interactionOperands.getCount();
                bCreateInteractioOperand = (nCnt < 1);
            }
            
            if (bCreateInteractioOperand)
            {
                ETPairT < Boolean, IElement > superResult = super.processOnDropElement(pElementBeingDropped);
                bCancelThisElement = superResult.getParamOne().booleanValue();
                pChangedElement = superResult.getParamTwo();
                
                if (bCancelThisElement == false)
                {
                    // Create the InteractionOperand
                    TypedFactoryRetriever < IInteractionOperand > factory = new TypedFactoryRetriever < IInteractionOperand > ();
                    
                    IInteractionOperand interactionOperand = factory.createType("InteractionOperand");
                    if (interactionOperand != null)
                    {
                        combinedFragment.addOperand(interactionOperand);
                    }
                }
            }
        }
        else if (interaction != null)
        {
            bCancelThisElement = true;
            
            // Fix W1762:  Make sure the interaction is not the same as this diagram's parent interaction
            INamespace namespace = getDrawingArea().getNamespace();
            IInteraction diagramsInteraction = namespace instanceof IInteraction ?  (IInteraction)namespace : null;
            
            if (diagramsInteraction != interaction && namespace != null)
            {
                ETPairT < Boolean, IElement > superResult = super.processOnDropElement(pElementBeingDropped);
                pChangedElement = superResult.getParamTwo();
                bCancelThisElement = superResult.getParamOne().booleanValue();
                
                if (bCancelThisElement == false)
                {
                    // Create the IInteractionOccurrence, and attach the interaction
                    TypedFactoryRetriever < IInteractionOccurrence > factory = new TypedFactoryRetriever < IInteractionOccurrence > ();
                    IInteractionOccurrence interactionOccurrence = factory.createType("InteractionOccurrence");
                    
                    if (interactionOccurrence != null)
                    {
                        interactionOccurrence.setInteraction(interaction);
                        
                        // Use the diagram's namespace for the interaction occurrence
                        interactionOccurrence.setNamespace(namespace);
                        
                        // Set the rcpElement to be the interaction occurrence so,
                        // the rest of the attach stuff works
                        pChangedElement = interactionOccurrence;
                    }
                }
            }
        }
        else if (classifier != null)
        {
            IElement owner = getOwner();
            
            if (owner != null)
            {
                boolean inSame = owner.inSameProject(pElementBeingDropped);
                
                if (inSame == false)
                {
                    // We've got to import the classifier into the current project
                    // before creating the lifeline
                    
                    MetaLayerRelationFactory fact = MetaLayerRelationFactory.instance();
                    
                    IInteraction seqDiagramInteraction = getSequenceDiagramInteraction();
                    
                    if (seqDiagramInteraction != null)
                    {
                        IDirectedRelationship rel = fact.establishImportIfNeeded(seqDiagramInteraction, classifier);
                    }
                    else
                    {
                        bCancelThisElement = true;
                    }
                }
                
                if (bCancelThisElement == false)
                {
                    ILifeline lifeline = createLifeline(classifier);
                    if (lifeline != null)
                    {
                        pChangedElement = lifeline;
                    }
                }
            }
        }
        else if (attribute != null)
        {
            // Fix W4090:  The attribute's type is the lifeline's representing classifier
            IDrawingAreaControl control = getDrawingArea();
            int kind = control.getDiagramKind();
            if(kind == DiagramEnums.DK_COLLABORATION_DIAGRAM)
            {
                classifier = attribute.getType();
                
                if (classifier != null)
                {
                    ETPairT < Boolean, IElement > superResult = super.processOnDropElement(pElementBeingDropped);
                    pChangedElement = superResult.getParamTwo();
                    bCancelThisElement = superResult.getParamOne().booleanValue();
                    
                    if (bCancelThisElement == false)
                    {
                        ILifeline lifeline = createLifeline(classifier);
                        if (lifeline != null)
                        {
                            // UPDATE:  for now we name the lifeline based on the attribute name
                            String name = attribute.getName();
                            lifeline.setName(name);
                            
                            pChangedElement = lifeline;
                        }
                    }
                }
            }
        }
        else
        {
            ILifeline lifeLine = pElementBeingDropped instanceof ILifeline ?  (ILifeline)pElementBeingDropped : null;
            
            if (lifeLine != null)
            {
                IElement owner = getOwner();
                
                if (owner != null)
                {
                    boolean inSame = owner.inSameProject(pElementBeingDropped);
                    
                    if (inSame == false)
                    {
                        bCancelThisElement = true;
                    }
                }
            }
            
            // This is a fix for bug#5110404 [should not allow dragging a package to a sequence diagram]
            else if (pElementBeingDropped instanceof IPackage )
            {
                bCancelThisElement = true;
            }
        }
        
        return new ETPairT < Boolean, IElement > (new Boolean(bCancelThisElement), pChangedElement);
    }
    
    protected ILifeline createLifeline(IClassifier classifier)
    {
        if (classifier == null)
            return null;
        
        TypedFactoryRetriever < ILifeline > factory = new TypedFactoryRetriever < ILifeline > ();
        
        ILifeline lifeline = factory.createType("Lifeline");
        if (lifeline != null)
        {
            updateLifelineData(lifeline);
            lifeline.initializeWith(classifier);
            
            // Fix W2438:  Need to refresh the project tree
            IProjectTreeControl projectTree = ProductHelper.getProjectTree();
            if (projectTree != null)
            {
                projectTree.refresh(true);
            }
        }
        
        return lifeline;
    }
    
    public void updateLifelineData(IElement element)
    {
        ILifeline lifeline = null;
        if (element instanceof ILifeline)
            lifeline = (ILifeline)element;
        
        IInteraction interaction = lifeline.getInteraction();
        
        if (interaction == null)
        {
            interaction = getSequenceDiagramInteraction();
            lifeline.setInteraction(interaction);
            
            if (interaction != null)
            {
                interaction.addLifeline(lifeline);
            }
        }
    }
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine#preDoLayout(int)
         */
    public boolean preDoLayout(int nLayoutStyle)
    {
        layoutSequenceDiagram();
        return true;
    }
    
    protected void layoutSequenceDiagram()
    {
        IDrawingAreaControl area = getDrawingArea();
        
        ADGraphWindow graphWindow = null;
        if (area != null)
        {
            graphWindow = area.getGraphWindow();
        }
        
        if (graphWindow != null)
        {
            GUIBlocker blocker = new GUIBlocker();
            
            blocker.setKind(IGUIBlocker.GBK.DIAGRAM_KEYBOARD);
            blocker.setKind(IGUIBlocker.GBK.DIAGRAM_RESIZE);
            blocker.setKind(IGUIBlocker.GBK.DIAGRAM_MOVEMENT);
            blocker.setKind(IGUIBlocker.GBK.DIAGRAM_DELETION);
            blocker.setKind(IGUIBlocker.GBK.DIAGRAM_CONTAINMENT);
            blocker.setKind(IGUIBlocker.GBK.DIAGRAM_LABEL_LAYOUT);
            
            try
            {
                // Make sure any lifeline moves are handled, before validating.
                area.refresh(false);
                area.pumpMessages(true);
                
                area.validateDiagram(false, null);
                
                // Fix W3510:  Make sure any lifeline moves are handled, before determining sizes.
                area.refresh(false);
                area.pumpMessages(true);
                
                alignLifelines();
                
                ETList < TSDNode > listOriginalNodes = GetHelper.getAllNodesByElementType(graphWindow, "Lifeline");
                
                if (listOriginalNodes != null)
                {
                    int listCnt = listOriginalNodes.getCount();
                    
                    if (listCnt > 0)
                    {
                        ETList < TSDNode > listNodes = GetHelper.sortObjectsLeftToRight(graphWindow, listOriginalNodes);
                        if (listNodes != null)
                        {
                            double minHeight = Double.POSITIVE_INFINITY;
                            double maxHeight = Double.NEGATIVE_INFINITY;
                            
                            // Loop through all the nodes retaining the max and min height values
                            Iterator < TSDNode > itrGO = listNodes.iterator();
                            for (int index = 0; index < listCnt; index++)
                            {
                                TSObject tsObject = itrGO.next();
                                
                                TSDNode node = null;
                                if (tsObject instanceof TSDNode)
                                    node = (TSDNode)tsObject;
                                
                                if (node != null)
                                {
                                    TSConstRect bounding = node.getBounds();
                                    if (bounding != null)
                                    {
                                        double top = bounding.getTop();
                                        maxHeight = Math.max(top, maxHeight);
                                        
                                        double bottom = bounding.getBottom();
                                        minHeight = Math.min(minHeight, bottom);
                                    }
                                }
                            }
                            
                            // Track where the previous node's right side is located
                            double previousRight = Double.NEGATIVE_INFINITY;
                            
                            // Loop through all the nodes setting the new height, and y location
                            itrGO = listNodes.iterator();
                            for (int index = 0; index < listCnt; index++)
                            {
                                TSObject tsObject = itrGO.next();
                                
                                IDrawEngine engine = TypeConversions.getDrawEngine(tsObject);
                                
                                TSDNode node = null;
                                if (tsObject instanceof TSDNode)
                                    node = (TSDNode)tsObject;
                                
                                ILifelineDrawEngine lifelineDE = null;
                                if (engine instanceof ILifelineDrawEngine)
                                    lifelineDE = (ILifelineDrawEngine)engine;
                                
                                if (lifelineDE != null)
                                {
                                    previousRight = moveLifeline(node, lifelineDE, previousRight, minHeight, maxHeight);
                                }
                            }
                        }
                        
                        // Make sure any lifeline moves are handled.
                        area.refresh(false);
                        area.pumpMessages(true);
                        
                        // use the sorted list of nodes to update the message edges
                        ensureMessagesAreHorizontal();
                        area.pumpMessages(true);
                    }
                }
                
                m_TrackBar.postLayoutSequenceDiagram();
                area.pumpMessages(true);
                
                updateMessageLabels(graphWindow);
                area.pumpMessages(true);
                
                updateCombinedFragments(graphWindow);
                area.pumpMessages(true);
                
                //			positionInteractionBoundary();
                area.pumpMessages(true);
                
                area.refresh(false);
                area.pumpMessages(true);
                
                graphWindow.updateScrollBarValues();
                area.pumpMessages(true);
            }
            catch( Exception e )
            {
                e.printStackTrace();
            }
            finally
            {
                blocker.clearBlockers();
            }
        }
    }
    
    protected void alignLifelines()
    {
        IDrawingAreaControl area = getDrawingArea();
        
        ADGraphWindow graphWindow = null;
        if (area != null)
        {
            graphWindow = area.getGraphWindow();
        }
        
        if (graphWindow != null)
        {
            ETList < TSDNode > listNodes = GetHelper.getAllNodesByElementType(graphWindow, "Lifeline");
            
            if (listNodes != null)
            {
                int listCount = listNodes.getCount();
                
                if (listCount > 0)
                {
                    int maxTop = Integer.MIN_VALUE;
                    
                    class PairObjectInt
                    {
                        PairObjectInt(TSDNode one, int two)
                        {
                            paramOne = one;
                            paramTwo = two;
                        }
                        public TSDNode paramOne = null;
                        public int paramTwo = 0;
                    }
                    
                    ETList < PairObjectInt > listPairs = new ETArrayList < PairObjectInt > ();
                    
                    Iterator < TSDNode > itrNode = listNodes.iterator();
                    for (int index = 0; index < listCount; index++)
                    {
                        TSDNode node = itrNode.next();
                        
                        IDrawEngine drawEngine = TypeConversions.getDrawEngine(node);
                        if (drawEngine instanceof ILifelineDrawEngine)
                        {
                            ILifelineDrawEngine lifelineDrawEngine = (ILifelineDrawEngine)drawEngine;
                            
                            IADLifelineCompartment compartment = (IADLifelineCompartment)TypeConversions.getCompartment(drawEngine, IADLifelineCompartment.class);
                            if (compartment != null)
                            {
                                IETRect rect = TypeConversions.getLogicalBoundingRect(compartment);
                                final int top = rect.getTop();
                                
                                if (top > maxTop)
                                {
                                    maxTop = top;
                                }
                                
                                // Fix J1597:  In C++ v6.1.4 this check was made in moveLifeline()
                                //             In v6.2, it was only necessary to check if there was create message.
                                //             However, I (BDB) could not get the timing to work properly if
                                //             messages were attached.  So, the connectors ended up in wrong locations.
                                if ( ! lifelineDrawEngine.hasMessagesAttached() )
                                {
                                    listPairs.add(new PairObjectInt(node, top));
                                }
                            }
                        }
                    }
                    
                    Iterator < PairObjectInt > itrPairs = listPairs.iterator();
                    while (itrPairs.hasNext())
                    {
                        PairObjectInt current = itrPairs.next();
                        
                        INodePresentation nodePE = TypeConversions.getNodePresentation(current.paramOne);
                        
                        if (nodePE != null)
                        {
                            final int dy = maxTop - current.paramTwo;
                            //nodePE.resizeByHandle(0, dy, TSHandleLocation.TS_TOP_CENTER_HANDLE, true); //commented by jyothi
                        }
                    }
                    
                    area.refresh(false);
                    area.pumpMessages(true);
                    
                    ensureMessagesAreHorizontal();
                    area.pumpMessages(true);
                }
            }
        }
    }
    
    protected void ensureMessagesAreHorizontal()
    {
        IDrawingAreaControl area = getDrawingArea();
        ADGraphWindow graphWindow = null;
        if (area != null)
            graphWindow = area.getGraphWindow();
        
        if (graphWindow != null)
        {
            ETList < TSEEdge > listEdges = GetHelper.getAllEdgesByElementType(graphWindow, "Message");
            
            if (listEdges != null)
            {
                int listCount = listEdges.getCount();
                
                if (listCount > 0)
                {
                    HashMap mapEdges = new HashMap();
                    
                    Iterator < TSEEdge > itrEdge = listEdges.iterator();
                    
                    while (itrEdge.hasNext())
                    {
                        TSEEdge edge = itrEdge.next();
                        
                        if (edge != null)
                        {
                            TSConnector sourceConnector = edge.getSourceConnector();
                            if (sourceConnector != null)
                            {
                                TSConstPoint source = sourceConnector.getCenter();
                                mapEdges.put(new Double(source.getY()), edge);
                            }
                        }
                    }
                    
                    Object[] keys = mapEdges.keySet().toArray();
                    for (int index = keys.length - 1; index > 0; index--)
                    {
                        TSEEdge edge = (TSEEdge)mapEdges.get(keys[index]);
                        
                        if (edge != null)
                        {
                            IDrawEngine engine = TypeConversions.getDrawEngine(edge);
                            IMessageEdgeDrawEngine edgeEngine = null;
                            if (engine instanceof IMessageEdgeDrawEngine)
                                edgeEngine = (IMessageEdgeDrawEngine)engine;
                            
                            if (edgeEngine != null)
                            {
                                TSConnector sourceConnector = edge.getSourceConnector();
                                if (sourceConnector != null)
                                {
                                    TSConstPoint center = sourceConnector.getCenter();
                                    edgeEngine.move(center.getY(), false);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    protected double moveLifeline(TSDNode node, ILifelineDrawEngine lifelineEngine, double previousRight, double minHeight, double maxHeight)
    {
        if (node == null)
            throw new RuntimeException("Invalid TSDNode into ADDiagramSequenceEngine.moveLifeline");
        if (lifelineEngine == null)
            throw new RuntimeException("Invalid ILifelineDrawEngine into ADDiagramSequenceEngine.moveLifeline");
        
        boolean isDestroyed = lifelineEngine.isDestroyed();
        
        IETRect rectBounding = TypeConversions.getLogicalBoundingRect(lifelineEngine);
        
        final int originalWidth = (int)rectBounding.getWidth();
        final int originalHeight = (int)rectBounding.getHeight();
        final int originalX = (int)rectBounding.getCenterPoint().getX();
        final int originalY = rectBounding.getTop() - (int)(originalHeight / 2.0 + 0.5);
        
        double top = rectBounding.getTop();
        double bottom = minHeight;
        
        if (isDestroyed)
            bottom = rectBounding.getBottom();
        
        double x = 0;
        
        if (rectBounding.getLeft() < previousRight)
        {
            x = previousRight + originalWidth / 2;
            previousRight = x + (int) (originalWidth / 2 + 0.5) + 10;
        }
        else
        {
            x = originalX;
            previousRight = rectBounding.getRight();
        }
        
        final double height = top - bottom;
        final double y = top - (height / 2 + 0.5);
        
        if ((Math.abs(y - originalY) > 1) || (Math.abs(x - originalX) > 1))
        {
            node.setCenter(x, y);
        }
        
        // test to avoid "creeping" size
        if (Math.abs(height - originalHeight) > 1)
        {
            node.setSize(originalWidth, height);
            
            // Fix W5529:  Ensure that lifelines get updated properly by calling,
            // ADLifelineCompartmentImpl::NodeResized()
//			JWG-TODO lifelineEngine.nodeResized( TSE_NODE_RESIZE_ORIG_INTERACTIVE );
        }
        return previousRight;
    }
    
    public void registerAccelerators()
    {
        ETList < String > accelsToRegister = new ETArrayList < String > ();
        
        // Add the normal accelerators, minus the layout stuff
        addNormalAccelerators(accelsToRegister, false);
        
        // Add the sequence diagram layout
        accelsToRegister.add(IDrawingAreaAcceleratorKind.DAVK_LAYOUT_SEQUENCE_DIAGRAM_LAYOUT);
        accelsToRegister.add(IDrawingAreaAcceleratorKind.DAVK_SYNCHRONUS_MESSAGE);
        
        // Toggle orthogonality
        accelsToRegister.add(IDrawingAreaAcceleratorKind.DAVK_TOGGLE_ORTHOGONALITY);
        accelsToRegister.add(IDrawingAreaAcceleratorKind.DAVK_CREATE_MESSAGE_AFTER);
        
        registerAcceleratorsByType(accelsToRegister);
    }
    
    public boolean onAcceleratorInvoke(String keyCode)
    {
        boolean retVal = false;
        
        if(keyCode.equals(IDrawingAreaAcceleratorKind.DAVK_CREATE_MESSAGE_AFTER))
        {
            String buttonID = getDrawingArea().getSelectedPaletteButton();
            if((buttonID != null) && (buttonID.length() > 0))
            {
                retVal = onCreateMessageByKeyboard(false);
                if(retVal == false)
                {
                    retVal = handleSpecialSelection(false);
                }
            }
        }
        else
        {
            retVal = super.onAcceleratorInvoke(keyCode);
        }
        
        return retVal;
    }
    
    protected void updateMessageLabels(ADGraphWindow graphWindow)
    {
        if (graphWindow == null)
            throw new RuntimeException("Null passed into updateMessageLabels");
        
        // Update all the messages' labels
        ETList < TSEEdge > listEdges = GetHelper.getAllEdgesByElementType(graphWindow, "Message");
        if (listEdges != null)
        {
            int listCnt = listEdges.getCount();
            if (listCnt > 0)
            {
                // Loop through all the messages reseting their message labels
                Iterator < TSEEdge > iterEdge = listEdges.iterator();
                while (iterEdge.hasNext())
                {
                    TSEEdge edge = iterEdge.next();
                    
                    IDrawEngine drawEngine = TypeConversions.getDrawEngine(edge);
                    if (drawEngine != null)
                    {
//						JWG-TODO drawEngine.delayedRelayoutAllLabels();
                    }
                }
            }
        }
    }
    
    /**
     * Make sure the combined fragments are positioned properly
     */
    protected void updateCombinedFragments(ADGraphWindow graphWindow)
    {
        IDrawingAreaControl drawingArea = getDrawingArea();
        
        if (graphWindow != null && drawingArea != null)
        {
            IInteraction interaction = getSequenceDiagramInteraction();
            if (interaction != null)
            {
                IElementLocator locator = new ElementLocator();
                if (locator != null)
                {
                    // We have to get the from the metadata in order to get the proper sorting order
                    ETList < IElement > elements = locator.findElementsByQuery(interaction, ".//UML:CombinedFragment");
                    
                    if (elements != null)
                    {
                        int count = elements.getCount();
                        
                        // Loop in reverse order so "parent" combined fragment will graphically contian their "children"
                        for (int index = (count - 1); index >= 0; index--)
                        {
                            IElement element = elements.item(index);
                            
                            if (element != null)
                            {
                                ETList < IPresentationElement > pes = drawingArea.getAllItems2(element);
                                if (pes != null)
                                {
                                    int countPEs = pes.getCount();
                                    if (countPEs > 0)
                                    {
                                        IPresentationElement pe = pes.item(0);
                                        if (pe != null)
                                        {
                                            IDrawEngine drawEngine = TypeConversions.getDrawEngine(pe);
                                            ICombinedFragmentDrawEngine engine = null;
                                            if (drawEngine instanceof ICombinedFragmentDrawEngine)
                                                engine = (ICombinedFragmentDrawEngine)drawEngine;
                                            
                                            if (engine != null)
                                            {
                                                // Fix W9132:  The combined fragment is not allowed to shrink during layout.
                                                engine.expandToIncludeInteractionOperands(false);
                                                
                                                drawingArea.pumpMessages(false);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    protected void refreshMessageNumbers()
    {
        IDrawingAreaControl pDrawingAreaControl = getDrawingArea();
        if (pDrawingAreaControl != null)
        {
            ETList<IPresentationElement> cpPEs = pDrawingAreaControl.getAllByType("Message");
            if( cpPEs != null )
            {
                int lCnt = cpPEs.size();
                for(int lIndx=0; lIndx<lCnt; lIndx++ )
                {
                    IPresentationElement cpPresentationElement = cpPEs.get(lIndx);
                    if( cpPresentationElement != null )
                    {
                        IDrawEngine cpDrawEngine = TypeConversions.getDrawEngine( cpPresentationElement);
                        if( cpDrawEngine != null )
                        {
                            ILabelManager cpLabelManager = cpDrawEngine.getLabelManager();
                            if( cpLabelManager != null )
                            {
                                cpLabelManager.resetLabelsText();
                            }
                        }
                    }
                }
            }
        }
    }
    
    private TSConnector createStartingConnector(IETNode startNode)
    {
        TSConnector retVal = null;
        
        IDrawEngine engine = startNode.getEngine();
        if(engine instanceof ILifelineDrawEngine)
        {
            ILifelineDrawEngine lifelineEngine = (ILifelineDrawEngine)engine;
            IADLifelineCompartment compartment = lifelineEngine.getLifelineCompartment();
            int nextY = compartment.getLocationOfNextMessage();
            
            IETPoint pt = new ETPoint(0, nextY);
            retVal = addConnector(startNode, pt, true);
        }
        
        return retVal;
    }
}
