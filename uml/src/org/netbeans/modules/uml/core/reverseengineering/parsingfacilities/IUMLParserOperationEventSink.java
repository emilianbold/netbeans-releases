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


package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities;

import org.netbeans.modules.uml.core.reverseengineering.reframework.ICreationEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IDestroyEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IInitializeEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IJumpEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IMethodEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IPostProcessingEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREBinaryOperator;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREClause;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREConditional;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IRECriticalSection;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREExceptionJumpHandlerEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREExceptionProcessingEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IRELoop;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREUnaryOperator;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IRaisedException;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IReferenceEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IReturnEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.ITestEvent;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;

/**
 */
public interface IUMLParserOperationEventSink
{
    public void onCreateAction(ICreationEvent event, IResultCell cell);
    public void onReferencedVariable(IReferenceEvent event, IResultCell cell);
    public void onMethodCall(IMethodEvent e, IResultCell cell);
    public void onReturnAction(IReturnEvent event, IResultCell cell);
    public void onDestroyAction(IDestroyEvent event, IResultCell cell);
    public void onBeginLoop(IResultCell cell);
    public void onEndLoop(IRELoop event, IResultCell cell);
    public void onBeginConditional(IResultCell cell);
    public void onEndConditional(IREConditional event, IResultCell cell);
    public void onBeginCriticalSection(IResultCell cell);
    public void onEndCriticalSection(IRECriticalSection event, IResultCell cell);
    public void onBeginClause(IResultCell cell);
    public void onEndClause(IREClause pEvent, IResultCell cell);
    public void onBeginInitialize(IResultCell cell);
    public void onEndInitialize(IInitializeEvent event, IResultCell cell);
    public void onBeginTest(IResultCell cell);
    public void onEndTest(ITestEvent event, IResultCell cell);
    public void onBeginPostProcessing(IResultCell cell);
    public void onEndPostProcessing(IPostProcessingEvent event, IResultCell cell);
    public void onJumpEvent(IJumpEvent event, IResultCell cell);
    public void onBeginRaisedException(IResultCell cell);
    public void onEndRaisedException(IRaisedException event, IResultCell cell);
    public void onLoop(IRELoop event, IResultCell cell);
    public void onConditional(IREConditional event, IResultCell cell);
    public void onCriticalSection(IRECriticalSection event, IResultCell cell);
    public void onBinaryOperator(IREBinaryOperator event, IResultCell cell);
    public void onUnaryOperator(IREUnaryOperator event, IResultCell cell);
    public void onBeginExceptionProcessing(IResultCell cell);
    public void onEndExceptionProcessing(IREExceptionProcessingEvent event, IResultCell cell);
    public void onBeginExceptionJumpHandler(IResultCell cell);
    public void onEndExceptionJumpHandler(IREExceptionJumpHandlerEvent event, IResultCell cell);
}