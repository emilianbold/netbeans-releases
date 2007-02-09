/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
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
package org.netbeans.api.debugger.jpda;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import com.sun.jdi.request.StepRequest;

/**
 * Represents one JPDA step.
 *
 * @author Roman Ondruska
 */
public abstract class JPDAStep {
    private int size;
    private int depth;
    private boolean hidden;
    /** Associated JPDA debugger */
    protected JPDADebugger debugger;
    private PropertyChangeSupport pcs;

    /** Step into any newly pushed frames */
    public static final int STEP_INTO   =   StepRequest.STEP_INTO;
    /** Step over any newly pushed frames */
    public static final int STEP_OVER   =   StepRequest.STEP_OVER;
    /** Step out of the current frame */
    public static final int STEP_OUT    =   StepRequest.STEP_OUT;
    /** Step to the next location on a different line */
    public static final int STEP_LINE   =   StepRequest.STEP_LINE;
    /** Step to the next available operation */
    public static final int STEP_OPERATION = 10;
    /** Step to the next available location */
    public static final int STEP_MIN    =   StepRequest.STEP_MIN;
    /** Property fired when the step is executed */
    public static final String PROP_STATE_EXEC = "exec";
    
    /** Constructs a JPDAStep for given {@link org.netbeans.api.debugger.jpda.JPDADebugger}, 
     *  size {@link #STEP_LINE}, {@link #STEP_MIN} 
     *  and depth {@link #STEP_OUT}, {@link #STEP_INTO}.
     * 
     *  @param debugger an associated JPDADebuger
     *  @param size step size
     *  @param depth step depth
     */
    public JPDAStep(JPDADebugger debugger, int size, int depth) {
        this.size = size;
        this.depth = depth;
        this.debugger = debugger;
        this.hidden = false;
        pcs = new PropertyChangeSupport(this);
    }
   
    /** Sets the hidden property. */
    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }
    
    /**
     * Returns hidden property of the step.
     *
     * @return hidden property 
     */
    public boolean getHidden() {
        return hidden;
    }
    
    /** Sets size of the step.
     *  @param size step size
     */
    public void setSize(int size) {
        this.size = size;
    }
    
    /**
     * Returns size of the step.
     *
     * @return step size
     */
    public int getSize() {
        return size;
    }
    
    /** Sets depth of the step.
     *  @param depth step depth
     */
    public void setDepth(int depth) {
        this.depth = depth;
    }
    
    /**
     * Returns depth of the step.
     *
     * @return step depth
     */
    public int getDepth() {
        return depth;
    }
    
    /** Adds the step request to the associated
     *  {@link org.netbeans.api.debugger.jpda.JPDADebugger}.
     *  Method is not synchronized.
     *
     *  @param tr associated thread
     */
    public abstract void addStep(JPDAThread tr);
    
    /**
    * Adds property change listener.
    *
    * @param l new listener.
    */
    public void addPropertyChangeListener (PropertyChangeListener l) {
        pcs.addPropertyChangeListener (l);
    }

    /**
    * Removes property change listener.
    *
    * @param l removed listener.
    */
    public void removePropertyChangeListener (PropertyChangeListener l) {
        pcs.removePropertyChangeListener (l);
    }

    /**
    * Adds property change listener.
    *
    * @param l new listener.
    */
    public void addPropertyChangeListener (String propertyName, PropertyChangeListener l) {
        pcs.addPropertyChangeListener (propertyName, l);
    }

    /**
    * Removes property change listener.
    *
    * @param l removed listener.
    */
    public void removePropertyChangeListener (String propertyName, PropertyChangeListener l) {
        pcs.removePropertyChangeListener (propertyName, l);
    }
    
    /**
    * Fires property change.
    */
    protected void firePropertyChange (String name, Object o, Object n) {
        pcs.firePropertyChange (name, o, n);
    }
    
}
