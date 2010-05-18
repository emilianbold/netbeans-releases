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
