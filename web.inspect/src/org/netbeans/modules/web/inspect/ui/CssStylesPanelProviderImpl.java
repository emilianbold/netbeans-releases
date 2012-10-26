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
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import org.netbeans.modules.css.visual.spi.CssStylesPanelProvider;
import org.netbeans.modules.web.inspect.PageInspectorImpl;
import org.netbeans.modules.web.inspect.PageModel;
import org.openide.explorer.view.BeanTreeView;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * CSS Styles view.
 *
 * @author Jan Stola
 */

public abstract class CssStylesPanelProviderImpl extends JPanel implements CssStylesPanelProvider {

    /**
     * Label shown when no styles information is available.
     */
    private JLabel noStylesLabel;
    /** Current view shown in this {@code TopComponent}.  */
    
    //very very much hacky
    private static int activeViewType = -1;
    
    
    /**
     * Creates a new {@code MatchedRulesTC}.
     */
    public CssStylesPanelProviderImpl() {
        setLayout(new BorderLayout());
        initNoStylesLabel();
        PageInspectorImpl.getDefault().addPropertyChangeListener(createInspectorListener());
    }

    public abstract int getViewType();
    
    /**
     * Initializes the "no Styles" label.
     */
    private void initNoStylesLabel() {
        noStylesLabel = new JLabel();
        noStylesLabel.setText(NbBundle.getMessage(CssStylesPanelProviderImpl.class, "MatchedRulesTC.noStylesLabel.text")); // NOI18N
        noStylesLabel.setHorizontalAlignment(SwingConstants.CENTER);
        noStylesLabel.setVerticalAlignment(SwingConstants.CENTER);
        noStylesLabel.setEnabled(false);
        noStylesLabel.setBackground(new BeanTreeView().getViewport().getView().getBackground());
        noStylesLabel.setOpaque(true);
    }

    protected void update(boolean forceRefresh) {
        PageModel pageModel = PageInspectorImpl.getDefault().getPage();
        boolean noPage = (pageModel == null);
        boolean noStylesLabelShown = noStylesLabel.getParent() != null;
        if(forceRefresh || activeViewType == getViewType()) {
            activeViewType = getViewType();
            if (!noStylesLabelShown || !noPage) {
                removeAll();
                if (noPage) {
                    add(noStylesLabel, BorderLayout.CENTER);
                } else {
                    PageModel.CSSStylesView stylesView = pageModel.getCSSStylesView();
                    add(stylesView.getView(getViewType()), BorderLayout.CENTER);
                }
            }
        }
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
                    update(false);
                }
            }
        };
    }

    @NbBundle.Messages({
        "CTL_CssStylesProviderImpl.document.view.title=Document"
    })
    @ServiceProvider(service = CssStylesPanelProvider.class, position=2000)
    public static class DocumentView extends CssStylesPanelProviderImpl {

        private static String DOCUMENT_PANEL_ID = "document"; //NOI18N

        @Override
        public String getPanelID() {
            return DOCUMENT_PANEL_ID;
        }

        @Override
        public String getPanelDisplayName() {
            return Bundle.CTL_CssStylesProviderImpl_document_view_title();
        }

        @Override
        public JComponent getContent() {
            update(true);
            return this;
        }

        @Override
        public int getViewType() {
            return PageModel.CSSStylesView.DOCUMENT_VIEW_TYPE;
        }
    }
    
    @NbBundle.Messages({
        "CTL_CssStylesProviderImpl.selection.view.title=Selection"
    })
    @ServiceProvider(service = CssStylesPanelProvider.class, position=1000)
    public static class SelectionView extends CssStylesPanelProviderImpl {

        private static String SELECTION_PANEL_ID = "selection"; //NOI18N

        @Override
        public String getPanelID() {
            return SELECTION_PANEL_ID;
        }

        @Override
        public String getPanelDisplayName() {
            return Bundle.CTL_CssStylesProviderImpl_selection_view_title();
        }

        @Override
        public JComponent getContent() {
            update(true);
            return this;
        }

        @Override
        public int getViewType() {
            return PageModel.CSSStylesView.SELECTION_VIEW_TYPE;
        }
    }
    
    
    
}
