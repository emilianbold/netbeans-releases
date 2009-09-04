/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
package org.openide;

import org.openide.util.Lookup;
import org.openide.util.Utilities;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.swing.*;
import org.openide.util.ImageUtilities;


/** Permits dialogs to be displayed.
 * @author Jesse Glick
 * @since 3.14
 */
public abstract class DialogDisplayer {
    /** Subclass constructor. */
    protected DialogDisplayer() {
    }

    /** Get the default dialog displayer.
     * @return the default instance from lookup
     */
    public static DialogDisplayer getDefault() {
        DialogDisplayer dd = Lookup.getDefault ().lookup (DialogDisplayer.class);

        if (dd == null) {
            dd = new Trivial();
        }

        return dd;
    }

    /** Notify the user of something in a message box, possibly with feedback.
     * <p>To support both GUI and non-GUI use, this method may be called
     * from any thread (providing you are not holding any locks), and
     * will block the caller's thread. In GUI mode, it will be run in the AWT
     * event thread automatically. If you wish to hold locks, or do not
     * need the result object immediately or at all, please make this call
     * asynchronously (e.g. from the request processor).
     * @param descriptor description of the notification
     * @return the option that caused the message box to be closed
     */
    public abstract Object notify(NotifyDescriptor descriptor);

    /** Notify the user of something in a message box, possibly with feedback,
     * this method method may be called
     * from any thread. The thread will return immediately and
     * the dialog will be shown <q>later</q>, usually when AWT thread
     * is empty and can handle the request.
     * 
     * <p class="non-normative">
     * Implementation note: Since version 7.3, implementation improved to work
     * also before main window is opened. For example: When method is called
     * from ModuleInstall.restored, then modal dialog is opened and blocks main
     * window until dialog is closed. Typical use case is login dialog.
     * </p>
     *
     * @param descriptor description of the notification
     * @since 7.0
     */
    public void notifyLater(final NotifyDescriptor descriptor) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                DialogDisplayer.this.notify(descriptor);
            }
        });
    }

    /** Get a new standard dialog.
     * The dialog is designed and created as specified in the parameter.
     * Anyone who wants a dialog with standard buttons and
     * standard behavior should use this method.
     * <p><strong>Do not cache</strong> the resulting dialog if it
     * is modal and try to reuse it! Always create a new dialog
     * using this method if you need to show a dialog again.
     * Otherwise previously closed windows can reappear.
     * @param descriptor general description of the dialog
     * @return the new dialog
     */
    public abstract Dialog createDialog(DialogDescriptor descriptor);

    /**
     * Minimal implementation suited for standalone usage.
     * @see "#30031"
     */
    private static final class Trivial extends DialogDisplayer {
        public Object notify(NotifyDescriptor nd) {
            JDialog dialog = new StandardDialog(nd.getTitle(), true, nd, null, null);
            dialog.setVisible(true);

            return (nd.getValue() != null) ? nd.getValue() : NotifyDescriptor.CLOSED_OPTION;
        }

        public Dialog createDialog(final DialogDescriptor dd) {
            final StandardDialog dialog = new StandardDialog(
                    dd.getTitle(), dd.isModal(), dd, dd.getClosingOptions(), dd.getButtonListener()
                );
            dd.addPropertyChangeListener(new DialogUpdater(dialog, dd));

            return dialog;
        }

        /**
         * Given a message object, create a displayable component from it.
         */
        private static Component message2Component(Object message) {
            if (message instanceof Component) {
                return (Component) message;
            } else if (message instanceof Object[]) {
                Object[] sub = (Object[]) message;
                JPanel panel = new JPanel();
                panel.setLayout(new FlowLayout());

                for (int i = 0; i < sub.length; i++) {
                    panel.add(message2Component(sub[i]));
                }

                return panel;
            } else if (message instanceof Icon) {
                return new JLabel((Icon) message);
            } else {
                // bugfix #35742, used JTextArea to correctly word-wrapping
                String text = message.toString();
                JTextArea area = new JTextArea(text);
                Color c = UIManager.getColor("Label.background"); // NOI18N

                if (c != null) {
                    area.setBackground(c);
                }

                area.setLineWrap(true);
                area.setWrapStyleWord(true);
                area.setEditable(false);
                area.setTabSize(4); // looks better for module sys messages than 8

                area.setColumns(40);

                if (text.indexOf('\n') != -1) {
                    // Complex multiline message.
                    return new JScrollPane(area);
                } else {
                    // Simple message.
                    return area;
                }
            }
        }

        private static Component option2Button(Object option, NotifyDescriptor nd, ActionListener l, JRootPane rp) {
            if (option instanceof AbstractButton) {
                AbstractButton b = (AbstractButton) option;
                b.addActionListener(l);

                return b;
            } else if (option instanceof Component) {
                return (Component) option;
            } else if (option instanceof Icon) {
                return new JLabel((Icon) option);
            } else {
                String text;
                boolean defcap;

                if (option == NotifyDescriptor.OK_OPTION) {
                    text = "OK"; // XXX I18N
                    defcap = true;
                } else if (option == NotifyDescriptor.CANCEL_OPTION) {
                    text = "Cancel"; // XXX I18N
                    defcap = false;
                } else if (option == NotifyDescriptor.YES_OPTION) {
                    text = "Yes"; // XXX I18N
                    defcap = true;
                } else if (option == NotifyDescriptor.NO_OPTION) {
                    text = "No"; // XXX I18N
                    defcap = false;
                } else if (option == NotifyDescriptor.CLOSED_OPTION) {
                    throw new IllegalArgumentException();
                } else {
                    text = option.toString();
                    defcap = false;
                }

                JButton b = new JButton(text);

                if (defcap && (rp.getDefaultButton() == null)) {
                    rp.setDefaultButton(b);
                }

                // added a simple accessible name to buttons
                b.getAccessibleContext().setAccessibleName(text);
                b.addActionListener(l);

                return b;
            }
        }

        private static final class StandardDialog extends JDialog {
            NotifyDescriptor nd;
            private Component messageComponent;
            private final JPanel buttonPanel;
            private final Object[] closingOptions;
            private final ActionListener buttonListener;
            private boolean haveFinalValue = false;
            private Color nbErrorForeground;
            private Color nbWarningForeground;
            private Color nbInfoForeground;
            private JLabel notificationLine;
            private static final int MSG_TYPE_ERROR = 1;
            private static final int MSG_TYPE_WARNING = 2;
            private static final int MSG_TYPE_INFO = 3;


            public StandardDialog(
                String title, boolean modal, NotifyDescriptor nd, Object[] closingOptions, ActionListener buttonListener
            ) {
                super((Frame) null, title, modal);
                this.nd = nd;
                this.closingOptions = closingOptions;
                this.buttonListener = buttonListener;
                getContentPane().setLayout(new BorderLayout());
                setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                updateMessage();
                buttonPanel = new JPanel();
                buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
                updateOptions();
                getContentPane().add(buttonPanel, BorderLayout.SOUTH, 1);

                KeyStroke k = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
                Object actionKey = "cancel"; // NOI18N
                getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(k, actionKey);

                Action cancelAction = new AbstractAction() {
                        public void actionPerformed(ActionEvent ev) {
                            cancel();
                        }
                    };

                getRootPane().getActionMap().put(actionKey, cancelAction);
                addWindowListener(
                    new WindowAdapter() {
                        public void windowClosing(WindowEvent ev) {
                            if (!haveFinalValue) {
                                StandardDialog.this.nd.setValue(NotifyDescriptor.CLOSED_OPTION);
                            }
                        }
                    }
                );
                pack();

                Rectangle r = Utilities.getUsableScreenBounds();
                int maxW = (r.width * 9) / 10;
                int maxH = (r.height * 9) / 10;
                Dimension d = getPreferredSize();
                d.width = Math.min(d.width, maxW);
                d.height = Math.min(d.height, maxH);
                setBounds(Utilities.findCenterBounds(d));
            }

            private void cancel() {
                nd.setValue(NotifyDescriptor.CANCEL_OPTION);
                haveFinalValue = true;
                dispose();
            }

            @Override
            public void dispose() {
                super.dispose();
                getContentPane().removeAll();
                nd = null;
                messageComponent = null;
            }

            public void updateMessage() {
                if (messageComponent != null) {
                    getContentPane().remove(messageComponent);
                }

                //System.err.println("updateMessage: " + nd.getMessage());
                messageComponent = message2Component(nd.getMessage());
                if (! (nd instanceof WizardDescriptor) && nd.getNotificationLineSupport () != null) {
                    JComponent toAdd = new JPanel (new BorderLayout ());
                    toAdd.add (messageComponent, BorderLayout.CENTER);

                    nbErrorForeground = UIManager.getColor("nb.errorForeground"); //NOI18N
                    if (nbErrorForeground == null) {
                        //nbErrorForeground = new Color(89, 79, 191); // RGB suggested by Bruce in #28466
                        nbErrorForeground = new Color(255, 0, 0); // RGB suggested by jdinga in #65358
                    }

                    nbWarningForeground = UIManager.getColor("nb.warningForeground"); //NOI18N
                    if (nbWarningForeground == null) {
                        nbWarningForeground = new Color(51, 51, 51); // Label.foreground
                    }

                    nbInfoForeground = UIManager.getColor("nb.warningForeground"); //NOI18N
                    if (nbInfoForeground == null) {
                        nbInfoForeground = UIManager.getColor("Label.foreground"); //NOI18N
                    }

                    notificationLine = new FixedHeightLabel ();
                    NotificationLineSupport nls = nd.getNotificationLineSupport ();
                    if (nls.getInformationMessage () != null) {
                        updateNotificationLine (this, MSG_TYPE_INFO, nls.getInformationMessage ());
                    } else if (nls.getWarningMessage () != null) {
                        updateNotificationLine (this, MSG_TYPE_WARNING, nls.getWarningMessage ());
                    } else if (nls.getErrorMessage () != null) {
                        updateNotificationLine (this, MSG_TYPE_ERROR, nls.getErrorMessage ());
                    }
                    toAdd.add (notificationLine, BorderLayout.SOUTH);
                    messageComponent = toAdd;
                }
                getContentPane().add(messageComponent, BorderLayout.CENTER);
            }

            public void updateOptions() {
                Set<Object> addedOptions = new HashSet<Object>(5);
                Object[] options = nd.getOptions();

                if (options == null) {
                    switch (nd.getOptionType()) {
                    case NotifyDescriptor.DEFAULT_OPTION:
                    case NotifyDescriptor.OK_CANCEL_OPTION:
                        options = new Object[] { NotifyDescriptor.OK_OPTION, NotifyDescriptor.CANCEL_OPTION, };

                        break;

                    case NotifyDescriptor.YES_NO_OPTION:
                        options = new Object[] { NotifyDescriptor.YES_OPTION, NotifyDescriptor.NO_OPTION, };

                        break;

                    case NotifyDescriptor.YES_NO_CANCEL_OPTION:
                        options = new Object[] {
                                NotifyDescriptor.YES_OPTION, NotifyDescriptor.NO_OPTION, NotifyDescriptor.CANCEL_OPTION,
                            };

                        break;

                    default:
                        throw new IllegalArgumentException();
                    }
                }

                //System.err.println("prep: " + Arrays.asList(options) + " " + Arrays.asList(closingOptions) + " " + buttonListener);
                buttonPanel.removeAll();

                JRootPane rp = getRootPane();

                for (int i = 0; i < options.length; i++) {
                    addedOptions.add(options[i]);
                    buttonPanel.add(option2Button(options[i], nd, makeListener(options[i]), rp));
                }

                options = nd.getAdditionalOptions();

                if (options != null) {
                    for (int i = 0; i < options.length; i++) {
                        addedOptions.add(options[i]);
                        buttonPanel.add(option2Button(options[i], nd, makeListener(options[i]), rp));
                    }
                }

                if (closingOptions != null) {
                    for (int i = 0; i < closingOptions.length; i++) {
                        if (addedOptions.add(closingOptions[i])) {
                            ActionListener l = makeListener(closingOptions[i]);
                            attachActionListener(closingOptions[i], l);
                        }
                    }
                }
            }

            private void attachActionListener(Object comp, ActionListener l) {
                // on JButtons attach simply by method call
                if (comp instanceof JButton) {
                    JButton b = (JButton) comp;
                    b.addActionListener(l);

                    return;
                } else {
                    // we will have to use dynamic method invocation to add the action listener
                    // to generic component (and we succeed only if it has the addActionListener method)
                    java.lang.reflect.Method m;

                    try {
                        m = comp.getClass().getMethod("addActionListener", new Class[] { ActionListener.class }); // NOI18N

                        try {
                            m.setAccessible(true);
                        } catch (SecurityException se) {
                            m = null; // no jo, we cannot make accessible
                        }
                    } catch (NoSuchMethodException e) {
                        m = null; // no jo, we cannot attach ActionListener to this Component
                    } catch (SecurityException e2) {
                        m = null; // no jo, we cannot attach ActionListener to this Component
                    }

                    if (m != null) {
                        try {
                            m.invoke(comp, new Object[] { l });
                        } catch (Exception e) {
                            // not succeeded, so give up
                        }
                    }
                }
            }

            private ActionListener makeListener(final Object option) {
                return new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            //System.err.println("actionPerformed: " + option);
                            nd.setValue(option);

                            if (buttonListener != null) {
                                // #34485: some listeners expect that the action source is the option, not the button
                                ActionEvent e2 = new ActionEvent(
                                        option, e.getID(), e.getActionCommand(), e.getWhen(), e.getModifiers()
                                    );
                                buttonListener.actionPerformed(e2);
                            }

                            if ((closingOptions == null) || Arrays.asList(closingOptions).contains(option)) {
                                haveFinalValue = true;
                                setVisible(false);
                            }
                        }
                    };
            }
        }

        private static class DialogUpdater implements PropertyChangeListener {

            private StandardDialog dialog;

            private DialogDescriptor dd;

            public DialogUpdater(StandardDialog dialog, DialogDescriptor dd) {
                super();
                this.dialog = dialog;
                this.dd = dd;
            }

            public void propertyChange(final PropertyChangeEvent ev) {
                if( !SwingUtilities.isEventDispatchThread() ) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            propertyChange(ev);
                        }
                    });
                    return;
                }
                String pname = ev.getPropertyName();
                if (NotifyDescriptor.PROP_TITLE.equals(pname)) {
                    dialog.setTitle(dd.getTitle());
                } else
                    if (NotifyDescriptor.PROP_MESSAGE.equals(pname)) {
                        dialog.updateMessage();
                        dialog.validate();
                        dialog.repaint();
                    } else
                        if (NotifyDescriptor.PROP_OPTIONS.equals(pname) || NotifyDescriptor.PROP_OPTION_TYPE.equals(pname)) {
                            dialog.updateOptions();
                            dialog.validate();
                            dialog.repaint();
                    } else if (NotifyDescriptor.PROP_INFO_NOTIFICATION.equals (ev.getPropertyName ())) {
                        updateNotificationLine (dialog, StandardDialog.MSG_TYPE_INFO, ev.getNewValue ());
                    } else if (NotifyDescriptor.PROP_WARNING_NOTIFICATION.equals (ev.getPropertyName ())) {
                        updateNotificationLine (dialog, StandardDialog.MSG_TYPE_WARNING, ev.getNewValue ());
                    } else if (NotifyDescriptor.PROP_ERROR_NOTIFICATION.equals (ev.getPropertyName ())) {
                        updateNotificationLine (dialog, StandardDialog.MSG_TYPE_ERROR, ev.getNewValue ());
                    }
            }
        }

        private static void updateNotificationLine (StandardDialog dialog, int msgType, Object o) {
            String msg = o == null ? null : o.toString ();
            if (msg != null && msg.trim().length() > 0) {
                switch (msgType) {
                    case StandardDialog.MSG_TYPE_ERROR:
                        prepareMessage(dialog.notificationLine, ImageUtilities.loadImageIcon("org/netbeans/modules/dialogs/error.gif", false),
                            dialog.nbErrorForeground);
                        break;
                    case StandardDialog.MSG_TYPE_WARNING:
                        prepareMessage(dialog.notificationLine, ImageUtilities.loadImageIcon("org/netbeans/modules/dialogs/warning.gif", false),
                            dialog.nbWarningForeground);
                        break;
                    case StandardDialog.MSG_TYPE_INFO:
                        prepareMessage(dialog.notificationLine, ImageUtilities.loadImageIcon("org/netbeans/modules/dialogs/info.png", false),
                            dialog.nbInfoForeground);
                        break;
                    default:
                }
                dialog.notificationLine.setToolTipText (msg);
            } else {
                prepareMessage(dialog.notificationLine, null, null);
                dialog.notificationLine.setToolTipText (null);
            }
            dialog.notificationLine.setText(msg);
        }

        private static void prepareMessage(JLabel label, ImageIcon icon, Color fgColor) {
            label.setIcon(icon);
            label.setForeground(fgColor);
        }

        private static final class FixedHeightLabel extends JLabel {

            private static final int ESTIMATED_HEIGHT = 16;

            public FixedHeightLabel () {
                super ();
            }

            @Override
            public Dimension getPreferredSize() {
                Dimension preferredSize = super.getPreferredSize();
                assert ESTIMATED_HEIGHT == ImageUtilities.loadImage ("org/netbeans/modules/dialogs/warning.gif").getHeight (null) : "Use only 16px icon.";
                preferredSize.height = Math.max (ESTIMATED_HEIGHT, preferredSize.height);
                return preferredSize;
            }
        }

    }
}
