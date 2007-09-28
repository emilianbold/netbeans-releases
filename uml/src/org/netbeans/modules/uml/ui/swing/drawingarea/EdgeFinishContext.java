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



package org.netbeans.modules.uml.ui.swing.drawingarea;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode;
import com.tomsawyer.drawing.TSConnector;
import com.tomsawyer.editor.TSEConnector;

/**
 * @author KevinM
 *
 */
public class EdgeFinishContext extends EdgeEventContext implements IEdgeFinishContext {

	IETNode m_startNode = null;
	IETNode m_finishNode = null;
	TSConnector m_startConnector = null;
	TSConnector m_finishConnector = null;
	
	
	/**
	 * 
	 */
	public EdgeFinishContext() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IEdgeFinishContext#getStartNode()
	 */
	public IETNode getStartNode() {
		return m_startNode;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IEdgeFinishContext#setStartNode(org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode)
	 */
	public void setStartNode(IETNode value) {
		m_startNode = value;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IEdgeFinishContext#getFinishNode()
	 */
	public IETNode getFinishNode() {
		return m_finishNode;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IEdgeFinishContext#setFinishNode(org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode)
	 */
	public void setFinishNode(IETNode value) {
		m_finishNode = value;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IEdgeFinishContext#getStartNodeModelElement()
	 */
	public IElement getStartNodeModelElement() {
		return getModelElement(getStartNode());
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IEdgeFinishContext#getFinishNodeModelElement()
	 */
	public IElement getFinishNodeModelElement() {
		return getModelElement(getFinishNode());
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IEdgeFinishContext#getStartConnector()
	 */
	public TSConnector getStartConnector() {
		return m_startConnector;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IEdgeFinishContext#setStartConnector(com.tomsawyer.editor.TSEConnector)
	 */
	public void setStartConnector(TSConnector value) {
		m_startConnector = value;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IEdgeFinishContext#getFinishConnector()
	 */
	public TSConnector getFinishConnector() {
		return m_finishConnector;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IEdgeFinishContext#setFinishConnector(com.tomsawyer.editor.TSEConnector)
	 */
	public void setFinishConnector(TSConnector value) {
		m_finishConnector = value;
	}
	
	protected IElement getModelElement(IETGraphObject obj)
	{
		return obj != null && obj.getETUI() != null ? obj.getETUI().getModelElement() : null;
	}
}
