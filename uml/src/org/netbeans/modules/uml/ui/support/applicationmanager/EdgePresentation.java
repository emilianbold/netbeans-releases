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



package org.netbeans.modules.uml.ui.support.applicationmanager;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;

import com.tomsawyer.editor.ui.TSEEdgeUI;
import com.tomsawyer.editor.TSEEdge;
import com.tomsawyer.editor.TSENode;
import com.tomsawyer.editor.TSEObjectUI;
import com.tomsawyer.drawing.TSConnector;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationReference;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.ReconnectEdgeCreateConnectorKind;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEnd;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.products.ad.drawengines.INodeDrawEngine;
import org.netbeans.modules.uml.ui.products.ad.graphobjects.ETNode;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.IETGraphObjectUI;
import org.netbeans.modules.uml.ui.support.NodeEndKindEnum;
import org.netbeans.modules.uml.ui.support.PresentationReferenceHelper;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.swing.drawingarea.IReconnectEdgeContext;

/*
 *
 * @author KevinM
 *
 */
public class EdgePresentation extends ProductGraphPresentation implements IEdgePresentation
{
    
    private TSEEdge mEdge = null;
    
    /**
     * Default constructor.
     */
    public EdgePresentation()
    {
        super();
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.applicationmanager.IEdgePresentation#getEdgeUI()
         */
    public TSEEdgeUI getEdgeUI()
    {
        //return this.getUI() instanceof TSEEdgeUI ? (TSEEdgeUI) this.getUI() : null;
        TSEEdge edge = getTSEdge();
        
        TSEEdgeUI retVal = null;
        
        if(edge != null)
        {
            TSEObjectUI ui = edge.getUI();
            if(ui instanceof TSEEdgeUI)
            {
                retVal = (TSEEdgeUI)ui;
            }
        }
        
        return retVal;
    }
    
    public IETGraphObjectUI getUI()
    {
        TSEEdge edge = getTSEdge();
        
        IETGraphObjectUI retVal = null;
        
        if(edge != null)
        {
            TSEObjectUI ui = edge.getUI();
            if(ui instanceof IETGraphObjectUI)
            {
                retVal = (IETGraphObjectUI)ui;
            }
        }
        
        return retVal;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.support.applicationmanager.IEdgePresentation#getTSEdge()
     */
    public TSEEdge getTSEdge()
    {
        return mEdge;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.support.applicationmanager.IEdgePresentation#setTSEdge(null)
     */
    public void setTSEdge(TSEEdge newVal)
    {
        mEdge = newVal;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.support.applicationmanager.IEdgePresentation#discardAllBends()
     */
    public void discardAllBends()
    {
        TSEEdge edge = getTSEdge();
        if (edge != null)
        {
            // pTSEEdge->desiredRoutingStyle(TS_EDGE_ROUTING_STYLE_STRAIGHT);
            edge.discardAllPathNodes();
        }
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.applicationmanager.IEdgePresentation#removeConnectors()
         */
    public boolean removeConnectors()
    {
        TSEEdge pEdge = getTSEdge();
        boolean rc = false;
        if (pEdge != null)
        {
            TSConnector fromConnector = pEdge.getSourceConnector();
            TSConnector toConnector = pEdge.getTargetConnector();
            if (fromConnector != null)
            {
                TSENode owner = (TSENode) fromConnector.getOwner();
                pEdge.setSourceNode(owner);
                owner.discard(fromConnector);
                rc = true;
            }
            
            if (toConnector != null)
            {
                TSENode owner = (TSENode) toConnector.getOwner();
                pEdge.setTargetNode(owner);
                owner.discard(toConnector);
                rc = true;
            }
        }
        return rc;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.applicationmanager.IEdgePresentation#getEdgeFromAndToNode()
         */
    public ETPairT < IETGraphObject, IETGraphObject > getEdgeFromAndToNode()
    {
        return new ETPairT < IETGraphObject, IETGraphObject > (getFromGraphObject(), getToGraphObject());
    }
    
    public IETGraphObjectUI getFromNodeUI()
    {
        TSEEdge edge = getTSEdge();
        if (edge != null)
        {
            TSENode node = (TSENode) edge.getSourceNode();
            if (node != null)
            {
                return (IETGraphObjectUI) node.getUI();
            }
        }
        return null;
    }
    
    public IETGraphObjectUI getToNodeUI()
    {
        TSEEdge edge = getTSEdge();
        if (edge != null)
        {
            TSENode node = (TSENode) edge.getTargetNode();
            if (node != null)
            {
                return (IETGraphObjectUI) node.getUI();
            }
        }
        return null;
        
    }
        /*
         * Returns the from node.
         */
    public IElement getFromNode()
    {
        TSEEdge edge = getTSEdge();
        if (edge != null)
        {
            TSENode node = (TSENode) edge.getSourceNode();
            if (node != null)
            {
                IETGraphObjectUI nodeUI = (IETGraphObjectUI) node.getUI();
                return nodeUI != null ? nodeUI.getModelElement() : null;
            }
        }
        
        return null;
    }
    
    /**
     * Retrieves the graph object that edge originates from.
     */
    public IETGraphObject getFromGraphObject()
    {
        IETGraphObject retVal = null;
        
        TSEEdge edge = getTSEdge();
        if (edge != null)
        {
            TSENode node = (TSENode) edge.getSourceNode();
            if (node instanceof IETGraphObject)
            {
                retVal = (IETGraphObject) node;
            }
        }
        
        return retVal;
    }
    
        /*
         * Returns the to node
         */
    public IElement getToNode()
    {
        TSEEdge edge = getTSEdge();
        if (edge != null)
        {
            TSENode node = (TSENode) edge.getTargetNode();
            if (node != null)
            {
                IETGraphObjectUI nodeUI = (IETGraphObjectUI) node.getUI();
                return nodeUI != null ? nodeUI.getModelElement() : null;
            }
        }
        
        return null;
    }
    
    /**
     * Retrieves the graph object that edge originates from.
     */
    public IETGraphObject getToGraphObject()
    {
        IETGraphObject retVal = null;
        
        TSEEdge edge = getTSEdge();
        if (edge != null)
        {
            TSENode node = (TSENode) edge.getTargetNode();
            if (node instanceof IETGraphObject)
            {
                retVal = (IETGraphObject) node;
            }
        }
        
        return retVal;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.applicationmanager.IEdgePresentation#getOtherEnd(org.netbeans.modules.uml.ui.support.applicationmanager.INodePresentation)
         */
    public INodePresentation getOtherEnd(INodePresentation pOneNode)
    {
        TSEEdge edge = getTSEdge();
        if (pOneNode != null && edge != null)
        {
            TSENode node = pOneNode.getTSNode() == edge.getSourceNode() ? (TSENode) edge.getTargetNode() : (TSENode) edge.getSourceNode();
            return getNodePresenation(node);
        }
        return null;
    }
    
    protected INodePresentation getNodePresenation(TSENode node)
    {
        if (node != null && node.getUI() != null)
        {
            IETGraphObjectUI nodeUI = (IETGraphObjectUI) node.getUI();
            return (INodePresentation) nodeUI.getDrawEngine().getPresentation();
        }
        return null;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.applicationmanager.IEdgePresentation#getNodeEnd(null)
         */
    public int getNodeEnd(IElement pElement)
    {
        int nNodeEndKind = NodeEndKindEnum.NEK_UNKNOWN;
        if (pElement == null)
            return nNodeEndKind;
        
        try
        {
            IElement pElementToCheck = null;
            
            // See if the user passed in an AssociationEnd.  If so then grab the participant
            IAssociationEnd pAssociationEnd = pElement instanceof IAssociationEnd ? (IAssociationEnd) pElement : null;
            if (pAssociationEnd != null)
            {
                IClassifier pClassifier = pAssociationEnd.getParticipant();
                pElementToCheck = pClassifier;
            }
            
            if (pElementToCheck == null)
                pElementToCheck = pElement;
            
            IETGraphObject pSourceNode = this.getFromGraphObject();
            IETGraphObject pTargetNode = this.getToGraphObject();
            
            // Now the returned elements could be qualifiers so navigate past those to the
            // actual connected element
            if (pSourceNode != null)
            {
                IETGraphObject pNodeAttachedToQualifier = navigatePastQualifier2(pSourceNode);
                
                if (pNodeAttachedToQualifier != null)
                    pSourceNode = pNodeAttachedToQualifier;
            }
            
            if (pTargetNode != null)
            {
                IETGraphObject pNodeAttachedToQualifier = navigatePastQualifier2(pTargetNode);
                
                if (pNodeAttachedToQualifier != null)
                    pTargetNode = pNodeAttachedToQualifier;
            }
            
            if (pSourceNode != null && pTargetNode != null)
            {
                IPresentationElement pFromPresentationElement;
                IPresentationElement  pToPresentationElement;
                
                pFromPresentationElement = TypeConversions.getPresentationElement(pSourceNode);
                pToPresentationElement = TypeConversions.getPresentationElement(pTargetNode);
                
                boolean bIsFromElement = false;
                boolean bIsToElement = false;
                
                if (pFromPresentationElement != null && pToPresentationElement != null)
                {
                    bIsFromElement = pFromPresentationElement.isFirstSubject(pElementToCheck);
                    bIsToElement = pToPresentationElement.isFirstSubject(pElementToCheck);
                    
                    if (bIsFromElement && bIsToElement)
                    {
                        nNodeEndKind = NodeEndKindEnum.NEK_BOTH;
                    }
                    else if (bIsFromElement)
                    {
                        nNodeEndKind = NodeEndKindEnum.NEK_FROM;
                    }
                    else if (bIsToElement)
                    {
                        nNodeEndKind = NodeEndKindEnum.NEK_TO;
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return nNodeEndKind;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.applicationmanager.IEdgePresentation#getNodeEnd2(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEnd)
         */
    public int getNodeEnd2(IAssociationEnd pAssociationEnd)
    {
        int nNodeEndKind = NodeEndKindEnum.NEK_UNKNOWN;
        
        if (pAssociationEnd != null)
        {
            IClassifier pEndParticipant = pAssociationEnd.getParticipant();
            if (pEndParticipant != null)
            {
                nNodeEndKind = getNodeEnd(pEndParticipant);
            }
        }
        return nNodeEndKind;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.applicationmanager.IEdgePresentation#getNodeEnd3(java.lang.String)
         */
    public int getNodeEnd3(String elementXMIID)
    {
        int retVal = NodeEndKindEnum.NEK_UNKNOWN;
        if (elementXMIID == null || elementXMIID.length() == 0)
            return retVal;
        
        ETPairT < IETGraphObject, IETGraphObject > engines = getEdgeFromAndToNode();
        IETGraphObject sourceNode = engines.getParamOne();
        IETGraphObject targetNode = engines.getParamTwo();
        
        if (sourceNode != null)
        {
            IETGraphObject nodeAttachedToQualifier = navigatePastQualifier2(sourceNode);
            if (nodeAttachedToQualifier != null)
            {
                sourceNode = nodeAttachedToQualifier;
            }
        }
        if (targetNode != null)
        {
            IETGraphObject nodeAttachedToQualifier = navigatePastQualifier2(targetNode);
            if (nodeAttachedToQualifier != null)
            {
                targetNode = nodeAttachedToQualifier;
            }
        }
        
        if (sourceNode != null && targetNode != null)
        {
            IPresentationElement fromPresentationElement = TypeConversions.getPresentationElement(sourceNode);
            IPresentationElement toPresentationElement = TypeConversions.getPresentationElement(targetNode);
            
            if (fromPresentationElement != null && toPresentationElement != null)
            {
                boolean isFromElement = fromPresentationElement.isFirstSubject2(elementXMIID);
                boolean isToElement = toPresentationElement.isFirstSubject2(elementXMIID);
                
                if (isFromElement && isToElement)
                {
                    retVal = NodeEndKindEnum.NEK_BOTH;
                }
                else if (isFromElement)
                {
                    retVal = NodeEndKindEnum.NEK_FROM;
                }
                else if (isToElement)
                {
                    retVal = NodeEndKindEnum.NEK_TO;
                }
            }
        }
        
        return retVal;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.applicationmanager.IEdgePresentation#validateLinkEnds()
         */
    public boolean validateLinkEnds()
    {
        return true;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.applicationmanager.IEdgePresentation#reconnectLinkToValidNodes()
         */
    public boolean reconnectLinkToValidNodes()
    {
        return false;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.applicationmanager.IEdgePresentation#reconnectLink(org.netbeans.modules.uml.ui.swing.drawingarea.IReconnectEdgeContext)
         */
    public boolean reconnectLink(IReconnectEdgeContext pContext)
    {
        return false;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.applicationmanager.IEdgePresentation#autoRoute(boolean)
         */
    public boolean autoRoute(boolean bResetRoutingStyle)
    {
        
//            double zoom = getDrawingArea().getCurrentZoom();
        
        com.tomsawyer.editor.TSEGraphWindow graphWindow = this.getDrawingArea().getGraphWindow();
        com.tomsawyer.service.layout.jlayout.client.TSLayoutProxy proxy = new com.tomsawyer.service.layout.jlayout.client.TSLayoutProxy();
        
        com.tomsawyer.editor.service.TSEAllOptionsServiceInputData inputData = new com.tomsawyer.editor.service.TSEAllOptionsServiceInputData(graphWindow.getGraphManager());
        com.tomsawyer.service.layout.jlayout.TSLayoutInputTailor tailor = new com.tomsawyer.service.layout.jlayout.TSLayoutInputTailor(inputData);
        tailor.setGraphManager(graphWindow.getGraphManager());
        
        List edgeList = new ArrayList();
        edgeList.add(getTSEdge());
        RoutingCommandNoZoom routingCommand = new RoutingCommandNoZoom(graphWindow.getGraphManager(),
                proxy,
                inputData,
                edgeList);
        graphWindow.transmit(routingCommand);
        
//            getDrawingArea().zoom(zoom);
        return true;
    }
    
    public class RoutingCommandNoZoom extends com.tomsawyer.editor.command.TSERoutingCommand
    {
        public RoutingCommandNoZoom(com.tomsawyer.editor.TSEGraphManager graphManager,
                com.tomsawyer.service.client.TSServiceProxy proxy,
                com.tomsawyer.editor.service.TSEAllOptionsServiceInputData inputData,
                List edgeList)
        {
            super(graphManager, proxy, inputData, edgeList);
        }
        
        protected /* UNEXPOSED */ void postLayout()
        {
        }
    }
    
//	/* (non-Javadoc)
//	 * @see org.netbeans.modules.uml.ui.support.applicationmanager.IEdgePresentation#getOppositeRoutingStyle()
//	 */
//	public int getOppositeRoutingStyle() {
//		// TODO Auto-generated method stub
//		return 0;
//	}
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.applicationmanager.IEdgePresentation#getNodeNearestPoint(org.netbeans.modules.uml.core.support.umlsupport.IETPoint)
         */
    public ETPairT < INodePresentation, Integer > getNodeNearestPoint(IETPoint pPoint)
    {
        return null;
    }
    
//	/* (non-Javadoc)
//	 * @see org.netbeans.modules.uml.ui.support.applicationmanager.IEdgePresentation#getRoutingStyle()
//	 */
//	public int getRoutingStyle() {
//		// TODO Auto-generated method stub
//		return 0;
//	}
//
//	/* (non-Javadoc)
//	 * @see org.netbeans.modules.uml.ui.support.applicationmanager.IEdgePresentation#setRoutingStyle(int)
//	 */
//	public void setRoutingStyle(int nKind) {
//		// TODO Auto-generated method stub
//
//	}
    
    /**
     * Returns the from and to draw engines.
     */
    public ETPairT < IDrawEngine, IDrawEngine > getEdgeFromAndToDrawEngines()
    {
        return new ETPairT < IDrawEngine, IDrawEngine > (getEdgeFromDrawEngine(), getEdgeToDrawEngine());
    }
    
    /**
     * Returns the from and to node if the element is an edge.
     */
    public ETPairT < INodePresentation, INodePresentation > getEdgeFromAndToPresentationElement()
    {
        return new ETPairT < INodePresentation, INodePresentation > (getEdgeFromPresentationElement(), getEdgeToPresentationElement());
    }
    
    /**
     * Returns the from and to element.  If bUseProjectData is true then we use the project data,
     * otherwise we get the elements off the nodes.  They could be different if the diagram is
     * not synched with the model
     */
    public ETPairT < IElement, IElement > getEdgeFromAndToElement(boolean bUseProjectData)
    {
        return new ETPairT < IElement, IElement > (getEdgeFromElement(bUseProjectData), getEdgeToElement(bUseProjectData));
    }
    
    /**
     * Returns the from and to draw engines if they have the specific ids.
     */
    public ETPairT < IDrawEngine, IDrawEngine > getEdgeFromAndToDrawEnginesWithID(String sDrawEngineID)
    {
        return new ETPairT < IDrawEngine, IDrawEngine > (getEdgeFromDrawEngineWithID(sDrawEngineID), getEdgeToDrawEngineWithID(sDrawEngineID));
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.applicationmanager.IEdgePresentation#getEdgeFromDrawEngine()
         */
    public IDrawEngine getEdgeFromDrawEngine()
    {
        TSEEdge edge = getTSEdge();
        if (edge != null)
        {
            TSENode node = (TSENode) edge.getSourceNode();
            if (node != null)
            {
                IETGraphObjectUI nodeUI = (IETGraphObjectUI) node.getUI();
                return nodeUI != null ? nodeUI.getDrawEngine() : null;
            }
        }
        return null;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.applicationmanager.IEdgePresentation#getEdgeToDrawEngine()
         */
    public IDrawEngine getEdgeToDrawEngine()
    {
        TSEEdge edge = getTSEdge();
        if (edge != null)
        {
            TSENode node = (TSENode) edge.getTargetNode();
            if (node != null)
            {
                IETGraphObjectUI nodeUI = (IETGraphObjectUI) node.getUI();
                return nodeUI != null ? nodeUI.getDrawEngine() : null;
            }
        }
        return null;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.applicationmanager.IEdgePresentation#getEdgeFromElement()
         */
    public IElement getEdgeFromElement()
    {
        return getEdgeFromElement(false);
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.applicationmanager.IEdgePresentation#getEdgeFromElement(boolean)
         */
    public IElement getEdgeFromElement(boolean bUseProjectData)
    {
        TSEEdge edge = getTSEdge();
        if (edge != null)
        {
            TSENode node = (TSENode) edge.getSourceNode();
            if (node != null && node.getUI() instanceof IETGraphObjectUI)
            {
                IETGraphObjectUI nodeUI = (IETGraphObjectUI) node.getUI();
                return nodeUI != null ? nodeUI.getModelElement() : null;
            }
        }
        return null;
    }
    
    public ETNode getSourceNode()
    {
        TSEEdge pEdge = this.getTSEdge();
        return pEdge != null ? (ETNode) pEdge.getSourceNode() : null;
    }
    
    public ETNode getTargetNode()
    {
        TSEEdge pEdge = this.getTSEdge();
        return pEdge != null ? (ETNode) pEdge.getTargetNode() : null;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.applicationmanager.IEdgePresentation#getEdgeFromPresentationElement()
         */
    public INodePresentation getEdgeFromPresentationElement()
    {
        TSEEdge edge = getTSEdge();
        return edge != null ? getNodePresenation((TSENode) edge.getSourceNode()) : null;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.applicationmanager.IEdgePresentation#getEdgeToElement()
         */
    public IElement getEdgeToElement()
    {
        return getEdgeToElement(false);
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.applicationmanager.IEdgePresentation#getEdgeToElement(boolean)
         */
    public IElement getEdgeToElement(boolean bUseProjectData)
    {
        TSEEdge edge = getTSEdge();
        if (edge != null)
        {
            TSENode node = (TSENode) edge.getTargetNode();
            if (node != null && node.getUI() instanceof IETGraphObjectUI)
            {
                IETGraphObjectUI nodeUI = (IETGraphObjectUI) node.getUI();
                return nodeUI != null ? nodeUI.getModelElement() : null;
            }
        }
        return null;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.applicationmanager.IEdgePresentation#getEdgeToPresentationElement()
         */
    public INodePresentation getEdgeToPresentationElement()
    {
        TSEEdge edge = getTSEdge();
        return edge != null ? getNodePresenation((TSENode) edge.getTargetNode()) : null;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.applicationmanager.IEdgePresentation#getEdgeFromDrawEngineWithID(java.lang.String)
         */
    public IDrawEngine getEdgeFromDrawEngineWithID(String sDrawEngineID)
    {
        IDrawEngine pNodeDrawEngine = this.getEdgeFromDrawEngine();
        String drawEngineID = pNodeDrawEngine != null ? pNodeDrawEngine.getDrawEngineID() : null;
        return drawEngineID != null && drawEngineID.equals(sDrawEngineID) ? pNodeDrawEngine : null;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.applicationmanager.IEdgePresentation#getEdgeToDrawEngineWithID(java.lang.String)
         */
    public IDrawEngine getEdgeToDrawEngineWithID(String sDrawEngineID)
    {
        IDrawEngine pNodeDrawEngine = this.getEdgeToDrawEngine();
        String drawEngineID = pNodeDrawEngine != null ? pNodeDrawEngine.getDrawEngineID() : null;
        return drawEngineID != null && drawEngineID.equals(sDrawEngineID) ? pNodeDrawEngine : null;
    }
    
    /**
     * If the passed in object is a qualifier then this navigates to the parent referring element
     */
    protected IETGraphObject navigatePastQualifier2(IETGraphObject pPossibleQualifier)
    {
        if (pPossibleQualifier == null)
            return null;
        
        IDrawEngine drawEngine = TypeConversions.getDrawEngine(pPossibleQualifier);
        
        if (drawEngine != null)
        {
            String sID = drawEngine.getDrawEngineID();
            
            if (sID != null && sID.compareTo("QualifierDrawEngine") == 0)
            {
                IPresentationElement possibleQualifierPE = TypeConversions.getPresentationElement(pPossibleQualifier);
                
                if (possibleQualifierPE != null)
                {
                    ETList < IPresentationElement > parentElements = PresentationReferenceHelper.getAllReferencingElements(possibleQualifierPE);
                    
                    if (parentElements != null)
                    {
                        if (parentElements.getCount() > 0)
                        {
                            IPresentationElement referringElementPE = parentElements.item(0);
                            if (referringElementPE != null)
                            {
                                return TypeConversions.getETGraphObject(referringElementPE);
                            }
                        }
                    }
                }
            }
        }
        
        return null;
    }
    
    protected ETList < IPresentationElement > getAllReferredElements(IPresentationElement pReferencing)
    {
        if (pReferencing == null)
            return null;
        
        ETList < IPresentationElement > pReferredElements = null;
        try
        {
            pReferredElements = new ETArrayList < IPresentationElement > ();
            
            long count = 0;
            
            // Get all the elements off the referencing element
            ETList < IElement > pElements = pReferencing.getElements();
            if (pElements != null)
            {
                count = pElements.size();
            }
            
            // Gather up all the IPresentationReferences and get the ReferredElement which
            // is the presentation element for an IPresentationReference
            for (int i = 0; i < count; i++)
            {
                IElement pThisElement = pElements.get(i);
                
                // Get all the relationships
                IPresentationReference pThisReference = (IPresentationReference) pThisElement;
                
                if (pThisReference != null)
                {
                    IPresentationElement pPE = pThisReference.getPresentationElement();
                    if (pPE != null)
                        pReferredElements.add(pPE);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return pReferredElements;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.applicationmanager.IEdgePresentation#setReconnectConnectorFlag(org.netbeans.modules.uml.ui.swing.drawingarea.IReconnectEdgeContext)
         */
    public void setReconnectConnectorFlag(IReconnectEdgeContext pContext)
    {
        if (pContext == null)
            return;
        
        try
        {
            int nReconnect = ReconnectEdgeCreateConnectorKind.RECCK_DONT_CREATE;
            
            // The proposed, new node to take its place
            IETNode pNewNode = pContext.getProposedEndNode();
            if (pNewNode != null)
            {
                IDrawEngine pDrawEngine = TypeConversions.getDrawEngine(pNewNode);
                INodeDrawEngine pNodeDE = pDrawEngine instanceof INodeDrawEngine ? (INodeDrawEngine) pDrawEngine : null;
                if (pNodeDE != null)
                {
                    nReconnect = pNodeDE.getReconnectConnector(this);
                    
                    // Tell the context to allow reconnecting the connector.  This
                    // gets used in DiagramReconnectTool.finishReconnection which
                    // will create a new connector if specified.
                    pContext.setReconnectConnector(nReconnect);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    protected boolean reconnectSimpleLinkToValidNodes()
    {
        boolean bSuccessfullyReconnected = false;
        try
        {
            if (!isTernaryAssociation())
            {
                IDiagram pDiagram = getDiagram();
                
                if (pDiagram != null)
                {
                    IElement pEnd1 = getEdgeFromElement(true);
                    IElement pEnd2 = getEdgeToElement(true);
                    
                    //				   ?? =getEdgeFromAndToElement(true, &pEnd1, &pEnd2));
                    //
                    // Should I move the getting of the from and to elements to presentation helper
                    // or the core?
                    //
                    // Check the generalization link
                    
                    if (pEnd1 != null && pEnd2 != null)
                    {
                        // Find these items on the diagram
                        ETList < IPresentationElement > pEnd1PEs = pDiagram.getAllItems2(pEnd1);
                        ETList < IPresentationElement > pEnd2PEs = pDiagram.getAllItems2(pEnd2);
                        
                        long end1Count = pEnd1PEs != null ? pEnd1PEs.size() : 0;
                        long end2Count = pEnd2PEs != null ? pEnd2PEs.size() : 0;
                        
                        if (end1Count == 1 && end2Count == 1)
                        {
                            // Reconnect the link
                            IPresentationElement pEnd1PE = pEnd1PEs.get(0);
                            IPresentationElement pEnd2PE = pEnd2PEs.get(0);
                            
                            if (pEnd1PE != null && pEnd2PE != null)
                            {
                                bSuccessfullyReconnected = pDiagram.reconnectLink(this, pEnd1PE, pEnd2PE);
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return bSuccessfullyReconnected;
    }
    
    protected boolean isTernaryAssociation()
    {
        boolean bIsTernary = false;
        try
        {
            IAssociation pAssociation = getElement() instanceof IAssociation ? (IAssociation) getElement() : null;
            if (pAssociation != null)
            {
                if (pAssociation.getNumEnds() > 2)
                {
                    bIsTernary = true;
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return bIsTernary;
    }
    
    /**
     * Creates the class element node.
     *
     * @param doc The document owner of the new node
     * @param parent The parent of the new node
     */
    public void establishNodePresence(Document doc, Node parent)
    {
        buildNodePresence("UML:EdgePresentation", doc, parent);
    }
    
    /**
     * If the passed in object is a qualifier then this navigates to the parent referring element
     */
    public IPresentationElement navigatePastQualifier(IPresentationElement pPossibleQualifier)
    {
        IPresentationElement pReferringElement = null;
        try
        {
            IDrawEngine pDrawEngine = TypeConversions.getDrawEngine(pPossibleQualifier);
            
            if (pDrawEngine != null)
            {
                String sID = pDrawEngine.getDrawEngineID();
                
                if (sID != null && sID.equals("QualifierDrawEngine"))
                {
                    ETList < IPresentationElement > pParentElements = PresentationReferenceHelper.getAllReferencingElements(pPossibleQualifier);
                    
                    if (pParentElements != null)
                    {
                        long count = pParentElements.getCount();
                        
                        if (count > 0)
                        {
                            pReferringElement = pParentElements.item(0);
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return pReferringElement;
    }
    
    /**
     * Verifies that the link ends are correct for those items that only have a from and to IElement.
     * A Ternary association does not fall into this case.
     *
     * @param bIsValid[in]
     */
    public boolean validateSimpleLinkEnds()
    {
        boolean valid = false;
        boolean isTernary = isTernaryAssociation();
        if (!isTernary)
        {
            // Get the from and to node IElements
            IElement pSourceNodeEle = getEdgeFromElement(false);
            IElement pTargetNodeEle = getEdgeToElement(false);
            
            // Get the from and to IElements from the data file
            IElement pProjNodeSourceNodeEle = getEdgeFromElement(true);
            IElement pProjNodeTargetNodeEle = getEdgeToElement(true);
            
            // The link draw engines can draw with either node at either
            // end.  So we need to see if these two sets match either end.
            boolean bSourceSame = false;
            boolean bTargetSame = false;
            
            if (pSourceNodeEle != null && pTargetNodeEle != null &&
                    pProjNodeSourceNodeEle != null && pProjNodeTargetNodeEle != null)
            {
                bSourceSame = pProjNodeSourceNodeEle.isSame(pSourceNodeEle);
                bTargetSame = pProjNodeTargetNodeEle.isSame(pTargetNodeEle);
                
                if (!bSourceSame || !bTargetSame)
                {
                    bSourceSame = pProjNodeSourceNodeEle.isSame(pTargetNodeEle);
                    bTargetSame = pProjNodeTargetNodeEle.isSame(pSourceNodeEle);
                }
            }
            
            if (bSourceSame && bTargetSame)
            {
                valid = true;
            }
        }
        return valid;
    }
}
