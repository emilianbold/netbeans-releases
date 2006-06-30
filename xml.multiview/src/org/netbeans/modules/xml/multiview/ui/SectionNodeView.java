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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.multiview.ui;

import org.netbeans.modules.xml.multiview.SectionNode;
import org.netbeans.modules.xml.multiview.Utils;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataObject;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataSynchronizer;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;

import java.util.HashMap;

/**
 * @author pfiala
 */
public abstract class SectionNodeView extends SectionView {

    private final XmlMultiViewDataObject dataObject;
    private SectionNode rootNode = null;
    private HashMap nodes = new HashMap();

    private final RequestProcessor.Task refreshTask = RequestProcessor.getDefault().create(new Runnable() {
        public void run() {
            getRootNode().refreshSubtree();
        }
    });

    private static final int REFRESH_DELAY = 20;

    public SectionNodeView(XmlMultiViewDataObject dataObject) {
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

    public XmlMultiViewDataObject getDataObject() {
        return dataObject;
    }

    public void openSection(Node node) {
        openPanel(node);
    }

    public void openPanel(Object key) {
        SectionNode sectionNode = retrieveSectionNode((SectionNode) key);
        SectionNodePanel sectionNodePanel = sectionNode.getSectionNodePanel();
        sectionNodePanel.open();
        setActivePanel(sectionNodePanel);
        sectionNodePanel.setActive(true);
        selectNode(sectionNodePanel.getNode());
        Utils.scrollToVisible(sectionNodePanel);
    }

    public SectionNode getRootNode() {
        return rootNode;
    }

    public void registerNode(SectionNode node) {
        nodes.put(node, node);
    }

    public SectionNode retrieveSectionNode(SectionNode node) {
        SectionNode sectionNode = (SectionNode) nodes.get(node);
        return sectionNode == null ? rootNode : sectionNode;
    }

    public void refreshView() {
        rootNode.refreshSubtree();
    }

    public void scheduleRefreshView() {
        refreshTask.schedule(REFRESH_DELAY);
    }

    public void dataModelPropertyChange(Object source, String propertyName, Object oldValue, Object newValue) {
        rootNode.dataModelPropertyChange(source, propertyName, oldValue, newValue);
    }

    public abstract XmlMultiViewDataSynchronizer getModelSynchronizer();

    ;
}