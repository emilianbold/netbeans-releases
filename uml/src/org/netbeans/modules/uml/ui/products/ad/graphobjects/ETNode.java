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
import org.netbeans.modules.uml.ui.support.applicationmanager.INodePresentation;
import java.util.Iterator;

import org.netbeans.modules.uml.common.ETSystem;
import org.netbeans.modules.uml.common.ETException;
import org.netbeans.modules.uml.common.generics.IteratorT;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramValidateKind;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramValidateResult;
import org.netbeans.modules.uml.core.metamodel.diagrams.IGraphEventKind;
import org.netbeans.modules.uml.core.metamodel.diagrams.IGraphObjectValidation;
import org.netbeans.modules.uml.core.metamodel.diagrams.ISynchStateKind;
import org.netbeans.modules.uml.core.support.umlsupport.ETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IStrings;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.controls.drawingarea.IUIDiagram;
import org.netbeans.modules.uml.ui.products.ad.application.IMenuManager;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.ETBaseUI;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.IETGraphObjectUI;
import org.netbeans.modules.uml.ui.support.ElementReloader;
import org.netbeans.modules.uml.ui.support.PresentationReferenceHelper;
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
import org.netbeans.modules.uml.ui.support.viewfactorysupport.NotificationTargets;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;

import com.tomsawyer.editor.TSEGraph;
import com.tomsawyer.editor.TSEGraphWindow;
import com.tomsawyer.editor.TSENode;
import com.tomsawyer.editor.TSEObject;
import com.tomsawyer.editor.TSEObjectUI;
import com.tomsawyer.editor.tool.TSEMoveSelectedTool;
import com.tomsawyer.editor.ui.TSENodeUI;
import com.tomsawyer.graph.TSGraphObject;
import com.tomsawyer.drawing.TSNodeLabel;
import com.tomsawyer.drawing.TSConnector;
import com.tomsawyer.drawing.geometry.TSConstPoint;
import com.tomsawyer.drawing.geometry.TSConstRect;
import com.tomsawyer.drawing.geometry.TSConstSize;
import com.tomsawyer.drawing.geometry.TSRect;
import com.tomsawyer.util.TSObject;
import org.netbeans.modules.uml.core.support.Debug;
import java.util.List;

public class ETNode extends TSENode implements IETNode
{
    //private IGraphPresentation m_presentation;
    private INodePresentation m_presentation = null;
    int mSynchState = ISynchStateKind.SSK_UNKNOWN_SYNCH_STATE;
    
    /**
     * Constructor of the class. This constructor should be implemented
     * to enable <code>TSENode</code> inheritance.
     */
    protected ETNode()
    {
        super();
    }
/* jyothi wrote this
        protected TSENodeUI newNodeUI() {
            return (new ETGenericNodeUI());
        }
 */
    protected void finalize()
    {
//		try
//		{
//			super.finalize();
//			FactoryRetriever ret = FactoryRetriever.instance();
//			String xmiid = XMLManip.getAttributeValue(m_Node, "xmi.id");
//			if (xmiid != null && xmiid.length() > 0)
//			{
//				ret.removeObject(xmiid);
//			}
//		}
//		catch (Throwable e)
//		{
//		}
        Debug.out.println("");
    }
    /**
     * This method allocates a new label for this node. This method
     * should be implemented to enable <code>TSENodeLabel</code>
     * inheritance.
     *
     * @return an object of the type derived from <code>TSENodeLabel</code>
     */
    protected TSNodeLabel newLabel()
    {
        ETSystem.out.println("Inside ETNode.newLabel()");
        return (new ETNodeLabel());
    }
    
    /**
     * This method allocates a new connector for this node. This method
     * should be implemented to enable <code>TSEConnector</code>
     * inheritance.
     *
     * @return an object of the type derived from <code>TSEConnector</code>
     */
    protected TSConnector newConnector()
    {
        return (new ETConnector());
    }
    
    /**
     * This method copies attributes of the source object to this
     * object. The source object has to be of the type compatible
     * with this class (equal or derived). The method should make a
     * deep copy of all instance variables declared in this class.
     * Variables of simple (non-object) types are automatically copied
     * by the call to the copy method of the super class.
     *
     * @param sourceObject  the source from which all attributes must
     *                      be copied
     */
    public void copy(Object sourceObject)
    {
        setPresentationElement(null);
        
        // copy the attributes of the super class first
        super.copy(sourceObject);
        
        IElement modelElement = TypeConversions.getElement((TSObject)sourceObject);
        if (modelElement != null)
        {
            ETBaseUI.attachAndCreatePresentationElement(modelElement, getETUI().getInitStringValue(), false, getETUI());
        }
        
    }
    
    /**
     * This method sets the resizability of this node. It also ensures
     * that layout respects the RESIZABILITY_PRESERVE_ASPECT flag if
     * possible.
     */
    public void setResizability(int resizability)
    {
        super.setResizability(resizability);
        
        int value;
        
        // make the layout server respect the RESIZABILITY_PRESERVE_ASPECT flag.
                /* commented by jyothi
                TSIntLayoutProperty layoutProperty = new TSIntLayoutProperty(TSTailorProperties.ORTHOGONAL_NODE_RESIZE_STYLE);
                 
                if ((resizability & TSENode.RESIZABILITY_PRESERVE_ASPECT) != 0) {
                        value = TSLayoutPropertyEnums.RESIZE_STYLE_PRESERVE_ASPECT_RATIO;
                } else {
                        value = TSLayoutPropertyEnums.RESIZE_STYLE_STRETCHABLE;
                 
                }
                 
                // set the value and the property. This will replace any existing property of the same type.
                layoutProperty.setCurrentValue(value);
                this.setTailorProperty(layoutProperty);
                 */
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode#connected()
         */
    public boolean connected()
    {
        return hasChildren() || hasParents();
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode#hasChildren()
         */
    public boolean hasChildren()
    {
        return outDegree() > 0;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode#hasParents()
         */
    public boolean hasParents()
    {
        return inDegree() > 0;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#affectModelElementDeletion()
         */
    public void affectModelElementDeletion()
    {
        IETGraphObjectUI ui = this.getETUI();
        if (ui != null)
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
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#getEngine()
         */
    public IDrawEngine getEngine()
    {
        return getETUI() != null ? getETUI().getDrawEngine() : null;
    }
    
    public IDrawingAreaControl getDrawingAreaControl()
    {
        return getETUI() != null ? getETUI().getDrawingArea() : null;
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
        return ui != null ? ui.getReferredElements() : null;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#getReloadedModelElementXMIID()
         */
    public String getReloadedModelElementXMIID()
    {
        IETGraphObjectUI ui = getETUI();
        return ui != null ? ui.getReloadedModelElementXMIID() : null;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#getReloadedOwnerPresentationXMIID()
         */
    public String getReloadedOwnerPresentationXMIID()
    {
        IETGraphObjectUI ui = getETUI();
        return ui != null ? ui.getReloadedOwnerPresentationXMIID() : null;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#getReloadedPresentationXMIID()
         */
    public String getReloadedPresentationXMIID()
    {
        IETGraphObjectUI ui = getETUI();
        return ui != null ? ui.getReloadedPresentationXMIID() : null;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#getReloadedTopLevelXMIID()
         */
    public String getReloadedTopLevelXMIID()
    {
        IETGraphObjectUI ui = getETUI();
        return ui != null ? ui.getReloadedTopLevelXMIID() : null;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#getTopLevelXMIID()
         */
    public String getTopLevelXMIID()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    /**
     * Reads this element from the etlp file
     */
    public void load(IProductArchive pProductArchive)
    {
        //implemented from BaseGraphObject.cpp
        if (pProductArchive != null)
        {
            String reloadedXMIID = getReloadedPresentationXMIID();
            IProductArchiveElement foundEle;
            if (reloadedXMIID.length() > 0)
            {
                foundEle = pProductArchive.getElement(reloadedXMIID);
            }
            else
            {
                foundEle = null;
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
    
    /**
     * Notification that the model element has changed.
     *
     * @param pTargets [in] Contains information about what the event was and who is getting
     * notified.
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
        if (nKind == IGraphEventKind.GEK_PRE_LAYOUT)
        {
            // make sure the current size is the orginalSize or
            // the layout server will set it back to the size on creation.
            // Kevin.
            setOriginalSize(getBounds().getSize());
        }
        ETBaseUI.onGraphEvent(nKind, getETUI());
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
    public boolean onKeyup(int nKeyCode, int nShift)
    {
        // TODO Auto-generated method stub
        return false;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#postLoad()
         */
    public void postLoad()
    {
        IPresentationElement pEle = getPresentationElement();
        ElementReloader reloader = new ElementReloader();
        
        String reloadedOwnerPresXMIID = getReloadedOwnerPresentationXMIID();
        String reloadedTopLevelXMIID = getReloadedTopLevelXMIID();
        IStrings refElemList = getReferredElements();
        
        boolean deleted = false;
        if (reloadedOwnerPresXMIID.length() > 0 && reloadedTopLevelXMIID.length() > 0 && pEle != null)
        {
            boolean attachFailed = true;
            
            // Get this presentation element and reattach.  If we are a bridge and cannot
            // reattach then delete ourselves
            IElement modEle = reloader.getElement(reloadedTopLevelXMIID, reloadedOwnerPresXMIID);
            if (modEle != null)
            {
                modEle.addElement(pEle);
                attachFailed = false;
            }
            
            if (attachFailed)
            {
                IDrawingAreaControl control = getDrawingAreaControl();
                if (control != null)
                {
                    control.postDeletePresentationElement(pEle);
                    deleted = true;
                }
            }
        }
        
        // Recreate the presentation reference relationships
        if (!deleted && refElemList != null)
        {
            long count = refElemList.getCount();
            Iterator < String > refElemIter = refElemList.iterator();
            while (refElemIter.hasNext())
            {
                String refId = refElemIter.next();
                IElement modEle = reloader.getElement(refId);
                if (modEle != null && modEle instanceof IPresentationElement)
                {
                    PresentationReferenceHelper.createPresentationReference(pEle, (IPresentationElement) modEle);
                    
                }
            }
        }
        
        // This call will allow the draw engine
        // to process information after all objects have been loaded.
        IDrawEngine engine = getEngine();
        if (!deleted && engine != null)
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
    
    /**
     * Restore from the product archive
     *
     * @param pProductArchive [in] The archive we're reading from
     * @param pElement [in] The current element, this is where this graph object's elements and attributes should be.
     */
    public void readFromArchive(IProductArchive prodArch, IProductArchiveElement archEle)
    {
        if (prodArch != null && archEle != null)
        {
            TSEObjectUI ui = getUI();
            if (ui != null && ui instanceof ETGenericNodeUI)
            {
                ((ETGenericNodeUI) ui).readFromArchive(prodArch, archEle);
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
        IETGraphObjectUI ui = this.getETUI();
        if (ui != null)
        {
            ETBaseUI.save(prodArch, ui);
        }
    }
    
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#setDiagram(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram)
         */
    public void setDiagram(IDiagram value)
    {
        //Implemented from ETGraphObjectImpl.cpp
        if (value instanceof IUIDiagram)
        {
            //			m_RawDrawingAreaControl = ((IUIDiagram)value).getDrawingArea();
            //			if (m_RawDrawingAreaControl != null)
            //			{
            //				m_RawDrawingAreaControl = null;
            //			}
        }
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
        //ETGenericNodeUI etgennodeui = new ETGenericNodeUI();
            /*
            List l = ui.getProperties();
            for (int i=0; i<l.size(); i++) {
                Debug.out.println(" Property = "+l.get(i));
            }
            Debug.out.println(" ui object : class = "+ui.getClass()+" owner = "+ui.getOwner());
             */
        // Keep the presentation element n'sync.
        if (ui instanceof IETGraphObjectUI || ui == null)
        {
            IPresentationElement pe = this.getPresentationElement();
            IGraphPresentation graphPE = pe instanceof IGraphPresentation ? (IGraphPresentation) pe : null;
            
//            if (graphPE != null)
//                graphPE.setUI((IETGraphObjectUI) ui);                                                              
        }
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#setPresentationElement(org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement)
         */
    public void setPresentationElement(IPresentationElement value)
    {
//        m_presentation = value instanceof IGraphPresentation ? (IGraphPresentation) value : null;
        m_presentation = value instanceof INodePresentation ? (INodePresentation) value : null;
        // Make sure the back pointer is in sync
        if (m_presentation != null && getETUI() != null)
        {
            //m_presentation.setUI(getETUI());
            m_presentation.setTSNode(this);
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
        if(getEngine() != null)
        {
            getEngine().sizeToContents();
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
            IETGraphObjectUI ui = getETUI();
            if (ui != null && ui instanceof ETGenericNodeUI)
                ((ETGenericNodeUI)ui).writeToArchive(prodArch, archEle);
        }
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ITSGraphObject#copy(org.netbeans.modules.uml.ui.support.viewfactorysupport.ITSGraphObject)
     */
    public void copy(ITSGraphObject objToClone)
    {
        // TODO Auto-generated method stub
        
    }
    
    protected TSEGraphWindow getGraphWindow()
    {
        IDrawingAreaControl ctrl = getDrawingAreaControl();
        return ctrl != null ? ctrl.getGraphWindow() : null;
    }
    
    protected void invalidateEdges(List edges)
    {
        IDrawingAreaControl ctrl = getDrawingAreaControl();
        
        if (ctrl == null || edges == null)
            return;
        
        TSEGraphWindow window = ctrl.getGraphWindow();
        if (window != null)
        {
            IteratorT<TSEObject> iter = new IteratorT<TSEObject>(edges);
            while (iter.hasNext())
                window.addInvalidRegion(iter.next());
        }
    }
    
    public void invalidateInEdges()
    {
        invalidateEdges(getInEdges());
    }
    
    public void invalidateOutEdges()
    {
        invalidateEdges(getOutEdges());
    }
    
    public void invalidateEdges()
    {
        invalidateInEdges();
        invalidateOutEdges();
    }
    
    /**
     * Used during paste, this creates a copy of this node.
     *
     * @param pTargetDiagram [in] The diagram this node is to appear on
     * @param pCenter [in] The location of the node
     * @param pCopy [out,retval] The created node
     */
    public IETNode createNodeCopy(IDiagram pTargetDiagram, IETPoint pCenterPoint)
    {
        IDrawingAreaControl pControl = null;
        TSEGraph pTSGraph = null;
        IETNode copy = null;
        
        if (pTargetDiagram != null)
        {
            IUIDiagram uiDiagram = null;
            if(pTargetDiagram instanceof IUIDiagram)
                uiDiagram = (IUIDiagram)pTargetDiagram;
            
            if (uiDiagram!=null)
            {
                pControl = uiDiagram.getDrawingArea();
                
                if (pControl!=null)
                {
                    pTSGraph = pControl.getCurrentGraph();
                }
            }
        }
        
        TSENode pCreatedNode = null;
        // Change the init string to a known ADViewFactory type and create a new node
        try
        {
            // Fixed issue 93686
            // the hardcoded intStr "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI" 
            // passed to the addNode method is not a complete string; therefore it causes an incorrectly result
            // when addNode tries to find the details of the node. Calling addNode(IElemenent, IPoint) instead.

            pCreatedNode = (TSENode)pControl.addNode(
                    TypeConversions.getElement((IETGraphObject)this),
                    pCenterPoint);
//            pCreatedNode = (TSENode)pControl.addNode("org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI",
//                    pCenterPoint,false,false,TypeConversions.getElement((IETGraphObject)this));
        }
        catch(ETException e)
        {
            e.printStackTrace();
        }
        
        if (pCreatedNode!=null)
        {
            // Convert the created node to our type so we can do an operator=
            TSENodeUI createdNodeUI = pCreatedNode.getNodeUI();
            createdNodeUI.copy(getNodeUI());
            
            // Set the drawing area backpointer
            IETGraphObject pETGraphObject = TypeConversions.getETGraphObject(createdNodeUI);
            
            pETGraphObject.setDiagram(pTargetDiagram);
            
            // Set the size and location of the node
            TSConstSize size = getSize();
            pCreatedNode.setSize(size);
            
            if(pETGraphObject instanceof IETNode)
                copy = (IETNode)pETGraphObject;
        }
        return copy;
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
    
    protected void deleteNonSelectedEdges(List edges)
    {
        if (edges != null)
        {
            IteratorT<IETGraphObject> iter = new IteratorT<IETGraphObject>(edges);
            while (iter.hasNext())
            {
                IETGraphObject pObj = iter.next();
                if (!pObj.isSelected())
                {
                    pObj.delete();
                    iter.reset(edges);
                }
            }
        }
    }
    
    protected void deleteNonSelectedEdges()
    {
        deleteNonSelectedEdges(this.getInEdges());
        deleteNonSelectedEdges(this.getOutEdges());
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ITSGraphObject#delete()
         */
    public void delete()
    {
        deleteNonSelectedLabels();
        deleteNonSelectedEdges();
        TSENodeUI ui = this.getUI() instanceof 	TSENodeUI ? (TSENodeUI)getUI() : null;
        
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
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ITSGraphObject#getETUI()
         */
    public IETGraphObjectUI getETUI()
    {
        return super.getUI() instanceof IETGraphObjectUI ? (IETGraphObjectUI) super.getUI() : null;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ITSGraphObject#getObject()
         */
    public TSEObject getObject()
    {
        return this;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ITSGraphObject#isEdge()
         */
    public boolean isEdge()
    {
        return false;
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
        return true;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ITSGraphObject#setText(java.lang.Object)
         */
    public void setText(Object text)
    {
        super.setTag(text);
    }
    
    public void onContextMenu(IMenuManager manager)
    {
        ETBaseUI.onContextMenu(manager, getETUI());
    }
    
    public IElement create(INamespace space, String initStr)
    {
        IElement retEle = null;
        retEle = ETBaseUI.create(space, initStr, getETUI());
        if (retEle != null)
        {
            INotificationTargets targets = new NotificationTargets();
            targets.setChangedModelElement(retEle);
            modelElementHasChanged(targets);
        }
        return retEle;
    }
    
    public void attach(IElement modEle, String initStr)
    {
        ETBaseUI.attach(modEle, initStr, getETUI());
        INotificationTargets targets = new NotificationTargets();
        targets.setChangedModelElement(modEle);
        modelElementHasChanged(targets);
    }
    
    public void onPostAddLink(IETGraphObject newLink, boolean isFromNode)
    {
        ETBaseUI.onPostAddLink(newLink, isFromNode, getETUI());
    }
    
    public List getInEdges()
    {
        return inEdges();
    }
    
    public List getOutEdges()
    {
        return outEdges();
    }
    
    public IETRect getEdgeBounds()
    {
        IETRect boundingRect = new ETRect();
        try
        {
            IteratorT < IETEdge > edgeIter = new IteratorT < IETEdge > (getInEdges());
            while (edgeIter.hasNext())
            {
                IDrawEngine edgeEngine = TypeConversions.getDrawEngine(edgeIter.next());
                if (edgeEngine != null)
                {
                    IETRect edgeRect = edgeEngine.getBoundingRect();
                    
                    boundingRect.unionWith(edgeRect);
                }
            }
            edgeIter.reset(getOutEdges());
            
            while (edgeIter.hasNext())
            {
                IDrawEngine edgeEngine = TypeConversions.getDrawEngine(edgeIter.next());
                if (edgeEngine != null)
                {
                    IETRect edgeRect = edgeEngine.getBoundingRect();
                    
                    boundingRect.unionWith(edgeRect);
                }
            }
        }
        catch (Exception e)
        {
        }
        return boundingRect;
    }
    
    public void onDiscard(TSGraphObject oldOwner)
    {
        // Should we call the super here?????? Kevin M
        //Jyothi: Fix for Bug#6258424
        super.onDiscard(oldOwner);
        
        if (getDrawingAreaControl() != null)
        {
            getDrawingAreaControl().refreshRect(getEdgeBounds());
        }
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
/*		IDrawingAreaControl ctrl = getDrawingAreaControl();
 
                if (ctrl == null)
                        return;
 
                TSEGraphWindow window = ctrl.getGraphWindow();
                if (window != null && this.getETUI() != null && this.getETUI().getOwner() == this)
                {
                        window.addInvalidRegion(this);
                }
 */
        IDrawEngine engine = getEngine();
        if (engine != null)
        {
            engine.invalidate();
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
    
    /**
     * Called to notify the node that a link has been added.
     *
     * @param pLinkAboutToBeDeleted The link about to be deleted
     * @param bIsFromNode <code>true</code> if this is the from node.
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
        m_presentation.setModelElement(null);
        ETBaseUI.resetDrawEngine(sInitializationString, getETUI());
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
        // TODO Auto-generated method stub-didn't see the emplementation in C++
        return null;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#validate(org.netbeans.modules.uml.core.metamodel.diagrams.IGraphObjectValidation)
         */
    public long validate(IGraphObjectValidation pValidationKind)
    {
        checkConnectionToPresentationElement( pValidationKind );
        checkDrawEngine( pValidationKind, true );
        return 0;
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
    
    public void setSelected(boolean selected)
    {
        //if (this.getGraphWindow() != null && getGraphWindow().getCurrentState() instanceof TSEMoveSelectedState)
        if (this.getGraphWindow() != null && getGraphWindow().getCurrentTool() instanceof TSEMoveSelectedTool)
        {
            ETSystem.out.println("Warning: can not change selection lists while in TSEMoveSelectedState state.");
            return;
        }
        super.setSelected(selected);
    }
    
    public void moveTo(TSConstPoint pt)
    {
//        ETTSRectEx bounds = new ETTSRectEx(this.getBounds());
        TSRect bounds = new TSRect(this.getBounds());
        if (bounds != null)
        {
            bounds.setCenter(pt);
            this.setBounds(bounds);
        }
    }

    public void setBounds(TSConstRect bounds)
    {
        super.setBounds(bounds);
    }
    
        /* (non-Javadoc)
         * @see com.tomsawyer.drawing.TSDNode#addConnector()
         */
    public TSConnector addConnector()
    {
        TSConnector connector = new ETConnector(this);
        insert(connector);
        
        return connector;
    }
    
    public void remove(TSConnector connector)
    {
        super.remove(connector);
    }
    
    public void discard(TSConnector connector)
    {
        super.discard(connector);
    }
    
    public void discardAllConnectors()
    {
        super.discardAllConnectors();
    }
    
    /* (non-Javadoc)
     * @see com.tomsawyer.editor.TSEObject#getToolTipText()
     */
    public String getToolTipText()
    {
        // The Commented out area if for Debug purposes only.
        
        String retVal = super.getToolTipText();
        retVal += "<hr><br><b>Node Drawing Area Control:</b> ";
        
        IDrawingAreaControl ctrl = getDrawingAreaControl();
        if(ctrl != null)
        {
            retVal += ctrl.getName();
        }
        else
        {
            retVal += "<i>NULL</i>";
        }        
        
        retVal += "<b>X</b>: " + getLeft();
        retVal += "<b>Y</b>: " + getTop();
        retVal += "<b>Width</b>: " + getWidth();
        retVal += "<b>Height</b>: " + getHeight();
        
        retVal += "<hr>";
        IPresentationElement element = this.getPresentationElement();
        if(element instanceof IGraphPresentation)
        {
            retVal += "<br>";    
            retVal += "<b>Presentation Element:</b> #" + element.hashCode();
            
            IGraphPresentation pElement = (IGraphPresentation)element;
            IETGraphObjectUI ui = pElement.getUI();
            retVal += "<br><b>Node UI:</b> ";
            if(ui != null)
            {
                retVal += "#" + Integer.toHexString(ui.hashCode());
            }
            else
            {
                retVal += "<i>NULL</i>";
            }
        }
        
        retVal += "<hr>";
        IElement modelElement = element.getFirstSubject();
        ETList < IPresentationElement > elements = modelElement.getPresentationElements();
        retVal += "<b>Number Of Presentation Elements:</b> " + elements.size();
        int index = 0;
        for(IPresentationElement curElement : elements)
        {
            retVal += "<br>";
            retVal += "<b><font color=\"0000ff\">Presentation Drawing Area Control [" + index++ + "]:</font></b> ";
            if(curElement instanceof IGraphPresentation)
            {
                retVal += Integer.toHexString(curElement.hashCode()) + " UI: ";
                IGraphPresentation pElement = (IGraphPresentation)curElement;
                IETGraphObjectUI ui = pElement.getUI();
                //IDrawingAreaControl ctrl2 = ui.getDrawingArea();
                if(ui != null)
                {
                    retVal += Integer.toHexString(ui.hashCode());
                }
                else
                {
                    retVal += "<i>NULL</i>";
                } 
            }
            else
            {
                retVal += "<i>NULL</i>";
            } 
        }
        return retVal;
        
        
//        //Disable label tooltips
//        return null;
    }
    
        /*
         * Returns the edge interfaces connected to this node.
         */
    public ETList<IETEdge> getEdges(boolean includeInEdges, boolean includeOutEdges)
    {
        // Because of recursive edges, we may have duplicates in this list.  
        // So, filter out the duplicate edges.
        
        ETList<IETEdge> edges = new ETArrayList<IETEdge>();
        if (includeInEdges)
        {
            IteratorT < IETEdge > edgeIter = new IteratorT < IETEdge > (getInEdges());
            while (edgeIter.hasNext())
            {
                IETEdge curEdge = edgeIter.next();
                if(edges.contains(curEdge) == false)
                {
                    edges.add(curEdge);
                }
            }
        }
        
        if (includeOutEdges)
        {
            IteratorT < IETEdge > edgeIter = new IteratorT < IETEdge > (getOutEdges());
            while (edgeIter.hasNext())
            {
                IETEdge curEdge = edgeIter.next();
                if(edges.contains(curEdge) == false)
                {
                    edges.add(curEdge);
                }
            }
        }
        return edges;
    }
    
        /*
         * Returns all the edge interfaces connected to this node.
         */
    public ETList<IETEdge> getEdges()
    {
        return getEdges(true, true);
    }
    
    //jyothi - wrote to remove compilation error.. need to work on this
    public boolean isConnector()
    {
        return false;
    }
}

