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
package org.netbeans.modules.cnd.tha.ui;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.FocusTraversalPolicy;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.FocusManager;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import org.netbeans.modules.dlight.management.api.DLightManager;
import org.netbeans.modules.dlight.management.api.DLightSession;
import org.netbeans.modules.dlight.spi.indicator.IndicatorComponentEmptyContentProvider;
import org.netbeans.modules.dlight.spi.indicator.Indicator;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Top component which displays something.
 */
final class THAIndicatorsTopComponent extends TopComponent implements ExplorerManager.Provider {

    private static THAIndicatorsTopComponent instance;
    private DLightSession session;
    /** path to the icon used by the component and its open action */
    static final String ICON_PATH = "org/netbeans/modules/dlight/core/ui/resources/indicators_small.png"; // NOI18N
    private static final String PREFERRED_ID = "GizmoIndicatorsTopComponent"; // NOI18N
    private static final AtomicInteger index = new AtomicInteger();
    private final CardLayout cardLayout = new CardLayout();
    private JPanel cardsLayoutPanel;
    private JPanel panel1;
    private JPanel panel2;
    private Vector<JComponent> indicatorPanels = null;
    private boolean showFirstPanel = true;
    private boolean dock;
    private final ExplorerManager manager = new ExplorerManager();
    private JComponent lastFocusedComponent = null;
    private final FocusTraversalPolicy focusPolicy = new FocusTraversalPolicyImpl();
    private THAIndicatorsTopComponentActionsProvider actionsProvider = null;
    private final PopupAction popupAction = new PopupAction("popupTHAIndicatorTopComponentAction");//NOI18N


    static {
        THAIndicatorTopComponentRegsitry.getRegistry();
    }

    private THAIndicatorsTopComponent(boolean dock) {
        initComponents();
        this.dock = dock;
        setSession(null);
        setName(getMessage("CTL_DLightIndicatorsTopComponent")); // NOI18N
        setToolTipText(getMessage("CTL_DLightIndicatorsTopComponent"));//NOI18N
        setIcon(ImageUtilities.loadImage(ICON_PATH, true));
        if (dock) {
            if (WindowManager.getDefault().findMode(this) == null || WindowManager.getDefault().findMode(this).getName().equals("explorer")) { // NOI18N
                if (WindowManager.getDefault().findMode("explorer") != null) { // NOI18N
                    WindowManager.getDefault().findMode("explorer").dockInto(this);//NOI18N
                }
            }
        }
        setFocusTraversalPolicyProvider(true);
        setFocusTraversalPolicy(focusPolicy);
        ActionMap map = new ActionMap();
        map.put("org.openide.actions.PopupAction", popupAction);//NOI18N
        this.associateLookup(ExplorerUtils.createLookup(manager, map));
        installActions();
    }

    public ExplorerManager getExplorerManager() {
        return manager;
    }

    void setActionsProvider(THAIndicatorsTopComponentActionsProvider actionsProvoder) {
        this.actionsProvider = actionsProvoder;

    }

    private Action getPopupAction() {
        return null;
    }

    private THAIndicatorsTopComponent() {
        this(false);
    }

    void initComponents() {
        cardsLayoutPanel = new JPanel(cardLayout);
        //create 2 panels
        panel1 = new JPanel();
        panel2 = new JPanel();
        cardsLayoutPanel.add(panel1, "#1");//NOI18N
        cardsLayoutPanel.add(panel2, "#2");//NOI18N
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(cardsLayoutPanel);

    }

    void installActions() {
        KeyStroke returnKey = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, true);
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(returnKey, "return"); // NOI18N
        getActionMap().put("return", new ESCHandler()); // NOI18N
    }

    void setActive() {
        cardLayout.show(cardsLayoutPanel, showFirstPanel ? "#1" : "#2");//NOI18N
        showFirstPanel = !showFirstPanel;
    }

    JPanel getNextPanel() {
        return (showFirstPanel ? panel1 : panel2);
    }

    private JPanel getCurrentPanel() {
        return (showFirstPanel ? panel2 : panel1);
    }

    public void setSession(DLightSession session) {
        if (this.session != null && this.session != session) {
            DLightManager.getDefault().closeSessionOnExit(this.session);//should close session which was opened here before
        }
        this.session = session;
        List<Indicator> indicators = null;
        if (session != null) {
            setDisplayName(getMessage("CTL_DLightIndicatorsTopComponent.withSession", session.getDisplayName())); // NOI18N
            setToolTipText(getMessage("CTL_DLightIndicatorsTopComponent.withSession", session.getDisplayName())); // NOI18N
            indicators = session.getIndicators();
        } else {
            setDisplayName(getMessage("CTL_DLightIndicatorsTopComponent")); // NOI18N
            setToolTipText(getMessage("CTL_DLightIndicatorsTopComponent")); // NOI18N
            IndicatorComponentEmptyContentProvider emptyContent = Lookup.getDefault().lookup(IndicatorComponentEmptyContentProvider.class);
            if (emptyContent != null) {
                indicators = emptyContent.getEmptyContent();
            }

        }
        Collections.sort(indicators, new Comparator<Indicator>() {

            public int compare(Indicator o1, Indicator o2) {
                if (o1.getPosition() < o2.getPosition()) {
                    return -1;
                } else if (o2.getPosition() < o1.getPosition()) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });
        setContent(indicators);
    }

    private void setContent(List<Indicator> indicators) {
        JComponent componentToAdd;
        if (indicators != null) {
            JScrollPane scrollPane = new JScrollPane();
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            JSplitPane prevSplit = null;
            indicatorPanels = new Vector<JComponent>(indicators.size());
            indicatorPanels.setSize(indicators.size());
            // We will resize only components without MaximumSize.
            // Implemented for Parallel Adviser indicator.
            int freeSizeComponentsNumber = 0;
            for (int i = 0; i < indicators.size(); ++i) {
                JComponent component = indicators.get(i).getComponent();
                if(!component.isMaximumSizeSet()) {
                    freeSizeComponentsNumber++;
                }
            }
            for (int i = 0; i < indicators.size(); ++i) {
                JComponent component = indicators.get(i).getComponent();
                indicatorPanels.set(i, component);
                if (i + 1 < indicators.size()) {
                    JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
                    splitPane.setBorder(BorderFactory.createEmptyBorder());
                    splitPane.setContinuousLayout(true);
                    splitPane.setDividerSize(5);
                    if(!component.isMaximumSizeSet()) {
                        splitPane.setResizeWeight(1.0 / (freeSizeComponentsNumber - i));
                    }
                    splitPane.setTopComponent(component);
                    component = splitPane;
                }
                if (prevSplit == null) {
                    scrollPane.setViewportView(component);
                } else {
                    prevSplit.setBottomComponent(component);
                }
                if (component instanceof JSplitPane) {
                    prevSplit = (JSplitPane) component;
                }
            }
//            add(scrollPane);
            componentToAdd = scrollPane;
        } else {
            indicatorPanels = null;
            JLabel emptyLabel = new JLabel(NbBundle.getMessage(THAIndicatorsTopComponent.class, "IndicatorsTopCompinent.EmptyContent")); // NOI18N
            emptyLabel.setAlignmentX(JComponent.CENTER_ALIGNMENT);
            componentToAdd = emptyLabel;
//            add(emptyLabel);
        }
        JPanel panel = getNextPanel();
        panel.removeAll();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(componentToAdd);
        setActive();
        repaint();
    }

    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link #findInstance}.
     */
    public static synchronized THAIndicatorsTopComponent getDefault() {
        if (instance == null) {
            instance = new THAIndicatorsTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the DLightIndicatorsTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized THAIndicatorsTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(THAIndicatorsTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");//NOI18N
            return getDefault();
        }
        if (win instanceof THAIndicatorsTopComponent) {
            return (THAIndicatorsTopComponent) win;
        }
        Logger.getLogger(THAIndicatorsTopComponent.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID + //NOI18N
                "' ID. That is a potential source of errors and unexpected behavior.");//NOI18N
        return getDefault();
    }

    public static synchronized THAIndicatorsTopComponent newInstance() {
        return new THAIndicatorsTopComponent(true);
    }

    public static synchronized TopComponent activateInstance() {
        //find and open VisualizerDispAction
        THAIndicatorTopComponentRegsitry registry = THAIndicatorTopComponentRegsitry.getRegistry();
        if (registry.getOpened() == null || registry.getOpened().size() == 0){
            return findInstance();
        }
        THAIndicatorsTopComponent activatedTopComponent = registry.getActivated();
        if (activatedTopComponent == null){
            activatedTopComponent = registry.getOpened().iterator().next();
        }
        activatedTopComponent.requestActive();
        return activatedTopComponent;
    }

    @Override
    public int getPersistenceType() {
        if (!dock) {
            return TopComponent.PERSISTENCE_ALWAYS;
        }
        return TopComponent.PERSISTENCE_NEVER;
    }

    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
    }

    @Override
    protected void componentDeactivated() {
        super.componentDeactivated();
        lastFocusedComponent = null;
        if (indicatorPanels == null || indicatorPanels.size() == 0) {
            return;
        }
        for (JComponent c : indicatorPanels) {
            if (c.hasFocus()) {
                lastFocusedComponent = c;
                break;
            }
        }
        if (lastFocusedComponent == null) {
            lastFocusedComponent = indicatorPanels.get(0);
        }
    }

    @Override
    protected void componentActivated() {
        //should request focus
        super.componentActivated();
        if (lastFocusedComponent != null) {
            lastFocusedComponent.requestFocus();
        } else {
            focusPolicy.getFirstComponent(this).requestFocus();
        }

    }

    @Override
    public void componentClosed() {
        if (session != null) {

            DLightManager.getDefault().closeSessionOnExit(session);
        }
        super.componentClosed();
    }

    DLightSession getSession() {
        return session;
    }

    /** replaces this in object stream */
    @Override
    public Object writeReplace() {
        return new ResolvableHelper();
    }

    @Override
    protected String preferredID() {
        if (!dock) {
            return PREFERRED_ID;
        }
        return PREFERRED_ID + index.incrementAndGet();
    }

    final static class ResolvableHelper implements Serializable {

        private static final long serialVersionUID = 1L;

        public Object readResolve() {
            return THAIndicatorsTopComponent.getDefault();
        }
    }

    private static String getMessage(String name, Object... params) {
        return NbBundle.getMessage(THAIndicatorsTopComponent.class, name, params);
    }



    private final class FocusTraversalPolicyImpl extends FocusTraversalPolicy {

        @Override
        public Component getComponentAfter(Container aContainer, Component aComponent) {
            if (aComponent == getCurrentPanel()) {
                return getCurrentPanel();//no path to go
            }
            int indexOf = indicatorPanels.indexOf(aComponent);
            if (indexOf == -1) {
                return getCurrentPanel();
            }
            if (indexOf == indicatorPanels.size() - 1) {
                return indicatorPanels.get(0);
            }
            return indicatorPanels.get(indexOf + 1);
        }

        @Override
        public Component getComponentBefore(Container aContainer, Component aComponent) {
            if (aComponent == getCurrentPanel()) {
                return getCurrentPanel();//no path to go
            }
            int indexOf = indicatorPanels.indexOf(aComponent);
            if (indexOf == -1) {
                return getCurrentPanel();
            }
            if (indexOf == 0) {
                return indicatorPanels.get(indicatorPanels.size() - 1);
            }
            return indicatorPanels.get(indexOf - 1);
        }

        @Override
        public Component getFirstComponent(Container aContainer) {
            if (indicatorPanels == null || indicatorPanels.size() == 0) {
                return getCurrentPanel();
            }
            return indicatorPanels.get(0);
        }

        @Override
        public Component getLastComponent(Container aContainer) {
            if (indicatorPanels == null || indicatorPanels.size() == 0) {
                return getCurrentPanel();
            }
            return indicatorPanels.get(indicatorPanels.size() - 1);
        }

        @Override
        public Component getDefaultComponent(Container aContainer) {
            if (indicatorPanels == null || indicatorPanels.size() == 0) {
                return getCurrentPanel();
            }
            return indicatorPanels.get(0);
        }
    }

    private class PopupAction extends AbstractAction implements Runnable {

        private PopupAction(String name) {
            super(name);
        }

        public void actionPerformed(ActionEvent e) {
            SwingUtilities.invokeLater(this);
        }

        public void run() {

            if (actionsProvider == null) {
                return;
            }
            Action[] actions = actionsProvider.getActions(THAIndicatorsTopComponent.this);
            if (actions != null && actions.length > 0) {
                //System.out.println("I have" + actions.length + " actions to display in menu");
                JPopupMenu menu = Utilities.actionsToPopup(actions, THAIndicatorsTopComponent.this);
                menu.show(THAIndicatorsTopComponent.this, 0, 0);

            }
        }
    }

    private class ESCHandler extends AbstractAction {

        public void actionPerformed(ActionEvent e) {
            Component focusOwner = FocusManager.getCurrentManager().getFocusOwner();
            if (THAIndicatorTopComponentRegsitry.getRegistry().getActivated() == null || focusOwner == null ||
                    !SwingUtilities.isDescendingFrom(focusOwner, THAIndicatorsTopComponent.this)) {
                return;
            }
            TopComponent prevFocusedTc = THAIndicatorTopComponentRegsitry.getRegistry().getActivatedNonIndicators();
            if (prevFocusedTc != null) {
                prevFocusedTc.requestActive();
            }
        }
    }
}
