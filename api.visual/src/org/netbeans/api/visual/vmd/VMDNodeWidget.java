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

import org.netbeans.api.visual.action.MouseHoverAction;
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.anchor.ProxyAnchor;
import org.netbeans.api.visual.anchor.RectangularAnchor;
import org.netbeans.api.visual.border.EmptyBorder;
import org.netbeans.api.visual.border.LineBorder;
import org.netbeans.api.visual.layout.SerialLayout;
import org.netbeans.api.visual.widget.*;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

/**
 * @author David Kaspar
 */
public class VMDNodeWidget extends Widget {

    private ImageWidget imageWidget;
    private LabelWidget nameWidget;
    private LabelWidget typeWidget;
    private Widget pinsWidget;
    private VMDGlyphSetWidget glyphSetWidget;
    private VMDMinimizeWidget minimizeWidget;
    private RectangularAnchor nodeAnchor = new RectangularAnchor (this);

    public VMDNodeWidget (Scene scene, MouseHoverAction hoverAction) {
        super (scene);

        setOpaque (true);
        setBorder (new LineBorder (1));
        setCursor (new Cursor (Cursor.MOVE_CURSOR));
        getActions ().addAction (hoverAction);


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
//        inner.addChild (pinsWidget);
        mainLayer.addChild (pinsWidget);

//        SeparatorWidget separator2 = new SeparatorWidget (scene, SeparatorWidget.Orientation.VERTICAL);
//        separator2.setBorder (new EmptyBorder (8));
//        inner.addChild (separator2);


        Widget topLayer = new Widget (scene);
        addChild (topLayer);

        minimizeWidget = new VMDMinimizeWidget (scene, mainLayer, Arrays.asList (pinsSeparator, pinsWidget));
        topLayer.addChild (minimizeWidget);
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
    
    public Anchor getNodeAnchor () {
        return nodeAnchor;
    }
    
    public ProxyAnchor createAnchorPin (Anchor anchor) {
        ProxyAnchor proxyAnchor = new ProxyAnchor (anchor, nodeAnchor);
        minimizeWidget.addProxyAnchor (proxyAnchor);
        return proxyAnchor;
    }

}
