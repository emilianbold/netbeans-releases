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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.cnd.debugger.gdb.models;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.api.debugger.Watch;
import org.netbeans.modules.cnd.debugger.gdb.Field;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;

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
    private WatchesTreeModel model;
    private StringBuilder typeBuf = new StringBuilder();
    private boolean[] invalidType = new boolean[] { false };
    private boolean[] invalidValue = new boolean[] { false };
    
    /** Creates a new instance of GdbWatchVariable */
    public GdbWatchVariable(WatchesTreeModel model, Watch watch) {
        super(watch.getExpression());
        this.model = model;
        this.watch = watch;
        
        if (watch.getExpression().length() > 0) {
            if (getDebugger() != null) {
                setTypeInvalid();
                setValueInvalid();
                getDebugger().addPropertyChangeListener(this);
            }
            watch.addPropertyChangeListener(this);
        } else {
            type = "";
            value = "";
        }
    }
    
    public void remove() {
        watch.remove();
        getDebugger().removePropertyChangeListener(this);
    }
    
    public void clearTypeBuf() {
        typeBuf.delete(0, typeBuf.length());
    }
    
    public void appendTypeBuf(String tline) {
        typeBuf.append(tline);
    }
    
    public String getTypeBuf() {
        return typeBuf.toString();
    }
    
    public void propertyChange(PropertyChangeEvent ev) {
        if (ev.getPropertyName().equals(GdbDebugger.PROP_STATE) &&
                ev.getNewValue().equals(GdbDebugger.STATE_STOPPED)) {
            setTypeInvalid();
            setValueInvalid();
        } else if (ev.getPropertyName().equals(Watch.PROP_EXPRESSION)) {
            setTypeInvalid();
            setValueInvalid();
        }
    }
    
    @Override
    public String getName() {
        return watch.getExpression();
    }
    
    @Override
    public String getType() {
        if (type == null || type.length() == 0) {
            synchronized (invalidType) {
                if (invalidType[0]) {
                    try {
                        invalidType.wait(200);
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
    
    @Override
    public void setType(String type) {
        synchronized (invalidType) {
            this.type = type;
            if (invalidType[0]) {
                invalidType.notifyAll();
                invalidType[0] = false;
            }
        }
    }
    
    private void setTypeInvalid() {
        if (getDebugger() != null) {
            synchronized (invalidType) {
                invalidType[0] = true;
                getDebugger().requestWatchType(this);
            }
        }
    }
    
    public void setTypeToError(String msg) {
        msg = msg.replace("\\\"", "\""); // NOI18N
        if (msg.charAt(msg.length() - 1) == '.') {
            msg = msg.substring(0, msg.length() - 1);
        }
        setType('>' + msg + '<');
    }
    
    @Override
    public String getValue() {
        if (value == null) {
            synchronized (invalidValue) {
                if (invalidValue[0]) {
                    try {
                        invalidValue.wait(200);
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
    
    @Override
    public void setValue(String value) {
        synchronized (invalidValue) {
            this.value = value;
            if (invalidValue[0]) {
                invalidValue.notifyAll();
                invalidValue[0] = false;
            }
            if (fields.length > 0) {
                fields = new Field[0];
                expandChildren();
            }
        }
    }
    
    public void setValueAt(String value) {
        super.setValue(value);
        setValueInvalid();
    }
    
    private void setValueInvalid() {
        if (getDebugger() != null) {
            synchronized (invalidValue) {
                invalidValue[0] = true;
                getDebugger().requestWatchValue(this);
            }
        }
    }
    
    public void setValueToError(String msg) {
        msg = msg.replace("\\\"", "\""); // NOI18N
        if (msg.charAt(msg.length() - 1) == '.') {
            msg = msg.substring(0, msg.length() - 1);
        }
        setValue('>' + msg + '<');
    }
    
//    private boolean expressionIsSimpleVariable() {
//        String expression = watch.getExpression();
//        
//        if (expression.length() > 0) {
//            char ch = expression.charAt(0);
//            if (Character.isLetter(ch) || ch == '_') {
//                for (int i = 1; i < expression.length(); i++) {
//                    ch = expression.charAt(i);
//                    if (!Character.isLetterOrDigit(ch) && ch != '_') {
//                        return false;
//                    }
//                }
//                return true;
//            }
//        }
//        return false;
//    }
//    
//    private GdbVariable findSimpleVariable() {
//        String expression = watch.getExpression();
//        
//        for (GdbVariable var : getDebugger().getLocalVariables()) {
//            if (expression.equals(var.getName())) {
//                return var;
//            }
//        }
//        return null;
//    }
    
    public String getExpression() {
        return watch.getExpression();
    }
    
    public void setExpression(String expression) {
        if (expression != null && expression.trim().length() > 0) {
            watch.setExpression(expression);
        }
    }
}
