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
package org.netbeans.modules.j2ee.ddloaders.multiview;

import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.xml.multiview.SectionNode;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.openide.nodes.Node;
import org.openide.nodes.Children;

/**
 * @author pfiala
 */
class EjbJarView extends SectionNodeView {

    protected EnterpriseBeansNode enterpriseBeansNode;
    protected EnterpriseBeans enterpriseBeans;
    protected EjbJar ejbJar;

    EjbJarView(EjbJarMultiViewDataObject dataObject) {
        super(dataObject);
        EjbSectionNode rootNode = new EjbSectionNode(this, this, Utils.getBundleMessage("LBL_Overview"),
                Utils.ICON_BASE_DD_VALID);
        ejbJar = dataObject.getEjbJar();
        rootNode.addChild(new EjbJarDetailsNode(this, ejbJar));
        setRootNode(rootNode);
    }

    public void dataModelPropertyChange(Object source, String propertyName, Object oldValue, Object newValue) {
        if (oldValue instanceof EnterpriseBeans || newValue instanceof EnterpriseBeans) {
            scheduleRefreshView();
        }
        super.dataModelPropertyChange(source, propertyName, oldValue, newValue);
    }

    public void refreshView() {
        checkEnterpriseBeans();
        super.refreshView();
    }

    private void checkEnterpriseBeans() {
        EnterpriseBeans enterpriseBeans = ejbJar.getEnterpriseBeans();
        if (enterpriseBeans != this.enterpriseBeans) {
            SectionNode rootNode = getRootNode();
            final Children children = rootNode.getChildren();
            final Node[] nodes = children.getNodes();
            for (int i = 0; i < nodes.length; i++) {
                Node node = nodes[i];
                if (node instanceof EnterpriseBeansNode) {
                    children.remove(new Node[]{node});
                }
            }
            if (enterpriseBeans != null) {
                enterpriseBeansNode = new EnterpriseBeansNode(this, enterpriseBeans);
                if (rootNode != null) {
                    rootNode.addChild(enterpriseBeansNode);
                    rootNode.populateBoxPanel();
                }
            }
            this.enterpriseBeans = enterpriseBeans;
        }
    }
}
