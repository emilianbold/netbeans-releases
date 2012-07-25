/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.javascript.debugger.breakpoints;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.modules.web.javascript.debugger.breakpoints.DOMNode.PathNotFoundException;
import org.netbeans.modules.web.webkit.debugging.api.Debugger;
import org.netbeans.modules.web.webkit.debugging.api.WebKitDebugging;
import org.netbeans.modules.web.webkit.debugging.api.dom.Node;

/**
 *
 * @author Martin
 */
abstract class WebKitBreakpointManager implements PropertyChangeListener {
    
    protected final Debugger d;
    private final AbstractBreakpoint ab;

    protected WebKitBreakpointManager(Debugger d, AbstractBreakpoint ab) {
        this.d = d;
        this.ab = ab;
        ab.addPropertyChangeListener(this);
    }
    
    public static WebKitBreakpointManager create(Debugger d, LineBreakpoint lb) {
        return new WebKitLineBreakpointManager(d, lb);
    }
    
    public static WebKitBreakpointManager create(WebKitDebugging wd, DOMBreakpoint db) {
        return new WebKitDOMBreakpointManager(wd, db);
    }

    public abstract void add();

    public abstract void remove();

    public void destroy() {
        remove();
        ab.removePropertyChangeListener(this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if (!Breakpoint.PROP_ENABLED.equals(event.getPropertyName())) {
            return;
        }
        Breakpoint b = (Breakpoint) event.getSource();
        if (b.isEnabled()) {
            add();
        } else {
            remove();
        }
    }
    
    private static final class WebKitLineBreakpointManager extends WebKitBreakpointManager {
        
        private final LineBreakpoint lb;
        private org.netbeans.modules.web.webkit.debugging.api.debugger.Breakpoint b;
        
        public WebKitLineBreakpointManager(Debugger d, LineBreakpoint lb) {
            super(d, lb);
            this.lb = lb;
        }

        @Override
        public void add() {
            if (b != null) {
                return ;
            }
            String url = lb.getURLString();
            url = reformatFileURL(url);
            b = d.addLineBreakpoint(url, lb.getLine().getLineNumber(), 0);
        }

        @Override
        public void remove() {
            if (b == null) {
                return ;
            }
            d.removeLineBreakpoint(b);
            b = null;
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
    
    }

    private static final class WebKitDOMBreakpointManager extends WebKitBreakpointManager {
        
        private final WebKitDebugging wd;
        private final DOMBreakpoint db;
        private Node node;
        private Set<org.netbeans.modules.web.webkit.debugging.api.debugger.Breakpoint> bps;
        
        public WebKitDOMBreakpointManager(WebKitDebugging wd, DOMBreakpoint db) {
            super(wd.getDebugger(), db);
            this.wd = wd;
            this.db = db;
        }

        @Override
        public void add() {
            if (bps != null) {
                return ;
            }
            DOMNode dn = db.getNode();
            dn.addPropertyChangeListener(this);
            try {
                dn.bindTo(wd.getDOM());
            } catch (PathNotFoundException pex) {
                db.setValidity(pex);
                return ;
            }
            Node n = dn.getNode();
            if (n != null) {
                addTo(n);
            }
        }
        
        private void addTo(Node node) {
            this.node = node;
            Set<DOMBreakpoint.Type> types = db.getTypes();
            if (types.isEmpty()) {
                return ;
            }
            bps = new HashSet<org.netbeans.modules.web.webkit.debugging.api.debugger.Breakpoint>(types.size());
            for (DOMBreakpoint.Type type : types) {
                org.netbeans.modules.web.webkit.debugging.api.debugger.Breakpoint b = 
                        d.addDOMBreakpoint(node, type.getTypeString());
                if (b != null) {
                    bps.add(b);
                }
            }
        }

        @Override
        public void remove() {
            DOMNode dn = db.getNode();
            dn.unbind();
            dn.removePropertyChangeListener(this);
            removeBreakpoints();
        }
        
        private void removeBreakpoints() {
            this.node = null;
            if (bps == null) {
                return ;
            }
            for (org.netbeans.modules.web.webkit.debugging.api.debugger.Breakpoint b : bps) {
                d.removeLineBreakpoint(b);
            }
            bps = null;
        }

        @Override
        public void propertyChange(PropertyChangeEvent event) {
            String propertyName = event.getPropertyName();
            if (DOMNode.PROP_NODE_CHANGED.equals(propertyName)) {
                Node oldNode = (Node) event.getOldValue();
                if (oldNode != null) {
                    removeBreakpoints();
                }
                Node newNode = (Node) event.getNewValue();
                if (newNode != null) {
                    addTo(newNode);
                }
            } else if (DOMNode.PROP_NODE_PATH_FAILED.equals(propertyName)) {
                removeBreakpoints();
                db.setValidity((DOMNode.PathNotFoundException) event.getNewValue());
            } else if (DOMBreakpoint.PROP_TYPES.equals(propertyName)) {
                Node theNode = node;
                if (theNode != null) {
                    removeBreakpoints();
                    addTo(theNode);
                }
            } else {
                super.propertyChange(event);
            }
        }
        
    }

}
