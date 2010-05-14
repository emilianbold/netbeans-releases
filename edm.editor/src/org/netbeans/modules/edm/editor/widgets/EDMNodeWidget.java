/*
 * The contents of this file are subject to the terms of the Common
 * Development and Distribution License (the License). You may not use this
 * file except in compliance with the License.  You can obtain a copy of the
 *  License at http://www.netbeans.org/cddl.html
 *
 * When distributing Covered Code, include this CDDL Header Notice in each
 * file and include the License. If applicable, add the following below the
 * CDDL Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems, Inc. All Rights Reserved
 *
 */
package org.netbeans.modules.edm.editor.widgets;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.border.Border;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.model.StateModel;
import org.netbeans.api.visual.widget.*;
import org.openide.util.Utilities;
import org.openide.windows.WindowManager;

import org.netbeans.modules.edm.editor.dataobject.MashupDataObject;

/**
 * This class represents a node widget in the EDM visualization style. It implements the minimize ability.
 * It allows to add pin widgets into the widget using <code>attachPinWidget</code> method.
 * <p>
 * The node widget consists of a header (with an image, a name, secondary name and a glyph set) and the content.
 * The content contains pin widgets. Pin widgets can be organized in pin-categories defined by calling <code>sortPins</code> method.
 * The <code>sortPins</code> method has to be called refresh the order after adding a pin widget.
 *
 * @author David Kaspar
 */
public class EDMNodeWidget extends Widget implements StateModel.Listener, EDMMinimizeAbility {
    
    private static final Color BORDER_CATEGORY_BACKGROUND = new Color(0x8BA6FF);
    private static final Color BORDER_CATEGORY_BACKGROUND_LIGHT = Color.LIGHT_GRAY;
    
    private static final Border BORDER_MINIMIZE = BorderFactory.createRoundedBorder(2, 2, null, EDMNodeBorder.COLOR_BORDER);
    static final Color COLOR_SELECTED = new Color(0xffffff);
    static final Border BORDER = BorderFactory.createOpaqueBorder(2, 8, 2, 8);
    static final Border BORDER_HOVERED = BorderFactory.createLineBorder(2, 8, 2, 8, Color.ORANGE);
    
    private Widget header;
    private ImageWidget minimizeWidget;
    private ImageWidget imageWidget;
    private LabelWidget nameWidget;
    private LabelWidget typeWidget;
    private EDMGlyphSetWidget glyphSetWidget;
    
    private SeparatorWidget pinsSeparator;
    
    private HashMap<String, Widget> pinCategoryWidgets = new LinkedHashMap<String, Widget> ();
    private Font fontPinCategory = getScene().getFont().deriveFont(10.0f);
    
    private StateModel stateModel = new StateModel(2);
    private Anchor nodeAnchor = new EDMNodeAnchor(this);
    
    /**
     * Creates a node widget.
     * @param scene the scene
     */
    public EDMNodeWidget(Scene scene) {
        super(scene);
        
        setOpaque(false);
        setBorder(EDMFactory.createEDMNodeBorder());
        setLayout(LayoutFactory.createVerticalFlowLayout());
        setMinimumSize(new Dimension(128, 8));
        
        header = new Widget(scene);
        header.setBorder(BORDER);
        header.setBackground(COLOR_SELECTED);
        header.setOpaque(false);
        header.setLayout(LayoutFactory.createHorizontalFlowLayout(LayoutFactory.SerialAlignment.CENTER, 8));
        addChild(header);
        
        minimizeWidget = new ImageWidget(scene,
                Utilities.loadImage("org/netbeans/modules/edm/editor/resources/edm-collapse.png"));
        minimizeWidget.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        minimizeWidget.setBorder(BORDER_MINIMIZE);
        minimizeWidget.getActions().addAction(new ToggleMinimizedAction());
        header.addChild(minimizeWidget);
        
        imageWidget = new ImageWidget(scene);
        header.addChild(imageWidget);
        
        glyphSetWidget = new EDMGlyphSetWidget(scene);
        header.addChild(glyphSetWidget);
        
        nameWidget = new LabelWidget(scene);
        nameWidget.setFont(scene.getDefaultFont().deriveFont(Font.BOLD));
        header.addChild(nameWidget);
        
        typeWidget = new LabelWidget(scene);
        typeWidget.setForeground(Color.BLACK);
        header.addChild(typeWidget);
        
        pinsSeparator = new SeparatorWidget(scene, SeparatorWidget.Orientation.HORIZONTAL);
        pinsSeparator.setForeground(BORDER_CATEGORY_BACKGROUND);
        addChild(pinsSeparator);
        
        Widget topLayer = new Widget(scene);
        addChild(topLayer);
        
        stateModel = new StateModel();
        stateModel.addListener(this);
        
        notifyStateChanged(ObjectState.createNormal(), ObjectState.createNormal());       
    }
    
    /**
     * Called to check whether a particular widget is minimizable. By default it returns true.
     * The result have to be the same for whole life-time of the widget. If not, then the revalidation has to be invoked manually.
     * An anchor (created by <code>EDMNodeWidget.createPinAnchor</code> is not affected by this method.
     *
     * @param widget the widget
     * @return true, if the widget is minimizable; false, if the widget is not minimizable
     */
    protected boolean isMinimizableWidget(Widget widget) {
        return true;
    }
    
    /**
     * Check the minimized state.
     * @return true, if minimized
     */
    public boolean isMinimized() {
        return stateModel.getBooleanState();
    }
    
    /**
     * Set the minimized state. This method will show/hide child widgets of this Widget and switches anchors between
     * node and pin widgets.
     * @param minimized if true, then the widget is going to be minimized
     */
    public void setMinimized(boolean minimized) {
        stateModel.setBooleanState(minimized);
    }
    
    /**
     * Toggles the minimized state. This method will show/hide child widgets of this Widget and switches anchors between
     * node and pin widgets.
     */
    public void toggleMinimized() {
        stateModel.toggleBooleanState();
    }
    
    /**
     * Called when a minimized state is changed. This method will show/hide child widgets of this Widget and switches anchors between
     * node and pin widgets.
     */
    public void stateChanged() {
        boolean minimized = stateModel.getBooleanState();
        Rectangle rectangle = minimized ? new Rectangle() : null;
        for (Widget widget : getChildren())
            if (widget != header  &&  widget != pinsSeparator) {
                getScene().getSceneAnimator().animatePreferredBounds(widget, minimized  && isMinimizableWidget(widget) ? rectangle : null);
            }
        minimizeWidget.setImage(minimized ?
            Utilities.loadImage("org/netbeans/modules/edm/editor/resources/edm-expand.png") :
            Utilities.loadImage("org/netbeans/modules/edm/editor/resources/edm-collapse.png")); // NOI18N
    }
    
    /**
     * Called to notify about the change of the widget state.
     * @param previousState the previous state
     * @param state the new state
     */
    protected void notifyStateChanged(ObjectState previousState, ObjectState state) {
        if (! previousState.isSelected()  &&  state.isSelected()) {
            bringToFront();
            MashupDataObject dObj = WindowManager.getDefault().getRegistry().
                    getActivated().getLookup().lookup(MashupDataObject.class);
            if(dObj != null) {
                dObj.getGraphManager().setSelectedNode(this);
            }
        }else if (! previousState.isHovered()  &&  state.isHovered()) {
            bringToFront();
        }
        header.setOpaque(state.isSelected());
        header.setBorder(state.isFocused() || state.isHovered() ? EDMNodeWidget.BORDER_HOVERED : EDMNodeWidget.BORDER);
    }
    
    /**
     * Sets a node image.
     * @param image the image
     */
    public void setNodeImage(Image image) {
        imageWidget.setImage(image);
        revalidate();
    }
    
    /**
     * Returns a node name.
     * @return the node name
     */
    public String getNodeName() {
        return nameWidget.getLabel();
    }
    
    /**
     * Sets a node name.
     * @param nodeName the node name
     */
    public void setNodeName(String nodeName) {
        nameWidget.setLabel(nodeName);
        revalidate();
    }
    
    /**
     * Sets a node type (secondary name).
     * @param nodeType the node type
     */
    public void setNodeType(String nodeType) {
        typeWidget.setLabel(nodeType != null ? "[" + nodeType + "]" : null);
        revalidate();
    }
    
    /**
     * Attaches a pin widget to the node widget.
     * @param widget the pin widget
     */
    public void attachPinWidget(Widget widget) {
        widget.setCheckClipping(true);
        addChild(widget);
        if (stateModel.getBooleanState()  && isMinimizableWidget(widget))
            widget.setPreferredBounds(new Rectangle());
        revalidate();
    }
    
    /**
     * Sets node glyphs.
     * @param glyphs the list of images
     */
    public void setGlyphs(List<Image> glyphs) {
        glyphSetWidget.setGlyphs(glyphs);
        revalidate();
    }
    
    /**
     * Sets all node properties at once.
     * @param image the node image
     * @param nodeName the node name
     * @param nodeType the node type (secondary name)
     * @param glyphs the node glyphs
     */
    public void setNodeProperties(Image image, String nodeName, String nodeType, List<Image> glyphs) {
        setNodeImage(image);        
        setNodeName(nodeName);
        setNodeType(nodeType);
        setGlyphs(glyphs);
    }
    
    /**
     * Returns a node name widget.
     * @return the node name widget
     */
    public LabelWidget getNodeNameWidget() {
        return nameWidget;
    }
    
    /**
     * Returns a node anchor.
     * @return the node anchor
     */
    public Anchor getNodeAnchor() {
        return nodeAnchor;
    }
    
    /**
     * Creates an extended pin anchor with an ability of reconnecting to the node anchor when the node is minimized.
     * @param anchor the original pin anchor from which the extended anchor is created
     * @return the extended pin anchor
     */
    public Anchor createAnchorPin(Anchor anchor) {
        return AnchorFactory.createProxyAnchor(stateModel, anchor, nodeAnchor);
    }
    
    /**
     * Returns a list of pin widgets attached to the node.
     * @return the list of pin widgets
     */
    private List<Widget> getPinWidgets() {
        ArrayList<Widget> pins = new ArrayList<Widget> (getChildren());
        pins.remove(header);
        pins.remove(pinsSeparator);
        return pins;
    }
    
    /**
     * Sorts and assigns pins into categories.
     * @param pinsCategories the map of category name as key and a list of pin widgets as value
     */
    public void sortPins(Map<String, List<Widget>> pinsCategories) {
        List<Widget> previousPins = getPinWidgets();
        ArrayList<Widget> unresolvedPins = new ArrayList<Widget> (previousPins);
        
        for (Iterator<Widget> iterator = unresolvedPins.iterator(); iterator.hasNext();) {
            Widget widget = iterator.next();
            if (pinCategoryWidgets.containsValue(widget)) {
                iterator.remove();
                revalidate();
            }
        }
        
        ArrayList<String> unusedCategories = new ArrayList<String> (pinCategoryWidgets.keySet());
        
        ArrayList<String> categoryNames = new ArrayList<String> (pinsCategories.keySet());        
        
        ArrayList<Widget> newWidgets = new ArrayList<Widget> ();
        for (String categoryName : categoryNames) {
            if (categoryName == null)
                continue;
            unusedCategories.remove(categoryName);
            revalidate();
            newWidgets.add(createPinCategoryWidget(categoryName));
            List<Widget> widgets = pinsCategories.get(categoryName);
            for (Widget widget : widgets)
                if (unresolvedPins.remove(widget)) {
                    newWidgets.add(widget);
                    revalidate();
                }
        }
        
        if (! unresolvedPins.isEmpty()) {
            newWidgets.addAll(0, unresolvedPins);
            revalidate();
        }
        
        for (String category : unusedCategories) {
            pinCategoryWidgets.remove(category);
            revalidate();
        }            
        
        removeChildren(previousPins);
        revalidate();
        addChildren(newWidgets);
        revalidate();
    }
    
    private Widget createPinCategoryWidget(String categoryDisplayName) {
        Widget w = pinCategoryWidgets.get(categoryDisplayName);
        if (w != null)
            return w;
        LabelWidget label = new LabelWidget(getScene(), categoryDisplayName);
        label.setOpaque(true);
        if(categoryDisplayName.equalsIgnoreCase("Unused Columns")) {
            label.setBackground(BORDER_CATEGORY_BACKGROUND_LIGHT);
            label.setForeground(Color.DARK_GRAY);
        } else {
            label.setBackground(BORDER_CATEGORY_BACKGROUND);
        }
            
        label.setFont(fontPinCategory);
        label.setAlignment(LabelWidget.Alignment.CENTER);
        label.setCheckClipping(true);
        if (stateModel.getBooleanState())
            label.setPreferredBounds(new Rectangle());
        pinCategoryWidgets.put(categoryDisplayName, label);
        revalidate();
        return label;
    }
    
    /**
     * Collapses the widget.
     */
    public void collapseWidget() {
        stateModel.setBooleanState(true);
    }
    
    /**
     * Expands the widget.
     */
    public void expandWidget() {
        stateModel.setBooleanState(false);
    }
    
    /**
     * Returns a header widget.
     * @return the header widget
     */
    public Widget getHeader() {
        return header;
    }
    
    private final class ToggleMinimizedAction extends WidgetAction.Adapter {
        
        public State mousePressed(Widget widget, WidgetMouseEvent event) {
            if (event.getButton() == MouseEvent.BUTTON1 || event.getButton() == MouseEvent.BUTTON2) {
                stateModel.toggleBooleanState();
                return State.CONSUMED;
            }
            return State.REJECTED;
        }
    }
    
}