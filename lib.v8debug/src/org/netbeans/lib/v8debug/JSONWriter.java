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

import java.util.Map;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import static org.netbeans.lib.v8debug.JSONConstants.*;
import org.netbeans.lib.v8debug.commands.Backtrace;
import org.netbeans.lib.v8debug.commands.ChangeBreakpoint;
import org.netbeans.lib.v8debug.commands.ClearBreakpoint;
import org.netbeans.lib.v8debug.commands.Continue;
import org.netbeans.lib.v8debug.commands.Evaluate;
import org.netbeans.lib.v8debug.commands.Flags;
import org.netbeans.lib.v8debug.commands.Frame;
import org.netbeans.lib.v8debug.commands.GC;
import org.netbeans.lib.v8debug.commands.Lookup;
import org.netbeans.lib.v8debug.commands.References;
import org.netbeans.lib.v8debug.commands.Scope;
import org.netbeans.lib.v8debug.commands.Scopes;
import org.netbeans.lib.v8debug.commands.Scripts;
import org.netbeans.lib.v8debug.commands.SetBreakpoint;
import org.netbeans.lib.v8debug.commands.SetExceptionBreak;
import org.netbeans.lib.v8debug.commands.SetVariableValue;
import org.netbeans.lib.v8debug.commands.Source;
import org.netbeans.lib.v8debug.commands.V8Flags;
import org.netbeans.lib.v8debug.vars.NewValue;
import org.netbeans.lib.v8debug.vars.ReferencedValue;
import org.netbeans.lib.v8debug.vars.V8Number;
import org.netbeans.lib.v8debug.vars.V8Value;

/**
 *
 * @author Martin Entlicher
 */
@SuppressWarnings("unchecked")
public class JSONWriter {
    
    private JSONWriter() {}
    
    public static JSONObject store(V8Request request) {
        JSONObject obj = new JSONObject();
        obj.put(SEQ, request.getSequence());
        obj.put(TYPE, V8Type.request.toString());
        V8Command command = request.getCommand();
        obj.put(COMMAND, command.toString());
        V8Arguments arguments = request.getArguments();
        if (arguments != null) {
            obj.put(ARGUMENTS, store(command, arguments));
        }
        return obj;
    }
    
    private static Object store(V8Command command, V8Arguments arguments) {
        JSONObject obj = new JSONObject();
        switch(command) {
            case Backtrace:
                Backtrace.Arguments btargs = (Backtrace.Arguments) arguments;
                storeIf(btargs.getFromFrame(), obj, FROM_FRAME);
                storeIf(btargs.getToFrame(), obj, TO_FRAME);
                storeIf(btargs.isBottom(), obj, BOTTOM);
                storeIf(btargs.isInlineRefs(), obj, INLINE_REFS);
                return obj;
            case Continue:
                Continue.Arguments cargs = (Continue.Arguments) arguments;
                obj.put(ARGS_STEP_ACTION, cargs.getStepAction().toString());
                storeIf(cargs.getStepCount(), obj, ARGS_STEP_COUNT);
                return obj;
            case Setbreakpoint:
                SetBreakpoint.Arguments sbargs = (SetBreakpoint.Arguments) arguments;
                String bpType;
                if (V8Breakpoint.Type.scriptName.equals(sbargs.getType())) {
                    bpType = "script";
                } else {
                    bpType = sbargs.getType().toString();
                }
                obj.put(TYPE, bpType);
                obj.put(TARGET, sbargs.getTarget());
                storeIf(sbargs.getLine(), obj, LINE);
                storeIf(sbargs.getColumn(), obj, COLUMN);
                storeIf(sbargs.isEnabled(), obj, BREAK_ENABLED);
                storeIf(sbargs.getCondition(), obj, BREAK_CONDITION);
                storeIf(sbargs.getIgnoreCount(), obj, BREAK_IGNORE_COUNT);
                return obj;
            case Changebreakpoint:
                ChangeBreakpoint.Arguments chbargs = (ChangeBreakpoint.Arguments) arguments;
                obj.put(BREAK_POINT, chbargs.getBreakpoint());
                storeIf(chbargs.isEnabled(), obj, BREAK_ENABLED);
                obj.put(BREAK_CONDITION, chbargs.getCondition());
                storeIf(chbargs.getIgnoreCount(), obj, BREAK_IGNORE_COUNT);
                return obj;
            case Clearbreakpoint:
                ClearBreakpoint.Arguments cbargs = (ClearBreakpoint.Arguments) arguments;
                obj.put(BREAK_POINT, cbargs.getBreakpoint());
                return obj;
            case Setexceptionbreak:
                SetExceptionBreak.Arguments sebargs = (SetExceptionBreak.Arguments) arguments;
                obj.put(TYPE, sebargs.getType().toString());
                obj.put(BREAK_ENABLED, sebargs.isEnabled());
                return obj;
            case Evaluate:
                Evaluate.Arguments eargs = (Evaluate.Arguments) arguments;
                obj.put(EVAL_EXPRESSION, eargs.getExpression());
                storeIf(eargs.getFrame(), obj, FRAME);
                storeIf(eargs.isGlobal(), obj, EVAL_GLOBAL);
                storeIf(eargs.isDisableBreak(), obj, EVAL_DISABLE_BREAK);
                JSONArray additionalContexts = store(eargs.getAdditionalContext());
                if (additionalContexts != null) {
                    obj.put(EVAL_ADDITIONAL_CONTEXT, additionalContexts);
                }
                return obj;
            case Frame:
                Frame.Arguments fargs = (Frame.Arguments) arguments;
                storeIf(fargs.getFrameNumber(), obj, NUMBER);
                return obj;
            case Lookup:
                Lookup.Arguments largs = (Lookup.Arguments) arguments;
                obj.put(HANDLES, array(largs.getHandles()));
                storeIf(largs.isIncludeSource(), obj, INCLUDE_SOURCE);
                return obj;
            case References:
                References.Arguments rargs = (References.Arguments) arguments;
                obj.put(TYPE, rargs.getType().name());
                obj.put(HANDLE, rargs.getHandle());
                return obj;
            case Scope:
                Scope.Arguments sargs = (Scope.Arguments) arguments;
                obj.put(NUMBER, sargs.getScopeNumber());
                storeIf(sargs.getFrameNumber(), obj, FRAME_NUMBER);
                return obj;
            case Scopes:
                Scopes.Arguments ssargs = (Scopes.Arguments) arguments;
                storeIf(ssargs.getFrameNumber(), obj, FRAME_NUMBER);
                return obj;
            case Scripts:
                Scripts.Arguments scargs = (Scripts.Arguments) arguments;
                if (scargs.getTypes() != null) {
                    obj.put(TYPES, scargs.getTypes().getIntTypes());
                }
                if (scargs.getIds() != null) {
                    obj.put(IDs, array(scargs.getIds()));
                }
                storeIf(scargs.isIncludeSource(), obj, INCLUDE_SOURCE);
                storeIf(scargs.getNameFilter(), obj, FILTER);
                storeIf(scargs.getIdFilter(), obj, FILTER);
                return obj;
            case Source:
                Source.Arguments srcargs = (Source.Arguments) arguments;
                storeIf(srcargs.getFrame(), obj, FRAME);
                storeIf(srcargs.getFromLine(), obj, FROM_LINE);
                storeIf(srcargs.getToLine(), obj, TO_LINE);
                return obj;
            case SetVariableValue:
                SetVariableValue.Arguments svargs = (SetVariableValue.Arguments) arguments;
                obj.put(NAME, svargs.getName());
                obj.put(NEW_VALUE, store(svargs.getNewValue()));
                JSONObject scope = new JSONObject();
                scope.put(NUMBER, svargs.getScopeNumber());
                storeIf(svargs.getScopeFrameNumber(), scope, FRAME_NUMBER);
                obj.put(SCOPE, scope);
                return obj;
            case Gc:
                GC.Arguments gcargs = (GC.Arguments) arguments;
                obj.put(TYPE, gcargs.getType());
                return obj;
            case V8flags:
                V8Flags.Arguments v8flargs = (V8Flags.Arguments) arguments;
                obj.put(FLAGS, v8flargs.getFlags());
                return obj;
            case Flags:
                Flags.Arguments flargs = (Flags.Arguments) arguments;
                Map<String, Boolean> flags = flargs.getFlags();
                if (flags != null) {
                    JSONArray arr = new JSONArray();
                    for (Map.Entry<String, Boolean> flagEntry : flags.entrySet()) {
                        JSONObject f = new JSONObject();
                        f.put(NAME, flagEntry.getKey());
                        f.put(VALUE, flagEntry.getValue());
                        arr.add(f);
                    }
                    obj.put(FLAGS, arr);
                }
                return obj;
            default:
                return null;
        }
    }
    
    private static JSONArray array(long[] array) {
        JSONArray jsArray = new JSONArray();
        for (int i = 0; i < array.length; i++) {
            jsArray.add(array[i]);
        }
        return jsArray;
    }
    
    private static void storeIf(PropertyBoolean prop, JSONObject obj, String propertyName) {
        if (prop.hasValue()) {
            obj.put(propertyName, prop.getValue());
        }
    }
    
    private static void storeIf(PropertyLong prop, JSONObject obj, String propertyName) {
        if (prop.hasValue()) {
            obj.put(propertyName, prop.getValue());
        }
    }
    
    private static void storeIf(String prop, JSONObject obj, String propertyName) {
        if (prop != null) {
            obj.put(propertyName, prop);
        }
    }
    
    private static JSONArray store(Evaluate.Arguments.Context[] contexts) {
        if (contexts == null) {
            return null;
        }
        JSONArray array = new JSONArray();
        for (Evaluate.Arguments.Context c : contexts) {
            array.add(store(c));
        }
        return array;
    }
    
    private static JSONObject store(Evaluate.Arguments.Context context) {
        JSONObject obj = new JSONObject();
        obj.put(NAME, context.getName());
        obj.put(HANDLE, context.getHandle());
        return obj;
    }
    
    private static JSONObject store(NewValue value) {
        JSONObject obj = new JSONObject();
        if (value.getHandle().hasValue()) {
            obj.put(HANDLE, value.getHandle().getValue());
        } else {
            obj.put(TYPE, value.getType().toString());
            storeIf(value.getDescription(), obj, STRING_DESCRIPTION);
        }
        return obj;
    }
}
