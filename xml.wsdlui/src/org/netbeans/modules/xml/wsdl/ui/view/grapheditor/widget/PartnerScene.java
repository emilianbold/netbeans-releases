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

package org.netbeans.modules.xml.wsdl.ui.view.grapheditor.widget;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.DragOverSceneLayer;
import org.netbeans.modules.xml.xam.ComponentEvent;
import org.netbeans.modules.xml.xam.ComponentListener;

/**
 *
 * @author anjeleevich
 */
public class PartnerScene extends ObjectScene implements ComponentListener, DnDHandler {
    private ButtonAction buttonAction;
    private WidgetAction selectAction;
    private DnDAction dndAction;
    private WidgetAction focusAction;
    
    private WSDLModel model;
    private MessagesWidget messagesWidget;
    private CollaborationsWidget collaborationsWidget;
    private DragOverSceneLayer dragOverLayer;
    private Font defaultFont = new JLabel().getFont();
    private SelectProvider selectProvider;
    private PartnerSceneCycleFocusProviderAndSceneListener cycleFocusProviderAndSceneListener;
    private PartnerSceneCycleFocusProvider mycycleFocusProvider;
    private List<Widget> selectedWidgets;

    public PartnerScene(WSDLModel model) {
        super();

        this.model = model;
        model.addComponentListener(this);
        setKeyEventProcessingType(EventProcessingType.FOCUSED_WIDGET_AND_ITS_CHILDREN_AND_ITS_PARENTS);
        cycleFocusProviderAndSceneListener = new PartnerSceneCycleFocusProviderAndSceneListener();
        
        mycycleFocusProvider = new PartnerSceneCycleFocusProvider();
        buttonAction = new ButtonAction();
        selectProvider = new ObjectSelectProvider();
        selectAction = ActionFactory.createSelectAction(selectProvider);
        dndAction = new DnDAction();
        focusAction = ActionFactory.createCycleFocusAction(mycycleFocusProvider);

        getPriorActions().addAction(focusAction);
        getPriorActions().addAction(new WidgetAction.Adapter() {

            @Override
            public State keyPressed(Widget widget, WidgetKeyEvent event) {
                //temp
/*                if (event.getKeyCode() == KeyEvent.VK_F3) {
                    printFocusCycle(widget.getScene(), "", false, false);
                }
                if (event.getKeyCode() == KeyEvent.VK_F4) {
                    if ((event.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK) != 0) {
                        printFocusCycle(widget.getScene(), "", true, true);
                    } else {
                        printFocusCycle(widget.getScene(), "", true, false);
                    }
                }*/
            	if (event.getKeyCode() == KeyEvent.VK_F5) {
                    widget.getScene().validate();
                }
                //end temp
                Widget w = getFocusedWidget();
                if (w == null) return State.REJECTED;
                
                return w.getActions().keyPressed(w, event);

            }
        });

        getActions().addAction(selectAction);
        getActions().addAction(buttonAction);
        getActions().addAction(dndAction);
        
        this.addObjectSceneListener(cycleFocusProviderAndSceneListener, ObjectSceneEventType.OBJECT_FOCUS_CHANGED, ObjectSceneEventType.OBJECT_SELECTION_CHANGED);
        selectedWidgets = new ArrayList<Widget>();
    }
    private Logger mLogger = Logger.getLogger(PartnerScene.class.getName());
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
    }

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

    /**
     * Scan the scene's object-to-widget mapping for components that are
     * no longer in the component model, and remove them if so.
     */
    private void pruneStaleBindings() {
        // Create a new set to avoid concurrent modification exceptions.
        Set<Object> objects = new HashSet<Object>(getObjects());
        for (Object object : objects) {
            if (object instanceof WSDLComponent &&
                    !isInModel((WSDLComponent) object)) {
                removeObject(object);
            }
        }
    }

    /**
     * Determine if the component is in the model or not.
     *
     * @param  component  the component to query.
     * @return  true if component is in model, false otherwise.
     */
    private boolean isInModel(WSDLComponent component) {
        WSDLComponent root = model.getRootComponent();
        for (WSDLComponent c = component; c != null; c = c.getParent()) {
            if (c == root) {
                return true;
            }
        }
        return false;
    }

    public void valueChanged(ComponentEvent componentEvent) {
        updateContent(componentEvent);
    }

    public void childrenAdded(ComponentEvent componentEvent) {
        updateContent(componentEvent);
    }

    public void childrenDeleted(ComponentEvent componentEvent) {
        updateContent(componentEvent);
        // Perform the pruning in one place, as opposed to in the
        // AbstractWidget class, which would be invoked many times.
        if (EventQueue.isDispatchThread()) {
            pruneStaleBindings();
        } else {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    pruneStaleBindings();
                }
            });
        }
    }

    /**
     * If the event source is the model root, update the top-level widgets
     * manually, since they are not registered as model listeners.
     */
    private void updateContent(ComponentEvent componentEvent) {
        if (componentEvent.getSource() instanceof Definitions) {
            Runnable updater = new Runnable() {
                public void run() {
                    getMessagesWidget().updateContent();
                    getCollaborationsWidget().updateContent();
                    // Validate the scene after making changes.
                    validate();
                }
            };
            if (EventQueue.isDispatchThread()) {
                updater.run();
            } else {
                EventQueue.invokeLater(updater);
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
            Object object = findObject(widget);
            
            if (object != null) {
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
        
    }

    public boolean isCollapsed() {
        return false;
    }
    
    @Override
    public void userSelectionSuggested(Set<?> suggestedSelectedObjects,
    		boolean invertSelection) {
    	validate();
    	super.userSelectionSuggested(suggestedSelectedObjects, invertSelection);
    	for (Widget w : getSelectedWidgets()) {
    		w.setState(w.getState().deriveSelected(false));
    	}
    	getSelectedWidgets().clear();
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
/*                    for (Widget w : scene.getSelectedWidgets()) {
                    	w.setState(w.getState().deriveSelected(false));
                    }
                    scene.getSelectedWidgets().clear();*/
                    scene.getSelectedWidgets().add(next);
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
                		scene.setFocusedWidget(scene.findWidget(obj));
                		scene.setFocusedObject(obj);
                	}            		
            	} else { 
            		if (!newSelection.isEmpty() && !newSelection.contains(focusedObject)) {
                		scene.setFocusedObject(newSelection.iterator().next());
                	}            		
            	}
            }
    }
}
