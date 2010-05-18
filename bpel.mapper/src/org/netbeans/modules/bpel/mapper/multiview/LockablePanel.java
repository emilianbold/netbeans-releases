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

package org.netbeans.modules.bpel.mapper.multiview;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

/**
 *
 * @author anjeleevich
 */
public class LockablePanel<T extends JComponent> extends JPanel {

    private T content;
    private boolean locked = false;

    private ContentPane contentPane;
    private GlassPane glassPane;

    private int layouting = 0;
    private int painting = 0;

    public LockablePanel() {
        this(null, false);
    }

    public LockablePanel(T content) {
        this(content, false);
    }

    public LockablePanel(T content, boolean locked) {
        super((LayoutManager) null);

        glassPane = new GlassPane();
        glassPane.setVisible(false);

        contentPane = new ContentPane();
        contentPane.setVisible(true);

        add(glassPane);
        add(contentPane);

        setContent(content);
        setLocked(locked);
    }

    public T getContent() {
        return content;
    }

    public void setContent(T content) {
        T oldContent = this.content;
        T newContent = content;

        if (oldContent != newContent) {
            if (oldContent != null) {
                contentPane.remove(oldContent);
            }

            if (newContent != null) {
                contentPane.add(newContent);
            }

            this.content = newContent;

            revalidate();
            repaint();

            firePropertyChange(CONTENT_PROPERTY, oldContent, newContent);
        }
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        boolean oldLocked = this.locked;
        boolean newLocked = locked;

        if (oldLocked != newLocked) {
            contentPane.setVisible(!newLocked);
            glassPane.setVisible(newLocked);

            this.locked = newLocked;

            repaint();

            firePropertyChange(LOCKED_PROPERTY, oldLocked, newLocked);
        }
    }

    public void setLockedText(String text) {
        glassPane.setLockedText(text);
    }

    public void setLockedIcon(Icon icon) {
        glassPane.setLockedIcon(icon);
    }

    public String getLockedText() {
        return glassPane.getLockedText();
    }

    public Icon getLockedIcon() {
        return glassPane.getLockedIcon();
    }

    private void startLayouting() {
        layouting++;
    }

    private void endLayouting() {
        layouting--;
    }

    private void startPainting() {
        painting++;
    }

    private void endPainting() {
        painting--;
    }

    private boolean isLayouting() {
        return layouting > 0;
    }

    private boolean isPainting() {
        return painting > 0;
    }

    @Override
    public void doLayout() {
        synchronized (getTreeLock()) {
            startLayouting();
            try {
                Insets insets = getInsets();

                int x = insets.left;
                int y = insets.top;

                int w = getWidth() - x - insets.right;
                int h = getHeight() - y - insets.bottom;

                contentPane.setBounds(x, y, w, h);
                glassPane.setBounds(x, y, w, h);
            } finally {
                endLayouting();
            }
        }
    }

    @Override
    public Dimension getPreferredSize() {
        synchronized (getTreeLock()) {
            startLayouting();
            try {
                Insets insets = getInsets();

                Dimension size = glassPane.getPreferredSize();
                Dimension contentSize = contentPane.getPreferredSize();

                size.width = Math.max(size.width, contentSize.width);
                size.height = Math.max(size.height, contentSize.height);

                size.width += insets.left + insets.right;
                size.height += insets.top + insets.bottom;

                return size;
            } finally {
                endLayouting();
            }
        }
    }

    @Override
    public Dimension getMinimumSize() {
        synchronized (getTreeLock()) {
            startLayouting();
            try {
                Insets insets = getInsets();

                Dimension size = glassPane.getMinimumSize();
                Dimension contentSize = contentPane.getMinimumSize();

                size.width = Math.max(size.width, contentSize.width);
                size.height = Math.max(size.height, contentSize.height);

                size.width += insets.left + insets.right;
                size.height += insets.top + insets.bottom;

                return size;
            } finally {
                endLayouting();
            }
        }
    }

    @Override
    protected void paintChildren(Graphics g) {
        startPainting();
        try {
            super.paintChildren(g);
        } finally {
            endPainting();
        }
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    @Override
    public boolean isOptimizedDrawingEnabled() {
        return !locked;
    }

    private class ContentPane extends JPanel {
        private ContentPane() {
            super((LayoutManager) null);
        }

        @Override
        public void doLayout() {
            synchronized (getTreeLock()) {
                if (content != null) {
                    content.setBounds(0, 0, getWidth(), getHeight());
                }
            }
        }

        @Override
        public Dimension getPreferredSize() {
            return (content != null)
                    ? content.getPreferredSize()
                    : new Dimension(0, 0);
        }

        @Override
        public Dimension getMinimumSize() {
            return (content != null)
                    ? content.getMinimumSize()
                    : new Dimension(0, 0);
        }

        @Override
        public Dimension getMaximumSize() {
            return getPreferredSize();
        }

        @Override
        public boolean isVisible() {
            return isLayouting() || isPainting() || super.isVisible();
        }
    }

    private class GlassPane extends JPanel {
        private MessageLabel label;

        public GlassPane() {
            super((LayoutManager) null);

            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            setOpaque(false);

            label = new MessageLabel();
            label.setText("Reloading...");

            add(label);
        }

        Icon getLockedIcon() {
            return label.getIcon();
        }

        String getLockedText() {
            return label.getText();
        }

        void setLockedIcon(Icon icon) {
            label.setIcon(icon);
        }

        void setLockedText(String text) {
            label.setText(text);
            setToolTipText(text);
        }

        @Override
        protected void paintComponent(Graphics g) {
            g.setColor(new Color(0x22000000, true));
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension size = label.getPreferredSize();
            size.width += 32;
            size.height += 32;
            return size;
        }

        @Override
        public Dimension getMinimumSize() {
            return getPreferredSize();
        }

        @Override
        public void doLayout() {
            Dimension size = label.getPreferredSize();

            int w = getWidth();
            int h = getHeight();

            int x = (w - size.width) / 2;
            int y = (h - size.height) / 2;

            label.setBounds(x, y, size.width, size.height);
        }
    }

    private class MessageLabel extends JLabel {
        public MessageLabel() {
            setOpaque(false);
            setBorder(new EmptyBorder(8, 16, 8, 16));
            setBackground(LABEL_BACKGROUND);
            setForeground(LABEL_FOREGROUND);
            setText("Please wait..."); // NOI18N
        }

        @Override
        protected void paintComponent(Graphics g) {
            int w = getWidth() - 1;
            int h = getHeight() - 1;

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                    RenderingHints.VALUE_STROKE_PURE);
            g2.translate(0.5, 0.5);

            g2.setColor(LABEL_BACKGROUND);
            g2.fillRoundRect(0, 0, w, h, 12, 12);

            g2.setColor(LABEL_BORDER_COLOR);
            g2.drawRoundRect(0, 0, w, h, 12, 12);

            g2.dispose();

            super.paintComponent(g);
        }
    }

    public static final String CONTENT_PROPERTY
            = "LockableContentProeprty"; // NOI18N
    public static final String LOCKED_PROPERTY
            = "LockedProperty"; // NOI18N

    private static final Color LABEL_FOREGROUND = Color.DARK_GRAY;
    private static final Color LABEL_BACKGROUND = Color.WHITE;
    private static final Color LABEL_BORDER_COLOR = Color.LIGHT_GRAY;

    public static void main(String[] args) {
        class TestFrame extends JFrame {
            LockablePanel<JComponent> lockablePanel;
            JPanel controlsPanel;
            JCheckBox lockCheckbox;
            JTextField lockedTextField;

            TestFrame() {
                setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                setSize(640, 480);
                setLocationRelativeTo(null);

                lockCheckbox = new JCheckBox("Lock");
                lockCheckbox.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        lockablePanel.setLocked(lockCheckbox.isSelected());
                    }
                });

                lockedTextField = new JTextField(15);
                lockedTextField.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        lockablePanel.setLockedText(lockedTextField.getText());
                    }
                });

                controlsPanel = new JPanel();
                controlsPanel.add(lockCheckbox);
                controlsPanel.add(lockedTextField);
                controlsPanel.setBorder(new LineBorder(Color.RED));

                JButton testButton = new JButton("Test Button");
                JTextField testTextField = new JTextField("Test TextField");
                JPanel testContent = new JPanel();
                testContent.add(testButton);
                testContent.add(testTextField);

                lockablePanel = new LockablePanel<JComponent>();
                lockablePanel.setContent(testContent);
                lockablePanel.setLocked(lockCheckbox.isSelected());

                lockedTextField.setText(lockablePanel.getLockedText());

                getContentPane().add(lockablePanel, BorderLayout.CENTER);
                getContentPane().add(controlsPanel, BorderLayout.SOUTH);
            }
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new TestFrame().setVisible(true);
            }
        });
    }
}
