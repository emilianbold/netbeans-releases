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
package org.netbeans.modules.cnd.remote.projectui.toolbar;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.UIResource;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ui.ServerListUI;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.remote.actions.base.RemoteOpenActionBase;
import org.openide.awt.Actions;
import org.openide.awt.DynamicMenuContent;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.actions.Presenter;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
@ServiceProvider(path="CND/Toobar/Services/RemoteHost", service=ActionListener.class)
public class RemoteHostActionPerformer implements ActionListener, PropertyChangeListener, Presenter.Menu {

    private static final Logger LOGGER = Logger.getLogger("remote.toolbar"); //NOI18N
    private static final RequestProcessor RP = new RequestProcessor(RemoteHostActionPerformer.class);
    private static final DefaultComboBoxModel EMPTY_MODEL = new DefaultComboBoxModel();
    private static final Object CUSTOMIZE_ENTRY = new Object();
    private JComboBox hostListCombo;
    private boolean listeningToCombo = true;
    private RemoteHostAction presenter;

    @SuppressWarnings("LeakingThisInConstructor")
    public RemoteHostActionPerformer() {
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (RemoteOpenActionBase.ACTIVATED_PSEUDO_ACTION_COMAND.equals(e.getActionCommand())) {
            if (presenter == null) {
                presenter = (RemoteHostAction) e.getSource();
                hostListCombo = presenter.hostListCombo;
                initHostsListCombo();
                ServerList.addPropertyChangeListener(WeakListeners.propertyChange(this, ServerList.getRecords()));
            }
        }
    }
    
    private void initHostsListCombo() {
        assert EventQueue.isDispatchThread();
        if (hostListCombo == null) {
            return;
        }
        LOGGER.fine("initConfigListCombo"); // NOI18N
        hostListCombo.addPopupMenuListener(new PopupMenuListener() {

            private Component prevFocusOwner = null;

            public @Override
            void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                prevFocusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
                hostListCombo.setFocusable(true);
                hostListCombo.requestFocusInWindow();
            }

            public @Override
            void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                if (prevFocusOwner != null) {
                    prevFocusOwner.requestFocusInWindow();
                }
                prevFocusOwner = null;
                hostListCombo.setFocusable(false);
            }

            public @Override
            void popupMenuCanceled(PopupMenuEvent e) {
            }
        });
        hostListCombo.setRenderer(new RemoteHostActionPerformer.HostCellRenderer());
        hostListCombo.setToolTipText(Actions.cutAmpersand(presenter.getName()));
        hostListCombo.setFocusable(false);
        hostsListChanged(ServerList.getRecords());
        defaultHostChanged(ServerList.getDefaultRecord());
        hostListCombo.addActionListener(new ActionListener() {

            public @Override
            void actionPerformed(ActionEvent e) {
                if (!listeningToCombo) {
                    return;
                }
                Object o = hostListCombo.getSelectedItem();
                if (o == CUSTOMIZE_ENTRY) {
                    defaultHostChanged(ServerList.getDefaultRecord());
                    RP.post(new Runnable() {

                        @Override
                        public void run() {
                            ServerListUI.showServerListDialog();
                            defaultHostChanged(ServerList.getDefaultRecord());
                        }
                    });
                } else if (o != null) {
                    activeHostSelected((ServerRecord) o);
                }
            }
        });
    }

    private synchronized void hostsListChanged(Collection<? extends ServerRecord> records) {
        LOGGER.log(Level.FINE, "hostsListChanged: {0}", records); // NOI18N
        if (records == null) {
            EventQueue.invokeLater(new Runnable() {

                public @Override
                void run() {
                    hostListCombo.setModel(EMPTY_MODEL);
                    hostListCombo.setEnabled(false); // possibly redundant, but just in case
                }
            });
        } else {
            final DefaultComboBoxModel model = new DefaultComboBoxModel(records.toArray());
            model.addElement(CUSTOMIZE_ENTRY);
            EventQueue.invokeLater(new Runnable() {

                public @Override
                void run() {
                    hostListCombo.setModel(model);
                    hostListCombo.setEnabled(true);
                    defaultHostChanged(ServerList.getDefaultRecord());
                }
            });
        }
    }

    private synchronized void defaultHostChanged(final ServerRecord record) {
        LOGGER.log(Level.FINE, "defaultHostChanged: {0}", record); // NOI18N
        EventQueue.invokeLater(new Runnable() {

            public @Override
            void run() {
                listeningToCombo = false;
                try {
                    hostListCombo.setSelectedIndex(-1);
                    if (record != null) {
                        ComboBoxModel m = hostListCombo.getModel();
                        for (int i = 0; i < m.getSize(); i++) {
                            if (record.equals(m.getElementAt(i))) {
                                hostListCombo.setSelectedIndex(i);
                                break;
                            }
                        }
                    }
                } finally {
                    listeningToCombo = true;
                }
            }
        });
    }

    private synchronized void activeHostSelected(final ServerRecord record) {
        RP.post(new Runnable() {

            @Override
            public void run() {
                LOGGER.log(Level.FINE, "activeHostSelected: {0}", record); // NOI18N
                ServerList.setDefaultRecord(record);
            }
        });
    }

    private static ServerRecord getActiveHost() {
        return ServerList.getDefaultRecord();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (ServerList.PROP_RECORD_LIST.equals(evt.getPropertyName())){
            hostsListChanged(ServerList.getRecords());
        } else if (ServerList.PROP_DEFAULT_RECORD.equals(evt.getPropertyName())){
            defaultHostChanged(ServerList.getDefaultRecord());
        }
    }

    @Override
    public JMenuItem getMenuPresenter() {
        return new HostMenu();
    }

    private class HostMenu extends JMenu implements DynamicMenuContent, ActionListener {

        @SuppressWarnings("LeakingThisInConstructor")
        public HostMenu() {
            Mnemonics.setLocalizedText(this, RemoteHostActionPerformer.this.presenter.getName());
        }

        public @Override
        JComponent[] getMenuPresenters() {
            removeAll();
            boolean something = false;
            final ServerRecord activeHost = getActiveHost();
            for (final ServerRecord host : ServerList.getRecords()) {
                JRadioButtonMenuItem jmi = new JRadioButtonMenuItem(host.getDisplayName(), host.equals(activeHost));
                jmi.addActionListener(new ActionListener() {

                    public @Override
                    void actionPerformed(ActionEvent e) {
                        activeHostSelected(activeHost);
                    }
                });
                add(jmi);
                something = true;
            }
            setEnabled(something);
            return new JComponent[]{this};

        }

        @Override
        public JComponent[] synchMenuPresenters(JComponent[] items) {
            // Always rebuild submenu.
            // For performance, could try to reuse it if context == null and nothing has changed.
            return getMenuPresenters();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
        }
    }

    @SuppressWarnings("serial")
    private static class HostCellRenderer extends JLabel implements ListCellRenderer, UIResource {

        private Border defaultBorder = getBorder();

        public HostCellRenderer() {
            setOpaque(true);
        }

        public @Override
        Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            // #89393: GTK needs name to render cell renderer "natively"
            setName("ComboBox.listRenderer"); // NOI18N

            String label;
            if (value instanceof ServerRecord) {
                label = ((ServerRecord) value).getDisplayName();
                setBorder(defaultBorder);
            } else if (value == CUSTOMIZE_ENTRY) {
                label = org.openide.awt.Actions.cutAmpersand(
                        NbBundle.getMessage(RemoteHostActionPerformer.class, "ActiveHost.customize")); // NOI18N
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(1, 0, 0, 0,
                        UIManager.getColor("controlDkShadow")), defaultBorder)); //NOI18N
            } else {
                assert value == null;
                label = null;
                setBorder(defaultBorder);
            }

            setText(label);
            setIcon(null);

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            return this;
        }

        // #89393: GTK needs name to render cell renderer "natively"
        @Override
        public String getName() {
            String name = super.getName();
            return name == null ? "ComboBox.renderer" : name;  // NOI18N
        }
    }

}
