/*
 * The contents of this file are subject to the terms of the Common
 * Development and Distribution License (the License). You may not use this
 * file except in compliance with the License.  You can obtain a copy of the
 *  License at http://www.netbeans.org/cddl.html
 *
 * When distributing Covered Code, include this CDDL Header Notice in each
 * file and include the License. If applicable, add the following below the
 * CDDL Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems, Inc. All Rights Reserved
 *
 */
package org.netbeans.modules.edm.editor.multiview;

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.Arrays;
import javax.swing.JScrollPane;

import org.openide.cookies.SaveCookie;
import org.openide.windows.TopComponent;
import org.openide.util.Lookup;
import org.openide.nodes.Node;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.util.lookup.AbstractLookup;

import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewFactory;

import org.netbeans.modules.edm.editor.dataobject.MashupDataObject;
import org.netbeans.modules.edm.editor.graph.MashupGraphManager;
import org.netbeans.modules.edm.editor.graph.components.EDMNavigatorComponent;
import org.netbeans.modules.edm.editor.graph.components.EDMNavigatorHint;
import org.netbeans.modules.edm.editor.graph.components.MashupToolbar;
import org.netbeans.modules.edm.editor.palette.PaletteSupport;
import org.openide.util.NbBundle;

public class MashupGraphMultiViewElement extends TopComponent
        implements MultiViewElement {

    private static final long serialVersionUID = -655912409997381426L;
    private MashupDataObject mObj = null;
    private transient InstanceContent nodesHack;
    private transient MultiViewElementCallback multiViewObserver;
    private transient javax.swing.JLabel errorLabel = new javax.swing.JLabel();
    private transient JToolBar mToolbar = null;
    private MashupGraphManager manager;

    public MashupGraphMultiViewElement() {
        super();
    }

    public MashupGraphMultiViewElement(MashupDataObject dObj) {
        this();
        this.mObj = dObj;
        initialize();
    }

    private void initialize() {
        setLayout(new BorderLayout());
        initUI();
        initializeLookup();
    }

    private void initializeLookup() {
        setActivatedNodes(new Node[]{getMashupDataObject().getNodeDelegate()});
        associateLookup(createAssociateLookup());
        addPropertyChangeListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent event) {
                if (event.getPropertyName().equals("activatedNodes")) {
                    nodesHack.set(Arrays.asList(getActivatedNodes()), null);
                }
            }
        });
    }

    private Lookup createAssociateLookup() {
        nodesHack = new InstanceContent();
        return new ProxyLookup(new Lookup[]{
                    getMashupDataObject().getLookup(),
                    Lookups.singleton(this),
                    new AbstractLookup(nodesHack),
                    Lookups.fixed(new Object[]{PaletteSupport.createPalette()}),
                    Lookups.fixed(new Object[]{new EDMNavigatorHint()})
                });
    }

    public CloseOperationState canCloseElement() {
        if ((mObj != null) && (mObj.isModified())) {
            return MultiViewFactory.createUnsafeCloseState(NbBundle.getMessage(MashupGraphMultiViewElement.class, "MSG_Data_object_modified"), null, null);
        }
        return CloseOperationState.STATE_OK;
    }

    private MashupDataObject getMashupDataObject() {
        return mObj;
    }

    /**
     * Overwrite when you want to change default persistence type. Default
     * persistence type is PERSISTENCE_ALWAYS.
     * Return value should be constant over a given TC's lifetime.
     */
    @Override
    public int getPersistenceType() {
        return PERSISTENCE_NEVER;
    }

    public void setMultiViewCallback(final MultiViewElementCallback callback) {
        multiViewObserver = callback;
    }

    @Override
    public void componentHidden() {
        super.componentHidden();
    }

    @Override
    public void componentClosed() {
        super.componentClosed();
    }

    @Override
    public void componentOpened() {
        super.componentOpened();
    }

    @Override
    public void componentActivated() {
        super.componentActivated();
        if (null != EDMNavigatorComponent.getInstance()) {
            EDMNavigatorComponent.getInstance().setNewContent(mObj);
        }
    }

    @Override
    public void componentDeactivated() {
        SaveCookie cookie = (SaveCookie) mObj.getCookie(SaveCookie.class);
        if (cookie != null) {
            getMashupDataObject().getMashupDataEditorSupport().synchDocument();
        }
        super.componentDeactivated();
    }

    @Override
    public void componentShowing() {
        super.componentShowing();
        SaveCookie cookie = (SaveCookie) mObj.getCookie(SaveCookie.class);
        if (cookie != null) {
            getMashupDataObject().getMashupDataEditorSupport().syncModel();
            mObj.newFile = true;
        } else {
            mObj.newFile = false;
        }
    }

    @Override
    protected String preferredID() {
        return "MashupGraphMultiViewElementTC";  //  NOI18N
    }

    private void initUI() {
        String errorMessage = null;
        try {
            removeAll();
            manager = getMashupDataObject().getGraphManager();
            setLayout(new BorderLayout());
            manager.refreshGraph();
            getMashupDataObject().getGraphManager().getGuiInfo();
            if (null != EDMNavigatorComponent.getInstance()) {
                EDMNavigatorComponent.getInstance().setNewContent(mObj);
            }
            JScrollPane pane = manager.getPanel();
            add(pane, BorderLayout.CENTER);
            return;
        } catch (Exception ex) {
            errorMessage = ex.getMessage();
        }

        // Clear the interface and show the error message.
        removeAll();
        errorLabel.setText("<" + errorMessage + ">");
        errorLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        errorLabel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        errorLabel.setEnabled(false);
        Color usualWindowBkg = UIManager.getColor("window"); //NOI18N
        errorLabel.setBackground(usualWindowBkg != null ? usualWindowBkg : Color.white);
        errorLabel.setOpaque(true);
        add(errorLabel, BorderLayout.CENTER);
    }

    public javax.swing.JComponent getToolbarRepresentation() {
        if (mToolbar == null) {
            try {
                mToolbar = new MashupToolbar(getMashupDataObject()).getToolBar();
                mToolbar.setFloatable(false);
                mToolbar.getAccessibleContext().setAccessibleName("Mashup ToolBar");
            } catch (Exception e) {
                //wait until the model is loaded
            }
        }
        return mToolbar;
    }

    public javax.swing.JComponent getVisualRepresentation() {
        return this;
    }

    public TopComponent getComponent() {
        return this;
    }
}