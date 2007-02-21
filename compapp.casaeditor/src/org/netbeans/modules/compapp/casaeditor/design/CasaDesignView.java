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

/*
* CasaDesignView.java
*
* Created on November 2, 2006, 4:31 PM
*
* To change this template, choose Tools | Template Manager
* and open the template in the editor.
*/

package org.netbeans.modules.compapp.casaeditor.design;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Properties;
import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import javax.swing.*;

import org.netbeans.modules.compapp.casaeditor.CasaDataObject;
import org.netbeans.modules.compapp.casaeditor.graph.RegionUtilities;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel;
import org.netbeans.modules.compapp.casaeditor.nodes.CasaNodeFactory;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 *
 * @author Josh Sandusky
 */
public class CasaDesignView {

    private CasaDataObject mDataObject;
    private CasaModelGraphScene mScene;
    private CasaDesignController mController;
    private JScrollPane mScroller;

    private JToolBar mToolBar;
    private Action mAutoLayoutAction;
    private Action mBuildAction;
    private Action mCustomizeAction;


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
            mScene = new CasaModelGraphScene(model, new CasaNodeFactory(mDataObject, model));
            view = mScene.getViewComponent();
            initializeSceneForDesignView();
        }
        
        mScroller.setViewportView(view);
        
        setupActions();
        setupToolBar();

        // Render the model
        if (model != null) {
            CasaModelGraphUtilities.renderModel(model, mScene);

            // Tie in the controller
            mController = new CasaDesignController(mDataObject, mScene);
            mScene.registerController(mController);
        }
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
        mAutoLayoutAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                mScene.autoLayout(true);
            }
        };
        mBuildAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                mScene.getModel().buildCompApp();
            }
        };
        mCustomizeAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                // customize L&F
                // todo: pop up a dialog with LF customization options..
                JFileChooser fc = new JFileChooser();
                fc.showOpenDialog(WindowManager.getDefault().getMainWindow());
                File selFile = fc.getSelectedFile();
                if (selFile != null && selFile.exists()) {
                    Properties ps = new Properties();
                    try {
                        ps.load(new FileInputStream(selFile));
                        //CasaFactory.getCasaCustomizer().loadFromProperties(ps);
                    } catch (IOException ex) {
                        ErrorManager.getDefault().notify(ex);
                    }
                }
            }
        };
    }

    private void setupToolBar() {
        mToolBar = new JToolBar();
        mToolBar.addSeparator();

        mToolBar.add(createButton(mAutoLayoutAction,
                                  "TXT_AutoLayout", // NOI18N
                                  "resources/auto_layout.png")); // NOI18N
        mToolBar.addSeparator();

        mToolBar.add(createButton(mBuildAction,
                                  "TXT_Build", // NOI18N
                                  "resources/build_project.png")); // NOI18N

        // TODO should not go on the toolbar as per HFE
//        mToolBar.add(createButton(mCustomizeAction,
//                                  "TXT_Customize", // NOI18N
//                                  "resources/style16.png")); // NOI18N
    }

    private JButton createButton(Action action, String tooltip, String icon) {
        JButton button = new JButton(action);
        button.setToolTipText(NbBundle.getMessage(CasaDesignView.class, tooltip)); // NOI18N
        button.setIcon(new ImageIcon(CasaDesignView.class.getResource(icon))); // NOI18N
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
        errorLabel.setText(NbBundle.getMessage(getClass(), "MSG_ModelError"));
        errorLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        errorLabel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        errorLabel.setEnabled(false);
        Color usualWindowBkg = UIManager.getColor("window"); //NOI18N
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
        if (mController != null) {
            mController.cleanup();
        }
    }
    
    public CasaModelGraphScene getScene() {
        return mScene;
    }
}
