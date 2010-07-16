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

package org.netbeans.modules.soa.ldap.browser;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.EventListener;
import java.util.EventObject;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.border.Border;
import javax.swing.event.EventListenerList;
import org.openide.util.NbBundle;

/**
 *
 * @author anjeleevich
 */
public class FilterPanel extends JToolBar {

    private FilterLabel filterLabel;
    private FilterComboBox filterTypeComboBox;
    private FilterTextField filterQueryTextField;
    private JButton applyButton;

    private ApplyFilterActionListener applyFilterActionListener
            = new ApplyFilterActionListener();

    private String filter = "";

    private EventListenerList filterPanelListenersList
            = new EventListenerList();

    public FilterPanel() {
        this(false);
    }

    public FilterPanel(boolean compact) {
        setFloatable(false);
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setBorder((compact) ? COMPACT_BORDER : BORDER);

        int hgap = (compact) ? 2 : 4;

        if (!compact) {
            filterLabel = new FilterLabel(NbBundle.getMessage(getClass(),
                    "FilterLabel")); // NOI18N
        }

        filterTypeComboBox = new FilterComboBox(FilterType.values());
        filterQueryTextField = new FilterTextField();

        filterTypeComboBox.setEditable(false);
        filterTypeComboBox.setPreferredHeightProviders(filterTypeComboBox,
                filterQueryTextField);

        filterQueryTextField.setPreferredHeightProviders(filterTypeComboBox,
                filterQueryTextField);
        filterQueryTextField.addActionListener(applyFilterActionListener);

        applyButton = new JButton();
        applyButton.addActionListener(applyFilterActionListener);

        if (!compact) {
            add(filterLabel);
            add(Box.createHorizontalStrut(hgap)).setFocusable(false);
        }
        
        add(filterTypeComboBox);
        add(Box.createHorizontalStrut(hgap)).setFocusable(false);
        add(filterQueryTextField);
        add(Box.createHorizontalStrut(hgap)).setFocusable(false);
        add(applyButton);

        applyButton.setIcon(IconPool.loadImageIcon("apply"));
        if (!compact) {
            applyButton.setText(NbBundle.getMessage(getClass(),
                "ApplyFilterButtonName")); // NOI18N
        }
    }

    public void addLDAPFilterListener(Listener listener) {
        filterPanelListenersList.add(Listener.class, listener);
    }

    public void removeLDAPFilterListener(Listener listener) {
        filterPanelListenersList.remove(Listener.class, listener);
    }


    public String getFilter() {
        return filter;
    }

    private void updateFilter() {
        String oldFilter = this.filter;
        String newFilter = calculateFilter();

        boolean equalFilters = (oldFilter == null)
                ? (newFilter == null)
                : oldFilter.equals(newFilter);

        if (!equalFilters) {
            this.filter = newFilter;
            Listener[] listeners = filterPanelListenersList
                    .getListeners(Listener.class);
            if (listeners != null && listeners.length > 0) {
                Event event = new Event(this, filter);
                for (int i = listeners.length - 1; i >= 0; i--) {
                    listeners[i].ldapFilterChanged(event);
                }
            }
        }
    }

    private String calculateFilter() {
        String s = filterQueryTextField.getText();
        if (s == null || s.trim().length() == 0) {
            return ""; // NOI18N
        }

        Object filterType = filterTypeComboBox.getSelectedItem();
        if (filterType == FilterType.LDAP_FILTER) {
            return s;
        }

        if (filterType == FilterType.USERS) {
            return ("*".equals(s)) // NOI18N
                    ? ALL_USERS_FILTER
                    : MessageFormat.format(USERS_FILTER, s);
        }

        if (filterType == FilterType.GROUPS) {
            return ("*".equals(s)) // NOI18N
                    ? ALL_GROUPS_FILTER
                    : MessageFormat.format(GROUPS_FILTER, s);
        }

        String attributeName = ((FilterType) filterType).getAttributeName();

        return MessageFormat.format(ATTR_FILTER, attributeName, s);
    }

    private static class FilterLabel extends JLabel {
        public FilterLabel(String text) {
            super(text);
        }

        @Override
        public Font getFont() {
            Font font = super.getFont();
            return (font == null) ? null : font.deriveFont(Font.BOLD);
        }
    }

    private static class FilterComboBox extends JComboBox implements 
            PreferredHeightProvider
    {
        private PreferredHeightProvider[] preferredHeightProviders;

        public FilterComboBox(Object[] items) {
            super(items);
        }

        public void setPreferredHeightProviders(PreferredHeightProvider...
                preferredHeightProviders)
        {
            this.preferredHeightProviders = preferredHeightProviders;
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension size = super.getPreferredSize();

            if (preferredHeightProviders != null) {
                for (PreferredHeightProvider provider
                        : preferredHeightProviders)
                {
                    if (provider != this) {
                        size.height = Math.max(size.height,
                                provider.getPreferredHieght());
                    }
                }
            }

            return size;
        }

        @Override
        public Dimension getMaximumSize() {
            return getPreferredSize();
        }

        @Override
        public Dimension getMinimumSize() {
            return getPreferredSize();
        }

        public int getPreferredHieght() {
            return super.getPreferredSize().height;
        }
    }

    private static class FilterTextField extends JTextField implements 
            PreferredHeightProvider
    {
        private PreferredHeightProvider[] preferredHeightProviders;

        public void setPreferredHeightProviders(PreferredHeightProvider...
                preferredHeightProviders)
        {
            this.preferredHeightProviders = preferredHeightProviders;
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension size = super.getPreferredSize();

            if (preferredHeightProviders != null) {
                for (PreferredHeightProvider provider
                        : preferredHeightProviders)
                {
                    if (provider != this) {
                        size.height = Math.max(size.height,
                                provider.getPreferredHieght());
                    }
                }
            }

            return size;
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
            size.width = Math.min(size.width, 100);
            return size;
        }

        public int getPreferredHieght() {
            return super.getPreferredSize().height;
        }
    }

    private static interface PreferredHeightProvider {
        int getPreferredHieght();
    }

    private static final Border BORDER = new Border() {
        public void paintBorder(Component c, Graphics g, int x, int y,
                int width, int height)
        {
            Color oldColor = g.getColor();

            g.setColor(c.getBackground().darker());
            g.drawLine(x, y + height - 1, x + width - 1, y + height - 1);
            g.setColor(oldColor);
        }

        public Insets getBorderInsets(Component c) {
            return new Insets(4, 4, 5, 4);
        }

        public boolean isBorderOpaque() {
            return false;
        }
    };

    private static final Border COMPACT_BORDER = new Border() {
        public void paintBorder(Component c, Graphics g, int x, int y, 
                int width, int height)
        {
            Color oldColor = g.getColor();
            Color background = c.getBackground();

            g.setColor(background.brighter());
            g.drawLine(x, y, x + width - 1, y);
            g.drawLine(x, y + 1, x, y + height - 2);

            g.setColor(background.darker());
            g.drawLine(x, y + height - 1, x + width - 1, y + height - 1);

            g.setColor(oldColor);
        }

        public Insets getBorderInsets(Component c) {
            return new Insets(2, 2, 2, 2);
        }

        public boolean isBorderOpaque() {
            return false;
        }
    };

    private class ApplyFilterActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            updateFilter();
        }
    }

    public static interface Listener extends EventListener {
        public void ldapFilterChanged(Event event);
    }

    public static class Event extends EventObject {
        private String filter;

        public Event(FilterPanel filterPanel, String filter) {
            super(filterPanel);
            this.filter = filter;
        }

        @Override
        public FilterPanel getSource() {
            return (FilterPanel) super.getSource();
        }

        public String getFilter() {
            return filter;
        }
    }


    public static final String ALL_ENTRIES_FILTER
            = "(|(objectClass=*)(objectClass=ldapsubentry))"; // NOI18N
    
    public static final String ALL_USERS_FILTER
            = "(|(objectClass=person)(objectClass=user))"; // NOI18N
    public static final String USERS_FILTER
            = "(&(|(objectClass=person)(objectClass=user))(|(cn={0})(sn={0})(uid={0})))"; // NOI18N

    public static final String ALL_GROUPS_FILTER
            = "(|(objectClass=groupOfUniqueNames)(objectClass=groupOfURLs)(objectClass=group))"; // NOI18N
    public static final String GROUPS_FILTER
            = "(&(|(objectClass=groupOfUniqueNames)(objectClass=groupOfURLs)(objectClass=group))(cn={0}))"; // NOI18N
    public static final String ATTR_FILTER = "({0}={1})"; // NOI18N
}
