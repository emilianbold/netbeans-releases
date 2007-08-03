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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.jsf.navigation.graph.actions;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JOptionPane;
import org.netbeans.modules.web.jsf.navigation.PageFlowController;
import org.netbeans.modules.web.jsf.navigation.graph.PageFlowScene;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.ConnectDecorator;
import org.netbeans.api.visual.action.InplaceEditorProvider;
import org.netbeans.api.visual.action.InplaceEditorProvider.EditorController;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.vmd.VMDConnectionWidget;
import org.netbeans.api.visual.vmd.VMDNodeWidget;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.action.PopupMenuProvider;
import org.netbeans.modules.web.jsf.navigation.NavigationCaseEdge;
import org.netbeans.modules.web.jsf.navigation.Page;
import org.netbeans.modules.web.jsf.navigation.Pin;
import org.netbeans.modules.web.jsf.navigation.graph.PageFlowSceneElement;
import org.netbeans.modules.web.jsf.navigation.graph.layout.SceneElementComparator;
import org.openide.cookies.OpenCookie;

/**
 *
 * @author joelle
 */
public class MapActionUtility {

    /** Creates a new instance of MapActionUtility */
    public MapActionUtility() {
    }


    public static ActionMap initActionMap() {
        ActionMap actionMap = new ActionMap();
        // Install the actions
        actionMap.put("handleTab", handleTab);
        actionMap.put("handleCtrlTab", handleCtrlTab);
        actionMap.put("handleEscape", handleEscape);

        actionMap.put("handleLinkStart", handleLinkStart);
        actionMap.put("handleLinkEnd", handleLinkEnd);
        //
        actionMap.put("handleZoomPage", handleZoomPage);
        actionMap.put("handleUnZoomPage", handleZoomPage);
        actionMap.put("handleOpenPage", handleOpenPage);
        //
        //        actionMap.put("handleNewWebForm", new TestAction("handleNewWebForm"));
        //
        actionMap.put("handleLeftArrowKey", handleCtrlTab);
        actionMap.put("handleRightArrowKey", handleTab);
        actionMap.put("handleUpArrowKey", handleUpArrow);
        actionMap.put("handleDownArrowKey", handleDownArrow);


        actionMap.put("handleRename", handleRename);
        actionMap.put("handlePopup", handlePopup);
        return actionMap;
    }

    public static InputMap initInputMap() {
        InputMap inputMap = new InputMap();
        // Tab Key
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), "handleTab");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_MASK), "handleCtrlTab");
        // Esc Key
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "handleEscape");
        //
        //        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_A,InputEvent.CTRL_MASK), "handleNewWebForm");
        //
        //Lower Case s,e,z,u
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0), "handleLinkStart");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_E, 0), "handleLinkEnd");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, 0), "handleZoomPage");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_U, 0), "handleUnZoomPage");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_P, 0), "handlePopup");
        //
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "handleOpenPage");

        // Upper Case S,E,Z,U
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.SHIFT_MASK), "handleLinkStart");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.SHIFT_MASK), "handleLinkEnd");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.SHIFT_MASK), "handleZoomPage");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.SHIFT_MASK), "handleUnZoomPage");

        // Upper and Lower Case R (rename)
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_MASK), "handleRename");

        //        // Non Numeric Key Pad arrow keys
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "handleLeftArrowKey");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "handleRightArrowKey");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "handleUpArrowKey");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "handleDownArrowKey");
        //
        // Numeric Key Pad arrow keys
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_LEFT, 0), "handleLeftArrowKey");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_RIGHT, 0), "handleRightArrowKey");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_UP, 0), "handleUpArrowKey");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_DOWN, 0), "handleDownArrowKey");
        //        // SHIFT + F10
        //        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F10,InputEvent.SHIFT_MASK), "handlePopupMenu");
        //
        //
        //        //Add File
        //        inputMap.put(KeyStroke.getKeyStroke( KeyEvent.VK_A, 0, false), "handleNewWebForm");
        //        //DELETE
        //        inputMap.put(KeyStroke.getKeyStroke( KeyEvent.VK_DELETE , 0), "handleDeleteKey");
        return inputMap;
    }

    // Handle Escape - cancels the link action
    public static Action handleEscape = new AbstractAction() {

        public void actionPerformed(ActionEvent e) {
            /* Cancel A11y Linking */
            Object sourceObj = e.getSource();
            if (!(sourceObj instanceof PageFlowScene)) {
                return;
            }
            PageFlowScene scene = (PageFlowScene) sourceObj;
            if (CONNECT_WIDGET != null) {
                CONNECT_WIDGET.removeFromParent();
                CONNECT_WIDGET = null;
            }
        }
    };

    // Handle Rename
    public static Action handleRename = new AbstractAction() {

        public void actionPerformed(ActionEvent e) {
            /* Cancel A11y Linking */
            Object sourceObj = e.getSource();
            if (!(sourceObj instanceof PageFlowScene)) {
                return;
            }
            PageFlowScene scene = (PageFlowScene) sourceObj;
            Set selectedObjects = scene.getSelectedObjects();
            if (selectedObjects.size() > 0) {
                PageFlowSceneElement selectedObj = (PageFlowSceneElement) selectedObjects.toArray()[0];
                Widget widget = scene.findWidget(selectedObj);
                assert widget != null;
                EditorController controller = null;
                if (widget instanceof VMDNodeWidget) {
                    LabelWidget labelWidget = ((VMDNodeWidget) widget).getNodeNameWidget();
                    controller = findEditorController(labelWidget.getActions().getActions());
                    if (controller != null) {
                        controller.openEditor(labelWidget);
                    }
                } else if (widget instanceof VMDConnectionWidget) {
                    List<Widget> childWidgets = widget.getChildren();
                    for (Widget childWidget : childWidgets) {
                        if (childWidget instanceof LabelWidget) {
                            controller = findEditorController(childWidget.getActions().getActions());
                            if (controller != null) {
                                controller.openEditor(childWidget);
                            }
                        }
                    }
                }
            }
        }

        public EditorController findEditorController(List<WidgetAction> actionList) {
            for (WidgetAction action : actionList) {
                if (action instanceof InplaceEditorProvider.EditorController) {
                    EditorController controller = ActionFactory.getInplaceEditorController(action);
                    return controller;
                }
            }
            return null;
        }
    };

    public static final Action handleTab = new AbstractAction() {

        public void actionPerformed(ActionEvent e) {
            boolean reverse = false;
            handleTabActionEvent(e, reverse);
        }
    };
    public static final Action handleCtrlTab = new AbstractAction() {

        public void actionPerformed(ActionEvent e) {
            boolean reverse = true;
            handleTabActionEvent(e, reverse);
        }
    };

    private static final void handleTabActionEvent(ActionEvent e, boolean reverse) {

        Object sourceObj = e.getSource();
        if (!(sourceObj instanceof PageFlowScene)) {
            return;
        }
        PageFlowScene scene = (PageFlowScene) sourceObj;
        handleTab(scene, reverse);
    }

    private static final void handleTab(PageFlowScene scene, boolean reverse) {

        PageFlowSceneElement nextElement = SceneElementComparator.getNextSelectableElement(scene, reverse, true, true, false);
        if (nextElement != null) {
            if (CONNECT_WIDGET != null && scene.getConnectionLayer().getChildren().contains(CONNECT_WIDGET)) {
                Anchor targetAnchor = null;
                if (nextElement instanceof Page) {
                    assert CONNECT_DECORATOR_DEFAULT != null;
                    targetAnchor = CONNECT_DECORATOR_DEFAULT.createTargetAnchor(scene.findWidget(nextElement));
                } else if (nextElement instanceof Pin) {
                    Widget pageWidget = scene.findWidget(((Pin) nextElement).getPage());
                    targetAnchor = CONNECT_DECORATOR_DEFAULT.createTargetAnchor(pageWidget);
                }
                if (targetAnchor != null) {
                    CONNECT_WIDGET.setTargetAnchor(targetAnchor);
                    scene.validate();
                }
            }
            Set<PageFlowSceneElement> set = new HashSet<PageFlowSceneElement>();
            set.add(nextElement);
            scene.setHoveredObject(nextElement); //Do this because the popup action is looking for hovered.
            scene.setSelectedObjects(set);
        } else {
            scene.setSelectedObjects(new HashSet());
            scene.setHoveredObject(null); //Not sure if I can do this yet.
        }
    }

    public static final Action handleDownArrow = new AbstractAction() {

        public void actionPerformed(ActionEvent e) {
            Object sourceObj = e.getSource();
            if (!(sourceObj instanceof PageFlowScene)) {
                return;
            }
            PageFlowScene scene = (PageFlowScene) sourceObj;
            boolean reverse = false;
            handleArrow(scene, reverse);
        }
    };


    private static final void handleArrow(PageFlowScene scene, boolean reverse) {
        PageFlowSceneElement nextElement = SceneElementComparator.getNextSelectableElement(scene, reverse, false, false, true);
        if (nextElement != null) {
            if (CONNECT_WIDGET != null && scene.getConnectionLayer().getChildren().contains(CONNECT_WIDGET)) {
                Anchor targetAnchor = null;
                if (nextElement instanceof Page) {
                    assert CONNECT_DECORATOR_DEFAULT != null;
                    targetAnchor = CONNECT_DECORATOR_DEFAULT.createTargetAnchor(scene.findWidget(nextElement));
                } else if (nextElement instanceof Pin) {
                    Widget pageWidget = scene.findWidget(((Pin) nextElement).getPage());
                    targetAnchor = CONNECT_DECORATOR_DEFAULT.createTargetAnchor(pageWidget);
                }
                if (targetAnchor != null) {
                    CONNECT_WIDGET.setTargetAnchor(targetAnchor);
                    scene.validate();
                }
            }
            Set<PageFlowSceneElement> set = new HashSet<PageFlowSceneElement>();
            set.add(nextElement);
            scene.setSelectedObjects(set);
        }
    }

    public static final Action handleUpArrow = new AbstractAction() {

        public void actionPerformed(ActionEvent e) {
            Object sourceObj = e.getSource();
            if (!(sourceObj instanceof PageFlowScene)) {
                return;
            }
            PageFlowScene scene = (PageFlowScene) sourceObj;
            boolean reverse = true;
            handleArrow(scene, reverse);
        }
    };

    private static ConnectDecorator CONNECT_DECORATOR_DEFAULT = null;
    private static ConnectionWidget CONNECT_WIDGET = null;
// Handle Link Start Key Stroke
    public static Action handleLinkStart = new AbstractAction() {

        public void actionPerformed(ActionEvent e) {
            Object sourceObj = e.getSource();
            if (sourceObj instanceof PageFlowScene) {
                PageFlowScene scene = (PageFlowScene) sourceObj;
                List<Object> elements = new ArrayList<Object>(scene.getSelectedObjects());
                if (elements.size() > 0) {
                    PageFlowSceneElement selElement = (PageFlowSceneElement) elements.get(0);
                    Widget selWidget = null;
                    if (selElement instanceof Page) {
                        selWidget = scene.findWidget(selElement);
                        //Pin selPin = scene.getDefaultPin((Page) selElement);
                    } else if (selElement instanceof Pin) {
                        //Pin selPin = (Pin) selElement;
                        selWidget = scene.findWidget((Pin) selElement);
                    }
                    if (selWidget != null) {
                        CONNECT_DECORATOR_DEFAULT = ActionFactory.createDefaultConnectDecorator();
                        CONNECT_DECORATOR_DEFAULT.createTargetAnchor(selWidget);
                        CONNECT_WIDGET = CONNECT_DECORATOR_DEFAULT.createConnectionWidget(scene);
                        System.out.println("Connection Widget: " + CONNECT_WIDGET);
                        CONNECT_WIDGET.setSourceAnchor(CONNECT_DECORATOR_DEFAULT.createSourceAnchor(selWidget));
                        System.out.println("Source Anchor: " + CONNECT_WIDGET.getSourceAnchor());
                        CONNECT_WIDGET.setTargetAnchor(CONNECT_DECORATOR_DEFAULT.createSourceAnchor(selWidget));
                        System.out.println("Target Anchor: " + CONNECT_WIDGET.getTargetAnchor());
                        scene.getConnectionLayer().addChild(CONNECT_WIDGET);
                        scene.validate();
                    }
                }
            }
        }
    };

// Handle Escape - cancels the link action
    public static Action handleLinkEnd = new AbstractAction() {

        public void actionPerformed(ActionEvent e) {
            /* Cancel A11y Linking */
            Object sourceObj = e.getSource();
            if (!(sourceObj instanceof PageFlowScene)) {
                return;
            }
            PageFlowScene scene = (PageFlowScene) sourceObj;
            if (CONNECT_WIDGET != null) {

                Anchor sourceAnchor = CONNECT_WIDGET.getSourceAnchor();
                Anchor targetAnchor = CONNECT_WIDGET.getTargetAnchor();
                if (sourceAnchor != null && targetAnchor != null) {
                    /* Figure out source */
                    Object sourceObject = scene.findObject(CONNECT_WIDGET.getSourceAnchor().getRelatedWidget());
                    Page sourcePage = null;
                    Pin sourcePin = null;
                    if (scene.isPin(sourceObject)) {
                        sourcePin = (Pin) sourceObject;
                        sourcePage = (sourcePin).getPage();
                    }
                    if (scene.isNode(sourceObject)) {
                        sourcePage = (Page) sourceObject;
                    }

                    /* Figure out target */
                    Object targetObject = scene.findObject(CONNECT_WIDGET.getTargetAnchor().getRelatedWidget());
                    Page targetPage = null;
                    if (scene.isPin(targetObject)) {
                        targetPage = ((Pin) targetObject).getPage();
                    }
                    if (scene.isNode(targetObject)) {
                        targetPage = (Page) targetObject;
                    }

                    if (sourcePage != null && targetPage != null) {
                        scene.getPageFlowView().getPageFlowController().createLink(sourcePage, targetPage, sourcePin);
                    }
                    CONNECT_WIDGET.removeFromParent();
                    CONNECT_WIDGET = null;
                    scene.validate();
                }
            }
        }
    };

    public static final Action handleOpenPage = new AbstractAction() {

        public void actionPerformed(ActionEvent e) {
            Object sourceObj = e.getSource();
            if (sourceObj instanceof PageFlowScene) {
                PageFlowScene scene = (PageFlowScene) sourceObj;
                Set<Object> selectedObjs = new HashSet<Object>(scene.getSelectedObjects());

                for (Object obj : selectedObjs) {
                    if (obj instanceof PageFlowSceneElement) {
                        OpenCookie openCookie = ((PageFlowSceneElement) obj).getNode().getCookie(OpenCookie.class);
                        if (openCookie != null) {
                            openCookie.open();
                        }
                    }
                }
            }
        }
    };


    public static Action handleNewWebForm = new AbstractAction() {
        PageFlowScene scene;

        public void actionPerformed(ActionEvent e) {
            //            This would work if we wanted to use the wizard.
            //            Action newFileAction = CommonProjectActions.newFileAction();
            //            JOptionPane.showMessageDialog(null, "Source: " + e.getSource());
            Object obj = e.getSource();
            if (obj instanceof PageFlowScene) {
                try {
                    scene = (PageFlowScene) obj;
                    PageFlowController pfc = scene.getPageFlowView().getPageFlowController();

                    FileObject webFileObject = pfc.getWebFolder();

                    String name = FileUtil.findFreeFileName(webFileObject, "Templates/JSP_Servlet/JSP.jsp", "jsp");
                    name = JOptionPane.showInputDialog("Select Page Name", name);

                    createIndexJSP(webFileObject, name);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }

        /**
         * Creates a JSP in the
         * @param name
         * @throws java.io.IOException
         */
        private void createIndexJSP(FileObject targetFolder, String name) throws IOException {
            //            FileOwnerQuery.getOwner(webFolder)
            //            FileObject webFO = fo.createFolder(DEFAULT_DOC_BASE_FOLDER);
            //            FileObject parentFolder = project.getProjectDirectory();
            //            FileObject webFileObject = parentFolder.getFileObject("web");
            FileObject jspTemplate = Repository.getDefault().getDefaultFileSystem().findResource("Templates/JSP_Servlet/JSP.jsp"); // NOI18N
            if (jspTemplate == null) {
                return; // Don't know the template
            }

            DataObject mt = DataObject.find(jspTemplate);
            DataFolder webDf = DataFolder.findFolder(targetFolder);
            mt.createFromTemplate(webDf, name); // NOI18N
        }


        private static final String DEFAULT_DOC_BASE_FOLDER = "web"; //NOI18N
    };

// Handle Zoom Key Stroke
    public static final Action handleZoomPage = new AbstractAction() {

        public void actionPerformed(ActionEvent e) {
            Object obj = e.getSource();
            if (!(obj instanceof PageFlowScene)) {
                return;
            }
            PageFlowScene scene = (PageFlowScene) obj;
            for (Object selObj : scene.getSelectedObjects()) {
                if (selObj instanceof Page) {
                    Page selPage = (Page) selObj;
                    if (scene.isNode(selPage)) {
                        VMDNodeWidget pageWidget = (VMDNodeWidget) scene.findWidget(selPage);
                        if (pageWidget.isMinimized()) {
                            pageWidget.expandWidget();
                        } else {
                            pageWidget.collapseWidget();
                        }
                    }
                }
            }
        }
    };

// Handle UnZoom Key Stroke
    public static final Action handleUnZoomPage = new AbstractAction() {

        public void actionPerformed(ActionEvent e) {
            //            GraphEvent selectedEvent  = graphDocument.getSelectedComponents();
            //            IGraphNode[] selectedNodes = selectedEvent.getNodes();
            //            for( IGraphNode node : selectedNodes ){
            //                if( node instanceof NavigationGraphNode ) {
            //                    ((NavigationGraphNode)node).setZoomed(false);
            //                }
            //            }
        }
    };

    public static Action handlePopup = new AbstractAction() {

        public void actionPerformed(ActionEvent e) {
            Object obj = e.getSource();
            if (!(obj instanceof PageFlowScene)) {
                return;
            }
            PageFlowScene scene = (PageFlowScene) obj;
            PopupMenuProvider provider = scene.getPopupMenuProvider();
            Object[] selObjs = scene.getSelectedObjects().toArray();

            Object selObj = (selObjs.length > 0) ? selObjs[0] : null;
            Widget selectedWidget;
            Point popupPoint;
            if (selObj instanceof PageFlowSceneElement) {
                selectedWidget = scene.findWidget(selObj);
                assert selectedWidget != null;
                
                /* Because you cannot use getLocation on a connectionwidget, I need to grab it's source pin for the location */
                if (selObj instanceof NavigationCaseEdge) {
                    NavigationCaseEdge edge = (NavigationCaseEdge) selObj;                    
                    VMDConnectionWidget connectionWidget = (VMDConnectionWidget) scene.findWidget(edge);
                    popupPoint = connectionWidget.getFirstControlPoint();
                } else {
                    popupPoint = selectedWidget.getLocation();
                }
            } else {
                Rectangle rectangleScene = scene.getClientArea();
                popupPoint = scene.convertSceneToLocal(new Point(rectangleScene.width / 2, rectangleScene.height / 2));
                selectedWidget = scene;
            }
            assert selectedWidget != null;
            assert popupPoint != null;
            JPopupMenu popupMenu = provider.getPopupMenu(selectedWidget, popupPoint);
            if (popupMenu != null) {
                popupMenu.show(scene.getView(), popupPoint.x, popupPoint.y);
            }
        }
    };
}
