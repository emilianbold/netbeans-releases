/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.soa.ldap.browser.compact;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.text.html.HTMLEditorKit;
import org.netbeans.modules.soa.ldap.LDAP;
import org.netbeans.modules.soa.ldap.LDAPChangeEvent;
import org.netbeans.modules.soa.ldap.LDAPConnection;
import org.netbeans.modules.soa.ldap.LDAPEvent;
import org.netbeans.modules.soa.ldap.LDAPListener;
import org.netbeans.modules.soa.ldap.properties.ConnectionPropertyType;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

/**
 *
 * @author anjeleevich
 */
public class IntroPanel extends JPanel implements ActionListener {
    private JLabel connectionLabel;
    private JComboBox connectionComboBox;

    private JLabel userNameAttributeLabel;
    private JComboBox userNameAttributeComboBox;

    private JLabel groupNameAttributeLabel;
    private JComboBox groupNameAttributeComboBox;

    private JButton connectButton;

    private JEditorPane messageEditorPane;

    private JPanel introPanelContent;

    private Object lastSelectedUserNameAttribute;
    private Object lastSelectedGroupNameAttribute;

    public IntroPanel(ActionListener connectButtonActionListener) {
        connectionComboBox = new IntroComboBox();
        connectionComboBox.setModel(new ConnectionComboBoxModel());
        connectionComboBox.addActionListener(this);
        connectionLabel = new IntroLabel("connectionLabel", // NOI18N
                connectionComboBox, false);

        messageEditorPane = new IntroEditorPane();

        userNameAttributeComboBox = new IntroComboBox();
        userNameAttributeComboBox.setModel(new UserNameAttributeModel());
        userNameAttributeComboBox.addActionListener(this);
        
        userNameAttributeLabel = new IntroLabel("userNameAttributeLabel", // NOI18N
                userNameAttributeComboBox, true);

        groupNameAttributeComboBox = new IntroComboBox();
        groupNameAttributeComboBox.setModel(new GroupNameAttributeModel());
        groupNameAttributeComboBox.addActionListener(this);
        groupNameAttributeLabel = new IntroLabel(
                "groupNameAttributeLabel",  // NOI18N
                groupNameAttributeComboBox, true);

        connectButton = new JButton(getMessage("connectButton")); // NOI18N
        connectButton.setAlignmentX(0.5f);
        connectButton.addActionListener(connectButtonActionListener);

        introPanelContent = new JPanel();
        introPanelContent.setLayout(new BoxLayout(introPanelContent,
                BoxLayout.Y_AXIS));
        
        introPanelContent.setBorder(new EmptyBorder(32, 32, 32, 32));
        introPanelContent.add(connectionLabel);
        introPanelContent.add(connectionComboBox);
        introPanelContent.add(messageEditorPane);
        introPanelContent.add(userNameAttributeLabel);
        introPanelContent.add(userNameAttributeComboBox);
        introPanelContent.add(groupNameAttributeLabel);
        introPanelContent.add(groupNameAttributeComboBox);
        introPanelContent.add(Box.createVerticalStrut(16)).setFocusable(false);
        introPanelContent.add(connectButton);

        add(introPanelContent);

        lastSelectedGroupNameAttribute = groupNameAttributeComboBox
                .getSelectedItem();
        lastSelectedUserNameAttribute = userNameAttributeComboBox
                .getSelectedItem();

        updateState();
    }

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source == userNameAttributeComboBox) {
            Object selectedItem = userNameAttributeComboBox.getSelectedItem();
            if (selectedItem instanceof OtherAttribute) {
                if (!requestAttributeName((OtherAttribute) selectedItem)) {
                    userNameAttributeComboBox
                            .setSelectedItem(lastSelectedUserNameAttribute);
                }
            }
            lastSelectedUserNameAttribute = userNameAttributeComboBox
                    .getSelectedItem();
            updateState();
        } else if (source == groupNameAttributeComboBox) {
            Object selectedItem = groupNameAttributeComboBox.getSelectedItem();
            if (selectedItem instanceof OtherAttribute) {
                if (!requestAttributeName((OtherAttribute) selectedItem)) {
                    groupNameAttributeComboBox
                            .setSelectedItem(lastSelectedGroupNameAttribute);
                }
            } 
            lastSelectedGroupNameAttribute = groupNameAttributeComboBox
                    .getSelectedItem();
            updateState();
        } else if (source == connectionComboBox) {
            updateState();
        }
    }

    public String getUserNameAttribute() {
        return getAttributeName(userNameAttributeComboBox);
    }

    public String getGroupNameAttribute() {
        return getAttributeName(groupNameAttributeComboBox);
    }

    public LDAPConnection getLDAPConnection() {
        return (LDAPConnection) connectionComboBox.getSelectedItem();
    }

    private String getAttributeName(JComboBox attributeComboBox) {
        Object selectedItem = attributeComboBox.getSelectedItem();
        String result = null;
        if (selectedItem instanceof String) {
            result = (String) selectedItem;
        } else if (selectedItem instanceof OtherAttribute) {
            result = ((OtherAttribute) selectedItem).getAttributeName();
        }

        return (result != null && result.length() > 0) ? result : null;
    }

    public void updateState() {
        boolean enabled = (getLDAPConnection() != null)
                && (getUserNameAttribute() != null)
                && (getGroupNameAttribute() != null);

        connectButton.setEnabled(enabled);
    }

    private boolean requestAttributeName(OtherAttribute otherAttribute) {
        NotifyDescriptor.InputLine inputLine = new NotifyDescriptor
                .InputLine(NbBundle.getMessage(IntroPanel.class,
                        "RequestAttributeNameDialog.text"), // NOI18N
                NbBundle.getMessage(IntroPanel.class, 
                        "RequestAttributeNameDialog.title")); // NOI18N

        if (otherAttribute.getAttributeName() != null) {
            inputLine.setInputText(otherAttribute.getAttributeName());
        }

        if (DialogDisplayer.getDefault().notify(inputLine)
                == NotifyDescriptor.OK_OPTION)
        {
            String newAttributeName = inputLine.getInputText();
            if (newAttributeName == null) {
                newAttributeName = ""; // NOI18N
            } else {
                newAttributeName = newAttributeName.trim();
            }

            if (newAttributeName.length() > 0) {
                otherAttribute.setAttributeName(newAttributeName);
                return true;
            }
        }

        String attribute = otherAttribute.getAttributeName();
        return (attribute != null) && (attribute.trim().length() > 0);
    }

    @Override
    public void doLayout() {
        synchronized (getTreeLock()) {
            Dimension size = introPanelContent.getPreferredSize();

            int w = getWidth();
            int h = getHeight();

            int x = 1 + Math.max(0, w - 1 - size.width >> 1);
            int y = 1 + Math.max(0, h - 1 - size.height >> 1);

            introPanelContent.setBounds(x, y, Math.min(w, size.width),
                    size.height);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        synchronized (getTreeLock()) {
            return introPanelContent.getPreferredSize();
        }
    }

    private static String getMessage(String key) {
        return NbBundle.getMessage(IntroPanel.class, "IntroPanel." // NOI18N
                + key + ".text"); // NOI18N
    }

    private class IntroEditorPane extends JEditorPane {
        IntroEditorPane() {
            setContentType("text/html");
            setEditorKit(new HTMLEditorKit());
            setText(getMessage("messageEditorPane")); // NOI18N
            setBorder(new EmptyBorder(4, 4, 4, 4));
            putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
            setFont(new JLabel().getFont());
            setEditable(false);
            setOpaque(false);
            setCaretPosition(0);
            setFocusable(false);
        }

        @Override
        public Dimension getMaximumSize() {
            Dimension size = getPreferredSize();
            size.width = Integer.MAX_VALUE;
            return size;
        }
    }

    private class IntroComboBox extends JComboBox {
        IntroComboBox() {
            setEditable(false);
        }

        @Override
        public Dimension getMaximumSize() {
            Dimension size = getPreferredSize();
            size.width = Integer.MAX_VALUE;
            return size;
        }
    }

    private class IntroLabel extends JLabel {
        IntroLabel(String key, JComponent labelFor, boolean addTopInset) {
            setBorder(new EmptyBorder((addTopInset) ? 8 : 0, 4, 4, 4));
            setOpaque(true);
            setText(getMessage(key));
            setAlignmentX(0.5f);
            setLabelFor(labelFor);
        }

        @Override
        public Dimension getMaximumSize() {
            Dimension size = getPreferredSize();
            size.width = Integer.MAX_VALUE;
            return size;
        }
    }

    private class OtherAttribute {
        private DefaultComboBoxModel model;
        private String attributeName;

        public OtherAttribute(DefaultComboBoxModel model) {
            this.model = model;
            this.attributeName = null;
        }

        @Override
        public String toString() {
            return (attributeName == null || attributeName.length() == 0)
                    ? NbBundle.getMessage(IntroPanel.class, 
                            "OtherAttribute.undefined") // NOI18N
                    : NbBundle.getMessage(IntroPanel.class, 
                            "OtherAttribute.defined", attributeName); // NOI18N
        }

        public String getAttributeName() {
            return attributeName;
        }

        public void setAttributeName(String attributeName) {
            String oldAttributeName = (this.attributeName == null)
                    ? "" // NOI18N
                    : this.attributeName.trim();
            String newAttributeName = (attributeName == null)
                    ? "" // NOI18N
                    : attributeName.trim();

            if (oldAttributeName.equals(newAttributeName)) {
                return;
            }

            this.attributeName = newAttributeName;

            int index = model.getIndexOf(this);
            if (index >= 0) {
                ListDataListener[] listeners = model.getListDataListeners();
                if (listeners != null && listeners.length > 0) {
                    ListDataEvent event = new ListDataEvent(model,
                            ListDataEvent.CONTENTS_CHANGED, index, index);
                    for (int i = listeners.length - 1; i >= 0; i--) {
                        listeners[i].contentsChanged(event);
                    }
                }
            }
        }
    }

    private class UserNameAttributeModel extends DefaultComboBoxModel {
        private OtherAttribute otherAttribute;

        public UserNameAttributeModel() {
            this.otherAttribute = new OtherAttribute(this);

            addElement("cn"); // NOI18N
            addElement("givenName"); // NOI18N
            addElement("uid"); // NOI18N
            addElement("distinguishedName"); // NOI18N
            addElement(otherAttribute);

            setSelectedItem("cn"); // NOI18N
        }
    }

    private class GroupNameAttributeModel extends DefaultComboBoxModel {
        private OtherAttribute otherAttribute;

        public GroupNameAttributeModel() {
            this.otherAttribute = new OtherAttribute(this);

            addElement("cn"); // NOI18N
            addElement("distinguishedName"); // NOI18N
            addElement(otherAttribute);

            setSelectedItem("cn"); // NOI18N
        }
    }

    private class ConnectionComboBoxModel extends DefaultComboBoxModel
            implements LDAPListener
    {
        public ConnectionComboBoxModel() {
            List<LDAPConnection> ldapConnections = LDAP.INSTANCE
                    .getConnections();

            if (ldapConnections != null) {
                for (LDAPConnection connection : ldapConnections) {
                    addElement(connection);
                }
            }

            LDAP.INSTANCE.addLDAPChangeListener(WeakListeners
                    .create(LDAPListener.class, this, null));
        }

        public void ldapConnectionRemoved(LDAPEvent event) {
            LDAPConnection connection = event.getLDAPConnection();
            removeElement(connection);
        }

        public void ldapConnectionChanged(LDAPChangeEvent event) {
            LDAPConnection connection = event.getLDAPConnection();

            int index = getIndexOf(connection);
            fireContentsChanged(this, index, index);
        }

        public void ldapConnectionAdded(LDAPEvent event) {
            LDAPConnection connection = event.getLDAPConnection();
            addElement(connection);
        }
    }
}
