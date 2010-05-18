/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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



/*
 * @author Trey Spiva
 */
package org.netbeans.modules.uml.ui.support.diagramsupport;

import java.util.ArrayList;
import org.netbeans.modules.uml.core.eventframework.EventDispatcher;
import org.netbeans.modules.uml.core.eventframework.EventFunctor;
import org.netbeans.modules.uml.core.eventframework.EventManager;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.openide.loaders.DataObject;



/**
 * 
 * @author Trey Spiva
 */
public class DrawingAreaEventDispatcherImpl extends EventDispatcher implements IDrawingAreaEventDispatcher 
{
    
//	private EventManager < IDrawingAreaSelectionEventsSink > m_DrawingAreaSelectionEventManager = null;
	private EventManager < IDrawingAreaEventsSink > m_DrawingAreaEventManager = null; 
//	private EventManager < IDrawingAreaSynchEventsSink > m_DrawingAreaSynchEventManager = null;
//
//	private EventManager < IDrawingAreaContextMenuEventsSink > m_DrawingAreaContextMenuEventManager = null;
//	private EventManager < IDrawingAreaAddNodeEventsSink > m_DrawingAreaAddNodeEventManager = null;
//	private EventManager < IDrawingAreaAddEdgeEventsSink > m_DrawingAreaAddEdgeEventManager = null;
//	private EventManager < IDrawingAreaReconnectEdgeEventsSink > m_DrawingAreaReconnectEdgeEventManager = null;
//	private EventManager < ICompartmentEventsSink > m_DrawingAreaCompartmentEventManager = null;
//	private EventManager < IChangeNotificationTranslatorSink > m_ChangeNotificationTranslatorEventManager = null;
    
    protected static final String DRAWINGAREA_INTERFACE_PACKAGE = "org.netbeans.modules.uml.ui.support.diagramsupport";

    protected EventFunctor createFunctor(final String fullPathToInterface, final String methodName)
    {
        return new EventFunctor(fullPathToInterface, methodName);
    }

    public DrawingAreaEventDispatcherImpl()
    {
//		m_DrawingAreaSelectionEventManager = new EventManager < IDrawingAreaSelectionEventsSink > ();
        m_DrawingAreaEventManager = new EventManager<IDrawingAreaEventsSink>();
//		m_DrawingAreaSynchEventManager = new EventManager < IDrawingAreaSynchEventsSink >();
//
//		m_DrawingAreaContextMenuEventManager = new EventManager < IDrawingAreaContextMenuEventsSink > ();
//		m_DrawingAreaAddNodeEventManager = new EventManager < IDrawingAreaAddNodeEventsSink > ();
//		m_DrawingAreaAddEdgeEventManager = new EventManager < IDrawingAreaAddEdgeEventsSink > ();
//		m_DrawingAreaReconnectEdgeEventManager = new EventManager < IDrawingAreaReconnectEdgeEventsSink > ();
//		m_DrawingAreaCompartmentEventManager = new EventManager < ICompartmentEventsSink > ();
//		m_ChangeNotificationTranslatorEventManager = new EventManager < IChangeNotificationTranslatorSink > ();
    }

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.IDrawingAreaEventDispatcher#registerDrawingAreaEvents(org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink)
	 */
	public int registerDrawingAreaEvents(IDrawingAreaEventsSink handler) 
        {
		m_DrawingAreaEventManager.addListener(handler, null);
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.IDrawingAreaEventDispatcher#revokeDrawingAreaSink(org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink)
	 */
	public void revokeDrawingAreaSink(IDrawingAreaEventsSink handler) 
        {
		m_DrawingAreaEventManager.removeListener(handler);
	}


	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.IDrawingAreaEventDispatcher#fireDrawingAreaPostCreated(org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl, org.netbeans.modules.uml.core.eventframework.IEventPayload)
	 */
	public void fireDrawingAreaPostCreated(DataObject dataobject, 
                                               IEventPayload payload) 
        {
		ArrayList < Object > collection = new ArrayList < Object > ();
		collection.add(dataobject);

		if (validateEvent("FireDrawingAreaPostCreated", collection)) {
			IResultCell cell = prepareResultCell(payload);
			EventFunctor functor = createFunctor(DRAWINGAREA_INTERFACE_PACKAGE + ".IDrawingAreaEventsSink", "onDrawingAreaPostCreated");
			Object[] parms = new Object[2];
			parms[0] = dataobject;
			parms[1] = cell;
			functor.setParameters(parms);
			m_DrawingAreaEventManager.notifyListenersWithQualifiedProceed(functor);
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.IDrawingAreaEventDispatcher#fireDrawingAreaPrePropertyChange(org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram, int, org.netbeans.modules.uml.core.eventframework.IEventPayload)
	 */
	public boolean fireDrawingAreaPrePropertyChange(IProxyDiagram pProxyDiagram, 
                                                        int nPropertyKindChanged, 
                                                        IEventPayload payload) 
        {
		boolean proceed = true;
		ArrayList < Object > collection = new ArrayList < Object > ();
		Integer parm2 = new Integer(nPropertyKindChanged);
		collection.add(pProxyDiagram);
		collection.add(parm2);

		if (validateEvent("DrawingAreaPrePropertyChanged", collection)) {
			IResultCell cell = prepareResultCell(payload);
			EventFunctor functor = createFunctor(DRAWINGAREA_INTERFACE_PACKAGE + ".IDrawingAreaEventsSink", "onDrawingAreaPrePropertyChange");
			Object[] parms = new Object[3];
			parms[0] = pProxyDiagram;
			parms[1] = parm2;
			parms[2] = cell;
			functor.setParameters(parms);
			m_DrawingAreaEventManager.notifyListenersWithQualifiedProceed(functor);
			proceed = cell.canContinue();
		}
		return proceed;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.IDrawingAreaEventDispatcher#fireDrawingAreaPostPropertyChange(org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram, int, org.netbeans.modules.uml.core.eventframework.IEventPayload)
	 */
	public void fireDrawingAreaPostPropertyChange(IProxyDiagram pProxyDiagram, 
                                                      int nPropertyKindChanged, 
                                                      IEventPayload payload) 
        {
		ArrayList < Object > collection = new ArrayList < Object > ();
		Integer parm2 = new Integer(nPropertyKindChanged);
		collection.add(pProxyDiagram);
		collection.add(parm2);

		if (validateEvent("DrawingAreaPostPropertyChanged", collection)) {
			IResultCell cell = prepareResultCell(payload);
			EventFunctor functor = createFunctor(DRAWINGAREA_INTERFACE_PACKAGE + ".IDrawingAreaEventsSink", "onDrawingAreaPostPropertyChange");
			Object[] parms = new Object[3];
			parms[0] = pProxyDiagram;
			parms[1] = parm2;
			parms[2] = cell;
			functor.setParameters(parms);
			m_DrawingAreaEventManager.notifyListenersWithQualifiedProceed(functor);
		}
	}


	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.IDrawingAreaEventDispatcher#fireDrawingAreaFileRemoved(java.lang.String, org.netbeans.modules.uml.core.eventframework.IEventPayload)
	 */
	public void fireDrawingAreaFileRemoved(String sFilename, IEventPayload payload) {
		ArrayList < Object > collection = new ArrayList < Object > ();
		collection.add(sFilename);

		if (validateEvent("DrawingAreaFileRemoved", collection)) {
			IResultCell cell = prepareResultCell(payload);
			EventFunctor functor = createFunctor(DRAWINGAREA_INTERFACE_PACKAGE + ".IDrawingAreaEventsSink", "onDrawingAreaFileRemoved");
			Object[] parms = new Object[2];
			parms[0] = sFilename;
			parms[1] = cell;
			functor.setParameters(parms);
			m_DrawingAreaEventManager.notifyListenersWithQualifiedProceed(functor);
		}
	}
}
