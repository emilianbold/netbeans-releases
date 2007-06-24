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

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.AbstractAction;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.widget.LabelWidget;
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
    /** The expand button image. */
    private static final String IMAGE_EXPAND = ">";
    /** The collapse button image. */
    private static final String IMAGE_COLLAPSE = "<";
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
    public ExpanderWidget(Scene scene, ExpandableWidget expandable,
            boolean expanded) {
        super(scene, expanded ? IMAGE_COLLAPSE : IMAGE_EXPAND);
        this.expandable = expandable;
        getButton().setBorder(BorderFactory.createEmptyBorder(0, 4));
        LabelWidget lbl = getButton().getLabelWidget();
        lbl.setOrientation(LabelWidget.Orientation.ROTATE_90);
        lbl.setFont(scene.getFont().deriveFont(Font.BOLD,
                scene.getFont().getSize2D()*1.5f));
        lbl.setForeground(Color.GRAY);
        setAction(new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                ExpanderWidget.this.expandable.setExpanded(
                        !ExpanderWidget.this.expandable.isExpanded());
                
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
    public void setExpanded(boolean expanded) {
        // Save the state of the expandable in case it gets recreated later.
        expandedCache.put(expandable.hashKey(), Boolean.valueOf(expanded));
        getButton().setLabel(expanded?IMAGE_COLLAPSE:IMAGE_EXPAND);
    }

}
