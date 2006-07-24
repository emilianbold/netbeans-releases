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
import org.netbeans.api.visual.anchor.ProxyAnchor;
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
import java.util.List;

/**
 * @author David Kaspar
 */
public class VMDNodeWidget extends Widget implements StateModel.Listener {

    private static Border BORDER_SHADOW_NORMAL = new ImageBorder (new Insets (5, 5, 5, 5), Utilities.loadImage ("org/netbeans/modules/visual/resources/border/shadow_normal.png")); // NOI18N
    private static Border BORDER_SHADOW_HOVERED = new ImageBorder (new Insets (5, 5, 5, 5), Utilities.loadImage ("org/netbeans/modules/visual/resources/border/shadow_hovered.png")); // NOI18N
    private static Border BORDER_SHADOW_SELECTED = new ImageBorder (new Insets (5, 5, 5, 5), Utilities.loadImage ("org/netbeans/modules/visual/resources/border/shadow_selected.png")); // NOI18N

    private ImageWidget imageWidget;
    private LabelWidget nameWidget;
    private LabelWidget typeWidget;
    private Widget pinsWidget;
    private VMDGlyphSetWidget glyphSetWidget;

    private StateModel stateModel = new StateModel (2);
    private Anchor nodeAnchor = new VMDNodeAnchor (this);

    public VMDNodeWidget (Scene scene) {
        super (scene);

        setBackground (Color.WHITE);
        setOpaque (true);
        setBorder (BORDER_SHADOW_NORMAL);
        setCursor (new Cursor (Cursor.MOVE_CURSOR));

        final Widget mainLayer = new Widget (scene);
        mainLayer.setLayout (new SerialLayout (SerialLayout.Orientation.VERTICAL));
        addChild (mainLayer);

        Widget header = new Widget (scene);
        header.setBorder (new EmptyBorder (4));
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

        Widget pinsSeparator = new SeparatorWidget (scene, SeparatorWidget.Orientation.HORIZONTAL);
        mainLayer.addChild (pinsSeparator);

//        Widget inner = new Widget (scene);
//        inner.setLayout (new SerialLayout (SerialLayout.Orientation.HORIZONTAL));
//        inner.setLayout (new SerialLayout (SerialLayout.Orientation.HORIZONTAL));
//        addChild (inner);

//        SeparatorWidget separator1 = new SeparatorWidget (scene, SeparatorWidget.Orientation.VERTICAL);
//        separator1.setBorder (new EmptyBorder (8));
//        inner.addChild (separator1);

        pinsWidget = new Widget (scene);
        pinsWidget.setBorder (new EmptyBorder (8, 4));
        pinsWidget.setLayout (new SerialLayout (SerialLayout.Orientation.VERTICAL, SerialLayout.Alignment.JUSTIFY, 0));
        pinsWidget.setCheckClipping (true);
//        inner.addChild (pinsWidget);
        mainLayer.addChild (pinsWidget);

//        SeparatorWidget separator2 = new SeparatorWidget (scene, SeparatorWidget.Orientation.VERTICAL);
//        separator2.setBorder (new EmptyBorder (8));
//        inner.addChild (separator2);


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
    public boolean isMinimized() {
        return stateModel.getBooleanState ();
    }

    /**
     * Set the minimized state.  This method will show/hide child
     * Widgets from the this Widget and switch Anchors.
     */
    public void setMinimized(boolean minimized) {
        stateModel.setBooleanState (minimized);
    }

    /**
     * Change the minimized state to !{@link #isMinimized()}.
     */
    public void toggleMinimized() {
        stateModel.toggleBooleanState ();
    }

    public void stateChanged () {
        pinsWidget.setPreferredBounds (stateModel.getBooleanState () ? new Rectangle () : null);
    }

    protected void notifyStateChanged (ObjectState state) {
        if (state.isHovered ())
            setBorder (BORDER_SHADOW_HOVERED);
        else if (state.isSelected ())
            setBorder (BORDER_SHADOW_SELECTED);
        else
            setBorder (BORDER_SHADOW_NORMAL);
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

    public void addPin (VMDPinWidget widget) {
        pinsWidget.addChild (widget);
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

    public Anchor getNodeAnchor () {
        return nodeAnchor;
    }
    
    public ProxyAnchor createAnchorPin (Anchor anchor) {
        return new ProxyAnchor (stateModel, anchor, nodeAnchor);
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
