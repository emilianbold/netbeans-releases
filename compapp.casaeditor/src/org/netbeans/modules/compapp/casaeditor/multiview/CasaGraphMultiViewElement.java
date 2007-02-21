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
    
    public static final String PREFERRED_ID = "CasaGraphMultiViewElementTC";
    
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
    
    private CasaDataObject getCasaDataObject() {
        return mDataObject;
    }
    
    private void initializeLookup() {
        ActionMap map = getActionMap();
        Node delegate = mDataObject.getNodeDelegate();
        ActivatedNodesMediator nodesMediator = new ActivatedNodesMediator(delegate);
        
        CookieProxyLookup cpl = new CookieProxyLookup(new Lookup[] {
            Lookups.fixed(new Object[] {
                // Need ActionMap in lookup so our actions are used.
                map,
                // Need the data object registered in the lookup so that the
                // projectui code will close our open editor windows when the
                // project is closed.
                mDataObject
            }),
            Lookups.singleton(CasaPalette.getPalette()),
            nodesMediator.getLookup(),
            // The Node delegate Lookup must be the last one in the list
            // for the CookieProxyLookup to work properly.
            delegate.getLookup(),
        }, delegate);
        
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
        if (mLookAndFeelRenderFlag.isSet()) {
            CasaFactory.getCasaCustomizer().renderCasaDesignView(mDesignView.getScene());
        }
    }
    
    @Override
    public void componentDeactivated() {
        super.componentDeactivated();
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
                } catch (Exception e) {}
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        RegionUtilities.stretchScene(mDesignView.getScene());
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
}
