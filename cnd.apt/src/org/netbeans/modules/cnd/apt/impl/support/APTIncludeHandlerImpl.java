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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.apt.impl.support;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;
import org.netbeans.modules.cnd.apt.support.APTIncludeHandler;
import org.netbeans.modules.cnd.apt.support.APTIncludeHandler.IncludeInfo;
import org.netbeans.modules.cnd.apt.support.APTIncludeResolver;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.apt.utils.FilePathCache;

/**
 * implementation of include handler responsible for preventing recursive inclusion
 * @author Vladimir Voskresensky
 */
public class APTIncludeHandlerImpl implements APTIncludeHandler {
    private List/*<String>*/ systemIncludePaths;
    private List/*<String>*/ userIncludePaths;    
    
    private Map/*<String, Integer>*/ recurseIncludes = null;
    private static final int MAX_INCLUDE_DEEP = 5;    
    private Stack/*IncludeInfoImpl*/ inclStack = null;
    private String startFile;
    
    public APTIncludeHandlerImpl(String startFile) {
        this(startFile, new ArrayList(), new ArrayList());
    }
    
    public APTIncludeHandlerImpl(String startFile, 
                                    List/*<String>*/ systemIncludePaths,
                                    List/*<String>*/ userIncludePaths) {
        this.startFile = FilePathCache.getString(startFile);
        this.systemIncludePaths = systemIncludePaths;
        this.userIncludePaths = userIncludePaths;        
    }

    public boolean pushInclude(String path, int directiveLine) {
        return pushIncludeImpl(path, directiveLine);
    }

    public String popInclude() {
        return popIncludeImpl();
    }
    
    public APTIncludeResolver getResolver(String path) {
        return new APTIncludeResolverImpl(path, systemIncludePaths, userIncludePaths);
    }
    
    public String getStartFile() {
        return startFile;
    }
    
    public String getCurPath() {
        assert (inclStack != null);
        IncludeInfo info = (IncludeInfo) inclStack.peek();
        return (String) info.getIncludedPath();
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // manage state (save/restore)
    
    public State getState() {
        StateImpl state = createStateImpl();
        state.initFrom(this);
        return state;
    }
    
    public void setState(State state) {
        if (state instanceof StateImpl) {
            ((StateImpl)state).restoreTo(this);
        }
    }
    
    protected StateImpl createStateImpl() {
        return new StateImpl();
    }
    
    public final static class StateImpl implements State {
        // for now just remember lists
        private List/*<String>*/ systemIncludePaths = null;
        private List/*<String>*/ userIncludePaths = null;    
        private String           startFile = null;
        
        private Map/*<String, Integer>*/ recurseIncludes = null;   
        private Stack<IncludeInfo> inclStack = null;        
        
        protected StateImpl() {
        }
        
        private StateImpl(StateImpl other) {
            // shared information
            this.startFile = other.startFile;
            this.systemIncludePaths = other.systemIncludePaths;
            this.userIncludePaths = other.userIncludePaths;
            
            // own copy of include information
            if (other.inclStack != null) {
                this.inclStack = new Stack();
                this.inclStack.addAll(other.inclStack);
            }
            if (other.recurseIncludes != null) {
                this.recurseIncludes = new HashMap();
                this.recurseIncludes.putAll(other.recurseIncludes);
            }
        }
        
        void initFrom(APTIncludeHandlerImpl handler) {
            this.systemIncludePaths = handler.systemIncludePaths;
            this.userIncludePaths = handler.userIncludePaths;
            this.startFile = handler.startFile;
            
            if (handler.recurseIncludes != null && !handler.recurseIncludes.isEmpty()) {
                this.recurseIncludes = new HashMap();
                this.recurseIncludes.putAll(handler.recurseIncludes);
            }
            if (handler.inclStack != null && !handler.inclStack.empty()) {
                this.inclStack = new Stack();
                this.inclStack.addAll(handler.inclStack);
            }
        }
        
        void restoreTo(APTIncludeHandlerImpl handler) {
            handler.userIncludePaths = this.userIncludePaths;
            handler.systemIncludePaths = this.systemIncludePaths;
            handler.startFile = this.startFile;
            
            if (this.recurseIncludes != null) {
                handler.recurseIncludes = new HashMap();
                handler.recurseIncludes.putAll(this.recurseIncludes);
            }
            if (this.inclStack != null) {
                handler.inclStack = new Stack();
                handler.inclStack.addAll(this.inclStack);
            }
        }

        public String toString() {
            return APTIncludeHandlerImpl.toString(startFile, systemIncludePaths, userIncludePaths, recurseIncludes, inclStack);
        }

        public boolean cleanExceptIncludeStack() {
            boolean cleaned = false;
            if (inclStack != null && inclStack.size() != 0) {
                cleaned = true;
            }
            recurseIncludes = null;
            return cleaned;
        }

        public Stack/*<IncludeInfo>*/ cleanIncludeStack() {
            Stack out = this.inclStack;
            this.inclStack = null;
            return out;
        }        

        public void write(DataOutput output) {
            throw new UnsupportedOperationException("Not yet implemented"); // NOI18N
        }
        
        public StateImpl(DataInput input) throws IOException {
            throw new UnsupportedOperationException("Not yet implemented"); // NOI18N
        }        

        public boolean equals(Object obj) {
            if (obj == null || (obj.getClass() != this.getClass())) {
                return false;
            }
            StateImpl other = (StateImpl)obj;
            return this.startFile.equals(other.startFile) &&
                    compareStacks(this.inclStack, other.inclStack);
        }
        
        private boolean compareStacks(Stack<IncludeInfo> inclStack1, Stack<IncludeInfo> inclStack2) {
            if (inclStack1 != null) {
                int size = inclStack1.size();
                if (inclStack2 == null || inclStack2.size() != size) {
                    return false;
                }
                for (int i = 0; i < size; i++) {
                    IncludeInfo cur1 = inclStack1.get(i);
                    IncludeInfo cur2 = inclStack2.get(i);
                    if (!cur1.equals(cur2)) {
                        return false;
                    }
                }
                return true;
            } else {
                return inclStack2 == null;
            }
        }

        
        public APTIncludeHandler.State copy() {
            return new StateImpl(this);
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // implementation details

    private boolean pushIncludeImpl(String path, int directiveLine) {
        if (recurseIncludes == null) {
            assert (inclStack == null): inclStack.toString() + " started on " + startFile;
            inclStack = new Stack();
            recurseIncludes = new HashMap();
        }
        Integer counter = (Integer) recurseIncludes.get(path);
        counter = (counter == null) ? new Integer(1) : new Integer(counter.intValue()+1);
        if (counter.intValue() < MAX_INCLUDE_DEEP) {
            recurseIncludes.put(path, counter);
            inclStack.push(new IncludeInfoImpl(path, directiveLine));
            return true;
        } else {
            assert (recurseIncludes.get(path) != null) : "included file must be in map"; // NOI18N
            APTUtils.LOG.log(Level.WARNING, "RECURSIVE inclusion:\n\t{0}\n\tin {1}\n", new Object[] { path , getCurPath() }); // NOI18N
            return false;
        }
    }    
    
    private static final class IncludeInfoImpl implements IncludeInfo {
        private final String path;
        private final int directiveLine;
        public IncludeInfoImpl(String path, int directiveLine) {
            assert path != null;
            this.path = path;
            assert directiveLine >= 0;
            this.directiveLine = directiveLine;
        }

        public String getIncludedPath() {
            return path;
        }

        public int getIncludeDirectiveLine() {
            return directiveLine;
        }

        public String toString() {
            String retValue;
            
            retValue = "(" + getIncludeDirectiveLine() + ": " + getIncludedPath() + ")"; // NOI18N
            return retValue;
        }

        public boolean equals(Object obj) {
            if (obj == null || (obj.getClass() != this.getClass())) {
                return false;
            }
            IncludeInfoImpl other = (IncludeInfoImpl)obj;
            return this.directiveLine == other.directiveLine &&
                    this.path.equals(other.path);
        }
    }
      
    private String popIncludeImpl() {        
        assert (inclStack != null);
        assert (!inclStack.isEmpty());
        assert (recurseIncludes != null);
        IncludeInfo inclInfo = (IncludeInfo) inclStack.pop();
        String path = inclInfo.getIncludedPath();
        Integer counter = (Integer)recurseIncludes.remove(path);
        assert (counter != null) : "must be added before"; // NOI18N
        // decrease include counter
        counter = new Integer(counter.intValue()-1);
        assert (counter.intValue() >= 0) : "can't be negative"; // NOI18N
        if (counter.intValue() != 0) {
            recurseIncludes.put(path, counter);
        }
        return path;
    }
    
    public String toString() {
        return APTIncludeHandlerImpl.toString(startFile, systemIncludePaths, userIncludePaths, recurseIncludes, inclStack);
    }    
    
    private static String toString(String startFile, 
                                    List/*<String>*/ systemIncludePaths,
                                    List/*<String>*/ userIncludePaths,
                                    Map/*<String, Integer>*/ recurseIncludes,
                                    Stack/*IncludeInfo*/ inclStack) {
        StringBuilder retValue = new StringBuilder();
        retValue.append("User includes:\n"); // NOI18N
        retValue.append(APTUtils.includes2String(userIncludePaths));
        retValue.append("\nSys includes:\n"); // NOI18N
        retValue.append(APTUtils.includes2String(systemIncludePaths));
        retValue.append("\nInclude Stack starting from:\n"); // NOI18N
        retValue.append(startFile).append("\n"); // NOI18N
        retValue.append(includesStack2String(inclStack));
        return retValue.toString();        
    }

    private static String includesStack2String(Stack/*IncludeInfo*/ inclStack) {
        StringBuilder retValue = new StringBuilder();
        if (inclStack == null) {
            retValue.append("<not from #include>"); // NOI18N
        } else {
            for (Iterator it = inclStack.iterator(); it.hasNext();) {
                IncludeInfo info = (IncludeInfo) it.next();
                retValue.append(info);
                if (it.hasNext()) {
                    retValue.append("->\n"); // NOI18N
                }
            }
        }
        return retValue.toString();
    }
}
