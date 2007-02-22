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

package org.netbeans.modules.xml.xam.ui.column;

import java.awt.Component;
import javax.swing.JComponent;
import javax.swing.JList;
import org.netbeans.modules.xml.xam.ui.cookies.CountChildrenCookie;
import org.openide.awt.HtmlRenderer;
import org.openide.explorer.view.NodeRenderer;
import org.openide.explorer.view.Visualizer;
import org.openide.nodes.Node;

/**
 * Renders the components in the list view.
 *
 * @author  Nathan Fiedler
 */
public class ColumnListCellRenderer extends NodeRenderer {
    private static final long serialVersionUID = 1L;

    public ColumnListCellRenderer() {
        super();
    }

    public Component getListCellRendererComponent(JList list,
            Object value, int index, boolean selected, boolean focused) {
        Component c = super.getListCellRendererComponent(
                list, value, index, selected, focused);
        if (c instanceof JComponent) {
            // Add our child indicator to the node renderer.
            // Testing if the value is a leaf is useless as the nodes
            // usually have non-leaf Children instances.
            Node node = Visualizer.findNode(value);
            boolean hasChildren = true;
            CountChildrenCookie ccc = (CountChildrenCookie) node.getCookie(
                        CountChildrenCookie.class);
            if (ccc != null) {
                hasChildren = ccc.getChildCount() > 0;
            }
            ArrowBorder border = new ArrowBorder(hasChildren);
            JComponent jc = (JComponent) c;
            jc.setBorder(border);
        }
        if (c instanceof HtmlRenderer.Renderer) {
            // Prefer the ... over simply clipping the label.
            HtmlRenderer.Renderer hr = (HtmlRenderer.Renderer) c;
            hr.setRenderStyle(HtmlRenderer.STYLE_TRUNCATE);
        }
        return c;
    }
}
