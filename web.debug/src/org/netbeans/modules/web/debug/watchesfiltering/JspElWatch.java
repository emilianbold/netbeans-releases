/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.debug.watchesfiltering;

import org.netbeans.api.debugger.Watch;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.openide.util.WeakListeners;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

/**
 * Represents a JSP EL watch.
 * 
 * @author Maros Sandor
 */
public class JspElWatch implements PropertyChangeListener {
    
    private final Watch     watch;
    
    private boolean         evaluated;
    private JPDADebugger    debugger;
    private Variable        variable;
    private Exception       exception;

    public JspElWatch(Watch w, JPDADebugger debugger) {
        watch = w;
        this.debugger = debugger;
        w.addPropertyChangeListener((PropertyChangeListener) WeakListeners.create(PropertyChangeListener.class, this, w));    
    }

    public String getExpression () {
        return watch.getExpression();
    }

    public String getType() {
        if (!evaluated) evaluate();
        return variable == null ? "" : variable.getType();
    }

    public String getValue() {
        if (!evaluated) evaluate();
        return variable == null ? "" : variable.getValue();
    }

    public String getExceptionDescription() {
        if (!evaluated) evaluate();
        return exception == null ? null : exception.toString();
    }

    public String getToStringValue() throws InvalidExpressionException {
        return getValue().toString();
    }

    public Watch getWatch() {
        return watch;
    }
    
    private synchronized void evaluate() {
        if (evaluated) return;
        String text = watch.getExpression ();
        text = org.openide.util.Utilities.replaceString(text, "\"", "\\\"");
        text = "pageContext.getExpressionEvaluator().evaluate(\"" + text +
                            "\", java.lang.String.class, (javax.servlet.jsp.PageContext)pageContext, null)";
        try {
            variable = debugger.evaluate(text);
        } catch (Exception e) {
            exception = e;
        } finally {
            evaluated = true;
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        evaluated = false;
    }
}
