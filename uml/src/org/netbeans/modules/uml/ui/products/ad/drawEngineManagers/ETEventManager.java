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



package org.netbeans.modules.uml.ui.products.ad.drawEngineManagers;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IGraphObjectValidation;
import org.netbeans.modules.uml.ui.controls.drawingarea.IUIDiagram;
import org.netbeans.modules.uml.ui.products.ad.application.IMenuManager;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IEventManager;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import com.tomsawyer.editor.TSENode;

/**
 * @author KevinM
 *
 */
public class ETEventManager implements IEventManager {

	/**
	 * 
	 */
	public ETEventManager() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IEventManager#onPostAddLink(org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject, boolean)
	 */
	public long onPostAddLink(IETGraphObject pNewLink, boolean bIsFromNode) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IEventManager#onPreDeleteLink(org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject, boolean)
	 */
	public long onPreDeleteLink(IETGraphObject pLinkAboutToBeDeleted, boolean bIsFromNode) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IEventManager#resetEdges()
	 */
	public long resetEdges() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IEventManager#onContextMenu(org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu, int, int)
	 */
	public long onContextMenu(IProductContextMenu pContextMenu, int logicalX, int logicalY) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IEventManager#onContextMenuHandleSelection(org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu, org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem)
	 */
	public long onContextMenuHandleSelection(IProductContextMenu pContextMenu, IProductContextMenuItem pMenuItem) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IEventManager#setSensitivityAndCheck(org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu, org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem, int)
	 */
	public boolean setSensitivityAndCheck(IProductContextMenu pContextMenu, IProductContextMenuItem pMenuItem, int buttonKind) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IGraphObjectManager#onGraphEvent(int)
	 */
	public void onGraphEvent(int nKind) {
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IGraphObjectManager#validate(org.netbeans.modules.uml.core.metamodel.diagrams.IGraphObjectValidation)
	 */
	public long validate(IGraphObjectValidation pValidationKind) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IEventManager#onContextMenu(org.netbeans.modules.uml.ui.products.ad.application.IMenuManager)
	 */
	public void onContextMenu(IMenuManager manager)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IEventManager#hasEdgesToReset()
	 */
	public boolean hasEdgesToReset()
	{
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IGraphObjectManager#getParentETGraphObject()
	 */
	public IETGraphObject getParentETGraphObject() {
		return m_parentETGraphObject;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IGraphObjectManager#setParentETGraphObject(org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject)
	 */
	public void setParentETGraphObject(IETGraphObject value) {
		m_parentETGraphObject = value;
	}
	
	public TSENode getOwnerNode() {
		return TypeConversions.getOwnerNode(m_parentETGraphObject);
	}
	
	public IPresentationElement getParentPresentationElement() {
		return TypeConversions.getPresentationElement(m_parentETGraphObject);
	}
	
	public IDrawEngine getParentDrawEngine() {
		return TypeConversions.getDrawEngine(m_parentETGraphObject);
	}
	
	public IDrawingAreaControl getDrawingArea() {
		IDiagram diagram = getDiagram();
		if(diagram instanceof IUIDiagram) {
			IUIDiagram uiDiagram = (IUIDiagram)diagram;
			return uiDiagram.getDrawingArea(); 
		}
		return null;
	}
	
	public IDiagram getDiagram() {
		IDrawEngine drawEngine = TypeConversions.getDrawEngine(m_parentETGraphObject);
		if(drawEngine != null) {
			return drawEngine.getDiagram();
		}
		return null;
	}
	
	protected IETGraphObject m_parentETGraphObject = null;
}

