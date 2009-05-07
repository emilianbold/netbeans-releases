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
import java.io.File;
import java.util.logging.Logger;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.netbeans.api.debugger.Watch;
import org.netbeans.modules.cnd.api.model.services.CsmMacroExpansion;
import org.netbeans.modules.cnd.debugger.gdb.CallStackFrame;
import org.netbeans.modules.cnd.debugger.gdb.Field;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.text.NbDocument;

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
 *   calls setValue() (still on the GDB Reader thread). The setValueAT calls tells the blocked
 *   getValue() to continue and the updated value is displayed.
 *
 * @author gordonp
 */
public class GdbWatchVariable extends AbstractVariable implements PropertyChangeListener {
    
    private static boolean disableMacros = Boolean.getBoolean("gdb.macros.disable");

    private final Watch watch;
    private static final Logger log = Logger.getLogger("gdb.logger.watches"); // NOI18N
    
    private boolean request = true;
    private boolean requestResolved = true;

    private String resolvedType;
    
    /** Creates a new instance of GdbWatchVariable */
    public GdbWatchVariable(Watch watch) {
        this.watch = watch;
        name = watch.getExpression();
        fields = new Field[0];
        type = null;
        value = null;
        tinfo = null;
//        derefValue = null;
        
        if (getDebugger() != null) {
            getDebugger().addPropertyChangeListener(this);
        }
        watch.addPropertyChangeListener(this);
    }
    
    public Watch getWatch() {
        return watch;
    }
    
    public void remove() {
        getDebugger().removePropertyChangeListener(this);
    }
    
    @Override
    public void propertyChange(final PropertyChangeEvent ev) {
        log.fine("GWV.propertyChange: Property change for " + ev.getPropertyName()); // NOI18N
        
        final String pname = ev.getPropertyName();
        // We do not need to listen to PROP_STATE here, because we do stack update on every stop
        if (pname.equals(GdbDebugger.PROP_CURRENT_THREAD) ||
                pname.equals(GdbDebugger.PROP_CURRENT_CALL_STACK_FRAME) ||
                pname.equals(Watch.PROP_EXPRESSION)) {
                    if (pname.equals(Watch.PROP_EXPRESSION)) {
                        resetVariable();
                    }
                    request = true;
                    requestResolved = true;
        } else if (ev.getPropertyName().equals(GdbDebugger.PROP_VALUE_CHANGED)) {
            super.propertyChange(ev);
        }
    }

    private void resetVariable() {
        tinfo = null;
        emptyFields();
    }
    
    /**
     * This method must be overriden because the expression can be changed and then the name
     * the var was created with changes. There is a startup race when getName() gets called
     * before watch has been set. Its OK, because AbstractVariable.name is initially set from
     * watch.getExpression() so all is well.
     * @return
     */
    @Override
    public String getName() {
        return watch.getExpression();
    }
    
    @Override
    public String getType() {
        checkValues();
        return type;
    }

    @Override
    protected String getResolvedType() {
        checkValues();
        if (requestResolved) {
            resolvedType = super.getResolvedType();
            requestResolved = false;
        }
        return resolvedType;
    }
    
    @Override
    public String getValue() {
        checkValues();
        return super.getValue();
    }

    private synchronized void checkValues() {
        if (request) {
            String expr = watch.getExpression();
            if (!disableMacros) {
                expr = expandMacro(getDebugger(), expr);
            }
            String t = getDebugger().requestWhatis(expr);
            if (t != null && t.length() > 0) {
                type = t;
                value = getDebugger().evaluate(expr);
            } else {
                type = ""; // NOI18N
                value = ""; // NOI18N
                resolvedType = ""; // NOI18N
                requestResolved = false;
            }
            setModifiedValue(value);
            request = false;
        }
    }

    public static String expandMacro(GdbDebugger debugger, String expr) {
        CallStackFrame csf = debugger.getCurrentCallStackFrame();
        if (csf != null) {
            Document doc = csf.getDocument();
            if (doc != null) {
                int offset = csf.getOffset();
                if (offset >= 0) {
                    return CsmMacroExpansion.expand(doc, offset, expr);
                }
            }
        }
        return expr;
    }
    
    @Override
    public void setValue(String value) {
        this.value = value;
    }
    
    public void setValueAt(String value) {
        super.setValue(value);
    }
}
