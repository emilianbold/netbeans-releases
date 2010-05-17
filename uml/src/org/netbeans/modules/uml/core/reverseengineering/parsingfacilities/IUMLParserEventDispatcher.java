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

import org.netbeans.modules.uml.core.eventframework.IEventDispatcher;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.reverseengineering.reframework.*;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IErrorEvent;

/**
 */
public interface IUMLParserEventDispatcher extends IEventDispatcher
{
    public void registerForUMLParserEvents(IUMLParserEventsSink handler, String fileName);
    
    public void revokeUMLParserSink(IUMLParserEventsSink handler);
    
    public void registerForOperationDetailsEvent(IUMLParserOperationEventSink sink);
    
    public void revokeOperationDetailsSink(IUMLParserOperationEventSink sink);
    
    public void registerForUMLParserAtomicEvents(IUMLParserAtomicEventsSink handler, String fileName);

    public void revokeUMLParserAtomicSink(IUMLParserAtomicEventsSink handler);
    
    public void firePackageFound(String fileName, IPackageEvent data, IEventPayload payLoad);
    
    public void fireDependencyFound(String fileName, IDependencyEvent data, IEventPayload payLoad);
    
    public void fireClassFound(String fileName, IClassEvent data, IEventPayload payLoad);
    
    public void fireBeginParse(String fileName, IEventPayload payLoad);
    
    public void fireEndParse(String fileName, IEventPayload payLoad);
    
    public void fireError(String fileName, IErrorEvent data, IEventPayload payLoad);
    
    public void fireCreateAction(ICreationEvent event, IEventPayload payLoad);
    
    public void fireReferencedVariable(IReferenceEvent event, IEventPayload payLoad);
    
    public void fireMethodCall(IMethodEvent e, IEventPayload payLoad);
    
    public void fireReturnAction(IReturnEvent event, IEventPayload payLoad);
    
    public void fireDestroyAction(IDestroyEvent event, IEventPayload payLoad);
    
    public void fireBeginLoop(IEventPayload payLoad);
    
    public void fireEndLoop(IRELoop event, IEventPayload payLoad);
    
    public void fireBeginConditional(IEventPayload payLoad);
    
    public void fireEndConditional(IREConditional event, IEventPayload payLoad);
    
    public void fireBeginCriticalSection(IEventPayload payLoad);
    
    public void fireEndCriticalSection(IRECriticalSection event, IEventPayload payLoad);
    
    public void fireBeginClause(IEventPayload payLoad);
    
    public void fireEndClause(IREClause pEvent, IEventPayload payLoad);
    
    public void fireBeginInitialize(IEventPayload payLoad);
    
    public void fireEndInitialize(IInitializeEvent event, IEventPayload payLoad);
    
    public void fireBeginTest(IEventPayload payLoad);
    
    public void fireEndTest(ITestEvent event, IEventPayload payLoad);
    
    public void fireBeginPostProcessing(IEventPayload payLoad);
        
    public void fireEndPostProcessing(IPostProcessingEvent event, IEventPayload payLoad);
    
    public void fireJumpEvent(IJumpEvent event, IEventPayload payLoad);
    
    public void fireBeginRaisedException(IEventPayload payLoad);
    
    public void fireEndRaisedException(IRaisedException event, IEventPayload payLoad);
    
    public void fireLoop(IRELoop event, IEventPayload payLoad);
    
    public void fireConditional(IREConditional event, IEventPayload payLoad);
    
    public void fireCriticalSection(IRECriticalSection event, IEventPayload payLoad);
    
    public void fireBinaryOperator(IREBinaryOperator event, IEventPayload payLoad);
    
    public void fireUnaryOperator(IREUnaryOperator event, IEventPayload payLoad);
    
    public void fireBeginExceptionProcessing(IEventPayload payLoad);
    
    public void fireEndExceptionProcessing(IREExceptionProcessingEvent event, IEventPayload payLoad);
    
    public void fireBeginExceptionJumpHandler(IEventPayload payLoad);
    
    public void fireEndExceptionJumpHandler(IREExceptionJumpHandlerEvent event, IEventPayload payLoad);
    
    public void fireOperationFound(IOperationEvent event, IEventPayload payLoad);
    
    public void fireAttributeFound(IAttributeEvent event, IEventPayload payLoad);
    
    public void fireGeneralizationFound(IREGeneralization event, IEventPayload payLoad);
    
    public void fireImplementationFound(IRERealization event, IEventPayload payLoad);
}
