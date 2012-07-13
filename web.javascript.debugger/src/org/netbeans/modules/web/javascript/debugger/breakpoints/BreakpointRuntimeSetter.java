/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.web.javascript.debugger.breakpoints;

import java.beans.PropertyChangeEvent;
import java.net.URL;
import org.netbeans.api.debugger.*;
import org.netbeans.modules.web.clientproject.api.RemoteFileCache;
import org.netbeans.modules.web.webkit.debugging.api.Debugger;
import org.openide.filesystems.FileObject;
import org.openide.util.RequestProcessor;


/**
 * Responsible for setting breakpoints while debugging.
 * ( Otherwise breakpoints are used that was set before debugger start ).
 * @author ads
 *
 */
public class BreakpointRuntimeSetter extends DebuggerManagerAdapter  {

    private static final RequestProcessor RP = new RequestProcessor("Breakpoint updater");
            
    /* (non-Javadoc)
     * @see org.netbeans.api.debugger.LazyDebuggerManagerListener#getProperties()
     */
    @Override
    public String[] getProperties() {
        return new String[] { DebuggerManager.PROP_BREAKPOINTS };
    }

    /* (non-Javadoc)
     * @see org.netbeans.api.debugger.DebuggerManagerListener#breakpointAdded(org.netbeans.api.debugger.Breakpoint)
     */
    @Override
    public void breakpointAdded( Breakpoint breakpoint ) {
        if (!(breakpoint instanceof LineBreakpoint)) {
            return;
        }
        breakpoint.addPropertyChangeListener(Breakpoint.PROP_ENABLED, this);
        final LineBreakpoint lb = (LineBreakpoint)breakpoint;
        RP.post(new Runnable() {
            @Override
            public void run() {
                addBreakpoint(lb);
            }
        });
    }
    
    private static void addBreakpoint(LineBreakpoint lb) {
        for (Session se: DebuggerManager.getDebuggerManager().getSessions()) {
            Debugger d = se.lookupFirst("", Debugger.class);
            if (d != null) {
                addBreakpoint(d, lb);
            }
        }
    }
    public static void addBreakpoint(Debugger d, LineBreakpoint lb) {
        String url = lb.getURLString();
        url = reformatFileURL(url);
        org.netbeans.modules.web.webkit.debugging.api.debugger.Breakpoint b = 
                d.addLineBreakpoint(url, lb.getLine().getLineNumber(), 0);
        lb.setWebkitBreakpoint(b);
    }

    // changes "file:/some" to "file:///some"
    private static String reformatFileURL(String tabToDebug) {
        if (!tabToDebug.startsWith("file:")) {
            return tabToDebug;
        }
        tabToDebug = tabToDebug.substring(5);
        while (tabToDebug.length() > 0 && tabToDebug.startsWith("/")) {
            tabToDebug = tabToDebug.substring(1);
        }
        return "file:///"+tabToDebug;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.api.debugger.DebuggerManagerListener#breakpointRemoved(org.netbeans.api.debugger.Breakpoint)
     */
    @Override
    public void breakpointRemoved( Breakpoint breakpoint ) {
        if (!(breakpoint instanceof LineBreakpoint)) {
            return;
        }
        breakpoint.removePropertyChangeListener(Breakpoint.PROP_ENABLED, this);
        final LineBreakpoint lb = (LineBreakpoint)breakpoint;
        RP.post(new Runnable() {
            @Override
            public void run() {
                removeBreakpoint(lb);
            }
        });
    }
    
    private void removeBreakpoint(LineBreakpoint lb) {
        for (DebuggerEngine de: DebuggerManager.getDebuggerManager().getDebuggerEngines()) {
            Debugger d = de.lookupFirst("", Debugger.class);
            if (d != null) {
                d.removeLineBreakpoint(lb.getWebkitBreakpoint());
            }
        }
    }

    /* (non-Javadoc)
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    @Override
    public void propertyChange( PropertyChangeEvent event ) {
        if (!Breakpoint.PROP_ENABLED.equals(event.getPropertyName())) {
            return;
        }
        Breakpoint b = (Breakpoint)event.getSource();
        if (!(b instanceof LineBreakpoint)) {
            return;
        }
        LineBreakpoint lb = (LineBreakpoint)b;
        for (DebuggerEngine de: DebuggerManager.getDebuggerManager().getDebuggerEngines()) {
            Debugger d = de.lookupFirst("", Debugger.class);
            if (d != null) {
                if (lb.getWebkitBreakpoint() != null) {
                    d.removeLineBreakpoint(lb.getWebkitBreakpoint());
                    lb.setWebkitBreakpoint(null);
                } else {
                    breakpointAdded(b);
                }
            }
        }
    }

}
