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
package org.netbeans.modules.xml.multiview;

import org.netbeans.modules.xml.multiview.ui.BoxPanel;
import org.netbeans.modules.xml.multiview.ui.SectionInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionNodePanel;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.netbeans.modules.xml.multiview.ui.SectionNodeInnerPanel;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

import java.util.List;
import java.util.LinkedList;
import java.awt.*;

/**
 * @author pfiala
 */
public class SectionNode extends AbstractNode {

    protected final Object key;
    private boolean expanded = false;
    private SectionNodePanel sectionPanel = null;
    private final String iconBase;
    private final SectionNodeView sectionNodeView;

    /**
     * Create a new section node with a given child set.
     *
     * @param children
     * @param key
     * @param title
     */
    protected SectionNode(SectionNodeView sectionNodeView, Children children, Object key, String title,
                                                         String iconBase) {
        super(children);
        this.sectionNodeView = sectionNodeView;
        this.key = key;
        super.setDisplayName(title);
        super.setIconBase(iconBase);
        this.iconBase = iconBase;
        sectionNodeView.registerNode(this);
    }

    public SectionNodeView getSectionNodeView() {
        return sectionNodeView;
    }

    public Object getKey() {
        return key;
    }

    public void addChild(SectionNode node) {
        getChildren().add(new Node[]{node});
    }

    public SectionNodeInnerPanel createInnerPanel() {
        Children children = getChildren();
        if (children.getNodesCount() == 0) {
            return createNodeInnerPanel();
        } else {
            BoxPanel boxPanel = new BoxPanel(sectionNodeView);
            populateBoxPanel(boxPanel);
            return boxPanel;
        }
    }

    public void populateBoxPanel() {
        SectionInnerPanel innerPanel = getSectionNodePanel().getInnerPanel();
        if (innerPanel instanceof BoxPanel) {
            populateBoxPanel((BoxPanel) innerPanel);
        }
    }
    public void populateBoxPanel(BoxPanel boxPanel) {
        List nodeList = new LinkedList();
        SectionInnerPanel nodeInnerPanel = createNodeInnerPanel();
        if (nodeInnerPanel != null) {
            nodeList.add(nodeInnerPanel);
        }
        Node[] nodes = getChildren().getNodes();
        for (int i = 0; i < nodes.length; i++) {
            nodeList.add(((SectionNode) nodes[i]).getSectionNodePanel());
        }
        boxPanel.setComponents((Component[]) nodeList.toArray(new Component[0]));
    }


    public boolean canDestroy() {
        return true;
    }

    protected SectionNodeInnerPanel createNodeInnerPanel() {
        return null;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public SectionNodePanel getSectionNodePanel() {
        if (sectionPanel == null) {
            sectionPanel = new SectionNodePanel(this);
        }
        return sectionPanel;
    }

    public String getIconBase() {
        return iconBase;
    }

    public boolean equals(Object obj) {
        if (getClass() == obj.getClass()) {
            if (key.equals(((SectionNode) obj).key)) {
                return true;
            }
        }
        return false;
    }

    public int hashCode() {
        return key.hashCode();
    }
                 
    public final void dataModelPropertyChange(Object source, String propertyName, Object oldValue, Object newValue) {
        if (sectionPanel != null) {
            SectionInnerPanel innerPanel = sectionPanel.getInnerPanel();
            if (innerPanel != null) {
                innerPanel.dataModelPropertyChange(source, propertyName, oldValue, newValue);
            }
        }
        Children children = getChildren();
        if (children != null) {
            Node[] nodes = children.getNodes();
            for (int i = 0; i < nodes.length; i++) {
                Node node = nodes[i];
                if (node instanceof SectionNode) {
                    ((SectionNode) node).dataModelPropertyChange(source, propertyName, oldValue, newValue);
                }
            }
        }
    }

    public void refreshSubtree() {
        if (sectionPanel != null) {
            SectionInnerPanel innerPanel = sectionPanel.getInnerPanel();
            if (innerPanel != null) {
                innerPanel.refreshView();
            }
        }
        Children children = getChildren();
        if (children != null) {
            Node[] nodes = children.getNodes();
            for (int i = 0; i < nodes.length; i++) {
                Node node = nodes[i];
                if (node instanceof SectionNode) {
                    ((SectionNode) node).refreshSubtree();
                }
            }
        }
    }
}
