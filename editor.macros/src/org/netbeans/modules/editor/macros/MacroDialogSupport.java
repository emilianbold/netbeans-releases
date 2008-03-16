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
package org.netbeans.modules.editor.macros;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.MissingResourceException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.border.EmptyBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.editor.macros.storage.MacroDescription;
import org.netbeans.modules.editor.macros.storage.MacrosStorage;
import org.netbeans.modules.editor.macros.storage.ui.MacrosPanel;
import org.netbeans.modules.editor.settings.storage.api.EditorSettingsStorage;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/** 
 *
 * @author  Petr Nejedly
 */
public final class MacroDialogSupport {

    private static final Logger LOG = Logger.getLogger(MacroDialogSupport.class.getName());
    
    private MacroDialogSupport() {
        // no-op
    }
    
    public static MacroDescription findMacro(MimePath mimeType, String macroName) {
        EditorSettingsStorage<String, MacroDescription> ess = EditorSettingsStorage.<String, MacroDescription>get(MacrosStorage.ID);
        
        MacroDescription macro = null;
        
        // try 'mimeType' specific macros
        try {
            macro = ess.load(mimeType, null, false).get(macroName);
        } catch (IOException ioe) {
            LOG.log(Level.WARNING, null, ioe);
        }
        
        if (macro == null) {
            // try 'all languages' macros
            try {
                macro = ess.load(MimePath.EMPTY, null, false).get(macroName);
            } catch (IOException ioe) {
                LOG.log(Level.WARNING, null, ioe);
            }
        }
        
        return macro;
    }
    
    public static class StartMacroRecordingAction extends BaseAction {

        static final long serialVersionUID = 1L;

        public StartMacroRecordingAction() {
            super(BaseKit.startMacroRecordingAction, NO_RECORDING);
            putValue(BaseAction.ICON_RESOURCE_PROPERTY,
                    "org/netbeans/modules/editor/macros/start_macro_recording.png"); // NOI18N
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                if (!startRecording(target)) {
                    target.getToolkit().beep();
                }
            }
        }

        private boolean startRecording(JTextComponent c) {
            try {
                Method m = BaseAction.class.getDeclaredMethod("startRecording", JTextComponent.class); //NOI18N
                m.setAccessible(true);
                return (Boolean) m.invoke(this, c);
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Can't call BaseAction.startRecording", e); //NOI18N
                return false;
            }
        }
    } // End of StartMacroRecordingAction class

    public static final class StopMacroRecordingAction extends BaseAction {

        static final long serialVersionUID = 1L;

        public StopMacroRecordingAction() {
            super(BaseKit.stopMacroRecordingAction, NO_RECORDING);
            putValue(BaseAction.ICON_RESOURCE_PROPERTY,
                    "org/netbeans/modules/editor/macros/stop_macro_recording.png"); // NOI18N
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                final String macro = stopRecording(target);
                if (macro == null) { // not recording
                    target.getToolkit().beep();
                } else {
                    // popup a macro dialog
                    final MacrosPanel panel = new MacrosPanel(Lookup.getDefault());
                    panel.setBorder(new EmptyBorder(10, 10, 10, 10));
                    panel.addAncestorListener(new AncestorListener() {
                        public void ancestorAdded(AncestorEvent event) {
                            panel.forceAddMacro(macro);
                        }

                        public void ancestorRemoved(AncestorEvent event) {
                        }

                        public void ancestorMoved(AncestorEvent event) {
                        }
                    });
                    panel.getModel().load();
                    
                    final DialogDescriptor descriptor = new DialogDescriptor(
                        panel,
                        NbBundle.getMessage(MacroDialogSupport.class, "Macros_Dialog_title"), //NOI18N
                        true,
                        new Object[] {
                            DialogDescriptor.OK_OPTION,
                            DialogDescriptor.CANCEL_OPTION
                        },
                        DialogDescriptor.OK_OPTION,
                        DialogDescriptor.DEFAULT_ALIGN,
                        null,
                        null
                    );
                    descriptor.setClosingOptions (new Object[] {
                        DialogDescriptor.OK_OPTION,
                        DialogDescriptor.CANCEL_OPTION
                    });
                    descriptor.setValid(false);
                    panel.getModel().addPropertyChangeListener(new PropertyChangeListener() {
                        public void propertyChange(PropertyChangeEvent evt) {
                            if (evt.getPropertyName() == null || OptionsPanelController.PROP_CHANGED.equals(evt.getPropertyName())) {
                                descriptor.setValid(panel.getModel().isChanged());
                            }
                        }
                    });

                    DialogDisplayer.getDefault ().notify (descriptor);
                    if (descriptor.getValue () == DialogDescriptor.OK_OPTION) {
                        panel.getModel().save();
                    }
                }
            }
        }

        private String stopRecording(JTextComponent c) {
            try {
                Method m = BaseAction.class.getDeclaredMethod("stopRecording", JTextComponent.class); //NOI18N
                m.setAccessible(true);
                return (String) m.invoke(this, c);
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Can't call BaseAction.stopRecording", e); //NOI18N
                return null;
            }
        }
    } // End of StopMacroRecordingAction class

    public static class RunMacroAction extends BaseAction {

        static final long serialVersionUID = 1L;
        static HashSet<String> runningActions = new HashSet<String>();
        private String macroName;

        public RunMacroAction(String name) {
            super(BaseKit.macroActionPrefix + name);
            this.macroName = name;
        }

        protected void error(JTextComponent target, String messageKey, Object... params) {
            String message;
            try {
                message = NbBundle.getMessage(RunMacroAction.class, messageKey, params);
            } catch (MissingResourceException e) {
                message = "Error in macro: " + messageKey + "; macroName = '" + macroName + "'"; //NOI18N
            }
            
            Utilities.setStatusText(target, message);
            Toolkit.getDefaultToolkit().beep();
            LOG.log(Level.WARNING, null, new Throwable(message));
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target == null) {
                return;
            }

            BaseKit kit = Utilities.getKit(target);
            if (kit == null) {
                return;
            }

            BaseDocument doc = Utilities.getDocument(target);
            if (doc == null) {
                return;
            }

            MimePath mimeType = MimePath.parse(NbEditorUtilities.getMimeType(target));
            MacroDescription macro = findMacro(mimeType, macroName);
            if (macro == null) {
                error(target, "macro-not-found", macroName); // NOI18N
                return;
            }

            if (!runningActions.add(macroName)) { // this macro is already running, beware of loops
                error(target, "loop", macroName); // NOI18N
                return;
            }
            try {
                runMacro(target, doc, kit, macro.getCode());
            } finally {
                runningActions.remove(macroName);
            }
        }

        private void runMacro(JTextComponent component, BaseDocument doc, BaseKit kit, String commandString) {
            StringBuffer actionName = new StringBuffer();
            char[] command = commandString.toCharArray();
            int len = command.length;

            doc.atomicLock();
            try {
                for (int i = 0; i < len; i++) {
                    if (Character.isWhitespace(command[i])) {
                        continue;
                    }
                    if (command[i] == '"') { //NOI18N
                        while (++i < len && command[i] != '"') { //NOI18N
                            char ch = command[i];
                            if (ch == '\\') { //NOI18N
                                if (++i >= len) { // '\' at the end
                                    error(component, "macro-malformed", macroName); // NOI18N
                                    return;
                                }
                                ch = command[i];
                                if (ch != '"' && ch != '\\') { // neither \\ nor \" // NOI18N
                                    error(component, "macro-malformed", macroName); // NOI18N
                                    return;
                                } // else fall through
                            }
                            Action a = component.getKeymap().getDefaultAction();

                            if (a != null) {
                                ActionEvent newEvt = new ActionEvent(component, 0, new String(new char[]{ch}));
                                if (a instanceof BaseAction) {
                                    ((BaseAction) a).updateComponent(component);
                                    ((BaseAction) a).actionPerformed(newEvt, component);
                                } else {
                                    a.actionPerformed(newEvt);
                                }
                            }
                        }
                    } else { // parse the action name
                        actionName.setLength(0);
                        while (i < len && !Character.isWhitespace(command[i])) {
                            char ch = command[i++];
                            if (ch == '\\') { //NOI18N
                                if (i >= len) { // macro ending with single '\'
                                    error(component, "macro-malformed", macroName); // NOI18N
                                    return;
                                }
                                ch = command[i++];
                                if (ch != '\\' && !Character.isWhitespace(ch)) { //NOI18N
                                    error(component, "macro-malformed", macroName); // neither "\\" nor "\ " // NOI18N
                                    return;
                                } // else fall through
                            }
                            actionName.append(ch);
                        }
                        // execute the action
                        Action a = kit.getActionByName(actionName.toString());
                        if (a != null) {
                            ActionEvent fakeEvt = new ActionEvent(component, 0, ""); //NOI18N
                            if (a instanceof BaseAction) {
                                ((BaseAction) a).updateComponent(component);
                                ((BaseAction) a).actionPerformed(fakeEvt, component);
                            } else {
                                a.actionPerformed(fakeEvt);
                            }
                            if (DefaultEditorKit.insertBreakAction.equals(actionName.toString())) {
                                Action def = component.getKeymap().getDefaultAction();
                                ActionEvent fakeEvt10 = new ActionEvent(component, 0, new String(new byte[]{10}));
                                if (def instanceof BaseAction) {
                                    ((BaseAction) def).updateComponent(component);
                                    ((BaseAction) def).actionPerformed(fakeEvt10, component);
                                } else {
                                    def.actionPerformed(fakeEvt10);
                                }
                            }
                        } else {
                            error(component, "macro-unknown-action", macroName, actionName.toString()); // NOI18N
                            return;
                        }
                    }
                }
            } finally {
                doc.atomicUnlock();
            }
        }
    } // End of RunMacroAction class
}
