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
 * File       : UMLTestListener.java
 * Created on : Feb 3, 2004
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.reverseengineering;

import org.dom4j.Node;

import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserEventsSink;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserOperationEventSink;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IClassEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.ICreationEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IDependencyEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IDestroyEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IInitializeEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IJumpEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IMethodEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IPackageEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IParserData;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IPostProcessingEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREBinaryOperator;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREClass;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREClause;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREConditional;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IRECriticalSection;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREExceptionJumpHandlerEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREExceptionProcessingEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IRELoop;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREOperation;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREUnaryOperator;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IRaisedException;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IReferenceEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IReturnEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.ITestEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IErrorEvent;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author Aztec
 */
public class UMLTestListener
    implements IUMLParserEventsSink, IUMLParserOperationEventSink
{
    
    private boolean m_haveError;
    private StringBuffer m_xml = new StringBuffer();
    private String m_holdMethod;
    private IREOperation m_heldMethod;
    private int m_nestedLevel;
    
    private void logOnBegin()
    {
        ++m_nestedLevel;
    }
    private void logOnEnd(IParserData event)
    {
        --m_nestedLevel;

        logData(event);
    }
    private void logOnEvent(IParserData event)
    {
        logData(event);
    }

    private void setBreak()
    {
        
    }
       

    private void logData(IParserData event)
    {
        if(m_nestedLevel > 0)
              return;

        Node pNode = event.getEventData();

        String xml = pNode.asXML();

        m_xml.append(xml);
    }
    private void getHoldMethod(IClassEvent pClassEvent)
    {
        if(m_holdMethod == null)
           return;
   
        IREClass pClass = pClassEvent.getREClass();

        if(pClass == null) throw new RuntimeException("RECLASS IS NULL");

        ETList< IREOperation > pOperations = pClass.getOperations();

        if(pClass == null) throw new RuntimeException("IREOperations are NULL");

        int max = max = pOperations.size();
        for(int index = 0 ; index < max; ++index)
        {
            IREOperation pTemp = pOperations.get(index);

            if(pTemp != null)
            {
                String methodName = pTemp.getName();

                if(methodName.equals(m_holdMethod))
                {
                    m_heldMethod = pTemp;
                }
            }
        }
    }
    
    public String getXML()
    {
        return m_xml.toString();
    }
    public void holdMethod(String methodName)
    {
        m_holdMethod = methodName;
    }
    
    public IREOperation getHeldMethod()
    {
        return m_heldMethod;
    }



    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserEventsSink#onBeginParseFile(java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onBeginParseFile(String fileName, IResultCell cell)
    {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserEventsSink#onClassFound(org.netbeans.modules.uml.core.reverseengineering.reframework.IClassEvent, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onClassFound(IClassEvent event, IResultCell cell)
    {
        logData(event);
        getHoldMethod(event);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserEventsSink#onDependencyFound(org.netbeans.modules.uml.core.reverseengineering.reframework.IDependencyEvent, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onDependencyFound(IDependencyEvent event, IResultCell cell)
    {
        logData(event);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserEventsSink#onEndParseFile(java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onEndParseFile(String fileName, IResultCell cell)
    {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserEventsSink#onError(org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IErrorEvent, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onError(IErrorEvent event, IResultCell cell)
    {
        m_haveError = true;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserEventsSink#onPackageFound(org.netbeans.modules.uml.core.reverseengineering.reframework.IPackageEvent, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPackageFound(IPackageEvent event, IResultCell cell)
    {
        logData(event);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserOperationEventSink#onBeginClause(org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onBeginClause(IResultCell cell)
    {
        logOnBegin();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserOperationEventSink#onBeginConditional(org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onBeginConditional(IResultCell cell)
    {
        logOnBegin();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserOperationEventSink#onBeginCriticalSection(org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onBeginCriticalSection(IResultCell cell)
    {
        logOnBegin();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserOperationEventSink#onBeginExceptionJumpHandler(org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onBeginExceptionJumpHandler(IResultCell cell)
    {
        logOnBegin();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserOperationEventSink#onBeginExceptionProcessing(org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onBeginExceptionProcessing(IResultCell cell)
    {
        logOnBegin();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserOperationEventSink#onBeginInitialize(org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onBeginInitialize(IResultCell cell)
    {
        logOnBegin();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserOperationEventSink#onBeginLoop(org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onBeginLoop(IResultCell cell)
    {
        logOnBegin();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserOperationEventSink#onBeginPostProcessing(org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onBeginPostProcessing(IResultCell cell)
    {
        logOnBegin();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserOperationEventSink#onBeginRaisedException(org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onBeginRaisedException(IResultCell cell)
    {
        logOnBegin();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserOperationEventSink#onBeginTest(org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onBeginTest(IResultCell cell)
    {
        logOnBegin();
    }
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserOperationEventSink#onBinaryOperator(org.netbeans.modules.uml.core.reverseengineering.reframework.IREBinaryOperator, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onBinaryOperator(IREBinaryOperator event, IResultCell cell)
    {
        logOnEvent(event);
    }
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserOperationEventSink#onConditional(org.netbeans.modules.uml.core.reverseengineering.reframework.IREConditional, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onConditional(IREConditional event, IResultCell cell)
    {
        logOnEvent(event);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserOperationEventSink#onCreateAction(org.netbeans.modules.uml.core.reverseengineering.reframework.ICreationEvent, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onCreateAction(ICreationEvent event, IResultCell cell)
    {
        logOnEvent(event);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserOperationEventSink#onCriticalSection(org.netbeans.modules.uml.core.reverseengineering.reframework.IRECriticalSection, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onCriticalSection(IRECriticalSection event, IResultCell cell)
    {
        logOnEvent(event);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserOperationEventSink#onDestroyAction(org.netbeans.modules.uml.core.reverseengineering.reframework.IDestroyEvent, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onDestroyAction(IDestroyEvent event, IResultCell cell)
    {
        logOnEvent(event);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserOperationEventSink#onEndClause(org.netbeans.modules.uml.core.reverseengineering.reframework.IREClause, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onEndClause(IREClause event, IResultCell cell)
    {
        logOnEnd(event);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserOperationEventSink#onEndConditional(org.netbeans.modules.uml.core.reverseengineering.reframework.IREConditional, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onEndConditional(IREConditional event, IResultCell cell)
    {
        logOnEnd(event);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserOperationEventSink#onEndCriticalSection(org.netbeans.modules.uml.core.reverseengineering.reframework.IRECriticalSection, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onEndCriticalSection(
        IRECriticalSection event,
        IResultCell cell)
    {
        logOnEnd(event);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserOperationEventSink#onEndExceptionJumpHandler(org.netbeans.modules.uml.core.reverseengineering.reframework.IREExceptionJumpHandlerEvent, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onEndExceptionJumpHandler(
        IREExceptionJumpHandlerEvent event,
        IResultCell cell)
    {
        logOnEnd(event);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserOperationEventSink#onEndExceptionProcessing(org.netbeans.modules.uml.core.reverseengineering.reframework.IREExceptionProcessingEvent, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onEndExceptionProcessing(
        IREExceptionProcessingEvent event,
        IResultCell cell)
    {
        logOnEnd(event);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserOperationEventSink#onEndInitialize(org.netbeans.modules.uml.core.reverseengineering.reframework.IInitializeEvent, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onEndInitialize(IInitializeEvent event, IResultCell cell)
    {
    }
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserOperationEventSink#onEndLoop(org.netbeans.modules.uml.core.reverseengineering.reframework.IRELoop, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onEndLoop(IRELoop event, IResultCell cell)
    {
        logOnEnd(event);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserOperationEventSink#onEndPostProcessing(org.netbeans.modules.uml.core.reverseengineering.reframework.IPostProcessingEvent, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onEndPostProcessing(
        IPostProcessingEvent event,
        IResultCell cell)
    {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserOperationEventSink#onEndRaisedException(org.netbeans.modules.uml.core.reverseengineering.reframework.IRaisedException, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onEndRaisedException(IRaisedException event, IResultCell cell)
    {
        logOnEnd(event);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserOperationEventSink#onEndTest(org.netbeans.modules.uml.core.reverseengineering.reframework.ITestEvent, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onEndTest(ITestEvent event, IResultCell cell)
    {
        logOnEnd(event);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserOperationEventSink#onJumpEvent(org.netbeans.modules.uml.core.reverseengineering.reframework.IJumpEvent, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onJumpEvent(IJumpEvent event, IResultCell cell)
    {
        logOnEvent(event);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserOperationEventSink#onLoop(org.netbeans.modules.uml.core.reverseengineering.reframework.IRELoop, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onLoop(IRELoop event, IResultCell cell)
    {
        logOnEvent(event);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserOperationEventSink#onMethodCall(org.netbeans.modules.uml.core.reverseengineering.reframework.IMethodEvent, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onMethodCall(IMethodEvent event, IResultCell cell)
    {
        logOnEvent(event);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserOperationEventSink#onReferencedVariable(org.netbeans.modules.uml.core.reverseengineering.reframework.IReferenceEvent, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onReferencedVariable(IReferenceEvent event, IResultCell cell)
    {
        logOnEvent(event);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserOperationEventSink#onReturnAction(org.netbeans.modules.uml.core.reverseengineering.reframework.IReturnEvent, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onReturnAction(IReturnEvent event, IResultCell cell)
    {
        logOnEvent(event);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserOperationEventSink#onUnaryOperator(org.netbeans.modules.uml.core.reverseengineering.reframework.IREUnaryOperator, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onUnaryOperator(IREUnaryOperator event, IResultCell cell)
    {
        logOnEvent(event);
    }

}
