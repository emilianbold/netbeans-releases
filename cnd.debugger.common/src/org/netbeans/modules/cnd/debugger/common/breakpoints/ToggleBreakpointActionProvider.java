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

package org.netbeans.modules.cnd.debugger.common.breakpoints;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.Set;
import java.util.logging.Logger;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.modules.cnd.debugger.common.EditorContextBridge;
import org.netbeans.modules.cnd.debugger.common.disassembly.DisassemblyProvider;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.spi.debugger.ActionsProviderSupport;
import org.netbeans.spi.debugger.ContextProvider;
import org.openide.util.NbBundle;

/**
 *
 * @author Egor Ushakov
 */
public class ToggleBreakpointActionProvider extends ActionsProviderSupport implements PropertyChangeListener {

    private final Logger log = Logger.getLogger("cnd.breakpoint.annotations"); // NOI18N
    
    public ToggleBreakpointActionProvider() {
        EditorContextBridge.getContext().addPropertyChangeListener(this);
    } 

    //is not used for now
    /** Creates a new instance of ToggleBreakpointActionProvider */
//    public ToggleBreakpointActionProvider(ContextProvider lookupProvider) {
//        EditorContextBridge.getContext().addPropertyChangeListener(this);
//    }

//    private void destroy () {
//        EditorContextBridge.getContext().removePropertyChangeListener(this);
//    }
    
    public void doAction(Object o) {
        DebuggerManager d = DebuggerManager.getDebuggerManager();
        
        // 1) get source name & line number
        int ln = EditorContextBridge.getContext().getCurrentLineNumber();
        String url = EditorContextBridge.getContext().getCurrentURL();
        if (url.trim().equals ("")) {
            return;
        }
        
        // 2) find and remove existing line breakpoint
        CndBreakpoint lb = findBreakpoint(url, ln);
        if (lb != null) {
            log.fine("ToggleBreakpointActionProvider.doAction: Removing breakpoint at " + lb.getPath() + ":" + lb.getLineNumber());
            d.removeBreakpoint(lb);
            return;
        }

        //FIXME : obtain DisassemblyProvider from the debugger
        DisassemblyProvider disProvider = null;
        if (disProvider != null && disProvider.isDis(url)) {
            lb = AddressBreakpoint.create(disProvider.getLineAddress(ln));
            lb.setPrintText(
                NbBundle.getBundle(ToggleBreakpointActionProvider.class).getString("CTL_Address_Breakpoint_Print_Text")
            );
            log.fine("ToggleBreakpointActionProvider.doAction: Adding disassembly breakpoint at " + lb.getPath() + ":" + lb.getLineNumber());
            d.addBreakpoint(lb);
            return;

        }
//        if (Disassembly.isDisasm(url)) {
//            Disassembly dis = Disassembly.getCurrent();
//            if (dis == null) {
//                return;
//            }
//            lb = AddressBreakpoint.create(dis.getLineAddress(ln));
//            lb.setPrintText(
//                NbBundle.getBundle(ToggleBreakpointActionProvider.class).getString("CTL_Address_Breakpoint_Print_Text")
//            );
//            log.fine("ToggleBreakpointActionProvider.doAction: Adding disassembly breakpoint at " + lb.getPath() + ":" + lb.getLineNumber());
//            d.addBreakpoint(lb);
//            return;
//        }
        
        // 3) create a new line breakpoint
        lb = LineBreakpoint.create(url, ln);
        log.fine("ToggleBreakpointActionProvider.doAction: Adding breakpoint at " + lb.getPath() + ":" + lb.getLineNumber());
        d.addBreakpoint(lb);
    }
    
    static CndBreakpoint findBreakpoint(String url, int lineNumber) {
        Breakpoint[] breakpoints = DebuggerManager.getDebuggerManager().getBreakpoints();
        //FIXME : obtain DisassemblyProvider from the debugger
        DisassemblyProvider disProvider = null;
        boolean inDis = disProvider != null && disProvider.isDis(url);
//        Disassembly dis = Disassembly.getCurrent();
//        boolean inDis = Disassembly.isDisasm(url);
        for (Breakpoint b : breakpoints) {
            if (b instanceof LineBreakpoint) {
                LineBreakpoint lb = (LineBreakpoint) b;
                if (lb.getURL().equals(url) && lb.getLineNumber() == lineNumber) {
                    return lb;
                }
            } else if (inDis && b instanceof AddressBreakpoint) {
                AddressBreakpoint ab = (AddressBreakpoint)b;
                if (disProvider.getAddressLine(ab.getAddress()) == lineNumber) {
                    return ab;
                }
            }
        }
        return null;
    }
    
    public Set getActions() {
        return Collections.singleton(ActionsManager.ACTION_TOGGLE_BREAKPOINT);
    }
        
    public void propertyChange(PropertyChangeEvent evt) {
        int lnum = EditorContextBridge.getContext().getCurrentLineNumber();
        String mimeType = EditorContextBridge.getContext().getCurrentMIMEType();
	boolean isValid = (MIMENames.isFortranOrHeaderOrCppOrC(mimeType)
                || mimeType.equals(MIMENames.ASM_MIME_TYPE))
                        && lnum > 0;
        setEnabled(ActionsManager.ACTION_TOGGLE_BREAKPOINT, isValid);
//        if (debugger != null && debugger.getState() == GdbDebugger.State.EXITED) {
//            destroy();
//        }
    }
}
