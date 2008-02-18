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

package org.netbeans.modules.cnd.debugger.gdb.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.Set;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.modules.cnd.debugger.gdb.breakpoints.BreakpointAnnotationListener;
import org.netbeans.modules.cnd.debugger.gdb.EditorContextBridge;
import org.netbeans.modules.cnd.debugger.gdb.breakpoints.GdbBreakpoint;
import org.netbeans.modules.cnd.debugger.gdb.breakpoints.LineBreakpoint;
import org.netbeans.spi.debugger.ActionsProviderSupport;
import org.netbeans.spi.debugger.ContextProvider;
import org.openide.util.NbBundle;

/**
 *
 * @author gordonp
 */
public class ToggleBreakpointActionProvider extends ActionsProviderSupport implements PropertyChangeListener {
    
    private BreakpointAnnotationListener breakpointAnnotationListener;
    
    /** Creates a new instance of ToggleBreakpointActionProvider */
    public ToggleBreakpointActionProvider() {
        EditorContextBridge.getContext().addPropertyChangeListener(this);
    } 
    
    /** Creates a new instance of ToggleBreakpointActionProvider */
    public ToggleBreakpointActionProvider(ContextProvider lookupProvider) {
        EditorContextBridge.getContext().addPropertyChangeListener(this);       
    }
    
    public void doAction(Object o) {
        DebuggerManager d = DebuggerManager.getDebuggerManager();
        
        // 1) get source name & line number
        int ln = EditorContextBridge.getContext().getCurrentLineNumber();
        String url = EditorContextBridge.getContext().getCurrentURL();
        if (url.trim().equals ("")) {
            return;
        }
        
        // 2) find and remove existing line breakpoint
        GdbBreakpoint lb = getBreakpointAnnotationListener().findBreakpoint(url, ln);
        if (lb != null) {
            d.removeBreakpoint(lb);
            return;
        }
        
        // 3) create a new line breakpoint
        lb = LineBreakpoint.create(url, ln);
        lb.setPrintText(NbBundle.getBundle(
                ToggleBreakpointActionProvider.class).getString("CTL_Line_Breakpoint_Print_Text")); // NOI18N
        d.addBreakpoint(lb);
    }
    
    private BreakpointAnnotationListener getBreakpointAnnotationListener() {
        if (breakpointAnnotationListener == null) {
            breakpointAnnotationListener = (BreakpointAnnotationListener) 
                DebuggerManager.getDebuggerManager().lookupFirst(null, BreakpointAnnotationListener.class);
        }
        return breakpointAnnotationListener;
    }
    
    public Set getActions() {
        return Collections.singleton(ActionsManager.ACTION_TOGGLE_BREAKPOINT);
    }
        
    public void propertyChange(PropertyChangeEvent evt) {
        int lnum = EditorContextBridge.getContext().getCurrentLineNumber();
        String mimeType = EditorContextBridge.getContext().getCurrentMIMEType();
	boolean isValid = (mimeType.equals("text/x-c") // NOI18N
                        || mimeType.equals("text/x-c++")) // NOI18N
                        && lnum > 0;
        setEnabled(ActionsManager.ACTION_TOGGLE_BREAKPOINT, isValid);
    }
}
