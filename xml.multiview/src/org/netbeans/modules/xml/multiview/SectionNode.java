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

import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.netbeans.modules.xml.multiview.ui.SectionPanel;
import org.netbeans.modules.xml.multiview.ui.SectionInnerPanel;
import org.netbeans.modules.xml.multiview.ui.BoxPanel;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.netbeans.modules.xml.multiview.ui.InnerPanelFactory;
import org.netbeans.modules.xml.multiview.ui.SectionNodePanel;

/**
 * @author pfiala
 */
public class SectionNode extends AbstractNode implements InnerPanelFactory {

    protected final Object key;
    private boolean expanded = false;
    private SectionNodePanel sectionPanel = null;
    private String iconBase;

    public SectionNode(boolean isLeaf, Object key, String title, String iconBase) {
        this(isLeaf ? Children.LEAF : new Children.Array(), key, title, iconBase);
    }

    public SectionNode(Object key, String title, String iconBase) {
        this(false, key, title, iconBase);
    }

    /**
     * Create a new section node with a given child set.
     *
     * @param children
     * @param key
     * @param title
     */
    protected SectionNode(Children children, Object key, String title, String iconBase) {
        super(children);
        this.key = key;
        super.setDisplayName(title);
        super.setIconBase(iconBase);
        this.iconBase = iconBase;
    }

    public SectionNodeView getSectionView() {
        if(key instanceof SectionNodeView) {
            return (SectionNodeView) key;
        } else {
            return ((SectionNode) getParentNode()).getSectionView();
        }
    }

    public Object getKey() {
        return key;
    }

    public void addChild(SectionNode node) {
        getChildren().add(new Node[]{node});
    }

    public SectionInnerPanel createInnerPanel() {
        Children children = getChildren();
        if(children.getNodesCount() == 0) {
            return createNodeInnerPanel();
        } else {
            BoxPanel boxPanel = new BoxPanel(getSectionView());
            SectionInnerPanel nodeInnerPanel = createNodeInnerPanel();
            if(nodeInnerPanel != null) {
                boxPanel.add(nodeInnerPanel);
            }
            Node[] nodes = children.getNodes();
            for (int i = 0; i < nodes.length; i++) {
                boxPanel.add(((SectionNode) nodes[i]).getSectionNodePanel());
            }
            return boxPanel;
        }
    }

    public boolean canDestroy() {
        return true;
    }

    protected SectionInnerPanel createNodeInnerPanel() {
        return null;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public SectionNodePanel getSectionNodePanel() {
        if(sectionPanel == null) {
            sectionPanel = new SectionNodePanel(this);
        }
        return sectionPanel;
    }

    public SectionInnerPanel createInnerPanel(Object key) {
        if(key == this) {
            return createInnerPanel();
        } else {
            return null;
        }
    }

    public String getIconBase() {
        return iconBase;
    }
}
