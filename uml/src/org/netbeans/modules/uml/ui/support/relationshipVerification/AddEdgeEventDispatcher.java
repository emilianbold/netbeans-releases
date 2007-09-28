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

package org.netbeans.modules.uml.ui.support.relationshipVerification;

import java.util.ArrayList;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.common.generics.ETTripleT;
import org.netbeans.modules.uml.core.eventframework.EventFunctor;
import org.netbeans.modules.uml.core.eventframework.EventManager;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.support.umlsupport.ETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.ui.controls.drawingarea.DrawingAreaEventDispatcherImpl;
import org.netbeans.modules.uml.ui.controls.drawingarea.IDrawingAreaEventDispatcher;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ETPointEx;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.swing.drawingarea.EdgeCreateContext;
import org.netbeans.modules.uml.ui.swing.drawingarea.EdgeFinishContext;
import org.netbeans.modules.uml.ui.swing.drawingarea.EdgeMouseMoveContext;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaAddEdgeEventsSink;
import org.netbeans.modules.uml.ui.swing.drawingarea.IEdgeCreateBendContext;
import org.netbeans.modules.uml.ui.swing.drawingarea.IEdgeCreateContext;
import org.netbeans.modules.uml.ui.swing.drawingarea.IEdgeFinishContext;
import org.netbeans.modules.uml.ui.swing.drawingarea.IEdgeMouseMoveContext;
import com.tomsawyer.drawing.TSConnector;
//import com.tomsawyer.util.TSConstPoint;
import com.tomsawyer.drawing.geometry.TSConstPoint;

/**
 * @author KevinM
  * The AddEdgeEventDispatcher encapsulates the creation and routing of Edge Event contexts,
  * through drawing area dispatchers exposed interfaces.
 */
public class AddEdgeEventDispatcher  implements IAddEdgeEvents {

	// Data, initialized by the constructor, no need to null them.
	protected IDrawingAreaEventDispatcher m_dispatcher;
	protected IDiagram m_parentDiagram;
	protected String m_viewDesc;	

	/**
	 * Preferred constructor.
	 */
	public AddEdgeEventDispatcher(IDrawingAreaEventDispatcher dipatcher, IDiagram pParentDiagram, String viewDesc) {
		super();
		setParentDiagram(pParentDiagram);
		m_viewDesc = viewDesc;
		m_dispatcher = dipatcher;
	}

	public AddEdgeEventDispatcher(IDrawingAreaEventDispatcher dipatcher, IDiagram pParentDiagram) {
		this(dipatcher, pParentDiagram, null);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.relationshipVerification.IAddEdgeEvents#getParentDiagram()
	 */
	public IDiagram getParentDiagram() {
		return m_parentDiagram;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.relationshipVerification.IAddEdgeEvents#setParentDiagram(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram)
	 */
	public void setParentDiagram(IDiagram pParentDiagram) {
		m_parentDiagram = pParentDiagram;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.relationshipVerification.IAddEdgeEvents#getViewDescription()
	 */
	public String getViewDescription() {
		return m_viewDesc;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.relationshipVerification.IAddEdgeEvents#setViewDescription(java.lang.String)
	 */
	public void setViewDescription(String sViewDescription) {
		m_viewDesc = sViewDescription;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.relationshipVerification.IAddEdgeEvents#fireStartingEdgeEvent(org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode, com.tomsawyer.util.TSConstPoint)
	 */
	public ETTripleT < TSConnector, Integer, IETPoint > fireStartingEdgeEvent(IETNode pNode, TSConstPoint point)
        {
            
            IETPoint rpoint = null;
            TSConnector connector = null;
            Integer canceled = null;
            
            if (pNode != null)
            {
                
                TSConstPoint tempPoint = null;
                if(point != null)
                {
                    tempPoint = (TSConstPoint)point.clone();
                }
                
                // Create the context, fill it and fire the event
                IEdgeCreateContext pEdgeCreateContext = new EdgeCreateContext();
                if (pEdgeCreateContext != null)
                {
                    pEdgeCreateContext.setNode(pNode);
                    pEdgeCreateContext.setViewDescription(getViewDescription());
                    pEdgeCreateContext.setLogicalPoint(point != null ? new ETPointEx(point) : null);
                    
                    if (getParentDiagram() != null)
                    {
                        pEdgeCreateContext = fireDrawingAreaStartingEdge(pEdgeCreateContext, false);
                        
                        // Now see if any listeners have canceled the event
                        canceled = new Integer(pEdgeCreateContext.getCancel() ? 1 : 0);
                        // Pass back the connector, if it is valid
                        connector = pEdgeCreateContext.getConnector();
                        
                        if (connector != null)
                        {
                            // Get the updated start point
                            rpoint = pEdgeCreateContext.getLogicalPoint();
                        }
                    }
                }
            }
            
            return new ETTripleT < TSConnector, Integer, IETPoint > (connector, canceled, rpoint);
        }

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.relationshipVerification.IAddEdgeEvents#fireShouldCreateBendEvent(com.tomsawyer.util.TSConstPoint)
	 */
	public boolean fireShouldCreateBendEvent(TSConstPoint point) {
		IEdgeCreateBendContext context = new EdgeCreateBendContext();
		context.setLogicalPoint(point != null ? new ETPointEx(point) : null);
		context.setViewDescription(getViewDescription());		
		fireShouldCreateBend(context);
		return context.getCancel() ? false : true;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.relationshipVerification.IAddEdgeEvents#fireEdgeMouseMoveEvent(org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode, org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode, com.tomsawyer.util.TSConstPoint)
	 */
	public boolean fireEdgeMouseMoveEvent(IETNode pStartNode, IETNode pNodeUnderMouse, TSConstPoint point) {
		IEdgeMouseMoveContext context = new EdgeMouseMoveContext();
		context.setLogicalPoint(point != null ? new ETPointEx(point) : null);
		context.setStartNode(pStartNode);
		context.setNodeUnderMouse(pNodeUnderMouse);
		context.setViewDescription(getViewDescription());
		fireEdgeMouseMove(context);
		return context.getValid();
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.relationshipVerification.IAddEdgeEvents#fireFinishEdgeEvent(org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode, org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode, com.tomsawyer.editor.TSEConnector, com.tomsawyer.util.TSConstPoint)
	 */
	public ETPairT < TSConnector, Integer > fireFinishEdgeEvent(IETNode pStartNode, IETNode pFinishNode, TSConnector pStartConnector, TSConstPoint point) {		
		if (pStartNode != null && pFinishNode != null)
		{
			IEdgeFinishContext pEdgeFinishContext = new EdgeFinishContext();
			pEdgeFinishContext.setStartNode(pStartNode);
			pEdgeFinishContext.setFinishNode(pFinishNode);
			pEdgeFinishContext.setStartConnector(pStartConnector);
			pEdgeFinishContext.setLogicalPoint(point != null ? new ETPointEx(point) : null);
			pEdgeFinishContext.setViewDescription(getViewDescription());
			
			fireFinishEdge(pEdgeFinishContext);
			
			boolean varCanceled = pEdgeFinishContext.getCancel();
			
			ETPairT < TSConnector, Integer > retCode = new ETPairT < TSConnector, Integer > (
				pEdgeFinishContext.getFinishConnector(), new Integer(varCanceled ? 1 : 0));
			return retCode;
		}
		return null;
	}

	protected IEdgeCreateContext fireDrawingAreaStartingEdge(IEdgeCreateContext pContext, boolean something) 
	{
		if (m_dispatcher != null && pContext != null)
		{
			m_dispatcher.fireDrawingAreaStartingEdge(getParentDiagram(), pContext, 
				m_dispatcher.createPayload("FireStartingEdge"));
		}
		return pContext;
	}

	protected void fireShouldCreateBend(IEdgeCreateBendContext pContext)
	{
		if (m_dispatcher != null && pContext != null)
		{
			m_dispatcher.fireDrawingAreaEdgeShouldCreateBend(getParentDiagram(), pContext, 
				m_dispatcher.createPayload("FireShouldCreateBendEvent"));
		}
	}

	protected void fireEdgeMouseMove(IEdgeMouseMoveContext pContext)
	{
		if (m_dispatcher != null && pContext != null)
		{
			m_dispatcher.fireDrawingAreaEdgeMouseMove(getParentDiagram(), pContext, 
				m_dispatcher.createPayload("FireEdgeMouseMoveEvent"));
		}
	}

	protected void fireFinishEdge(IEdgeFinishContext pContext)
	{
		if (m_dispatcher != null && pContext != null)
		{
			m_dispatcher.fireDrawingAreaFinishEdge(getParentDiagram(), pContext, 
				m_dispatcher.createPayload("FireFinishEdgeEvent"));
		}
	}
}
