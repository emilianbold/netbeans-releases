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


package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities;

import java.util.Stack;

import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers.AttributeStateHandler;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers.ClassStateHandler;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers.StaticDependencyStateHandler;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers.TemplateClassStateHandler;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers.EnumStateHandler;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers.DependencyStateHandler;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers.InterfaceStateHandler;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers.OperationStateHandler;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers.PackageStateHandler;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers.StateHandler;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers.TemplateInterfaceStateHandler;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IStateFilter;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IStateListener;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IStatePayload;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenFilter;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenProcessor;

/**
 */
public class JavaUMLParserProcessor
        implements
            IJavaUMLParserProcessor,
            IStateFilter,
            IStateListener,
            ITokenProcessor,
            ITokenFilter
{
    /**
     * ProcessState determines if a state is to be filtered or not.
     * 
     * @param stateName [in] The name of the state.
     * @param pVal [out] True if the state is to be processed, false otherwise.
     */ 
    public boolean processState(String stateName, String language)
    {
        // I want to filter out all states that are inside of an operation.
        // However I still want to listen to tokens found in a operation.
        // I want the "Method Body Start" and "Method Body End" tokens.
        // So, I do not want to filter out the "Method Body" state itself.
        boolean ret = !m_InOperationBody;
        if ("Method Body".equals(stateName))
            m_InOperationBody = true;
        return ret;
    }

    /**
     * The OnBeginState event is fired when the state of the parser has changed.
     * A new state can begin while still in a state."
     * 
     * @param stateName [in] The name of the state.
     * @param payload [in] Extra data.
     */
    public void onBeginState(String stateName, String language,
            IStatePayload Payload)
    {
        addStateHandler(stateName, language);
    }

    /**
     * The OnEndState event will be fired when exiting a state.
     * 
     * @param stateName [in] The name of the state.
     */
    public void onEndState(String stateName)
    {
        removeStateHandler(stateName);
        
        // We we reached the end state I know that we are no longer in the
        // operation body.  At best we are just leaving the operation body.
        m_InOperationBody = false;
    }

    /**
     * Process the token that has been discovered by the parser.
     * 
     * pToken [in] The token to be processed.
     */
    public void processToken(ITokenDescriptor pToken, String language)
    {
        if (m_StateHandlers.size() > 0)
        {
            HandlerData data = m_StateHandlers.peek();
            if (data.handler != null)
                data.handler.processToken(pToken, language);
        }
    }
    
    protected void addStateHandler(String stateName, String language)
    {
        HandlerData data = new HandlerData();
        data.stateName = stateName;

        if (m_StateHandlers.size() > 0)
        {
            HandlerData handler = m_StateHandlers.peek();
            if (handler.handler != null)
            {    
                data.handler = 
                    handler.handler.createSubStateHandler(stateName, language);
                
                // I do not want to initialize the handler twice.  So, if the returned
                // handler is the same as the current handler (in other words 
                // CreateSubStateHandler returned the THIS pointe), then do not initialize
                // the handler.  The handler has already been initialized.
                if (data.handler != handler.handler && data.handler != null)
                    data.handler.initialize();
            }
        }
        else if("Package".equals(stateName))
        {
            data.handler = new PackageStateHandler(language);
            data.handler.initialize();
        }
        else if("Dependency".equals(stateName))
        {
            data.handler = new DependencyStateHandler(language);
            data.handler.initialize();
        }
        else if("Static Dependency".equals(stateName))
        {
            data.handler = new StaticDependencyStateHandler(language);
            data.handler.initialize();
        }
        else if("Class Declaration".equals(stateName))
        {
            //data.handler = new ClassStateHandler(language, m_CurrentPackage);
            data.handler = new TemplateClassStateHandler(language, m_CurrentPackage);
            data.handler.initialize();
        }
        else if("Enumeration Declaration".equals(stateName))
        {
            data.handler = new EnumStateHandler(language, m_CurrentPackage);
            data.handler.initialize();
        }
        else if("Interface Declaration".equals(stateName))
        {
            data.handler = new TemplateInterfaceStateHandler(language, m_CurrentPackage);
            data.handler.initialize();
        }
        else if("Variable Definition".equals(stateName))
        {
            data.handler = new AttributeStateHandler(language, stateName);
            data.handler.initialize();
        }
        else if("Method Definition".equals(stateName))
        {
            data.handler = new OperationStateHandler(language, 
                    stateName,
                    OperationStateHandler.OPERATION);
            data.handler.initialize();
        }
        else if("Constructor Definition".equals(stateName))
        {
            data.handler = new OperationStateHandler(language, 
                    stateName,
                    OperationStateHandler.CONSTRUCTOR);
            data.handler.initialize();
        }
        else if("Destructor Definition".equals(stateName))
        {
            data.handler = new OperationStateHandler(language, 
                    stateName,
                    OperationStateHandler.DESTRUCTOR);
            data.handler.initialize();
        }
        
        m_StateHandlers.push(data);
    }
    
    protected void removeStateHandler(String stateName)
    {
        if (m_StateHandlers.size() > 0)
        {
            HandlerData oldData = m_StateHandlers.pop();
            
            // Remove the hander from the stack and notify the handler that
            // the state has ended.
            if (oldData.handler != null)
            {
                oldData.handler.stateComplete(stateName);
                
                if ("Package".equals(oldData.stateName))
                {
                    PackageStateHandler pd = 
                            (PackageStateHandler) oldData.handler;
                    if (pd != null)
                        m_CurrentPackage = pd.getFullPackageName();
                }
            }
        }
    }
    
    protected void cleanUpStateHandlers()
    {
        // Cycle through the states that have not been completed yet.  
        // Act as if they have beed completed.  This wil force all
        // data to be sent to the listeners.  This should only occur
        // if something terrible has happened while processing
        // the source file.  Example Stack Overflow error.
        while (m_StateHandlers.size() > 0)
            removeStateHandler(m_StateHandlers.peek().stateName);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenFilter#isTokenValid(java.lang.String, java.lang.String, java.lang.String)
     */
    public boolean isTokenValid(String tokenType, String stateName,
            String language)
    {
        // I want to filter out all states that are inside of an operation.
        // However I still want to listen to tokens found in a operation.
        // I want the "Method Body Start" and "Method Body End" tokens.
        // So, I do not want to filter out the "Method Body" state itself.
        return !(m_InOperationBody && 
                  !"Method Body Start".equals(tokenType) &&
                  !"Method Body End".equals(tokenType));
    }
    
    protected static class HandlerData
    {
        public StateHandler  handler;
        public String        stateName;
    }
    
    protected Stack<HandlerData> m_StateHandlers = new Stack<HandlerData>();
    private boolean           m_InOperationBody;
    protected String             m_CurrentPackage;
}
