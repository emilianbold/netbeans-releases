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
package org.netbeans.modules.debugger.jpda.projectsui;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JEditorPane;
import javax.swing.UIManager;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.ToolTipSupport;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.ui.EditorContextDispatcher;

/**
 *
 * @author martin
 */
@DebuggerServiceRegistration(path = "netbeans-JPDASession/PinWatchHeadActions", types = Action.class)
public class PinWatchExpandAction extends AbstractAction {

    private final Icon toExpandIcon = UIManager.getIcon ("Tree.collapsedIcon");    // NOI18N
    private final Icon toCollapsIcon = UIManager.getIcon ("Tree.expandedIcon");    // NOI18N
    private Reference<JPDADebugger> debuggerRef;
    private String expression;
    private Reference<ObjectVariable> varRef;
    private boolean expanded;

    public PinWatchExpandAction() {
        putValue(Action.SMALL_ICON, toExpandIcon);
        putValue(Action.LARGE_ICON_KEY, toExpandIcon);
    }

    @Override
    public void putValue(String key, Object value) {
        switch (key) {
            case "debugger":
                synchronized (this) {
                    if (debuggerRef == null || debuggerRef.get() != value) {
                        debuggerRef = new WeakReference<>((JPDADebugger) value);
                    }
                }
                break;
            case "expression":
                synchronized (this) {
                    expression = (String) value;
                }
                break;
            case "variable":
                synchronized (this) {
                    if (varRef == null || varRef.get() != value) {
                        varRef = new WeakReference<>((ObjectVariable) value);
                        expanded = false;
                    }
                }
                break;
            case "disposeState":
                synchronized (this) {
                    debuggerRef = null;
                    expression = null;
                    varRef = null;
                }
            default:
                super.putValue(key, value);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JPDADebugger debugger = null;
        String exp;
        ObjectVariable var = null;
        boolean expand;
        synchronized (this) {
            if (debuggerRef != null) {
                debugger = debuggerRef.get();
            }
            exp = expression;
            if (varRef != null) {
                var = varRef.get();
            }
            expanded = !expanded;
            expand = expanded;
        }
        if (debugger != null && exp != null && var != null) {
            if (expand) {
                displayExpanded(debugger, expression, var);
                putValue(Action.SMALL_ICON, toCollapsIcon);
                putValue(Action.LARGE_ICON_KEY, toCollapsIcon);
            } else {
                collapse();
                putValue(Action.SMALL_ICON, toExpandIcon);
                putValue(Action.LARGE_ICON_KEY, toExpandIcon);
            }
        }
    }

    private void displayExpanded(JPDADebugger debugger, String expression, ObjectVariable var) {
        ToolTipView toolTipView = ToolTipView.getToolTipView(debugger, expression, var);
        JEditorPane currentEditor = EditorContextDispatcher.getDefault().getMostRecentEditor();
        EditorUI eui = Utilities.getEditorUI(currentEditor);
        if (eui != null) {
            final ToolTipSupport toolTipSupport = eui.getToolTipSupport();
            toolTipView.setToolTipSupport(toolTipSupport);
            toolTipSupport.setToolTipVisible(true, false);
            toolTipSupport.setToolTip(toolTipView);
            toolTipSupport.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (ToolTipSupport.PROP_STATUS.equals(evt.getPropertyName())) {
                        if (!toolTipSupport.isToolTipVisible()) {
                            synchronized (PinWatchExpandAction.this) {
                                expanded = false;
                            }
                            putValue(Action.SMALL_ICON, toExpandIcon);
                            putValue(Action.LARGE_ICON_KEY, toExpandIcon);
                            toolTipSupport.removePropertyChangeListener(this);
                        }
                    }
                }
            });
        }
    }
    
    private void collapse() {
        JEditorPane currentEditor = EditorContextDispatcher.getDefault().getMostRecentEditor();
        EditorUI eui = Utilities.getEditorUI(currentEditor);
        if (eui != null) {
            eui.getToolTipSupport().setToolTipVisible(false, false);
        }
    }

}
