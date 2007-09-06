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



package org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.primitivetypes.IMessageKind;
import org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETSize;
import org.netbeans.modules.uml.ui.controls.drawingarea.DiagramAreaEnumerations;
import org.netbeans.modules.uml.ui.products.ad.compartments.ETClassNameCompartment;
import org.netbeans.modules.uml.ui.products.ad.diagramengines.sequencediagram.ILifelineDrawEngine;
import org.netbeans.modules.uml.ui.support.applicationmanager.INodePresentation;
import org.netbeans.modules.uml.ui.support.applicationmanager.MoveToFlags;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ModelElementChangedKind;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.PresentationHelper;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import org.netbeans.modules.uml.ui.swing.trackbar.JTrackBar;
import com.tomsawyer.drawing.TSConnector;
import com.tomsawyer.editor.TSEConnector;

/**
 *
 * @author Trey Spiva
 */
public class ETLifelineNameCompartment extends ETClassNameCompartment
        implements ILifelineNameCompartment, IConnectorsCompartment
{
    private String m_RepresentingMetaType = "";
    
    public ETLifelineNameCompartment()
    {
        setName(" : ");
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.products.ad.compartments.ILifelineNameCompartment#setRepresentsMetaType(java.lang.String)
    */
    public void setRepresentsMetaType(String value)
    {
        m_RepresentingMetaType = value;
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.products.ad.compartments.ILifelineNameCompartment#getRepresentsMetaType()
    */
    public String getRepresentsMetaType()
    {
        return m_RepresentingMetaType;
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#getCompartmentID()
    */
    public String getCompartmentID()
    {
        return "LifelineNameCompartment";
    }
    
    /**
     * Calculates the "best" size for this compartment.  The calculation sets
     * the member variable GetCachedOptimumSize(), which represents the "best"
     * size of the compartment at 100%.
     *
     * @param pDrawInfo The draw information.
     * @param bAt100Pct nX,nY is either in current zoom or 100% based on this
     *                  flag.  If bAt100Pct then it's at 100%
     * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#calculateOptimumSize(org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo, boolean)
     */
    public IETSize calculateOptimumSize(IDrawInfo pDrawInfo, boolean bAt100Pct)
    {
        // Apply the scale at the end.
        IETSize retVal = super.calculateOptimumSize(pDrawInfo, true);
        
        // might be zero if we're hidden or no name set
        IETSize cachedSize = getCachedOptimumSize();
        if((cachedSize != null) &&
                (cachedSize.getWidth() > 0) &&
                (cachedSize.getHeight() > 0 ))
        {
            // Fix W1841:  Increase the size of the lifeline name compartment
            // Careful, increasing these values may corrupt the sequence diagram
            // generation process.
            int w = cachedSize.getWidth() + 4;
            int h = cachedSize.getHeight() + 4;
            
            // Fix W1827:  make sure the lifeline head's width is at least twice
            // the height
            w = Math.max(w, h * 2);
            
            cachedSize.setSize(w, h);
            internalSetOptimumSize(cachedSize);
        }
        
        return bAt100Pct ? m_cachedOptimumSize : scaleSize(m_cachedOptimumSize, pDrawInfo != null ? pDrawInfo.getTSTransform() : getTransform());
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#modelElementHasChanged(org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets)
    */
    public long modelElementHasChanged(INotificationTargets pTargets)
    {
        long retVal = 0;
        
        IElement modelElement = null;
        if(pTargets != null)
        {
            int kind = pTargets.getKind();
            if((kind == ModelElementChangedKind.MECK_NAMEMODIFIED) ||
                    (kind == ModelElementChangedKind.MECK_REPRESENTINGCLASSIFIERCHANGED) ||
                    (kind == ModelElementChangedKind.MECK_ALIASNAMEMODIFIED))
            {
                modelElement = pTargets.getChangedModelElement();
            }
        }
        
        boolean isLifeline = (modelElement instanceof ILifeline);
        boolean isClassifier = (modelElement instanceof IClassifier);
        
        if((isLifeline == true) || (isClassifier == true))
        {
            reattach();
            
            // Update the diagram's trackbar, and the draw engine's size
            IDrawEngine engine = getEngine();
            if(engine != null)
            {
                engine.sizeToContents();
                IPresentationElement element = TypeConversions.getPresentationElement(engine);
                
                IDrawingAreaControl ctrl = getDrawingArea();
                if(ctrl != null)
                {
                    ctrl.postSimplePresentationDelayedAction(element, DiagramAreaEnumerations.SPAK_UPDATE_TRACKBAR);
                    ctrl.postSimplePresentationDelayedAction(element, DiagramAreaEnumerations.SPAK_VALIDATENODE);
                }
                
            }
        }
        
        return retVal;
    }
    
    //**************************************************
    // IConnectorsCompartment implementation
    //**************************************************
    
    /**
     * Indicates that a message edge can be started from the current logical location.
     *
     * @param  ptLogical[in] Logical view coordinates to test
     * @return TRUE if the location is a place where a message can be started
     */
    public boolean canStartMessage( IETPoint ptLogical )
    {
        return false;
    }
    
    /**
     * Indicates that a message edge can be finished from the current logical location.
     *
     * @param ptLogical[in] Logical view coordinates to test
     * @return TRUE if the location is a place where a message can be finished
     */
    public boolean canFinishMessage( IETPoint ptLogical )
    {
        return true;
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.products.ad.compartments.IConnectorsCompartment#connectMessage(org.netbeans.modules.uml.core.support.umlsupport.IETPoint, int, int)
    */
    public TSEConnector connectMessage(IETPoint pPoint, int kind, int connectMessageKind, TSEConnector connector)
    {
        if( IMessageKind.MK_CREATE == kind )
        {
            // TODO: handle the create message
        }
        else if( m_engine != null )
        {
            IADLifelineCompartment cpLifelineCompartment = (IADLifelineCompartment)TypeConversions.getCompartment( m_engine, IADLifelineCompartment.class );
            if( cpLifelineCompartment instanceof IConnectorsCompartment )
            {
                IConnectorsCompartment  connectorsCompartment = (IConnectorsCompartment) cpLifelineCompartment;
                
                connector = connectorsCompartment.connectMessage( pPoint, kind, connectMessageKind, connector );
            }
        }
        
        return connector;
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.products.ad.compartments.IConnectorsCompartment#updateConnectors(org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo)
    */
    public void updateConnectors(IDrawInfo pDrawInfo)
    {
        if( m_engine instanceof ILifelineDrawEngine )
        {
            ILifelineDrawEngine lifelineDE = (ILifelineDrawEngine)m_engine;
            if( lifelineDE != null )
            {
                TSConnector cpDEConnector = lifelineDE.getConnectorForCreateMessage();
                if( cpDEConnector != null )
                {
                    // Determine the connector on the other end of the edge
                    TSConnector cpOtherConnector = PresentationHelper.getConnectorOnOtherEndOfEdge( cpDEConnector, false );
                    if( cpOtherConnector != null )
                    {
                        double y = cpOtherConnector.getCenterY();
                        moveConnector( cpDEConnector, y, false, false );
                    }
                }
            }
        }
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.products.ad.compartments.IConnectorsCompartment#moveConnector(com.tomsawyer.editor.TSEConnector, double, boolean, boolean)
    */
    public void moveConnector(TSConnector connector, double nY, boolean bDoItNow, boolean bSetYOfAssociatedPiece)
    {
        // The only connector that will be attached to the lifeline name compartment is the create message.
        // Therefore, just move the lifeline itself.
        
        final double lDy = nY - connector.getCenterY();
        if( Math.abs( lDy ) > 0.5 )
        {
            if( m_engine != null )
            {
                IPresentationElement presentationElement =
                        TypeConversions.getPresentationElement( m_engine );
                if( presentationElement instanceof INodePresentation )
                {
                    INodePresentation nodePresentation = (INodePresentation)presentationElement;
                    
                    IETRect rectBounding = TypeConversions.getLogicalBoundingRect( nodePresentation );
                    
                    final int lNewY = (int)(rectBounding.getCenterY() + lDy);
                    nodePresentation.moveTo( 0, lNewY, (int)(MoveToFlags.MTF_MOVEY | MoveToFlags.MTF_LOGICALCOORD));
                }
            }
        }
    }
    
}
