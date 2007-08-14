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
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;

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
import org.netbeans.modules.xml.wsdl.model.Fault;
import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.Output;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PartnerLinkType;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Role;
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

    public PartnerScene(WSDLModel model) {
        super();

        this.model = model;
        model.addComponentListener(this);
        setKeyEventProcessingType(EventProcessingType.FOCUSED_WIDGET_AND_ITS_CHILDREN_AND_ITS_PARENTS);
        cycleFocusProviderAndSceneListener = new PartnerSceneCycleFocusProviderAndSceneListener(this);
        
        buttonAction = new ButtonAction();
        selectProvider = new ObjectSelectProvider();
        selectAction = ActionFactory.createSelectAction(selectProvider);
        dndAction = new DnDAction();
        focusAction = ActionFactory.createCycleFocusAction(cycleFocusProviderAndSceneListener);

        getPriorActions().addAction(focusAction);
        getActions().addAction(new WidgetAction.Adapter() {

            @Override
            public State keyPressed(Widget widget, WidgetKeyEvent event) {
                Widget w = getFocusedWidget();
                if (w == null) return State.REJECTED;

                if (w instanceof AbstractWidget) {
                	if ((event.getKeyCode() == KeyEvent.VK_F10 && (event.getModifiers () & InputEvent.SHIFT_DOWN_MASK) != 0) || event.getKeyCode() == KeyEvent.VK_CONTEXT_MENU) {
                		JPopupMenu popupMenu = ((AbstractWidget)w).getPopupMenu(w, null);
                		if (popupMenu != null) {
                			Scene scene = w.getScene ();
                			JComponent view = scene.getView ();
                			if (view != null) {
                				Point widgetPoint = new Point(w.getLocation ().x + w.getBounds().getLocation ().x,  w.getLocation ().y + w.getBounds().getLocation ().y);
                				Point visibleRect = scene.convertSceneToView (w.convertLocalToScene(widgetPoint));
                				popupMenu.show (view, visibleRect.x + 10, visibleRect.y + 10);
                			}
                		}
                		return State.CONSUMED;
                	}
                }
//                } 
                return State.REJECTED;
            }
        });

        getActions().addAction(selectAction);
        getActions().addAction(buttonAction);
        getActions().addAction(dndAction);
        
        this.addObjectSceneListener(cycleFocusProviderAndSceneListener, ObjectSceneEventType.OBJECT_SELECTION_CHANGED, 
                                                                        ObjectSceneEventType.OBJECT_FOCUS_CHANGED, 
                                                                        ObjectSceneEventType.OBJECT_ADDED, 
                                                                        ObjectSceneEventType.OBJECT_REMOVED);
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
            } else {
                userSelectionSuggested(Collections.emptySet(),
                        invertSelection);
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
    
    static public class PartnerSceneCycleFocusProviderAndSceneListener implements CycleFocusProvider, ObjectSceneListener {
            
            TreeSet<Object> set;
            Set<String> strSet;
            PartnerScene partnerScene;
            public PartnerSceneCycleFocusProviderAndSceneListener(PartnerScene scene) {
                partnerScene = scene;
                strSet = new TreeSet<String>();
                set = new TreeSet<Object>(new Comparator<Object>() {
                
                    @SuppressWarnings("unchecked")
                    public int compare(Object o1, Object o2) {
                        return partnerScene.getWeight(o1).compareTo(partnerScene.getWeight(o2));
                    }
                
                });
            }
            
            public boolean switchPreviousFocus (Widget widget) {
                Scene scene = widget.getScene ();
                return scene instanceof PartnerScene  &&  switchFocus ((PartnerScene) scene, false);
            }

            public boolean switchNextFocus (Widget widget) {
                Scene scene = widget.getScene ();
                return scene instanceof PartnerScene  &&  switchFocus ((PartnerScene) scene, true);
            }
            
            private boolean switchFocus (PartnerScene scene, boolean forwardDirection) {
                Object object = scene.getFocusedObject();
                
                scene.setFocusedObject(findNextVisibleObject(object, forwardDirection));
                
                return true;
            }
            
            @SuppressWarnings ("unchecked")
            private Object findNextVisibleObject(Object object, boolean forwardDirection) {
                if (!set.isEmpty()) {
                    Object next = object;
                    if (next == null) {
                        next = set.first();
                    } else {
                        SortedSet newSet = set;//set.headSet("someNonWSDLComponent");
                        if (forwardDirection) {
                            SortedSet tSet = newSet.tailSet(object);
                            //this set also includes the "object" that we searched for.
                            // so ignore the first element.
                            Iterator iter = null;
                            if (tSet.size() > 1) {
                                iter = tSet.iterator();
                                iter.next();
                                if (iter.hasNext()) next = iter.next();
                            } else {
                                if (tSet.size() == 1) {
                                    next = set.first();
                                }
                            }
                        } else {
                            SortedSet tSet = newSet.headSet(object);
                            if (tSet.isEmpty()) {
                                next = newSet.last();
                            } else {
                                next = tSet.last();
                            }
                        }
                    }
                    List<Widget> ws = partnerScene.findWidgets(next);
                    if (ws != null && !ws.isEmpty()) {
                        for (Widget w : ws) {
                            Widget temp = w;
                            do {
                                if (!temp.isVisible())  break;
                                if (temp instanceof ButtonWidget) {
                                    if (!((ButtonWidget) temp).isButtonEnabled()) break;
                                }
                            } while ((temp = temp.getParentWidget()) != null);

                            if (temp == null) {
                                return next;
                            } else {
                                return findNextVisibleObject(next, forwardDirection);
                            }
                        }
                    }
                }
                return null;
            }

            @SuppressWarnings("unchecked")
            public void focusChanged(ObjectSceneEvent event,
                    Object previousFocusedObject, Object newFocusedObject) {
                if (newFocusedObject == null) return;
                ObjectScene scene = event.getObjectScene();
                if (!scene.getSelectedObjects().contains(newFocusedObject)) {
                    Set set = new HashSet();
                    set.add(newFocusedObject);
                    scene.setSelectedObjects(set);
                }
            }

            public void objectAdded(ObjectSceneEvent event, Object addedObject) {
                set.add(addedObject);
                strSet.add(partnerScene.getWeight(addedObject));
            }
            
            public void objectRemoved(ObjectSceneEvent event,
                    Object removedObject) {
                set.remove(removedObject);
                strSet.remove(partnerScene.getWeight(removedObject));
            }
            
            public void highlightingChanged(ObjectSceneEvent event,
                    Set<Object> previousHighlighting,
                    Set<Object> newHighlighting) {
                
            }

            public void hoverChanged(ObjectSceneEvent event,
                    Object previousHoveredObject, Object newHoveredObject) {
                
            }

            public void objectStateChanged(ObjectSceneEvent event,
                    Object changedObject, ObjectState previousState,
                    ObjectState newState) {
                
            }

            public void selectionChanged(ObjectSceneEvent event,
                    Set<Object> previousSelection, Set<Object> newSelection) {
                if (newSelection == null || newSelection.isEmpty()) return;
                
                Object obj = newSelection.iterator().next();
                Widget widget = partnerScene.findWidget(obj);
                Rectangle bounds = widget.getClientArea();
                if (bounds != null) {
                    bounds = partnerScene.convertSceneToView(widget.convertLocalToScene(bounds));
                    partnerScene.getView().scrollRectToVisible(bounds);
                }
            }
    }
    
    public String getWeight(Object obj) {
        if (obj instanceof WSDLComponent) {
            return getWeight((WSDLComponent) obj);
        } else if (obj instanceof String) {
            return (String) obj;
        }
        return "Z";
    }
    
    public String getWeight(WSDLComponent comp) {
        String zeroPaddedNumber = String.format("%010d", comp.findPosition());
        if (comp instanceof PartnerLinkType) {
            return "AZ" + zeroPaddedNumber;
        } else if (comp instanceof Role) {
            return getWeight(comp.getParent()) + "BZ" + zeroPaddedNumber;
        } else if (comp instanceof PortType) {
            return "CZ" + zeroPaddedNumber;
        } else if (comp instanceof Operation) {
            return getWeight(comp.getParent()) + "DZ" + zeroPaddedNumber;
        } else if (comp instanceof Input) {
            return getWeight(comp.getParent()) + "EZ" + zeroPaddedNumber;
        } else if (comp instanceof Output) {
            return getWeight(comp.getParent()) + "EZ" + zeroPaddedNumber;
        } else if (comp instanceof Fault) {
            return getWeight(comp.getParent()) + "EZ" +  zeroPaddedNumber;
        } else if (comp instanceof Message) {
            return "FZ" + zeroPaddedNumber;
        } else if (comp instanceof Part) {
            return getWeight(comp.getParent()) + "GZ" + zeroPaddedNumber;
        }
        return "Z";
    }

}
