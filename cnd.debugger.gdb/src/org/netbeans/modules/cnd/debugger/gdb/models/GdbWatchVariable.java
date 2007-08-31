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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.debugger.gdb.models;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.api.debugger.Watch;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;
import org.netbeans.modules.cnd.debugger.gdb.GdbVariable;

/**
 * The variable type used in Gdb watches.
 *
 * Model:
 *   When created, see if there is an in-scope local variable in the debugger
 *   (since most Watches are made on in-scope variables). If so, use initialize type
 *   and value from that variable. If not, request type and value from gdb. We assume
 *   type never changes, so the we never update type information
 *
 *   At each stop, every GdbWatchVariable is invalidated. If the Watches view is visible,
 *   getValue() will be called. It will send a request to gdb and block until the request
 *   is completed (since we always read gdb responses on the GDB Reader thread we're
 *   guaranteed not to deadlock). The GdbDebugger responds to the response from gdb and
 *   calls setValue() (still on the GDB Reader thread). The setValue calls tells the blocked
 *   getValue() to continue and the updated value is displayed.
 *
 * @author gordonp
 */
public class GdbWatchVariable extends AbstractVariable implements PropertyChangeListener {
    
    private Watch watch;
    private GdbWatchVariable evaluatedWatch;
    private WatchesTreeModel model;
    private StringBuilder typeBuf = new StringBuilder();
    private boolean[] invalidType = new boolean[] { false };
    private boolean[] invalidValue = new boolean[] { false };
    
    /** Creates a new instance of GdbWatchVariable */
    public GdbWatchVariable(WatchesTreeModel model, Watch watch) {
        super(watch.getExpression());
        this.model = model;
        this.watch = watch;
        
        GdbVariable var;
        if (expressionIsSimpleVariable() && (var = findSimpleVariable()) != null) {
            type = var.getType();
            value = var.getValue();
            if (type != null && value != null) {
                expandChildrenFromValue(this);
            }
        }
        if (type == null && getDebugger().getState() == GdbDebugger.STATE_STOPPED) {
            setTypeInvalid();
            setValueInvalid();
        }
        getDebugger().addPropertyChangeListener(this);
        watch.addPropertyChangeListener(this);
    }
    
    public void remove() {
        watch.remove();
    }
    
    private void setValueInvalid() {
        synchronized (invalidValue) {
            invalidValue[0] = true;
            getDebugger().requestWatchValue(this);
        }
    }
    
    private void setTypeInvalid() {
        synchronized (invalidType) {
            invalidType[0] = true;
            getDebugger().requestWatchType(this);
        }
    }
    
    public void appendTypeBuf(String tline) {
        typeBuf.append(tline);
    }
    
    public String getTypeBuf() {
        return typeBuf.toString();
    }
    
    public void propertyChange(PropertyChangeEvent ev) {
        if (ev.getPropertyName() == GdbDebugger.PROP_STATE && ev.getNewValue() == GdbDebugger.STATE_STOPPED) {
            setValueInvalid();
        } else if (ev.getPropertyName() == Watch.PROP_EXPRESSION) {
            setTypeInvalid();
            setValueInvalid();
        } else if (ev.getPropertyName() == Watch.PROP_VALUE) {
            System.err.println("");
        }
    }
    
    public String getName() {
        return watch.getExpression();
    }
    
    public String getType() {
        if (type == null) {
            synchronized (invalidType) {
                if (invalidType[0]) {
                    try {
                        invalidType.wait(10000);
                        expandChildrenFromValue(this);
                    } catch (InterruptedException ex) {
                        return "";
                    }
                } else {
                    invalidType[0] = true;
                    getDebugger().requestWatchType(this);
                }
            }
        }
        return type;
    }
    
    public void setType(String type) {
        synchronized (invalidType) {
            this.type = type;
            if (invalidType[0]) {
                invalidType.notifyAll();
                invalidType[0] = false;
            }
        }
    }
    
    public String getValue() {
        if (value == null) {
            synchronized (invalidValue) {
                if (invalidValue[0]) {
                    try {
                        invalidValue.wait(10000);
                    } catch (InterruptedException ex) {
                        invalidValue[0] = false;
                        return "";
                    }
                } else {
                    invalidValue[0] = true;
                    getDebugger().requestWatchValue(this);
                }
            }
        }
        return value;
    }
    
    public void setValue(String value) {
        synchronized (invalidValue) {
            this.value = value;
            if (invalidValue[0]) {
                invalidValue.notifyAll();
                invalidValue[0] = false;
            }
        }
    }
    
    public void setValueAt(String value) {
        super.setValue(value);
    }
    
    public void setError(String msg) {
        msg = msg.replace("\\\"", "\"");
        if (msg.charAt(msg.length() - 1) == '.') {
            msg = msg.substring(0, msg.length() - 1);
        }
        setValue('>' + msg + '<');
    }
    
    private boolean expressionIsSimpleVariable() {
        String expression = watch.getExpression();
        
        char ch = expression.charAt(0);
        if (Character.isLetter(ch) || ch == '_') {
            for (int i = 1; i < expression.length(); i++) {
                ch = expression.charAt(i);
                if (!Character.isLetterOrDigit(ch) && ch != '_') {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }
    
    private GdbVariable findSimpleVariable() {
        String expression = watch.getExpression();
        
        for (GdbVariable var : getDebugger().getLocalVariables()) {
            if (expression.equals(var.getName())) {
                return var;
            }
        }
        return null;
    }
    
    public String getExpression() {
        return watch.getExpression();
    }
    
    public void setExpression(String expression) {
        watch.setExpression(expression);
    }
        
//    public void setEvaluated(GdbWatchVariable evaluatedWatch) {
//        this.evaluatedWatch = evaluatedWatch;
//        if (evaluatedWatch != null) {
//
//        }
//    }
}
