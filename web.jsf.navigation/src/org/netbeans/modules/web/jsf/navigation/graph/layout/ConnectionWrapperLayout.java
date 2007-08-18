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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.jsf.navigation.graph.layout;

import java.util.EnumSet;
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.layout.Layout;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Widget;

/**
 * This wrapper delegates to original ConnectionLayout, but allows lazy label formating.
 * @author joelle
 */
public class ConnectionWrapperLayout implements Layout {

    private ConnectionWidget connectionWidget;
    private Layout connectionWidgetLayout;
    private LabelWidget label;

    public ConnectionWrapperLayout(ConnectionWidget connectionWidget, LabelWidget label) {
        this.connectionWidget = connectionWidget;
        this.connectionWidgetLayout = connectionWidget.getLayout();
        this.label = label;
    }

    public void layout(Widget widget) {
        connectionWidgetLayout.layout(widget);
        resetLabelConstraint(connectionWidget, label);
    }

    public boolean requiresJustification(Widget widget) {
        return connectionWidgetLayout.requiresJustification(widget);
    }

    public void justify(Widget widget) {
        connectionWidgetLayout.justify(widget);
    }

    private static final void resetLabelConstraint(ConnectionWidget connectionWidget, LabelWidget label) {
        assert connectionWidget != null;
        
        if (label != null) {

            connectionWidget.removeConstraint(label);
            connectionWidget.removeChild(label);

            EnumSet<Anchor.Direction> directions = connectionWidget.getSourceAnchor().compute(connectionWidget.getSourceAnchorEntry()).getDirections();
            if (directions.contains(Anchor.Direction.TOP)) {
                label.setOrientation(LabelWidget.Orientation.ROTATE_90);
                connectionWidget.setConstraint(label, LayoutFactory.ConnectionWidgetLayoutAlignment.TOP_RIGHT, 10);
            } else if (directions.contains(Anchor.Direction.BOTTOM)) {
                label.setOrientation(LabelWidget.Orientation.ROTATE_90);
                connectionWidget.setConstraint(label, LayoutFactory.ConnectionWidgetLayoutAlignment.BOTTOM_RIGHT, 10);
            } else if (directions.contains(Anchor.Direction.RIGHT)) {
                connectionWidget.setConstraint(label, LayoutFactory.ConnectionWidgetLayoutAlignment.TOP_RIGHT, 10);
                label.setOrientation(LabelWidget.Orientation.NORMAL);
            } else {
                connectionWidget.setConstraint(label, LayoutFactory.ConnectionWidgetLayoutAlignment.TOP_LEFT, 10);
                label.setOrientation(LabelWidget.Orientation.NORMAL);
            }
            connectionWidget.addChild(label);
        }
    }
}
