/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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



package org.netbeans.modules.uml.ui.products.ad.viewfactory;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.util.List;

import org.netbeans.modules.uml.common.ETException;
import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ICreationFactory;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationReference;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IStrings;
import org.netbeans.modules.uml.ui.products.ad.drawengines.ETDrawEngineFactory;
import org.netbeans.modules.uml.ui.products.ad.drawengines.ETDrawInfo;
import org.netbeans.modules.uml.ui.products.ad.graphobjects.ETNode;
import org.netbeans.modules.uml.ui.support.applicationmanager.DrawingFactory;
import org.netbeans.modules.uml.ui.support.applicationmanager.INodePresentation;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchive;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveDefinitions;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement;
import org.netbeans.modules.uml.ui.support.relationshipVerification.INodeVerification;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.DrawEngineLineKindEnum;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ETRectEx;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.GDISupport;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNodeUI;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ITSGraphObject;
import org.netbeans.modules.uml.ui.swing.drawingarea.ADGraphWindow;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import com.tomsawyer.editor.TSEGraphWindow;
import com.tomsawyer.editor.TSEObjectUI;
import com.tomsawyer.editor.TSENode;
import com.tomsawyer.editor.graphics.TSEGraphics;
import com.tomsawyer.editor.ui.TSENodeUI;
import com.tomsawyer.editor.ui.TSEDefaultNodeUI;
//import com.tomsawyer.util.TSConstRect;
import com.tomsawyer.drawing.geometry.TSConstRect;
import com.tomsawyer.util.TSProperty;
import org.netbeans.modules.uml.core.support.Debug;

//public class ETGenericNodeUI extends TSENodeUI implements IETNodeUI
public class ETGenericNodeUI extends TSEDefaultNodeUI implements IETNodeUI {
    public ETGenericNodeUI() {
        super();
    }
    
    private final String ZERO_VALUE = "";
    
    private IDrawEngine drawEngine = null;
    private String drawEngineClass = null;
    
    private String peidValue = this.ZERO_VALUE;
    private String meidValue = this.ZERO_VALUE;
    private String topLevelMEIDValue = this.ZERO_VALUE;
    private String initStringValue = this.ZERO_VALUE;
    private String m_ReloadedOwnerPresentationXMIID = this.ZERO_VALUE;
    private IStrings m_PresentationReferenceReferredElements = null;
    private boolean m_WasModelElementDeleted = false;
    private boolean m_FailedToCreateDrawEngine = false;
    
    private IProductArchiveElement archiveElement;
    private IElement modelElement = null;
    private boolean m_resizeable = true;
    protected RenderingHints qualityHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
    /**
     * This method returns the pixel size of the nodes grapples. The size of the
     * grapples will be scaled according to the node graph zoom level.
     */
    public int getGrappleSize() {
        int retVal = 0;
        
        IDrawingAreaControl ctrl = getDrawingArea();
        if (ctrl != null) {
            retVal = getGrappleSize(ctrl.getCurrentZoom());
        }
        
        return retVal;
    }
    
    // lvv - 128186 TS StackOverflow fix
    boolean inDraw = false;
    public void draw(TSEGraphics graphics) {
        if (inDraw) 
        {
            return;
        }
        try {
            inDraw = true;
            IDrawEngine de = getDrawEngine();
            if (de != null) {
                RenderingHints prevHint = graphics.getRenderingHints();
                qualityHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                
                graphics.setRenderingHints(qualityHints);
                IDrawInfo drawInfo = getDrawInfo(graphics);
                // TODO: Determine what the DrawinToMainDrawingArea and AlwaysSetFont
                //       Should be set to.
                
                if (drawInfo != null) {
                    Rectangle clipRect = drawInfo.clip();
                    de.doDraw(drawInfo);
                    graphics.setClip(clipRect);
                } else {
                    GDISupport.frameRectangle(graphics.getGraphics(), ETBaseUI.getDeviceBounds(graphics, this),DrawEngineLineKindEnum.DELK_DOT, 1, Color.BLACK);
                }
                graphics.setRenderingHints(prevHint);
            }
        } finally {
            inDraw = false;
        }
    }
    
    /* (non-Javadoc)
     * @see com.tomsawyer.editor.TSEObjectUI#drawSelectedOutline(com.tomsawyer.editor.graphics.TSEGraphics)
     */
    public void drawSelectedOutline(TSEGraphics graphics) {
        if (this.getOwner() != null && graphics != null) {
            Stroke pen = GDISupport.getLineStroke(DrawEngineLineKindEnum.DELK_HATCHED,1);
            Stroke prevPen = graphics.getStroke();
            graphics.setStroke(pen);
            super.drawSelectedOutline(graphics);
            graphics.setStroke(prevPen);
        }
    }
    
    /* (non-Javadoc)
     * @see com.tomsawyer.editor.TSEObjectUI#drawSelected(com.tomsawyer.editor.graphics.TSEGraphics)
     */
    public void drawSelected(TSEGraphics graphics) {
        super.drawSelected(graphics);
        drawSelectedOutline(graphics);
    }
    /*
     * Returns the GraphWindow.
     */
    public TSEGraphWindow getGraphWindow() {
        return ETBaseUI.getGraphWindow(this);
    }
    
    /*
     * Returns World points, (Logical)
     */
    public IETRect getLogicalBounds() {
        return ETBaseUI.getLogicalBounds(this);
    }
    
    /*
     * Returns the device bounding rect.
     */
    public IETRect getDeviceBounds() {
        return ETBaseUI.getDeviceBounds(this);
    }
    
    /**
     * Retrieves the graphics context for the node ui.
     *
     * @param graphics The TS graphics class.
     * @return The graphics context.
     */
    public IDrawInfo getDrawInfo(TSEGraphics graphics) {
        IDrawInfo retVal = ETBaseUI.getDrawInfo(graphics, this);
        
        if (retVal != null) {
            retVal.setIsTransparent(isTransparent());
            retVal.setIsBorderDrawn(isBorderDrawn());
        }
        
        // TODO: Determine what the DrawinToMainDrawingArea and AlwaysSetFont
        //       Should be set to.
        
        return retVal;
    }
    
    /**
     * Retrieves the graphics context for the node ui.
     *
     * @return The graphics context.
     */
    public IDrawInfo getDrawInfo() {
        TSEGraphWindow window = this.getGraphWindow();
        if (window != null) {
            Graphics g = window.getGraphics();
            TSEGraphics tsGraphics;
            
            if (g instanceof TSEGraphics) {
                tsGraphics = (TSEGraphics) g;
                
            } else if (g != null) {
                tsGraphics = window.newGraphics(g);
            } else {
                tsGraphics = null;
            }
            
            return getDrawInfo(tsGraphics);
        }
        
        return null;
    }
    
    public void reset() {
        super.reset();
        this.peidValue = this.ZERO_VALUE;
        this.meidValue = this.ZERO_VALUE;
        this.topLevelMEIDValue = this.ZERO_VALUE;
        this.initStringValue = this.ZERO_VALUE;
        this.drawEngine = null;
    }
    
    public void copy(TSEObjectUI sourceUI) {
        this.reset();
        
        super.copy(sourceUI);
        ETGenericNodeUI sourceNodeUI = (ETGenericNodeUI) sourceUI;
        this.meidValue = sourceNodeUI.meidValue;
        this.topLevelMEIDValue = sourceNodeUI.topLevelMEIDValue;
        this.modelElement = sourceNodeUI.modelElement;
        
        this.setInitStringValue(sourceNodeUI.getInitStringValue());
        this.setDrawEngineClass(sourceNodeUI.getDrawEngineClass());
        getDrawEngine().setParent(this);
    }
    
    public List getProperties() {
       /* jyothi added this as the presEleId is not getting populated in the .eltd file
      List list = super.getProperties();
      list.add(new TSProperty(IProductArchiveDefinitions.PRESENTATIONELEMENTID_STRING, this.peidValue));
      return list;
        */
        List list = super.getProperties();
            String value = null;
            IPresentationElement pPE = ((IETGraphObject) getTSObject()).getPresentationElement();
            if (pPE != null) {
                // Get the presentation el  ement id
                String presEleId = pPE.getXMIID();
                if (presEleId != null && presEleId.length() > 0) {
                    value = presEleId;
                }
            }
            
            list.add(new TSProperty(IProductArchiveDefinitions.PRESENTATIONELEMENTID_STRING, value));
            return list;
    }
    
    public void setProperty(TSProperty property) {
        //String attrString = (String) property.getValue(); //jyothi
        if (IProductArchiveDefinitions.PRESENTATIONELEMENTID_STRING.equals(property.getName())) {
            String attrString = (String) property.getValue();
            this.peidValue = attrString;
        } else {
            super.setProperty(property);
        }
    }
    
    public List getChangedProperties() {
        List list = super.getChangedProperties();
        
        IPresentationElement pPE = ((IETGraphObject) getTSObject()).getPresentationElement();
        if (pPE != null) {
            // Get the presentation element id
            String presEleId = pPE.getXMIID();
            if (presEleId != null && presEleId.length() > 0) {
                list.add(new TSProperty(IProductArchiveDefinitions.PRESENTATIONELEMENTID_STRING, presEleId));
            }
        }
        return list;
    }
    
    public IDrawEngine getDrawEngine() {
        if (drawEngine == null) {
            try {
                this.initDrawEngine();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        
        return drawEngine;
    }
    
    public String getDrawEngineClass() {
        return drawEngineClass;
    }
    
    public void setDrawEngineClass(String string) {
        drawEngineClass = string;
    }
    
    public void setDrawEngine(IDrawEngine newDrawEngine) {
        try {
            if (newDrawEngine != drawEngine) {
                drawEngine = newDrawEngine;
                //				if (drawEngine != null)
                //					drawEngine.init();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public String getPeidValue() {
        return peidValue;
    }
    
    private boolean initDrawEngine() throws ETException {
        if (this.getDrawEngineClass() != null && getDrawEngineClass().length() > 0) {
            this.setDrawEngine(ETDrawEngineFactory.createDrawEngine(this));
            return this.drawEngine != null;
        } else
            return false;
    }
    
    public String getMeidValue() {
        return this.meidValue;
    }
    
    public String getTopLevelMEIDValue() {
        return this.topLevelMEIDValue;
    }
    
    public IElement getModelElement() {
        return this.modelElement;
    }
    
    public void setModelElement(IElement element) {
        this.modelElement = element;
    }
    
    public IProductArchiveElement getArchiveElement() {
        return this.archiveElement;
    }
    
    public void setArchiveElement(IProductArchiveElement element) {
        this.archiveElement = element;
        this.readFromArchive(element);
    }
    
    private void readFromArchive(IProductArchiveElement element) {
        
        this.meidValue = element.getAttributeString(IProductArchiveDefinitions.MEID_STRING);
        this.topLevelMEIDValue = element.getAttributeString(IProductArchiveDefinitions.TOPLEVELID_STRING);
        this.setInitStringValue(element.getAttributeString(IProductArchiveDefinitions.INITIALIZATIONSTRING_STRING));
        
        IProductArchiveElement engineElement = element.getElement(IProductArchiveDefinitions.ENGINENAMEELEMENT_STRING);
        this.setDrawEngineClass(engineElement.getAttributeString(IProductArchiveDefinitions.ENGINENAMEATTRIBUTE_STRING));
    }
    
    public void readFromArchive(IProductArchive prodArch, IProductArchiveElement archEle) {
        archiveElement = archEle;
        ETBaseUI.readFromArchive(prodArch, archEle, this);
    }
    
    public void writeToArchive(IProductArchive prodArch, IProductArchiveElement archEle) {
        ETBaseUI.writeToArchive(prodArch, archEle, this);
    }
    
    /**
     * Creates a new IPresentationElement
     *
     * @param pElement Not used
     * @param pElem [out,retval] Creates the appropriate IPresentationElement for this node (right now it's
     * always an INodePresentation.)
     */
    public IPresentationElement createPresentationElement(IElement pEle) {
        IPresentationElement retObj = null;
        INodePresentation nodePres = DrawingFactory.retrieveNodePresentationMetaType();
        if (nodePres != null) {
            nodePres.setTSNode((ETNode) getTSObject());
            retObj = nodePres;
        }
        return retObj;
    }
    
    public IDrawingAreaControl getDrawingArea() {
        return ETBaseUI.getDrawingArea(this);
    }
    
    public void setDrawingArea(IDrawingAreaControl control) {
        // We don't need to store this anymore.
        //this.drawingArea = control;
    }
    
    public String getInitStringValue() {
        return this.initStringValue;
    }
    
    public void setInitStringValue(String string) {
        this.initStringValue = string;
    }
    
    public boolean isAnnotationEditable() {
        return false;
    }
    
    public ITSGraphObject getTSObject() {
        return getOwner() instanceof ITSGraphObject ? (ITSGraphObject) getOwner() : null;
    }
    
    /// Returns the Model Element XMIID that was loaded from the file.
    public String getReloadedModelElementXMIID() {
        return meidValue;
    }
    /// Sets the Model Element XMIID that will be persisted to the file.
    public void setReloadedModelElementXMIID(String newVal) {
        meidValue = newVal;
    }
    /// Returns the Toplevel Element XMIID that was loaded from the file.
    public String getReloadedTopLevelXMIID() {
        return topLevelMEIDValue;
    }
    /// Sets the Toplevel Element XMIID that will be persisted to the file.
    public void setReloadedTopLevelXMIID(String newVal) {
        topLevelMEIDValue = newVal;
    }
    /// Returns the Toplevel Element XMIID that was loaded from the file.
    public String getReloadedPresentationXMIID() {
        return peidValue;
    }
    /// Sets the Toplevel Element XMIID that will be persisted to the file.
    public void setReloadedPresentationXMIID(String newVal) {
        peidValue = newVal;
    }
    /// Returns the Toplevel Element XMIID that was loaded from the file.
    public String getReloadedOwnerPresentationXMIID() {
        return m_ReloadedOwnerPresentationXMIID;
    }
    /// Sets the Toplevel Element XMIID that will be persisted to the file.
    public void setReloadedOwnerPresentationXMIID(String newVal) {
        m_ReloadedOwnerPresentationXMIID = newVal;
    }
    public IStrings getReferredElements() {
        return m_PresentationReferenceReferredElements;
    }
    public void setReferredElements(IStrings newVal) {
        m_PresentationReferenceReferredElements = newVal;
    }
    public boolean getWasModelElementDeleted() {
        return m_WasModelElementDeleted;
    }
    public void setWasModelElementDeleted(boolean newVal) {
        m_WasModelElementDeleted = newVal;
    }
    public boolean getFailedToCreateDrawEngine() {
        return m_FailedToCreateDrawEngine;
    }
    public void setFailedToCreateDrawEngine(boolean newVal) {
        m_FailedToCreateDrawEngine = newVal;
    }
    
    /**
     * This routine is called when an object needs to be created from scratch.  The user has dropped
     * a TS node on the tree and we need to create the appropriate model element and presentation elements and
     * tie them together.  After all that is done look at the initialization string and create the correct engine.
     *
     * @param pNamespace [in] The namespace to add the new item to
     * @param sInitializationString [in] The init string used to create this node
     * @param pCreatedPresentationElement [out] The presentatation element created if all goes well.
     * @param pCreatedElement [out] The model element created if all goes well.
     */
    public IElement createNew(INamespace space, String initStr) {
        IElement retEle = null;
        IDrawingAreaControl control = getDrawingArea();
        String metaType = ETBaseUI.getMetaType(this);
        IETNode etObj = (IETNode) getTSObject();
        IDiagram pDia = null;
        if (control != null) {
            pDia = control.getDiagram();
        }
        
        INodeVerification pNodeVer = getNodeVerification();
        IPresentationElement presEle = null;
        if (pDia != null && pNodeVer != null) {
            ETPairT < IElement, IPresentationElement > result = pNodeVer.createAndVerify(pDia, etObj, space, metaType, initStr);
            if (result != null) {
                retEle = result.getParamOne();
                presEle = result.getParamTwo();
            }
        }
        
        // If we have a good model element then create the presentation element and
        // hook the two up.
        if (retEle != null) {
            IPresentationElement pEle = createPresentationElement(retEle);
            if (pEle != null) {
                etObj.setPresentationElement(pEle);
                pEle.addSubject(retEle);
                if (presEle != null) {
                    IPresentationReference pRef = org.netbeans.modules.uml.ui.support.PresentationReferenceHelper.createPresentationReference(presEle, pEle);
                }
            }
        }
        
        // No need to perform any synchronization logic while
        // this element is new
        //setSynchState(SSK_IN_SYNCH_DEEP);
        
        return retEle;
    }
    
    /**
     * Creates the appropriate EdgeVerification
     *
     * @param pVerif [out,retval] The returned, created edge verification created through the factory
     */
    private INodeVerification getNodeVerification() {
        INodeVerification retObj = null;
        ICreationFactory factory = FactoryRetriever.instance().getCreationFactory();
        if (factory != null) {
            Object obj = factory.retrieveEmptyMetaType("RelationshipVerification", "NodeVerification", null);
            if (obj != null && obj instanceof INodeVerification) {
                retObj = (INodeVerification) obj;
            }
        }
        return retObj;
    }
    
    public void setResizable(boolean resizeable) {
        m_resizeable = resizeable;
    }
    
    public boolean resizable() {
        return m_resizeable;
    }
    
    public boolean isResizable() {
        return super.isResizable() && resizable();
    }
    
    public String getFormattedText() {
        return super.getFormattedText();
    }
    
    /*
     *  (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.products.ad.viewfactory.IETGraphObjectUI#isOnTheScreen(com.tomsawyer.editor.graphics.TSEGraphics)
     */
    public boolean isOnTheScreen(TSEGraphics g) {
        return ETBaseUI.isOnTheScreen(g,this);
    }
    
    /**
     * Gives the draw engine the chance to setup the node for the first time.
     */
    public void setOwner(com.tomsawyer.editor.TSENode tSENode) {
        super.setOwner(tSENode);
        
        IDrawEngine engine = getDrawEngine();
        if(engine != null) {
            engine.setupOwner();
        }
    }
    
    protected void nullifyOwner()
    {
        super.nullifyOwner();
        
        Debug.out.println("Nullifing the Owner");
    }
}
