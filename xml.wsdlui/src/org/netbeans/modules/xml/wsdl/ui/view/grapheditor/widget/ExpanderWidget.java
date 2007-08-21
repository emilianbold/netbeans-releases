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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.api.visual.widget.Scene;

/**
 * Class ExpanderWidget provides a simple icon widget for controlling the
 * expanded/collapsed state of another widget. This widget can be added to
 * any widget, and given an instance of ExpandableWidget, it can make that
 * object expand or collapse each time the icon is clicked by the user.
 * It is up to the ExpandableWidget implementation to determine how the
 * size of the widget is altered. This class provides one or more methods
 * for altering the widget size.
 *
 * @author radval
 * @author Nathan Fiedler
 */
public class ExpanderWidget extends ButtonWidget implements ActionListener {
    /** The expand button image. */
    private static final Image IMAGE_EXPAND = new BufferedImage(12, 12,
            BufferedImage.TYPE_INT_ARGB);
    /** The collapse button image. */
    private static final Image IMAGE_COLLAPSE = new BufferedImage(12, 12,
            BufferedImage.TYPE_INT_ARGB);
    /** Cache of the expanded state of ExpandableWidget instances. This
     * is used to restore the original state of an expandable if it is
     * created again, say as a result of an undo/redo operation. */
    private static Map<Object, Boolean> expandedCache;
    /** The expandable (content) widget. */
    private ExpandableWidget expandable;
    /** True if expanded, false if collapsed. */
    private boolean isExpanded;

    static {
        expandedCache = new WeakHashMap<Object, Boolean>();

        // Create the expand image.
        Graphics2D g2 = ((BufferedImage) IMAGE_EXPAND).createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        float w = IMAGE_EXPAND.getWidth(null);
        float h = IMAGE_EXPAND.getHeight(null);
        float r = Math.min(w, h) * 0.5f * 0.75f;
        GeneralPath gp = new GeneralPath();
        float dx = (float) (r * Math.cos(Math.toRadians(-30)));
        float dy = (float) (r * Math.sin(Math.toRadians(-30)));
        gp.moveTo(dx, dy);
        gp.lineTo(0, r);
        gp.lineTo(-dx, dy);
        gp.lineTo(dx, dy);
        gp.closePath();
        g2.translate(w / 2, h / 2);
        g2.setPaint(new Color(0x888888));
        g2.fill(gp);

        // Create the collapse image.
        g2 = ((BufferedImage) IMAGE_COLLAPSE).createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        w = IMAGE_EXPAND.getWidth(null);
        h = IMAGE_EXPAND.getHeight(null);
        r = Math.min(w, h) * 0.5f * 0.75f;
        gp = new GeneralPath();
        dx = (float) (r * Math.cos(Math.toRadians(30)));
        dy = (float) (r * Math.sin(Math.toRadians(30)));
        gp.moveTo(dx, dy);
        gp.lineTo(0, -r);
        gp.lineTo(-dx, dy);
        gp.lineTo(dx, dy);
        gp.closePath();
        g2.translate(w / 2, h / 2);
        g2.setPaint(new Color(0x888888));
        g2.fill(gp);
    }

    /**
     * Creates a new instance of ExpanderWidget.
     *
     * @param  scene       the Scene to contain this widget.
     * @param  expandable  the expandable widget this expander will control.
     * @param  expanded    true if widget is initially expanded, false if collapsed.
     */
    public ExpanderWidget(Scene scene, ExpandableWidget expandable,
            boolean expanded) {
        super(scene, expanded ? IMAGE_COLLAPSE : IMAGE_EXPAND);
        this.expandable = expandable;
        isExpanded = expanded;
        setMargin(new Insets(2, 2, 2, 2));
        setActionListener(this);
        getActions().removeAction(((PartnerScene)scene).getSelectAction());
    }

//    /**
//     * Animate the preferred bounds of the given widget to the desired size.
//     *
//     * @param  content  widget whose size will be changed.
//     * @param  size     the new size for the widget.
//     */
//    public void setExpandableSize(Widget content, Dimension size) {
//        Dimension delta = null;
//        Rectangle bounds = content.getBounds();
//        if (isExpanded) {
//            delta = new Dimension(bounds.width - size.width,
//                    bounds.height - size.height);
//        } else {
//            delta = new Dimension(size.width - bounds.width,
//                    size.height - bounds.height);
//        }
//
//// XXX: note that the child widgets also need to have their size changed,
////      but the question is, how do we determine the appropriate sizing?
//
//        // Resize the expandable widget.
//        SceneAnimator anim = getScene().getSceneAnimator();
//        anim.animatePreferredBounds(content, new Rectangle(
//                0, 0, size.width, size.height));
//
//        // Adjust the size of the parents up to the root of the tree.
//        Widget parent = content.getParentWidget();
//        while (parent != null) {
//            Rectangle pbounds = parent.getBounds();
//            anim.animatePreferredBounds(parent, new Rectangle(
//                    0, 0, pbounds.width + delta.width,
//                    pbounds.height + delta.height));
//            parent = parent.getParentWidget();
//        }
//    }

    public void actionPerformed(ActionEvent e) {
        setExpanded(!isExpanded);
    }

    /**
     * Indicates if this expander is expanded or collapsed.
     *
     * @return  true if expanded, false if collapsed.
     */
    public boolean isExpanded() {
        return isExpanded;
    }

    /**
     * Retrieve the former expanded state of the given expandable. If
     * the expandable state was not cached (or the cache has been cleaned
     * by the garbage collector), this method returns the value of the
     * <code>def</code> parameter.
     *
     * @param  expandable  the ExpandableWidget to query.
     * @param  def         default value for the expanded state.
     * @return  true if expanded, false if collapsed.
     */
    public static boolean isExpanded(ExpandableWidget expandable, boolean def) {
        Boolean val = expandedCache.get(expandable.hashKey());
        return val != null ? val.booleanValue() : def;
    }

    /**
     * Set the expanded state of the widget.
     *
     * @param  expanded  true to expand, false to collapse.
     */
    public void setExpanded(boolean expanded) {
        // Save the state of the expandable in case it gets recreated later.
        expandedCache.put(expandable.hashKey(), Boolean.valueOf(expanded));
        isExpanded = expanded;
        setIcon(isExpanded ? IMAGE_COLLAPSE : IMAGE_EXPAND);
        if (isExpanded) {
            expandable.expandWidget(this);
        } else {
            expandable.collapseWidget(this);
        }
    }
}
