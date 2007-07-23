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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JLabel;

import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.SelectProvider;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.action.WidgetAction.WidgetDropTargetDragEvent;
import org.netbeans.api.visual.action.WidgetAction.WidgetDropTargetDropEvent;
import org.netbeans.api.visual.model.ObjectScene;
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
    
    private WSDLModel model;
    private MessagesWidget messagesWidget;
    private CollaborationsWidget collaborationsWidget;
    private DragOverSceneLayer dragOverLayer;
    private Font defaultFont = new JLabel().getFont();
    private SelectProvider selectProvider;

    public PartnerScene(WSDLModel model) {
        super();
        
        this.model = model;
        model.addComponentListener(this);

        buttonAction = new ButtonAction();
        selectProvider = new ObjectSelectProvider();
        selectAction = ActionFactory.createSelectAction(selectProvider);
        dndAction = new DnDAction();

        getActions().addAction(selectAction);
        getActions().addAction(buttonAction);
        getActions().addAction(dndAction);
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
//            Object object = findObject(widget);
//            return object != null && (invertSelection  
//                    || ! getSelectedObjects().contains(object));
        }
        

        public void select(Widget widget, Point localLocation, 
                boolean invertSelection) 
        {
            if (widget instanceof ButtonWidget) {
                if (!((ButtonWidget) widget).isParenSelectionAllowed()) {
                    // do nothing
                    return;
                }
            }
            
            Object object = findObject(widget);
            
//            System.out.println("Object=" + object);
//            System.out.println("getFocusedObject=" + getFocusedObject());
            
            // setFocusedObject(object);
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
                
                // Make the widget visible when it is selected.
                Rectangle bounds = widget.getClientArea();
                if (bounds != null) {
                    bounds = widget.convertLocalToScene(bounds);
                    getView().scrollRectToVisible(bounds);
                }
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

}
