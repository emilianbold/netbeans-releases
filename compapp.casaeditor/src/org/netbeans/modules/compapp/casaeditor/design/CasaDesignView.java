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
package org.netbeans.modules.compapp.casaeditor.design;

import java.awt.Color;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.*;

import org.netbeans.modules.compapp.casaeditor.CasaDataObject;
import org.netbeans.modules.compapp.casaeditor.graph.CasaFactory;
import org.netbeans.modules.compapp.casaeditor.graph.RegionUtilities;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel;
import org.netbeans.modules.compapp.casaeditor.nodes.CasaNodeFactory;
import org.netbeans.modules.compapp.casaeditor.nodes.actions.AutoLayoutAction;
import org.netbeans.modules.compapp.casaeditor.nodes.actions.BuildAction;
import org.netbeans.modules.compapp.casaeditor.nodes.actions.CasaValidateAction;
import org.netbeans.modules.compapp.casaeditor.nodes.actions.DeployAction;
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
    private AbstractAction mDeployAction;
    private AbstractAction mValidateAction;

    
    public CasaDesignView(CasaDataObject dataObject) {
        mDataObject = dataObject;

        mScroller = new JScrollPane();
        mScroller.getVerticalScrollBar().setUnitIncrement(30);
        mScroller.getHorizontalScrollBar().setUnitIncrement(20);

        // #138971
        LookAndFeel laf = UIManager.getLookAndFeel();
        String lafID = laf.getID();
        if (lafID.equals("GTK")  // NOI18N
//                || lafID.equals("Nimbus")
                || "true".equalsIgnoreCase(System.getProperty("casa.scrollbars.always"))) { // NOI18N
            mScroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
            mScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        }
        
        final CasaWrapperModel model = mDataObject.getEditorSupport().getModel(); 
        if (model != null) {
            // validate after casa view is shown
            if (!CasaFactory.getCasaCustomizer().getBOOLEAN_DISABLE_VALIDATION()) {
                new CasaValidateAction(model).actionPerformed(null);
            }
        }
      
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
            @Override
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
        CasaWrapperModel model = mScene.getModel();
        mAutoLayoutAction = new AutoLayoutAction(mDataObject);
        mBuildAction = new BuildAction(model);
        mDeployAction = new DeployAction(model);
        mValidateAction = new CasaValidateAction(model);
    }

    private void setupToolBar() {
        mToolBar = new JToolBar();
        mToolBar.addSeparator();

        mToolBar.add(createButton(mAutoLayoutAction,
                                  (String) mAutoLayoutAction.getValue(Action.NAME), 
                                  (Icon)   mAutoLayoutAction.getValue(Action.SMALL_ICON))); 
        mToolBar.addSeparator();

        mToolBar.add(createButton(mBuildAction,
                                  (String) mBuildAction.getValue(Action.NAME), 
                                  (Icon)   mBuildAction.getValue(Action.SMALL_ICON))); 
        
        mToolBar.add(createButton(mDeployAction,
                                  (String) mDeployAction.getValue(Action.NAME),
                                  (Icon)   mDeployAction.getValue(Action.SMALL_ICON)));
       
        mToolBar.addSeparator();
        mToolBar.add(createButton(mValidateAction,
                                  (String) mValidateAction.getValue(Action.NAME),
                                  (Icon)   mValidateAction.getValue(Action.SMALL_ICON)));
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
