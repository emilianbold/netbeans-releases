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

package org.netbeans.modules.websvc.design.view.widget;

import java.awt.BasicStroke;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.geom.GeneralPath;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.AbstractAction;
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
 * @author Ajit Bhate
 * @author radval
 * @author Nathan Fiedler
 */
public class ExpanderWidget extends ButtonWidget {

    /** Cache of the expanded state of ExpandableWidget instances. This
     * is used to restore the original state of an expandable if it is
     * created again, say as a result of an undo/redo operation. */
    private static Map<Object, Boolean> expandedCache;
    /** The expandable (content) widget. */
    private ExpandableWidget expandable;
    static {
        expandedCache = new WeakHashMap<Object, Boolean>();
    }

    /**
     * Creates a new instance of ExpanderWidget.
     *
     * @param  scene       the Scene to contain this widget.
     * @param  expandable  the expandable widget this expander will control.
     * @param  expanded    true if widget is initially expanded, false if collapsed.
     */
    public ExpanderWidget(Scene scene, ExpandableWidget expandable, boolean expanded) {
        super(scene, (String) null);
        this.expandable = expandable;
        setImage(new ExpanderImageWidget(scene, true, 8, 30));
        setSelectedImage(new ExpanderImageWidget(scene, false, 8, 30));
        setSelected(expanded);
        setRoundedBorder(0, 4, 4, null);
        setAction(new AbstractAction() {

            public void actionPerformed(ActionEvent arg0) {
                ExpanderWidget.this.expandable.setExpanded(!ExpanderWidget.this.expandable.isExpanded());
            }
        });
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
    @Override
    public void setSelected(boolean expanded) {
        // Save the state of the expandable in case it gets recreated later.
        expandedCache.put(expandable.hashKey(), Boolean.valueOf(expanded));
        super.setSelected(expanded);
        revalidate(true);
    }

    private static class ExpanderImageWidget extends ImageLabelWidget.PaintableImageWidget {

        private static final Stroke STROKE = new BasicStroke(2.5F, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER);

        private double cos;

        public ExpanderImageWidget(Scene scene, boolean up, int size, int degrees) {
            super(scene, BORDER_COLOR, size, size);
            double radians = Math.PI * degrees / 180.0;
            cos = Math.cos(radians / 2.0);
            if (!up) {
                cos = -cos;
            }
        }

        protected Stroke getImageStroke() {
            return STROKE;
        }

        protected Shape createImage(int width, int height) {
            GeneralPath path = new GeneralPath();
            float y1 = (float) (height+cos*height/2) / 2;
            float y2 = (float) (height-cos*height/2) / 2;
            path.moveTo(width / 2, y1);
            path.lineTo(0, y2);
            path.moveTo(width / 2, y1);
            path.lineTo(width, y2);
            return path;
        }
    }
}
