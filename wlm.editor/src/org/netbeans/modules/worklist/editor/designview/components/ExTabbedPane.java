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
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.worklist.editor.designview.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author anjeleevich
 */
public class ExTabbedPane extends JPanel {
    
    private List<Tab> tabs = new ArrayList<Tab>(6);
    private Tab activeTab;
    
    private TabbedPaneHeader header;
    private TabbedPaneContent content;
    
    private Set<JComponent> components = new HashSet<JComponent>();

    private final List<ChangeListener> changeListeners
            = new ArrayList<ChangeListener>();
    
    public ExTabbedPane() {
        header = new TabbedPaneHeader();
        content = new TabbedPaneContent();
        
        setLayout(new BorderLayout());
        add(header, BorderLayout.NORTH);
        add(content, BorderLayout.CENTER);
        
        setFocusable(true);
        
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0),
                "prev-tab");

        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0),
                "next-tab");

        getActionMap().put("prev-tab", new AbstractAction() { // NOI18N
            public void actionPerformed(ActionEvent event) {
                Tab activeTab = getActiveTab();
                if (activeTab != null) {
                    Tab prev = activeTab.getPrevious();
                    if (prev != null) {
                        prev.setActive();
                    }
                }
            }
        });

        getActionMap().put("next-tab", new AbstractAction() { // NOI18N
            public void actionPerformed(ActionEvent event) {
                Tab activeTab = getActiveTab();
                if (activeTab != null) {
                    Tab next = activeTab.getNext();
                    if (next != null) {
                        next.setActive();
                    }
                }
            }
        });
        
        addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
                activeTab = getActiveTab();
                if (activeTab != null) {
                    activeTab.repaint();
                }
            }
            
            public void focusLost(FocusEvent e) {
                activeTab = getActiveTab();
                if (activeTab != null) {
                    activeTab.repaint();
                }
            }
        });
    }

    public void addChangeListener(ChangeListener changeListener) {
        synchronized (changeListeners) {
            changeListeners.add(changeListener);
        }
    }

    public void removeChangeListener(ChangeListener changeListener) {
        synchronized (changeListeners) {
            int index = changeListeners.lastIndexOf(changeListener);
            if (index >= 0) {
                changeListeners.remove(index);
            }
        }
    }

    protected void fireChangeEvent() {
        ChangeListener[] listeners;

        synchronized (changeListeners) {
            listeners = changeListeners.toArray(
                    new ChangeListener[changeListeners.size()]);
        }

        if (listeners.length > 0) {
            ChangeEvent event = new ChangeEvent(this);
            for (int i = listeners.length - 1; i >= 0; i--) {
                listeners[i].stateChanged(event);
            }
        }
    }

    public int getTabCount() {
        return tabs.size();
    }
    
    public Tab getTab(int index) {
        return tabs.get(index);
    }
    
    public Tab getActiveTab() {
        return activeTab;
    }
    
    public Tab getNextTab(Tab tab) {
        if (tab == null) {
            return tab;
        }
        
        int i = tabs.indexOf(tab);
        return (i >= 0 && i + 1 < tabs.size()) 
            ? tabs.get(i + 1)
            : null;
    }
    
    public Tab getPreviousTab(Tab tab) {
        if (tab == null) {
            return null;
        }
        
        int i = tabs.indexOf(tab);
        return (i > 0) ? tabs.get(i - 1) : null;
    }
    
    public Tab addTab(String name, JComponent centerContent,
            boolean wrapWithScrollPane)
    {
        return addTab(name, centerContent, wrapWithScrollPane, null, null);
    }
    
    public Tab addTab(String name, 
            JComponent centerContent, boolean wrapWithScrollPane, 
            JComponent rightContent, 
            JComponent bottomContent) 
    {
        boolean firstTab = tabs.isEmpty();
        
        Tab tab = new Tab(name, centerContent, wrapWithScrollPane,
                rightContent, bottomContent);
        
        header.add(tab);
        tabs.add(tab);
        
        JComponent centerComponent = tab.getCenterComponent();
        centerComponent.setVisible(false);
        
        content.add(centerComponent);

        components.add(centerComponent);
        
        if (rightContent != null) {
            rightContent.setVisible(firstTab);
            if (components.add(rightContent)) {
                content.add(rightContent);
            }
        }
        
        if (bottomContent != null) {
            bottomContent.setVisible(firstTab);
            if (components.add(bottomContent)) {
                content.add(bottomContent);
            }
        }
        
        if (firstTab) {
            tab.setActive();
        }
        
        // vlv: print
        setPrintable(centerContent);
        setPrintable(rightContent);
        setPrintable(bottomContent);

        return tab;
    }

    private void setPrintable(JComponent component) {
        if (component != null) {
          component.putClientProperty("print.printable", Boolean.TRUE); // NOI18N
        }
    }

    public void activateTab(JComponent centerContent) {
        for (Tab tab : tabs) {
            if (tab.getCenterContent() == centerContent) {
                activateTab(tab);
                return;
            }
        }
    }
    
    private void activateTab(Tab newTab) {
        Tab oldTab = activeTab;
        
        if (oldTab != newTab) {
            if (oldTab != null) {
                setVisible(oldTab.getBottomContent(), false);
                setVisible(oldTab.getRightContent(), false);
                setVisible(oldTab.getCenterComponent(), false);
                oldTab.setSelected(false);
            }
            
            setVisible(newTab.getBottomContent(), true);
            setVisible(newTab.getRightContent(), true);
            setVisible(newTab.getCenterComponent(), true);
            
            newTab.setSelected(true);
            header.setComponentZOrder(newTab, 0);
            
            content.revalidate();
            content.repaint();
            
            activeTab = newTab;

            fireChangeEvent();
        }
    }
    
    public Color getTabSeparatorColor() {
        return TAB_SEPARATOR_COLOR;
    }
    
    public Color getHeaderTopColor() {
        return TOP_GRADIENT_COLOR;
    }
    
    public Color getHeaderBottomColor() {
        return BOTTOM_GRADIENT_COLOR;
    }
    
    public Color getActiveTabBackground() {
        return ACTIVE_TAB_BACKGROUND;
    }
    
    public Color getTabBorderColor() { 
        return TAB_BORDER_COLOR;
    }
    
    public Color getTabRolloverBackground() {
        return TAB_ROLLOVER_BACKGROUND;
    }
    
    private class TabbedPaneContent extends JPanel {
        TabbedPaneContent() {
            setBackground(getActiveTabBackground());
        }
        
        @Override 
        public void doLayout() {
            synchronized (getTreeLock()) {
                Tab tab = getActiveTab();
                if (tab == null) return;

                Insets insets = getInsets();

                int x1 = insets.left;
                int y1 = insets.top;

                int x2 = getWidth() - insets.right;
                int y2 = getHeight() - insets.bottom;

                JComponent rightContent = tab.getRightContent();
                JComponent bottomContent = tab.getBottomContent();
                JComponent centerComponent = tab.getCenterComponent();

                if (bottomContent != null) {
                    bottomContent.setVisible(true);
                    Dimension size = bottomContent.getPreferredSize();
                    int h = Math.min(y2 - y1 - 100, size.height);
                    y2 -= h;
                    bottomContent.setBounds(x1, y2, x2 - x1, h);
                }

                if (rightContent != null) {
                    rightContent.setVisible(true);
                    Dimension size = rightContent.getPreferredSize();
                    int w = Math.min(x2 - x1 - 100, size.width);
                    x2 -= w;
                    rightContent.setBounds(x2, y1, w, y2 - y1);
                }

                centerComponent.setBounds(x1, y1, x2 - x1, y2 - y1);
            }
        }
        
        @Override
        public Dimension getPreferredSize() {
            return new Dimension(100, 100);
        }
        
        @Override
        public Dimension getMinimumSize() {
            return new Dimension(100, 100);
        }
    }
    
    private class TabbedPaneHeader extends JPanel {
        public TabbedPaneHeader() {
            setOpaque(true);
            setFocusable(true);
        }
        
        @Override 
        public Insets getInsets() {
            return new Insets(3, 3, 4, 3);
        }
        
        @Override
        public void doLayout() {
            synchronized (getTreeLock()) {
                Insets insets = getInsets();
                
                int x = insets.left;
                int y = insets.top;
                int w = getWidth() - x - insets.right;
                int h = getHeight() - y - insets.bottom;

                if (!tabs.isEmpty()) {
                    int tabCount = getTabCount();
                    
                    int overlappingPixels = Math.max(0, tabCount - 1);
                    int preferredTabWidth = 0;
                    int minimumTabWidth = 0;

                    for (Tab tab : tabs) {
                        Dimension preferredTabSize = tab.getPreferredSize();
                        Dimension minimumTabSize = tab.getMinimumSize();

                        preferredTabWidth = Math.max(preferredTabWidth, 
                                preferredTabSize.width);
                        minimumTabWidth = Math.max(minimumTabWidth, 
                                minimumTabSize.width);
                    }
                    
                    int tabsWidth = Math.max(
                            minimumTabWidth * tabCount,
                            Math.min(preferredTabWidth * tabCount, 
                                    w + overlappingPixels));
                    
                    for (Tab tab : tabs) {
                        int tabWidth = (tabsWidth + tabCount / 2) / tabCount;
                        tab.setBounds(x, y, tabWidth, h);
                        tabsWidth -= tabWidth;
                        x += tabWidth - 1;
                        tabCount--;
                    }
                }
            }
        }
        
        @Override
        public Dimension getPreferredSize() {
            synchronized (getTreeLock()) {
                int w = 0;
                int h = 0;

                if (!tabs.isEmpty()) {
                    int tabWidth = 0;
                    int tabHeight = 0;

                    for (Tab tab : tabs) {
                        Dimension size = tab.getPreferredSize();

                        tabWidth = Math.max(tabWidth, size.width);
                        tabHeight = Math.max(tabHeight, size.height);
                    }

                    int tabCount = tabs.size();

                    w = tabWidth * tabCount - Math.max(0, tabCount - 1);
                    h = tabHeight;
                }

                Insets insets = getInsets();

                w += insets.left + insets.right;
                h += insets.top + insets.bottom;
                
                return new Dimension(w, h);
            }
        }
        
        @Override
        public Dimension getMinimumSize() {
            synchronized (getTreeLock()) {
                int w = 0;
                int h = 0;

                if (!tabs.isEmpty()) {
                    int tabWidth = 0;
                    int tabHeight = 0;

                    for (Tab tab : tabs) {
                        Dimension size = tab.getMinimumSize();

                        tabWidth = Math.max(tabWidth, size.width);
                        tabHeight = Math.max(tabHeight, size.height);
                    }

                    int tabCount = tabs.size();

                    w = tabWidth * tabCount - Math.max(0, tabCount - 1);
                    h = tabHeight;
                }

                Insets insets = getInsets();

                w += insets.left + insets.right;
                h += insets.top + insets.bottom;

                return new Dimension(w, h);
            }
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            int y1 = 0;
            int y3 = getHeight();
            int y2 = y3 - getInsets().bottom;
            
            int w = getWidth();
            
            Graphics2D g2 = (Graphics2D) g;
            
            Color c1 = getHeaderTopColor();
            Color c2 = getHeaderBottomColor();
            
            g2.setPaint(new GradientPaint(0, y1, c1, 0, y2, c2));
            g2.fillRect(0, y1, w, y2 - y1);
            
            g2.setPaint(getActiveTabBackground());
            g2.fillRect(0, y2, w, y3 - y2);
            
            g2.setPaint(getTabBorderColor());
            g2.drawLine(0, y2 - 1, w - 1, y2 - 1);
        }
        
        @Override 
        protected void paintBorder(Graphics g) {
            int y = getHeight() - 1;
            g.setColor(getTabBorderColor().brighter());
            g.drawLine(0, y, getWidth() - 1, y);
        }
    }
    
    public class Tab extends JToggleButton implements ActionListener {
        private String name;
        private List<HeaderRow> headerRows = new ArrayList<HeaderRow>(3);
        private JComponent rightContent;
        private JComponent bottomContent;
        private JComponent centerContent;
        private ExScrollPane scrollPane;
        
        private Tab(String name, JComponent centerContent, 
                boolean wrapWithScrollPane, 
                JComponent rightContent, 
                JComponent bottomContent) 
        {
            setSelected(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setBorder(null);
            setRolloverEnabled(true);
            setOpaque(false);
            setFocusable(false);
            setFocusPainted(false);
            
            this.name = name;
            this.centerContent = centerContent;
            this.rightContent = rightContent;
            this.bottomContent = bottomContent;
            
            if (wrapWithScrollPane) {
                scrollPane = new ExScrollPane(centerContent);
                
                Color c = getActiveTabBackground();
                
                scrollPane.setBackground(c);
                scrollPane.getJViewport().setBackground(c);
            } 
            
            addActionListener(this);
        }
        
        public int getHeaderRowCount() {
            return headerRows.size();
        }
        
        public HeaderRow getHeaderRow(int index) {
            return headerRows.get(index);
        }
        
        public void setActive() {
            ExTabbedPane.this.activateTab(this);
        }
        
        public boolean isActive() {
            return ExTabbedPane.this.getActiveTab() == this;
        }
        
        public Tab getNext() {
            return ExTabbedPane.this.getNextTab(this);
        }
        
        public Tab getPrevious() {
            return ExTabbedPane.this.getPreviousTab(this);
        }
        
        public JComponent getCenterComponent() {
            return (scrollPane != null) 
                    ? scrollPane 
                    : centerContent;
        }
        
        public JComponent getCenterContent() {
            return centerContent;
        }
        
        public JComponent getRightContent() {
            return rightContent;
        }
        
        public JComponent getBottomContent() {
            return bottomContent;
        }
        
        public HeaderRow addHeaderRow(String text, String count, 
                StyledLabel.Style style) 
        {
            HeaderRow headerRow = new HeaderRow(this, text, count, style);
            headerRows.add(headerRow);
            add(headerRow);
            return headerRow;
        }
        
        @Override
        public Insets getInsets() {
            return new Insets(12, 12, 12, 12);
        }
        
        @Override
        public Dimension getPreferredSize() {
            synchronized (getTreeLock()) {
                int w = 0;
                int h = 0;
            
                for (HeaderRow row : headerRows) {
                    Dimension size = row.getPreferredSize();
                    w = Math.max(w, size.width);
                    h += size.height;
                }
                
                Insets insets = getInsets();
                
                w = Math.max(w, 150);
                
                w += insets.left + insets.right;
                h += insets.top + insets.bottom;
                
                return new Dimension(w, h);
            }
        }
        
        @Override
        public Dimension getMinimumSize() {
            synchronized (getTreeLock()) {
                int w = 0;
                int h = 0;
            
                for (HeaderRow row : headerRows) {
                    Dimension size = row.getMinimumSize();
                    w = Math.max(w, size.width);
                    h += size.height;
                }
                
                Insets insets = getInsets();
                
                w += insets.left + insets.right;
                h += insets.top + insets.bottom;
                
                return new Dimension(w, h);
            }
        }
        
        @Override
        public void doLayout() {
            synchronized (getTreeLock()) {
                Insets insets = getInsets();
                
                int x = insets.left;
                int y = insets.top;
                
                int w = getWidth() - x - insets.right;
                int h = getHeight() - y - insets.bottom;
                
                int rowsHeight = 0;
                int rowsWidth = 0;
                
                Dimension preferredSize = getPreferredSize();
                
                preferredSize.width -= x + insets.right;
                preferredSize.height -= y + insets.bottom;
                
                y += (h - preferredSize.height) / 2;
                
                for (HeaderRow row : headerRows) {
                    Dimension rowPreferredSize = row.getPreferredSize();

                    int rowWidth;
                    int rowHeight = rowPreferredSize.height;
                    
                    if (w < rowPreferredSize.width) {
                        int rowMinimumWidth = row.getMinimumSize().width;
                        rowWidth = Math.max(rowMinimumWidth, w);
                    } else {
                        rowWidth = rowPreferredSize.width;
                    }

                    int rowX = x + (w - rowWidth) / 2;
                    
                    row.setBounds(rowX, y, rowWidth, rowHeight);
                    
                    y += rowHeight;
                }
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            int w = getWidth();
            int h = getHeight();
            
            if (isActive()) {
                Graphics2D g2 = ExUtils.prepareG2(g, true);
                int d = TAB_CORNER_DIAMETR - 1;
                
                g2.setPaint(getActiveTabBackground());
                g2.fillRoundRect(0, 0, w - 1, h + d, d, d);
                
                ExUtils.disposeG2(g2, true);
            } else if (getModel().isRollover()) {
                Graphics2D g2 = ExUtils.prepareG2(g, false);
                
                int d = TAB_CORNER_DIAMETR;
                
                g2.setPaint(getTabRolloverBackground());
                g2.fillRoundRect(0, 0, w, h + d, d, d);
                
                ExUtils.disposeG2(g2, false);
            }
            
            if (isActive() && ExTabbedPane.this.hasFocus()) {
                g.setColor(new Color(0xA3B8CC));
                g.drawRect(4, 4, w - 9, h - 9);
            }
        }
        
        @Override
        protected void paintBorder(Graphics g) {
            int w = getWidth();
            int h = getHeight();
            
            if (isActive()) {
                Graphics2D g2 = ExUtils.prepareG2(g, true);
                int d = TAB_CORNER_DIAMETR - 1;
                
                g2.setPaint(getTabBorderColor());
                g2.drawRoundRect(0, 0, w - 1, h + d, d, d);
                
                ExUtils.disposeG2(g2, true);
            } else {
                int o = TAB_CORNER_DIAMETR / 2;
                int x1 = 0;
                int x2 = w - 1;
                
                int y1 = o;
                int y2 = h - o - getParent().getInsets().top;
                
                g.setColor(getTabSeparatorColor());
                g.drawLine(x1, y1, x1, y2);
                g.drawLine(x2, y1, x2, y2);
                g.setColor(getTabBorderColor());
                g.drawLine(0, h - 1, w - 1, h - 1);
            }
        }

        public void actionPerformed(ActionEvent e) {
            setSelected(isActive());
            setActive();
        }
    }
    
    public static class HeaderRow extends JComponent{
        private StyledLabel textLabel;
        private StyledLabel countLabel;
        
        private Tab tab;
        private String text;
        private String count;
        private int hgap = 4;
        
        private HeaderRow(Tab tab, String text, String count, 
                StyledLabel.Style style) 
        {
            setOpaque(false);
            
            this.tab = tab;
            this.text = text;
            this.count = count;
            
            textLabel = new StyledLabel(style);
            textLabel.setText(text);
            textLabel.setOpaque(false);
            
            countLabel = new StyledLabel(style);
            countLabel.setOpaque(false);

            if (count != null) {
                countLabel.setText(count); 
                countLabel.setVisible(true);
            } else {
                countLabel.setVisible(false);
            }
            
            add(countLabel);
            add(textLabel);
        }
        
        public Tab getTab() {
            return tab;
        }
        
        public String getText() {
            return text;
        }
        
        public void setText(String text) {
            this.text = text;
            textLabel.setText(text);
        }
        
        public String getCount() {
            return count;
        }
        
        public void setStyle(StyledLabel.Style style) {
            textLabel.setStyle(style);
            countLabel.setStyle(style);
        }
        
        public void setCount(String count) {
            this.count = count;
            
            if (count != null) {
                countLabel.setText(count);
                countLabel.setVisible(true);
            } else {
                countLabel.setVisible(false);
            }
        }
        
        @Override
        public Dimension getMinimumSize() {
            synchronized (getTreeLock()) {
                Dimension size = textLabel.getMinimumSize();
                size.width = Math.min(10, size.width);

                if (countLabel.isVisible()) {
                    Dimension countSize = countLabel.getPreferredSize();
                    size.width += hgap + countSize.width;
                    size.height = Math.max(size.height, countSize.height);
                }
                
                Insets insets = getInsets();
                
                size.width += insets.left + insets.right;
                size.height += insets.top + insets.bottom;

                return size;
            }
        }
        
        @Override
        public Dimension getPreferredSize() {
            synchronized (getTreeLock()) {
                Dimension size = textLabel.getPreferredSize();

                if (countLabel.isVisible()) {
                    Dimension countSize = countLabel.getPreferredSize();
                    size.width += hgap + countSize.width;
                    size.height = Math.max(size.height, countSize.height);
                }

                Insets insets = getInsets();
                
                size.width += insets.left + insets.right;
                size.height += insets.top + insets.bottom;
                
                return size;
            }
        }

        @Override
        public void doLayout() {
            synchronized (getTreeLock()) {
                Insets insets = getInsets();
                
                int x = insets.left;
                int y = insets.top;
                
                int w = getWidth() - x - insets.right;
                int h = getHeight() - y - insets.bottom;

                Dimension textSize = textLabel.getPreferredSize();
                
                if (countLabel.isVisible()) {
                    Dimension countSize = countLabel.getPreferredSize();
                    
                    int commonWidth = textSize.width + hgap + countSize.width;

                    int textHeight = Math.min(h, textSize.height);
                    int textY = y + (h - textHeight) / 2;

                    int countHeight = Math.min(h, countSize.height);
                    int countY = y + (h - countHeight) / 2;
                    
                    if (commonWidth <= w) {
                        int textX = x + (w - commonWidth) / 2;
                        int countX = textX + textSize.width + hgap;
                        
                        textLabel.setBounds(textX, textY, textSize.width, 
                                textHeight);
                        countLabel.setBounds(countX, countY, countSize.width, 
                                countHeight);
                    } else {
                        int minTextWidth = textLabel.getMinimumSize().width;
                        minTextWidth = Math.min(10, minTextWidth);
                        
                        int textWidth = Math.max(minTextWidth, 
                                w - hgap - countSize.width);
                        
                        int textX = x + (w - textWidth - hgap 
                                - countSize.width) / 2;
                        int countX = textX + textWidth + hgap;

                        textLabel.setBounds(textX, textY, textWidth, 
                                textHeight);
                        countLabel.setBounds(countX, countY, countSize.width, 
                                countHeight);
                    }
                } else {
                    int textWidth;
                    
                    if (w < textSize.width) {
                        int minTextWidth = textLabel.getMinimumSize().width;
                        minTextWidth = Math.min(10, minTextWidth);
                        textWidth = Math.max(minTextWidth, w);
                    } else {
                        textWidth = textSize.width;
                    }
                    
                    int textHeight = Math.min(h, textSize.height);
                    int textX = x + (w - textWidth) / 2;
                    int textY = y + (h - textHeight) / 2;
                    
                    textLabel.setBounds(textX, textY, textWidth, textHeight);
                }
            }
        }
    }
    
    private static void setVisible(JComponent component, boolean visible) {
        if (component != null) {
            component.setVisible(visible);
        }
    }
    
    public static final Color TOP_GRADIENT_COLOR = new Color(0xEEEEEE);
    public static final Color BOTTOM_GRADIENT_COLOR = new Color(0xFFFFFF);
    public static final Color TAB_BORDER_COLOR = new Color(0xA7A2A7);
    public static final Color TAB_SEPARATOR_COLOR = Color.LIGHT_GRAY;
    public static final Color TAB_ROLLOVER_BACKGROUND = new Color(0xCCCCCC);
    public static final Color ACTIVE_TAB_BACKGROUND = new Color(0xFFFFFF);
    private static final int TAB_CORNER_DIAMETR = 12;
}
