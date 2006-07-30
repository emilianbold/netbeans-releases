/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.api.visual.vmd;

import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.border.Border;
import org.netbeans.api.visual.border.EmptyBorder;
import org.netbeans.api.visual.border.ImageBorder;
import org.netbeans.api.visual.layout.SerialLayout;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.model.StateModel;
import org.netbeans.api.visual.widget.*;
import org.openide.util.Utilities;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

/**
 * @author David Kaspar
 */
public class VMDNodeWidget extends Widget implements StateModel.Listener {

    private static final Border BORDER_SHADOW_NORMAL = new ImageBorder (new Insets (5, 5, 5, 5), Utilities.loadImage ("org/netbeans/modules/visual/resources/border/shadow_normal.png")); // NOI18N
    private static final Border BORDER_SHADOW_HOVERED = new ImageBorder (new Insets (5, 5, 5, 5), Utilities.loadImage ("org/netbeans/modules/visual/resources/border/shadow_hovered.png")); // NOI18N
    private static final Border BORDER_SHADOW_SELECTED = new ImageBorder (new Insets (5, 5, 5, 5), Utilities.loadImage ("org/netbeans/modules/visual/resources/border/shadow_selected.png")); // NOI18N

    private static final Color COLOR_CATEGORY_BACKGROUND = new Color (0xEEEEEE);
    private static final Color COLOR_CATEGORY_FOREGROUND = Color.GRAY;

    private Widget mainLayer;

    private Widget header;
    private ImageWidget imageWidget;
    private LabelWidget nameWidget;
    private LabelWidget typeWidget;
    private VMDGlyphSetWidget glyphSetWidget;

    private SeparatorWidget pinsSeparator;

    private HashMap<String, Widget> pinCategoryWidgets = new HashMap<String, Widget> ();
    private Font fontPinCategory = getScene ().getFont ().deriveFont (10.0f);

    private StateModel stateModel = new StateModel (2);
    private Anchor nodeAnchor = new VMDNodeAnchor (this);

    public VMDNodeWidget (Scene scene) {
        super (scene);

        setBackground (Color.WHITE);
        setOpaque (true);
        setBorder (BORDER_SHADOW_NORMAL);

        mainLayer = new Widget (scene);
        mainLayer.setLayout (new SerialLayout (SerialLayout.Orientation.VERTICAL));
        mainLayer.setCheckClipping (true);
        addChild (mainLayer);

        header = new Widget (scene);
        header.setBorder (new EmptyBorder (4));
        header.setOpaque (true);
        header.setLayout (new SerialLayout (SerialLayout.Orientation.HORIZONTAL, SerialLayout.Alignment.CENTER, 0));
        mainLayer.addChild (header);

        imageWidget = new ImageWidget (scene);
        header.setBorder (new EmptyBorder (4));
        header.addChild (imageWidget);

        Widget desc = new Widget (scene);
        desc.setBorder (new EmptyBorder (4));
        desc.setLayout (new SerialLayout (SerialLayout.Orientation.VERTICAL));
        header.addChild (desc);

        nameWidget = new LabelWidget (scene);
        nameWidget.setFont (scene.getDefaultFont ().deriveFont (Font.BOLD));
        desc.addChild (nameWidget);

        typeWidget = new LabelWidget (scene);
        typeWidget.setForeground (Color.GRAY);
        desc.addChild (typeWidget);

        glyphSetWidget = new VMDGlyphSetWidget (scene);
        desc.addChild (glyphSetWidget);

        pinsSeparator = new SeparatorWidget (scene, SeparatorWidget.Orientation.HORIZONTAL);
        mainLayer.addChild (pinsSeparator);

        Widget topLayer = new Widget (scene);
        addChild (topLayer);

        stateModel = new StateModel ();
        stateModel.addListener (this);

        Widget minimizeWidget = new ImageWidget (scene, Utilities.loadImage ("org/netbeans/modules/visual/resources/minimize.png"));
        minimizeWidget.setCursor (Cursor.getPredefinedCursor (Cursor.HAND_CURSOR));
        minimizeWidget.getActions ().addAction (new ToggleMinimizedAction ());

        topLayer.addChild (minimizeWidget);
    }

    /**
     * Check the minimized state.
     */
    public boolean isMinimized () {
        return stateModel.getBooleanState ();
    }

    /**
     * Set the minimized state.  This method will show/hide child
     * Widgets from the this Widget and switch Anchors.
     */
    public void setMinimized (boolean minimized) {
        stateModel.setBooleanState (minimized);
    }

    /**
     * Change the minimized state to !{@link #isMinimized()}.
     */
    public void toggleMinimized () {
        stateModel.toggleBooleanState ();
    }

    public void stateChanged () {
        Rectangle rectangle = stateModel.getBooleanState () ? new Rectangle () : null;
        for (Widget widget : mainLayer.getChildren ())
            if (widget != header  &&  widget != pinsSeparator)
                getScene ().getSceneAnimator ().getPreferredBoundsAnimator ().setPreferredBounds (widget, rectangle);
    }

    protected void notifyStateChanged (ObjectState state) {
        if (state.isHovered ())
            setBorder (BORDER_SHADOW_HOVERED);
        else if (state.isSelected ())
            setBorder (BORDER_SHADOW_SELECTED);
        else
            setBorder (BORDER_SHADOW_NORMAL);
        header.setBackground (getScene ().getLookFeel ().getBackground (state));
    }

    public void setNodeImage (Image image) {
        imageWidget.setImage (image);
        revalidate ();
    }

    public String getNodeName () {
        return nameWidget.getLabel ();
    }

    public void setNodeName (String nodeName) {
        nameWidget.setLabel (nodeName);
    }

    public void setNodeType (String nodeType) {
        typeWidget.setLabel ("[" + nodeType + "]");
    }

    public void attachPinWidget (Widget widget) {
        widget.setCheckClipping (true);
        mainLayer.addChild (widget);
        if (stateModel.getBooleanState ())
            widget.setPreferredBounds (new Rectangle ());
    }

    public void setGlyphs (List<Image> glyphs) {
        glyphSetWidget.setGlyphs (glyphs);
    }

    public void setNodeProperties (Image image, String nodeName, String nodeType, List<Image> glyphs) {
        setNodeImage (image);
        setNodeName (nodeName);
        setNodeType (nodeType);
        setGlyphs (glyphs);
    }

    public LabelWidget getNodeNameWidget () {
        return nameWidget;
    }

    public Anchor getNodeAnchor () {
        return nodeAnchor;
    }

    public Anchor createAnchorPin (Anchor anchor) {
        return AnchorFactory.createProxyAnchor (stateModel, anchor, nodeAnchor);
    }

    private List<Widget> getPinWidgets () {
        ArrayList<Widget> pins = new ArrayList<Widget> (mainLayer.getChildren ());
        pins.remove (header);
        pins.remove (pinsSeparator);
        return pins;
    }

    public void sortPins (HashMap<String, List<Widget>> pinsCategories) {
        List<Widget> previousPins = getPinWidgets ();
        ArrayList<Widget> unresolvedPins = new ArrayList<Widget> (previousPins);

        for (Iterator<Widget> iterator = unresolvedPins.iterator (); iterator.hasNext ();) {
            Widget widget = iterator.next ();
            if (pinCategoryWidgets.containsValue (widget))
                iterator.remove ();
        }

        ArrayList<String> unusedCategories = new ArrayList<String> (pinCategoryWidgets.keySet ());

        ArrayList<String> categoryNames = new ArrayList<String> (pinsCategories.keySet ());
        Collections.sort (categoryNames);

        ArrayList<Widget> newWidgets = new ArrayList<Widget> ();
        for (String categoryName : categoryNames) {
            if (categoryName == null)
                continue;
            unusedCategories.remove (categoryName);
            newWidgets.add (createPinCategoryWidget (categoryName));
            List<Widget> widgets = pinsCategories.get (categoryName);
            for (Widget widget : widgets)
                if (unresolvedPins.remove (widget))
                    newWidgets.add (widget);
        }

        if (! unresolvedPins.isEmpty ())
            newWidgets.addAll (0, unresolvedPins);

        for (String category : unusedCategories)
            pinCategoryWidgets.remove (category);

        mainLayer.removeChildren (previousPins);
        mainLayer.addChildren (newWidgets);
    }

    private Widget createPinCategoryWidget (String categoryDisplayName) {
        Widget w = pinCategoryWidgets.get (categoryDisplayName);
        if (w != null)
            return w;
        LabelWidget label = new LabelWidget (getScene (), categoryDisplayName);
        label.setOpaque (true);
        label.setBackground (COLOR_CATEGORY_BACKGROUND);
        label.setForeground (COLOR_CATEGORY_FOREGROUND);
        label.setFont (fontPinCategory);
        label.setAligment (LabelWidget.Alignment.CENTER);
        label.setCheckClipping (true);
        pinCategoryWidgets.put (categoryDisplayName, label); 
        return label;
    }

    private final class ToggleMinimizedAction extends WidgetAction.Adapter {

        public State mousePressed (Widget widget, WidgetMouseEvent event) {
            if (event.getButton () == MouseEvent.BUTTON1 || event.getButton () == MouseEvent.BUTTON2) {
                stateModel.toggleBooleanState ();
                return State.CONSUMED;
            }
            return State.REJECTED;
        }
    }

}
