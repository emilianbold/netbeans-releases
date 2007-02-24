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



package org.netbeans.modules.uml.ui.swing.drawingarea;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.IETGraphObjectUI;
import org.netbeans.modules.uml.ui.support.applicationmanager.IGraphPresentation;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode;
import com.tomsawyer.editor.TSEConnector;

/**
 * @author KevinM
 *
 */
public class EdgeCreateContext extends EdgeEventContext implements IEdgeCreateContext {

	// Data
	protected IETNode m_node = null;
	protected TSEConnector m_connector = null; 
	
	/**
	 * 
	 */
	public EdgeCreateContext() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IEdgeCreateContext#getNodeModelElement()
	 */
	public IElement getNodeModelElement() {
      IElement retVal = null;
      
		IETNode node = getNode();
      if(node != null)
      {
         IETGraphObjectUI ui = node.getETUI();
         if(ui == null)
         {
            IPresentationElement presElement = node.getPresentationElement();
            if(presElement instanceof IGraphPresentation)
            {
               IGraphPresentation graphPE = (IGraphPresentation)presElement;
               ui = graphPE.getUI();
            }
         }
         
         if(ui != null)
         {
            retVal = ui.getModelElement();
         }
      }
		return retVal;
	}


	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IEdgeCreateContext#getConnector()
	 */
	public TSEConnector getConnector() {
		return m_connector;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IEdgeCreateContext#setConnector(com.tomsawyer.editor.TSEConnector)
	 */
	public void setConnector(TSEConnector value) {
		m_connector = value;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IEdgeCreateContext#getNode()
	 */
	public IETNode getNode() {
		return m_node;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IEdgeCreateContext#setNode(org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode)
	 */
	public void setNode(IETNode value) {
		m_node = value;
	}

}

