/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.debugger.jpda.models;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.InvalidStackFrameException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.StackFrame;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.This;
import org.netbeans.spi.viewmodel.NoInformationException;


/**
* Class representating one line of callstack.
*/
public class CallStackFrameImpl implements CallStackFrame {
    
    private StackFrame sf;
    private CallStackTreeModel ctm;
    private String id;
    
    
    public CallStackFrameImpl (
        StackFrame sf, 
        CallStackTreeModel ctm,
        String id
    ) {
        this.sf = sf;
        this.ctm = ctm;
        this.id = id;
    }
    
    
    // public interface ........................................................
        
    /**
    * Returns line number of this frame in this callstack.
    *
    * @return Returns line number of this frame in this callstack.
    */
    public int getLineNumber (String struts) {
        try {
            return sf.location ().lineNumber (struts);
        } catch (InvalidStackFrameException ex) {
//            ex.printStackTrace ();
        }
        return 0;
    }

    /**
    * Returns method name of this frame in this callstack.
    *
    * @return Returns method name of this frame in this callstack.
    */
    public String getMethodName () {
        try {
            return sf.location ().method ().name ();
        } catch (InvalidStackFrameException ex) {
//            ex.printStackTrace ();
        }
        return "";
    }

    /**
    * Returns class name of this frame in this callstack.
    *
    * @return class name of this frame in this callstack
    */
    public String getClassName () {
        try {
            return sf.location ().declaringType ().name ();
        } catch (InvalidStackFrameException ex) {
//            ex.printStackTrace ();
        }
        return "";
    }

    /**
    * Returns name of default stratumn.
    *
    * @return name of default stratumn
    */
    public String getDefaultStratum () {
        try {
            return sf.location ().declaringType ().defaultStratum ();
        } catch (InvalidStackFrameException ex) {
        }
        return "";
    }

    /**
    * Returns name of default stratumn.
    *
    * @return name of default stratumn
    */
    public List getAvailableStrata () {
        try {
            return sf.location ().declaringType ().availableStrata ();
        } catch (InvalidStackFrameException ex) {
        }
        return new ArrayList ();
    }

    /**
    * Returns name of file of this frame.
    *
    * @return name of file of this frame
    * @throws DebuggerException if informations about source are not included or some other error
    *   occurres.
    */
    public String getSourceName (String stratum) throws NoInformationException {
        try {
            return sf.location ().sourceName (stratum);
        } catch (InvalidStackFrameException ex) {
//            ex.printStackTrace ();
        } catch (AbsentInformationException ex) {
            throw new NoInformationException (ex.getMessage ());
        }
        return "";
    }
    
    /**
     * Returns source path of file this frame is stopped in or null.
     *
     * @return source path of file this frame is stopped in or null
     */
    public String getSourcePath (String stratum) 
    throws NoInformationException {
        try {
            return sf.location ().sourcePath (stratum);
        } catch (InvalidStackFrameException ex) {
//            ex.printStackTrace ();
        } catch (AbsentInformationException ex) {
            throw new NoInformationException (ex.getMessage ());
        }
        return "";
    }
    
    public void makeCurrent () {
        ctm.getDebugger ().setCurrentCallStackFrame (this);
    }
    
    public org.netbeans.api.debugger.jpda.LocalVariable[] getLocalVariables () 
    throws NoInformationException {
        LocalsTreeModel ltm = ctm.getLocalsTreeModel ();
        AbstractVariable vs[] = ltm.getLocalVariables (sf, false);
        org.netbeans.api.debugger.jpda.LocalVariable[] var = new
            org.netbeans.api.debugger.jpda.LocalVariable [vs.length];
        System.arraycopy (vs, 0, var, 0, vs.length);
        return var;
    }
    
    public This getThisVariable () {
        ObjectReference thisR = sf.thisObject ();
        if (thisR == null) return null;
        LocalsTreeModel ltm = ctm.getLocalsTreeModel ();
        return ltm.getThis (thisR, "");
    }
    
    
    // other methods............................................................
        
    void setStackFrame (StackFrame sf) {
        this.sf = sf;
    }
    
    StackFrame getStackFrame () {
        return sf;
    }

    public boolean equals (Object o) {
        return  (o instanceof CallStackFrameImpl) &&
                (id.equals (((CallStackFrameImpl) o).id));
    }
    
    public int hashCode () {
        return id.hashCode ();
    }
}

