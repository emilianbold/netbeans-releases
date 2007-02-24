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
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.products.ad.application.IMenuManager;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.ETBaseUI;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeLabelUI;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.IETGraphObjectUI;
import org.netbeans.modules.uml.ui.support.applicationmanager.IGraphPresentation;
import org.netbeans.modules.uml.ui.support.applicationmanager.ILabelPresentation;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchive;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETLabel;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ITSGraphObject;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TSLabelKind;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TSLabelPlacementKind;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import com.tomsawyer.drawing.TSLabel;
import com.tomsawyer.editor.TSEGraphWindow;
import com.tomsawyer.editor.TSENode;
import com.tomsawyer.editor.TSENodeLabel;
import com.tomsawyer.editor.TSEObject;
import com.tomsawyer.editor.TSEObjectUI;
//import com.tomsawyer.editor.state.TSEMoveSelectedState;
import com.tomsawyer.editor.tool.TSEMoveSelectedTool;
import com.tomsawyer.editor.ui.TSELabelUI;
import com.tomsawyer.editor.ui.TSELabelUI;
import com.tomsawyer.graph.TSGraphObject;
import org.openide.ErrorManager;

public class ETNodeLabel extends TSENodeLabel implements IETLabel {

	private ILabelPresentation m_presentation;

   int mSynchState = ISynchStateKind.SSK_UNKNOWN_SYNCH_STATE;
   
	/**
	 * Constructor of the class. This constructor should be implemented
	 * to enable <code>TSENodeLabel</code> inheritance.
	 */
	protected ETNodeLabel() {
		// call the equivalent constructor for the super class
		super();
                this.getLabelUI().setFont(com.tomsawyer.editor.TSEFont.SANS_SERIF_12);
                this.setResizability(this.RESIZABILITY_TIGHT_FIT);
                m_presentation = null;

		// perform class specific initialization here
		// ...
	}

	/**
	 * This method copies attributes of the source object to this 
	 * object. The source object has to be of the type compatible
	 * with this class (equal or derived). The method should make a
	 * deep copy of all instance variables declared in this class.
	 * Variables of simple (non-object) types are automatically copied
	 * by the call to the cloneAttributes method of the super class.
	 * It is called automatically when the clone method, inherited
	 * from the parent class, is called.
	 *
	 * @param sourceObject  the source from which all attributes must
	 *                      be copied
	 */
	public void copy(Object sourceObject) {
                m_presentation = null;
            
		// copy the attributes of the super class first
		super.copy(sourceObject);

		// copy any class specific attributes here
		// ...
		if (sourceObject instanceof ITSGraphObject)
			this.copy((ITSGraphObject) sourceObject);
	}

	// add class-specific methods, instance and class variables
	// ...
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ITSGraphObject#getObject()
	 */
	public TSEObject getObject() {
		return (TSEObject) this;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ITSGraphObject#getUI()
	 */
	public IETGraphObjectUI getETUI() {
		return super.getUI() instanceof IETGraphObjectUI ? (IETGraphObjectUI) super.getUI() : null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ITSGraphObject#isConnector()
	 */
	public boolean isConnector() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ITSGraphObject#isEdge()
	 */
	public boolean isEdge() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ITSGraphObject#isLabel()
	 */
	public boolean isLabel() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ITSGraphObject#isNode()
	 */
	public boolean isNode() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ITSGraphObject#isPathNode()
	 */
	public boolean isPathNode() {
		return false;
	}


	/*
	 *  (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ITSGraphObject#delete()
	 */
	public void delete() {
		TSENode node = (TSENode)getOwner();
		if (node != null)
		{
			TSELabelUI ui = this.getUI() instanceof TSELabelUI ? (TSELabelUI)getUI() : null;
			
			IDrawEngine drawEngine = this.getEngine();
			if (drawEngine != null)
			{
				drawEngine.onDiscardParentETElement();		
			}
			
			node.discard(this); 
			if (ui != null){
				ui.setOwner(null);
			}			
		}  			
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETLabel#createLabelCopy(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.core.support.umlsupport.IETPoint, org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement)
	 */
	public IETLabel createLabelCopy(IDiagram pTargetDiagram, IETPoint pCenter, IPresentationElement pOwner) {
		
		return null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETLabel#getLabel()
	 */
	public TSLabel getLabel() {
		return this;
	}

	public int getLabelKind() {

		ETGenericNodeLabelUI pLabelView = this.getUI() instanceof ETGenericNodeLabelUI ? (ETGenericNodeLabelUI)getUI() : null;

		return (pLabelView != null)? pLabelView.getLabelKind():0;
	}

	public int getLabelPlacement() {

		ETGenericNodeLabelUI pLabelView = this.getUI() instanceof ETGenericNodeLabelUI ? (ETGenericNodeLabelUI)getUI() : null;

		return (pLabelView != null)? pLabelView.getPlacement():0;
	}

	public IETPoint getSpecifiedXY() {
		ETGenericNodeLabelUI pLabelView = this.getUI() instanceof ETGenericNodeLabelUI ? (ETGenericNodeLabelUI)getUI() : null;

		return (pLabelView != null)? pLabelView.getSpecifiedXY():null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETLabel#getLabelView()
	 */
	public TSELabelUI getLabelView() {
		return getUI() instanceof TSELabelUI ? (TSELabelUI)getUI() : null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETLabel#getParentETElement()
	 */
	public IETGraphObject getParentETElement() {
		return getOwner() instanceof IETGraphObject ? (IETGraphObject)getOwner() : null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETLabel#getParentPresentationElement()
	 */
	public IPresentationElement getParentPresentationElement() {
		IETGraphObject parent = this.getParentETElement();
		return parent != null ? parent.getPresentationElement() : null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETLabel#onContextMenu(org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu, int, int)
	 */
	public void onContextMenu(IProductContextMenu pContextMenu, int logicalX, int logicalY) {
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETLabel#onContextMenuHandleSelection(org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu, org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem)
	 */
	public void onContextMenuHandleSelection(IProductContextMenu pContextMenu, IProductContextMenuItem pMenuItem) {
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETLabel#reposition()
	 */
	public void reposition() {

		IDrawEngine pLabelView = this.getEngine();

		if (pLabelView != null && pLabelView instanceof ILabelDrawEngine) {
			((ILabelDrawEngine) pLabelView).reposition();
		}
	}



	public void setLabelKind(int newValue)
	{
		ETGenericNodeLabelUI pLabelView = this.getUI() instanceof ETGenericNodeLabelUI ? (ETGenericNodeLabelUI)getUI() : null;

		if (pLabelView != null)
		{
			pLabelView.setLabelKind(newValue);
		}
	}

	public void setLabelPlacement(int newValue)
	{
		ETGenericNodeLabelUI pLabelView = this.getUI() instanceof ETGenericNodeLabelUI ? (ETGenericNodeLabelUI)getUI() : null;

		if (pLabelView != null)
		{
			pLabelView.setPlacement(newValue);
		}
	}

	public void setSpecifiedXY(IETPoint value)
	{
		ETGenericNodeLabelUI pLabelView = this.getUI() instanceof ETGenericNodeLabelUI ? (ETGenericNodeLabelUI)getUI() : null;

		if (pLabelView != null)
		{
			pLabelView.setSpecifiedXY(value);
			pLabelView.setPlacement(TSLabelPlacementKind.TSLPK_SPECIFIED_XY);
		}

	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETLabel#setLabelView(com.tomsawyer.editor.ui.TSELabelUI)
	 */
	public void setLabelView(TSELabelUI value) {
		setUI(value);
	}


	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#affectModelElementDeletion()
	 */
	public void affectModelElementDeletion() {
		IETGraphObjectUI ui = this.getETUI();
		if (ui != null) {
			IDrawEngine de = ui.getDrawEngine();
			if (de != null) {
				de.affectModelElementDeletion();
			}
		}
	}


	public IDiagram getDiagram() {
		IDrawingAreaControl da = getDrawingAreaControl();
		return da != null ? da.getDiagram() : null;
	}

	public IDrawingAreaControl getDrawingAreaControl() {
		return getETUI() != null ? getETUI().getDrawingArea() : null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#getEngine()
	 */
	public IDrawEngine getEngine() {
		return getETUI() != null ? getETUI().getDrawEngine() : null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#getObjectView()
	 */
	public TSEObjectUI getObjectView() {
		
		return null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#getPresentationElement()
	 */
	public IPresentationElement getPresentationElement() {
		return m_presentation;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#getReferredElements()
	 */
	public IStrings getReferredElements() {
		IETGraphObjectUI ui = getETUI();
		return ui != null ? ui.getReferredElements() : null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#getReloadedModelElementXMIID()
	 */
	public String getReloadedModelElementXMIID() {
		IETGraphObjectUI ui = getETUI();
		return ui != null ? ui.getReloadedModelElementXMIID() : null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#getReloadedOwnerPresentationXMIID()
	 */
	public String getReloadedOwnerPresentationXMIID() {
		IETGraphObjectUI ui = getETUI();
		return ui != null ? ui.getReloadedOwnerPresentationXMIID() : null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#getReloadedPresentationXMIID()
	 */
	public String getReloadedPresentationXMIID() {
		IETGraphObjectUI ui = getETUI();
		return ui != null ? ui.getReloadedPresentationXMIID() : null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#getReloadedTopLevelXMIID()
	 */
	public String getReloadedTopLevelXMIID() {
		IETGraphObjectUI ui = getETUI();
		return ui != null ? ui.getReloadedTopLevelXMIID() : null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#getTopLevelXMIID()
	 */
	public String getTopLevelXMIID() {
		
		return null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#load(org.netbeans.modules.uml.ui.support.archivesupport.IProductArchive)
	 */
	public void load(IProductArchive pProductArchive) {
		//implemented from BaseGraphObject.cpp
		if (pProductArchive != null) {
			String reloadedXMIID = getReloadedPresentationXMIID();
			IProductArchiveElement foundEle = null;
			if (reloadedXMIID.length() > 0) {
				foundEle = pProductArchive.getElement(reloadedXMIID);
			}
			if (foundEle != null) {
				readFromArchive(pProductArchive, foundEle);
			}
		}
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#modelElementDeleted(org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets)
	 */
	public void modelElementDeleted(INotificationTargets pTargets) {
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#modelElementHasChanged(org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets)
	 */
	public void modelElementHasChanged(INotificationTargets pTargets) {
		IDrawEngine pEng = getParentETElement().getETUI().getDrawEngine();
		if (pEng != null && getLabelKind() != TSLabelKind.TSLK_STEREOTYPE) {
			pEng.modelElementHasChanged(pTargets);

			/// Now tell the label manager
			ILabelManager labelMgr = pEng.getLabelManager();
			if (labelMgr != null) {
				labelMgr.modelElementHasChanged(pTargets);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#onGraphEvent(int)
	 */
	public void onGraphEvent(int nKind) {
		ETBaseUI.onGraphEvent(nKind, getETUI());
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#onKeydown(int, int)
	 */
	public boolean onKeydown(int nKeyCode, int nShift) {
		return ETBaseUI.onKeyDown(nKeyCode, nShift, getETUI());
	}

        /* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#onCharTyped(char)
	 */
        public boolean onCharTyped(char ch){
	        return ETBaseUI.onCharTyped(ch, getETUI());
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#onKeyup(int, int)
	 */
	public boolean onKeyup(int KeyCode, int Shift) {
		
		return false;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#postLoad()
	 */
	public void postLoad() {
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#readData(int)
	 */
	public void readData(int pTSEData) {
		
	}

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#readFromArchive(org.netbeans.modules.uml.ui.support.archivesupport.IProductArchive, org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement)
    */
   public void readFromArchive(IProductArchive prodArch, IProductArchiveElement archEle)
   {
      if (prodArch != null && archEle != null)
      {
         TSEObjectUI ui = getUI();
         if (ui != null && ui instanceof ETGenericNodeLabelUI)
         {
            ((ETGenericNodeLabelUI)ui).readFromArchive(prodArch, archEle);
         }
      }  
   }

	/**
	 * Saves this element to the etlp file
	 *
	 * @param pProductArchive [in] The archive file we're serializing to.
	 */
	public void save(IProductArchive prodArch) {
		TSEObjectUI ui = getUI();
		if (ui != null && ui instanceof IETGraphObjectUI) {
			ETBaseUI.save(prodArch, (IETGraphObjectUI) ui);
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#setDiagram(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram)
	 */
	public void setDiagram(IDiagram value) {
		

	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#setObjectView(com.tomsawyer.editor.TSEObjectUI)
	 */
	public void setObjectView(TSEObjectUI value) {
		

	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#setReferredElements(org.netbeans.modules.uml.core.support.umlsupport.IStrings)
	 */
	public void setReferredElements(IStrings value) {
		IETGraphObjectUI ui = getETUI();
		if (ui != null) {
			ui.setReferredElements(value);
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#setReloadedModelElementXMIID(java.lang.String)
	 */
	public void setReloadedModelElementXMIID(String value) {
		IETGraphObjectUI ui = getETUI();
		if (ui != null) {
			ui.setReloadedModelElementXMIID(value);
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#setReloadedOwnerPresentationXMIID(java.lang.String)
	 */
	public void setReloadedOwnerPresentationXMIID(String value) {
		IETGraphObjectUI ui = getETUI();
		if (ui != null) {
			ui.setReloadedOwnerPresentationXMIID(value);
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#setReloadedPresentationXMIID(java.lang.String)
	 */
	public void setReloadedPresentationXMIID(String value) {
		IETGraphObjectUI ui = getETUI();
		if (ui != null) {
			ui.setReloadedPresentationXMIID(value);
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#setReloadedTopLevelXMIID(java.lang.String)
	 */
	public void setReloadedTopLevelXMIID(String value) {
		IETGraphObjectUI ui = getETUI();
		if (ui != null) {
			ui.setReloadedTopLevelXMIID(value);
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#sizeToContents()
	 */
	public void sizeToContents() {
		IETGraphObjectUI ui = this.getETUI();
		IDrawEngine de = ui != null ? ui.getDrawEngine() : null;
		if (de != null) {
			de.sizeToContents();
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#writeData(int)
	 */
	public void writeData(int pTSEDataMgr) {
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#writeToArchive(org.netbeans.modules.uml.ui.support.archivesupport.IProductArchive, org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement)
	 */
	public void writeToArchive(IProductArchive prodArch, IProductArchiveElement archEle)
   {
      if (prodArch != null && archEle != null)
      {
         TSEObjectUI ui = getUI();
         if (ui != null && ui instanceof ETGenericNodeLabelUI)
         {
            ((ETGenericNodeLabelUI)ui).writeToArchive(prodArch, archEle);
         }
      }
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ITSGraphObject#copy(org.netbeans.modules.uml.ui.support.viewfactorysupport.ITSGraphObject)
	 */
	public void copy(ITSGraphObject objToClone) {
		

	}

	public void onContextMenu(IMenuManager manager) {
		ETBaseUI.onContextMenu(manager, getETUI());
	}

	public IElement create(INamespace space, String initStr) {
		IElement retEle = null;
		retEle = ETBaseUI.create(space, initStr, getETUI());
		return retEle;
	}

	public void attach(IElement modEle, String initStr) {
		ETBaseUI.attach(modEle, initStr, getETUI());
	}

	public void onPostAddLink(IETGraphObject newLink, boolean isFromNode) {
		ETBaseUI.onPostAddLink(newLink, isFromNode, getETUI());
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
            m_presentation = value instanceof ILabelPresentation ? (ILabelPresentation) value : null;
            // Make sure the back pointer is in sync
            if (m_presentation != null && getETUI() != null)
            {
                m_presentation.setTSLabel(this);
            }			
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#create(org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace, java.lang.String, org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IElement)
	 */
	public void create(INamespace pNamespace, String sInitializationString, IPresentationElement pCreatedPresentationElement, IElement pCreatedElement) {
		

	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#getGraphObject()
	 */
	public TSGraphObject getGraphObject() {
		
		return null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#getInitializationString()
	 */
	public String getInitializationString() {
		
		return null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#getOLEDragElements(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement[])
	 */
	public ETList <IElement> getDragElements() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#getSynchState()
	 */
	public int getSynchState() {
		return mSynchState;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#getWasModelElementDeleted()
	 */
	public boolean getWasModelElementDeleted() {
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
	public boolean handleLeftMouseBeginDrag(IETPoint pStartPos, IETPoint pCurrentPos) {
		
		return false;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#handleLeftMouseDrag(org.netbeans.modules.uml.core.support.umlsupport.IETPoint, org.netbeans.modules.uml.core.support.umlsupport.IETPoint)
	 */
	public boolean handleLeftMouseDrag(IETPoint pStartPos, IETPoint pCurrentPos) {
		
		return false;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#handleLeftMouseDrop(org.netbeans.modules.uml.core.support.umlsupport.IETPoint, org.netbeans.modules.uml.core.metamodel.core.foundation.IElement[], boolean)
	 */
	public boolean handleLeftMouseDrop(IETPoint ptCurrentPos, IElement[] pElements, boolean bMoving) {
		
		return false;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#invalidate()
	 */
	public void invalidate() {
		

	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#onPreDeleteLink(org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject, boolean)
	 */
	public long onPreDeleteLink(IETGraphObject pLinkAboutToBeDeleted, boolean bIsFromNode) {
		
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#performDeepSynch()
	 */
	public long performDeepSynch() {
		
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#resetDrawEngine(java.lang.String)
	 */
	public long resetDrawEngine(String sInitializationString) {
		
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#setEngineParent(com.tomsawyer.editor.TSEObjectUI)
	 */
	public long setEngineParent(TSEObjectUI pObjectView) {
		
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#setInitializationString(java.lang.String)
	 */
	public void setInitializationString(String value) {
		

	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#setSynchState(int)
	 */
	public void setSynchState(int value) {
      mSynchState = value;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#transform(java.lang.String)
	 */
	public IPresentationElement transform(String typeName) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject#validate(org.netbeans.modules.uml.core.metamodel.diagrams.IGraphObjectValidation)
	 */
	public long validate(IGraphObjectValidation pValidationKind) {
		checkConnectionToPresentationElement( pValidationKind );
		return 0;
	}
	
	private void checkConnectionToPresentationElement(IGraphObjectValidation pValidationKind) {
		if (pValidationKind == null) {
			//error
			return;
		}

		boolean bCheckConnectionToElement =
			pValidationKind.getValidationKind(
				IDiagramValidateKind.DVK_VALIDATE_CONNECTIONTOELEMENT);

		// Check connection to our element
		if (bCheckConnectionToElement) {
			int dvResult = IDiagramValidateResult.DVR_INVALID;

			IPresentationElement pe = getPresentationElement();

			IGraphPresentation graphPE = null;
			if (graphPE instanceof IGraphPresentation)
				graphPE = (IGraphPresentation) pe;

			if (graphPE != null) {
				// Clear our cached ME so that a proper connection can be verified
				//			 graphPE.clearModelElementCache();
			}

			// Get the IElement for this graph object
			IElement element =
				TypeConversions.getElement((IETGraphObject) this);

			if (pe != null && element != null) {
				// See if the element knows about this presentation elment
				boolean bPEIsPresent = element.isPresent(pe);
				if (!bPEIsPresent) {
					// We've got to reconnect
					element.addPresentationElement(pe);
					bPEIsPresent = element.isPresent(pe);
				}

				if (bPEIsPresent) {
					dvResult = IDiagramValidateResult.DVR_VALID;
				}
			}

			pValidationKind.setValidationResult(
				IDiagramValidateKind.DVK_VALIDATE_CONNECTIONTOELEMENT,
				dvResult);
		}
	}

	public void setText(Object text)
	{
		this.setText(text);
	}

	public void setText(String value)
	{
		super.setText(value);
		super.setTag(value);
		IDrawEngine pLabelView = this.getEngine();
		if (pLabelView != null && pLabelView instanceof ILabelDrawEngine)
		{
			((ILabelDrawEngine) pLabelView).setText(value);
		}
	}
	
	public String getText()
	{
		String sText = "";
		IDrawEngine pLabelView = this.getEngine();
		if (pLabelView != null && pLabelView instanceof ILabelDrawEngine)
		{
			sText = ((ILabelDrawEngine) pLabelView).getText();
		}
		if (sText == null || sText.length() == 0)
		{
			sText = super.getText();
		}
	   return sText;
	}
   /* (non-Javadoc)
    * @see com.tomsawyer.editor.TSEObject#getToolTipText()
    */
   public String getToolTipText()
   {
		//Disable label tooltips
		return null;
   }
   
	protected TSEGraphWindow getGraphWindow()
	{
		IDrawingAreaControl ctrl = getDrawingAreaControl();
		return ctrl != null ? ctrl.getGraphWindow() : null;
	}

	public void setSelected(boolean selected){
		//if (this.getGraphWindow() != null && getGraphWindow().getCurrentState() instanceof TSEMoveSelectedState)
		if (this.getGraphWindow() != null && getGraphWindow().getCurrentState() instanceof TSEMoveSelectedTool)
		{
			ETSystem.out.println("Warning: can not change selection lists while in TSEMoveSelectedState state.");
			return;
		}
		super.setSelected(selected);
	}
}
