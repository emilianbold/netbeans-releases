/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.multiview.ui;

import org.netbeans.modules.xml.multiview.SectionNode;
import org.openide.nodes.Node;

import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.*;

/**
 * @author pfiala
 */
public class SectionNodePanel extends SectionPanel {

    public SectionNodePanel(SectionNode node) {
        super(node.getSectionView(), node, node.getDisplayName(), node.getKey());
        if(node.getKey() instanceof SectionView) {
            setExpandedViewMode();
        } else if(node.isExpanded()) {
            setInnerViewMode();

        }
    }

    protected void setInnerViewMode() {
        getTitleButton().setVisible(true);
        getFoldButton().setVisible(false);
        getSeparator().setVisible(false);
        Border emptyBorder = new EmptyBorder(0, 4, 4, 4);
        Border lineBorder;
        lineBorder = new JTextField().getBorder();

        setBorder(new CompoundBorder(emptyBorder, new CompoundBorder(lineBorder, emptyBorder)));
        openInnerPanel();
        getFiller().setVisible(false);
    }

    protected void setExpandedViewMode() {
        getTitleButton().setVisible(false);
        getFoldButton().setVisible(false);
        getSeparator().setVisible(false);
        openInnerPanel();
        getFiller().setVisible(false);
    }

    protected SectionInnerPanel createInnerpanel() {
        SectionInnerPanel innerPanel = ((SectionNode) getNode()).createInnerPanel();
        if (innerPanel == null) {
            SectionNode node = (SectionNode) getNode();
            innerPanel = new BoxPanel(node.getSectionView());
        }
        return innerPanel;
    }

    public void open() {
        Node parentNode = getNode().getParentNode();
        if(parentNode instanceof SectionNode) {
            ((SectionNode)parentNode).getSectionNodePanel().open();
        }
        super.open();
    }
}
