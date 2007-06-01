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
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion;
import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.common.PortInfo;
import org.netbeans.modules.j2ee.sun.ddloaders.SunDescriptorDataObject;
import org.netbeans.modules.j2ee.sun.ddloaders.Utils;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.BaseSectionNode;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.DDSectionNodeView;
import org.netbeans.modules.xml.multiview.SectionNode;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataObject;
import org.netbeans.modules.xml.multiview.ui.BoxPanel;
import org.netbeans.modules.xml.multiview.ui.SectionNodeInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionNodePanel;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Mutex;


/**
 * @author Peter Williams
 */
public abstract class NamedBeanGroupNode extends BaseSectionNode {

    protected CommonDDBean commonDD;
    private String beanNameProperty;
    private AddBeanAction addBeanAction;
    
    private volatile boolean doCheck = false;
    private volatile boolean checking = false;
    private volatile boolean modelInitialized = false;

    private static AtomicInteger newBeanId = new AtomicInteger(1);
    
    public NamedBeanGroupNode(SectionNodeView sectionNodeView, CommonDDBean commonDD,
            String beanNameProperty, String header, String iconBase,
            ASDDVersion version) {
        super(sectionNodeView, false, commonDD, version, header, iconBase);
        
        this.commonDD = commonDD;
        this.beanNameProperty = beanNameProperty;
        
        setExpanded(true);
    }

    @Override
    public void refreshSubtree() {
        if(!modelInitialized) {
            checkChildren(null);
            modelInitialized = true;
        }
        
        super.refreshSubtree();
    }
    
    /** Expected to be called from derived class constructor, if needed.
     */
    protected void enableAddAction(String addActionTitle) {
        addBeanAction = new AddBeanAction(addActionTitle);
    }
    
    protected abstract SectionNode createNode(CommonDDBean bean);
    
    protected abstract CommonDDBean [] getBeansFromModel();
    
    protected abstract CommonDDBean addNewBean();
    
    protected abstract void removeBean(CommonDDBean bean);
    
    @Override
    public SectionNodeInnerPanel createInnerPanel() {
        SectionNodeView sectionNodeView = getSectionNodeView();
        BoxPanel boxPanel = new BoxPanel(sectionNodeView) {
            @Override
            public void dataModelPropertyChange(Object source, String propertyName, Object oldValue, Object newValue) {
                if(source == commonDD) {
                    if(oldValue != null && newValue == null || oldValue == null && newValue != null) {
                        checkChildren(null);
                    }
                }
            }
            
            @Override
            public void refreshView() {
                checkChildren(null);
            }
        };
        populateBoxPanel(boxPanel);
        return boxPanel;
    }
    
    @Override
    public SectionNodePanel getSectionNodePanel() {
        SectionNodePanel nodePanel = super.getSectionNodePanel();
        if(addBeanAction != null && nodePanel.getHeaderButtons() == null) {
            nodePanel.setHeaderActions(new Action [] { addBeanAction });
        }
        return nodePanel;
    }
    
    @Override
    protected SectionNodeInnerPanel createNodeInnerPanel() {
        SectionNodeInnerPanel innerPanel = super.createNodeInnerPanel();
        return innerPanel;
    }
    
    void checkChildren(final CommonDDBean focusBean) {
        Mutex.EVENT.readAccess(new Runnable() {
            public void run() {
                doCheck = true;
                if (setChecking(true)) {
                    try {
                        while (doCheck) {
                            doCheck = false;
                            check(focusBean);
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
    
    private void check(final CommonDDBean focusBean) {
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
                // !FIXME Hack this for port-info until I refactor how naming of nodes works.
                if(b1 instanceof PortInfo && b2 instanceof PortInfo) {
                    return PortInfoNode.generateTitle((PortInfo) b1).compareTo(
                            PortInfoNode.generateTitle((PortInfo) b2));
                } else {
                    return Utils.getBeanDisplayName(b1, beanNameProperty).compareTo(
                            Utils.getBeanDisplayName(b2, beanNameProperty));
                }
            }
        });
        
        SectionNode focusNode = null;
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
            if(bean == focusBean) {
                focusNode = node;
            }
        }
        if (dirty) {
            children.remove(nodes);
            children.add(newNodes);
            populateBoxPanel();
        }
        
        if(focusBean != null && focusNode != null) {
            SectionNodePanel nodePanel = focusNode.getSectionNodePanel();
            nodePanel.open();
            nodePanel.scroll();
            nodePanel.setActive(true);
        }
    }
    
    public int getNewBeanId() {
        return newBeanId.getAndIncrement();
    }
    
    public final class AddBeanAction extends AbstractAction {
        
        public AddBeanAction(String actionText) {
            super(actionText);
//            char mnem = NbBundle.getMessage(PortInfoGroupNode.class, "MNE_Add" + resourceBase).charAt(0);
//            putValue(MNEMONIC_KEY, Integer.valueOf(mnem));
        }
        
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            SectionNodeView view = getSectionNodeView();
            if(view instanceof DDSectionNodeView) {
                XmlMultiViewDataObject dObj = ((DDSectionNodeView) view).getDataObject();
                if(dObj instanceof SunDescriptorDataObject) {
                    SunDescriptorDataObject sunDO = (SunDescriptorDataObject) dObj;
                    sunDO.modelUpdatedFromUI();
                    // dataObject.setChangedFromUI(true);
                    
                    CommonDDBean newBean = addNewBean();
                    checkChildren(newBean);
                }
            }
        }
    }
    
}
