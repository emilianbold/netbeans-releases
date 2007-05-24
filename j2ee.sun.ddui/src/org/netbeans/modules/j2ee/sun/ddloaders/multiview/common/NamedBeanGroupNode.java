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

package org.netbeans.modules.j2ee.sun.ddloaders.multiview.common;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion;
import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.RootInterface;
import org.netbeans.modules.j2ee.sun.ddloaders.Utils;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.BaseSectionNode;
import org.netbeans.modules.xml.multiview.SectionNode;
import org.netbeans.modules.xml.multiview.ui.BoxPanel;
import org.netbeans.modules.xml.multiview.ui.SectionNodeInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Mutex;


/**
 * @author Peter Williams
 */
public abstract class NamedBeanGroupNode extends BaseSectionNode {

    protected RootInterface rootDD;
    private String beanNameProperty;
            
    private volatile boolean doCheck = false;
    private volatile boolean checking = false;

    public NamedBeanGroupNode(SectionNodeView sectionNodeView, RootInterface rootDD, 
            String beanNameProperty, String header, String iconBase, ASDDVersion version) {
        super(sectionNodeView, false, rootDD, version, header, iconBase);
        
        this.rootDD = rootDD;
        this.beanNameProperty = beanNameProperty;
        
        checkChildren();
    }

    protected abstract SectionNode createNode(CommonDDBean bean);

    protected abstract CommonDDBean [] getBeansFromModel();
    
    public SectionNodeInnerPanel createInnerPanel() {
        SectionNodeView sectionNodeView = getSectionNodeView();
        BoxPanel boxPanel = new BoxPanel(sectionNodeView) {
            public void dataModelPropertyChange(Object source, String propertyName, Object oldValue, Object newValue) {
                if(source == rootDD) {
                    if(oldValue != null && newValue == null || oldValue == null && newValue != null) {
                        checkChildren();
                    }
                }
            }

            public void refreshView() {
                checkChildren();
            }
        };
        populateBoxPanel(boxPanel);
        return boxPanel;
    }

    private void checkChildren() {
        Mutex.EVENT.readAccess(new Runnable() {
            public void run() {
                doCheck = true;
                if (setChecking(true)) {
                    try {
                        while (doCheck) {
                            doCheck = false;
                            check();
                        }
                    } finally {
                        setChecking(false);
                    }
                }
            }
        });
    }

    private synchronized boolean setChecking(boolean value) {
        if (value) {
            if (checking) {
                return false;
            } else {
                checking = true;
                return true;
            }
        } else {
            checking = false;
            return true;
        }
    }

    private void check() {
        Map nodeMap = new HashMap();
        Children children = getChildren();
        Node[] nodes = children.getNodes();
        for (int i = 0; i < nodes.length; i++) {
            Node node = nodes[i];
            nodeMap.put(((SectionNode) node).getKey(), node);
        }
        
        CommonDDBean [] beans = getBeansFromModel();
        
        // Sort beans by display name.
        Arrays.sort(beans, new Comparator<CommonDDBean>() {
            public int compare(CommonDDBean b1, CommonDDBean b2) {
                return Utils.getBeanDisplayName(b1, beanNameProperty).compareTo(
                        Utils.getBeanDisplayName(b2, beanNameProperty));
            }
        });
        
        boolean dirty = nodes.length != beans.length;
        Node[] newNodes = new Node[beans.length];
        for (int i = 0; i < beans.length; i++) {
            CommonDDBean bean = beans[i];
            SectionNode node = (SectionNode) nodeMap.get(bean);
            if (node == null) {
                node = createNode(bean);
                dirty = true;
            }
            newNodes[i] = node;
            if (!dirty) {
                dirty = ((SectionNode) nodes[i]).getKey() != node.getKey();
            }
        }
        if (dirty) {
            children.remove(nodes);
            children.add(newNodes);
            populateBoxPanel();
        }
    }

}
