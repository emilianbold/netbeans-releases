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
import org.netbeans.modules.xml.multiview.Utils;
import org.openide.nodes.Node;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

/**
 * @author pfiala
 *         <p/>
 *         The SectionNodePanel shows data of related SectionNode object
 *         which contains all information about the section
 */
public class SectionNodePanel extends SectionPanel {

    public SectionNodePanel(final SectionNode node) {
        super(node.getSectionNodeView(), node, node.getDisplayName(), node);
        if (node.getKey() instanceof SectionView) {
            // the section corresponding to the top level node is always expanded
            setInnerViewMode();
        } else if (node.isExpanded()) {
            setExpandedViewMode();
        }
        node.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (Node.PROP_DISPLAY_NAME.equals(evt.getPropertyName())) {
                    setTitle(node.getDisplayName());
                }
            }
        });
    }

    /**
     * The expanded viev mode shows only title bar and border around inner panel,
     * The inner panel is always visible and the section cannot be collapsed
     */
    protected void setExpandedViewMode() {
        getTitleButton().setVisible(true);
        getFoldButton().setVisible(false);
        getHeaderSeparator().setVisible(false);
        Border emptyBorder = new EmptyBorder(0, 4, 4, 4);
        Border lineBorder;
        lineBorder = new JTextField().getBorder();

        setBorder(new CompoundBorder(emptyBorder, new CompoundBorder(lineBorder, emptyBorder)));
        openInnerPanel();
        getFillerLine().setVisible(false);
        getFillerEnd().setVisible(false);
    }

    /**
     * The inner view mode shows only inner panel.
     * The inner panel is always visible and the section cannot be collapsed
     */
    protected void setInnerViewMode() {
        getTitleButton().setVisible(false);
        getFoldButton().setVisible(false);
        getHeaderSeparator().setVisible(false);
        openInnerPanel();
        getFillerLine().setVisible(false);
        getFillerEnd().setVisible(false);
    }

    /**
     * Creation of inner panel using related SectionNode object
     *
     * @return newly created inner panel
     */
    protected SectionInnerPanel createInnerpanel() {
        SectionInnerPanel innerPanel = ((SectionNode) getNode()).createInnerPanel();
        if (innerPanel == null) {
            // This case arises only if the inner panel has not been implemented yet.
            // Then we show empty panel.
            innerPanel = new BoxPanel(((SectionNode) getNode()).getSectionNodeView());
        }
        return innerPanel;
    }

    protected void openInnerPanel() {
        super.openInnerPanel();
        Node[] childNodes = ((SectionNode) getNode()).getChildren().getNodes();
        if (childNodes != null && childNodes.length > 0) {
            ((SectionNode) childNodes[0]).getSectionNodePanel().openInnerPanel();
        }
    }

    protected void closeInnerPanel() {
        if (getFoldButton().isVisible()) {
            if (getSectionView().getActivePanel() == this) {
                Container parent = getParent();
                while (parent != null) {
                    if (parent instanceof SectionPanel) {
                        final SectionPanel sectionPanel = (SectionPanel) parent;
                        Utils.runInAwtDispatchThread(new Runnable() {
                            public void run() {
                                sectionPanel.setActive(true);
                            }
                        });
                        break;
                    } else {
                        parent = parent.getParent();
                    }
                }
            }
            super.closeInnerPanel();
        }
    }

    /**
     * Method of NodeSectionPanel interface
     */
    public void open() {
        Node parentNode = getNode().getParentNode();
        if (parentNode instanceof SectionNode) {
            ((SectionNode) parentNode).getSectionNodePanel().open();
        }
        if (getInnerPanel() == null) {
            super.open();
        }
    }
}
