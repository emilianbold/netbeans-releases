/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.spi.editor;

import org.netbeans.modules.editor.lib2.actions.*;
import java.awt.event.ActionEvent;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Base class for editor actions.
 *
 * @author Miloslav Metelka
 * @since 1.14
 */
public abstract class AbstractEditorAction extends TextAction {

    /** Logger for reporting invoked actions */
    private static Logger UILOG = Logger.getLogger("org.netbeans.ui.actions.editor"); // NOI18N

    private static final long serialVersionUID = 1L; // Serialization no longer used (prevent warning)

    private final Map<String,?> attrs;

    /**
     * Constructor that should be used when a descendant requests a direct action's instantiation.
     * When annotated with <code>@EditorActionRegistration</code> the infrastructure
     * will pass action's properties from a generated layer to the action.
     *
     * @param attrs non-null attributes that hold action's properties.
     */
    protected AbstractEditorAction(Map<String,?> attrs) {
        super(null);
        this.attrs = attrs;

        if (attrs != null) {
            String actionName = (String)attrs.get(Action.NAME);
            if (actionName == null) {
                throw new IllegalArgumentException("Null Action.NAME attribute for action " + this.getClass()); // NOI18N
            }
            putValue(Action.NAME, actionName);
        }
    }

    /**
     * Constructor for a regular registration with <code>@EditorActionRegistration</code>
     * or for an explicit instantiation when no extra arguments need to be passed
     * to the action.
     */
    protected AbstractEditorAction() {
        this(null);
    }

    /**
     * Implementation of the action must be defined by descendants.
     *
     * @param evt non-null event
     * @param component "active" text component obtained by {@link TextAction#getFocusedComponent()}.
     */
    public abstract void actionPerformed(ActionEvent evt, JTextComponent component);

    /**
     * Called by {@link #putValue(String,String)} when {@link Action#NAME} property
     * is set to a non-null String value. This allows a "polymorphic" action (with
     * Action.NAME-specific behavior) to update certain properties (e.g. an icon)
     * according to the name that was set.
     *
     * @param actionName non-null action's name (value of Action.NAME property).
     */
    protected void actionNameUpdate(String actionName) {
    }

    /**
     * Possibly allow asynchronous execution of the action by returning true.
     * @return false (by default) or true to allow asynchronous execution.
     */
    protected boolean asynchronous() {
        return false;
    }

    /**
     * @return value of <code>Action.NAME</code> property.
     */
    protected final String actionName() {
        return (String) getValue(Action.NAME);
    }


    /**
     * Reset caret's magic position.
     * @param component target text component.
     */
    protected final void resetCaretMagicPosition(JTextComponent component) {
        Caret caret;
        if (component != null && (caret = component.getCaret()) != null) {
            caret.setMagicCaretPosition(null);
        }
    }

    @Override
    public final void actionPerformed(final ActionEvent evt) {
        final JTextComponent component = getTextComponent(evt);
        MacroRecording.get().recordAction(this, evt); // Possibly record action in a currently recorded macro

        if (UILOG.isLoggable(Level.FINE)) {
            // TODO [Mila] - Set action's property to disable UI logging
            String actionNameLowerCase = actionName();
            if (actionNameLowerCase != null &&
                !"default-typed".equals(actionNameLowerCase) && //NOI18N
                -1 == actionNameLowerCase.indexOf("caret") && //NOI18N
                -1 == actionNameLowerCase.indexOf("delete") && //NOI18N
                -1 == actionNameLowerCase.indexOf("selection") && //NOI18N
                -1 == actionNameLowerCase.indexOf("build-tool-tip") &&//NOI18N
                -1 == actionNameLowerCase.indexOf("build-popup-menu") &&//NOI18N
                -1 == actionNameLowerCase.indexOf("page-up") &&//NOI18N
                -1 == actionNameLowerCase.indexOf("page-down") &&//NOI18N
                -1 == actionNameLowerCase.indexOf("-kit-install") //NOI18N
            ) {
                LogRecord r = new LogRecord(Level.FINE, "UI_ACTION_EDITOR"); // NOI18N
                r.setResourceBundle(NbBundle.getBundle(AbstractEditorAction.class));
                if (evt != null) {
                    r.setParameters(new Object[] { evt, evt.toString(), this, toString(), getValue(NAME) });
                } else {
                    r.setParameters(new Object[] { "no-ActionEvent", "no-ActionEvent", this, toString(), getValue(NAME) }); //NOI18N
                }
                r.setLoggerName(UILOG.getName());
                UILOG.log(r);
            }
        }

        if (asynchronous()) {
            RequestProcessor.getDefault().post(new Runnable () {
                public void run() {
                    actionPerformed(evt, component);
                }
            });
        } else {
            actionPerformed(evt, component);
        }
    }

    @Override
    public Object getValue(String key) {
        Object value = super.getValue(key);
        if (value == null && attrs != null) {
            if (!"instanceCreate".equals(key)) { // Return null for this key
                value = attrs.get(key);
            }
        }
        return value;
    }

    @Override
    public void putValue(String key, Object value) {
        super.putValue(key, value);
        if (Action.NAME.equals(key) && value instanceof String) {
            actionNameUpdate((String)value);
        }
    }


}
