/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.compapp.casaeditor.multiview;

import org.openide.windows.TopComponent;
import org.netbeans.modules.compapp.casaeditor.CasaDataObject;
import org.netbeans.modules.compapp.casaeditor.CasaDataEditorSupport;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewFactory;

import javax.swing.*;
import java.awt.*;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.compapp.casaeditor.design.CasaDesignView;
import org.netbeans.modules.compapp.casaeditor.graph.CasaFactory;
import org.netbeans.modules.compapp.casaeditor.graph.RegionUtilities;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponent;
import org.netbeans.modules.compapp.casaeditor.nodes.CasaNodeFactory;
import org.netbeans.modules.compapp.casaeditor.palette.CasaPalette;
import org.netbeans.modules.xml.xam.ui.multiview.ActivatedNodesMediator;
import org.netbeans.modules.xml.xam.ui.multiview.CookieProxyLookup;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jeri Lockhart
 */
public class CasaGraphMultiViewElement extends TopComponent implements MultiViewElement {

    static final long serialVersionUID = -665273878020879395L;
    public static final String PREFERRED_ID = "CasaGraphMultiViewElementTC";        // NOI18N
    private CasaDataObject mDataObject;
    private CasaDesignView mDesignView;
    private transient MultiViewElementCallback multiViewObserver;
    private transient JLabel errorLabel = new JLabel();
    private transient JToolBar mToolbar;
    private GetAndResetFlag mLookAndFeelRenderFlag = new GetAndResetFlag();

    public CasaGraphMultiViewElement() {
        super();
    }

    public CasaGraphMultiViewElement(CasaDataObject dataObject) {
        super();
        this.mDataObject = dataObject;
        initializeLookup();
        initializeUI();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        mDesignView.render();
    }

    private CasaDataObject getCasaDataObject() {
        return mDataObject;
    }
    
    private void initializeLookup() {
        ActionMap map = getActionMap();
        Node delegate = mDataObject.getNodeDelegate();
        ActivatedNodesMediator nodesMediator = new ActivatedNodesMediator(delegate);

        CookieProxyLookup cpl = new CookieProxyLookup(new Lookup[]{
                    Lookups.fixed(new Object[]{
                        // Need ActionMap in lookup so our actions are used.
                        map,
                        // Need the data object registered in the lookup so that the
                        // projectui code will close our open editor windows when the
                        // project is closed.
                        mDataObject,}),
                    Lookups.singleton(CasaPalette.getPalette(Lookups.fixed(new Object[]{mDataObject, delegate}))),
                    nodesMediator.getLookup(),
                    Lookups.singleton(this),
                    // The Node delegate Lookup must be the last one in the list
                    // for the CookieProxyLookup to work properly.
                    delegate.getLookup(),}, delegate);

        associateLookup(cpl);

        addPropertyChangeListener(TopComponent.Registry.PROP_ACTIVATED_NODES, nodesMediator);
        addPropertyChangeListener(TopComponent.Registry.PROP_ACTIVATED_NODES, cpl);
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        mDesignView = new CasaDesignView(mDataObject);
        add(mDesignView.getComponent(), BorderLayout.CENTER);
    }

    /**
     * Overwrite when you want to change default persistence type. Default
     * persistence type is PERSISTENCE_ALWAYS.
     * Return value should be constant over a given TC's lifetime.
     *
     * @return one of P_X constants
     * @since 4.20
     */
    @Override
    public int getPersistenceType() {
        return PERSISTENCE_NEVER;
    }

    public void setMultiViewCallback(final MultiViewElementCallback callback) {
        multiViewObserver = callback;
    }

    public CloseOperationState canCloseElement() {
        // if this is not the last cloned xml editor component, closing is OK
        if (!CasaDataEditorSupport.isLastView(multiViewObserver.getTopComponent())) {
            return CloseOperationState.STATE_OK;
        }
        // return a placeholder state - to be sure our CloseHandler is called
        return MultiViewFactory.createUnsafeCloseState(
                "ID_TEXT_CLOSING", // dummy ID // NOI18N
                MultiViewFactory.NOOP_CLOSE_ACTION,
                MultiViewFactory.NOOP_CLOSE_ACTION);
    }

//    @Override
//    public UndoRedo getUndoRedo() {
//        return mDataObject.getEditorSupport().getUndoManager();
//    }
    @Override
    public void componentClosed() {
        super.componentClosed();
        //required to release all references to OM
        if (mDesignView != null) {
            mDesignView.closeView();
            mDesignView = null;
        }
    }

    @Override
    public void componentOpened() {
        super.componentOpened();
    }

    @Override
    public void componentHidden() {
        super.componentHidden();
    }

    @Override
    public void componentActivated() {
        super.componentActivated();
        CasaMultiViewFactory.updateGroupVisibility(CasaGraphMultiViewDesc.PREFERRED_ID);
        if (mLookAndFeelRenderFlag.isSet()) {
            CasaFactory.getCasaCustomizer().renderCasaDesignView(mDesignView.getScene());
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                mDataObject.updateTopComponentActivatedNodesSaveCookie();
            }
        });
    }

    @Override
    public void componentDeactivated() {
        super.componentDeactivated();
        CasaMultiViewFactory.updateGroupVisibility(CasaGraphMultiViewDesc.PREFERRED_ID);
    }

    @Override
    public void componentShowing() {
        super.componentShowing();
        if (mLookAndFeelRenderFlag.isSet()) {
            CasaFactory.getCasaCustomizer().renderCasaDesignView(mDesignView.getScene());
        }

        // The graph is to be shown and it was previously not visible.
        // We need to ensure the region stretches to fill the scene, but only after
        // the scrollbars on the scrollers have a chance to adjust themselves.
        new Thread(new Runnable() {

            public void run() {
                try {
                    // An invoke later is simply not sufficient, we need a delay.
                    // Not entirely a graceful approach but entirely sufficent.
                    Thread.sleep(100);
                } catch (Exception e) {
                }
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        if (mDesignView != null && mDesignView.getScene() != null) {
                            RegionUtilities.stretchScene(mDesignView.getScene());
                        }
                    }
                });
            }
        }).start();
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;  //  NOI18N
    }

    public JComponent getToolbarRepresentation() {
        if (mToolbar == null) {
            mToolbar = mDesignView.getToolBar();
            mToolbar.setFloatable(false);

            // Adjust toolbar component sizes
            int maxButtonHeight = 0;
            for (Component c : mToolbar.getComponents()) {
                if (c instanceof JButton || c instanceof JToggleButton) {
                    maxButtonHeight = Math.max(c.getPreferredSize().height,
                            maxButtonHeight);
                }
            }
            for (Component c : mToolbar.getComponents()) {
                if (c instanceof JButton || c instanceof JToggleButton) {
                    Dimension size = c.getMaximumSize();
                    size.height = maxButtonHeight;
                    c.setMaximumSize(size);
                    c.setMinimumSize(c.getPreferredSize());
                } else if (c instanceof JTextComponent) {
                    c.setMaximumSize(c.getPreferredSize());
                    c.setMinimumSize(c.getPreferredSize());
                } else if (c instanceof JSlider) {
                    Dimension size;
                    size = c.getMaximumSize();
                    size.width = 160;
                    c.setMaximumSize(size);

                    size = c.getPreferredSize();
                    size.width = 160;
                    c.setPreferredSize(size);
                } else {
                    c.setMinimumSize(c.getPreferredSize());
                }
            }

        }
        return mToolbar;
    }

    public JComponent getVisualRepresentation() {
        return this;
    }

    public TopComponent getComponent() {
        return this;
    }

    public void scheduleLookAndFeelRender() {
        mLookAndFeelRenderFlag.set();
    }

    // A flag that can be set, but only read once.
    // After the first read, the flag is cleared.
    private static class GetAndResetFlag {

        private boolean mFlag;

        public GetAndResetFlag() {
            mFlag = false;
        }

        public void set() {
            mFlag = true;
        }

        public boolean isSet() {
            boolean flag = mFlag;
            mFlag = false;
            return flag;
        }
    }

    public void select(CasaComponent casaComponent) {

        CasaNodeFactory nodeFactory = mDesignView.getScene().getNodeFactory();
        Node node = nodeFactory.createNodeFor(casaComponent);

        if (node != null) {
            setActivatedNodes(new Node[]{node});
        }

        requestVisible();
    }
}
