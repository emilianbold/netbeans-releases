/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.debugger.jpda.truffle.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.modules.debugger.jpda.truffle.MIMETypes;
import org.netbeans.modules.debugger.jpda.truffle.breakpoints.TruffleLineBreakpoint;
import org.netbeans.modules.javascript2.debug.EditorLineHandlerFactory;
import org.netbeans.spi.debugger.ActionsProvider;
import org.netbeans.spi.debugger.ActionsProviderSupport;
import org.netbeans.spi.debugger.ui.EditorContextDispatcher;
import org.openide.filesystems.FileObject;
import org.openide.text.Line;
import org.openide.util.WeakListeners;

/**
 *
 * @author Martin
 */
@ActionsProvider.Registration(path="", actions={ "toggleBreakpoint" })
public class ToggleBreakpointsInLanguagesActionProvider extends ActionsProviderSupport
                                                        implements PropertyChangeListener {
    
    private static final Set<String> IGNORED_MIME_TYPES = new HashSet<>(
            Arrays.asList("text/javascript", "text/x-java"));                     // NOI18N
    
    private volatile Line postedLine;
    
    public ToggleBreakpointsInLanguagesActionProvider() {
        setEnabled(ActionsManager.ACTION_TOGGLE_BREAKPOINT, false);
        EditorContextDispatcher.getDefault().addPropertyChangeListener(
                WeakListeners.propertyChange(this, EditorContextDispatcher.getDefault()));
        MIMETypes.getDefault().addPropertyChangeListener(
                WeakListeners.propertyChange(this, MIMETypes.getDefault()));
    }

    @Override
    public void postAction(Object action, final Runnable actionPerformedNotifier) {
        assert action == ActionsManager.ACTION_TOGGLE_BREAKPOINT : action;
        EditorContextDispatcher context = EditorContextDispatcher.getDefault();
        postedLine = context.getCurrentLine();
        if (postedLine == null) {
            actionPerformedNotifier.run();
            return ;
        }
        super.postAction(action, new Runnable() {
            @Override
            public void run() {
                try {
                    actionPerformedNotifier.run();
                } finally {
                    postedLine = null;
                }
            }
        });
    }
    
    @Override
    public void doAction(Object action) {
        assert action == ActionsManager.ACTION_TOGGLE_BREAKPOINT : action;
        DebuggerManager d = DebuggerManager.getDebuggerManager ();
        Line line = postedLine;
        if (line == null) {
            line = EditorContextDispatcher.getDefault().getCurrentLine();
            if (line == null) {
                return ;
            }
        }
        FileObject fo = line.getLookup().lookup(FileObject.class);
        if (fo == null) {
            return ;
        }
        if (IGNORED_MIME_TYPES.contains(fo.getMIMEType())) {
            return ;
        }
        Set<String> mts = MIMETypes.getDefault().get();
        if (!mts.contains(fo.getMIMEType())) {
            return ;
        }
        toggleBreakpoint(fo, line);
    }

    @Override
    public Set getActions() {
        return Collections.singleton (ActionsManager.ACTION_TOGGLE_BREAKPOINT);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String propertyName = evt.getPropertyName();
        if (MIMETypes.PROP_MIME_TYPES.equals(propertyName)) {
            // Platform MIME types changed, enable the action
            // and load them when the action is invoked.
            setEnabled(ActionsManager.ACTION_TOGGLE_BREAKPOINT, true);
        } else if (EditorContextDispatcher.PROP_FILE.equals(propertyName)) {
            boolean enabled = false;
            FileObject fo = EditorContextDispatcher.getDefault().getCurrentFile();
            if (fo != null && !IGNORED_MIME_TYPES.contains(fo.getMIMEType())) {
                Set<String> mts = MIMETypes.getDefault().getCached();
                if (mts == null || mts.contains(fo.getMIMEType())) {
                    // When MIME types are not loaded yet, enable the action
                    // and load them when the action is invoked.
                    enabled = true;
                }
            }
            setEnabled(ActionsManager.ACTION_TOGGLE_BREAKPOINT, enabled);
        }
    }
    
    private void toggleBreakpoint(FileObject fo, Line line) {
        DebuggerManager d = DebuggerManager.getDebuggerManager();
        boolean add = true;
        int lineNumber = line.getLineNumber() + 1;
        for (Breakpoint breakpoint : d.getBreakpoints()) {
            if (breakpoint instanceof TruffleLineBreakpoint &&
                ((TruffleLineBreakpoint) breakpoint).getFileObject().equals(fo) &&
                ((TruffleLineBreakpoint) breakpoint).getLineNumber() == lineNumber) {
                
                d.removeBreakpoint(breakpoint);
                add = false;
                break;
            }
        }
        if (add) {
            d.addBreakpoint(createLineBreakpoint(line));
        }
        
    }

    private Breakpoint createLineBreakpoint(Line line) {
        FileObject fo = line.getLookup().lookup(FileObject.class);
        return new TruffleLineBreakpoint(EditorLineHandlerFactory.getHandler(fo, line.getLineNumber() + 1));
    }
    
}
