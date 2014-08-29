/* 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.lib.v8debug;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import static org.netbeans.lib.v8debug.JSONConstants.*;
import org.netbeans.lib.v8debug.commands.Backtrace;
import org.netbeans.lib.v8debug.commands.ClearBreakpoint;
import org.netbeans.lib.v8debug.commands.Continue;
import org.netbeans.lib.v8debug.commands.Evaluate;
import org.netbeans.lib.v8debug.commands.Frame;
import org.netbeans.lib.v8debug.commands.GC;
import org.netbeans.lib.v8debug.commands.ListBreakpoints;
import org.netbeans.lib.v8debug.commands.Lookup;
import org.netbeans.lib.v8debug.commands.References;
import org.netbeans.lib.v8debug.commands.Scope;
import org.netbeans.lib.v8debug.commands.Scopes;
import org.netbeans.lib.v8debug.commands.Scripts;
import org.netbeans.lib.v8debug.commands.SetBreakpoint;
import org.netbeans.lib.v8debug.commands.SetExceptionBreak;
import org.netbeans.lib.v8debug.commands.SetVariableValue;
import org.netbeans.lib.v8debug.commands.Source;
import org.netbeans.lib.v8debug.commands.Threads;
import org.netbeans.lib.v8debug.commands.Version;
import org.netbeans.lib.v8debug.events.AfterCompileEventBody;
import org.netbeans.lib.v8debug.events.BreakEventBody;
import org.netbeans.lib.v8debug.events.ExceptionEventBody;
import org.netbeans.lib.v8debug.vars.V8Boolean;
import org.netbeans.lib.v8debug.vars.V8Function;
import org.netbeans.lib.v8debug.vars.V8Number;
import org.netbeans.lib.v8debug.vars.V8Object;
import org.netbeans.lib.v8debug.vars.V8ScriptValue;
import org.netbeans.lib.v8debug.vars.V8String;
import org.netbeans.lib.v8debug.vars.V8Value;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author Martin Entlicher
 */
public class JSONReader {

    private JSONReader() {}
    
    public static V8Type getType(JSONObject obj) throws IllegalArgumentException {
        String type = (String) obj.get(TYPE);
        if (type == null) {
            throw new IllegalArgumentException("No type in "+obj.toJSONString());
        }
        return V8Type.valueOf(type);
    }
    
    public static V8Response getResponse(JSONObject obj) throws IllegalArgumentException {
        long sequence = (Long) obj.get(SEQ);
        V8Type type = getType(obj);
        long requestSequence = (Long) obj.get(SEQ_REQUEST);
        String commandName = (String) obj.get(COMMAND);
        V8Command command = V8Command.fromString(commandName);
        boolean running = (Boolean) obj.get(RUNNING);
        boolean success = (Boolean) obj.get(SUCCESS);
        V8Body body = null;
        String errorMessage = null;
        if (success) {
            Object bodyObj = obj.get(BODY);
            if (bodyObj instanceof JSONObject) {
                body = getBody(command, (JSONObject) bodyObj);
            } else if (bodyObj instanceof JSONArray) {
                body = getBody(command, (JSONArray) bodyObj);
            } else if (body != null) {
                throw new IllegalArgumentException("Unknown body "+bodyObj+" in "+obj.toJSONString());
            }
        } else {
            errorMessage = (String) obj.get(MESSAGE);
        }
        return new V8Response(sequence, requestSequence, command, body, running, success, errorMessage);
    }
    
    public static V8Event getEvent(JSONObject obj) throws IllegalArgumentException {
        long sequence = (Long) obj.get(SEQ);
        String eventName = (String) obj.get(EVENT);
        V8Event.Kind eventKind = V8Event.Kind.fromString(eventName);
        V8Body body = null;
        obj = (JSONObject) obj.get(BODY);
        switch (eventKind) {
            case AfterCompile:
                V8Script script = getScript((JSONObject) obj.get(EVT_SCRIPT));
                body = new AfterCompileEventBody(script);
                break;
            case Break:
                String invocationText = (String) obj.get(EVT_INVOCATION_TEXT);
                long sourceLine = getLong(obj, EVT_SOURCE_LINE);
                long sourceColumn = getLong(obj, EVT_SOURCE_COLUMN);
                String sourceLineText = (String) obj.get(EVT_SOURCE_LINE_TEXT);
                V8ScriptLocation scriptLocation = getScriptLocation((JSONObject) obj.get(EVT_SCRIPT));
                long[] breakpoints = getLongArray((JSONArray) obj.get(EVT_BREAKPOINTS));
                body = new BreakEventBody(invocationText, sourceLine, sourceColumn, sourceLineText, scriptLocation, breakpoints);
                break;
            case Exception:
                boolean uncaught = (boolean) obj.get(EVT_UNCAUGHT);
                String exception = (String) obj.get(EVT_EXCEPTION);
                sourceLine = getLong(obj, EVT_SOURCE_LINE);
                sourceColumn = getLong(obj, EVT_SOURCE_COLUMN);
                sourceLineText = (String) obj.get(EVT_SOURCE_LINE_TEXT);
                scriptLocation = getScriptLocation((JSONObject) obj.get(EVT_SCRIPT));
                body = new ExceptionEventBody(uncaught, exception, sourceLine, sourceColumn, sourceLineText, scriptLocation);
                break;
            default:
                new IllegalArgumentException("Unknown event "+eventName+" in "+obj.toJSONString()).printStackTrace();
        }
        return new V8Event(sequence, eventKind, body);
    }

    private static V8Body getBody(V8Command command, JSONObject obj) {
        switch (command) {
            case Listbreakpoints:
                V8Breakpoint[] breakpoints = getBreakpoints((JSONArray) obj.get(BREAK_POINTS));
                boolean breakOnExceptions = getBoolean(obj, BREAK_ON_EXCEPTIONS);
                boolean breakOnUncaughtExceptions = getBoolean(obj, BREAK_ON_UNCAUGHT_EXCEPTIONS);
                return new ListBreakpoints.ResponseBody(breakpoints, breakOnExceptions, breakOnUncaughtExceptions);
            case Setbreakpoint:
                String type = getString(obj, TYPE);
                long bpId = getLong(obj, BREAK_POINT);
                String scriptName = null;
                if ("scriptName".equals(type)) {
                    scriptName = getString(obj, SCRIPT_NAME);
                }
                long line = getLong(obj, LINE, -1);
                long column = getLong(obj, COLUMN, -1);
                V8Breakpoint.ActualLocation[] actualLocations = getActualLocations((JSONArray) obj.get(BREAK_ACTUAL_LOCATIONS));
                return new SetBreakpoint.ResponseBody(V8Breakpoint.Type.typeFrom(type), bpId,
                                                      scriptName, line, column, actualLocations);
            case Setexceptionbreak:
                String typeName = getString(obj, TYPE);
                V8ExceptionBreakType extype = V8ExceptionBreakType.valueOf(typeName);
                if (extype == null) {
                    throw new IllegalArgumentException("Unknown exception breakpoint type: '"+typeName+"'.");
                }
                boolean enabled = getBoolean(obj, BREAK_ENABLED);
                return new SetExceptionBreak.ResponseBody(extype, enabled);
            case Clearbreakpoint:
                bpId = getLong(obj, BREAK_POINT);
                return new ClearBreakpoint.ResponseBody(bpId);
            case Backtrace:
                long fromFrame = getLong(obj, FROM_FRAME);
                long toFrame = getLong(obj, TO_FRAME);
                long totalFrames = getLong(obj, TOTAL_FRAMES);
                V8Frame[] frames = getFrames((JSONArray) obj.get(FRAMES));
                return new Backtrace.ResponseBody(fromFrame, toFrame, totalFrames, frames);
            case Frame:
                V8Frame frame = getFrame(obj);
                return new Frame.ResponseBody(frame);
            case Lookup:
                Map<Long, V8Value> valuesByHandle = new HashMap<>();
                for (Object element : obj.values()) {
                    V8Value value = getValue((JSONObject) element);
                    valuesByHandle.put(value.getHandle(), value);
                }
                return new Lookup.ResponseBody(valuesByHandle);
            case Evaluate:
                V8Value value = getValue(obj);
                return new Evaluate.ResponseBody(value);
            case Setvariablevalue:
                value = getValue((JSONObject) obj.get(NEW_VALUE));
                return new SetVariableValue.ResponseBody(value);
            case Scope:
                V8Scope scope = getScope(obj, null);
                return new Scope.ResponseBody(scope);
            case Scopes:
                long fromScope = getLong(obj, FROM_SCOPE);
                long toScope = getLong(obj, TO_SCOPE);
                long totalScopes = getLong(obj, TOTAL_SCOPES);
                V8Scope[] scopes = getScopes((JSONArray) obj.get(SCOPES), null);
                return new Scopes.ResponseBody(fromScope, toScope, totalScopes, scopes);
            case Source:
                String source = getString(obj, SOURCE);
                long fromLine = getLong(obj, FROM_LINE);
                long toLine = getLong(obj, TO_LINE);
                long fromPosition = getLong(obj, FROM_POSITION);
                long toPosition = getLong(obj, TO_POSITION);
                long totalLines = getLong(obj, TOTAL_LINES);
                return new Source.ResponseBody(source, fromLine, toLine, fromPosition, toPosition, totalLines);
            case Threads:
                long numThreads = getLong(obj, TOTAL_THREADS);
                Map<Long, Boolean> threads = getThreads((JSONArray) obj.get(THREADS));
                return new Threads.ResponseBody(numThreads, threads);
            case Gc:
                long before = getLong(obj, GC_BEFORE);
                long after = getLong(obj, GC_AFTER);
                return new GC.ResponseBody(before, after);
            case Version:
                String version = getString(obj, BODY_VERSION);
                return new Version.ResponseBody(version);
            default:
                return null;
        }
    }

    private static V8Body getBody(V8Command command, JSONArray array) {
        switch (command) {
            case Scripts:
                int n = array.size();
                V8Script[] scripts = new V8Script[n];
                for (int i = 0; i < n; i++) {
                    scripts[i] = getScript((JSONObject) array.get(i));
                }
                return new Scripts.ResponseBody(scripts);
            case References:
                n = array.size();
                V8Value[] refs = new V8Value[n];
                for (int i = 0; i < n; i++) {
                    refs[i] = getValue((JSONObject) array.get(i));
                }
                return new References.ResponseBody(refs);
            default:
                return null;
        }
    }
    
    /**
     * @return the String property value, or <code>null</code> when not defined.
     */
    private static String getString(JSONObject obj, String propertyName) {
        return (String) obj.get(propertyName);
    }
    
    /**
     * @return the long property value, or <code>-1</code> when not defined.
     */
    private static long getLong(JSONObject obj, String propertyName) {
        return getLong(obj, propertyName, -1);
    }
    
    /**
     * @return the long property value, or the defaultValue when not defined.
     */
    private static long getLong(JSONObject obj, String propertyName, long defaultValue) {
        Object prop = obj.get(propertyName);
        if (prop == null) {
            return defaultValue;
        }
        if (prop instanceof Long) {
            return (Long) prop;
        } else {
            String str = (String) prop;
            return Long.parseLong(str);
        }
    }
    
    /**
     * @return the boolean property value, or <code>false</code> when not defined.
     */
    private static boolean getBoolean(JSONObject obj, String propertyName) {
        Object prop = obj.get(propertyName);
        if (prop == null) {
            return false;
        }
        return (Boolean) prop;
    }
    
    private static V8ScriptLocation getScriptLocation(JSONObject obj) {
        String name = getString(obj, NAME);
        long line = getLong(obj, SCRIPT_LINE_OFFSET);
        long column = getLong(obj, SCRIPT_COLUMN_OFFSET);
        long lineCount = getLong(obj, SCRIPT_LINE_COUNT);
        return new V8ScriptLocation(name, line, column, lineCount);
    }
    
    private static V8Script getScript(JSONObject obj) {
        String name = getString(obj, NAME);
        long id = getLong(obj, ID);
        long lineOffset = getLong(obj, SCRIPT_LINE_OFFSET);
        long columnOffset = getLong(obj, SCRIPT_COLUMN_OFFSET);
        long lineCount = getLong(obj, SCRIPT_LINE_COUNT);
        Object data = obj.get(DATA);
        String source = getString(obj, SOURCE);
        String sourceStart = getString(obj, SOURCE_START);
        long sourceLength = getLong(obj, SOURCE_LENGTH);
        long scriptTypeNum = getLong(obj, SCRIPT_TYPE);
        V8Script.Type scriptType = V8Script.Type.valueOf((int) scriptTypeNum);
        long compilationTypeNum = getLong(obj, COMPILATION_TYPE);
        V8Script.CompilationType compilationType = V8Script.CompilationType.valueOf((int) compilationTypeNum);
        String evalFromScript = getString(obj, EVAL_FROM_SCRIPT);
        V8Script.EvalFromLocation evalFromLocation;
        if (V8Script.CompilationType.EVAL.equals(compilationType)) {
            evalFromLocation = new V8Script.EvalFromLocation(getLong(obj, LINE), getLong(obj, COLUMN));
        } else {
            evalFromLocation = null;
        }
        return new V8Script(name, id, lineOffset, columnOffset, lineCount, data, source, sourceStart, sourceLength, scriptType, compilationType, evalFromScript, evalFromLocation);
    }

    private static V8Value getValue(JSONObject obj) {
        long handle = getLong(obj, HANDLE);
        V8Value.Type type = V8Value.Type.fromString(getString(obj, TYPE));
        String text = getString(obj, TEXT);
        switch (type) {
            case Boolean:
                return new V8Boolean(handle, getBoolean(obj, VALUE), text);
            case Number:
                return new V8Number(handle, getLong(obj, VALUE), text);
            case String:
                return new V8String(handle, getString(obj, VALUE), text);
            case Function:
                String name = getString(obj, NAME);
                String inferredName = getString(obj, FUNCTION_INFERRED_NAME);
                String source = getString(obj, SOURCE);
                long scriptRef = getReference(obj, SCRIPT);
                long scriptId = getLong(obj, SCRIPT_ID);
                long position = getLong(obj, POSITION);
                long line = getLong(obj, LINE);
                long column = getLong(obj, COLUMN);
                long constructorFunctionHandle = getReference(obj, VALUE_CONSTRUCTOR_FUNCTION);
                long protoObject = getReference(obj, VALUE_PROTO_OBJECT);
                long prototypeObject = getReference(obj, VALUE_PROTOTYPE_OBJECT);
                Map<String, Long> properties = getReferences((JSONArray) obj.get(VALUE_PROPERTIES));
                return new V8Function(handle, constructorFunctionHandle,
                                      protoObject, prototypeObject,
                                      name, inferredName,
                                      source, scriptRef, scriptId,
                                      position, line, column, properties, text);
            case Object:
                String className = getString(obj, VALUE_CLASS_NAME);
                constructorFunctionHandle = getReference(obj, VALUE_CONSTRUCTOR_FUNCTION);
                protoObject = getReference(obj, VALUE_PROTO_OBJECT);
                prototypeObject = getReference(obj, VALUE_PROTOTYPE_OBJECT);
                properties = getReferences((JSONArray) obj.get(VALUE_PROPERTIES));
                return new V8Object(handle, className,
                                    constructorFunctionHandle,
                                    protoObject, prototypeObject,
                                    properties, text);
            case Frame:
                // ? TODO
                return new V8Value(handle, type, text);
            case Script:
                V8Script script = getScript(obj);
                return new V8ScriptValue(handle, script, text);
            default: // null, undefined
                return new V8Value(handle, type, text);
        }
    }

    private static long[] getLongArray(JSONArray array) {
        if (array == null) {
            return null;
        }
        int n = array.size();
        long[] iarr = new long[n];
        for (int i = 0; i < n; i++) {
            iarr[i] = (Long) array.get(i);
        }
        return iarr;
    }
    
    private static V8Breakpoint[] getBreakpoints(JSONArray array) {
        int n = array.size();
        V8Breakpoint[] breakpoints = new V8Breakpoint[n];
        for (int i = 0; i < n; i++) {
            breakpoints[i] = getBreakpoint((JSONObject) array.get(i));
        }
        return breakpoints;
    }

    private static V8Breakpoint getBreakpoint(JSONObject obj) {
        String typeStr = (String) obj.get(TYPE);
        if ("scriptName".equals(typeStr)) {
            typeStr = V8Breakpoint.Type.script.name();
        }
        V8Breakpoint.Type type = V8Breakpoint.Type.valueOf(typeStr);
        long scriptId;
        String scriptName;
        if (V8Breakpoint.Type.scriptId.equals(type)) {
            scriptId = getLong(obj, SCRIPT_ID);
            scriptName = null;
        } else {
            scriptId = -1;
            scriptName = getString(obj, SCRIPT_NAME);
        }
        long number = getLong(obj, NUMBER);
        long line = getLong(obj, LINE);
        long column = getLong(obj, COLUMN);
        long groupId = getLong(obj, BREAK_GROUP_ID);
        long hitCount = getLong(obj, BREAK_HIT_COUNT, 0);
        boolean active = getBoolean(obj, BREAK_ACTIVE);
        long ignoreCount = getLong(obj, BREAK_IGNORE_COUNT, 0);
        V8Breakpoint.ActualLocation[] actualLocations = getActualLocations((JSONArray) obj.get(BREAK_ACTUAL_LOCATIONS));
        return new V8Breakpoint(type, scriptId, scriptName, number, line, column, groupId, hitCount, active, ignoreCount, actualLocations);
    }
    
    private static V8Breakpoint.ActualLocation[] getActualLocations(JSONArray array) {
        int n = array.size();
        V8Breakpoint.ActualLocation[] locations = new V8Breakpoint.ActualLocation[n];
        for (int i = 0; i < n; i++) {
            JSONObject location = (JSONObject) array.get(i);
            long line = getLong(location, LINE);
            long column = getLong(location, COLUMN);
            String scriptName = getString(location, SCRIPT_NAME);
            if (scriptName != null) {
                locations[i] = new V8Breakpoint.ActualLocation(line, column, scriptName);
            } else {
                long scriptId = getLong(location, SCRIPT_ID);
                locations[i] = new V8Breakpoint.ActualLocation(line, column, scriptId);
            }
        }
        return locations;
    }
    
    private static V8Frame[] getFrames(JSONArray array) {
        int n = array.size();
        V8Frame[] frames = new V8Frame[n];
        for (int i = 0; i < n; i++) {
            frames[i] = getFrame((JSONObject) array.get(i));
        }
        return frames;
    }
    
    private static V8Frame getFrame(JSONObject obj) {
        long index = getLong(obj, FRAME_INDEX);
        long receiver = getReference(obj, FRAME_RECEIVER);
        long func = getReference(obj, FRAME_FUNC);
        long scriptRef = getReference(obj, SCRIPT);
        boolean constructCall = getBoolean(obj, FRAME_CONSTRUCT_CALL);
        boolean atReturn = getBoolean(obj, FRAME_AT_RETURN);
        boolean debuggerFrame = getBoolean(obj, FRAME_DEBUGGER);
        Map<String, Long> arguments = getReferences((JSONArray) obj.get(FRAME_ARGUMENTS));
        Map<String, Long> locals = getReferences((JSONArray) obj.get(FRAME_LOCALS));
        long position = getLong(obj, POSITION);
        long line = getLong(obj, LINE);
        long column = getLong(obj, COLUMN);
        String sourceLineText = getString(obj, EVT_SOURCE_LINE_TEXT);
        V8Scope[] scopes = getScopes((JSONArray) obj.get(SCOPES), index);
        return new V8Frame(index, receiver, func, scriptRef, constructCall, atReturn,
                           debuggerFrame, arguments, locals, position, line, column,
                           sourceLineText, scopes);
    }
    
    private static Map<String, Long> getReferences(JSONArray array) {
        Map<String, Long> references = new HashMap<>();
        for (Object obj : array) {
            String name = getString((JSONObject) obj, NAME);
            Long ref = getReference((JSONObject) obj, VALUE);
            references.put(name, ref);
        }
        return references;
    }

    private static long getReference(JSONObject obj) {
        return getLong(obj, REF);
    }
    
    private static Long getReference(JSONObject obj, String propertyName) {
        JSONObject ref = (JSONObject) obj.get(propertyName);
        if (ref == null) {
            return null;
        }
        return getReference(ref);
    }
    
    private static V8Scope[] getScopes(JSONArray array, Long frameIndex) {
        int n = array.size();
        V8Scope[] scopes = new V8Scope[n];
        for (int i = 0; i < n; i++) {
            scopes[i] = getScope((JSONObject) array.get(i), frameIndex);
        }
        return scopes;
    }

    private static V8Scope getScope(JSONObject scope, Long frameIndex) {
        V8Scope.Type type = V8Scope.Type.valueOf((int) getLong(scope, TYPE));
        long index = getLong(scope, SCOPE_INDEX);
        long scopeFrameIndex = getLong(scope, FRAME_INDEX);
        if (scopeFrameIndex >= 0) {
            frameIndex = scopeFrameIndex;
        }
        if (frameIndex == null) {
            frameIndex = scopeFrameIndex;
        }
        V8Object object = null; // TODO
        return new V8Scope(index, frameIndex, type, object);
    }
    
    private static Map<Long, Boolean> getThreads(JSONArray array) {
        Map<Long, Boolean> threads = new LinkedHashMap<>();
        for (Object o : array) {
            JSONObject obj = (JSONObject) o;
            threads.put(getLong(obj, ID), getBoolean(obj, CURRENT));
        }
        return threads;
    }

}
