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

import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.nodes.Children;
import org.openide.nodes.AbstractNode;
import org.netbeans.modules.xml.multiview.SectionNode;

import javax.swing.*;
import java.awt.*;

/**
 * @author pfiala
 */
public class SectionNodeView extends SectionView {
    private final DataObject dataObject;
    private SectionNode rootNode = null;


    public SectionNodeView(DataObject dataObject) {
        super();
        this.dataObject = dataObject;
    }

    public void setRootNode(SectionNode rootNode) {
        this.rootNode = rootNode;
        Children root = new Children.Array();
        root.add(new Node[]{rootNode});

        AbstractNode mainNode = new AbstractNode(root);
        mainNode.setDisplayName(rootNode.getDisplayName());
        mainNode.setIconBase(rootNode.getIconBase());
        setRoot(mainNode);
        addSection(rootNode.getSectionNodePanel());
    }

    public DataObject getDataObject() {
        return dataObject;
    }

    protected void openSection(Node node) {
        if(node instanceof SectionNode) {
            SectionNode sectionNode = (SectionNode) node;
            SectionNodePanel sectionPanel = sectionNode.getSectionNodePanel();
            Node parent = sectionNode.getParentNode();
            while (parent instanceof SectionNode) {
                SectionNodePanel parentSectionPanel = ((SectionNode) parent).getSectionNodePanel();
                if (parentSectionPanel.getInnerPanel() == null) {
                    parentSectionPanel.open();
                }
                parent = parent.getParentNode();
            }
            if(sectionPanel.getInnerPanel() == null) {
                sectionPanel.open();
            }
            JComponent comp = sectionPanel;
            comp.scrollRectToVisible(new Rectangle(comp.getWidth(), comp.getHeight()));
            setActivePanel(sectionPanel);
            sectionPanel.setActive(true);
        } else {
            super.openSection(node);
        }
    }

    public void openPanel(Object key) {
        if (key instanceof SectionNode) {
            SectionNodePanel panel = ((SectionNode) key).getSectionNodePanel();
            if (panel.getInnerPanel() == null) {
                panel.open();
            }
            panel.scroll();
            panel.setActive(true);
        } else {
            rootNode.getSectionNodePanel().setActive(true);
        }
    }
}
