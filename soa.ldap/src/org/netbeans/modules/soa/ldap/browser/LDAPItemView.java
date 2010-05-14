/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.soa.ldap.browser;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.InterruptedNamingException;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.ldap.LdapName;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import org.netbeans.modules.soa.ldap.LDAPConnection;
import org.netbeans.modules.soa.ldap.LDAPUtils;
import org.netbeans.modules.soa.ldap.browser.attributetable.AttributeNode;
import org.netbeans.modules.soa.ldap.browser.attributetable.AttributeTable;
import org.netbeans.modules.soa.ldap.browser.attributetable.AttributeTableModel;
import org.netbeans.modules.soa.ldap.browser.attributetable.AttributeValueCount;
import org.netbeans.modules.soa.ldap.properties.ConnectionProperties;
import org.openide.util.RequestProcessor;

/**
 *
 * @author anjeleevich
 */
public class LDAPItemView extends JPanel {
    private LDAPConnection connection;

    private DNTitleLabel dnTitleLabel;
    private DNValueLabel dnValueLabel;

    private Box header;

    private AttributeTable attributeTable;
    private JScrollPane scrollPane;

    private RequestProcessor requestProcessor = new RequestProcessor(
            "Load LDAP entry attributes", 3, true); // NOI18N
    
    private RequestProcessor.Task loadAttributesTask = null;

    private LdapName ldapName = null;

    private long attributesVersion = 0;

    public LDAPItemView(LDAPConnection connection) {
        this.connection = connection;

        dnTitleLabel = new DNTitleLabel("DN:");
        dnValueLabel = new DNValueLabel();

        header = Box.createHorizontalBox();
        header.setBorder(HEADER_BORDER);
        header.add(dnTitleLabel);
        header.add(Box.createHorizontalStrut(4)).setFocusable(false);
        header.add(dnValueLabel);

        attributeTable = new AttributeTable(EMPTY_ATTRIBUTE_MODEL);

        scrollPane = new JScrollPane(attributeTable);
        scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        scrollPane.getViewport().setBackground(Color.WHITE);

        setLayout(new BorderLayout());
        add(header, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void abortBackgroundTasks() {
        if (loadAttributesTask != null) {
            loadAttributesTask.cancel();
            loadAttributesTask = null;
        }
    }

    public void setLDAPName(LdapName ldapName) {
        if (this.ldapName == ldapName) {
            return;
        }

        if (this.ldapName != null && ldapName != null 
                && this.ldapName.equals(ldapName))
        {
            return;
        }

        attributesVersion++;

        this.ldapName = ldapName;

        attributeTable.setAttributeTableModel(EMPTY_ATTRIBUTE_MODEL);

        if (loadAttributesTask != null) {
            loadAttributesTask.cancel();
            loadAttributesTask = null;
        }

        if (ldapName == null) {
            dnValueLabel.setText("");
        } else {
            dnValueLabel.setText(ldapName.toString());

            loadAttributesTask = requestProcessor.post(
                    new LoadAttributesRunnable(connection,
                    ldapName, attributesVersion));
        }
    }

    private static class DNTitleLabel extends JLabel {
        public DNTitleLabel() {
        }

        public DNTitleLabel(String text) {
            super(text);
        }

        @Override
        public Font getFont() {
            Font font = super.getFont();
            return (font == null) ? null : font.deriveFont(Font.BOLD);
        }

        @Override
        public Dimension getMaximumSize() {
            return getPreferredSize();
        }

        @Override
        public Dimension getMinimumSize() {
            return getPreferredSize();
        }
    }

    private static class DNValueLabel extends JLabel {
        public DNValueLabel() {
        }

        public DNValueLabel(String text) {
            super(text);
        }

        @Override
        public Font getFont() {
            Font font = super.getFont();
            return (font == null) ? null : font.deriveFont(Font.PLAIN);
        }

        @Override
        public Dimension getMaximumSize() {
            Dimension size = getPreferredSize();
            size.width = Integer.MAX_VALUE;
            return size;
        }

        @Override
        public Dimension getMinimumSize() {
            Dimension size = getPreferredSize();
            size.width = Math.min(50, size.width);
            return size;
        }
    }

    private class LoadAttributesRunnable implements Runnable {
        private LDAPConnection connection;
        private ConnectionProperties connectionProperties;
        private LdapName ldapName;
        private LDAPItemView itemView;
        private long attributesVersion;

        LoadAttributesRunnable(LDAPConnection connection, LdapName ldapName, 
                long attributesVersion)
        {
            this.connection = connection;
            this.connectionProperties = connection.getProperties();
            this.ldapName = ldapName;
            this.attributesVersion = attributesVersion;
        }

        public void run() {
            AttributeTableModel tableModel = new AttributeTableModel();

            DirContext dirContext = null;

            SearchControls searchControls = new SearchControls();
            searchControls.setSearchScope(SearchControls.OBJECT_SCOPE);

            NamingEnumeration<? extends Attribute> result = null;
            try {
                dirContext = connectionProperties.createDirContext();
                
                Attributes attributes = dirContext.getAttributes(ldapName);
                result = attributes.getAll();

                while (result.hasMore()) {
                    LDAPUtils.checkInterrupted();
                    
                    Attribute attribute = result.next();
                    String id = attribute.getID();

                    if (id != null) {
                        int valueCount = attribute.size();

                        AttributeNode node = tableModel
                                .addAttributeNode(id,
                                new AttributeValueCount(valueCount));
                        for (int i = 0; i < valueCount; i++) {
                            node.addValue(attribute.get(i));
                        }
                    }
                }
            } catch (InterruptedNamingException ex) {
                return;
            } catch (NameNotFoundException ex) {
                // do nothing
            } catch (NamingException ex) {
                Logger.getLogger(LDAPItemView.class.getName()).log(Level.INFO,
                        ex.getMessage(), ex);
            } catch (InterruptedException ex) {
                
            } finally {
                LDAPUtils.close(result);
                LDAPUtils.close(dirContext);
            }

            tableModel.unfold(5);

            SwingUtilities.invokeLater(
                    new PublishAttributesRunnable(tableModel, attributesVersion));
        }
    }

    private class PublishAttributesRunnable implements Runnable {
        private AttributeTableModel attributeTableModel;
        private long attributesVersion;

        public PublishAttributesRunnable(
                AttributeTableModel attributeTableModel,
                long attributesVersion)
        {
            this.attributeTableModel = attributeTableModel;
            this.attributesVersion = attributesVersion;
        }

        public void run() {
            if (attributesVersion == LDAPItemView.this.attributesVersion) {
                attributeTable.setAttributeTableModel(attributeTableModel);
                loadAttributesTask = null;
            }
        }
    }

    private static final Border HEADER_BORDER = new Border() {
        public void paintBorder(Component c, Graphics g, int x, int y,
                int width, int height)
        {
            int x1 = x + width - 1;
            int y1 = y + height - 1;

            Color oldColor = g.getColor();

            g.setColor(c.getBackground().brighter());
            g.drawLine(x, y, x1, y);
            g.setColor(c.getBackground().darker());
            g.drawLine(x, y1, x1, y1);

            g.setColor(oldColor);
        }

        public Insets getBorderInsets(Component c) {
            return new Insets(5, 4, 5, 4);
        }

        public boolean isBorderOpaque() {
            return false;
        }
    };

    private static final AttributeTableModel EMPTY_ATTRIBUTE_MODEL
            = new AttributeTableModel();

    public static final Border LINE_BORDER = new Border() {
        public void paintBorder(Component c, Graphics g, int x, int y,
                int width, int height)
        {
            Color oldColor = g.getColor();
            g.setColor(c.getBackground().darker());
            g.drawRect(x, y, width - 1, height - 1);
            g.setColor(oldColor);
        }

        public Insets getBorderInsets(Component c) {
            return new Insets(1, 1, 1, 1);
        }

        public boolean isBorderOpaque() {
            return true;
        }
    };
}
