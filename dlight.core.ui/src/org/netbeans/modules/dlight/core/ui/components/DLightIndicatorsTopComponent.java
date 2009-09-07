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
package org.netbeans.modules.dlight.core.ui.components;

import java.awt.CardLayout;
import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import org.netbeans.modules.dlight.management.api.DLightManager;
import org.netbeans.modules.dlight.management.api.DLightSession;
import org.netbeans.modules.dlight.spi.indicator.IndicatorComponentEmptyContentProvider;
import org.netbeans.modules.dlight.spi.indicator.Indicator;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Top component which displays something.
 */
final class DLightIndicatorsTopComponent extends TopComponent {

    private static DLightIndicatorsTopComponent instance;
    private DLightSession session;
    /** path to the icon used by the component and its open action */
    static final String ICON_PATH = "org/netbeans/modules/dlight/core/ui/resources/indicators_small.png"; // NOI18N
    private static final String PREFERRED_ID = "DLightIndicatorsTopComponent"; // NOI18N
    private final CardLayout cardLayout = new CardLayout();
    private JPanel cardsLayoutPanel;
    private JPanel panel1;
    private JPanel panel2;
    private boolean showFirstPanel = true;

    private DLightIndicatorsTopComponent() {
        initComponents();
        setSession(null);
        setName(getMessage("CTL_DLightIndicatorsTopComponent")); // NOI18N
        //setToolTipText(NbBundle.getMessage(DLightIndicatorsTopComponent.class, "HINT_DLightIndicatorsTopComponent"));
        setIcon(ImageUtilities.loadImage(ICON_PATH, true));
//        if (WindowManager.getDefault().findMode(this) == null || WindowManager.getDefault().findMode(this).getName().equals("navigator")){ // NOI18N
//            if (WindowManager.getDefault().findMode("navigator") != null){ // NOI18N
//                WindowManager.getDefault().findMode("navigator").dockInto(this);//NOI18N
//            }
//        }
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

    void setActive() {
        cardLayout.show(cardsLayoutPanel, showFirstPanel ? "#1" : "#2");//NOI18N
        showFirstPanel = !showFirstPanel;
    }

    JPanel getNextPanel() {
        return (showFirstPanel ? panel1 : panel2);
    }

    public void setSession(DLightSession session) {
        if (this.session != null && this.session != session){
            DLightManager.getDefault().closeSessionOnExit(this.session);//should close session which was opened here before
        }
        this.session = session;
        List<Indicator<?>> indicators = null;
        if (session != null) {
            setDisplayName(getMessage("CTL_DLightIndicatorsTopComponent.withSession", session.getDisplayName())); // NOI18N
            indicators = session.getIndicators();
        } else {
            setDisplayName(getMessage("CTL_DLightIndicatorsTopComponent")); // NOI18N
            IndicatorComponentEmptyContentProvider emptyContent = Lookup.getDefault().lookup(IndicatorComponentEmptyContentProvider.class);
            if (emptyContent != null) {
                indicators = emptyContent.getEmptyContent();
            }

        }
        Collections.sort(indicators, new Comparator<Indicator<?>>() {

            public int compare(Indicator<?> o1, Indicator<?> o2) {
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

    private void setContent(List<Indicator<?>> indicators) {
        JComponent componentToAdd;
        if (indicators != null) {
            JScrollPane scrollPane = new JScrollPane();
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            JSplitPane prevSplit = null;
            for (int i = 0; i < indicators.size(); ++i) {
                JComponent component = indicators.get(i).getComponent();
                if (i + 1 < indicators.size()) {
                    JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
                    splitPane.setBorder(BorderFactory.createEmptyBorder());
                    splitPane.setContinuousLayout(true);
                    splitPane.setDividerSize(5);
                    splitPane.setResizeWeight(1.0 / (indicators.size() - i));
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
            JLabel emptyLabel = new JLabel(NbBundle.getMessage(DLightIndicatorsTopComponent.class, "IndicatorsTopCompinent.EmptyContent")); // NOI18N
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
    public static synchronized DLightIndicatorsTopComponent getDefault() {
        if (instance == null) {
            instance = new DLightIndicatorsTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the DLightIndicatorsTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized DLightIndicatorsTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(DLightIndicatorsTopComponent.class.getName()).warning(
                "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");//NOI18N
            return getDefault();
        }
        if (win instanceof DLightIndicatorsTopComponent) {
            return (DLightIndicatorsTopComponent) win;
        }
        Logger.getLogger(DLightIndicatorsTopComponent.class.getName()).warning(
            "There seem to be multiple components with the '" + PREFERRED_ID + //NOI18N
            "' ID. That is a potential source of errors and unexpected behavior.");//NOI18N
        return getDefault();
    }

    public static synchronized DLightIndicatorsTopComponent newInstance() {
        return new DLightIndicatorsTopComponent();
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
        if (session != null){
            DLightManager.getDefault().closeSessionOnExit(session);
        }
        super.componentClosed();
    }

    DLightSession getSession(){
        return session;
    }

    /** replaces this in object stream */
    @Override
    public Object writeReplace() {
        return new ResolvableHelper();
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    final static class ResolvableHelper implements Serializable {

        private static final long serialVersionUID = 1L;

        public Object readResolve() {
            return DLightIndicatorsTopComponent.getDefault();
        }
    }

    private static String getMessage(String name, Object... params) {
        return NbBundle.getMessage(DLightIndicatorsTopComponent.class, name, params);
    }
}
