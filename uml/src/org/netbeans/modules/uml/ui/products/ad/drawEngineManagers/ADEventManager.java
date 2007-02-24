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

import org.netbeans.modules.uml.core.metamodel.diagrams.IGraphObjectValidation;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;


/**
 * @author KevinM
 *
 */
public class ADEventManager extends ETEventManager implements IADEventManager {

	
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
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IGraphObjectManager#setParentETElement(org.netbeans.modules.uml.ui.support.viewfactorysupport.ITSGraphObject)
	 */
	public void setParentETElement(IETGraphObject value) {
		this.setParentETGraphObject(value);

	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IGraphObjectManager#getParentETElement()
	 */
	public IETGraphObject getParentETElement() {
		return this.getParentETGraphObject();
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

}
