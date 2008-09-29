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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.etl.ui.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import net.java.hulp.i18n.Logger;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.netbeans.modules.sql.framework.ui.output.ETLOutputPanel;
import org.netbeans.modules.sql.framework.ui.output.dataview.JoinOperatorDataPanel;
import org.netbeans.modules.sql.framework.ui.output.dataview.JoinViewDataPanel;
import org.netbeans.modules.sql.framework.ui.output.dataview.SourceTableDataPanel;
import org.netbeans.modules.sql.framework.ui.output.dataview.TargetTableDataPanel;
import org.openide.awt.MouseUtils;
import org.openide.awt.TabbedPaneFactory;
import org.openide.util.ImageUtilities;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Top component which displays various output panel
 * 
 * @author Ahimanikya Satapathy
 * @author Nithya Radhakrishnan
 */
public final class ETLOutputWindowTopComponent extends TopComponent {

    private static ETLOutputWindowTopComponent instance;
    private static final String PREFERRED_ID = "ETLOutputWindowTopComponent";
    private JTabbedPane tabbedPane = TabbedPaneFactory.createCloseButtonTabbedPane();
    private PopupListener listener;
    private ChangeListener listen;
    private JPopupMenu pop;
    private CloseListener closeL;
    private transient boolean isVisible = false;
    private ETLOutputPanel lastKnownSelection = null;
    private ETLOutputPanel newSelection;
    private JToolBar verticalBar;
    public static final String ICON_RESOURCE = "org/netbeans/modules/sql/framework/ui/resources/images/showOutput.png"; // NOI18N

    private static transient final Logger mLogger = Logger.getLogger(ETLOutputWindowTopComponent.class.getName());
    private static transient final Localizer mLoc = Localizer.get();

    private ETLOutputWindowTopComponent() {
        initComponents();
        setLayout(new BorderLayout());

        setFocusable(true);
        setBackground(UIManager.getColor("text")); //NOI18N

        String nbBundle1 = mLoc.t("BUND167: Data Integrator Output");
        setName(nbBundle1.substring(15));
        setIcon(ImageUtilities.loadImage(ICON_RESOURCE));
        String nbBundle2 = mLoc.t("BUND167: Data Integrator Output");
        setToolTipText(nbBundle2.substring(15));

        // create it but don't add it yet...
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
                    newSelection = (ETLOutputPanel) jp.getSelectedComponent();
                    fire(lastKnownSelection, newSelection);
                }
            }
        };

    }
    String nbBundle1 = mLoc.t("BUND168: Close Tab");
    String nbBundle2 = mLoc.t("BUND169: Close All Tabs");
    String nbBundle3 = mLoc.t("BUND150: Close Other Tabs");

    private class Close extends AbstractAction {

        public Close() {
            super(nbBundle1.substring(15));
        }

        public void actionPerformed(ActionEvent e) {
            if (tabbedPane.getComponentCount() > 0) {
                removePanel(tabbedPane.getSelectedComponent());
            }

        }
    }

    private final class CloseAll extends AbstractAction {

        public CloseAll() {
            super(nbBundle2.substring(15));
        }

        public void actionPerformed(ActionEvent e) {
            if (tabbedPane.getComponentCount() > 0) {
                closeAll(tabbedPane);
            }
            removeAll();
            close();
        }
    }

    private class CloseAllButCurrent extends AbstractAction {

        public CloseAllButCurrent() {
            super(nbBundle3.substring(15));
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
            pop.show(ETLOutputWindowTopComponent.this, e.getX(), e.getY());
        }
    } // end of PopupListener

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
     * @return ETLOutputWindowTopComponent defaultInstance
     */
    public static synchronized ETLOutputWindowTopComponent getDefault() {
        if (instance == null) {
            instance = new ETLOutputWindowTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the ETLOutputWindowTopComponent instance. Never call {@link #getDefault} directly!
     * @return ETLOutputWindowTopComponent defaultInstance
     */
    public static synchronized ETLOutputWindowTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            mLogger.infoNoloc(mLoc.t("EDIT511: Cannot find {0}component. It will not be located properly in the window system.", PREFERRED_ID));
            return getDefault();
        }
        if (win instanceof ETLOutputWindowTopComponent) {
            return (ETLOutputWindowTopComponent) win;
        }
        mLogger.infoNoloc(mLoc.t("EDIT512: There seem to be multiple components with the '{0} ' ID. That is a potential source of errors and unexpected behavior.", PREFERRED_ID));
        return getDefault();
    }

    public void removePanel(Component panel) {
        if (tabbedPane.getComponentCount() == 0) {
            remove(panel);
        } else {
            tabbedPane.remove(panel);
            if (tabbedPane.getComponentCount() == 1) {
                Component c = tabbedPane.getSelectedComponent();
                lastKnownSelection = (ETLOutputPanel) c;
                tabbedPane.removeMouseListener(listener);
                tabbedPane.removePropertyChangeListener(closeL);
                remove(tabbedPane);
                add(c, BorderLayout.CENTER);
            }
        }
        revalidate();
    }

    public void addPanel(Component panel) {
        if (getComponentCount() == 0) {
            add(panel, BorderLayout.CENTER);
            if (panel instanceof ETLOutputPanel) {
                lastKnownSelection = (ETLOutputPanel) panel;
                verticalBar.removeAll();
                JButton[] btns = ((ETLOutputPanel) panel).getVerticalToolBar();
                for (JButton btn : btns) {
                    if(btn != null){
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
            tabbedPane.addTab(comp.getName() + "  ", null, comp, comp.getName()); //NOI18N
            tabbedPane.addTab(panel.getName() + "  ", null, panel, panel.getName()); //NOI18N
            tabbedPane.setSelectedComponent(panel);
            tabbedPane.validate();
        } else if (lastKnownSelection != panel) {
            tabbedPane.addTab(panel.getName() + "  ", null, panel, panel.getName()); //NOI18N
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
        if (sqlObj.getObjectType() == SQLConstants.SOURCE_TABLE || sqlObj.getObjectType() == SQLConstants.TARGET_TABLE) {
            if (tabbedPane.getComponentCount() <= 0) {
                removeAll();
                close();
            }

            for (Component comp : tabbedPane.getComponents()) {
                if (comp instanceof TargetTableDataPanel) {
                    if (((TargetTableDataPanel) comp).getTable() == sqlObj) {
                        removePanel(comp);
                    }
                } else if (comp instanceof SourceTableDataPanel) {
                    if (((SourceTableDataPanel) comp).getTable() == sqlObj) {
                        removePanel(comp);
                    }
                } else if (comp instanceof JoinViewDataPanel) {
                    if (((JoinViewDataPanel) comp).getTable() == sqlObj) {
                        removePanel(comp);
                    }
                } else if (comp instanceof JoinOperatorDataPanel) {
                    if (((JoinOperatorDataPanel) comp).getTable() == sqlObj) {
                        removePanel(comp);
                    // TODO: remove all the source object data view
                    }
                }
            }

        }

    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }

    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    @Override
    public Object writeReplace() {
        return new ResolvableHelper();
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    static final class ResolvableHelper implements Serializable {

        private static final long serialVersionUID = 1L;

        public Object readResolve() {
            return ETLOutputWindowTopComponent.getDefault();
        }
    }

    protected void fire(ETLOutputPanel formerSelection, ETLOutputPanel selection) {
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
