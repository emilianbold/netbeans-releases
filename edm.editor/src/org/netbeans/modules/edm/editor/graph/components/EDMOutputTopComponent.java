/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.edm.editor.graph.components;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.Serializable;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.logging.Logger;

import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

import org.netbeans.modules.edm.model.SQLObject;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.edm.model.SQLConstants;
import org.openide.awt.MouseUtils;
import org.openide.awt.TabbedPaneFactory;

/**
 * Top component which displays something.
 */
public final class EDMOutputTopComponent extends TopComponent {

    private static EDMOutputTopComponent instance;
    private static EDMSQLLogView logView;
    /** path to the icon used by the component and its open action */
    private static final String ICON_PATH = "org/netbeans/modules/edm/editor/resources/mashup.png";
    private static final String PREFERRED_ID = "EDMOutputTopComponent";
    private JTabbedPane tabbedPane = TabbedPaneFactory.createCloseButtonTabbedPane();
    private JToolBar verticalBar;
    private EDMOutputPanel lastKnownSelection = null;
    private EDMOutputPanel newSelection;
    private PopupListener listener;
    private JPopupMenu pop;
    private CloseListener closeL;
    private ChangeListener listen;
    private transient boolean isVisible = false;
    private static final Logger mLogger = Logger.getLogger(EDMOutputTopComponent.class.getName());

    private EDMOutputTopComponent() {
        initComponents();
        setLayout(new BorderLayout());

        setFocusable(true);
        setBackground(UIManager.getColor("text")); //NOI18N
        setName(NbBundle.getMessage(EDMOutputTopComponent.class, "CTL_EDMOutputTopComponent"));
        setToolTipText(NbBundle.getMessage(EDMOutputTopComponent.class, "HINT_EDMOutputTopComponent"));
        setIcon(Utilities.loadImage(ICON_PATH, true));

        verticalBar = new JToolBar(JToolBar.VERTICAL);
        verticalBar.setLayout(new BoxLayout(verticalBar, BoxLayout.Y_AXIS));
        verticalBar.setFloatable(false);

        Insets ins = verticalBar.getMargin();
        JButton sample = new JButton();
        sample.setBorderPainted(false);
        sample.setOpaque(false);
        sample.setText(null);
        sample.setIcon(new Icon() {

            public int getIconHeight() {
                return 16;
            }

            public int getIconWidth() {
                return 16;
            }

            public void paintIcon(Component c, Graphics g, int x, int y) {
            }
        });
        verticalBar.add(sample);
        Dimension buttonPref = sample.getPreferredSize();
        Dimension minDim = new Dimension(buttonPref.width + ins.left + ins.right, buttonPref.height + ins.top + ins.bottom);
        verticalBar.setMinimumSize(minDim);
        verticalBar.setPreferredSize(minDim);
        verticalBar.remove(sample);
        verticalBar.setBorder(new VariableRightBorder(tabbedPane));
        verticalBar.setBorderPainted(true);

        pop = new JPopupMenu();
        pop.add(new Close());
        pop.add(new CloseAll());
        pop.add(new CloseAllButCurrent());
        listener = new PopupListener();
        closeL = new CloseListener();
        listen = new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                if (e.getSource() instanceof JTabbedPane) {
                    JTabbedPane jp = ((JTabbedPane) e.getSource());
                    newSelection = (EDMOutputPanel)jp.getSelectedComponent();
                    fire(lastKnownSelection, newSelection);
                }
            }
        };
    }
    String nbBundle1 = NbBundle.getMessage(EDMOutputTopComponent.class, "LBL_CloseTab");
    String nbBundle2 = NbBundle.getMessage(EDMOutputTopComponent.class, "LBL_CloseAllTabs");
    String nbBundle3 = NbBundle.getMessage(EDMOutputTopComponent.class, "LBL_CloseOtherTabs");

    private class Close extends AbstractAction {

        public Close() {
            super(nbBundle1);
        }

        public void actionPerformed(ActionEvent e) {
            if (tabbedPane.getComponentCount() > 0) {
                removePanel(tabbedPane.getSelectedComponent());
            }

        }
    }

    private final class CloseAll extends AbstractAction {

        public CloseAll() {
            super(nbBundle2);
        }

        public void actionPerformed(ActionEvent e) {
            if (tabbedPane.getComponentCount() > 0) {
                closeAll(tabbedPane);
            }
            removeAll();
        }
    }

    private class CloseAllButCurrent extends AbstractAction {

        public CloseAllButCurrent() {
            super(nbBundle3);
        }

        public void actionPerformed(ActionEvent e) {
            if (tabbedPane.getComponentCount() > 0) {
                closeAllButCurrent(tabbedPane);
            }
        }
    }

    void closeAllButCurrent(JTabbedPane tabs) {
        Component current = tabs.getSelectedComponent();
        for (Component comp : tabs.getComponents()) {
            if (comp != current) {
                removePanel(comp);
            }
        }
    }

    void closeAll(JTabbedPane tabs) {
        for (Component comp : tabs.getComponents()) {
            removePanel(comp);
        }
        revalidate();
    }

    private class CloseListener implements PropertyChangeListener {

        public void propertyChange(java.beans.PropertyChangeEvent evt) {
            if (TabbedPaneFactory.PROP_CLOSE.equals(evt.getPropertyName())) {
                removePanel((Component) evt.getNewValue());
            }
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private class PopupListener extends MouseUtils.PopupMouseAdapter {

        /**
         * Called when the sequence of mouse events should lead to actual showing popup menu
         */
        @Override
        protected void showPopup(MouseEvent e) {
            pop.show(EDMOutputTopComponent.this, e.getX(), e.getY());
        }
    } // end of PopupListener

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link findInstance}.
     */
    public static synchronized EDMOutputTopComponent getDefault() {
        if (instance == null) {
            instance = new EDMOutputTopComponent();
            logView = new EDMSQLLogView();
        }
        return instance;
    }

    /**
     * Obtain the EDMOutputTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized EDMOutputTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(EDMOutputTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof EDMOutputTopComponent) {
            return (EDMOutputTopComponent) win;
        }
        Logger.getLogger(EDMOutputTopComponent.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID +
                "' ID. That is a potential source of errors and unexpected behavior.");
        return getDefault();
    }

    public void removePanel(Component panel) {
        if (tabbedPane.getComponentCount() == 0) {
            remove(panel);
        } else {
            tabbedPane.remove(panel);
            if (tabbedPane.getComponentCount() == 1) {
                Component c = tabbedPane.getSelectedComponent();
                lastKnownSelection = (EDMOutputPanel)c;
                tabbedPane.removeMouseListener(listener);
                tabbedPane.removePropertyChangeListener(closeL);
                remove(tabbedPane);
                add(c, BorderLayout.CENTER);
            }
        }
        revalidate();
    }

    public void addPanel(Component panel, JButton[] btns, String tooltip) {
        if (getComponentCount() == 0) {
            add(panel, BorderLayout.CENTER);
            if (panel instanceof Component) {
                lastKnownSelection = (EDMOutputPanel)panel;
                verticalBar.removeAll();
                for (JButton btn : btns) {
                    if (btn != null) {
                        verticalBar.add(btn);
                    }
                }
                add(verticalBar, BorderLayout.WEST);
            }
        } else if (tabbedPane.getComponentCount() == 0 && lastKnownSelection != panel) {
            Component comp = (Component) lastKnownSelection;
            remove(comp);
            tabbedPane.addMouseListener(listener);
            tabbedPane.addPropertyChangeListener(closeL);
            tabbedPane.addChangeListener(listen);
            add(tabbedPane, BorderLayout.CENTER);

            if(comp instanceof JComponent){
                tabbedPane.addTab(comp.getName(), null, comp, ((JComponent)comp).getToolTipText()); //NOI18N
            } else {
                tabbedPane.addTab(comp.getName(), null, comp); //NOI18N
            }

            tabbedPane.addTab(panel.getName(), null, panel, tooltip); //NOI18N

            tabbedPane.setSelectedComponent(panel);
            tabbedPane.validate();
        } else if (lastKnownSelection != panel) {
            tabbedPane.addTab(panel.getName(), null, panel, tooltip); //NOI18N

            tabbedPane.setSelectedComponent(panel);
            tabbedPane.validate();
        }
        if (!isVisible) {
            isVisible = true;
            open();
        }
        validate();
        requestActive();
    }

    public void findAndRemoveComponent(SQLObject sqlObj) {
        if (tabbedPane.getComponentCount() <= 0) {
            removeAll();
            close();
        }

        for (Component comp : tabbedPane.getComponents()) {
            switch(sqlObj.getObjectType()){
                case SQLConstants.SOURCE_TABLE:
                    removePanel(comp);
                    break;
                case SQLConstants.JOIN:
                    removePanel(comp);
                    break;
                case SQLConstants.JOIN_VIEW:
                    removePanel(comp);
                    break;
            }
            // TODO: remove all the source object data view
        }
    }

    private void fire(EDMOutputPanel formerSelection, EDMOutputPanel selection) {
        if (formerSelection != selection && selection != null) {
            lastKnownSelection = selection;
            setToolbarButtons(selection.getVerticalToolBar());
        } else if (lastKnownSelection != null) {
            setToolbarButtons(lastKnownSelection.getVerticalToolBar());
        }
    }

    private void setToolbarButtons(JButton[] buttons) {
        verticalBar.removeAll();
        for (JButton btn : buttons) {
            if (btn != null) {
                verticalBar.add(btn);
            }
        }
        verticalBar.repaint();
        verticalBar.validate();
    }

    public void setLog(String msg) {
        logView.appendToView(msg + "\n");
        //logView.appendToView("Logged at " + getTime() + "\n\n");
        addPanel(logView, new JButton[0], logView.getName());
    }

    public void addComponent(Component comp) {
        removeAll();
        setLayout(new BorderLayout());
        add(comp, BorderLayout.CENTER);
        revalidate();
    }

    private String getTime() {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        String format = "yyyy-MM-dd HH:mm:ss";
        java.text.SimpleDateFormat dataFormat =
                new java.text.SimpleDateFormat(format);
        dataFormat.setTimeZone(TimeZone.getDefault());
        return dataFormat.format(calendar.getTime());
    }

    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }

    public void componentOpened() {
    // TODO add custom code on component opening
    }

    public void componentClosed() {
        isVisible = false;
    }

    /** replaces this in object stream */
    public Object writeReplace() {
        return new ResolvableHelper();
    }

    protected String preferredID() {
        return PREFERRED_ID;
    }

    final static class ResolvableHelper implements Serializable {

        private static final long serialVersionUID = 1L;

        public Object readResolve() {
            return EDMOutputTopComponent.getDefault();
        }
    }

    private class VariableRightBorder implements Border {

        private JTabbedPane pane;

        public VariableRightBorder(JTabbedPane pane) {
            this.pane = pane;
        }

        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Color old = g.getColor();
            g.setColor(getColor());
            g.drawLine(x + width - 1, y, x + width - 1, y + height);
            g.setColor(old);
        }

        public Color getColor() {
            if (Utilities.isMac()) {
                Color c1 = UIManager.getColor("controlShadow");
                Color c2 = UIManager.getColor("control");
                return new Color((c1.getRed() + c2.getRed()) / 2, (c1.getGreen() + c2.getGreen()) / 2, (c1.getBlue() + c2.getBlue()) / 2);
            } else {
                return UIManager.getColor("controlShadow");
            }
        }

        public Insets getBorderInsets(Component c) {
            return new Insets(0, 0, 0, 2);
        }

        public boolean isBorderOpaque() {
            return true;
        }
    }
}
