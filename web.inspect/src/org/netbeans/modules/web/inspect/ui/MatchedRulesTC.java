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
package org.netbeans.modules.web.inspect.ui;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import org.netbeans.modules.web.inspect.PageInspectorImpl;
import org.netbeans.modules.web.inspect.PageModel;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.explorer.view.BeanTreeView;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ProxyLookup;

/**
 * CSS Styles view.
 * 
 * @author Jan Stola
 */
@TopComponent.Description(
        preferredID = MatchedRulesTC.ID,
        persistenceType = TopComponent.PERSISTENCE_ALWAYS,
        iconBase = MatchedRulesTC.ICON_BASE)
@TopComponent.Registration(
        mode = "commonpalette", // NOI18N
        position = 200,
        openAtStartup = false)
@ActionID(
        category = "Window", // NOI18N
        id = "org.netbeans.modules.web.inspect.ui.MatchedRulesTC") // NOI18N
@ActionReference(
        path = "Menu/Window/Web", // NOI18N
        position = 200)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_MatchedRulesAction", // NOI18N
        preferredID = MatchedRulesTC.ID)
@Messages({
    "CTL_MatchedRulesAction=CSS Styles", // NOI18N
    "CTL_MatchedRulesTC=CSS Styles", // NOI18N
    "HINT_MatchedRulesTC=This window shows matched style rules of an element." // NOI18N
})
public final class MatchedRulesTC extends TopComponent {
    /** Icon base of the {@code TopComponent}. */
    static final String ICON_BASE = "org/netbeans/modules/web/inspect/resources/matchedRules.png"; // NOI18N
    /** TopComponent ID. */
    public static final String ID = "MatchedRulesTC"; // NOI18N
    /** Label shown when no styles information is available. */
    private JLabel noStylesLabel;
    /** Current view shown in this {@code TopComponent}.  */
    private PageModel.CSSStylesView currentView;
    /** Wrapper for the lookup of the current view. */
    private MatchedRulesLookup lookup;

    /**
     * Creates a new {@code MatchedRulesTC}.
     */
    public MatchedRulesTC() {
        lookup = new MatchedRulesLookup();
        associateLookup(lookup);
        setName(Bundle.CTL_MatchedRulesTC());
        setToolTipText(Bundle.HINT_MatchedRulesTC());
        setLayout(new BorderLayout());
        initNoStylesLabel();
        PageInspectorImpl.getDefault().addPropertyChangeListener(createInspectorListener());
        update();
    }

    /**
     * Initializes the "no Styles" label.
     */
    private void initNoStylesLabel() {
        noStylesLabel = new JLabel();
        noStylesLabel.setText(NbBundle.getMessage(MatchedRulesTC.class, "MatchedRulesTC.noStylesLabel.text")); // NOI18N
        noStylesLabel.setHorizontalAlignment(SwingConstants.CENTER);
        noStylesLabel.setVerticalAlignment(SwingConstants.CENTER);
        noStylesLabel.setEnabled(false);
        noStylesLabel.setBackground(new BeanTreeView().getViewport().getView().getBackground());
        noStylesLabel.setOpaque(true);
    }

    /**
     * Updates the content of this {@code TopComponent}.
     */
    private void update() {
        PageModel pageModel = PageInspectorImpl.getDefault().getPage();
        boolean noPage = (pageModel == null);
        boolean noStylesLabelShown = noStylesLabel.getParent() != null;
        if (!noStylesLabelShown || !noPage) {
            removeAll();
            if (noPage) {
                add(noStylesLabel, BorderLayout.CENTER);
                currentView = null;
            } else {
                PageModel.CSSStylesView stylesView = pageModel.getCSSStylesView();
                add(stylesView.getView(), BorderLayout.CENTER);
                currentView = stylesView;
            }
        }
        lookup.setView(currentView);
        revalidate();
        repaint();
    }

    /**
     * Creates a page inspector listener.
     * 
     * @return page inspector listener.
     */
    private PropertyChangeListener createInspectorListener() {
        return new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                String propName = evt.getPropertyName();
                if (PageInspectorImpl.PROP_MODEL.equals(propName)) {
                    update();
                }
            }
        };
    }

    @Override
    protected void componentActivated() {
        super.componentActivated();
        if (currentView != null) {
            currentView.activated();
        }
    }

    @Override
    protected void componentDeactivated() {
        super.componentDeactivated();
        if (currentView != null) {
            currentView.deactivated();
        }
    }

    /**
     * Wrapper for the lookup of the current view.
     */
    static class MatchedRulesLookup extends ProxyLookup {

        /**
         * Sets the current view.
         * 
         * @param view current view.
         */
        void setView(PageModel.CSSStylesView view) {
            if (view == null) {
                setLookups();
            } else {
                setLookups(view.getLookup());
            }
        }
    }

}
