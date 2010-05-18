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
