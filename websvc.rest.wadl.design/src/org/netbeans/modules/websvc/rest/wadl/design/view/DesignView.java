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
package org.netbeans.modules.websvc.rest.wadl.design.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseWheelListener;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.ComponentWidget;
import org.netbeans.api.visual.widget.EventProcessingType;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.SeparatorWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.websvc.rest.wadl.design.Util;
import org.netbeans.modules.websvc.rest.wadl.design.multiview.MultiViewSupport;
import org.netbeans.modules.websvc.rest.wadl.model.WadlModel;
import org.netbeans.modules.websvc.rest.wadl.design.view.widget.ButtonWidget;
import org.netbeans.modules.websvc.rest.wadl.design.view.widget.ListImageWidget;
import org.netbeans.modules.websvc.rest.wadl.design.view.widget.ListMethodsWidget;
import org.netbeans.modules.websvc.rest.wadl.design.view.widget.ResourcesWidget;
import org.netbeans.modules.websvc.rest.wadl.design.view.widget.TabImageWidget;
import org.netbeans.modules.websvc.rest.wadl.model.Resources;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;

/**
 * WebService Designer
 *
 * @author Ayub Khan
 */
public class DesignView extends JPanel {

    public static final Object messageLayerKey = new Object();
    private FileObject implementationClass;
    private WadlModel wadlModel;
    /** Manages the state of the widgets and corresponding objects. */
    private ObjectScene scene;
    /** Manages the zoom level. */
    private ZoomManager zoomer;
    private Widget mainLayer;
    private Widget messageWidget;
//    private LabelWidget addUrlWidget;
    private Widget addUrlWidget;
    private Widget headerWidget;
    private Widget buttons;
    private Widget contentWidget;
    private Widget mainWidget;
    private Widget separatorWidget;
    private transient ResourcesWidget resourcesWidget;
    private static volatile ListMethodsWidget listWidget;
    private transient ButtonWidget viewButton;

    /**
     * Creates a new instance of GraphView.
     * @param service
     * @param implementationClass
     */
    public DesignView(final WadlModel wadlModel, FileObject implementationClass) {
        super(new BorderLayout());

        this.setPreferredSize(new Dimension(80, 40));

        this.wadlModel = wadlModel;
        this.implementationClass = implementationClass;

        scene = new ObjectScene() {

            @Override
            /**
             * Use our own traversal policy
             */
            public Comparable<DesignerWidgetIdentityCode> getIdentityCode(Object object) {
                return new DesignerWidgetIdentityCode(scene, object);
            }
        };
        final JComponent sceneView = scene.createView();
        zoomer = new ZoomManager(scene);

        scene.getActions().addAction(ActionFactory.createCycleObjectSceneFocusAction());
        scene.setKeyEventProcessingType(EventProcessingType.FOCUSED_WIDGET_AND_ITS_PARENTS);

        mainLayer = new LayerWidget(scene);
        mainLayer.setPreferredLocation(new Point(0, 0));
        mainLayer.setLayout(LayoutFactory.createVerticalFlowLayout(
                LayoutFactory.SerialAlignment.JUSTIFY, 12));
        scene.addChild(mainLayer);

        mainWidget = new Widget(scene);
        mainWidget.setLayout(LayoutFactory.createVerticalFlowLayout(
                LayoutFactory.SerialAlignment.JUSTIFY, 12));

        headerWidget = new Widget(scene);
        headerWidget.setLayout(LayoutFactory.createHorizontalFlowLayout(
                LayoutFactory.SerialAlignment.JUSTIFY, 12));
        mainWidget.addChild(headerWidget);

        separatorWidget = new SeparatorWidget(scene,
                SeparatorWidget.Orientation.HORIZONTAL);
        separatorWidget.setForeground(Color.ORANGE);
        mainWidget.addChild(separatorWidget);


        contentWidget = new Widget(scene);
        contentWidget.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        contentWidget.setLayout(LayoutFactory.createVerticalFlowLayout(
                LayoutFactory.SerialAlignment.JUSTIFY, 16));
        mainWidget.addChild(contentWidget);

        setWadlView();

        sceneView.removeMouseWheelListener((MouseWheelListener) sceneView);
        final JScrollPane panel = new JScrollPane(sceneView);
        panel.getVerticalScrollBar().setUnitIncrement(16);
        panel.getHorizontalScrollBar().setUnitIncrement(16);
        panel.setBorder(null);
        add(panel);
        mainLayer.addChild(mainWidget);

        messageWidget = new Widget(scene);
        messageWidget.setLayout(LayoutFactory.createVerticalFlowLayout(
                LayoutFactory.SerialAlignment.JUSTIFY, 4));
        mainLayer.addChild(messageWidget);
        scene.addObject(messageLayerKey, messageWidget);

        scene.addSceneListener(new ObjectScene.SceneListener() {

            public void sceneRepaint() {
            }

            public void sceneValidating() {
            }

            public void sceneValidated() {
                int width = panel.getViewport().getWidth();
                if (width <= scene.getBounds().width) {
                    mainWidget.setMinimumSize(new Dimension(width, 0));
                }
            }
        });

        // vlv: print
        getContent().putClientProperty("print.printable", Boolean.TRUE); // NOI18N
        }
//    
//    public static ListMethodsWidget getListMethodsWidget() {
//        return listWidget;
//    }
    /**
     * Adds the graph actions to the given toolbar.
     *
     * @param  toolbar  to which the actions are added.
     */
    public void addToolbarActions(JToolBar toolbar) {
        toolbar.addSeparator();
        zoomer.addToolbarActions(toolbar);
        toolbar.addSeparator();
//        resourcesWidget.addToolbarActions(toolbar);
    }

    /**
     * Return the view content, suitable for printing (i.e. without a
     * scroll pane, which would result in the scroll bars being printed).
     *
     * @return  the view content, sans scroll pane.
     */
    public JComponent getContent() {
        return scene.getView();
    }

    public void requestFocus() {
        super.requestFocus();
        // Ensure the graph widgets have the focus.
        scene.getView().requestFocus();
    }

    public boolean requestFocusInWindow() {
        super.requestFocusInWindow();
        // Ensure the graph widgets have the focus.
        return scene.getView().requestFocusInWindow();
    }

    protected ObjectScene getObjectScene() {
        return (ObjectScene) scene;
    }

    private void setWadlView() {
        try {
//            if (!wadlModel.getState().equals(Model.State.VALID)) {
//                throw new IOException("Wadl model is invalid.");
//            }
            boolean wadlView = viewButton == null || !viewButton.isSelected();
            if (viewButton == null || viewButton.isSelected() != wadlView) {
                if (viewButton != null) {
                    viewButton.setSelected(wadlView);
                }

                if (listWidget != null && listWidget.getParentWidget() == contentWidget) {
                    contentWidget.removeChild(listWidget);
                }
                if (resourcesWidget != null && resourcesWidget.getParentWidget() == contentWidget) {
                    contentWidget.removeChild(resourcesWidget);
                }
                if (wadlView) {
                    //add resources widget
                    resourcesWidget = new ResourcesWidget(scene, wadlModel);
                    contentWidget.addChild(resourcesWidget);
                } else {
                    listWidget = new ListMethodsWidget(scene, 16, wadlModel);
                    contentWidget.addChild(listWidget);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            reportError(ex);
        }
        if(viewButton == null) {
            updateHeaderWidget();
            viewButton.setSelected(true);
        }
    }

    private void refreshWadlView() {
        if (resourcesWidget != null && resourcesWidget.getParentWidget() == contentWidget) {
            contentWidget.removeChild(resourcesWidget);
        }
        try {
            //add resources widget
            resourcesWidget = new ResourcesWidget(scene, wadlModel);
            contentWidget.addChild(resourcesWidget);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void reportError(Exception ex) {
        headerWidget.removeChildren();
        final JLabel lbServiceUrl = new JLabel(NbBundle.getMessage(MultiViewSupport.class, "MSG_InvalidWadl"));
        lbServiceUrl.setPreferredSize(new Dimension(700, 20));
        lbServiceUrl.setFont(getObjectScene().getFont().deriveFont(Font.BOLD));
        ComponentWidget serviceUrlLabel = new ComponentWidget(scene, lbServiceUrl);
        headerWidget.addChild(serviceUrlLabel);
        getObjectScene().validate();
    }

    private void updateHeaderWidget() {
        final JLabel lbServiceUrl = new JLabel(NbBundle.getMessage(DesignView.class, "LBL_ServiceUrl"));
        lbServiceUrl.setPreferredSize(new Dimension(300, 20));
//        lbServiceUrl.setFont(getObjectScene().getFont().deriveFont(Font.BOLD));
        ComponentWidget serviceUrlLabel = new ComponentWidget(scene, lbServiceUrl);

        final Resources resources = wadlModel.getApplication().getResources().iterator().next();
        String serviceUrl = resources.getBase();
        JTextField tfServiceUrl = new JTextField(serviceUrl, 50);
        tfServiceUrl.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                final JTextField tf = (JTextField) e.getSource();
                if (viewButton.isSelected()) {// hierarchy
                    try {
                        wadlModel.startTransaction();
                        resources.setBase(tf.getText());
                    } finally {
                        wadlModel.endTransaction();
                    }
                    refreshWadlView();
                    getObjectScene().validate();
                } else {
                    Task task = new Task(new Runnable() {

                        public void run() {
                            try {
                                Util.addResource(tf.getText(), wadlModel);
                                setWadlView();
                                setWadlView();
                                getObjectScene().validate();
                            } catch (Exception e) {
                                ErrorManager.getDefault().notify(e);
                            }
                        }
                    });
                    RequestProcessor.getDefault().post(task);
                }
            }
        });

        addUrlWidget = new ComponentWidget(scene, tfServiceUrl);
//        addUrlWidget.setLayout(LayoutFactory.createHorizontalFlowLayout(
//                LayoutFactory.SerialAlignment.JUSTIFY, 1));
//        addUrlWidget.setFont(scene.getFont().deriveFont(Font.BOLD));
//        addUrlWidget.setForeground(Color.GRAY);
//        addUrlWidget.setBorder(BorderFactory.createEmptyBorder(6, 28, 0, 0));
        addUrlWidget.setToolTipText(NbBundle.getMessage(DesignView.class, "HINT_ServiceUrl"));

        buttons = new Widget(scene);
        buttons.setLayout(LayoutFactory.createHorizontalFlowLayout(
                LayoutFactory.SerialAlignment.CENTER, 8));

        viewButton = new ButtonWidget(scene, null, null);
        viewButton.setImage(new ListImageWidget(scene, 16));
        viewButton.setSelectedImage(new TabImageWidget(scene, 16));
        viewButton.setAction(new AbstractAction() {

            public void actionPerformed(ActionEvent arg0) {
                if (!viewButton.isSelected()) {// hierarchy
                    lbServiceUrl.setText(
                            NbBundle.getMessage(DesignView.class, "LBL_ServiceUrl"));
                    lbServiceUrl.setToolTipText(
                            NbBundle.getMessage(DesignView.class, "HINT_ServiceUrl"));
                } else {
                    lbServiceUrl.setText(
                            NbBundle.getMessage(DesignView.class, "LBL_AddService"));
                    lbServiceUrl.setToolTipText(
                            NbBundle.getMessage(DesignView.class, "HINT_AddService"));
                }
                setWadlView();
            }
        });
        buttons.addChild(viewButton);

        headerWidget.addChild(buttons);
        headerWidget.addChild(serviceUrlLabel);
        headerWidget.addChild(addUrlWidget);
    }
}
