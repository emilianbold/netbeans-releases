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

import java.util.LinkedList;
import java.util.List;
import org.w3c.dom.Node;

/**
 *
 * @author ads, jdeva
 */
public class Breakpoint extends BaseMessageChildElement{
    protected static final String FUNCTION = "function";            // NOI18N
    protected static final String LINENO = "lineno";                // NOI18N
    protected static final String EXCEPTION = "exception";          // NOI18N
    protected static final String HIT_VALUE = "hit_value";          // NOI18N
    protected static final String HIT_CONDITION = "hit_condition";  // NOI18N
    protected static final String HIT_COUNT = "hit_count";          // NOI18N
    protected static final String TYPE = "type";                    // NOI18N
    protected static final String FILENAME = "filename";            // NOI18N
    protected static final String STATE = "state";                  // NOI18N
    protected static final String ID = "id";                        // NOI18N

    Breakpoint(Node node) {
        super(node);
    }

    public String getId() {
        return getAttribute(ID);
    }

    public State getState() {
        String state = getAttribute(STATE);
        return State.valueOf(state.toUpperCase());
    }

    public String getFileURI() {
        return getAttribute(FILENAME);
    }

    public String getFunction() {
        return getAttribute(FUNCTION);
    }

    public String getException() {
        return getAttribute(EXCEPTION);
    }

    public Type getType() {
        String type = getAttribute(TYPE);
        return Type.valueOf(type.toUpperCase());
    }

    public int getLineNumber() {
        return getInt(LINENO);
    }

    public int getHitValue() {
        return getInt(HIT_VALUE);
    }

    public int getHitCount() {
        return getInt(HIT_COUNT);
    }

    public HitCondition getHitCondition() {
        String type = getAttribute(TYPE);
        return HitCondition.valueOf(type.toUpperCase());
    }
    
    public enum HitCondition {
        // Keep the names same as org.netbeans.api.debugger.Breakpoint.HIT_COUNT_FILTERING_STYLE 
        EQUAL("=="),
        GREATER(">="),
        MULTIPLE("%");
        private String value;

        HitCondition(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    protected enum Type {
        LINE("-n "),
        CALL("-m "),
        RETURN("-m "),
        EXCEPTION("-x "),
        CONDITIONAL("-- "),
        WATCH("-- ");
        private String argSwitch;

        Type(String argSwitch) {
            this.argSwitch = argSwitch;
        }

        public String getArgumentSwitch() {
            return argSwitch;
        }

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }
    }

    public enum State {
        ENABLED,
        DISABLED;

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }
    }

    public static abstract class BreakpointSetCommand extends Command {
        protected static final String TYPE_ARG = "-t ";                 // NOI18N
        protected static final String STATE_ARG = "-s ";                // NOI18N
        protected static final String TEMP_ARG = "-r ";                 // NOI18N
        protected static final String HIT_VALUE_ARG = "-h ";            // NOI18N
        protected static final String HIT_CONDITION_ARG = "-o ";        // NOI18N        
        private State state;
        private Type type;
        private String fileURI;
        private boolean isTemporary;
        private int hitValue = -1;
        private HitCondition hitCondition;
        private int lineNumber = -1;
        private String condition;

        BreakpointSetCommand(int transactionId, Type type) {
            this(CommandMap.BREAKPOINT_SET.getCommand(), transactionId, type);
        }

        BreakpointSetCommand(String command, int transactionId, Type type) {
            super(command, transactionId);
            state = State.ENABLED;
            this.type = type;
        }

        BreakpointSetCommand(String command, int transactionId) {
            super(command, transactionId);
            state = State.ENABLED;
        }

        @Override
        protected String getArguments() {
            return getBaseArguments().toString();
        }

        StringBuilder getBaseArguments() {
            StringBuilder builder = new StringBuilder();

            //set type - may be null in case of update
            if (type != null) {
                builder.append(TYPE_ARG);
                builder.append(type);                
            }

            //optional arguments
            if (state != null) {
                builder.append(SPACE);
                builder.append(STATE_ARG);
                builder.append(state.toString());
            }

            builder.append(SPACE);
            builder.append(TEMP_ARG);
            builder.append(isTemporary ? 1 : 0);

            if (fileURI != null) {
                builder.append(SPACE);
                builder.append(FILE_ARG);
                builder.append(fileURI);
            }

            if (lineNumber != -1) {
                builder.append(SPACE);
                builder.append(Type.LINE.getArgumentSwitch());
                builder.append(lineNumber);
            }

            if (hitValue != -1) {
                builder.append(SPACE);
                builder.append(HIT_VALUE_ARG);
                builder.append(hitValue);
            }

            if (hitCondition != null) {
                builder.append(SPACE);
                builder.append(HIT_CONDITION_ARG);
                builder.append(hitCondition.toString());
            }
            
            if (condition != null) {
                builder.append(SPACE);
                builder.append(Type.CONDITIONAL.getArgumentSwitch());
                builder.append(condition);
            }

            return builder;
        }

        public void setTemporary(boolean isTemporary) {
            this.isTemporary = isTemporary;
        }

        public void setFileURI(String uri) {
            this.fileURI = uri;
        }

        public void setState(boolean enabled) {
            this.state = enabled ? State.ENABLED : State.DISABLED;
        }

        public void setType(Type type) {
            this.type = type;
        }

        public void setHitCondition(String hitCondition) {
            this.hitCondition = HitCondition.valueOf(hitCondition.toUpperCase());
        }

        public void setHitValue(int hitValue) {
            this.hitValue = hitValue;
        }

        public void setCondition(String condition) {
            this.condition = condition;
        }
        
        public void setLineNumber(int lineNumber) {
            this.lineNumber = lineNumber;
        }
    }

    public static class LineBreakpointSetCommand extends BreakpointSetCommand {
        LineBreakpointSetCommand(int transactionId, String uri, int lineNumber) {
            super(transactionId, Type.LINE);
            setLineNumber(lineNumber);
            setFileURI(uri);
        }
    }

    public static class CallBreakpointSetCommand extends BreakpointSetCommand {
        private String functionName;

        CallBreakpointSetCommand(int transactionId, String functionName) {
            super(transactionId, Type.CALL);
            this.functionName = functionName;
        }

        @Override
        protected String getArguments() {
            StringBuilder builder = getBaseArguments();
            builder.append(SPACE);
            builder.append(Type.CALL.getArgumentSwitch());
            builder.append(functionName);
            return builder.toString();
        }
    }

    public static class ReturnBreakpointSetCommand extends CallBreakpointSetCommand {
        ReturnBreakpointSetCommand(int transactionId, String functionName) {
            super(transactionId, functionName);
            setType(Type.RETURN);
        }
    }

    public static class ExceptionBreakpointCommand extends BreakpointSetCommand {
        private String exception;

        ExceptionBreakpointCommand(int transactionId, String exception) {
            super(transactionId, Type.EXCEPTION);
            this.exception = exception;
        }

        @Override
        protected String getArguments() {
            StringBuilder builder = getBaseArguments();
            builder.append(SPACE);
            builder.append(Type.EXCEPTION.getArgumentSwitch());
            builder.append(exception);
            return builder.toString();
        }
    }

    public static class ConditionalBreakpointSetCommand extends BreakpointSetCommand {
        private String expression;

        ConditionalBreakpointSetCommand(int transactionId, String expression) {
            super(transactionId, Type.CONDITIONAL);
            this.expression = expression;
        }

        @Override
        protected String getArguments() {
            StringBuilder builder = getBaseArguments();
            builder.append(SPACE);
            builder.append(Type.CONDITIONAL.getArgumentSwitch());
            builder.append(expression);
            return builder.toString();
        }
    }

    public static class WatchBreakpointSetCommand extends ConditionalBreakpointSetCommand {
        WatchBreakpointSetCommand(int transactionId, String expression) {
            super(transactionId, expression);
            setType(Type.WATCH);
        }
    }
    
    
    public static class BreakpointSetResponse extends ResponseMessage {
        BreakpointSetResponse(Node node) {
            super(node);
        }

        public String getId() {
            return getAttribute(getNode(), ID);
        }

        public State getState() {
            String state = getAttribute(getNode(), STATE);
            return State.valueOf(state.toUpperCase());
        }
    }
    
    public static class BreakpointGetCommand extends BreakpointSetCommand {
        static final String ID_ARG = "-d ";                // NOI18N
        private String breakPointId;
        
        public BreakpointGetCommand(int transactionId, String breakPointId) {
            this(CommandMap.BREAKPOINT_GET.getCommand(), transactionId, breakPointId);
        }
        
        public BreakpointGetCommand(String command, int transactionId, String breakPointId) {
            super(command, transactionId);
            this.breakPointId = breakPointId;
        }        
        
        @Override
        protected String getArguments() {
            StringBuilder builder = new StringBuilder();
            builder.append(BreakpointUpdateCommand.ID_ARG);
            builder.append(getId());
            return builder.toString();
        }
        
        public String getId() {
            return breakPointId;
        }        
    }
    
    public static class BreakpointGetResponse extends BreakpointSetResponse {
        protected static final String BREAKPOINT    = "breakpoint";     // NOI18N
        
        BreakpointGetResponse(Node node) {
            super(node);
        }
        
        public Breakpoint getBreakpoint() {
            return new Breakpoint(getChild(getNode(), BREAKPOINT));
        }
    }
    
    public static class BreakpointListCommand extends BreakpointSetCommand {
        public BreakpointListCommand(int transactionId) {
            super(CommandMap.BREAKPOINT_LIST.getCommand(), transactionId);
        }
    }
    
    public static class BreakpointListResponse extends BreakpointGetResponse {
        BreakpointListResponse(Node node) {
            super(node);
        }
        
        public List<Breakpoint> getBreakpoints() {
            List<Breakpoint> bps = new LinkedList<Breakpoint>();
            for (Node node : getChildren(getNode(), BREAKPOINT)) {
                bps.add(new Breakpoint(node));
            }
            return bps;
        }
    }    
    
    public static class BreakpointUpdateCommand extends BreakpointGetCommand {
        public BreakpointUpdateCommand(int transactionId, String breakPointId) {
            super(CommandMap.BREAKPOINT_UPDATE.getCommand(), transactionId, breakPointId);            
        }

        @Override
        protected String getArguments() {
            //isTemporary and file URI cannot be updated, therefore unset them
            //not to include in the command
            setTemporary(false);
            setFileURI(null);
            setType(null);
            StringBuilder builder = getBaseArguments();
            return super.getArguments() + " " + builder.toString();
        }
    }

    public static class BreakpointUpdateResponse extends ResponseMessage {
        BreakpointUpdateResponse(Node node) {
            super(node);
        }
    }
    
    public static class BreakpointRemoveCommand extends BreakpointGetCommand {
        public BreakpointRemoveCommand(int transactionId, String breakpointId) {
            super(CommandMap.BREAKPOINT_REMOVE.getCommand(), transactionId, breakpointId);
        }
    }
    
    public static class BreakpointRemoveResponse extends ResponseMessage {
        BreakpointRemoveResponse(Node node) {
            super(node);
        }
    }
}
