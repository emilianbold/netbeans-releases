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
 * AbstractWidget.java
 *
 * Created on August 15, 2006, 11:23 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.ui.view.grapheditor.widget;

import java.awt.BasicStroke;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import javax.swing.Action;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.PopupMenuProvider;
import org.netbeans.api.visual.border.Border;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.NodesFactory;
import org.netbeans.modules.xml.xam.ComponentEvent;
import org.netbeans.modules.xml.xam.ComponentListener;
import org.netbeans.modules.xml.xam.Model;
import org.openide.actions.ReorderAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.TopComponent;

/**
 * Class AbstractWidget is the base class for all widgets that represent
 * a WSDL component.
 *
 * @author radval
 * @author Nathan Fiedler
 */
public abstract class AbstractWidget<T extends WSDLComponent> extends Widget
        implements ComponentListener, PopupMenuProvider, FocusableWidget, PropertyChangeListener {
    
    /** The WSDL component this widget represents; may be null. */
    private T wsdlComponent;
    /** The Lookup for this widget. */
    private Lookup widgetLookup;
    /** The content of our customized Lookup. */
    private InstanceContent lookupContent;
    /** The Node for the WSDLComponent, if it has been created. */
    private Node componentNode;
    /** Used to weakly listen to the component model. */
    private ComponentListener weakComponentListener;
    /** store the WSDLModel representing the wsdl being edited **/
    private WSDLModel model;
    
    private Border currentBorder;
	private PropertyChangeListener weakModelListener;
    
    
    /**
     * Creates a new instance of AbstractWidget.
     *
     * @param  scene      the widget Scene.
     * @param  component  the corresponding WSDL component.
     * @param  lookup     the Lookup for this widget.
     */
    public AbstractWidget(Scene scene, T component, Lookup lookup) {
        super(scene);
        model = ((PartnerScene)scene).getModel();
        lookupContent = new InstanceContent();
        widgetLookup = new ProxyLookup(new Lookup[] {
            new AbstractLookup(lookupContent),
            lookup
        });
        if (component != null) {
            lookupContent.add(component);
            setWSDLComponent(component);
            weakModelListener = WeakListeners.propertyChange(this, component.getModel());
            component.getModel().addPropertyChangeListener(weakModelListener);
        }
        getActions().addAction(ActionFactory.createPopupMenuAction(this));
    }

    /**
     * Deletes the model component. Subclasses should in general avoid
     * overriding this method and creating another transaction, as that
     * will create another undoable edit on the undo/redo queue. Instead,
     * override the postDeleteComponent(Model) method, which is invoked
     * during the transaction, so any changes to the model will be
     * captured in a single undoable edit.
     */
    protected void deleteComponent() {
        if (wsdlComponent == null) {
            return;
        }
        // Remove our compopnent listener.
        registerListener(null);
        
        if (weakModelListener != null) {
        	wsdlComponent.getModel().removePropertyChangeListener(weakModelListener);
        }
        
        try {
            if (model.startTransaction()) {
                model.removeChildComponent(wsdlComponent);
                postDeleteComponent(model);
            }
        } finally {
            model.endTransaction();
        }
        model = null;
    }

    public WSDLModel getModel() {
        return model;
    }
    
    /**
     * Subclasses may override this method to make additional changes to
     * the model within the same transaction as the one used to delete
     * the model component.
     *
     * @param  model  the model that is in transaction.
     */
    protected void postDeleteComponent(@SuppressWarnings("unused")
    Model model) {
        // Do nothing here, as this is exclusively for subclasses to override.
    }

    /**
     * Locates the TopComponent parent of the view containing the Scene
     * that owns this widget, if possible.
     *
     * @return  the parent TopComponent, or null if not found.
     */
    protected TopComponent findTopComponent() {
        return (TopComponent) SwingUtilities.getAncestorOfClass(
                TopComponent.class, getScene().getView());
    }

    @Override
    public Lookup getLookup() {
        return widgetLookup;
    }

    /**
     * Return the content of this widget's custom Lookup. Subclasses may
     * add objects to this content, thereby altering the contents of the
     * Lookup associated with this widget.
     *
     * @return  Lookup content.
     */
    protected InstanceContent getLookupContent() {
        return lookupContent;
    }

    /**
     * Returns a Node for the WSDL component that this widget represents.
     * If this widget does not have an assigned WSDL component, then this
     * returns an AbstractNode with no interesting properties.
     */
    public synchronized Node getNode() {
        if (componentNode == null) {
            if (wsdlComponent == null) {
                // No component? Then supply a dummy node.
                componentNode = new AbstractNode(Children.LEAF);
            } else {
                // Use the factory to construct the Node.
                NodesFactory factory = NodesFactory.getInstance();
                componentNode = factory.create(wsdlComponent);
            }
            componentNode = getNodeFilter(componentNode);
        }
        return componentNode;
    }

    /**
     * Method for subclasses to have an opportunity to wrap the original
     * Node in a FilterNode, which can then be customized to suit the
     * individual widget (e.g. to filter the NewType list).
     *
     * the current widget is passed so that delete can work.
     * @param  original  the Node to be filtered.
     * @return  the filtered node.
     */
    protected Node getNodeFilter(Node original) {
        return new WidgetFilterNode(original, this);
    }

    public JPopupMenu getPopupMenu(Widget widget, Point point) {
        Node node = getNode();
        if (node != null && wsdlComponent != null) {
            Set<WSDLComponent> set = new HashSet<WSDLComponent>();
            set.add(getWSDLComponent());
            ((PartnerScene)getScene()).userSelectionSuggested(set, false);
            TopComponent tc = findTopComponent();
            Lookup lookup;
            if (tc != null) {
                // Activate the node just as any explorer view would do.
                tc.setActivatedNodes(new Node[] { node });
                // To get the explorer actions enabled, must have the
                // lookup from the parent TopComponent.
                lookup = tc.getLookup();
            } else {
                lookup = Lookup.EMPTY;
            }
            // Remove the actions that we do not want to support in this view.
            Action[] actions = node.getActions(true);
            List<Action> list = new ArrayList<Action>();
            Collections.addAll(list, actions);
            updateActions(list);
            actions = list.toArray(new Action[list.size()]);
            return Utilities.actionsToPopup(actions, lookup);
        }
        return null;
    }

    /**
     * Return the WSDL component this widget represents.
     *
     * @return  WSDL component for this widget; may be null.
     */
    public T getWSDLComponent() {
        return wsdlComponent;
    }

    @Override
    protected void notifyStateChanged(ObjectState previousState, ObjectState state) {
        super.notifyStateChanged(previousState, state);
        //add delete action only if the widget is selected.
        if (state.isSelected()) {
            TopComponent tc = findTopComponent();
            if (tc != null) {
                Node node = getNode();
                tc.setActivatedNodes(new Node[] { node });
            }
            if (currentBorder == null) {
                currentBorder = getBorder();
                Insets insets = currentBorder.getInsets();
                setBorder(BorderFactory.createEmptyBorder(insets.top, insets.left, insets.bottom, insets.right));
            }
        } else {
            if (currentBorder != null) {
                setBorder(currentBorder);
            }
            currentBorder = null;
        }
        repaint();
        revalidate(true);
    }

    /**
     * Manage the listener registration with the component model. If the
     * current component is non-null, removes this widget as a listener
     * from that component's model. If the new component is non-null, adds
     * a weak component listener to that component's model.
     *
     * @param  component  the component to listen to.
     */
    protected void registerListener(T component) {
        if (wsdlComponent != null) {
            wsdlComponent.getModel().removeComponentListener(weakComponentListener);
            weakComponentListener = null;
        }
        if (component != null) {
            weakComponentListener = WeakListeners.create(ComponentListener.class, this, model);
            model.addComponentListener(weakComponentListener);
        }
    }

    /**
     * Change the WSDL component that this widget is associated with.
     * Only a new component of the same type can be assigned. This manages
     * the listener registration with the component model.
     *
     * @param  component  the new WSDL component.
     */
    protected void setWSDLComponent(T component) {
        registerListener(component);
        wsdlComponent = component;
    }

    @Override
    protected void paintChildren() {
        super.paintChildren();
        
        if (getState().isSelected()) {
            Graphics2D g2 = getGraphics();

            Object oldStrokeControl = g2.getRenderingHint(RenderingHints
                    .KEY_STROKE_CONTROL);
            Paint oldPaint = g2.getPaint();
            
            Stroke oldStroke = g2.getStroke();
            
            g2.setStroke(new BasicStroke(2));
            
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, 
                    RenderingHints.VALUE_STROKE_PURE);
            
            g2.setPaint(WidgetConstants.SELECTION_COLOR);
            
            g2.draw(createSelectionShape());

            g2.setPaint(oldPaint);
            g2.setStroke(oldStroke);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, 
                    oldStrokeControl);
        }
    }
    
    /**
     * Return the shape to be used to show the selection of this widget.
     *
     * @return  selection shape.
     */
    protected Shape createSelectionShape() {
        Rectangle rect = getBounds();
        return new Rectangle2D.Double(rect.x + 1, rect.y + 1, rect.width - 2, 
                rect.height - 2);
    }

    /**
     * Add/remove actions from the given list, as needed for each type
     * of widget. For instance, the default implementation removes the
     * ReorderAction instance from the list provided by the backing Node.
     * Subclasses may add or remove additional actions. To prevent removing
     * the default actions, override without calling this superclass method.
     *
     * @param  actions  list of Action instances to be updated.
     */
    protected void updateActions(List<Action> actions) {
        ListIterator<Action> liter = actions.listIterator();
        while (liter.hasNext()) {
            Action action = liter.next();
            if (action instanceof ReorderAction) {
                liter.remove();
            }
        }
    }

    /**
     * Invoked when the model component has changed in some way (either
     * values changed, children were added, or children were removed).
     * Subclasses should override this method to update their content.
     */
    protected void updateContent() {
    }

    /**
     * Check if this widget should update its content based on the given
     * component event. If so, the contents and scene validation will be
     * performed on the event dispatching thread.
     *
     * @param  event  component event.
     */
    private void checkUpdate(ComponentEvent event) {
        Object src = event.getSource();
        if (src.equals(wsdlComponent)) {
            Runnable updater = new Runnable() {
                public void run() {
                    updateContent();
                    getScene().validate();
                }
            };
            if (EventQueue.isDispatchThread()) {
                updater.run();
            } else {
                EventQueue.invokeLater(updater);
            }
        }
    }

    public void childrenAdded(ComponentEvent event) {
        checkUpdate(event);
    }

    public void childrenDeleted(ComponentEvent event) {
        checkUpdate(event);
    }

    public void valueChanged(ComponentEvent event) {
        checkUpdate(event);
    }
    
    public boolean isFocusable() {
        Widget temp = this;
        while (temp.isVisible() && (temp = temp.getParentWidget()) != null);
        
        if (temp == null) return true;
        
        return false;
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
    }
    
}
