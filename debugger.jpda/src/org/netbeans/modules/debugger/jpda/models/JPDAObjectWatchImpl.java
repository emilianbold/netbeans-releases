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

import com.sun.jdi.ObjectReference;
import com.sun.jdi.Value;

import org.netbeans.api.debugger.Watch;
import org.netbeans.api.debugger.jpda.JPDAWatch;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;


/**
 * Represents watch in JPDA debugger.
 *
 * @author   Jan Jancura
 */

public class JPDAObjectWatchImpl extends AbstractVariable implements JPDAWatch,
ObjectVariable {

    private WatchesModel        model;
    private Watch               watch;
    private String              exceptionDescription;
    
    
    JPDAObjectWatchImpl (WatchesModel model, Watch watch, ObjectReference v) {
        super (
            model.getLocalsTreeModel (), 
            v, 
            "" + watch +
                (v instanceof ObjectReference ? "^" : "")
        );
        this.model = model;
        this.watch = watch;
    }
    
    JPDAObjectWatchImpl (WatchesModel model, Watch watch, String exceptionDescription) {
        super (
            model.getLocalsTreeModel (), 
            null, 
            "" + watch
        );
        this.model = model;
        this.watch = watch;
        this.exceptionDescription = exceptionDescription;
    }
    
    /**
     * Watched expression.
     *
     * @return watched expression
     */
    public String getExpression () {
        return watch.getExpression ();
    }

    /**
     * Sets watched expression.
     *
     * @param expression a expression to be watched
     */
    public void setExpression (String expression) {
        watch.setExpression (expression);
    }
    
    /**
     * Remove the watch from the list of all watches in the system.
     */
    public void remove () {
        watch.remove ();
    }
    
    /**
     * Returns description of problem is this watch can not be evaluated
     * in current context.
     *
     * @return description of problem
     */
    public String getExceptionDescription () {
        return exceptionDescription;
    }
    
    void setInnerValue (Value v) {
        super.setInnerValue (v);
        exceptionDescription = null;
    }
    
    void setException (String exceptionDescription) {
        setInnerValue (null);
        this.exceptionDescription = exceptionDescription;
    }
    
    boolean isPrimitive () {
        return !(getInnerValue () instanceof ObjectReference);
    }
}

