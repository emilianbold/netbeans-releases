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

package org.netbeans.modules.cnd.debugger.gdb;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.debugger.gdb.models.AbstractVariable;

/**
 * Represents one stack frame.
 *
 * <pre style="background-color: rgb(255, 255, 102);">
 * Since JDI interfaces evolve from one version to another, it's strongly recommended
 * not to implement this interface in client code. New methods can be added to
 * this interface at any time to keep up with the JDI functionality.</pre>
 *
 * @author Gordon Prieur (copied from Jan Jancura's JPDA implementation)
 */
public class CallStackFrame {
    
    public static final int OBSOLETE = 1;
    public static final int VALID = 2;
    
    private CallStackFrame sf;
    private GdbDebugger debugger;
    private int lineNumber;
    private String func;
    private String file;
    private String fullname;
    private int frameNumber;
    private String address;
    private int state;
    private LocalVariable[] cachedLocalVariables;
    private Map<String, Object> typeMap = new HashMap();
    private Logger log = Logger.getLogger("gdb.logger"); // NOI18N
    
    public CallStackFrame(GdbDebugger debugger, String func, String file, String fullname, String lnum, String address) {
        this.debugger = debugger;
        set(func, file, fullname, lnum, address);
        frameNumber = 0;
    }
    
    /**
     *  Set frame values.
     *
     *  @param func Function name from gdb
     *  @param file File name (basename) from gdb
     *  @param fullname Absolute path from gdb
     *  @param lnum Line number (as a String) from gdb
     */
    public void set(String func, String file, String fullname, String lnum, String address) {
        if (this.fullname != null && !this.fullname.equals(fullname)) {
            typeMap.clear();
        }
        this.func = func;
        this.file = file;
        this.fullname = fullname;
        this.address = address;
        if (lnum != null) {
            try {
                lineNumber = Integer.parseInt(lnum);
            } catch (NumberFormatException ex) {
                lineNumber = 1; // shouldn't happen
            }
        } else {
            lineNumber = -1;
        }
        invalidateCache();
        setState(CallStackFrame.VALID);
    }
    
    public void invalidateCache() {
        cachedLocalVariables = null;
    }
    
    public void addType(String key, Object o) {
        if (!(o instanceof Map && ((Map)o).isEmpty())) {
            typeMap.put(key, o);
        }
    }
    
    /**
     * When a type is added without a value, its a placeholder telling us that a gdb request
     * has been made and that we shouldn't send another request to gdb.
     */
    public void addType(String key) {
        addType(key, "");
    }
    
    public Object getType(String key) {
        if (key != null) {
            Object o = typeMap.get(key);
            Object o2;
            while (o instanceof String && (o2 = typeMap.get(o)) != null) {
                o = o2;
            }
            if (o == null && key.startsWith("class ")) { // NOI18N
                return getType(key.substring(6));
            }
            if (o == null && (o = typeMap.get("class " + key)) != null) { // NOI18N
                return o;
            }
            return o;
        } else {
            return "";
        }
    }
    
    /**
     *  Set frame number.
     *
     *  @param frameNumber Frame number in Call Stack ("0" means top)
     */
    public void setFrameNumber(int frameNumber) {
        this.frameNumber = frameNumber;
    }
    
    /**
     *  Get frame number.
     *
     *  @return Frame nunmber in Call Stack ("0" means top)
     */
    public int getFrameNumber() {
        return frameNumber;
    }
    
    /**
     * Returns line number associated with this stack frame.
     *
     * @return line number associated with this this stack frame
     */
    public int getLineNumber() {
        return lineNumber;
    }
    
    /**
     * Set the linenumber after a step operation
     */
    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }
    
    /**
     * Returns method name associated with this stack frame.
     *
     * @return method name associated with this stack frame
     */
    public String getFunctionName() {
        return func;
    }
    
    /**
     * Returns name of file this stack frame is stopped in.
     *
     * @return name of file this stack frame is stopped in
     */
    public String getFileName() {
        return file;
    }
    
    /**
     * Returns name of file this stack frame is stopped in.
     *
     * @return name of file this stack frame is stopped in
     */
    public String getFullname() {
        return fullname;
    }
    
    /**
     * @return address this stack frame is stopped in
     */
    public String getAddr() {
        return address;
    }
    
    /** Sets this frame current */
    public void makeCurrent() {
        debugger.setCurrentCallStackFrame(this);
    }
    
    /** Set the state of this frame */
    public void setState(int state) {
        if (state != this.state && (state == OBSOLETE || state == VALID)) {
            this.state = state;
        }
    }
    
    /**
     * Returns <code>true</code> if this frame is obsoleted.
     *
     * @return <code>true</code> if this frame is obsoleted
     */
    public  boolean isObsolete() {
        return state == OBSOLETE;
    }
    
    /** UNCOMMENT WHEN THIS METHOD IS NEEDED. IT'S ALREADY IMPLEMENTED IN THE IMPL. CLASS.
     * Determine, if this stack frame can be poped off the stack.
     *
     * @return <code>true</code> if this frame can be poped
     *
     * public abstract boolean canPop();
     */
    
    /**
     * Pop stack frames. All frames up to and including the frame
     * are popped off the stack. The frame previous to the parameter
     * frame will become the current frame.
     */
    public void popFrame() {
        debugger.getGdbProxy().exec_finish();
    }
    
    /** Get stack frame */
    public CallStackFrame getStackFrame() {
        return sf;
    }
    
    /**
     * Returns local variables.
     * If local variables are not available returns empty array.
     *
     * @return local variables
     */
    public LocalVariable[] getLocalVariables() {
        if (cachedLocalVariables == null) {
            List<GdbVariable> list = debugger.getLocalVariables();
            int n = list.size();

            LocalVariable[] locals = new LocalVariable[n];
            for (int i = 0; i < n; i++) {
                locals[i] = new AbstractVariable(list.get(i));
            }
            cachedLocalVariables = locals;
            return locals;
        } else {
            return cachedLocalVariables;
        }
    }
}
 
