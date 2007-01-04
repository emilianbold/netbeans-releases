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

package org.netbeans.modules.cnd.apt.impl.support;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;
import org.netbeans.modules.cnd.apt.support.APTIncludeHandler;
import org.netbeans.modules.cnd.apt.support.APTIncludeResolver;
import org.netbeans.modules.cnd.apt.utils.APTUtils;

/**
 * implementation of include handler responsible for preventing recursive inclusion
 * @author Vladimir Voskresensky
 */
public class APTIncludeHandlerImpl implements APTIncludeHandler {
    private List/*<String>*/ systemIncludePaths;
    private List/*<String>*/ userIncludePaths;    
    
    private Map/*<String, Integer>*/ recurseIncludes = new HashMap();
    private static final int MAX_INCLUDE_DEEP = 5;    
    private Stack/*String*/ inclStack = new Stack();
    
    public APTIncludeHandlerImpl() {
        this(new ArrayList(), new ArrayList());
    }
    
    public APTIncludeHandlerImpl(List/*<String>*/ systemIncludePaths,
                                    List/*<String>*/ userIncludePaths) {
        this.systemIncludePaths = systemIncludePaths;
        this.userIncludePaths = userIncludePaths;        
    }

    public boolean pushInclude(String path) {
        return pushIncludeImpl(path);
    }

    public String popInclude() {
        return popIncludeImpl();
    }
    
    public APTIncludeResolver getResolver(String path) {
        return new APTIncludeResolverImpl(path, systemIncludePaths, userIncludePaths);
    }
    
    public String getCurPath() {
        return (String) inclStack.peek();
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
    
    protected static class StateImpl implements State {
        // for now just remember lists
        private List/*<String>*/ systemIncludePaths;
        private List/*<String>*/ userIncludePaths;   
        
        private Map/*<String, Integer>*/ recurseIncludes = new HashMap();   
        private Stack/*String*/ inclStack = new Stack();        
        
        void initFrom(APTIncludeHandlerImpl handler) {
            this.systemIncludePaths = handler.systemIncludePaths;
            this.userIncludePaths = handler.userIncludePaths;
            
            this.recurseIncludes.putAll(handler.recurseIncludes);
            this.inclStack.addAll(handler.inclStack);
        }
        
        void restoreTo(APTIncludeHandlerImpl handler) {
            handler.userIncludePaths = this.userIncludePaths;
            handler.systemIncludePaths = this.systemIncludePaths;
            
            handler.recurseIncludes.clear();
            handler.recurseIncludes.putAll(this.recurseIncludes);
            handler.inclStack.clear();
            handler.inclStack.addAll(this.inclStack);
        }

        public String toString() {
            return APTIncludeHandlerImpl.toString(systemIncludePaths, userIncludePaths, recurseIncludes, inclStack);
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // implementation details

    private boolean pushIncludeImpl(String path) {
        Integer counter = (Integer) recurseIncludes.get(path);
        counter = (counter == null) ? new Integer(1) : new Integer(counter.intValue()+1);
        if (counter.intValue() < MAX_INCLUDE_DEEP) {
            recurseIncludes.put(path, counter);
            inclStack.push(path);
            return true;
        } else {
            assert (recurseIncludes.get(path) != null) : "included file must be in map";
            APTUtils.LOG.log(Level.WARNING, "RECURSIVE inclusion:\n\t{0}\n\tin {1}\n", new Object[] { path , getCurPath() });
            return false;
        }
    }    
    
    private String popIncludeImpl() {        
        assert (!inclStack.isEmpty());
        String path = (String) inclStack.pop();
        Integer counter = (Integer)recurseIncludes.remove(path);
        assert (counter != null) : "must be added before";
        // decrease include counter
        counter = new Integer(counter.intValue()-1);
        assert (counter.intValue() >= 0) : "can't be negative";
        if (counter.intValue() != 0) {
            recurseIncludes.put(path, counter);
        }
        return path;
    }
    
    public String toString() {
        return APTIncludeHandlerImpl.toString(systemIncludePaths, userIncludePaths, recurseIncludes, inclStack);
    }    
    
    private static String toString(List/*<String>*/ systemIncludePaths,
                                    List/*<String>*/ userIncludePaths,
                                    Map/*<String, Integer>*/ recurseIncludes,
                                    Stack/*String*/ inclStack) {
        StringBuffer retValue = new StringBuffer();
        retValue.append("User includes:\n");
        retValue.append(APTUtils.includes2String(userIncludePaths));
        retValue.append("\nSys includes:\n");
        retValue.append(APTUtils.includes2String(systemIncludePaths));
        return retValue.toString();        
    }
}
