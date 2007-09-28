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



package org.netbeans.modules.uml.ui.swing.drawingarea.diagramtools;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import com.tomsawyer.editor.TSEEdge;
import com.tomsawyer.drawing.geometry.TSConstPoint;

import java.awt.event.MouseEvent;

/**
 * @author KevinM
 *
 */
public class DiagramAddEdgeTool extends ADCreateEdgeState implements IDiagramAddEdgeTool {

	protected int m_desiredRoutingStyle = 0;
	protected boolean m_initialShowConnectorsValue = false;
	protected boolean m_alwaysShowConnectors = false;
	protected boolean m_initialShowPortsValue = false;
	protected boolean m_alwaysShowPorts = false;
	protected boolean m_deselectAll = true;
	protected boolean m_createBends = true;
	protected String m_defaultEdgeName = "";
	protected String m_defaultToolTipText = "";
	protected IElement m_ElementToAttachTo = null;
	
	
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.diagramtools.IDiagramAddEdgeTool#getDefaultEdgeName()
	 */
	public String getDefaultEdgeName() {
		return m_defaultEdgeName;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.diagramtools.IDiagramAddEdgeTool#setDefaultEdgeName(java.lang.String)
	 */
	public void setDefaultEdgeName(String defaultName) {
		m_defaultEdgeName = defaultName;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.diagramtools.IDiagramAddEdgeTool#getDefaultToolTipText()
	 */
	public String getDefaultToolTipText() {
		return m_defaultToolTipText;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.diagramtools.IDiagramAddEdgeTool#setDefaultToolTipText(java.lang.String)
	 */
	public void setDefaultToolTipText(String defaultToolTipText) {
		m_defaultToolTipText = defaultToolTipText;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.diagramtools.IDiagramAddEdgeTool#getCreateBends()
	 */
	public boolean getCreateBends() {
		return m_createBends;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.diagramtools.IDiagramAddEdgeTool#setCreateBends(boolean)
	 */
	public void setCreateBends(boolean createBends) {
		m_createBends = createBends;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.diagramtools.IDiagramAddEdgeTool#getDesiredRoutingStyle()
	 */
	public int getDesiredRoutingStyle() {
		return m_desiredRoutingStyle;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.diagramtools.IDiagramAddEdgeTool#setDesiredRoutingStyle(int)
	 */
	public void setDesiredRoutingStyle(int style) {
		m_desiredRoutingStyle = style;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.diagramtools.IDiagramAddEdgeTool#getAlwaysShowConnectors()
	 */
	public boolean getAlwaysShowConnectors() {
		return m_alwaysShowConnectors;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.diagramtools.IDiagramAddEdgeTool#setAlwaysShowConnectors(boolean)
	 */
	public void setAlwaysShowConnectors(boolean showConnector) {
		m_alwaysShowConnectors = showConnector;

	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.diagramtools.IDiagramAddEdgeTool#getAlwaysShowPorts()
	 */
	public boolean getAlwaysShowPorts() {
		return m_alwaysShowPorts;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.diagramtools.IDiagramAddEdgeTool#setAlwaysShowPorts(boolean)
	 */
	public void setAlwaysShowPorts(boolean showPorts) {
		m_alwaysShowPorts = showPorts;

	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.diagramtools.IDiagramAddEdgeTool#getAttachElement()
	 */
	public IElement getAttachElement() {
		return m_ElementToAttachTo;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.diagramtools.IDiagramAddEdgeTool#putAttachElement(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement)
	 */
	public void setAttachElement(IElement pModelElement) {
		m_ElementToAttachTo = pModelElement;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.diagramtools.ADCreateEdgeState#onPostDrawingAreaNotifyedObjCreated()
	 */
	protected void onPostDrawingAreaNotifyedObjCreated() {
		super.onPostDrawingAreaNotifyedObjCreated();
		
		TSEEdge pCreatedEdge = this.getCreatedEdge();
		if (pCreatedEdge != null)
		{
			IETGraphObject object = pCreatedEdge instanceof IETGraphObject? (IETGraphObject)pCreatedEdge : null;		
		}
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.diagramtools.ADCreateEdgeState#onPreDrawingAreaNofityObjCreated()
	 */
	protected void onPreDrawingAreaNofityObjCreated() {
		IDrawingAreaControl pControl = this.getDrawingArea();
		if (pControl != null && getAttachElement() != null)
		{
		   pControl.setModelElement(getAttachElement());
		}

	
		super.onPreDrawingAreaNofityObjCreated();
				
		if (pControl != null && getAttachElement() != null)
		{
		   pControl.setModelElement(null);
		}
		
		this.setAttachElement(null);
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.relationshipVerification.IAddEdgeEvents#fireShouldCreateBendEvent(com.tomsawyer.util.TSConstPoint)
	 */
	public boolean fireShouldCreateBendEvent(TSConstPoint pt) {		
		return super.fireShouldCreateBendEvent(pt) && this.getCreateBends();
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.diagramtools.ADCreateEdgeState#onMousePressAddPathNode(java.awt.event.MouseEvent)
	 */
	protected boolean onMousePressAddPathNode(MouseEvent pEvent) {
		// TODO Auto-generated method stub
		return super.onMousePressAddPathNode(pEvent);
	}

}

