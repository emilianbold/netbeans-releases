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
package org.netbeans.modules.compapp.casaeditor.design;

import java.awt.Color;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.*;

import org.netbeans.modules.compapp.casaeditor.CasaDataObject;
import org.netbeans.modules.compapp.casaeditor.graph.RegionUtilities;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel;
import org.netbeans.modules.compapp.casaeditor.nodes.CasaNodeFactory;
import org.netbeans.modules.compapp.casaeditor.nodes.actions.AutoLayoutAction;
import org.netbeans.modules.compapp.casaeditor.nodes.actions.BuildAction;
import org.netbeans.modules.print.api.PrintManagerAccess;

import org.openide.util.NbBundle;

/**
 * @author Josh Sandusky
 */
public class CasaDesignView {

    private CasaDataObject mDataObject;
    private CasaModelGraphScene mScene;
    private CasaDesignModelListener mModelListener;
    private JScrollPane mScroller;

    private JToolBar mToolBar;
    private AbstractAction mAutoLayoutAction;
    private AbstractAction mBuildAction;

    
    public CasaDesignView(CasaDataObject dataObject) {
        mDataObject = dataObject;

        mScroller = new JScrollPane();
        mScroller.getVerticalScrollBar().setUnitIncrement(30);
        mScroller.getHorizontalScrollBar().setUnitIncrement(20);
        
        CasaWrapperModel model = mDataObject.getEditorSupport().getModel();

        JComponent view = null;
        if (model == null) {
            view = getErrorPane();
        } else {
            mScene = new CasaModelGraphScene(mDataObject, model, new CasaNodeFactory(mDataObject, model));
            mDataObject.getEditorSupport().setScene(mScene);
            view = mScene.getViewComponent();
            initializeSceneForDesignView();
        }
        
        mScroller.setViewportView(view);
        
        setupActions();
        setupToolBar();
        
        if (model != null) {
            mModelListener = new CasaDesignModelListener(mDataObject, mScene);
            mScene.registerModelListener(mModelListener);
        }
    }
   
    public void render() {
        // Render the model
        CasaWrapperModel model = mDataObject.getEditorSupport().getModel();
        if (model != null) {
            CasaModelGraphUtilities.renderModel(model, mScene);
        }
    }
    
     /**
     * Return the view content, suitable for printing (i.e. without a
     * scroll pane, which would result in the scroll bars being printed).
     *
     * @return  the view content, sans scroll pane.
     */
    public JComponent getContent() {
        return mScene.getViewComponent();
    }

    private void initializeSceneForDesignView() {
        mScroller.addComponentListener(new ComponentAdapter() {
            private boolean mIsIgnoringResizeEvents;
            public void componentResized(ComponentEvent evt) {
                if (evt.getID() == ComponentEvent.COMPONENT_RESIZED) {
                    if (
                            !mIsIgnoringResizeEvents &&
                            mScene != null && 
                            mScene.isVisible() &&
                            mScene.isValidated())
                    {
                        // Invoke the region stretching later, because the
                        // scrollbars of the scroll pane still need time to
                        // determine whether they need to display or not.
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                mIsIgnoringResizeEvents = true;
                                RegionUtilities.stretchScene(mScene);
                                mIsIgnoringResizeEvents = false;
                            }
                        });
                    }
                }
            }
        });

        AdjustmentListener scrollAdjuster = new AdjustmentListener() {
            public void adjustmentValueChanged(AdjustmentEvent e) {
                if (
                        !e.getValueIsAdjusting() &&
                        mScene != null &&
                        mScene.isVisible() &&
                        mScene.isValidated())
                {
                    try {
                        RegionUtilities.stretchScene(mScene);
                    } catch (Throwable t) {
                        // We cannot allow an exception to be thrown here, so we
                        // fail quietly and print to stderr.
                        t.printStackTrace(System.err);
                    }
                }
            }
        };
        
        // We must adjust the region height AND width when either the horizontal
        // or vertical scroll bars change. Otherwise, drag actions that can cause
        // the scrollbars to appear (because of a viewport bounds change) will
        // not cause the regions to stretch back to fill in the empty space
        // left by the scrollbars if the scrollbars disappear.
        mScroller.getHorizontalScrollBar().addAdjustmentListener(scrollAdjuster);
        mScroller.getVerticalScrollBar().addAdjustmentListener(scrollAdjuster);
    }
    
    private void setupActions() {
        mAutoLayoutAction = new AutoLayoutAction(mDataObject);
        mBuildAction = new BuildAction(mScene.getModel());
    }

    private void setupToolBar() {
        mToolBar = new JToolBar();
        mToolBar.addSeparator();

        mToolBar.add(createButton(mAutoLayoutAction,
                                  (String) mAutoLayoutAction.getValue(Action.NAME), // NOI18N
                                  (Icon)   mAutoLayoutAction.getValue(Action.SMALL_ICON))); // NOI18N
        mToolBar.addSeparator();

        mToolBar.add(createButton(mBuildAction,
                                  (String) mBuildAction.getValue(Action.NAME), // NOI18N
                                  (Icon)   mBuildAction.getValue(Action.SMALL_ICON))); // NOI18N
        // vlv: print
        mToolBar.addSeparator();
        mToolBar.add(PrintManagerAccess.getManager().getPreviewAction());
    }

    private JButton createButton(Action action, String tooltip, Icon icon) {
        JButton button = new JButton(action);
        button.setText("");
        button.setToolTipText(tooltip);
        button.setIcon(icon);
        return button;
    }

    public JToolBar getToolBar() {
        return mToolBar;
    }

    public JComponent getComponent() {
        return mScroller;
    }

    public JComponent getErrorPane() {
        JLabel errorLabel = new JLabel();
        errorLabel.setText(NbBundle.getMessage(getClass(), "MSG_ModelError")); // NOI18N
        errorLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        errorLabel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        errorLabel.setEnabled(false);
        Color usualWindowBkg = UIManager.getColor("window"); // NOI18N
        errorLabel.setBackground(
                usualWindowBkg != null ? usualWindowBkg : Color.white);
        errorLabel.setOpaque(true);
        return errorLabel;
    }

    public void closeView() {
        if (mScene != null) {
            // release scene
            mScene.cleanup();
            mScene = null;
        }
        if (mModelListener != null) {
            mModelListener.cleanup();
        }
    }
    
    public CasaModelGraphScene getScene() {
        return mScene;
    }
}
