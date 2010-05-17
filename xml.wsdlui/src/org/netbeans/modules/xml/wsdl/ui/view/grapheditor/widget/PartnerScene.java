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

package org.netbeans.modules.xml.wsdl.ui.view.grapheditor.widget;

import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.Transferable;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.swing.JLabel;

import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.CycleFocusProvider;
import org.netbeans.api.visual.action.SelectProvider;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.action.WidgetAction.WidgetDropTargetDragEvent;
import org.netbeans.api.visual.action.WidgetAction.WidgetDropTargetDropEvent;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.model.ObjectSceneEvent;
import org.netbeans.api.visual.model.ObjectSceneEventType;
import org.netbeans.api.visual.model.ObjectSceneListener;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.widget.EventProcessingType;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.ui.basic.SchemaModelCookie;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.Utility;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.WSDLDataObject;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.WSDLModelCookie;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.DragOverSceneLayer;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.pastetype.SchemaImportPasteType;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.pastetype.WSDLImportPasteType;
import org.netbeans.modules.xml.xam.ComponentEvent;
import org.netbeans.modules.xml.xam.ComponentListener;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.WeakListeners;
import org.openide.util.datatransfer.PasteType;

/**
 *
 * @author anjeleevich
 */
public class PartnerScene extends ObjectScene implements ComponentListener, DnDHandler, PropertyChangeListener {
    private ButtonAction buttonAction;
    private WidgetAction selectAction;
    private DnDAction dndAction;
    private WidgetAction focusAction;
    
    private final WSDLModel model;
    private MessagesWidget messagesWidget;
    private CollaborationsWidget collaborationsWidget;
    private DragOverSceneLayer dragOverLayer;
    private Font defaultFont = new JLabel().getFont();
    private SelectProvider selectProvider;
    private PartnerSceneCycleFocusProviderAndSceneListener cycleFocusProviderAndSceneListener;
    private PartnerSceneCycleFocusProvider mycycleFocusProvider;
    private List<Widget> selectedWidgets;
    private PropertyChangeListener weakModelListener;
    private ComponentListener weakComponentListener;

    public PartnerScene(WSDLModel model) {
        super();

        this.model = model;
        setKeyEventProcessingType(EventProcessingType.FOCUSED_WIDGET_AND_ITS_CHILDREN_AND_ITS_PARENTS);
        cycleFocusProviderAndSceneListener = new PartnerSceneCycleFocusProviderAndSceneListener();
        
        mycycleFocusProvider = new PartnerSceneCycleFocusProvider();
        buttonAction = new ButtonAction();
        selectProvider = new ObjectSelectProvider();
        selectAction = ActionFactory.createSelectAction(selectProvider);
        dndAction = new DnDAction();
        focusAction = ActionFactory.createCycleFocusAction(mycycleFocusProvider);
        
        //IZ 93508 fixes mouse wheel scroll.
        getActions().addAction(ActionFactory.createWheelPanAction());
        
        getPriorActions().addAction(focusAction);
        
        //Add a scene select action, which brings focus to the graph component.
        getActions().addAction(ActionFactory.createSelectAction(new SelectProvider() {
        
            public void select(Widget widget, Point localLocation,
                    boolean invertSelection) {
                getView().requestFocusInWindow();
            }
        
            public boolean isSelectionAllowed(Widget widget, Point localLocation,
                    boolean invertSelection) {
                return true;
            }
        
            public boolean isAimingAllowed(Widget widget, Point localLocation,
                    boolean invertSelection) {
                return true;
            }
        
        }));
        getActions().addAction(buttonAction);
        getActions().addAction(dndAction);
        
        this.addObjectSceneListener(cycleFocusProviderAndSceneListener, ObjectSceneEventType.OBJECT_FOCUS_CHANGED, ObjectSceneEventType.OBJECT_SELECTION_CHANGED);
        selectedWidgets = new ArrayList<Widget>();
        dndAction.addPriorHandler(new FileDropDnDHandler());
    }
    
    @Override
    protected void notifyAdded() {
        super.notifyAdded();
        weakModelListener = WeakListeners.propertyChange(this, model);
        model.addPropertyChangeListener(weakModelListener);
        weakComponentListener = WeakListeners.create(ComponentListener.class, this, model);
        model.addComponentListener(weakComponentListener);
    }
    
    @Override
    protected void notifyRemoved() {
        super.notifyRemoved();
        if (weakModelListener != null) {
            model.removePropertyChangeListener(weakModelListener);
            weakModelListener = null;
        }
        
        if (weakComponentListener != null) {
            model.removeComponentListener(weakComponentListener);
            weakComponentListener = null;
        }
    }
    
    /*private Logger mLogger = Logger.getLogger(PartnerScene.class.getName());
    protected void printFocusCycle(Widget widget, String indent, boolean onlyFocusables, boolean onlyFocusableTrue) {
        for (Widget w : widget.getChildren()) {
            if (w instanceof FocusableWidget) {
                if (!onlyFocusableTrue) {
                    mLogger.log(Level.SEVERE, indent + w + ": Focusable : " + ((FocusableWidget)w).isFocusable());
                } else if (((FocusableWidget)w).isFocusable()){
                    mLogger.log(Level.SEVERE, indent + w + ": Focusable : "); 
                }
            } else if (!onlyFocusables){
                mLogger.log(Level.SEVERE, indent + w.toString());
            }
            printFocusCycle(w, indent  + "--", onlyFocusables, onlyFocusableTrue);
        }
    }*/

    public WSDLModel getModel() {
        return model;
    }
    
    
    /**
     * Return the provider for performing widget selections.
     *
     * @return  selection provider.
     */
    public SelectProvider getSelectProvider() {
        return selectProvider;
    }
    
    public ButtonAction getButtonAction() {
        return buttonAction;
    }

    public WidgetAction getSelectAction() {
        return selectAction;
    }
    
    
    public WidgetAction getDnDAction() {
        return dndAction;
    }

    
    public MessagesWidget getMessagesWidget() {
        if (messagesWidget == null) {
             messagesWidget = new MessagesWidget(this, model);
        }
        
        return messagesWidget;
    }
    
    
    public CollaborationsWidget getCollaborationsWidget() {
        if (collaborationsWidget == null) {
            collaborationsWidget = new CollaborationsWidget(this, model);
        }
        
        return collaborationsWidget;
    }
    
    public DragOverSceneLayer getDragOverLayer() {
        if (dragOverLayer == null) {
            dragOverLayer = new DragOverSceneLayer(this);
        }
        return dragOverLayer;
    }

    public void childrenAdded(ComponentEvent componentEvent) {
        //do nothing
    }
    
    public void childrenDeleted(ComponentEvent componentEvent) {
        //do nothing, handled by property change
    }
    
    public void valueChanged(ComponentEvent componentEvent) {
        //do nothing.
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource() == model.getDefinitions()) {
            if (evt.getPropertyName().equals(Definitions.MESSAGE_PROPERTY)) {
                getMessagesWidget().updateContent(evt);
            } else if (evt.getPropertyName().equals(Definitions.EXTENSIBILITY_ELEMENT_PROPERTY)) {
                getCollaborationsWidget().updateContent(evt);
            }
        }
    }
    

    @Override
    public Font getDefaultFont() {
        return defaultFont;
    }

    private class ObjectSelectProvider implements SelectProvider {
        public boolean isAimingAllowed(Widget widget, Point localLocation, 
                boolean invertSelection) 
        {
            return false;
        }
        
        public boolean isSelectionAllowed(Widget widget, Point localLocation, 
                boolean invertSelection) 
        {
            return true;
        }
        

        public void select(Widget widget, Point localLocation, 
                boolean invertSelection) 
        {
            getView().requestFocusInWindow();
            Object object = findObject(widget);

            if (object != null) {
                if (!findWidgets(object).contains(widget)) {
                	return;
                }
                if (getSelectedObjects().contains(object)) {
                    return;
                }
                userSelectionSuggested(Collections.singleton(object),
                        invertSelection);
                //Expand all the widgets before it.
                Widget parent = widget.getParentWidget();
                while (parent != null) {
                    if (parent instanceof ExpandableWidget) {
                        ExpandableWidget.class.cast(parent).expandWidget();
                        //There are only two expandable widgets, Partnerlinktype and Message widgets.
                        //so break the loop, once any one is found.
                        break;
                    }
                    parent = parent.getParentWidget();
                }
                //Make the parents visible so that this widget can be seen. 
                WidgetHelper.makeWidgetVisible(widget);
                setFocusedObject (object);
                setFocusedWidget(widget);
                Rectangle bounds = widget.getClientArea();
                if (bounds != null) {
                    bounds = widget.getScene().convertSceneToView(widget.convertLocalToScene(bounds));
                    widget.getScene().getView().scrollRectToVisible(bounds);
                }
            } else {
                userSelectionSuggested(Collections.emptySet(),
                        invertSelection);
                widget.setState(widget.getState().deriveSelected(true));
                getSelectedWidgets().add(widget);
            }
            
        }
    }
    
    public void dragExit() {
        collaborationsWidget.dragExit();
        messagesWidget.dragExit();
    }

    public boolean dragOver(Point scenePoint, WidgetDropTargetDragEvent event) {
        boolean messageVisible = messagesWidget != null && messagesWidget.isVisible();
        boolean collabVisible = collaborationsWidget != null && collaborationsWidget.isVisible();
        
        if (messageVisible && collabVisible) {

            boolean collaborationsHit = 
                collaborationsWidget.isHitAt(collaborationsWidget.convertSceneToLocal(new Point(100, scenePoint.y)));

            if (collaborationsHit) {
                return collaborationsWidget.dragOver(scenePoint, event);
            }
            return messagesWidget.dragOver(scenePoint, event);
            
        } else if (messageVisible) {
            return messagesWidget.dragOver(scenePoint, event);
        } else if (collabVisible) {
            return collaborationsWidget.dragOver(scenePoint, event);
        }
        return false;
    }

    public boolean drop(Point scenePoint, WidgetDropTargetDropEvent event) {
        boolean messageVisible = messagesWidget != null && messagesWidget.isVisible();
        boolean collabVisible = collaborationsWidget != null && collaborationsWidget.isVisible();
        
        if (messageVisible && collabVisible) {

            boolean collaborationsHit = 
                collaborationsWidget.isHitAt(collaborationsWidget.convertSceneToLocal(new Point(100, scenePoint.y)));

            if (collaborationsHit) {
                return collaborationsWidget.drop(scenePoint, event);
            }
            return messagesWidget.drop(scenePoint, event);
            
        } else if (messageVisible) {
            return messagesWidget.drop(scenePoint, event);
        } else if (collabVisible) {
            return collaborationsWidget.drop(scenePoint, event);
        }
        return false;
    }

    public void expandForDragAndDrop() {
        messagesWidget.expandForDragAndDrop();
        collaborationsWidget.expandForDragAndDrop();
    }

    public boolean isCollapsed() {
        return messagesWidget.isCollapsed() || collaborationsWidget.isCollapsed();
    }
    
    @Override
    public void userSelectionSuggested(Set<?> suggestedSelectedObjects,
    		boolean invertSelection) {
        boolean unRecognizedObjectsPresent = false;
        for (Object obj : suggestedSelectedObjects) {
            if (!isObject(obj)) {
                unRecognizedObjectsPresent = true;
            }
        }
        if (!unRecognizedObjectsPresent) {
            super.userSelectionSuggested(suggestedSelectedObjects, invertSelection);
        }
    	for (Widget w : getSelectedWidgets()) {
    		w.setState(w.getState().deriveSelected(false));
    	}
    	getSelectedWidgets().clear();
    	validate();
    }
    
    protected List<Widget> getSelectedWidgets() {
    	return selectedWidgets;
    }
    
    static public class PartnerSceneCycleFocusProvider implements CycleFocusProvider {
        
        public boolean switchPreviousFocus (Widget widget) {
            Scene scene = widget.getScene ();
            return scene instanceof PartnerScene  &&  switchFocus ((PartnerScene) scene, false);
        }

        public boolean switchNextFocus (Widget widget) {
            Scene scene = widget.getScene ();
            return scene instanceof PartnerScene  &&  switchFocus ((PartnerScene) scene, true);
        }
        
        private boolean switchFocus (PartnerScene scene, boolean forwardDirection) {
            //Object object = scene.getFocusedObject();
            Widget widget = scene.getFocusedWidget();
            Widget next = null;
            
            if (forwardDirection) {
                if (widget == null) {
                    next = getFirstFocusableWidget(scene);
                } else {
                    next = getNextFocusableWidget(widget);
                    if (next == null) {
                        next = getFirstFocusableWidget(scene);
                    }
                }
            } else {
                if (widget == null) {
                    next = getLastFocusableWidget(scene);
                } else {
                    next = getPreviousFocusableWidget(widget);
                    if (next == null) {
                        next = getLastFocusableWidget(scene);
                    }
                }
            }
            if (next != null) {
                scene.setFocusedWidget(next);
                Object obj = scene.findObject(next);
                if (obj != null && scene.findWidgets(obj).contains(next)) { 
                	scene.setFocusedObject(obj);
                } else {
                    scene.userSelectionSuggested(Collections.emptySet(), false);
                    next.setState(next.getState().deriveSelected(true));
                    scene.getSelectedWidgets().add(next);
                    scene.setFocusedObject(null);
                    scene.setFocusedWidget(next);
                }

                return true;
            }
            
            return false;
        }
        
        private Widget getFirstFocusableWidget(PartnerScene scene) {
            return getNextFocusableWidgetInChildren(scene);
        }
        
        
        
        private Widget getNextFocusableWidget(Widget widget) {
            Widget next = getNextFocusableWidgetInChildren(widget);
            if (next == null) {
                next = getNextFocusableWidgetInSiblings(widget);
            }
            if (next != null) return next;
            
            return null;
        }
        
        private Widget getPreviousFocusableWidget(Widget widget) {
            return getPreviousFocusableWidgetInSiblings(widget);
        }

        private Widget getNextFocusableWidgetInSiblings(Widget widget) {
            if (widget == null) return null;
            
            Widget next = null;
            Widget parent = widget.getParentWidget();
            if (parent != null) {
                List<Widget> siblings = parent.getChildren();
                int index = siblings.indexOf(widget);
                for (int i = index + 1; i < siblings.size(); i++) {
                    Widget s = siblings.get(i);
                    if (s instanceof FocusableWidget) {
                        if (((FocusableWidget)s).isFocusable()) {
                            return s;
                        }
                    } else {
                        next = getNextFocusableWidget(siblings.get(i));
                        if (next != null) {
                            break;
                        }
                    }
                }
            }
            if (next == null) {
                return getNextFocusableWidgetInSiblings(parent);
            }
            
            return next;
        }

        private Widget getNextFocusableWidgetInChildren(Widget widget) {
            for (Widget w : widget.getChildren()) {
                if (w instanceof FocusableWidget) {
                    FocusableWidget fw = (FocusableWidget) w;
                    if (fw.isFocusable()) {
                        return w;
                    }
                }
                Widget next = getNextFocusableWidgetInChildren(w);
                if (next != null) {
                    return next;
                }
            }
            return null;
        }
        
        private Widget getLastFocusableWidget(PartnerScene scene) {
            return getPreviousFocusableWidgetInChildren(scene);
        }

        private Widget getPreviousFocusableWidgetInSiblings(Widget widget) {
            Widget parent = widget.getParentWidget();
            Widget next = null;
            if (parent != null) {
                List<Widget> siblings = parent.getChildren();
                int index = siblings.indexOf(widget);
                for (int i = index - 1; i >= 0; i--) {
                    next = getPreviousFocusableWidgetInChildren(siblings.get(i));
                    if (next != null) return next;
                }
                if (parent instanceof FocusableWidget && (((FocusableWidget)parent).isFocusable())) {
                    return parent;
                }
                next = getPreviousFocusableWidgetInSiblings(parent);
                if (next != null) return next;
            }
            return null;
        }
        
        private Widget getPreviousFocusableWidgetInChildren(Widget widget) {
            if (widget == null) return null;
            Widget next = null;
            List<Widget> children = widget.getChildren();
            for (int i = children.size() - 1; i >= 0; i--) {
                Widget w = children.get(i);
                next = getPreviousFocusableWidgetInChildren(w);
                if (next != null) return next;
                
                if (w instanceof FocusableWidget) {
                    if (((FocusableWidget)w).isFocusable()) {
                        return w;
                    }
                }
            }
            if (widget instanceof FocusableWidget && (((FocusableWidget)widget).isFocusable())) {
                return widget;
            }
            return null;
        }
    }
    
    static public class PartnerSceneCycleFocusProviderAndSceneListener implements ObjectSceneListener {
            
            public void focusChanged(ObjectSceneEvent event,
                    Object previousFocusedObject, Object newFocusedObject) {
                if (newFocusedObject == null) return;
                ObjectScene scene = event.getObjectScene();
                if (scene instanceof PartnerScene) {
                    PartnerScene partnerScene = (PartnerScene) scene;
                    List<Widget> widgets = scene.findWidgets(newFocusedObject);

                    Widget fw = scene.getFocusedWidget();
                    if (fw != null) {
                        if (widgets.contains(fw)) {
                            Rectangle bounds = fw.getClientArea();
                            if (bounds != null) {
                                bounds = partnerScene.convertSceneToView(fw.convertLocalToScene(bounds));
                                partnerScene.getView().scrollRectToVisible(bounds);
                            }
                        }
                        if (!scene.getSelectedObjects().contains(newFocusedObject)) {
                        	scene.userSelectionSuggested(Collections.singleton(newFocusedObject), false);
                        }
                    }
                }
            }

            public void objectAdded(ObjectSceneEvent event, Object addedObject) {
                // do nothing
            }
            
            public void objectRemoved(ObjectSceneEvent event,
                    Object removedObject) {
                // do nothing
            }
            
            public void highlightingChanged(ObjectSceneEvent event,
                    Set<Object> previousHighlighting,
                    Set<Object> newHighlighting) {
                // do nothing
            }

            public void hoverChanged(ObjectSceneEvent event,
                    Object previousHoveredObject, Object newHoveredObject) {
                // do nothing
            }

            public void objectStateChanged(ObjectSceneEvent event,
                    Object changedObject, ObjectState previousState,
                    ObjectState newState) {
                // do nothing
            }

            public void selectionChanged(ObjectSceneEvent event,
                    Set<Object> previousSelection, Set<Object> newSelection) {
            	ObjectScene scene = event.getObjectScene();
            	Widget w = scene.getFocusedWidget();
            	Object focusedObject = scene.getFocusedObject();
            	if (w != null && w != scene.findObject(w)) { // in case of button widgets
                	if (!newSelection.isEmpty()) {
                		Object obj = newSelection.iterator().next();
                		Widget newWidget = scene.findWidget(obj);
                		if (newWidget != null) {
                		    WidgetHelper.makeWidgetVisible(newWidget);
                		}
                		scene.setFocusedWidget(newWidget);
                		scene.setFocusedObject(obj);
                	}            		
            	} else { 
            		if (!newSelection.isEmpty() && !newSelection.contains(focusedObject)) {
                		scene.setFocusedObject(newSelection.iterator().next());
                	}            		
            	}
            }
    }
    
    
    private class FileDropDnDHandler implements DnDHandler {
        
        public FileDropDnDHandler() {
            //default constructor.
        }
        
        public boolean dragOver(Point scenePoint, WidgetDropTargetDragEvent event) {
            Transferable t = event.getTransferable();
            if (t != null) {
                Node[] nodes = Utility.getNodes(t);
                if (nodes.length == 1) {
                    Node node = nodes[0];
                    DataObject dObj = node.getLookup().lookup(DataObject.class);
                    if (dObj.getNodeDelegate().equals(node)) {
                        PasteType p = Utility.getWSDLOrSchemaPasteType(dObj, model, true, true);
                        if (p != null) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }
        
        public boolean drop(Point scenePoint, WidgetDropTargetDropEvent event) {
            Transferable t = event.getTransferable();
            if (t != null) {
                Node[] nodes = Utility.getNodes(t);
                if (nodes.length == 1) {
                    Node node = nodes[0];
                    DataObject dObj = node.getLookup().lookup(DataObject.class);
                    if (dObj.getNodeDelegate().equals(node)) {
                        PasteType p = Utility.getWSDLOrSchemaPasteType(dObj, model, true, true);
                        if (p != null) {
                            try {
                                p.paste();
                            } catch (IOException e) {
                                //ignore
                            }
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        public void dragExit() {
            //do nothing
        }

        public void expandForDragAndDrop() {
            //do nothing
        }

        public boolean isCollapsed() {
            return false;
        }
    }
}
