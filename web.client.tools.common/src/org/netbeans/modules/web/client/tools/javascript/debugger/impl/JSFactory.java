/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.web.client.tools.javascript.debugger.impl;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;


import org.netbeans.modules.web.client.tools.common.dbgp.HttpMessage;
import org.netbeans.modules.web.client.tools.common.dbgp.SourcesMessage.Source;
import org.netbeans.modules.web.client.tools.common.dbgp.WindowsMessage.Window;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSArray;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSBreakpoint;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSCallStackFrame;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSDebugger;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSFunction;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSHttpMessage;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSHttpMessage.MethodType;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSHttpMessage.Type;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSObject;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSPrimitive;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSProperty;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSSource;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSValue;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSValue.TypeOf;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSWindow;

/**
 *
 * @author Sandip V. Chitale <sandipchitale@netbeans.org>
 */
public class JSFactory {

    public static JSBreakpoint createJSBreakpoint(String fileURI, int lineNumber) {
        return new JSBreakpointImpl(fileURI, lineNumber);
    }

    public static JSBreakpoint createJSBreakpoint(String fileURI, int lineNumber, String id) {
        JSBreakpointImpl jSBreakpoint = new JSBreakpointImpl(fileURI, lineNumber);
        jSBreakpoint.setId(id);
        return jSBreakpoint;
    }
    // CallStackFrame
    public static JSCallStackFrame createJSCallStackFrame(JSDebugger debugger, int level, JSCallStackFrameImpl.TYPE type, String where, URI uri, int lineNumber) {
        return createJSCallStackFrame(debugger, level, type, where, uri, lineNumber, -1);
    }

    public static JSCallStackFrame createJSCallStackFrame(JSDebugger debugger, int level, JSCallStackFrameImpl.TYPE type, String where, URI uri, int lineNumber, int columneNumber) {
        return new JSCallStackFrameImpl(debugger, level, type, where, uri, lineNumber, columneNumber);
    }

    public static JSCallStackFrame createJSCallStackFrame(JSDebugger debugger, int level, JSCallStackFrameImpl.TYPE type, String where, String uri, int lineNumber) {
        return createJSCallStackFrame(debugger, level, type, where, uri, lineNumber, -1);
    }

    public static JSCallStackFrame createJSCallStackFrame(JSDebugger debugger, int level, JSCallStackFrameImpl.TYPE type, String where, String uri, int lineNumber, int columneNumber) {
        return new JSCallStackFrameImpl(debugger, level, type, where, uri, lineNumber, columneNumber);
    }

    // Property
    public static JSProperty createJSProperty(String name, JSValue value) {
        return new JSPropertyImpl(name, value);
    }

    // Values

    // Primitive
    public static JSPrimitive createJSPrimitive(JSCallStackFrame callStackFrame, String fullName, TypeOf type, String value) {
        return new JSPrimitiveImpl((JSCallStackFrameImpl) callStackFrame, fullName, type, value);
    }

    // Object
    public static JSObject createJSObject(JSCallStackFrame callStackFrame, String fullName, String className) {
        JSObjectImpl object = new JSObjectImpl((JSCallStackFrameImpl) callStackFrame, fullName, className);
        return object;
    }

    // Array
    public static JSArray createJSArray(JSCallStackFrame callStackFrame, String fullName) {
        JSArrayImpl array = new JSArrayImpl((JSCallStackFrameImpl) callStackFrame, fullName);
        return array;
    }

    // Function
    public static JSFunction createJSFunction(JSCallStackFrame callStackFrame, String fullName, String name, String body, String className) {
        JSFunctionImpl array = new JSFunctionImpl((JSCallStackFrameImpl) callStackFrame,
                fullName, name, body, className);
        return array;
    }

    // Sources
    public static JSSource createJSSource(String uri) {
        return new JSSourceImpl(uri);
    }

    public static JSSource[] getJSSources(List<Source> sources) {
        List<? super JSSource> jsSources = new ArrayList<JSSource>(sources.size());
        for (Source source : sources) {
            jsSources.add(createJSSource(source.getURI()));
        }
        return jsSources.toArray(JSSource.EMPTY_ARRAY);
    }
    // Windows
    public static JSWindow createJSWindow(String uri) {
        return createJSWindow(uri, null);
    }

    public static JSWindow createJSWindow(String uri, JSWindow parent) {
        return createJSWindow(uri, parent, false);
    }

    public static JSWindow createJSWindow(String uri, JSWindow parent, boolean suspended) {
        return new JSWindowImpl(uri, parent, suspended);
    }

    public static JSWindow[] getJSWindows(List<Window> windows) {
        List<? super JSWindow> jsWindows = new ArrayList<JSWindow>(windows.size());
        for (Window window : windows) {
            final JSWindow jsWindow = createJSWindow(window.getURI());
            jsWindows.add(jsWindow);
            addChildWindows(window, jsWindow);
        }
        return (jsWindows.toArray(JSWindow.EMPTY_ARRAY));
    }

    public static MethodType getHttpMessageMethodType(String string) {
        if (string.equals(MethodType.POST.toString())) {
            return MethodType.POST;
        } else {
            return MethodType.GET;
        }
    }

    private static Type getHttpMessageType(String string) {
        if (string == null) {
            return null;
        }
        String lowerStr = string.toLowerCase();
        if (lowerStr.equals(Type.REQUEST.toString())) {
            return Type.REQUEST;
        } else if (lowerStr.equals(Type.RESPONSE.toString())) {
            return Type.RESPONSE;
        } else {
            return Type.PROGRESS;
        }
    }

    public static JSHttpMessage createJSHttpMessage(HttpMessage message) {
        JSHttpMessage jsHttpMessage;
        Type type = getHttpMessageType(message.getType());
        switch (type){
            case REQUEST:
                jsHttpMessage = new JSHttpRequest(message);
                break;
            case RESPONSE:
                jsHttpMessage = new JSHttpResponse(message);
                break;
            case PROGRESS:
                jsHttpMessage = new JSHttpProgress(message);
                break;
            default:
                jsHttpMessage = null;
                break;
        }
        return jsHttpMessage;
    }

    private static void addChildWindows(Window window, JSWindow jsWindow) {
        List<Window> childWindows = window.getChildren();
        if (childWindows == null) {
            return;
        }
        List<? super JSWindow> childJSWindows = new ArrayList<JSWindow>(childWindows.size());
        for (Window childWindow : childWindows) {
            final JSWindow childJSWindow = JSFactory.createJSWindow(childWindow.getURI(), jsWindow);
            childJSWindows.add(childJSWindow);
        }
        ((JSWindowImpl) jsWindow).setChildren(childJSWindows.toArray(JSWindow.EMPTY_ARRAY));
    }
}
