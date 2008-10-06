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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.client.tools.common.dbgp;

/**
 *
 * @author jdeva
 */
public enum CommandMap {
    STATUS(Status.StatusCommand.class, Status.StatusResponse.class),
    FEATURE_GET(Feature.FeatureGetCommand.class, Feature.FeatureGetResponse.class),
    FEATURE_SET(Feature.FeatureSetCommand.class, Feature.FeatureSetResponse.class),
    RUN(Continue.RunCommand.class, Status.StatusResponse.class),
    PAUSE(Continue.PauseCommand.class, Status.StatusResponse.class),    
    STEP_INTO(Continue.StepIntoCommand.class, Status.StatusResponse.class),
    STEP_OVER(Continue.StepOverCommand.class, Status.StatusResponse.class),
    STEP_OUT(Continue.StepOutCommand.class, Status.StatusResponse.class),
    STOP(Continue.StopCommand.class, Status.StatusResponse.class),
    DETACH(Continue.DetachCommand.class, Status.StatusResponse.class),
    BREAKPOINT_SET(Breakpoint.BreakpointSetCommand.class, Breakpoint.BreakpointSetResponse.class),
    BREAKPOINT_GET(Breakpoint.BreakpointGetCommand.class, Breakpoint.BreakpointGetResponse.class),
    BREAKPOINT_UPDATE(Breakpoint.BreakpointUpdateCommand.class, Breakpoint.BreakpointUpdateResponse.class),
    BREAKPOINT_REMOVE(Breakpoint.BreakpointRemoveCommand.class, Breakpoint.BreakpointRemoveResponse.class),
    BREAKPOINT_LIST(Breakpoint.BreakpointListCommand.class, Breakpoint.BreakpointListResponse.class),
    STACK_DEPTH(Stack.StackDepthCommand.class, Stack.StackDepthResponse.class),
    STACK_GET(Stack.StackGetCommand.class, Stack.StackGetResponse.class),
    CONTEXT_NAMES(Context.ContextNamesCommand.class, Context.ContextNamesResponse.class),
    CONTEXT_GET(Context.ContextGetCommand.class, Context.ContextGetResponse.class),
    TYPEMAP_GET(TypeMap.TypeMapGetCommand.class, TypeMap.TypeMapGetResponse.class),
    PROPERTY_GET(Property.PropertyGetCommand.class, Property.PropertyGetResponse.class),
    PROPERTY_SET(Property.PropertySetCommand.class, Property.PropertySetResponse.class),
    PROPERTY_VALUE(Property.PropertyValueCommand.class, Property.PropertyValueResponse.class),
    SOURCE(Source.SourceCommand.class, Source.SourceResponse.class),
    STDOUT(Stream.StreamCommand.class, Stream.StreamResponse.class),
    STDERR(Stream.StreamCommand.class, Stream.StreamResponse.class),
    EVAL(Eval.EvalCommand.class, Eval.EvalResponse.class),
    BREAK(Break.BreakCommand.class, Break.BreakResponse.class),
    OPEN_URI(Extension.OpenURICommand.class, Status.StatusResponse.class),
    RUNTIME_ERROR(Command.class, RuntimeErrorResponse.class);
    //EXPR,
    //EXEC;

    Class commandClass, responseClass;
    CommandMap(Class commandClass, Class responseClass) {
        this.commandClass = commandClass;
        this.responseClass = responseClass;

    }

    public String getCommand() {
        return name().toLowerCase();
    }

    public Class getCommandClass() {
        return commandClass;
    }

    public Class getResponseClass() {
        return responseClass;
    }
}
