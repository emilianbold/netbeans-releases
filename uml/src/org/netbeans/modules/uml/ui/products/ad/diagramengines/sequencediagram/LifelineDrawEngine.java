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



package org.netbeans.modules.uml.ui.products.ad.diagramengines.sequencediagram;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.List;

import javax.swing.SwingUtilities;

import org.netbeans.modules.uml.common.ETException;
import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.primitivetypes.IMessageKind;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IGraphEventKind;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionOperand;
import org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline;
import org.netbeans.modules.uml.core.metamodel.dynamics.IMessage;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.support.umlsupport.ETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.ETRect;
import org.netbeans.modules.uml.core.support.umlsupport.ETSize;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETSize;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.products.ad.ADDrawEngines.ADNodeDrawEngine;
import org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.IADLifelineCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.IADNameCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.IADStereotypeCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.IConnectorsCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.ILifelineNameCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.LifelineConnectorLocation;
import org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.lifelinepieces.ConnectorPiece;
import org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.lifelinepieces.LifelinePiecesKind;
import org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.lifelinepieces.LifelineCompartmentPiece;
import org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.lifelinepieces.LifelinePiece;
import org.netbeans.modules.uml.ui.products.ad.diagramengines.IADSequenceDiagEngine;
import org.netbeans.modules.uml.ui.products.ad.drawengines.ETDrawEngine;
import org.netbeans.modules.uml.ui.products.ad.drawengines.MouseQuadrantEnum;
import org.netbeans.modules.uml.ui.products.ad.graphobjects.ETNode;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.IETGraphObjectUI;
import org.netbeans.modules.uml.ui.support.applicationmanager.IEdgePresentation;
import org.netbeans.modules.uml.ui.support.applicationmanager.INodePresentation;
import org.netbeans.modules.uml.ui.support.applicationmanager.MoveToFlags;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchive;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveDefinitions;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.GDISupport;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNodeUI;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IStickFigureCompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ITSGraphObject;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ModelElementChangedKind;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.PresentationHelper;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.swing.drawingarea.ADGraphWindow;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import org.netbeans.modules.uml.ui.swing.drawingarea.diagramtools.SmartDragTool;
import com.tomsawyer.drawing.TSConnector;
import com.tomsawyer.drawing.TSDEdge;
import com.tomsawyer.drawing.TSDNode;
import com.tomsawyer.editor.TSEEdge;
import com.tomsawyer.editor.TSENode;
import com.tomsawyer.editor.tool.TSEReconnectEdgeTool;
import com.tomsawyer.graph.TSEdge;
import com.tomsawyer.graph.TSGraphObject;
import com.tomsawyer.graph.TSNode;
import com.tomsawyer.drawing.geometry.TSConstPoint;
import com.tomsawyer.editor.TSTransform;
import java.awt.GradientPaint;
import org.netbeans.modules.uml.core.support.Debug;
import org.openide.ErrorManager;

/**
 *
 * @author Trey Spiva
 */
public class LifelineDrawEngine extends ADNodeDrawEngine
        implements ILifelineDrawEngine
{
    public final static int PIECES_BUFFER      = 20;
    public final static int LIFELINE_MIN_WIDTH = 40;
    
    private IStickFigureCompartment  m_StickFigureCompartment = null;
    private ILifelineNameCompartment m_NameCompartment        = null;
    private IADLifelineCompartment   m_LifelineCompartment    = null;
    private IADStereotypeCompartment m_StereotypeCompartment  = null;
    private TSConnector m_ConnectorCreate = null;
    
    /// xml id for the create message that was read from the archive
    private String m_strCreateMessageXML_ID;
    
    /// Mouse quadrant is the quadrant of the node the user is resizing
    int m_mqResize = MouseQuadrantEnum.MQ_UNKNOWN;
    
    /// the minimum allowable rectangle for this container
    IETRect m_rectMinimumResize = null;
    private boolean adjustCreateConnnector = false;
    
    public void init() throws ETException
    {
        if (TypeConversions.getElement(this) != null)
        {
            IPresentationElement pPE = getPresentationElement();
            if (pPE != null)
            {
                clearCompartments();
                createCompartments();
                initCompartments(pPE);
            }
            
            initResources();
            handleSizeToContents(-1);
        }
    }
    
    
    public String getElementType()
    {
        String type = super.getElementType();
        if (type == null)
        {
            type = new String("Lifeline");
        }
        return type;
    }
    
    
    public String getDrawEngineID()
    {
        return "LifelineDrawEngine";
    }
    
    public String getManagerMetaType(int nManagerKind)
    {
        return nManagerKind == ETDrawEngine.MK_EVENTMANAGER && getFirstModelElement() instanceof ILifeline ? "ADLifelineEventManager" : "";
    }
    
    /**
     * Place a specific type of decoration on the node at the specified location.
     *
     * @param type The type of decoration to add.
     * @param ptLocation The location of the decoration.
     */
    public void addDecoration(String type, IETPoint location)
    {
        IADLifelineCompartment compartment = getLifelineCompartment();
        if(compartment != null)
        {
            compartment.addDecoration(type, location);
            invalidate();
        }
    }
    
    private boolean updateConnectors = false;
    
    public void setFontResource(int resourceKind, Font font)
    {
        super.setFontResource(resourceKind, font);
        updateConnectors = true;
        
        
    }
    
    int previousTopHeight = -1;
    
    /**
     * Draws the contents of the node.  The contents of the node is decided by
     * each draw engine.
     *
     * @param pDrawInfo The information needed to draw.
     */
    protected void drawContents(IDrawInfo pDrawInfo)
    {
        IETRect rectBounding = pDrawInfo.getDeviceBounds();
        
        // Since all the compartments are stacked from top to bottom, keep track
        // of where the next top will be in device units
        int topOfNextCompartment = pDrawInfo.getDeviceBounds().getTop();
        
        IStickFigureCompartment stickFigure = getStickFigureCompartment();
        if(stickFigure != null)
        {
            IETSize size = stickFigure.calculateOptimumSize(pDrawInfo, false);
            IETRect stickBounds = pDrawInfo.getDeviceBounds();
            stickBounds.setBottom(stickBounds.getTop() + size.getHeight());
            // Device
            topOfNextCompartment = stickBounds.getBottom();
            
            stickFigure.draw(pDrawInfo, stickBounds);
        }
        
        // First calculate the size of our stereotype and name compartments, then
        // draw the box around both compartments and allow them to draw their text
        IETRect nameBoxRect = (IETRect)pDrawInfo.getDeviceBounds().clone();
        nameBoxRect.setTop(topOfNextCompartment);
        
        IETRect rectStereotype;
        IADStereotypeCompartment stereoType = getStereotypeCompartment();
        if(stereoType != null)
        {
            rectStereotype = (IETRect)nameBoxRect.clone();
            rectStereotype.setTop((int)topOfNextCompartment);
            
            IETSize stereoTypeSize = stereoType.calculateOptimumSize(pDrawInfo, false);
            
            // Size the actor compartment to its minimum size, centered on the
            // life line
            topOfNextCompartment = rectStereotype.getTop() + stereoTypeSize.getHeight();
            rectStereotype.setBottom((int)topOfNextCompartment);
        }
        else
            rectStereotype = null;
        
        // Now calculate the size of the name compartment
        ILifelineNameCompartment nameCompartment = getNameCompartment();
        IETRect rectNameCompartment = null;
        if(nameCompartment != null)
        {
            rectNameCompartment = (IETRect)rectBounding.clone();
            rectNameCompartment.setTop((int)topOfNextCompartment);
            
            IETSize nameSize = nameCompartment.calculateOptimumSize(pDrawInfo, false);
            topOfNextCompartment = rectNameCompartment.getTop() + nameSize.getHeight();
            rectNameCompartment.setBottom((int)topOfNextCompartment);
        }
        
        // Now draw the name around the stereotype and the name compartments
        nameBoxRect.setBottom(topOfNextCompartment);
        
        float centerX = (float) nameBoxRect.getCenterX();
        GradientPaint paint = new GradientPaint(centerX, nameBoxRect.getBottom(), getBkColor(), centerX, nameBoxRect.getTop(), getLightGradientFillColor());
        GDISupport.drawRectangle(pDrawInfo.getTSEGraphics(), nameBoxRect.getRectangle(), getBorderBoundsColor(), paint);
        
        // For the stereotype and name compartments tell them to draw so their
        // text is placed on top of the rectangle we drew above.
        
        if (stereoType != null && rectStereotype != null)
        {
            stereoType.draw(pDrawInfo,  rectStereotype);
        }
        
        if (nameCompartment != null && rectNameCompartment != null)
        {
            nameCompartment.draw(pDrawInfo, rectNameCompartment);
        }
        
        IADLifelineCompartment lifeline = getLifelineCompartment();
        if(lifeline != null)
        {
            IETRect rectLifeline = (IETRect)rectBounding.clone();
            if(rectBounding.getWidth() >= 4)
            {
                rectLifeline.setTop((int)topOfNextCompartment);
                if(topOfNextCompartment == 0)
                {
                    IETPoint worldOffset = lifeline.getLogicalOffsetInDrawEngineRect();
                    
                    if(worldOffset != null)
                    {
                        rectLifeline.setTop(pDrawInfo.getTSTransform().yToDevice(worldOffset.getY()));
                    }
                }
                
                if(rectLifeline.getHeight() != 0)
                {
                    lifeline.draw(pDrawInfo, rectLifeline);
                }
            }
        }
        
        if(updateConnectors == true)
        {
            // If the size of the head changes, we need to make sure that we
            // update the messages, because everything will push down.
            // This is not perfect, we rely on layout being executed after
            // drawing
            updateConnectors = false;
            
            int curHeight = (int)nameBoxRect.getHeight();
            if((previousTopHeight > 0) && (previousTopHeight != curHeight))
            {
                getLifelineCompartment().lifelineTopHeightChanged(curHeight - previousTopHeight);
            }
        }
        
        previousTopHeight = (int)nameBoxRect.getHeight();
    }
    
    /**
     * Tells the draw engine to write its data to the IProductArchive.
     *
     * @param productArchive [in] The archive we're saving to
     * @param parentElement [in] The current element, or parent for any new attributes or elements.
     */
    public long writeToArchive( IProductArchive productArchive, IProductArchiveElement parentElement)
    {
        if( null == productArchive ) throw new IllegalArgumentException();
        if( null == parentElement ) throw new IllegalArgumentException();
        
        super.writeToArchive( productArchive, parentElement );
        
        // Write the create connector's ID to the archive
        if( m_ConnectorCreate != null )
        {
            IETGraphObject etGraphObject = (IETGraphObject)getParentETElement();
            if( etGraphObject != null )
            {
                TSDEdge edge = PresentationHelper.getConnectedEdge( m_ConnectorCreate, false );
                if( edge != null )
                {
                    IPresentationElement pe = TypeConversions.getPresentationElement( edge );
                    if( pe != null )
                    {
                        String strXMIID = pe.getXMIID();
                        if( strXMIID.length() > 0 )
                        {
                            IProductArchiveElement engineElement =
                                    parentElement.getElement( IProductArchiveDefinitions.ENGINENAMEELEMENT_STRING );
                            if( engineElement != null )
                            {
                                engineElement.addAttributeString(
                                        IProductArchiveDefinitions.LIFELINEENGINE_CREATEMESSAGE_STRING, strXMIID );
                            }
                        }
                    }
                }
            }
        }
        
        return 0;
    }
    
    /**
     * Tells the draw engine to read its data to the IProductArchive
     *
     * @param productArchive [in] The archive we're reading from
     * @param pEngineElement [in] The element where this draw engine's information should exist.
     */
    public long readFromArchive( IProductArchive productArchive,
            IProductArchiveElement parentElement)
    {
        if( null == productArchive ) throw new IllegalArgumentException();
        if( null == parentElement ) throw new IllegalArgumentException();
        
        super.readFromArchive( productArchive , parentElement );
        
        m_strCreateMessageXML_ID = parentElement.getAttributeString(
                IProductArchiveDefinitions.LIFELINEENGINE_CREATEMESSAGE_STRING );
        
        // The message is attached during attachCreateMessageConnector()
        
        updateNameCompartmentRepresentsMetaType();
        
        return 0;
    }
    
    /**
     * Notification of a post load event.
     */
    public long postLoad()
    {
        // Fix W3561:  Hook up the create message after all the presentaion elements are hooked up
        attachCreateMessageConnector();
        
        super.postLoad();
        
        layout();
        
        return 0;
    }
    
    /**
     * Initializes our compartments.
     *
     * @param pElement [in] The presentation element we are representing
     * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#initCompartments(org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement)
     */
    public void initCompartments(IPresentationElement pElement)
    {
        try
        {
            // We may get here with no compartments.  This happens if we've been
            // created by the user.  If we read from a file then the compartments
            // have been pre-created and we just need to initialize them.
            if(getNumCompartments() == 0)
            {
                createCompartments();
            }
            else
            {
                // Fix W2736:  Need to add the stick figure for the actor
                String currentModelElementType = getRepresentsMetaType();
                if (currentModelElementType != null && currentModelElementType.equals("Actor") == true)
                {
                    if(getStickFigureCompartment() == null)
                    {
                        ICompartment stickFigure = createAndAddCompartment("StickFigureCompartment", 0);
                        setStickFigureCompartment(stickFigure);
                    }
                }
            }
            
            // Enhancement W6120:  Process the stereotype compartment
            IADStereotypeCompartment stereotypeCompartment = getStereotypeCompartment();
            if(stereotypeCompartment != null)
            {
                stereotypeCompartment.setEngine(this);
            }
            updateStereotypeCompartment(pElement);
            
            IElement modelElement = TypeConversions.getElement(pElement);
            if(modelElement != null)
            {
                ILifelineNameCompartment nameCompartment = getNameCompartment();
                if(nameCompartment != null)
                {
                    nameCompartment.addModelElement(modelElement, -1);
                    setDefaultCompartment(nameCompartment);
                }
                
                IADLifelineCompartment lifeLineCompartment = getLifelineCompartment();
                if(lifeLineCompartment != null)
                {
                    lifeLineCompartment.addModelElement(modelElement, -1);
                }
            }
            
            // Fix W2987:  Make sure the representing metatype is updated properly
            updateNameCompartmentRepresentsMetaType();
        }
        catch (ETException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    /**
     * Initializes our compartments.
     *
     * @param pElement [in] The presentation element we are representing
     */
    public void initCompartments()
    {
        initCompartments(getPresentation());
    }
    
    /**
     * Create the compartments for this node.
     */
    public void createCompartments() throws ETException
    {
        // Fixed issue 82208, 82207
        // get the flag which indicates if this lifeline is a actor lifeline.
        // If the lifeline is an actor lifeline or representes an actor classifier,
        // then add a stickFigureCompartment
        boolean isActorLifeline = false;
        ILifeline lifeline = getLifeline();
        if (lifeline != null)
        {
            isActorLifeline = lifeline.getIsActorLifeline();
        }
        String currentModelElementType = getRepresentsMetaType();
        
        // Process the stick figure compartment, if necessary
        if (currentModelElementType.equals("Actor") || isActorLifeline)
        {
            ICompartment compartment = createAndAddCompartment("StickFigureCompartment", 0);
            if (compartment instanceof IStickFigureCompartment)
            {
                setStickFigureCompartment((IStickFigureCompartment)compartment);
            }
        }
        else
        {
            ICompartment stereotype = createAndAddCompartment("StereotypeCompartment");
            if (stereotype instanceof IADStereotypeCompartment)
            {
                IADStereotypeCompartment stereotypeCompartment = (IADStereotypeCompartment)stereotype;
                setStereotypeCompartment(stereotypeCompartment);
            }
        }
        
        ICompartment nameCompart = createAndAddCompartment("LifelineNameCompartment");
        if (nameCompart instanceof ILifelineNameCompartment)
        {
            ILifelineNameCompartment lifeName = (ILifelineNameCompartment)nameCompart;
            lifeName.setNameCompartmentBorderKind(IADNameCompartment.NCBK_DRAW_JUST_NAME);
            setNameCompartment(lifeName);
        }
        
        ICompartment lifeCompart = createAndAddCompartment("ADLifelineCompartment");
        if (lifeCompart instanceof IADLifelineCompartment)
        {
            setLifelineCompartment((IADLifelineCompartment)lifeCompart);
        }
    }
    
    
    //**************************************************
    // helper methods
    //**************************************************
    
    /**
     * Update the location for the create message connector.
     */
    protected void updateConnector()
    {
        if(m_ConnectorCreate != null)
        {
            ILifelineNameCompartment compartment = getNameCompartment();
            if(compartment != null)
            {
                IETRect rectEngineBounding = getLogicalBoundingRect();
                IETRect rectNameBounding = TypeConversions.getLogicalBoundingRect(compartment);
                
                // Here we make sure the connector is on the left side of the name
                // compartment
                // UPDATE:  This is because we can not get the clipping to work
                //          properly.
                double yDelta = rectEngineBounding.getTop() - rectNameBounding.getTop();
                double yOffset = yDelta + (rectNameBounding.getHeight() / 2);
                double origYOffset = m_ConnectorCreate.getConstantYOffset();
                
                if((origYOffset != yOffset) &&
                        (origYOffset != (yOffset - 1)))  // solves "dancing" problem
                {
                    m_ConnectorCreate.setConstantYOffset(-yOffset);
                    m_ConnectorCreate.setProportionalYOffset(0.5);
                    m_ConnectorCreate.setProportionalXOffset(-0.5);
                    
                    // Fix J2573:  We need to ensure that the connector will always
                    //             be found inside the lifeline name compartment,
                    //             because that compartment knows how to make sure
                    //             the create edge stays horizontal, in
                    //             ETLifelineNameCompartment.updateConnectors()
                    m_ConnectorCreate.setConstantXOffset( 2 );
                    
                    //               if(isResizing() == false)
                    //               {
                    //                  makeCreateMessageHorizontal();
                    //               }
                }
            }
        }
    }
    
    
    
    /**
     * Uses the current representing classifier to update the stereotype
     * compartment information
     */
    protected void updateStereotypeCompartment(IPresentationElement element)
    {
        IADStereotypeCompartment compartment = getStereotypeCompartment();
        if(compartment != null)
        {
            IClassifier classifier = getRepresentingClassifier(element);
            if(classifier != null)
            {
                compartment.addModelElement(classifier, -1);
            }
        }
    }
    
    /**
     * Handle the pre resize graph event
     */
    void handlePreResize()
    {
        boolean bProcessMinRect = false;
        Point ptLeftTop = new Point( Integer.MAX_VALUE, Integer.MIN_VALUE );
        Point ptRightBottom = new Point( Integer.MIN_VALUE, Integer.MAX_VALUE );
        
        // Determine the minimum resize rectangle from the contained connectors
        TSENode tsNode = getNode();
        if( tsNode != null )
        {
            List connectors = tsNode.connectors();
            for (Iterator iter = connectors.iterator(); iter.hasNext();)
            {
                TSConnector connector = (TSConnector)iter.next();
                
                if( ! TypeConversions.areSameTSObjects( m_ConnectorCreate, connector ) )
                {
                    TSConstPoint ptCenter = connector.getCenter();
                    
                    ptLeftTop.x = Math.min( ptLeftTop.x, (int)ptCenter.getX() );
                    ptLeftTop.y = Math.max( ptLeftTop.y, (int)ptCenter.getY() );
                    
                    ptRightBottom.x = Math.max( ptRightBottom.x, (int)ptCenter.getX() );
                    ptRightBottom.y = Math.min( ptRightBottom.y, (int)ptCenter.getY() );
                    
                    bProcessMinRect = true;
                }
            }
        }
        
        m_mqResize = MouseQuadrantEnum.MQ_UNKNOWN;
        
        if( bProcessMinRect )
        {
            m_rectMinimumResize = new ETRect();
            m_rectMinimumResize.setSides( (int)ptLeftTop.getX(), (int)ptLeftTop.getY(), (int)ptRightBottom.getX(), (int)ptRightBottom.getY() );
            
            m_rectMinimumResize.inflate( LifelineCompartmentPiece.PIECE_WIDTH, 2 * LifelineCompartmentPiece.MIN_SIBLING_SPACE );
            
            // Also account for the area above the lifeline compartment
            {
                IADLifelineCompartment lifelineCompartment = getCompartmentByKind( IADLifelineCompartment.class );
                if ( lifelineCompartment != null )
                {
                    IETPoint ptOffset = lifelineCompartment.getLogicalOffsetInDrawEngineRect();
                    if( ptOffset != null )
                    {
                        int iOffsetY = ptOffset.getY();
                        
                        final int iOldBottom = m_rectMinimumResize.getBottom();
                        m_rectMinimumResize.setTop( m_rectMinimumResize.getTop() + iOffsetY );
                        m_rectMinimumResize.setBottom( iOldBottom );
                    }
                }
            }
            
            // Determine which quadrant is being resized
            m_mqResize = MouseQuadrantEnum.getQuadrant( getGraphWindow().getCurrentTool() );
        }
        else
        {
            m_rectMinimumResize = null;
        }
    }
    
    /**
     * Handle the post resize graph event
     */
    void handlePostResize()
    {
        m_rectMinimumResize = null;
        
        // Since we are using the resize mechanisim from the 6.2 code we need this code from 6.2
        if( m_ConnectorCreate!= null )
        {
            ConnectorPiece otherPiece = ConnectorPiece.getAssociatedPiece( m_ConnectorCreate, false );
            if( otherPiece != null )
            {
                otherPiece.setLogicalTop( (int)m_ConnectorCreate.getCenterY() );
            }
        }
    }
    
    /**
     * Returns the metatype for the represents object on the lifeline.
     */
    protected String getRepresentsMetaType()
    {
        String retVal = "";
        
        IClassifier classifier = getRepresentingClassifier();
        if(classifier != null)
        {
            retVal = classifier.getElementType();
        }
        
        if((retVal == null) || (retVal.length() <= 0))
        {
            retVal = "Class";
            
            String initStr = getInitializationString();
            if((initStr != null) && (initStr.length() > 0))
            {
                int pos = initStr.lastIndexOf(' ');
                
                String shortName  = initStr;
                if(pos >= 0)
                {
                    shortName = initStr.substring(pos + 1);
                }
                if((shortName != null) && (shortName.equals("Actor") == true))
                {
                    retVal = shortName;
                }
            }
        }
        
        return retVal;
    }
    
    /**
     * Returns the metatype for the represents object on the lifeline.
     * @return The representing classifier.
     */
    protected IClassifier getRepresentingClassifier()
    {
        IClassifier retVal = null;
        
        ILifeline lifeline = getLifeline();
        if(lifeline != null)
        {
            retVal = lifeline.getRepresentingClassifier();
        }
        
        return retVal;
    }
    
    /**
     * Returns the metatype for the represents object on the lifeline.
     * @return The representing classifier.
     */
    protected IClassifier getRepresentingClassifier(IPresentationElement element)
    {
        IClassifier retVal = null;
        
        ILifeline lifeline = getLifeline(element);
        if(lifeline != null)
        {
            retVal = lifeline.getRepresentingClassifier();
        }
        
        return retVal;
    }
    
    /**
     * Determine the ILifeline from the draw engine's model element.
     *
     * @return The lifeline that the draw engine represents.
     */
    protected ILifeline getLifeline()
    {
        ILifeline retVal = null;
        
        retVal = getLifeline(getPresentationElement());
        
        return retVal;
    }
    
    /**
     * Determine the ILifeline from the draw engine's model element.
     *
     * @return The lifeline that the draw engine represents.
     */
    protected ILifeline getLifeline(IPresentationElement pElement)
    {
        ILifeline retVal = null;
        
        if(pElement != null)
        {
            IElement element = TypeConversions.getElement(pElement);
            if (element instanceof ILifeline)
            {
                retVal = (ILifeline)element;
            }
        }
        
        return retVal;
    }
    
    /**
     * Determine the ILifeline given the TSDConnector.
     *
     * @param pConnector Connector to determine the lifeline from
     * @return The lifeline associated with the connector
     */
    protected ILifeline getLifeline(TSConnector connector)
    {
        ILifeline retVal = null;
        
        if(connector != null)
        {
            TSGraphObject tsObject = connector.getOwner();
            IElement element = TypeConversions.getElement(tsObject);
            
            if (element instanceof ILifeline)
            {
                retVal = (ILifeline)element;
            }
        }
        
        return retVal;
    }
    
    /**
     * Retrieves the stick figure compartment (if one exist).  The stick figure
     * compartment will only be present if the lifeline is an actor lifeline.
     */
    protected IStickFigureCompartment getStickFigureCompartment()
    {
        IStickFigureCompartment retVal = m_StickFigureCompartment;
        
        if(retVal == null)
        {
            retVal = getCompartmentByKind(IStickFigureCompartment.class);
            setStickFigureCompartment(retVal);
        }
        
        return retVal;
    }
    
    /**
     * Sets the stick figure compartment.
     *
     * @param value The stick figure compartment.
     */
    protected void setStickFigureCompartment(ICompartment value)
    {
        if (value instanceof IStickFigureCompartment)
        {
            setStickFigureCompartment((IStickFigureCompartment)value);
        }
    }
    
    /**
     * Sets the stick figure compartment.
     *
     * @param value The stick figure compartment.
     */
    protected void setStickFigureCompartment(IStickFigureCompartment value)
    {
        m_StickFigureCompartment = value;
    }
    /**
     * @return
     */
    public IADLifelineCompartment getLifelineCompartment()
    {
        IADLifelineCompartment retVal = m_LifelineCompartment;
        
        if(retVal == null)
        {
            retVal = getCompartmentByKind(IADLifelineCompartment.class);
            setLifelineCompartment(retVal);
        }
        
        return retVal;
    }
    
    /**
     * @param compartment
     */
    public void setLifelineCompartment(IADLifelineCompartment compartment)
    {
        m_LifelineCompartment = compartment;
    }
    
    /**
     * @return
     */
    public ILifelineNameCompartment getNameCompartment()
    {
        //return m_NameCompartment;
        ILifelineNameCompartment retVal = m_NameCompartment;
        
        if(retVal == null)
        {
            retVal = getCompartmentByKind(ILifelineNameCompartment.class);
            setNameCompartment(retVal);
        }
        
        return retVal;
    }
    
    /**
     * @param compartment
     */
    public void setNameCompartment(ILifelineNameCompartment compartment)
    {
        m_NameCompartment = compartment;
    }
    /**
     * @return
     */
    public IADStereotypeCompartment getStereotypeCompartment()
    {
        IADStereotypeCompartment retVal = m_StereotypeCompartment;
        
        if(retVal == null)
        {
            retVal = getCompartmentByKind(IADStereotypeCompartment.class);
            setStereotypeCompartment(retVal);
        }
        
        return retVal;
    }
    
    /**
     * @param compartment
     */
    public void setStereotypeCompartment(IADStereotypeCompartment compartment)
    {
        m_StereotypeCompartment = compartment;
    }
    
    /**
     * Creates a connector for the target end of create message.
     *
     * @see org.netbeans.modules.uml.ui.products.ad.diagramengines.sequencediagram.ILifelineDrawEngine#getConnectorForCreateMessage()
     */
    public TSConnector getConnectorForCreateMessage()
    {
        // Fix W5739:  The member create connector can become invalid
        //             when the user deletes the create message.
        if(m_ConnectorCreate != null)
        {
            TSENode ownerNode = getOwnerNode();
            if(ownerNode != null)
            {
                List connectors = ownerNode.connectors();
                if(connectors.contains(m_ConnectorCreate) == false)
                {
                    m_ConnectorCreate = null;
                }
            }
        }
        
        if(m_ConnectorCreate == null)
        {
            m_ConnectorCreate = addConnector();
        }
        return m_ConnectorCreate;
    }
    
    /**
     * Validates the model data against the displayed data.
     *
     * @return <code>true</code> if the node is valid.
     */
    public boolean validateNode()
    {
        // We migbt have been deleted and this gets called by invokeLater , so make sure we have an owner.
        boolean retVal = this.getOwnerNode() != null;
        
        // J2101-Deleting fragment and diagram results in exception (regression)
        // Also watch for deleted/closed diagram
        if(retVal && getDrawingArea() == null)
        {
            retVal = false;
        }
        
        if (retVal)
        {
            IDrawingAreaControl daCtrl = getDrawingArea();
            ADGraphWindow graphWindow = daCtrl != null ? daCtrl.getGraphWindow() : null;
            // Don't remove the connectors if we are in the Reconnect Edge mode,
            //			boolean reconnecting = graphWindow != null && graphWindow.getCurrentTool() instanceof TSEReconnectEdgeState;
            boolean reconnecting = graphWindow != null && graphWindow.getCurrentTool() instanceof TSEReconnectEdgeTool;
            if (!reconnecting)
            {
                removeUnusedConnectors();
                
                IADLifelineCompartment compartment = getLifelineCompartment();
                if (compartment != null)
                {
                    compartment.validatePieces();
                }
                
                layout();
                
                // Fix J2701:  Ensure that the lifeline is invalidated so the pieces get cleaned up.
                invalidate();
            }
            else if (graphWindow != null && reconnecting)
            {
                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        validateNode();
                    }
                });
            }
        }
        
        return retVal;
        
    }
    
    /**
     * Removes any connectors that don't have any messages attached
     */
    protected void removeUnusedConnectors()
    {
        TSENode node = getNode();
        if(node != null)
        {
            List connectors = node.connectors();
            ETList<ITSGraphObject> unusedConnector = new ETArrayList<ITSGraphObject>();
            for (Iterator iter = connectors.iterator(); iter.hasNext();)
            {
                TSConnector curConnector = (TSConnector)iter.next();
                if(curConnector != null && curConnector.degree() == 0 && curConnector.getOwner() == node &&
                        curConnector instanceof ITSGraphObject)
                {
                    unusedConnector.add((ITSGraphObject)curConnector);
                }
            }
            
            // We don't want to delete from TomSawyers list, build a copy.
            Iterator<ITSGraphObject> deleteIter = unusedConnector.iterator();
            while (deleteIter.hasNext())
            {
                ITSGraphObject connector = deleteIter.next();
                try
                {
                    connector.delete();
                }
                catch(Exception e)
                {
                }
                deleteIter.remove();
            }
        }
    }
    
    /**
     * Attaches the create message from the archive.
     */
    protected void attachCreateMessageConnector()
    {
        if( m_strCreateMessageXML_ID.length() > 0 )
        {
            m_ConnectorCreate = null;
            
            IDiagram diagram = getDiagram();
            if( diagram != null )
            {
                IPresentationElement presentationElement = diagram.findPresentationElement( m_strCreateMessageXML_ID );
                if( presentationElement instanceof IEdgePresentation )
                {
                    IEdgePresentation edgePresentation = (IEdgePresentation)presentationElement;
                    
                    TSEEdge edge = edgePresentation.getTSEdge();
                    if( edge != null )
                    {
                        TSConnector connector = edge.getTargetConnector();
                        if( connector == null )
                        {
                            // Hook up the create message connector for this draw engine
                            // to the edge whose xml id is contained in the archive
                            connector = getConnectorForCreateMessage();
                            if( connector != null )
                            {
                                edge.setTargetConnector( connector );
                                
                                invalidate() ;
                            }
                        }
                        else
                        {
                            m_ConnectorCreate = connector;
                        }
                    }
                }
            }
            
            // Clear the String so we only perform this action once
            assert m_ConnectorCreate != null;
            m_strCreateMessageXML_ID = "";
        }
    }
    
    /**
     * Informs the name compartment of the metatype the lifeline is representing.
     */
    protected void updateNameCompartmentRepresentsMetaType()
    {
        // Make sure the represents metatype is updated
        ILifelineNameCompartment cpNameCompartment = getNameCompartment();
        if( cpNameCompartment != null )
        {
            cpNameCompartment.setRepresentsMetaType( getRepresentsMetaType() );
        }
    }
    
    /**
     * Calculates the <i>best</i> size for this draw engine, allowing for border thickness
     *
     * @param pCDC This is the CDC* represented as an OLE_HANDLE
     * @param bAt100Pct nX,nY is either in current zoom or 100% based on
     *                  this flag.  If bAt100Pct then it's at 100%.
     * @return The <i>best</i>width and height of the draw engine
     */
    public IETSize calculateOptimumSize(IDrawInfo pDrawInfo, boolean bAt100Pct)
    {
        IETSize retVal = super.calculateOptimumSize(pDrawInfo, true);
        
        if( retVal != null )
        {
            // Fix J1794:  Because we handle sizing differently in Java, we need to
            //             ensure the minimum width of the lifeline is maintained here.
            final int width = Math.max( LIFELINE_MIN_WIDTH, retVal.getWidth() );
            retVal.setWidth( width + (2 * getBorderThickness()));
            
            if( ! bAt100Pct )
            {
                TSTransform transform = pDrawInfo != null ? pDrawInfo.getTSTransform() : getTransform();
                if( transform != null )
                {
                    retVal = scaleSize( retVal, transform );
                }
            }
        }
        
        return retVal;
    }
    
    /**
     * Resizes the node to fit the incoming compartment.
     *
     * @param pCompartment The compartment to use in the calculations.
     * @param bKeepUpperLeftPoint Not used.
     * @param bIgnorePreferences Not Used.
     *
     * @see org.netbeans.modules.uml.ui.products.ad.drawengines.INodeDrawEngine#resizeToFitCompartment(org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment, boolean, boolean)
     */
    public void resizeToFitCompartment(ICompartment pCompartment, boolean bKeepUpperLeftPoint, boolean bIgnorePreferences)
    {
        if (pCompartment instanceof IADLifelineCompartment)
        {
            handleSizeToContents((long)getLogicalBoundingRect().getWidth());
        }
    }
    
    /**
     * Returns the optimum size for an item.  This is used when an item is
     * created from the toolbar.
     * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#sizeToContents()
     */
    public void sizeToContents()
    {
        handleSizeToContents(-1);
    }
    
    /**
     * Moves this draw engine so the create message, if it exists, is horizontal.
     */
    public void makeCreateMessageHorizontal()
    {
        if(m_ConnectorCreate != null)
        {
            updateConnector();
            
            TSConnector otherConnector = PresentationHelper.getConnectorOnOtherEndOfEdge(m_ConnectorCreate, false);
            if(otherConnector != null)
            {
                moveConnectorYTo(m_ConnectorCreate, otherConnector.getCenterY());
            }
            
            updateConnectors();
        }
    }
    
    /**
     *
     */
    public void updateConnectors()
    {
        updateConnectors(getLifelineCompartment());
    }
    
    /**
     *
     */
    public void updateConnectors(IADLifelineCompartment compartment)
    {
        if (compartment instanceof IConnectorsCompartment)
        {
            IConnectorsCompartment connectCompartment = (IConnectorsCompartment)compartment;
            connectCompartment.updateConnectors(null);
        }
    }
    
    /**
     * Moves this draw engine so the create message connector is at the specified
     * vertical location.
     *
     * @param m_ConnectorCreate The connector to update.
     * @param y The new vertical location for the create message connector
     */
    protected void moveConnectorYTo(TSConnector connector, double y)
    {
        double delta = y - connector.getCenterY();
        
        // The C++ version is concerned with zoom here.  I am going to try to not
        // not be concerned with zoom.
        
        INodePresentation nodeP = TypeConversions.getNodePresentation(this);
        if(nodeP != null)
        {
            IETRect bounding = TypeConversions.getLogicalBoundingRect(nodeP);
            // Fix J2573:  CLEAN bounding.normalizeRect();
            
            //         assert bounding.getTop() <= bounding.getBottom() : "The normalizeRect method failed";
            // Fix J2314:  For some reason the draw engine was not getting redrawn during
            //             SQD CDFS.  So, we just invalidate the node to make sure it gets drawn.
            nodeP.moveTo(0, (int)(bounding.getCenterY() + delta),
                    (MoveToFlags.MTF_MOVEY | MoveToFlags.MTF_LOGICALCOORD | MoveToFlags.MTF_INVALIDATE));
        }
    }
    
    /**
     * Find 1st message below the edge's location on the diagram.
     * @see org.netbeans.modules.uml.ui.products.ad.diagramengines.sequencediagram.ILifelineDrawEngine#findFirstMessageBelow(int)
     */
    public IMessage findFirstMessageBelow(int lY)
    {
        IMessage retVal = null;
        
        IADSequenceDiagEngine sqdEngine = getDiagramEngine();
        if(sqdEngine != null)
        {
            IElement element = sqdEngine.findFirstElementBelow("Message", lY);
            if (element instanceof IMessage)
            {
                retVal = (IMessage)element;
            }
        }
        
        return retVal;
    }
    
    /**
     * Find 1st message Above the edge's location on the diagram.
     * @see org.netbeans.modules.uml.ui.products.ad.diagramengines.sequencediagram.ILifelineDrawEngine#findFirstMessageAbove(int)
     */
    public IMessage findFirstMessageAbove(int lY)
    {
        IMessage retVal = null;
        
        IADSequenceDiagEngine sqdEngine = getDiagramEngine();
        if(sqdEngine != null)
        {
            IElement element = sqdEngine.findFirstElementAbove("Message", lY);
            if (element instanceof IMessage)
            {
                retVal = (IMessage)element;
            }
        }
        
        return retVal;
    }
    
    /**
     * Creates a return message on the bottom of the pieces connected to the
     * input connectors.  The input pieces are used to determine the lifeline
     * pieces to start/end the newly created return message.
     *
     * @param pFromConnector The piece attached to this connector will be
     *                       the starting piece for the return message.
     * @param pToConnector The piece attached to this connector will be the
     *                     ending piece for the return message.
     * @param pInteractionOperand The interaction operand that contains the
     *                            return message
     * @see org.netbeans.modules.uml.ui.products.ad.diagramengines.sequencediagram.ILifelineDrawEngine#createReturnMessage(com.tomsawyer.graph.TSEdge, org.netbeans.modules.uml.core.metamodel.dynamics.IMessage, org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionOperand, org.netbeans.modules.uml.core.metamodel.dynamics.IMessage)
     */
    public void createReturnMessage(TSEEdge             synchronousEdge,
            IMessage            synchronousMessage,
            IInteractionOperand interactionOperand,
            IMessage            beforeMessage)
    {
        if((synchronousEdge != null) && (synchronousMessage != null))
        {
            TSConnector toConnector = synchronousEdge.getSourceConnector();
            TSConnector fromConnector = synchronousEdge.getTargetConnector();
            IDrawingAreaControl ctrl = getDrawingArea();
            
            if((toConnector != null) &&
                    (fromConnector != null) &&
                    (ctrl != null))
            {
                ILifeline fromLifeline = getLifeline(fromConnector);
                ILifeline toLifeline   = getLifeline(toConnector);
                
                if((fromLifeline != null) && (toLifeline != null))
                {
                    // Create the result message
                    IMessage returnMsg = fromLifeline.insertMessage(beforeMessage,
                            interactionOperand,
                            toLifeline,
                            interactionOperand,
                            null,
                            IMessageKind.MK_RESULT);
                    
                    TSGraphObject fromObject = fromConnector.getOwner();
                    TSGraphObject toObject   = toConnector.getOwner();
                    
                    TSEEdge newEdge = createEdge(returnMsg, (TSENode)fromObject, (TSENode)toObject);
                    
                    // Update the edge's connectors with the new locations
                    IADLifelineCompartment lifelineCompartment = getLifelineCompartment();
                    if(lifelineCompartment != null)
                    {
                        lifelineCompartment.connectReturnEdge(fromConnector, toConnector, newEdge);
                    }
                    
                    returnMsg.setSendingMessage(synchronousMessage);
                    
                    if(newEdge != null)
                    {
                        // Hide the return edge if that is the user's setting
                        IADSequenceDiagEngine sqdEngine = getDiagramEngine();
                        if(sqdEngine != null)
                        {
                            if(sqdEngine.isShowAllReturnMessages() == false)
                            {
                                IDrawEngine engine = TypeConversions.getDrawEngine(newEdge);
                                if(engine instanceof IMessageEdgeDrawEngine)
                                {
                                    ((IMessageEdgeDrawEngine)engine).setShow(false);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Creates a message, type determined by the input message, to the other draw engine.
     *
     * @param message [in] Meta data message to attach to the presentation element
     * @param toEngine [in] The recieving lifeline draw engine
     * @param verticalLocatoin The vertical location of the message.
     * @return The message draw engine that renders the message.
     * @see org.netbeans.modules.uml.ui.products.ad.diagramengines.sequencediagram.ILifelineDrawEngine#createMessage(org.netbeans.modules.uml.core.metamodel.dynamics.IMessage, org.netbeans.modules.uml.ui.products.ad.diagramengines.sequencediagram.ILifelineDrawEngine, int)
     */
    public ETPairT<IMessageEdgeDrawEngine,Integer> createMessage(IMessage message,ILifelineDrawEngine toEngine,
            int verticalLocation)
    {
        IMessageEdgeDrawEngine retEngine = null;
        Integer retLocation = new Integer(verticalLocation);
        
        if((message != null) && (toEngine != null))
        {
            TSENode fromNode = getNode();
            TSENode toNode   = toEngine.getNode();
            
            boolean isMessageToSelf = (fromNode == toNode);
            
            if((fromNode != null) && (toNode != null))
            {
                TSEEdge newEdge = createEdge(message, fromNode, toNode);
                int newLocation = updatePiecesAndAttachMessage(message, toEngine, newEdge, verticalLocation);
                retLocation = new Integer(newLocation);
                
                IDrawEngine engine = TypeConversions.getDrawEngine(newEdge);
                if (engine instanceof IMessageEdgeDrawEngine)
                {
                    retEngine = (IMessageEdgeDrawEngine)engine;
                    retEngine.setIsMessageToSelf(isMessageToSelf);
                    
                    // Make sure the operation is shown
                    ILabelManager labelManager = retEngine.getLabelManager();
                    if(labelManager != null)
                    {
                        labelManager.createInitialLabels();
                    }
                }
                
                // Must invalidate the nodes so the pieces get drawn properly during
                // sequence diagram generation.
                invalidate();
                if(isMessageToSelf == false)
                {
                    toEngine.invalidate();
                }
            }
        }
        
        return new ETPairT<IMessageEdgeDrawEngine,Integer>(retEngine,retLocation);
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.products.ad.diagramengines.sequencediagram.ILifelineDrawEngine#hasMessagesAttached()
    */
    public boolean hasMessagesAttached()
    {
        TSENode node = getOwnerNode();
        if(node != null)
        {
            return node.connectors().size()>1;
        }
        
        return false;
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.products.ad.diagramengines.sequencediagram.ILifelineDrawEngine#isDestroyed()
    */
    public boolean isDestroyed()
    {
        boolean bIsDestroyed = false;
        
        IADLifelineCompartment compartment = getLifelineCompartment();
        if( compartment != null )
        {
            bIsDestroyed = compartment.getIsDestroyed();
        }
        
        return bIsDestroyed;
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.products.ad.diagramengines.sequencediagram.ILifelineDrawEngine#removeIncomingOperations(boolean)
    */
    public void removeIncomingOperations(boolean bRemoveAssociatedMessages)
    {
        // TODO Auto-generated method stub
        
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.products.ad.diagramengines.sequencediagram.ILifelineDrawEngine#getAllMessageToSelfs()
    */
    public ETList < IPresentationElement >  getAllMessageToSelfs()
    {
        return null;
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.products.ad.ADDrawEngines.IADNodeDrawEngine#launchNodeTool(org.netbeans.modules.uml.core.support.umlsupport.IETPoint, org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment, org.netbeans.modules.uml.core.support.umlsupport.IETRect)
    */
    public void launchNodeTool(IETPoint pStartPos, ICompartment pCompartment, IETRect pBounds)
    {
        // TODO Auto-generated method stub
        
    }
    
    //**************************************************
    // Helper Methods
    //**************************************************
    
    /**
     * Size the node to the contained element with a minimum width.
     *
     * @param lMinWidth [in] The minumum width of the node in device scale,
     *                       -1 indicates use the calculated width.
     */
    protected void handleSizeToContents(long minWidth)
    {
        IETGraphObjectUI ui = getUI();
        if (ui instanceof IETNodeUI)
        {
            IETNodeUI nodeUI = (IETNodeUI)ui;
            IDrawInfo info = nodeUI.getDrawInfo();
            
            IETSize optimumSize = calculateOptimumSize(info, false);
                       
            // disable automatic down-sizing #90587, but stretch lifeline when necessary 
            resize(new ETSize((int)Math.max(optimumSize.getWidth(), minWidth),
                    (int)Math.max(optimumSize.getHeight(), nodeUI.getHeight())) , true);
        }
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#onGraphEvent(int)
    */
    public void onGraphEvent(int nKind)
    {
        super.onGraphEvent(nKind);
        
        switch( nKind )
        {
        case IGraphEventKind.GEK_PRE_MOVE:
            // This mechanism should not be necessary
            // This flag was used in C++ to capture the fact that TS passes a bogus resize event
            // during a moveto command from the node presentation element.
            // CLEAN m_bIsMoving = true;
            break;
            
        case IGraphEventKind.GEK_POST_MOVE:
        {
            // CLEAN m_bIsMoving = false;
            
            IADLifelineCompartment lifelineCompartment = getCompartmentByKind( IADLifelineCompartment.class );
            if ( lifelineCompartment != null )
            {
                layout();
                
                updateConnectors( lifelineCompartment );
                
                // Fix W4687:  The wiggle from CSequenceDiagramAddEdgeListener.postIncrementalMoveMessages()
                // sends a this event, so here is a good place to update the reflexive bends.
                lifelineCompartment.updateReflexiveBends() ;
            }
        }
        break;
        
        case IGraphEventKind.GEK_POST_PASTE_VIEW:
        {
            validateNode();
        }
        break;
        
        case IGraphEventKind.GEK_PRE_RESIZE:
            handlePreResize();
            break;
            
        case IGraphEventKind.GEK_POST_RESIZE:
            handlePostResize();
            break;
            
        case IGraphEventKind.GEK_SQD_DIAGRAM_POST_SCROLLZOOM:
        {
            // Notification that the sequence diagram has been zoomed
            layout();
        }
        break;
        
        case IGraphEventKind.GEK_PRE_DELETEGATHERSELECTED :
            onPreDeleteGatherSelected();
            break;
            
        default:
            // do nothing
            break;
        }
    }
    
    /**
     * alled when a node is resized.  nodeResizeOriginator is a TSENodeResizeOriginator.
     */
    public void onResized()
    {
        super.onResized();
        
        if(adjustCreateConnnector == true)
        {
            makeCreateMessageHorizontal();
            adjustCreateConnnector = false;
        }
    }
    
    /**
     * Called before the owner node is resized so that this view can restrict the way in which a resize can occur.
     *
     * @param plWidth
     * @param plHeight
     */
    public Dimension validateResize( int x, int y )
    {
        if( m_mqResize != MouseQuadrantEnum.MQ_UNKNOWN )
        {
            // The m_rectMinimumResize is the minimum allowable rectangle,
            // so don't allow any of the sides to end up inside this rectangle
            
            final IETRect rectCurrent = getLogicalBoundingRect( false );
            
            if( (x > 0) &&
                    ((m_rectMinimumResize.getLeft() != 0) ||
                    (m_rectMinimumResize.getRight() != 0)) )
            {
                if( MouseQuadrantEnum.MQ_LEFT == (m_mqResize & MouseQuadrantEnum.MQ_LEFT) )
                {
                    final int iLeft  = m_rectMinimumResize.getLeft();
                    final int iRight = rectCurrent.getRight();
                    x = Math.max( iRight - iLeft, x );
                }
                else if( MouseQuadrantEnum.MQ_RIGHT == (m_mqResize & MouseQuadrantEnum.MQ_RIGHT) )
                {
                    final int iLeft  = rectCurrent.getLeft();
                    final int iRight = m_rectMinimumResize.getRight();
                    x = Math.max( iRight - iLeft, x );
                }
                
                assert ( x > 0 );
            }
            
            
            if( (y > 0) &&
                    ((m_rectMinimumResize.getTop() != 0) ||
                    (m_rectMinimumResize.getBottom() != 0)) )
            {
                // Since these rectangle are not upside-down in Java,
                // I (BDB) had to switch the top & bottom calculations for the Java code.
                
                if( MouseQuadrantEnum.MQ_TOP == (m_mqResize & MouseQuadrantEnum.MQ_TOP) )
                {
                    final int iTop    = m_rectMinimumResize.getTop();
                    final int iBottom = rectCurrent.getBottom();
                    y = Math.max( iTop - iBottom, y );
                }
                else if( MouseQuadrantEnum.MQ_BOTTOM == (m_mqResize & MouseQuadrantEnum.MQ_BOTTOM) )
                {
                    final int iTop    = rectCurrent.getTop();
                    final int iBottom = m_rectMinimumResize.getBottom();
                    y = Math.max( iTop - iBottom, y );
                }
                
                assert ( y > 0 );
            }
        }
        
        /// Ensure that the lifeline has minimim size
        IADLifelineCompartment fromCompartment = getCompartmentByKind( IADLifelineCompartment.class );
        if ( fromCompartment != null )
        {
            int iMinHeight = fromCompartment.getMinimumHeight();
            if( iMinHeight < Integer.MAX_VALUE )
            {
                IETPoint point = fromCompartment.getLogicalOffsetInDrawEngineRect();
                
                iMinHeight += point.getY();
                y = Math.max( y, iMinHeight );
            }
        }
        
        // Fix W3290:  Have to make sure the width does not get too small.
        // Hopefully this avoids the problem where the connectors get deleted.
        x = Math.max( x, LIFELINE_MIN_WIDTH );
        
        return new Dimension( x, y );
    }
    
    /**
     * Notifier that the model element has changed, if available the changed
     * IFeature is passed along.
     *
     * @param pTargets Information about what has changed
     */
    public long modelElementHasChanged(INotificationTargets targets)
    {
        if (targets != null)
        {
            int kind = targets.getKind();
            switch( kind )
            {
            case ModelElementChangedKind.MECK_REPRESENTINGCLASSIFIERCHANGED:
                // Enhancement W6120:  Process the stereotype compartment
                updateStereotypeCompartment();
                // fall through
                
            case ModelElementChangedKind.MECK_STEREOTYPEAPPLIED:
            case ModelElementChangedKind.MECK_STEREOTYPEDELETED:
                delayedSizeToContents();
                adjustCreateConnnector = true;
                break;
                
            default:
                // do nothing
                break;
            }
        }
        
        return super.modelElementHasChanged(targets);
    }
    
    /**
     * Uses the current representing classifier to update the stereotype
     * compartment information.
     */
    protected void updateStereotypeCompartment()
    {
        IADStereotypeCompartment stereotypeCompartment = getStereotypeCompartment();
        if ( stereotypeCompartment != null )
        {
            IClassifier classifier = getRepresentingClassifier();
            if( classifier != null )
            {
                stereotypeCompartment.addModelElement( classifier, -1 );
            }
        }
    }
    
    //**************************************************
    // Helper Methods
    //**************************************************
    
    /**
     * Creates a TS edge between the two nodes.
     *
     * @param message The message to associated with the TS edge, also determines
     *                edge type
     * @param fromNode The node where the edge starts from
     * @param toNode The node where the edge finishes
     * @return The created edge
     */
    protected TSEEdge createEdge(IMessage message, TSENode fromNode, TSENode toNode)
    {
        TSEEdge retVal = null;
        
        if((message != null) && (fromNode != null) && (toNode != null))
        {
            String initStr = "";
            switch(message.getKind())
            {
            case IMessageKind.MK_CREATE:
                initStr = "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Message create";
                break;
                
            default:
                assert false : "did we add another message kind?";
                
            case IMessageKind.MK_SYNCHRONOUS:
                initStr = "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Message";
                break;
                
            case IMessageKind.MK_ASYNCHRONOUS:
                initStr = "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Message asynchronous";
                break;
                
            case IMessageKind.MK_RESULT:
                initStr = "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Message result";
                break;
            }
            
            try
            {
                IDrawingAreaControl ctrl = getDrawingArea();
                if((initStr.length() > 0) && (ctrl != null))
                {
                    ctrl.setModelElement(message);
                    TSEdge edge = ctrl.addEdge(initStr, fromNode, toNode, false, false);
                    ctrl.setModelElement(null);
                    
                    if (edge instanceof TSEEdge)
                    {
                        retVal = (TSEEdge)edge;
                    }
                    
                }
            }
            catch(ETException e)
            {
                retVal = null;
            }
        }
        
        return retVal;
    }
    
    
    /**
     * Update any necessary pieces on the lifelines, and attach the TS edge to
     * them.
     *
     * @param pMessage The message to associated with the TS edge, also determines edge type
     * @param pFromNode The node where the edge starts from
     * @param pToNode The node where the edge finishes
     * @param pEdge The TS edge to be connected to the lifelines
     * @return The drawing area vertical location for the TS edge
     */
    protected int updatePiecesAndAttachMessage(IMessage message,
            ILifelineDrawEngine toEngine,
            TSEEdge edge,
            int verticalLocation)
    {
        int retVal = verticalLocation;
        if((message != null) && (toEngine != null) && (edge != null))
        {
            int kind = message.getKind();
            switch(kind)
            {
            case IMessageKind.MK_CREATE:
                retVal = attachCreateEdge(toEngine, edge, verticalLocation);
                break;
                
            default:
                assert false : "did we add another message kind?";
                
            case IMessageKind.MK_SYNCHRONOUS:
                retVal = createPiecesAndAttachEdge(toEngine,
                        edge,
                        kind,
                        verticalLocation);
                break;
                
            case IMessageKind.MK_ASYNCHRONOUS:
                retVal = createPiecesAndAttachEdge(toEngine,
                        edge,
                        kind,
                        verticalLocation);
                break;
                
            case IMessageKind.MK_RESULT:
                attachResultEdge(toEngine, edge, verticalLocation);
                break;
            }
        }
        
        return retVal;
    }
    
    /**
     * Update any necessary pieces on the lifelines, and attach the TS edge to them.
     *
     * @param toNode The node where the edge finishes
     * @param edge The TS edge to be connected to the lifelines
     * @param plVerticalLocation The drawing area vertical location for the TS edge
     */
    protected void attachResultEdge(ILifelineDrawEngine toEngine,
            TSEEdge             edge,
            int                verticalLocation)
    {
        if((toEngine != null) && (edge != null))
        {
            boolean isMessageToSelf = (this == toEngine);
            
            IElement element = TypeConversions.getElement(edge);
            if (element instanceof IMessage)
            {
                IMessage returnMsg = (IMessage)element;
                IMessage sendingMessage = returnMsg.getSendingMessage();
                if(sendingMessage == null)
                {
                    sendingMessage = discoverSendingMessage(returnMsg);
                }
                
                IADLifelineCompartment fromCompartment = getLifelineCompartment();
                
                if(sendingMessage != null)
                {
                    IDiagram diagram = getDiagram();
                    ETList<IPresentationElement> presElements = diagram.getAllItems2(sendingMessage);
                    
                    assert presElements.size() == 1 : "There should only be one presentation element per message";
                    if((presElements != null) && (presElements.size() > 0))
                    {
                        IPresentationElement curPresElement = presElements.get(0);
                        if(curPresElement instanceof IEdgePresentation)
                        {
                            // Update the edge's connectors with the new locations
                            IEdgePresentation edgeP = (IEdgePresentation)curPresElement;
                            
                            TSEEdge tsEdge = edgeP.getTSEdge();
                            TSConnector toConnector = tsEdge.getSourceConnector();
                            TSConnector fromConnector = tsEdge.getTargetConnector();
                            if((toConnector != null) && (fromConnector != null))
                            {
                                fromCompartment.connectReturnEdge( fromConnector, toConnector, edge );
                            }
                        }
                    }
                }
                else
                {
                    IADLifelineCompartment toComparment = toEngine.getLifelineCompartment();
                    long deltaClosestAbove = 0;
                    
                    int lclFrom = LifelineConnectorLocation.LCL_UNKNOWN;
                    int lclTo = LifelineConnectorLocation.LCL_UNKNOWN;
                    if( fromCompartment == toComparment )
                    {
                        lclFrom = lclTo = LifelineConnectorLocation.LCL_BOTTOMRIGHT;
                        deltaClosestAbove = PIECES_BUFFER;
                    }
                    else
                    {
                        lclFrom = determineBottomPieceFromCorner( toEngine );
                        lclTo = determineBottomPieceToCorner( toEngine );
                    }
                    
                    // Find the closest piece on the "from" lifeline
                    IETPoint point = createNewPiecePoint( fromCompartment,
                            (int)(verticalLocation + deltaClosestAbove));
                    
                    //               IETPoint point = new ETPoint((int)rectBounding.getCenterX(),
                    //                                            (int)(verticalLocation + deltaClosestAbove));
                    
                    LifelinePiece fromPiece = fromCompartment.getClosestLifelinePiece(point);
                    if((fromPiece != null) && (fromPiece.isValid() == true))
                    {
                        // Create the from connector
                        TSConnector fromConnect = fromPiece.createConnector(lclFrom);
                        
                        // and attach the message
                        edge.setSourceConnector(fromConnect);
                        
                        // here we are trying to find the to piece by tracing back
                        // along our from piece's first connnector.  Attach the
                        // message to the suspension bar on the "to" lifeline
                        LifelinePiece toPiece = null;
                        if( isMessageToSelf == true)
                        {
                            // "Message to self" needs to be handled the "old" way
                            toPiece = fromPiece.getParentPiece();
                        }
                        else
                        {
                            toPiece = fromPiece.getAssociatedPiece();
                        }
                        
                        if(( toPiece != null ) && (toPiece.isValid() == true))
                        {
                            // Create the from connector
                            TSConnector toConnector = toPiece.createConnector(lclTo);
                            
                            // and attach the message
                            edge.setTargetConnector( toConnector);
                            
                            // Update the vertical location
                            toPiece.setIsPartOfMessageToSelf(true);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Select all the messages attached to the lifeline
     */
    protected void onPreDeleteGatherSelected()
    {
        // Remove any connectors that don't have an edge
        TSENode node = getNode();
        if( node != null )
        {
            List list = node.connectors();
            if( list != null )
            {
                for (Iterator iter = list.iterator(); iter.hasNext();)
                {
                    TSConnector connector = (TSConnector)iter.next();
                    
                    // Determine if there is an edge connected to this connector
                    TSEEdge edge = (TSEEdge)PresentationHelper.getConnectedEdge( connector, true );
                    if( edge != null )
                    {
                        if( TypeConversions.areSameTSObjects( connector, m_ConnectorCreate ) )
                        {
                            // select the edge, to be deleted later
                            edge.setSelected( true );
                            
                            // invalidate the edge so that the user can see what is to be deleted
                            IETGraphObject etGraphObject = TypeConversions.getETGraphObject( edge );
                            if( etGraphObject != null )
                            {
                                etGraphObject.invalidate() ;
                            }
                        }
                        else
                        {
                            IElement element = TypeConversions.getElement( edge );
                            if (element instanceof IMessage)
                            {
                                IMessage message = (IMessage)element;
                                
                                IETGraphObject etGraphObject = TypeConversions.getETGraphObject( edge );
                                if( etGraphObject != null )
                                {
                                    ConnectorPiece.selectAssociatedEdges( etGraphObject );
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Create the pieces necessary to attach the message.
     *
     * @param pMessage The message to associated with the TS edge, also
     *                 determines edge type
     * @param toEngine The draw engine the where the edge finishes
     * @param edge The TS edge to be connected to the lifelines
     * @param kind Used to determine the type of pieces to create
     * @param verticalLocation The vertical location for the TS edge.  The
     *                         return value is an updated version of the
     *                         verticdal location.
     * @return The drawing area vertical location for the TS edge.
     *
     * @return HRESULT
     */
    protected int createPiecesAndAttachEdge(ILifelineDrawEngine toEngine,
            TSEEdge             edge,
            int                 kind,
            int                 verticalLocation)
    {
        int retVal = verticalLocation;
        
        if((toEngine != null) && (edge != null))
        {
            IADLifelineCompartment fromCompartment = getLifelineCompartment();
            IADLifelineCompartment toCompartment = toEngine.getLifelineCompartment();
            if((fromCompartment != null) && (toCompartment != null))
            {
                int lclFrom = LifelineConnectorLocation.LCL_UNKNOWN;
                int lclTo = LifelineConnectorLocation.LCL_UNKNOWN;
                boolean isMessageToSelf = false;
                if( fromCompartment == toCompartment )
                {
                    lclFrom = LifelineConnectorLocation.LCL_TOPRIGHT;
                    lclTo = LifelineConnectorLocation.LCL_TOPRIGHT;
                    isMessageToSelf = true;
                }
                else
                {
                    lclFrom = determinePieceFromCorner( toEngine );
                    lclTo   = determinePieceToCorner( toEngine );
                }
                
                // Create the required piece on the from lifeline
                //            CComPtr< ILifelinePiece > cpFromPiece;
                int kindPiece = LifelinePiecesKind.LPK_SUSPENSION;
                if(IMessageKind.MK_ASYNCHRONOUS == kind)
                {
                    kindPiece = LifelinePiecesKind.LPK_ATOMIC_FRAGMENT;
                }
                
                IETPoint point = createNewPiecePoint( fromCompartment, retVal );
                LifelinePiece fromPiece = fromCompartment.createLifelinePiece( kindPiece,
                        point);
                if( fromPiece != null )
                {
                    // Create the from connector
                    TSConnector fromConnector = fromPiece.createConnector(lclFrom);
                    
                    // and attach the message
                    edge.setSourceConnector(fromConnector);
                    
                    // Determine the "to" vertical location
                    if( fromConnector != null )
                    {
                        retVal = fromPiece.getLogicalTop();
                        
                        // Fix W2355 & W2761:  When the user tries to add the message
                        // to self decoration on an activation bar the creation was
                        // getting messed up.
                        if( isMessageToSelf == true )
                        {
                            retVal -= PIECES_BUFFER / 2;
                        }
                    }
                }
                
                // Clean up the activation bars so that the new activation bar will hook
                // hook to the one above, if possible.
                // This inturn also updates the associated suspension area, which is
                // really what we need.
                // TESTING fromCompartment.cleanUpActivationBars();
                
                // Create an activation bar on the to lifeline, and attach the message
                point = createNewPiecePoint( toCompartment, retVal);
                LifelinePiece toPiece = toCompartment.createLifelinePiece( LifelinePiecesKind.LPK_ACTIVATION,
                        point);
                if( toPiece.isValid() == true )
                {
                    // Create the from connector
                    TSConnector toConnector = toPiece.createConnector(lclTo);
                    
                    // and attach the message
                    edge.setTargetConnector(toConnector);
                    
                    if( fromCompartment == toCompartment )
                    {
                        // For reflexive messages,
                        // the next point must be just beyond the bottom of the activation bar.
                        int bottom = toPiece.getLogicalBottom();
                        retVal = bottom - (PIECES_BUFFER / 2);
                    }
                    else if((LifelinePiecesKind.LPK_ATOMIC_FRAGMENT == kind) &&
                            (toPiece != null) )
                    {
                        retVal = toPiece.getLogicalBottom( );
                        retVal -= PIECES_BUFFER / 2;
                    }
                    else
                    {
                        // For non-reflexive messages,
                        // the next point must not be further down then the end of the parent piece
                        retVal -= PIECES_BUFFER;
                    }
                }
            }
            
        }
        
        return retVal;
    }
    
    /**
     * Determine the sending message for the input result message
     * This is done by searching for the preceeding synchronous message that
     * points the opposite direction to the result message.
     *
     * @warning For now the code fails if there is a synchronous/result pair between the result and its sender
     */
    protected IMessage discoverSendingMessage( IMessage messageResult)
    {
        // The C++ version always returns NULL from some reason.  So, we are
        // doing the same thing here.
        return null;
    }
    /**
     * @param toEngine
     * @param edge
     * @return
     */
    protected int attachCreateEdge(ILifelineDrawEngine toEngine,
            TSEEdge             edge,
            int                 verticalLocation)
    {
        int retVal = verticalLocation;
        
        if((toEngine != null) && (edge != null))
        {
            IADLifelineCompartment fromCompartment = getLifelineCompartment();
            if(fromCompartment != null)
            {
                TSConnector fromConnector = addConnector();
                
                // Create a suspension area on the from lifeline, and attach the
                // message
                IETPoint pt = createNewPiecePoint(fromCompartment, verticalLocation);
                fromConnector.setCenterY(pt.getY());
                //fromConnector.setCenterY(0);
                fromCompartment.createElement(LifelinePiecesKind.LPK_ATOMIC_FRAGMENT,
                        fromConnector,
                        determinePieceFromCorner(toEngine));
                
                edge.setSourceConnector(fromConnector);
                
                // Attach the end of the edge to the create connector for the to DE
                TSConnector toConnector = toEngine.getConnectorForCreateMessage();
                edge.setTargetConnector(toConnector);
                toEngine.makeCreateMessageHorizontal();
                
                retVal -=  PIECES_BUFFER / 2;
            }
        }
        
        Debug.out.println("Number of Connectors " + getNode().numberOfConnectors());
        return retVal;
    }
    
    /**
     * Determine which from corner is the inside corners between this DE and the to
     * DE.
     *
     * @param toEngine  The other engine
     * @return The connector location for this engine.  Will be one of the
     *         LifelineConnectorLocation values.
     *
     * @see LifelineConnectorLocation
     */
    protected int determinePieceFromCorner(ILifelineDrawEngine toEngine)
    {
        int retVal = LifelineConnectorLocation.LCL_UNKNOWN;
        
        if(toEngine != null)
        {
            IETRect rectFromBounding = getLogicalBoundingRect();
            IETRect rectToBounding = TypeConversions.getLogicalBoundingRect(toEngine);
            
            if(rectFromBounding.getCenterX() < rectToBounding.getCenterX())
            {
                retVal = LifelineConnectorLocation.LCL_TOPRIGHT;
            }
            else
            {
                retVal = LifelineConnectorLocation.LCL_TOPLEFT;
            }
        }
        
        return retVal;
    }
    
    /**
     * Determine which from corner is the inside corners between this DE and the to
     * DE.
     *
     * @param toEngine  The other engine
     * @return The connector location for this engine.  Will be one of the
     *         LifelineConnectorLocation values.
     *
     * @see LifelineConnectorLocation
     */
    protected int determineBottomPieceFromCorner(ILifelineDrawEngine toEngine)
    {
        int retVal = LifelineConnectorLocation.LCL_UNKNOWN;
        
        if(toEngine != null)
        {
            IETRect rectFromBounding = getLogicalBoundingRect();
            IETRect rectToBounding = TypeConversions.getLogicalBoundingRect(toEngine);
            
            if(rectFromBounding.getCenterX() < rectToBounding.getCenterX())
            {
                retVal = LifelineConnectorLocation.LCL_BOTTOMRIGHT;
            }
            else
            {
                retVal = LifelineConnectorLocation.LCL_BOTTOMLEFT;
            }
        }
        
        return retVal;
    }
    
    /**
     * Determine which from corner is the inside corners between this DE and the to
     * DE.
     *
     * @param toEngine  The other engine
     * @return The connector location for this engine.  Will be one of the
     *         LifelineConnectorLocation values.
     *
     * @see LifelineConnectorLocation
     */
    protected int determineBottomPieceToCorner(ILifelineDrawEngine toEngine)
    {
        int retVal = LifelineConnectorLocation.LCL_UNKNOWN;
        
        if(toEngine != null)
        {
            IETRect rectFromBounding = getLogicalBoundingRect();
            IETRect rectToBounding = TypeConversions.getLogicalBoundingRect(toEngine);
            
            if(rectFromBounding.getCenterX() < rectToBounding.getCenterX())
            {
                retVal = LifelineConnectorLocation.LCL_BOTTOMLEFT;
            }
            else
            {
                retVal = LifelineConnectorLocation.LCL_BOTTOMRIGHT;
            }
        }
        
        return retVal;
    }
    
    /**
     * Determine which from corner is the inside corners between this DE and the to
     * DE.
     *
     * @param toEngine  The other engine
     * @return The connector location for this engine.  Will be one of the
     *         LifelineConnectorLocation values.
     *
     * @see LifelineConnectorLocation
     */
    protected int determinePieceToCorner(ILifelineDrawEngine toEngine)
    {
        int retVal = LifelineConnectorLocation.LCL_UNKNOWN;
        
        if(toEngine != null)
        {
            IETRect rectFromBounding = getLogicalBoundingRect();
            IETRect rectToBounding = TypeConversions.getLogicalBoundingRect(toEngine);
            
            if(rectFromBounding.getCenterX() < rectToBounding.getCenterX())
            {
                retVal = LifelineConnectorLocation.LCL_TOPLEFT;
            }
            else
            {
                retVal = LifelineConnectorLocation.LCL_TOPRIGHT;
            }
        }
        
        return retVal;
    }
    
    /**
     * Creates an ET point that is the logical drawing area coordinate for a new
     * piece this is done by using the center location of the draw engine for the
     * horizontal location
     *
     * @param comparment The reference compartment.
     * @param verticalLocation The logical drawing area vertical location for
     *                          the new piece
     * @return The point
     */
    protected IETPoint createNewPiecePoint(IADLifelineCompartment compartment,
            int                    verticalLocation)
    {
        IETPoint retVal = null;
        
        IETRect rectBounding = TypeConversions.getLogicalBoundingRect(compartment);
        retVal = new ETPoint((int)rectBounding.getCenterX(), verticalLocation);
        
        return retVal;
    }
    
    /**
     * Retreives the diagram engine for the diagram that contains the draw engine.
     * @return The sequence diagram engine contains the lifeline.
     */
    protected IADSequenceDiagEngine getDiagramEngine()
    {
        IADSequenceDiagEngine retVal = null;
        
        IDiagramEngine engine = TypeConversions.getDiagramEngine(getDiagram());
        if (engine instanceof IADSequenceDiagEngine)
        {
            retVal = (IADSequenceDiagEngine)engine;
        }
        
        return retVal;
    }
    
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#handleLeftMouseButtonPressed(java.awt.event.MouseEvent)
         */
    public boolean handleLeftMouseButtonPressed(MouseEvent pEvent)
    {
        if(hasMessagesAttached() )
        {
            IDrawingAreaControl daCtrl = getDrawingArea();
            ADGraphWindow graphWindow = daCtrl != null ? daCtrl.getGraphWindow() : null;
            
            SmartDragTool dragTool = createSmartDragTool(pEvent);
            
            if(dragTool == null)
                return false;
            
            dragTool.setDragRestrictionType(SmartDragTool.DR.HORIZONTAL_MOVE_ONLY);
            
            //graphWindow.getCurrentState().setState(dragTool);
            graphWindow.getCurrentTool().setTool(dragTool);
            dragTool.onMousePressed(pEvent);
            
            return true;
        }
        else
            return super.handleLeftMouseButtonPressed(pEvent);
    }
    
    public boolean isCreated()
    {
        validateCreateMessageConnector();
        
        return m_ConnectorCreate != null;
    }
    
    /**
     * Inserts a new message between a this lifeline and another lifeline.
     * If a relative message is also specified, the new message will be added
     * before the specified message.  Otherwise the new message will be added
     * after all other message.
     *
     * @param to The reciever of the message.
     * @param msgType The type of message.
     * @param relativeMessage The message that will be after the new message.
     *                        May be null.
     */
    public void insertMessageBefore(ILifelineDrawEngine to,
            int msgType,
            IMessageEdgeDrawEngine relativeMessage)
    {
        IADLifelineCompartment compartment = getLifelineCompartment();
        compartment.addMessageBefore(to,
                relativeMessage,
                msgType);
    }
    
    /**
     * Inserts a new message between a this lifeline and another lifeline.
     * If a relative message is also specified, the new message will be added
     * after the specified message.  Otherwise the new message will be added
     * after all other message.
     *
     * @param to The reciever of the message.
     * @param msgType The type of message.
     * @param relativeMessage The message that will be before the new message.
     *                        May be null.
     */
    public void insertMessageAfter(ILifelineDrawEngine to,
            int msgType,
            IMessageEdgeDrawEngine relativeMessage)
    {
        IADLifelineCompartment compartment = getLifelineCompartment();
        compartment.addMessageAfter(to,
                relativeMessage,
                msgType);
    }
    
    /**
     * Adds a destroy decorator to the end of a the lifeline.
     */
    public void addDestroyMessage()
    {
        getLifelineCompartment().addDecoration("destroy", null);
    }
    
    /**
     * Inserts a new message to self.  The location of the new messages is
     * based on target messages.  The new message to self will be either
     * before or after the target message.
     *
     * If the target message is null, the new message will be at the end of the
     * lifeline.
     *
     * @param targetMsg The message that the new message will be relative to.
     *                  Can be null.
     * @param before if true, the new message will be before the target message.
     *               if false, the new message will be after the target message.
     */
    public void addMessageToSelf(IMessageEdgeDrawEngine targetMsg, boolean before)
    {
        IADLifelineCompartment compartment = getLifelineCompartment();
        if(before == true)
        {
            compartment.addMessageBefore(this, targetMsg,
                    IMessageKind.MK_SYNCHRONOUS);
        }
        else
        {
            compartment.addMessageAfter(this, targetMsg,
                    IMessageKind.MK_SYNCHRONOUS);
        }
    }
    
    /**
     * Inserts a new create message.  The location of the new messages is
     * based on target messages.  The new create message will be either
     * before or after the target message.
     *
     * If the target message is null, the new message will be at the end of the
     * lifeline.
     *
     * @param targetMsg The message that the new message will be relative to.
     *                  Can be null.
     * @param before if true, the new message will be before the target message.
     *               if false, the new message will be after the target message.
     */
    public void addCreateMessage(IMessageEdgeDrawEngine targetMsg, boolean before)
    {
        try
        {
            ETNode node = (ETNode)TypeConversions.getTSObject(this);
            double newNodeX = node.getLocalRight() + 20;
            
            ETPoint pt = new ETPoint((int)newNodeX, (int)node.getLocalTop());
            ETNode newNode = getDrawingArea().addNodeForType("Lifeline", pt, false, true);
            
            IDrawEngine targetEngine = TypeConversions.getDrawEngine((TSNode)newNode);
            if(targetEngine instanceof ILifelineDrawEngine)
            {
                ILifelineDrawEngine targetLifeline = (ILifelineDrawEngine)targetEngine;
                IADLifelineCompartment compartment = getLifelineCompartment();
                if(before == true)
                {
                    compartment.addMessageBefore(targetLifeline, targetMsg,
                            IMessageKind.MK_CREATE);
                }
                else
                {
                    compartment.addMessageAfter(targetLifeline, targetMsg,
                            IMessageKind.MK_CREATE);
                }
            }
        }
        catch (ETException ex)
        {
            ErrorManager.getDefault().notify(ex);
        }
    }
    
    protected void validateCreateMessageConnector()
    {
        if(m_ConnectorCreate != null)
        {
            TSDNode node = getOwnerNode();
            
            if(node!=null)
            {
                List connectorList = node.connectors();
                if(!connectorList.contains(m_ConnectorCreate))
                    m_ConnectorCreate = null;
            }
        }
    }
    
    public void initResources()
    {
        setFillColor("lifelinefill", 211, 227, 244);
        setLightGradientFillColor("lifelinelightgradientfill", 255, 255, 255);
        setBorderColor("lifelineborder", Color.BLACK);
        
        super.initResources();
    }
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#performDeepSynch()
    */
    public long performDeepSynch()
    {
        // this should be in ETDrawEngine.performDeepSynch, but the engines aren't implemented correctly
        IPresentationElement presentationElement = TypeConversions.getPresentationElement(this);
        if(presentationElement != null)
            initCompartments(presentationElement);
        
        validateNode();
        
        return 0;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#onDiscardParentETElement()
         */
    public void onDiscardParentETElement()
    {
        super.onDiscardParentETElement();
        // Stops the connectors from updating if our parent node is deleted from the graph.
        m_StickFigureCompartment = null;
        
        m_NameCompartment        = null;
        m_LifelineCompartment    = null;
        m_StereotypeCompartment  = null;
        m_ConnectorCreate = null;
        
        /// the minimum allowable rectangle for this container
        m_rectMinimumResize = null;
    }
    
}
