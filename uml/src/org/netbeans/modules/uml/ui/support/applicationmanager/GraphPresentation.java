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

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.PresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.IETGraphObjectUI;
import org.netbeans.modules.uml.ui.support.drawingproperties.IColorProperty;
import org.netbeans.modules.uml.ui.support.drawingproperties.IDrawingPropertyProvider;
import org.netbeans.modules.uml.ui.support.drawingproperties.IFontProperty;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.support.drawingproperties.IDrawingProperty;
import com.tomsawyer.editor.ui.TSENodeUI;
import com.tomsawyer.editor.ui.TSEEdgeUI;
import com.tomsawyer.editor.TSEObject;
import com.tomsawyer.editor.TSEObjectUI;

/*
 *
 * @author KevinM
 *
 */
public abstract class GraphPresentation extends PresentationElement implements IGraphPresentation, IDrawingPropertyProvider
{
    
    //private IETGraphObjectUI m_ui = null;
    
    /**
     *
     */
    public GraphPresentation()
    {
        super();
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.applicationmanager.IGraphPresentation#getModelElement()
         */
    public IElement getModelElement()
    {
        IETGraphObjectUI ui = getUI();
        
        return ui != null ? ui.getModelElement() : null;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.applicationmanager.IGraphPresentation#setModelElement(null)
         */
    public void setModelElement(IElement value)
    {
        IETGraphObjectUI ui = getUI();
        if (ui != null)
        {
            ui.setModelElement(value);
        }
    }
    
    
    /**
     *
     * Returns the first element on the subjects collection.
     *
     * @param subject[out] the first element in the list
     *
     * @return HRESULTs
     *
     */
    public IElement getFirstSubject()
    {
        IElement retVal = getModelElement();
        
        if (retVal == null)
        {
            IElement pElement = super.getFirstSubject();
            
            // Verify the connection from the ME to the PE.  This'll fail if our parent
            if (pElement != null)
            {
//            CComPtr < IPresentationElement > pPE;
//            _VH(GetPEInterface(&pPE));
                
                boolean bPEIsPresent = pElement.isPresent( this );
                if (bPEIsPresent == false)
                {
                    // We've got to reconnect
                    pElement.addPresentationElement(this);
                }
                
                retVal = pElement;
                setModelElement(pElement);
            }
        }
        
        return retVal;
    }
    
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.IGraphPresentation#getIsOnDiagram(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram)
    */
    public boolean getIsOnDiagram(IDiagram pDiagram)
    {
        boolean retVal = false;
        
        IETGraphObjectUI ui = getUI();
        if(ui != null)
        {
            if( (ui.getTSObject() != null) && (ui.getDrawEngine() != null))
            {
                IDiagram assocDiagram = ui.getDrawEngine().getDiagram();
                if(assocDiagram == pDiagram)
                {
                    retVal = true;
                }
            }
        }
        return retVal;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.applicationmanager.IGraphPresentation#getIsOnDiagramFilename(java.lang.String)
         */
    public boolean getIsOnDiagramFilename(String sFullFilename)
    {
        IETGraphObjectUI ui = getUI();
        return ui != null && ui.getDrawEngine() != null ? ui.getDrawEngine().getDiagram().getFilename().equals(sFullFilename) : false;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.applicationmanager.IGraphPresentation#getSelected()
         */
    public boolean getSelected()
    {
        try
        {
            IETGraphObjectUI ui = getUI();
            return ui != null && ui.getTSObject() != null ? ui.getTSObject().isSelected() : false;
        }
        catch (Exception e)
        {
            return false;
        }
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.applicationmanager.IGraphPresentation#setSelected(boolean)
         */
    public void setSelected(boolean value)
    {
        try
        {
            IETGraphObjectUI ui = getUI();
            if (ui != null && ui.getDrawEngine() != null)
            {
                ui.getDrawEngine().getParent().getOwner().setSelected(value);
            }
        }
        catch (Exception e)
        {
        }
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.applicationmanager.ILabelPresentation#invalidate()
         */
    public void invalidate()
    {
    }
    
    protected boolean isNode()
    {
        return getUI() instanceof TSENodeUI;
    }
    
    protected boolean isEdge()
    {
        return getUI() instanceof TSEEdgeUI;
    }
    
//    public IETGraphObjectUI getUI()
//    {
//        return m_ui;
//    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.applicationmanager.IGraphPresentation#getDrawEngine()
         */
    public IDrawEngine getDrawEngine()
    {
        return getUI() != null ? getUI().getDrawEngine() : null;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.applicationmanager.IGraphPresentation#getETGraphObject()
         */
    public IETGraphObject getETGraphObject()
    {
        IETGraphObject retVal = null;
        TSEObjectUI ui = (TSEObjectUI) getUI();
        
        TSEObject tssGraphObj = ui != null ? ui.getOwner() : null;        
        if(tssGraphObj  instanceof IETGraphObject)
        {
            retVal = (IETGraphObject) tssGraphObj;
        }
        return retVal;
    }
    
    /**
     * Called when the element connected to this PE has possibly been reparented to another document as a result of SCC operations.")]
     */
    public void externalElementLoaded()
    {
        // Just clear out our cached model element.  When the user checks in/out of SCC the elements
        // selected go into another document.  The SCCIntegrator will handle those element to make sure
        // any in memory objects get their dom node changed, but not children.  So, for instance, if
        // you check in an end of a generalization, the generalization will NOT get it's DOM node changed.
        // So we have to loop over all PE's during SCC events and clear out the ME cache on everything.
        clearModelElementCache();
        
        // Now get the first subject to re-establish the cache and verify the connection from the PE
        // to the ME and back.
        IElement pTempElement = getFirstSubject();
    }
    
    /**
     * Clear model element cache
     */
    public void clearModelElementCache()
    {
        // Clear out the cached model element.  This is done in several spots.  For instance, if we're
        // checking the connection to the model element it defeats purposes to use a cached element.
        setModelElement(null);
    }
    
    
    //	IDrawingPropertyProvider
    public ETList<IDrawingProperty> getDrawingProperties()
    {
        ETList<IDrawingProperty> pProperties = null;
        
        IDrawEngine pDE = getDrawEngine();
        if (pDE != null)
        {
            IDrawingPropertyProvider pProvider = (IDrawingPropertyProvider)pDE;
            pProperties = pProvider.getDrawingProperties();
        }
        
        return pProperties;
    }
    
    public void saveColor(String sDrawEngineType, String sResourceName, int nColor)
    {
        IDrawEngine pDE = getDrawEngine();
        if (pDE != null)
        {
            IDrawingPropertyProvider pProvider = (IDrawingPropertyProvider)pDE;
            pProvider.saveColor(sDrawEngineType, sResourceName, nColor);
        }
    }
    
    public void saveColor2(IColorProperty pProperty)
    {
        IDrawEngine pDE = getDrawEngine();
        if (pDE != null)
        {
            IDrawingPropertyProvider pProvider = (IDrawingPropertyProvider)pDE;
            pProvider.saveColor2(pProperty);
        }
    }
    
    public void saveFont(  String sDrawEngineName,
            String sResourceName,
            String sFaceName,
            int nHeight,
            int nWeight,
            boolean bItalic,
            int nColor)
    {
        IDrawEngine pDE = getDrawEngine();
        if (pDE != null)
        {
            IDrawingPropertyProvider pProvider = (IDrawingPropertyProvider)pDE;
            pProvider.saveFont(sDrawEngineName,
                    sResourceName,
                    sFaceName,
                    nHeight,
                    nWeight,
                    bItalic,
                    nColor);
        }
    }
    
    public void saveFont2(IFontProperty pProperty)
    {
        IDrawEngine pDE = getDrawEngine();
        if (pDE != null)
        {
            IDrawingPropertyProvider pProvider = (IDrawingPropertyProvider)pDE;
            pProvider.saveFont2(pProperty);
        }
    }
    
    public void resetToDefaultResource( String sDrawEngineName,
            String sResourceName,
            String sResourceType)
    {
        IDrawEngine pDE = getDrawEngine();
        if (pDE != null)
        {
            IDrawingPropertyProvider pProvider = (IDrawingPropertyProvider)pDE;
            pProvider.resetToDefaultResource( sDrawEngineName,
                    sResourceName,
                    sResourceType);
        }
    }
    
    public void resetToDefaultResources()
    {
        IDrawEngine pDE = getDrawEngine();
        if (pDE != null)
        {
            IDrawingPropertyProvider pProvider = (IDrawingPropertyProvider)pDE;
            pProvider.resetToDefaultResources();
        }
    }
    
    public void resetToDefaultResources2(String sDrawEngineName)
    {
        IDrawEngine pDE = getDrawEngine();
        if (pDE != null)
        {
            IDrawingPropertyProvider pProvider = (IDrawingPropertyProvider)pDE;
            pProvider.resetToDefaultResources2(sDrawEngineName);
        }
    }
    
    public void dumpToFile(String sFile, boolean bDumpChildren, boolean bAppendToExistingFile)
    {
        IDrawEngine pDE = getDrawEngine();
        if (pDE != null)
        {
            IDrawingPropertyProvider pProvider = (IDrawingPropertyProvider)pDE;
            pProvider.dumpToFile(sFile, bDumpChildren, bAppendToExistingFile);
        }
    }
    
    public boolean displayFontDialog(IFontProperty pProperty)
    {
        IDrawEngine pDE = getDrawEngine();
        if (pDE != null)
        {
            IDrawingPropertyProvider pProvider = (IDrawingPropertyProvider)pDE;
            return pProvider.displayFontDialog(pProperty);
        }
        else
        {
            return false;
        }
    }
    
    public boolean displayColorDialog(IColorProperty pProperty)
    {
        IDrawEngine pDE = getDrawEngine();
        if (pDE != null)
        {
            IDrawingPropertyProvider pProvider = (IDrawingPropertyProvider)pDE;
            return pProvider.displayColorDialog(pProperty);
        }
        else
        {
            return false;
        }
        
    }
    
    public void invalidateProvider()
    {
        IDrawEngine pDE = getDrawEngine();
        if (pDE != null)
        {
            IDrawingPropertyProvider pProvider = (IDrawingPropertyProvider)pDE;
            pProvider.invalidateProvider();
        }
    }
}
