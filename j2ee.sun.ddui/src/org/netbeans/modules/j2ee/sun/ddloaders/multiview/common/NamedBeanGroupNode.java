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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion;
import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.common.EjbRef;
import org.netbeans.modules.j2ee.sun.dd.api.common.ResourceEnvRef;
import org.netbeans.modules.j2ee.sun.dd.api.common.ResourceRef;
import org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping;
import org.netbeans.modules.j2ee.sun.dd.api.common.ServiceRef;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.sun.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.sun.ddloaders.SunDescriptorDataObject;
import org.netbeans.modules.j2ee.sun.ddloaders.Utils;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.BaseSectionNode;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.DDSectionNodeView;
import org.netbeans.modules.j2ee.sun.share.configbean.SunONEDeploymentConfiguration;
import org.netbeans.modules.xml.multiview.SectionNode;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataObject;
import org.netbeans.modules.xml.multiview.ui.BoxPanel;
import org.netbeans.modules.xml.multiview.ui.SectionNodeInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionNodePanel;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Mutex;


/**
 * @author Peter Williams
 */
public abstract class NamedBeanGroupNode extends BaseSectionNode implements BeanResolver {

    public static final String STANDARD_SERVLET_NAME = Servlet.SERVLET_NAME; // e.g. "ServletName"
    public static final String STANDARD_EJB_NAME = Ejb.EJB_NAME; // e.g. "EjbName"
    public static final String STANDARD_EJB_REF_NAME = EjbRef.EJB_REF_NAME; // e.g. "EjbRefName"
    public static final String STANDARD_RES_REF_NAME = ResourceRef.RES_REF_NAME; // e.g. "ResourceRefName"
    public static final String STANDARD_RESOURCE_ENV_REF_NAME = ResourceEnvRef.RESOURCE_ENV_REF_NAME; // e.g. "ResourceEnvRefName"
    public static final String STANDARD_SERVICE_REF_NAME = ServiceRef.SERVICE_REF_NAME; // e.g. "ServiceRefName"
    public static final String STANDARD_ROLE_NAME = SecurityRoleMapping.ROLE_NAME; // e.g. "RoleName"
    
    protected CommonDDBean commonDD;
    private String beanNameProperty;
    private AddBeanAction addBeanAction;
    
    private volatile boolean doCheck = false;
    private volatile boolean checking = false;
//    private volatile boolean modelInitialized = false;

    private static AtomicInteger newBeanId = new AtomicInteger(1);
    
    public NamedBeanGroupNode(SectionNodeView sectionNodeView, CommonDDBean commonDD,
            String beanNameProperty, String header, String iconBase,
            ASDDVersion version) {
        super(sectionNodeView, false, commonDD, version, header, iconBase);
        
        this.commonDD = commonDD;
        this.beanNameProperty = beanNameProperty;
        
        setExpanded(true);
    }
    
//    @Override
//    public void refreshSubtree() {
//        if(!modelInitialized) {
//            checkChildren(null);
//            modelInitialized = true;
//        }
//        
//        super.refreshSubtree();
//    }    

    /** Expected to be called from derived class constructor, if needed.
     */
    protected void enableAddAction(String addActionTitle) {
        addBeanAction = new AddBeanAction(addActionTitle);
    }
    
    protected abstract SectionNode createNode(DDBinding binding);
    
    protected abstract CommonDDBean [] getBeansFromModel();
    
    protected abstract org.netbeans.modules.j2ee.dd.api.common.CommonDDBean [] getStandardBeansFromModel();

    protected abstract CommonDDBean addNewBean();

    protected abstract CommonDDBean addBean(CommonDDBean newBean);
    
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
    
    public void checkChildren(final CommonDDBean focusBean) {
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
    
    // !PW FIXME was private, change back soon
    protected synchronized boolean setChecking(boolean value) {
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
    
    protected void check(final CommonDDBean focusBean) {
        Map<Object, Node> nodeMap = new HashMap<Object, Node>();
        Children children = getChildren();
        Node[] nodes = children.getNodes();
        for (int i = 0; i < nodes.length; i++) {
            Node node = nodes[i];
            nodeMap.put(((SectionNode) node).getKey(), node);
        }
       
        // Get the raw data
        CommonDDBean [] sunBeans = getBeansFromModel();
        Map<String, org.netbeans.modules.j2ee.dd.api.common.CommonDDBean> stdBeanMap = getBeanMap(getStandardBeansFromModel());
//        List<XPathNode> data = getAnnotationModel();
//        for(XPathNode node: data) {
//            System.out.println("Annotation: " + node.getXPath() + " = " + node.getValue());
//        }

        SortedSet<DDBinding> bindingDataSet = new TreeSet<DDBinding>();

        // Match up like names
        for(CommonDDBean sunBean: sunBeans) {
            String name = getBeanName(sunBean);
            name = (name != null) ? name.trim() : name;
            org.netbeans.modules.j2ee.dd.api.common.CommonDDBean stdBean = stdBeanMap.get(name);
            if(stdBean != null) {
                stdBeanMap.remove(name);
            }
            DDBinding binding = new DDBinding(this, sunBean, stdBean, null);
            bindingDataSet.add(binding);
            System.out.println(binding.toString());
        }
        
        // Add dummy entries for all unmatched standard servlets (unmatched sun servlets were added previous step)
        Iterator<Map.Entry<String, org.netbeans.modules.j2ee.dd.api.common.CommonDDBean>> entryIter = stdBeanMap.entrySet().iterator();
        while(entryIter.hasNext()) {
            Map.Entry<String, org.netbeans.modules.j2ee.dd.api.common.CommonDDBean> entry = entryIter.next();
            org.netbeans.modules.j2ee.dd.api.common.CommonDDBean stdBean = entry.getValue();
            CommonDDBean newSunBean = createBean();
            setBeanName(newSunBean, getBeanName(stdBean));

            // !PW FIXME Look up prior virtual key in nodeMap here?  See below.

            DDBinding binding = new DDBinding(this, newSunBean, stdBean, null, true);
            bindingDataSet.add(binding);
            System.out.println(binding.toString());
        }

        // !PW FIXME Mix annotations into previous calculations if we had them (none for servlet, but what about @RunAs?)
        // Possibly only consider annotations on bound servlets?  Then we can look at specific servlet class for annotations
        // using <servlet-class> field in standard descriptor.
        
        // How to match virtual servlets from prior pass with virtual servlets from this pass?
        // Currently their keys will always be created new.  Can we look them up?
        
        SectionNode focusNode = null;
        boolean dirty = nodes.length != bindingDataSet.size();
        List<Node> newNodeList = new ArrayList(bindingDataSet.size());
        
        int index = 0;
        Iterator<DDBinding> setIter = bindingDataSet.iterator();
        while(setIter.hasNext()) {
            DDBinding binding = setIter.next();
            SectionNode node = (SectionNode) nodeMap.get(binding.getSunBean());
            if(node == null) {
                node = createNode(binding);
                dirty = true;
            }
            newNodeList.add(node);
            if(!dirty) {
                dirty = ((SectionNode) nodes[index]).getKey() != node.getKey();
            }
            if(binding.getSunBean() == focusBean) {
                focusNode = node;
            }
            index++;
        }
        
        if (dirty) {
            Node [] newNodes = newNodeList.toArray(new Node[newNodeList.size()]);
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
    
    protected Map<String, org.netbeans.modules.j2ee.dd.api.common.CommonDDBean> 
            getBeanMap(org.netbeans.modules.j2ee.dd.api.common.CommonDDBean [] stdBeans) {
        Map<String, org.netbeans.modules.j2ee.dd.api.common.CommonDDBean> stdBeanMap =
                new HashMap<String, org.netbeans.modules.j2ee.dd.api.common.CommonDDBean>(stdBeans.length*2+3);
        
        for(org.netbeans.modules.j2ee.dd.api.common.CommonDDBean stdBean: stdBeans) {
            String name = getBeanName(stdBean);
            // Ignore unnamed standard beans -- we don't know what to match them with
            // and they are illegal anyway.
            name = (name != null) ? name.trim() : name;
            if(Utils.notEmpty(name)) {
                org.netbeans.modules.j2ee.dd.api.common.CommonDDBean oldBean = stdBeanMap.put(name, stdBean);
                if(oldBean != null) {
                    System.out.println("Ack! duplicate standard names!!!");
                }
            }
        }
        
        return stdBeanMap;
    }
    
    protected <T> MetadataModel<T> getMetadataModel(Class<T> type) {
        MetadataModel<T> metadataModel = null;
        SectionNodeView view = getSectionNodeView();
        XmlMultiViewDataObject dObj = view.getDataObject();
        SunONEDeploymentConfiguration dc = SunONEDeploymentConfiguration.getConfiguration(FileUtil.toFile(dObj.getPrimaryFile()));
        if(dc != null) {
            metadataModel = dc.getMetadataModel(type);
        }
        return metadataModel;
    }

    protected org.netbeans.modules.j2ee.dd.api.common.RootInterface getStandardRootDD() {
        org.netbeans.modules.j2ee.dd.api.common.RootInterface stdRootDD = null;
        SectionNodeView view = getSectionNodeView();
        XmlMultiViewDataObject dObj = view.getDataObject();
        SunONEDeploymentConfiguration dc = SunONEDeploymentConfiguration.getConfiguration(FileUtil.toFile(dObj.getPrimaryFile()));
        if(dc != null) {
            stdRootDD = dc.getStandardRootDD();
        }
        return stdRootDD;
    }

    protected org.netbeans.modules.j2ee.dd.api.common.RootInterface getWebServicesRootDD() {
        org.netbeans.modules.j2ee.dd.api.common.RootInterface wsRootDD = null;
        SectionNodeView view = getSectionNodeView();
        XmlMultiViewDataObject dObj = view.getDataObject();
        SunONEDeploymentConfiguration dc = SunONEDeploymentConfiguration.getConfiguration(FileUtil.toFile(dObj.getPrimaryFile()));
        if(dc != null) {
            wsRootDD = dc.getWebServicesRootDD();
        }
        return wsRootDD;
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
    
//    public List<XPathNode> getAnnotationModel() {
//        return Collections.EMPTY_LIST;
//    }
//    
//    public static class XPathNode {
//        
//        private final String path;
//        private final String value;
//        
//        public XPathNode(String path, String value) {
//            this.path = path;
//            this.value = value;
//        }
//        
//        public String getXPath() {
//            return path;
//        }
//        
//        public String getValue() {
//            return value;
//        }
//    }
    
}
