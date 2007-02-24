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


package org.netbeans.modules.uml.ui.products.ad.graphobjects;

import org.netbeans.modules.uml.common.ETSystem;
import org.netbeans.modules.uml.common.ETException;
import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.common.generics.IteratorT;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramValidateKind;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramValidateResult;
import org.netbeans.modules.uml.core.metamodel.diagrams.IGraphObjectValidation;
import org.netbeans.modules.uml.core.metamodel.diagrams.ISynchStateKind;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IStrings;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.controls.drawingarea.IUIDiagram;
import org.netbeans.modules.uml.ui.products.ad.application.IMenuManager;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.ETBaseUI;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericEdgeUI;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.IETGraphObjectUI;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge;
import org.netbeans.modules.uml.ui.support.applicationmanager.IEdgePresentation;
import org.netbeans.modules.uml.ui.support.applicationmanager.IGraphPresentation;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProductGraphPresentation;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchive;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETEdge;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETLabel;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IEventManager;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ITSGraphObject;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.swing.drawingarea.ADGraphWindow;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;

import com.tomsawyer.drawing.TSConnector;
import com.tomsawyer.drawing.TSEdgeLabel;
import com.tomsawyer.drawing.TSPNode;
import com.tomsawyer.editor.TSENode;
import com.tomsawyer.editor.TSEEdge;
import com.tomsawyer.editor.TSEGraph;
import com.tomsawyer.editor.TSEGraphWindow;
import com.tomsawyer.editor.TSEObject;
import com.tomsawyer.editor.TSEObjectUI;
//import com.tomsawyer.editor.state.TSEMoveSelectedState;
import com.tomsawyer.editor.tool.TSEMoveSelectedTool;
//import com.tomsawyer.editor.state.TSEReconnectEdgeState;
import com.tomsawyer.editor.tool.TSEReconnectEdgeTool;
import com.tomsawyer.editor.ui.TSEEdgeUI;
import com.tomsawyer.graph.TSGraphObject;
import com.tomsawyer.graph.TSNode;
//import com.tomsawyer.util.TSConstPoint;
import com.tomsawyer.drawing.geometry.TSConstPoint;
import com.tomsawyer.util.TSObject;
//import com.tomsawyer.util.TSPoint;
import com.tomsawyer.drawing.geometry.TSPoint;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.tomsawyer.editor.ui.TSEDefaultEdgeUI;

public class ETEdge extends TSEEdge implements IETEdge
{
    //IGraphPresentation m_presentation;
    private IEdgePresentation m_presentation;
    int mSynchState = ISynchStateKind.SSK_UNKNOWN_SYNCH_STATE;
    /**
     * Constructor of the class. This constructor should be implemented
     * to enable <code>TSEEdge</code> inheritance.
     */
    protected ETEdge()
    {
        super();
        m_presentation = null;
    }
/* jyothi wrote this..
        protected TSEDefaultEdgeUI newEdgeUI() {
            return (new ETGenericEdgeUI());
        }
 */
        /*
        protected TSENodeUI newNodeUI() {
            return (new TSEOvalNodeUI());
        }
         */
    /**
     * This method allocates a new label for this edge. This method
     * should be implemented to enable <code>TSEEdgeLabel</code>
     * inheritance.
     *
     * @return an object of the type derived from <code>TSEEdgeLabel</code>
     */
    protected TSEdgeLabel newLabel()
    {
        ETSystem.out.println("Inside ETEdge.newLabel()");
        return (new ETEdgeLabel());
    }
    
    /**
     * This method returns a new path node assignable to this edge.
     * This method should be implemented to enable
     * <code>TSEPNode</code> inheritance.
     *
     * @return
     *		an object of the type derived from <code>TSEPNode</code>
     */
    protected TSPNode newPathNode()
    {
        return (new ETPNode());
    }
    
    /**
     * This method copies attributes of the source object to this
     * object. The source object has to be of the type compatible with
     * this class (equal or derived). The method should make a deep
     * copy of all instance variables declared in this class. Variables
     * of simple (non-object) types are automatically copies by the
     * call to the copy method of the super class.
     *
     * @param sourceObject  the source from which all attributes must
     *                      be copied
     */
    public void copy(Object sourceObject)
    {
        setPresentationElement(null);
        // copy the attributes of the super class first
        super.copy(sourceObject);
        
        // copy any class specific attributes here
        // ...
        if (sourceObject instanceof ITSGraphObject)
        {
            this.copy((ITSGraphObject)sourceObject);
        }
        
        // Copy the diagram
        if( sourceObject instanceof ETEdge )
        {
            ETEdge oldetedge = (ETEdge)sourceObject;
            getETUI().setDrawingArea( oldetedge.getETUI().getDrawingArea() );
        }
        
        IElement modelElement = TypeConversions.getElement((TSObject)sourceObject);
        if (modelElement != null)
        {
            ETBaseUI.attachAndCreatePresentationElement(modelElement, getETUI().getInitStringValue(), false, getETUI());
        }
    }
    
    // add class-specific methods, instance and class variables
    // ...
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#affectModelElementDeletion()
         */
    public void affectModelElementDeletion()
    {
        IETGraphObjectUI ui = this.getETUI();
        if(ui != null)
        {
            IDrawEngine de = ui.getDrawEngine();
            if (de != null)
            {
                de.affectModelElementDeletion();
            }
        }
    }
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#getDiagram()
         */
    public IDiagram getDiagram()
    {
        IDrawingAreaControl da = getDrawingAreaControl();
        return da != null ? da.getDiagram() : null;
    }
    
    public IDrawingAreaControl getDrawingAreaControl()
    {
        return getETUI() != null ? getETUI().getDrawingArea() : null;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#getEngine()
         */
    public IDrawEngine getEngine()
    {
        return getETUI() != null ? getETUI().getDrawEngine() : null;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#getObjectView()
         */
    public TSEObjectUI getObjectView()
    {
        return super.getUI();
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#getPresentationElement()
         */
    public IPresentationElement getPresentationElement()
    {
        return m_presentation;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#getReferredElements()
         */
    public IStrings getReferredElements()
    {
        IETGraphObjectUI ui = getETUI();
        if (ui != null)
        {
            return ui.getReferredElements();
        }
        // TODO Auto-generated method stub
        return null;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#getReloadedModelElementXMIID()
         */
    public String getReloadedModelElementXMIID()
    {
        IETGraphObjectUI ui = getETUI();
        if (ui != null)
        {
            return ui.getReloadedModelElementXMIID();
        }
        // TODO Auto-generated method stub
        return null;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#getReloadedOwnerPresentationXMIID()
         */
    public String getReloadedOwnerPresentationXMIID()
    {
        IETGraphObjectUI ui = getETUI();
        if (ui != null)
        {
            return ui.getReloadedOwnerPresentationXMIID();
        }
        // TODO Auto-generated method stub
        return null;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#getReloadedPresentationXMIID()
         */
    public String getReloadedPresentationXMIID()
    {
        IETGraphObjectUI ui = getETUI();
        if (ui != null)
        {
            return ui.getReloadedPresentationXMIID();
        }
        // TODO Auto-generated method stub
        return null;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#getReloadedTopLevelXMIID()
         */
    public String getReloadedTopLevelXMIID()
    {
        IETGraphObjectUI ui = getETUI();
        if (ui != null)
        {
            return ui.getReloadedTopLevelXMIID();
        }
        // TODO Auto-generated method stub
        return null;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#getTopLevelXMIID()
         */
    public String getTopLevelXMIID()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#load(org.netbeans.modules.uml.ui.support.archivesupport.IProductArchive)
         */
    public void load(IProductArchive pProductArchive)
    {
        //implemented from BaseGraphObject.cpp
        if (pProductArchive != null)
        {
            String reloadedXMIID = getReloadedPresentationXMIID();
            IProductArchiveElement foundEle = null;
            if (reloadedXMIID.length() > 0)
            {
                foundEle = pProductArchive.getElement(reloadedXMIID);
            }
            if (foundEle != null)
            {
                readFromArchive(pProductArchive, foundEle);
            }
        }
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#modelElementDeleted(org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets)
         */
    public void modelElementDeleted(INotificationTargets pTargets)
    {
        IDrawEngine pEng = getETUI().getDrawEngine();
        if (pEng != null)
        {
            pEng.modelElementDeleted(pTargets);
        }
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#modelElementHasChanged(org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets)
         */
    public void modelElementHasChanged(INotificationTargets pTargets)
    {
        IDrawEngine pEng = getETUI().getDrawEngine();
        if (pEng != null)
        {
            pEng.modelElementHasChanged(pTargets);
            
            /// Now tell the label manager
            ILabelManager labelMgr = pEng.getLabelManager();
            if (labelMgr != null)
            {
                labelMgr.modelElementHasChanged(pTargets);
            }
        }
        
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#onGraphEvent(int)
         */
    public void onGraphEvent(int nKind)
    {
        ETBaseUI.onGraphEvent(nKind,getETUI());
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#onKeydown(int, int)
         */
    public boolean onKeydown(int nKeyCode, int nShift)
    {
        return ETBaseUI.onKeyDown(nKeyCode, nShift, getETUI());
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#onCharTyped(char)
         */
    public boolean onCharTyped(char ch)
    {
        return ETBaseUI.onCharTyped(ch, getETUI());
    }

        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#onKeyup(int, int)
         */
    public boolean onKeyup(int KeyCode, int Shift)
    {
        // TODO Auto-generated method stub
        return false;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#postLoad()
         */
    public void postLoad()
    {
        
        // This call will allow the draw engine
        // to process information after all objects have been loaded.
        IDrawEngine engine = getEngine();
        if (engine != null)
        {
            engine.postLoad();
        }
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#readData(int)
         */
    public void readData(int pTSEData)
    {
        // TODO Auto-generated method stub
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#readFromArchive(org.netbeans.modules.uml.ui.support.archivesupport.IProductArchive, org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement)
         */
    public void readFromArchive(IProductArchive prodArch, IProductArchiveElement archEle)
    {
        if (prodArch != null && archEle != null)
        {
            TSEObjectUI ui = getUI();
            if (ui != null && ui instanceof ETGenericEdgeUI)
            {
                ((ETGenericEdgeUI)ui).readFromArchive(prodArch, archEle);
            }
        }
    }
    
    /**
     * Saves this element to the etlp file
     *
     * @param pProductArchive [in] The archive file we're serializing to.
     */
    public void save(IProductArchive prodArch)
    {
        TSEObjectUI ui = getUI();
        if (ui != null && ui instanceof IETGraphObjectUI)
        {
            ETBaseUI.save(prodArch, (IETGraphObjectUI)ui);
        }
    }
    
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#setDiagram(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram)
         */
    public void setDiagram(IDiagram value)
    {
        // TODO Auto-generated method stub
        
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#setObjectView(com.tomsawyer.editor.TSEObjectUI)
         */
    public void setObjectView(TSEObjectUI value)
    {
        setUI(value);
    }
    
    public void setUI(TSEObjectUI ui)
    {
        super.setUI(ui);
        // Keep the presentation element n'sync.
        if (ui instanceof IETGraphObjectUI || ui == null)
        {
            IPresentationElement pe = this.getPresentationElement();
            IGraphPresentation graphPE = pe instanceof IGraphPresentation ? (IGraphPresentation) pe : null;            
        }
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#setPresentationElement(org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement)
         */
    public void setPresentationElement(IPresentationElement value)
    {
        m_presentation = value instanceof IEdgePresentation ? (IEdgePresentation) value : null;
        // Make sure the back pointer is in sync
        if (m_presentation != null && getETUI() != null)
        {
            m_presentation.setTSEdge(this);
        }
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#setReferredElements(org.netbeans.modules.uml.core.support.umlsupport.IStrings)
         */
    public void setReferredElements(IStrings value)
    {
        IETGraphObjectUI ui = getETUI();
        if (ui != null)
        {
            ui.setReferredElements(value);
        }
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#setReloadedModelElementXMIID(java.lang.String)
         */
    public void setReloadedModelElementXMIID(String value)
    {
        IETGraphObjectUI ui = getETUI();
        if (ui != null)
        {
            ui.setReloadedModelElementXMIID(value);
        }
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#setReloadedOwnerPresentationXMIID(java.lang.String)
         */
    public void setReloadedOwnerPresentationXMIID(String value)
    {
        IETGraphObjectUI ui = getETUI();
        if (ui != null)
        {
            ui.setReloadedOwnerPresentationXMIID(value);
        }
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#setReloadedPresentationXMIID(java.lang.String)
         */
    public void setReloadedPresentationXMIID(String value)
    {
        IETGraphObjectUI ui = getETUI();
        if (ui != null)
        {
            ui.setReloadedPresentationXMIID(value);
        }
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#setReloadedTopLevelXMIID(java.lang.String)
         */
    public void setReloadedTopLevelXMIID(String value)
    {
        IETGraphObjectUI ui = getETUI();
        if (ui != null)
        {
            ui.setReloadedTopLevelXMIID(value);
        }
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#sizeToContents()
         */
    public void sizeToContents()
    {
        IETGraphObjectUI ui = this.getETUI();
        IDrawEngine de = ui != null ? ui.getDrawEngine() : null;
        if (de != null)
        {
            de.sizeToContents();
        }
    }
    
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#writeData(int)
         */
    public void writeData(int pTSEDataMgr)
    {
        // TODO Auto-generated method stub
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#writeToArchive(org.netbeans.modules.uml.ui.support.archivesupport.IProductArchive, org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement)
         */
    public void writeToArchive(IProductArchive prodArch, IProductArchiveElement archEle)
    {
        if (prodArch != null && archEle != null)
        {
            TSEObjectUI ui = getUI();
            if (ui != null && ui instanceof ETGenericEdgeUI)
            {
                ((ETGenericEdgeUI)ui).writeToArchive(prodArch, archEle);
            }
        }
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ITSGraphObject#getETUI()
         */
    public IETGraphObjectUI getETUI()
    {
        return super.getUI() instanceof IETGraphObjectUI ? (IETGraphObjectUI)super.getUI() : null;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ITSGraphObject#getObject()
         */
    public TSEObject getObject()
    {
        return this;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ITSGraphObject#isConnector()
         */
    public boolean isConnector()
    {
        return false;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ITSGraphObject#isEdge()
         */
    public boolean isEdge()
    {
        return true;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ITSGraphObject#isLabel()
         */
    public boolean isLabel()
    {
        return false;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ITSGraphObject#isNode()
         */
    public boolean isNode()
    {
        return false;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ITSGraphObject#isPathNode()
         */
    public boolean isPathNode()
    {
        return false;
    }
    
    public void setText(Object text)
    {
        super.setTag(text);
    }
    
    public IETEdge createEdgeCopy(IDiagram pTargetDiagram, IETPoint pCenter, IPresentationElement pNewSourceNode, IPresentationElement pNewTargetNode)
    {
        IETEdge pCopy = null;
        
        long centerX = pCenter.getX();
        long centerY = pCenter.getY();
        
        TSENode pSourceNode = TypeConversions.getOwnerNode(pNewSourceNode);
        TSENode pTargetNode = TypeConversions.getOwnerNode(pNewTargetNode);
        
        if (pSourceNode != null && pTargetNode != null)
        {
            IDrawingAreaControl pControl = null;
            TSEGraph pTSGraph = null;
            
            if (pTargetDiagram != null)
            {
                IUIDiagram uiDiagram = null;
                if(pTargetDiagram instanceof IUIDiagram)
                    uiDiagram = (IUIDiagram)pTargetDiagram;
                
                if (uiDiagram!=null)
                {
                    pControl = uiDiagram.getDrawingArea();
                    if (pControl != null)
                    {
                        pTSGraph = pControl.getCurrentGraph();
                    }
                }
            }
            
            // Our only edge is a CRelationEdge, so make sure we're of that type right now
            if (getEdgeUI() instanceof RelationEdge)
            {
                RelationEdge currentUI = (RelationEdge)getEdgeUI();
                
                // Change the init string to a known ADViewFactory type and create a new node
                TSEEdge pCreatedEdge = null;
                
                try
                {
                    pCreatedEdge = (TSEEdge)pControl.addEdge(currentUI.getInitStringValue(),
                            pSourceNode, pTargetNode, false, false, TypeConversions.getElement((IETGraphObject)this));
                }
                catch(ETException e)
                {
                    e.printStackTrace();
                }
                
                if ( pCreatedEdge!= null )
                {
                    // Convert the created edge to our type so we can do an operator=
                    TSEEdgeUI pCreatedEdgeUI = pCreatedEdge.getEdgeUI();
                    pCreatedEdgeUI.copy(getEdgeUI());
                    pCreatedEdgeUI.setOwner(pCreatedEdge);
                    
                    // Set the drawing area backpointer
                    IETGraphObject pETGraphObject = TypeConversions.getETGraphObject(pCreatedEdgeUI);
                    
                    pETGraphObject.setDiagram(pTargetDiagram);
                    if(pETGraphObject instanceof IETEdge)
                        pCopy = (IETEdge)pETGraphObject;
                    
                    // See what the delta is between the just created edge's source point and pCenter
                    // where the new source should be
                    double deltaX = centerX - getSourceX();
                    double deltaY = centerY - getSourceY();
                    
                    List oldBendList = bendPoints();
                    List newBendList = new ArrayList();
                    
                    for(int index = 0; index < oldBendList.size(); index++)
                    {
                        TSConstPoint oldPoint = (TSConstPoint)oldBendList.get(index);
                        
                        TSPoint newPoint = new TSPoint(oldPoint.getX()+deltaX,oldPoint.getY());
                        newBendList.add(newPoint);
                    }
                    
                    pCreatedEdge.reroute(newBendList);
                }
            }
        }
        return pCopy;
    }
    
        /*
         * Returns the set of labels.
         */
    public ETList<IETLabel> getLabels()
    {
        return getLabels(true, true);
    }
    
        /*
         * Returns the set of labels.
         */
    public ETList<IETLabel> getLabels(boolean includeSelected, boolean includeNonselected)
    {
        List labels = this.labels();
        if (labels != null && labels.size() > 0)
        {
            ETList<IETLabel> filteredLabels = new ETArrayList<IETLabel>();
            IteratorT<IETLabel> iter = new IteratorT<IETLabel>(labels);
            while (iter.hasNext())
            {
                IETLabel label = iter.next();
                boolean isSelected = label.isSelected();
                if ((includeSelected && isSelected) || (includeNonselected && !isSelected))
                {
                    filteredLabels.add(label);
                }
            }
            
            return filteredLabels.size() > 0 ? filteredLabels : null;
        }
        return null;
    }
    
        /*
         * The drawing area control doesn't delete non selected items, its our job to fire the delete notifications.
         */
    protected void deleteNonSelectedLabels()
    {
        ETList<IETLabel> labels = getLabels(false,true);
        if (labels != null)
        {
            Iterator<IETLabel> iter = labels.iterator();
            while (iter.hasNext())
            {
                iter.next().delete();
            }
        }
    }
    
    public void delete()
    {
        this.deleteNonSelectedLabels();
        TSEEdgeUI ui = this.getUI() instanceof TSEEdgeUI ? (TSEEdgeUI)getUI() : null;
        this.invalidate();
        IDrawEngine drawEngine = this.getEngine();
        if (drawEngine != null)
        {
            drawEngine.onDiscardParentETElement();
        }
        getOwnerGraph().discard(this);
        if (ui != null)
        {
            ui.setOwner(null);
        }
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.products.ad.viewfactory.IETEdge#getFromNode()
         */
    public IETNode getFromNode()
    {
        return getSourceNode() instanceof IETNode ? (IETNode)getSourceNode() : null;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.products.ad.viewfactory.IETEdge#getFromObject()
         */
    public ITSGraphObject getFromObject()
    {
        TSConnector connector = getSourceConnector();
        return connector instanceof ITSGraphObject ? (ITSGraphObject)connector : getFromNode();
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.products.ad.viewfactory.IETEdge#getToNode()
         */
    public IETNode getToNode()
    {
        return getTargetNode() instanceof IETNode ? (IETNode)getTargetNode() : null;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.products.ad.viewfactory.IETEdge#getToObject()
         */
    public ITSGraphObject getToObject()
    {
        TSConnector connector = getTargetConnector();
        return connector instanceof ITSGraphObject ? (ITSGraphObject)connector : getToNode();
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ITSGraphObject#copy(org.netbeans.modules.uml.ui.support.viewfactorysupport.ITSGraphObject)
         */
    public void copy(ITSGraphObject objToClone)
    {
        // TODO Auto-generated method stub
        
    }
    
    public void onContextMenu(IMenuManager manager)
    {
        ETBaseUI.onContextMenu(manager, getETUI());
    }
    
    public IElement create(INamespace space, String initStr)
    {
        return ETBaseUI.create(space, initStr, getETUI());
    }
    
    public void attach(IElement modEle, String initStr)
    {
        ETBaseUI.attach(modEle, initStr, getETUI());
    }
    
    public void onPostAddLink(IETGraphObject newLink, boolean isFromNode)
    {
        ETBaseUI.onPostAddLink(newLink, isFromNode, getETUI());
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#create(org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace, java.lang.String, org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IElement)
         */
    public void create(INamespace pNamespace, String sInitializationString, IPresentationElement pCreatedPresentationElement, IElement pCreatedElement)
    {
        // TODO Auto-generated method stub
        
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#getGraphObject()
         */
    public TSGraphObject getGraphObject()
    {
        return this;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#getInitializationString()
         */
    public String getInitializationString()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#getOLEDragElements(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement[])
         */
    public ETList <IElement> getDragElements()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#getSynchState()
         */
    public int getSynchState()
    {
        return mSynchState;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#getWasModelElementDeleted()
         */
    public boolean getWasModelElementDeleted()
    {
        // this used to check an attribute, but per Pat, that is soon to be obsolete
        return TypeConversions.getElement((IETGraphObject)this)==null;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#handleAccelerator(int)
         */
    public boolean handleAccelerator(String accelerator)
    {
        boolean bHandled = false;
        
        IETGraphObjectUI ui = this.getETUI();
        if (ui != null)
        {
            IDrawEngine de = ui.getDrawEngine();
            if (de != null)
            {
                bHandled = de.handleAccelerator(accelerator);
            }
        }
        
        return bHandled;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#handleLeftMouseBeginDrag(org.netbeans.modules.uml.core.support.umlsupport.IETPoint, org.netbeans.modules.uml.core.support.umlsupport.IETPoint)
         */
    public boolean handleLeftMouseBeginDrag(IETPoint pStartPos, IETPoint pCurrentPos)
    {
        // TODO Auto-generated method stub
        return false;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#handleLeftMouseDrag(org.netbeans.modules.uml.core.support.umlsupport.IETPoint, org.netbeans.modules.uml.core.support.umlsupport.IETPoint)
         */
    public boolean handleLeftMouseDrag(IETPoint pStartPos, IETPoint pCurrentPos)
    {
        // TODO Auto-generated method stub
        return false;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#handleLeftMouseDrop(org.netbeans.modules.uml.core.support.umlsupport.IETPoint, org.netbeans.modules.uml.core.metamodel.core.foundation.IElement[], boolean)
         */
    public boolean handleLeftMouseDrop(IETPoint ptCurrentPos, IElement[] pElements, boolean bMoving)
    {
        // TODO Auto-generated method stub
        return false;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#invalidate()
         */
    public void invalidate()
    {
        IDrawingAreaControl ctrl = getDrawingAreaControl();
        
        if (ctrl == null)
            return;
        
        TSEGraphWindow window = ctrl.getGraphWindow();
        if (window != null && this.getUI() != null && getUI().getOwner() == this)
        {
            window.addInvalidRegion(this);
        }
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#onContextMenu(org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu, int, int)
         */
    public void onContextMenu(IProductContextMenu pContextMenu, int logicalX, int logicalY)
    {
        // TODO Auto-generated method stub
        
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#onContextMenuHandleSelection(org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu, org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem)
         */
    public void onContextMenuHandleSelection(IProductContextMenu pContextMenu, IProductContextMenuItem pMenuItem)
    {
        // TODO Auto-generated method stub
        
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#onPreDeleteLink(org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject, boolean)
         */
    public long onPreDeleteLink(IETGraphObject pLinkAboutToBeDeleted, boolean bIsFromNode)
    {
        
        IPresentationElement element = getPresentationElement();
        if (element instanceof IProductGraphPresentation)
        {
            IProductGraphPresentation presentation = (IProductGraphPresentation)element;
            IEventManager manager = presentation.getEventManager();
            if(manager != null)
            {
                manager.onPreDeleteLink(pLinkAboutToBeDeleted, bIsFromNode);
            }
        }
        
        return 0;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#performDeepSynch()
         */
    public long performDeepSynch()
    {
        IETGraphObjectUI ui = this.getETUI();
        if (ui != null)
        {
            IDrawEngine de = ui.getDrawEngine();
            if (de != null)
            {
                de.performDeepSynch();
                
                // Reset the text on all the labels, including changing the aliasing based on
                // the friendly names.
                ILabelManager pLabelManager = de.getLabelManager();
                if (pLabelManager != null)
                {
                    pLabelManager.resetLabelsText();
                }
            }
        }
        
        return 0;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#resetDrawEngine(java.lang.String)
         */
    public long resetDrawEngine(String sInitializationString)
    {
        ILabelManager pLabelManager = null;
        IETGraphObjectUI ui = (IETGraphObjectUI)getUI();
        IETGraphObject pThis = (IETGraphObject) ui.getTSObject();
        if (pThis != null)
        {
            IDrawEngine m_Engine = ui.getDrawEngine();
            // Remove all the labels on this element, we will reset them again
            // once the draw engine has been re-created
            if (m_Engine != null)
            {
                pLabelManager = m_Engine.getLabelManager();
                if (pLabelManager != null)
                {
                    pLabelManager.discardAllLabels();
                    pLabelManager = null;
                }
                
                // NULL out the draw engine
                ui.setDrawEngine(null);
            }
            
            IPresentationElement presEle = ((IETGraphObject) ui.getTSObject()).getPresentationElement();
            IElement pElement = null;
            if (presEle != null)
            {
                pElement = presEle.getFirstSubject();
            }
            
            // Go through the attach logic to reinitialize the draw engine
            if (pElement != null)
            {
                // Call our base attach which will attach to this IElement - unlike Create this does not
                // create a new IElement, it uses the argument 'pVal'
                attach( pElement, sInitializationString );
            }
            
            // Reset the labels and edges
            m_Engine = ui.getDrawEngine();
            if (m_Engine != null)
            {
                pLabelManager = m_Engine.getLabelManager();
            }
            if (pLabelManager != null)
            {
                pLabelManager.resetLabels();
            }
            IEventManager  pEventManager = m_Engine.getEventManager();
            if (pEventManager != null)
            {
                pEventManager.resetEdges();
            }
        }
        return 0;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#setEngineParent(com.tomsawyer.editor.TSEObjectUI)
         */
    public long setEngineParent(TSEObjectUI pObjectView)
    {
        // TODO Auto-generated method stub
        return 0;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#setInitializationString(java.lang.String)
         */
    public void setInitializationString(String value)
    {
        // TODO Auto-generated method stub
        
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#setSynchState(int)
         */
    public void setSynchState(int value)
    {
        mSynchState = value;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#transform(java.lang.String)
         */
    public IPresentationElement transform(String typeName)
    {
        IPresentationElement pPresentationElement = null;
        
        IPresentationElement cpPE = getPresentationElement();
        if (cpPE != null)
        {
            String xmiid = cpPE.getXMIID();
            if (xmiid != null && xmiid.length() > 0)
            {
                // Keep our xmiid so we can restore it on the newly created presentation element
//				IETGraphObject pETGraphObject = ((IGraphPresentation)cpPE).getETGraphObject();
//				if (pETGraphObject != null)
//				{
//					pETGraphObject.setReloadedPresentationXMIID(xmiid);
//				}
                setReloadedPresentationXMIID(xmiid);
                
                // Remove the presentation element from the model element
                IElement cpElement = cpPE.getFirstSubject();
                if ( cpElement != null )
                {
                    ETBaseUI.reattach(cpElement, typeName, getETUI());
                    
                    cpPE = getPresentationElement();
                    if (cpPE != null)
                    {
                        pPresentationElement = cpPE;
                    }
                }
            }
        }
        
        return pPresentationElement;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#validate(org.netbeans.modules.uml.core.metamodel.diagrams.IGraphObjectValidation)
         */
    public long validate(IGraphObjectValidation pValidationKind)
    {
        checkConnectionToPresentationElement( pValidationKind );
        checkLinkEnds( pValidationKind );
        checkDrawEngine( pValidationKind, false );
        
        return 0;
    }
    
    void checkLinkEnds(IGraphObjectValidation pValidationKind)
    {
        // Check the link ends
        boolean bCheckLinkEnds =
                pValidationKind.getValidationKind(
                IDiagramValidateKind.DVK_VALIDATE_LINKENDS);
        
        if (bCheckLinkEnds)
        {
            int dvrLinkEnds = IDiagramValidateResult.DVR_INVALID;
            
            IPresentationElement pe = getPresentationElement();
            
            IEdgePresentation edgePresentation = null;
            if (pe instanceof IEdgePresentation)
                edgePresentation = (IEdgePresentation) pe;
            
            if (edgePresentation != null)
            {
                boolean bLinkEndsAreValid = edgePresentation.validateLinkEnds();
                dvrLinkEnds =
                        (bLinkEndsAreValid)
                        ? IDiagramValidateResult.DVR_VALID
                        : IDiagramValidateResult.DVR_INVALID;
            }
            
            pValidationKind.setValidationResult(
                    IDiagramValidateKind.DVK_VALIDATE_LINKENDS,
                    dvrLinkEnds);
        }
    }
    
    private void checkDrawEngine(IGraphObjectValidation pValidationKind,boolean bAllowDeepSync)
    {
        if (pValidationKind == null)
        {
            // error
            return;
        }
        
        int dvrDrawEngine = IDiagramValidateResult.DVR_INVALID;
        
        // Get what we should validate and set the invalid/valid states to a default.
        boolean bCheckDrawEngine =
                pValidationKind.getValidationKind(
                IDiagramValidateKind.DVK_VALIDATE_DRAWENGINE);
        
        boolean bCheckResyncDeep =
                pValidationKind.getValidationKind(
                IDiagramValidateKind.DVK_VALIDATE_RESYNC_DEEP);
        
        // Check the draw engine and go deep if necessary
        if (bCheckDrawEngine)
        {
            IDrawEngine engine = getEngine();
            if (engine != null)
            {
                boolean bIsValid = engine.isDrawEngineValidForModelElement();
                if (bIsValid)
                {
                    dvrDrawEngine = IDiagramValidateResult.DVR_VALID;
                    
                    if (bAllowDeepSync)
                    {
                        if (bCheckResyncDeep)
                        {
                            bIsValid = engine.validateNode();
                        }
                    }
                }
            }
        }
        
        if (bCheckDrawEngine)
        {
            pValidationKind.setValidationResult(
                    IDiagramValidateKind.DVK_VALIDATE_DRAWENGINE,
                    dvrDrawEngine);
        }
        if (bCheckResyncDeep)
        {
            // Set the resync deep invalid to tell the caller that we do have a deep resync function.
            // If we leave it alone the diagram validator doesn't call deep sync.
            pValidationKind.setValidationResult(
                    IDiagramValidateKind.DVK_VALIDATE_RESYNC_DEEP,
                    IDiagramValidateResult.DVR_INVALID);
        }
    }
    private void checkConnectionToPresentationElement(IGraphObjectValidation pValidationKind)
    {
        if (pValidationKind == null)
        {
            //error
            return;
        }
        
        boolean bCheckConnectionToElement =
                pValidationKind.getValidationKind(
                IDiagramValidateKind.DVK_VALIDATE_CONNECTIONTOELEMENT);
        
        // Check connection to our element
        if (bCheckConnectionToElement)
        {
            int dvResult = IDiagramValidateResult.DVR_INVALID;
            
            IPresentationElement pe = getPresentationElement();
            
            IGraphPresentation graphPE = null;
            if (graphPE instanceof IGraphPresentation)
                graphPE = (IGraphPresentation) pe;
            
            if (graphPE != null)
            {
                // Clear our cached ME so that a proper connection can be verified
                //			 graphPE.clearModelElementCache();
            }
            
            // Get the IElement for this graph object
            IElement element =
                    TypeConversions.getElement((IETGraphObject) this);
            
            if (pe != null && element != null)
            {
                // See if the element knows about this presentation elment
                boolean bPEIsPresent = element.isPresent(pe);
                if (!bPEIsPresent)
                {
                    // We've got to reconnect
                    element.addPresentationElement(pe);
                    bPEIsPresent = element.isPresent(pe);
                }
                
                if (bPEIsPresent)
                {
                    dvResult = IDiagramValidateResult.DVR_VALID;
                }
            }
            
            pValidationKind.setValidationResult(
                    IDiagramValidateKind.DVK_VALIDATE_CONNECTIONTOELEMENT,
                    dvResult);
        }
    }
    
    /**
     * This method disconnects this edge when the edge is removed from a graph.
     * The old owner should not be null and it should be the same as the current
     * owner. This method is public for implementation purposes. It may be
     * overridden by extending classes, but should not be called by the user
     * except from a method which overrides it.
     *
     * @param oldOwner The graph from which this edge is being removed.
    
     */
    public void onRemove(TSGraphObject oldOwner)
    {
        ETGraphManager mgr = (ETGraphManager)this.getOwnerGraph().getOwnerGraphManager();
        ADGraphWindow wnd = (ADGraphWindow)mgr.getGraphWindow();
        
//      if (wnd != null && !(wnd.getCurrentState() instanceof TSEReconnectEdgeState))
        if (wnd != null && !(wnd.getCurrentTool() instanceof TSEReconnectEdgeTool))
            sendPreDeleteEvent();
        
        super.onRemove(oldOwner);
    }
    
    /**
     * Calls OnPreDeleteLink on the to and from nodes, if available.
     *
     */
    protected void sendPreDeleteEvent()
    {
        IPresentationElement presentation = getPresentationElement();
        if (presentation instanceof IEdgePresentation)
        {
            IEdgePresentation edgePresentation = (IEdgePresentation)presentation;
            ETPairT<IETGraphObject, IETGraphObject>  nodes = edgePresentation.getEdgeFromAndToNode();
            
            IETGraphObject fromNode = nodes.getParamOne();
            if(fromNode != null)
            {
                fromNode.onPreDeleteLink(this, true);
            }
            
            IETGraphObject toNode = nodes.getParamOne();
            if(toNode != null)
            {
                toNode.onPreDeleteLink(this, false);
            }
        }
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETEdge#hasBends()
         */
    public boolean hasBends()
    {
        List bends = bendPoints();
        return bends != null && !bends.isEmpty();
    }
    
        /* (non-Javadoc)
         * @see com.tomsawyer.editor.TSEObject#getToolTipText()
         */
    public String getToolTipText()
    {
        //Disable label tooltips
        return null;
    }
    
   /* (non-Javadoc)
    * @see com.tomsawyer.graph.TSEdge#setSourceNode(com.tomsawyer.graph.TSNode)
    */
    public void setSourceNode(TSNode arg0)
    {
        // TODO Auto-generated method stub
        super.setSourceNode(arg0);
    }
    
   /* (non-Javadoc)
    * @see com.tomsawyer.graph.TSEdge#setTargetNode(com.tomsawyer.graph.TSNode)
    */
    public void setTargetNode(TSNode arg0)
    {
        // TODO Auto-generated method stub
        super.setTargetNode(arg0);
    }
    
   /* (non-Javadoc)
    * @see com.tomsawyer.drawing.TSDEdge#setSourceConnector(com.tomsawyer.drawing.TSConnector)
    */
    public void setSourceConnector(TSConnector arg0)
    {
        // TODO Auto-generated method stub
        super.setSourceConnector(arg0);
    }
    
   /* (non-Javadoc)
    * @see com.tomsawyer.drawing.TSDEdge#setTargetConnector(com.tomsawyer.drawing.TSConnector)
    */
    public void setTargetConnector(TSConnector arg0)
    {
        // TODO Auto-generated method stub
        super.setTargetConnector(arg0);
    }
    
    protected TSEGraphWindow getGraphWindow()
    {
        IDrawingAreaControl ctrl = getDrawingAreaControl();
        return ctrl != null ? ctrl.getGraphWindow() : null;
    }
    
    public void setSelected(boolean selected)
    {
        //if (this.getGraphWindow() != null && getGraphWindow().getCurrentState() instanceof TSEMoveSelectedState)
        if (this.getGraphWindow() != null && getGraphWindow().getCurrentTool() instanceof TSEMoveSelectedTool)
        {
            ETSystem.out.println("Warning: can not change selection lists while in TSEMoveSelectedState  state.");
            return;
        }
        super.setSelected(selected);
    }
}

