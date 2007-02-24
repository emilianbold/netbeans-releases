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



package org.netbeans.modules.uml.ui.products.ad.viewfactory;

import java.util.List;
import java.awt.Graphics;
import java.awt.RenderingHints;

import com.tomsawyer.util.TSProperty;
import com.tomsawyer.editor.TSEEdge;
import com.tomsawyer.editor.TSEGraphWindow;
import com.tomsawyer.editor.TSEObject;
import com.tomsawyer.editor.TSEObjectUI;
import com.tomsawyer.editor.graphics.TSEGraphics;
import com.tomsawyer.editor.ui.TSEDefaultEdgeUI;
import com.tomsawyer.editor.TSEColor;
import org.netbeans.modules.uml.common.ETException;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ICreationFactory;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IStrings;
import org.netbeans.modules.uml.ui.support.applicationmanager.DrawingFactory;
import org.netbeans.modules.uml.ui.support.applicationmanager.IEdgePresentation;
import org.netbeans.modules.uml.ui.support.applicationmanager.IPresentationTypesMgr;
import org.netbeans.modules.uml.ui.support.applicationmanager.PresentationTypesMgrImpl;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchive;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveDefinitions;
import org.netbeans.modules.uml.ui.support.relationshipVerification.IEdgeVerification;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETEdge;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETEdgeUI;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
import org.netbeans.modules.uml.ui.products.ad.applicationcore.ADPresentationTypesMgrImpl;
import org.netbeans.modules.uml.ui.products.ad.drawengines.ETDrawEngineFactory;
import org.netbeans.modules.uml.ui.products.ad.graphobjects.ETEdge;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ITSGraphObject;

/*
 * 
 * @author KevinM
 *
 */
public class ETGenericEdgeUI extends TSEDefaultEdgeUI implements IETEdgeUI {

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
	protected RenderingHints qualityHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	
	/*
	 * 
	 */
	public ETGenericEdgeUI()
	{
		super();
		// we draw the arrowheads
		setArrowType(NO_ARROW);
	}
	
	/*
	 * 
	 */
	private boolean initDrawEngine() throws ETException {
		if (this.getDrawEngineClass() != null && getDrawEngineClass().length() > 0) {
			this.setDrawEngine(ETDrawEngineFactory.createDrawEngine(this));
			return this.drawEngine != null;
		} else
			return false;
	}
		
	/**
	 * This method draws the object represented by this UI.
	 * @param graphics the <code>TSEGraphics</code> object onto which
	 * the UI is being drawn.
	 */
	public void draw(TSEGraphics graphics) {
		if (getDrawEngine() != null) {
			RenderingHints prevHint = graphics.getRenderingHints();
			qualityHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

			graphics.setRenderingHints(qualityHints);

			IDrawInfo drawInfo = getDrawInfo(graphics);
			if (drawInfo != null)
				getDrawEngine().doDraw(drawInfo);
			else
				super.draw(graphics);

			graphics.setRenderingHints(prevHint);
		}
		else
			super.draw(graphics);
	}

	/**
	 * Retrieves the graphics context for the node ui.
	 * 
	 * @return The graphics context.
	 */
	public IDrawInfo getDrawInfo() {
		TSEGraphWindow window = getGraphWindow();
		if (window != null) {
			Graphics g = window.getGraphics();
			return g instanceof TSEGraphics ? getDrawInfo((TSEGraphics) g) : null;
		} else
			return null;
	}

	/*
	 * Returns the GraphWindow.
	 */
	public TSEGraphWindow getGraphWindow()
	{
		return ETBaseUI.getGraphWindow(this);
	}
	
	/*
	 * Returns World points, (Logical)
	 */
	public IETRect getLogicalBounds()
	{
		return ETBaseUI.getLogicalBounds(this);
	}

	/*
	 * Returns the device bounding rect.
	 */
	public IETRect getDeviceBounds()
	{
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
	
		if (retVal != null)
		{
			retVal.setIsTransparent(false);
			retVal.setIsBorderDrawn(false);
		}
		// TODO: Determine what the DrawinToMainDrawingArea and AlwaysSetFont
		//       Should be set to.

		return retVal;
	}

	/*
	 *  (non-Javadoc)
	 * @see com.tomsawyer.editor.TSEObjectUI#drawOutline(com.tomsawyer.editor.graphics.TSEGraphics)
	 */
	public void drawOutline(TSEGraphics graphics) {
		draw(graphics);
	}

	/*
	 *  (non-Javadoc)
	 * @see com.tomsawyer.editor.TSEObjectUI#drawSelected(com.tomsawyer.editor.graphics.TSEGraphics)
	 */
	public void drawSelected(TSEGraphics graphics) {
		draw(graphics);
	}

	/*
	 *  (non-Javadoc)
	 * @see com.tomsawyer.editor.TSEObjectUI#drawSelectedOutline(com.tomsawyer.editor.graphics.TSEGraphics)
	 */
	public void drawSelectedOutline(TSEGraphics graphics) {
		draw(graphics);
	}

	/*
	 *  (non-Javadoc)
	 * @see com.tomsawyer.editor.TSEObjectUI#reset()
	 */
	public void reset() {
		super.reset();
		this.setHighlightedColor(TSEColor.paleBlue);

		this.peidValue = this.ZERO_VALUE;
		this.meidValue = this.ZERO_VALUE;
		this.topLevelMEIDValue = this.ZERO_VALUE;
		this.initStringValue = this.ZERO_VALUE;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see com.tomsawyer.editor.TSEObjectUI#copy(com.tomsawyer.editor.TSEObjectUI)
	 */
   public void copy(TSEObjectUI sourceUI)
   {
      super.copy(sourceUI);
      ETGenericEdgeUI sourceObjUI = (ETGenericEdgeUI)sourceUI;
      this.meidValue = sourceObjUI.meidValue;
      this.topLevelMEIDValue = sourceObjUI.topLevelMEIDValue;
      this.modelElement = sourceObjUI.modelElement;

      this.setInitStringValue(sourceObjUI.getInitStringValue());
      this.setDrawEngineClass(sourceObjUI.getDrawEngineClass());
   }

	/*
	 *  (non-Javadoc)
	 * @see com.tomsawyer.editor.TSEObjectUI#getProperties()
	 */
	public List getProperties() {
            /* commented by jyothi
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

	/*
	 *  (non-Javadoc)
	 * @see com.tomsawyer.editor.TSEObjectUI#setProperty(com.tomsawyer.util.TSProperty)
	 */
	public void setProperty(TSProperty property) {
		//String attrString = (String) property.getValue(); //Jyothi
		if (IProductArchiveDefinitions.PRESENTATIONELEMENTID_STRING.equals(property.getName())) {
                    String attrString = (String) property.getValue();
			this.peidValue = attrString;
                }
		else {
			super.setProperty(property);
                }
	}

	/*
	 *  (non-Javadoc)
	 * @see com.tomsawyer.editor.TSEObjectUI#getChangedProperties()
	 */
	public List getChangedProperties() {
		List list = super.getChangedProperties();

		IPresentationElement pPE = ((IETGraphObject)getTSObject()).getPresentationElement();
		if (pPE != null)
		{
			// Get the presentation element id
			String presEleId = pPE.getXMIID();
			if (presEleId != null && presEleId.length() > 0)
			{
				list.add(new TSProperty(IProductArchiveDefinitions.PRESENTATIONELEMENTID_STRING, presEleId));
			}
		}
		return list;
	}

	/*
	 *  (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.viewfactory.IETGraphObjectUI#getDrawEngine()
	 */
	public IDrawEngine getDrawEngine() {
		if (drawEngine == null) {
			try {
				this.initDrawEngine();
			} catch (Exception e) {
				return null;
			}
		}

		return drawEngine;
	}

	/*
	 *  (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.viewfactory.IETGraphObjectUI#getDrawEngineClass()
	 */
	public String getDrawEngineClass() {
		return drawEngineClass;
	}

	/*
	 *  (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.viewfactory.IETGraphObjectUI#setDrawEngineClass(java.lang.String)
	 */
	public void setDrawEngineClass(String string) {
		drawEngineClass = string;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.viewfactory.IETGraphObjectUI#setDrawEngine(org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine)
	 */
	public void setDrawEngine(IDrawEngine newDrawEngine) {
		try
		{
			if (newDrawEngine != drawEngine)
			{
				drawEngine = newDrawEngine;
//				if (drawEngine != null)
//					drawEngine.init();

			}			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/*
	 * 
	 */
	public String getPeidValue() {
		return peidValue;
	}
	
	public String getMeidValue() {
		return this.meidValue;
	}
	

	/*
	 * 
	 */
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

	/*
	 * 
	 */
	public void setArchiveElement(IProductArchiveElement element) {
		this.archiveElement = element;
		this.readFromArchive(element);
	}

	/*
	 * 
	 */
	private void readFromArchive(IProductArchiveElement element) {
		if (element != null) {
			this.meidValue = element.getAttributeString(IProductArchiveDefinitions.MEID_STRING);
			this.topLevelMEIDValue = element.getAttributeString(IProductArchiveDefinitions.TOPLEVELID_STRING);
			setInitStringValue(element.getAttributeString(IProductArchiveDefinitions.INITIALIZATIONSTRING_STRING));

			IProductArchiveElement engineElement = element.getElement(IProductArchiveDefinitions.ENGINENAMEELEMENT_STRING);
			setDrawEngineClass(engineElement.getAttributeString(IProductArchiveDefinitions.ENGINENAMEATTRIBUTE_STRING));
		}
	}

	public void readFromArchive(IProductArchive prodArch, IProductArchiveElement archEle) {
		archiveElement = archEle;
		ETBaseUI.readFromArchive(prodArch, archEle, this);
	}

	public void writeToArchive(IProductArchive prodArch, IProductArchiveElement archEle) {
		ETBaseUI.writeToArchive(prodArch, archEle, this);
	}

	/*
	 *  (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.viewfactory.IETGraphObjectUI#getDrawingArea()
	 */
	public IDrawingAreaControl getDrawingArea() {
		return ETBaseUI.getDrawingArea(this);
	}

	/*
	 *  (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.viewfactory.IETGraphObjectUI#setDrawingArea(org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl)
	 */
	public void setDrawingArea(IDrawingAreaControl control) {
		//this.drawingArea = control;
	}

	/*
	 *  (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.viewfactory.IETGraphObjectUI#getInitStringValue()
	 */
	public String getInitStringValue() 
	{
		String retStr = initStringValue;
		String className = this.getClass().getName();
		if (!retStr.startsWith(className))
		{
			retStr = className + " " + initStringValue;
		}
		return  retStr;
	}

	/*
	 *  (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.viewfactory.IETGraphObjectUI#setInitStringValue(java.lang.String)
	 */
	public void setInitStringValue(String string) {
		this.initStringValue = string;
	}

	/*
	 *  (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.viewfactory.IETGraphObjectUI#getTSObject()
	 */
	public ITSGraphObject getTSObject() {
		return getOwner() instanceof ITSGraphObject ? (ITSGraphObject) getOwner() : null;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.viewfactory.IETGraphObjectUI#getReloadedModelElementXMIID()
	 */
	public String getReloadedModelElementXMIID() {
		return meidValue;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.viewfactory.IETGraphObjectUI#setReloadedModelElementXMIID(java.lang.String)
	 */
	public void setReloadedModelElementXMIID(String newVal) {
		meidValue = newVal;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.viewfactory.IETGraphObjectUI#getReloadedTopLevelXMIID()
	 */
	public String getReloadedTopLevelXMIID() {
		return topLevelMEIDValue;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.viewfactory.IETGraphObjectUI#setReloadedTopLevelXMIID(java.lang.String)
	 */
	public void setReloadedTopLevelXMIID(String newVal) {
		topLevelMEIDValue = newVal;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.viewfactory.IETGraphObjectUI#getReloadedPresentationXMIID()
	 */
	public String getReloadedPresentationXMIID() {
		return peidValue;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.viewfactory.IETGraphObjectUI#setReloadedPresentationXMIID(java.lang.String)
	 */
	public void setReloadedPresentationXMIID(String newVal) {
		peidValue = newVal;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.viewfactory.IETGraphObjectUI#getReloadedOwnerPresentationXMIID()
	 */
	public String getReloadedOwnerPresentationXMIID() {
		return m_ReloadedOwnerPresentationXMIID;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.viewfactory.IETGraphObjectUI#setReloadedOwnerPresentationXMIID(java.lang.String)
	 */
	public void setReloadedOwnerPresentationXMIID(String newVal) {
		m_ReloadedOwnerPresentationXMIID = newVal;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.viewfactory.IETGraphObjectUI#getReferredElements()
	 */
	public IStrings getReferredElements() {
		return m_PresentationReferenceReferredElements;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.viewfactory.IETGraphObjectUI#setReferredElements(org.netbeans.modules.uml.core.support.umlsupport.IStrings)
	 */
	public void setReferredElements(IStrings newVal) {
		m_PresentationReferenceReferredElements = newVal;
	}

	/**
	 * Creates a new IEdgePresentation and returns true and pElem!=0 if successful.
	 *
	 * @param pElement [in] The element that this edge view represents
	 * @param pElem [out,retval] Creates the appropriate IPresentationElement for this node (right now it's
	 * always an INodePresentation.)
	 */
	public IPresentationElement createPresentationElement(IElement pEle) {
		IPresentationElement retObj = null;
      IPresentationTypesMgr typesMgr = null;
      
		IDrawingAreaControl control = getDrawingArea();
      if (control != null)
      {
         typesMgr = control.getPresentationTypesMgr();
      }
      else
      {
         // This is here so we can support copy/paste when the ui is in the
         // clipboard and not on a control.
         typesMgr = new ADPresentationTypesMgrImpl();
      }
		if (typesMgr != null && pEle != null) {

			// The type to create depends on the metatype of the incoming IElement
			String incomingMetaType = pEle.getElementType();
			String peToCreate = "";
			if (typesMgr != null) {
				peToCreate = typesMgr.getPresentationElementMetaType(incomingMetaType, initStringValue);
			}
			if (peToCreate == null || peToCreate.length() == 0) {
				peToCreate = "EdgePresentation";
			}

			IEdgePresentation edgePres = DrawingFactory.retrieveEdgePresentationMetaType(peToCreate);
			if (edgePres != null) {
				edgePres.setTSEdge((ETEdge) getTSObject());
				retObj = edgePres;
			}
		}
		return retObj;
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
	 * Creates the appropriate EdgeVerification
	 *
	 * @param pVerif [out,retval] The returned, created edge verification created through the factory
	 */
	private IEdgeVerification getEdgeVerification()
	{
		IEdgeVerification retObj = null;
		ICreationFactory factory = FactoryRetriever.instance().getCreationFactory();
		if (factory != null)
		{
			Object obj = factory.retrieveEmptyMetaType("RelationshipVerification", "EdgeVerification", null);
			if (obj != null && obj instanceof IEdgeVerification)
			{
				retObj = (IEdgeVerification)obj;
			}
		}
		return retObj;
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
	public IElement createNew(INamespace space, String initStr)
	{
		IElement retEle = null;
		IETEdge etObj = (IETEdge)getTSObject();
		IEdgeVerification pEdgeVerification = getEdgeVerification();
		String metaType = ETBaseUI.getMetaType(this);
		if (pEdgeVerification != null)
		{
			// Verify and create the edge relationship
			retEle = pEdgeVerification.verifyAndCreateEdgeRelation(etObj, space, metaType, initStr);
		}
		
		// If we have a good model element then create the presentation element and
		// hook the two up.
		if (retEle != null)
		{
			IPresentationElement pEle = createPresentationElement(retEle);
			if (pEle != null)
			{
				pEle.addSubject(retEle);
				etObj.setPresentationElement(pEle);
			}

			// There are times when the verify logic actually changes the metatype of what gets
			// created.  Get the metatype of the modelelement that got created and if it's different
			// then just post a reset on the drawing area for this product element
			IDrawingAreaControl control = getDrawingArea();
			if (control != null)
			{
				String actualMetaType = retEle.getElementType();
				if (actualMetaType != null && !actualMetaType.equals(metaType))
				{
					control.resetDrawEngine2(etObj);
				}
			}
		}
		return retEle;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.viewfactory.IETGraphObjectUI#isOnTheScreen(com.tomsawyer.editor.graphics.TSEGraphics)
	 */
	public boolean isOnTheScreen(TSEGraphics g)
	{
		return ETBaseUI.isOnTheScreen(g,this);
	}
}
