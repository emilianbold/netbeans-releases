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
    
    private boolean         evaluated = false;
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
        if (!evaluated) {
            evaluate();
        }
        return variable == null ? "" : variable.getType();
    }

    public String getValue() {
        if (!evaluated) {
            evaluate();
        }
        return variable == null ? "" : variable.getValue();
    }

    public String getExceptionDescription() {
        if (!evaluated) {
            evaluate();
        }
        return exception == null ? null : exception.getMessage();
    }

    public String getToStringValue() throws InvalidExpressionException {
        return getValue().toString();
    }

    public Watch getWatch() {
        return watch;
    }
    
    private synchronized void evaluate() {
        String text = watch.getExpression ();
        text = org.openide.util.Utilities.replaceString(text, "\"", "\\\"");
        text = "pageContext.getExpressionEvaluator().evaluate(\"" + text +
                            "\", java.lang.String.class, (javax.servlet.jsp.PageContext)pageContext, null)";
        try {
            variable = debugger.evaluate(text);
            exception = null;
        } catch (Exception e) {
            exception = e;
        } finally {
            evaluated = true;
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        setUnevaluated();
    }
    
    public void setUnevaluated() {
        evaluated = false;
    }
}
