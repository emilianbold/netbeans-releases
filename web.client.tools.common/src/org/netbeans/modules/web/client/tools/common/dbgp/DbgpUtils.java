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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

import org.netbeans.api.debugger.Breakpoint.HIT_COUNT_FILTERING_STYLE;
import org.netbeans.modules.web.client.tools.common.dbgp.Status.DebugMessage;
import org.netbeans.modules.web.client.tools.common.dbgp.Status.StatusResponse;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSBreakpoint;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSCallStackFrame;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSDebugger;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSDebuggerState;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSProperty;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSValue;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSErrorInfo;
import org.netbeans.modules.web.client.tools.javascript.debugger.impl.JSBreakpointImpl;
import org.netbeans.modules.web.client.tools.javascript.debugger.impl.JSCallStackFrameImpl;
import org.netbeans.modules.web.client.tools.javascript.debugger.impl.JSFactory;
import org.netbeans.modules.web.client.tools.javascript.debugger.impl.JSObjectImpl;
import org.netbeans.modules.web.client.tools.api.JSLocation;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSURILocation;

/**
 *
 * @author jdeva
 */
public class DbgpUtils {
    public static JSBreakpoint getBreakpoint(StatusResponse response) {
        if(response == null) {
            return null;
        }
        DebugMessage dbgMessage = response.getDebugMessage();
        JSBreakpointImpl jsBreakpoint = (JSBreakpointImpl) 
                JSFactory.createJSBreakpoint(dbgMessage.getFileName(), dbgMessage.getLineNumber());
        if (dbgMessage.getBreakpointId() != null) {
            jsBreakpoint.setId(dbgMessage.getBreakpointId());
        }
        return jsBreakpoint;
    }

    public static boolean isStepSuccessfull(StatusResponse response){
       return (response != null && response.getState() == Status.State.BREAKPOINT && 
                response.getReason() == Status.Reason.OK);     
    }
    
    public static Breakpoint.BreakpointSetCommand getDbgpBreakpointCommand(DebuggerProxy proxy, 
            JSURILocation location, boolean temprary) {
        CommandFactory commandFactory = proxy.getCommandFactory();        
        Breakpoint.BreakpointSetCommand bpSetCommand = 
                commandFactory.lineBreakpointSetCommand(location.getURI(), location.getLineNumber());
        bpSetCommand.setTemporary(true);
        //set default values for rest
        bpSetCommand.setType(Breakpoint.Type.LINE);
        bpSetCommand.setState(true);
        bpSetCommand.setHitValue(0);
        bpSetCommand.setHitCondition(Breakpoint.HitCondition.EQUAL.name());
        bpSetCommand.setCondition("");
        return bpSetCommand;
    }

    public static Breakpoint.BreakpointSetCommand getDbgpBreakpointCommand(DebuggerProxy proxy, 
            JSBreakpoint breakpoint) {
        Breakpoint.BreakpointSetCommand setCommand = null;
        CommandFactory commandFactory = proxy.getCommandFactory();
        switch(breakpoint.getType()){
            case LINE:
                JSLocation location = breakpoint.getLocation();
                setCommand = commandFactory.lineBreakpointSetCommand(location.getURI(), 
                        location.getLineNumber());
                break;
            case CALL:
                setCommand = commandFactory.callBreakpointSetCommand(breakpoint.getFunction());
                break;
            case CONDITIONAL:
                setCommand = commandFactory.conditionalBreakpointSetCommand(breakpoint.getCondition());
                break;
            case EXCEPTION:
                setCommand = commandFactory.conditionalBreakpointSetCommand(breakpoint.getException());
                break;
            default:
                break;
        }
        
        if(setCommand != null) {
            Boolean state = breakpoint.isEnabled();
            if(state != null) {
                setCommand.setState(state.booleanValue());
            }
            HIT_COUNT_FILTERING_STYLE hitCondition = breakpoint.getHitCondition();
            if (hitCondition != null){
                setCommand.setHitCondition(hitCondition.name());
                setCommand.setHitValue(breakpoint.getHitValue());
            }
            String condition = breakpoint.getCondition();
            if(condition != null) {
                setCommand.setCondition(condition);
            }
        }
        
        return setCommand;
    }    

    public static JSDebuggerState getDebuggerState(Message message) {
        if(message instanceof InitMessage) {
            return JSDebuggerState.STARTING_INIT;
        } else if(message instanceof OnloadMessage) {
            return JSDebuggerState.STARTING_READY;        
        } if(message instanceof StatusResponse) {
            StatusResponse response = (StatusResponse)message;
            if(response.getState() == Status.State.RUNNING) {
                return JSDebuggerState.RUNNING;
            } else if(response.getState() == Status.State.FIRST_LINE) {
                return JSDebuggerState.SUSPENDED_FIRST_LINE;
            } else if(response.getState() == Status.State.BREAKPOINT) {
                return JSDebuggerState.getDebuggerState(getBreakpoint((StatusResponse)message));
            } else if(response.getState() == Status.State.EXCEPTION) {
                return JSDebuggerState.getDebuggerState((JSErrorInfo) null);
            } else if(response.getState() == Status.State.STEP) {
                return JSDebuggerState.SUSPENDED_STEP;
            } else if(response.getState() == Status.State.DEBUGGER) {
                return JSDebuggerState.SUSPENDED_DEBUGGER;
            } else if(response.getState() == Status.State.STOPPED) {
                return response.getReason() == Status.Reason.EXCEPTION ? JSDebuggerState.DISCONNECTED : JSDebuggerState.DISCONNECTED_USER;
            }
        }
        return null;
    }

    public static List<JSBreakpoint> getJSBreakpoints(List<Breakpoint> breakpoints) {
        List<JSBreakpoint> jsBreakpoints = new ArrayList<JSBreakpoint>();
        if(breakpoints != null) {
            for(Breakpoint breakpoint: breakpoints) {
                jsBreakpoints.add(getJSBreakpoint(breakpoint));
            }
        }
        return jsBreakpoints;
    }

    public static JSBreakpoint getJSBreakpoint(Breakpoint breakpoint) {
        if(breakpoint != null) {
            JSFactory.createJSBreakpoint(breakpoint.getFileURI(), breakpoint.getLineNumber(), breakpoint.getId());
        }
        return null;
    }

    public static JSCallStackFrame getJSCallStackFrame(JSDebugger debugger, Stack callStack) {
        if(callStack != null) {
            return JSFactory.createJSCallStackFrame(debugger, callStack.getLevel(), 
                    JSCallStackFrameImpl.TYPE.valueOf(callStack.getType().name()), callStack.getWhere(), 
                    callStack.getFileName(), callStack.getLine());
        }
        return null;
    }
    
    public static List<JSCallStackFrame> getJSCallStackFrames(JSDebugger debugger, List<Stack> callStacks) {
        List<JSCallStackFrame> frames = new ArrayList<JSCallStackFrame>();
        if(callStacks != null) {
            for(Stack stack: callStacks) {
                frames.add(getJSCallStackFrame(debugger, stack));
            }
        }
        return frames;
    }
    
    public static JSProperty getJSProperty(JSCallStackFrame frame, Property property) {
        if(property == null) {
            return null;
        }
        try {
            JSValue.TypeOf type = JSValue.TypeOf.valueOf(property.getType().toUpperCase());
            String fullName = property.getFullName();
            String name = property.getName();
            String value = property.getStringValue();
            JSValue jsValue = null;
            switch (type) {
                case BOOLEAN:
                case DOUBLE:
                case INT:
                case VOID:
                case NULL:
                case STRING:
                    jsValue = JSFactory.createJSPrimitive(frame, fullName, type, value);
                    break;
                case OBJECT:
                    jsValue = JSFactory.createJSObject(frame, fullName, property.getClassName());
                    break;
                case ARRAY:
                    jsValue = JSFactory.createJSArray(frame, fullName);
                    break;
                case FUNCTION:
                    jsValue = JSFactory.createJSFunction(frame, fullName, name, value, property.getClassName());
            }
            if (jsValue != null) {
                if(jsValue instanceof JSObjectImpl) {
                    JSObjectImpl jsObject = (JSObjectImpl)jsValue;
                    if (property.getChildrenSize() == 0) {
                        jsObject.setProperties(JSProperty.EMPTY_ARRAY);
                    } else if (property.getChildrenSize() > 0) {
                        List<JSProperty> jsProps = new LinkedList<JSProperty>();
                        List<Property> children = property.getChildren();
                        // Only if there are sub Properties the initialize the cache
                        if (children.size() > 0) {
                            for(Property prop : children) {
                                jsProps.add(getJSProperty(frame, prop));
                            }
                            jsObject.setProperties(jsProps.toArray(JSProperty.EMPTY_ARRAY));     
                        }
                    }
                    // When property.getChildrenSize() == -1 we do not initialize the cache
                }
                return JSFactory.createJSProperty(property.getName(), jsValue);
            }
        }  catch (UnsufficientValueException ex) {
            Log.getLogger().log(Level.SEVERE, "Unable to get JSProperty", ex);   //NOI18N
        }
        return null;
    }
    
    public static JSProperty[] getJSProperties(JSCallStackFrame callStackFrame, Property property) {
        if(property == null || property.getChildrenSize() == 0) {
            return JSProperty.EMPTY_ARRAY;
        }
            
        List<JSProperty> properties = new LinkedList<JSProperty>();
        for(Property prop : property.getChildren()) {
            properties.add(getJSProperty(callStackFrame, prop));
        }
        return properties.toArray(JSProperty.EMPTY_ARRAY);
    }
}
