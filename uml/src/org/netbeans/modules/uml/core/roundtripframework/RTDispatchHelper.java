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

/*
 * File       : RTDispatchHelper.java
 * Created on : Nov 10, 2003
 * Author     : Aztec
 * Description: No need to create other helpers like RTClassifierDispatchHelper
 *              etc. Only RTRelationDispatchHelper has an odd signature for
 *              establish(). So creating RTRelationDispatchHelper alone.
 */
package org.netbeans.modules.uml.core.roundtripframework;

import org.netbeans.modules.uml.core.eventframework.EventDispatchNameKeeper;
import org.netbeans.modules.uml.core.eventframework.EventDispatchRetriever;
import org.netbeans.modules.uml.core.eventframework.IEventDispatchController;
import org.netbeans.modules.uml.core.eventframework.IEventDispatcher;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;

/**
 * @author Aztec
 */
public class RTDispatchHelper
{
    protected IEventDispatcher           m_Dispatcher;
    protected boolean                    m_Proceed;
    protected IRoundTripEventPayload     m_Payload;
    protected boolean                    m_CellOrigValue;
    protected IEventDispatchController   m_Controller;
    protected IProcessorManager          m_ProcManager;
    
    public RTDispatchHelper()
    {
        
    }
    
    public RTDispatchHelper(IProcessorManager procMan, 
                            IEventDispatchController controller, 
                            /*EventDispatchType*/int type)
    {
        m_ProcManager = procMan;
        m_Controller = controller;
        
        m_Dispatcher = new EventDispatchRetriever(m_Controller).getDispatcher(EventDispatchNameKeeper.dispatcherName(type));
    }

    public void establish(boolean create, boolean override, IElement element, IResultCell cell)
    {
        if (m_ProcManager.establishProcessors( create, override, element ) 
                || override)
        { 
            createRTContextPayload(cell, element);
        }
        else
        {
            m_Dispatcher = null;
        }
    }
    
    /**
     *
     * The IResultCell comes in on the initial event. The Data property of that cell
     * may or may not have valid data on it. Regardless, a new IRoundTripEventPayload object
     * is created, pulling the data property from the IResultCell and placing it into
     * the IRoundTripEventPayload, thereby not losing it. 
     *
     * @param cell[in] The IResultCell to retrieve the data from.
     * @param elementOfDocument[in] 
     *
     * @return HRESULT
     *
     */

    public void createRTContextPayload(IResultCell cell, IElement elementOfDocument)
    {
        if (cell == null) return;

        m_Payload = createRTContextPayload2( cell, elementOfDocument);
        m_CellOrigValue = cell.canContinue();
        m_Proceed = m_CellOrigValue;
    }
    /**
     *
     * The IResultCell comes in on the initial event. The Data property of that cell
     * may or may not have valid data on it. Regardless, a new IRoundTripEventPayload object
     * is created, pulling the data property from the IResultCell and placing it into
     * the IRoundTripEventPayload, thereby not losing it. 
     *
     * @param cell[in] The IResultCell to retrieve the data from.
     * @param rtCell[out] The new RoundTripDataCell.
     *
     * @return HRESULT
     *
     */

    public IRoundTripEventPayload createRTContextPayload2(IResultCell cell, 
                                  IElement pOwner)
    {
        if (cell == null) return null;
        
        IProject project = null;
        if (pOwner != null)
            project = pOwner.getProject();
        return createRTContextPayload(cell, project, pOwner);
    }
    /**
     *
     * The IResultCell comes in on the initial event. The Data property of that cell
     * may or may not have valid data on it. Regardless, a new IRoundTripEventPayload object
     * is created, pulling the data property from the IResultCell and placing it into
     * the IRoundTripEventPayload, thereby not losing it. 
     *
     * @param cell[in] The IResultCell to retrieve the data from.
     * @param rtCell[out] The new RoundTripDataCell.
     *
     * @return HRESULT
     *
     */

    public IRoundTripEventPayload createRTContextPayload(IResultCell cell, 
                                      IProject pProject,
                                      IElement pOwner)
    {
        if (cell == null) return null;
        
        IElementContextPayload newCell = new ElementContextPayload();
        
        newCell.setData(cell.getContextData());
        newCell.setProject(pProject);
        newCell.setOwner(pOwner);
        return newCell;
    }
    
    public IRoundTripEventPayload getPayload()
    {
        return m_Payload;
    }

    public boolean getCellOrigValue()
    {
        return m_CellOrigValue;
    }

    public boolean getProceed()
    {
        return m_Proceed;
    }
    
    public IEventDispatcher getDispatcher()
    {
        return m_Dispatcher;
    }
    
    public IEventDispatchController getController()
    {
        return m_Controller;
    }
}
